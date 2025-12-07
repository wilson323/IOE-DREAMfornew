package net.lab1024.sa.common.domain;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页参数DTO
 * <p>
 * 统一的分页查询请求参数格式
 * 严格遵循CLAUDE.md规范：
 * - 统一的分页参数结构
 * - 标准化的分页信息
 * - 完整的参数验证
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
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页大小
     */
    public static final Integer MAX_PAGE_SIZE = 1000;

    /**
     * 当前页码（从1开始）
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize;

    /**
     * 创建默认分页参数
     *
     * @return 默认分页参数
     */
    public static PageParam defaultPage() {
        return PageParam.builder()
                .pageNum(DEFAULT_PAGE_NUM)
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
    }

    /**
     * 创建分页参数
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页参数
     */
    public static PageParam of(Integer pageNum, Integer pageSize) {
        // 参数验证和默认值
        if (pageNum == null || pageNum < 1) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        // 限制最大每页大小
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        return PageParam.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 验证并规范化分页参数
     * <p>
     * 如果参数无效，使用默认值
     * </p>
     */
    public void normalize() {
        if (this.pageNum == null || this.pageNum < 1) {
            this.pageNum = DEFAULT_PAGE_NUM;
        }
        if (this.pageSize == null || this.pageSize < 1) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        }
        // 限制最大每页大小
        if (this.pageSize > MAX_PAGE_SIZE) {
            this.pageSize = MAX_PAGE_SIZE;
        }
    }
}

