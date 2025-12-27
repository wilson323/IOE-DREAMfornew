package net.lab1024.sa.attendance.engine.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RuleTestContext 单元测试
 * <p>
 * 测试类型安全的测试上下文类、转换方法和手动Builder实现
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 */
@DisplayName("RuleTestContext 测试")
class RuleTestContextTest {

    @Test
    @DisplayName("测试使用testBuilder()构建所有字段")
    void testBuildTestContextWithAllFields() {
        // Given & When - 使用testBuilder()设置所有测试字段
        LocalTime testTime = LocalTime.of(8, 30);
        LocalDateTime testDateTime = LocalDateTime.now();
        Map<String, Object> userAttrs = Map.of("position", "工程师", "level", 5);
        Map<String, Object> attendanceAttrs = Map.of("lateMinutes", 5);
        Map<String, Object> envParams = Map.of("temperature", 25, "weather", "晴");

        RuleTestContext testContext = RuleTestContext.testBuilder()
                // 基础字段（继承自RuleExecutionContext）
                .employeeId(1001L)
                .employeeName("张三")
                .employeeNo("EMP001")
                .departmentId(10L)
                .departmentName("研发部")
                .scheduleDate(LocalDate.of(2025, 12, 27))
                .shiftId(1L)
                .shiftCode("SHIFT001")
                .shiftType(1)
                .consecutiveWorkDays(5)
                .restDays(2)
                .monthlyWorkDays(22)
                .monthlyWorkHours(176.0)
                .skills("Java,Python")
                // 测试专用字段
                .executionId("EXEC-TEST-001")
                .executionTimestamp(testDateTime)
                .executionMode("TEST")
                .punchTime(testTime)
                .punchType("IN")
                .scheduleStartTime(LocalTime.of(8, 0))
                .scheduleEndTime(LocalTime.of(17, 30))
                .workLocation("总部")
                .deviceId("DEV001")
                .deviceName("考勤机1号")
                .userAttributes(userAttrs)
                .attendanceAttributes(attendanceAttrs)
                .environmentParams(envParams)
                .build();

        // Then - 验证所有字段正确设置
        assertNotNull(testContext, "测试上下文对象不应为null");

        // 验证基础字段
        assertEquals(1001L, testContext.getEmployeeId());
        assertEquals("张三", testContext.getEmployeeName());
        assertEquals(10L, testContext.getDepartmentId());
        assertEquals(LocalDate.of(2025, 12, 27), testContext.getScheduleDate());

        // 验证测试专用字段
        assertEquals("EXEC-TEST-001", testContext.getExecutionId());
        assertEquals(testDateTime, testContext.getExecutionTimestamp());
        assertEquals("TEST", testContext.getExecutionMode());
        assertEquals(testTime, testContext.getPunchTime());
        assertEquals("IN", testContext.getPunchType());
        assertEquals(LocalTime.of(8, 0), testContext.getScheduleStartTime());
        assertEquals(LocalTime.of(17, 30), testContext.getScheduleEndTime());
        assertEquals("总部", testContext.getWorkLocation());
        assertEquals("DEV001", testContext.getDeviceId());
        assertEquals("考勤机1号", testContext.getDeviceName());

        // 验证Map类型字段
        assertEquals(userAttrs, testContext.getUserAttributes());
        assertEquals(attendanceAttrs, testContext.getAttendanceAttributes());
        assertEquals(envParams, testContext.getEnvironmentParams());
    }

    @Test
    @DisplayName("测试toRuleExecutionContext()转换方法")
    void testToRuleExecutionContextConversion() {
        // Given - 构建包含所有字段的RuleTestContext
        LocalTime punchTime = LocalTime.of(8, 35);
        Map<String, Object> userAttrs = Map.of("position", "工程师");
        Map<String, Object> attendanceAttrs = Map.of("lateMinutes", 5);

        RuleTestContext testContext = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .employeeName("张三")
                .departmentId(10L)
                .scheduleDate(LocalDate.of(2025, 12, 27))
                .executionId("EXEC-TEST-001")
                .punchTime(punchTime)
                .punchType("IN")
                .scheduleStartTime(LocalTime.of(8, 0))
                .scheduleEndTime(LocalTime.of(17, 30))
                .deviceId("DEV001")
                .deviceName("考勤机1号")
                .userAttributes(userAttrs)
                .attendanceAttributes(attendanceAttrs)
                .environmentParams(new HashMap<>())
                .build();

        // When - 转换为RuleExecutionContext
        RuleExecutionContext context = testContext.toRuleExecutionContext();

        // Then - 验证基础字段正确保留
        assertNotNull(context, "转换后的上下文不应为null");
        assertEquals(1001L, context.getEmployeeId(), "员工ID应保留");
        assertEquals("张三", context.getEmployeeName(), "员工姓名应保留");
        assertEquals(10L, context.getDepartmentId(), "部门ID应保留");
        assertEquals(LocalDate.of(2025, 12, 27), context.getScheduleDate(), "排班日期应保留");

        // 验证测试字段已合并到customVariables
        Map<String, Object> customVars = context.getCustomVariables();
        assertNotNull(customVars, "customVariables不应为null");
        assertEquals("EXEC-TEST-001", customVars.get("executionId"), "executionId应在customVariables中");
        assertEquals(punchTime, customVars.get("punchTime"), "punchTime应在customVariables中");
        assertEquals("IN", customVars.get("punchType"), "punchType应在customVariables中");
        assertEquals(LocalTime.of(8, 0), customVars.get("scheduleStartTime"), "scheduleStartTime应在customVariables中");
        assertEquals(LocalTime.of(17, 30), customVars.get("scheduleEndTime"), "scheduleEndTime应在customVariables中");
        assertEquals("DEV001", customVars.get("deviceId"), "deviceId应在customVariables中");
        assertEquals("考勤机1号", customVars.get("deviceName"), "deviceName应在customVariables中");

        // 验证Map类型字段已合并
        assertEquals(userAttrs, customVars.get("userAttributes"), "userAttributes应在customVariables中");
        assertEquals(attendanceAttrs, customVars.get("attendanceAttributes"), "attendanceAttributes应在customVariables中");
    }

    @Test
    @DisplayName("测试customVariables合并逻辑")
    void testCustomVariablesMerge() {
        // Given - RuleTestContext有customVariables
        RuleTestContext testContext = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .scheduleDate(LocalDate.now())
                .executionId("EXEC-001")
                .punchTime(LocalTime.of(8, 30))
                .build();

        // 向父类的customVariables添加一些数据
        testContext.getCustomVariables().put("parentKey", "parentValue");

        // When - 转换为RuleExecutionContext
        RuleExecutionContext context = testContext.toRuleExecutionContext();

        // Then - 验证customVariables合并正确
        Map<String, Object> customVars = context.getCustomVariables();
        assertNotNull(customVars, "customVariables不应为null");

        // 父类的customVariables数据应保留
        assertEquals("parentValue", customVars.get("parentKey"), "父类的customVariables应保留");

        // RuleTestContext的测试字段应添加到customVariables
        assertEquals("EXEC-001", customVars.get("executionId"), "测试字段应添加到customVariables");
        assertEquals(LocalTime.of(8, 30), customVars.get("punchTime"), "测试字段应添加到customVariables");

        // 验证total size
        assertEquals(3, customVars.size(), "customVariables应包含父类数据和测试数据");
    }

    @Test
    @DisplayName("测试null值字段不合并到customVariables")
    void testNullFieldsNotMerged() {
        // Given - RuleTestContext有部分null字段
        RuleTestContext testContext = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .scheduleDate(LocalDate.now())
                .executionId("EXEC-001")
                .punchTime(LocalTime.of(8, 30))
                // punchType、scheduleStartTime等字段不设置（保持null）
                .build();

        // When - 转换为RuleExecutionContext
        RuleExecutionContext context = testContext.toRuleExecutionContext();

        // Then - 验证null字段不会添加到customVariables
        Map<String, Object> customVars = context.getCustomVariables();
        assertNotNull(customVars, "customVariables不应为null");

        // 非null字段应存在
        assertEquals("EXEC-001", customVars.get("executionId"));
        assertEquals(LocalTime.of(8, 30), customVars.get("punchTime"));

        // null字段不应存在
        assertFalse(customVars.containsKey("punchType"), "null的punchType不应添加到customVariables");
        assertFalse(customVars.containsKey("scheduleStartTime"), "null的scheduleStartTime不应添加到customVariables");
    }

    @Test
    @DisplayName("测试类型安全（无需强制类型转换）")
    void testTypeSafety() {
        // Given - 使用强类型字段构建RuleTestContext
        LocalTime expectedPunchTime = LocalTime.of(8, 30, 15);
        Integer expectedMonthlyWorkDays = 22;

        RuleTestContext testContext = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .punchTime(expectedPunchTime)
                .scheduleDate(LocalDate.now())
                .monthlyWorkDays(expectedMonthlyWorkDays)
                .build();

        // When - 直接访问字段（无需类型转换）
        LocalTime actualPunchTime = testContext.getPunchTime();
        Integer actualMonthlyWorkDays = testContext.getMonthlyWorkDays();

        // Then - 验证类型安全，无需强制转换
        assertEquals(expectedPunchTime, actualPunchTime, "punchTime类型应正确");
        assertEquals(expectedMonthlyWorkDays, actualMonthlyWorkDays, "monthlyWorkDays类型应正确");

        // 验证类型确实是LocalTime和Integer（不是String或其他类型）
        assertInstanceOf(LocalTime.class, actualPunchTime, "punchTime应为LocalTime类型");
        assertInstanceOf(Integer.class, actualMonthlyWorkDays, "monthlyWorkDays应为Integer类型");
    }

    @Test
    @DisplayName("测试空Map字段的默认值")
    void testEmptyMapDefaultValues() {
        // Given & When - 使用testBuilder()但不设置Map字段
        RuleTestContext testContext = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .scheduleDate(LocalDate.now())
                .build();

        // Then - 验证Map字段初始化为空HashMap（而非null）
        assertNotNull(testContext.getUserAttributes(), "userAttributes应初始化为空Map");
        assertNotNull(testContext.getAttendanceAttributes(), "attendanceAttributes应初始化为空Map");
        assertNotNull(testContext.getEnvironmentParams(), "environmentParams应初始化为空Map");

        assertTrue(testContext.getUserAttributes().isEmpty(), "userAttributes应为空");
        assertTrue(testContext.getAttendanceAttributes().isEmpty(), "attendanceAttributes应为空");
        assertTrue(testContext.getEnvironmentParams().isEmpty(), "environmentParams应为空");
    }

    @Test
    @DisplayName("测试继承的getVariables()方法包含测试字段")
    void testGetVariables_includesTestFields() {
        // Given - 构建RuleTestContext并添加测试字段
        RuleTestContext testContext = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .employeeName("张三")
                .scheduleDate(LocalDate.of(2025, 12, 27))
                .executionId("EXEC-001")
                .punchTime(LocalTime.of(8, 30))
                .punchType("IN")
                .build();

        // When - 调用继承的getVariables()方法
        Map<String, Object> variables = testContext.getVariables();

        // Then - 验证只包含基础字段（测试字段不在getVariables()中）
        assertEquals(1001L, variables.get("employeeId"), "应包含基础字段employeeId");
        assertEquals("张三", variables.get("employeeName"), "应包含基础字段employeeName");

        // 测试字段不会自动出现在getVariables()中（需要通过toRuleExecutionContext()转换）
        assertFalse(variables.containsKey("executionId"),
                "executionId不应在getVariables()中（需转换后通过customVariables访问）");
        assertFalse(variables.containsKey("punchTime"),
                "punchTime不应在getVariables()中（需转换后通过customVariables访问）");
    }

    @Test
    @DisplayName("测试toRuleExecutionContext()后getVariables()包含测试字段")
    void testGetVariables_afterConversion() {
        // Given - 构建RuleTestContext并转换
        RuleTestContext testContext = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .scheduleDate(LocalDate.of(2025, 12, 27))
                .executionId("EXEC-001")
                .punchTime(LocalTime.of(8, 30))
                .punchType("IN")
                .build();

        RuleExecutionContext context = testContext.toRuleExecutionContext();

        // When - 调用getVariables()
        Map<String, Object> variables = context.getVariables();

        // Then - 验证测试字段现在在customVariables中，因此也在getVariables()中
        assertTrue(variables.containsKey("executionId"), "转换后executionId应在variables中");
        assertTrue(variables.containsKey("punchTime"), "转换后punchTime应在variables中");
        assertTrue(variables.containsKey("punchType"), "转换后punchType应在variables中");
    }

    @Test
    @DisplayName("测试@NoArgsConstructor创建空对象")
    void testNoArgsConstructor() {
        // When - 使用无参构造函数
        RuleTestContext testContext = new RuleTestContext();

        // Then - 验证对象创建成功且Map字段初始化
        assertNotNull(testContext, "测试上下文对象不应为null");
        assertNull(testContext.getExecutionId(), "executionId应为null");
        assertNull(testContext.getPunchTime(), "punchTime应为null");

        // Map字段应初始化为空HashMap
        assertNotNull(testContext.getUserAttributes(), "userAttributes应初始化");
        assertNotNull(testContext.getAttendanceAttributes(), "attendanceAttributes应初始化");
        assertNotNull(testContext.getEnvironmentParams(), "environmentParams应初始化");
    }

    @Test
    @DisplayName("测试手动Builder链式调用")
    void testManualBuilderChaining() {
        // Given & When - 验证Builder方法返回Builder实例（支持链式调用）
        RuleTestContext.Builder builder = RuleTestContext.testBuilder()
                .employeeId(1001L)
                .employeeName("张三")
                .punchTime(LocalTime.of(8, 30));

        // Then - 验证可以继续链式调用
        assertNotNull(builder, "Builder实例不应为null");

        RuleTestContext context = builder
                .scheduleDate(LocalDate.now())
                .executionId("EXEC-001")
                .build();

        assertNotNull(context, "构建的上下文对象不应为null");
        assertEquals(1001L, context.getEmployeeId());
        assertEquals("张三", context.getEmployeeName());
        assertEquals(LocalTime.of(8, 30), context.getPunchTime());
        assertEquals(LocalDate.now(), context.getScheduleDate());
        assertEquals("EXEC-001", context.getExecutionId());
    }
}
