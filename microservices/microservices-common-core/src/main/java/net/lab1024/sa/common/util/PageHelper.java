package net.lab1024.sa.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 分页查询辅助工具类
 * <p>
 * 提供游标分页和传统分页的统一接口
 * 自动选择最优分页方式（游标分页优先）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
public class PageHelper {

    /**
     * 游标分页查询（基于ID）
     * <p>
     * 推荐使用此方法进行分页查询，性能优于传统分页
     * </p>
     *
     * @param mapper DAO接口
     * @param wrapper 查询条件包装器
     * @param pageSize 每页大小
     * @param lastId 上一页最后一条记录的ID（首次查询传null）
     * @param getIdFunc 获取ID的Lambda表达式（如：Entity::getId）
     * @param getCreateTimeFunc 获取创建时间的Lambda表达式（如：Entity::getCreateTime）
     * @param <T> 实体类型
     * @return 游标分页结果
     */
    public static <T> CursorPagination.CursorPageResult<T> cursorPageById(
            BaseMapper<T> mapper,
            LambdaQueryWrapper<T> wrapper,
            Integer pageSize,
            Long lastId,
            SFunction<T, Long> getIdFunc,
            SFunction<T, LocalDateTime> getCreateTimeFunc) {

        // 1. 参数验证
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大100条
        }

        // 2. 添加游标条件
        if (lastId != null && lastId > 0) {
            wrapper.lt(getIdFunc, lastId);
        }

        // 3. 按ID倒序排序
        wrapper.orderByDesc(getIdFunc);

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
            if (lastRecord != null) {
                try {
                    // 通过反射获取ID和createTime值
                    java.lang.reflect.Method getId = lastRecord.getClass().getMethod("getId");
                    Object id = getId.invoke(lastRecord);
                    finalLastId = id != null ? Long.valueOf(id.toString()) : null;

                    java.lang.reflect.Method getCreateTime = lastRecord.getClass().getMethod("getCreateTime");
                    Object createTime = getCreateTime.invoke(lastRecord);
                    finalLastTime = createTime instanceof LocalDateTime ? (LocalDateTime) createTime : null;
                } catch (Exception e) {
                    log.debug("[分页辅助] 通过反射获取ID和createTime失败, entityClass={}", lastRecord.getClass().getName(), e);
                }
            }
        }

        // 8. 构建返回结果
        return CursorPagination.CursorPageResult.<T>builder()
                .list(list)
                .hasNext(hasNext)
                .lastId(finalLastId)
                .lastTime(finalLastTime)
                .size(list.size())
                .build();
    }

    /**
     * 游标分页查询（基于时间）
     * <p>
     * 适用于需要按时间排序的场景
     * </p>
     *
     * @param mapper DAO接口
     * @param wrapper 查询条件包装器
     * @param pageSize 每页大小
     * @param lastTime 上一页最后一条记录的时间（首次查询传null）
     * @param getCreateTimeFunc 获取创建时间的Lambda表达式（如：Entity::getCreateTime）
     * @param getIdFunc 获取ID的Lambda表达式（如：Entity::getId）
     * @param <T> 实体类型
     * @return 游标分页结果
     */
    public static <T> CursorPagination.CursorPageResult<T> cursorPageByTime(
            BaseMapper<T> mapper,
            LambdaQueryWrapper<T> wrapper,
            Integer pageSize,
            LocalDateTime lastTime,
            SFunction<T, LocalDateTime> getCreateTimeFunc,
            SFunction<T, Long> getIdFunc) {

        // 1. 参数验证
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大100条
        }

        // 2. 添加时间游标条件
        if (lastTime != null) {
            wrapper.lt(getCreateTimeFunc, lastTime);
        }

        // 3. 按时间倒序排序
        wrapper.orderByDesc(getCreateTimeFunc);

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
            if (lastRecord != null) {
                try {
                    // 通过反射获取ID和createTime值
                    java.lang.reflect.Method getId = lastRecord.getClass().getMethod("getId");
                    Object id = getId.invoke(lastRecord);
                    finalLastId = id != null ? Long.valueOf(id.toString()) : null;

                    java.lang.reflect.Method getCreateTime = lastRecord.getClass().getMethod("getCreateTime");
                    Object createTime = getCreateTime.invoke(lastRecord);
                    finalLastTime = createTime instanceof LocalDateTime ? (LocalDateTime) createTime : null;
                } catch (Exception e) {
                    log.debug("[分页辅助] 通过反射获取ID和createTime失败, entityClass={}", lastRecord.getClass().getName(), e);
                }
            }
        }

        // 8. 构建返回结果
        return CursorPagination.CursorPageResult.<T>builder()
                .list(list)
                .hasNext(hasNext)
                .lastId(finalLastId)
                .lastTime(finalLastTime)
                .size(list.size())
                .build();
    }

    /**
     * 智能分页选择
     * <p>
     * 根据页码自动选择分页方式：
     * - 页码 <= 100：使用传统分页
     * - 页码 > 100：使用游标分页（基于ID或时间）
     * </p>
     *
     * @param mapper DAO接口
     * @param wrapper 查询条件包装器
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param lastId 上一页最后一条记录的ID（用于游标分页）
     * @param lastTime 上一页最后一条记录的时间（用于游标分页）
     * @param getIdFunc 获取ID的Lambda表达式
     * @param getCreateTimeFunc 获取创建时间的Lambda表达式
     * @param <T> 实体类型
     * @return 分页结果
     */
    public static <T> CursorPagination.CursorPageResult<T> smartPage(
            BaseMapper<T> mapper,
            LambdaQueryWrapper<T> wrapper,
            Integer pageNum,
            Integer pageSize,
            Long lastId,
            LocalDateTime lastTime,
            SFunction<T, Long> getIdFunc,
            SFunction<T, LocalDateTime> getCreateTimeFunc) {

        // 如果页码大于100，使用游标分页
        if (pageNum != null && pageNum > 100) {
            if (lastId != null) {
                return cursorPageById(mapper, wrapper, pageSize, lastId, getIdFunc, getCreateTimeFunc);
            } else if (lastTime != null) {
                return cursorPageByTime(mapper, wrapper, pageSize, lastTime, getCreateTimeFunc, getIdFunc);
            } else {
                // 如果没有游标，尝试基于时间的第一页查询
                return cursorPageByTime(mapper, wrapper, pageSize, null, getCreateTimeFunc, getIdFunc);
            }
        }

        // 页码 <= 100，使用传统分页（MyBatis-Plus）
        // 注意：这里返回的是CursorPageResult，但实际应该返回传统PageResult
        // 为了统一接口，这里建议直接使用MyBatis-Plus的Page
        throw new UnsupportedOperationException("页码 <= 100时，请使用MyBatis-Plus的Page类进行分页查询");
    }
}

