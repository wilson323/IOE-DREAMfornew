package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备发现结果视图对象
 * <p>
 * 用于展示设备自动发现任务的结果信息
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
@Schema(description = "设备发现结果")
public class DeviceDiscoveryResultVO {

    /**
     * 扫描任务ID
     */
    @Schema(description = "扫描任务ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String scanId;

    /**
     * 扫描状态
     * PENDING-待执行
     * RUNNING-运行中
     * COMPLETED-已完成
     * FAILED-失败
     * CANCELLED-已取消
     */
    @Schema(description = "扫描状态", example = "RUNNING")
    private String status;

    /**
     * 扫描进度（0-100）
     */
    @Schema(description = "扫描进度（0-100）", example = "30")
    private Integer progress;

    /**
     * 发现设备总数
     */
    @Schema(description = "发现设备总数", example = "15")
    private Integer totalDevices;

    /**
     * 验证通过设备数
     */
    @Schema(description = "验证通过设备数", example = "12")
    private Integer verifiedDevices;

    /**
     * 新设备数（未在系统中）
     */
    @Schema(description = "新设备数", example = "8")
    private Integer newDevices;

    /**
     * 发现的设备列表
     */
    @Schema(description = "发现的设备列表")
    private List<DiscoveredDeviceVO> discoveredDevices;

    /**
     * 扫描开始时间
     */
    @Schema(description = "扫描开始时间", example = "1706584800000")
    private Long startTime;

    /**
     * 扫描结束时间
     */
    @Schema(description = "扫描结束时间", example = "1706584980000")
    private Long endTime;

    /**
     * 扫描耗时（毫秒）
     */
    @Schema(description = "扫描耗时（毫秒）", example = "180000")
    private Long duration;

    /**
     * 错误信息（如果失败）
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 扫描的子网
     */
    @Schema(description = "扫描的子网", example = "192.168.1.0/24")
    private String scannedSubnet;

    /**
     * 扫描的协议
     */
    @Schema(description = "扫描的协议", example = "[\"ONVIF\", \"PRIVATE\"]")
    private List<String> scannedProtocols;

    /**
     * 添加成功的设备数
     */
    @Schema(description = "添加成功的设备数", example = "10")
    private Integer addedSuccessCount;

    /**
     * 添加失败的设备数
     */
    @Schema(description = "添加失败的设备数", example = "2")
    private Integer addedFailedCount;

    /**
     * 添加失败详情
     */
    @Schema(description = "添加失败详情")
    private List<String> addFailureDetails;

    // ==================== 显式添加 getter/setter 方法以确保编译通过 ====================

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Integer getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(Integer totalDevices) {
        this.totalDevices = totalDevices;
    }

    public List<DiscoveredDeviceVO> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void setDiscoveredDevices(List<DiscoveredDeviceVO> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }
}
