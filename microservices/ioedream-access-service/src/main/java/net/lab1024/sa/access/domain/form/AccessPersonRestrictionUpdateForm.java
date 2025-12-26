package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 门禁人员限制更新表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁人员限制更新表单")
public class AccessPersonRestrictionUpdateForm {

    @Schema(description = "限制规则ID", example = "1")
    private Long restrictionId;

    @Schema(description = "用户姓名", example = "张三(更新)")
    private String userName;

    @Schema(description = "用户手机号", example = "13800138000")
    private String userPhone;

    @Schema(description = "限制类型", example = "WHITELIST")
    private String restrictionType;

    @Schema(description = "限制区域ID列表", example = "[1001, 1002, 1003]")
    private List<Long> areaIds;

    @Schema(description = "限制门ID列表", example = "[2001, 2002, 2003]")
    private List<Long> doorIds;

    @Schema(description = "限制开始时间", example = "08:00:00")
    private LocalTime timeStart;

    @Schema(description = "限制结束时间", example = "20:00:00")
    private LocalTime timeEnd;

    @Schema(description = "生效日期", example = "2025-01-25")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期", example = "2025-12-31")
    private LocalDate expireDate;

    @Schema(description = "限制原因", example = "严重违规操作")
    private String reason;

    @Schema(description = "优先级", example = "150")
    private Integer priority;

    @Schema(description = "规则描述", example = "多次未授权进入(更新)")
    private String description;
}
