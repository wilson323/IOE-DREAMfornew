package net.lab1024.sa.common.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.manager.SpaceCapacityManager;
import net.lab1024.sa.common.organization.service.SpaceCapacityService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 空间容量分析控制器
 * 提供区域空间容量分析和优化建议API接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organization/space-capacity")
@RequiredArgsConstructor
@Tag(name = "空间容量分析", description = "空间容量分析相关接口")
@Validated
public class SpaceCapacityController {

    private final SpaceCapacityService spaceCapacityService;

    @GetMapping("/analyze/{areaId}")
    @Operation(summary = "分析区域空间容量", description = "分析指定区域的空间容量利用情况")
    public ResponseDTO<SpaceCapacityManager.SpaceCapacityAnalysis> analyzeSpaceCapacity(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(spaceCapacityService.analyzeSpaceCapacity(areaId));
    }

    @PostMapping("/batch-analyze")
    @Operation(summary = "批量分析区域空间容量", description = "批量分析多个区域的空间容量")
    public ResponseDTO<List<SpaceCapacityManager.SpaceCapacityAnalysis>> batchAnalyzeSpaceCapacity(
            @RequestBody @NotNull List<Long> areaIds) {
        return ResponseDTO.ok(spaceCapacityService.batchAnalyzeSpaceCapacity(areaIds));
    }

    @GetMapping("/comparison/{parentAreaId}")
    @Operation(summary = "获取区域空间容量对比", description = "获取指定父区域下所有子区域的容量对比分析")
    public ResponseDTO<SpaceCapacityManager.SpaceCapacityComparison> getSpaceCapacityComparison(
            @Parameter(description = "父区域ID", required = true)
            @PathVariable @NotNull Long parentAreaId) {
        return ResponseDTO.ok(spaceCapacityService.getSpaceCapacityComparison(parentAreaId));
    }

    @GetMapping("/report/{areaId}")
    @Operation(summary = "生成空间容量报告", description = "生成指定区域的详细空间容量分析报告")
    public ResponseDTO<SpaceCapacityManager.SpaceCapacityReport> generateSpaceCapacityReport(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(spaceCapacityService.generateSpaceCapacityReport(areaId));
    }

    @GetMapping("/suggestions/{areaId}")
    @Operation(summary = "获取区域容量优化建议", description = "获取指定区域的容量优化建议")
    public ResponseDTO<List<String>> getCapacityOptimizationSuggestions(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return ResponseDTO.ok(spaceCapacityService.getCapacityOptimizationSuggestions(areaId));
    }

    @GetMapping("/trends/{areaId}")
    @Operation(summary = "获取区域容量历史趋势", description = "获取指定区域的容量历史变化趋势")
    public ResponseDTO<List<SpaceCapacityManager.CapacityTrend>> getCapacityTrends(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId,
            @Parameter(description = "历史月份数", required = false)
            @RequestParam(defaultValue = "6") @Min(1) @Max(24) Integer months) {
        return ResponseDTO.ok(spaceCapacityService.getCapacityTrends(areaId, months));
    }

    @PostMapping("/forecast/{areaId}")
    @Operation(summary = "预测区域未来容量需求", description = "预测指定区域未来一段时间的容量需求")
    public ResponseDTO<SpaceCapacityService.CapacityForecastResult> forecastCapacityDemand(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId,
            @Parameter(description = "预测月份数", required = false)
            @RequestParam(defaultValue = "12") @Min(1) @Max(36) Integer forecastMonths) {
        return ResponseDTO.ok(spaceCapacityService.forecastCapacityDemand(areaId, forecastMonths));
    }

    // ================ 便捷查询接口 ================

    @GetMapping("/campus/list")
    @Operation(summary = "获取所有园区的容量分析", description = "获取所有园区级别的空间容量分析")
    public ResponseDTO<List<SpaceCapacityManager.SpaceCapacityAnalysis>> getAllCampusCapacityAnalysis() {
        // 这里需要查询所有园区级别的区域ID
        // 实际实现中需要调用相应的服务获取园区ID列表
        return ResponseDTO.ok(List.of());
    }

    @GetMapping("/overload/list")
    @Operation(summary = "获取超负荷区域列表", description = "获取所有超负荷的区域")
    public ResponseDTO<List<SpaceCapacityManager.SpaceCapacityAnalysis>> getOverloadAreas() {
        // 这里需要查询所有超负荷的区域
        // 实际实现中需要调用相应的服务筛选超负荷区域
        return ResponseDTO.ok(List.of());
    }

    @GetMapping("/low-usage/list")
    @Operation(summary = "获取低利用率区域列表", description = "获取所有低利用率的区域")
    public ResponseDTO<List<SpaceCapacityManager.SpaceCapacityAnalysis>> getLowUsageAreas() {
        // 这里需要查询所有低利用率的区域
        // 实际实现中需要调用相应的服务筛选低利用率区域
        return ResponseDTO.ok(List.of());
    }

    @GetMapping("/dashboard/summary")
    @Operation(summary = "获取空间容量仪表板摘要", description = "获取整个系统的空间容量概览")
    public ResponseDTO<SpaceCapacityDashboardSummary> getSpaceCapacityDashboardSummary() {
        // 创建仪表板摘要数据
        SpaceCapacityDashboardSummary summary = new SpaceCapacityDashboardSummary();

        // 初始化默认值（实际应用中应从数据库获取）
        summary.setTotalAreas(0);
        summary.setTotalDevices(0);
        summary.setAverageUtilizationRate(0.0);
        summary.setOverloadCount(0);
        summary.setLowUsageCount(0);
        summary.setNormalUsageCount(0);
        summary.setHighUsageCount(0);

        return ResponseDTO.ok(summary);
    }

    // ================ 内部类 ================

    /**
     * 空间容量仪表板摘要
     */
    public static class SpaceCapacityDashboardSummary {
        private Integer totalAreas;
        private Integer totalDevices;
        private double averageUtilizationRate;
        private Integer overloadCount;
        private Integer lowUsageCount;
        private Integer normalUsageCount;
        private Integer highUsageCount;
        private List<AreaTypeSummary> areaTypeSummaries;

        // getters and setters
        public Integer getTotalAreas() { return totalAreas; }
        public void setTotalAreas(Integer totalAreas) { this.totalAreas = totalAreas; }
        public Integer getTotalDevices() { return totalDevices; }
        public void setTotalDevices(Integer totalDevices) { this.totalDevices = totalDevices; }
        public double getAverageUtilizationRate() { return averageUtilizationRate; }
        public void setAverageUtilizationRate(double averageUtilizationRate) { this.averageUtilizationRate = averageUtilizationRate; }
        public Integer getOverloadCount() { return overloadCount; }
        public void setOverloadCount(Integer overloadCount) { this.overloadCount = overloadCount; }
        public Integer getLowUsageCount() { return lowUsageCount; }
        public void setLowUsageCount(Integer lowUsageCount) { this.lowUsageCount = lowUsageCount; }
        public Integer getNormalUsageCount() { return normalUsageCount; }
        public void setNormalUsageCount(Integer normalUsageCount) { this.normalUsageCount = normalUsageCount; }
        public Integer getHighUsageCount() { return highUsageCount; }
        public void setHighUsageCount(Integer highUsageCount) { this.highUsageCount = highUsageCount; }
        public List<AreaTypeSummary> getAreaTypeSummaries() { return areaTypeSummaries; }
        public void setAreaTypeSummaries(List<AreaTypeSummary> areaTypeSummaries) { this.areaTypeSummaries = areaTypeSummaries; }
    }

    /**
     * 区域类型摘要
     */
    public static class AreaTypeSummary {
        private Integer areaType;
        private String areaTypeName;
        private Integer areaCount;
        private Integer deviceCount;
        private double averageUtilizationRate;

        // getters and setters
        public Integer getAreaType() { return areaType; }
        public void setAreaType(Integer areaType) { this.areaType = areaType; }
        public String getAreaTypeName() { return areaTypeName; }
        public void setAreaTypeName(String areaTypeName) { this.areaTypeName = areaTypeName; }
        public Integer getAreaCount() { return areaCount; }
        public void setAreaCount(Integer areaCount) { this.areaCount = areaCount; }
        public Integer getDeviceCount() { return deviceCount; }
        public void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
        public double getAverageUtilizationRate() { return averageUtilizationRate; }
        public void setAverageUtilizationRate(double averageUtilizationRate) { this.averageUtilizationRate = averageUtilizationRate; }
    }
}
