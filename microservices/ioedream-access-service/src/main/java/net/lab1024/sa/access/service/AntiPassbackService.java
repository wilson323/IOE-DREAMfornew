package net.lab1024.sa.access.service;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 门禁反潜回服务接口
 * <p>
 * 内存优化设计原则：
 * - 接口精简，职责单一
 * - 使用异步处理，提高并发性能
 * - 熔断器保护，防止级联故障
 * - 缓存策略优化，减少重复查询
 * - 批量操作支持，减少IO开销
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AntiPassbackService {

    // ==================== 反潜回验证 ====================

    /**
     * 执行反潜回检查
     * <p>
     * 检查用户是否存在潜回行为
     * 支持多种反潜回模式：硬反潜回、软反潜回、区域反潜回
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param verificationData 验证数据
     * @return 反潜回检查结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackCheckFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<AntiPassbackResult>> performAntiPassbackCheck(
            Long userId,
            Long deviceId,
            Long areaId,
            String verificationData
    );

    /**
     * 检查区域反潜回
     * <p>
     * 检查用户是否从区域A进入后未从区域B离开
     * 适用于需要成对进出的区域控制
     * </p>
     *
     * @param userId 用户ID
     * @param entryAreaId 进入区域ID
     * @param exitAreaId 离开区域ID
     * @param direction 进出方向（in/out）
     * @return 区域反潜回检查结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackCheckFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<AntiPassbackResult>> checkAreaAntiPassback(
            Long userId,
            Long entryAreaId,
            Long exitAreaId,
            String direction
    );

    // ==================== 反潜回配置管理 ====================

    /**
     * 设置设备反潜回策略
     *
     * @param deviceId 设备ID
     * @param antiPassbackType 反潜回类型（hard/soft/area/global）
     * @param config 配置参数
     * @return 设置结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> setAntiPassbackPolicy(
            Long deviceId,
            String antiPassbackType,
            Map<String, Object> config
    );

    /**
     * 获取设备反潜回策略
     *
     * @param deviceId 设备ID
     * @return 反潜回策略信息
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getAntiPassbackPolicy(Long deviceId);

    /**
     * 更新反潜回配置
     *
     * @param deviceId 设备ID
     * @param config 配置参数
     * @return 更新结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> updateAntiPassbackConfig(
            Long deviceId,
            Map<String, Object> config
    );

    // ==================== 反潜回记录管理 ====================

    /**
     * 记录通行事件
     * <p>
     * 记录用户通行事件，用于反潜回分析
     * 包括进出时间、设备、区域、方向等信息
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param direction 进出方向
     * @param verificationData 验证数据
     * @param result 通行结果
     * @return 记录结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> recordAccessEvent(
            Long userId,
            Long deviceId,
            Long areaId,
            String direction,
            String verificationData,
            Boolean result
    );

    /**
     * 获取用户反潜回状态
     *
     * @param userId 用户ID
     * @return 用户反潜回状态
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getUserAntiPassbackStatus(Long userId);

    /**
     * 清理用户反潜回记录
     * <p>
     * 清理用户的反潜回记录，通常在正常完成进出流程后调用
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 清理结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> clearUserAntiPassbackRecords(
            Long userId,
            Long deviceId
    );

    // ==================== 反潜回异常处理 ====================

    /**
     * 处理反潜回异常
     * <p>
     * 当检测到反潜回异常时进行处理
     * 包括告警通知、记录异常、临时锁定等
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param antiPassbackResult 反潜回结果
     * @return 处理结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> handleAntiPassbackViolation(
            Long userId,
            Long deviceId,
            AntiPassbackResult antiPassbackResult
    );

    /**
     * 重置用户反潜回状态
     * <p>
     * 管理员手动重置用户的反潜回状态
     * 用于解决异常情况或误报
     * </p>
     *
     * @param userId 用户ID
     * @param operatorId 操作员ID
     * @param reason 重置原因
     * @return 重置结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> resetUserAntiPassbackStatus(
            Long userId,
            Long operatorId,
            String reason
    );

    // ==================== 反潜回统计分析 ====================

    /**
     * 获取反潜回统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getAntiPassbackStatistics(
            String startTime,
            String endTime
    );

    /**
     * 获取反潜回异常报告
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 异常报告
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getAntiPassbackViolationReport(
            Long deviceId,
            String startTime,
            String endTime
    );

    // ==================== 批量操作 ====================

    /**
     * 批量检查反潜回状态
     *
     * @param userIds 用户ID列表
     * @return 批量检查结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackBatchOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Map<Long, Object>>> batchCheckAntiPassbackStatus(
            String userIds
    );

    /**
     * 批量清理反潜回记录
     *
     * @param userIds 用户ID列表
     * @return 批量清理结果
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackBatchOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Map<Long, Object>>> batchClearAntiPassbackRecords(
            String userIds
    );

    // ==================== 内部数据类 ====================

    /**
     * 反潜回结果
     */
    class AntiPassbackResult {
        /**
         * 是否通过反潜回检查
         */
        private Boolean passed;

        /**
         * 反潜回类型
         */
        private String antiPassbackType;

        /**
         * 拒绝原因
         */
        private String denyReason;

        /**
         * 最后通行记录
         */
        private Object lastAccessRecord;

        /**
         * 违规级别
         */
        private String violationLevel;

        /**
         * 建议处理方式
         */
        private String recommendedAction;

        /**
         * 风险评分
         */
        private Integer riskScore;

        // Getters and Setters
        public Boolean getPassed() {
            return passed;
        }

        public void setPassed(Boolean passed) {
            this.passed = passed;
        }

        public String getAntiPassbackType() {
            return antiPassbackType;
        }

        public void setAntiPassbackType(String antiPassbackType) {
            this.antiPassbackType = antiPassbackType;
        }

        public String getDenyReason() {
            return denyReason;
        }

        public void setDenyReason(String denyReason) {
            this.denyReason = denyReason;
        }

        public Object getLastAccessRecord() {
            return lastAccessRecord;
        }

        public void setLastAccessRecord(Object lastAccessRecord) {
            this.lastAccessRecord = lastAccessRecord;
        }

        public String getViolationLevel() {
            return violationLevel;
        }

        public void setViolationLevel(String violationLevel) {
            this.violationLevel = violationLevel;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public void setRecommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
        }

        public Integer getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(Integer riskScore) {
            this.riskScore = riskScore;
        }

        public boolean isPassed() {
            return passed != null && passed;
        }
    }

    // ==================== 降级处理方法 ====================

    /**
     * 反潜回检查降级处理
     */
    default CompletableFuture<ResponseDTO<AntiPassbackResult>> antiPassbackCheckFallback(
            Long userId, Exception exception, Object... params) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTIPASSBACK_SERVICE_UNAVAILABLE", "反潜回服务暂时不可用，已降级为常规检查")
        );
    }

    /**
     * 反潜回操作降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> antiPassbackOperationFallback(
            Exception exception, Object... params) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTIPASSBACK_SERVICE_UNAVAILABLE", "反潜回服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 反潜回批量操作降级处理
     */
    default CompletableFuture<ResponseDTO<Map<Long, Object>>> antiPassbackBatchOperationFallback(
            String params, Exception exception) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTIPASSBACK_BATCH_SERVICE_UNAVAILABLE", "批量反潜回服务暂时不可用，请稍后重试")
        );
    }
}
