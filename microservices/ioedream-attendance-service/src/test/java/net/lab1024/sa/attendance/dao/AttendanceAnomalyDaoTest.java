package net.lab1024.sa.attendance.dao;

import net.lab1024.sa.attendance.config.EnhancedTestConfiguration;
import net.lab1024.sa.attendance.config.IntegrationTestConfiguration;
import org.springframework.context.annotation.Import;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.entity.attendance.AttendanceAnomalyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 考勤异常DAO集成测试
 *
 * 测试范围：
 * 1. 基础CRUD操作（继承自BaseMapper）
 * 2. 自定义查询方法
 * 3. 分页查询
 * 4. 复杂条件查询
 * 5. 统计查询
 *
 * 注意：这是集成测试，需要真实的数据库环境
 *
 * @TestData:
 * - 班次数据: 3个班次
 * - 考勤规则: 2个规则配置
 * - 考勤记录: 6条记录
 * - 异常记录: 2条记录（缺卡、旷工）
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@Import(EnhancedTestConfiguration.class)
@ActiveProfiles("h2-test")
@Transactional
@Sql(scripts = "/sql/00-test-schema.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sql/01-test-basic-data.sql",
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("考勤异常DAO集成测试")
class AttendanceAnomalyDaoTest {

    @Autowired
    private AttendanceAnomalyDao anomalyDao;

    private AttendanceAnomalyEntity testAnomaly;

    @BeforeEach
    void setUp() {
        // 创建测试异常记录
        testAnomaly = new AttendanceAnomalyEntity();
        testAnomaly.setUserId(1001L);
        testAnomaly.setUserName("测试用户");
        testAnomaly.setDepartmentId(1L);
        testAnomaly.setDepartmentName("测试部门");
        testAnomaly.setShiftId(1L);
        testAnomaly.setShiftName("正常班");
        testAnomaly.setAttendanceDate(LocalDate.of(2025, 1, 30));
        testAnomaly.setAnomalyType("LATE");
        testAnomaly.setSeverityLevel("NORMAL");
        testAnomaly.setAnomalyStatus("PENDING");
        testAnomaly.setExpectedPunchTime(LocalDateTime.of(2025, 1, 30, 9, 0, 0));
        testAnomaly.setActualPunchTime(LocalDateTime.of(2025, 1, 30, 9, 10, 0));
        testAnomaly.setPunchType("CHECK_IN");
        testAnomaly.setAnomalyDuration(10);
        testAnomaly.setAnomalyReason("迟到超过5分钟");
    }

    // ==================== 基础CRUD测试 ====================

    @Test
    @DisplayName("DAO测试：插入异常记录 - 成功")
    void testInsert_Success() {
        System.out.println("[DAO测试] 测试插入异常记录");

        // When
        int result = anomalyDao.insert(testAnomaly);

        // Then
        assertTrue(result > 0, "插入应成功");
        assertNotNull(testAnomaly.getAnomalyId(), "应自动生成ID");

        System.out.println("[DAO测试] ✅ 插入异常记录测试通过");
    }

    @Test
    @DisplayName("DAO测试：根据ID查询 - 成功")
    void testSelectById_Success() {
        System.out.println("[DAO测试] 测试根据ID查询");

        // Given: 先插入
        anomalyDao.insert(testAnomaly);
        Long anomalyId = testAnomaly.getAnomalyId();

        // When
        AttendanceAnomalyEntity result = anomalyDao.selectById(anomalyId);

        // Then
        assertNotNull(result);
        assertEquals("测试用户", result.getUserName());
        assertEquals("LATE", result.getAnomalyType());

        System.out.println("[DAO测试] ✅ 根据ID查询测试通过");
    }

    @Test
    @DisplayName("DAO测试：更新异常记录 - 成功")
    void testUpdateById_Success() {
        System.out.println("[DAO测试] 测试更新异常记录");

        // Given: 先插入
        anomalyDao.insert(testAnomaly);
        Long anomalyId = testAnomaly.getAnomalyId();

        // When: 更新状态
        testAnomaly.setAnomalyStatus("APPROVED");
        testAnomaly.setHandlerId(1002L);
        testAnomaly.setHandlerName("管理员");
        testAnomaly.setHandleTime(LocalDateTime.now());
        testAnomaly.setHandleComment("批准");

        int result = anomalyDao.updateById(testAnomaly);

        // Then
        assertTrue(result > 0);

        // 验证更新
        AttendanceAnomalyEntity updated = anomalyDao.selectById(anomalyId);
        assertEquals("APPROVED", updated.getAnomalyStatus());
        assertEquals("管理员", updated.getHandlerName());

        System.out.println("[DAO测试] ✅ 更新异常记录测试通过");
    }

    @Test
    @DisplayName("DAO测试：删除异常记录 - 成功")
    void testDeleteById_Success() {
        System.out.println("[DAO测试] 测试删除异常记录");

        // Given: 先插入
        anomalyDao.insert(testAnomaly);
        Long anomalyId = testAnomaly.getAnomalyId();

        // When
        int result = anomalyDao.deleteById(anomalyId);

        // Then
        assertTrue(result > 0);

        // 验证删除
        AttendanceAnomalyEntity deleted = anomalyDao.selectById(anomalyId);
        assertNull(deleted);

        System.out.println("[DAO测试] ✅ 删除异常记录测试通过");
    }

    // ==================== 自定义查询方法测试 ====================

    @Test
    @DisplayName("DAO测试：根据用户ID和日期查询 - 成功")
    void testSelectByUserIdAndDate_Success() {
        System.out.println("[DAO测试] 测试根据用户ID和日期查询");

        // Given: 插入测试数据
        anomalyDao.insert(testAnomaly);

        // When
        List<AttendanceAnomalyEntity> result = anomalyDao.selectByUserIdAndDate(
                1001L,
                LocalDate.of(2025, 1, 30)
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("测试用户", result.get(0).getUserName());

        System.out.println("[DAO测试] ✅ 根据用户ID和日期查询测试通过");
    }

    @Test
    @DisplayName("DAO测试：根据状态查询 - 成功")
    void testSelectByStatus_Success() {
        System.out.println("[DAO测试] 测试根据状态查询");

        // Given: 插入不同状态的记录
        testAnomaly.setAnomalyStatus("PENDING");
        anomalyDao.insert(testAnomaly);

        AttendanceAnomalyEntity approvedAnomaly = createTestAnomaly();
        approvedAnomaly.setAnomalyStatus("APPROVED");
        anomalyDao.insert(approvedAnomaly);

        // When
        List<AttendanceAnomalyEntity> pendingList = anomalyDao.selectByStatus("PENDING");
        List<AttendanceAnomalyEntity> approvedList = anomalyDao.selectByStatus("APPROVED");

        // Then
        assertNotNull(pendingList);
        assertNotNull(approvedList);
        assertTrue(pendingList.stream().allMatch(a -> "PENDING".equals(a.getAnomalyStatus())));
        assertTrue(approvedList.stream().allMatch(a -> "APPROVED".equals(a.getAnomalyStatus())));

        System.out.println("[DAO测试] ✅ 根据状态查询测试通过");
    }

    @Test
    @DisplayName("DAO测试：根据部门和日期范围查询 - 成功")
    void testSelectByDepartmentAndDateRange_Success() {
        System.out.println("[DAO测试] 测试根据部门和日期范围查询");

        // Given: 插入测试数据
        anomalyDao.insert(testAnomaly);

        // When
        List<AttendanceAnomalyEntity> result = anomalyDao.selectByDepartmentAndDateRange(
                1L,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 1);

        System.out.println("[DAO测试] ✅ 根据部门和日期范围查询测试通过");
    }

    @Test
    @DisplayName("DAO测试：统计指定日期的异常数量 - 成功")
    void testStatisticsByDate_Success() {
        System.out.println("[DAO测试] 测试统计指定日期的异常数量");

        // Given: 插入多条记录
        anomalyDao.insert(testAnomaly);

        AttendanceAnomalyEntity earlyAnomaly = createTestAnomaly();
        earlyAnomaly.setAnomalyType("EARLY");
        anomalyDao.insert(earlyAnomaly);

        // When
        List<AttendanceAnomalyEntity> statistics = anomalyDao.statisticsByDate(
                LocalDate.of(2025, 1, 30)
        );

        // Then
        assertNotNull(statistics);
        assertTrue(statistics.size() >= 2);

        System.out.println("[DAO测试] ✅ 统计指定日期的异常数量测试通过");
    }

    @Test
    @DisplayName("DAO测试：查询用户异常统计 - 成功")
    void testSelectUserAnomalyStatistics_Success() {
        System.out.println("[DAO测试] 测试查询用户异常统计");

        // Given: 插入测试数据
        anomalyDao.insert(testAnomaly);

        // When
        List<AttendanceAnomalyEntity> statistics = anomalyDao.selectUserAnomalyStatistics(
                1001L,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        // Then
        assertNotNull(statistics);
        assertTrue(statistics.size() >= 1);

        System.out.println("[DAO测试] ✅ 查询用户异常统计测试通过");
    }

    // ==================== 分页查询测试 ====================

    @Test
    @DisplayName("DAO测试：分页查询 - 成功")
    void testSelectPage_Success() {
        System.out.println("[DAO测试] 测试分页查询");

        // Given: 插入多条记录
        for (int i = 0; i < 25; i++) {
            AttendanceAnomalyEntity anomaly = createTestAnomaly();
            anomaly.setUserId(2000L + i);
            anomaly.setUserName("测试用户" + i);
            anomalyDao.insert(anomaly);
        }

        // When: 分页查询
        Page<AttendanceAnomalyEntity> page = new Page<>(1, 10);
        Page<AttendanceAnomalyEntity> result = anomalyDao.selectPage(page, null);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getRecords().size());
        assertTrue(result.getTotal() >= 25);

        System.out.println("[DAO测试] ✅ 分页查询测试通过");
    }

    // ==================== LambdaQueryWrapper测试 ====================

    @Test
    @DisplayName("DAO测试：Lambda条件查询 - 成功")
    void testLambdaQueryWrapper_Success() {
        System.out.println("[DAO测试] 测试Lambda条件查询");

        // Given: 插入测试数据
        anomalyDao.insert(testAnomaly);

        // When: 使用LambdaQueryWrapper查询
        LambdaQueryWrapper<AttendanceAnomalyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceAnomalyEntity::getUserId, 1001L)
                .eq(AttendanceAnomalyEntity::getAnomalyType, "LATE")
                .eq(AttendanceAnomalyEntity::getAnomalyStatus, "PENDING");

        List<AttendanceAnomalyEntity> result = anomalyDao.selectList(queryWrapper);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().allMatch(a ->
                a.getUserId().equals(1001L) &&
                        "LATE".equals(a.getAnomalyType()) &&
                        "PENDING".equals(a.getAnomalyStatus())
        ));

        System.out.println("[DAO测试] ✅ Lambda条件查询测试通过");
    }

    @Test
    @DisplayName("DAO测试：复杂多条件查询 - 成功")
    void testComplexQuery_Success() {
        System.out.println("[DAO测试] 测试复杂多条件查询");

        // Given: 插入测试数据
        for (int i = 0; i < 10; i++) {
            AttendanceAnomalyEntity anomaly = createTestAnomaly();
            anomaly.setUserId(3000L + i);
            anomaly.setAnomalyType(i % 2 == 0 ? "LATE" : "EARLY");
            anomaly.setSeverityLevel(i % 3 == 0 ? "SERIOUS" : "NORMAL");
            anomalyDao.insert(anomaly);
        }

        // When: 复杂查询条件
        LambdaQueryWrapper<AttendanceAnomalyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceAnomalyEntity::getDepartmentId, 1L)
                .in(AttendanceAnomalyEntity::getAnomalyType, Arrays.asList("LATE", "EARLY"))
                .ge(AttendanceAnomalyEntity::getAnomalyDuration, 5)
                .eq(AttendanceAnomalyEntity::getAnomalyStatus, "PENDING")
                .orderByDesc(AttendanceAnomalyEntity::getAnomalyDuration);

        List<AttendanceAnomalyEntity> result = anomalyDao.selectList(queryWrapper);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 10);
        // 验证排序
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getAnomalyDuration() >= result.get(i + 1).getAnomalyDuration());
        }

        System.out.println("[DAO测试] ✅ 复杂多条件查询测试通过");
    }

    // ==================== 批量操作测试 ====================

    @Test
    @DisplayName("DAO测试：批量插入 - 成功")
    void testInsertBatch_Success() {
        System.out.println("[DAO测试] 测试批量插入");

        // Given: 准备批量数据
        List<AttendanceAnomalyEntity> batchList = Arrays.asList(
                createTestAnomaly(),
                createTestAnomaly(),
                createTestAnomaly()
        );

        // When
        int totalInserted = 0;
        for (AttendanceAnomalyEntity anomaly : batchList) {
            totalInserted += anomalyDao.insert(anomaly);
        }

        // Then
        assertEquals(3, totalInserted);

        System.out.println("[DAO测试] ✅ 批量插入测试通过");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试异常记录
     */
    private AttendanceAnomalyEntity createTestAnomaly() {
        AttendanceAnomalyEntity anomaly = new AttendanceAnomalyEntity();
        anomaly.setUserId(1001L);
        anomaly.setUserName("测试用户");
        anomaly.setDepartmentId(1L);
        anomaly.setDepartmentName("测试部门");
        anomaly.setShiftId(1L);
        anomaly.setShiftName("正常班");
        anomaly.setAttendanceDate(LocalDate.of(2025, 1, 30));
        anomaly.setAnomalyType("LATE");
        anomaly.setSeverityLevel("NORMAL");
        anomaly.setAnomalyStatus("PENDING");
        anomaly.setExpectedPunchTime(LocalDateTime.of(2025, 1, 30, 9, 0, 0));
        anomaly.setActualPunchTime(LocalDateTime.of(2025, 1, 30, 9, 10, 0));
        anomaly.setPunchType("CHECK_IN");
        anomaly.setAnomalyDuration(10);
        anomaly.setAnomalyReason("迟到超过5分钟");
        return anomaly;
    }
}
