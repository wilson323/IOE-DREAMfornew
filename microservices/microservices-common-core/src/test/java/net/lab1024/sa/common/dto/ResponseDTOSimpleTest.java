package net.lab1024.sa.common.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResponseDTO 简化单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@DisplayName("ResponseDTO 简化单元测试")
class ResponseDTOSimpleTest {

    private ResponseDTO<String> successResponse;
    private ResponseDTO<String> errorResponse;

    @BeforeEach
    void setUp() {
        successResponse = ResponseDTO.ok("测试数据");
        errorResponse = ResponseDTO.error("TEST_ERROR", "测试错误");
    }

    @Test
    @DisplayName("测试成功响应创建")
    void testCreateSuccessResponse() {
        // Given & When
        ResponseDTO<String> response = ResponseDTO.ok("成功数据");

        // Then
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("成功数据", response.getData());
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    @DisplayName("测试错误响应创建")
    void testCreateErrorResponse() {
        // Given & When
        ResponseDTO<String> response = ResponseDTO.error("TEST_ERROR", "测试错误");

        // Then
        assertEquals(500, response.getCode()); // 字符串错误码被转换为500
        assertEquals("测试错误", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    @DisplayName("测试带数据的错误响应")
    void testCreateErrorResponseWithData() {
        // Given & When
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("field", "username");
        errorData.put("message", "用户名不能为空");

        ResponseDTO<Map<String, Object>> response = ResponseDTO.error("VALIDATION_ERROR", "验证失败", errorData);

        // Then
        assertEquals(500, response.getCode()); // 字符串错误码被转换为500
        assertEquals("验证失败", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("username", response.getData().get("field"));
        assertEquals("用户名不能为空", response.getData().get("message"));
    }

    @Test
    @DisplayName("测试响应时间戳自动设置")
    void testTimestampAutoSet() {
        // Given & When
        long beforeCreate = System.currentTimeMillis();
        ResponseDTO<String> response = ResponseDTO.ok("测试");
        long afterCreate = System.currentTimeMillis();

        // Then
        assertTrue(response.getTimestamp() >= beforeCreate);
        assertTrue(response.getTimestamp() <= afterCreate);
    }

    @Test
    @DisplayName("测试错误码为字符串的情况")
    void testErrorCodeAsString() {
        // Given & When
        ResponseDTO<String> response = ResponseDTO.error("VALIDATION_FAILED", "验证失败");

        // Then
        assertEquals(500, response.getCode()); // 字符串错误码被转换为500
        assertEquals("验证失败", response.getMessage());
    }

    @Test
    @DisplayName("测试错误码为数字的情况")
    void testErrorCodeAsNumber() {
        // Given & When
        ResponseDTO<String> response = ResponseDTO.error(400, "请求参数错误");

        // Then
        assertEquals(400, response.getCode());
        assertEquals("请求参数错误", response.getMessage());
    }

    @Test
    @DisplayName("测试响应对象的toString方法")
    void testToStringMethod() {
        // Given & When
        ResponseDTO<String> response = ResponseDTO.ok("测试数据");
        String responseString = response.toString();

        // Then
        assertNotNull(responseString);
        assertTrue(responseString.contains("ResponseDTO"));
    }

    @Test
    @DisplayName("测试响应对象equals方法")
    void testEqualsMethod() {
        // Given
        ResponseDTO<String> response1 = ResponseDTO.ok("相同数据");
        ResponseDTO<String> response2 = ResponseDTO.ok("相同数据");
        ResponseDTO<String> response3 = ResponseDTO.ok("不同数据");

        // When & Then
        assertEquals(response1, response2); // 相同数据应该相等
        assertNotEquals(response1, response3); // 不同数据应该不相等
        assertNotEquals(response1, null); // 不应该等于null
        assertNotEquals(response1, "字符串"); // 不应该等于其他类型
    }

    @Test
    @DisplayName("测试响应对象hashCode方法")
    void testHashCodeMethod() {
        // Given
        ResponseDTO<String> response1 = ResponseDTO.ok("相同数据");
        ResponseDTO<String> response2 = ResponseDTO.ok("相同数据");

        // When & Then
        assertEquals(response1.hashCode(), response2.hashCode()); // 相等对象应该有相同hashCode
    }

    @Test
    @DisplayName("测试空数据处理")
    void testNullDataHandling() {
        // Given & When
        ResponseDTO<String> response = ResponseDTO.ok(null);

        // Then
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试复杂对象数据")
    void testComplexObjectData() {
        // Given
        TestData testData = new TestData("张三", 25);

        // When
        ResponseDTO<TestData> response = ResponseDTO.ok(testData);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("张三", response.getData().getName());
        assertEquals(25, response.getData().getAge());
    }

    /**
     * 测试数据类
     */
    private static class TestData {
        private String name;
        private int age;

        public TestData(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
