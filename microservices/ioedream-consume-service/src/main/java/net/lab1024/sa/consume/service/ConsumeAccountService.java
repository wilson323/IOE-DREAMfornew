package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeAccountAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountUpdateForm;
import net.lab1024.sa.consume.domain.form.ConsumeAccountRechargeForm;
import net.lab1024.sa.consume.domain.vo.ConsumeAccountVO;

/**
 * 消费账户服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeAccountService {

    /**
     * 分页查询账户列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeAccountVO> queryAccounts(ConsumeAccountQueryForm queryForm);

    /**
     * 获取账户详情
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    ConsumeAccountVO getAccountDetail(Long accountId);

    /**
     * 根据用户ID获取账户
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    ConsumeAccountVO getAccountByUserId(Long userId);

    /**
     * 创建账户
     *
     * @param addForm 创建表单
     * @return 账户ID
     */
    Long createAccount(ConsumeAccountAddForm addForm);

    /**
     * 更新账户信息
     *
     * @param accountId 账户ID
     * @param updateForm 更新表单
     */
    void updateAccount(Long accountId, ConsumeAccountUpdateForm updateForm);

    /**
     * 账户充值
     *
     * @param accountId 账户ID
     * @param rechargeForm 充值表单
     * @return 充值结果
     */
    Boolean rechargeAccount(Long accountId, ConsumeAccountRechargeForm rechargeForm);

    /**
     * 账户扣款
     *
     * @param accountId 账户ID
     * @param amount 扣款金额
     * @param description 扣款描述
     * @return 扣款结果
     */
    Boolean deductAmount(Long accountId, BigDecimal amount, String description);

    /**
     * 账户退款
     *
     * @param accountId 账户ID
     * @param amount 退款金额
     * @param reason 退款原因
     * @return 退款结果
     */
    Boolean refundAmount(Long accountId, BigDecimal amount, String reason);

    /**
     * 冻结账户
     *
     * @param accountId 账户ID
     * @param reason 冻结原因
     */
    void freezeAccount(Long accountId, String reason);

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @param reason 解冻原因
     */
    void unfreezeAccount(Long accountId, String reason);

    /**
     * 注销账户
     *
     * @param accountId 账户ID
     * @param reason 注销原因
     */
    void closeAccount(Long accountId, String reason);

    /**
     * 获取账户余额
     *
     * @param accountId 账户ID
     * @return 账户余额
     */
    BigDecimal getAccountBalance(Long accountId);

    /**
     * 获取用户消费统计
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费统计
     */
    java.util.Map<String, Object> getUserConsumeStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取活跃账户列表
     *
     * @return 活跃账户列表
     */
    List<ConsumeAccountVO> getActiveAccounts();

    /**
     * 批量创建账户
     *
     * @param addForms 账户列表
     * @return 创建结果
     */
    java.util.Map<String, Object> batchCreateAccounts(List<ConsumeAccountAddForm> addForms);
}