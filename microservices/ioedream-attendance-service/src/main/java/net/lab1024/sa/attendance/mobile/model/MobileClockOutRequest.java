package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 移动端下班打卡请求
 * <p>
 * 封装移动端下班打卡的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端下班打卡请求")
public class MobileClockOutRequest {

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    /**
     * 位置信息
     */
    @Schema(description = "位置信息")
    private MobileClockInRequest.Location location;

    /**
     * 生物识别数据
     */
    @Schema(description = "生物识别数据")
    private MobileClockInRequest.BiometricData biometricData;

    /**
     * 打卡备注
     */
    @Schema(description = "打卡备注", example = "正常下班打卡")
    private String remark;

    /**
     * 打卡照片
     */
    @Schema(description = "打卡照片Base64")
    private String photo;

    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private Map<String, Object> extendParams;
}
