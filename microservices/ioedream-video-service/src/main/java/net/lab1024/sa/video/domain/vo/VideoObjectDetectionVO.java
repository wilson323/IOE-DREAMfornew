package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频目标检测视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
@Schema(description = "视频目标检测视图对象")
public class VideoObjectDetectionVO {

    /**
     * 检测记录ID
     */
    @Schema(description = "检测记录ID", example = "1689854272000000001")
    private Long detectionId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "CAM001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "大门口摄像头")
    private String deviceName;

    /**
     * 通道ID
     */
    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    /**
     * 通道名称
     */
    @Schema(description = "通道名称", example = "主通道")
    private String channelName;

    /**
     * 检测时间
     */
    @Schema(description = "检测时间", example = "2025-12-16T10:30:00")
    private LocalDateTime detectionTime;

    /**
     * 检测时间格式化
     */
    @Schema(description = "检测时间格式化", example = "2025-12-16 10:30:00")
    private String detectionTimeFormatted;

    /**
     * 检测算法
     */
    @Schema(description = "检测算法", example = "YOLOv8")
    private String detectionAlgorithm;

    /**
     * 算法显示名称
     */
    @Schema(description = "算法显示名称", example = "YOLOv8目标检测")
    private String algorithmDisplayName;

    /**
     * 目标类型
     */
    @Schema(description = "目标类型", example = "1")
    private Integer objectType;

    /**
     * 目标类型描述
     */
    @Schema(description = "目标类型描述", example = "人员")
    private String objectTypeDesc;

    /**
     * 目标子类型
     */
    @Schema(description = "目标子类型", example = "1")
    private Integer objectSubType;

    /**
     * 目标子类型描述
     */
    @Schema(description = "目标子类型描述", example = "成年人")
    private String objectSubTypeDesc;

    /**
     * 目标ID
     */
    @Schema(description = "目标ID", example = "OBJ_001")
    private String objectId;

    /**
     * 置信度
     */
    @Schema(description = "置信度", example = "0.95")
    private BigDecimal confidenceScore;

    /**
     * 置信度百分比
     */
    @Schema(description = "置信度百分比", example = "95%")
    private String confidencePercentage;

    /**
     * 置信度等级
     */
    @Schema(description = "置信度等级", example = "high")
    private String confidenceLevel;

    /**
     * 边界框坐标
     */
    @Schema(description = "边界框坐标", example = "{\"x\":100,\"y\":200,\"width\":150,\"height\":200}")
    private Map<String, Object> boundingBox;

    /**
     * 中心点坐标X
     */
    @Schema(description = "中心点坐标X", example = "175.5")
    private BigDecimal centerX;

    /**
     * 中心点坐标Y
     */
    @Schema(description = "中心点坐标Y", example = "300.0")
    private BigDecimal centerY;

    /**
     * 目标宽度
     */
    @Schema(description = "目标宽度", example = "150")
    private BigDecimal objectWidth;

    /**
     * 目标高度
     */
    @Schema(description = "目标高度", example = "200")
    private BigDecimal objectHeight;

    /**
     * 目标面积
     */
    @Schema(description = "目标面积", example = "30000")
    private BigDecimal objectArea;

    /**
     * 相对大小
     */
    @Schema(description = "相对大小", example = "0.05")
    private BigDecimal relativeSize;

    /**
     * 相对大小百分比
     */
    @Schema(description = "相对大小百分比", example = "5%")
    private String relativeSizePercentage;

    /**
     * 颜色特征
     */
    @Schema(description = "颜色特征", example = "{\"dominant\":\"#FF0000\"}")
    private Map<String, Object> colorFeatures;

    /**
     * 主要颜色
     */
    @Schema(description = "主要颜色", example = "#FF0000")
    private String dominantColor;

    /**
     * 纹理特征
     */
    @Schema(description = "纹理特征", example = "smooth")
    private String textureFeatures;

    /**
     * 形状特征
     */
    @Schema(description = "形状特征", example = "rectangle")
    private String shapeFeatures;

    /**
     * 运动方向
     */
    @Schema(description = "运动方向", example = "45.5")
    private BigDecimal movementDirection;

    /**
     * 运动方向描述
     */
    @Schema(description = "运动方向描述", example = "东北方向")
    private String movementDirectionDesc;

    /**
     * 运动速度
     */
    @Schema(description = "运动速度", example = "25.8")
    private BigDecimal movementSpeed;

    /**
     * 运动速度等级
     */
    @Schema(description = "运动速度等级", example = "中速")
    private String movementSpeedLevel;

    /**
     * 运动状态
     */
    @Schema(description = "运动状态", example = "2")
    private Integer movementStatus;

    /**
     * 运动状态描述
     */
    @Schema(description = "运动状态描述", example = "慢速")
    private String movementStatusDesc;

    /**
     * 目标距离
     */
    @Schema(description = "目标距离", example = "15.5")
    private BigDecimal objectDistance;

    /**
     * 距离描述
     */
    @Schema(description = "距离描述", example = "15.5米")
    private String distanceDescription;

    /**
     * 目标实际大小
     */
    @Schema(description = "目标实际大小", example = "1.75")
    private BigDecimal actualSize;

    /**
     * 实际大小描述
     */
    @Schema(description = "实际大小描述", example = "1.75米")
    private String actualSizeDescription;

    /**
     * 目标分类
     */
    @Schema(description = "目标分类", example = "adult_male")
    private String objectClass;

    /**
     * 目标分类描述
     */
    @Schema(description = "目标分类描述", example = "成年男性")
    private String objectClassDesc;

    /**
     * 目标属性
     */
    @Schema(description = "目标属性", example = "{\"gender\":\"male\"}")
    private Map<String, Object> objectAttributes;

    /**
     * 目标数量
     */
    @Schema(description = "目标数量", example = "1")
    private Integer objectCount;

    /**
     * 密度等级
     */
    @Schema(description = "密度等级", example = "2")
    private Integer densityLevel;

    /**
     * 密度等级描述
     */
    @Schema(description = "密度等级描述", example = "正常")
    private String densityLevelDesc;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1001")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "入口区域")
    private String areaName;

    /**
     * 区域类型
     */
    @Schema(description = "区域类型", example = "2")
    private Integer areaType;

    /**
     * 区域类型描述
     */
    @Schema(description = "区域类型描述", example = "监控区")
    private String areaTypeDesc;

    /**
     * 是否在禁区内
     */
    @Schema(description = "是否在禁区内", example = "0")
    private Integer inRestrictedArea;

    /**
     * 禁区内状态描述
     */
    @Schema(description = "禁区内状态描述", example = "否")
    private String restrictedAreaStatus;

    /**
     * 告警级别
     */
    @Schema(description = "告警级别", example = "2")
    private Integer alertLevel;

    /**
     * 告警级别描述
     */
    @Schema(description = "告警级别描述", example = "提醒")
    private String alertLevelDesc;

    /**
     * 是否触发告警
     */
    @Schema(description = "是否触发告警", example = "0")
    private Integer alertTriggered;

    /**
     * 告警状态描述
     */
    @Schema(description = "告警状态描述", example = "未触发")
    private String alertStatusDesc;

    /**
     * 告警类型
     */
    @Schema(description = "告警类型", example = "area_intrusion")
    private String alertType;

    /**
     * 告警类型描述
     */
    @Schema(description = "告警类型描述", example = "区域入侵")
    private String alertTypeDesc;

    /**
     * 告警描述
     */
    @Schema(description = "告警描述", example = "人员进入禁区")
    private String alertDescription;

    /**
     * 关联人脸ID
     */
    @Schema(description = "关联人脸ID", example = "FACE_001")
    private String faceId;

    /**
     * 关联人员ID
     */
    @Schema(description = "关联人员ID", example = "1001")
    private Long personId;

    /**
     * 关联人员姓名
     */
    @Schema(description = "关联人员姓名", example = "张三")
    private String personName;

    /**
     * 关联车牌号
     */
    @Schema(description = "关联车牌号", example = "京A12345")
    private String plateNumber;

    /**
     * 关联车辆品牌
     */
    @Schema(description = "关联车辆品牌", example = "宝马")
    private String vehicleBrand;

    /**
     * 关联车辆型号
     */
    @Schema(description = "关联车辆型号", example = "X5")
    private String vehicleModel;

    /**
     * 关联车辆颜色
     */
    @Schema(description = "关联车辆颜色", example = "白色")
    private String vehicleColor;

    /**
     * 车辆完整描述
     */
    @Schema(description = "车辆完整描述", example = "白色宝马X5")
    private String vehicleDescription;

    /**
     * 目标图像URL
     */
    @Schema(description = "目标图像URL", example = "/images/detection/20251216/obj_001.jpg")
    private String objectImageUrl;

    /**
     * 目标缩略图URL
     */
    @Schema(description = "目标缩略图URL", example = "/images/thumbnail/20251216/obj_001_thumb.jpg")
    private String thumbnailUrl;

    /**
     * 检测模型版本
     */
    @Schema(description = "检测模型版本", example = "v2.1.0")
    private String modelVersion;

    /**
     * 处理状态
     */
    @Schema(description = "处理状态", example = "0")
    private Integer processStatus;

    /**
     * 处理状态描述
     */
    @Schema(description = "处理状态描述", example = "未处理")
    private String processStatusDesc;

    /**
     * 验证结果
     */
    @Schema(description = "验证结果", example = "0")
    private Integer verificationResult;

    /**
     * 验证结果描述
     */
    @Schema(description = "验证结果描述", example = "未验证")
    private String verificationResultDesc;

    /**
     * 验证人员ID
     */
    @Schema(description = "验证人员ID", example = "1001")
    private Long verifiedBy;

    /**
     * 验证人员姓名
     */
    @Schema(description = "验证人员姓名", example = "李四")
    private String verifiedByName;

    /**
     * 验证时间
     */
    @Schema(description = "验证时间", example = "2025-12-16T10:35:00")
    private LocalDateTime verificationTime;

    /**
     * 验证时间格式化
     */
    @Schema(description = "验证时间格式化", example = "2025-12-16 10:35:00")
    private String verificationTimeFormatted;

    /**
     * 验证备注
     */
    @Schema(description = "验证备注", example = "检测结果正确")
    private String verificationNote;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源", example = "1")
    private Integer dataSource;

    /**
     * 数据来源描述
     */
    @Schema(description = "数据来源描述", example = "实时检测")
    private String dataSourceDesc;

    /**
     * 检测耗时
     */
    @Schema(description = "检测耗时", example = "125")
    private Integer processingTime;

    /**
     * 处理耗时描述
     */
    @Schema(description = "处理耗时描述", example = "125毫秒")
    private String processingTimeDesc;

    /**
     * 硬件加速
     */
    @Schema(description = "硬件加速", example = "1")
    private Integer hardwareAcceleration;

    /**
     * 硬件加速描述
     */
    @Schema(description = "硬件加速描述", example = "GPU加速")
    private String hardwareAccelerationDesc;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性", example = "{\"custom_field\":\"custom_value\"}")
    private Map<String, Object> extendedAttributes;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "重要检测记录")
    private String remark;

    /**
     * 关联的跟踪信息
     */
    @Schema(description = "关联的跟踪信息")
    private ObjectTrackingInfo trackingInfo;

    /**
     * 处理建议
     */
    @Schema(description = "处理建议", example = "[\"关注该目标后续行为\",\"确认是否为授权人员\"]")
    private List<String> processingSuggestions;

    /**
     * 风险评估
     */
    @Schema(description = "风险评估", example = "low")
    private String riskAssessment;

    /**
     * 风险评分
     */
    @Schema(description = "风险评分", example = "2.5")
    private BigDecimal riskScore;

    /**
     * 关联链接
     */
    @Schema(description = "关联链接", example = "{\"video_url\":\"/video/record/001\",\"tracking_url\":\"/tracking/OBJ_001\"}")
    private Map<String, String> relatedLinks;

    /**
     * 内部类：跟踪信息
     */
    @Data
    @Schema(description = "跟踪信息")
    public static class ObjectTrackingInfo {
        @Schema(description = "跟踪ID", example = "TRACK_001")
        private String trackingId;

        @Schema(description = "跟踪开始时间", example = "2025-12-16T10:30:00")
        private LocalDateTime trackingStartTime;

        @Schema(description = "跟踪持续时间", example = "300")
        private Long trackingDuration;

        @Schema(description = "跟踪状态", example = "1")
        private Integer trackingStatus;

        @Schema(description = "总位移距离", example = "640.3")
        private BigDecimal totalDistance;

        @Schema(description = "平均速度", example = "25.6")
        private BigDecimal averageSpeed;

        @Schema(description = "轨迹点数量", example = "150")
        private Integer trajectoryPoints;
    }
}