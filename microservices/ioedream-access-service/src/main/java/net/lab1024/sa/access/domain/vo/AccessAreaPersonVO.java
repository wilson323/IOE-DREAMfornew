package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁区域人员视图对象
 * <p>
 * 用于区域空间管理模块显示区域内人员信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessAreaPersonVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户工号
     */
    private String userNo;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 权限类型
     * <p>
     * ALWAYS-永久权限
     * TIME_LIMITED-限时权限
     * </p>
     */
    private String permissionType;

    /**
     * 权限类型名称
     */
    private String permissionTypeName;

    /**
     * 生效开始时间
     */
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime endTime;

    /**
     * 是否在有效期内
     */
    private Boolean isValid;

    /**
     * 最后通行时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 今日通行次数
     */
    private Long todayAccessCount;
}
