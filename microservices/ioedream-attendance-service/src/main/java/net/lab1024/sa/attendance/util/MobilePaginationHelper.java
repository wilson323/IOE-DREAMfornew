package net.lab1024.sa.attendance.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * 移动端分页辅助工具
 * <p>
 * 提供统一的分页处理逻辑，包括参数验证、Page对象创建、分页元数据计算等
 * 支持游标分页和传统偏移量分页
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class MobilePaginationHelper {

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最小每页大小
     */
    private static final int MIN_PAGE_SIZE = 1;

    /**
     * 最大每页大小（防止一次查询过多数据）
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 创建MyBatis-Plus分页对象
     * <p>
     * 自动处理分页参数的验证和默认值
     * </p>
     *
     * @param pageNum 页码（可选，为null时使用默认值）
     * @param pageSize 每页大小（可选，为null时使用默认值）
     * @return MyBatis-Plus Page对象
     */
    public <T> Page<T> createPage(Integer pageNum, Integer pageSize) {
        // 参数验证和默认值处理
        int validPageNum = pageNum != null && pageNum > 0 ? pageNum : DEFAULT_PAGE_NUM;
        int validPageSize = validatePageSize(pageSize);

        log.debug("[分页工具] 创建分页对象: pageNum={}, pageSize={}", validPageNum, validPageSize);

        // 创建Page对象（MyBatis-Plus分页从第1页开始）
        return new Page<>(validPageNum, validPageSize);
    }

    /**
     * 验证并规范化每页大小
     * <p>
     * 确保pageSize在合理范围内
     * </p>
     *
     * @param pageSize 原始每页大小
     * @return 规范化后的每页大小
     */
    private int validatePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < MIN_PAGE_SIZE) {
            log.warn("[分页工具] pageSize过小，使用最小值: requested={}, min={}", pageSize, MIN_PAGE_SIZE);
            return MIN_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            log.warn("[分页工具] pageSize过大，使用最大值: requested={}, max={}", pageSize, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 构建移动端分页响应结果
     * <p>
     * 包含分页元数据：总记录数、总页数、当前页、是否有下一页等
     * </p>
     *
     * @param page MyBatis-Plus分页结果
     * @param dataTransform 数据转换函数（从实体到VO）
     * @return 分页结果对象
     */
    public <E, V> MobilePageResult<V> buildPageResult(Page<E> page, Function<E, V> dataTransform) {
        List<V> records = page.getRecords().stream()
                .map(dataTransform)
                .collect(Collectors.toList());

        return MobilePageResult.<V>builder()
                .records(records)
                .totalCount(page.getTotal())
                .pageNum((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .totalPages(page.getPages())
                .hasNext(page.getCurrent() < page.getPages())
                .hasPrev(page.getCurrent() > 1)
                .build();
    }

    /**
     * 移动端分页响应对象
     * <p>
     * 包含分页元数据和数据列表
     * </p>
     *
     * @param <T> 数据类型
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MobilePageResult<T> {
        /**
         * 数据列表
         */
        private List<T> records;

        /**
         * 总记录数
         */
        private Long totalCount;

        /**
         * 当前页码
         */
        private Integer pageNum;

        /**
         * 每页大小
         */
        private Integer pageSize;

        /**
         * 总页数
         */
        private Long totalPages;

        /**
         * 是否有下一页
         */
        private Boolean hasNext;

        /**
         * 是否有上一页
         */
        private Boolean hasPrev;

        /**
         * 获取下一页页码（如果有下一页）
         */
        public Integer getNextPageNum() {
            return hasNext ? pageNum + 1 : null;
        }

        /**
         * 获取上一页页码（如果有上一页）
         */
        public Integer getPrevPageNum() {
            return hasPrev ? pageNum - 1 : null;
        }

        /**
         * 是否为第一页
         */
        public boolean isFirstPage() {
            return pageNum == 1;
        }

        /**
         * 是否为最后一页
         */
        public boolean isLastPage() {
            return !hasNext;
        }
    }

    /**
     * 游标分页辅助类
     * <p>
     * 用于大数据量场景下的无限滚动加载
     * 基于唯一ID和时间戳进行游标分页
     * </p>
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CursorPagination {
        /**
         * 游标值（最后一条记录的ID或时间戳）
         */
        private String cursor;

        /**
         * 每页大小
         */
        private Integer limit;

        /**
         * 排序方向（ASC/DESC）
         */
        private String order;

        /**
         * 从游标值解析Long类型ID
         */
        public Long parseCursorAsLong() {
            if (cursor == null || cursor.trim().isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(cursor);
            } catch (NumberFormatException e) {
                log.warn("[游标分页] 游标值格式错误: cursor={}", cursor);
                return null;
            }
        }

        /**
         * 创建下一页游标
         */
        public static CursorPagination createNextCursor(Long lastId, Integer limit, String order) {
            return CursorPagination.builder()
                    .cursor(lastId != null ? String.valueOf(lastId) : null)
                    .limit(limit)
                    .order(order)
                    .build();
        }
    }

    /**
     * 创建游标分页对象
     *
     * @param cursor 游标值
     * @param limit 每页大小
     * @param order 排序方向
     * @return 游标分页对象
     */
    public CursorPagination createCursorPagination(String cursor, Integer limit, String order) {
        int validLimit = validatePageSize(limit);
        String validOrder = order != null && order.equalsIgnoreCase("ASC") ? "ASC" : "DESC";

        return CursorPagination.builder()
                .cursor(cursor)
                .limit(validLimit)
                .order(validOrder)
                .build();
    }

    /**
     * 构建游标分页响应结果
     *
     * @param records 数据列表
     * @param totalCount 总记录数（可选，游标分页通常不需要）
     * @param limit 每页大小
     * @param hasNext 是否有下一页
     * @param lastCursor 下一页游标
     * @return 游标分页结果
     */
    public <T> MobileCursorPageResult<T> buildCursorResult(List<T> records, Long totalCount, Integer limit,
            boolean hasNext, String lastCursor) {
        return MobileCursorPageResult.<T>builder()
                .records(records)
                .totalCount(totalCount)
                .limit(limit)
                .hasNext(hasNext)
                .nextCursor(hasNext ? lastCursor : null)
                .build();
    }

    /**
     * 移动端游标分页响应对象
     * <p>
     * 用于无限滚动加载场景
     * </p>
     *
     * @param <T> 数据类型
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MobileCursorPageResult<T> {
        /**
         * 数据列表
         */
        private List<T> records;

        /**
         * 总记录数（可选，游标分页通常不返回总数以提高性能）
         */
        private Long totalCount;

        /**
         * 每页大小
         */
        private Integer limit;

        /**
         * 是否有下一页
         */
        private Boolean hasNext;

        /**
         * 下一页游标（用于加载下一页）
         */
        private String nextCursor;

        /**
         * 是否为第一页
         */
        public boolean isFirstPage() {
            return records.isEmpty() || nextCursor == null;
        }
    }
}
