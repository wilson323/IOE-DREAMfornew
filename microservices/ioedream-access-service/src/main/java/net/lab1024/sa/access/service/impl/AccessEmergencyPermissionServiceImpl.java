package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.access.dao.AccessPermissionApplyDao;
import net.lab1024.sa.access.dao.AreaPersonDao;
import net.lab1024.sa.access.domain.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessEmergencyPermissionService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaPersonEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;
import org.springframework.http.HttpMethod;

/**
 * 紧急权限申请服务实现类
 * <p>
 * 实现紧急权限申请相关业务功能，集成工作流审批
 * 特点：
 * - 简化审批流程（快速审批）
 * - 临时权限生效
 * - 自动过期机制（24小时后自动失效）
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-access-service中
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
public class AccessEmergencyPermissionServiceImpl implements AccessEmergencyPermissionService {

    /**
     * 紧急权限有效期（小时）
     */
    private static final int EMERGENCY_PERMISSION_VALID_HOURS = 24;

    @Resource
    private AccessPermissionApplyDao accessPermissionApplyDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private AreaPersonDao areaPersonDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessPermissionApplyEntity submitEmergencyPermissionApply(AccessPermissionApplyForm form) {
        log.info("[紧急权限申请] 提交紧急权限申请，applicantId={}, areaId={}, reason={}",
                form.getApplicantId(), form.getAreaId(), form.getApplyReason());

        // 1. 创建紧急权限申请记录
        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo(generateEmergencyApplyNo());
        entity.setApplicantId(form.getApplicantId());
        // 从用户服务获取申请人姓名
        String applicantName = getUserNameById(form.getApplicantId());
        entity.setApplicantName(applicantName != null ? applicantName : "未知用户");
        entity.setAreaId(form.getAreaId());
        // 从区域服务获取区域名称
        String areaName = getAreaNameById(form.getAreaId());
        entity.setAreaName(areaName != null ? areaName : "未知区域");
        entity.setApplyType("EMERGENCY"); // 固定为紧急权限
        entity.setApplyReason(form.getApplyReason());

        // 紧急权限：开始时间立即生效，结束时间24小时后
        LocalDateTime now = LocalDateTime.now();
        entity.setStartTime(form.getStartTime() != null ? form.getStartTime() : now);
        entity.setEndTime(form.getEndTime() != null ? form.getEndTime() : now.plusHours(EMERGENCY_PERMISSION_VALID_HOURS));

        entity.setRemark("紧急权限申请，有效期24小时");
        entity.setStatus("PENDING");
        accessPermissionApplyDao.insert(entity);

        // 2. 构建表单数据（用于审批流程）
        Map<String, Object> formData = new HashMap<>();
        formData.put("applyNo", entity.getApplyNo());
        formData.put("applicantId", form.getApplicantId());
        formData.put("applicantName", entity.getApplicantName());
        formData.put("areaId", form.getAreaId());
        formData.put("areaName", entity.getAreaName());
        formData.put("applyType", "EMERGENCY");
        formData.put("applyReason", form.getApplyReason());
        formData.put("startTime", entity.getStartTime());
        formData.put("endTime", entity.getEndTime());
        formData.put("applyTime", now);
        formData.put("validHours", EMERGENCY_PERMISSION_VALID_HOURS); // 有效期标识

        // 3. 构建流程变量（紧急权限流程变量）
        Map<String, Object> variables = new HashMap<>();
        variables.put("emergency", true); // 标识为紧急权限
        variables.put("validHours", EMERGENCY_PERMISSION_VALID_HOURS); // 有效期

        // 4. 启动快速审批流程（使用紧急权限流程定义）
        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.ACCESS_EMERGENCY_PERMISSION, // 紧急权限流程定义ID
                entity.getApplyNo(), // 业务Key
                "紧急权限申请-" + entity.getApplyNo(), // 流程实例名称
                form.getApplicantId(), // 发起人ID
                BusinessTypeEnum.ACCESS_EMERGENCY_PERMISSION.name(), // 业务类型
                formData, // 表单数据
                variables // 流程变量
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[紧急权限申请] 启动审批流程失败，applyNo={}", entity.getApplyNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        // 5. 更新紧急权限申请的workflowInstanceId
        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        accessPermissionApplyDao.updateById(entity);

        log.info("[紧急权限申请] 紧急权限申请提交成功，applyNo={}, workflowInstanceId={}, validHours={}",
                entity.getApplyNo(), workflowInstanceId, EMERGENCY_PERMISSION_VALID_HOURS);

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmergencyPermissionStatus(String applyNo, String status, String approvalComment) {
        log.info("[紧急权限申请] 更新紧急权限状态，applyNo={}, status={}", applyNo, status);

        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[紧急权限申请] 紧急权限申请不存在，applyNo={}", applyNo);
            return;
        }

        // 验证是否为紧急权限申请
        if (!"EMERGENCY".equals(entity.getApplyType())) {
            log.warn("[紧急权限申请] 申请类型不匹配，applyNo={}, applyType={}", applyNo, entity.getApplyType());
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        accessPermissionApplyDao.updateById(entity);

        // 如果审批通过，执行紧急权限授予逻辑
        if ("APPROVED".equals(status)) {
            executeEmergencyPermissionGrant(entity);
        }

        log.info("[紧急权限申请] 紧急权限状态更新成功，applyNo={}, status={}", applyNo, status);
    }

    /**
     * 执行紧急权限授予逻辑
     * <p>
     * 紧急权限特点：
     * - 审批通过后立即生效
     * - 有效期24小时（自动过期）
     * - 临时权限，过期后自动回收
     * </p>
     *
     * @param entity 权限申请实体
     */
    private void executeEmergencyPermissionGrant(AccessPermissionApplyEntity entity) {
        log.info("[紧急权限申请] 执行紧急权限授予，applyNo={}, applicantId={}, areaId={}, validHours={}",
                entity.getApplyNo(), entity.getApplicantId(), entity.getAreaId(), EMERGENCY_PERMISSION_VALID_HOURS);

        try {
            // 1. 检查是否已有权限（紧急权限可以覆盖现有权限）
            boolean hasPermission = areaPersonDao.hasAreaPermission(entity.getApplicantId(), entity.getAreaId());
            if (hasPermission) {
                log.info("[紧急权限申请] 用户已有该区域权限，更新为紧急权限，applicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
                updateToEmergencyPermission(entity);
            } else {
                // 2. 创建紧急权限关联记录
                createEmergencyPermission(entity);
            }

            // 3. 同步权限到门禁设备
            syncEmergencyPermissionToDevices(entity);

            log.info("[紧急权限申请] 紧急权限授予成功，applyNo={}, applicantId={}, areaId={}",
                    entity.getApplyNo(), entity.getApplicantId(), entity.getAreaId());

        } catch (BusinessException e) {
            log.error("[紧急权限申请] 紧急权限授予业务异常，applyNo={}", entity.getApplyNo(), e);
            throw e;
        } catch (Exception e) {
            log.error("[紧急权限申请] 紧急权限授予处理异常，applyNo={}", entity.getApplyNo(), e);
            throw new BusinessException("紧急权限授予处理异常：" + e.getMessage());
        }
    }

    /**
     * 创建紧急权限关联记录
     *
     * @param entity 权限申请实体
     */
    private void createEmergencyPermission(AccessPermissionApplyEntity entity) {
        AreaPersonEntity areaPerson = new AreaPersonEntity();
        areaPerson.setPersonId(entity.getApplicantId());
        areaPerson.setAreaId(entity.getAreaId());
        areaPerson.setAccessLevel(2); // 紧急权限访问级别：2-紧急访问
        areaPerson.setAccessReason("紧急权限：" + entity.getApplyReason());
        areaPerson.setGrantUserId(entity.getApplicantId());
        areaPerson.setGrantTime(LocalDateTime.now());
        areaPerson.setValidStartTime(entity.getStartTime() != null ? entity.getStartTime() : LocalDateTime.now());
        areaPerson.setValidEndTime(entity.getEndTime());
        areaPerson.setExpireTime(entity.getEndTime()); // 过期时间与结束时间一致
        areaPerson.setStatus(1); // 1-有效

        int insertResult = areaPersonDao.insert(areaPerson);
        if (insertResult <= 0) {
            log.error("[紧急权限申请] 紧急权限关联记录创建失败，applicantId={}, areaId={}, applyNo={}",
                    entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
            throw new BusinessException("紧急权限关联记录创建失败");
        }

        log.info("[紧急权限申请] 紧急权限关联记录创建成功，relationId={}, applicantId={}, areaId={}, applyNo={}",
                areaPerson.getId(), entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
    }

    /**
     * 更新现有权限为紧急权限
     *
     * @param entity 权限申请实体
     */
    private void updateToEmergencyPermission(AccessPermissionApplyEntity entity) {
        try {
            // 查询现有权限
            List<AreaPersonEntity> existingPermissions = areaPersonDao.getEffectivePermissionsByTimeRange(
                    entity.getApplicantId(),
                    LocalDateTime.now(),
                    entity.getEndTime() != null ? entity.getEndTime() : LocalDateTime.now().plusHours(EMERGENCY_PERMISSION_VALID_HOURS)
            );

            // 更新为紧急权限
            if (existingPermissions != null) {
                for (AreaPersonEntity permission : existingPermissions) {
                    if (permission.getAreaId().equals(entity.getAreaId())) {
                        permission.setAccessLevel(2); // 更新为紧急权限级别
                        permission.setAccessReason("紧急权限：" + entity.getApplyReason());
                        permission.setValidEndTime(entity.getEndTime());
                        permission.setExpireTime(entity.getEndTime());
                        areaPersonDao.updateById(permission);
                        log.info("[紧急权限申请] 权限已更新为紧急权限，relationId={}, expireTime={}",
                                permission.getId(), entity.getEndTime());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("[紧急权限申请] 更新权限为紧急权限异常，applicantId={}, areaId={}",
                    entity.getApplicantId(), entity.getAreaId(), e);
            throw new BusinessException("更新权限为紧急权限异常：" + e.getMessage());
        }
    }

    /**
     * 同步紧急权限到门禁设备
     *
     * @param entity 权限申请实体
     */
    private void syncEmergencyPermissionToDevices(AccessPermissionApplyEntity entity) {
        log.info("[紧急权限申请] 同步紧急权限到门禁设备，applicantId={}, areaId={}, applyNo={}",
                entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());

        try {
            // 构建权限同步请求
            Map<String, Object> syncRequest = new HashMap<>();
            syncRequest.put("personId", entity.getApplicantId());
            syncRequest.put("areaId", entity.getAreaId());
            syncRequest.put("validStartTime", entity.getStartTime());
            syncRequest.put("validEndTime", entity.getEndTime());
            syncRequest.put("emergency", true); // 标识为紧急权限
            syncRequest.put("validHours", EMERGENCY_PERMISSION_VALID_HOURS);

            // 通过网关调用设备管理服务同步权限
            ResponseDTO<Void> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/permission/sync-emergency",
                    HttpMethod.POST,
                    syncRequest,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("[紧急权限申请] 紧急权限同步到设备成功，applicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
            } else {
                log.warn("[紧急权限申请] 紧急权限同步到设备失败，但不影响权限授予，applicantId={}, areaId={}, applyNo={}, message={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo(),
                        response != null ? response.getMessage() : "未知错误");
            }

        } catch (Exception e) {
            log.error("[紧急权限申请] 紧急权限同步到设备异常，但不影响权限授予，applicantId={}, areaId={}, applyNo={}",
                    entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndRevokeExpiredPermission(String applyNo) {
        log.info("[紧急权限申请] 检查并回收过期权限，applyNo={}", applyNo);

        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[紧急权限申请] 紧急权限申请不存在，applyNo={}", applyNo);
            return false;
        }

        // 验证是否为紧急权限申请
        if (!"EMERGENCY".equals(entity.getApplyType())) {
            log.warn("[紧急权限申请] 申请类型不匹配，applyNo={}, applyType={}", applyNo, entity.getApplyType());
            return false;
        }

        // 检查是否已过期
        LocalDateTime now = LocalDateTime.now();
        if (entity.getEndTime() != null && now.isAfter(entity.getEndTime())) {
            log.info("[紧急权限申请] 紧急权限已过期，开始回收，applyNo={}, endTime={}", applyNo, entity.getEndTime());

            // 回收权限
            revokeEmergencyPermission(entity);
            return true;
        }

        return false;
    }

    /**
     * 回收紧急权限
     *
     * @param entity 权限申请实体
     */
    private void revokeEmergencyPermission(AccessPermissionApplyEntity entity) {
        try {
            // 1. 更新权限状态为失效
            List<AreaPersonEntity> existingPermissions = areaPersonDao.getEffectivePermissionsByTimeRange(
                    entity.getApplicantId(),
                    LocalDateTime.now().minusHours(EMERGENCY_PERMISSION_VALID_HOURS),
                    LocalDateTime.now()
            );

            if (existingPermissions != null) {
                for (AreaPersonEntity permission : existingPermissions) {
                    if (permission.getAreaId().equals(entity.getAreaId())) {
                        permission.setStatus(0); // 0-已失效
                        permission.setExpireTime(LocalDateTime.now());
                        areaPersonDao.updateById(permission);
                        log.info("[紧急权限申请] 紧急权限已回收，relationId={}, applyNo={}",
                                permission.getId(), entity.getApplyNo());
                        break;
                    }
                }
            }

            // 2. 从设备中移除权限
            removePermissionFromDevices(entity);

            log.info("[紧急权限申请] 紧急权限回收成功，applyNo={}", entity.getApplyNo());

        } catch (Exception e) {
            log.error("[紧急权限申请] 回收紧急权限异常，applyNo={}", entity.getApplyNo(), e);
        }
    }

    /**
     * 从设备中移除权限
     *
     * @param entity 权限申请实体
     */
    private void removePermissionFromDevices(AccessPermissionApplyEntity entity) {
        try {
            Map<String, Object> revokeRequest = new HashMap<>();
            revokeRequest.put("personId", entity.getApplicantId());
            revokeRequest.put("areaId", entity.getAreaId());
            revokeRequest.put("emergency", true);

            ResponseDTO<Void> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/permission/revoke-emergency",
                    HttpMethod.POST,
                    revokeRequest,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("[紧急权限申请] 从设备移除权限成功，applicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
            }

        } catch (Exception e) {
            log.error("[紧急权限申请] 从设备移除权限异常，applyNo={}", entity.getApplyNo(), e);
        }
    }

    /**
     * 定时任务：检查并回收过期紧急权限
     * <p>
     * 每小时执行一次，检查所有过期的紧急权限并自动回收
     * </p>
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void scheduledRevokeExpiredPermissions() {
        log.info("[紧急权限申请] 开始执行定时任务：回收过期紧急权限");

        try {
            // 查询所有已过期的紧急权限申请
            LocalDateTime currentTime = LocalDateTime.now();
            List<AccessPermissionApplyEntity> expiredPermissions = accessPermissionApplyDao.selectExpiredEmergencyPermissions(currentTime);

            if (expiredPermissions == null || expiredPermissions.isEmpty()) {
                log.info("[紧急权限申请] 没有发现已过期的紧急权限申请");
                return;
            }

            log.info("[紧急权限申请] 发现{}条已过期的紧急权限申请，开始回收", expiredPermissions.size());

            // 逐个回收过期权限
            int successCount = 0;
            int failCount = 0;
            for (AccessPermissionApplyEntity entity : expiredPermissions) {
                try {
                    boolean revoked = checkAndRevokeExpiredPermission(entity.getApplyNo());
                    if (revoked) {
                        successCount++;
                        log.info("[紧急权限申请] 成功回收过期权限，applyNo={}", entity.getApplyNo());
                    } else {
                        failCount++;
                        log.warn("[紧急权限申请] 回收过期权限失败，applyNo={}", entity.getApplyNo());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("[紧急权限申请] 回收过期权限异常，applyNo={}", entity.getApplyNo(), e);
                }
            }

            log.info("[紧急权限申请] 定时任务执行完成：回收过期紧急权限，成功={}, 失败={}, 总计={}",
                    successCount, failCount, expiredPermissions.size());

        } catch (Exception e) {
            log.error("[紧急权限申请] 定时任务执行异常：回收过期紧急权限", e);
        }
    }

    /**
     * 生成紧急权限申请编号
     *
     * @return 申请编号
     */
    private String generateEmergencyApplyNo() {
        return "EP" + System.currentTimeMillis();
    }

    /**
     * 根据用户ID获取用户姓名
     * <p>
     * 通过网关调用公共服务获取用户信息
     * 优先返回realName，如果为空则返回username
     * </p>
     *
     * @param userId 用户ID
     * @return 用户姓名，获取失败返回null
     */
    private String getUserNameById(Long userId) {
        if (userId == null) {
            log.warn("[紧急权限申请] 用户ID为空，无法获取用户姓名");
            return null;
        }

        try {
            ResponseDTO<UserDetailVO> userResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/users/" + userId,
                    HttpMethod.GET,
                    null,
                    UserDetailVO.class
            );

            if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                UserDetailVO userDetail = userResponse.getData();
                // 优先使用realName，如果为空则使用username
                String userName = userDetail.getRealName() != null && !userDetail.getRealName().trim().isEmpty()
                        ? userDetail.getRealName()
                        : userDetail.getUsername();
                log.debug("[紧急权限申请] 获取用户姓名成功，userId={}, userName={}", userId, userName);
                return userName;
            } else {
                log.warn("[紧急权限申请] 获取用户信息失败，userId={}, message={}",
                        userId, userResponse != null ? userResponse.getMessage() : "未知错误");
                return null;
            }
        } catch (Exception e) {
            log.error("[紧急权限申请] 获取用户信息异常，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 根据区域ID获取区域名称
     * <p>
     * 通过网关调用公共服务获取区域信息
     * </p>
     *
     * @param areaId 区域ID
     * @return 区域名称，获取失败返回null
     */
    private String getAreaNameById(Long areaId) {
        if (areaId == null) {
            log.warn("[紧急权限申请] 区域ID为空，无法获取区域名称");
            return null;
        }

        try {
            ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );

            if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                AreaEntity area = areaResponse.getData();
                log.debug("[紧急权限申请] 获取区域名称成功，areaId={}, areaName={}", areaId, area.getAreaName());
                return area.getAreaName();
            } else {
                log.warn("[紧急权限申请] 获取区域信息失败，areaId={}, message={}",
                        areaId, areaResponse != null ? areaResponse.getMessage() : "未知错误");
                return null;
            }
        } catch (Exception e) {
            log.error("[紧急权限申请] 获取区域信息异常，areaId={}", areaId, e);
            return null;
        }
    }
}

