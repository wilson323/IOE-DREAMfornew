package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.RulePerformanceTestForm;
import net.lab1024.sa.attendance.domain.vo.RulePerformanceTestDetailVO;
import net.lab1024.sa.attendance.domain.vo.RulePerformanceTestResultVO;
import net.lab1024.sa.attendance.service.RulePerformanceTestService;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 规则性能测试控制器
 * <p>
 * 提供规则性能测试相关API接口
 * 支持大数据量并发测试和性能分析
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/rule-performance-test")
@Tag(name = "规则性能测试管理")
public class AttendanceRulePerformanceTestController {

    @Resource
    private RulePerformanceTestService rulePerformanceTestService;

    /**
     * 执行性能测试
     *
     * @param testForm 测试请求表单
     * @return 测试结果
     */
    @Observed(name = "rulePerformanceTest.execute", contextualName = "execute-performance-test")
    @PostMapping("/execute")
    @Operation(summary = "执行性能测试", description = "执行规则性能测试，支持大数据量并发测试")
    public ResponseDTO<RulePerformanceTestResultVO> executePerformanceTest(
            @Valid @RequestBody RulePerformanceTestForm testForm) {
        log.info("[性能测试] 执行性能测试: testName={}, type={}, concurrentUsers={}",
                testForm.getTestName(), testForm.getTestType(), testForm.getConcurrentUsers());

        try {
            RulePerformanceTestResultVO result = rulePerformanceTestService.executePerformanceTest(testForm);

            log.info("[性能测试] 测试完成: testId={}, totalRequests={}, successRate={}%, avgTime={}ms",
                    result.getTestId(), result.getTotalRequests(),
                    result.getSuccessRate(), result.getAvgResponseTime());
            return ResponseDTO.ok(result);
        } catch (InterruptedException e) {
            log.error("[性能测试] 测试被中断: testName={}", testForm.getTestName(), e);
            Thread.currentThread().interrupt(); // 恢复中断状态
            throw new RuntimeException("性能测试被中断", e);
        }
    }

    /**
     * 查询测试结果
     *
     * @param testId 测试ID
     * @return 测试结果
     */
    @Observed(name = "rulePerformanceTest.getResult", contextualName = "get-test-result")
    @GetMapping("/{testId}")
    @Operation(summary = "查询测试结果", description = "根据测试ID查询性能测试结果")
    public ResponseDTO<RulePerformanceTestResultVO> getTestResult(
            @PathVariable @Parameter(description = "测试ID", required = true) Long testId) {
        log.info("[性能测试] 查询测试结果: testId={}", testId);

        RulePerformanceTestResultVO result = rulePerformanceTestService.getTestResult(testId);

        if (result == null) {
            log.warn("[性能测试] 测试记录不存在: testId={}", testId);
            return ResponseDTO.error("TEST_NOT_FOUND", "测试记录不存在");
        }

        log.info("[性能测试] 查询测试结果成功: testId={}, status={}", testId, result.getTestStatus());
        return ResponseDTO.ok(result);
    }

    /**
     * 查询测试详情
     *
     * @param testId 测试ID
     * @return 测试详情
     */
    @Observed(name = "rulePerformanceTest.getDetail", contextualName = "get-test-detail")
    @GetMapping("/{testId}/detail")
    @Operation(summary = "查询测试详情", description = "查询性能测试的详细信息，包含响应时间分布和慢请求")
    public ResponseDTO<RulePerformanceTestDetailVO> getTestDetail(
            @PathVariable @Parameter(description = "测试ID", required = true) Long testId) {
        log.info("[性能测试] 查询测试详情: testId={}", testId);

        RulePerformanceTestDetailVO detail = rulePerformanceTestService.getTestDetail(testId);

        if (detail == null) {
            log.warn("[性能测试] 测试记录不存在: testId={}", testId);
            return ResponseDTO.error("TEST_NOT_FOUND", "测试记录不存在");
        }

        log.info("[性能测试] 查询测试详情成功: testId={}", testId);
        return ResponseDTO.ok(detail);
    }

    /**
     * 查询最近的测试列表
     *
     * @param limit 限制数量
     * @return 测试列表
     */
    @Observed(name = "rulePerformanceTest.getRecent", contextualName = "get-recent-tests")
    @GetMapping("/recent")
    @Operation(summary = "查询最近的测试", description = "查询最近执行的性能测试列表")
    public ResponseDTO<List<RulePerformanceTestResultVO>> getRecentTests(
            @RequestParam(defaultValue = "10") @Parameter(description = "限制数量", required = false) Integer limit) {
        log.info("[性能测试] 查询最近测试: limit={}", limit);

        List<RulePerformanceTestResultVO> tests = rulePerformanceTestService.getRecentTests(limit);

        log.info("[性能测试] 查询最近测试成功: count={}", tests.size());
        return ResponseDTO.ok(tests);
    }

    /**
     * 取消正在运行的测试
     *
     * @param testId 测试ID
     * @return 操作结果
     */
    @Observed(name = "rulePerformanceTest.cancel", contextualName = "cancel-test")
    @PostMapping("/{testId}/cancel")
    @Operation(summary = "取消测试", description = "取消正在运行的性能测试")
    public ResponseDTO<Void> cancelTest(
            @PathVariable @Parameter(description = "测试ID", required = true) Long testId) {
        log.info("[性能测试] 取消测试: testId={}", testId);

        rulePerformanceTestService.cancelTest(testId);

        log.info("[性能测试] 测试已取消: testId={}", testId);
        return ResponseDTO.ok();
    }

    /**
     * 删除测试记录
     *
     * @param testId 测试ID
     * @return 操作结果
     */
    @Observed(name = "rulePerformanceTest.delete", contextualName = "delete-test")
    @DeleteMapping("/{testId}")
    @Operation(summary = "删除测试记录", description = "删除指定的性能测试记录")
    public ResponseDTO<Void> deleteTest(
            @PathVariable @Parameter(description = "测试ID", required = true) Long testId) {
        log.info("[性能测试] 删除测试记录: testId={}", testId);

        rulePerformanceTestService.deleteTest(testId);

        log.info("[性能测试] 删除测试记录成功: testId={}", testId);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除测试记录
     *
     * @param testIds 测试ID列表
     * @return 操作结果
     */
    @Observed(name = "rulePerformanceTest.batchDelete", contextualName = "batch-delete-tests")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除测试记录", description = "批量删除多条性能测试记录")
    public ResponseDTO<Void> batchDeleteTests(
            @RequestBody @Parameter(description = "测试ID列表", required = true) List<Long> testIds) {
        log.info("[性能测试] 批量删除测试记录: count={}", testIds.size());

        rulePerformanceTestService.batchDeleteTests(testIds);

        log.info("[性能测试] 批量删除测试记录成功: count={}", testIds.size());
        return ResponseDTO.ok();
    }
}
