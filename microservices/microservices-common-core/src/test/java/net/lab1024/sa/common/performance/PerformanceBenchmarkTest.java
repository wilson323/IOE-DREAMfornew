package net.lab1024.sa.common.performance;

import net.lab1024.sa.common.util.AESUtil;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能基准测试
 * 用于建立性能基准线和测试系统关键路径的性能表现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@DisplayName("性能基准测试")
class PerformanceBenchmarkTest {

    private static final String TEST_KEY = "12345678901234567890123456789012";
    private static final String TEST_PLAINTEXT = "这是一段性能测试文本，用于加密解密性能基准测试。包含中文字符和特殊字符：@#$%^&*()_+-=[]{}|;':\",./<>?";

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(12345L); // 固定种子确保测试结果可重现
    }

    @Test
    @DisplayName("AES加密性能基准测试")
    void testAESEncryptionPerformance() {
        int iterations = 1000;
        List<String> testStrings = generateTestStrings(iterations);

        long startTime = System.nanoTime();

        // 执行1000次AES加密
        for (String testString : testStrings) {
            AESUtil.encrypt(testString, TEST_KEY);
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double avgTimePerOperation = (double) totalTime / iterations / 1_000_000; // 转换为毫秒

        System.out.println("=== AES加密性能测试结果 ===");
        System.out.println("总迭代次数: " + iterations);
        System.out.println("总耗时: " + TimeUnit.NANOSECONDS.toMillis(totalTime) + "ms");
        System.out.println("平均每次耗时: " + String.format("%.3f", avgTimePerOperation) + "ms");
        System.out.println("每秒可处理次数: " + String.format("%.0f", 1000.0 / avgTimePerOperation));

        // 性能基准要求：每次加密应在10ms内完成
        assertTrue(avgTimePerOperation < 10.0, "AES加密性能不达标，平均耗时超过10ms");
    }

    @Test
    @DisplayName("AES解密性能基准测试")
    void testAESDecryptionPerformance() {
        int iterations = 1000;
        List<String> encryptedStrings = new ArrayList<>();

        // 先加密所有测试数据
        for (int i = 0; i < iterations; i++) {
            String testString = generateRandomString(50 + random.nextInt(100));
            String encrypted = AESUtil.encrypt(testString, TEST_KEY);
            encryptedStrings.add(encrypted);
        }

        long startTime = System.nanoTime();

        // 执行1000次AES解密
        for (String encrypted : encryptedStrings) {
            AESUtil.decrypt(encrypted, TEST_KEY);
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double avgTimePerOperation = (double) totalTime / iterations / 1_000_000; // 转换为毫秒

        System.out.println("=== AES解密性能测试结果 ===");
        System.out.println("总迭代次数: " + iterations);
        System.out.println("总耗时: " + TimeUnit.NANOSECONDS.toMillis(totalTime) + "ms");
        System.out.println("平均每次耗时: " + String.format("%.3f", avgTimePerOperation) + "ms");
        System.out.println("每秒可处理次数: " + String.format("%.0f", 1000.0 / avgTimePerOperation));

        // 性能基准要求：每次解密应在10ms内完成
        assertTrue(avgTimePerOperation < 10.0, "AES解密性能不达标，平均耗时超过10ms");
    }

    @Test
    @DisplayName("ResponseDTO创建性能基准测试")
    void testResponseDTOCreationPerformance() {
        int iterations = 10000;

        long startTime = System.nanoTime();

        // 执行10000次ResponseDTO创建
        for (int i = 0; i < iterations; i++) {
            ResponseDTO.ok("测试数据_" + i);
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double avgTimePerOperation = (double) totalTime / iterations / 1_000_000; // 转换为毫秒

        System.out.println("=== ResponseDTO创建性能测试结果 ===");
        System.out.println("总迭代次数: " + iterations);
        System.out.println("总耗时: " + TimeUnit.NANOSECONDS.toMillis(totalTime) + "ms");
        System.out.println("平均每次耗时: " + String.format("%.3f", avgTimePerOperation) + "ms");
        System.out.println("每秒可创建次数: " + String.format("%.0f", 1000.0 / avgTimePerOperation));

        // 性能基准要求：每次创建应在1ms内完成
        assertTrue(avgTimePerOperation < 1.0, "ResponseDTO创建性能不达标，平均耗时超过1ms");
    }

    @Test
    @DisplayName("字符串操作性能基准测试")
    void testStringOperationPerformance() {
        int iterations = 10000;
        String baseString = "这是一个性能测试基准字符串";

        long startTime = System.nanoTime();

        // 执行字符串拼接操作
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            result.append(baseString).append("_").append(i);
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double avgTimePerOperation = (double) totalTime / iterations / 1_000_000; // 转换为毫秒

        System.out.println("=== 字符串操作性能测试结果 ===");
        System.out.println("总迭代次数: " + iterations);
        System.out.println("总耗时: " + TimeUnit.NANOSECONDS.toMillis(totalTime) + "ms");
        System.out.println("平均每次耗时: " + String.format("%.3f", avgTimePerOperation) + "ms");
        System.out.println("每秒可处理次数: " + String.format("%.0f", 1000.0 / avgTimePerOperation));
        System.out.println("最终字符串长度: " + result.length());

        // 性能基准要求：每次拼接应在0.1ms内完成
        assertTrue(avgTimePerOperation < 0.1, "字符串操作性能不达标，平均耗时超过0.1ms");
    }

    @Test
    @DisplayName("内存分配性能基准测试")
    void testMemoryAllocationPerformance() {
        int iterations = 1000;
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        List<String> stringList = new ArrayList<>(iterations);

        long startTime = System.nanoTime();

        // 执行对象分配
        for (int i = 0; i < iterations; i++) {
            stringList.add("测试字符串_" + i + "_" + System.currentTimeMillis());
        }

        long endTime = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryUsed = endMemory - startMemory;

        System.out.println("=== 内存分配性能测试结果 ===");
        System.out.println("总分配对象数: " + iterations);
        System.out.println("总耗时: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + "ms");
        System.out.println("内存使用: " + (memoryUsed / 1024) + "KB");
        System.out.println("平均每个对象大小: " + (memoryUsed / iterations) + " bytes");

        // 清理内存
        stringList.clear();
        System.gc();
    }

    @Test
    @DisplayName("JSON序列化性能基准测试")
    void testJSONSerializationPerformance() {
        int iterations = 1000;
        List<TestObject> testObjects = new ArrayList<>();

        // 创建测试对象
        for (int i = 0; i < iterations; i++) {
            TestObject obj = new TestObject("测试对象_" + i, i, "test_" + i + "@example.com", true);
            testObjects.add(obj);
        }

        long startTime = System.nanoTime();

        // 简单的JSON序列化模拟（使用toString模拟）
        StringBuilder jsonResult = new StringBuilder();
        for (TestObject obj : testObjects) {
            jsonResult.append(obj.toString());
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double avgTimePerOperation = (double) totalTime / iterations / 1_000_000;

        System.out.println("=== JSON序列化性能测试结果 ===");
        System.out.println("总序列化对象数: " + iterations);
        System.out.println("总耗时: " + TimeUnit.NANOSECONDS.toMillis(totalTime) + "ms");
        System.out.println("平均每次耗时: " + String.format("%.3f", avgTimePerOperation) + "ms");
        System.out.println("每秒可序列化次数: " + String.format("%.0f", 1000.0 / avgTimePerOperation));

        // 性能基准要求：每次序列化应在2ms内完成
        assertTrue(avgTimePerOperation < 2.0, "JSON序列化性能不达标，平均耗时超过2ms");
    }

    @Test
    @DisplayName("集合操作性能基准测试")
    void testCollectionOperationPerformance() {
        int listSize = 1000;
        int iterations = 100;
        List<String> testList = new ArrayList<>();

        // 填充测试数据
        for (int i = 0; i < listSize; i++) {
            testList.add("测试数据_" + i);
        }

        long startTime = System.nanoTime();

        // 执行集合查找操作
        for (int i = 0; i < iterations; i++) {
            String searchKey = "测试数据_" + (i % listSize);
            boolean found = testList.contains(searchKey);
        }

        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double avgTimePerOperation = (double) totalTime / iterations / 1_000_000;

        System.out.println("=== 集合操作性能测试结果 ===");
        System.out.println("列表大小: " + listSize);
        System.out.println("查找次数: " + iterations);
        System.out.println("总耗时: " + TimeUnit.NANOSECONDS.toMillis(totalTime) + "ms");
        System.out.println("平均每次查找耗时: " + String.format("%.3f", avgTimePerOperation) + "ms");
        System.out.println("每秒可查找次数: " + String.format("%.0f", 1000.0 / avgTimePerOperation));

        // 性能基准要求：每次查找应在1ms内完成
        assertTrue(avgTimePerOperation < 1.0, "集合查找性能不达标，平均耗时超过1ms");
    }

    // 辅助方法
    private List<String> generateTestStrings(int count) {
        List<String> strings = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            strings.add("测试字符串_" + i + "_" + generateRandomString(20 + random.nextInt(30)));
        }
        return strings;
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 测试用的简单POJO类
    static class TestObject {
        private final String name;
        private final int age;
        private final String email;
        private final boolean active;

        public TestObject(String name, int age, String email, boolean active) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.active = active;
        }

        @Override
        public String toString() {
            return String.format("{\"name\":\"%s\",\"age\":%d,\"email\":\"%s\",\"active\":%s}", name, age, email, active);
        }
    }
}