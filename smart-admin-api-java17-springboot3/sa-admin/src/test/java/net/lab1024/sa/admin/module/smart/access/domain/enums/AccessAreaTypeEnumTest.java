package net.lab1024.sa.admin.module.smart.access.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 门禁区域类型枚举单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@DisplayName("门禁区域类型枚举单元测试")
class AccessAreaTypeEnumTest {

    /**
     * 测试根据值获取枚举
     */
    @Test
    @DisplayName("测试根据值获取枚举")
    void testFromValue() {
        assertEquals(AccessAreaTypeEnum.CAMPUS, AccessAreaTypeEnum.fromValue(1), "值1应该对应园区");
        assertEquals(AccessAreaTypeEnum.BUILDING, AccessAreaTypeEnum.fromValue(2), "值2应该对应建筑");
        assertEquals(AccessAreaTypeEnum.FLOOR, AccessAreaTypeEnum.fromValue(3), "值3应该对应楼层");
        assertEquals(AccessAreaTypeEnum.ROOM, AccessAreaTypeEnum.fromValue(4), "值4应该对应房间");
        assertEquals(AccessAreaTypeEnum.AREA, AccessAreaTypeEnum.fromValue(5), "值5应该对应区域");
        assertEquals(AccessAreaTypeEnum.OTHER, AccessAreaTypeEnum.fromValue(6), "值6应该对应其他");

        assertNull(AccessAreaTypeEnum.fromValue(0), "无效值应该返回null");
        assertNull(AccessAreaTypeEnum.fromValue(7), "无效值应该返回null");
        assertNull(AccessAreaTypeEnum.fromValue(null), "null值应该返回null");
    }

    /**
     * 测试根据代码获取枚举
     */
    @Test
    @DisplayName("测试根据代码获取枚举")
    void testFromCode() {
        assertEquals(AccessAreaTypeEnum.CAMPUS, AccessAreaTypeEnum.fromCode("CAMPUS"), "代码CAMPUS应该对应园区");
        assertEquals(AccessAreaTypeEnum.BUILDING, AccessAreaTypeEnum.fromCode("BUILDING"), "代码BUILDING应该对应建筑");
        assertEquals(AccessAreaTypeEnum.CAMPUS, AccessAreaTypeEnum.fromCode("campus"), "代码应该不区分大小写");

        assertNull(AccessAreaTypeEnum.fromCode("INVALID"), "无效代码应该返回null");
        assertNull(AccessAreaTypeEnum.fromCode(null), "null代码应该返回null");
        assertNull(AccessAreaTypeEnum.fromCode(""), "空代码应该返回null");
    }

    /**
     * 测试值验证
     */
    @Test
    @DisplayName("测试值验证")
    void testIsValid() {
        assertTrue(AccessAreaTypeEnum.isValid(1), "值1应该有效");
        assertTrue(AccessAreaTypeEnum.isValid(6), "值6应该有效");
        assertFalse(AccessAreaTypeEnum.isValid(0), "值0应该无效");
        assertFalse(AccessAreaTypeEnum.isValid(7), "值7应该无效");
        assertFalse(AccessAreaTypeEnum.isValid(null), "null值应该无效");
    }

    /**
     * 测试枚举属性
     */
    @Test
    @DisplayName("测试枚举属性")
    void testEnumProperties() {
        assertEquals(1, AccessAreaTypeEnum.CAMPUS.getValue(), "园区值应该是1");
        assertEquals("园区", AccessAreaTypeEnum.CAMPUS.getName(), "园区名称应该是'园区'");
        assertEquals("CAMPUS", AccessAreaTypeEnum.CAMPUS.getCode(), "园区代码应该是'CAMPUS'");
    }
}
