package net.lab1024.sa.data.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据报表实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_data_report")
@Schema(description = "数据报表实体")
public class ReportEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "报表ID")
    private Long reportId;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "报表类型")
    private String reportType;

    @Schema(description = "业务模块")
    private String businessModule;

    @Schema(description = "数据源类型")
    private String sourceType;

    @Schema(description = "表名或API路径")
    private String sourceName;

    @Schema(description = "数据源配置")
    private String sourceConfig;

    @Schema(description = "字段映射")
    private String fieldMapping;

    @Schema(description = "查询配置")
    private String queryConfig;

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

    @Schema(description = "报表描述")
    private String description;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记")
    private Integer deletedFlag;

    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;
}
