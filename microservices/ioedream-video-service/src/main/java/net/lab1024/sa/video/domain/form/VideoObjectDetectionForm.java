package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频目标检测查询表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
@Schema(description = "视频目标检测查询表单")
public class VideoObjectDetectionForm {

    /**
     * 检测记录ID
     */
    @Schema(description = "检测记录ID", example = "1689854272000000001")
    private Long detectionId;

    /**
     * 设备ID列表
     */
    @Schema(description = "设备ID列表", example = "[1001, 1002, 1003]")
    private List<Long> deviceIds;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "CAM001")
    private String deviceCode;

    /**
     * 通道ID
     */
    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    /**
     * 检测开始时间
     */
    @Schema(description = "检测开始时间", example = "2025-12-16T00:00:00")
    private LocalDateTime startTime;

    /**
     * 检测结束时间
     */
    @Schema(description = "检测结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime endTime;

    /**
     * 目标类型列表 1-人员 2-车辆 3-物体 4-动物 5-人脸 6-车牌 7-行李 8-危险品 9-其他
     */
    @Schema(description = "目标类型列表", example = "[1,2,5]")
    private List<Integer> objectTypes;

    /**
     * 目标子类型
     */
    @Schema(description = "目标子类型", example = "1")
    private Integer objectSubType;

    /**
     * 目标ID
     */
    @Schema(description = "目标ID", example = "OBJ_001")
    private String objectId;

    /**
     * 置信度范围 - 最小值
     */
    @Schema(description = "置信度范围 - 最小值", example = "0.8")
    @DecimalMin(value = "0.0", message = "最小置信度不能小于0")
    @DecimalMax(value = "1.0", message = "最小置信度不能大于1")
    private BigDecimal minConfidence;

    /**
     * 置信度范围 - 最大值
     */
    @Schema(description = "置信度范围 - 最大值", example = "1.0")
    @DecimalMin(value = "0.0", message = "最大置信度不能小于0")
    @DecimalMax(value = "1.0", message = "最大置信度不能大于1")
    private BigDecimal maxConfidence;

    /**
     * 相对大小范围 - 最小值
     */
    @Schema(description = "相对大小范围 - 最小值", example = "0.01")
    @DecimalMin(value = "0.0", message = "最小相对大小不能小于0")
    private BigDecimal minRelativeSize;

    /**
     * 相对大小范围 - 最大值
     */
    @Schema(description = "相对大小范围 - 最大值", example = "0.5")
    @DecimalMin(value = "0.0", message = "最大相对大小不能小于0")
    private BigDecimal maxRelativeSize;

    /**
     * 运动速度范围 - 最小值
     */
    @Schema(description = "运动速度范围 - 最小值", example = "5.0")
    @DecimalMin(value = "0.0", message = "最小运动速度不能小于0")
    private BigDecimal minMovementSpeed;

    /**
     * 运动速度范围 - 最大值
     */
    @Schema(description = "运动速度范围 - 最大值", example = "50.0")
    @DecimalMin(value = "0.0", message = "最大运动速度不能小于0")
    private BigDecimal maxMovementSpeed;

    /**
     * 运动状态列表 1-静止 2-慢速 3-中速 4-快速 5-变速
     */
    @Schema(description = "运动状态列表", example = "[2,3,4]")
    private List<Integer> movementStatusList;

    /**
     * 目标距离范围 - 最小值（米）
     */
    @Schema(description = "目标距离范围 - 最小值", example = "1.0")
    @DecimalMin(value = "0.0", message = "最小目标距离不能小于0")
    private BigDecimal minObjectDistance;

    /**
     * 目标距离范围 - 最大值（米）
     */
    @Schema(description = "目标距离范围 - 最大值", example = "100.0")
    @DecimalMin(value = "0.0", message = "最大目标距离不能小于0")
    private BigDecimal maxObjectDistance;

    /**
     * 目标分类
     */
    @Schema(description = "目标分类", example = "adult_male")
    private String objectClass;

    /**
     * 密度等级列表 1-稀疏 2-正常 3-密集 4-拥挤
     */
    @Schema(description = "密度等级列表", example = "[2,3]")
    private List<Integer> densityLevels;

    /**
     * 区域ID列表
     */
    @Schema(description = "区域ID列表", example = "[1001, 1002]")
    private List<Long> areaIds;

    /**
     * 区域类型列表 1-禁区 2-监控区 3-安全区 4-公共区 5-办公区 6-生产区
     */
    @Schema(description = "区域类型列表", example = "[1,2]")
    private List<Integer> areaTypes;

    /**
     * 是否在禁区内
     */
    @Schema(description = "是否在禁区内", example = "1")
    private Integer inRestrictedArea;

    /**
     * 告警级别列表 1-信息 2-提醒 3-警告 4-严重 5-紧急
     */
    @Schema(description = "告警级别列表", example = "[3,4,5]")
    private List<Integer> alertLevels;

    /**
     * 是否触发告警
     */
    @Schema(description = "是否触发告警", example = "1")
    private Integer alertTriggered;

    /**
     * 告警类型
     */
    @Schema(description = "告警类型", example = "area_intrusion")
    private String alertType;

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
     * 处理状态列表 0-未处理 1-处理中 2-已处理 3-已忽略
     */
    @Schema(description = "处理状态列表", example = "[0,1]")
    private List<Integer> processStatusList;

    /**
     * 验证结果列表 0-未验证 1-正确 2-错误 3-不确定
     */
    @Schema(description = "验证结果列表", example = "[0,1]")
    private List<Integer> verificationResults;

    /**
     * 验证人员ID
     */
    @Schema(description = "验证人员ID", example = "1001")
    private Long verifiedBy;

    /**
     * 验证开始时间
     */
    @Schema(description = "验证开始时间", example = "2025-12-16T00:00:00")
    private LocalDateTime verificationStartTime;

    /**
     * 验证结束时间
     */
    @Schema(description = "验证结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime verificationEndTime;

    /**
     * 数据来源列表 1-实时检测 2-录像分析 3-图片分析 4-批量导入
     */
    @Schema(description = "数据来源列表", example = "[1,2]")
    private List<Integer> dataSources;

    /**
     * 检测算法
     */
    @Schema(description = "检测算法", example = "YOLOv8")
    private String detectionAlgorithm;

    /**
     * 模型版本
     */
    @Schema(description = "模型版本", example = "v2.1.0")
    private String modelVersion;

    /**
     * 硬件加速
     */
    @Schema(description = "硬件加速", example = "1")
    private Integer hardwareAcceleration;

    /**
     * 处理时间范围 - 最小值（毫秒）
     */
    @Schema(description = "处理时间范围 - 最小值", example = "10")
    @Min(value = 0, message = "最小处理时间不能小于0")
    private Integer minProcessingTime;

    /**
     * 处理时间范围 - 最大值（毫秒）
     */
    @Schema(description = "处理时间范围 - 最大值", example = "1000")
    @Min(value = 0, message = "最大处理时间不能小于0")
    private Integer maxProcessingTime;

    /**
     * 关键词搜索（目标类型描述、告警描述、备注等）
     */
    @Schema(description = "关键词搜索", example = "人员")
    private String keyword;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "detection_time")
    private String sortField;

    /**
     * 排序方向
     */
    @Schema(description = "排序方向", example = "desc")
    private String sortOrder;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 20;

    /**
     * 是否只返回总数
     */
    @Schema(description = "是否只返回总数", example = "false")
    private Boolean countOnly = false;

    /**
     * 是否返回详细轨迹
     */
    @Schema(description = "是否返回详细轨迹", example = "false")
    private Boolean includeTrajectory = false;

    /**
     * 是否返回关联信息
     */
    @Schema(description = "是否返回关联信息", example = "true")
    private Boolean includeRelated = true;

    /**
     * 是否返回图像URL
     */
    @Schema(description = "是否返回图像URL", example = "true")
    private Boolean includeImages = true;
}