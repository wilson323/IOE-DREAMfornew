package net.lab1024.sa.attendance.roster.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮班计划生成结果
 * <p>
 * 封装轮班计划生成操作的结果信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationPlanResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 生成的轮班安排列表
     */
    private List<net.lab1024.sa.attendance.roster.model.RotationSchedule> schedules;

    /**
     * 总生成数量
     */
    private Integer totalGenerated;

    /**
     * 成功生成数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failureCount;

    /**
     * 消息
     */
    private String message;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 生成时间
     */
    private LocalDateTime generationTime;

    /**
     * 生成耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 生成统计信息
     */
    private GenerationStatistics statistics;

    /**
     * 生成统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationStatistics {
        /**
         * 早班数量
         */
        private Integer earlyShiftCount;

        /**
         * 中班数量
         */
        private Integer middleShiftCount;

        /**
         * 晚班数量
         */
        private Integer nightShiftCount;

        /**
         * 休息日数量
         */
        private Integer restDayCount;

        /**
         * 跨天班次数量
         */
        private Integer overnightShiftCount;

        /**
         * 周末班次数量
         */
        private Integer weekendShiftCount;

        /**
         * 节假日班次数量
         */
        private Integer holidayShiftCount;
    }
}