package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地图热点实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_video_map_hotspot")
@Schema(description = "地图热点实体")
public class VideoMapHotspotEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "热点ID")
    private Long id;

    @Schema(description = "热点UUID")
    private String hotspotUuid;

    @Schema(description = "所属地图ID")
    private Long mapImageId;

    @Schema(description = "热点名称")
    private String hotspotName;

    @Schema(description = "热点类型 camera/zone/entrance/exit/landmark")
    private String hotspotType;

    @Schema(description = "X坐标")
    private BigDecimal xCoordinate;

    @Schema(description = "Y坐标")
    private BigDecimal yCoordinate;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "图标大小 1-5")
    private Integer iconSize;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "关联设备ID")
    private Long deviceId;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "区域坐标(JSON数组)")
    private String areaCoordinates;

    @Schema(description = "点击行为 1-播放视频 2-查看设备 3-跳转链接")
    private Integer clickAction;

    @Schema(description = "行为数据(URL或设备ID等)")
    private String actionData;

    @Schema(description = "提示文本")
    private String tooltipText;

    @Schema(description = "显示状态 0-隐藏 1-显示")
    private Integer displayStatus;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
