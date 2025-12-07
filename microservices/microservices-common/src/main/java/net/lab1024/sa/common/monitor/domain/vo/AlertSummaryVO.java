package net.lab1024.sa.common.monitor.domain.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 告警摘要VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@JsonFormat格式化时间字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class AlertSummaryVO {

    /**
     * 告警ID
     */
    private Long alertId;

    /**
     * 告警级别
     */
    private String alertLevel;

    /**
     * 告警标题
     */
    private String alertTitle;

    /**
     * 告警消息
     */
    private String alertMessage;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

