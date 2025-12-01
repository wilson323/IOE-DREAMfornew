package net.lab1024.sa.admin.module.consume.engine.mode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 引擎健康检查结果
 * 严格遵循repowiki规范：定义引擎健康状态检查的标准返回格式
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Schema(description = "引擎健康检查结果")
public class EngineHealthResult {

    @Schema(description = "引擎名称")
    private String engineName;

    @Schema(description = "健康状态：HEALTHY-健康，UNHEALTHY-不健康，DEGRADED-降级")
    private String status;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "响应时间（毫秒）")
    private Long responseTime;

    @Schema(description = "版本信息")
    private String version;

    @Schema(description = "启动时间")
    private LocalDateTime startTime;

    @Schema(description = "正常运行时间（秒）")
    private Long uptime;

    @Schema(description = "注册的模块数量")
    private Integer moduleCount;

    @Schema(description = "检查详情")
    private Map<String, Object> details;

    @Schema(description = "错误信息")
    private String errorMessage;

    // ===== 手动添加getter/setter方法 =====

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    public Integer getModuleCount() {
        return moduleCount;
    }

    public void setModuleCount(Integer moduleCount) {
        this.moduleCount = moduleCount;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isHealthy() {
        return "HEALTHY".equals(this.status);
    }

    public boolean isFallbackAvailable() {
        return this.details != null && Boolean.TRUE.equals(this.details.get("fallbackAvailable"));
    }

    public void setHealthy(boolean healthy) {
        this.status = healthy ? "HEALTHY" : "UNHEALTHY";
    }

    public void setMessage(String message) {
        this.errorMessage = message;
    }

    /**
     * 创建健康结果
     *
     * @param engineName 引擎名称
     * @return 健康结果
     */
    public static EngineHealthResult healthy(String engineName) {
        EngineHealthResult result = new EngineHealthResult();
        result.setEngineName(engineName);
        result.setStatus("HEALTHY");
        result.setCheckTime(LocalDateTime.now());
        result.setVersion("1.0.0");
        result.setStartTime(LocalDateTime.now());
        result.setUptime(0L);
        result.setModuleCount(0);
        return result;
    }

    /**
     * 创建不健康结果
     *
     * @param engineName 引擎名称
     * @param errorMessage 错误信息
     * @return 不健康结果
     */
    public static EngineHealthResult unhealthy(String engineName, String errorMessage) {
        EngineHealthResult result = new EngineHealthResult();
        result.setEngineName(engineName);
        result.setStatus("UNHEALTHY");
        result.setCheckTime(LocalDateTime.now());
        result.setErrorMessage(errorMessage);
        return result;
    }

    /**
     * 创建降级结果
     *
     * @param engineName 引擎名称
     * @param details 详细信息
     * @return 降级结果
     */
    public static EngineHealthResult degraded(String engineName, Map<String, Object> details) {
        EngineHealthResult result = new EngineHealthResult();
        result.setEngineName(engineName);
        result.setStatus("DEGRADED");
        result.setCheckTime(LocalDateTime.now());
        result.setDetails(details);
        return result;
    }
}