package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountAddForm;
import net.lab1024.sa.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.common.domain.PageResult;

/**
 * 账户服务接口
 * <p>
 * 提供账户管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO（Controller层返回ResponseDTO）
 * </p>
 * <p>
 * 业务场景：
 * - 账户创建、查询、更新、删除
 * - 账户余额管理（增加、扣减、冻结、解冻）
 * - 账户状态管理（启用、禁用、冻结、解冻、关闭）
 * - 账户查询和统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccountService {

    /**
     * 创建账户
     *
     * @param form 账户创建表单
     * @return 账户ID
     */
    Long createAccount(AccountAddForm form);

    /**
     * 更新账户信息
     *
     * @param form 账户更新表单
     * @return 是否成功
     */
    boolean updateAccount(AccountUpdateForm form);

    /**
     * 删除账户（软删除）
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    boolean deleteAccount(Long accountId);

    /**
     * 根据账户ID查询账户
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    AccountEntity getById(Long accountId);

    /**
     * 根据用户ID查询账户
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    AccountEntity getByUserId(Long userId);

    /**
     * 分页查询账户列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 关键词（账户ID、用户ID、用户姓名）
     * @param accountKindId 账户类别ID（可选）
     * @param status 账户状态（可选）
     * @return 账户列表
     */
    PageResult<AccountEntity> pageAccounts(Integer pageNum, Integer pageSize, 
            String keyword, Long accountKindId, Integer status);

    /**
     * 查询账户列表（不分页）
     *
     * @param accountKindId 账户类别ID（可选）
     * @param status 账户状态（可选）
     * @return 账户列表
     */
    List<AccountEntity> listAccounts(Long accountKindId, Integer status);

    /**
     * 获取账户详情（包含统计信息）
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    AccountEntity getAccountDetail(Long accountId);

    /**
     * 增加账户余额
     *
     * @param accountId 账户ID
     * @param amount 增加金额（单位：元）
     * @param reason 增加原因
     * @return 是否成功
     */
    boolean addBalance(Long accountId, BigDecimal amount, String reason);

    /**
     * 扣减账户余额
     *
     * @param accountId 账户ID
     * @param amount 扣减金额（单位：元）
     * @param reason 扣减原因
     * @return 是否成功
     */
    boolean deductBalance(Long accountId, BigDecimal amount, String reason);

    /**
     * 冻结账户金额
     *
     * @param accountId 账户ID
     * @param amount 冻结金额（单位：元）
     * @param reason 冻结原因
     * @return 是否成功
     */
    boolean freezeAmount(Long accountId, BigDecimal amount, String reason);

    /**
     * 解冻账户金额
     *
     * @param accountId 账户ID
     * @param amount 解冻金额（单位：元）
     * @param reason 解冻原因
     * @return 是否成功
     */
    boolean unfreezeAmount(Long accountId, BigDecimal amount, String reason);

    /**
     * 验证账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount 需要金额（单位：元）
     * @return 是否充足
     */
    boolean validateBalance(Long accountId, BigDecimal amount);

    /**
     * 启用账户
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    boolean enableAccount(Long accountId);

    /**
     * 禁用账户
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    boolean disableAccount(Long accountId);

    /**
     * 冻结账户
     *
     * @param accountId 账户ID
     * @param reason 冻结原因
     * @return 是否成功
     */
    boolean freezeAccount(Long accountId, String reason);

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    boolean unfreezeAccount(Long accountId);

    /**
     * 关闭账户
     *
     * @param accountId 账户ID
     * @param reason 关闭原因
     * @return 是否成功
     */
    boolean closeAccount(Long accountId, String reason);

    /**
     * 批量更新账户状态
     *
     * @param accountIds 账户ID列表
     * @param status 目标状态
     * @return 成功更新的数量
     */
    int batchUpdateStatus(List<Long> accountIds, Integer status);

    /**
     * 批量创建账户
     *
     * @param forms 账户创建表单列表
     * @return 成功创建的账户ID列表
     */
    List<Long> batchCreateAccounts(List<AccountAddForm> forms);

    /**
     * 获取账户余额
     *
     * @param accountId 账户ID
     * @return 账户余额（单位：元）
     */
    BigDecimal getBalance(Long accountId);

    /**
     * 获取账户统计信息
     *
     * @param accountKindId 账户类别ID（可选）
     * @return 统计信息
     */
    Map<String, Object> getAccountStatistics(Long accountKindId);

    /**
     * 批量查询账户信息
     * <p>
     * 根据账户ID列表批量查询账户信息，使用IN查询提升性能
     * </p>
     *
     * @param accountIds 账户ID列表
     * @return 账户列表
     */
    List<AccountEntity> getAccountsByIds(List<Long> accountIds);
}

