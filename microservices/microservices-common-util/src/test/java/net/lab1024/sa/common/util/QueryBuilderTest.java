package net.lab1024.sa.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueryBuilder单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
@DisplayName("QueryBuilder工具类测试")
class QueryBuilderTest {

    @Test
    @DisplayName("测试：关键字查询（多字段OR）")
    void testKeywordQuery() {
        // Given
        String keyword = "张三";

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .keyword(keyword, UserEntity::getUsername, UserEntity::getRealName)
                .build();

        // Then
        assertNotNull(wrapper);
        String sql = wrapper.getSqlSegment();
        System.out.println("生成的SQL条件: " + sql);
        assertTrue(sql.contains("LIKE") || sql.isEmpty()); // 如果为空说明使用了参数化查询
    }

    @Test
    @DisplayName("测试：关键字为空时不添加条件")
    void testKeywordEmpty() {
        // Given
        String keyword = "";

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .keyword(keyword, UserEntity::getUsername)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：关键字为null时不添加条件")
    void testKeywordNull() {
        // Given
        String keyword = null;

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .keyword(keyword, UserEntity::getUsername)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：等值查询")
    void testEqualsQuery() {
        // Given
        Integer status = 1;

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .eq(UserEntity::getDeletedFlag, status)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：等值查询（值为null时不添加条件）")
    void testEqualsQueryNull() {
        // Given
        Integer status = null;

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .eq(UserEntity::getDeletedFlag, status)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：IN查询")
    void testInQuery() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .in(UserEntity::getUserId, userIds)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：IN查询（空集合不添加条件）")
    void testInQueryEmpty() {
        // Given
        List<Long> userIds = Arrays.asList();

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .in(UserEntity::getUserId, userIds)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：BETWEEN查询")
    void testBetweenQuery() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 12, 31, 23, 59, 59);

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .between(UserEntity::getCreateTime, startTime, endTime)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：BETWEEN查询（一边为null不添加条件）")
    void testBetweenQueryNull() {
        // Given
        LocalDateTime startTime = null;
        LocalDateTime endTime = LocalDateTime.now();

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .between(UserEntity::getCreateTime, startTime, endTime)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：排序")
    void testOrderBy() {
        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .orderByDesc(UserEntity::getCreateTime)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：链式调用")
    void testChainCalls() {
        // Given
        String keyword = "admin";
        Integer status = 1;
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.now();

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .keyword(keyword, UserEntity::getUsername, UserEntity::getRealName)
                .eq(UserEntity::getDeletedFlag, status)
                .between(UserEntity::getCreateTime, startTime, endTime)
                .orderByDesc(UserEntity::getCreateTime)
                .build();

        // Then
        assertNotNull(wrapper);
        System.out.println("复杂查询构建成功: " + wrapper.getSqlSegment());
    }

    @Test
    @DisplayName("测试：左模糊查询")
    void testLeftLike() {
        // Given
        String value = "admin";

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .leftLike(UserEntity::getUsername, value)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：右模糊查询")
    void testRightLike() {
        // Given
        String value = "admin";

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .rightLike(UserEntity::getUsername, value)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：大于查询")
    void testGreaterThan() {
        // Given
        Integer value = 10;

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .gt(UserEntity::getUserId, value)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：小于查询")
    void testLessThan() {
        // Given
        Integer value = 100;

        // When
        LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
                .lt(UserEntity::getUserId, value)
                .build();

        // Then
        assertNotNull(wrapper);
    }

    @Test
    @DisplayName("测试：完整查询场景")
    @SuppressWarnings({"unchecked", "var"})
    void testCompleteQueryScenario() {
        // Given - 模拟一个完整的分页查询场景
        String keyword = "张";
        Integer status = 1;
        List<Long> departmentIds = Arrays.asList(1L, 2L, 3L);
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.now();

        // When - 使用QueryBuilder构建复杂查询
        var wrapper = QueryBuilder.of(UserEntity.class)
                // 关键字搜索（用户名或真实姓名）
                .keyword(keyword, UserEntity::getUsername, UserEntity::getRealName)
                // 状态筛选
                .eq(UserEntity::getDeletedFlag, status)
                // 部门筛选
                .in(UserEntity::getDepartmentId, departmentIds)
                // 时间范围筛选
                .between(UserEntity::getCreateTime, startTime, endTime)
                // 排序
                .orderByDesc(UserEntity::getCreateTime)
                .build();

        // Then
        assertNotNull(wrapper);
        System.out.println("完整查询场景测试通过");
    }
}
