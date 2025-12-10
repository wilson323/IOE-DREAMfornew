package net.lab1024.sa.visitor.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.organization.entity.AreaEntity;

import java.time.LocalDateTime;

/**
 * 访客区域实体类
 * <p>
 * 核心概念：访客管理专用区域配置，继承统一区域概念
 * 严格遵循CLAUDE.md规范：
 * - 继承AreaEntity获取区域基础属性
 * - 添加访客专用业务属性
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 访客业务场景：
 * - 访客预约区域管理
 * - 访客登记区域配置
 * - 访客通行权限控制
 * - 访客轨迹跟踪管理
 * </p>
 * <p>
 * 设计原则：
 * - 继承统一区域概念，保持一致性
 * - 扩展访客专用业务属性
 * - 支持访客权限的精细化控制
 * - 集成访客设备管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_area")
@Schema(description = "访客区域实体")
public class VisitorAreaEntity extends AreaEntity {

    /**
     * 访客区域ID（主键）
     */
    @TableField("visitor_area_id")
    @Schema(description = "访客区域ID", example = "1001")
    private Long visitorAreaId;

    /**
     * 访问类型
     * <p>
     * 1-预约访问（需要提前预约）
     * 2-临时访问（现场登记）
     * 3-VIP访问（绿色通道）
     * 4-供应商访问（长期权限）
     * 5-维修访问（设备维护）
     * </p>
     */
    @NotNull
    @TableField("visit_type")
    @Schema(description = "访问类型", example = "1")
    private Integer visitType;

    /**
     * 区域访问权限级别
     * <p>
     * 1-公开区域（无需特殊权限）
     * 2-限制区域（需要预约或审批）
     * 3-保密区域（需要高级别审批）
     * 4-禁区（禁止访客进入）
     * </p>
     */
    @NotNull
    @TableField("access_level")
    @Schema(description = "访问权限级别", example = "2")
    private Integer accessLevel;

    /**
     * 最大同时访客数量
     */
    @TableField("max_visitors")
    @Schema(description = "最大同时访客数量", example = "50")
    private Integer maxVisitors;

    /**
     * 当前访客数量
     */
    @TableField("current_visitors")
    @Schema(description = "当前访客数量", example = "15")
    private Integer currentVisitors;

    /**
     * 是否需要接待人员
     */
    @TableField("reception_required")
    @Schema(description = "是否需要接待人员", example = "true")
    private Boolean receptionRequired;

    /**
     * 接待人员ID
     */
    @TableField("receptionist_id")
    @Schema(description = "接待人员ID", example = "1001")
    private Long receptionistId;

    /**
     * 接待人员姓名
     */
    @Size(max = 50, message = "接待人员姓名长度不能超过50个字符")
    @TableField("receptionist_name")
    @Schema(description = "接待人员姓名", example = "张三")
    private String receptionistName;

    /**
     * 是否允许拍照
     */
    @TableField("photo_allowed")
    @Schema(description = "是否允许拍照", example = "true")
    private Boolean photoAllowed;

    /**
     * 是否允许录制
     */
    @TableField("video_allowed")
    @Schema(description = "是否允许录制", example = "false")
    private Boolean videoAllowed;

    /**
     * 访问时间限制（分钟）
     */
    @TableField("visit_time_limit")
    @Schema(description = "访问时间限制（分钟）", example = "120")
    private Integer visitTimeLimit;

    /**
     * 预约提前天数限制
     */
    @TableField("appointment_days_limit")
    @Schema(description = "预约提前天数限制", example = "7")
    private Integer appointmentDaysLimit;

    /**
     * 是否需要健康检查
     */
    @TableField("health_check_required")
    @Schema(description = "是否需要健康检查", example = "true")
    private Boolean healthCheckRequired;

    /**
     * 健康检查标准
     * <p>
     * JSON格式：{"temperature": true, "mask": true, "healthCode": true}
     * </p>
     */
    @TableField("health_check_standard")
    @Schema(description = "健康检查标准", example = "{\"temperature\": true, \"mask\": true}")
    private String healthCheckStandard;

    /**
     * 是否需要身份证验证
     */
    @TableField("id_card_required")
    @Schema(description = "是否需要身份证验证", example = "true")
    private Boolean idCardRequired;

    /**
     * 是否需要人脸识别
     */
    @TableField("face_recognition_required")
    @Schema(description = "是否需要人脸识别", example = "false")
    private Boolean faceRecognitionRequired;

    /**
     * 访客设备配置
     * <p>
     * JSON格式：{"registrationDevice": "DEV001", "accessDevice": "DEV002", "cameraDevice": "CAM001"}
     * </p>
     */
    @TableField("visitor_devices")
    @Schema(description = "访客设备配置", example = "{\"registrationDevice\": \"DEV001\"}")
    private String visitorDevices;

    /**
     * 安全注意事项
     */
    @Size(max = 500, message = "安全注意事项长度不能超过500个字符")
    @TableField("safety_notes")
    @Schema(description = "安全注意事项", example = "请佩戴访客证件，禁止进入办公区域")
    private String safetyNotes;

    /**
     * 区域开放时间配置
     * <p>
     * JSON格式：{"workdays": {"start": "09:00", "end": "18:00"}, "weekends": {"start": "10:00", "end": "16:00"}}
     * </p>
     */
    @TableField("open_hours")
    @Schema(description = "区域开放时间配置", example = "{\"workdays\": {\"start\": \"09:00\"}}")
    private String openHours;

    /**
     * 访客审批流程配置
     * <p>
     * 1-无需审批
     * 2-部门经理审批
     * 3-安全部门审批
     * 4-多级审批
     * </p>
     */
    @TableField("approval_process")
    @Schema(description = "访客审批流程", example = "2")
    private Integer approvalProcess;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    @Schema(description = "审批人ID", example = "2001")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Size(max = 50, message = "审批人姓名长度不能超过50个字符")
    @TableField("approver_name")
    @Schema(description = "审批人姓名", example = "李四")
    private String approverName;

    /**
     * 紧急联系人信息
     * <p>
     * JSON格式：{"name": "张三", "phone": "13800138000", "department": "安全部"}
     * </p>
     */
    @TableField("emergency_contact")
    @Schema(description = "紧急联系人信息", example = "{\"name\": \"张三\", \"phone\": \"13800138000\"}")
    private String emergencyContact;

    /**
     * 访客须知
     */
    @Size(max = 1000, message = "访客须知长度不能超过1000个字符")
    @TableField("visitor_instructions")
    @Schema(description = "访客须知", example = "1. 请在前台登记 2. 佩戴访客证件 3. 不要进入限制区域")
    private String visitorInstructions;

    /**
     * 区域访客统计配置
     * <p>
     * JSON格式：{"enableStatistics": true, "reportInterval": "daily", "alerts": {"maxVisitors": true, "overstay": true}}
     * </p>
     */
    @TableField("visitor_statistics_config")
    @Schema(description = "访客统计配置", example = "{\"enableStatistics\": true}")
    private String visitorStatisticsConfig;

    /**
     * 是否启用
     */
    @TableField("enabled")
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @Schema(description = "生效时间", example = "2025-12-08T09:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    @Schema(description = "失效时间", example = "2026-12-08T09:00:00")
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    @Schema(description = "备注", example = "主要接待区域，需要严格安全管理")
    private String remark;
}
