package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 离线消费配置Entity
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_offline_consume_config")
@Schema(description = "离线消费配置")
public class OfflineConsumeConfigEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型")
    private String configType;

    @Schema(description = "配置描述")
    private String description;

    @Schema(description = "配置分组")
    private String groupName;

    @Schema(description = "启用状态")
    private Integer enabled;

    @Schema(description = "创建人")
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新人")
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
