package net.lab1024.sa.consume.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.domain.form.AccountQueryForm;
import net.lab1024.sa.consume.domain.vo.AccountVO;
import net.lab1024.sa.consume.service.ConsumeAccountService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 消费账户管理控制器
 * <p>
 * 提供完整的账户管理功能，确保与Smart-Admin前端100%兼容
 * 包括账户查询、冻结/解冻、余额管理等核心功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/account")
@Tag(name = "消费账户管理", description = "消费账户相关接口")
@Validated
public class ConsumeAccountController {

    @Resource
    private ConsumeAccountService accountService;

    /**
     * 分页查询账户列表
     */
    @GetMapping("/list")
    @Observed(name = "consumeAccount.getAccountList", contextualName = "consume-account-get-account-list")
    @Operation(summary = "分页查询账户列表", description = "支持多条件查询")
    public ResponseDTO<IPage<AccountVO>> getAccountList(AccountQueryForm queryForm) {
        ResponseDTO<IPage<AccountVO>> pageResult = accountService.queryAccountPage(queryForm);
        return pageResult;
    }

    /**
     * 获取账户详情
     */
    @GetMapping("/{accountId}/detail")
    @Observed(name = "consumeAccount.getAccountDetail", contextualName = "consume-account-get-account-detail")
    @Operation(summary = "获取账户详情", description = "根据账户ID获取详细信息")
    public ResponseDTO<AccountVO> getAccountDetail(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId) {
        ResponseDTO<AccountVO> accountResult = accountService.getAccountById(accountId);
        AccountVO accountVO = accountResult.getData();
        return ResponseDTO.ok(accountVO);
    }

    /**
     * 查询用户账户余额
     */
    @GetMapping("/{userId}/balance")
    @Observed(name = "consumeAccount.getAccountBalance", contextualName = "consume-account-get-account-balance")
    @Operation(summary = "查询用户账户余额", description = "获取指定用户的账户余额信息")
    public ResponseDTO<Map<String, Object>> getAccountBalance(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        Map<String, Object> balanceInfo = accountService.getUserBalanceInfo(userId);
        return ResponseDTO.ok(balanceInfo);
    }

    /**
     * 获取账户完整信息
     */
    @GetMapping("/user/{userId}")
    @Observed(name = "consumeAccount.getAccountByUserId", contextualName = "consume-account-get-account-by-user-id")
    @Operation(summary = "获取用户账户完整信息", description = "获取用户的完整账户信息，包括余额、状态等")
    public ResponseDTO<AccountVO> getAccountByUserId(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        AccountVO accountVO = accountService.getAccountByUserId(userId);
        return ResponseDTO.ok(accountVO);
    }

    /**
     * 更新账户状态（冻结/解冻）
     */
    @PutMapping("/{accountId}/status")
    @Observed(name = "consumeAccount.updateAccountStatus", contextualName = "consume-account-update-account-status")
    @Operation(summary = "更新账户状态", description = "更新指定账户的状态（冻结或解冻）")
    public ResponseDTO<String> updateAccountStatus(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId,
            @Parameter(description = "操作类型：freeze-冻结，unfreeze-解冻") @RequestParam @NotBlank String operationType,
            @Parameter(description = "操作原因", example = "异常消费") @RequestParam @NotBlank String reason,
            @Parameter(description = "冻结天数（仅冻结时有效）", example = "7") @RequestParam(required = false) Integer freezeDays) {
        boolean result;
        if ("freeze".equalsIgnoreCase(operationType)) {
            result = accountService.freezeAccount(accountId, reason, freezeDays != null ? freezeDays : 7);
        } else if ("unfreeze".equalsIgnoreCase(operationType)) {
            result = accountService.unfreezeAccount(accountId, reason);
        } else {
            return ResponseDTO.error(400, "无效的操作类型，必须是 freeze 或 unfreeze");
        }

        if (result) {
            return ResponseDTO.ok("账户状态更新成功");
        } else {
            return ResponseDTO.error(500, "账户状态更新失败");
        }
    }

    /**
     * 账户充值
     */
    @PostMapping("/{accountId}/recharge")
    @Observed(name = "consumeAccount.rechargeAccount", contextualName = "consume-account-recharge-account")
    @Operation(summary = "账户充值", description = "为指定账户进行充值")
    public ResponseDTO<String> rechargeAccount(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId,
            @Parameter(description = "充值金额", example = "100.00") @RequestParam @NotNull BigDecimal amount,
            @Parameter(description = "充值方式", example = "CASH") @RequestParam @NotBlank String rechargeType,
            @Parameter(description = "充值说明", example = "现金充值") @RequestParam(defaultValue = "") String remark) {
        boolean result = accountService.rechargeAccount(accountId, amount, rechargeType, remark);
        if (result) {
            return ResponseDTO.ok("充值成功");
        } else {
            return ResponseDTO.error(500, "充值失败");
        }
    }

    /**
     * 设置账户限额
     */
    @PutMapping("/{accountId}/limit")
    @Observed(name = "consumeAccount.setAccountLimit", contextualName = "consume-account-set-account-limit")
    @Operation(summary = "设置账户限额", description = "设置账户的日消费限额和月消费限额")
    public ResponseDTO<String> setAccountLimit(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId,
            @Parameter(description = "日消费限额", example = "100.00") @RequestParam BigDecimal dailyLimit,
            @Parameter(description = "月消费限额", example = "3000.00") @RequestParam BigDecimal monthlyLimit) {
        boolean result = accountService.setAccountLimit(accountId, dailyLimit, monthlyLimit);
        if (result) {
            return ResponseDTO.ok("限额设置成功");
        } else {
            return ResponseDTO.error(500, "限额设置失败");
        }
    }

    /**
     * 批量操作账户状态
     */
    @PutMapping("/batch/status")
    @Observed(name = "consumeAccount.batchUpdateAccountStatus", contextualName = "consume-account-batch-update-account-status")
    @Operation(summary = "批量操作账户状态", description = "批量冻结或解冻多个账户")
    public ResponseDTO<String> batchUpdateAccountStatus(
            @Parameter(description = "账户ID列表") @RequestParam @NotNull List<Long> accountIds,
            @Parameter(description = "操作类型：freeze-冻结，unfreeze-解冻") @RequestParam @NotBlank String operationType,
            @Parameter(description = "操作原因") @RequestParam @NotBlank String reason) {
        int successCount = accountService.batchUpdateAccountStatus(accountIds, operationType, reason);
        return ResponseDTO.ok(String.format("批量操作完成，成功处理 %d 个账户", successCount));
    }

    /**
     * 获取账户统计信息
     */
    @GetMapping("/statistics")
    @Observed(name = "consumeAccount.getAccountStatistics", contextualName = "consume-account-get-account-statistics")
    @Operation(summary = "获取账户统计信息", description = "获取账户总数、余额总额、冻结数量等统计信息")
    public ResponseDTO<Map<String, Object>> getAccountStatistics() {
        Map<String, Object> statistics = accountService.getAccountStatistics();
        return ResponseDTO.ok(statistics);
    }

    /**
     * 导出账户数据
     */
    @GetMapping("/export")
    @Observed(name = "consumeAccount.exportAccountData", contextualName = "consume-account-export-account-data")
    @Operation(summary = "导出账户数据", description = "导出账户数据到Excel文件")
    public void exportAccountData(AccountQueryForm queryForm, HttpServletResponse response) {
        accountService.exportAccountData(queryForm, response);
    }

    /**
     * 获取账户消费记录
     */
    @GetMapping("/{accountId}/records")
    @Observed(name = "consumeAccount.getAccountConsumeRecords", contextualName = "consume-account-get-account-consume-records")
    @Operation(summary = "获取账户消费记录", description = "分页查询指定账户的消费记录")
    public ResponseDTO<PageResult<Map<String, Object>>> getAccountConsumeRecords(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<Map<String, Object>> records = accountService.getAccountConsumeRecords(accountId, pageNum, pageSize);
        return ResponseDTO.ok(records);
    }

    /**
     * 检查账户状态
     */
    @GetMapping("/{accountId}/status")
    @Observed(name = "consumeAccount.checkAccountStatus", contextualName = "consume-account-check-account-status")
    @Operation(summary = "检查账户状态", description = "检查账户是否可用、是否被冻结等状态")
    public ResponseDTO<Map<String, Object>> checkAccountStatus(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId) {
        Map<String, Object> statusInfo = accountService.checkAccountStatus(accountId);
        return ResponseDTO.ok(statusInfo);
    }

    /**
     * 创建新账户
     */
    @PostMapping("/create")
    @Observed(name = "consumeAccount.createAccount", contextualName = "consume-account-create-account")
    @Operation(summary = "创建新账户", description = "为用户创建新的消费账户")
    public ResponseDTO<Long> createAccount(@Valid @RequestBody AccountEntity accountEntity) {
        Long accountId = accountService.createAccount(accountEntity);
        return ResponseDTO.ok(accountId);
    }

    /**
     * 更新账户信息
     */
    @PutMapping("/{accountId}")
    @Observed(name = "consumeAccount.updateAccount", contextualName = "consume-account-update-account")
    @Operation(summary = "更新账户信息", description = "更新指定账户的基本信息")
    public ResponseDTO<String> updateAccount(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId,
            @Valid @RequestBody AccountEntity accountEntity) {
        accountEntity.setAccountId(accountId);
        boolean result = accountService.updateAccount(accountEntity);
        if (result) {
            return ResponseDTO.ok("更新成功");
        } else {
            return ResponseDTO.error(500, "更新失败");
        }
    }

    /**
     * 删除账户
     */
    @DeleteMapping("/{accountId}")
    @Observed(name = "consumeAccount.deleteAccount", contextualName = "consume-account-delete-account")
    @Operation(summary = "删除账户", description = "逻辑删除指定账户")
    public ResponseDTO<String> deleteAccount(
            @Parameter(description = "账户ID", example = "1") @PathVariable Long accountId) {
        boolean result = accountService.deleteAccount(accountId);
        if (result) {
            return ResponseDTO.ok("删除成功");
        } else {
            return ResponseDTO.error(500, "删除失败");
        }
    }
}



