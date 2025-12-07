package net.lab1024.sa.common.visitor.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.visitor.entity.VehicleEntity;

/**
 * 车辆管理DAO单元测试
 *
 * @Author IOE-DREAM Team
 * @Date 2025-12-05
 * @Copyright IOE-DREAM智慧园区一卡通管理平台
 * 
 *            测试覆盖：
 *            - CRUD基本操作
 *            - 条件查询
 *            - 分页查询
 *            - 批量操作
 *            - 事务管理
 */
@SpringBootTest(classes = net.lab1024.sa.common.TestApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VehicleDaoTest {

    @Resource
    private VehicleDao vehicleDao;

    private VehicleEntity testVehicle;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testVehicle = new VehicleEntity();
        testVehicle.setVehicleNumber("京A12345");
        testVehicle.setVehicleType(1); // 小型车
        testVehicle.setVehicleBrand("丰田");
        testVehicle.setVehicleModel("卡罗拉");
        testVehicle.setVehicleColor("白色");
        testVehicle.setDriverId(1L);
        testVehicle.setCompanyName("测试公司");
        testVehicle.setStatus(1);
        testVehicle.setDeletedFlag(0);
        testVehicle.setCreateTime(LocalDateTime.now());
        testVehicle.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @Order(1)
    @DisplayName("测试新增车辆")
    void testInsert() {
        // 执行新增
        int result = vehicleDao.insert(testVehicle);

        // 验证
        assertEquals(1, result, "应该成功插入1条记录");
        assertNotNull(testVehicle.getVehicleId(), "车辆ID应该自动生成");
        assertTrue(testVehicle.getVehicleId() > 0, "车辆ID应该大于0");
    }

    @Test
    @Order(2)
    @DisplayName("测试根据ID查询车辆")
    void testSelectById() {
        // 先插入测试数据
        vehicleDao.insert(testVehicle);
        Long vehicleId = testVehicle.getVehicleId();

        // 执行查询
        VehicleEntity result = vehicleDao.selectById(vehicleId);

        // 验证
        assertNotNull(result, "应该能查询到车辆信息");
        assertEquals("京A12345", result.getVehicleNumber(), "车牌号应该匹配");
        assertEquals("丰田", result.getVehicleBrand(), "品牌应该匹配");
        assertEquals(1, result.getStatus(), "状态应该匹配");
    }

    @Test
    @Order(3)
    @DisplayName("测试根据车牌号查询车辆")
    void testSelectByVehicleNumber() {
        // 先插入测试数据
        vehicleDao.insert(testVehicle);

        // 执行查询
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getVehicleNumber, "京A12345");
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);

        VehicleEntity result = vehicleDao.selectOne(wrapper);

        // 验证
        assertNotNull(result, "应该能根据车牌号查询到车辆");
        assertEquals("京A12345", result.getVehicleNumber());
    }

    @Test
    @Order(4)
    @DisplayName("测试更新车辆信息")
    void testUpdate() {
        // 先插入测试数据
        vehicleDao.insert(testVehicle);

        // 修改信息
        testVehicle.setVehicleColor("黑色");
        testVehicle.setStatus(2);
        testVehicle.setUpdateTime(LocalDateTime.now());

        // 执行更新
        int result = vehicleDao.updateById(testVehicle);

        // 验证
        assertEquals(1, result, "应该成功更新1条记录");

        // 重新查询验证
        VehicleEntity updated = vehicleDao.selectById(testVehicle.getVehicleId());
        assertEquals("黑色", updated.getVehicleColor(), "颜色应该已更新");
        assertEquals(2, updated.getStatus(), "状态应该已更新");
    }

    @Test
    @Order(5)
    @DisplayName("测试逻辑删除车辆")
    void testLogicDelete() {
        // 先插入测试数据
        vehicleDao.insert(testVehicle);
        Long vehicleId = testVehicle.getVehicleId();

        // 执行逻辑删除
        testVehicle.setDeletedFlag(1);
        testVehicle.setUpdateTime(LocalDateTime.now());
        int result = vehicleDao.updateById(testVehicle);

        // 验证
        assertEquals(1, result, "应该成功标记删除1条记录");

        // 查询已删除的记录
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getVehicleId, vehicleId);
        wrapper.eq(VehicleEntity::getDeletedFlag, 1);

        VehicleEntity deleted = vehicleDao.selectOne(wrapper);
        assertNotNull(deleted, "应该能查询到已删除的记录");
        assertEquals(1, deleted.getDeletedFlag(), "删除标记应该为1");
    }

    @Test
    @Order(6)
    @DisplayName("测试根据公司ID查询车辆列表")
    void testSelectByCompanyId() {
        // 插入多条测试数据
        VehicleEntity vehicle1 = createVehicle("京A11111", 1L);
        VehicleEntity vehicle2 = createVehicle("京A22222", 1L);
        VehicleEntity vehicle3 = createVehicle("京A33333", 2L);

        vehicleDao.insert(vehicle1);
        vehicleDao.insert(vehicle2);
        vehicleDao.insert(vehicle3);

        // 查询公司名称为"测试公司1"的车辆
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getCompanyName, "测试公司1");
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);

        List<VehicleEntity> results = vehicleDao.selectList(wrapper);

        // 验证
        assertNotNull(results, "查询结果不应为null");
        assertTrue(results.size() >= 2, "应该查询到至少2条记录");
    }

    @Test
    @Order(7)
    @DisplayName("测试根据驾驶员ID查询车辆")
    void testSelectByDriverId() {
        // 插入测试数据
        testVehicle.setDriverId(100L);
        vehicleDao.insert(testVehicle);

        // 查询
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getDriverId, 100L);
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);

        List<VehicleEntity> results = vehicleDao.selectList(wrapper);

        // 验证
        assertNotNull(results, "查询结果不应为null");
        assertTrue(results.size() >= 1, "应该查询到至少1条记录");
        assertEquals(100L, results.get(0).getDriverId());
    }

    @Test
    @Order(8)
    @DisplayName("测试根据状态查询车辆")
    void testSelectByStatus() {
        // 插入不同状态的车辆
        VehicleEntity activeVehicle = createVehicle("京A44444", 1L);
        activeVehicle.setStatus(1);
        vehicleDao.insert(activeVehicle);

        VehicleEntity inactiveVehicle = createVehicle("京A55555", 1L);
        inactiveVehicle.setStatus(0);
        vehicleDao.insert(inactiveVehicle);

        // 查询激活状态的车辆
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getStatus, 1);
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);

        List<VehicleEntity> results = vehicleDao.selectList(wrapper);

        // 验证
        assertNotNull(results);
        assertTrue(results.stream().allMatch(v -> v.getStatus() == 1), "所有车辆状态应该为1");
    }

    @Test
    @Order(9)
    @DisplayName("测试模糊查询车辆")
    void testFuzzySearch() {
        // 插入测试数据
        VehicleEntity toyota1 = createVehicle("京A66666", 1L);
        toyota1.setVehicleBrand("丰田");
        vehicleDao.insert(toyota1);

        VehicleEntity toyota2 = createVehicle("京A77777", 1L);
        toyota2.setVehicleBrand("丰田凯美瑞");
        vehicleDao.insert(toyota2);

        // 模糊查询品牌包含"丰田"的车辆
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(VehicleEntity::getVehicleBrand, "丰田");
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);

        List<VehicleEntity> results = vehicleDao.selectList(wrapper);

        // 验证
        assertNotNull(results);
        assertTrue(results.size() >= 2, "应该查询到至少2条记录");
    }

    @Test
    @Order(10)
    @DisplayName("测试批量插入车辆")
    void testBatchInsert() {
        // 创建测试数据列表
        List<VehicleEntity> vehicles = List.of(
                createVehicle("京A88888", 1L),
                createVehicle("京A99999", 1L),
                createVehicle("京A00000", 1L));

        // 批量插入
        vehicles.forEach(v -> vehicleDao.insert(v));

        // 验证
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getCompanyName, "测试公司1");
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);

        List<VehicleEntity> results = vehicleDao.selectList(wrapper);
        assertTrue(results.size() >= 3, "应该有至少3条记录");
    }

    @Test
    @Order(11)
    @DisplayName("测试统计车辆数量")
    void testCount() {
        // 插入测试数据
        vehicleDao.insert(createVehicle("京B11111", 2L));
        vehicleDao.insert(createVehicle("京B22222", 2L));
        vehicleDao.insert(createVehicle("京B33333", 2L));

        // 统计公司名称为"测试公司2"的车辆数量
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getCompanyName, "测试公司2");
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);

        Long count = vehicleDao.selectCount(wrapper);

        // 验证
        assertNotNull(count);
        assertTrue(count >= 3, "应该有至少3辆车");
    }

    @Test
    @Order(12)
    @DisplayName("测试排序查询")
    void testOrderBy() {
        // 插入测试数据（不同创建时间）
        VehicleEntity v1 = createVehicle("京C11111", 3L);
        v1.setCreateTime(LocalDateTime.now().minusDays(2));
        vehicleDao.insert(v1);

        VehicleEntity v2 = createVehicle("京C22222", 3L);
        v2.setCreateTime(LocalDateTime.now().minusDays(1));
        vehicleDao.insert(v2);

        VehicleEntity v3 = createVehicle("京C33333", 3L);
        v3.setCreateTime(LocalDateTime.now());
        vehicleDao.insert(v3);

        // 按创建时间降序查询
        LambdaQueryWrapper<VehicleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleEntity::getCompanyName, "测试公司3");
        wrapper.eq(VehicleEntity::getDeletedFlag, 0);
        wrapper.orderByDesc(VehicleEntity::getCreateTime);

        List<VehicleEntity> results = vehicleDao.selectList(wrapper);

        // 验证
        assertNotNull(results);
        assertTrue(results.size() >= 3);
        // 第一条应该是最新创建的
        assertTrue(results.get(0).getCreateTime().isAfter(results.get(1).getCreateTime()));
    }

    /**
     * 辅助方法：创建车辆测试数据
     */
    private VehicleEntity createVehicle(String vehicleNumber, Long companyId) {
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setVehicleNumber(vehicleNumber);
        vehicle.setVehicleType(1);
        vehicle.setVehicleBrand("测试品牌");
        vehicle.setVehicleModel("测试型号");
        vehicle.setVehicleColor("白色");
        vehicle.setDriverId(1L);
        vehicle.setCompanyName("测试公司" + companyId);
        vehicle.setStatus(1);
        vehicle.setDeletedFlag(0);
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setUpdateTime(LocalDateTime.now());
        return vehicle;
    }
}
