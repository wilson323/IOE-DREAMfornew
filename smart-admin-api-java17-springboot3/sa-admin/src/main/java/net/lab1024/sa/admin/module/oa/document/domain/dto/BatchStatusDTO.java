package net.lab1024.sa.admin.module.oa.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量状态更新DTO
 * 严格遵循repowiki规范：定义批量状态更新的数据传输对象
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "批量状态更新DTO")
public class BatchStatusDTO {

    @Schema(description = "文档ID列表", required = true)
    @NotNull(message = "文档ID列表不能为空")
    private List<Long> documentIds;

    @Schema(description = "目标状态", example = "PUBLISHED", required = true)
    @NotNull(message = "目标状态不能为空")
    private String status;

    @Schema(description = "更新原因", example = "批量发布")
    private String reason;

    @Schema(description = "是否发送通知", example = "true")
    private Boolean sendNotification;

    /**
     * 状态枚举
     */
    public static class Status {
        public static final String DRAFT = "DRAFT";
        public static final String PUBLISHED = "PUBLISHED";
        public static final String ARCHIVED = "ARCHIVED";
        public static final String DELETED = "DELETED";
    }
}