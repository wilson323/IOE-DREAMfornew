package net.lab1024.sa.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游标分页工具类
 * <p>
 * 用于优化深度分页查询性能，避免使用LIMIT offset, size方式
 * 使用基于ID或时间戳的游标分页，性能更优
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public class CursorPagination {

    /**
     * 游标分页请求参数
     *
     * @param <T> 实体类型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursorPageRequest<T> {
        /**
         * 每页大小（默认20，最大100）
         */
        @Builder.Default
        private Integer pageSize = 20;

        /**
         * 上一页最后一条记录的ID（用于基于ID的游标分页）
         */
        private Long lastId;

        /**
         * 上一页最后一条记录的时间（用于基于时间的游标分页）
         */
        private LocalDateTime lastTime;

        /**
         * 是否倒序查询（默认true，按创建时间倒序）
         */
        @Builder.Default
        private Boolean desc = true;
    }

    /**
     * 游标分页响应结果
     *
     * @param <T> 实体类型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CursorPageResult<T> {
        /**
         * 数据列表
         */
        private List<T> list;

        /**
         * 是否还有下一页
         */
        private Boolean hasNext;

        /**
         * 最后一条记录的ID（用于下一页查询）
         */
        private Long lastId;

        /**
         * 最后一条记录的时间（用于下一页查询）
         */
        private LocalDateTime lastTime;

        /**
         * 当前页数据数量
         */
        private Integer size;
    }

    /**
     * 基于ID的游标分页查询
     * <p>
     * 适用于主键为Long类型且有自增特性的表
     * </p>
     *
     * @param mapper DAO接口
     * @param wrapper 查询条件包装器
     * @param request 分页请求参数
     * @param <T> 实体类型（必须包含id和createTime字段）
     * @return 分页结果
     */
    public static <T> CursorPageResult<T> queryByIdCursor(
            BaseMapper<T> mapper,
            LambdaQueryWrapper<T> wrapper,
            CursorPageRequest<T> request) {

        // 1. 参数验证和默认值
        Integer pageSize = request.getPageSize();
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大100条
        }

        // 2. 添加游标条件
        Long lastId = request.getLastId();
        if (lastId != null && lastId > 0) {
            if (Boolean.TRUE.equals(request.getDesc())) {
                wrapper.lt(getIdField(), lastId);
            } else {
                wrapper.gt(getIdField(), lastId);
            }
        }

        // 3. 排序
        if (Boolean.TRUE.equals(request.getDesc())) {
            wrapper.orderByDesc(getIdField());
        } else {
            wrapper.orderByAsc(getIdField());
        }

        // 4. 限制查询数量（多查1条用于判断是否有下一页）
        wrapper.last("LIMIT " + (pageSize + 1));

        // 5. 执行查询
        List<T> list = mapper.selectList(wrapper);

        // 6. 判断是否有下一页
        boolean hasNext = list.size() > pageSize;
        if (hasNext) {
            list = list.subList(0, pageSize); // 移除多余的一条
        }

        // 7. 获取最后一条记录的ID
        Long finalLastId = null;
        LocalDateTime finalLastTime = null;
        if (!list.isEmpty()) {
            T lastRecord = list.get(list.size() - 1);
            finalLastId = getIdValue(lastRecord);
            finalLastTime = getCreateTimeValue(lastRecord);
        }

        // 8. 构建返回结果
        return CursorPageResult.<T>builder()
                .list(list)
                .hasNext(hasNext)
                .lastId(finalLastId)
                .lastTime(finalLastTime)
                .size(list.size())
                .build();
    }

    /**
     * 基于时间的游标分页查询
     * <p>
     * 适用于需要按时间排序的场景，即使主键不是自增也能使用
     * </p>
     *
     * @param mapper DAO接口
     * @param wrapper 查询条件包装器
     * @param request 分页请求参数
     * @param <T> 实体类型（必须包含createTime字段）
     * @return 分页结果
     */
    public static <T> CursorPageResult<T> queryByTimeCursor(
            BaseMapper<T> mapper,
            LambdaQueryWrapper<T> wrapper,
            CursorPageRequest<T> request) {

        // 1. 参数验证和默认值
        Integer pageSize = request.getPageSize();
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大100条
        }

        // 2. 添加时间游标条件
        LocalDateTime lastTime = request.getLastTime();
        if (lastTime != null) {
            if (Boolean.TRUE.equals(request.getDesc())) {
                wrapper.lt(getCreateTimeField(), lastTime);
            } else {
                wrapper.gt(getCreateTimeField(), lastTime);
            }
        }

        // 3. 排序
        if (Boolean.TRUE.equals(request.getDesc())) {
            wrapper.orderByDesc(getCreateTimeField());
        } else {
            wrapper.orderByAsc(getCreateTimeField());
        }

        // 4. 限制查询数量（多查1条用于判断是否有下一页）
        wrapper.last("LIMIT " + (pageSize + 1));

        // 5. 执行查询
        List<T> list = mapper.selectList(wrapper);

        // 6. 判断是否有下一页
        boolean hasNext = list.size() > pageSize;
        if (hasNext) {
            list = list.subList(0, pageSize); // 移除多余的一条
        }

        // 7. 获取最后一条记录的信息
        Long finalLastId = null;
        LocalDateTime finalLastTime = null;
        if (!list.isEmpty()) {
            T lastRecord = list.get(list.size() - 1);
            finalLastId = getIdValue(lastRecord);
            finalLastTime = getCreateTimeValue(lastRecord);
        }

        // 8. 构建返回结果
        return CursorPageResult.<T>builder()
                .list(list)
                .hasNext(hasNext)
                .lastId(finalLastId)
                .lastTime(finalLastTime)
                .size(list.size())
                .build();
    }

    /**
     * 获取ID字段的SFunction（反射实现）
     * <p>
     * 注意：此方法需要实体类有标准的getId()方法
     * 实际使用中可以通过Lambda表达式直接传入
     * </p>
     *
     * @deprecated 实际使用时应该直接传入Lambda表达式，如：Entity::getId
     */
    @Deprecated
    private static <T> com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> getIdField() {
        // 实际使用时，应该在调用处直接传入Lambda表达式，如：Entity::getId
        // 这里返回null作为占位符，实际实现需要使用反射或模板方法
        return null;
    }

    /**
     * 获取createTime字段的SFunction
     *
     * @deprecated 实际使用时应该直接传入Lambda表达式，如：Entity::getCreateTime
     */
    @Deprecated
    private static <T> com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> getCreateTimeField() {
        // 实际使用时，应该在调用处直接传入Lambda表达式，如：Entity::getCreateTime
        return null;
    }

    /**
     * 获取实体的ID值
     */
    private static <T> Long getIdValue(T entity) {
        try {
            java.lang.reflect.Method getId = entity.getClass().getMethod("getId");
            Object id = getId.invoke(entity);
            return id != null ? Long.valueOf(id.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取实体的createTime值
     */
    private static <T> LocalDateTime getCreateTimeValue(T entity) {
        try {
            java.lang.reflect.Method getCreateTime = entity.getClass().getMethod("getCreateTime");
            Object createTime = getCreateTime.invoke(entity);
            return createTime instanceof LocalDateTime ? (LocalDateTime) createTime : null;
        } catch (Exception e) {
            return null;
        }
    }
}

