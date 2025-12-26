package net.lab1024.sa.attendance.engine.algorithm;

import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;

/**
 * 排班算法接口
 * <p>
 * 定义智能排班算法的标准接口，支持多种算法策略
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ScheduleAlgorithm {

    /**
     * 算法类型
     */
    String getAlgorithmType();

    /**
     * 算法名称
     */
    String getAlgorithmName();

    /**
     * 算法描述
     */
    String getAlgorithmDescription();

    /**
     * 生成排班方案
     *
     * @param scheduleData 排班数据
     * @return 排班结果
     */
    ScheduleResult generateSchedule(ScheduleData scheduleData);

    /**
     * 初始化算法参数
     *
     * @param parameters 参数配置
     */
    void initialize(java.util.Map<String, Object> parameters);

    /**
     * 获取算法参数
     *
     * @return 参数配置
     */
    java.util.Map<String, Object> getParameters();

    /**
     * 验证算法参数
     *
     * @param parameters 参数配置
     * @return 验证结果
     */
    AlgorithmValidationResult validateParameters(java.util.Map<String, Object> parameters);

    /**
     * 估算算法执行时间
     *
     * @param scheduleData 排班数据
     * @return 预估执行时间（毫秒）
     */
    long estimateExecutionTime(ScheduleData scheduleData);

    /**
     * 获取算法复杂度
     *
     * @return 时间复杂度和空间复杂度描述
     */
    AlgorithmComplexity getComplexity();

    /**
     * 检查算法是否适用于指定的数据规模
     *
     * @param employeeCount 员工数量
     * @param shiftCount 班次数量
     * @param timeRange 时间范围（天）
     * @return 是否适用
     */
    boolean isApplicable(int employeeCount, int shiftCount, int timeRange);

    /**
     * 获取算法适用场景
     *
     * @return 适用场景列表
     */
    java.util.List<String> getApplicationScenarios();

    /**
     * 设置算法回调
     *
     * @param callback 执行回调
     */
    void setCallback(AlgorithmCallback callback);

    /**
     * 获取算法状态
     *
     * @return 算法状态
     */
    AlgorithmStatus getStatus();

    /**
     * 获取算法元数据
     *
     * @return 算法元数据
     */
    AlgorithmMetadata getMetadata();

    /**
     * 停止算法执行
     */
    void stop();

    /**
     * 暂停算法执行
     */
    void pause();

    /**
     * 恢复算法执行
     */
    void resume();

    /**
     * 算法验证结果类
     */
    class AlgorithmValidationResult {
        private boolean valid;
        private String errorMessage;
        private java.util.List<String> warnings;
        private java.util.Map<String, Object> validationDetails;

        public AlgorithmValidationResult() {
            this.warnings = new java.util.ArrayList<>();
            this.validationDetails = new java.util.HashMap<>();
        }

        public AlgorithmValidationResult(boolean valid, String errorMessage) {
            this();
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public java.util.List<String> getWarnings() { return warnings; }
        public void setWarnings(java.util.List<String> warnings) { this.warnings = warnings; }

        public java.util.Map<String, Object> getValidationDetails() { return validationDetails; }
        public void setValidationDetails(java.util.Map<String, Object> validationDetails) { this.validationDetails = validationDetails; }

        public void addWarning(String warning) { this.warnings.add(warning); }
        public void addValidationDetail(String key, Object value) { this.validationDetails.put(key, value); }
    }

    /**
     * 算法复杂度类
     */
    class AlgorithmComplexity {
        private String timeComplexity;
        private String spaceComplexity;
        private String worstCaseTime;
        private String bestCaseTime;
        private String averageCaseTime;

        // Constructors
        public AlgorithmComplexity() {}

        public AlgorithmComplexity(String timeComplexity, String spaceComplexity) {
            this.timeComplexity = timeComplexity;
            this.spaceComplexity = spaceComplexity;
        }

        // Getters and Setters
        public String getTimeComplexity() { return timeComplexity; }
        public void setTimeComplexity(String timeComplexity) { this.timeComplexity = timeComplexity; }

        public String getSpaceComplexity() { return spaceComplexity; }
        public void setSpaceComplexity(String spaceComplexity) { this.spaceComplexity = spaceComplexity; }

        public String getWorstCaseTime() { return worstCaseTime; }
        public void setWorstCaseTime(String worstCaseTime) { this.worstCaseTime = worstCaseTime; }

        public String getBestCaseTime() { return bestCaseTime; }
        public void setBestCaseTime(String bestCaseTime) { this.bestCaseTime = bestCaseTime; }

        public String getAverageCaseTime() { return averageCaseTime; }
        public void setAverageCaseTime(String averageCaseTime) { this.averageCaseTime = averageCaseTime; }

        @Override
        public String toString() {
            return String.format("Time: %s, Space: %s", timeComplexity, spaceComplexity);
        }
    }

    /**
     * 算法回调接口
     */
    @FunctionalInterface
    interface AlgorithmCallback {
        void onProgress(AlgorithmProgress progress);
    }

    /**
     * 算法进度类
     */
    class AlgorithmProgress {
        private double progressPercentage; // 0.0 - 1.0
        private String currentPhase;
        private String message;
        private long elapsedTime;
        private long estimatedRemainingTime;

        // Constructors
        public AlgorithmProgress() {}

        public AlgorithmProgress(double progressPercentage, String currentPhase) {
            this.progressPercentage = progressPercentage;
            this.currentPhase = currentPhase;
        }

        // Getters and Setters
        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

        public String getCurrentPhase() { return currentPhase; }
        public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public long getElapsedTime() { return elapsedTime; }
        public void setElapsedTime(long elapsedTime) { this.elapsedTime = elapsedTime; }

        public long getEstimatedRemainingTime() { return estimatedRemainingTime; }
        public void setEstimatedRemainingTime(long estimatedRemainingTime) { this.estimatedRemainingTime = estimatedRemainingTime; }
    }

    /**
     * 算法状态枚举
     */
    enum AlgorithmStatus {
        INITIALIZED,    // 已初始化
        RUNNING,        // 运行中
        PAUSED,         // 已暂停
        STOPPED,        // 已停止
        COMPLETED,      // 已完成
        ERROR           // 错误
    }
}
