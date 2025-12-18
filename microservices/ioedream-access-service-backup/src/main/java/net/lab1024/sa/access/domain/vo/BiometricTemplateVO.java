package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生物识别模板视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 用于Controller返回模板信息
 * - 包含完整的生物识别模板数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "生物识别模板视图对象")
public class BiometricTemplateVO {

    /**
     * 模板ID
     */
    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 生物识别类型
     */
    @Schema(description = "生物识别类型", example = "1")
    private Integer biometricType;

    /**
     * 生物识别类型描述
     */
    @Schema(description = "生物识别类型描述", example = "人脸识别")
    private String biometricTypeDesc;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", example = "用户人脸特征模板")
    private String templateName;

    /**
     * 模板状态
     */
    @Schema(description = "模板状态", example = "1")
    private Integer templateStatus;

    /**
     * 模板状态描述
     */
    @Schema(description = "模板状态描述", example = "激活")
    private String templateStatusDesc;

    /**
     * 匹配阈值
     */
    @Schema(description = "匹配阈值", example = "0.85")
    private Double matchThreshold;

    /**
     * 算法版本
     */
    @Schema(description = "算法版本", example = "v2.1.0")
    private String algorithmVersion;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "DEVICE_001")
    private String deviceId;

    /**
     * 采集时间
     */
    @Schema(description = "采集时间", example = "2025-01-30T14:30:00")
    private LocalDateTime captureTime;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间", example = "2026-01-30T14:30:00")
    private LocalDateTime expireTime;

    /**
     * 使用次数
     */
    @Schema(description = "使用次数", example = "156")
    private Integer useCount;

    /**
     * 认证成功次数
     */
    @Schema(description = "认证成功次数", example = "152")
    private Integer successCount;

    /**
     * 认证失败次数
     */
    @Schema(description = "认证失败次数", example = "4")
    private Integer failCount;

    /**
     * 成功率
     */
    @Schema(description = "成功率", example = "97.43%")
    private String successRate;

    /**
     * 上次使用时间
     */
    @Schema(description = "上次使用时间", example = "2025-01-30T09:15:00")
    private LocalDateTime lastUseTime;

    /**
     * 图片路径
     */
    @Schema(description = "图片路径", example = "/biometric/face/1001_20250130.jpg")
    private String imagePath;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30T14:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-30T14:30:00")
    private LocalDateTime updateTime;

    /**
     * 特征数据
     */
    @Schema(description = "特征数据", example = "加密的生物识别特征数据")
    private String featureData;

    /**
     * 特征向量
     */
    @Schema(description = "特征向量", example = "AI算法生成的特征向量")
    private String featureVector;

    // ========== 统计相关字段 ==========

    /**
     * 用户总模板数
     */
    @Schema(description = "用户总模板数", example = "3")
    private Integer totalTemplates;

    /**
     * 用户总使用次数
     */
    @Schema(description = "用户总使用次数", example = "1250")
    private Integer totalUseCount;

    /**
     * 用户总成功次数
     */
    @Schema(description = "用户总成功次数", example = "1190")
    private Integer totalSuccessCount;

    /**
     * 用户总失败次数
     */
    @Schema(description = "用户总失败次数", example = "60")
    private Integer totalFailCount;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "人脸识别模板")
    private String remarks;
}