package net.lab1024.sa.common.domain;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页结果DTO
 * <p>
 * 统一的分页查询响应格式
 * 严格遵循CLAUDE.md规范：
 * - 统一的分页结构
 * - 标准化的分页信息
 * - 完整的数据列表
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
     * 数据列表
     */
    private List<T> list;

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
    private Integer pages;

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
                .list(list)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(pages)
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
                .list(List.of())
                .total(0L)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(0)
                .build();
    }
}
