package net.lab1024.sa.consume.service.impl.reimbursement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.consume.dao.ReimbursementApplicationDao;
import net.lab1024.sa.common.consume.entity.ReimbursementApplicationEntity;
import net.lab1024.sa.consume.domain.form.ReimbursementApplicationForm;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.service.reimbursement.ReimbursementApplicationService;

/**
 * 报销申请服务实现类
 * <p>
 * 实现报销申请相关业务功能，集成工作流审批
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-consume-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ReimbursementApplicationServiceImpl implements ReimbursementApplicationService {

    @Resource
    private ReimbursementApplicationDao reimbursementApplicationDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private AccountManager accountManager;

    @Override
    @Observed(name = "consume.reimbursementApplication.submitReimbursementApplication", contextualName = "consume-reimbursement-application-submit")
    @Transactional(rollbackFor = Exception.class)
    @Retry(name = "external-service-retry", fallbackMethod = "submitReimbursementApplicationFallback")
    public ReimbursementApplicationEntity submitReimbursementApplication(ReimbursementApplicationForm form) {
        log.info("[报销申请] 提交报销申请，userId={}, amount={}, type={}",
                form.getUserId(), form.getReimbursementAmount(), form.getReimbursementType());

        // 1. 创建报销申请记录
        ReimbursementApplicationEntity entity = new ReimbursementApplicationEntity();
        entity.setReimbursementNo(generateReimbursementNo());
        entity.setUserId(form.getUserId());
        entity.setReimbursementAmount(form.getReimbursementAmount());
        entity.setReimbursementType(form.getReimbursementType());
        entity.setReason(form.getReason());
        entity.setStatus("PENDING");
        reimbursementApplicationDao.insert(entity);

        // 2. 构建表单数据（用于审批流程）
        Map<String, Object> formData = new HashMap<>();
        formData.put("reimbursementNo", entity.getReimbursementNo());
        formData.put("userId", form.getUserId());
        formData.put("reimbursementAmount", form.getReimbursementAmount());
        formData.put("reimbursementType", form.getReimbursementType());
        formData.put("reason", form.getReason());
        formData.put("applyTime", LocalDateTime.now());

        // 3. 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", form.getReimbursementAmount()); // 用于金额判断（如：超过5000需要财务审批）

        // 4. 启动审批流程
        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.CONSUME_REIMBURSEMENT, // 流程定义ID
                entity.getReimbursementNo(), // 业务Key
                "报销申请-" + entity.getReimbursementNo(), // 流程实例名称
                form.getUserId(), // 发起人ID
                BusinessTypeEnum.CONSUME_REIMBURSEMENT.name(), // 业务类型
                formData, // 表单数据
                variables // 流程变量
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[报销申请] 启动审批流程失败，reimbursementNo={}", entity.getReimbursementNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        // 5. 更新报销申请的workflowInstanceId
        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        reimbursementApplicationDao.updateById(entity);

        log.info("[报销申请] 报销申请提交成功，reimbursementNo={}, workflowInstanceId={}",
                entity.getReimbursementNo(), workflowInstanceId);

        return entity;
    }

    /**
     * 报销申请提交降级方法
     * <p>
     * 当工作流服务调用失败时，使用此降级方法
     * </p>
     */
    public ReimbursementApplicationEntity submitReimbursementApplicationFallback(
            ReimbursementApplicationForm form, Exception e) {
        log.error("[报销申请] 启动审批流程失败，使用降级方案, userId={}, error={}", form.getUserId(), e.getMessage(), e);
        throw new BusinessException("启动审批流程失败: " + e.getMessage());
    }

    @Override
    @Observed(name = "consume.reimbursementApplication.updateReimbursementStatus", contextualName = "consume-reimbursement-application-update-status")
    @Transactional(rollbackFor = Exception.class)
    public void updateReimbursementStatus(String reimbursementNo, String status, String approvalComment) {
        log.info("[报销申请] 更新报销状态，reimbursementNo={}, status={}", reimbursementNo, status);

        ReimbursementApplicationEntity entity = reimbursementApplicationDao.selectByReimbursementNo(reimbursementNo);
        if (entity == null) {
            log.warn("[报销申请] 报销申请不存在，reimbursementNo={}", reimbursementNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        reimbursementApplicationDao.updateById(entity);

        // 如果审批通过，执行报销逻辑
        if ("APPROVED".equals(status)) {
            executeReimbursement(entity);
        }

        log.info("[报销申请] 报销状态更新成功，reimbursementNo={}, status={}", reimbursementNo, status);
    }

    /**
     * 执行报销逻辑
     * <p>
     * 报销流程：
     * 1. 查询用户账户
     * 2. 增加账户余额
     * 3. 记录报销流水（可选，根据业务需求）
     * </p>
     *
     * @param entity 报销申请实体
     */
    private void executeReimbursement(ReimbursementApplicationEntity entity) {
        log.info("[报销申请] 执行报销，reimbursementNo={}, userId={}, amount={}, type={}",
                entity.getReimbursementNo(), entity.getUserId(), entity.getReimbursementAmount(),
                entity.getReimbursementType());

        try {
            // 1. 查询用户账户
            var account = accountManager.getAccountByUserId(entity.getUserId());
            if (account == null) {
                log.error("[报销申请] 用户账户不存在，userId={}, reimbursementNo={}",
                        entity.getUserId(), entity.getReimbursementNo());
                throw new BusinessException("用户账户不存在");
            }

            // 2. 增加账户余额
            boolean success = accountManager.addBalance(account.getId(), entity.getReimbursementAmount());
            if (!success) {
                log.error("[报销申请] 账户余额增加失败，accountId={}, amount={}, reimbursementNo={}",
                        account.getId(), entity.getReimbursementAmount(), entity.getReimbursementNo());
                throw new BusinessException("账户余额增加失败，请重试");
            }

            log.info("[报销申请] 报销处理成功，reimbursementNo={}, accountId={}, amount={}, type={}",
                    entity.getReimbursementNo(), account.getId(), entity.getReimbursementAmount(),
                    entity.getReimbursementType());

            // 3. 记录报销流水（可选）
            // 如果需要记录报销流水到交易表，可以在这里添加
            // 目前报销直接增加账户余额，不记录交易流水，符合一般报销业务逻辑

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[报销申请] 报销处理参数错误，reimbursementNo={}, error={}", entity.getReimbursementNo(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[报销申请] 报销业务异常，reimbursementNo={}, code={}, message={}", entity.getReimbursementNo(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[报销申请] 报销处理系统异常，reimbursementNo={}, code={}, message={}", entity.getReimbursementNo(), e.getCode(), e.getMessage(), e);
            throw new SystemException("REIMBURSEMENT_PROCESS_SYSTEM_ERROR", "报销处理异常：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[报销申请] 报销处理未知异常，reimbursementNo={}", entity.getReimbursementNo(), e);
            throw new SystemException("REIMBURSEMENT_PROCESS_SYSTEM_ERROR", "报销处理异常：" + e.getMessage(), e);
        }
    }

    /**
     * 生成报销申请编号
     *
     * @return 报销申请编号
     */
    private String generateReimbursementNo() {
        return "RB" + System.currentTimeMillis();
    }
}




