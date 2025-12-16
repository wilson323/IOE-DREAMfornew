package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.dto.RechargeRequestDTO;
import net.lab1024.sa.consume.domain.form.AccountQueryForm;
import net.lab1024.sa.consume.domain.vo.AccountVO;

import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 消费账户服务接口
 *
 * @author IOE-DREAM
 * @since 2025-12-09
 */
public interface ConsumeAccountService {

    /**
     * 分页查询账户列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    ResponseDTO<IPage<AccountVO>> queryAccountPage(AccountQueryForm queryForm);

    /**
     * 根据ID查询账户详情
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    ResponseDTO<AccountVO> getAccountById(Long accountId);

    /**
     * 根据账户编号查询账户详情
     *
     * @param accountNo 账户编号
     * @return 账户详情
     */
    ResponseDTO<AccountVO> getAccountByNo(String accountNo);

    /**
     * 根据用户ID查询账户
     *
     * @param userId 用户ID
     * @return 账户详情
     */
    AccountVO getAccountByUserId(Long userId);

    /**
     * 账户充值
     *
     * @param rechargeRequest 充值请求
     * @return 充值结果
     */
    ResponseDTO<Void> recharge(RechargeRequestDTO rechargeRequest);

    /**
     * 账户扣费
     *
     * @param accountId 账户ID
     * @param amount    扣费金额
     * @return 扣费结果
     */
    ResponseDTO<Void> deduct(Long accountId, BigDecimal amount);

    /**
     * 账户退款
     *
     * @param accountId 账户ID
     * @param amount    退款金额
     * @param reason    退款原因
     * @return 退款结果
     */
    ResponseDTO<Void> refund(Long accountId, BigDecimal amount, String reason);

    /**
     * 账户冻结
     *
     * @param accountId 账户ID
     * @param amount    冻结金额
     * @return 冻结结果
     */
    ResponseDTO<Void> freeze(Long accountId, BigDecimal amount);

    /**
     * 账户解冻
     *
     * @param accountId 账户ID
     * @param amount    解冻金额
     * @return 解冻结果
     */
    ResponseDTO<Void> unfreeze(Long accountId, BigDecimal amount);

    /**
     * 账户启用/禁用
     *
     * @param accountId 账户ID
     * @param enabled   是否启用
     * @return 操作结果
     */
    ResponseDTO<Void> updateAccountStatus(Long accountId, Boolean enabled);

    /**
     * 导出账户数据
     *
     * @param queryForm 查询条件
     * @param response  HTTP响应
     */
    void exportAccountData(AccountQueryForm queryForm, HttpServletResponse response);

    /**
     * 获取账户余额
     *
     * @param accountId 账户ID
     * @return 账户余额
     */
    ResponseDTO<BigDecimal> getAccountBalance(Long accountId);

    /**
     * 更新账户信息
     *
     * @param accountVO 账户信息
     * @return 更新结果
     */
    ResponseDTO<Void> updateAccount(AccountVO accountVO);

    /**
     * 检查账户是否存在
     *
     * @param accountId 账户ID
     * @return 是否存在
     */
    Boolean existsAccount(Long accountId);

    /**
     * 检查账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount    需要的金额
     * @return 是否充足
     */
    Boolean checkBalance(Long accountId, BigDecimal amount);

    /**
     * 账户余额变动通知
     *
     * @param accountId 账户ID
     * @param oldBalance 变动前余额
     * @param newBalance 变动后余额
     * @param changeType 变动类型
     * @param remark     备注
     */
    void balanceChangeNotification(Long accountId, BigDecimal oldBalance, BigDecimal newBalance, String changeType, String remark);

    /**
     * 获取用户账户余额信息
     *
     * @param userId 用户ID
     * @return 账户余额信息
     */
    Map<String, Object> getUserBalanceInfo(Long userId);

   
    /**
     * 冻结账户
     *
     * @param accountId 账户ID
     * @param reason 冻结原因
     * @param freezeDays 冻结天数
     * @return 是否成功
     */
    boolean freezeAccount(Long accountId, String reason, Integer freezeDays);

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @param reason 解冻原因
     * @return 是否成功
     */
    boolean unfreezeAccount(Long accountId, String reason);

    /**
     * 账户充值
     *
     * @param accountId 账户ID
     * @param amount 充值金额
     * @param rechargeType 充值类型
     * @param remark 备注
     * @return 是否成功
     */
    boolean rechargeAccount(Long accountId, BigDecimal amount, String rechargeType, String remark);

    /**
     * 设置账户限额
     *
     * @param accountId 账户ID
     * @param dailyLimit 日限额
     * @param monthlyLimit 月限额
     * @return 是否成功
     */
    boolean setAccountLimit(Long accountId, BigDecimal dailyLimit, BigDecimal monthlyLimit);

    /**
     * 批量更新账户状态
     *
     * @param accountIds 账户ID列表
     * @param operationType 操作类型
     * @param reason 操作原因
     * @return 成功数量
     */
    int batchUpdateAccountStatus(java.util.List<Long> accountIds, String operationType, String reason);

    /**
     * 获取账户统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getAccountStatistics();

   
    /**
     * 获取账户消费记录
     *
     * @param accountId 账户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 消费记录
     */
    net.lab1024.sa.common.domain.PageResult<Map<String, Object>> getAccountConsumeRecords(Long accountId, Integer pageNum, Integer pageSize);

    /**
     * 检查账户状态
     *
     * @param accountId 账户ID
     * @return 状态信息
     */
    Map<String, Object> checkAccountStatus(Long accountId);

    /**
     * 创建账户
     *
     * @param accountEntity 账户实体
     * @return 账户ID
     */
    Long createAccount(net.lab1024.sa.common.consume.entity.AccountEntity accountEntity);

    /**
     * 更新账户
     *
     * @param accountEntity 账户实体
     * @return 是否成功
     */
    boolean updateAccount(net.lab1024.sa.common.consume.entity.AccountEntity accountEntity);

    /**
     * 删除账户
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    boolean deleteAccount(Long accountId);
}


