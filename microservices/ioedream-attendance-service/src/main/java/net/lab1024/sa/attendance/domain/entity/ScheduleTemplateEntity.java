package net.lab1024.sa.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 排班模板实体
 * 存储排班模板的配置信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_attendance_schedule_template")
@Schema(description = "排班模板实体")
public class ScheduleTemplateEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "模板名称", example = "技术部标准排班模板")
    private String templateName;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "模板类型", example = "部门模板")
    private String templateType;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "模板配置JSON", example = "{\"cycle_type\": \"weekly\", \"schedule_pattern\": []}")
    private String templateConfigJson;

    @NotNull
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "适用员工数", example = "25")
    private Integer applicableEmployees;

    @Schema(description = "模板版本", example = "1.0")
    private String templateVersion;

    @Schema(description = "创建人ID", example = "2001")
    private Long createUserId;

    @Schema(description = "更新人ID", example = "2002")
    private Long updateUserId;

    @Schema(description = "最后应用时间", example = "2025-01-30T14:30:00")
    private LocalDateTime lastAppliedTime;

    @Schema(description = "应用次数", example = "15")
    private Integer applyCount;

    // 枚举定义
    public enum TemplateType {
        DEPARTMENT("部门模板"),
        POSITION("岗位模板"),
        PERSONAL("个人模板"),
        GLOBAL("全局模板");

        private final String description;

        TemplateType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateType fromDescription(String description) {
            for (TemplateType type : values()) {
                if (type.description.equals(description)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid template type: " + description);
        }
    }

    public enum TemplateStatus {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");

        private final int code;
        private final String description;

        TemplateStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateStatus fromCode(int code) {
            for (TemplateStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid template status code: " + code);
        }
    }
}