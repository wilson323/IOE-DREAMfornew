package net.lab1024.sa.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Function;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * 统一查询构建器
 * <p>
 * 封装MyBatis-Plus的LambdaQueryWrapper构建逻辑，减少重复代码
 * 支持链式调用，提供常见查询条件的统一封装
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * LambdaQueryWrapper<UserEntity> wrapper = QueryBuilder.of(UserEntity.class)
 *     .keyword(form.getKeyword(), UserEntity::getUsername, UserEntity::getRealName)
 *     .eq(UserEntity::getStatus, form.getStatus())
 *     .eq(UserEntity::getAreaId, form.getAreaId())
 *     .in(UserEntity::getDepartmentId, form.getDepartmentIds())
 *     .between(UserEntity::getCreateTime, form.getStartTime(), form.getEndTime())
 *     .orderByDesc(UserEntity::getCreateTime)
 *     .build();
 * }</pre>
 * </p>
 *
 * @param <T> 实体类型
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
@Data
public class QueryBuilder<T> {

    /**
     * MyBatis-Plus查询条件包装器
     */
    private final LambdaQueryWrapper<T> wrapper;

    /**
     * 实体类型
     */
    private final Class<T> entityClass;

    /**
     * 私有构造函数
     *
     * @param entityClass 实体类型
     */
    private QueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.wrapper = new LambdaQueryWrapper<>();
    }

    /**
     * 创建查询构建器实例
     *
     * @param entityClass 实体类型
     * @param <T>         实体类型
     * @return 查询构建器实例
     */
    public static <T> QueryBuilder<T> of(Class<T> entityClass) {
        return new QueryBuilder<>(entityClass);
    }

    /**
     * 关键字查询（支持多字段OR查询）
     * <p>
     * 如果关键字不为空，则对指定的多个字段进行LIKE查询，字段之间使用OR连接
     * </p>
     *
     * @param value 关键字值
     * @param fields 要查询的字段（可变参数）
     * @param <R>    字段类型
     * @return this
     */
    @SafeVarargs
    public final <R> QueryBuilder<T> keyword(String value, SFunction<T, R>... fields) {
        if (StringUtils.isNotBlank(value) && fields != null && fields.length > 0) {
            wrapper.and(w -> {
                for (int i = 0; i < fields.length; i++) {
                    if (i == 0) {
                        w.like(fields[i], value);
                    } else {
                        w.or().like(fields[i], value);
                    }
                }
            });
        }
        return this;
    }

    /**
     * 左模糊查询（字段值以指定值开头）
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型
     * @return this
     */
    public <R> QueryBuilder<T> leftLike(SFunction<T, R> field, String value) {
        if (StringUtils.isNotBlank(value)) {
            wrapper.likeLeft(field, value);
        }
        return this;
    }

    /**
     * 右模糊查询（字段值以指定值结尾）
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型
     * @return this
     */
    public <R> QueryBuilder<T> rightLike(SFunction<T, R> field, String value) {
        if (StringUtils.isNotBlank(value)) {
            wrapper.likeRight(field, value);
        }
        return this;
    }

    /**
     * 等值查询
     * <p>
     * 如果值不为null，则添加等值条件
     * </p>
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型
     * @return this
     */
    public <R> QueryBuilder<T> eq(SFunction<T, R> field, R value) {
        if (value != null) {
            wrapper.eq(field, value);
        }
        return this;
    }

    /**
     * 不等值查询（!=）
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型
     * @return this
     */
    public <R> QueryBuilder<T> ne(SFunction<T, R> field, R value) {
        if (value != null) {
            wrapper.ne(field, value);
        }
        return this;
    }

    /**
     * IN查询
     * <p>
     * 如果集合不为null且不为空，则添加IN条件
     * </p>
     *
     * @param field  字段
     * @param values 值集合
     * @param <R>    字段类型
     * @return this
     */
    public <R> QueryBuilder<T> in(SFunction<T, R> field, Collection<R> values) {
        if (values != null && !values.isEmpty()) {
            wrapper.in(field, values);
        }
        return this;
    }

    /**
     * IN查询（可变参数）
     *
     * @param field  字段
     * @param values 值数组
     * @param <R>    字段类型
     * @return this
     */
    @SafeVarargs
    public final <R> QueryBuilder<T> in(SFunction<T, R> field, R... values) {
        if (values != null && values.length > 0) {
            wrapper.in(field, values);
        }
        return this;
    }

    /**
     * NOT IN查询
     *
     * @param field  字段
     * @param values 值集合
     * @param <R>    字段类型
     * @return this
     */
    public <R> QueryBuilder<T> notIn(SFunction<T, R> field, Collection<R> values) {
        if (values != null && !values.isEmpty()) {
            wrapper.notIn(field, values);
        }
        return this;
    }

    /**
     * 大于查询
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型（必须实现Comparable）
     * @return this
     */
    public <R extends Comparable<? super R>> QueryBuilder<T> gt(SFunction<T, R> field, R value) {
        if (value != null) {
            wrapper.gt(field, value);
        }
        return this;
    }

    /**
     * 大于等于查询
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型（必须实现Comparable）
     * @return this
     */
    public <R extends Comparable<? super R>> QueryBuilder<T> ge(SFunction<T, R> field, R value) {
        if (value != null) {
            wrapper.ge(field, value);
        }
        return this;
    }

    /**
     * 小于查询
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型（必须实现Comparable）
     * @return this
     */
    public <R extends Comparable<? super R>> QueryBuilder<T> lt(SFunction<T, R> field, R value) {
        if (value != null) {
            wrapper.lt(field, value);
        }
        return this;
    }

    /**
     * 小于等于查询
     *
     * @param field 字段
     * @param value 值
     * @param <R>   字段类型（必须实现Comparable）
     * @return this
     */
    public <R extends Comparable<? super R>> QueryBuilder<T> le(SFunction<T, R> field, R value) {
        if (value != null) {
            wrapper.le(field, value);
        }
        return this;
    }

    /**
     * BETWEEN查询
     * <p>
     * 如果开始值和结束值都不为null，则添加BETWEEN条件
     * </p>
     *
     * @param field 字段
     * @param start 开始值
     * @param end   结束值
     * @param <R>   字段类型（必须实现Comparable）
     * @return this
     */
    public <R extends Comparable<? super R>> QueryBuilder<T> between(SFunction<T, R> field, R start, R end) {
        if (start != null && end != null) {
            wrapper.between(field, start, end);
        }
        return this;
    }

    /**
     * 时间范围查询（LocalDateTime类型）
     * <p>
     * 如果开始时间和结束时间都不为null，则添加BETWEEN条件
     * </p>
     *
     * @param field      字段
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return this
     */
    public QueryBuilder<T> between(SFunction<T, LocalDateTime> field, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            wrapper.between(field, startTime, endTime);
        }
        return this;
    }

    /**
     * 升序排序
     *
     * @param field 字段
     * @param <R>   字段类型
     * @return this
     */
    public <R> QueryBuilder<T> orderByAsc(SFunction<T, R> field) {
        wrapper.orderByAsc(true, field);
        return this;
    }

    /**
     * 降序排序
     *
     * @param field 字段
     * @param <R>   字段类型
     * @return this
     */
    public <R> QueryBuilder<T> orderByDesc(SFunction<T, R> field) {
        wrapper.orderByDesc(true, field);
        return this;
    }

    /**
     * 构建查询条件
     *
     * @return LambdaQueryWrapper查询条件
     */
    public LambdaQueryWrapper<T> build() {
        return wrapper;
    }

    /**
     * 构建查询条件并返回LambdaQueryWrapper（别名方法）
     *
     * @return LambdaQueryWrapper查询条件
     */
    public LambdaQueryWrapper<T> getWrapper() {
        return wrapper;
    }
}
