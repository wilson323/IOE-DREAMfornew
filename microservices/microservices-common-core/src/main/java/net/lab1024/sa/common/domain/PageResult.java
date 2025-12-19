package net.lab1024.sa.common.domain;

import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页结果DTO
 * <p>
 * 统一的分页查询响应格式
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：
 * - 统一的分页结构
 * - 标准化的分页信息
 * - 完整的数据列表
 * - 支持records字段（文档要求）和list字段（向后兼容）
 * </p>
 *
 * @param <T> 数据项类型
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表（向后兼容字段）
     * <p>
     * 使用@JsonAlias支持records和list两个字段名
     * </p>
     */
    @JsonAlias({"records", "list"})
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 获取数据列表（向后兼容方法）
     * <p>
     * 为了保持向后兼容，提供getList方法
     * </p>
     *
     * @return 数据列表
     */
    public List<T> getList() {
        return records;
    }

    /**
     * 设置数据列表（向后兼容方法）
     * <p>
     * 为了保持向后兼容，提供setList方法
     * </p>
     *
     * @param list 数据列表
     */
    public void setList(List<T> list) {
        this.records = list;
    }

    /**
     * 获取总页数（向后兼容方法）
     * <p>
     * 为了保持向后兼容，提供getPages方法
     * </p>
     *
     * @return 总页数
     */
    public Integer getPages() {
        return totalPages;
    }

    /**
     * 设置总页数（向后兼容方法）
     * <p>
     * 为了保持向后兼容，提供setPages方法
     * </p>
     *
     * @param pages 总页数
     */
    public void setPages(Integer pages) {
        this.totalPages = pages;
    }

    /**
     * 从MyBatis-Plus Page对象创建分页结果
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @param page MyBatis-Plus分页对象
     * @param <T>  数据项类型
     * @return 分页结果
     */
    public static <T> PageResult<T> from(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setTotalPages((int) page.getPages());
        return result;
    }

    /**
     * 创建分页结果
     *
     * @param list     数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param <T>      数据项类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        // 计算总页数
        int pages = (int) Math.ceil((double) total / pageSize);
        if (pages < 0) {
            pages = 0;
        }

        return PageResult.<T>builder()
                .records(list)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(pages)
                .build();
    }

    /**
     * 创建空分页结果
     *
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param <T>      数据项类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return PageResult.<T>builder()
                .records(List.of())
                .total(0L)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(0)
                .build();
    }
}
