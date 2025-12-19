package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 移动端位置上报请求
 * <p>
 * 封装移动端位置上报的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端位置上报请求")
public class MobileLocationReportRequest {

    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    @Schema(description = "纬度", example = "39.9042")
    private Double latitude;

    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    @Schema(description = "经度", example = "116.4074")
    private Double longitude;

    /**
     * 地址
     */
    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;

    /**
     * 精度（米）
     */
    @Schema(description = "精度（米）", example = "10.5")
    private Double accuracy;
}


