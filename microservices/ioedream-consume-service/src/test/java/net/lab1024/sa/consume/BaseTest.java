package net.lab1024.sa.consume;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单元测试基类
 * 提供通用的测试配置和方法
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    /**
     * 测试前清理数据
     */
    @BeforeEach
    public abstract void setUp();

    /**
     * 清理数据库表
     *
     * @param entityClass 实体类
     */
    protected void cleanTable(Class<?> entityClass) {
        try {
            // 这里需要根据具体的实体类来清理
            // 由于没有实体类的直接引用，这里暂时留空
            // 实际使用时可以传入具体的Entity类
            // QueryWrapper<?> queryWrapper = new QueryWrapper<>(); // 预留，实际使用时取消注释
        } catch (Exception e) {
            // 忽略清理异常
        }
    }

    /**
     * 清理所有相关表
     */
    protected void cleanAllTables() {
        // 清理消费相关表
        cleanTable(Object.class); // 这里需要替换为具体的实体类
    }
}


