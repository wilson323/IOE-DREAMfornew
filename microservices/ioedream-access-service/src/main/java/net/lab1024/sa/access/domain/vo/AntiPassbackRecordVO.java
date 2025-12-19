package net.lab1024.sa.access.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反潜回记录视图对象
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
public class AntiPassbackRecordVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 进出状态
     * 1=进
     * 2=出
     */
    private Integer inOutStatus;

    /**
     * 进出状态描述
     */
    private String inOutStatusDesc;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 通行类型
     * IN=进入
     * OUT=离开
     */
    private String accessType;

    /**
     * 验证方式
     * 0=密码
     * 1=指纹
     * 2=卡
     * 11=面部
     */
    private Integer verifyType;

    /**
     * 验证方式描述
     */
    private String verifyTypeDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
