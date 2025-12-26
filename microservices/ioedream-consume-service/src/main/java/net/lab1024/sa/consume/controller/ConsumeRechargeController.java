package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
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
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeStatisticsVO;
import net.lab1024.sa.consume.service.ConsumeRechargeService;

/**
 * 消费充值控制器
 * <p>
 * 提供充值记录管理的REST API接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/api/consume/recharge")
@Tag(name = "消费管理-充值管理", description = "充值记录管理相关API接口")
@Slf4j
public class ConsumeRechargeController {

    @Resource
    private ConsumeRechargeService consumeRechargeService;

    @PostMapping("/query")
    @Operation(summary = "分页查询充值记录", description = "分页查询充值记录列表")
    public ResponseDTO<PageResult<ConsumeRechargeRecordVO>> queryRechargeRecords(
            @Valid @RequestBody ConsumeRechargeQueryForm queryForm) {
        try {
            PageResult<ConsumeRechargeRecordVO> result = consumeRechargeService.queryRechargeRecords(queryForm);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询充值记录失败: queryForm={}", queryForm, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    @GetMapping("/detail/{recordId}")
    @Operation(summary = "获取充值记录详情", description = "根据记录ID获取充值记录详情")
    public ResponseDTO<ConsumeRechargeRecordVO> getRechargeRecordDetail(@PathVariable Long recordId) {
        try {
            ConsumeRechargeRecordVO result = consumeRechargeService.getRechargeRecordDetail(recordId);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取充值记录详情失败: recordId={}", recordId, e);
            return ResponseDTO.error("获取详情失败，请稍后重试");
        }
    }

    @PostMapping("/add")
    @Operation(summary = "新增充值记录", description = "新增一条充值记录")
    public ResponseDTO<Long> addRechargeRecord(@Valid @RequestBody ConsumeRechargeAddForm addForm) {
        try {
            Long recordId = consumeRechargeService.addRechargeRecord(addForm);
            return ResponseDTO.ok(recordId);
        } catch (Exception e) {
            log.error("新增充值记录失败: addForm={}", addForm, e);
            return ResponseDTO.error("新增失败，请稍后重试");
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户充值记录", description = "根据用户ID获取充值记录列表")
    public ResponseDTO<PageResult<ConsumeRechargeRecordVO>> getUserRechargeRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<ConsumeRechargeRecordVO> result = consumeRechargeService.getUserRechargeRecords(userId, pageNum,
                    pageSize);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取用户充值记录失败: userId={}", userId, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日充值记录", description = "获取今日所有的充值记录")
    public ResponseDTO<List<ConsumeRechargeRecordVO>> getTodayRechargeRecords() {
        try {
            List<ConsumeRechargeRecordVO> result = consumeRechargeService.getTodayRechargeRecords();
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取今日充值记录失败", e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取充值统计信息", description = "根据条件获取充值统计信息")
    public ResponseDTO<ConsumeRechargeStatisticsVO> getRechargeStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        try {
            ConsumeRechargeStatisticsVO result = consumeRechargeService.getRechargeStatistics(userId, startDate,
                    endDate);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取充值统计信息失败: userId={}, startDate={}, endDate={}", userId, startDate, endDate, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    @GetMapping("/method-statistics")
    @Operation(summary = "获取充值方式统计", description = "获取充值方式、渠道、设备等统计信息")
    public ResponseDTO<Map<String, Object>> getRechargeMethodStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        try {
            Map<String, Object> result = consumeRechargeService.getRechargeMethodStatistics(startDate, endDate);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取充值方式统计失败: startDate={}, endDate={}", startDate, endDate, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    @GetMapping("/trend")
    @Operation(summary = "获取充值趋势", description = "获取充值趋势数据")
    public ResponseDTO<Map<String, Object>> getRechargeTrend(@RequestParam(defaultValue = "7") Integer days) {
        try {
            Map<String, Object> result = consumeRechargeService.getRechargeTrend(days);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取充值趋势数据失败: days={}", days, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    @PostMapping("/export")
    @Operation(summary = "导出充值记录", description = "导出符合条件的充值记录")
    public ResponseDTO<String> exportRechargeRecords(@Valid @RequestBody ConsumeRechargeQueryForm queryForm) {
        try {
            String exportUrl = consumeRechargeService.exportRechargeRecords(queryForm);
            return ResponseDTO.ok(exportUrl);
        } catch (Exception e) {
            log.error("导出充值记录失败: queryForm={}", queryForm, e);
            return ResponseDTO.error("导出失败，请稍后重试");
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量充值", description = "为多个用户执行批量充值")
    public ResponseDTO<Map<String, Object>> batchRecharge(
            @RequestParam List<Long> userIds,
            @RequestParam BigDecimal amount,
            @RequestParam String rechargeWay,
            @RequestParam(required = false) String remark) {
        try {
            Map<String, Object> result = consumeRechargeService.batchRecharge(userIds, amount, rechargeWay, remark);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("批量充值失败: userIds={}, amount={}, rechargeWay={}", userIds, amount, rechargeWay, e);
            return ResponseDTO.error("批量充值失败，请稍后重试");
        }
    }

    @GetMapping("/verify/{recordId}")
    @Operation(summary = "验证充值记录", description = "验证充值记录是否有效")
    public ResponseDTO<Boolean> verifyRechargeRecord(@PathVariable Long recordId) {
        try {
            Boolean result = consumeRechargeService.verifyRechargeRecord(recordId);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("验证充值记录失败: recordId={}", recordId, e);
            return ResponseDTO.error("验证失败，请稍后重试");
        }
    }

    @PostMapping("/reverse/{recordId}")
    @Operation(summary = "冲正充值记录", description = "对充值记录进行冲正操作")
    public ResponseDTO<Void> reverseRechargeRecord(
            @PathVariable Long recordId,
            @RequestParam String reason) {
        try {
            consumeRechargeService.reverseRechargeRecord(recordId, reason);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("冲正充值记录失败: recordId={}, reason={}", recordId, reason, e);
            return ResponseDTO.error("冲正失败，请稍后重试");
        }
    }
}
