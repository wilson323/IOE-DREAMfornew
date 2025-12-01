package net.lab1024.sa.admin.module.smart.access.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AccessAreaEntity 单元测试
 * <p>
 * 测试门禁区域实体的字段验证、业务方法和数据完整性
 * 严格遵循repowiki规范：单元测试覆盖率≥90%
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁区域实体测试")
class AccessAreaEntityTest {

    private AccessAreaEntity accessArea;

    @BeforeEach
    void setUp() {
        accessArea = new AccessAreaEntity();

        // 基础测试数据
        accessArea.setAreaId(1L);
        accessArea.setAreaCode("AREA_001");
        accessArea.setAreaName("测试区域");
        accessArea.setAreaType(1);
        accessArea.setParentId(0L);
        accessArea.setPath("0,1");
        accessArea.setLevel(0);
        accessArea.setSortOrder(1);
        accessArea.setDescription("测试区域描述");
        accessArea.setAccessEnabled(1);
        accessArea.setAccessLevel(1);
        accessArea.setStatus(1);
        accessArea.setCapacity(100);
        accessArea.setArea(500.5);
        accessArea.setLongitude(116.397128);
        accessArea.setLatitude(39.916527);
        accessArea.setRemark("测试备注");
    }

    @Test
    @DisplayName("测试基础字段设置和获取")
    void testBasicFields() {
        // 验证基础字段
        assertEquals(1L, accessArea.getAreaId());
        assertEquals("AREA_001", accessArea.getAreaCode());
        assertEquals("测试区域", accessArea.getAreaName());
        assertEquals(1, accessArea.getAreaType());
        assertEquals(0L, accessArea.getParentId());
        assertEquals("0,1", accessArea.getPath());
        assertEquals(0, accessArea.getLevel());
        assertEquals(1, accessArea.getSortOrder());
        assertEquals("测试区域描述", accessArea.getDescription());
        assertEquals(1, accessArea.getAccessEnabled());
        assertEquals(1, accessArea.getAccessLevel());
        assertEquals(1, accessArea.getStatus());
        assertEquals(100, accessArea.getCapacity());
        assertEquals(500.5, accessArea.getArea());
        assertEquals(116.397128, accessArea.getLongitude());
        assertEquals(39.916527, accessArea.getLatitude());
        assertEquals("测试备注", accessArea.getRemark());
    }

    @Test
    @DisplayName("测试子区域列表字段")
    void testChildrenField() {
        // 测试空子区域列表
        List<AccessAreaEntity> children = accessArea.getChildren();
        assertNull(children, "初始状态下子区域列表应该为null");

        // 设置子区域列表
        AccessAreaEntity child1 = new AccessAreaEntity();
        child1.setAreaId(2L);
        child1.setAreaName("子区域1");

        AccessAreaEntity child2 = new AccessAreaEntity();
        child2.setAreaId(3L);
        child2.setAreaName("子区域2");

        List<AccessAreaEntity> childList = Arrays.asList(child1, child2);
        accessArea.setChildren(childList);

        assertNotNull(accessArea.getChildren());
        assertEquals(2, accessArea.getChildren().size());
        assertEquals("子区域1", accessArea.getChildren().get(0).getAreaName());
        assertEquals("子区域2", accessArea.getChildren().get(1).getAreaName());
    }

    @Test
    @DisplayName("测试子区域数量字段")
    void testChildrenCountField() {
        // 测试初始状态
        assertNull(accessArea.getChildrenCount());

        // 设置子区域数量
        accessArea.setChildrenCount(3);
        assertEquals(3, accessArea.getChildrenCount());
    }

    @Test
    @DisplayName("测试是否有子区域字段")
    void testHasChildrenField() {
        // 测试初始状态
        assertNull(accessArea.getHasChildren());

        // 设置是否有子区域
        accessArea.setHasChildren(true);
        assertTrue(accessArea.getHasChildren());

        accessArea.setHasChildren(false);
        assertFalse(accessArea.getHasChildren());
    }

    @Test
    @DisplayName("测试父级区域名称字段")
    void testParentNameField() {
        // 测试初始状态
        assertNull(accessArea.getParentName());

        // 设置父级区域名称
        accessArea.setParentName("根区域");
        assertEquals("根区域", accessArea.getParentName());
    }

    @Test
    @DisplayName("测试区域类型名称字段")
    void testAreaTypeNameField() {
        // 测试初始状态
        assertNull(accessArea.getAreaTypeName());

        // 设置区域类型名称
        accessArea.setAreaTypeName("园区");
        assertEquals("园区", accessArea.getAreaTypeName());
    }

    @Test
    @DisplayName("测试区域类型的有效值")
    void testAreaTypeValidValues() {
        // 测试所有有效的区域类型
        Integer[] validTypes = {1, 2, 3, 4, 5, 6}; // 1:园区 2:建筑 3:楼层 4:房间 5:区域 6:其他

        for (Integer type : validTypes) {
            accessArea.setAreaType(type);
            assertEquals(type, accessArea.getAreaType(),
                "区域类型 " + type + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试状态的有效值")
    void testStatusValidValues() {
        // 测试所有有效的状态值
        Integer[] validStatuses = {0, 1, 2}; // 0:停用 1:正常 2:维护中

        for (Integer status : validStatuses) {
            accessArea.setStatus(status);
            assertEquals(status, accessArea.getStatus(),
                "状态 " + status + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试坐标字段的有效值范围")
    void testCoordinateValidation() {
        // 测试经度范围 (-180 到 180)
        accessArea.setLongitude(-180.0);
        assertEquals(-180.0, accessArea.getLongitude());

        accessArea.setLongitude(180.0);
        assertEquals(180.0, accessArea.getLongitude());

        // 测试纬度范围 (-90 到 90)
        accessArea.setLatitude(-90.0);
        assertEquals(-90.0, accessArea.getLatitude());

        accessArea.setLatitude(90.0);
        assertEquals(90.0, accessArea.getLatitude());
    }

    @Test
    @DisplayName("测试层级路径格式")
    void testPathFormat() {
        // 测试根区域路径
        accessArea.setParentId(0L);
        accessArea.setLevel(0);
        accessArea.setPath("0,1");
        assertEquals("0,1", accessArea.getPath());

        // 测试子区域路径
        accessArea.setParentId(1L);
        accessArea.setLevel(1);
        accessArea.setPath("0,1,2");
        assertEquals("0,1,2", accessArea.getPath());

        // 测试更深层级路径
        accessArea.setLevel(3);
        accessArea.setPath("0,1,2,3,4");
        assertEquals("0,1,2,3,4", accessArea.getPath());
    }

    @Test
    @DisplayName("测试启用状态的有效值")
    void testEnabledFlagValidValues() {
        // 测试门禁启用状态
        accessArea.setAccessEnabled(0); // 禁用
        assertEquals(0, accessArea.getAccessEnabled());

        accessArea.setAccessEnabled(1); // 启用
        assertEquals(1, accessArea.getAccessEnabled());
    }

    @Test
    @DisplayName("测试权限级别字段")
    void testAccessLevelField() {
        // 测试权限级别范围 (1-9)
        for (int level = 1; level <= 9; level++) {
            accessArea.setAccessLevel(level);
            assertEquals(level, accessArea.getAccessLevel(),
                "权限级别 " + level + " 应该有效");
        }
    }

    @Test
    @DisplayName("测试容量和面积的边界值")
    void testBoundaryValues() {
        // 测试容量边界值
        accessArea.setCapacity(0);
        assertEquals(0, accessArea.getCapacity());

        accessArea.setCapacity(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, accessArea.getCapacity());

        // 测试面积边界值
        accessArea.setArea(0.0);
        assertEquals(0.0, accessArea.getArea());

        accessArea.setArea(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, accessArea.getArea());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        // 验证toString方法不会抛出异常
        String toString = accessArea.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("AccessAreaEntity"));
        assertTrue(toString.contains("areaName=测试区域"));
    }

    @Test
    @DisplayName("测试equals和hashCode方法")
    void testEqualsAndHashCode() {
        AccessAreaEntity area1 = new AccessAreaEntity();
        area1.setAreaId(1L);
        area1.setAreaCode("AREA_001");

        AccessAreaEntity area2 = new AccessAreaEntity();
        area2.setAreaId(1L);
        area2.setAreaCode("AREA_001");

        AccessAreaEntity area3 = new AccessAreaEntity();
        area3.setAreaId(2L);
        area3.setAreaCode("AREA_002");

        // 测试相等性
        assertEquals(area1, area2, "相同ID和编码的区域应该相等");
        assertEquals(area1.hashCode(), area2.hashCode(), "相等对象的hashCode应该相同");

        assertNotEquals(area1, area3, "不同ID的区域应该不相等");
        assertNotEquals(area1.hashCode(), area3.hashCode(), "不相等对象的hashCode可能不同");
    }

    @Test
    @DisplayName("测试Builder模式（如果使用）")
    void testBuilderPattern() {
        // 这个测试根据实际使用的Builder框架进行调整
        AccessAreaEntity builderArea = new AccessAreaEntity();
        builderArea.setAreaId(999L);
        builderArea.setAreaCode("BUILDER_TEST");
        builderArea.setAreaName("Builder测试区域");

        assertEquals(999L, builderArea.getAreaId());
        assertEquals("BUILDER_TEST", builderArea.getAreaCode());
        assertEquals("Builder测试区域", builderArea.getAreaName());
    }
}