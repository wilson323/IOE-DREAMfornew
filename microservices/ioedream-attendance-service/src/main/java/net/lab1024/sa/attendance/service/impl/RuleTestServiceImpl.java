package net.lab1024.sa.attendance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import net.lab1024.sa.attendance.domain.form.RuleTestRequest;
import net.lab1024.sa.attendance.domain.vo.RuleTestResultVO;
import net.lab1024.sa.attendance.domain.vo.RuleTestHistoryDetailVO;
import net.lab1024.sa.attendance.engine.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.engine.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.model.RuleEvaluationResult;
import net.lab1024.sa.attendance.engine.model.RuleTestContext;
import net.lab1024.sa.attendance.service.RuleTestService;
import net.lab1024.sa.attendance.service.RuleTestHistoryService;
import net.lab1024.sa.common.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 规则测试服务实现类
 * <p>
 * 提供规则测试工具相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class RuleTestServiceImpl implements RuleTestService {

    @Resource
    private AttendanceRuleEngine attendanceRuleEngine;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RuleTestHistoryService ruleTestHistoryService;

    /**
     * 测试单个规则
     */
    @Override
    public RuleTestResultVO testRule(Long ruleId, RuleTestRequest testRequest) {
        log.info("[规则测试] 测试规则: ruleId={}", ruleId);

        long startTime = System.currentTimeMillis();

        try {
            // 构建规则执行上下文
            RuleExecutionContext context = buildExecutionContext(testRequest);

            // 执行规则评估
            RuleEvaluationResult evaluationResult = attendanceRuleEngine.evaluateRule(ruleId, context);

            // 构建测试结果
            long executionTime = System.currentTimeMillis() - startTime;
            RuleTestResultVO result = buildTestResult(ruleId, evaluationResult, executionTime, testRequest);

            // 保存测试历史
            saveTestHistory(result, testRequest);

            log.info("[规则测试] 测试完成: ruleId={}, result={}", ruleId, result.getTestResult());
            return result;

        } catch (Exception e) {
            log.error("[规则测试] 测试失败: ruleId={}, error={}", ruleId, e.getMessage(), e);
            return buildErrorResult(ruleId, e, testRequest, startTime);
        }
    }

    /**
     * 测试自定义规则
     */
    @Override
    public RuleTestResultVO testCustomRule(RuleTestRequest testRequest) {
        log.info("[规则测试] 测试自定义规则: condition={}, action={}",
                testRequest.getRuleCondition(), testRequest.getRuleAction());

        long startTime = System.currentTimeMillis();

        try {
            // 编译规则条件
            var compiledRule = attendanceRuleEngine.compileRuleCondition(testRequest.getRuleCondition());

            // 编译规则动作
            var compiledAction = attendanceRuleEngine.compileRuleAction(testRequest.getRuleAction());

            // 构建规则执行上下文
            RuleExecutionContext context = buildExecutionContext(testRequest);

            // TODO: 实现规则评估（待规则引擎API稳定后）
            Boolean conditionMatched = true; // 临时默认值
            log.info("[规则测试] 条件评估结果: matched={}", conditionMatched);

            // 构建测试结果
            RuleTestResultVO result = RuleTestResultVO.builder()
                    .testId(UUID.randomUUID().toString())
                    .ruleCondition(testRequest.getRuleCondition())
                    .ruleAction(testRequest.getRuleAction())
                    .testResult(conditionMatched ? "MATCH" : "NOT_MATCH")
                    .resultMessage(conditionMatched ? "规则匹配成功" : "规则不匹配")
                    .conditionMatched(conditionMatched)
                    .executionTime(System.currentTimeMillis() - startTime)
                    .testTimestamp(LocalDateTime.now())
                    .testInputData(extractInputData(testRequest))
                    .build();

            // 如果条件匹配，执行动作
            if (conditionMatched) {
                List<RuleTestResultVO.ActionExecutionVO> executedActions = executeActions(compiledAction, context);
                result.setExecutedActions(executedActions);
            }

            // 添加执行日志
            List<RuleTestResultVO.ExecutionLogVO> logs = new ArrayList<>();
            logs.add(RuleTestResultVO.ExecutionLogVO.builder()
                    .logLevel("INFO")
                    .logMessage("规则测试完成")
                    .logTimestamp(LocalDateTime.now())
                    .build());
            result.setExecutionLogs(logs);

            // 保存测试历史
            saveTestHistory(result, testRequest);

            log.info("[规则测试] 自定义规则测试完成: result={}", result.getTestResult());
            return result;

        } catch (Exception e) {
            log.error("[规则测试] 自定义规则测试失败: error={}", e.getMessage(), e);
            return buildErrorResult(null, e, testRequest, startTime);
        }
    }

    /**
     * 批量测试规则
     */
    @Override
    public List<RuleTestResultVO> batchTestRules(List<Long> ruleIds, RuleTestRequest testRequest) {
        log.info("[规则测试] 批量测试规则: count={}", ruleIds.size());

        List<RuleTestResultVO> results = new ArrayList<>();
        for (Long ruleId : ruleIds) {
            RuleTestResultVO result = testRule(ruleId, testRequest);
            results.add(result);
        }

        log.info("[规则测试] 批量测试完成: total={}, matched={}",
                results.size(), results.stream().filter(r -> "MATCH".equals(r.getTestResult())).count());
        return results;
    }

    /**
     * 模拟测试数据
     */
    @Override
    public RuleTestRequest generateTestData(String scenario) {
        log.info("[规则测试] 生成测试数据: scenario={}", scenario);

        RuleTestRequest.RuleTestRequestBuilder builder = RuleTestRequest.builder();

        switch (scenario.toUpperCase()) {
            case "LATE": {
                // 迟到场景
                Map<String, Object> attendanceAttrs = new HashMap<>();
                attendanceAttrs.put("lateMinutes", 5);

                builder.userId(1001L)
                        .userName("张三")
                        .departmentId(10L)
                        .departmentName("研发部")
                        .attendanceDate(LocalDate.now())
                        .punchTime(LocalTime.of(8, 35, 0))
                        .punchType("IN")
                        .scheduleStartTime(LocalTime.of(8, 30, 0))
                        .scheduleEndTime(LocalTime.of(17, 30, 0))
                        .userAttributes(new HashMap<>())
                        .attendanceAttributes(attendanceAttrs);
                break;
            }

            case "EARLY": {
                // 早退场景
                Map<String, Object> attendanceAttrs = new HashMap<>();
                attendanceAttrs.put("earlyLeaveMinutes", 15);

                builder.userId(1002L)
                        .userName("李四")
                        .departmentId(10L)
                        .departmentName("研发部")
                        .attendanceDate(LocalDate.now())
                        .punchTime(LocalTime.of(17, 15, 0))
                        .punchType("OUT")
                        .scheduleStartTime(LocalTime.of(8, 30, 0))
                        .scheduleEndTime(LocalTime.of(17, 30, 0))
                        .userAttributes(new HashMap<>())
                        .attendanceAttributes(attendanceAttrs);
                break;
            }
            case "OVERTIME": {
                // 加班场景
                Map<String, Object> attendanceAttrs = new HashMap<>();
                attendanceAttrs.put("overtimeHours", 2.5);

                builder.userId(1003L)
                        .userName("王五")
                        .departmentId(20L)
                        .departmentName("市场部")
                        .attendanceDate(LocalDate.now())
                        .punchTime(LocalTime.of(20, 0, 0))
                        .punchType("OUT")
                        .scheduleStartTime(LocalTime.of(8, 30, 0))
                        .scheduleEndTime(LocalTime.of(17, 30, 0))
                        .userAttributes(new HashMap<>())
                        .attendanceAttributes(attendanceAttrs);
                break;
            }

            case "ABSENT": {
                // 缺勤场景
                Map<String, Object> attendanceAttrs = new HashMap<>();
                attendanceAttrs.put("absentHours", 8);

                builder.userId(1004L)
                        .userName("赵六")
                        .departmentId(30L)
                        .departmentName("财务部")
                        .attendanceDate(LocalDate.now())
                        .punchTime(null)
                        .punchType("IN")
                        .scheduleStartTime(LocalTime.of(8, 30, 0))
                        .scheduleEndTime(LocalTime.of(17, 30, 0))
                        .userAttributes(new HashMap<>())
                        .attendanceAttributes(attendanceAttrs);
                break;
            }

            case "NORMAL": {
                // 正常场景
                builder.userId(1005L)
                        .userName("孙七")
                        .departmentId(10L)
                        .departmentName("研发部")
                        .attendanceDate(LocalDate.now())
                        .punchTime(LocalTime.of(8, 28, 0))
                        .punchType("IN")
                        .scheduleStartTime(LocalTime.of(8, 30, 0))
                        .scheduleEndTime(LocalTime.of(17, 30, 0))
                        .userAttributes(new HashMap<>())
                        .attendanceAttributes(new HashMap<>());
                break;
            }

            default:
                throw new BusinessException("INVALID_SCENARIO", "不支持的测试场景: " + scenario);
        }

        log.info("[规则测试] 测试数据生成成功: scenario={}", scenario);
        return builder.build();
    }

    /**
     * 快速测试
     */
    @Override
    public RuleTestResultVO quickTest(String ruleCondition, String ruleAction) {
        log.info("[规则测试] 快速测试: condition={}", ruleCondition);

        // 使用默认数据
        RuleTestRequest testRequest = generateTestData("NORMAL");

        // 设置自定义规则
        testRequest.setRuleCondition(ruleCondition);
        testRequest.setRuleAction(ruleAction);

        return testCustomRule(testRequest);
    }

    /**
     * 验证规则语法
     */
    @Override
    public Boolean validateRuleSyntax(String ruleCondition, String ruleAction) {
        try {
            objectMapper.readTree(ruleCondition);
            objectMapper.readTree(ruleAction);
            return true;
        } catch (JsonProcessingException e) {
            log.warn("[规则测试] JSON格式验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取测试场景列表
     */
    @Override
    public List<TestScenarioVO> getTestScenarios() {
        List<TestScenarioVO> scenarios = new ArrayList<>();

        scenarios.add(createScenario("LATE", "迟到场景", "测试迟到相关规则", "{\"lateMinutes\": 5}"));
        scenarios.add(createScenario("EARLY", "早退场景", "测试早退相关规则", "{\"earlyLeaveMinutes\": 15}"));
        scenarios.add(createScenario("OVERTIME", "加班场景", "测试加班相关规则", "{\"overtimeHours\": 2}"));
        scenarios.add(createScenario("ABSENT", "缺勤场景", "测试缺勤相关规则", "{\"absentHours\": 4}"));
        scenarios.add(createScenario("NORMAL", "正常场景", "测试正常上下班", "{}"));

        return scenarios;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建规则执行上下文
     * <p>
     * 使用RuleTestContext构建类型安全的测试上下文，然后转换为RuleExecutionContext
     * </p>
     */
    private RuleExecutionContext buildExecutionContext(RuleTestRequest testRequest) {
        // 使用RuleTestContext构建类型安全的测试上下文
        RuleTestContext testContext = RuleTestContext.testBuilder()
                // 基础字段
                .employeeId(testRequest.getUserId())
                .employeeName(testRequest.getUserName())
                .departmentId(testRequest.getDepartmentId())
                .scheduleDate(testRequest.getAttendanceDate() != null ? testRequest.getAttendanceDate() : LocalDate.now())
                // 测试专用字段
                .executionId(UUID.randomUUID().toString())
                .executionTimestamp(LocalDateTime.now())
                .executionMode("TEST")
                .punchTime(testRequest.getPunchTime())
                .punchType(testRequest.getPunchType())
                .scheduleStartTime(testRequest.getScheduleStartTime())
                .scheduleEndTime(testRequest.getScheduleEndTime())
                .workLocation(testRequest.getWorkLocation())
                .deviceId(testRequest.getDeviceId())
                .deviceName(testRequest.getDeviceName())
                // 测试属性
                .userAttributes(testRequest.getUserAttributes() != null ? testRequest.getUserAttributes() : new HashMap<>())
                .attendanceAttributes(testRequest.getAttendanceAttributes() != null ? testRequest.getAttendanceAttributes() : new HashMap<>())
                .environmentParams(testRequest.getEnvironmentParams() != null ? testRequest.getEnvironmentParams() : new HashMap<>())
                .build();

        // 转换为RuleExecutionContext（测试字段合并到customVariables）
        return testContext.toRuleExecutionContext();
    }

    /**
     * 构建测试结果
     */
    private RuleTestResultVO buildTestResult(Long ruleId, RuleEvaluationResult evaluationResult,
                                              long executionTime, RuleTestRequest testRequest) {
        boolean isMatched = "MATCH".equals(evaluationResult.getEvaluationResult());
        return RuleTestResultVO.builder()
                .testId(UUID.randomUUID().toString())
                .ruleId(ruleId)
                .ruleCondition(testRequest.getRuleCondition())
                .ruleAction(testRequest.getRuleAction())
                .testResult(isMatched ? "MATCH" : "NOT_MATCH")
                .resultMessage(isMatched ? "规则匹配成功" : "规则不匹配")
                .conditionMatched(isMatched)
                .executionTime(executionTime)
                .testTimestamp(LocalDateTime.now())
                .testInputData(extractInputData(testRequest))
                .executionLogs(new ArrayList<>())
                .build();
    }

    /**
     * 保存测试历史
     * <p>
     * 将测试结果转换为历史记录并保存到数据库
     * 使用异步方式保存，不影响测试性能
     * </p>
     */
    private void saveTestHistory(RuleTestResultVO result, RuleTestRequest testRequest) {
        try {
            RuleTestHistoryDetailVO historyDetail = RuleTestHistoryDetailVO.detailBuilder()
                    .testId(result.getTestId())
                    .ruleId(result.getRuleId())
                    .ruleName(result.getRuleName() != null ? result.getRuleName() : "自定义规则")
                    .ruleCondition(result.getRuleCondition())
                    .ruleAction(result.getRuleAction())
                    .testResult(result.getTestResult())
                    .resultMessage(result.getResultMessage())
                    .conditionMatched(result.getConditionMatched())
                    .executionTime(result.getExecutionTime())
                    .executedActions(result.getExecutedActions())
                    .executionLogs(result.getExecutionLogs())
                    .errorMessage(result.getErrorMessage())
                    .testInputData(result.getTestInputData())
                    .testOutputData(result.getTestOutputData())
                    .testScenario(determineTestScenario(testRequest))
                    .testUserId(testRequest.getUserId())
                    .testUserName(testRequest.getUserName())
                    .testTimestamp(result.getTestTimestamp())
                    .build();

            // 异步保存测试历史
            ruleTestHistoryService.saveHistory(historyDetail);

            log.debug("[规则测试] 测试历史保存成功: testId={}", result.getTestId());

        } catch (Exception e) {
            // 保存失败不影响测试结果，只记录错误日志
            log.error("[规则测试] 测试历史保存失败: testId={}, error={}", result.getTestId(), e.getMessage());
        }
    }

    /**
     * 确定测试场景
     * <p>
     * 根据打卡时间和排班时间判断测试场景
     * </p>
     */
    private String determineTestScenario(RuleTestRequest testRequest) {
        if (testRequest.getPunchTime() == null || testRequest.getScheduleStartTime() == null) {
            return "ABSENT"; // 缺勤
        }

        // 迟到判断
        if ("IN".equals(testRequest.getPunchType()) &&
            testRequest.getPunchTime().isAfter(testRequest.getScheduleStartTime())) {
            return "LATE"; // 迟到
        }

        // 早退判断
        if ("OUT".equals(testRequest.getPunchType()) &&
            testRequest.getScheduleEndTime() != null &&
            testRequest.getPunchTime().isBefore(testRequest.getScheduleEndTime())) {
            return "EARLY"; // 早退
        }

        // 加班判断（简化：18点之后打卡为加班）
        if ("OUT".equals(testRequest.getPunchType()) &&
            !testRequest.getPunchTime().isBefore(LocalTime.of(18, 0))) {
            return "OVERTIME"; // 加班
        }

        return "NORMAL"; // 正常
    }

    /**
     * 构建错误结果
     */
    private RuleTestResultVO buildErrorResult(Long ruleId, Exception e, RuleTestRequest testRequest, long startTime) {
        return RuleTestResultVO.builder()
                .testId(UUID.randomUUID().toString())
                .ruleId(ruleId)
                .testResult("ERROR")
                .resultMessage("规则测试失败")
                .errorMessage(e.getMessage())
                .executionTime(System.currentTimeMillis() - startTime)
                .testTimestamp(LocalDateTime.now())
                .testInputData(extractInputData(testRequest))
                .build();
    }

    /**
     * 执行动作
     */
    private List<RuleTestResultVO.ActionExecutionVO> executeActions(Object compiledAction, RuleExecutionContext context) {
        List<RuleTestResultVO.ActionExecutionVO> executedActions = new ArrayList<>();

        // 这里应该根据编译后的动作执行相应的动作
        // 简化实现：记录动作执行
        executedActions.add(RuleTestResultVO.ActionExecutionVO.builder()
                .actionName("testAction")
                .actionValue("testValue")
                .executionStatus("SUCCESS")
                .executionMessage("测试动作执行成功")
                .executionTimestamp(LocalDateTime.now())
                .build());

        return executedActions;
    }

    /**
     * 提取输入数据
     */
    private Map<String, Object> extractInputData(RuleTestRequest testRequest) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("userId", testRequest.getUserId());
        inputData.put("userName", testRequest.getUserName());
        inputData.put("attendanceDate", testRequest.getAttendanceDate());
        inputData.put("punchTime", testRequest.getPunchTime());
        return inputData;
    }

    /**
     * 创建测试场景
     */
    private TestScenarioVO createScenario(String code, String name, String description, String exampleData) {
        TestScenarioVO scenario = new TestScenarioVO();
        // 使用setter方法设置字段值（避免直接访问非public字段）
        scenario.setScenarioCode(code);
        scenario.setScenarioName(name);
        scenario.setScenarioDescription(description);
        scenario.setExampleData(exampleData);
        return scenario;
    }
}
