package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 视频行为检测视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
@Schema(description = "视频行为检测视图对象")
public class VideoBehaviorVO {

    /**
     * 行为ID
     */
    @Schema(description = "行为ID", example = "1698745325654325123")
    private Long behaviorId;

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
    @Schema(description = "设备名称", example = "主入口摄像头")
    private String deviceName;

    /**
     * 通道ID
     */
    @Schema(description = "通道ID", example = "1")
    private Long channelId;

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
     * 检测时间描述
     */
    @Schema(description = "检测时间描述", example = "2025-12-16 10:30:00")
    private String detectionTimeDesc;

    /**
     * 行为类型
     */
    @Schema(description = "行为类型", example = "5")
    private Integer behaviorType;

    /**
     * 行为类型描述
     */
    @Schema(description = "行为类型描述", example = "异常行为")
    private String behaviorTypeDesc;

    /**
     * 行为子类型
     */
    @Schema(description = "行为子类型", example = "1")
    private Integer behaviorSubType;

    /**
     * 行为子类型描述
     */
    @Schema(description = "行为子类型描述", example = "人员徘徊")
    private String behaviorSubTypeDesc;

    /**
     * 行为严重程度
     */
    @Schema(description = "行为严重程度", example = "2")
    private Integer severityLevel;

    /**
     * 行为严重程度描述
     */
    @Schema(description = "行为严重程度描述", example = "中风险")
    private String severityLevelDesc;

    /**
     * 置信度
     */
    @Schema(description = "置信度", example = "85.5")
    private BigDecimal confidenceScore;

    /**
     * 置信度等级
     */
    @Schema(description = "置信度等级", example = "高")
    private String confidenceGrade;

    /**
     * 检测目标数量
     */
    @Schema(description = "检测目标数量", example = "3")
    private Integer targetCount;

    /**
     * 目标ID列表
     */
    @Schema(description = "目标ID列表", example = "[1001,1002,1003]")
    private String targetIds;

    /**
     * 目标ID列表（解析后）
     */
    @Schema(description = "目标ID列表（解析后）", example = "[1001, 1002, 1003]")
    private List<Long> targetIdsList;

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "1001")
    private Long personId;

    /**
     * 人员编号
     */
    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 人员姓名（脱敏）
     */
    @Schema(description = "人员姓名（脱敏）", example = "张*")
    private String personNameMasked;

    /**
     * 行为描述
     */
    @Schema(description = "行为描述", example = "检测到人员在门口徘徊超过5分钟")
    private String behaviorDescription;

    /**
     * 行为区域坐标
     */
    @Schema(description = "行为区域坐标", example = "[{\"x\":100,\"y\":200,\"w\":150,\"h\":180}]")
    private String behaviorRegions;

    /**
     * 行为区域坐标（解析后）
     */
    @Schema(description = "行为区域坐标（解析后）", example = "[{\"x\":100,\"y\":200,\"w\":150,\"h\":180}]")
    private String behaviorRegionsParsed;

    /**
     * 持续时间（秒）
     */
    @Schema(description = "持续时间（秒）", example = "300")
    private Long durationSeconds;

    /**
     * 持续时间描述
     */
    @Schema(description = "持续时间描述", example = "5分钟")
    private String durationDesc;

    /**
     * 行为开始时间
     */
    @Schema(description = "行为开始时间", example = "2025-12-16T10:25:00")
    private LocalDateTime startTime;

    /**
     * 行为结束时间
     */
    @Schema(description = "行为结束时间", example = "2025-12-16T10:30:00")
    private LocalDateTime endTime;

    /**
     * 场景图片URL
     */
    @Schema(description = "场景图片URL", example = "/uploads/behavior/scene_1698745325654325123.jpg")
    private String sceneImageUrl;

    /**
     * 场景缩略图URL
     */
    @Schema(description = "场景缩略图URL", example = "/uploads/behavior/scene_thumb_1698745325654325123.jpg")
    private String sceneThumbnailUrl;

    /**
     * 行为快照URL列表
     */
    @Schema(description = "行为快照URL列表", example = "url1.jpg,url2.jpg,url3.jpg")
    private String snapshotUrls;

    /**
     * 行为快照URL列表（解析后）
     */
    @Schema(description = "行为快照URL列表（解析后）", example = "[\"url1.jpg\",\"url2.jpg\",\"url3.jpg\"]")
    private List<String> snapshotUrlsList;

    /**
     * 视频片段URL
     */
    @Schema(description = "视频片段URL", example = "/uploads/behavior/video_1698745325654325123.mp4")
    private String videoSegmentUrl;

    /**
     * 视频片段缩略图URL
     */
    @Schema(description = "视频片段缩略图URL", example = "/uploads/behavior/video_thumb_1698745325654325123.jpg")
    private String videoThumbnailUrl;

    /**
     * 检测算法类型
     */
    @Schema(description = "检测算法类型", example = "1")
    private Integer detectionAlgorithm;

    /**
     * 检测算法类型描述
     */
    @Schema(description = "检测算法类型描述", example = "YOLOv5")
    private String detectionAlgorithmDesc;

    /**
     * 检测模型版本
     */
    @Schema(description = "检测模型版本", example = "YOLOv5s-v6.0")
    private String detectionModel;

    /**
     * 检测耗时（毫秒）
     */
    @Schema(description = "检测耗时（毫秒）", example = "250")
    private Long detectionDuration;

    /**
     * 告警标志
     */
    @Schema(description = "告警标志", example = "1")
    private Integer alarmTriggered;

    /**
     * 告警标志描述
     */
    @Schema(description = "告警标志描述", example = "已触发")
    private String alarmTriggeredDesc;

    /**
     * 告警类型
     */
    @Schema(description = "告警类型", example = "异常行为,安全隐患")
    private String alarmTypes;

    /**
     * 告警级别
     */
    @Schema(description = "告警级别", example = "2")
    private Integer alarmLevel;

    /**
     * 告警级别描述
     */
    @Schema(description = "告警级别描述", example = "重要")
    private String alarmLevelDesc;

    /**
     * 是否需要人工确认
     */
    @Schema(description = "是否需要人工确认", example = "1")
    private Integer needManualConfirm;

    /**
     * 需要人工确认描述
     */
    @Schema(description = "需要人工确认描述", example = "需要")
    private String needManualConfirmDesc;

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
     * 处理时间
     */
    @Schema(description = "处理时间", example = "2025-12-16T10:35:00")
    private LocalDateTime processTime;

    /**
     * 处理时间描述
     */
    @Schema(description = "处理时间描述", example = "2025-12-16 10:35:00")
    private String processTimeDesc;

    /**
     * 处理耗时（分钟）
     */
    @Schema(description = "处理耗时（分钟）", example = "5")
    private Long processDurationMinutes;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID", example = "1001")
    private Long processUserId;

    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名", example = "管理员")
    private String processUserName;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注", example = "确认为正常访客，已登记")
    private String processRemark;

    /**
     * 关联事件ID列表
     */
    @Schema(description = "关联事件ID列表", example = "[1001,1002]")
    private String relatedEventIds;

    /**
     * 行为标签
     */
    @Schema(description = "行为标签", example = "徘徊,可疑,重点关注")
    private String behaviorTags;

    /**
     * 影响等级
     */
    @Schema(description = "影响等级", example = "3")
    private Integer impactLevel;

    /**
     * 影响等级描述
     */
    @Schema(description = "影响等级描述", example = "一般影响")
    private String impactLevelDesc;

    /**
     * 风险等级
     */
    @Schema(description = "风险等级", example = "3")
    private Integer riskLevel;

    /**
     * 风险等级描述
     */
    @Schema(description = "风险等级描述", example = "中风险")
    private String riskLevelDesc;

    /**
     * 处理优先级
     */
    @Schema(description = "处理优先级", example = "2")
    private Integer processPriority;

    /**
     * 处理优先级描述
     */
    @Schema(description = "处理优先级描述", example = "中")
    private String processPriorityDesc;

    /**
     * 环境信息（JSON格式）
     */
    @Schema(description = "环境信息", example = "{\"lighting\":\"良好\",\"weather\":\"晴天\",\"temperature\":25}")
    private String environmentInfo;

    /**
     * 环境信息（解析后）
     */
    @Schema(description = "环境信息（解析后）", example = "{\"lighting\":\"良好\",\"weather\":\"晴天\",\"temperature\":25}")
    private Map<String, Object> environmentInfoParsed;

    /**
     * 光照条件
     */
    @Schema(description = "光照条件", example = "良好")
    private String lightingCondition;

    /**
     * 天气状况
     */
    @Schema(description = "天气状况", example = "晴天")
    private String weatherCondition;

    /**
     * 温度
     */
    @Schema(description = "温度", example = "25")
    private Integer temperature;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性", example = "{\"custom_field1\":\"value1\",\"custom_field2\":\"value2\"}")
    private String extendedAttributes;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-12-16T10:30:00")
    private LocalDateTime createTime;

    /**
     * 创建时间描述
     */
    @Schema(description = "创建时间描述", example = "2025-12-16 10:30:00")
    private String createTimeDesc;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-12-16T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 最后修改人ID
     */
    @Schema(description = "最后修改人ID", example = "1001")
    private Long lastModifiedById;

    /**
     * 最后修改人姓名
     */
    @Schema(description = "最后修改人姓名", example = "AI系统")
    private String lastModifiedByName;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;

    /**
     * 阅读时间
     */
    @Schema(description = "阅读时间", example = "2025-12-16T10:32:00")
    private LocalDateTime readTime;

    /**
     * 是否已归档
     */
    @Schema(description = "是否已归档", example = "false")
    private Boolean isArchived;

    /**
     * 归档时间
     */
    @Schema(description = "归档时间", example = "2025-12-16T12:00:00")
    private LocalDateTime archivedTime;

    /**
     * 是否已导出
     */
    @Schema(description = "是否已导出", example = "false")
    private Boolean isExported;

    /**
     * 导出时间
     */
    @Schema(description = "导出时间", example = "2025-12-16T12:00:00")
    private LocalDateTime exportedTime;

    /**
     * 关联AI模型ID
     */
    @Schema(description = "关联AI模型ID", example = "MODEL_001")
    private String aiModelId;

    /**
     * 关联AI模型名称
     */
    @Schema(description = "关联AI模型名称", example = "行为检测模型v1.0")
    private String aiModelName;

    /**
     * 处理建议
     */
    @Schema(description = "处理建议", example = "[\"立即处理，优先级高\",\"建议人工复核检测结果\"]")
    private List<String> processingSuggestions;

    /**
     * 相关链接
     */
    @Schema(description = "相关链接", example = "[{\"text\":\"查看详情\",\"url\":\"/behavior/detail/123\"}]")
    private List<Map<String, String>> relatedLinks;

    /**
     * 标签颜色
     */
    @Schema(description = "标签颜色", example = "#FF5722")
    private String tagColor;

    /**
     * 状态图标
     */
    @Schema(description = "状态图标", example = "warning")
    private String statusIcon;
}