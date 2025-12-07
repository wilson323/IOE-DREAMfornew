package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
import net.lab1024.sa.access.service.AccessPermissionApplyService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaPersonEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;
import org.springframework.http.HttpMethod;

/**
 * 门禁权限申请服务实现类
 * <p>
 * 实现门禁权限申请相关业务功能，集成工作流审批
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
public class AccessPermissionApplyServiceImpl implements AccessPermissionApplyService {

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
    public AccessPermissionApplyEntity submitPermissionApply(AccessPermissionApplyForm form) {
        log.info("[权限申请] 提交权限申请，applicantId={}, areaId={}, applyType={}",
                form.getApplicantId(), form.getAreaId(), form.getApplyType());

        // 1. 创建权限申请记录
        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo(generateApplyNo());
        entity.setApplicantId(form.getApplicantId());
        // 从用户服务获取申请人姓名
        String applicantName = getUserNameById(form.getApplicantId());
        entity.setApplicantName(applicantName != null ? applicantName : "未知用户");
        entity.setAreaId(form.getAreaId());
        // 从区域服务获取区域名称
        String areaName = getAreaNameById(form.getAreaId());
        entity.setAreaName(areaName != null ? areaName : "未知区域");
        entity.setApplyType(form.getApplyType());
        entity.setApplyReason(form.getApplyReason());
        entity.setStartTime(form.getStartTime());
        entity.setEndTime(form.getEndTime());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
        accessPermissionApplyDao.insert(entity);

        // 2. 构建表单数据（用于审批流程）
        Map<String, Object> formData = new HashMap<>();
        formData.put("applyNo", entity.getApplyNo());
        formData.put("applicantId", form.getApplicantId());
        formData.put("applicantName", entity.getApplicantName());
        formData.put("areaId", form.getAreaId());
        formData.put("areaName", entity.getAreaName());
        formData.put("applyType", form.getApplyType());
        formData.put("applyReason", form.getApplyReason());
        formData.put("startTime", form.getStartTime());
        formData.put("endTime", form.getEndTime());
        formData.put("applyTime", LocalDateTime.now());

        // 3. 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        // 可以根据申请类型设置不同的流程定义ID

        // 4. 启动审批流程
        // 根据申请类型选择不同的流程定义ID
        Long definitionId = "EMERGENCY".equals(form.getApplyType())
                ? WorkflowDefinitionConstants.ACCESS_EMERGENCY_PERMISSION
                : WorkflowDefinitionConstants.ACCESS_PERMISSION_APPLY;

        String businessType = "EMERGENCY".equals(form.getApplyType())
                ? BusinessTypeEnum.ACCESS_EMERGENCY_PERMISSION.name()
                : BusinessTypeEnum.ACCESS_PERMISSION_APPLY.name();

        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                definitionId, // 流程定义ID
                entity.getApplyNo(), // 业务Key
                "权限申请-" + entity.getApplyNo(), // 流程实例名称
                form.getApplicantId(), // 发起人ID
                businessType, // 业务类型
                formData, // 表单数据
                variables // 流程变量
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[权限申请] 启动审批流程失败，applyNo={}", entity.getApplyNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        // 5. 更新权限申请的workflowInstanceId
        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        accessPermissionApplyDao.updateById(entity);

        log.info("[权限申请] 权限申请提交成功，applyNo={}, workflowInstanceId={}",
                entity.getApplyNo(), workflowInstanceId);

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissionApplyStatus(String applyNo, String status, String approvalComment) {
        log.info("[权限申请] 更新权限申请状态，applyNo={}, status={}", applyNo, status);

        AccessPermissionApplyEntity entity = accessPermissionApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[权限申请] 权限申请不存在，applyNo={}", applyNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        accessPermissionApplyDao.updateById(entity);

        // 如果审批通过，执行权限授予逻辑
        if ("APPROVED".equals(status)) {
            executePermissionGrant(entity);
        }

        log.info("[权限申请] 权限申请状态更新成功，applyNo={}, status={}", applyNo, status);
    }

    /**
     * 执行权限授予逻辑
     * <p>
     * 权限授予流程：
     * 1. 检查是否已有权限（避免重复授权）
     * 2. 创建区域人员权限关联记录
     * 3. 同步权限到门禁设备（通过设备管理服务）
     * 4. 记录权限变更日志
     * </p>
     *
     * @param entity 权限申请实体
     */
    private void executePermissionGrant(AccessPermissionApplyEntity entity) {
        log.info("[权限申请] 执行权限授予，applyNo={}, applicantId={}, areaId={}, startTime={}, endTime={}",
                entity.getApplyNo(), entity.getApplicantId(), entity.getAreaId(),
                entity.getStartTime(), entity.getEndTime());

        try {
            // 1. 检查是否已有权限（避免重复授权）
            boolean hasPermission = areaPersonDao.hasAreaPermission(entity.getApplicantId(), entity.getAreaId());
            if (hasPermission) {
                log.warn("[权限申请] 用户已有该区域权限，跳过授权，applicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
                // 可以选择更新权限有效期，或者直接返回
                // 这里选择更新权限有效期
                updatePermissionExpireTime(entity);
                return;
            }

            // 2. 创建区域人员权限关联记录
            AreaPersonEntity areaPerson = new AreaPersonEntity();
            areaPerson.setPersonId(entity.getApplicantId());
            areaPerson.setAreaId(entity.getAreaId());
            areaPerson.setAccessLevel(1); // 默认访问级别：1-普通访问
            areaPerson.setAccessReason(entity.getApplyReason());
            areaPerson.setGrantUserId(entity.getApplicantId()); // 授权人默认为申请人
            areaPerson.setGrantTime(LocalDateTime.now());
            areaPerson.setValidStartTime(entity.getStartTime() != null ? entity.getStartTime() : LocalDateTime.now());
            areaPerson.setValidEndTime(entity.getEndTime());
            areaPerson.setExpireTime(entity.getEndTime()); // 过期时间与结束时间一致
            areaPerson.setStatus(1); // 1-有效

            int insertResult = areaPersonDao.insert(areaPerson);
            if (insertResult <= 0) {
                log.error("[权限申请] 区域权限关联记录创建失败，applicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
                throw new BusinessException("区域权限关联记录创建失败");
            }

            log.info("[权限申请] 区域权限关联记录创建成功，relationId={}, applicantId={}, areaId={}, applyNo={}",
                    areaPerson.getRelationId(), entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());

            // 3. 同步权限到门禁设备（通过设备管理服务）
            syncPermissionToDevices(entity, areaPerson);

            log.info("[权限申请] 权限授予成功，applyNo={}, applicantId={}, areaId={}",
                    entity.getApplyNo(), entity.getApplicantId(), entity.getAreaId());

        } catch (BusinessException e) {
            log.error("[权限申请] 权限授予业务异常，applyNo={}", entity.getApplyNo(), e);
            throw e;
        } catch (Exception e) {
            log.error("[权限申请] 权限授予处理异常，applyNo={}", entity.getApplyNo(), e);
            throw new BusinessException("权限授予处理异常：" + e.getMessage());
        }
    }

    /**
     * 更新权限过期时间（如果权限已存在）
     *
     * @param entity 权限申请实体
     */
    private void updatePermissionExpireTime(AccessPermissionApplyEntity entity) {
        log.info("[权限申请] 更新权限过期时间，applicantId={}, areaId={}, endTime={}",
                entity.getApplicantId(), entity.getAreaId(), entity.getEndTime());

        try {
            // 查询现有权限
            var existingPermissions = areaPersonDao.getEffectivePermissionsByTimeRange(
                    entity.getApplicantId(),
                    LocalDateTime.now(),
                    entity.getEndTime() != null ? entity.getEndTime() : LocalDateTime.now().plusYears(1)
            );

            // 更新过期时间
            for (AreaPersonEntity permission : existingPermissions) {
                if (permission.getAreaId().equals(entity.getAreaId())) {
                    permission.setValidEndTime(entity.getEndTime());
                    permission.setExpireTime(entity.getEndTime());
                    areaPersonDao.updateById(permission);
                    log.info("[权限申请] 权限过期时间更新成功，relationId={}, expireTime={}",
                            permission.getRelationId(), entity.getEndTime());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("[权限申请] 更新权限过期时间异常，applicantId={}, areaId={}",
                    entity.getApplicantId(), entity.getAreaId(), e);
        }
    }

    /**
     * 同步权限到门禁设备
     * <p>
     * 通过设备管理服务同步权限到相关门禁设备
     * </p>
     *
     * @param entity 权限申请实体
     * @param areaPerson 区域人员权限关联实体
     */
    private void syncPermissionToDevices(AccessPermissionApplyEntity entity, AreaPersonEntity areaPerson) {
        log.info("[权限申请] 同步权限到门禁设备，applicantId={}, areaId={}, applyNo={}",
                entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());

        try {
            // 构建权限同步请求
            java.util.Map<String, Object> syncRequest = new java.util.HashMap<>();
            syncRequest.put("personId", entity.getApplicantId());
            syncRequest.put("areaId", entity.getAreaId());
            syncRequest.put("relationId", areaPerson.getRelationId());
            syncRequest.put("validStartTime", areaPerson.getValidStartTime());
            syncRequest.put("validEndTime", areaPerson.getValidEndTime());

            // 通过网关调用设备管理服务同步权限
            // 接口路径已确认：/api/v1/device/permission/sync
            // 设备通讯服务需要实现此接口以支持权限同步功能
            ResponseDTO<Void> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/permission/sync",
                    HttpMethod.POST,
                    syncRequest,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("[权限申请] 权限同步到设备成功，applicantId={}, areaId={}, applyNo={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo());
            } else {
                log.warn("[权限申请] 权限同步到设备失败，但不影响权限授予，applicantId={}, areaId={}, applyNo={}, message={}",
                        entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo(),
                        response != null ? response.getMessage() : "未知错误");
                // 权限同步失败不影响权限授予，仅记录警告
            }

        } catch (Exception e) {
            log.error("[权限申请] 权限同步到设备异常，但不影响权限授予，applicantId={}, areaId={}, applyNo={}",
                    entity.getApplicantId(), entity.getAreaId(), entity.getApplyNo(), e);
            // 权限同步异常不影响权限授予，仅记录错误
        }
    }

    /**
     * 生成申请编号
     *
     * @return 申请编号
     */
    private String generateApplyNo() {
        return "AP" + System.currentTimeMillis();
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
            log.warn("[权限申请] 用户ID为空，无法获取用户姓名");
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
                log.debug("[权限申请] 获取用户姓名成功，userId={}, userName={}", userId, userName);
                return userName;
            } else {
                log.warn("[权限申请] 获取用户信息失败，userId={}, message={}",
                        userId, userResponse != null ? userResponse.getMessage() : "未知错误");
                return null;
            }
        } catch (Exception e) {
            log.error("[权限申请] 获取用户信息异常，userId={}", userId, e);
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
            log.warn("[权限申请] 区域ID为空，无法获取区域名称");
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
                log.debug("[权限申请] 获取区域名称成功，areaId={}, areaName={}", areaId, area.getAreaName());
                return area.getAreaName();
            } else {
                log.warn("[权限申请] 获取区域信息失败，areaId={}, message={}",
                        areaId, areaResponse != null ? areaResponse.getMessage() : "未知错误");
                return null;
            }
        } catch (Exception e) {
            log.error("[权限申请] 获取区域信息异常，areaId={}", areaId, e);
            return null;
        }
    }
}

