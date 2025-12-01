package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 区域门禁扩展实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_area_ext")
public class AreaAccessExtEntity extends BaseEntity {

    /**
     * 扩展ID
     */
    @TableId
    private Long extId;

    /**
     * 基础区域ID
     */
    private Long areaId;

    /**
     * 门禁级别
     */
    private Integer accessLevel;

    /**
     * 门禁模式
     */
    private String accessMode;

    /**
     * 关联设备数量
     */
    private Integer deviceCount;

    /**
     * 是否需要安保人员
     */
    private Boolean guardRequired;

    /**
     * 时间限制配置
     */
    private String timeRestrictions;

    /**
     * 是否允许访客
     */
    private Boolean visitorAllowed;

    /**
     * 是否为紧急通道
     */
    private Boolean emergencyAccess;

    /**
     * 是否启用监控
     */
    private Boolean monitoringEnabled;
}