package net.lab1024.sa.monitor.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 组件健康状态VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class ComponentHealthVO {

    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 组件类型 (SERVICE、DATABASE、CACHE、MESSAGE_QUEUE、EXTERNAL_API)
     */
    private String componentType;

    /**
     * 健康状态 (UP、DOWN、WARNING、UNKNOWN)
     */
    private String status;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 版本信息
     */
    private String version;

    /**
     * 环境信息
     */
    private String environment;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 详细信息
     */
    private Map<String, Object> details;

    /**
     * 指标数据
     */
    private Map<String, Object> metrics;

    /**
     * 标签
     */
    private String tags;

    /**
     * 运行时长（秒）
     */
    private Long uptime;

    /**
     * 可用性（百分比）
     */
    private Double availability;
}