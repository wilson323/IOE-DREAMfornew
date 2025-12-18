package net.lab1024.sa.biometric.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生物模板视图对象
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用VO后缀命名
 * - 包含展示字段
 * - 使用@Schema注解描述字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Schema(description = "生物模板视图对象")
public class BiometricTemplateVO {

    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "生物识别类型", example = "1")
    private Integer biometricType;

    @Schema(description = "生物识别类型描述", example = "人脸识别")
    private String biometricTypeDesc;

    @Schema(description = "模板名称", example = "用户人脸特征模板")
    private String templateName;

    @Schema(description = "模板状态", example = "1")
    private Integer templateStatus;

    @Schema(description = "模板状态描述", example = "激活")
    private String templateStatusDesc;

    @Schema(description = "匹配阈值", example = "0.85")
    private Double matchThreshold;

    @Schema(description = "算法版本", example = "v2.1.0")
    private String algorithmVersion;

    @Schema(description = "设备ID", example = "DEVICE_001")
    private String deviceId;

    @Schema(description = "采集时间", example = "2025-01-30T14:30:00")
    private LocalDateTime captureTime;

    @Schema(description = "过期时间", example = "2026-01-30T14:30:00")
    private LocalDateTime expireTime;

    @Schema(description = "使用次数", example = "156")
    private Integer useCount;

    @Schema(description = "验证成功次数", example = "152")
    private Integer successCount;

    @Schema(description = "验证失败次数", example = "4")
    private Integer failCount;

    @Schema(description = "成功率", example = "97.43%")
    private String successRate;

    @Schema(description = "上次使用时间", example = "2025-01-30T09:15:00")
    private LocalDateTime lastUseTime;

    @Schema(description = "图片路径", example = "/biometric/face/1001_20250130.jpg")
    private String imagePath;

    @Schema(description = "质量分数", example = "0.95")
    private Double qualityScore;

    @Schema(description = "模板版本", example = "1.0")
    private String templateVersion;

    @Schema(description = "创建时间", example = "2025-01-30T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-30T14:30:00")
    private LocalDateTime updateTime;
}
