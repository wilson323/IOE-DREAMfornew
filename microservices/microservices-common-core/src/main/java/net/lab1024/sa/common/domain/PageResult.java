package net.lab1024.sa.common.domain;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页响应结果
 * <p>
 * 统一的分页响应类，支持list和records两种字段名（向后兼容）
 * </p>
 *
 * @param <T> 数据类型
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应结果")
public class PageResult<T> {

    @Schema(description = "数据列表（兼容list和records）")
    private List<T> list;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "总页数", example = "5")
    private Integer pages;

    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrev;

    /**
     * 获取records（向后兼容）
     */
    public List<T> getRecords() {
        return list;
    }

    /**
     * 设置records（向后兼容）
     */
    public void setRecords(List<T> records) {
        this.list = records;
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
                .list(List.of())
                .total(0L)
                .pageNum(1)
                .pageSize(20)
                .pages(0)
                .hasNext(false)
                .hasPrev(false)
                .build();
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(pages)
                .hasNext(pageNum < pages)
                .hasPrev(pageNum > 1)
                .build();
    }
}
