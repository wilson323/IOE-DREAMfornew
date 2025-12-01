package net.lab1024.sa.admin.module.smart.access.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 门禁区域状态枚举单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@DisplayName("门禁区域状态枚举单元测试")
class AccessAreaStatusEnumTest {

    /**
     * 测试根据值获取枚举
     */
    @Test
    @DisplayName("测试根据值获取枚举")
    void testFromValue() {
        assertEquals(AccessAreaStatusEnum.ENABLED, AccessAreaStatusEnum.fromValue(1), "值1应该对应启用");
        assertEquals(AccessAreaStatusEnum.DISABLED, AccessAreaStatusEnum.fromValue(0), "值0应该对应禁用");

        assertNull(AccessAreaStatusEnum.fromValue(2), "无效值应该返回null");
        assertNull(AccessAreaStatusEnum.fromValue(null), "null值应该返回null");
    }

    /**
     * 测试根据代码获取枚举
     */
    @Test
    @DisplayName("测试根据代码获取枚举")
    void testFromCode() {
        assertEquals(AccessAreaStatusEnum.ENABLED, AccessAreaStatusEnum.fromCode("ENABLED"), "代码ENABLED应该对应启用");
        assertEquals(AccessAreaStatusEnum.DISABLED, AccessAreaStatusEnum.fromCode("DISABLED"), "代码DISABLED应该对应禁用");
        assertEquals(AccessAreaStatusEnum.ENABLED, AccessAreaStatusEnum.fromCode("enabled"), "代码应该不区分大小写");

        assertNull(AccessAreaStatusEnum.fromCode("INVALID"), "无效代码应该返回null");
        assertNull(AccessAreaStatusEnum.fromCode(null), "null代码应该返回null");
    }

    /**
     * 测试值验证
     */
    @Test
    @DisplayName("测试值验证")
    void testIsValid() {
        assertTrue(AccessAreaStatusEnum.isValid(0), "值0应该有效");
        assertTrue(AccessAreaStatusEnum.isValid(1), "值1应该有效");
        assertFalse(AccessAreaStatusEnum.isValid(2), "值2应该无效");
        assertFalse(AccessAreaStatusEnum.isValid(null), "null值应该无效");
    }

    /**
     * 测试枚举属性
     */
    @Test
    @DisplayName("测试枚举属性")
    void testEnumProperties() {
        assertEquals(1, AccessAreaStatusEnum.ENABLED.getValue(), "启用值应该是1");
        assertEquals("启用", AccessAreaStatusEnum.ENABLED.getName(), "启用名称应该是'启用'");
        assertEquals("ENABLED", AccessAreaStatusEnum.ENABLED.getCode(), "启用代码应该是'ENABLED'");

        assertEquals(0, AccessAreaStatusEnum.DISABLED.getValue(), "禁用值应该是0");
        assertEquals("禁用", AccessAreaStatusEnum.DISABLED.getName(), "禁用名称应该是'禁用'");
    }
}
