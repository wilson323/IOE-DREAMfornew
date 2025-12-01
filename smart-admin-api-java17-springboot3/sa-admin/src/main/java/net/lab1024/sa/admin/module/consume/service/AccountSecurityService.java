/*
 * 账户安全控制服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.result.AccountSecurityResult;
import net.lab1024.sa.admin.module.consume.domain.vo.BatchSecurityResult;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountSecurityScore;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountSecurityConfig;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountRiskInfo;
import net.lab1024.sa.admin.module.consume.domain.vo.OperationPermissionResult;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountSecurityReport;
import net.lab1024.sa.admin.module.consume.domain.vo.EmailServiceStatus;
import net.lab1024.sa.admin.module.consume.domain.result.PaymentPasswordResult;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountFreezeInfo;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountFreezeHistory;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountFreezeRequest;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountSecurityEvent;
import net.lab1024.sa.admin.module.consume.domain.vo.EmergencyContact;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountUnfreezeRequest;
import net.lab1024.sa.admin.module.consume.domain.vo.AutoFreezeRule;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountSecurityStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户安全控制服务接口
 * 提供账户冻结、解冻、安全控制等功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface AccountSecurityService {

    /**
     * 冻结账户
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @param freezeReason 冻结原因
     * @param freezeMinutes 冻结时长（分钟）
     * @param operatorId 操作人ID
     * @return 冻结结果
     */
    AccountSecurityResult freezeAccount(@NotNull Long accountId, @NotNull Long personId,
                                         @NotNull String freezeReason, Integer freezeMinutes,
                                         Long operatorId);

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @param unfreezeReason 解冻原因
     * @param operatorId 操作人ID
     * @return 解冻结果
     */
    AccountSecurityResult unfreezeAccount(@NotNull Long accountId, @NotNull Long personId,
                                           @NotNull String unfreezeReason, Long operatorId);

    /**
     * 临时冻结账户
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @param reason 冻结原因
     * @param freezeMinutes 冻结时长（分钟）
     * @param operatorId 操作人ID
     * @return 冻结结果
     */
    AccountSecurityResult temporaryFreezeAccount(@NotNull Long accountId, @NotNull Long personId,
                                                 @NotNull String reason, @NotNull Integer freezeMinutes,
                                                 Long operatorId);

    /**
     * 永久冻结账户
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @param reason 冻结原因
     * @param operatorId 操作人ID
     * @return 冻结结果
     */
    AccountSecurityResult permanentFreezeAccount(@NotNull Long accountId, @NotNull Long personId,
                                                @NotNull String reason, Long operatorId);

    /**
     * 检查账户是否被冻结
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @return 是否被冻结
     */
    boolean isAccountFrozen(@NotNull Long accountId, @NotNull Long personId);

    /**
     * 获取账户冻结信息
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @return 冻结信息
     */
    AccountFreezeInfo getAccountFreezeInfo(@NotNull Long accountId, @NotNull Long personId);

    /**
     * 延长冻结时间
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @param extendMinutes 延长时长（分钟）
     * @param reason 延长原因
     * @param operatorId 操作人ID
     * @return 延长结果
     */
    AccountSecurityResult extendFreezeTime(@NotNull Long accountId, @NotNull Long personId,
                                            @NotNull Integer extendMinutes, @NotNull String reason,
                                            Long operatorId);

    /**
     * 修改冻结原因
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @param newReason 新的冻结原因
     * @param operatorId 操作人ID
     * @return 修改结果
     */
    boolean modifyFreezeReason(@NotNull Long accountId, @NotNull Long personId,
                               @NotNull String newReason, Long operatorId);

    /**
     * 获取冻结历史记录
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @param limit 记录数量限制
     * @return 冻结历史记录
     */
    List<AccountFreezeHistory> getFreezeHistory(@NotNull Long accountId, @NotNull Long personId, Integer limit);

    /**
     * 批量冻结账户
     *
     * @param freezeRequests 冻结请求列表
     * @param operatorId 操作人ID
     * @return 批量冻结结果
     */
    BatchSecurityResult batchFreezeAccounts(@NotNull List<AccountFreezeRequest> freezeRequests,
                                            Long operatorId);

    /**
     * 批量解冻账户
     *
     * @param unfreezeRequests 解冻请求列表
     * @param operatorId 操作人ID
     * @return 批量解冻结果
     */
    BatchSecurityResult batchUnfreezeAccounts(@NotNull List<AccountUnfreezeRequest> unfreezeRequests,
                                              Long operatorId);

    /**
     * 设置自动冻结规则
     *
     * @param personId 人员ID
     * @param ruleType 规则类型
     * @param rules 规则配置
     * @return 设置结果
     */
    boolean setAutoFreezeRules(@NotNull Long personId, @NotNull String ruleType,
                                @NotNull List<AutoFreezeRule> rules);

    /**
     * 获取自动冻结规则
     *
     * @param personId 人员ID
     * @param ruleType 规则类型
     * @return 规则列表
     */
    List<AutoFreezeRule> getAutoFreezeRules(@NotNull Long personId, String ruleType);

    /**
     * 检查是否触发自动冻结
     *
     * @param personId 人员ID
     * @param triggerEvent 触发事件
     * @param eventData 事件数据
     * @return 是否触发冻结
     */
    boolean checkAutoFreezeTrigger(@NotNull Long personId, @NotNull String triggerEvent,
                                   Object eventData);

    /**
     * 执行自动冻结
     *
     * @param personId 人员ID
     * @param triggerRule 触发的规则
     * @param eventData 事件数据
     * @return 执行结果
     */
    AccountSecurityResult executeAutoFreeze(@NotNull Long personId, @NotNull AutoFreezeRule triggerRule,
                                            Object eventData);

    /**
     * 获取账户安全状态
     *
     * @param accountId 账户ID
     * @param personId 人员ID
     * @return 安全状态
     */
    AccountSecurityStatus getAccountSecurityStatus(@NotNull Long accountId, @NotNull Long personId);

    /**
     * 获取账户安全评分
     *
     * @param personId 人员ID
     * @return 安全评分
     */
    AccountSecurityScore getAccountSecurityScore(@NotNull Long personId);

    /**
     * 更新账户安全评分
     *
     * @param personId 人员ID
     * @param securityEvent 安全事件
     * @return 更新结果
     */
    boolean updateSecurityScore(@NotNull Long personId, @NotNull AccountSecurityEvent securityEvent);

    /**
     * 设置账户安全级别
     *
     * @param personId 人员ID
     * @param securityLevel 安全级别
     * @param reason 设置原因
     * @param operatorId 操作人ID
     * @return 设置结果
     */
    boolean setSecurityLevel(@NotNull Long personId, @NotNull String securityLevel,
                             String reason, Long operatorId);

    /**
     * 获取账户安全配置
     *
     * @param personId 人员ID
     * @return 安全配置
     */
    AccountSecurityConfig getAccountSecurityConfig(@NotNull Long personId);

    /**
     * 更新账户安全配置
     *
     * @param personId 人员ID
     * @param securityConfig 安全配置
     * @return 更新结果
     */
    boolean updateSecurityConfig(@NotNull Long personId, @NotNull AccountSecurityConfig securityConfig);

    /**
     * 生成安全报告
     *
     * @param personId 人员ID
     * @param reportType 报告类型
     * @param timeRange 时间范围
     * @return 安全报告
     */
    AccountSecurityReport generateSecurityReport(@NotNull Long personId, @NotNull String reportType,
                                                   String timeRange);

    /**
     * 获取高风险账户列表
     *
     * @param riskLevel 风险级别
     * @param limit 限制数量
     * @return 高风险账户列表
     */
    List<AccountRiskInfo> getHighRiskAccounts(@NotNull String riskLevel, Integer limit);

    /**
     * 检查账户操作权限
     *
     * @param personId 人员ID
     * @param operationType 操作类型
     * @param operationData 操作数据
     * @return 权限检查结果
     */
    OperationPermissionResult checkOperationPermission(@NotNull Long personId, @NotNull String operationType,
                                                        Object operationData);

    /**
     * 记录安全事件
     *
     * @param securityEvent 安全事件
     * @return 记录结果
     */
    boolean recordSecurityEvent(@NotNull AccountSecurityEvent securityEvent);

    /**
     * 获取安全事件历史
     *
     * @param personId 人员ID
     * @param eventType 事件类型
     * @param limit 记录数量限制
     * @return 安全事件历史
     */
    List<AccountSecurityEvent> getSecurityEventHistory(@NotNull Long personId, String eventType,
                                                       Integer limit);

    /**
     * 设置紧急联系人
     *
     * @param personId 人员ID
     * @param contacts 紧急联系人列表
     * @return 设置结果
     */
    boolean setEmergencyContacts(@NotNull Long personId, @NotNull List<EmergencyContact> contacts);

    /**
     * 获取紧急联系人
     *
     * @param personId 人员ID
     * @return 紧急联系人列表
     */
    List<EmergencyContact> getEmergencyContacts(@NotNull Long personId);

    /**
     * 发送安全通知
     *
     * @param personId 人员ID
     * @param notificationType 通知类型
     * @param message 通知消息
     * @param relatedData 相关数据
     * @return 发送结果
     */
    boolean sendSecurityNotification(@NotNull Long personId, @NotNull String notificationType,
                                        @NotNull String message, Object relatedData);
}