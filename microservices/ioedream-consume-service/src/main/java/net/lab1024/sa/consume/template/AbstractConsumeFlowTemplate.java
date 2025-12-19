package net.lab1024.sa.consume.template;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.dao.DeviceDao;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费流程模板
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用模板方法模式定义消费处理的标准流程
 * 符合"中心实时验证"模式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public abstract class AbstractConsumeFlowTemplate {

    @Resource
    protected DeviceDao deviceDao;

    /**
     * 模板方法: 消费流程
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 定义消费处理的标准流程，子类不能重写此方法
     * </p>
     *
     * @param form 支付处理表单
     * @return 消费结果
     */
    public final ConsumeResult processConsume(PaymentProcessForm form) {
        try {
            // 1. 参数校验
            validate(form);

            // 2. 设备验证
            DeviceEntity device = validateDevice(form.getDeviceId());

            // 3. 账户验证(抽象方法 - 子类实现)
            AccountEntity account = validateAccount(form);
            if (account == null) {
                return ConsumeResult.failed("账户不存在或无效");
            }

            // 4. 余额检查(抽象方法 - 子类实现)
            boolean balanceSufficient = checkBalance(account, form);
            if (!balanceSufficient) {
                return ConsumeResult.failed("余额不足");
            }

            // 5. 执行扣款(抽象方法 - 子类实现)
            ConsumeRecordEntity record = executePayment(account, form, device);

            // 6. 记录消费
            saveConsumeRecord(record);

            // 7. 事件通知(钩子方法)
            notifyConsumeEvent(account, record, form);

            return ConsumeResult.success(record, account.getBalance());

        } catch (Exception e) {
            log.error("[消费流程异常] form={}", form, e);
            return ConsumeResult.error("系统异常: " + e.getMessage());
        }
    }

    /**
     * 抽象方法: 账户验证
     * <p>
     * 子类必须实现具体的账户验证逻辑
     * </p>
     *
     * @param form 支付处理表单
     * @return 账户实体
     */
    protected abstract AccountEntity validateAccount(PaymentProcessForm form);

    /**
     * 抽象方法: 余额检查
     * <p>
     * 子类必须实现具体的余额检查逻辑
     * </p>
     *
     * @param account 账户实体
     * @param form 支付处理表单
     * @return 余额是否充足
     */
    protected abstract boolean checkBalance(AccountEntity account, PaymentProcessForm form);

    /**
     * 抽象方法: 执行扣款
     * <p>
     * 子类必须实现具体的扣款逻辑
     * </p>
     *
     * @param account 账户实体
     * @param form 支付处理表单
     * @param device 设备实体
     * @return 消费记录
     */
    protected abstract ConsumeRecordEntity executePayment(
            AccountEntity account, PaymentProcessForm form, DeviceEntity device);

    /**
     * 钩子方法: 事件通知(可选覆盖)
     * <p>
     * 子类可以选择覆盖此方法以实现自定义通知逻辑
     * </p>
     *
     * @param account 账户实体
     * @param record 消费记录
     * @param form 支付处理表单
     */
    protected void notifyConsumeEvent(AccountEntity account, ConsumeRecordEntity record, PaymentProcessForm form) {
        // 默认空实现
    }

    /**
     * 参数校验
     */
    private void validate(PaymentProcessForm form) {
        if (form == null) {
            throw new IllegalArgumentException("支付处理表单不能为空");
        }
        if (form.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (form.getAccountId() == null) {
            throw new IllegalArgumentException("账户ID不能为空");
        }
        if (form.getPaymentAmount() == null || form.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付金额必须大于0");
        }
    }

    /**
     * 设备验证
     * <p>
     * 根据chonggou.txt和业务模块文档要求实现
     * 验证设备是否存在且可用
     * DeviceEntity.deviceId是String类型（主键），支持通过设备ID或设备编码查询
     * </p>
     *
     * @param deviceId 设备ID或设备编码（String格式）
     * @return 设备实体，如果设备不存在或不可用则返回null
     */
    private DeviceEntity validateDevice(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            log.debug("[消费流程] 设备ID为空，跳过设备验证");
            return null;
        }

        if (deviceDao == null) {
            log.warn("[消费流程] 设备Dao未注入，跳过设备验证");
            return null;
        }

        try {
            // 方案1：直接使用deviceId作为主键查询（DeviceEntity.deviceId是String类型）
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device != null && device.getDeleted() != null && device.getDeleted() == 0) {
                log.debug("[消费流程] 设备验证通过（通过ID查询）, deviceId={}, deviceName={}", deviceId, device.getDeviceName());
                return device;
            }

            // 方案2：尝试作为设备编码查询
            try {
                device = deviceDao.selectByDeviceCode(deviceId);
                if (device != null && device.getDeleted() != null && device.getDeleted() == 0) {
                    log.debug("[消费流程] 设备验证通过（通过编码查询）, deviceCode={}, deviceName={}", deviceId, device.getDeviceName());
                    return device;
                }
            } catch (Exception e) {
                log.debug("[消费流程] 设备编码查询失败: deviceId={}, error={}", deviceId, e.getMessage());
            }

            // 方案3：尝试作为设备序列号查询
            try {
                device = deviceDao.selectBySerialNumber(deviceId);
                if (device != null && device.getDeleted() != null && device.getDeleted() == 0) {
                    log.debug("[消费流程] 设备验证通过（通过序列号查询）, serialNumber={}, deviceName={}", deviceId, device.getDeviceName());
                    return device;
                }
            } catch (Exception e) {
                log.debug("[消费流程] 设备序列号查询失败: deviceId={}, error={}", deviceId, e.getMessage());
            }

            log.warn("[消费流程] 设备不存在或已删除, deviceId={}", deviceId);
            return null;

        } catch (Exception e) {
            log.error("[消费流程] 设备验证异常, deviceId={}, error={}", deviceId, e.getMessage(), e);
            // 设备验证异常不影响主流程，返回null
            return null;
        }
    }

    /**
     * 保存消费记录
     * <p>
     * 根据chonggou.txt要求实现
     * 注意：此方法在executePayment之后调用，记录可能已经在executePayment中保存
     * 这里主要进行日志记录和后续处理
     * </p>
     *
     * @param record 消费记录实体
     */
    private void saveConsumeRecord(ConsumeRecordEntity record) {
        if (record == null) {
            log.warn("[消费流程] 消费记录为空，跳过保存");
            return;
        }

        log.info("[消费流程] 保存消费记录, recordId={}, userId={}, amount={}",
                record.getRecordId(), record.getUserId(), record.getAmount());

        // 注意：实际的数据库保存操作已在executePayment方法中完成（子类实现）
        // 这里主要进行日志记录和后续处理
        // 如果需要在保存后进行额外处理，可以在这里添加
    }

    /**
     * 消费结果
     */
    public static class ConsumeResult {
        private final boolean success;
        private final String message;
        private final ConsumeRecordEntity record;
        private final BigDecimal balance;

        private ConsumeResult(boolean success, String message, ConsumeRecordEntity record, BigDecimal balance) {
            this.success = success;
            this.message = message;
            this.record = record;
            this.balance = balance;
        }

        public static ConsumeResult success(ConsumeRecordEntity record, BigDecimal balance) {
            return new ConsumeResult(true, "消费成功", record, balance);
        }

        public static ConsumeResult failed(String message) {
            return new ConsumeResult(false, message, null, null);
        }

        public static ConsumeResult error(String message) {
            return new ConsumeResult(false, message, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public ConsumeRecordEntity getRecord() {
            return record;
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }
}
