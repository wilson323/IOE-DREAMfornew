package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.domain.form.AntiPassbackRuleConfigForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackRuleConfigVO;
import net.lab1024.sa.access.service.AntiPassbackRuleConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反潜回规则配置控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/anti-passback-rules")
@Tag(name = "反潜回规则配置", description = "提供反潜回规则配置管理接口")
public class AntiPassbackRuleConfigController {

    private final AntiPassbackRuleConfigService antiPassbackRuleConfigService;

    public AntiPassbackRuleConfigController(AntiPassbackRuleConfigService antiPassbackRuleConfigService) {
        this.antiPassbackRuleConfigService = antiPassbackRuleConfigService;
    }

    @PostMapping
    @Operation(summary = "创建反潜回规则", description = "创建新的反潜回规则配置")
    public ResponseDTO<Long> createRule(@RequestBody AntiPassbackRuleConfigForm form) {
        log.info("[反潜回规则] 创建规则请求: ruleName={}", form.getRuleName());
        Long ruleId = antiPassbackRuleConfigService.createRule(form);
        return ResponseDTO.ok(ruleId);
    }

    @PutMapping("/{ruleId}")
    @Operation(summary = "更新反潜回规则", description = "更新反潜回规则配置")
    public ResponseDTO<Void> updateRule(
            @PathVariable Long ruleId,
            @RequestBody AntiPassbackRuleConfigForm form) {
        log.info("[反潜回规则] 更新规则请求: ruleId={}", ruleId);
        antiPassbackRuleConfigService.updateRule(ruleId, form);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{ruleId}")
    @Operation(summary = "删除反潜回规则", description = "删除反潜回规则配置")
    public ResponseDTO<Void> deleteRule(@PathVariable Long ruleId) {
        log.info("[反潜回规则] 删除规则请求: ruleId={}", ruleId);
        antiPassbackRuleConfigService.deleteRule(ruleId);
        return ResponseDTO.ok();
    }

    @GetMapping("/{ruleId}")
    @Operation(summary = "获取规则详情", description = "查询反潜回规则配置详情")
    public ResponseDTO<AntiPassbackRuleConfigVO> getRuleById(@PathVariable Long ruleId) {
        log.info("[反潜回规则] 查询规则请求: ruleId={}", ruleId);
        AntiPassbackRuleConfigVO rule = antiPassbackRuleConfigService.getRuleById(ruleId);
        return ResponseDTO.ok(rule);
    }

    @GetMapping
    @Operation(summary = "查询规则列表", description = "查询反潜回规则配置列表")
    public ResponseDTO<List<AntiPassbackRuleConfigVO>> queryRules(
            @RequestParam(required = false) Long areaId) {
        log.info("[反潜回规则] 查询规则列表请求: areaId={}", areaId);
        List<AntiPassbackRuleConfigVO> rules = antiPassbackRuleConfigService.queryRules(areaId);
        return ResponseDTO.ok(rules);
    }

    @PutMapping("/{ruleId}/toggle")
    @Operation(summary = "启用/禁用规则", description = "切换反潜回规则启用状态")
    public ResponseDTO<Void> toggleRule(
            @PathVariable Long ruleId,
            @RequestParam Boolean enabled) {
        log.info("[反潜回规则] 切换规则状态请求: ruleId={}, enabled={}", ruleId, enabled);
        antiPassbackRuleConfigService.toggleRule(ruleId, enabled);
        return ResponseDTO.ok();
    }

    @PostMapping("/{ruleId}/test")
    @Operation(summary = "测试规则", description = "模拟通行场景测试规则")
    public ResponseDTO<Object> testRule(
            @PathVariable Long ruleId,
            @RequestParam String testScenario) {
        log.info("[反潜回规则] 测试规则请求: ruleId={}, scenario={}", ruleId, testScenario);
        Object result = antiPassbackRuleConfigService.testRule(ruleId, testScenario);
        return ResponseDTO.ok(result);
    }
}
