package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

import net.lab1024.sa.common.domain.form.BaseQueryForm;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费账户查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费账户查询表单")
public class ConsumeAccountQueryForm extends BaseQueryForm {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "账户类型", example = "STAFF")
    private String accountType;

    @Schema(description = "账户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "最小余额", example = "0.00")
    private BigDecimal minBalance;

    @Schema(description = "最大余额", example = "1000.00")
    private BigDecimal maxBalance;

    @Schema(description = "创建开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime createTimeStart;

    @Schema(description = "创建结束时间", example = "2025-12-31T23:59:59")
    private LocalDateTime createTimeEnd;

    @Schema(description = "关键词搜索（用户名、备注）", example = "张")
    private String keyword;
}