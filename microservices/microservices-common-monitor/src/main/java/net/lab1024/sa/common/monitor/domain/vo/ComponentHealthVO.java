package net.lab1024.sa.common.monitor.domain.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 组件健康VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@JsonFormat格式化时间字段
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class ComponentHealthVO {

    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 组件类型
     */
    private String componentType;

    /**
     * 状态（HEALTHY、WARNING、CRITICAL、DOWN）
     */
    private String status;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 最后检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 详细信息
     */
    private String details;
}

