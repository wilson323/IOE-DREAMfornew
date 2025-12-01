package net.lab1024.sa.admin.module.consume.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.domain.dto.RechargeQueryDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.RechargeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.RechargeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.entity.RechargeRecordEntity;
import net.lab1024.sa.admin.module.consume.service.RechargeService;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 充值管理控制器
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@RestController
@RequestMapping("/api/consume/recharge")
@Tag(name = "充值管理", description = "充值相关接口")
public class RechargeController extends SupportBaseController {

    @Resource
    private RechargeService rechargeService;

    @Operation(summary = "充值", description = "账户充值")
    @SaCheckPermission("consume:recharge:add")
    @PostMapping("/recharge")
    public ResponseDTO<RechargeResultDTO> recharge(@RequestBody @Valid RechargeRequestDTO rechargeRequest) {
        try {
            ResponseDTO<RechargeResultDTO> result = rechargeService.createRecharge(rechargeRequest);
            return result;
        } catch (Exception e) {
            log.error("充值失败", e);
            return ResponseDTO.error("充值失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询充值记录", description = "分页查询充值记录")
    @SaCheckPermission("consume:recharge:query")
    @PostMapping("/query")
    public ResponseDTO<PageResult<RechargeRecordEntity>> queryRechargeRecords(
            @RequestBody @Valid RechargeQueryDTO queryDTO) {
        try {
            ResponseDTO<org.springframework.data.domain.Page<RechargeRecordEntity>> response = rechargeService
                    .queryRechargeRecords(queryDTO);
            if (response.getOk() && response.getData() != null) {
                // 转换Spring Data Page到PageResult
                org.springframework.data.domain.Page<RechargeRecordEntity> page = response.getData();
                PageResult<RechargeRecordEntity> pageResult = new PageResult<>();
                pageResult.setPageNum((long) (page.getNumber() + 1));
                pageResult.setPageSize((long) page.getSize());
                pageResult.setTotal(page.getTotalElements());
                pageResult.setPages((long) page.getTotalPages());
                pageResult.setList(page.getContent());
                pageResult.setEmptyFlag(page.getContent().isEmpty());
                return ResponseDTO.ok(pageResult);
            }
            return ResponseDTO.error("查询充值记录失败");
        } catch (Exception e) {
            log.error("查询充值记录失败", e);
            return ResponseDTO.error("查询充值记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取充值详情", description = "根据ID获取充值详情")
    @SaCheckPermission("consume:recharge:detail")
    @GetMapping("/detail/{id}")
    public ResponseDTO<RechargeRecordEntity> getRechargeDetail(@PathVariable Long id) {
        try {
            ResponseDTO<RechargeRecordEntity> response = rechargeService.getRechargeDetail(id);
            return response;
        } catch (Exception e) {
            log.error("获取充值详情失败: id={}", id, e);
            return ResponseDTO.error("获取详情失败: " + e.getMessage());
        }
    }

    @Operation(summary = "退款", description = "充值退款")
    @SaCheckPermission("consume:recharge:refund")
    @PostMapping("/refund/{id}")
    public ResponseDTO<String> refundRecharge(@PathVariable Long id, @RequestBody Map<String, Object> refundData) {
        try {
            String reason = (String) refundData.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseDTO.error("退款原因不能为空");
            }
            return rechargeService.refundRecharge(id, reason);
        } catch (Exception e) {
            log.error("退款失败: id={}", id, e);
            return ResponseDTO.error("退款失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取充值统计", description = "获取充值统计数据")
    @SaCheckPermission("consume:recharge:statistics")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Object>> getRechargeStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime startDateTime = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime endDateTime = endDate != null ? LocalDateTime.parse(endDate) : null;
            ResponseDTO<Map<String, Object>> response = rechargeService.getRechargeStatistics(userId, startDateTime,
                    endDateTime);
            return response;
        } catch (Exception e) {
            log.error("获取充值统计失败", e);
            return ResponseDTO.error("获取统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取充值方式", description = "获取可用的充值方式")
    @SaCheckPermission("consume:recharge:methods")
    @GetMapping("/methods")
    public ResponseDTO<List<Map<String, Object>>> getRechargeMethods() {
        try {
            return rechargeService.getRechargeMethods();
        } catch (Exception e) {
            log.error("获取充值方式失败", e);
            return ResponseDTO.error("获取充值方式失败: " + e.getMessage());
        }
    }

    @Operation(summary = "验证充值金额", description = "验证充值金额是否有效")
    @SaCheckPermission("consume:recharge:validate")
    @PostMapping("/validate-amount")
    public ResponseDTO<Map<String, Object>> validateRechargeAmount(@RequestBody Map<String, Object> validateData) {
        try {
            String paymentMethod = (String) validateData.get("paymentMethod");
            Object amountObj = validateData.get("amount");
            if (paymentMethod == null || amountObj == null) {
                return ResponseDTO.error("支付方式和金额不能为空");
            }
            java.math.BigDecimal amount = new java.math.BigDecimal(amountObj.toString());
            return rechargeService.validateRechargeAmount(paymentMethod, amount);
        } catch (Exception e) {
            log.error("验证充值金额失败", e);
            return ResponseDTO.error("验证金额失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量充值", description = "批量充值操作")
    @SaCheckPermission("consume:recharge:batch")
    @PostMapping("/batch-recharge")
    public ResponseDTO<Map<String, Object>> batchRecharge(@RequestBody List<RechargeRequestDTO> rechargeRequests) {
        try {
            return rechargeService.batchRecharge(rechargeRequests);
        } catch (Exception e) {
            log.error("批量充值失败", e);
            return ResponseDTO.error("批量充值失败: " + e.getMessage());
        }
    }

    @Operation(summary = "导出充值记录", description = "导出充值记录数据")
    @SaCheckPermission("consume:recharge:export")
    @GetMapping("/export")
    public ResponseDTO<String> exportRechargeRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "EXCEL") String format) {
        try {
            return rechargeService.exportRechargeRecords(startDate, endDate, format);
        } catch (Exception e) {
            log.error("导出充值记录失败", e);
            return ResponseDTO.error("导出失败: " + e.getMessage());
        }
    }
}
