package net.lab1024.sa.consume;

/**
 * 单元测试基类
 * 提供通用的测试配置和方法
 *
 * 由于当前项目存在编译错误，暂时使用简化的基类
 * 等待主代码修复后再启用完整的测试框架
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public abstract class BaseTest {

    /**
     * 测试前清理数据
     */
    public abstract void setUp();

    /**
     * 测试数据工厂方法
     */
    protected <T> T createMockData(Class<T> clazz) {
        // 预留的测试数据创建方法
        return null;
    }

    /**
     * 断言辅助方法
     */
    protected void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected not null");
        }
    }

    /**
     * 断言不为null（带提示信息）
     *
     * @param obj     待断言对象
     * @param message 失败提示信息
     */
    protected void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message != null ? message : "Expected not null");
        }
    }

    protected void assertEquals(Object expected, Object actual) {
        if ((expected == null && actual != null) ||
            (expected != null && !expected.equals(actual))) {
            throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
        }
    }

    protected void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true");
        }
    }

    /**
     * 断言为true（带提示信息）
     *
     * @param condition 条件
     * @param message   失败提示信息
     */
    protected void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message != null ? message : "Expected true");
        }
    }

    protected void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Expected false");
        }
    }
}


