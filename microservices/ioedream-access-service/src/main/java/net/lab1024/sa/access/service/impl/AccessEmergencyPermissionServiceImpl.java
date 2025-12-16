package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessPermissionApplyDao;
import net.lab1024.sa.access.dao.AreaPersonDao;
import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessEmergencyPermissionService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.entity.AreaPersonEntity;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 紧急权限申请服务实现类
 * <p>
 * 提供紧急权限申请相关业务功能实现
 * 特点：
 * - 简化审批流程（快速审批）
 * - 临时权限生效
 * - 自动过期机制（24小时后自动失效）
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Transactional管理事务
 * - 使用@Resource注入依赖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessEmergencyPermissionServiceImpl implements AccessEmergencyPermissionService {

    @Resource
    private AccessPermissionApplyDao accessPermissionApplyDao;

    @Resource
    private AreaPersonDao areaPersonDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    /**
     * 提交紧急权限申请（启动快速审批流程）
     * <p>
     * 实现步骤：
     * 1. 创建申请实体并保存到数据库
     * 2. 通过网关调用工作流服务启动快速审批流程
     * 3. 返回包含workflowInstanceId的申请实体
     * </p>
     *
     * @param form 紧急权限申请表单
     * @return 权限申请实体（包含workflowInstanceId）
     */
    @Override
    public AccessPermissionApplyEntity submitEmergencyPermissionApply(AccessPermissionApplyForm form) {
        log.info("[紧急权限申请] 提交申请，applicantId={}, reason={}", form.getApplicantId(), form.getApplyReason());

        // 创建申请实体
        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo("EMERGENCY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setApplicantId(form.getApplicantId());
        entity.setAreaId(form.getAreaId());
        entity.setApplyType("EMERGENCY");
        entity.setApplyReason(form.getApplyReason());
        entity.setStatus("PENDING");
        entity.setCreateTime(LocalDateTime.now());
        entity.setStartTime(LocalDateTime.now());
        entity.setEndTime(LocalDateTime.now().plusHours(24)); // 24小时后过期

        // 保存到数据库
        accessPermissionApplyDao.insert(entity);
        log.info("[紧急权限申请] 申请已保存到数据库，id={}, applyNo={}", entity.getId(), entity.getApplyNo());

        // 启动快速审批流程
        Map<String, Object> formData = new HashMap<>();
        formData.put("applyNo", entity.getApplyNo());
        formData.put("applicantId", entity.getApplicantId());
        formData.put("areaId", entity.getAreaId());
        formData.put("applyType", "EMERGENCY");
        formData.put("applyReason", entity.getApplyReason());
        formData.put("startTime", entity.getStartTime());
        formData.put("endTime", entity.getEndTime());
        formData.put("priority", "HIGH");

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
            null, // definitionId为null，从审批配置中获取
            entity.getApplyNo(), // businessKey使用申请编号
            "紧急权限申请-" + entity.getApplyNo(), // instanceName
            entity.getApplicantId(), // initiatorId
            "ACCESS_EMERGENCY_PERMISSION_APPLY", // businessType
            formData, // formData
            null // variables
        );

        if (workflowResult.getCode() == 200 && workflowResult.getData() != null) {
            entity.setWorkflowInstanceId(workflowResult.getData());
            accessPermissionApplyDao.updateById(entity);
            log.info("[紧急权限申请] 工作流已启动，workflowInstanceId={}, applyNo={}",
                workflowResult.getData(), entity.getApplyNo());
        } else {
            log.warn("[紧急权限申请] 工作流启动失败，applyNo={}, error={}",
                entity.getApplyNo(), workflowResult.getMessage());
            // 不抛出异常，允许申请记录保存，后续可手动处理
        }

        log.info("[紧急权限申请] 申请已创建，applyNo={}", entity.getApplyNo());
        return entity;
    }

    /**
     * 更新紧急权限申请状态（由审批结果监听器调用）
     * <p>
     * 实现步骤：
     * 1. 从数据库查询申请记录
     * 2. 更新申请状态和审批信息
     * 3. 如果审批通过，激活临时权限（添加AreaPersonEntity记录）
     * </p>
     *
     * @param applyNo 申请编号
     * @param status 审批状态
     * @param approvalComment 审批意见
     */
    @Override
    public void updateEmergencyPermissionStatus(String applyNo, String status, String approvalComment) {
        log.info("[紧急权限申请] 更新状态，applyNo={}, status={}, comment={}", applyNo, status, approvalComment);

        // 从数据库查询申请记录
        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[紧急权限申请] 申请记录不存在，applyNo={}", applyNo);
            return;
        }

        // 更新状态
        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        accessPermissionApplyDao.updateById(entity);
        log.info("[紧急权限申请] 申请状态已更新，applyNo={}, status={}", applyNo, status);

        // 如果审批通过，激活临时权限
        if ("APPROVED".equals(status)) {
            log.info("[紧急权限申请] 审批通过，激活临时权限，applyNo={}", applyNo);
            activateTemporaryPermission(entity);
        } else if ("REJECTED".equals(status)) {
            log.info("[紧急权限申请] 审批拒绝，applyNo={}", applyNo);
        }
    }

    /**
     * 检查并回收过期权限
     * <p>
     * 实现步骤：
     * 1. 从数据库查询申请记录
     * 2. 检查是否过期（endTime < 当前时间）
     * 3. 如果过期，回收权限（删除AreaPersonEntity记录）
     * </p>
     *
     * @param applyNo 申请编号
     * @return 是否已过期并回收
     */
    @Override
    public boolean checkAndRevokeExpiredPermission(String applyNo) {
        log.info("[紧急权限申请] 检查过期权限，applyNo={}", applyNo);

        // 从数据库查询申请记录
        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[紧急权限申请] 申请记录不存在，applyNo={}", applyNo);
            return false;
        }

        // 检查是否过期
        boolean isExpired = LocalDateTime.now().isAfter(entity.getEndTime());

        if (isExpired && "APPROVED".equals(entity.getStatus())) {
            log.info("[紧急权限申请] 权限已过期，执行回收，applyNo={}", applyNo);
            revokeTemporaryPermission(entity);
            return true;
        }

        return false;
    }

    /**
     * 激活门禁临时权限
     * <p>
     * 通过添加AreaPersonEntity记录来激活临时权限
     * 使用accessReason字段存储申请编号，用于后续权限回收
     * </p>
     *
     * @param entity 权限申请实体
     */
    private void activateTemporaryPermission(AccessPermissionApplyEntity entity) {
        try {
            // 检查是否已存在相同申请的权限记录（通过accessReason判断）
            List<AreaPersonEntity> existingPermissions = areaPersonDao.selectList(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(AreaPersonEntity.class)
                            .eq(AreaPersonEntity::getPersonId, entity.getApplicantId())
                            .eq(AreaPersonEntity::getAreaId, entity.getAreaId())
                            .eq(AreaPersonEntity::getAccessReason, "EMERGENCY_APPLY_" + entity.getApplyNo())
            );

            if (!existingPermissions.isEmpty()) {
                log.info("[紧急权限申请] 临时权限已存在，无需重复添加，applicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
                return;
            }

            // 创建临时权限记录
            AreaPersonEntity areaPerson = new AreaPersonEntity();
            areaPerson.setPersonId(entity.getApplicantId());
            areaPerson.setAreaId(entity.getAreaId());
            areaPerson.setValidStartTime(entity.getStartTime());
            areaPerson.setValidEndTime(entity.getEndTime());
            areaPerson.setExpireTime(entity.getEndTime());
            areaPerson.setAccessReason("EMERGENCY_APPLY_" + entity.getApplyNo()); // 使用accessReason存储申请编号
            areaPerson.setStatus(1); // 1-有效
            areaPerson.setCreateTime(LocalDateTime.now());
            areaPerson.setUpdateTime(LocalDateTime.now());

            areaPersonDao.insert(areaPerson);
            log.info("[紧急权限申请] 临时权限已激活，applicantId={}, areaId={}, applyNo={}",
                    entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
        } catch (Exception e) {
            log.error("[紧急权限申请] 激活临时权限失败，applyNo={}, error={}", entity.getApplyNo(), e.getMessage(), e);
            throw new RuntimeException("激活临时权限失败", e);
        }
    }

    /**
     * 回收门禁权限
     * <p>
     * 通过删除AreaPersonEntity记录来回收临时权限
     * 根据accessReason字段匹配申请编号
     * </p>
     *
     * @param entity 权限申请实体
     */
    private void revokeTemporaryPermission(AccessPermissionApplyEntity entity) {
        try {
            // 删除临时权限记录（根据accessReason匹配申请编号）
            int deletedCount = areaPersonDao.delete(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(AreaPersonEntity.class)
                            .eq(AreaPersonEntity::getPersonId, entity.getApplicantId())
                            .eq(AreaPersonEntity::getAreaId, entity.getAreaId())
                            .eq(AreaPersonEntity::getAccessReason, "EMERGENCY_APPLY_" + entity.getApplyNo())
            );
            log.info("[紧急权限申请] 临时权限已回收，applicantId={}, areaId={}, applyNo={}, deletedCount={}",
                    entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo(), deletedCount);
        } catch (Exception e) {
            log.error("[紧急权限申请] 回收临时权限失败，applyNo={}, error={}", entity.getApplyNo(), e.getMessage(), e);
            throw new RuntimeException("回收临时权限失败", e);
        }
    }
}
