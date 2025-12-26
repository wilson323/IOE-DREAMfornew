package net.lab1024.sa.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Collections;

/**
 * 分页结果DTO - 重构版本
 * <p>
 * 解决原有PageResult的依赖混乱问题，提供简洁统一的分页格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-22
 */
@Data
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
    @JsonProperty("total")
    private Long total;

    /**
     * 当前页码
     */
    @JsonProperty("pageNum")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @JsonProperty("pageSize")
    private Integer pageSize;

    /**
     * 总页数
     */
    @JsonProperty("pages")
    private Integer pages;

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        if (list == null) {
            list = Collections.emptyList();
        }

        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(total != null ? total : 0L);
        pageResult.setPageNum(pageNum != null ? pageNum : 1);
        pageResult.setPageSize(pageSize != null ? pageSize : 10);

        // 计算总页数
        if (total != null && pageSize != null && pageSize > 0) {
            int pages = (int) Math.ceil((double) total / pageSize);
            pageResult.setPages(pages);
        } else {
            pageResult.setPages(0);
        }

        return pageResult;
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return of(Collections.emptyList(), 0L, 1, 10);
    }

    /**
     * 判断是否有数据
     */
    public boolean hasData() {
        return list != null && !list.isEmpty();
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return !hasData();
    }
}