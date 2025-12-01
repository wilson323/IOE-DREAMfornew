package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.RefundRecordDao;
import net.lab1024.sa.consume.domain.dto.RefundQueryDTO;
import net.lab1024.sa.consume.domain.dto.RefundRequestDTO;
import net.lab1024.sa.consume.domain.dto.RefundResultDTO;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.domain.entity.RefundRecordEntity;
import net.lab1024.sa.consume.domain.enums.RefundStatusEnum;
import net.lab1024.sa.consume.manager.HeartBeatManager;
import net.lab1024.sa.consume.manager.RefundManager;
import net.lab1024.sa.consume.service.RefundService;

/**
 * 退款服务实现类
 * 负责处理退款申请、审核、执行等流程
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Service
public class RefundServiceImpl implements RefundService {

    @Resource
    private RefundManager refundManager;

    @Resource
    private RefundRecordDao refundRecordDao;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private HeartBeatManager heartBeatManager;

    /**
     * 创建退款申请
     *
     * @param refundRequest 退款申请
     * @return 退款结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<RefundResultDTO> createRefund(@Valid RefundRequestDTO refundRequest) {
        try {
            log.info("开始创建退款申请: 用户ID={}, 消费记录ID={}, 退款金额={}",
                    refundRequest.getUserId(), refundRequest.getConsumeRecordId(), refundRequest.getRefundAmount());

            // 1. 验证退款申请
            String validationResult = validateRefundRequest(refundRequest);
            if (validationResult != null) {
                log.warn("退款申请验证失败: {}", validationResult);
                return ResponseDTO.userErrorParam( validationResult);
            }

            // 2. 查询原始消费记录
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectById(refundRequest.getConsumeRecordId());
            if (consumeRecord == null) {
                return ResponseDTO.userErrorParam( "原始消费记录不存在");
            }

            // 3. 创建退款记录
            RefundRecordEntity refundRecord = createRefundRecord(refundRequest, consumeRecord);
            refundRecordDao.insert(refundRecord);

            // 4. 处理退款流程
            RefundResultDTO result = processRefund(refundRecord, consumeRecord);

            log.info("退款申请创建完成: 退款单号={}, 状态={}", result.getRefundNo() != null ? result.getRefundNo() : refundRecord.getRefundId(), result.getStatus());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("创建退款申请失败", e);
            return ResponseDTO.error("创建退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 审核退款申请
     *
     * @param refundId    退款记录ID
     * @param approved    是否通过
     * @param auditRemark 审核备注
     * @return 审核结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> auditRefund(Long refundId, boolean approved, String auditRemark) {
        try {
            log.info("开始审核退款申请: 退款ID={}, 审核结果={}", refundId, approved ? "通过" : "拒绝");

            // 1. 查询退款记录
            RefundRecordEntity refundRecord = refundRecordDao.selectById(refundId);
            if (refundRecord == null) {
                return ResponseDTO.userErrorParam( "退款记录不存在");
            }

            // 2. 检查退款状态（status是String类型，需要转换为枚举判断）
            RefundStatusEnum currentStatus = getRefundStatusEnum(refundRecord.getStatus());
            if (currentStatus == null || currentStatus != RefundStatusEnum.PENDING) {
                return ResponseDTO.userErrorParam( "退款状态不允许审核，只有待处理状态的退款可以审核");
            }

            // 3. 更新审核状态
            if (approved) {
                // 审核通过，状态改为处理中，然后执行退款
                refundRecord.setStatus(String.valueOf(RefundStatusEnum.PROCESSING.getValue()));
                refundRecord.setProcessTime(LocalDateTime.now());
                refundRecord.setOperatorName("系统审核");
                refundRecord.setRemark(auditRemark != null ? auditRemark : "审核通过");
                refundRecordDao.updateById(refundRecord);

                // 4. 执行退款
                executeRefund(refundRecord);

                log.info("退款审核通过，执行退款完成: 退款单号={}", refundRecord.getRefundId());
            } else {
                // 审核拒绝，状态改为失败
                refundRecord.setStatus(String.valueOf(RefundStatusEnum.FAILED.getValue()));
                refundRecord.setProcessTime(LocalDateTime.now());
                refundRecord.setOperatorName("系统审核");
                refundRecord.setRemark(auditRemark != null ? auditRemark : "审核拒绝");
                refundRecordDao.updateById(refundRecord);

                log.info("退款审核拒绝: 退款单号={}", refundRecord.getRefundId());
            }

            return ResponseDTO.ok("审核完成");

        } catch (Exception e) {
            log.error("审核退款申请失败: 退款ID={}", refundId, e);
            return ResponseDTO.error("审核退款申请失败");
        }
    }

    /**
     * 查询退款记录
     *
     * @param queryDTO 查询条件
     * @return 退款记录分页
     */
    @Override
    public ResponseDTO<Page<RefundRecordEntity>> queryRefundRecords(RefundQueryDTO queryDTO) {
        try {
            log.info("查询退款记录: 用户ID={}, 状态={}", queryDTO.getUserId(), queryDTO.getStatus());

            Pageable pageable = PageRequest.of(
                    queryDTO.getPageNum() - 1,
                    queryDTO.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createTime"));

            Page<RefundRecordEntity> records = refundManager.queryRefundRecords(queryDTO, pageable);

            log.info("查询退款记录完成: 总数={}", records.getTotalElements());
            return ResponseDTO.ok(records);

        } catch (Exception e) {
            log.error("查询退款记录失败", e);
            return ResponseDTO.error("查询退款记录失败");
        }
    }

    /**
     * 获取退款统计
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    @Override
    public ResponseDTO<Map<String, Object>> getRefundStatistics(Long userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        try {
            log.info("获取退款统计: 用户ID={}, 开始日期={}, 结束日期={}", userId, startDate, endDate);

            Map<String, Object> statistics = refundManager.getRefundStatistics(userId, startDate, endDate);

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("获取退款统计失败", e);
            return ResponseDTO.error("获取退款统计失败");
        }
    }

    /**
     * 批量审核退款
     *
     * @param refundIds   退款记录ID列表
     * @param approved    是否通过
     * @param auditRemark 审核备注
     * @return 批量审核结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> batchAuditRefund(List<Long> refundIds, boolean approved,
            String auditRemark) {
        try {
            log.info("开始批量审核退款: 数量={}, 审核结果={}", refundIds.size(), approved ? "通过" : "拒绝");

            Map<String, Object> result = new HashMap<>();
            int successCount = 0;
            int failureCount = 0;

            for (Long refundId : refundIds) {
                try {
                    ResponseDTO<String> auditResult = auditRefund(refundId, approved, auditRemark);
                    if (auditResult.getCode() == 200) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error("批量审核退款失败: 退款ID={}", refundId, e);
                    failureCount++;
                }
            }

            result.put("totalCount", refundIds.size());
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);

            log.info("批量审核退款完成: 总数={}, 成功={}, 失败={}", refundIds.size(), successCount, failureCount);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("批量审核退款失败", e);
            return ResponseDTO.error("批量审核退款失败");
        }
    }

    /**
     * 获取退款详情
     *
     * @param refundId 退款记录ID
     * @return 退款详情
     */
    @Override
    public ResponseDTO<RefundRecordEntity> getRefundDetail(Long refundId) {
        try {
            RefundRecordEntity refundRecord = refundRecordDao.selectById(refundId);
            if (refundRecord == null) {
                return ResponseDTO.userErrorParam( "退款记录不存在");
            }

            // 查询原始消费记录信息
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectById(refundRecord.getConsumeRecordId());
            if (consumeRecord != null) {
                // 设置消费记录信息到退款记录中（可选）
                // refundRecord.setConsumeRecord(consumeRecord);
            }

            return ResponseDTO.ok(refundRecord);

        } catch (Exception e) {
            log.error("获取退款详情失败: 退款ID={}", refundId, e);
            return ResponseDTO.error("获取退款详情失败");
        }
    }

    /**
     * 根据退款单号获取退款记录
     *
     * @param refundNo 退款单号（refundId字段）
     * @return 退款记录
     */
    @Override
    public RefundRecordEntity getRefundByNo(String refundNo) {
        try {
            log.info("根据退款单号查询退款记录: refundNo={}", refundNo);
            return refundRecordDao.selectByRefundId(refundNo);
        } catch (Exception e) {
            log.error("根据退款单号查询退款记录失败: refundNo={}", refundNo, e);
            return null;
        }
    }

    /**
     * 取消退款申请
     *
     * @param refundNo 退款单号
     * @return 取消结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> cancelRefund(String refundNo) {
        try {
            log.info("开始取消退款申请: refundNo={}", refundNo);

            // 1. 查询退款记录
            RefundRecordEntity refundRecord = refundRecordDao.selectByRefundId(refundNo);
            if (refundRecord == null) {
                return ResponseDTO.userErrorParam( "退款记录不存在");
            }

            // 2. 检查退款状态是否允许取消
            RefundStatusEnum currentStatus = getRefundStatusEnum(refundRecord.getStatus());
            if (currentStatus == null || !currentStatus.canCancel()) {
                return ResponseDTO.userErrorParam(
                        "当前退款状态不允许取消，只能取消待处理或退款中的申请");
            }

            // 3. 更新退款状态为已取消
            refundRecord.setStatus(String.valueOf(RefundStatusEnum.CANCELLED.getValue()));
            refundRecord.setProcessTime(LocalDateTime.now());
            refundRecord.setRemark("用户主动取消退款申请");
            refundRecordDao.updateById(refundRecord);

            log.info("取消退款申请成功: refundNo={}", refundNo);
            return ResponseDTO.ok("取消退款申请成功");

        } catch (Exception e) {
            log.error("取消退款申请失败: refundNo={}", refundNo, e);
            return ResponseDTO.error("取消退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 重新提交退款申请
     *
     * @param refundNo       原退款单号
     * @param refundRequest  退款申请
     * @return 退款结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<RefundResultDTO> resubmitRefund(String refundNo, RefundRequestDTO refundRequest) {
        try {
            log.info("开始重新提交退款申请: refundNo={}, consumeRecordId={}", refundNo,
                    refundRequest.getConsumeRecordId());

            // 1. 查询原退款记录
            RefundRecordEntity originalRefund = refundRecordDao.selectByRefundId(refundNo);
            if (originalRefund == null) {
                return ResponseDTO.userErrorParam( "原退款记录不存在");
            }

            // 2. 检查原退款状态是否允许重新提交（只有已取消或失败的退款可以重新提交）
            RefundStatusEnum originalStatus = getRefundStatusEnum(originalRefund.getStatus());
            if (originalStatus == null || (originalStatus != RefundStatusEnum.CANCELLED
                    && originalStatus != RefundStatusEnum.FAILED)) {
                return ResponseDTO.userErrorParam(
                        "当前退款状态不允许重新提交，只能重新提交已取消或失败的退款");
            }

            // 3. 验证退款申请
            String validationResult = validateRefundRequest(refundRequest);
            if (validationResult != null) {
                log.warn("退款申请验证失败: {}", validationResult);
                return ResponseDTO.userErrorParam( validationResult);
            }

            // 4. 查询原始消费记录
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectById(refundRequest.getConsumeRecordId());
            if (consumeRecord == null) {
                return ResponseDTO.userErrorParam( "原始消费记录不存在");
            }

            // 5. 创建新的退款记录
            RefundRecordEntity newRefundRecord = createRefundRecord(refundRequest, consumeRecord);
            newRefundRecord.setRemark("重新提交退款申请，原退款单号: " + refundNo);
            refundRecordDao.insert(newRefundRecord);

            // 6. 处理退款流程
            RefundResultDTO result = processRefund(newRefundRecord, consumeRecord);

            log.info("重新提交退款申请完成: 新退款单号={}, 状态={}", result.getRefundNo(), result.getStatus());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("重新提交退款申请失败: refundNo={}", refundNo, e);
            return ResponseDTO.error("重新提交退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取退款状态统计
     *
     * @return 状态统计数据
     */
    @Override
    public ResponseDTO<Map<String, Object>> getRefundStatusStatistics() {
        try {
            log.info("获取退款状态统计");

            Map<String, Object> statistics = new HashMap<>();

            // 统计各状态的退款数量
            Long pendingCount = refundRecordDao.countByStatus(String.valueOf(RefundStatusEnum.PENDING.getValue()));
            Long processingCount = refundRecordDao
                    .countByStatus(String.valueOf(RefundStatusEnum.PROCESSING.getValue()));
            Long successCount = refundRecordDao.countByStatus(String.valueOf(RefundStatusEnum.SUCCESS.getValue()));
            Long failedCount = refundRecordDao.countByStatus(String.valueOf(RefundStatusEnum.FAILED.getValue()));
            Long cancelledCount = refundRecordDao
                    .countByStatus(String.valueOf(RefundStatusEnum.CANCELLED.getValue()));

            statistics.put("PENDING", pendingCount != null ? pendingCount : 0L);
            statistics.put("PROCESSING", processingCount != null ? processingCount : 0L);
            statistics.put("SUCCESS", successCount != null ? successCount : 0L);
            statistics.put("FAILED", failedCount != null ? failedCount : 0L);
            statistics.put("CANCELLED", cancelledCount != null ? cancelledCount : 0L);

            // 计算总数
            long total = (pendingCount != null ? pendingCount : 0L) +
                    (processingCount != null ? processingCount : 0L) +
                    (successCount != null ? successCount : 0L) +
                    (failedCount != null ? failedCount : 0L) +
                    (cancelledCount != null ? cancelledCount : 0L);
            statistics.put("TOTAL", total);

            log.info("退款状态统计完成: {}", statistics);
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("获取退款状态统计失败", e);
            return ResponseDTO.error("获取退款状态统计失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证退款申请
     */
    private String validateRefundRequest(RefundRequestDTO request) {
        // 验证退款金额
        if (request.getRefundAmount() == null || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "退款金额必须大于0";
        }

        // 验证消费记录ID
        if (request.getConsumeRecordId() == null || request.getConsumeRecordId() <= 0) {
            return "消费记录ID无效";
        }

        // 验证用户ID
        if (request.getUserId() == null || request.getUserId() <= 0) {
            return "用户ID无效";
        }

        // 验证退款原因
        if (request.getRefundReason() == null) {
            return "退款原因不能为空";
        }

        return null; // 验证通过
    }

    /**
     * 创建退款记录
     */
    private RefundRecordEntity createRefundRecord(RefundRequestDTO request, ConsumeRecordEntity consumeRecord) {
        RefundRecordEntity record = new RefundRecordEntity();

        // 基本信息
        record.setRefundId(generateRefundNo()); // refundId作为业务唯一标识（退款单号）
        record.setConsumeRecordId(request.getConsumeRecordId());
        record.setRefundAmount(request.getRefundAmount());
        // 退款原因：转换为字符串
        String reasonStr = request.getRefundReason() != null ? request.getRefundReason().toString() : "用户申请退款";
        record.setReason(reasonStr);
        record.setStatus(String.valueOf(RefundStatusEnum.PENDING.getValue())); // status是String类型

        // 时间信息
        record.setApplyTime(LocalDateTime.now());

        // 备注信息
        String remark = "退款申请";
        if (request.getRefundDescription() != null) {
            remark = request.getRefundDescription();
        }
        record.setRemark(remark);

        return record;
    }

    /**
     * 处理退款流程
     */
    private RefundResultDTO processRefund(RefundRecordEntity refundRecord, ConsumeRecordEntity consumeRecord) {
        RefundResultDTO result = new RefundResultDTO();

        result.setRefundNo(refundRecord.getRefundId()); // refundId作为退款单号
        result.setRefundAmount(refundRecord.getRefundAmount());
        result.setRefundReason(refundRecord.getReason());
        result.setStatus(refundRecord.getStatus());
        result.setApplyTime(refundRecord.getApplyTime());

        // 根据退款金额和原始消费记录判断处理方式
        if (refundRecord.getRefundAmount().compareTo(consumeRecord.getAmount()) >= 0) {
            // 全额退款
            result.setRefundType("FULL_REFUND");
            result.setMessage("全额退款申请已提交，等待审核");
        } else {
            // 部分退款
            result.setRefundType("PARTIAL_REFUND");
            result.setMessage("部分退款申请已提交，等待审核");
        }

        return result;
    }

    /**
     * 执行退款
     */
    @Transactional(rollbackFor = Exception.class)
    private void executeRefund(RefundRecordEntity refundRecord) {
        try {
            log.info("开始执行退款: 退款单号={}, 退款金额={}", refundRecord.getRefundId(), refundRecord.getRefundAmount());

            // 1. 查询消费记录获取用户ID
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectById(refundRecord.getConsumeRecordId());
            if (consumeRecord == null) {
                throw new RuntimeException("消费记录不存在");
            }

            // 2. 退还用户余额
            refundManager.refundUserBalance(consumeRecord.getPersonId(), refundRecord.getRefundAmount(),
                    refundRecord.getRefundId());

            // 3. 更新退款状态为成功
            refundRecord.setStatus(String.valueOf(RefundStatusEnum.SUCCESS.getValue()));
            refundRecord.setRefundTime(LocalDateTime.now());
            refundRecord.setProcessTime(LocalDateTime.now());
            refundRecordDao.updateById(refundRecord);

            // 4. 更新原始消费记录的退款状态
            updateConsumeRecordRefundStatus(refundRecord.getConsumeRecordId(), refundRecord.getRefundAmount());

            // 5. 发送退款成功通知
            sendRefundNotification(refundRecord);

            log.info("退款执行完成: 退款单号={}", refundRecord.getRefundId());

        } catch (Exception e) {
            log.error("执行退款失败: 退款单号={}", refundRecord.getRefundId(), e);

            // 更新退款状态为失败
            refundRecord.setStatus(String.valueOf(RefundStatusEnum.FAILED.getValue()));
            refundRecord.setRefundTime(LocalDateTime.now());
            refundRecord.setProcessTime(LocalDateTime.now());
            refundRecord.setRemark("退款失败: " + e.getMessage());
            refundRecordDao.updateById(refundRecord);

            throw e;
        }
    }

    /**
     * 更新消费记录退款状态
     */
    private void updateConsumeRecordRefundStatus(Long consumeRecordId, BigDecimal refundAmount) {
        try {
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectById(consumeRecordId);
            if (consumeRecord != null) {
                // 更新退款金额
                BigDecimal currentRefundAmount = consumeRecord.getRefundAmount() != null
                        ? consumeRecord.getRefundAmount()
                        : BigDecimal.ZERO;
                consumeRecord.setRefundAmount(currentRefundAmount.add(refundAmount));

                // 如果全额退款，更新状态
                if (consumeRecord.getRefundAmount().compareTo(consumeRecord.getAmount()) >= 0) {
                    consumeRecord.setStatus("REFUNDED");
                } else {
                    consumeRecord.setStatus("PARTIAL_REFUNDED");
                }

                consumeRecord.setRefundTime(LocalDateTime.now());
                consumeRecordDao.updateById(consumeRecord);
            }
        } catch (Exception e) {
            log.error("更新消费记录退款状态失败: 消费记录ID={}", consumeRecordId, e);
        }
    }

    /**
     * 发送退款成功通知
     */
    private void sendRefundNotification(RefundRecordEntity record) {
        try {
            // 查询消费记录获取用户ID
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectById(record.getConsumeRecordId());
            if (consumeRecord == null) {
                log.warn("发送退款通知失败: 消费记录不存在, consumeRecordId={}", record.getConsumeRecordId());
                return;
            }

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "REFUND_SUCCESS");
            notification.put("userId", consumeRecord.getPersonId());
            notification.put("refundAmount", record.getRefundAmount());
            notification.put("refundNo", record.getRefundId());
            notification.put("timestamp", System.currentTimeMillis());

            heartBeatManager.broadcastToUserDevices(consumeRecord.getPersonId(), notification);

        } catch (Exception e) {
            log.error("发送退款通知失败: 退款单号={}", record.getRefundId(), e);
        }
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "RF" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 将String类型的status转换为RefundStatusEnum
     *
     * @param statusStr 状态字符串
     * @return 退款状态枚举
     */
    private RefundStatusEnum getRefundStatusEnum(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        try {
            Integer statusValue = Integer.parseInt(statusStr);
            return RefundStatusEnum.getByValue(statusValue);
        } catch (NumberFormatException e) {
            log.warn("非法解析退款状态: {}", statusStr);
            return null;
        }
    }
}