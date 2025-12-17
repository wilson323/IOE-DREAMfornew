package net.lab1024.sa.oa.workflow.listener;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;

import org.springframework.http.HttpMethod;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;

/**
 * 工作流审批结果监听器
 * <p>
 * 监听审批结果事件并更新业务状态
 * 严格遵循CLAUDE.md规范：
 * - 使用Spring事件机制
 * - 异步处理避免阻塞主线程
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 职责：
 * - 监听审批状态变更事件
 * - 根据业务类型更新对应的业务状态
 * - 发送业务通知
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
// 暂时禁用此Listener，因为依赖的WorkflowApprovalManager需要类型兼容性修复
// @Component
public class WorkflowApprovalResultListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowApprovalResultListener.class);

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 处理审批结果事件
     * <p>
     * 通过WebSocket或消息队列接收审批结果事件
     * 异步处理，避免阻塞主线程
     * </p>
     *
     * @param event 审批结果事件
     */
    @EventListener
    @Async
    public void handleApprovalResult(WorkflowApprovalResultEvent event) {
        Long instanceId = event.getInstanceId();
        String businessType = event.getBusinessType();
        String businessKey = event.getBusinessKey();
        String status = event.getStatus(); // APPROVED, REJECTED, CANCELLED

        log.info("接收到审批结果事件，实例ID: {}, 业务类型: {}, 业务Key: {}, 状态: {}",
                instanceId, businessType, businessKey, status);

        try {
            // 获取流程实例详情（包含业务数据）
            ResponseDTO<Map<String, Object>> instanceResult =
                    workflowApprovalManager.getProcessInstanceStatus(instanceId);

            if (instanceResult == null || !instanceResult.isSuccess()) {
                log.warn("获取流程实例详情失败，实例ID: {}", instanceId);
                return;
            }

            Map<String, Object> instanceData = instanceResult.getData();
            @SuppressWarnings("unchecked")
            Map<String, Object> formData = instanceData != null && instanceData.get("formData") instanceof Map
                    ? (Map<String, Object>) instanceData.get("formData")
                    : null;

            // 根据业务类型更新对应的业务状态
            // 注意：这里通过业务模块的回调接口更新状态，而不是直接访问DAO
            // 遵循微服务架构规范，保持服务边界清晰
            switch (businessType) {
                case "CONSUME_REFUND":
                    // 消费模块退款：通过网关调用消费服务的状态更新接口
                    updateConsumeRefundStatus(businessKey, status, formData);
                    break;
                case "CONSUME_REIMBURSEMENT":
                    // 消费模块报销：通过网关调用消费服务的状态更新接口
                    updateConsumeReimbursementStatus(businessKey, status, formData);
                    break;
                case "VISITOR_APPOINTMENT":
                    // 访客模块预约：通过网关调用访客服务的状态更新接口
                    updateVisitorAppointmentStatus(businessKey, status, formData);
                    break;
                case "ACCESS_PERMISSION_APPLY":
                    // 门禁模块权限申请：通过网关调用门禁服务的状态更新接口
                    updateAccessPermissionStatus(businessKey, status, formData);
                    break;
                case "ACCESS_EMERGENCY_PERMISSION":
                    // 门禁模块紧急权限申请：通过网关调用紧急权限服务的状态更新接口
                    updateEmergencyPermissionStatus(businessKey, status, formData);
                    break;
                case "ATTENDANCE_LEAVE":
                case "ATTENDANCE_TRAVEL":
                case "ATTENDANCE_OVERTIME":
                case "ATTENDANCE_SUPPLEMENT":
                case "ATTENDANCE_SHIFT":
                    // 考勤模块异常申请：通过网关调用考勤服务的状态更新接口
                    updateAttendanceExceptionStatus(businessKey, status, businessType, formData);
                    break;
                default:
                    log.warn("未知的业务类型: {}", businessType);
            }

            log.info("审批结果事件处理完成，实例ID: {}, 业务类型: {}, 状态: {}", instanceId, businessType, status);

        } catch (Exception e) {
            log.error("处理审批结果事件失败，实例ID: {}, 业务类型: {}", instanceId, businessType, e);
            // 记录失败日志，但不抛出异常，避免影响其他事件处理
        }
    }

    /**
     * 更新消费模块退款状态
     */
    @SuppressWarnings("null")
    private void updateConsumeRefundStatus(String businessKey, String status, Map<String, Object> formData) {
        log.info("更新消费退款状态，业务Key: {}, 状态: {}", businessKey, status);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("status", status);
            if (formData != null && formData.get("approvalComment") != null) {
                requestParams.put("approvalComment", formData.get("approvalComment"));
            }

            ResponseDTO<Void> response = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/refund/" + businessKey + "/status",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("消费退款状态更新成功，业务Key: {}, 状态: {}", businessKey, status);
            } else {
                log.error("消费退款状态更新失败，业务Key: {}, 错误: {}", businessKey,
                        response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("更新消费退款状态异常，业务Key: {}", businessKey, e);
        }
    }

    /**
     * 更新消费模块报销状态
     */
    @SuppressWarnings("null")
    private void updateConsumeReimbursementStatus(String businessKey, String status, Map<String, Object> formData) {
        log.info("更新消费报销状态，业务Key: {}, 状态: {}", businessKey, status);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("status", status);
            if (formData != null && formData.get("approvalComment") != null) {
                requestParams.put("approvalComment", formData.get("approvalComment"));
            }

            ResponseDTO<Void> response = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/reimbursement/" + businessKey + "/status",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("消费报销状态更新成功，业务Key: {}, 状态: {}", businessKey, status);
            } else {
                log.error("消费报销状态更新失败，业务Key: {}, 错误: {}", businessKey,
                        response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("更新消费报销状态异常，业务Key: {}", businessKey, e);
        }
    }

    /**
     * 更新访客模块预约状态
     */
    @SuppressWarnings("null")
    private void updateVisitorAppointmentStatus(String businessKey, String status, Map<String, Object> formData) {
        log.info("更新访客预约状态，业务Key: {}, 状态: {}", businessKey, status);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("status", status);
            if (formData != null && formData.get("approvalComment") != null) {
                requestParams.put("approvalComment", formData.get("approvalComment"));
            }

            // businessKey是appointmentId（Long类型）
            Long appointmentId = Long.parseLong(businessKey);
            ResponseDTO<Void> response = gatewayServiceClient.callVisitorService(
                    "/api/v1/mobile/visitor/appointment/" + appointmentId + "/status",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("访客预约状态更新成功，业务Key: {}, 状态: {}", businessKey, status);
            } else {
                log.error("访客预约状态更新失败，业务Key: {}, 错误: {}", businessKey,
                        response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("更新访客预约状态异常，业务Key: {}", businessKey, e);
        }
    }

    /**
     * 更新门禁模块权限申请状态
     */
    private void updateAccessPermissionStatus(String businessKey, String status, Map<String, Object> formData) {
        log.info("更新门禁权限申请状态，业务Key: {}, 状态: {}", businessKey, status);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("status", status);
            if (formData != null && formData.get("approvalComment") != null) {
                requestParams.put("approvalComment", formData.get("approvalComment"));
            }

            ResponseDTO<Void> response = gatewayServiceClient.callAccessService(
                    "/api/v1/access/permission/apply/" + businessKey + "/status",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("门禁权限申请状态更新成功，业务Key: {}, 状态: {}", businessKey, status);
            } else {
                log.error("门禁权限申请状态更新失败，业务Key: {}, 错误: {}", businessKey,
                        response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("更新门禁权限申请状态异常，业务Key: {}", businessKey, e);
        }
    }

    /**
     * 更新门禁模块紧急权限申请状态
     * <p>
     * 紧急权限申请使用独立的服务接口
     * </p>
     */
    @SuppressWarnings("null")
    private void updateEmergencyPermissionStatus(String businessKey, String status, Map<String, Object> formData) {
        log.info("更新门禁紧急权限申请状态，业务Key: {}, 状态: {}", businessKey, status);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("status", status);
            if (formData != null && formData.get("approvalComment") != null) {
                requestParams.put("approvalComment", formData.get("approvalComment"));
            }

            ResponseDTO<Void> response = gatewayServiceClient.callAccessService(
                    "/api/v1/access/emergency-permission/" + businessKey + "/status",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("门禁紧急权限申请状态更新成功，业务Key: {}, 状态: {}", businessKey, status);
            } else {
                log.error("门禁紧急权限申请状态更新失败，业务Key: {}, 错误: {}", businessKey,
                        response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("更新门禁紧急权限申请状态异常，业务Key: {}", businessKey, e);
        }
    }

    /**
     * 更新考勤模块异常申请状态
     * <p>
     * 根据业务类型判断是哪种考勤申请（请假、出差、加班、补签、调班）
     * </p>
     */
    @SuppressWarnings("null")
    private void updateAttendanceExceptionStatus(String businessKey, String status, String businessType, Map<String, Object> formData) {
        log.info("更新考勤异常申请状态，业务Key: {}, 业务类型: {}, 状态: {}", businessKey, businessType, status);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("status", status);
            if (formData != null && formData.get("approvalComment") != null) {
                requestParams.put("approvalComment", formData.get("approvalComment"));
            }

            // 根据业务类型确定API路径
            String apiPath;
            if ("ATTENDANCE_LEAVE".equals(businessType) || businessKey.startsWith("LV")) {
                apiPath = "/api/v1/attendance/leave/" + businessKey + "/status";
            } else if ("ATTENDANCE_TRAVEL".equals(businessType) || businessKey.startsWith("TR")) {
                apiPath = "/api/v1/attendance/travel/" + businessKey + "/status";
            } else if ("ATTENDANCE_OVERTIME".equals(businessType) || businessKey.startsWith("OT")) {
                apiPath = "/api/v1/attendance/overtime/" + businessKey + "/status";
            } else if ("ATTENDANCE_SUPPLEMENT".equals(businessType) || businessKey.startsWith("SP")) {
                apiPath = "/api/v1/attendance/supplement/" + businessKey + "/status";
            } else if ("ATTENDANCE_SHIFT".equals(businessType) || businessKey.startsWith("SH")) {
                apiPath = "/api/v1/attendance/shift/" + businessKey + "/status";
            } else {
                log.warn("无法确定考勤申请类型，业务Key: {}, 业务类型: {}", businessKey, businessType);
                return;
            }

            ResponseDTO<Void> response = gatewayServiceClient.callAttendanceService(
                    apiPath,
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("考勤异常申请状态更新成功，业务Key: {}, 状态: {}", businessKey, status);
            } else {
                log.error("考勤异常申请状态更新失败，业务Key: {}, 错误: {}", businessKey,
                        response != null ? response.getMessage() : "响应为空");
            }
        } catch (Exception e) {
            log.error("更新考勤异常申请状态异常，业务Key: {}", businessKey, e);
        }
    }

    /**
     * 工作流审批结果事件
     * <p>
     * 用于在工作流状态变更时触发业务状态更新
     * </p>
     */
    public static class WorkflowApprovalResultEvent {
        private Long instanceId;
        private String businessType;
        private String businessKey;
        private String status;

        public WorkflowApprovalResultEvent(Long instanceId, String businessType, String businessKey, String status) {
            this.instanceId = instanceId;
            this.businessType = businessType;
            this.businessKey = businessKey;
            this.status = status;
        }

        // Getters
        public Long getInstanceId() {
            return instanceId;
        }

        public String getBusinessType() {
            return businessType;
        }

        public String getBusinessKey() {
            return businessKey;
        }

        public String getStatus() {
            return status;
        }
    }
}





