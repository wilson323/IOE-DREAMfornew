package net.lab1024.sa.oa.workflow.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.Map;
import java.util.List;

/**
 * 工作流仿真请求表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Schema(description = "工作流仿真请求表单")
public class WorkflowSimulationForm {

    @NotBlank(message = "流程定义ID不能为空")
    @Schema(description = "流程定义ID", example = "leaveRequest:1:12345")
    private String processDefinitionId;

    @Schema(description = "仿真名称", example = "请假流程仿真测试")
    private String simulationName;

    @Schema(description = "仿真描述")
    private String description;

    @NotNull(message = "仿真次数不能为空")
    @Min(value = 1, message = "仿真次数至少为1")
    @Max(value = 1000, message = "仿真次数不能超过1000")
    @Schema(description = "仿真次数", example = "100")
    private Integer simulationCount;

    @Schema(description = "启动参数", example = "{\"leaveType\": \"年假\", \"days\": 3}")
    private Map<String, Object> startParameters;

    @Schema(description = "业务数据", example = "{\"employeeId\": 1001, \"departmentId\": 10}")
    private Map<String, Object> businessData;

    @Schema(description = "候选人配置", example = "{\"managerTask\": [1001, 1002], \"hrTask\": [2001]}")
    private Map<String, List<Long>> candidateConfig;

    @Schema(description = "仿真模式", example = "NORMAL")
    private SimulationMode simulationMode;

    @Schema(description = "是否并行仿真", example = "true")
    private Boolean parallelSimulation = true;

    @Schema(description = "是否收集详细指标", example = "true")
    private Boolean collectDetailedMetrics = true;

    @Schema(description = "仿真场景列表")
    private List<SimulationScenario> scenarios;

    /**
     * 仿真模式枚举
     */
    public enum SimulationMode {
        /**
         * 正常模式
         */
        NORMAL("NORMAL", "正常模式"),

        /**
         * 压力测试模式
         */
        STRESS_TEST("STRESS_TEST", "压力测试模式"),

        /**
         * 极端情况模式
         */
        EXTREME_CASE("EXTREME_CASE", "极端情况模式"),

        /**
         * 性能基准模式
         */
        PERFORMANCE_BENCHMARK("PERFORMANCE_BENCHMARK", "性能基准模式");

        private final String code;
        private final String description;

        SimulationMode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 仿真场景
     */
    @Data
    @Schema(description = "仿真场景")
    public static class SimulationScenario {

        @Schema(description = "场景名称", example = "正常审批场景")
        private String scenarioName;

        @Schema(description = "场景描述")
        private String scenarioDescription;

        @Schema(description = "场景参数")
        private Map<String, Object> scenarioParameters;

        @Schema(description = "预期结果")
        private Map<String, Object> expectedResults;

        @Schema(description = "场景权重", example = "0.3")
        private Double weight;
    }
}
