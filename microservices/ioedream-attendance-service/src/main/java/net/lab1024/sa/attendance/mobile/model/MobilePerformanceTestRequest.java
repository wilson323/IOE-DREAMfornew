package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 移动端性能测试请求
 * <p>
 * 封装移动端性能测试的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端性能测试请求")
public class MobilePerformanceTestRequest {

    /**
     * 测试类型
     */
    @Schema(description = "测试类型", example = "API_RESPONSE", allowableValues = {"API_RESPONSE", "DATA_SYNC", "BIOMETRIC_VERIFICATION"})
    private String testType;

    /**
     * 测试次数
     */
    @Schema(description = "测试次数", example = "10")
    private Integer testCount;
}


