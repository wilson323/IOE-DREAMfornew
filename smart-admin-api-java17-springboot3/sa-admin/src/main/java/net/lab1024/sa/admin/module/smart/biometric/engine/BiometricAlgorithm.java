package net.lab1024.sa.admin.module.smart.biometric.engine;

import lombok.Data;
import net.lab1024.sa.admin.module.smart.biometric.constant.BiometricTypeEnum;

import java.util.Map;

/**
 * 生物识别算法基础接口
 *
 * 定义生物识别算法的通用接口，支持不同类型的生物识别算法实现
 * 提供统一的算法调用和管理机制
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
public interface BiometricAlgorithm {

    /**
     * 获取算法类型
     *
     * @return 生物识别类型枚举
     */
    BiometricTypeEnum getAlgorithmType();

    /**
     * 初始化算法
     *
     * @param config 算法配置参数
     * @return 初始化是否成功
     */
    boolean initialize(Map<String, Object> config);

    /**
     * 注册生物特征模板
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param biometricData 生物特征数据
     * @return 注册结果
     */
    BiometricResult registerTemplate(Long userId, String deviceId, byte[] biometricData);

    /**
     * 删除生物特征模板
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    BiometricResult deleteTemplate(String templateId);

    /**
     * 验证生物特征
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param biometricData 待验证的生物特征数据
     * @param templateId 可选的模板ID（用于指定验证特定模板）
     * @return 验证结果
     */
    BiometricResult authenticate(Long userId, String deviceId, byte[] biometricData, String templateId);

    /**
     * 批量验证生物特征
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param biometricData 待验证的生物特征数据数组
     * @return 批量验证结果
     */
    BiometricBatchResult batchAuthenticate(Long userId, String deviceId, byte[][] biometricData);

    /**
     * 获取算法状态
     *
     * @return 算法状态信息
     */
    AlgorithmStatus getAlgorithmStatus();

    /**
     * 获取算法性能指标
     *
     * @return 性能指标
     */
    PerformanceMetrics getPerformanceMetrics();

    /**
     * 清理资源
     */
    void cleanup();

    /**
     * 生物识别结果
     */
    @Data
    class BiometricResult {
        /** 操作是否成功 */
        private boolean success;

        /** 置信度分数（0.0-1.0） */
        private double confidence;

        /** 处理时间（毫秒） */
        private long processingTimeMs;

        /** 结果详情 */
        private String message;

        /** 额外数据 */
        private Map<String, Object> extraData;

        /** 模板ID */
        private String templateId;

        public BiometricResult() {
            this.extraData = new java.util.HashMap<>();
        }

        public BiometricResult(boolean success, double confidence, long processingTimeMs, String message) {
            this();
            this.success = success;
            this.confidence = confidence;
            this.processingTimeMs = processingTimeMs;
            this.message = message;
        }

        public BiometricResult(boolean success, double confidence, long processingTimeMs, String message, String templateId) {
            this(success, confidence, processingTimeMs, message);
            this.templateId = templateId;
        }
    }

    /**
     * 批量生物识别结果
     */
    @Data
    class BiometricBatchResult {
        /** 批量操作是否全部成功 */
        private boolean allSuccess;

        /** 成功数量 */
        private int successCount;

        /** 失败数量 */
        private int failureCount;

        /** 总处理时间（毫秒） */
        private long totalProcessingTimeMs;

        /** 平均处理时间（毫秒） */
        private double avgProcessingTimeMs;

        /** 平均置信度（0.0-1.0） */
        private double avgConfidence;

        /** 最高置信度 */
        private double maxConfidence;

        /** 最低置信度 */
        private double minConfidence;

        /** 详细结果列表 */
        private java.util.List<BiometricResult> results;

        /** 额外数据 */
        private Map<String, Object> extraData;

        public BiometricBatchResult() {
            this.results = new java.util.ArrayList<>();
            this.extraData = new java.util.HashMap<>();
        }
    }

    /**
     * 算法状态枚举
     */
    enum AlgorithmStatus {
        /** 未初始化 */
        UNINITIALIZED,
        /** 正在初始化 */
        INITIALIZING,
        /** 已就绪 */
        READY,
        /** 忙碌中 */
        BUSY,
        /** 错误状态 */
        ERROR,
        /** 已停止 */
        STOPPED
    }

    /**
     * 性能指标
     */
    @Data
    public static class PerformanceMetrics {
        /** 总调用次数 */
        private long totalCalls;

        /** 成功调用次数 */
        private long successCalls;

        /** 失败调用次数 */
        private long failureCalls;

        /** 平均处理时间（毫秒） */
        private double avgProcessingTimeMs;

        /** 最大处理时间（毫秒） */
        private long maxProcessingTimeMs;

        /** 最小处理时间（毫秒） */
        private long minProcessingTimeMs;

        /** 平均置信度 */
        private double avgConfidence;

        /** 内存使用量（字节） */
        private long memoryUsageBytes;

        /** CPU使用率（百分比） */
        private double cpuUsagePercent;

        /** 线程池活跃线程数 */
        private int activeThreads;

        /** 队列长度 */
        private int queueSize;

        // 手动添加setter方法以确保编译通过
        public void setFailureCalls(long failureCalls) {
            this.failureCalls = failureCalls;
        }

        public void setMinProcessingTime(long minProcessingTime) {
            this.minProcessingTimeMs = minProcessingTime;
        }
    }
}