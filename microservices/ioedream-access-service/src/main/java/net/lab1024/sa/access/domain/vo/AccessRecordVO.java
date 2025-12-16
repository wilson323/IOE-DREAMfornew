package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁记录视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 用于Controller返回给前端的数据
 * - 不包含敏感信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessRecordVO {

    /**
     * 记录ID
     */
    private Long logId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 操作类型（验证方式）
     */
    private String operation;

    /**
     * 操作结果
     * <p>
     * 1-成功
     * 2-失败
     * 3-异常
     * </p>
     */
    private Integer result;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 备注
     */
    private String remark;
}


