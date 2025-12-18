package net.lab1024.sa.access.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门禁验证请求DTO
 * <p>
 * 用于统一验证入口的请求参数
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
@Schema(description = "门禁验证请求")
public class AccessVerificationRequest {

    /**
     * 用户ID（设备已识别）
     */
    @NotNull
    @Schema(description = "用户ID", example = "1001", required = true)
    private Long userId;

    /**
     * 设备ID
     */
    @NotNull
    @Schema(description = "设备ID", example = "2001", required = true)
    private Long deviceId;

    /**
     * 区域ID
     */
    @NotNull
    @Schema(description = "区域ID", example = "3001", required = true)
    private Long areaId;

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号", example = "DEV001")
    private String serialNumber;

    /**
     * 卡号
     */
    @Schema(description = "卡号", example = "CARD001")
    private String cardNo;

    /**
     * 事件类型
     * 0=正常刷卡开门
     * 14=正常按指纹开门
     * 24=反潜
     * 25=互锁
     */
    @Schema(description = "事件类型", example = "0")
    private Integer event;

    /**
     * 验证方式
     * 0=密码
     * 1=指纹
     * 2=卡
     * 11=面部
     */
    @Schema(description = "验证方式", example = "1")
    private Integer verifyType;

    /**
     * 进出状态
     * 1=进
     * 2=出
     */
    @Schema(description = "进出状态", example = "1")
    private Integer inOutStatus;

    /**
     * 验证时间
     */
    @Schema(description = "验证时间")
    private LocalDateTime verifyTime;

    /**
     * 门号（多门设备）
     */
    @Schema(description = "门号", example = "1")
    private Integer doorNumber;
}
