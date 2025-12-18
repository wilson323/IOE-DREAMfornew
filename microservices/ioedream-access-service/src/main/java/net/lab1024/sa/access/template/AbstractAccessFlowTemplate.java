package net.lab1024.sa.access.template;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessRequest;
import net.lab1024.sa.access.strategy.IAccessPermissionStrategy;
import net.lab1024.sa.common.factory.StrategyFactory;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.access.dao.AccessRecordDao;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 门禁通行流程模板
 * <p>
 * 使用模板方法模式定义门禁通行的标准流程
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
public abstract class AbstractAccessFlowTemplate {

    @Resource
    protected DeviceDao deviceDao;

    @Resource
    protected AccessRecordDao accessRecordDao;

    @Resource
    protected StrategyFactory<IAccessPermissionStrategy> strategyFactory;

    /**
     * 模板方法: 通行流程
     * <p>
     * 定义门禁通行的标准流程，子类不能重写此方法
     * </p>
     *
     * @param request 通行请求
     * @return 通行结果
     */
    public final AccessResult processAccess(AccessRequest request) {
        try {
            // 1. 参数校验
            validate(request);

            // 2. 设备验证
            DeviceEntity device = validateDevice(request.getDeviceId());

            // 3. 用户识别(抽象方法 - 子类实现)
            UserIdentityResult identity = identifyUser(request);
            if (!identity.isSuccess()) {
                return AccessResult.denied("身份识别失败: " + identity.getMessage());
            }

            // 4. 权限验证(策略模式)
            boolean hasPermission = checkPermission(
                    identity.getUserId(),
                    request.getAreaId(),
                    request
            );
            if (!hasPermission) {
                recordFailedAccess(identity, device, "权限不足");
                return AccessResult.denied("权限不足");
            }

            // 5. 开门指令(抽象方法 - 子类实现)
            boolean opened = openDoor(device, request);

            // 6. 记录通行
            recordSuccessAccess(identity, device, request);

            // 7. 事件通知(钩子方法)
            notifyAccessEvent(identity, device, opened);

            return opened ? AccessResult.success() : AccessResult.failed("开门失败");

        } catch (Exception e) {
            log.error("[通行流程异常] request={}", request, e);
            return AccessResult.error("系统异常: " + e.getMessage());
        }
    }

    /**
     * 抽象方法: 用户识别
     * <p>
     * 子类必须实现具体的用户识别逻辑
     * </p>
     *
     * @param request 通行请求
     * @return 用户识别结果
     */
    protected abstract UserIdentityResult identifyUser(AccessRequest request);

    /**
     * 抽象方法: 开门
     * <p>
     * 子类必须实现具体的开门逻辑
     * </p>
     *
     * @param device 设备实体
     * @param request 通行请求
     * @return 是否开门成功
     */
    protected abstract boolean openDoor(DeviceEntity device, AccessRequest request);

    /**
     * 钩子方法: 事件通知(可选覆盖)
     * <p>
     * 子类可以选择覆盖此方法以实现自定义通知逻辑
     * </p>
     *
     * @param identity 用户识别结果
     * @param device 设备实体
     * @param opened 是否开门成功
     */
    protected void notifyAccessEvent(UserIdentityResult identity,
                                     DeviceEntity device, boolean opened) {
        // 默认空实现
    }

    /**
     * 参数校验
     */
    private void validate(AccessRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("通行请求不能为空");
        }
        if (request.getDeviceId() == null) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
    }

    /**
     * 设备验证
     */
    private DeviceEntity validateDevice(Long deviceId) {
        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + deviceId);
        }
        if (device.getStatus() != 1) {
            throw new IllegalArgumentException("设备未启用: " + deviceId);
        }
        return device;
    }

    /**
     * 权限验证(策略模式)
     */
    private boolean checkPermission(Long userId, Long areaId, AccessRequest request) {
        List<IAccessPermissionStrategy> strategies = strategyFactory.getAll();

        // 按优先级排序
        strategies.sort(Comparator.comparingInt(
                IAccessPermissionStrategy::getPriority).reversed()
        );

        // 任一策略通过即可
        return strategies.stream()
                .anyMatch(strategy -> strategy.hasPermission(request));
    }

    /**
     * 记录失败通行
     */
    private void recordFailedAccess(UserIdentityResult identity, DeviceEntity device, String reason) {
        AccessRecordEntity record = new AccessRecordEntity();
        record.setUserId(identity.getUserId());
        record.setDeviceId(device.getId());
        record.setAccessTime(LocalDateTime.now());
        record.setAuthResult("FAILED");
        record.setDoorOpened(false);
        record.setRemark(reason);
        accessRecordDao.insert(record);
    }

    /**
     * 记录成功通行
     */
    private void recordSuccessAccess(UserIdentityResult identity, DeviceEntity device, AccessRequest request) {
        AccessRecordEntity record = new AccessRecordEntity();
        record.setUserId(identity.getUserId());
        record.setDeviceId(device.getId());
        record.setAccessTime(LocalDateTime.now());
        record.setAuthResult("SUCCESS");
        record.setAuthMethod(request.getBiometricData() != null ? "BIOMETRIC" : "CARD");
        record.setDoorOpened(true);
        accessRecordDao.insert(record);
    }

    /**
     * 通行结果
     */
    public static class AccessResult {
        private final boolean success;
        private final String message;

        private AccessResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static AccessResult success() {
            return new AccessResult(true, "通行成功");
        }

        public static AccessResult denied(String message) {
            return new AccessResult(false, message);
        }

        public static AccessResult failed(String message) {
            return new AccessResult(false, message);
        }

        public static AccessResult error(String message) {
            return new AccessResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 用户识别结果
     */
    public static class UserIdentityResult {
        private final boolean success;
        private final Long userId;
        private final String message;

        private UserIdentityResult(boolean success, Long userId, String message) {
            this.success = success;
            this.userId = userId;
            this.message = message;
        }

        public static UserIdentityResult success(Long userId) {
            return new UserIdentityResult(true, userId, "识别成功");
        }

        public static UserIdentityResult failed(String message) {
            return new UserIdentityResult(false, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public Long getUserId() {
            return userId;
        }

        public String getMessage() {
            return message;
        }
    }
}
