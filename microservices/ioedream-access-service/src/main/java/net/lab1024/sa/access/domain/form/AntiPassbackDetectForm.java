package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 反潜回检测请求表单
 * <p>
 * 用于通行时的反潜回检测
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
@Schema(description = "反潜回检测请求")
public class AntiPassbackDetectForm {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @NotBlank(message = "用户姓名不能为空")
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 卡号
     */
    @NotBlank(message = "卡号不能为空")
    @Schema(description = "卡号", example = "1234567890")
    private String userCardNo;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "2001")
    private Long deviceId;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Schema(description = "设备名称", example = "A栋1楼门禁")
    private String deviceName;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    @Schema(description = "设备编码", example = "AC-001")
    private String deviceCode;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "101")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    /**
     * 通行时间（毫秒时间戳）
     */
    @NotNull(message = "通行时间不能为空")
    @Schema(description = "通行时间（毫秒）", example = "1706584800000")
    private Long passTime;

    /**
     * 是否跳过反潜回检测（管理员特殊通行）
     */
    @Schema(description = "跳过检测", example = "false")
    private Boolean skipDetection;
}
