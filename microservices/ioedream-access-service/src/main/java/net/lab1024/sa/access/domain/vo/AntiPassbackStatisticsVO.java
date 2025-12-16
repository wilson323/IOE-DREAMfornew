package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁反潜回统计VO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀标识视图对象
 * - 使用@Data注解简化getter/setter
 * - 包含Swagger注解便于API文档生成
 * - 提供详细的反潜回统计数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁反潜回统计VO")
public class AntiPassbackStatisticsVO {

    /**
     * 统计时间
     */
    @Schema(description = "统计时间", example = "2025-01-30T10:30:00")
    private LocalDateTime statisticsTime;

    /**
     * 总检查次数
     */
    @Schema(description = "总检查次数", example = "1500")
    private Long totalCheckCount;

    /**
     * 成功通过次数
     */
    @Schema(description = "成功通过次数", example = "1200")
    private Long successCount;

    /**
     * 失败次数
     */
    @Schema(description = "失败次数", example = "300")
    private Long failureCount;

    /**
     * 成功率
     */
    @Schema(description = "成功率", example = "80.0")
    private Double successRate;

    /**
     * 硬反潜回违规次数
     */
    @Schema(description = "硬反潜回违规次数", example = "50")
    private Long hardAntiPassbackViolations;

    /**
     * 软反潜回异常次数
     */
    @Schema(description = "软反潜回异常次数", example = "100")
    private Long softAntiPassbackExceptions;

    /**
     * 区域反潜回违规次数
     */
    @Schema(description = "区域反潜回违规次数", example = "30")
    private Long areaAntiPassbackViolations;

    /**
     * 全局反潜回违规次数
     */
    @Schema(description = "全局反潜回违规次数", example = "20")
    private Long globalAntiPassbackViolations;

    /**
     * 各区域统计
     */
    @Schema(description = "各区域统计")
    private List<AreaStatistics> areaStatisticsList;

    /**
     * 各设备统计
     */
    @Schema(description = "各设备统计")
    private List<DeviceStatistics> deviceStatisticsList;

    /**
     * 时间分布统计
     */
    @Schema(description = "时间分布统计")
    private Map<String, Long> timeDistribution;

    /**
     * 区域统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaStatistics {

        @Schema(description = "区域ID")
        private Long areaId;

        @Schema(description = "区域名称")
        private String areaName;

        @Schema(description = "检查次数")
        private Long checkCount;

        @Schema(description = "违规次数")
        private Long violationCount;

        @Schema(description = "违规率")
        private Double violationRate;
    }

    /**
     * 设备统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStatistics {

        @Schema(description = "设备ID")
        private Long deviceId;

        @Schema(description = "设备名称")
        private String deviceName;

        @Schema(description = "设备位置")
        private String deviceLocation;

        @Schema(description = "检查次数")
        private Long checkCount;

        @Schema(description = "违规次数")
        private Long violationCount;

        @Schema(description = "设备状态")
        private String deviceStatus;
    }
}