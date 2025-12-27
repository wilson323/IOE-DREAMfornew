package net.lab1024.sa.attendance.engine.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RuleExecutionContext 单元测试
 * <p>
 * 测试Lombok @Builder生成的构建器、默认值、别名方法和getVariables()方法
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 */
@DisplayName("RuleExecutionContext 测试")
class RuleExecutionContextTest {

    @Test
    @DisplayName("测试使用Builder构建所有字段")
    void testBuildContextWithAllFields() {
        // Given & When - 使用Builder设置所有字段
        RuleExecutionContext context = RuleExecutionContext.builder()
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
                .executionId("EXEC-12345")
                .sessionId("SESSION-67890")
                .build();

        // Then - 验证所有字段正确设置
        assertNotNull(context, "上下文对象不应为null");
        assertEquals(1001L, context.getEmployeeId(), "员工ID应正确设置");
        assertEquals("张三", context.getEmployeeName(), "员工姓名应正确设置");
        assertEquals("EMP001", context.getEmployeeNo(), "员工工号应正确设置");
        assertEquals(10L, context.getDepartmentId(), "部门ID应正确设置");
        assertEquals("研发部", context.getDepartmentName(), "部门名称应正确设置");
        assertEquals(LocalDate.of(2025, 12, 27), context.getScheduleDate(), "排班日期应正确设置");
        assertEquals(1L, context.getShiftId(), "班次ID应正确设置");
        assertEquals("SHIFT001", context.getShiftCode(), "班次编码应正确设置");
        assertEquals(1, context.getShiftType(), "班次类型应正确设置");
        assertEquals(5, context.getConsecutiveWorkDays(), "连续工作天数应正确设置");
        assertEquals(2, context.getRestDays(), "休息天数应正确设置");
        assertEquals(22, context.getMonthlyWorkDays(), "当月工作天数应正确设置");
        assertEquals(176.0, context.getMonthlyWorkHours(), "当月工作小时应正确设置");
        assertEquals("Java,Python", context.getSkills(), "技能应正确设置");
        assertEquals("EXEC-12345", context.getExecutionId(), "执行ID应正确设置");
        assertEquals("SESSION-67890", context.getSessionId(), "会话ID应正确设置");
    }

    @Test
    @DisplayName("测试Builder的默认值（customVariables应为空HashMap）")
    void testBuildContextWithDefaultValues() {
        // Given & When - 使用Builder构建但不设置customVariables
        RuleExecutionContext context = RuleExecutionContext.builder()
                .employeeId(1001L)
                .scheduleDate(LocalDate.now())
                .build();

        // Then - 验证customVariables默认为空HashMap（@Builder.Default生效）
        assertNotNull(context, "上下文对象不应为null");
        assertNotNull(context.getCustomVariables(), "customVariables不应为null");
        assertTrue(context.getCustomVariables().isEmpty(), "customVariables应为空Map");
    }

    @Test
    @DisplayName("测试@Builder.Default注解确保customVariables独立")
    void testBuilderDefaultIndependence() {
        // Given - 构建两个独立的上下文对象
        RuleExecutionContext context1 = RuleExecutionContext.builder()
                .employeeId(1001L)
                .scheduleDate(LocalDate.now())
                .build();

        RuleExecutionContext context2 = RuleExecutionContext.builder()
                .employeeId(1002L)
                .scheduleDate(LocalDate.now())
                .build();

        // When - 向context1的customVariables添加数据
        context1.getCustomVariables().put("testKey", "testValue");

        // Then - context2的customVariables应保持独立（不受影响）
        assertTrue(context2.getCustomVariables().isEmpty(),
                "context2的customVariables应为空，证明@Builder.Default创建了独立实例");
        assertEquals(1, context1.getCustomVariables().size(),
                "context1的customVariables应包含1个元素");
    }

    @Test
    @DisplayName("测试别名方法getUserId()和setUserId()")
    void testAliasMethods_getUserId_setUserId() {
        // Given & When - 使用setUserId()设置ID
        RuleExecutionContext context = RuleExecutionContext.builder()
                .scheduleDate(LocalDate.now())
                .build();
        context.setUserId(1001L);

        // Then - 验证getUserId()返回正确的employeeId值
        assertEquals(1001L, context.getUserId(), "getUserId()应返回employeeId的值");
        assertEquals(1001L, context.getEmployeeId(), "employeeId应与userId一致");
    }

    @Test
    @DisplayName("测试别名方法getAttendanceDate()和setAttendanceDate()")
    void testAliasMethods_getAttendanceDate_setAttendanceDate() {
        // Given
        LocalDate testDate = LocalDate.of(2025, 12, 27);

        // When - 使用setAttendanceDate()设置日期
        RuleExecutionContext context = RuleExecutionContext.builder()
                .build();
        context.setAttendanceDate(testDate);

        // Then - 验证getAttendanceDate()返回正确的scheduleDate值
        assertEquals(testDate, context.getAttendanceDate(),
                "getAttendanceDate()应返回scheduleDate的值");
        assertEquals(testDate, context.getScheduleDate(),
                "scheduleDate应与attendanceDate一致");
    }

    @Test
    @DisplayName("测试getVariables()方法包含所有字段")
    void testGetVariables() {
        // Given - 构建包含所有字段的上下文
        LocalDate testDate = LocalDate.of(2025, 12, 27);
        RuleExecutionContext context = RuleExecutionContext.builder()
                .employeeId(1001L)
                .employeeName("张三")
                .employeeNo("EMP001")
                .departmentId(10L)
                .departmentName("研发部")
                .scheduleDate(testDate)
                .shiftId(1L)
                .shiftCode("SHIFT001")
                .shiftType(1)
                .consecutiveWorkDays(5)
                .restDays(2)
                .monthlyWorkDays(22)
                .monthlyWorkHours(176.0)
                .skills("Java,Python")
                .build();

        // When - 调用getVariables()
        Map<String, Object> variables = context.getVariables();

        // Then - 验证所有字段都包含在返回的Map中
        assertNotNull(variables, "variables Map不应为null");

        // 员工信息
        assertEquals(1001L, variables.get("employeeId"), "应包含employeeId");
        assertEquals("张三", variables.get("employeeName"), "应包含employeeName");
        assertEquals("EMP001", variables.get("employeeNo"), "应包含employeeNo");

        // 部门信息
        assertEquals(10L, variables.get("departmentId"), "应包含departmentId");
        assertEquals("研发部", variables.get("departmentName"), "应包含departmentName");

        // 日期信息
        assertEquals(testDate, variables.get("scheduleDate"), "应包含scheduleDate");
        assertEquals(2025, variables.get("year"), "应包含year");
        assertEquals(12, variables.get("month"), "应包含month");
        assertEquals(27, variables.get("day"), "应包含day");
        // 2025-12-27是星期六（dayOfWeek=6）
        assertEquals(6, variables.get("dayOfWeek"), "应包含dayOfWeek（2025-12-27是星期六）");

        // 班次信息
        assertEquals(1L, variables.get("shiftId"), "应包含shiftId");
        assertEquals("SHIFT001", variables.get("shiftCode"), "应包含shiftCode");
        assertEquals(1, variables.get("shiftType"), "应包含shiftType");

        // 统计信息
        assertEquals(5, variables.get("consecutiveWorkDays"), "应包含consecutiveWorkDays");
        assertEquals(2, variables.get("restDays"), "应包含restDays");
        assertEquals(22, variables.get("monthlyWorkDays"), "应包含monthlyWorkDays");
        assertEquals(176.0, variables.get("monthlyWorkHours"), "应包含monthlyWorkHours");

        // 技能信息
        assertEquals("Java,Python", variables.get("skills"), "应包含skills");
    }

    @Test
    @DisplayName("测试getVariables()方法包含customVariables")
    void testGetVariables_withCustomVariables() {
        // Given - 构建上下文并添加customVariables
        RuleExecutionContext context = RuleExecutionContext.builder()
                .employeeId(1001L)
                .scheduleDate(LocalDate.now())
                .build();

        context.getCustomVariables().put("customKey1", "customValue1");
        context.getCustomVariables().put("customKey2", 123);

        // When - 调用getVariables()
        Map<String, Object> variables = context.getVariables();

        // Then - 验证customVariables中的数据也包含在返回的Map中
        assertNotNull(variables, "variables Map不应为null");
        assertEquals("customValue1", variables.get("customKey1"), "应包含customKey1");
        assertEquals(123, variables.get("customKey2"), "应包含customKey2");
        assertEquals(1001L, variables.get("employeeId"), "应包含基础字段employeeId");
    }

    @Test
    @DisplayName("测试@NoArgsConstructor创建空对象")
    void testNoArgsConstructor() {
        // When - 使用无参构造函数
        RuleExecutionContext context = new RuleExecutionContext();

        // Then - 验证对象创建成功且所有字段为null（除了customVariables）
        assertNotNull(context, "上下文对象不应为null");
        assertNull(context.getEmployeeId(), "employeeId应为null");
        assertNull(context.getEmployeeName(), "employeeName应为null");
        assertNull(context.getScheduleDate(), "scheduleDate应为null");
        assertNotNull(context.getCustomVariables(), "customVariables应初始化为空Map");
        assertTrue(context.getCustomVariables().isEmpty(), "customVariables应为空");
    }

    @Test
    @DisplayName("测试@AllArgsConstructor创建完整对象")
    void testAllArgsConstructor() {
        // Given - 准备所有字段值
        LocalDate testDate = LocalDate.of(2025, 12, 27);
        Map<String, Object> customVars = Map.of("key1", "value1");

        // When - 使用全参构造函数
        RuleExecutionContext context = new RuleExecutionContext(
                1001L, "张三", "EMP001",
                10L, "研发部",
                testDate,
                1L, "SHIFT001", 1,
                5, 2, 22, 176.0,
                "Java,Python",
                customVars,
                "EXEC-12345", "SESSION-67890"
        );

        // Then - 验证所有字段正确设置
        assertEquals(1001L, context.getEmployeeId());
        assertEquals("张三", context.getEmployeeName());
        assertEquals("EMP001", context.getEmployeeNo());
        assertEquals(10L, context.getDepartmentId());
        assertEquals("研发部", context.getDepartmentName());
        assertEquals(testDate, context.getScheduleDate());
        assertEquals(1L, context.getShiftId());
        assertEquals("SHIFT001", context.getShiftCode());
        assertEquals(1, context.getShiftType());
        assertEquals(5, context.getConsecutiveWorkDays());
        assertEquals(2, context.getRestDays());
        assertEquals(22, context.getMonthlyWorkDays());
        assertEquals(176.0, context.getMonthlyWorkHours());
        assertEquals("Java,Python", context.getSkills());
        assertEquals(customVars, context.getCustomVariables());
        assertEquals("EXEC-12345", context.getExecutionId());
        assertEquals("SESSION-67890", context.getSessionId());
    }
}
