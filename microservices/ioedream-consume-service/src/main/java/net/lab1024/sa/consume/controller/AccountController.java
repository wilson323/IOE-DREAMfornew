package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountAddForm;
import net.lab1024.sa.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.consume.service.AccountService;

/**
 * 账户管理控制器
 * <p>
 * 提供账户管理相关的REST API接口
 * 严格遵循CLAUDE.md规范：
 * - Controller层负责接收请求、参数验证、返回响应
 * - 使用@Resource注入Service
 * - 使用@Valid进行参数验证
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 账户CRUD操作
 * - 账户余额管理
 * - 账户状态管理
 * - 账户查询和统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/account")
@Tag(name = "账户管理", description = "消费账户管理相关接口")
public class AccountController {

    @Resource
    private AccountService accountService;

    /**
     * 创建账户
     * <p>
     * 为指定用户创建新的消费账户，支持设置初始余额和账户类型
     * </p>
     *
     * @param form 账户创建表单
     * @return 账户ID
     * @apiNote 示例请求：
     * <pre>
     * {
     *   "userId": 1001,
     *   "accountKindId": 1,
     *   "initialBalance": 100.00
     * }
     * </pre>
     */
    @PostMapping("/add")
    @Observed(name = "account.createAccount", contextualName = "account-create-account")
    @Operation(
        summary = "创建账户",
        description = "为指定用户创建新的消费账户，支持设置初始余额和账户类型。如果用户已有账户，将返回错误。",
        tags = {"账户管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "创建成功，返回账户ID",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Long.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "账户已存在或参数错误"
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Long> createAccount(
            @Parameter(description = "账户创建表单", required = true)
            @Valid @RequestBody AccountAddForm form) {
        log.info("[账户管理] 创建账户请求，userId={}, accountKindId={}",
                form.getUserId(), form.getAccountKindId());
        try {
            Long accountId = accountService.createAccount(form);
            return ResponseDTO.ok(accountId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 创建账户参数错误，userId={}, error={}", form.getUserId(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 创建账户业务异常，userId={}, code={}, message={}", form.getUserId(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 创建账户系统异常，userId={}, code={}, message={}", form.getUserId(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("CREATE_ACCOUNT_SYSTEM_ERROR", "创建账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 创建账户未知异常，userId={}", form.getUserId(), e);
            return ResponseDTO.error("CREATE_ACCOUNT_ERROR", "创建账户失败: " + e.getMessage());
        }
    }

    /**
     * 更新账户信息
     *
     * @param form 账户更新表单
     * @return 是否成功
     */
    @PutMapping("/update")
    @Observed(name = "account.updateAccount", contextualName = "account-update-account")
    @Operation(summary = "更新账户信息", description = "更新账户基本信息")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> updateAccount(@Valid @RequestBody AccountUpdateForm form) {
        log.info("[账户管理] 更新账户请求，accountId={}", form.getAccountId());
        try {
            boolean success = accountService.updateAccount(form);
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 更新账户参数错误，accountId={}, error={}", form.getAccountId(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 更新账户业务异常，accountId={}, code={}, message={}", form.getAccountId(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 更新账户系统异常，accountId={}, code={}, message={}", form.getAccountId(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_ACCOUNT_SYSTEM_ERROR", "更新账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 更新账户未知异常，accountId={}", form.getAccountId(), e);
            return ResponseDTO.error("UPDATE_ACCOUNT_ERROR", "更新账户失败: " + e.getMessage());
        }
    }

    /**
     * 查询账户详情
     * <p>
     * 根据账户ID查询账户的完整信息，包括余额、状态、账户类型等
     * </p>
     *
     * @param id 账户ID（必填）
     * @return 账户详情，包含账户的所有信息
     */
    @GetMapping("/{id}")
    @Observed(name = "account.getAccountById", contextualName = "account-get-account-by-id")
    @Operation(
        summary = "查询账户详情",
        description = "根据账户ID查询账户的完整信息，包括余额、状态、账户类型、创建时间等详细信息。",
        tags = {"账户管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AccountEntity.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "账户不存在"
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
    public ResponseDTO<AccountEntity> getAccountById(
            @Parameter(description = "账户ID", required = true, example = "2001")
            @PathVariable Long id) {
        log.info("[账户管理] 查询账户详情，accountId={}", id);
        try {
            AccountEntity account = accountService.getById(id);
            if (account == null) {
                return ResponseDTO.error("ACCOUNT_NOT_FOUND", "账户不存在");
            }
            return ResponseDTO.ok(account);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 查询账户详情参数错误，accountId={}, error={}", id, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 查询账户详情业务异常，accountId={}, code={}, message={}", id, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 查询账户详情系统异常，accountId={}, code={}, message={}", id, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_ACCOUNT_SYSTEM_ERROR", "查询账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 查询账户详情未知异常，accountId={}", id, e);
            return ResponseDTO.error("QUERY_ACCOUNT_ERROR", "查询账户失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询账户
     *
     * @param userId 用户ID
     * @return 账户详情
     */
    @GetMapping("/user/{userId}")
    @Observed(name = "account.getAccountByUserId", contextualName = "account-get-account-by-user-id")
    @Operation(summary = "根据用户ID查询账户", description = "根据用户ID查询账户信息")
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
    public ResponseDTO<AccountEntity> getAccountByUserId(@PathVariable Long userId) {
        log.info("[账户管理] 根据用户ID查询账户，userId={}", userId);
        try {
            AccountEntity account = accountService.getByUserId(userId);
            if (account == null) {
                return ResponseDTO.error("ACCOUNT_NOT_FOUND", "账户不存在");
            }
            return ResponseDTO.ok(account);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 根据用户ID查询账户参数错误，userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 根据用户ID查询账户业务异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 根据用户ID查询账户系统异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_ACCOUNT_SYSTEM_ERROR", "查询账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 根据用户ID查询账户未知异常，userId={}", userId, e);
            return ResponseDTO.error("QUERY_ACCOUNT_ERROR", "查询账户失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询账户列表
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param keyword 关键词（可选）
     * @param userId 用户ID（可选）
     * @param accountKindId 账户类别ID（可选）
     * @param status 账户状态（可选）
     * @return 账户列表
     */
    @GetMapping("/query")
    @Observed(name = "account.queryAccounts", contextualName = "account-query-accounts")
    @Operation(summary = "分页查询账户列表", description = "分页查询账户列表，支持多条件筛选")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<PageResult<AccountEntity>> queryAccounts(
            @Parameter(description = "页码（从1开始）")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词（账户名称、用户名称）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "用户ID（可选）")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "账户类别ID（可选）")
            @RequestParam(required = false) Long accountKindId,
            @Parameter(description = "账户状态（可选）")
            @RequestParam(required = false) Integer status) {
        log.info("[账户管理] 分页查询账户列表，pageNum={}, pageSize={}, keyword={}, userId={}, accountKindId={}, status={}",
                pageNum, pageSize, keyword, userId, accountKindId, status);
        try {
            // 使用AccountService的分页查询方法
            PageResult<AccountEntity> result = accountService.pageAccounts(
                    pageNum, pageSize, keyword, accountKindId, status);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 分页查询账户列表参数错误: pageNum={}, pageSize={}, error={}", pageNum, pageSize, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 分页查询账户列表业务异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 分页查询账户列表系统异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_ACCOUNTS_SYSTEM_ERROR", "查询账户列表失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 分页查询账户列表未知异常: pageNum={}, pageSize={}", pageNum, pageSize, e);
            return ResponseDTO.error("QUERY_ACCOUNTS_ERROR", "查询账户列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除账户
     *
     * @param id 账户ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @Observed(name = "account.deleteAccount", contextualName = "account-delete-account")
    @Operation(summary = "删除账户", description = "删除指定账户")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> deleteAccount(@PathVariable Long id) {
        log.info("[账户管理] 删除账户请求，accountId={}", id);
        if (id == null) {
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：账户ID不能为空");
        }
        try {
            boolean success = accountService.deleteAccount(id);
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 删除账户参数错误，accountId={}, error={}", id, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 删除账户业务异常，accountId={}, code={}, message={}", id, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 删除账户系统异常，accountId={}, code={}, message={}", id, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("DELETE_ACCOUNT_SYSTEM_ERROR", "删除账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 删除账户未知异常，accountId={}", id, e);
            return ResponseDTO.error("DELETE_ACCOUNT_ERROR", "删除账户失败: " + e.getMessage());
        }
    }

    /**
     * 增加账户余额
     *
     * @param accountId 账户ID
     * @param amount 增加金额（单位：元）
     * @param remark 备注
     * @return 是否成功
     */
    @PostMapping("/balance/add")
    @Observed(name = "account.addBalance", contextualName = "account-add-balance")
    @Operation(summary = "增加账户余额", description = "增加指定账户的余额")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> addBalance(
            @RequestParam Long accountId,
            @Parameter(description = "增加金额，单位：元") @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remark) {
        log.info("[账户管理] 增加账户余额，accountId={}, amount={}, remark={}", accountId, amount, remark);
        try {
            boolean success = accountService.addBalance(accountId, amount, remark != null ? remark : "管理员增加余额");
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 增加账户余额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 增加账户余额业务异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 增加账户余额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ADD_BALANCE_SYSTEM_ERROR", "增加账户余额失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 增加账户余额未知异常，accountId={}, amount={}", accountId, amount, e);
            return ResponseDTO.error("ADD_BALANCE_ERROR", "增加账户余额失败: " + e.getMessage());
        }
    }

    /**
     * 扣减账户余额
     *
     * @param accountId 账户ID
     * @param amount 扣减金额（单位：元）
     * @param remark 备注
     * @return 是否成功
     */
    @PostMapping("/balance/deduct")
    @Observed(name = "account.deductBalance", contextualName = "account-deduct-balance")
    @Operation(summary = "扣减账户余额", description = "扣减指定账户的余额")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> deductBalance(
            @RequestParam Long accountId,
            @Parameter(description = "扣减金额，单位：元") @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remark) {
        log.info("[账户管理] 扣减账户余额，accountId={}, amount={}, remark={}", accountId, amount, remark);
        try {
            boolean success = accountService.deductBalance(accountId, amount, remark != null ? remark : "管理员扣减余额");
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 扣减账户余额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 扣减账户余额业务异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 扣减账户余额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("DEDUCT_BALANCE_SYSTEM_ERROR", "扣减账户余额失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 扣减账户余额未知异常，accountId={}, amount={}", accountId, amount, e);
            return ResponseDTO.error("DEDUCT_BALANCE_ERROR", "扣减账户余额失败: " + e.getMessage());
        }
    }

    /**
     * 冻结账户余额
     * <p>
     * 严格遵循CLAUDE.md规范：更新操作使用PUT方法
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 冻结金额（单位：元）
     * @param remark 备注
     * @return 是否成功
     */
    @PutMapping("/balance/freeze")
    @Observed(name = "account.freezeBalance", contextualName = "account-freeze-balance")
    @Operation(summary = "冻结账户余额", description = "冻结指定账户的余额")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> freezeBalance(
            @RequestParam Long accountId,
            @Parameter(description = "冻结金额，单位：元") @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remark) {
        log.info("[账户管理] 冻结账户余额，accountId={}, amount={}, remark={}", accountId, amount, remark);
        try {
            boolean success = accountService.freezeAmount(accountId, amount, remark != null ? remark : "管理员冻结余额");
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 冻结账户余额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 冻结账户余额业务异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 冻结账户余额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("FREEZE_BALANCE_SYSTEM_ERROR", "冻结账户余额失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 冻结账户余额未知异常，accountId={}, amount={}", accountId, amount, e);
            return ResponseDTO.error("FREEZE_BALANCE_ERROR", "冻结账户余额失败: " + e.getMessage());
        }
    }

    /**
     * 解冻账户余额
     * <p>
     * 严格遵循CLAUDE.md规范：更新操作使用PUT方法
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 解冻金额（单位：元）
     * @param remark 备注
     * @return 是否成功
     */
    @PutMapping("/balance/unfreeze")
    @Observed(name = "account.unfreezeBalance", contextualName = "account-unfreeze-balance")
    @Operation(summary = "解冻账户余额", description = "解冻指定账户的余额")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> unfreezeBalance(
            @RequestParam Long accountId,
            @Parameter(description = "解冻金额，单位：元") @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remark) {
        log.info("[账户管理] 解冻账户余额，accountId={}, amount={}, remark={}", accountId, amount, remark);
        try {
            boolean success = accountService.unfreezeAmount(accountId, amount, remark != null ? remark : "管理员解冻余额");
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 解冻账户余额参数错误，accountId={}, amount={}, error={}", accountId, amount, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 解冻账户余额业务异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 解冻账户余额系统异常，accountId={}, amount={}, code={}, message={}", accountId, amount, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("UNFREEZE_BALANCE_SYSTEM_ERROR", "解冻账户余额失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 解冻账户余额未知异常，accountId={}, amount={}", accountId, amount, e);
            return ResponseDTO.error("UNFREEZE_BALANCE_ERROR", "解冻账户余额失败: " + e.getMessage());
        }
    }

    /**
     * 启用账户
     * <p>
     * 严格遵循CLAUDE.md规范：更新操作使用PUT方法
     * </p>
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @PutMapping("/status/enable")
    @Observed(name = "account.enableAccount", contextualName = "account-enable-account")
    @Operation(summary = "启用账户", description = "启用指定账户")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> enableAccount(@RequestParam Long accountId) {
        log.info("[账户管理] 启用账户，accountId={}", accountId);
        try {
            boolean success = accountService.enableAccount(accountId);
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 启用账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 启用账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 启用账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ENABLE_ACCOUNT_SYSTEM_ERROR", "启用账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 启用账户未知异常，accountId={}", accountId, e);
            return ResponseDTO.error("ENABLE_ACCOUNT_ERROR", "启用账户失败: " + e.getMessage());
        }
    }

    /**
     * 禁用账户
     * <p>
     * 严格遵循CLAUDE.md规范：更新操作使用PUT方法
     * </p>
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @PutMapping("/status/disable")
    @Observed(name = "account.disableAccount", contextualName = "account-disable-account")
    @Operation(summary = "禁用账户", description = "禁用指定账户")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> disableAccount(@RequestParam Long accountId) {
        log.info("[账户管理] 禁用账户，accountId={}", accountId);
        try {
            boolean success = accountService.disableAccount(accountId);
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 禁用账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 禁用账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 禁用账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("DISABLE_ACCOUNT_SYSTEM_ERROR", "禁用账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 禁用账户未知异常，accountId={}", accountId, e);
            return ResponseDTO.error("DISABLE_ACCOUNT_ERROR", "禁用账户失败: " + e.getMessage());
        }
    }

    /**
     * 冻结账户状态
     * <p>
     * 严格遵循CLAUDE.md规范：更新操作使用PUT方法
     * </p>
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @PutMapping("/status/freeze")
    @Observed(name = "account.freezeAccountStatus", contextualName = "account-freeze-account-status")
    @Operation(summary = "冻结账户状态", description = "冻结指定账户状态")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> freezeAccountStatus(
            @RequestParam Long accountId,
            @RequestParam(required = false) String reason) {
        log.info("[账户管理] 冻结账户状态，accountId={}, reason={}", accountId, reason);
        try {
            boolean success = accountService.freezeAccount(accountId, reason);
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 冻结账户状态参数错误，accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 冻结账户状态业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 冻结账户状态系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("FREEZE_ACCOUNT_STATUS_SYSTEM_ERROR", "冻结账户状态失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 冻结账户状态未知异常，accountId={}", accountId, e);
            return ResponseDTO.error("FREEZE_ACCOUNT_STATUS_ERROR", "冻结账户状态失败: " + e.getMessage());
        }
    }

    /**
     * 解冻账户状态
     * <p>
     * 严格遵循CLAUDE.md规范：更新操作使用PUT方法
     * </p>
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @PutMapping("/status/unfreeze")
    @Observed(name = "account.unfreezeAccountStatus", contextualName = "account-unfreeze-account-status")
    @Operation(summary = "解冻账户状态", description = "解冻指定账户状态")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> unfreezeAccountStatus(@RequestParam Long accountId) {
        log.info("[账户管理] 解冻账户状态，accountId={}", accountId);
        try {
            boolean success = accountService.unfreezeAccount(accountId);
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 解冻账户状态参数错误，accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 解冻账户状态业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 解冻账户状态系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("UNFREEZE_ACCOUNT_STATUS_SYSTEM_ERROR", "解冻账户状态失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 解冻账户状态未知异常，accountId={}", accountId, e);
            return ResponseDTO.error("UNFREEZE_ACCOUNT_STATUS_ERROR", "解冻账户状态失败: " + e.getMessage());
        }
    }

    /**
     * 关闭账户
     * <p>
     * 严格遵循CLAUDE.md规范：更新操作使用PUT方法
     * </p>
     *
     * @param accountId 账户ID
     * @return 是否成功
     */
    @PutMapping("/status/close")
    @Observed(name = "account.closeAccount", contextualName = "account-close-account")
    @Operation(summary = "关闭账户", description = "关闭指定账户")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<Boolean> closeAccount(
            @RequestParam Long accountId,
            @RequestParam(required = false) String reason) {
        log.info("[账户管理] 关闭账户，accountId={}, reason={}", accountId, reason);
        try {
            boolean success = accountService.closeAccount(accountId, reason);
            return ResponseDTO.ok(success);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 关闭账户参数错误，accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 关闭账户业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 关闭账户系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("CLOSE_ACCOUNT_SYSTEM_ERROR", "关闭账户失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 关闭账户未知异常，accountId={}", accountId, e);
            return ResponseDTO.error("CLOSE_ACCOUNT_ERROR", "关闭账户失败: " + e.getMessage());
        }
    }

    /**
     * 获取账户余额
     *
     * @param accountId 账户ID
     * @return 账户余额（单位：元）
     */
    @GetMapping("/balance/{accountId}")
    @Observed(name = "account.getAccountBalance", contextualName = "account-get-account-balance")
    @Operation(summary = "获取账户余额", description = "获取指定账户的余额")
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
    public ResponseDTO<BigDecimal> getAccountBalance(@PathVariable Long accountId) {
        log.info("[账户管理] 获取账户余额，accountId={}", accountId);
        try {
            BigDecimal balance = accountService.getBalance(accountId);
            return ResponseDTO.ok(balance);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 获取账户余额参数错误，accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 获取账户余额业务异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 获取账户余额系统异常，accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_BALANCE_SYSTEM_ERROR", "获取账户余额失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 获取账户余额未知异常，accountId={}", accountId, e);
            return ResponseDTO.error("GET_BALANCE_ERROR", "获取账户余额失败: " + e.getMessage());
        }
    }

    /**
     * 通过用户ID获取账户余额
     * <p>
     * 用于设备协议推送余额查询
     * </p>
     *
     * @param userId 用户ID
     * @return 账户余额（单位：元）
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/consume/account/balance/user/1001
     * </pre>
     */
    @GetMapping("/balance/user/{userId}")
    @Observed(name = "account.getAccountBalanceByUserId", contextualName = "account-get-account-balance-by-user-id")
    @Operation(
        summary = "通过用户ID获取账户余额",
        description = "用于设备协议推送余额查询，根据用户ID查询账户余额",
        tags = {"账户管理", "设备协议"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BigDecimal.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "参数错误或账户不存在"
    )
    public ResponseDTO<BigDecimal> getAccountBalanceByUserId(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId) {
        log.info("[账户管理] 通过用户ID获取账户余额，userId={}", userId);

        try {
            // 通过用户ID查询账户
            AccountEntity account = accountService.getByUserId(userId);
            if (account == null) {
                log.warn("[账户管理] 账户不存在，userId={}", userId);
                return ResponseDTO.error("ACCOUNT_NOT_FOUND", "账户不存在");
            }

            // 获取余额（单位：元）
            BigDecimal balance = account.getBalanceAmount();
            log.info("[账户管理] 通过用户ID获取账户余额成功，userId={}, balance={}", userId, balance);
            return ResponseDTO.ok(balance);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 通过用户ID获取账户余额参数错误，userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 通过用户ID获取账户余额业务异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 通过用户ID获取账户余额系统异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_BALANCE_BY_USER_ID_SYSTEM_ERROR", "获取账户余额失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 通过用户ID获取账户余额未知异常，userId={}", userId, e);
            return ResponseDTO.error("GET_BALANCE_BY_USER_ID_ERROR", "获取账户余额失败: " + e.getMessage());
        }
    }

    /**
     * 批量查询账户信息
     * <p>
     * 使用POST方法接收请求体，避免URL过长问题
     * 路径使用/search更符合RESTful语义（批量查询操作）
     * </p>
     *
     * @param accountIds 账户ID列表
     * @return 账户列表
     */
    @PostMapping("/batch/search")
    @Observed(name = "account.getAccountsByIds", contextualName = "account-get-accounts-by-ids")
    @Operation(summary = "批量查询账户信息", description = "根据账户ID列表批量查询账户信息。使用POST方法接收请求体，避免URL过长问题。注意：虽然这是查询操作，但为支持大批量ID列表，使用POST避免URL长度限制。")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<List<AccountEntity>> getAccountsByIds(@RequestBody List<Long> accountIds) {
        log.info("[账户管理] 批量查询账户信息，accountIds={}", accountIds);
        try {
            // 使用AccountService的批量查询方法（使用IN查询，性能优于循环查询）
            List<AccountEntity> accounts = accountService.getAccountsByIds(accountIds);
            return ResponseDTO.ok(accounts);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[账户管理] 批量查询账户信息参数错误，accountIds={}, error={}", accountIds, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[账户管理] 批量查询账户信息业务异常，accountIds={}, code={}, message={}", accountIds, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[账户管理] 批量查询账户信息系统异常，accountIds={}, code={}, message={}", accountIds, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("BATCH_QUERY_ACCOUNTS_SYSTEM_ERROR", "批量查询账户信息失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[账户管理] 批量查询账户信息未知异常，accountIds={}", accountIds, e);
            return ResponseDTO.error("BATCH_QUERY_ACCOUNTS_ERROR", "批量查询账户信息失败: " + e.getMessage());
        }
    }
}




