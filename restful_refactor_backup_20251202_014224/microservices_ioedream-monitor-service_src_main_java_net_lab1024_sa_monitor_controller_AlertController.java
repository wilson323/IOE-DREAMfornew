package net.lab1024.sa.monitor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.monitor.domain.form.AlertRuleAddForm;
import net.lab1024.sa.monitor.domain.form.AlertRuleQueryForm;
import net.lab1024.sa.monitor.domain.vo.AlertRuleVO;
import net.lab1024.sa.monitor.service.AlertService;

/**
 * 告警管理控制器
 * 负责告警规则管理、告警通知、告警历史等功能
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/alert")
@Tag(name = "告警管理", description = "告警规则管理、告警通知、告警历史等功能")
@Validated
public class AlertController {

    @Resource
    private AlertService alertService;

    @PostMapping("/rule/add")
    @Operation(summary = "添加告警规则", description = "创建新的告警监控规则")
    public ResponseDTO<Long> addAlertRule(@RequestBody @Valid AlertRuleAddForm addForm) {
        log.info("添加告警规则，规则名称：{}，监控指标：{}", addForm.getRuleName(), addForm.getMetricName());

        Long ruleId = alertService.addAlertRule(addForm);

        log.info("告警规则添加成功，规则ID：{}", ruleId);

        return ResponseDTO.ok(ruleId);
    }

    @GetMapping("/rule/query-page")
    @Operation(summary = "分页查询告警规则", description = "分页查询告警规则列表")
    public ResponseDTO<PageResult<AlertRuleVO>> queryAlertRulePage(@Valid AlertRuleQueryForm queryForm) {
        log.info("分页查询告警规则，页码：{}，页大小：{}", queryForm.getPageNum(), queryForm.getPageSize());

        PageResult<AlertRuleVO> pageResult = alertService.queryAlertRulePage(queryForm);

        log.info("告警规则分页查询完成，记录数：{}", pageResult.getList() != null ? pageResult.getList().size() : 0);

        return ResponseDTO.ok(pageResult);
    }

    @GetMapping("/rule/{ruleId}")
    @Operation(summary = "获取告警规则详情", description = "获取指定告警规则的详细信息")
    public ResponseDTO<AlertRuleVO> getAlertRuleDetail(@PathVariable Long ruleId) {
        log.info("获取告警规则详情，规则ID：{}", ruleId);

        AlertRuleVO ruleDetail = alertService.getAlertRuleDetail(ruleId);

        log.info("告警规则详情获取完成，规则名称：{}", ruleDetail.getRuleName());

        return ResponseDTO.ok(ruleDetail);
    }

    @PutMapping("/rule/{ruleId}/enable")
    @Operation(summary = "启用告警规则", description = "启用指定的告警规则")
    public ResponseDTO<Void> enableAlertRule(@PathVariable Long ruleId) {
        log.info("启用告警规则，规则ID：{}", ruleId);

        alertService.enableAlertRule(ruleId);

        log.info("告警规则启用完成");

        return ResponseDTO.ok();
    }

    @PutMapping("/rule/{ruleId}/disable")
    @Operation(summary = "禁用告警规则", description = "禁用指定的告警规则")
    public ResponseDTO<Void> disableAlertRule(@PathVariable Long ruleId) {
        log.info("禁用告警规则，规则ID：{}", ruleId);

        alertService.disableAlertRule(ruleId);

        log.info("告警规则禁用完成");

        return ResponseDTO.ok();
    }

    @DeleteMapping("/rule/{ruleId}")
    @Operation(summary = "删除告警规则", description = "删除指定的告警规则")
    public ResponseDTO<Void> deleteAlertRule(@PathVariable Long ruleId) {
        log.info("删除告警规则，规则ID：{}", ruleId);

        alertService.deleteAlertRule(ruleId);

        log.info("告警规则删除完成");

        return ResponseDTO.ok();
    }

    @GetMapping("/history")
    @Operation(summary = "告警历史", description = "获取告警历史记录")
    public ResponseDTO<PageResult<Map<String, Object>>> getAlertHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        log.info("获取告警历史，页码：{}，页大小：{}，严重级别：{}，状态：{}",
                pageNum, pageSize, severity, status);

        PageResult<Map<String, Object>> alertHistory = alertService.getAlertHistory(
                pageNum, pageSize, severity, status, startTime, endTime);

        log.info("告警历史获取完成，记录数：{}", alertHistory.getList() != null ? alertHistory.getList().size() : 0);

        return ResponseDTO.ok(alertHistory);
    }

    @GetMapping("/active/count")
    @Operation(summary = "活跃告警统计", description = "获取当前活跃告警的统计信息")
    public ResponseDTO<Map<String, Object>> getActiveAlertCount() {
        log.info("获取活跃告警统计");

        Map<String, Object> alertCount = alertService.getActiveAlertCount();

        log.info("活跃告警统计获取完成，总告警数：{}", alertCount.get("total"));

        return ResponseDTO.ok(alertCount);
    }

    @GetMapping("/statistics")
    @Operation(summary = "告警统计", description = "获取告警相关的统计信息")
    public ResponseDTO<Map<String, Object>> getAlertStatistics(
            @RequestParam(required = false) Integer days) {
        log.info("获取告警统计，统计天数：{}", days);

        Map<String, Object> statistics = alertService.getAlertStatistics(days);

        log.info("告警统计获取完成");

        return ResponseDTO.ok(statistics);
    }

    @PostMapping("/notification/test")
    @Operation(summary = "测试通知", description = "测试告警通知发送")
    public ResponseDTO<Map<String, Object>> testNotification(@RequestParam String notificationType,
            @RequestParam(required = false) List<String> recipients) {
        log.info("测试告警通知，通知类型：{}，接收人：{}", notificationType, recipients);

        Map<String, Object> testResult = alertService.testNotification(notificationType, recipients);

        log.info("告警通知测试完成，发送结果：{}", testResult.get("success"));

        return ResponseDTO.ok(testResult);
    }

    @GetMapping("/notification/channels")
    @Operation(summary = "通知渠道", description = "获取可用的告警通知渠道")
    public ResponseDTO<List<Map<String, Object>>> getNotificationChannels() {
        log.info("获取告警通知渠道");

        List<Map<String, Object>> channels = alertService.getNotificationChannels();

        log.info("告警通知渠道获取完成，渠道数量：{}", channels.size());

        return ResponseDTO.ok(channels);
    }

    @PostMapping("/batch/resolve")
    @Operation(summary = "批量解决告警", description = "批量解决多个告警")
    public ResponseDTO<Map<String, Integer>> batchResolveAlerts(@RequestBody List<Long> alertIds,
            @RequestParam(required = false) String resolution) {
        log.info("批量解决告警，告警数量：{}，解决说明：{}", alertIds.size(), resolution);

        Map<String, Integer> result = alertService.batchResolveAlerts(alertIds, resolution);

        log.info("批量告警解决完成，成功：{}，失败：{}", result.get("success"), result.get("failed"));

        return ResponseDTO.ok(result);
    }

    @GetMapping("/trends")
    @Operation(summary = "告警趋势", description = "获取告警趋势分析数据")
    public ResponseDTO<List<Map<String, Object>>> getAlertTrends(@RequestParam(defaultValue = "7") Integer days) {
        log.info("获取告警趋势，时间范围：{}天", days);

        List<Map<String, Object>> trends = alertService.getAlertTrends(days);

        log.info("告警趋势获取完成，数据点数：{}", trends.size());

        return ResponseDTO.ok(trends);
    }
}
