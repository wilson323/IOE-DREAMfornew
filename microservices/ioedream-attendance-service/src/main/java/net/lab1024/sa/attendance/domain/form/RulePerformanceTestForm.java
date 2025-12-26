package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 规则性能测试请求表单
 * <p>
 * 用于发起性能测试的请求参数
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规则性能测试请求表单")
public class RulePerformanceTestForm {

    @NotBlank(message = "测试名称不能为空")
    @Schema(description = "测试名称", example = "早高峰考勤规则性能测试")
    private String testName;

    @NotNull(message = "测试类型不能为空")
    @Schema(description = "测试类型：SINGLE-单规则/BATCH-批量规则/CONCURRENT-并发测试", example = "CONCURRENT")
    private String testType;

    @Schema(description = "测试的规则ID列表", example = "[1, 2, 3]")
    private List<Long> ruleIds;

    @NotNull(message = "并发用户数不能为空")
    @Min(value = 1, message = "并发用户数至少为1")
    @Schema(description = "并发用户数（线程数）", example = "10")
    private Integer concurrentUsers;

    @NotNull(message = "每用户请求数不能为空")
    @Min(value = 1, message = "每用户请求数至少为1")
    @Schema(description = "每个用户执行的请求数", example = "100")
    private Integer requestsPerUser;

    @Schema(description = "请求间隔（毫秒）", example = "100")
    private Integer requestIntervalMs;

    @Schema(description = "超时时间（毫秒）", example = "5000")
    private Integer timeoutMs;

    @Schema(description = "测试场景：LATE-迟到/NORMAL-正常/EARLY-早退等", example = "NORMAL")
    private String testScenario;

    @Schema(description = "是否保存详细结果", example = "true")
    private Boolean saveDetailedResults;
}
