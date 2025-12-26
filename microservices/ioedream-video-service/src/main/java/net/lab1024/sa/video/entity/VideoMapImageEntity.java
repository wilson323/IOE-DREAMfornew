package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 视频地图图片实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_video_map_image")
@Schema(description = "视频地图图片实体")
public class VideoMapImageEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "图片ID")
    private Long id;

    @Schema(description = "图片UUID")
    private String imageUuid;

    @Schema(description = "图片名称")
    private String imageName;

    @Schema(description = "图片类型 floor/birdview/panorama")
    private String imageType;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "所属区域ID")
    private Long areaId;

    @Schema(description = "楼层")
    private Integer floorLevel;

    @Schema(description = "地图宽度(像素)")
    private Integer mapWidth;

    @Schema(description = "地图高度(像素)")
    private Integer mapHeight;

    @Schema(description = "坐标系统 pixel/geographic")
    private String coordinateSystem;

    @Schema(description = "原点X坐标")
    private java.math.BigDecimal originX;

    @Schema(description = "原点Y坐标")
    private java.math.BigDecimal originY;

    @Schema(description = "每米像素数")
    private java.math.BigDecimal pixelsPerMeter;

    @Schema(description = "状态 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "是否默认地图")
    private Integer isDefault;

    @Schema(description = "描述")
    private String description;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "更新人ID")
    private Long updateUserId;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
