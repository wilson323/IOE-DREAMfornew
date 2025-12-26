package net.lab1024.sa.common.monitor.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组件健康状态视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class ComponentHealthVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 组件类型
     */
    private String componentType;

    /**
     * 状态
     */
    private String status;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 错误消息
     */
    private String errorMessage;
}

