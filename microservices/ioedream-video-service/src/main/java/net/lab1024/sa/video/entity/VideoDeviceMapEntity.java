package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视频设备地图实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_video_device_map")
@Schema(description = "视频设备地图实体")
public class VideoDeviceMapEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "地图ID")
    private Long id;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "地图类型 1-平面图 2-3D地图 3-BIM地图")
    private Integer mapType;

    @Schema(description = "坐标类型 1-像素坐标 2-地理坐标(经纬度)")
    private Integer coordinateType;

    @Schema(description = "X坐标(像素或经度)")
    private BigDecimal xCoordinate;

    @Schema(description = "Y坐标(像素或纬度)")
    private BigDecimal yCoordinate;

    @Schema(description = "Z坐标(高度，3D/BIM使用)")
    private BigDecimal zCoordinate;

    @Schema(description = "所属区域ID")
    private Long areaId;

    @Schema(description = "楼层(0-地下层 1-N楼层)")
    private Integer floorLevel;

    @Schema(description = "区域编码")
    private String zoneCode;

    @Schema(description = "标记类型 default/camera/ptz/dome")
    private String markerType;

    @Schema(description = "标记颜色")
    private String markerColor;

    @Schema(description = "标记大小 1-5")
    private Integer markerSize;

    @Schema(description = "自定义图标URL")
    private String iconUrl;

    @Schema(description = "平面图图片ID")
    private Long mapImageId;

    @Schema(description = "地图名称")
    private String mapName;

    @Schema(description = "地图缩放比例")
    private BigDecimal mapScale;

    @Schema(description = "显示状态 0-隐藏 1-显示 2-维护")
    private Integer displayStatus;

    @Schema(description = "点击行为 1-播放视频 2-查看详情 3-云台控制")
    private Integer clickAction;

    @Schema(description = "是否启用弹窗 0-否 1-是")
    private Integer enablePopup;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @Schema(description = "扩展属性(JSON格式)")
    private String extendedAttributes;

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
    @Schema(description = "删除标记 0-未删除 1-已删除")
    private Integer deletedFlag;
}
