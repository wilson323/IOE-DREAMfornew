package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备监控请求表单
 * <p>
 * 用于设备监控相关的请求参数
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 完整的字段验证注释
 * - Swagger文档注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备监控请求")
public class DeviceMonitorRequest {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 监控类型
     * BASIC - 基础监控
     * DETAILED - 详细监控
     * REALTIME - 实时监控
     */
    @Schema(description = "监控类型", example = "DETAILED", allowableValues = {"BASIC", "DETAILED", "REALTIME"})
    private String monitorType = "DETAILED";

    /**
     * 监控时长（秒）
     */
    @Schema(description = "监控时长（秒）", example = "300")
    private Integer monitorDuration = 300;

    /**
     * 是否包含网络诊断
     */
    @Schema(description = "是否包含网络诊断", example = "true")
    private Boolean includeNetworkDiagnosis = true;

    /**
     * 是否包含性能测试
     */
    @Schema(description = "是否包含性能测试", example = "true")
    private Boolean includePerformanceTest = true;

    /**
     * 是否生成健康报告
     */
    @Schema(description = "是否生成健康报告", example = "false")
    private Boolean generateHealthReport = false;
}