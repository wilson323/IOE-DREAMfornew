package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门禁人员限制VO
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁人员限制VO")
public class AccessPersonRestrictionVO {

    @Schema(description = "限制规则ID", example = "1")
    private Long restrictionId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "用户手机号", example = "13800138000")
    private String userPhone;

    @Schema(description = "限制类型", example = "BLACKLIST")
    private String restrictionType;

    @Schema(description = "限制区域ID列表(JSON数组)", example = "[1001, 1002]")
    private String areaIds;

    @Schema(description = "限制门ID列表(JSON数组)", example = "[2001, 2002]")
    private String doorIds;

    @Schema(description = "限制开始时间", example = "09:00:00")
    private String timeStart;

    @Schema(description = "限制结束时间", example = "18:00:00")
    private String timeEnd;

    @Schema(description = "生效日期", example = "2025-01-25")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期", example = "2025-12-31")
    private LocalDate expireDate;

    @Schema(description = "限制原因", example = "违规操作")
    private String reason;

    @Schema(description = "优先级", example = "100")
    private Integer priority;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;

    @Schema(description = "规则描述", example = "多次未授权进入")
    private String description;

    @Schema(description = "创建时间", example = "2025-01-25T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-25T10:00:00")
    private LocalDateTime updateTime;
}
