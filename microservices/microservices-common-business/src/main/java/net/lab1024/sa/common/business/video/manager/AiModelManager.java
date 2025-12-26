package net.lab1024.sa.common.business.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.video.AiModelEntity;

import java.math.BigDecimal;

/**
 * AI模型管理Manager
 * <p>
 * 提供AI模型的业务逻辑：
 * 1. 模型版本管理
 * 2. 模型状态控制
 * 3. 文件大小验证
 * 4. 模型类型管理
 * </p>
 * <p>
 * 注意：这是纯Java类，提供业务逻辑辅助方法。
 * 数据访问操作由Service层通过DAO完成。
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@Slf4j
public class AiModelManager {

    /**
     * 验证文件大小
     *
     * @param fileSize 文件大小（字节）
     * @return 是否有效
     */
    public boolean validateFileSize(Long fileSize) {
        log.debug("[AI模型Manager] 验证文件大小: size={} bytes", fileSize);

        // 模型文件不能超过500MB
        boolean valid = fileSize != null && fileSize > 0 && fileSize <= 500L * 1024 * 1024;

        if (!valid) {
            log.warn("[AI模型Manager] 文件大小验证失败: size={}", fileSize);
        }

        return valid;
    }

    /**
     * 验证模型版本格式
     *
     * @param modelVersion 模型版本
     * @return 是否有效
     */
    public boolean validateModelVersion(String modelVersion) {
        if (modelVersion == null || modelVersion.isEmpty()) {
            return false;
        }

        // 版本格式应为 x.y.z
        boolean valid = modelVersion.matches("^\\d+\\.\\d+\\.\\d+$");

        if (!valid) {
            log.warn("[AI模型Manager] 模型版本格式无效: version={}", modelVersion);
        }

        return valid;
    }

    /**
     * 验证模型状态转换
     *
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @return 是否允许转换
     */
    public boolean validateStatusTransition(Integer currentStatus, Integer targetStatus) {
        log.debug("[AI模型Manager] 验证状态转换: current={}, target={}", currentStatus, targetStatus);

        // 状态定义：0-草稿 1-已发布 2-已弃用
        // 允许的转换：
        // 草稿 -> 已发布
        // 已发布 -> 已弃用
        // 草稿 -> 已弃用
        boolean valid = switch (currentStatus) {
            case 0 -> targetStatus == 1 || targetStatus == 2; // 草稿 -> 已发布/已弃用
            case 1 -> targetStatus == 2; // 已发布 -> 已弃用
            default -> false;
        };

        if (!valid) {
            log.warn("[AI模型Manager] 状态转换不允许: current={}, target={}", currentStatus, targetStatus);
        }

        return valid;
    }

    /**
     * 获取模型状态名称
     *
     * @param status 状态值
     * @return 状态名称
     */
    public String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }

        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "已弃用";
            default -> "未知";
        };
    }

    /**
     * 格式化文件大小为MB
     *
     * @param fileSize 文件大小（字节）
     * @return 格式化后的字符串
     */
    public String formatFileSizeMb(Long fileSize) {
        if (fileSize == null) {
            return "N/A";
        }

        return String.format("%.2f", fileSize / (1024.0 * 1024.0));
    }

    /**
     * 计算准确率百分比
     *
     * @param accuracyRate 准确率（0-1）
     * @return 百分比字符串
     */
    public String calculateAccuracyPercent(BigDecimal accuracyRate) {
        if (accuracyRate == null) {
            return "N/A";
        }

        return accuracyRate.multiply(BigDecimal.valueOf(100))
                .setScale(0, BigDecimal.ROUND_HALF_UP) + "%";
    }

    /**
     * 生成模型存储路径
     *
     * @param modelType    模型类型
     * @param modelName    模型名称
     * @param modelVersion 模型版本
     * @return 存储路径
     */
    public String generateModelPath(String modelType, String modelName, String modelVersion) {
        // MinIO存储路径：ai-models/{modelType}/{modelName}/{modelVersion}/model.onnx
        // 例如：ai-models/YOLOv8/fall_detection/2.0.0/model.onnx
        return String.format("ai-models/%s/%s/%s/model.onnx", modelType, modelName, modelVersion);
    }

    /**
     * 从完整路径提取相对路径
     *
     * @param fullPath 完整路径
     * @return 相对路径（用于MinIO对象键）
     */
    public String extractRelativePath(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) {
            return "";
        }

        // 移除开头的斜杠（如果有）
        String path = fullPath.startsWith("/") ? fullPath.substring(1) : fullPath;

        log.debug("[AI模型Manager] 提取相对路径: fullPath={}, relativePath={}", fullPath, path);
        return path;
    }
}
