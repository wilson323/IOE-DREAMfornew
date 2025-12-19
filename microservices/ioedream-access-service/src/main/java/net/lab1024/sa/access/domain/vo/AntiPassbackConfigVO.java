package net.lab1024.sa.access.domain.vo;

import lombok.Data;

/**
 * 反潜回配置视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 用于Controller返回给前端的数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AntiPassbackConfigVO {

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 是否启用反潜回
     */
    private Boolean enabled;

    /**
     * 时间窗口（秒）
     */
    private Integer timeWindow;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;
}
