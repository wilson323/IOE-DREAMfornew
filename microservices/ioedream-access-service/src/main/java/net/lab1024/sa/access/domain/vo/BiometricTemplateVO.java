package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生物识别模板视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "生物识别模板视图对象")
public class BiometricTemplateVO {

    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

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

    @Schema(description = "创建时间", example = "2025-01-30T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-30T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "特征数据", example = "加密的生物识别特征数据")
    private String featureData;

    @Schema(description = "特征向量", example = "AI算法生成的特征向量")
    private String featureVector;

    // ========== 统计相关字段 ==========

    @Schema(description = "用户总模板数", example = "3")
    private Integer totalTemplates;

    @Schema(description = "用户总使用次数", example = "1250")
    private Integer totalUseCount;

    @Schema(description = "用户总成功次数", example = "1190")
    private Integer totalSuccessCount;

    @Schema(description = "用户总失败次数", example = "60")
    private Integer totalFailCount;

    @Schema(description = "备注", example = "人脸识别模板")
    private String remarks;
}