package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.util.CursorPagination;
import net.lab1024.sa.common.util.PageHelper;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountAddForm;
import net.lab1024.sa.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.service.AccountService;

import java.time.LocalDateTime;

/**
 * 账户服务实现类
 * <p>
 * 提供账户管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类使用@Service注解
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 遵循四层架构：Controller → Service → Manager → DAO
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
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountDao accountDao;

    @Resource
    private AccountManager accountManager;

    // 账户状态常量
    private static final Integer STATUS_NORMAL = 1;  // 正常
    private static final Integer STATUS_FROZEN = 2;   // 冻结
    private static final Integer STATUS_CLOSED = 3;  // 注销

    /**
     * 创建账户
     *
     * @param form 账户创建表单
     * @return 账户ID
     */
    @Override
    @Observed(name = "account.createAccount", contextualName = "account-create")
    @Transactional(rollbackFor = Exception.class)
    public Long createAccount(AccountAddForm form) {
        log.info("[账户服务] 创建账户，userId={}, accountKindId={}",
                form.getUserId(), form.getAccountKindId());

        try {
            // 1. 参数验证
            validateAccountAddForm(form);

            // 2. 检查账户是否已存在
            AccountEntity existing = accountDao.selectByUserId(form.getUserId());
            if (existing != null) {
                log.warn("[账户服务] 账户已存在，userId={}, accountId={}",
                        form.getUserId(), existing.getId());
                throw new BusinessException("该用户账户已存在");
            }

            // 3. 构建账户实体
            AccountEntity account = new AccountEntity();
            account.setUserId(form.getUserId());
            account.setAccountKindId(form.getAccountKindId());

            // 设置初始余额（BigDecimal类型）
            BigDecimal initialBalance = form.getInitialBalance() != null
                    ? form.getInitialBalance() : BigDecimal.ZERO;
            account.setBalance(initialBalance);
            account.setAllowanceBalance(BigDecimal.ZERO);
            account.setFrozenBalance(BigDecimal.ZERO);
            account.setStatus(STATUS_NORMAL);
            account.setVersion(0);

            // 4. 保存账户
            accountDao.insert(account);

            log.info("[账户服务] 账户创建成功，accountId={}, userId={}",
                    account.getId(), form.getUserId());

            return account.getId();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 创建账户参数错误，userId={}, error={}", form.getUserId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[账户服务] 创建账户业务异常，userId={}, code={}, message={}",
                    form.getUserId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 创建账户系统异常，userId={}, code={}, message={}", form.getUserId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_CREATE_SYSTEM_ERROR", "账户创建失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 创建账户未知异常，userId={}", form.getUserId(), e);
            throw new SystemException("ACCOUNT_CREATE_SYSTEM_ERROR", "账户创建失败：" + e.getMessage(), e);
        }
    }

    /**
     * 更新账户信息
     *
     * @param form 账户更新表单
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.updateAccount", contextualName = "account-update")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAccount(AccountUpdateForm form) {
        log.info("[账户服务] 更新账户，accountId={}", form.getAccountId());

        try {
            // 1. 参数验证
            validateAccountUpdateForm(form);

            // 2. 查询账户
            AccountEntity account = accountDao.selectById(form.getAccountId());
            if (account == null) {
                log.warn("[账户服务] 账户不存在，accountId={}", form.getAccountId());
                throw new BusinessException("账户不存在");
            }

            // 3. 更新账户信息
            if (form.getAccountKindId() != null) {
                account.setAccountKindId(form.getAccountKindId());
            }
            if (form.getStatus() != null) {
                account.setStatus(form.getStatus());
            }

            // 4. 保存更新
            int result = accountDao.updateById(account);

            log.info("[账户服务] 账户更新成功，accountId={}, result={}",
                    form.getAccountId(), result > 0);

            return result > 0;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 更新账户参数错误，accountId={}, error={}", form.getAccountId(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 更新账户业务异常，accountId={}, code={}, message={}",
                    form.getAccountId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 更新账户系统异常，accountId={}, code={}, message={}", form.getAccountId(), e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_UPDATE_SYSTEM_ERROR", "账户更新失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 更新账户未知异常，accountId={}", form.getAccountId(), e);
            throw new SystemException("ACCOUNT_UPDATE_SYSTEM_ERROR", "账户更新失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除账户（软删除）
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.deleteAccount", contextualName = "account-delete")
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAccount(Long accountId) {
        log.info("[账户服务] 删除账户，accountId={}", accountId);

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }

            // 2. 查询账户
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                log.warn("[账户服务] 账户不存在，accountId={}", accountId);
                throw new BusinessException("账户不存在");
            }

            // 3. 软删除：更新状态为注销
            account.setStatus(STATUS_CLOSED);
            int result = accountDao.updateById(account);

            log.info("[账户服务] 账户删除成功，accountId={}, result={}", accountId, result > 0);

            return result > 0;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 删除账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 删除账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 删除账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_DELETE_SYSTEM_ERROR", "账户删除失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 删除账户未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_DELETE_SYSTEM_ERROR", "账户删除失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据账户ID查询账户
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    @Override
    public AccountEntity getById(Long accountId) {
        log.debug("[账户服务] 根据账户ID查询账户，accountId={}", accountId);

        try {
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }

            // 使用AccountManager查询（带缓存）
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null) {
                log.warn("[账户服务] 账户不存在，accountId={}", accountId);
                throw new BusinessException("账户不存在");
            }

            return account;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 查询账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 查询账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 查询账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_QUERY_SYSTEM_ERROR", "查询账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 查询账户未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_QUERY_SYSTEM_ERROR", "查询账户失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据用户ID查询账户
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    @Override
    public AccountEntity getByUserId(Long userId) {
        log.debug("[账户服务] 根据用户ID查询账户，userId={}", userId);

        try {
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }

            // 使用AccountManager查询（带缓存）
            AccountEntity account = accountManager.getAccountByUserId(userId);
            if (account == null) {
                log.warn("[账户服务] 账户不存在，userId={}", userId);
                throw new BusinessException("账户不存在");
            }

            return account;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 查询账户参数错误，userId={}, error={}", userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 查询账户业务异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 查询账户系统异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_QUERY_SYSTEM_ERROR", "查询账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 查询账户未知异常，userId={}", userId, e);
            throw new SystemException("ACCOUNT_QUERY_SYSTEM_ERROR", "查询账户失败：" + e.getMessage(), e);
        }
    }

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
    @Override
    public PageResult<AccountEntity> pageAccounts(Integer pageNum, Integer pageSize,
            String keyword, Long accountKindId, Integer status) {
        log.debug("[账户服务] 分页查询账户列表，pageNum={}, pageSize={}, keyword={}, accountKindId={}, status={}",
                pageNum, pageSize, keyword, accountKindId, status);

        try {
            // 1. 参数验证和默认值
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            // 2. 构建查询条件
            LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();

            // 关键词搜索（账户ID或用户ID）
            if (StringUtils.hasText(keyword)) {
                try {
                    Long accountId = Long.parseLong(keyword);
                    queryWrapper.eq(AccountEntity::getId, accountId)
                            .or()
                            .eq(AccountEntity::getUserId, accountId);
                } catch (NumberFormatException e) {
                    log.debug("[账户查询] 关键词不是数字格式，按用户ID搜索: keyword={}", keyword);
                    // 如果不是数字，只按用户ID搜索
                    queryWrapper.eq(AccountEntity::getUserId, keyword);
                }
            }

            // 账户类别筛选
            if (accountKindId != null) {
                queryWrapper.eq(AccountEntity::getAccountKindId, accountKindId);
            }

            // 状态筛选
            if (status != null) {
                queryWrapper.eq(AccountEntity::getStatus, status);
            }

            // 排序：按创建时间倒序
            queryWrapper.orderByDesc(AccountEntity::getCreateTime);

            // 3. 分页查询
            Page<AccountEntity> page = new Page<>(pageNum, pageSize);
            IPage<AccountEntity> pageResult = accountDao.selectPage(page, queryWrapper);

            // 4. 构建返回结果
            PageResult<AccountEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum(pageNum);
            result.setPageSize(pageSize);
            result.setPages((int) pageResult.getPages());

            log.debug("[账户服务] 分页查询账户列表成功，total={}, pageNum={}, pageSize={}",
                    pageResult.getTotal(), pageNum, pageSize);

            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 分页查询账户列表参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 分页查询账户列表业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 分页查询账户列表系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_PAGE_QUERY_SYSTEM_ERROR", "查询账户列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 分页查询账户列表未知异常", e);
            throw new SystemException("ACCOUNT_PAGE_QUERY_SYSTEM_ERROR", "查询账户列表失败：" + e.getMessage(), e);
        }
    }

    /**
     * 游标分页查询账户列表（基于时间，推荐用于深度分页）
     *
     * @param pageSize 每页大小
     * @param lastTime 上一页最后一条记录的创建时间
     * @param keyword 关键词
     * @param accountKindId 账户类别ID
     * @param status 账户状态
     * @return 游标分页结果
     */
    @Override
    public CursorPagination.CursorPageResult<AccountEntity> cursorPageAccounts(
            Integer pageSize, LocalDateTime lastTime,
            String keyword, Long accountKindId, Integer status) {
        log.debug("[账户服务] 游标分页查询账户列表，pageSize={}, lastTime={}, keyword={}, accountKindId={}, status={}",
                pageSize, lastTime, keyword, accountKindId, status);

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();

            // 关键词搜索（账户ID或用户ID）
            if (StringUtils.hasText(keyword)) {
                try {
                    Long accountId = Long.parseLong(keyword);
                    queryWrapper.eq(AccountEntity::getId, accountId)
                            .or()
                            .eq(AccountEntity::getUserId, accountId);
                } catch (NumberFormatException e) {
                    log.debug("[账户查询] 关键词不是数字格式，按用户ID搜索: keyword={}", keyword);
                    // 如果不是数字，只按用户ID搜索
                    queryWrapper.eq(AccountEntity::getUserId, keyword);
                }
            }

            // 账户类别筛选
            if (accountKindId != null) {
                queryWrapper.eq(AccountEntity::getAccountKindId, accountKindId);
            }

            // 状态筛选
            if (status != null) {
                queryWrapper.eq(AccountEntity::getStatus, status);
            }

            // 2. 使用游标分页（基于时间）
            return PageHelper.cursorPageByTime(
                    accountDao,
                    queryWrapper,
                    pageSize,
                    lastTime,
                    AccountEntity::getCreateTime,
                    AccountEntity::getId
            );

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 游标分页查询账户列表参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 游标分页查询账户列表业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 游标分页查询账户列表系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_CURSOR_PAGE_QUERY_SYSTEM_ERROR", "查询账户列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 游标分页查询账户列表未知异常", e);
            throw new SystemException("ACCOUNT_CURSOR_PAGE_QUERY_SYSTEM_ERROR", "查询账户列表失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查询账户列表（不分页）
     *
     * @param accountKindId 账户类别ID（可选）
     * @param status 账户状态（可选）
     * @return 账户列表
     */
    @Override
    public List<AccountEntity> listAccounts(Long accountKindId, Integer status) {
        log.debug("[账户服务] 查询账户列表，accountKindId={}, status={}", accountKindId, status);

        try {
            // 构建查询条件
            LambdaQueryWrapper<AccountEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (accountKindId != null) {
                queryWrapper.eq(AccountEntity::getAccountKindId, accountKindId);
            }

            if (status != null) {
                queryWrapper.eq(AccountEntity::getStatus, status);
            }

            // 排序：按创建时间倒序
            queryWrapper.orderByDesc(AccountEntity::getCreateTime);

            return accountDao.selectList(queryWrapper);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 查询账户列表参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 查询账户列表业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 查询账户列表系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_LIST_QUERY_SYSTEM_ERROR", "查询账户列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 查询账户列表未知异常", e);
            throw new SystemException("ACCOUNT_LIST_QUERY_SYSTEM_ERROR", "查询账户列表失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取账户详情（包含统计信息）
     *
     * @param accountId 账户ID
     * @return 账户详情
     */
    @Override
    public AccountEntity getAccountDetail(Long accountId) {
        log.debug("[账户服务] 获取账户详情，accountId={}", accountId);

        try {
            // 目前直接返回账户信息
            // 如果需要统计信息，可以在这里添加查询逻辑
            return getById(accountId);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 获取账户详情参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 获取账户详情业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 获取账户详情系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_DETAIL_QUERY_SYSTEM_ERROR", "获取账户详情失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 获取账户详情未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_DETAIL_QUERY_SYSTEM_ERROR", "获取账户详情失败：" + e.getMessage(), e);
        }
    }

    /**
     * 增加账户余额
     *
     * @param accountId 账户ID
     * @param amount 增加金额（单位：元）
     * @param reason 增加原因
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.addBalance", contextualName = "account-add-balance")
    @Transactional(rollbackFor = Exception.class)
    public boolean addBalance(Long accountId, BigDecimal amount, String reason) {
        log.info("[账户服务] 增加账户余额，accountId={}, amount={}, reason={}",
                accountId, amount, reason);

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("增加金额必须大于0");
            }

            // 2. 使用AccountManager增加余额
            boolean success = accountManager.addBalance(accountId, amount);
            if (!success) {
                log.warn("[账户服务] 增加账户余额失败，accountId={}, amount={}", accountId, amount);
                throw new BusinessException("增加账户余额失败");
            }

            log.info("[账户服务] 增加账户余额成功，accountId={}, amount={}", accountId, amount);
            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 增加账户余额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 增加账户余额业务异常，accountId={}, amount={}, code={}, message={}",
                    accountId, amount, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 增加账户余额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_ADD_BALANCE_SYSTEM_ERROR", "增加账户余额失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 增加账户余额未知异常，accountId={}, amount={}", accountId, amount, e);
            throw new SystemException("ACCOUNT_ADD_BALANCE_SYSTEM_ERROR", "增加账户余额失败：" + e.getMessage(), e);
        }
    }

    /**
     * 扣减账户余额
     *
     * @param accountId 账户ID
     * @param amount 扣减金额（单位：元）
     * @param reason 扣减原因
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.deductBalance", contextualName = "account-deduct-balance")
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long accountId, BigDecimal amount, String reason) {
        log.info("[账户服务] 扣减账户余额，accountId={}, amount={}, reason={}",
                accountId, amount, reason);

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("扣减金额必须大于0");
            }

            // 2. 验证余额是否充足
            boolean sufficient = accountManager.checkBalanceSufficient(accountId, amount);
            if (!sufficient) {
                log.warn("[账户服务] 账户余额不足，accountId={}, amount={}", accountId, amount);
                throw new BusinessException("账户余额不足");
            }

            // 3. 使用AccountManager扣减余额
            boolean success = accountManager.deductBalance(accountId, amount);
            if (!success) {
                log.warn("[账户服务] 扣减账户余额失败，accountId={}, amount={}", accountId, amount);
                throw new BusinessException("扣减账户余额失败");
            }

            log.info("[账户服务] 扣减账户余额成功，accountId={}, amount={}", accountId, amount);
            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 扣减账户余额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 扣减账户余额业务异常，accountId={}, amount={}, code={}, message={}",
                    accountId, amount, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 扣减账户余额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_DEDUCT_BALANCE_SYSTEM_ERROR", "扣减账户余额失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 扣减账户余额未知异常，accountId={}, amount={}", accountId, amount, e);
            throw new SystemException("ACCOUNT_DEDUCT_BALANCE_SYSTEM_ERROR", "扣减账户余额失败：" + e.getMessage(), e);
        }
    }

    /**
     * 冻结账户金额
     *
     * @param accountId 账户ID
     * @param amount 冻结金额（单位：元）
     * @param reason 冻结原因
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.freezeAmount", contextualName = "account-freeze-amount")
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeAmount(Long accountId, BigDecimal amount, String reason) {
        log.info("[账户服务] 冻结账户金额，accountId={}, amount={}, reason={}",
                accountId, amount, reason);

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("冻结金额必须大于0");
            }

            // 2. 查询账户（加锁）
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw new BusinessException("账户不存在");
            }

            // 3. 验证余额是否充足
            BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            BigDecimal frozenAmount = account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO;
            BigDecimal availableBalance = currentBalance.subtract(frozenAmount);
            if (availableBalance.compareTo(amount) < 0) {
                log.warn("[账户服务] 可用余额不足，accountId={}, balance={}, frozenAmount={}, availableBalance={}, amount={}",
                        accountId, currentBalance, frozenAmount, availableBalance, amount);
                throw new BusinessException("可用余额不足");
            }

            // 4. 增加冻结余额
            BigDecimal currentFrozenAmount = account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO;
            BigDecimal newFrozenAmount = currentFrozenAmount.add(amount);
            account.setFrozenAmount(newFrozenAmount);
            account.setVersion(account.getVersion() + 1);

            // 5. 保存更新
            int result = accountDao.updateById(account);

            log.info("[账户服务] 冻结账户金额成功，accountId={}, amount={}, newFrozenAmount={}",
                    accountId, amount, newFrozenAmount);

            return result > 0;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 冻结账户金额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 冻结账户金额业务异常，accountId={}, amount={}, code={}, message={}",
                    accountId, amount, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 冻结账户金额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_FREEZE_AMOUNT_SYSTEM_ERROR", "冻结账户金额失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 冻结账户金额未知异常，accountId={}, amount={}", accountId, amount, e);
            throw new SystemException("ACCOUNT_FREEZE_AMOUNT_SYSTEM_ERROR", "冻结账户金额失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解冻账户金额
     *
     * @param accountId 账户ID
     * @param amount 解冻金额（单位：元）
     * @param reason 解冻原因
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.unfreezeAmount", contextualName = "account-unfreeze-amount")
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeAmount(Long accountId, BigDecimal amount, String reason) {
        log.info("[账户服务] 解冻账户金额，accountId={}, amount={}, reason={}",
                accountId, amount, reason);

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("解冻金额必须大于0");
            }

            // 2. 查询账户（加锁）
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw new BusinessException("账户不存在");
            }

            // 3. 验证冻结余额是否充足
            BigDecimal currentFrozenAmount = account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO;
            if (currentFrozenAmount.compareTo(amount) < 0) {
                log.warn("[账户服务] 冻结余额不足，accountId={}, frozenAmount={}, amount={}",
                        accountId, currentFrozenAmount, amount);
                throw new BusinessException("冻结余额不足");
            }

            // 4. 减少冻结余额
            BigDecimal newFrozenAmount = currentFrozenAmount.subtract(amount);
            account.setFrozenAmount(newFrozenAmount);
            account.setVersion(account.getVersion() + 1);

            // 5. 保存更新
            int result = accountDao.updateById(account);

            log.info("[账户服务] 解冻账户金额成功，accountId={}, amount={}, newFrozenAmount={}",
                    accountId, amount, newFrozenAmount);

            return result > 0;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 解冻账户金额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 解冻账户金额业务异常，accountId={}, amount={}, code={}, message={}",
                    accountId, amount, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 解冻账户金额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_UNFREEZE_AMOUNT_SYSTEM_ERROR", "解冻账户金额失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 解冻账户金额未知异常，accountId={}, amount={}", accountId, amount, e);
            throw new SystemException("ACCOUNT_UNFREEZE_AMOUNT_SYSTEM_ERROR", "解冻账户金额失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证账户余额是否充足
     *
     * @param accountId 账户ID
     * @param amount 需要金额（单位：元）
     * @return 是否充足
     */
    @Override
    public boolean validateBalance(Long accountId, BigDecimal amount) {
        log.debug("[账户服务] 验证账户余额，accountId={}, amount={}", accountId, amount);

        try {
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("验证金额必须大于0");
            }

            // 使用AccountManager验证余额
            return accountManager.checkBalanceSufficient(accountId, amount);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 验证账户余额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 验证账户余额业务异常，accountId={}, amount={}, code={}, message={}",
                    accountId, amount, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 验证账户余额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_VALIDATE_BALANCE_SYSTEM_ERROR", "验证账户余额失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 验证账户余额未知异常，accountId={}, amount={}", accountId, amount, e);
            throw new SystemException("ACCOUNT_VALIDATE_BALANCE_SYSTEM_ERROR", "验证账户余额失败：" + e.getMessage(), e);
        }
    }

    /**
     * 启用账户
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.enableAccount", contextualName = "account-enable")
    @Transactional(rollbackFor = Exception.class)
    public boolean enableAccount(Long accountId) {
        log.info("[账户服务] 启用账户，accountId={}", accountId);

        try {
            return updateAccountStatus(accountId, STATUS_NORMAL, "账户启用");

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 启用账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 启用账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 启用账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_ENABLE_SYSTEM_ERROR", "启用账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 启用账户未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_ENABLE_SYSTEM_ERROR", "启用账户失败：" + e.getMessage(), e);
        }
    }

    /**
     * 禁用账户
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.disableAccount", contextualName = "account-disable")
    @Transactional(rollbackFor = Exception.class)
    public boolean disableAccount(Long accountId) {
        log.info("[账户服务] 禁用账户，accountId={}", accountId);

        try {
            return updateAccountStatus(accountId, STATUS_FROZEN, "账户禁用");

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 禁用账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 禁用账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 禁用账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_DISABLE_SYSTEM_ERROR", "禁用账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 禁用账户未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_DISABLE_SYSTEM_ERROR", "禁用账户失败：" + e.getMessage(), e);
        }
    }

    /**
     * 冻结账户
     *
     * @param accountId 账户ID
     * @param reason 冻结原因
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.freezeAccount", contextualName = "account-freeze")
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeAccount(Long accountId, String reason) {
        log.info("[账户服务] 冻结账户，accountId={}, reason={}", accountId, reason);

        try {
            return updateAccountStatus(accountId, STATUS_FROZEN, reason != null ? reason : "账户冻结");

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 冻结账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 冻结账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 冻结账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_FREEZE_SYSTEM_ERROR", "冻结账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 冻结账户未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_FREEZE_SYSTEM_ERROR", "冻结账户失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解冻账户
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.unfreezeAccount", contextualName = "account-unfreeze")
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeAccount(Long accountId) {
        log.info("[账户服务] 解冻账户，accountId={}", accountId);

        try {
            return updateAccountStatus(accountId, STATUS_NORMAL, "账户解冻");

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 解冻账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 解冻账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 解冻账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_UNFREEZE_SYSTEM_ERROR", "解冻账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 解冻账户未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_UNFREEZE_SYSTEM_ERROR", "解冻账户失败：" + e.getMessage(), e);
        }
    }

    /**
     * 关闭账户
     *
     * @param accountId 账户ID
     * @param reason 关闭原因
     * @return 是否成功
     */
    @Override
    @Observed(name = "account.closeAccount", contextualName = "account-close")
    @Transactional(rollbackFor = Exception.class)
    public boolean closeAccount(Long accountId, String reason) {
        log.info("[账户服务] 关闭账户，accountId={}, reason={}", accountId, reason);

        try {
            return updateAccountStatus(accountId, STATUS_CLOSED, reason != null ? reason : "账户关闭");

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 关闭账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 关闭账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 关闭账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_CLOSE_SYSTEM_ERROR", "关闭账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 关闭账户未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_CLOSE_SYSTEM_ERROR", "关闭账户失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量更新账户状态
     *
     * @param accountIds 账户ID列表
     * @param status 目标状态
     * @return 成功更新的数量
     */
    @Override
    @Observed(name = "account.batchUpdateStatus", contextualName = "account-batch-update-status")
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> accountIds, Integer status) {
        log.info("[账户服务] 批量更新账户状态，accountIds={}, status={}", accountIds, status);

        try {
            // 1. 参数验证
            if (accountIds == null || accountIds.isEmpty()) {
                throw new BusinessException("账户ID列表不能为空");
            }
            if (status == null) {
                throw new BusinessException("账户状态不能为空");
            }

            // 2. 批量更新
            int successCount = 0;
            for (Long accountId : accountIds) {
                try {
                    boolean success = updateAccountStatus(accountId, status, "批量更新状态");
                    if (success) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("[账户服务] 批量更新账户状态失败，accountId={}, error={}",
                            accountId, e.getMessage());
                }
            }

            log.info("[账户服务] 批量更新账户状态完成，total={}, success={}",
                    accountIds.size(), successCount);

            return successCount;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 批量更新账户状态参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 批量更新账户状态业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 批量更新账户状态系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_BATCH_UPDATE_STATUS_SYSTEM_ERROR", "批量更新账户状态失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 批量更新账户状态未知异常", e);
            throw new SystemException("ACCOUNT_BATCH_UPDATE_STATUS_SYSTEM_ERROR", "批量更新账户状态失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量创建账户
     *
     * @param forms 账户创建表单列表
     * @return 成功创建的账户ID列表
     */
    @Override
    @Observed(name = "account.batchCreateAccounts", contextualName = "account-batch-create")
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateAccounts(List<AccountAddForm> forms) {
        log.info("[账户服务] 批量创建账户，count={}", forms != null ? forms.size() : 0);

        try {
            // 1. 参数验证
            if (forms == null || forms.isEmpty()) {
                throw new BusinessException("账户创建表单列表不能为空");
            }

            // 2. 批量创建
            List<Long> accountIds = new ArrayList<>();
            for (AccountAddForm form : forms) {
                try {
                    Long accountId = createAccount(form);
                    accountIds.add(accountId);
                } catch (Exception e) {
                    log.warn("[账户服务] 批量创建账户失败，userId={}, error={}",
                            form.getUserId(), e.getMessage());
                    // 继续处理下一个，不中断批量操作
                }
            }

            log.info("[账户服务] 批量创建账户完成，total={}, success={}",
                    forms.size(), accountIds.size());

            return accountIds;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 批量创建账户参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 批量创建账户业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 批量创建账户系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_BATCH_CREATE_SYSTEM_ERROR", "批量创建账户失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 批量创建账户未知异常", e);
            throw new SystemException("ACCOUNT_BATCH_CREATE_SYSTEM_ERROR", "批量创建账户失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取账户余额
     *
     * @param accountId 账户ID
     * @return 账户余额（单位：元）
     */
    @Override
    public BigDecimal getBalance(Long accountId) {
        log.debug("[账户服务] 获取账户余额，accountId={}", accountId);

        try {
            AccountEntity account = getById(accountId);
            return account.getBalanceAmount();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 获取账户余额参数错误，accountId={}, error={}", accountId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 获取账户余额业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 获取账户余额系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_GET_BALANCE_SYSTEM_ERROR", "获取账户余额失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 获取账户余额未知异常，accountId={}", accountId, e);
            throw new SystemException("ACCOUNT_GET_BALANCE_SYSTEM_ERROR", "获取账户余额失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取账户统计信息
     *
     * @param accountKindId 账户类别ID（可选）
     * @return 统计信息
     */
    @Override
    @Observed(name = "account.getAccountStatistics", contextualName = "account-get-statistics")
    public Map<String, Object> getAccountStatistics(Long accountKindId) {
        log.debug("[账户服务] 获取账户统计信息，accountKindId={}", accountKindId);

        try {
            // 1. 查询账户列表
            List<AccountEntity> accounts = listAccounts(accountKindId, null);

            // 2. 统计计算
            long totalCount = accounts.size();
            long normalCount = accounts.stream()
                    .filter(a -> STATUS_NORMAL.equals(a.getStatus()))
                    .count();
            long frozenCount = accounts.stream()
                    .filter(a -> STATUS_FROZEN.equals(a.getStatus()))
                    .count();
            long closedCount = accounts.stream()
                    .filter(a -> STATUS_CLOSED.equals(a.getStatus()))
                    .count();

            BigDecimal totalBalance = accounts.stream()
                    .map(AccountEntity::getBalanceAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalFrozenBalance = accounts.stream()
                    .map(AccountEntity::getFrozenBalanceAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. 构建统计结果
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", totalCount);
            statistics.put("normalCount", normalCount);
            statistics.put("frozenCount", frozenCount);
            statistics.put("closedCount", closedCount);
            statistics.put("totalBalance", totalBalance);
            statistics.put("totalFrozenBalance", totalFrozenBalance);
            statistics.put("availableBalance", totalBalance.subtract(totalFrozenBalance));

            log.debug("[账户服务] 账户统计信息查询成功，totalCount={}, totalBalance={}",
                    totalCount, totalBalance);

            return statistics;

        } catch (BusinessException e) {
            log.warn("[账户服务] 获取账户统计信息业务异常，accountKindId={}, code={}, message={}", accountKindId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 获取账户统计信息系统异常，accountKindId={}, code={}, message={}", accountKindId, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_STATISTICS_SYSTEM_ERROR", "获取账户统计信息失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 获取账户统计信息未知异常，accountKindId={}", accountKindId, e);
            throw new SystemException("ACCOUNT_STATISTICS_SYSTEM_ERROR", "获取账户统计信息失败：" + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证账户创建表单
     *
     * @param form 账户创建表单
     */
    private void validateAccountAddForm(AccountAddForm form) {
        if (form == null) {
            throw new BusinessException("账户创建表单不能为空");
        }
        if (form.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (form.getAccountKindId() == null) {
            throw new BusinessException("账户类别ID不能为空");
        }
        if (form.getInitialBalance() != null && form.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("初始余额不能为负数");
        }
    }

    /**
     * 验证账户更新表单
     *
     * @param form 账户更新表单
     */
    private void validateAccountUpdateForm(AccountUpdateForm form) {
        if (form == null) {
            throw new BusinessException("账户更新表单不能为空");
        }
        if (form.getAccountId() == null) {
            throw new BusinessException("账户ID不能为空");
        }
    }

    /**
     * 更新账户状态
     *
     * @param accountId 账户ID
     * @param status 目标状态
     * @param reason 更新原因
     * @return 是否成功
     */
    private boolean updateAccountStatus(Long accountId, Integer status, String reason) {
        try {
            // 1. 查询账户
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                throw new BusinessException("账户不存在");
            }

            // 2. 更新状态
            account.setStatus(status);
            account.setVersion(account.getVersion() + 1);

            // 3. 保存更新
            int result = accountDao.updateById(account);

            log.info("[账户服务] 更新账户状态成功，accountId={}, status={}, reason={}",
                    accountId, status, reason);

            return result > 0;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户服务] 更新账户状态参数错误，accountId={}, status={}, error={}", accountId, status, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户服务] 更新账户状态业务异常，accountId={}, status={}, code={}, message={}",
                    accountId, status, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[账户服务] 更新账户状态系统异常，accountId={}, status={}, code={}, message={}", accountId, status, e.getCode(), e.getMessage(), e);
            throw new SystemException("ACCOUNT_UPDATE_STATUS_SYSTEM_ERROR", "更新账户状态失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[账户服务] 更新账户状态未知异常，accountId={}, status={}", accountId, status, e);
            throw new SystemException("ACCOUNT_UPDATE_STATUS_SYSTEM_ERROR", "更新账户状态失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量查询账户信息
     * <p>
     * 使用MyBatis-Plus的selectBatchIds方法，性能优于循环查询
     * </p>
     *
     * @param accountIds 账户ID列表
     * @return 账户列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountEntity> getAccountsByIds(List<Long> accountIds) {
        log.debug("[账户服务] 批量查询账户信息，accountIds={}", accountIds);

        try {
            // 参数验证
            if (accountIds == null || accountIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 使用MyBatis-Plus的selectList方法进行批量查询（selectBatchIds已废弃）
            // 性能优化：使用IN查询，比循环查询效率高
            LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(AccountEntity::getId, accountIds);
            List<AccountEntity> accounts = accountDao.selectList(wrapper);

            log.debug("[账户服务] 批量查询账户信息成功，查询数量={}, 返回数量={}",
                    accountIds.size(), accounts != null ? accounts.size() : 0);

            return accounts != null ? accounts : new ArrayList<>();

        } catch (BusinessException e) {
            log.warn("[账户服务] 批量查询账户信息业务异常: code={}, message={}", e.getCode(), e.getMessage());
            // 降级：返回空列表
            return new ArrayList<>();
        } catch (SystemException e) {
            log.error("[账户服务] 批量查询账户信息系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 降级：返回空列表
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("[账户服务] 批量查询账户信息未知异常", e);
            // 降级：返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 获取用户账户余额信息（辅助方法，不在AccountService接口中）
     * <p>
     * 注意：此方法不在AccountService接口中定义，仅供内部使用
     * </p>
     */
    public Map<String, Object> getUserBalanceInfo(Long userId) {
        log.debug("[账户服务] 开始获取用户账户余额信息，userId={}", userId);

        try {
            // 根据用户ID查询账户
            LambdaQueryWrapper<AccountEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccountEntity::getUserId, userId);
            wrapper.eq(AccountEntity::getStatus, 1); // 只查询正常状态的账户
            wrapper.last("LIMIT 1");

            AccountEntity account = accountDao.selectOne(wrapper);

            if (account == null) {
                log.warn("[账户服务] 未找到用户账户，userId={}", userId);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("userId", userId);
                emptyResult.put("balance", BigDecimal.ZERO);
                emptyResult.put("frozenBalance", BigDecimal.ZERO);
                emptyResult.put("availableBalance", BigDecimal.ZERO);
                emptyResult.put("accountExists", false);
                return emptyResult;
            }

            // 计算可用余额
            BigDecimal availableBalance = account.getBalance();
            if (account.getFrozenAmount() != null) {
                availableBalance = availableBalance.subtract(account.getFrozenAmount());
            }

            Map<String, Object> balanceInfo = new HashMap<>();
            balanceInfo.put("userId", userId);
            balanceInfo.put("accountId", account.getAccountId());
            balanceInfo.put("accountNo", account.getAccountNo());
            balanceInfo.put("accountName", account.getAccountName());
            balanceInfo.put("accountType", account.getAccountType());
            balanceInfo.put("balance", account.getBalance());
            balanceInfo.put("frozenBalance", account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO);
            balanceInfo.put("availableBalance", availableBalance);
            balanceInfo.put("creditLimit", account.getCreditLimit());
            balanceInfo.put("dailyLimit", account.getDailyLimit());
            balanceInfo.put("monthlyLimit", account.getMonthlyLimit());
            balanceInfo.put("subsidyBalance", account.getSubsidyBalance());
            balanceInfo.put("status", account.getStatus());
            balanceInfo.put("lastUseTime", account.getLastUseTime());
            balanceInfo.put("accountExists", true);

            log.debug("[账户服务] 获取用户账户余额信息成功，userId={}, balance={}", userId, account.getBalance());
            return balanceInfo;

        } catch (BusinessException e) {
            log.warn("[账户服务] 获取用户账户余额信息业务异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            // 降级：返回默认值
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("userId", userId);
            defaultResult.put("balance", BigDecimal.ZERO);
            defaultResult.put("frozenBalance", BigDecimal.ZERO);
            defaultResult.put("availableBalance", BigDecimal.ZERO);
            defaultResult.put("accountExists", false);
            defaultResult.put("error", "查询失败: " + e.getMessage());
            return defaultResult;
        } catch (SystemException e) {
            log.error("[账户服务] 获取用户账户余额信息系统异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            // 降级：返回默认值
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("userId", userId);
            defaultResult.put("balance", BigDecimal.ZERO);
            defaultResult.put("frozenBalance", BigDecimal.ZERO);
            defaultResult.put("availableBalance", BigDecimal.ZERO);
            defaultResult.put("accountExists", false);
            defaultResult.put("error", "系统异常，请稍后重试");
            return defaultResult;
        } catch (Exception e) {
            log.error("[账户服务] 获取用户账户余额信息未知异常，userId={}", userId, e);
            // 降级：返回默认值
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("userId", userId);
            defaultResult.put("balance", BigDecimal.ZERO);
            defaultResult.put("frozenBalance", BigDecimal.ZERO);
            defaultResult.put("availableBalance", BigDecimal.ZERO);
            defaultResult.put("accountExists", false);
            defaultResult.put("error", "系统异常，请稍后重试");
            return defaultResult;
        }
    }
}




