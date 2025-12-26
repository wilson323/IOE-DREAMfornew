package net.lab1024.sa.biometric.template;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.biometric.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.biometric.domain.dto.BiometricTemplateSyncRequest;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 生物模板同步流程模板
 * <p>
 * 使用模板方法模式定义模板同步的标准流程
 * 严格遵循CLAUDE.md规范：
 * - 使用模板方法模式实现
 * - 定义标准流程，子类实现具体步骤
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public abstract class AbstractTemplateSyncTemplate {

    @Resource
    protected GatewayServiceClient gatewayServiceClient;

    /**
     * 模板方法: 同步模板流程
     * <p>
     * 定义模板同步的标准流程，子类不能重写此方法
     * </p>
     *
     * @param template 生物模板
     * @param deviceId 设备ID
     * @return 同步结果
     */
    public final TemplateSyncResult syncTemplate(BiometricTemplateEntity template, String deviceId) {
        try {
            // 1. 参数校验
            validate(template, deviceId);

            // 2. 设备验证
            DeviceEntity device = validateDevice(deviceId);

            // 3. 构建同步请求(抽象方法 - 子类实现)
            BiometricTemplateSyncRequest syncRequest = buildSyncRequest(template, device);

            // 4. 发送同步请求(抽象方法 - 子类实现)
            boolean success = sendSyncRequest(syncRequest, device);

            // 5. 记录同步结果
            recordSyncResult(template, device, success);

            // 6. 后处理(钩子方法)
            afterSync(template, device, success);

            return success ? TemplateSyncResult.success() : TemplateSyncResult.failed("同步失败");

        } catch (Exception e) {
            log.error("[模板同步流程异常] templateId={}, deviceId={}", template.getTemplateId(), deviceId, e);
            return TemplateSyncResult.error("系统异常: " + e.getMessage());
        }
    }

    /**
     * 抽象方法: 构建同步请求
     * <p>
     * 子类必须实现具体的请求构建逻辑
     * </p>
     *
     * @param template 生物模板
     * @param device 设备实体
     * @return 同步请求
     */
    protected abstract BiometricTemplateSyncRequest buildSyncRequest(
            BiometricTemplateEntity template, DeviceEntity device);

    /**
     * 抽象方法: 发送同步请求
     * <p>
     * 子类必须实现具体的同步请求发送逻辑
     * </p>
     *
     * @param syncRequest 同步请求
     * @param device 设备实体
     * @return 是否同步成功
     */
    protected abstract boolean sendSyncRequest(
            BiometricTemplateSyncRequest syncRequest, DeviceEntity device);

    /**
     * 钩子方法: 同步后处理(可选覆盖)
     * <p>
     * 子类可以选择覆盖此方法以实现自定义后处理逻辑
     * </p>
     *
     * @param template 生物模板
     * @param device 设备实体
     * @param success 是否同步成功
     */
    protected void afterSync(BiometricTemplateEntity template, DeviceEntity device, boolean success) {
        // 默认空实现
    }

    /**
     * 参数校验
     */
    private void validate(BiometricTemplateEntity template, String deviceId) {
        if (template == null) {
            throw new IllegalArgumentException("生物模板不能为空");
        }
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
    }

    /**
     * 设备验证
     */
    private DeviceEntity validateDevice(String deviceId) {
        // 通过网关调用common-service获取设备信息
        @SuppressWarnings("unchecked")
        ResponseDTO<DeviceEntity> response = gatewayServiceClient.callCommonService(
                "/api/v1/device/" + deviceId,
                HttpMethod.GET,
                null,
                new TypeReference<ResponseDTO<DeviceEntity>>() {}
        );
        DeviceEntity device = response != null ? response.getData() : null;

        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + deviceId);
        }
        if (device.getDeviceStatus() != null && device.getDeviceStatus() != 1) {
            throw new IllegalArgumentException("设备未启用: " + deviceId);
        }
        return device;
    }

    /**
     * 记录同步结果
     */
    private void recordSyncResult(BiometricTemplateEntity template, DeviceEntity device, boolean success) {
        log.info("[模板同步] 同步结果 templateId={}, deviceId={}, success={}",
                template.getTemplateId(), device.getDeviceId(), success);
    }

    /**
     * 模板同步结果
     */
    public static class TemplateSyncResult {
        private final boolean success;
        private final String message;

        private TemplateSyncResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static TemplateSyncResult success() {
            return new TemplateSyncResult(true, "同步成功");
        }

        public static TemplateSyncResult failed(String message) {
            return new TemplateSyncResult(false, message);
        }

        public static TemplateSyncResult error(String message) {
            return new TemplateSyncResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
