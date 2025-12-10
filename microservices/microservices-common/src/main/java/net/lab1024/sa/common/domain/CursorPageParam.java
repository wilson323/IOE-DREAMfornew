package net.lab1024.sa.common.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游标分页参数DTO
 * <p>
 * 用于优化深度分页查询，避免使用OFFSET导致的性能问题
 * 严格遵循CLAUDE.md规范：
 * - 统一的分页参数结构
 * - 标准化的游标分页信息
 * - 完整的参数验证
 * </p>
 * <p>
 * 使用场景：
 * - 替代传统的LIMIT offset, size分页方式
 * - 适用于按时间、ID等有序字段分页的场景
 * - 性能优于深度分页（offset > 1000）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页大小
     */
    public static final Integer MAX_PAGE_SIZE = 1000;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize;

    /**
     * 游标值（上一页最后一条记录的排序字段值）
     * <p>
     * 例如：如果按createTime排序，则cursor为上一页最后一条记录的createTime
     * </p>
     */
    private LocalDateTime cursorTime;

    /**
     * 游标ID（上一页最后一条记录的ID）
     * <p>
     * 用于处理相同时间的情况，确保分页准确性
     * </p>
     */
    private Long cursorId;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderDirection;

    /**
     * 创建默认游标分页参数
     *
     * @return 默认游标分页参数
     */
    public static CursorPageParam defaultPage() {
        return CursorPageParam.builder()
                .pageSize(DEFAULT_PAGE_SIZE)
                .orderDirection("DESC")
                .build();
    }

    /**
     * 创建游标分页参数
     *
     * @param pageSize 每页大小
     * @return 游标分页参数
     */
    public static CursorPageParam of(Integer pageSize) {
        return CursorPageParam.builder()
                .pageSize(pageSize != null && pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE)
                .orderDirection("DESC")
                .build();
    }

    /**
     * 创建带游标的分页参数
     *
     * @param pageSize   每页大小
     * @param cursorTime 游标时间
     * @param cursorId   游标ID
     * @return 游标分页参数
     */
    public static CursorPageParam of(Integer pageSize, LocalDateTime cursorTime, Long cursorId) {
        return CursorPageParam.builder()
                .pageSize(pageSize != null && pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE)
                .cursorTime(cursorTime)
                .cursorId(cursorId)
                .orderDirection("DESC")
                .build();
    }

    /**
     * 验证参数有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return pageSize != null && pageSize > 0 && pageSize <= MAX_PAGE_SIZE;
    }
}

