package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.AccountCreateForm;
import net.lab1024.sa.consume.domain.form.AccountRechargeForm;
import net.lab1024.sa.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.consume.domain.vo.AccountDetailVO;
import net.lab1024.sa.consume.domain.vo.AccountVO;

/**
 * 账户服务接口
 *
 * @author OpenSpec Task 2.10 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
public interface AccountService {

    /**
     * 创建账户
     *
     * @param createForm 创建表单
     * @return 账户ID
     */
    String createAccount(AccountCreateForm createForm);

    /**
     * 获取账户列表
     *
     * @param pageParam 分页参数
     * @param accountName 账户名称
     * @param status 状态
     * @param accountType 账户类型
     * @return 账户列表
     */
    PageResult<AccountVO> getAccountList(PageParam pageParam, String accountName, Integer status, String accountType);

    /**
     * 获取账户详情
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    AccountDetailVO getAccountDetail(Long accountId);

    /**
     * 更新账户
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    String updateAccount(AccountUpdateForm updateForm);

    /**
     * 账户充值
     *
     * @param rechargeForm 充值表单
     * @return 操作结果
     */
    String rechargeAccount(AccountRechargeForm rechargeForm);

    /**
     * 获取账户余额
     *
     * @param accountId 账户ID
     * @return 账户余额
     */
    BigDecimal getAccountBalance(Long accountId);

    /**
     * 冻结账户
     *
     * @param accountId 账户ID
     * @param reason 原因
     * @return 操作结果
     */
    String freezeAccount(Long accountId, String reason);

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @param reason 原因
     * @return 操作结果
     */
    String unfreezeAccount(Long accountId, String reason);

    /**
     * 关闭账户
     *
     * @param accountId 账户ID
     * @param reason 原因
     * @return 操作结果
     */
    String closeAccount(Long accountId, String reason);

    /**
     * 获取账户交易记录
     *
     * @param accountId 账户ID
     * @param pageParam 分页参数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param transactionType 交易类型
     * @return 交易记录
     */
    PageResult<Map<String, Object>> getAccountTransactions(Long accountId, PageParam pageParam,
            LocalDateTime startTime, LocalDateTime endTime, String transactionType);

    /**
     * 获取账户统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    Map<String, Object> getAccountStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 导出账户
     *
     * @param accountName 账户名称
     * @param status 状态
     * @param accountType 账户类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导出文件路径
     */
    String exportAccounts(String accountName, Integer status, String accountType,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取账户类型列表
     *
     * @return 账户类型列表
     */
    List<Map<String, Object>> getAccountTypes();

    /**
     * 批量更新账户状态
     *
     * @param accountIds 账户ID列表
     * @param status 状态
     * @param reason 原因
     * @return 操作结果
     */
    String batchUpdateStatus(List<Long> accountIds, Integer status, String reason);
}

