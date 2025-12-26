package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.RuleTestRequest;
import net.lab1024.sa.attendance.domain.vo.RuleTestResultVO;
import net.lab1024.sa.attendance.service.RuleTestService;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 考勤规则测试控制器
 * <p>
 * 提供规则测试工具相关API接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/rule-test")
@Tag(name = "考勤规则测试工具")
public class AttendanceRuleTestController {

    @Resource
    private RuleTestService ruleTestService;

    /**
     * 测试单个规则
     *
     * @param ruleId 规则ID
     * @param testRequest 测试请求
     * @return 测试结果
     */
    @Observed(name = "ruleTest.testRule", contextualName = "test-single-rule")
    @PostMapping("/test/{ruleId}")
    @Operation(summary = "测试单个规则", description = "测试指定规则的执行结果")
    public ResponseDTO<RuleTestResultVO> testRule(
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId,
            @Valid @RequestBody RuleTestRequest testRequest) {
        log.info("[规则测试] 测试规则: ruleId={}, userId={}, punchTime={}",
                ruleId, testRequest.getUserId(), testRequest.getPunchTime());

        RuleTestResultVO result = ruleTestService.testRule(ruleId, testRequest);

        log.info("[规则测试] 规则测试完成: ruleId={}, testResult={}, executionTime={}ms",
                ruleId, result.getTestResult(), result.getExecutionTime());
        return ResponseDTO.ok(result);
    }

    /**
     * 测试自定义规则
     *
     * @param testRequest 测试请求（包含ruleCondition和ruleAction）
     * @return 测试结果
     */
    @Observed(name = "ruleTest.testCustomRule", contextualName = "test-custom-rule")
    @PostMapping("/test-custom")
    @Operation(summary = "测试自定义规则", description = "测试自定义规则条件和动作的执行结果")
    public ResponseDTO<RuleTestResultVO> testCustomRule(@Valid @RequestBody RuleTestRequest testRequest) {
        log.info("[规则测试] 测试自定义规则: condition={}, action={}",
                testRequest.getRuleCondition(), testRequest.getRuleAction());

        RuleTestResultVO result = ruleTestService.testCustomRule(testRequest);

        log.info("[规则测试] 自定义规则测试完成: testResult={}, executionTime={}ms",
                result.getTestResult(), result.getExecutionTime());
        return ResponseDTO.ok(result);
    }

    /**
     * 批量测试规则
     *
     * @param ruleIds 规则ID列表
     * @param testRequest 测试请求
     * @return 测试结果列表
     */
    @Observed(name = "ruleTest.batchTest", contextualName = "batch-test-rules")
    @PostMapping("/batch")
    @Operation(summary = "批量测试规则", description = "批量测试多个规则的执行结果")
    public ResponseDTO<List<RuleTestResultVO>> batchTestRules(
            @RequestBody List<Long> ruleIds,
            @Valid @RequestBody RuleTestRequest testRequest) {
        log.info("[规则测试] 批量测试规则: count={}", ruleIds.size());

        List<RuleTestResultVO> results = ruleTestService.batchTestRules(ruleIds, testRequest);

        long matchedCount = results.stream()
                .filter(r -> "MATCH".equals(r.getTestResult()))
                .count();

        log.info("[规则测试] 批量测试完成: total={}, matched={}", results.size(), matchedCount);
        return ResponseDTO.ok(results);
    }

    /**
     * 快速测试（使用默认数据）
     *
     * @param ruleCondition 规则条件
     * @param ruleAction 规则动作
     * @return 测试结果
     */
    @Observed(name = "ruleTest.quickTest", contextualName = "quick-test-rule")
    @PostMapping("/quick")
    @Operation(summary = "快速测试", description = "使用默认测试数据快速测试规则")
    public ResponseDTO<RuleTestResultVO> quickTest(
            @RequestParam @Parameter(description = "规则条件", required = true) String ruleCondition,
            @RequestParam @Parameter(description = "规则动作", required = true) String ruleAction) {
        log.info("[规则测试] 快速测试: condition={}", ruleCondition);

        RuleTestResultVO result = ruleTestService.quickTest(ruleCondition, ruleAction);

        log.info("[规则测试] 快速测试完成: testResult={}", result.getTestResult());
        return ResponseDTO.ok(result);
    }

    /**
     * 生成测试数据
     *
     * @param scenario 测试场景：LATE-迟到 EARLY-早退 OVERTIME-加班 ABSENT-缺勤 NORMAL-正常
     * @return 测试请求数据
     */
    @Observed(name = "ruleTest.generateData", contextualName = "generate-test-data")
    @GetMapping("/generate/{scenario}")
    @Operation(summary = "生成测试数据", description = "根据场景生成模拟测试数据")
    public ResponseDTO<RuleTestRequest> generateTestData(
            @PathVariable @Parameter(description = "测试场景", required = true) String scenario) {
        log.info("[规则测试] 生成测试数据: scenario={}", scenario);

        RuleTestRequest testRequest = ruleTestService.generateTestData(scenario);

        log.info("[规则测试] 测试数据生成成功: scenario={}, userId={}", scenario, testRequest.getUserId());
        return ResponseDTO.ok(testRequest);
    }

    /**
     * 验证规则语法
     *
     * @param ruleCondition 规则条件
     * @param ruleAction 规则动作
     * @return 验证结果：true-有效 false-无效
     */
    @Observed(name = "ruleTest.validateSyntax", contextualName = "validate-rule-syntax")
    @PostMapping("/validate")
    @Operation(summary = "验证规则语法", description = "验证规则条件和动作的JSON格式和语法")
    public ResponseDTO<Boolean> validateRuleSyntax(
            @RequestParam @Parameter(description = "规则条件", required = true) String ruleCondition,
            @RequestParam @Parameter(description = "规则动作", required = false) String ruleAction) {
        log.info("[规则测试] 验证规则语法: condition={}, action={}", ruleCondition, ruleAction);

        Boolean isValid = ruleTestService.validateRuleSyntax(ruleCondition, ruleAction);

        log.info("[规则测试] 语法验证完成: valid={}", isValid);
        return ResponseDTO.ok(isValid);
    }

    /**
     * 获取测试场景列表
     *
     * @return 测试场景列表
     */
    @Observed(name = "ruleTest.getScenarios", contextualName = "get-test-scenarios")
    @GetMapping("/scenarios")
    @Operation(summary = "获取测试场景列表", description = "返回可用的测试场景列表")
    public ResponseDTO<List<RuleTestService.TestScenarioVO>> getTestScenarios() {
        log.info("[规则测试] 获取测试场景列表");

        List<RuleTestService.TestScenarioVO> scenarios = ruleTestService.getTestScenarios();

        log.info("[规则测试] 测试场景列表获取成功: count={}", scenarios.size());
        return ResponseDTO.ok(scenarios);
    }
}
