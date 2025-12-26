package net.lab1024.sa.common.entity.visitor;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客区域配置实体
 * <p>
 * 存储访客专用区域的配置信息，支持精细化访客权限控制
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>访客区域配置管理</li>
 *   <li>访客权限级别控制</li>
 *   <li>访客容量管理</li>
 *   <li>访客设备配置</li>
 *   <li>访客统计分析</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>访客可访问区域配置</li>
 *   <li>访客容量限制管理</li>
 *   <li>访客开放时间设置</li>
 *   <li>访客接待要求配置</li>
 *   <li>访客统计分析</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.visitor.domain.service.VisitorAreaService 访客区域服务接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_visitor_area")
@Schema(description = "访客区域配置实体")
public class VisitorAreaEntity extends BaseEntity {

    /**
     * 访客区域ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "访客区域ID", example = "1001")
    private Long visitorAreaId;

    /**
     * 区域ID（外键）
     * <p>
     * 关联公共区域表的区域ID
     * 参考AreaEntity.areaId
     * </p>
     */
    @NotNull
    @TableField("area_id")
    @Schema(description = "区域ID", example = "1001")
    private Long areaId;

    /**
     * 区域名称
     * <p>
     * 冗余字段，用于快速查询
     * 与AreaEntity.areaName保持同步
     * </p>
     */
    @TableField("area_name")
    @Schema(description = "区域名称", example = "A栋一楼大厅")
    private String areaName;

    /**
     * 访问类型
     * <p>
     * 1-临时访问
     * 2-预约访问
     * 3-VIP访问
     * 4-承包商访问
     * 5-配送访问
     * </p>
     */
    @NotNull
    @TableField("visit_type")
    @Schema(description = "访问类型", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer visitType;

    /**
     * 访问权限级别
     * <p>
     * 1-公共区域（所有访客）
     * 2-受限区域（需要审批）
     * 3-保密区域（仅VIP）
     * 4-禁区（禁止访客）
     * </p>
     */
    @NotNull
    @TableField("access_level")
    @Schema(description = "访问权限级别", example = "1", allowableValues = {"1", "2", "3", "4"})
    private Integer accessLevel;

    /**
     * 最大访客数
     * <p>
     * 该区域允许的最大访客数量
     * 用于容量控制和安全管理
     * </p>
     */
    @NotNull
    @TableField("max_visitors")
    @Schema(description = "最大访客数", example = "50")
    private Integer maxVisitors;

    /**
     * 当前访客数
     * <p>
     * 当前在该区域的访客数量
     * 实时更新，用于容量监控
     * </p>
     */
    @TableField("current_visitors")
    @Schema(description = "当前访客数", example = "10")
    private Integer currentVisitors;

    /**
     * 是否需要接待
     * <p>
     * true-需要接待人员陪同
     * false-可自由访问
     * </p>
     */
    @TableField("reception_required")
    @Schema(description = "是否需要接待", example = "false")
    private Boolean receptionRequired;

    /**
     * 接待人员ID
     * <p>
     * 指定的接待人员用户ID
     * 关联UserEntity.userId
     * </p>
     */
    @TableField("receptionist_id")
    @Schema(description = "接待人员ID", example = "1001")
    private Long receptionistId;

    /**
     * 启用状态
     * <p>
     * true-启用：访客可以访问
     * false-禁用：访客不可访问
     * </p>
     */
    @TableField("enabled")
    @Schema(description = "启用状态", example = "true")
    private Boolean enabled;

    /**
     * 开放时间开始
     * <p>
     * 访客可访问的开始时间
     * 格式：HH:mm:ss
     * </p>
     */
    @TableField("open_time_start")
    @Schema(description = "开放时间开始", example = "08:00:00")
    private LocalTime openTimeStart;

    /**
     * 开放时间结束
     * <p>
     * 访客可访问的结束时间
     * 格式：HH:mm:ss
     * </p>
     */
    @TableField("open_time_end")
    @Schema(description = "开放时间结束", example = "18:00:00")
    private LocalTime openTimeEnd;

    /**
     * 访客须知
     * <p>
     * 访客访问该区域时的注意事项
     * 纯文本格式
     * </p>
     */
    @Size(max = 2000)
    @TableField("visitor_instructions")
    @Schema(description = "访客须知", example = "访客请在前台登记，佩戴访客证，由接待人员陪同进入")
    private String visitorInstructions;

    /**
     * 安全注意事项
     * <p>
     * 该区域的安全提醒和注意事项
     * 纯文本格式
     * </p>
     */
    @Size(max = 2000)
    @TableField("safety_notes")
    @Schema(description = "安全注意事项", example = "禁止携带危险物品进入，禁止拍照录像")
    private String safetyNotes;

    /**
     * 紧急联系人
     * <p>
     * 该区域的紧急联系人信息（JSON格式）
     * 示例：{"name":"张三","phone":"13800138000"}
     * </p>
     */
    @TableField("emergency_contact")
    @Schema(description = "紧急联系人（JSON格式）", example = "{\"name\":\"张三\",\"phone\":\"13800138000\"}")
    private String emergencyContact;

    /**
     * 访客设备配置
     * <p>
     * 该区域的访客相关设备配置（JSON格式）
     * 示例：{"camera":"CAM001","gate":"GATE001"}
     * </p>
     */
    @TableField("visitor_devices")
    @Schema(description = "访客设备配置（JSON格式）", example = "{\"camera\":\"CAM001\",\"gate\":\"GATE001\"}")
    private String visitorDevices;

    /**
     * 健康检查标准
     * <p>
     * 该区域的健康检查要求（JSON格式）
     * 示例：{"temperatureCheck":true,"maskRequired":true}
     * </p>
     */
    @TableField("health_check_standard")
    @Schema(description = "健康检查标准（JSON格式）", example = "{\"temperatureCheck\":true,\"maskRequired\":true}")
    private String healthCheckStandard;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储访客区域相关的扩展信息
     * 示例：特殊要求、附加限制等
     * </p>
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 访问类型枚举
     */
    public enum VisitType {
        TEMPORARY(1, "临时访问"),
        APPOINTMENT(2, "预约访问"),
        VIP(3, "VIP访问"),
        CONTRACTOR(4, "承包商访问"),
        DELIVERY(5, "配送访问");

        private final int code;
        private final String description;

        VisitType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static VisitType fromCode(int code) {
            for (VisitType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid visit type code: " + code);
        }
    }

    /**
     * 访问权限级别枚举
     */
    public enum AccessLevel {
        PUBLIC(1, "公共区域"),
        RESTRICTED(2, "受限区域"),
        CONFIDENTIAL(3, "保密区域"),
        PROHIBITED(4, "禁区");

        private final int code;
        private final String description;

        AccessLevel(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AccessLevel fromCode(int code) {
            for (AccessLevel level : values()) {
                if (level.code == code) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Invalid access level code: " + code);
        }
    }
}
