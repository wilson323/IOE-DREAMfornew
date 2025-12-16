package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessPermissionApplyDao;
import net.lab1024.sa.access.domain.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessPermissionApplyService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * 门禁权限申请服务实现类
 * <p>
 * 实现门禁权限申请相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Transactional管理事务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessPermissionApplyServiceImpl implements AccessPermissionApplyService {

    @Resource
    private AccessPermissionApplyDao accessPermissionApplyDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    /**
     * 提交权限申请（启动审批流程）
     * <p>
     * 实现步骤：
     * 1. 创建申请实体并保存到数据库
     * 2. 启动工作流审批流程
     * 3. 更新申请实体的工作流实例ID
     * </p>
     *
     * @param form 权限申请表单
     * @return 权限申请实体（包含workflowInstanceId）
     */
    @Override
    public AccessPermissionApplyEntity submitPermissionApply(AccessPermissionApplyForm form) {
        log.info("[权限申请] 提交权限申请，applicantId={}", form.getApplicantId());

        // 1. 创建申请实体
        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo("AP" + System.currentTimeMillis());
        entity.setApplicantId(form.getApplicantId());
        entity.setAreaId(form.getAreaId());
        entity.setApplyType(form.getApplyType() != null ? form.getApplyType() : "NORMAL");
        entity.setApplyReason(form.getApplyReason());
        entity.setStartTime(form.getStartTime());
        entity.setEndTime(form.getEndTime());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());

        // 2. 保存到数据库
        accessPermissionApplyDao.insert(entity);
        log.info("[权限申请] 申请已保存到数据库，applyNo={}, id={}", entity.getApplyNo(), entity.getId());

        // 3. 启动审批流程
        String businessType = "NORMAL".equals(entity.getApplyType())
            ? "ACCESS_PERMISSION_APPLY"
            : "ACCESS_EMERGENCY_PERMISSION_APPLY";

        Map<String, Object> formData = new HashMap<>();
        formData.put("applyNo", entity.getApplyNo());
        formData.put("applicantId", entity.getApplicantId());
        formData.put("areaId", entity.getAreaId());
        formData.put("applyType", entity.getApplyType());
        formData.put("applyReason", entity.getApplyReason());
        formData.put("startTime", entity.getStartTime());
        formData.put("endTime", entity.getEndTime());

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
            null, // definitionId为null，从审批配置中获取
            entity.getApplyNo(), // businessKey使用申请编号
            "门禁权限申请-" + entity.getApplyNo(), // instanceName
            entity.getApplicantId(), // initiatorId
            businessType, // businessType
            formData, // formData
            null // variables
        );

        if (workflowResult.getCode() == 200 && workflowResult.getData() != null) {
            // 4. 更新工作流实例ID
            entity.setWorkflowInstanceId(workflowResult.getData());
            accessPermissionApplyDao.updateById(entity);
            log.info("[权限申请] 审批流程已启动，applyNo={}, workflowInstanceId={}",
                entity.getApplyNo(), entity.getWorkflowInstanceId());
        } else {
            log.error("[权限申请] 启动审批流程失败，applyNo={}, error={}",
                entity.getApplyNo(), workflowResult.getMessage());
            // 审批流程启动失败不影响申请记录保存，但需要记录错误
        }

        log.info("[权限申请] 申请提交成功，applyNo={}", entity.getApplyNo());
        return entity;
    }

    /**
     * 更新权限申请状态（由审批结果监听器调用）
     * <p>
     * 实现步骤：
     * 1. 根据申请编号查询申请记录
     * 2. 更新申请状态、审批意见、审批时间
     * 3. 保存到数据库
     * </p>
     *
     * @param applyNo 申请编号
     * @param status 审批状态（APPROVED/REJECTED）
     * @param approvalComment 审批意见
     */
    @Override
    public void updatePermissionApplyStatus(String applyNo, String status, String approvalComment) {
        log.info("[权限申请] 更新申请状态，applyNo={}, status={}", applyNo, status);

        // 1. 根据申请编号查询申请记录
        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[权限申请] 申请记录不存在，applyNo={}", applyNo);
            return;
        }

        // 2. 更新申请状态
        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());

        // 3. 保存到数据库
        accessPermissionApplyDao.updateById(entity);
        log.info("[权限申请] 申请状态已更新，applyNo={}, status={}", applyNo, status);
    }
}
