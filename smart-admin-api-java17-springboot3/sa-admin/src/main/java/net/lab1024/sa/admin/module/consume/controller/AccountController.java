package net.lab1024.sa.admin.module.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.admin.module.consume.domain.form.AccountCreateForm;
import net.lab1024.sa.admin.module.consume.domain.form.AccountRechargeForm;
import net.lab1024.sa.admin.module.consume.domain.form.AccountUpdateForm;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountDetailVO;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountVO;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.validate.ValidateGroup;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 账户管理控制器
 *
 * @author OpenSpec Task 2.10 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@RestController
@RequestMapping("/api/consume/account")
@Tag(name = "账户管理", description = "消费账户相关接口")
@Validated
@RequiredArgsConstructor
public class AccountController extends SupportBaseController {

    private final AccountService accountService;

    @PostMapping("/create")
    @Operation(summary = "创建账户", description = "为用户创建消费账户")
    @SaCheckLogin
    @SaCheckPermission("consume:account:create")
    public ResponseDTO<String> createAccount(@Valid @RequestBody AccountCreateForm createForm) {
        try {
            return ResponseDTO.ok(accountService.createAccount(createForm));
        } catch (Exception e) {
            return ResponseDTO.error("创建账户失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "账户列表", description = "分页查询账户列表")
    @SaCheckLogin
    @SaCheckPermission("consume:account:list")
    public ResponseDTO<PageResult<AccountVO>> getAccountList(@Valid PageParam pageParam,
                                                           @RequestParam(required = false) String accountName,
                                                           @RequestParam(required = false) Integer status,
                                                           @RequestParam(required = false) String accountType) {
        try {
            return ResponseDTO.ok(accountService.getAccountList(pageParam, accountName, status, accountType));
        } catch (Exception e) {
            return ResponseDTO.error("查询账户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{accountId}")
    @Operation(summary = "账户详情", description = "获取账户详细信息")
    @SaCheckLogin
    @SaCheckPermission("consume:account:detail")
    public ResponseDTO<AccountDetailVO> getAccountDetail(@PathVariable @NotNull Long accountId) {
        try {
            return ResponseDTO.ok(accountService.getAccountDetail(accountId));
        } catch (Exception e) {
            return ResponseDTO.error("获取账户详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新账户", description = "更新账户基本信息")
    @SaCheckLogin
    @SaCheckPermission("consume:account:update")
    public ResponseDTO<String> updateAccount(@Valid @RequestBody AccountUpdateForm updateForm) {
        try {
            return ResponseDTO.ok(accountService.updateAccount(updateForm));
        } catch (Exception e) {
            return ResponseDTO.error("更新账户失败: " + e.getMessage());
        }
    }

    @PostMapping("/recharge")
    @Operation(summary = "账户充值", description = "为账户充值")
    @SaCheckLogin
    @SaCheckPermission("consume:account:recharge")
    public ResponseDTO<String> rechargeAccount(@Valid @RequestBody AccountRechargeForm rechargeForm) {
        try {
            return ResponseDTO.ok(accountService.rechargeAccount(rechargeForm));
        } catch (Exception e) {
            return ResponseDTO.error("账户充值失败: " + e.getMessage());
        }
    }

    @GetMapping("/balance/{accountId}")
    @Operation(summary = "查询余额", description = "查询账户当前余额")
    @SaCheckLogin
    @SaCheckPermission("consume:account:balance")
    public ResponseDTO<BigDecimal> getAccountBalance(@PathVariable @NotNull Long accountId) {
        try {
            return ResponseDTO.ok(accountService.getAccountBalance(accountId));
        } catch (Exception e) {
            return ResponseDTO.error("查询账户余额失败: " + e.getMessage());
        }
    }

    @PostMapping("/freeze/{accountId}")
    @Operation(summary = "冻结账户", description = "冻结指定账户")
    @SaCheckLogin
    @SaCheckPermission("consume:account:freeze")
    public ResponseDTO<String> freezeAccount(@PathVariable @NotNull Long accountId,
                                           @RequestParam @NotNull String reason) {
        try {
            return ResponseDTO.ok(accountService.freezeAccount(accountId, reason));
        } catch (Exception e) {
            return ResponseDTO.error("冻结账户失败: " + e.getMessage());
        }
    }

    @PostMapping("/unfreeze/{accountId}")
    @Operation(summary = "解冻账户", description = "解冻指定账户")
    @SaCheckLogin
    @SaCheckPermission("consume:account:unfreeze")
    public ResponseDTO<String> unfreezeAccount(@PathVariable @NotNull Long accountId,
                                             @RequestParam @NotNull String reason) {
        try {
            return ResponseDTO.ok(accountService.unfreezeAccount(accountId, reason));
        } catch (Exception e) {
            return ResponseDTO.error("解冻账户失败: " + e.getMessage());
        }
    }

    @PostMapping("/close/{accountId}")
    @Operation(summary = "关闭账户", description = "关闭指定账户")
    @SaCheckLogin
    @SaCheckPermission("consume:account:close")
    public ResponseDTO<String> closeAccount(@PathVariable @NotNull Long accountId,
                                          @RequestParam @NotNull String reason) {
        try {
            return ResponseDTO.ok(accountService.closeAccount(accountId, reason));
        } catch (Exception e) {
            return ResponseDTO.error("关闭账户失败: " + e.getMessage());
        }
    }

    @GetMapping("/transactions/{accountId}")
    @Operation(summary = "交易记录", description = "查询账户交易记录")
    @SaCheckLogin
    @SaCheckPermission("consume:account:transactions")
    public ResponseDTO<PageResult<Map<String, Object>>> getAccountTransactions(
            @PathVariable @NotNull Long accountId,
            @Valid PageParam pageParam,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String transactionType) {
        try {
            return ResponseDTO.ok(accountService.getAccountTransactions(accountId, pageParam, startTime, endTime, transactionType));
        } catch (Exception e) {
            return ResponseDTO.error("查询交易记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "账户统计", description = "获取账户统计数据")
    @SaCheckLogin
    @SaCheckPermission("consume:account:statistics")
    public ResponseDTO<Map<String, Object>> getAccountStatistics(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        try {
            return ResponseDTO.ok(accountService.getAccountStatistics(startTime, endTime));
        } catch (Exception e) {
            return ResponseDTO.error("获取账户统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    @Operation(summary = "导出账户", description = "导出账户数据")
    @SaCheckLogin
    @SaCheckPermission("consume:account:export")
    public ResponseDTO<String> exportAccounts(
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        try {
            return ResponseDTO.ok(accountService.exportAccounts(accountName, status, accountType, startTime, endTime));
        } catch (Exception e) {
            return ResponseDTO.error("导出账户数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/types")
    @Operation(summary = "账户类型", description = "获取可用的账户类型")
    @SaCheckLogin
    @SaCheckPermission("consume:account:types")
    public ResponseDTO<List<Map<String, Object>>> getAccountTypes() {
        try {
            return ResponseDTO.ok(accountService.getAccountTypes());
        } catch (Exception e) {
            return ResponseDTO.error("获取账户类型失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch/status")
    @Operation(summary = "批量修改状态", description = "批量修改账户状态")
    @SaCheckLogin
    @SaCheckPermission("consume:account:batch:status")
    public ResponseDTO<String> batchUpdateStatus(@RequestParam @NotNull List<Long> accountIds,
                                                @RequestParam @NotNull Integer status,
                                                @RequestParam(required = false) String reason) {
        try {
            return ResponseDTO.ok(accountService.batchUpdateStatus(accountIds, status, reason));
        } catch (Exception e) {
            return ResponseDTO.error("批量修改状态失败: " + e.getMessage());
        }
    }
}