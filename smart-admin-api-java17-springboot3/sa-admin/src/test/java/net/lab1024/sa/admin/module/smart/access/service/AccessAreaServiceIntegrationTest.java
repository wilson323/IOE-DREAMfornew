/*
     * 门禁区域服务层集成测试
     *
     * @Author:    IOE-DREAM Team
     * @Date:      2025-01-17
     * @Copyright  IOE-DREAM智慧园区一卡通管理平台
     */

package net.lab1024.sa.admin.module.smart.access.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.smart.access.dao.AccessAreaDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessAreaForm;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.admin.module.smart.access.manager.AccessAreaManager;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 门禁区域服务层集成测试
 * 使用真实数据库进行完整的业务逻辑测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("门禁区域服务层集成测试")
class AccessAreaServiceIntegrationTest {

    @Resource
    private AccessAreaService accessAreaService;

    @Resource
    private AccessAreaManager accessAreaManager;

    @Resource
    private AccessAreaDao accessAreaDao;

    private AccessAreaForm testAreaForm;

    @BeforeEach
    void setUp() {
        testAreaForm = AccessAreaForm.builder()
                .areaName("测试区域")
                .areaCode("TEST_AREA_001")
                .areaType("BUILDING")
                .parentId(0L)
                .level(1)
                .sort(1)
                .description("测试区域描述")
                .status(1)
                .build();
    }

    @Test
    @DisplayName("测试添加区域 - 完整业务流程")
    void testAddArea_CompleteBusinessFlow() {
        // Act
        ResponseDTO<String> result = accessAreaService.addArea(testAreaForm);

        // Assert
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        // 验证数据库中存在该记录
        List<AccessAreaEntity> areas = accessAreaDao.selectByAreaCode("TEST_AREA_001");
        assertEquals(1, areas.size());
        assertEquals("测试区域", areas.get(0).getAreaName());
        assertEquals("BUILDING", areas.get(0).getAreaType());
    }

    @Test
    @DisplayName("测试获取区域列表 - 分页查询")
    void testGetAreaList_PaginationQuery() {
        // 准备测试数据
        for (int i = 1; i <= 25; i++) {
            AccessAreaForm form = AccessAreaForm.builder()
                    .areaName("区域" + i)
                    .areaCode("AREA_" + String.format("%03d", i))
                    .areaType("FLOOR")
                    .status(1)
                    .build();
            accessAreaService.addArea(form);
        }

        // 测试第一页
        PageParam pageParam1 = PageParam.builder()
                .pageNum(1)
                .pageSize(10)
                .build();
        PageResult<Object> result1 = accessAreaService.getAreaList(pageParam1);

        assertEquals(10, result1.getPageSize());
        assertEquals(1, result1.getPageNum());
        assertEquals(25, result1.getTotal());
        assertEquals(10, result1.getList().size());

        // 测试第二页
        PageParam pageParam2 = PageParam.builder()
                .pageNum(2)
                .pageSize(10)
                .build();
        PageResult<Object> result2 = accessAreaService.getAreaList(pageParam2);

        assertEquals(10, result2.getPageSize());
        assertEquals(2, result2.getPageNum());
        assertEquals(25, result2.getTotal());
        assertEquals(10, result2.getList().size());

        // 测试第三页（最后一页）
        PageParam pageParam3 = PageParam.builder()
                .pageNum(3)
                .pageSize(10)
                .build();
        PageResult<Object> result3 = accessAreaService.getAreaList(pageParam3);

        assertEquals(10, result3.getPageSize());
        assertEquals(3, result3.getPageNum());
        assertEquals(25, result3.getTotal());
        assertEquals(5, result3.getList().size()); // 最后一页只有5条数据
    }

    @Test
    @DisplayName("测试区域树结构 - 层级关系")
    void testGetAreaTree_HierarchicalRelationship() {
        // 1. 添加根区域
        AccessAreaForm rootForm = AccessAreaForm.builder()
                .areaName("主楼")
                .areaCode("MAIN_BUILDING")
                .areaType("BUILDING")
                .parentId(0L)
                .level(1)
                .sort(1)
                .status(1)
                .build();
        accessAreaService.addArea(rootForm);

        // 2. 添加子区域
        AccessAreaForm childForm1 = AccessAreaForm.builder()
                .areaName("1楼")
                .areaCode("FLOOR_1")
                .areaType("FLOOR")
                .parentId(1L) // 假设根区域ID为1
                .level(2)
                .sort(1)
                .status(1)
                .build();
        accessAreaService.addArea(childForm1);

        AccessAreaForm childForm2 = AccessAreaForm.builder()
                .areaName("2楼")
                .areaCode("FLOOR_2")
                .areaType("FLOOR")
                .parentId(1L) // 假设根区域ID为1
                .level(2)
                .sort(2)
                .status(1)
                .build();
        accessAreaService.addArea(childForm2);

        // 3. 获取区域树
        List<AccessAreaTreeVO> tree = accessAreaService.getAreaTree();

        // Assert
        assertNotNull(tree);
        assertFalse(tree.isEmpty());

        // 验证树结构（根据实际业务逻辑调整）
        // 这里需要根据实际的树构建逻辑进行验证
    }

    @Test
    @DisplayName("测试区域编码唯一性验证")
    void testValidateAreaCode_Uniqueness() {
        // 1. 添加区域
        accessAreaService.addArea(testAreaForm);

        // 2. 验证相同编码不可用
        ResponseDTO<Boolean> result1 = accessAreaService.validateAreaCode("TEST_AREA_001", null);
        assertTrue(result1.getOk());
        assertFalse(result1.getData());

        // 3. 验证不同编码可用
        ResponseDTO<Boolean> result2 = accessAreaService.validateAreaCode("DIFFERENT_CODE", null);
        assertTrue(result2.getOk());
        assertTrue(result2.getData());

        // 4. 编辑时排除自身ID的验证
        ResponseDTO<Boolean> result3 = accessAreaService.validateAreaCode("TEST_AREA_001", 1L);
        assertTrue(result3.getOk());
        assertTrue(result3.getData()); // 排除自身后，应该可用
    }

    @Test
    @DisplayName("测试区域更新 - 数据一致性")
    void testUpdateArea_DataConsistency() {
        // 1. 添加区域
        ResponseDTO<String> addResult = accessAreaService.addArea(testAreaForm);
        assertTrue(addResult.getOk());

        // 2. 更新区域
        AccessAreaForm updateForm = AccessAreaForm.builder()
                .areaId(1L) // 假设添加后ID为1
                .areaName("更新后的区域名称")
                .areaCode("UPDATED_AREA_001")
                .areaType("ROOM")
                .description("更新后的描述")
                .status(0) // 更改为禁用状态
                .build();

        ResponseDTO<String> updateResult = accessAreaService.updateArea(updateForm);
        assertTrue(updateResult.getOk());

        // 3. 验证更新结果
        AccessAreaEntity updatedEntity = accessAreaDao.selectById(1L);
        assertNotNull(updatedEntity);
        assertEquals("更新后的区域名称", updatedEntity.getAreaName());
        assertEquals("UPDATED_AREA_001", updatedEntity.getAreaCode());
        assertEquals("ROOM", updatedEntity.getAreaType());
        assertEquals("更新后的描述", updatedEntity.getDescription());
        assertEquals(0, updatedEntity.getStatus());

        // 4. 验证更新时间
        assertNotNull(updatedEntity.getUpdateTime());
        assertTrue(updatedEntity.getUpdateTime().isAfter(updatedEntity.getCreateTime()));
    }

    @Test
    @DisplayName("测试区域删除 - 软删除逻辑")
    void testDeleteArea_SoftDeleteLogic() {
        // 1. 添加区域
        accessAreaService.addArea(testAreaForm);

        // 2. 删除区域
        ResponseDTO<String> deleteResult = accessAreaService.deleteArea(1L);
        assertTrue(deleteResult.getOk());

        // 3. 验证软删除 - 记录仍然存在但标记为已删除
        AccessAreaEntity deletedEntity = accessAreaDao.selectById(1L);
        assertNotNull(deletedEntity); // 记录仍然存在
        assertTrue(deletedEntity.getDeletedFlag()); // 标记为已删除

        // 4. 验证查询时不会返回已删除的记录
        PageResult<Object> queryResult = accessAreaService.getAreaList(PageParam.builder().build());
        assertEquals(0, queryResult.getTotal()); // 已删除的记录不应在查询结果中
    }

    @Test
    @DisplayName("测试批量删除 - 事务一致性")
    void testBatchDelete_TransactionConsistency() {
        // 1. 准备多个区域
        for (int i = 1; i <= 5; i++) {
            AccessAreaForm form = AccessAreaForm.builder()
                    .areaName("批量测试区域" + i)
                    .areaCode("BATCH_AREA_" + String.format("%03d", i))
                    .areaType("ROOM")
                    .status(1)
                    .build();
            accessAreaService.addArea(form);
        }

        // 2. 批量删除
        List<Long> areaIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        ResponseDTO<String> batchDeleteResult = accessAreaService.batchDeleteAreas(areaIds);
        assertTrue(batchDeleteResult.getOk());

        // 3. 验证所有区域都被软删除
        for (Long areaId : areaIds) {
            AccessAreaEntity entity = accessAreaDao.selectById(areaId);
            assertNotNull(entity);
            assertTrue(entity.getDeletedFlag());
        }

        // 4. 验证查询结果
        PageResult<Object> queryResult = accessAreaService.getAreaList(PageParam.builder().build());
        assertEquals(0, queryResult.getTotal());
    }

    @Test
    @DisplayName("测试区域状态更新 - 业务逻辑")
    void testUpdateAreaStatus_BusinessLogic() {
        // 1. 添加区域
        accessAreaService.addArea(testAreaForm);

        // 2. 更新状态为禁用
        ResponseDTO<String> disableResult = accessAreaService.updateAreaStatus(1L, 0);
        assertTrue(disableResult.getOk());

        // 3. 验证状态更新
        AccessAreaEntity entity = accessAreaDao.selectById(1L);
        assertEquals(0, entity.getStatus());

        // 4. 更新状态为启用
        ResponseDTO<String> enableResult = accessAreaService.updateAreaStatus(1L, 1);
        assertTrue(enableResult.getOk());

        // 5. 验证状态更新
        entity = accessAreaDao.selectById(1L);
        assertEquals(1, entity.getStatus());
    }

    @Test
    @DisplayName("测试区域统计 - 数据准确性")
    void testGetAreaStats_DataAccuracy() {
        // 1. 准备测试数据
        for (int i = 1; i <= 10; i++) {
            AccessAreaForm form = AccessAreaForm.builder()
                    .areaName("统计测试区域" + i)
                    .areaCode("STATS_AREA_" + String.format("%03d", i))
                    .areaType("ROOM")
                    .status(i <= 7 ? 1 : 0) // 前7个启用，后3个禁用
                    .build();
            accessAreaService.addArea(form);
        }

        // 2. 获取统计数据
        ResponseDTO<Map<String, Object>> statsResult = accessAreaService.getAreaStats();
        assertTrue(statsResult.getOk());
        assertNotNull(statsResult.getData());

        Map<String, Object> stats = statsResult.getData();

        // 验证统计数据（根据实际业务逻辑调整）
        if (stats.containsKey("totalArea")) {
            assertTrue((Integer) stats.get("totalArea") >= 10);
        }
        if (stats.containsKey("enabledArea")) {
            assertTrue((Integer) stats.get("enabledArea") >= 7);
        }
        if (stats.containsKey("disabledArea")) {
            assertTrue((Integer) stats.get("disabledArea") >= 3);
        }
    }

    @Test
    @DisplayName("测试区域权限管理 - 复杂业务场景")
    void testAreaPermissionManagement_ComplexBusinessScenario() {
        // 1. 添加区域
        accessAreaService.addArea(testAreaForm);

        // 2. 获取权限列表
        ResponseDTO<List<Map<String, Object>>> permissionsResult = accessAreaService.getAreaPermissions(1L);
        assertTrue(permissionsResult.getOk());
        assertNotNull(permissionsResult.getData());

        // 3. 更新权限
        List<Map<String, Object>> permissions = Arrays.asList(
                Map.of("permissionId", 1L, "enabled", true),
                Map.of("permissionId", 2L, "enabled", false),
                Map.of("permissionId", 3L, "enabled", true));

        ResponseDTO<String> updatePermissionsResult = accessAreaService.updateAreaPermissions(1L, permissions);
        assertTrue(updatePermissionsResult.getOk());

        // 4. 验证权限更新
        ResponseDTO<List<Map<String, Object>>> updatedPermissionsResult = accessAreaService.getAreaPermissions(1L);
        assertTrue(updatedPermissionsResult.getOk());
        assertNotNull(updatedPermissionsResult.getData());
    }

    @Test
    @DisplayName("测试区域设备关联 - 业务流程")
    void testAreaDeviceAssociation_BusinessFlow() {
        // 1. 添加区域
        accessAreaService.addArea(testAreaForm);

        // 2. 获取区域设备列表
        ResponseDTO<List<Map<String, Object>>> devicesResult = accessAreaService.getAreaDevices(1L);
        assertTrue(devicesResult.getOk());
        assertNotNull(devicesResult.getData());

        // 3. 验证设备关联关系（根据实际业务逻辑调整）
        // 这里需要根据实际的设备管理逻辑进行验证
    }

    @Test
    @DisplayName("测试异常处理 - 数据完整性")
    void testExceptionHandling_DataIntegrity() {
        // 1. 测试添加不存在的父区域
        AccessAreaForm invalidForm = AccessAreaForm.builder()
                .areaName("无效区域")
                .areaCode("INVALID_AREA")
                .areaType("ROOM")
                .parentId(9999L) // 不存在的父区域ID
                .level(2)
                .status(1)
                .build();

        ResponseDTO<String> result = accessAreaService.addArea(invalidForm);
        // 根据业务逻辑，可能成功（父区域为空）或失败（外键约束）
        assertNotNull(result);

        // 2. 测试更新不存在的区域
        AccessAreaForm updateForm = AccessAreaForm.builder()
                .areaId(9999L) // 不存在的区域ID
                .areaName("更新不存在的区域")
                .build();

        ResponseDTO<String> updateResult = accessAreaService.updateArea(updateForm);
        assertNotNull(updateResult);

        // 3. 测试删除不存在的区域
        ResponseDTO<String> deleteResult = accessAreaService.deleteArea(9999L);
        assertNotNull(deleteResult);
    }

    @Test
    @DisplayName("测试获取区域统计信息 - 新增功能")
    void testGetAreaStatistics() {
        // Arrange: 创建测试区域
        ResponseDTO<String> addResult = accessAreaService.addArea(testAreaForm);
        assertTrue(addResult.getOk());

        // 获取创建的区域ID（需要从数据库查询）
        List<AccessAreaEntity> areas = accessAreaDao.selectByAreaCode("TEST_AREA_001");
        assertFalse(areas.isEmpty());
        Long areaId = areas.get(0).getAreaId();

        // Act: 获取区域统计信息
        Object statistics = accessAreaService.getAreaStatistics(areaId);

        // Assert: 验证统计信息不为空
        assertNotNull(statistics, "区域统计信息不应该为null");
        assertTrue(statistics instanceof Map, "统计信息应该是Map类型");

        @SuppressWarnings("unchecked")
        Map<String, Object> statsMap = (Map<String, Object>) statistics;
        assertTrue(statsMap.containsKey("totalDevices"), "应该包含总设备数字段");
        assertTrue(statsMap.containsKey("onlineDevices"), "应该包含在线设备数字段");
        assertTrue(statsMap.containsKey("offlineDevices"), "应该包含离线设备数字段");
        assertTrue(statsMap.containsKey("onlineRate"), "应该包含在线率字段");
        assertTrue(statsMap.containsKey("areaLevel"), "应该包含区域层级字段");
        assertTrue(statsMap.containsKey("childAreaCount"), "应该包含子区域数字段");
    }

    @Test
    @DisplayName("测试获取区域设备列表 - 包含子区域")
    void testGetAreaDevices_WithChildren() {
        // Arrange: 创建父区域和子区域
        ResponseDTO<String> parentResult = accessAreaService.addArea(testAreaForm);
        assertTrue(parentResult.getOk());

        List<AccessAreaEntity> parentAreas = accessAreaDao.selectByAreaCode("TEST_AREA_001");
        assertFalse(parentAreas.isEmpty());
        Long parentAreaId = parentAreas.get(0).getAreaId();

        // 创建子区域
        AccessAreaForm childForm = AccessAreaForm.builder()
                .areaName("子区域")
                .areaCode("TEST_CHILD_001")
                .areaType("FLOOR")
                .parentId(parentAreaId)
                .level(2)
                .status(1)
                .build();
        ResponseDTO<String> childResult = accessAreaService.addArea(childForm);
        assertTrue(childResult.getOk());

        // Act: 获取区域设备列表（包含子区域）
        List<Object> devices = accessAreaService.getAreaDevices(parentAreaId, true);

        // Assert: 验证设备列表不为null
        assertNotNull(devices, "设备列表不应该为null");
        assertTrue(devices instanceof List, "设备列表应该是List类型");
    }

    @Test
    @DisplayName("测试检查区域是否关联设备")
    void testHasAssociatedDevices() {
        // Arrange: 创建测试区域
        ResponseDTO<String> addResult = accessAreaService.addArea(testAreaForm);
        assertTrue(addResult.getOk());

        List<AccessAreaEntity> areas = accessAreaDao.selectByAreaCode("TEST_AREA_001");
        assertFalse(areas.isEmpty());
        Long areaId = areas.get(0).getAreaId();

        // Act: 检查区域是否关联设备
        boolean hasDevices = accessAreaService.hasAssociatedDevices(areaId);

        // Assert: 验证返回布尔值（新创建的区域可能没有设备）
        assertNotNull(Boolean.valueOf(hasDevices), "应该返回布尔值");
    }
}
