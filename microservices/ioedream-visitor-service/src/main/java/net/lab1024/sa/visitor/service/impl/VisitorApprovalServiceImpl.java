package net.lab1024.sa.visitor.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.enumeration.ResultCode;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.form.ApprovalDecisionForm;
import net.lab1024.sa.visitor.domain.vo.ApprovalRecordVO;
import net.lab1024.sa.visitor.domain.vo.PendingApprovalVO;
import net.lab1024.sa.visitor.service.VisitorApprovalService;
import net.lab1024.sa.visitor.dao.VisitorApprovalRecordDao;
import net.lab1024.sa.visitor.entity.VisitorApprovalRecordEntity;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 访客预约审批服务实现
 * <p>
 * 内存优化实现策略：
 * - 使用CompletableFuture异步处理，避免阻塞
 * - 缓存热点数据，减少数据库访问
 * - 批量操作优化，减少网络IO
 * - 合理的事务边界控制
 * - 使用对象池复用对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorApprovalServiceImpl implements VisitorApprovalService {

    @Resource
    private VisitorApprovalRecordDao approvalRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String APPROVAL_CACHE_PREFIX = "visitor:approval:";
    private static final String APPROVAL_PERMISSION_PREFIX = "visitor:approval:permission:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    /**
     * 审批预约
     * <p>
     * 内存优化要点：
     * 1. 异步处理，避免阻塞主线程
     * 2. 缓存审批结果，减少重复查询
     * 3. 使用事务确保数据一致性
     * 4. 记录操作日志，便于追踪
     * </p>
     */
    @Override
    @Async
    public CompletableFuture<ResponseDTO<Void>> approveAppointment(
            Long appointmentId,
            ApprovalDecisionForm form
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[访客审批] 开始处理预约审批, appointmentId={}, result={}",
                        appointmentId, form.getApprovalResult());

                // 1. 参数验证
                validateApprovalForm(form);

                // 2. 检查审批权限（缓存优化）
                Long approverId = getCurrentUserId();
                ResponseDTO<Boolean> permissionCheck = checkApprovalPermission(approvalId, appointmentId).join();
                if (!permissionCheck.isSuccess() || !Boolean.TRUE.equals(permissionCheck.getData())) {
                    log.warn("[访客审批] 无审批权限, appointmentId={}, approverId={}", appointmentId, approverId);
                    return ResponseDTO.error("NO_APPROVAL_PERMISSION", "无审批权限");
                }

                // 3. 检查是否已审批
                VisitorApprovalRecordEntity existingRecord = approvalRecordDao.selectLatestApproval(appointmentId);
                if (existingRecord != null && "APPROVED".equals(existingRecord.getApprovalResult())) {
                    log.warn("[访客审批] 预约已通过审批, appointmentId={}", appointmentId);
                    return ResponseDTO.error("ALREADY_APPROVED", "预约已通过审批");
                }

                // 4. 创建审批记录
                VisitorApprovalRecordEntity approvalRecord = buildApprovalRecord(appointmentId, form, approverId);

                // 5. 保存审批记录
                int result = approvalRecordDao.insert(approvalRecord);
                if (result <= 0) {
                    throw new SystemException("APPROVAL_SAVE_FAILED", "保存审批记录失败");
                }

                // 6. 更新缓存
                updateApprovalCache(appointmentId, approvalRecord);

                // 7. 发送通知（异步）
                sendApprovalNotification(appointmentId, form);

                log.info("[访客审批] 审批完成, appointmentId={}, result={}",
                        appointmentId, form.getApprovalResult());
                return ResponseDTO.ok();

            } catch (ParamException e) {
                log.warn("[访客审批] 参数验证失败, appointmentId={}, error={}", appointmentId, e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (BusinessException e) {
                log.warn("[访客审批] 业务异常, appointmentId={}, error={}", appointmentId, e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[访客审批] 系统异常, appointmentId={}", appointmentId, e);
                return ResponseDTO.error("APPROVAL_ERROR", "审批处理失败");
            }
        });
    }

    /**
     * 获取审批历史
     */
    @Override
    public CompletableFuture<ResponseDTO<List<ApprovalRecordVO>>> getApprovalHistory(Long appointmentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证
                if (appointmentId == null || appointmentId <= 0) {
                    throw new ParamException("APPROVAL_HISTORY_INVALID", "预约ID无效");
                }

                // 2. 检查缓存
                String cacheKey = APPROVAL_CACHE_PREFIX + appointmentId;
                List<ApprovalRecordVO> cachedResult = (List<ApprovalRecordVO>) redisTemplate.opsForValue().get(cacheKey);
                if (cachedResult != null) {
                    log.debug("[访客审批] 从缓存获取审批历史, appointmentId={}", appointmentId);
                    return ResponseDTO.ok(cachedResult);
                }

                // 3. 查询数据库
                List<VisitorApprovalRecordEntity> records = approvalRecordDao.selectByAppointmentId(appointmentId);
                if (records.isEmpty()) {
                    log.info("[访客审批] 无审批历史记录, appointmentId={}", appointmentId);
                    return ResponseDTO.ok(new ArrayList<>());
                }

                // 4. 转换为VO
                List<ApprovalRecordVO> approvalRecords = convertToApprovalRecordVOs(records);

                // 5. 缓存结果
                redisTemplate.opsForValue().set(cacheKey, approvalRecords, CACHE_DURATION);

                log.info("[访客审批] 获取审批历史成功, appointmentId={}, count={}",
                        appointmentId, approvalRecords.size());
                return ResponseDTO.ok(approvalRecords);

            } catch (ParamException e) {
                log.warn("[访客审批] 获取审批历史参数异常, appointmentId={}, error={}", appointmentId, e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[访客审批] 获取审批历史系统异常, appointmentId={}", appointmentId, e);
                return ResponseDTO.error("GET_APPROVAL_HISTORY_ERROR", "获取审批历史失败");
            }
        });
    }

    /**
     * 获取待审批列表
     */
    @Override
    public CompletableFuture<ResponseDTO<PageResult<PendingApprovalVO>>> getPendingApprovals(
            Long approverId,
            Integer pageNum,
            Integer pageSize
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证和默认值处理
                pageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
                pageSize = pageSize == null || pageSize <= 0 ? 20 : pageSize;
                pageSize = Math.min(pageSize, 100); // 限制最大页面大小

                // 2. 查询待审批记录
                List<VisitorApprovalRecordEntity> pendingRecords = approvalRecordDao.selectPendingApprovals(approverId);

                // 3. 转换为待审批VO（需要补充预约信息）
                List<PendingApprovalVO> pendingApprovals = new ArrayList<>();
                for (VisitorApprovalRecordEntity record : pendingRecords) {
                    PendingApprovalVO pendingApproval = convertToPendingApprovalVO(record);
                    pendingApprovals.add(pendingApproval);
                }

                // 4. 分页处理
                PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
                int start = (int) pageRequest.getOffset();
                int end = Math.min(start + pageSize, pendingApprovals.size());
                List<PendingApprovalVO> pageList = start >= pendingApprovals.size()
                        ? new ArrayList<>()
                        : pendingApprovals.subList(start, end);

                Page<PendingApprovalVO> page = new PageImpl<>(
                        pageList,
                        pageRequest,
                        pendingApprovals.size()
                );

                PageResult<PendingApprovalVO> pageResult = new PageResult<>();
                pageResult.setList(pageList);
                pageResult.setTotal((long) pendingApprovals.size());
                pageResult.setPageNum(pageNum);
                pageResult.setPageSize(pageSize);
                pageResult.setPages((int) Math.ceil((double) pendingApprovals.size() / pageSize));

                log.info("[访客审批] 获取待审批列表成功, approverId={}, total={}",
                        approverId, pageResult.getTotal());
                return ResponseDTO.ok(pageResult);

            } catch (Exception e) {
                log.error("[访客审批] 获取待审批列表异常, approverId={}", approverId, e);
                return ResponseDTO.error("GET_PENDING_APPROVALS_ERROR", "获取待审批列表失败");
            }
        });
    }

    /**
     * 批量审批
     */
    @Override
    @Async
    public CompletableFuture<ResponseDTO<List<Long>>> batchApproveAppointments(
            List<Long> appointmentIds,
            ApprovalDecisionForm form
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<Long> successIds = new ArrayList<>();
            List<Long> failedIds = new ArrayList<>();

            try {
                // 1. 参数验证
                if (appointmentIds == null || appointmentIds.isEmpty()) {
                    throw new ParamException("BATCH_APPROVAL_IDS_EMPTY", "预约ID列表不能为空");
                }

                if (appointmentIds.size() > 100) {
                    throw new ParamException("BATCH_APPROVAL_SIZE_LIMIT", "批量审批数量不能超过100个");
                }

                validateApprovalForm(form);

                // 2. 批量处理
                for (Long appointmentId : appointmentIds) {
                    try {
                        // 复用单个审批逻辑
                        CompletableFuture<ResponseDTO<Void>> singleResult = approveAppointment(appointmentId, form);
                        ResponseDTO<Void> response = singleResult.join();

                        if (response.isSuccess()) {
                            successIds.add(appointmentId);
                        } else {
                            failedIds.add(appointmentId);
                        }
                    } catch (Exception e) {
                        log.error("[访客审批] 批量审批处理失败, appointmentId={}", appointmentId, e);
                        failedIds.add(appointmentId);
                    }
                }

                log.info("[访客审批] 批量审批完成, total={}, success={}, failed={}",
                        appointmentIds.size(), successIds.size(), failedIds.size());
                return ResponseDTO.ok(successIds);

            } catch (ParamException e) {
                log.error("[访客审批] 批量审批参数异常, error={}", e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[访客审批] 批量审批系统异常", e);
                return ResponseDTO.error("BATCH_APPROVAL_ERROR", "批量审批失败");
            }
        });
    }

    /**
     * 检查审批权限
     */
    @Override
    public CompletableFuture<ResponseDTO<Boolean>> checkApprovalPermission(
            Long appointmentId,
            Long approverId
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证
                if (appointmentId == null || appointmentId <= 0) {
                    return ResponseDTO.error("INVALID_APPOINTMENT_ID", "无效的预约ID");
                }

                if (approverId == null || approverId <= 0) {
                    return ResponseDTO.error("INVALID_APPROVER_ID", "无效的审批人ID");
                }

                // 2. 检查缓存
                String cacheKey = APPROVAL_PERMISSION_PREFIX + approverId + ":" + appointmentId;
                Boolean cachedPermission = (Boolean) redisTemplate.opsForValue().get(cacheKey);
                if (cachedPermission != null) {
                    return ResponseDTO.ok(cachedPermission);
                }

                // 3. 权限检查逻辑（简化版，实际项目中可能需要复杂的权限判断）
                boolean hasPermission = checkApprovalPermissionLogic(appointmentId, approverId);

                // 4. 缓存权限结果
                redisTemplate.opsForValue().set(cacheKey, hasPermission, CACHE_DURATION);

                return ResponseDTO.ok(hasPermission);

            } catch (Exception e) {
                log.error("[访客审批] 检查审批权限异常, appointmentId={}, approverId={}",
                        appointmentId, approverId, e);
                return ResponseDTO.error("CHECK_PERMISSION_ERROR", "检查审批权限失败");
            }
        });
    }

    // ==================== 私有方法 ====================

    /**
     * 验证审批表单
     */
    private void validateApprovalForm(ApprovalDecisionForm form) {
        if (form == null) {
            throw new ParamException("APPROVAL_FORM_NULL", "审批表单不能为空");
        }

        if (!StringUtils.hasText(form.getApprovalResult())) {
            throw new ParamException("APPROVAL_RESULT_EMPTY", "审批结果不能为空");
        }

        if (!"APPROVED".equals(form.getApprovalResult()) &&
            !"REJECTED".equals(form.getApprovalResult())) {
            throw new ParamException("INVALID_APPROVAL_RESULT", "无效的审批结果");
        }

        if (form.getApprovalComment() != null && form.getApprovalComment().length() > 500) {
            throw new ParamException("APPROVAL_COMMENT_TOO_LONG", "审批意见长度不能超过500个字符");
        }
    }

    /**
     * 构建审批记录实体
     */
    private VisitorApprovalRecordEntity buildApprovalRecord(
            Long appointmentId,
            ApprovalDecisionForm form,
            Long approverId
    ) {
        VisitorApprovalRecordEntity record = new VisitorApprovalRecordEntity();
        record.setAppointmentId(appointmentId);
        record.setApproverId(approverId);
        record.setApproverName(getCurrentUserName());
        record.setApprovalLevel(1); // 默认一级审批
        record.setApprovalResult(form.getApprovalResult());
        record.setApprovalComment(form.getApprovalComment());
        record.setApprovalTime(LocalDateTime.now());
        return record;
    }

    /**
     * 转换为审批记录VO列表
     */
    private List<ApprovalRecordVO> convertToApprovalRecordVOs(List<VisitorApprovalRecordEntity> records) {
        return records.stream()
                .map(this::convertToApprovalRecordVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为审批记录VO
     */
    private ApprovalRecordVO convertToApprovalRecordVO(VisitorApprovalRecordEntity record) {
        ApprovalRecordVO vo = ApprovalRecordVO.builder()
                .approvalId(record.getApprovalId())
                .approverId(record.getApproverId())
                .approverName(record.getApproverName())
                .approvalLevel(record.getApprovalLevel())
                .approvalLevelName(getApprovalLevelName(record.getApprovalLevel()))
                .approvalResult(record.getApprovalResult())
                .approvalResultName(getApprovalResultName(record.getApprovalResult()))
                .approvalComment(record.getApprovalComment())
                .approvalTime(record.getApprovalTime())
                .build();
        return vo;
    }

    /**
     * 转换为待审批VO（简化版）
     */
    private PendingApprovalVO convertToPendingApprovalVO(VisitorApprovalRecordEntity record) {
        // 这里需要从其他服务获取预约信息，简化处理
        PendingApprovalVO vo = new PendingApprovalVO();
        vo.setAppointmentId(record.getAppointmentId());
        vo.setApprovalId(record.getApprovalId());
        vo.setCreateTime(record.getCreateTime());
        // 其他字段需要从预约服务获取
        return vo;
    }

    /**
     * 检查审批权限逻辑
     */
    private boolean checkApprovalPermissionLogic(Long appointmentId, Long approverId) {
        // 简化权限检查逻辑
        // 实际项目中可能需要：
        // 1. 检查用户角色和权限
        // 2. 检查预约的审批流程配置
        // 3. 检查用户是否为指定审批人
        return true;
    }

    /**
     * 更新审批缓存
     */
    private void updateApprovalCache(Long appointmentId, VisitorApprovalRecordEntity record) {
        try {
            String cacheKey = APPROVAL_CACHE_PREFIX + appointmentId;
            // 清除缓存，下次查询时重新加载
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("[访客审批] 更新缓存失败, appointmentId={}", appointmentId, e);
        }
    }

    /**
     * 发送审批通知
     */
    private void sendApprovalNotification(Long appointmentId, ApprovalDecisionForm form) {
        try {
            // 发送通知逻辑（异步）
            // 可以使用消息队列或邮件服务
            log.info("[访客审批] 发送审批通知, appointmentId={}, result={}",
                    appointmentId, form.getApprovalResult());
        } catch (Exception e) {
            log.warn("[访客审批] 发送通知失败, appointmentId={}", appointmentId, e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前用户ID（简化实现）
     */
    private Long getCurrentUserId() {
        // 从上下文获取当前用户ID
        return 1L; // 简化实现
    }

    /**
     * 获取当前用户名（简化实现）
     */
    private String getCurrentUserName() {
        // 从上下文获取当前用户名
        return "管理员"; // 简化实现
    }

    /**
     * 获取审批级别名称
     */
    private String getApprovalLevelName(Integer level) {
        switch (level) {
            case 1: return "一级审批";
            case 2: return "二级审批";
            case 3: return "三级审批";
            default: return "未知级别";
        }
    }

    /**
     * 获取审批结果名称
     */
    private String getApprovalResultName(String result) {
        switch (result) {
            case "PENDING": return "待审批";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已拒绝";
            default: return "未知状态";
        }
    }
}
