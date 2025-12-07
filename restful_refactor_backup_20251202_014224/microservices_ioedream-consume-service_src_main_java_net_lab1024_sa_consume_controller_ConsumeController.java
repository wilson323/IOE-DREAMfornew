package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
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
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.common.annotation.RequireResource;
import net.lab1024.sa.common.annotation.SaCheckLogin;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import net.lab1024.sa.common.controller.SupportBaseController;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.service.ConsumeService;

@RestController
@RequestMapping("/api/consume")
@Tag(name = "消费管理", description = "POS/支付/账单")
@Validated
public class ConsumeController extends SupportBaseController {

    @Resource
    private ConsumeService consumeService;

    @PostMapping("/pay")
    @Operation(summary = "支付", description = "创建消费记录")
    @SaCheckLogin
    @SaCheckPermission("consume:pay:add")
    @RequireResource(resource = "consume:pay", dataScope = "SELF")
    public ResponseDTO<String> pay(@RequestParam @NotNull Long personId,
            @RequestParam @NotNull String personName, @RequestParam @NotNull BigDecimal amount,
            @RequestParam @NotNull String payMethod, @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String remark) {
        return consumeService.pay(personId, personName, amount, payMethod, deviceId, remark);
    }

    @GetMapping("/records")
    @Operation(summary = "分页查询消费记录")
    @SaCheckLogin
    @SaCheckPermission("consume:record:query")
    @RequireResource(resource = "smart:consume:record", dataScope = "DEPT")
    public ResponseDTO<PageResult<ConsumeRecordEntity>> pageRecords(@Valid PageParam pageParam,
            @RequestParam(required = false) Long personId) {
        return ResponseDTO.ok(consumeService.pageRecords(pageParam, personId));
    }

    @PostMapping("/consume")
    @Operation(summary = "消费", description = "执行消费扣费，支持多种消费模式")
    @SaCheckLogin
    @SaCheckPermission("consume:execute:add")
    public ResponseDTO<Map<String, Object>> consume(@Valid @RequestBody Map<String, Object> consumeRequest) {
        try {
            // 这里调用消费引擎进行实际消费处理
            return ResponseDTO.ok(consumeService.processConsume(consumeRequest));
        } catch (Exception e) {
            return ResponseDTO.error("消费处理失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取消费详情", description = "根据ID获取消费记录详情")
    @SaCheckLogin
    @SaCheckPermission("consume:record:detail")
    public ResponseDTO<ConsumeRecordEntity> getConsumeDetail(@PathVariable Long id) {
        try {
            return ResponseDTO.ok(consumeService.getConsumeDetail(id));
        } catch (Exception e) {
            return ResponseDTO.error("获取消费详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "消费统计", description = "获取消费统计数据")
    @SaCheckLogin
    @SaCheckPermission("consume:statistics:view")
    public ResponseDTO<Map<String, Object>> getConsumeStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        try {
            return ResponseDTO.ok(consumeService.getConsumeStatistics(userId, startTime, endTime));
        } catch (Exception e) {
            return ResponseDTO.error("获取消费统计失败: " + e.getMessage());
        }
    }

    @PostMapping("/refund/{id}")
    @Operation(summary = "退款", description = "对消费记录进行退款")
    @SaCheckLogin
    @SaCheckPermission("consume:refund:add")
    public ResponseDTO<String> refundConsume(@PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            return ResponseDTO.ok(consumeService.refundConsume(id, reason));
        } catch (Exception e) {
            return ResponseDTO.error("退款失败: " + e.getMessage());
        }
    }

    @GetMapping("/modes")
    @Operation(summary = "消费模式列表", description = "获取可用的消费模式")
    @SaCheckLogin
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<List<Map<String, Object>>> getConsumeModes() {
        try {
            return ResponseDTO.ok(consumeService.getAvailableConsumeModes());
        } catch (Exception e) {
            return ResponseDTO.error("获取消费模式失败: " + e.getMessage());
        }
    }

    @GetMapping("/account/{userId}/balance")
    @Operation(summary = "查询账户余额", description = "查询用户账户余额")
    @SaCheckLogin
    @SaCheckPermission("consume:account:view")
    public ResponseDTO<Map<String, Object>> getAccountBalance(@PathVariable Long userId) {
        try {
            return ResponseDTO.ok(consumeService.getAccountBalance(userId));
        } catch (Exception e) {
            return ResponseDTO.error("查询账户余额失败: " + e.getMessage());
        }
    }

    @PostMapping("/account/{userId}/freeze")
    @Operation(summary = "冻结账户", description = "冻结用户账户")
    @SaCheckLogin
    @SaCheckPermission("consume:account:freeze")
    public ResponseDTO<String> freezeAccount(@PathVariable Long userId,
            @RequestParam String reason) {
        try {
            return ResponseDTO.ok(consumeService.freezeAccount(userId, reason));
        } catch (Exception e) {
            return ResponseDTO.error("冻结账户失败: " + e.getMessage());
        }
    }

    @PostMapping("/account/{userId}/unfreeze")
    @Operation(summary = "解冻账户", description = "解冻用户账户")
    @SaCheckLogin
    @SaCheckPermission("consume:account:unfreeze")
    public ResponseDTO<String> unfreezeAccount(@PathVariable Long userId,
            @RequestParam String reason) {
        try {
            return ResponseDTO.ok(consumeService.unfreezeAccount(userId, reason));
        } catch (Exception e) {
            return ResponseDTO.error("解冻账户失败: " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "消费验证", description = "验证消费参数和权限")
    @SaCheckLogin
    @SaCheckPermission("consume:validate")
    public ResponseDTO<Map<String, Object>> validateConsume(@RequestParam @NotNull Long personId,
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String consumeMode) {
        try {
            return ResponseDTO.ok(consumeService.validateConsume(personId, amount, deviceId, consumeMode));
        } catch (Exception e) {
            return ResponseDTO.error("消费验证失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量消费", description = "批量处理消费记录")
    @SaCheckLogin
    @SaCheckPermission("consume:batch:add")
    public ResponseDTO<List<Map<String, Object>>> batchConsume(
            @Valid @RequestBody List<Map<String, Object>> consumeRequests) {
        try {
            return ResponseDTO.ok(consumeService.batchConsume(consumeRequests));
        } catch (Exception e) {
            return ResponseDTO.error("批量消费失败: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    @Operation(summary = "导出消费记录", description = "导出消费记录数据")
    @SaCheckLogin
    @SaCheckPermission("consume:record:export")
    public ResponseDTO<String> exportRecords(
            @RequestParam(required = false) Long personId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String consumeMode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String format) {
        try {
            return ResponseDTO.ok(consumeService.exportRecords(personId, orderNo, deviceId, consumeMode, status,
                    startTime, endTime, format));
        } catch (Exception e) {
            return ResponseDTO.error("导出消费记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/trend")
    @Operation(summary = "消费趋势", description = "获取消费趋势数据")
    @SaCheckLogin
    @SaCheckPermission("consume:trend:view")
    public ResponseDTO<List<Map<String, Object>>> getConsumeTrend(
            @RequestParam(required = false) String timeDimension,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) Long personId) {
        try {
            return ResponseDTO.ok(consumeService.getConsumeTrend(timeDimension, startTime, endTime, personId));
        } catch (Exception e) {
            return ResponseDTO.error("获取消费趋势失败: " + e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消消费", description = "取消未完成的消费记录")
    @SaCheckLogin
    @SaCheckPermission("consume:record:cancel")
    public ResponseDTO<String> cancelConsume(@PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            return ResponseDTO.ok(consumeService.cancelConsume(id, reason));
        } catch (Exception e) {
            return ResponseDTO.error("取消消费失败: " + e.getMessage());
        }
    }

    @GetMapping("/logs/{id}")
    @Operation(summary = "消费日志", description = "获取消费记录的操作日志")
    @SaCheckLogin
    @SaCheckPermission("consume:record:log")
    public ResponseDTO<List<Map<String, Object>>> getConsumeLogs(@PathVariable Long id) {
        try {
            return ResponseDTO.ok(consumeService.getConsumeLogs(id));
        } catch (Exception e) {
            return ResponseDTO.error("获取消费日志失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync")
    @Operation(summary = "同步数据", description = "同步消费数据到其他系统")
    @SaCheckLogin
    @SaCheckPermission("consume:sync")
    public ResponseDTO<String> syncConsumeData(@RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        try {
            return ResponseDTO.ok(consumeService.syncConsumeData(startTime, endTime));
        } catch (Exception e) {
            return ResponseDTO.error("同步消费数据失败: " + e.getMessage());
        }
    }
}
