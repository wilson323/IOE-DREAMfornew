package net.lab1024.sa.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.dao.RefundApplicationDao;
import net.lab1024.sa.consume.entity.RefundApplicationEntity;
import net.lab1024.sa.consume.domain.form.RefundQueryForm;
import net.lab1024.sa.consume.domain.form.RefundRequestForm;
import net.lab1024.sa.consume.domain.vo.RefundRecordVO;
import net.lab1024.sa.consume.service.AccountService;
import net.lab1024.sa.consume.service.ConsumeRefundService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消费退款服务实现类
 * <p>
 * 实现完整的退款业务功能
 * 严格遵循四层架构规范和事务管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeRefundServiceImpl implements ConsumeRefundService {

    @Resource
    private RefundApplicationDao refundApplicationDao;

    @Resource
    private ConsumeTransactionDao consumeTransactionDao;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private AccountService accountService;

    /**
     * 申请退款
     * <p>
     * 实现完整的退款申请流程：
     * 1. 验证交易记录是否存在
     * 2. 验证退款金额是否合法
     * 3. 检查是否已有退款记录
     * 4. 创建退款记录
     * </p>
     *
     * @param refundRequest 退款申请表单
     * @return 退款ID
     */
    @Override
    @Observed(name = "consume.refund.applyRefund", contextualName = "consume-refund-apply")
    public Long applyRefund(RefundRequestForm refundRequest) {
        log.info("[退款服务] 申请退款，transactionNo={}, amount={}",
                refundRequest.getTransactionNo(), refundRequest.getRefundAmount());

        try {
            // 1. 验证交易记录是否存在
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectByTransactionNo(refundRequest.getTransactionNo());
            if (consumeRecord == null) {
                log.warn("[退款服务] 交易记录不存在，transactionNo={}", refundRequest.getTransactionNo());
                throw new BusinessException("交易记录不存在");
            }

            // 2. 验证退款金额是否合法
            BigDecimal originalAmount = consumeRecord.getAmount();
            if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[退款服务] 原交易金额无效，transactionNo={}, amount={}",
                        refundRequest.getTransactionNo(), originalAmount);
                throw new BusinessException("原交易金额无效");
            }

            if (refundRequest.getRefundAmount().compareTo(originalAmount) > 0) {
                log.warn("[退款服务] 退款金额超过原交易金额，transactionNo={}, refundAmount={}, originalAmount={}",
                        refundRequest.getTransactionNo(), refundRequest.getRefundAmount(), originalAmount);
                throw new BusinessException("退款金额不能超过原交易金额");
            }

            if (refundRequest.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[退款服务] 退款金额必须大于0，transactionNo={}, refundAmount={}",
                        refundRequest.getTransactionNo(), refundRequest.getRefundAmount());
                throw new BusinessException("退款金额必须大于0");
            }

            // 3. 检查是否已有退款记录（查询该交易号的所有退款申请）
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RefundApplicationEntity::getPaymentRecordId, consumeRecord.getId());
            wrapper.in(RefundApplicationEntity::getStatus,
                    Arrays.asList("PENDING", "APPROVED", "PROCESSING"));
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            long existingRefundCount = refundApplicationDao.selectCount(wrapper);
            if (existingRefundCount > 0) {
                log.warn("[退款服务] 该交易已有待处理或已审批的退款申请，transactionNo={}",
                        refundRequest.getTransactionNo());
                throw new BusinessException("该交易已有待处理或已审批的退款申请");
            }

            // 4. 计算已退款金额（查询已完成的退款）
            LambdaQueryWrapper<RefundApplicationEntity> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.eq(RefundApplicationEntity::getPaymentRecordId, consumeRecord.getId());
            completedWrapper.eq(RefundApplicationEntity::getStatus, "APPROVED");
            completedWrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            List<RefundApplicationEntity> completedRefunds = refundApplicationDao.selectList(completedWrapper);
            BigDecimal totalRefunded = completedRefunds.stream()
                    .map(RefundApplicationEntity::getRefundAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 5. 验证可退款金额
            BigDecimal availableAmount = originalAmount.subtract(totalRefunded);
            if (refundRequest.getRefundAmount().compareTo(availableAmount) > 0) {
                log.warn("[退款服务] 退款金额超过可退款金额，transactionNo={}, refundAmount={}, availableAmount={}",
                        refundRequest.getTransactionNo(), refundRequest.getRefundAmount(), availableAmount);
                throw new BusinessException("退款金额超过可退款金额，可退款金额为：" + availableAmount);
            }

            // 6. 创建退款申请记录
            RefundApplicationEntity refundApplication = new RefundApplicationEntity();
            refundApplication.setRefundNo(generateRefundNo());
            refundApplication.setPaymentRecordId(consumeRecord.getId());
            refundApplication.setUserId(consumeRecord.getUserId());
            refundApplication.setRefundAmount(refundRequest.getRefundAmount());
            refundApplication.setRefundReason(refundRequest.getRefundReason());
            refundApplication.setStatus("PENDING"); // 待审批

            int insertResult = refundApplicationDao.insert(refundApplication);
            if (insertResult <= 0) {
                log.error("[退款服务] 创建退款申请失败，transactionNo={}", refundRequest.getTransactionNo());
                throw new BusinessException("创建退款申请失败");
            }

            log.info("[退款服务] 退款申请创建成功，refundId={}, refundNo={}, transactionNo={}",
                    refundApplication.getId(), refundApplication.getRefundNo(), refundRequest.getTransactionNo());

            return refundApplication.getId();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 退款申请参数错误，transactionNo={}, error={}", refundRequest.getTransactionNo(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 退款申请业务异常，transactionNo={}, code={}, message={}",
                    refundRequest.getTransactionNo(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 退款申请系统异常，transactionNo={}, code={}, message={}", refundRequest.getTransactionNo(), e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_APPLY_SYSTEM_ERROR", "退款申请失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 退款申请未知异常，transactionNo={}", refundRequest.getTransactionNo(), e);
            throw new SystemException("REFUND_APPLY_SYSTEM_ERROR", "退款申请失败：" + e.getMessage(), e);
        }
    }

    /**
     * 生成退款申请编号
     * <p>
     * 格式：RF + YYYYMMDDHHmmss + 6位随机数（共20位）
     * 示例：RF20250130143025123456
     * </p>
     *
     * @return 退款申请编号
     */
    private String generateRefundNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", (int) (Math.random() * 1000000));
        return "RF" + timestamp + random;
    }

    @Override
    @Observed(name = "consume.refund.getRefundDetail", contextualName = "consume-refund-get-detail")
    @Transactional(readOnly = true)
    public RefundRecordVO getRefundDetail(Long refundId) {
        log.info("[退款服务] 查询退款详情，refundId={}", refundId);

        try {
            // 1. 查询退款申请记录
            RefundApplicationEntity refundApplication = refundApplicationDao.selectById(refundId);
            if (refundApplication == null) {
                log.warn("[退款服务] 退款申请不存在，refundId={}", refundId);
                throw new BusinessException("退款申请不存在");
            }

            // 2. 查询关联的交易记录
            ConsumeRecordEntity consumeRecord = null;
            if (refundApplication.getPaymentRecordId() != null) {
                consumeRecord = consumeRecordDao.selectById(refundApplication.getPaymentRecordId());
            }

            // 3. 转换为VO
            RefundRecordVO refundRecord = convertToRefundRecordVO(refundApplication, consumeRecord);

            log.info("[退款服务] 查询退款详情成功，refundId={}, status={}",
                    refundId, refundRecord.getRefundStatusName());

            return refundRecord;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 查询退款详情参数错误，refundId={}, error={}", refundId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 查询退款详情业务异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 查询退款详情系统异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_DETAIL_QUERY_SYSTEM_ERROR", "查询退款详情失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 查询退款详情未知异常，refundId={}", refundId, e);
            throw new SystemException("REFUND_DETAIL_QUERY_SYSTEM_ERROR", "查询退款详情失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.getRefundPage", contextualName = "consume-refund-get-page")
    @Transactional(readOnly = true)
    public PageResult<RefundRecordVO> getRefundPage(RefundQueryForm queryForm) {
        log.info("[退款服务] 分页查询退款记录，pageNum={}, pageSize={}, userId={}, status={}",
                queryForm.getPageNum(), queryForm.getPageSize(),
                queryForm.getUserId(), queryForm.getRefundStatus());

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();

            // 退款ID
            if (queryForm.getRefundId() != null) {
                wrapper.eq(RefundApplicationEntity::getId, queryForm.getRefundId());
            }

            // 用户ID
            if (queryForm.getUserId() != null) {
                wrapper.eq(RefundApplicationEntity::getUserId, queryForm.getUserId());
            }

            // 退款状态（需要将Integer转换为String）
            if (queryForm.getRefundStatus() != null) {
                String status = convertRefundStatusToString(queryForm.getRefundStatus());
                wrapper.eq(RefundApplicationEntity::getStatus, status);
            }

            // 审批状态
            if (queryForm.getApprovalStatus() != null) {
                if (queryForm.getApprovalStatus() == 0) {
                    wrapper.eq(RefundApplicationEntity::getStatus, "PENDING");
                } else if (queryForm.getApprovalStatus() == 1) {
                    wrapper.eq(RefundApplicationEntity::getStatus, "APPROVED");
                } else if (queryForm.getApprovalStatus() == 2) {
                    wrapper.eq(RefundApplicationEntity::getStatus, "REJECTED");
                }
            }

            // 申请时间范围
            if (queryForm.getStartApplyTime() != null) {
                wrapper.ge(RefundApplicationEntity::getCreateTime, queryForm.getStartApplyTime());
            }
            if (queryForm.getEndApplyTime() != null) {
                wrapper.le(RefundApplicationEntity::getCreateTime, queryForm.getEndApplyTime());
            }

            // 退款原因关键词
            if (queryForm.getRefundReasonKeyword() != null && !queryForm.getRefundReasonKeyword().isEmpty()) {
                wrapper.like(RefundApplicationEntity::getRefundReason, queryForm.getRefundReasonKeyword());
            }

            // 未删除
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);

            // 排序
            String sortBy = queryForm.getSortBy() != null ? queryForm.getSortBy() : "createTime";
            String sortDirection = queryForm.getSortDirection() != null ? queryForm.getSortDirection() : "desc";
            if ("applyTime".equals(sortBy) || "createTime".equals(sortBy)) {
                if ("asc".equalsIgnoreCase(sortDirection)) {
                    wrapper.orderByAsc(RefundApplicationEntity::getCreateTime);
                } else {
                    wrapper.orderByDesc(RefundApplicationEntity::getCreateTime);
                }
            } else if ("refundAmount".equals(sortBy)) {
                if ("asc".equalsIgnoreCase(sortDirection)) {
                    wrapper.orderByAsc(RefundApplicationEntity::getRefundAmount);
                } else {
                    wrapper.orderByDesc(RefundApplicationEntity::getRefundAmount);
                }
            } else {
                wrapper.orderByDesc(RefundApplicationEntity::getCreateTime);
            }

            // 2. 执行分页查询
            Page<RefundApplicationEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            IPage<RefundApplicationEntity> pageResult = refundApplicationDao.selectPage(page, wrapper);

            // 3. 转换为VO列表
            List<RefundRecordVO> voList = new ArrayList<>();
            for (RefundApplicationEntity refundApplication : pageResult.getRecords()) {
                ConsumeRecordEntity consumeRecord = null;
                if (refundApplication.getPaymentRecordId() != null) {
                    consumeRecord = consumeRecordDao.selectById(refundApplication.getPaymentRecordId());
                }
                RefundRecordVO vo = convertToRefundRecordVO(refundApplication, consumeRecord);
                voList.add(vo);
            }

            // 4. 构建分页结果
            PageResult<RefundRecordVO> result = new PageResult<>();
            result.setList(voList);
            result.setTotal(pageResult.getTotal());
            result.setPageNum(queryForm.getPageNum());
            result.setPageSize(queryForm.getPageSize());
            result.setPages((int) Math.ceil((double) pageResult.getTotal() / queryForm.getPageSize()));

            log.info("[退款服务] 分页查询退款记录成功，total={}, size={}",
                    result.getTotal(), voList.size());

            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 分页查询退款记录参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 分页查询退款记录业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 分页查询退款记录系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_PAGE_QUERY_SYSTEM_ERROR", "分页查询退款记录失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 分页查询退款记录未知异常", e);
            throw new SystemException("REFUND_PAGE_QUERY_SYSTEM_ERROR", "分页查询退款记录失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将退款状态Integer转换为String
     *
     * @param status 退款状态（1-待审批，2-已审批，3-已拒绝，4-已处理，5-已取消）
     * @return 状态字符串
     */
    private String convertRefundStatusToString(Integer status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case 1: return "PENDING";
            case 2: return "APPROVED";
            case 3: return "REJECTED";
            case 4: return "PROCESSING";
            case 5: return "CANCELLED";
            default: return "PENDING";
        }
    }

    /**
     * 将退款申请Entity转换为VO
     *
     * @param refundApplication 退款申请实体
     * @param consumeRecord 消费记录实体
     * @return 退款记录VO
     */
    private RefundRecordVO convertToRefundRecordVO(RefundApplicationEntity refundApplication,
                                                   ConsumeRecordEntity consumeRecord) {
        RefundRecordVO vo = new RefundRecordVO();

        // 基本信息
        vo.setRefundId(refundApplication.getId());
        vo.setUserId(refundApplication.getUserId());
        vo.setRefundAmount(refundApplication.getRefundAmount());
        vo.setRefundReason(refundApplication.getRefundReason());
        vo.setApplyTime(refundApplication.getCreateTime());

        // 交易信息
        if (consumeRecord != null) {
            vo.setTransactionNo(consumeRecord.getTransactionNo());
            vo.setOriginalAmount(consumeRecord.getAmount());
        }

        // 状态信息
        String status = refundApplication.getStatus();
        vo.setRefundStatus(convertRefundStatusToInteger(status));
        vo.setRefundStatusName(convertRefundStatusName(status));

        // 审批信息
        vo.setApprovalComment(refundApplication.getApprovalComment());
        vo.setApproveTime(refundApplication.getApprovalTime());

        // 审批状态
        if ("APPROVED".equals(status)) {
            vo.setApprovalStatus(1);
            vo.setApprovalStatusName("已通过");
        } else if ("REJECTED".equals(status)) {
            vo.setApprovalStatus(2);
            vo.setApprovalStatusName("已拒绝");
        } else {
            vo.setApprovalStatus(0);
            vo.setApprovalStatusName("待审批");
        }

        return vo;
    }

    /**
     * 将退款状态String转换为Integer
     *
     * @param status 状态字符串
     * @return 状态整数
     */
    private Integer convertRefundStatusToInteger(String status) {
        if (status == null) {
            return 1; // 默认待审批
        }
        switch (status) {
            case "PENDING": return 1;
            case "APPROVED": return 2;
            case "REJECTED": return 3;
            case "PROCESSING": return 4;
            case "CANCELLED": return 5;
            default: return 1;
        }
    }

    /**
     * 获取退款状态名称
     *
     * @param status 状态字符串
     * @return 状态名称
     */
    private String convertRefundStatusName(String status) {
        if (status == null) {
            return "待审批";
        }
        switch (status) {
            case "PENDING": return "待审批";
            case "APPROVED": return "已审批";
            case "REJECTED": return "已拒绝";
            case "PROCESSING": return "处理中";
            case "CANCELLED": return "已取消";
            default: return "待审批";
        }
    }

    @Override
    @Observed(name = "consume.refund.batchApplyRefund", contextualName = "consume-refund-batch-apply")
    public int batchApplyRefund(List<String> transactionNos, String reason) {
        log.info("[退款服务] 批量申请退款，transactionNos={}, reason={}",
                transactionNos.size(), reason);

        int successCount = 0;
        int failCount = 0;

        for (String transactionNo : transactionNos) {
            try {
                // 查询交易记录获取金额
                ConsumeRecordEntity consumeRecord = consumeRecordDao.selectByTransactionNo(transactionNo);
                if (consumeRecord == null) {
                    log.warn("[退款服务] 交易记录不存在，跳过，transactionNo={}", transactionNo);
                    failCount++;
                    continue;
                }

                // 创建退款申请表单
                RefundRequestForm refundRequest = new RefundRequestForm();
                refundRequest.setTransactionNo(transactionNo);
                refundRequest.setRefundAmount(consumeRecord.getAmount()); // 全额退款
                refundRequest.setRefundReason(reason != null ? reason : "批量退款");

                // 申请退款
                applyRefund(refundRequest);
                successCount++;

            } catch (BusinessException e) {
                log.warn("[退款服务] 批量申请退款业务异常，transactionNo={}, code={}, message={}",
                        transactionNo, e.getCode(), e.getMessage());
                failCount++;
            } catch (SystemException e) {
                log.error("[退款服务] 批量申请退款系统异常，transactionNo={}, code={}, message={}",
                        transactionNo, e.getCode(), e.getMessage(), e);
                failCount++;
            } catch (Exception e) {
                log.error("[退款服务] 批量申请退款未知异常，transactionNo={}, error={}",
                        transactionNo, e.getMessage());
                failCount++;
            }
        }

        log.info("[退款服务] 批量申请退款完成，总数={}, 成功={}, 失败={}",
                transactionNos.size(), successCount, failCount);

        return successCount;
    }

    @Override
    @Observed(name = "consume.refund.batchApplyRefundWithAmount", contextualName = "consume-refund-batch-apply-amount")
    public int batchApplyRefundWithAmount(List<Map<String, Object>> refundRequests) {
        log.info("[退款服务] 批量申请退款（带金额），count={}", refundRequests.size());

        int successCount = 0;
        int failCount = 0;

        for (Map<String, Object> requestMap : refundRequests) {
            try {
                // 提取参数
                String transactionNo = (String) requestMap.get("transactionNo");
                Object amountObj = requestMap.get("refundAmount");
                String reason = (String) requestMap.getOrDefault("reason", "批量退款");

                if (transactionNo == null || transactionNo.isEmpty()) {
                    log.warn("[退款服务] 交易号为空，跳过");
                    failCount++;
                    continue;
                }

                BigDecimal refundAmount;
                if (amountObj instanceof BigDecimal) {
                    refundAmount = (BigDecimal) amountObj;
                } else if (amountObj instanceof Number) {
                    refundAmount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
                } else if (amountObj instanceof String) {
                    refundAmount = new BigDecimal((String) amountObj);
                } else {
                    // 如果没有指定金额，查询交易记录获取全额
                    ConsumeRecordEntity consumeRecord = consumeRecordDao.selectByTransactionNo(transactionNo);
                    if (consumeRecord == null) {
                        log.warn("[退款服务] 交易记录不存在，跳过，transactionNo={}", transactionNo);
                        failCount++;
                        continue;
                    }
                    refundAmount = consumeRecord.getAmount();
                }

                // 创建退款申请表单
                RefundRequestForm refundRequest = new RefundRequestForm();
                refundRequest.setTransactionNo(transactionNo);
                refundRequest.setRefundAmount(refundAmount);
                refundRequest.setRefundReason(reason);

                // 申请退款
                applyRefund(refundRequest);
                successCount++;

            } catch (BusinessException e) {
                log.warn("[退款服务] 批量申请退款（带金额）业务异常，requestMap={}, code={}, message={}",
                        requestMap, e.getCode(), e.getMessage());
                failCount++;
            } catch (SystemException e) {
                log.error("[退款服务] 批量申请退款（带金额）系统异常，requestMap={}, code={}, message={}",
                        requestMap, e.getCode(), e.getMessage(), e);
                failCount++;
            } catch (Exception e) {
                log.error("[退款服务] 批量申请退款（带金额）未知异常，requestMap={}, error={}",
                        requestMap, e.getMessage());
                failCount++;
            }
        }

        log.info("[退款服务] 批量申请退款（带金额）完成，总数={}, 成功={}, 失败={}",
                refundRequests.size(), successCount, failCount);

        return successCount;
    }

    @Override
    @Observed(name = "consume.refund.approveRefund", contextualName = "consume-refund-approve")
    public boolean approveRefund(Long refundId, Boolean approved, String comment) {
        log.info("[退款服务] 审批退款，refundId={}, approved={}, comment={}",
                refundId, approved, comment);

        try {
            // 1. 查询退款申请
            RefundApplicationEntity refundApplication = refundApplicationDao.selectById(refundId);
            if (refundApplication == null) {
                log.warn("[退款服务] 退款申请不存在，refundId={}", refundId);
                throw new BusinessException("退款申请不存在");
            }

            // 2. 验证状态
            if (!"PENDING".equals(refundApplication.getStatus())) {
                log.warn("[退款服务] 退款申请状态不允许审批，refundId={}, status={}",
                        refundId, refundApplication.getStatus());
                throw new BusinessException("退款申请状态不允许审批，当前状态：" + refundApplication.getStatus());
            }

            // 3. 更新审批结果
            if (Boolean.TRUE.equals(approved)) {
                refundApplication.setStatus("APPROVED");
            } else {
                refundApplication.setStatus("REJECTED");
            }
            refundApplication.setApprovalComment(comment);
            refundApplication.setApprovalTime(LocalDateTime.now());

            int updateResult = refundApplicationDao.updateById(refundApplication);
            if (updateResult <= 0) {
                log.error("[退款服务] 更新退款审批结果失败，refundId={}", refundId);
                throw new BusinessException("更新退款审批结果失败");
            }

            log.info("[退款服务] 退款审批成功，refundId={}, approved={}", refundId, approved);
            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 退款审批参数错误，refundId={}, error={}", refundId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 退款审批业务异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 退款审批系统异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_APPROVE_SYSTEM_ERROR", "退款审批失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 退款审批未知异常，refundId={}", refundId, e);
            throw new SystemException("REFUND_APPROVE_SYSTEM_ERROR", "退款审批失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.batchApproveRefund", contextualName = "consume-refund-batch-approve")
    public int batchApproveRefund(List<Long> refundIds, Boolean approved, String comment) {
        log.info("[退款服务] 批量审批退款，refundIds={}, approved={}",
                refundIds.size(), approved);

        int successCount = 0;
        int failCount = 0;

        for (Long refundId : refundIds) {
            try {
                approveRefund(refundId, approved, comment);
                successCount++;
            } catch (BusinessException e) {
                log.warn("[退款服务] 批量审批退款业务异常，refundId={}, code={}, message={}",
                        refundId, e.getCode(), e.getMessage());
                failCount++;
            } catch (SystemException e) {
                log.error("[退款服务] 批量审批退款系统异常，refundId={}, code={}, message={}",
                        refundId, e.getCode(), e.getMessage(), e);
                failCount++;
            } catch (Exception e) {
                log.error("[退款服务] 批量审批退款未知异常，refundId={}, error={}",
                        refundId, e.getMessage());
                failCount++;
            }
        }

        log.info("[退款服务] 批量审批退款完成，总数={}, 成功={}, 失败={}",
                refundIds.size(), successCount, failCount);

        return successCount;
    }

    @Override
    @Observed(name = "consume.refund.cancelRefund", contextualName = "consume-refund-cancel")
    public boolean cancelRefund(Long refundId, String reason) {
        log.info("[退款服务] 取消退款，refundId={}, reason={}", refundId, reason);

        try {
            // 1. 查询退款申请
            RefundApplicationEntity refundApplication = refundApplicationDao.selectById(refundId);
            if (refundApplication == null) {
                log.warn("[退款服务] 退款申请不存在，refundId={}", refundId);
                throw new BusinessException("退款申请不存在");
            }

            // 2. 验证状态（只有待审批状态可以取消）
            if (!"PENDING".equals(refundApplication.getStatus())) {
                log.warn("[退款服务] 退款申请状态不允许取消，refundId={}, status={}",
                        refundId, refundApplication.getStatus());
                throw new BusinessException("只有待审批状态的退款申请可以取消");
            }

            // 3. 更新状态为已取消
            refundApplication.setStatus("CANCELLED");
            if (reason != null && !reason.isEmpty()) {
                refundApplication.setApprovalComment("取消原因：" + reason);
            }

            int updateResult = refundApplicationDao.updateById(refundApplication);
            if (updateResult <= 0) {
                log.error("[退款服务] 取消退款失败，refundId={}", refundId);
                throw new BusinessException("取消退款失败");
            }

            log.info("[退款服务] 取消退款成功，refundId={}", refundId);
            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 取消退款参数错误，refundId={}, error={}", refundId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 取消退款业务异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 取消退款系统异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_CANCEL_SYSTEM_ERROR", "取消退款失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 取消退款未知异常，refundId={}", refundId, e);
            throw new SystemException("REFUND_CANCEL_SYSTEM_ERROR", "取消退款失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.processRefund", contextualName = "consume-refund-process")
    public boolean processRefund(Long refundId) {
        log.info("[退款服务] 处理退款，refundId={}", refundId);

        try {
            // 1. 查询退款申请
            RefundApplicationEntity refundApplication = refundApplicationDao.selectById(refundId);
            if (refundApplication == null) {
                log.warn("[退款服务] 退款申请不存在，refundId={}", refundId);
                throw new BusinessException("退款申请不存在");
            }

            // 2. 验证状态（只有已审批状态可以处理）
            if (!"APPROVED".equals(refundApplication.getStatus())) {
                log.warn("[退款服务] 退款申请状态不允许处理，refundId={}, status={}",
                        refundId, refundApplication.getStatus());
                throw new BusinessException("只有已审批状态的退款申请可以处理");
            }

            // 3. 查询关联的交易记录
            ConsumeRecordEntity consumeRecord = null;
            if (refundApplication.getPaymentRecordId() != null) {
                consumeRecord = consumeRecordDao.selectById(refundApplication.getPaymentRecordId());
            }

            if (consumeRecord == null) {
                log.warn("[退款服务] 关联的交易记录不存在，refundId={}, paymentRecordId={}",
                        refundId, refundApplication.getPaymentRecordId());
                throw new BusinessException("关联的交易记录不存在");
            }

            // 4. 执行退款到账户余额
            Long accountId = consumeRecord.getAccountId();
            if (accountId == null) {
                log.warn("[退款服务] 账户ID为空，无法退款，refundId={}", refundId);
                throw new BusinessException("账户ID为空，无法退款");
            }

            // 调用账户服务退款
            boolean refundSuccess = accountService.addBalance(
                    accountId,
                    refundApplication.getRefundAmount(),
                    "退款：" + refundApplication.getRefundReason()
            );

            if (!refundSuccess) {
                log.error("[退款服务] 账户退款失败，refundId={}, accountId={}, amount={}",
                        refundId, accountId, refundApplication.getRefundAmount());
                throw new BusinessException("账户退款失败");
            }

            // 5. 更新退款申请状态为处理中（实际已完成）
            refundApplication.setStatus("PROCESSING");
            refundApplication.setApprovalComment("退款已成功到账");

            int updateResult = refundApplicationDao.updateById(refundApplication);
            if (updateResult <= 0) {
                log.error("[退款服务] 更新退款处理状态失败，refundId={}", refundId);
                // 注意：这里账户已经退款成功，即使更新状态失败也不应该回滚账户退款
                // 可以通过补偿机制或人工处理来解决状态不一致问题
            }

            log.info("[退款服务] 退款处理成功，refundId={}, accountId={}, amount={}",
                    refundId, accountId, refundApplication.getRefundAmount());

            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 退款处理参数错误，refundId={}, error={}", refundId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 退款处理业务异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 退款处理系统异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_PROCESS_SYSTEM_ERROR", "退款处理失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 退款处理未知异常，refundId={}", refundId, e);
            throw new SystemException("REFUND_PROCESS_SYSTEM_ERROR", "退款处理失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.getRefundStatistics", contextualName = "consume-refund-get-statistics")
    @Transactional(readOnly = true)
    public Map<String, Object> getRefundStatistics() {
        log.info("[退款服务] 获取退款统计信息");

        try {
            // 1. 查询所有退款申请（未删除）
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            List<RefundApplicationEntity> allRefunds = refundApplicationDao.selectList(wrapper);

            // 2. 统计总数和总金额
            long totalCount = allRefunds.size();
            BigDecimal totalAmount = allRefunds.stream()
                    .map(RefundApplicationEntity::getRefundAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. 按状态统计
            long pendingCount = allRefunds.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .count();
            long approvedCount = allRefunds.stream()
                    .filter(r -> "APPROVED".equals(r.getStatus()))
                    .count();
            long rejectedCount = allRefunds.stream()
                    .filter(r -> "REJECTED".equals(r.getStatus()))
                    .count();
            long processingCount = allRefunds.stream()
                    .filter(r -> "PROCESSING".equals(r.getStatus()))
                    .count();
            long cancelledCount = allRefunds.stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .count();

            // 4. 计算已审批金额
            BigDecimal approvedAmount = allRefunds.stream()
                    .filter(r -> "APPROVED".equals(r.getStatus()) || "PROCESSING".equals(r.getStatus()))
                    .map(RefundApplicationEntity::getRefundAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 5. 构建统计结果
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", totalCount);
            statistics.put("totalAmount", totalAmount);
            statistics.put("pendingCount", pendingCount);
            statistics.put("approvedCount", approvedCount);
            statistics.put("rejectedCount", rejectedCount);
            statistics.put("processingCount", processingCount);
            statistics.put("cancelledCount", cancelledCount);
            statistics.put("approvedAmount", approvedAmount);

            log.info("[退款服务] 获取退款统计信息成功，totalCount={}, totalAmount={}",
                    totalCount, totalAmount);

            return statistics;

        } catch (BusinessException e) {
            log.warn("[退款服务] 获取退款统计信息业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 获取退款统计信息系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_STATISTICS_SYSTEM_ERROR", "获取退款统计信息失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 获取退款统计信息未知异常", e);
            throw new SystemException("REFUND_STATISTICS_SYSTEM_ERROR", "获取退款统计信息失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.getUserRefundStatistics", contextualName = "consume-refund-get-user-statistics")
    @Transactional(readOnly = true)
    public Map<String, Object> getUserRefundStatistics(Long userId) {
        log.info("[退款服务] 获取用户退款统计，userId={}", userId);

        try {
            // 1. 查询该用户的所有退款申请（未删除）
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RefundApplicationEntity::getUserId, userId);
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            List<RefundApplicationEntity> userRefunds = refundApplicationDao.selectList(wrapper);

            // 2. 统计总数和总金额
            long totalCount = userRefunds.size();
            BigDecimal totalAmount = userRefunds.stream()
                    .map(RefundApplicationEntity::getRefundAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. 按状态统计
            long pendingCount = userRefunds.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .count();
            long approvedCount = userRefunds.stream()
                    .filter(r -> "APPROVED".equals(r.getStatus()))
                    .count();
            long rejectedCount = userRefunds.stream()
                    .filter(r -> "REJECTED".equals(r.getStatus()))
                    .count();
            long processingCount = userRefunds.stream()
                    .filter(r -> "PROCESSING".equals(r.getStatus()))
                    .count();

            // 4. 计算已审批金额
            BigDecimal approvedAmount = userRefunds.stream()
                    .filter(r -> "APPROVED".equals(r.getStatus()) || "PROCESSING".equals(r.getStatus()))
                    .map(RefundApplicationEntity::getRefundAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 5. 构建统计结果
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("userId", userId);
            statistics.put("totalCount", totalCount);
            statistics.put("totalAmount", totalAmount);
            statistics.put("pendingCount", pendingCount);
            statistics.put("approvedCount", approvedCount);
            statistics.put("rejectedCount", rejectedCount);
            statistics.put("processingCount", processingCount);
            statistics.put("approvedAmount", approvedAmount);

            log.info("[退款服务] 获取用户退款统计成功，userId={}, totalCount={}, totalAmount={}",
                    userId, totalCount, totalAmount);

            return statistics;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 获取用户退款统计参数错误，userId={}, error={}", userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 获取用户退款统计业务异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 获取用户退款统计系统异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("USER_REFUND_STATISTICS_SYSTEM_ERROR", "获取用户退款统计失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 获取用户退款统计未知异常，userId={}", userId, e);
            throw new SystemException("USER_REFUND_STATISTICS_SYSTEM_ERROR", "获取用户退款统计失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.getRefundTrend", contextualName = "consume-refund-get-trend")
    @Transactional(readOnly = true)
    public Map<String, Object> getRefundTrend(String startDate, String endDate, String statisticsType) {
        log.info("[退款服务] 获取退款趋势，startDate={}, endDate={}, type={}",
                startDate, endDate, statisticsType);

        try {
            // 1. 解析日期
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (start.isAfter(end)) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }

            // 2. 确定统计粒度（默认按天）
            String type = statisticsType != null ? statisticsType.toUpperCase() : "DAY";
            if (!"DAY".equals(type) && !"WEEK".equals(type) && !"MONTH".equals(type)) {
                type = "DAY";
            }

            // 3. 查询时间范围内的退款申请
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(RefundApplicationEntity::getCreateTime, start.atStartOfDay());
            wrapper.le(RefundApplicationEntity::getCreateTime, end.atTime(23, 59, 59));
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            wrapper.orderByAsc(RefundApplicationEntity::getCreateTime);
            List<RefundApplicationEntity> refunds = refundApplicationDao.selectList(wrapper);

            // 4. 按时间分组统计
            Map<String, Long> countMap = new LinkedHashMap<>();
            Map<String, BigDecimal> amountMap = new LinkedHashMap<>();
            List<String> dates = new ArrayList<>();

            // 生成日期列表
            LocalDate current = start;
            while (!current.isAfter(end)) {
                String dateKey = formatDateKey(current, type);
                dates.add(dateKey);
                countMap.put(dateKey, 0L);
                amountMap.put(dateKey, BigDecimal.ZERO);

                if ("DAY".equals(type)) {
                    current = current.plusDays(1);
                } else if ("WEEK".equals(type)) {
                    current = current.plusWeeks(1);
                } else {
                    current = current.plusMonths(1);
                }
            }

            // 统计数据
            for (RefundApplicationEntity refund : refunds) {
                LocalDate refundDate = refund.getCreateTime().toLocalDate();
                String dateKey = formatDateKey(refundDate, type);

                if (countMap.containsKey(dateKey)) {
                    countMap.put(dateKey, countMap.get(dateKey) + 1);
                    BigDecimal amount = refund.getRefundAmount() != null ? refund.getRefundAmount() : BigDecimal.ZERO;
                    amountMap.put(dateKey, amountMap.get(dateKey).add(amount));
                }
            }

            // 5. 构建返回结果
            Map<String, Object> trend = new HashMap<>();
            trend.put("dates", dates);
            trend.put("counts", new ArrayList<>(countMap.values()));
            trend.put("amounts", amountMap.values().stream()
                    .map(amount -> amount.setScale(2, RoundingMode.HALF_UP).toString())
                    .collect(Collectors.toList()));

            log.info("[退款服务] 获取退款趋势成功，type={}, dateCount={}", type, dates.size());

            return trend;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 获取退款趋势参数错误，startDate={}, endDate={}, type={}, error={}", startDate, endDate, statisticsType, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 获取退款趋势业务异常，startDate={}, endDate={}, type={}, code={}, message={}", startDate, endDate, statisticsType, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 获取退款趋势系统异常，startDate={}, endDate={}, type={}, code={}, message={}", startDate, endDate, statisticsType, e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_TREND_SYSTEM_ERROR", "获取退款趋势失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 获取退款趋势未知异常，startDate={}, endDate={}, type={}", startDate, endDate, statisticsType, e);
            throw new SystemException("REFUND_TREND_SYSTEM_ERROR", "获取退款趋势失败：" + e.getMessage(), e);
        }
    }

    /**
     * 格式化日期键
     *
     * @param date 日期
     * @param type 统计类型（DAY/WEEK/MONTH）
     * @return 格式化后的日期键
     */
    private String formatDateKey(LocalDate date, String type) {
        if ("WEEK".equals(type)) {
            // 返回周的开始日期（周一）
            LocalDate monday = date.minusDays(date.getDayOfWeek().getValue() - 1);
            return monday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if ("MONTH".equals(type)) {
            // 返回月份的第一天
            return date.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } else {
            // 按天
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    @Override
    @Observed(name = "consume.refund.exportRefundData", contextualName = "consume-refund-export")
    public void exportRefundData(RefundQueryForm queryForm, HttpServletResponse response) {
        log.info("[退款服务] 导出退款数据");

        PrintWriter writer = null;
        try {
            // 1. 查询退款数据（使用分页查询逻辑，但获取所有数据）
            RefundQueryForm exportQuery = new RefundQueryForm();
            exportQuery.setRefundId(queryForm.getRefundId());
            exportQuery.setUserId(queryForm.getUserId());
            exportQuery.setTransactionNo(queryForm.getTransactionNo());
            exportQuery.setRefundStatus(queryForm.getRefundStatus());
            exportQuery.setApprovalStatus(queryForm.getApprovalStatus());
            exportQuery.setStartApplyTime(queryForm.getStartApplyTime());
            exportQuery.setEndApplyTime(queryForm.getEndApplyTime());
            exportQuery.setRefundReasonKeyword(queryForm.getRefundReasonKeyword());
            exportQuery.setPageNum(1);
            exportQuery.setPageSize(10000); // 设置较大的页面大小以获取所有数据

            // 2. 构建查询条件（复用分页查询逻辑）
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            if (exportQuery.getRefundId() != null) {
                wrapper.eq(RefundApplicationEntity::getId, exportQuery.getRefundId());
            }
            if (exportQuery.getUserId() != null) {
                wrapper.eq(RefundApplicationEntity::getUserId, exportQuery.getUserId());
            }
            if (exportQuery.getRefundStatus() != null) {
                String status = convertRefundStatusToString(exportQuery.getRefundStatus());
                wrapper.eq(RefundApplicationEntity::getStatus, status);
            }
            if (exportQuery.getStartApplyTime() != null) {
                wrapper.ge(RefundApplicationEntity::getCreateTime, exportQuery.getStartApplyTime());
            }
            if (exportQuery.getEndApplyTime() != null) {
                wrapper.le(RefundApplicationEntity::getCreateTime, exportQuery.getEndApplyTime());
            }
            if (exportQuery.getRefundReasonKeyword() != null && !exportQuery.getRefundReasonKeyword().isEmpty()) {
                wrapper.like(RefundApplicationEntity::getRefundReason, exportQuery.getRefundReasonKeyword());
            }
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            wrapper.orderByDesc(RefundApplicationEntity::getCreateTime);

            List<RefundApplicationEntity> refunds = refundApplicationDao.selectList(wrapper);

            // 3. 设置响应头
            String fileName = "退款记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));

            // 4. 写入CSV数据
            writer = response.getWriter();

            // 写入BOM（解决Excel打开中文乱码问题）
            writer.write('\ufeff');

            // 写入表头
            writer.write("退款ID,退款编号,交易号,用户ID,退款金额,原交易金额,退款原因,退款状态,申请时间,审批时间,审批意见\n");

            // 写入数据
            for (RefundApplicationEntity refund : refunds) {
                ConsumeRecordEntity consumeRecord = null;
                if (refund.getPaymentRecordId() != null) {
                    consumeRecord = consumeRecordDao.selectById(refund.getPaymentRecordId());
                }

                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        refund.getId() != null ? refund.getId().toString() : "",
                        refund.getRefundNo() != null ? refund.getRefundNo() : "",
                        consumeRecord != null && consumeRecord.getTransactionNo() != null ? consumeRecord.getTransactionNo() : "",
                        refund.getUserId() != null ? refund.getUserId().toString() : "",
                        refund.getRefundAmount() != null ? refund.getRefundAmount().toString() : "0.00",
                        consumeRecord != null && consumeRecord.getAmount() != null ? consumeRecord.getAmount().toString() : "0.00",
                        refund.getRefundReason() != null ? refund.getRefundReason().replace(",", "，") : "",
                        convertRefundStatusName(refund.getStatus()),
                        refund.getCreateTime() != null ? refund.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "",
                        refund.getApprovalTime() != null ? refund.getApprovalTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "",
                        refund.getApprovalComment() != null ? refund.getApprovalComment().replace(",", "，") : ""
                );
                writer.write(line);
            }

            writer.flush();
            log.info("[退款服务] 导出退款数据成功，记录数={}", refunds.size());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 导出退款数据参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (IOException e) {
            log.error("[退款服务] 导出退款数据IO异常", e);
            throw new SystemException("REFUND_EXPORT_IO_ERROR", "导出退款数据IO异常：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[退款服务] 导出退款数据业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 导出退款数据系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_EXPORT_SYSTEM_ERROR", "导出退款数据失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 导出退款数据未知异常", e);
            throw new SystemException("REFUND_EXPORT_SYSTEM_ERROR", "导出退款数据失败：" + e.getMessage(), e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    @Observed(name = "consume.refund.checkRefundStatus", contextualName = "consume-refund-check-status")
    @Transactional(readOnly = true)
    public Map<String, Object> checkRefundStatus(Long refundId) {
        log.info("[退款服务] 检查退款状态，refundId={}", refundId);

        try {
            // 1. 查询退款申请
            RefundApplicationEntity refundApplication = refundApplicationDao.selectById(refundId);
            if (refundApplication == null) {
                log.warn("[退款服务] 退款申请不存在，refundId={}", refundId);
                throw new BusinessException("退款申请不存在");
            }

            // 2. 构建状态信息
            Map<String, Object> statusInfo = new HashMap<>();
            statusInfo.put("refundId", refundId);
            statusInfo.put("refundNo", refundApplication.getRefundNo());
            statusInfo.put("status", convertRefundStatusToInteger(refundApplication.getStatus()));
            statusInfo.put("statusName", convertRefundStatusName(refundApplication.getStatus()));

            // 3. 判断可操作权限
            String status = refundApplication.getStatus();
            boolean canCancel = "PENDING".equals(status);
            boolean canModify = "PENDING".equals(status);
            boolean canApprove = "PENDING".equals(status);
            boolean canProcess = "APPROVED".equals(status);

            statusInfo.put("canCancel", canCancel);
            statusInfo.put("canModify", canModify);
            statusInfo.put("canApprove", canApprove);
            statusInfo.put("canProcess", canProcess);
            statusInfo.put("applyTime", refundApplication.getCreateTime());
            statusInfo.put("approveTime", refundApplication.getApprovalTime());
            statusInfo.put("approvalComment", refundApplication.getApprovalComment());

            log.info("[退款服务] 检查退款状态成功，refundId={}, status={}",
                    refundId, statusInfo.get("statusName"));

            return statusInfo;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 检查退款状态参数错误，refundId={}, error={}", refundId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 检查退款状态业务异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 检查退款状态系统异常，refundId={}, code={}, message={}", refundId, e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_STATUS_CHECK_SYSTEM_ERROR", "检查退款状态失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 检查退款状态未知异常，refundId={}", refundId, e);
            throw new SystemException("REFUND_STATUS_CHECK_SYSTEM_ERROR", "检查退款状态失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.getAvailableRefundAmount", contextualName = "consume-refund-get-available-amount")
    @Transactional(readOnly = true)
    public Map<String, Object> getAvailableRefundAmount(String transactionNo) {
        log.info("[退款服务] 获取可退款金额，transactionNo={}", transactionNo);

        try {
            // 1. 查询交易记录
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectByTransactionNo(transactionNo);
            if (consumeRecord == null) {
                log.warn("[退款服务] 交易记录不存在，transactionNo={}", transactionNo);
                throw new BusinessException("交易记录不存在");
            }

            BigDecimal originalAmount = consumeRecord.getAmount();
            if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[退款服务] 原交易金额无效，transactionNo={}, amount={}",
                        transactionNo, originalAmount);
                throw new BusinessException("原交易金额无效");
            }

            // 2. 查询该交易的所有退款申请
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RefundApplicationEntity::getPaymentRecordId, consumeRecord.getId());
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            List<RefundApplicationEntity> refunds = refundApplicationDao.selectList(wrapper);

            // 3. 计算已退款金额（已审批和已处理的退款）
            BigDecimal refundedAmount = refunds.stream()
                    .filter(r -> "APPROVED".equals(r.getStatus()) || "PROCESSING".equals(r.getStatus()))
                    .map(RefundApplicationEntity::getRefundAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 4. 计算可退款金额
            BigDecimal availableAmount = originalAmount.subtract(refundedAmount);
            if (availableAmount.compareTo(BigDecimal.ZERO) < 0) {
                availableAmount = BigDecimal.ZERO;
            }

            // 5. 检查是否有待审批的退款申请
            boolean hasPendingRefund = refunds.stream()
                    .anyMatch(r -> "PENDING".equals(r.getStatus()));

            // 6. 构建返回结果
            Map<String, Object> amountInfo = new HashMap<>();
            amountInfo.put("transactionNo", transactionNo);
            amountInfo.put("originalAmount", originalAmount);
            amountInfo.put("refundedAmount", refundedAmount);
            amountInfo.put("availableAmount", availableAmount);
            amountInfo.put("canRefund", availableAmount.compareTo(BigDecimal.ZERO) > 0 && !hasPendingRefund);
            amountInfo.put("hasPendingRefund", hasPendingRefund);
            amountInfo.put("refundCount", refunds.size());

            log.info("[退款服务] 获取可退款金额成功，transactionNo={}, originalAmount={}, availableAmount={}",
                    transactionNo, originalAmount, availableAmount);

            return amountInfo;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 获取可退款金额参数错误，transactionNo={}, error={}", transactionNo, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款服务] 获取可退款金额业务异常，transactionNo={}, code={}, message={}",
                    transactionNo, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 获取可退款金额系统异常，transactionNo={}, code={}, message={}", transactionNo, e.getCode(), e.getMessage(), e);
            throw new SystemException("AVAILABLE_REFUND_AMOUNT_SYSTEM_ERROR", "获取可退款金额失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 获取可退款金额未知异常，transactionNo={}", transactionNo, e);
            throw new SystemException("AVAILABLE_REFUND_AMOUNT_SYSTEM_ERROR", "获取可退款金额失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.refund.getRefundByTransactionNo", contextualName = "consume-refund-get-by-transaction")
    @Transactional(readOnly = true)
    public List<RefundRecordVO> getRefundByTransactionNo(String transactionNo) {
        log.info("[退款服务] 根据交易号查询退款记录，transactionNo={}", transactionNo);

        try {
            // 1. 查询交易记录
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectByTransactionNo(transactionNo);
            if (consumeRecord == null) {
                log.warn("[退款服务] 交易记录不存在，transactionNo={}", transactionNo);
                return new ArrayList<>();
            }

            // 2. 查询该交易的所有退款申请
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RefundApplicationEntity::getPaymentRecordId, consumeRecord.getId());
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            wrapper.orderByDesc(RefundApplicationEntity::getCreateTime);
            List<RefundApplicationEntity> refunds = refundApplicationDao.selectList(wrapper);

            // 3. 转换为VO列表
            List<RefundRecordVO> voList = refunds.stream()
                    .map(refund -> convertToRefundRecordVO(refund, consumeRecord))
                    .collect(Collectors.toList());

            log.info("[退款服务] 根据交易号查询退款记录成功，transactionNo={}, count={}",
                    transactionNo, voList.size());

            return voList;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 根据交易号查询退款记录参数错误，transactionNo={}, error={}", transactionNo, e.getMessage());
            // 降级：返回空列表
            return new ArrayList<>();
        } catch (BusinessException e) {
            log.warn("[退款服务] 根据交易号查询退款记录业务异常，transactionNo={}, code={}, message={}", transactionNo, e.getCode(), e.getMessage());
            // 降级：返回空列表
            return new ArrayList<>();
        } catch (SystemException e) {
            log.error("[退款服务] 根据交易号查询退款记录系统异常，transactionNo={}, code={}, message={}", transactionNo, e.getCode(), e.getMessage(), e);
            // 降级：返回空列表
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("[退款服务] 根据交易号查询退款记录未知异常，transactionNo={}", transactionNo, e);
            // 降级：返回空列表
            return new ArrayList<>();
        }
    }

    @Override
    @Observed(name = "consume.refund.validateRefundRequest", contextualName = "consume-refund-validate")
    @Transactional(readOnly = true)
    public Map<String, Object> validateRefundRequest(RefundRequestForm refundRequest) {
        log.info("[退款服务] 验证退款申请，transactionNo={}", refundRequest.getTransactionNo());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // 1. 验证交易记录是否存在
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectByTransactionNo(refundRequest.getTransactionNo());
            if (consumeRecord == null) {
                errors.add("交易记录不存在");
                return buildValidationResult(false, errors, warnings);
            }

            // 2. 验证原交易金额
            BigDecimal originalAmount = consumeRecord.getAmount();
            if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("原交易金额无效");
                return buildValidationResult(false, errors, warnings);
            }

            // 3. 验证退款金额
            if (refundRequest.getRefundAmount() == null) {
                errors.add("退款金额不能为空");
            } else if (refundRequest.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("退款金额必须大于0");
            } else if (refundRequest.getRefundAmount().compareTo(originalAmount) > 0) {
                errors.add("退款金额不能超过原交易金额");
            }

            // 4. 验证退款原因
            if (refundRequest.getRefundReason() == null || refundRequest.getRefundReason().trim().isEmpty()) {
                errors.add("退款原因不能为空");
            } else if (refundRequest.getRefundReason().length() > 500) {
                errors.add("退款原因长度不能超过500个字符");
            }

            // 5. 检查是否已有退款申请
            LambdaQueryWrapper<RefundApplicationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RefundApplicationEntity::getPaymentRecordId, consumeRecord.getId());
            wrapper.in(RefundApplicationEntity::getStatus,
                    Arrays.asList("PENDING", "APPROVED", "PROCESSING"));
            wrapper.eq(RefundApplicationEntity::getDeletedFlag, false);
            long existingRefundCount = refundApplicationDao.selectCount(wrapper);
            if (existingRefundCount > 0) {
                errors.add("该交易已有待处理或已审批的退款申请");
            }

            // 6. 计算可退款金额
            List<RefundApplicationEntity> completedRefunds = refundApplicationDao.selectList(
                    new LambdaQueryWrapper<RefundApplicationEntity>()
                            .eq(RefundApplicationEntity::getPaymentRecordId, consumeRecord.getId())
                            .in(RefundApplicationEntity::getStatus, Arrays.asList("APPROVED", "PROCESSING"))
                            .eq(RefundApplicationEntity::getDeletedFlag, false)
            );
            BigDecimal totalRefunded = completedRefunds.stream()
                    .map(RefundApplicationEntity::getRefundAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal availableAmount = originalAmount.subtract(totalRefunded);
            if (refundRequest.getRefundAmount() != null &&
                    refundRequest.getRefundAmount().compareTo(availableAmount) > 0) {
                errors.add("退款金额超过可退款金额，可退款金额为：" + availableAmount);
            }

            // 7. 检查退款时间限制（7天内）
            LocalDateTime transactionTime = consumeRecord.getCreateTime();
            if (transactionTime != null) {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                        transactionTime.toLocalDate(), LocalDate.now());
                if (daysBetween > 7) {
                    warnings.add("交易已超过7天，可能不符合退款政策");
                }
            }

            // 8. 构建验证结果
            boolean valid = errors.isEmpty();
            return buildValidationResult(valid, errors, warnings);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款服务] 验证退款申请参数错误，transactionNo={}, error={}", refundRequest.getTransactionNo(), e.getMessage());
            errors.add("参数错误：" + e.getMessage());
            return buildValidationResult(false, errors, warnings);
        } catch (BusinessException e) {
            log.warn("[退款服务] 验证退款申请业务异常，transactionNo={}, code={}, message={}", refundRequest.getTransactionNo(), e.getCode(), e.getMessage());
            errors.add("业务异常：" + e.getMessage());
            return buildValidationResult(false, errors, warnings);
        } catch (SystemException e) {
            log.error("[退款服务] 验证退款申请系统异常，transactionNo={}, code={}, message={}", refundRequest.getTransactionNo(), e.getCode(), e.getMessage(), e);
            errors.add("系统异常，请稍后重试");
            return buildValidationResult(false, errors, warnings);
        } catch (Exception e) {
            log.error("[退款服务] 验证退款申请未知异常，transactionNo={}", refundRequest.getTransactionNo(), e);
            errors.add("系统异常，请稍后重试");
            return buildValidationResult(false, errors, warnings);
        }
    }

    /**
     * 构建验证结果
     *
     * @param valid 是否有效
     * @param errors 错误列表
     * @param warnings 警告列表
     * @return 验证结果
     */
    private Map<String, Object> buildValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
    }

    @Override
    @Observed(name = "consume.refund.getRefundPolicy", contextualName = "consume-refund-get-policy")
    @Transactional(readOnly = true)
    public Map<String, Object> getRefundPolicy() {
        log.info("[退款服务] 获取退款政策信息");

        try {
            // 构建退款政策信息
            // 注意：实际项目中，这些政策信息应该从配置中心或数据库读取
            Map<String, Object> policyInfo = new HashMap<>();

            // 退款时间限制
            policyInfo.put("refundTimeLimit", "7天");
            policyInfo.put("refundTimeLimitDays", 7);

            // 退款条件
            List<String> conditions = new ArrayList<>();
            conditions.add("交易记录存在且有效");
            conditions.add("交易时间在7天内");
            conditions.add("退款金额不超过原交易金额");
            conditions.add("无待处理或已审批的退款申请");
            policyInfo.put("refundConditions", conditions);

            // 退款方式
            List<Map<String, Object>> refundMethods = new ArrayList<>();
            Map<String, Object> method1 = new HashMap<>();
            method1.put("code", 1);
            method1.put("name", "原路退回");
            method1.put("description", "退回到原支付方式");
            refundMethods.add(method1);

            Map<String, Object> method2 = new HashMap<>();
            method2.put("code", 2);
            method2.put("name", "退回余额");
            method2.put("description", "退回到账户余额");
            refundMethods.add(method2);

            Map<String, Object> method3 = new HashMap<>();
            method3.put("code", 3);
            method3.put("name", "退回银行卡");
            method3.put("description", "退回到指定银行卡");
            refundMethods.add(method3);

            policyInfo.put("refundMethods", refundMethods);

            // 退款手续费
            policyInfo.put("refundFee", "免费");
            policyInfo.put("refundFeeAmount", BigDecimal.ZERO);

            // 退款流程说明
            List<String> processSteps = new ArrayList<>();
            processSteps.add("1. 提交退款申请");
            processSteps.add("2. 等待审批（1-3个工作日）");
            processSteps.add("3. 审批通过后处理退款");
            processSteps.add("4. 退款到账（1-3个工作日）");
            policyInfo.put("refundProcess", processSteps);

            // 退款状态说明
            Map<String, String> statusDescriptions = new HashMap<>();
            statusDescriptions.put("PENDING", "待审批：退款申请已提交，等待审批");
            statusDescriptions.put("APPROVED", "已审批：退款申请已通过审批");
            statusDescriptions.put("REJECTED", "已拒绝：退款申请被拒绝");
            statusDescriptions.put("PROCESSING", "处理中：退款正在处理中");
            statusDescriptions.put("CANCELLED", "已取消：退款申请已取消");
            policyInfo.put("statusDescriptions", statusDescriptions);

            log.info("[退款服务] 获取退款政策信息成功");

            return policyInfo;

        } catch (BusinessException e) {
            log.warn("[退款服务] 获取退款政策信息业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款服务] 获取退款政策信息系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_POLICY_SYSTEM_ERROR", "获取退款政策信息失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款服务] 获取退款政策信息未知异常", e);
            throw new SystemException("REFUND_POLICY_SYSTEM_ERROR", "获取退款政策信息失败：" + e.getMessage(), e);
        }
    }
}



