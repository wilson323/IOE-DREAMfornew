package net.lab1024.sa.common.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游标分页结果DTO
 * <p>
 * 用于返回游标分页查询结果，包含下一页游标信息
 * 严格遵循CLAUDE.md规范：
 * - 统一的分页结果结构
 * - 标准化的游标信息
 * </p>
 *
 * @param <T> 数据类型
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 下一页游标时间（最后一条记录的排序字段值）
     */
    private LocalDateTime nextCursorTime;

    /**
     * 下一页游标ID（最后一条记录的ID）
     */
    private Long nextCursorId;

    /**
     * 创建游标分页结果
     *
     * @param list           数据列表
     * @param pageSize       每页大小
     * @param nextCursorTime 下一页游标时间
     * @param nextCursorId   下一页游标ID
     * @param <T>            数据类型
     * @return 游标分页结果
     */
    public static <T> CursorPageResult<T> of(List<T> list, Integer pageSize, 
                                              LocalDateTime nextCursorTime, Long nextCursorId) {
        boolean hasNext = list.size() == pageSize && nextCursorTime != null;
        return CursorPageResult.<T>builder()
                .list(list)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .nextCursorTime(nextCursorTime)
                .nextCursorId(nextCursorId)
                .build();
    }

    /**
     * 创建最后一页结果
     *
     * @param list     数据列表
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 游标分页结果
     */
    public static <T> CursorPageResult<T> lastPage(List<T> list, Integer pageSize) {
        return CursorPageResult.<T>builder()
                .list(list)
                .pageSize(pageSize)
                .hasNext(false)
                .nextCursorTime(null)
                .nextCursorId(null)
                .build();
    }
}

