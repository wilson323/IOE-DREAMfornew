package net.lab1024.sa.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.dto.SubsidyCalculationDTO;
import net.lab1024.sa.consume.domain.dto.SubsidyResultDTO;
import net.lab1024.sa.common.entity.consume.SubsidyRuleEntity;
import net.lab1024.sa.common.entity.consume.SubsidyRuleLogEntity;
import net.lab1024.sa.common.entity.consume.UserSubsidyRecordEntity;
import net.lab1024.sa.consume.service.SubsidyRuleEngineService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 补贴规则管理Controller
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@RestController
@RequestMapping("/api/consume/subsidy")
@Tag(name = "补贴规则管理", description = "补贴规则引擎管理接口")
@Slf4j
public class SubsidyRuleController {

    @Resource
    private SubsidyRuleEngineService subsidyRuleEngineService;

    // ==================== 规则计算 ====================

    /**
     * 计算补贴
     */
    @PostMapping("/calculate")
    @Operation(summary = "计算补贴")
    public ResponseDTO<SubsidyResultDTO> calculateSubsidy(@RequestBody SubsidyCalculationDTO calculationDTO) {
        log.info("[补贴规则API] 计算补贴: userId={}, amount={}",
                calculationDTO.getUserId(), calculationDTO.getConsumeAmount());
        SubsidyResultDTO result = subsidyRuleEngineService.calculateSubsidy(calculationDTO);
        return ResponseDTO.ok(result);
    }

    /**
     * 执行规则
     */
    @PostMapping("/execute")
    @Operation(summary = "执行规则")
    public ResponseDTO<SubsidyResultDTO> executeRule(@RequestBody SubsidyCalculationDTO calculationDTO) {
        log.info("[补贴规则API] 执行规则: userId={}, amount={}",
                calculationDTO.getUserId(), calculationDTO.getConsumeAmount());
        SubsidyResultDTO result = subsidyRuleEngineService.executeRule(calculationDTO);
        return ResponseDTO.ok(result);
    }

    // ==================== 规则管理 ====================

    /**
     * 查询有效规则
     */
    @GetMapping("/rules/effective")
    @Operation(summary = "查询有效规则")
    public ResponseDTO<List<SubsidyRuleEntity>> getEffectiveRules() {
        log.info("[补贴规则API] 查询有效规则");
        List<SubsidyRuleEntity> rules = subsidyRuleEngineService.getEffectiveRules();
        return ResponseDTO.ok(rules);
    }

    /**
     * 查询规则列表
     */
    @GetMapping("/rules")
    @Operation(summary = "查询规则列表")
    public ResponseDTO<List<SubsidyRuleEntity>> getRules(
            @Parameter(description = "补贴类型") @RequestParam(required = false) Integer subsidyType) {
        log.info("[补贴规则API] 查询规则列表: subsidyType={}", subsidyType);
        List<SubsidyRuleEntity> rules = subsidyType != null
                ? subsidyRuleEngineService.getRulesBySubsidyType(subsidyType)
                : subsidyRuleEngineService.getEffectiveRules();
        return ResponseDTO.ok(rules);
    }

    /**
     * 获取规则详情
     */
    @GetMapping("/rules/{ruleId}")
    @Operation(summary = "获取规则详情")
    public ResponseDTO<SubsidyRuleEntity> getRuleDetail(
            @Parameter(description = "规则ID") @PathVariable Long ruleId) {
        log.info("[补贴规则API] 查询规则详情: ruleId={}", ruleId);
        SubsidyRuleEntity rule = subsidyRuleEngineService.getById(ruleId);
        return ResponseDTO.ok(rule);
    }

    /**
     * 新增规则
     */
    @PostMapping("/rules")
    @Operation(summary = "新增规则")
    public ResponseDTO<Long> addRule(@RequestBody SubsidyRuleEntity rule) {
        log.info("[补贴规则API] 新增规则: ruleCode={}, ruleName={}",
                rule.getRuleCode(), rule.getRuleName());
        subsidyRuleEngineService.save(rule);
        return ResponseDTO.ok(rule.getId());
    }

    /**
     * 更新规则
     */
    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "更新规则")
    public ResponseDTO<Void> updateRule(
            @Parameter(description = "规则ID") @PathVariable Long ruleId,
            @RequestBody SubsidyRuleEntity rule) {
        log.info("[补贴规则API] 更新规则: ruleId={}", ruleId);
        rule.setId(ruleId);
        subsidyRuleEngineService.updateById(rule);
        return ResponseDTO.ok();
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "删除规则")
    public ResponseDTO<Void> deleteRule(
            @Parameter(description = "规则ID") @PathVariable Long ruleId) {
        log.info("[补贴规则API] 删除规则: ruleId={}", ruleId);
        subsidyRuleEngineService.removeById(ruleId);
        return ResponseDTO.ok();
    }

    /**
     * 启用规则
     */
    @PutMapping("/rules/{ruleId}/enable")
    @Operation(summary = "启用规则")
    public ResponseDTO<Void> enableRule(
            @Parameter(description = "规则ID") @PathVariable Long ruleId) {
        log.info("[补贴规则API] 启用规则: ruleId={}", ruleId);
        subsidyRuleEngineService.enableRule(ruleId);
        return ResponseDTO.ok();
    }

    /**
     * 禁用规则
     */
    @PutMapping("/rules/{ruleId}/disable")
    @Operation(summary = "禁用规则")
    public ResponseDTO<Void> disableRule(
            @Parameter(description = "规则ID") @PathVariable Long ruleId) {
        log.info("[补贴规则API] 禁用规则: ruleId={}", ruleId);
        subsidyRuleEngineService.disableRule(ruleId);
        return ResponseDTO.ok();
    }

    /**
     * 调整规则优先级
     */
    @PutMapping("/rules/{ruleId}/priority")
    @Operation(summary = "调整规则优先级")
    public ResponseDTO<Void> adjustPriority(
            @Parameter(description = "规则ID") @PathVariable Long ruleId,
            @Parameter(description = "优先级") @RequestParam Integer priority) {
        log.info("[补贴规则API] 调整优先级: ruleId={}, priority={}", ruleId, priority);
        subsidyRuleEngineService.adjustPriority(ruleId, priority);
        return ResponseDTO.ok();
    }

    // ==================== 补贴记录查询 ====================

    /**
     * 查询用户补贴记录
     */
    @GetMapping("/records/user/{userId}")
    @Operation(summary = "查询用户补贴记录")
    public ResponseDTO<List<UserSubsidyRecordEntity>> getUserRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate endDate) {
        log.info("[补贴规则API] 查询用户补贴记录: userId={}, startDate={}, endDate={}",
                userId, startDate, endDate);
        // TODO: 实现查询逻辑
        return ResponseDTO.ok(new ArrayList<>());
    }

    /**
     * 查询规则执行日志
     */
    @GetMapping("/logs")
    @Operation(summary = "查询规则执行日志")
    public ResponseDTO<List<SubsidyRuleLogEntity>> getRuleLogs(
            @Parameter(description = "规则ID") @RequestParam Long ruleId,
            @Parameter(description = "查询数量") @RequestParam(defaultValue = "100") Integer limit) {
        log.info("[补贴规则API] 查询规则执行日志: ruleId={}, limit={}", ruleId, limit);
        // TODO: 实现查询逻辑
        return ResponseDTO.ok(new ArrayList<>());
    }
}
