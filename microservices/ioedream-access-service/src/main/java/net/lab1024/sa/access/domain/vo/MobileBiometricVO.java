package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 移动端生物识别响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端生物识别响应")
public class MobileBiometricVO {

    @Schema(description = "验证结果", example = "success")
    private String result;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "置信度", example = "0.95")
    private Double confidence;

    @Schema(description = "匹配成功", example = "true")
    private Boolean matched;

    @Schema(description = "访问权限", example = "true")
    private Boolean hasAccess;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    @Schema(description = "验证时间", example = "2025-01-30T12:00:00")
    private LocalDateTime verifyTime;

    @Schema(description = "验证消息", example = "验证通过")
    private String message;

    @Schema(description = "生物特征类型", example = "face")
    private String biometricType;
}
