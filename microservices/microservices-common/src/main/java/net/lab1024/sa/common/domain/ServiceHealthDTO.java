package net.lab1024.sa.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 微服务健康状态DTO
 * <p>
 * 标准化的微服务健康检查响应数据结构
 * 用于微服务间通信和监控系统集成
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "微服务健康状态")
public class ServiceHealthDTO {

    @Schema(description = "服务名称")
    private String serviceName;

    @Schema(description = "服务状态: UP-正常, DOWN-异常, UNKNOWN-未知")
    private String status;

    @Schema(description = "健康检查时间戳")
    private LocalDateTime timestamp;

    @Schema(description = "服务运行时间，格式: HH:mm:ss")
    private String uptime;

    @Schema(description = "服务版本")
    private String version;

    @Schema(description = "服务端口")
    private Integer port;

    @Schema(description = "JVM内存信息")
    private MemoryInfo memory;

    @Schema(description = "服务发现信息")
    private DiscoveryInfo discovery;

    @Schema(description = "依赖服务状态")
    private Map<String, String> dependencies;

    @Schema(description = "自定义健康指标")
    private Map<String, Object> customMetrics;

    @Schema(description = "错误信息（如果有）")
    private String errorMessage;

    /**
     * JVM内存信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryInfo {

        @Schema(description = "总内存(MB)")
        private Long totalMemory;

        @Schema(description = "已使用内存(MB)")
        private Long usedMemory;

        @Schema(description = "空闲内存(MB)")
        private Long freeMemory;

        @Schema(description = "最大内存(MB)")
        private Long maxMemory;

        @Schema(description = "内存使用率(%)")
        private Double usagePercentage;
    }

    /**
     * 服务发现信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscoveryInfo {

        @Schema(description = "是否启用服务发现")
        private Boolean enabled;

        @Schema(description = "注册中心类型")
        private String registryType;

        @Schema(description = "服务实例数量")
        private Integer instanceCount;

        @Schema(description = "注册状态")
        private String registrationStatus;
    }

    /**
     * 创建健康的服务状态
     */
    public static ServiceHealthDTO healthy(String serviceName) {
        return ServiceHealthDTO.builder()
                .serviceName(serviceName)
                .status("UP")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建异常的服务状态
     */
    public static ServiceHealthDTO unhealthy(String serviceName, String errorMessage) {
        return ServiceHealthDTO.builder()
                .serviceName(serviceName)
                .status("DOWN")
                .timestamp(LocalDateTime.now())
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建未知的服务状态
     */
    public static ServiceHealthDTO unknown(String serviceName) {
        return ServiceHealthDTO.builder()
                .serviceName(serviceName)
                .status("UNKNOWN")
                .timestamp(LocalDateTime.now())
                .build();
    }
}