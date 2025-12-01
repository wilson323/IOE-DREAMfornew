package net.lab1024.sa.admin.module.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.form.AccountCreateForm;
import net.lab1024.sa.admin.module.consume.domain.form.AccountRechargeForm;
import net.lab1024.sa.admin.module.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountDetailVO;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountVO;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;

/**
 * 账户服务接口
 * 负责消费账户的全生命周期管理
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface AccountService {

    /**
     * 根据人员ID获取账户信息
     *
     * @param personId 人员ID
     * @return 账户信息
     */
    AccountEntity getByPersonId(Long personId);

    /**
     * 根据账户ID获取账户信息
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    AccountEntity getById(Long accountId);

    /**
     * 扣减账户余额
     * 原子性操作，使用乐观锁确保并发安全
     *
     * @param accountId 账户ID
     * @param amount    扣减金额
     * @param orderNo   订单号（用于幂等性控制）
     * @return 是否成功
     */
    boolean deductBalance(Long accountId, BigDecimal amount, String orderNo);

    /**
     * 增加账户余额
     * 原子性操作，用于充值
     *
     * @param accountId  账户ID
     * @param amount     增加金额
     * @param rechargeNo 充值单号
     * @return 是否成功
     */
    boolean addBalance(Long accountId, BigDecimal amount, String rechargeNo);

    /**
     * 冻结账户金额
     *
     * @param accountId 账户ID
     * @param amount    冻结金额
     * @param reason    冻结原因
     * @return 是否成功
     */
    boolean freezeAmount(Long accountId, BigDecimal amount, String reason);

    /**
     * 解冻账户金额
     *
     * @param accountId 账户ID
     * @param amount    解冻金额
     * @param reason    解冻原因
     * @return 是否成功
     */
    boolean unfreezeAmount(Long accountId, BigDecimal amount, String reason);

    /**
     * 更新账户状态
     *
     * @param accountId 账户ID
     * @param status    新状态
     * @param reason    变更原因
     * @return 是否成功
     */
    boolean updateAccountStatus(Long accountId, String status, String reason);

    /**
     * 创建新账户
     *
     * @param createForm 账户创建表单
     * @return 创建的账户ID
     */
    String createAccount(AccountCreateForm createForm);

    /**
     * 获取今日消费金额
     *
     * @param personId 人员ID
     * @return 今日消费金额
     */
    BigDecimal getTodayConsumeAmount(Long personId);

    /**
     * 获取本月消费金额
     *
     * @param personId 人员ID
     * @return 本月消费金额
     */
    BigDecimal getMonthlyConsumeAmount(Long personId);

    /**
     * 查询账户列表
     *
     * @param personId    人员ID（可选）
     * @param accountType 账户类型（可选）
     * @param status      账户状态（可选）
     * @return 账户列表
     */
    List<AccountEntity> queryAccounts(Long personId, String accountType, String status);

    /**
     * 验证账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount    需要的金额
     * @return 是否充足
     */
    boolean validateBalance(Long accountId, BigDecimal amount);

    /**
     * 验证缓存一致性
     *
     * @param accountId 账户ID
     * @return 缓存是否一致
     */
    boolean validateCacheConsistency(Long accountId);

    /**
     * 修复缓存一致性
     *
     * @param accountId 账户ID
     */
    void repairCacheConsistency(Long accountId);

    /**
     * 分页查询账户列表
     *
     * @param pageParam   分页参数
     * @param accountName 账户名称（可选）
     * @param status      账户状态（可选）
     * @param accountType 账户类型（可选）
     * @return 分页结果
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
     * @param updateForm 账户更新表单
     * @return 更新结果消息
     */
    String updateAccount(AccountUpdateForm updateForm);

    /**
     * 账户充值
     *
     * @param rechargeForm 充值表单
     * @return 充值结果消息
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
     * @param reason    冻结原因
     * @return 冻结结果消息
     */
    String freezeAccount(Long accountId, String reason);

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @param reason    解冻原因
     * @return 解冻结果消息
     */
    String unfreezeAccount(Long accountId, String reason);

    /**
     * 关闭账户
     *
     * @param accountId 账户ID
     * @param reason    关闭原因
     * @return 关闭结果消息
     */
    String closeAccount(Long accountId, String reason);

    /**
     * 获取账户交易记录
     *
     * @param accountId       账户ID
     * @param pageParam       分页参数
     * @param startTime       开始时间（可选）
     * @param endTime         结束时间（可选）
     * @param transactionType 交易类型（可选）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getAccountTransactions(Long accountId, PageParam pageParam,
            LocalDateTime startTime, LocalDateTime endTime, String transactionType);

    /**
     * 获取账户统计
     *
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 统计数据
     */
    Map<String, Object> getAccountStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 导出账户数据
     *
     * @param accountName 账户名称（可选）
     * @param status      账户状态（可选）
     * @param accountType 账户类型（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
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
     * @param status     新状态
     * @param reason     变更原因（可选）
     * @return 更新结果消息
     */
    String batchUpdateStatus(List<Long> accountIds, Integer status, String reason);
}
