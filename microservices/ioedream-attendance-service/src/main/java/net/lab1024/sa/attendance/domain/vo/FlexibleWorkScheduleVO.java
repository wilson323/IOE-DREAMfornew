package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹性工作制配置VO
 * <p>
 * 用于列表展示
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "弹性工作制配置VO")
public class FlexibleWorkScheduleVO {

    @Schema(description = "配置ID", example = "1")
    private Long scheduleId;

    @Schema(description = "配置名称", example = "研发部弹性工作制")
    private String scheduleName;

    @Schema(description = "配置编码", example = "FLEX_RD_001")
    private String scheduleCode;

    @Schema(description = "弹性模式：STANDARD-标准弹性 FLEXIBLE-完全弹性 HYBRID-混合弹性", example = "STANDARD")
    private String flexMode;

    @Schema(description = "弹性模式描述", example = "标准弹性")
    private String flexModeDesc;

    @Schema(description = "弹性上班时间范围", example = "07:00-10:00")
    private String flexStartRange;

    @Schema(description = "弹性下班时间范围", example = "16:00-20:00")
    private String flexEndRange;

    @Schema(description = "核心工作时间", example = "10:00-16:00")
    private String coreTimeRange;

    @Schema(description = "标准工作时长（分钟）", example = "480")
    private Integer standardWorkDuration;

    @Schema(description = "适用部门ID列表", example = "[1, 2, 3]")
    private List<Long> departmentIds;

    @Schema(description = "适用部门名称列表", example = "[\"研发部\", \"技术部\"]")
    private List<String> departmentNames;

    @Schema(description = "已分配员工数", example = "120")
    private Integer assignedEmployeeCount;

    @Schema(description = "配置状态：1-启用 0-禁用", example = "1")
    private Integer status;

    @Schema(description = "配置状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "生效时间", example = "2025-01-01T00:00:00")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-15T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "创建人姓名", example = "张三")
    private String createUserName;

    @Schema(description = "更新人ID", example = "2")
    private Long updateUserId;

    @Schema(description = "更新人姓名", example = "李四")
    private String updateUserName;
}
