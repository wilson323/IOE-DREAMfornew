package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 排班模板视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "排班模板视图对象")
public class ScheduleTemplateVO {

    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @Schema(description = "模板名称", example = "技术部标准排班模板")
    private String templateName;

    @Schema(description = "模板类型", example = "部门模板")
    private String templateType;

    @Schema(description = "模板类型描述", example = "部门专用模板")
    private String templateTypeDesc;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "模板配置JSON", example = "{\"cycle_type\": \"weekly\"}")
    private String templateConfigJson;

    @Schema(description = "模板版本", example = "1.0")
    private String templateVersion;

    @Schema(description = "应用次数", example = "15")
    private Integer applyCount;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "创建时间", example = "2025-01-01 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "创建人ID", example = "2001")
    private Long createUserId;

    @Schema(description = "创建人姓名", example = "管理员")
    private String createUserName;

    @Schema(description = "更新时间", example = "2025-01-15 10:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "更新人ID", example = "2001")
    private Long updateUserId;

    @Schema(description = "更新人姓名", example = "管理员")
    private String updateUserName;
}