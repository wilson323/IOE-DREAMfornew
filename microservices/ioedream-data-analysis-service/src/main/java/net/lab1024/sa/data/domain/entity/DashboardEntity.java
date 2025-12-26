package net.lab1024.sa.data.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据仪表板实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_data_dashboard")
@Schema(description = "数据仪表板实体")
public class DashboardEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "仪表板ID")
    private Long dashboardId;

    @Schema(description = "仪表板名称")
    private String dashboardName;

    @Schema(description = "仪表板编码")
    private String dashboardCode;

    @Schema(description = "布局配置")
    private String layoutConfig;

    @Schema(description = "权限配置")
    private String permissionConfig;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "仪表板描述")
    private String description;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "最后刷新时间")
    private LocalDateTime refreshTime;

    @TableLogic
    @Schema(description = "删除标记")
    private Integer deletedFlag;

    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;
}
