package net.lab1024.sa.admin.module.consume.util;

import java.math.BigDecimal;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountBaseEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountExtensionEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;

/**
 * 账户实体合并工具类
 * <p>
 * 基于扩展表架构，提供AccountBaseEntity和AccountExtensionEntity的合并功能
 * 支持双向转换，确保原有AccountEntity的兼容性
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
public class AccountMergeUtil {

    /**
     * 合并基础实体和扩展实体为完整的AccountEntity
     *
     * @param baseEntity     基础账户实体
     * @param extensionEntity 扩展账户实体
     * @return 完整的账户实体
     */
    public static AccountEntity mergeToAccountEntity(AccountBaseEntity baseEntity, AccountExtensionEntity extensionEntity) {
        if (baseEntity == null) {
            log.warn("基础账户实体为空，返回null");
            return null;
        }

        try {
            AccountEntity accountEntity = new AccountEntity();

            // 复制基础实体字段
            SmartBeanUtil.copyProperties(baseEntity, accountEntity);

            // 复制扩展实体字段（如果扩展实体不为空）
            if (extensionEntity != null) {
                copyExtensionFields(extensionEntity, accountEntity);
            }

            return accountEntity;
        } catch (Exception e) {
            log.error("合并账户实体失败，baseId: {}, extId: {}",
                     baseEntity.getAccountId(),
                     extensionEntity != null ? extensionEntity.getAccountId() : null, e);
            return null;
        }
    }

    /**
     * 从AccountEntity拆分为BaseEntity和ExtensionEntity
     *
     * @param accountEntity 完整的账户实体
     * @return 包含基础实体和扩展实体的数组，索引0为BaseEntity，索引1为ExtensionEntity
     */
    public static Object[] splitToBaseAndExtension(AccountEntity accountEntity) {
        if (accountEntity == null) {
            log.warn("完整账户实体为空，返回null数组");
            return new Object[]{null, null};
        }

        try {
            // 创建基础实体
            AccountBaseEntity baseEntity = new AccountBaseEntity();
            copyBaseFields(accountEntity, baseEntity);

            // 创建扩展实体
            AccountExtensionEntity extensionEntity = new AccountExtensionEntity();
            copyToExtensionFields(accountEntity, extensionEntity);

            return new Object[]{baseEntity, extensionEntity};
        } catch (Exception e) {
            log.error("拆分账户实体失败，accountId: {}", accountEntity.getAccountId(), e);
            return new Object[]{null, null};
        }
    }

    /**
     * 从AccountEntity创建基础实体
     *
     * @param accountEntity 完整的账户实体
     * @return 基础账户实体
     */
    public static AccountBaseEntity createBaseEntity(AccountEntity accountEntity) {
        if (accountEntity == null) {
            return null;
        }

        try {
            AccountBaseEntity baseEntity = new AccountBaseEntity();
            copyBaseFields(accountEntity, baseEntity);
            return baseEntity;
        } catch (Exception e) {
            log.error("创建基础账户实体失败，accountId: {}", accountEntity.getAccountId(), e);
            return null;
        }
    }

    /**
     * 从AccountEntity创建扩展实体
     *
     * @param accountEntity 完整的账户实体
     * @return 扩展账户实体
     */
    public static AccountExtensionEntity createExtensionEntity(AccountEntity accountEntity) {
        if (accountEntity == null) {
            return null;
        }

        try {
            AccountExtensionEntity extensionEntity = new AccountExtensionEntity();
            copyToExtensionFields(accountEntity, extensionEntity);
            return extensionEntity;
        } catch (Exception e) {
            log.error("创建扩展账户实体失败，accountId: {}", accountEntity.getAccountId(), e);
            return null;
        }
    }

    /**
     * 复制基础字段到目标实体
     *
     * @param source 源实体
     * @param target 目标基础实体
     */
    private static void copyBaseFields(AccountEntity source, AccountBaseEntity target) {
        target.setAccountId(source.getAccountId());
        target.setPersonId(source.getPersonId());
        target.setPersonName(source.getPersonName());
        target.setEmployeeNo(source.getEmployeeNo());
        target.setAccountNo(source.getAccountNo());
        target.setBalance(source.getBalance());
        target.setFrozenAmount(source.getFrozenAmount());
        target.setCreditLimit(source.getCreditLimit());
        target.setAvailableLimit(source.getAvailableLimit());
        target.setAccountType(source.getAccountType());
        target.setStatus(source.getStatus());
        target.setRegionId(source.getRegionId());
        target.setRegionName(source.getRegionName());
        target.setDepartmentId(source.getDepartmentId());
        target.setDepartmentName(source.getDepartmentName());
        target.setAccountLevel(source.getAccountLevel());
        target.setPoints(source.getPoints());
        target.setTotalConsumeAmount(source.getTotalConsumeAmount());
        target.setTotalRechargeAmount(source.getTotalRechargeAmount());
        target.setLastConsumeTime(source.getLastConsumeTime());
        target.setLastRechargeTime(source.getLastRechargeTime());
        target.setAccountCreateTime(source.getAccountCreateTime());
        target.setActiveTime(source.getActiveTime());
        target.setFreezeTime(source.getFreezeTime());
        target.setCloseTime(source.getCloseTime());
        target.setFreezeReason(source.getFreezeReason());
        target.setCloseReason(source.getCloseReason());

        // 复制BaseEntity的审计字段
        target.setCreateUserId(source.getCreateUserId());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateUserId(source.getUpdateUserId());
        target.setUpdateTime(source.getUpdateTime());
        target.setDeletedFlag(source.getDeletedFlag());
        target.setVersion(source.getVersion());
    }

    /**
     * 复制扩展字段到AccountEntity
     *
     * @param source 源扩展实体
     * @param target 目标AccountEntity
     */
    private static void copyExtensionFields(AccountExtensionEntity source, AccountEntity target) {
        target.setMonthlyLimit(source.getMonthlyLimit());
        target.setDailyLimit(source.getDailyLimit());
        target.setSingleLimit(source.getSingleLimit());
        target.setCurrentMonthlyAmount(source.getCurrentMonthlyAmount());
        target.setCurrentDailyAmount(source.getCurrentDailyAmount());
        target.setPassword(source.getPassword());
        target.setPayPassword(source.getPayPassword());
        target.setCardId(source.getCardId());
        target.setCardStatus(source.getCardStatus());
        target.setFaceFeatureId(source.getFaceFeatureId());
        target.setFingerprintId(source.getFingerprintId());
        target.setPhoneNumber(source.getPhoneNumber());
        target.setEmail(source.getEmail());
        target.setSmsNotification(source.getSmsNotification());
        target.setEmailNotification(source.getEmailNotification());
        target.setConsumeReminder(source.getConsumeReminder());
        target.setReminderThreshold(source.getReminderThreshold());
        target.setAutoRecharge(source.getAutoRecharge());
        target.setAutoRechargeThreshold(source.getAutoRechargeThreshold());
        target.setAutoRechargeAmount(source.getAutoRechargeAmount());
        target.setPaymentMethods(source.getPaymentMethods());
        target.setAccountConfig(source.getAccountConfig());
        target.setExtendData(source.getExtendData());
        target.setPasswordResetTime(source.getPasswordResetTime());
        target.setPayPasswordResetTime(source.getPayPasswordResetTime());
        target.setLastLoginIp(source.getLastLoginIp());
        target.setLastLoginTime(source.getLastLoginTime());
        target.setLoginFailCount(source.getLoginFailCount());
        target.setLockTime(source.getLockTime());
        target.setRemark(source.getRemark());
    }

    /**
     * 从AccountEntity复制扩展字段到ExtensionEntity
     *
     * @param source 源AccountEntity
     * @param target 目标扩展实体
     */
    private static void copyToExtensionFields(AccountEntity source, AccountExtensionEntity target) {
        target.setAccountId(source.getAccountId());
        target.setMonthlyLimit(source.getMonthlyLimit());
        target.setDailyLimit(source.getDailyLimit());
        target.setSingleLimit(source.getSingleLimit());
        target.setCurrentMonthlyAmount(source.getCurrentMonthlyAmount());
        target.setCurrentDailyAmount(source.getCurrentDailyAmount());
        target.setPassword(source.getPassword());
        target.setPayPassword(source.getPayPassword());
        target.setCardId(source.getCardId());
        target.setCardStatus(source.getCardStatus());
        target.setFaceFeatureId(source.getFaceFeatureId());
        target.setFingerprintId(source.getFingerprintId());
        target.setPhoneNumber(source.getPhoneNumber());
        target.setEmail(source.getEmail());
        target.setSmsNotification(source.getSmsNotification());
        target.setEmailNotification(source.getEmailNotification());
        target.setConsumeReminder(source.getConsumeReminder());
        target.setReminderThreshold(source.getReminderThreshold());
        target.setAutoRecharge(source.getAutoRecharge());
        target.setAutoRechargeThreshold(source.getAutoRechargeThreshold());
        target.setAutoRechargeAmount(source.getAutoRechargeAmount());
        target.setPaymentMethods(source.getPaymentMethods());
        target.setAccountConfig(source.getAccountConfig());
        target.setExtendData(source.getExtendData());
        target.setPasswordResetTime(source.getPasswordResetTime());
        target.setPayPasswordResetTime(source.getPayPasswordResetTime());
        target.setLastLoginIp(source.getLastLoginIp());
        target.setLastLoginTime(source.getLastLoginTime());
        target.setLoginFailCount(source.getLoginFailCount());
        target.setLockTime(source.getLockTime());
        target.setRemark(source.getRemark());

        // 复制BaseEntity的审计字段
        target.setCreateUserId(source.getCreateUserId());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateUserId(source.getUpdateUserId());
        target.setUpdateTime(source.getUpdateTime());
        target.setDeletedFlag(source.getDeletedFlag());
        target.setVersion(source.getVersion());
    }

    /**
     * 更新基础实体的消费相关金额
     *
     * @param baseEntity       基础实体
     * @param consumeAmount    消费金额
     * @param rechargeAmount   充值金额（可为null）
     */
    public static void updateConsumptionAmounts(AccountBaseEntity baseEntity, BigDecimal consumeAmount, BigDecimal rechargeAmount) {
        if (baseEntity == null) {
            return;
        }

        try {
            // 更新累计消费金额
            if (consumeAmount != null) {
                baseEntity.updateTotalConsumeAmount(consumeAmount);
                baseEntity.setLastConsumeTime(java.time.LocalDateTime.now());
            }

            // 更新累计充值金额
            if (rechargeAmount != null) {
                baseEntity.updateTotalRechargeAmount(rechargeAmount);
                baseEntity.setLastRechargeTime(java.time.LocalDateTime.now());
            }

            // 重新计算可用额度
            baseEntity.setAvailableLimit(baseEntity.calculateAvailableLimit());
        } catch (Exception e) {
            log.error("更新消费金额失败，accountId: {}", baseEntity.getAccountId(), e);
        }
    }

    /**
     * 验证基础实体和扩展实体的关联性
     *
     * @param baseEntity       基础实体
     * @param extensionEntity  扩展实体
     * @return 验证结果
     */
    public static boolean validateEntityRelation(AccountBaseEntity baseEntity, AccountExtensionEntity extensionEntity) {
        if (baseEntity == null || extensionEntity == null) {
            return false;
        }

        return baseEntity.getAccountId().equals(extensionEntity.getAccountId());
    }

    /**
     * 创建空的扩展实体（用于新账户创建）
     *
     * @param accountId 账户ID
     * @return 空的扩展实体
     */
    public static AccountExtensionEntity createEmptyExtension(Long accountId) {
        if (accountId == null) {
            return null;
        }

        AccountExtensionEntity extensionEntity = new AccountExtensionEntity();
        extensionEntity.setAccountId(accountId);

        // 设置默认值
        extensionEntity.setSmsNotification(false);
        extensionEntity.setEmailNotification(false);
        extensionEntity.setConsumeReminder(true);
        extensionEntity.setAutoRecharge(false);
        extensionEntity.setCardStatus("NORMAL");
        extensionEntity.setLoginFailCount(0);

        return extensionEntity;
    }

    /**
     * 比较两个账户实体是否相等（基于accountId）
     *
     * @param account1 第一个账户实体
     * @param account2 第二个账户实体
     * @return 是否相等
     */
    public static boolean areAccountsEqual(AccountEntity account1, AccountEntity account2) {
        if (account1 == null || account2 == null) {
            return false;
        }

        return java.util.Objects.equals(account1.getAccountId(), account2.getAccountId());
    }

    /**
     * 获取账户的显示名称
     *
     * @param baseEntity 基础实体
     * @return 显示名称（优先使用personName，其次employeeNo，最后accountNo）
     */
    public static String getAccountDisplayName(AccountBaseEntity baseEntity) {
        if (baseEntity == null) {
            return "未知账户";
        }

        if (baseEntity.getPersonName() != null && !baseEntity.getPersonName().trim().isEmpty()) {
            return baseEntity.getPersonName();
        }

        if (baseEntity.getEmployeeNo() != null && !baseEntity.getEmployeeNo().trim().isEmpty()) {
            return baseEntity.getEmployeeNo();
        }

        if (baseEntity.getAccountNo() != null && !baseEntity.getAccountNo().trim().isEmpty()) {
            return baseEntity.getAccountNo();
        }

        return "ID:" + baseEntity.getAccountId();
    }
}