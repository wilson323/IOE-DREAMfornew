package net.lab1024.sa.access.advanced.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.advanced.domain.dto.LinkageTriggerDTO;
import net.lab1024.sa.access.advanced.domain.dto.LinkageRuleQueryDTO;
import net.lab1024.sa.access.advanced.domain.dto.LinkageRuleCreateDTO;
import net.lab1024.sa.access.advanced.domain.dto.LinkageRuleUpdateDTO;
import net.lab1024.sa.access.advanced.domain.vo.LinkageRuleVO;
import net.lab1024.sa.access.advanced.domain.vo.LinkageExecutionVO;
import net.lab1024.sa.access.advanced.service.LinkageRuleService;
import net.lab1024.sa.access.advanced.service.LinkageLogService;
import net.lab1024.sa.access.advanced.engine.GlobalLinkageEngine;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 全局联动控制器
 * 提供联动规则管理、触发控制和执行监控功能
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/linkage")
@Tag(name = "全局联动管理", description = "设备间联动控制和管理")
@Slf4j
public class GlobalLinkageController {

    @Resource
    private LinkageRuleService linkageRuleService;

    @Resource
    private LinkageLogService linkageLogService;

    @Resource
    private GlobalLinkageEngine linkageEngine;

    /**
     * 手动触发联动
     *
     * @param triggerDTO 联动触发信息
     * @return 触发结果
     */
    @PostMapping("/trigger")
    @Operation(summary = "手动触发联动", description = "根据指定条件手动触发联动规则执行")
    @SaCheckPermission("access:linkage:trigger")
    public ResponseDTO<String> triggerLinkage(@Valid @RequestBody LinkageTriggerDTO triggerDTO) {
        log.info("[GlobalLinkageController] 手动触发联动: triggerType={}, deviceId={}, userId={}",
                triggerDTO.getTriggerType(), triggerDTO.getDeviceId(), triggerDTO.getUserId());

        try {
            // 异步执行联动
            linkageEngine.processLinkageTrigger(triggerDTO)
                .thenAccept(response -> {
                    if (response.getCode() == 200) {
                        log.info("[GlobalLinkageController] 联动触发成功: {}", response.getMessage());
                    } else {
                        log.error("[GlobalLinkageController] 联动触发失败: {}", response.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("[GlobalLinkageController] 联动触发异常", throwable);
                    return null;
                });

            return ResponseDTO.userOk("联动触发已提交");

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 联动触发处理失败", e);
            return ResponseDTO.error("联动触发失败: " + e.getMessage());
        }
    }

    /**
     * 获取联动规则列表
     *
     * @param queryDTO 查询条件
     * @return 规则列表
     */
    @GetMapping("/rules")
    @Operation(summary = "获取联动规则列表", description = "根据条件查询联动规则")
    @SaCheckPermission("access:linkage:rules:list")
    public ResponseDTO<List<LinkageRuleVO>> getLinkageRules(LinkageRuleQueryDTO queryDTO) {
        log.debug("[GlobalLinkageController] 获取联动规则列表: {}", queryDTO);

        try {
            List<LinkageRuleVO> rules = linkageRuleService.getRules(queryDTO);
            return ResponseDTO.userOk(rules);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 获取联动规则列表失败", e);
            return ResponseDTO.error("获取联动规则失败: " + e.getMessage());
        }
    }

    /**
     * 创建联动规则
     *
     * @param createDTO 创建表单
     * @return 创建结果
     */
    @PostMapping("/rules")
    @Operation(summary = "创建联动规则", description = "创建新的设备联动规则")
    @SaCheckPermission("access:linkage:rules:create")
    public ResponseDTO<String> createLinkageRule(@Valid @RequestBody LinkageRuleCreateDTO createDTO) {
        log.info("[GlobalLinkageController] 创建联动规则: ruleName={}, triggerType={}",
                createDTO.getRuleName(), createDTO.getTriggerType());

        try {
            String ruleId = linkageRuleService.createRule(createDTO);
            return ResponseDTO.userOk(String.format("联动规则创建成功，ID: %s", ruleId));

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 创建联动规则失败", e);
            return ResponseDTO.error("创建联动规则失败: " + e.getMessage());
        }
    }

    /**
     * 更新联动规则
     *
     * @param ruleId 规则ID
     * @param updateDTO 更新表单
     * @return 更新结果
     */
    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "更新联动规则", description = "更新指定的联动规则")
    @SaCheckPermission("access:linkage:rules:update")
    public ResponseDTO<String> updateLinkageRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody LinkageRuleUpdateDTO updateDTO) {
        log.info("[GlobalLinkageController] 更新联动规则: ruleId={}", ruleId);

        try {
            linkageRuleService.updateRule(ruleId, updateDTO);
            return ResponseDTO.userOk("联动规则更新成功");

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 更新联动规则失败", e);
            return ResponseDTO.error("更新联动规则失败: " + e.getMessage());
        }
    }

    /**
     * 删除联动规则
     *
     * @param ruleId 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "删除联动规则", description = "删除指定的联动规则")
    @SaCheckPermission("access:linkage:rules:delete")
    public ResponseDTO<String> deleteLinkageRule(@PathVariable Long ruleId) {
        log.info("[GlobalLinkageController] 删除联动规则: ruleId={}", ruleId);

        try {
            linkageRuleService.deleteRule(ruleId);
            return ResponseDTO.userOk("联动规则删除成功");

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 删除联动规则失败", e);
            return ResponseDTO.error("删除联动规则失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用联动规则
     *
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PutMapping("/rules/{ruleId}/status")
    @Operation(summary = "启用/禁用联动规则", description = "切换联动规则的启用状态")
    @SaCheckPermission("access:linkage:rules:status")
    public ResponseDTO<String> toggleRuleStatus(
            @PathVariable Long ruleId,
            @RequestParam boolean enabled) {
        log.info("[GlobalLinkageController] 切换联动规则状态: ruleId={}, enabled={}", ruleId, enabled);

        try {
            linkageRuleService.toggleRuleStatus(ruleId, enabled);
            return ResponseDTO.userOk(String.format("联动规则已%s", enabled ? "启用" : "禁用"));

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 切换联动规则状态失败", e);
            return ResponseDTO.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 测试联动规则
     *
     * @param ruleId 规则ID
     * @param triggerDTO 测试触发数据
     * @return 测试结果
     */
    @PostMapping("/rules/{ruleId}/test")
    @Operation(summary = "测试联动规则", description = "测试指定联动规则的执行效果")
    @SaCheckPermission("access:linkage:rules:test")
    public ResponseDTO<Map<String, Object>> testLinkageRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody LinkageTriggerDTO triggerDTO) {
        log.info("[GlobalLinkageController] 测试联动规则: ruleId={}", ruleId);

        try {
            Map<String, Object> testResult = linkageRuleService.testRule(ruleId, triggerDTO);
            return ResponseDTO.userOk(testResult);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 测试联动规则失败", e);
            return ResponseDTO.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取联动执行记录
     *
     * @param ruleId 规则ID（可选）
     * @param status 执行状态（可选）
     * @param pageSize 页面大小
     * @return 执行记录列表
     */
    @GetMapping("/executions")
    @Operation(summary = "获取联动执行记录", description = "查询联动规则的执行历史")
    @SaCheckPermission("access:linkage:executions:list")
    public ResponseDTO<List<LinkageExecutionVO>> getLinkageExecutions(
            @RequestParam(required = false) Long ruleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        log.debug("[GlobalLinkageController] 获取联动执行记录: ruleId={}, status={}", ruleId, status);

        try {
            List<LinkageExecutionVO> executions = linkageLogService.getExecutions(ruleId, status, pageSize);
            return ResponseDTO.userOk(executions);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 获取联动执行记录失败", e);
            return ResponseDTO.error("获取执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取联动执行详情
     *
     * @param executionId 执行ID
     * @return 执行详情
     */
    @GetMapping("/executions/{executionId}")
    @Operation(summary = "获取联动执行详情", description = "获取指定联动执行的详细信息")
    @SaCheckPermission("access:linkage:executions:detail")
    public ResponseDTO<LinkageExecutionVO> getLinkageExecutionDetail(@PathVariable String executionId) {
        log.debug("[GlobalLinkageController] 获取联动执行详情: executionId={}", executionId);

        try {
            LinkageExecutionVO execution = linkageLogService.getExecutionDetail(executionId);
            return ResponseDTO.userOk(execution);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 获取联动执行详情失败", e);
            return ResponseDTO.error("获取执行详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取联动统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取联动统计信息", description = "获取联动系统的运行统计数据")
    @SaCheckPermission("access:linkage:statistics")
    public ResponseDTO<Map<String, Object>> getLinkageStatistics() {
        log.debug("[GlobalLinkageController] 获取联动统计信息");

        try {
            // 获取引擎统计
            Map<String, Object> engineStats = linkageEngine.getLinkageStatistics();

            // 获取规则统计
            Map<String, Object> ruleStats = linkageRuleService.getRuleStatistics();

            // 获取执行统计
            Map<String, Object> executionStats = linkageLogService.getExecutionStatistics();

            Map<String, Object> allStats = Map.of(
                "engine", engineStats,
                "rules", ruleStats,
                "executions", executionStats,
                "timestamp", System.currentTimeMillis()
            );

            return ResponseDTO.userOk(allStats);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 获取联动统计信息失败", e);
            return ResponseDTO.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清理历史数据
     *
     * @param days 保留天数
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理历史数据", description = "清理指定天数之前的联动历史数据")
    @SaCheckPermission("access:linkage:cleanup")
    public ResponseDTO<Map<String, Object>> cleanupHistoryData(@RequestParam Integer days) {
        log.info("[GlobalLinkageController] 清理历史数据: days={}", days);

        try {
            // 清理执行记录
            Map<String, Object> cleanupResult = linkageLogService.cleanupHistoryData(days);

            // 清理引擎缓存
            linkageEngine.cleanupCompletedLinkages();

            return ResponseDTO.userOk(cleanupResult);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 清理历史数据失败", e);
            return ResponseDTO.error("清理失败: " + e.getMessage());
        }
    }

    /**
     * 导出联动规则
     *
     * @param ruleIds 规则ID列表（可选）
     * @return 导出结果
     */
    @GetMapping("/rules/export")
    @Operation(summary = "导出联动规则", description = "导出指定的联动规则配置")
    @SaCheckPermission("access:linkage:rules:export")
    public ResponseDTO<Map<String, Object>> exportLinkageRules(
            @RequestParam(required = false) List<Long> ruleIds) {
        log.info("[GlobalLinkageController] 导出联动规则: ruleCount={}", ruleIds != null ? ruleIds.size() : "all");

        try {
            Map<String, Object> exportData = linkageRuleService.exportRules(ruleIds);
            return ResponseDTO.userOk(exportData);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 导出联动规则失败", e);
            return ResponseDTO.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导入联动规则
     *
     * @param importData 导入数据
     * @return 导入结果
     */
    @PostMapping("/rules/import")
    @Operation(summary = "导入联动规则", description = "导入联动规则配置")
    @SaCheckPermission("access:linkage:rules:import")
    public ResponseDTO<Map<String, Object>> importLinkageRules(@RequestBody Map<String, Object> importData) {
        log.info("[GlobalLinkageController] 导入联动规则");

        try {
            Map<String, Object> importResult = linkageRuleService.importRules(importData);
            return ResponseDTO.userOk(importResult);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 导入联动规则失败", e);
            return ResponseDTO.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取联动模板
     *
     * @param templateType 模板类型
     * @return 模板数据
     */
    @GetMapping("/templates/{templateType}")
    @Operation(summary = "获取联动模板", description = "获取指定类型的联动规则模板")
    @SaCheckPermission("access:linkage:templates")
    public ResponseDTO<Map<String, Object>> getLinkageTemplate(@PathVariable String templateType) {
        log.debug("[GlobalLinkageController] 获取联动模板: templateType={}", templateType);

        try {
            Map<String, Object> template = linkageRuleService.getTemplate(templateType);
            return ResponseDTO.userOk(template);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 获取联动模板失败", e);
            return ResponseDTO.error("获取模板失败: " + e.getMessage());
        }
    }

    /**
     * 验证联动规则配置
     *
     * @param ruleData 规则配置数据
     * @return 验证结果
     */
    @PostMapping("/rules/validate")
    @Operation(summary = "验证联动规则配置", description = "验证联动规则配置的正确性")
    @SaCheckPermission("access:linkage:rules:validate")
    public ResponseDTO<Map<String, Object>> validateRuleConfiguration(@RequestBody Map<String, Object> ruleData) {
        log.debug("[GlobalLinkageController] 验证联动规则配置");

        try {
            Map<String, Object> validationResult = linkageRuleService.validateRuleConfiguration(ruleData);
            return ResponseDTO.userOk(validationResult);

        } catch (Exception e) {
            log.error("[GlobalLinkageController] 验证联动规则配置失败", e);
            return ResponseDTO.error("验证失败: " + e.getMessage());
        }
    }
}