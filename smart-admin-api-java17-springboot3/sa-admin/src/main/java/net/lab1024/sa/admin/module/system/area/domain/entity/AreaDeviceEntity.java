package net.lab1024.sa.admin.module.system.area.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区域设备关联实体类
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
@TableName("t_area_device")
public class AreaDeviceEntity {

    /**
     * 关联ID
     */
    @TableId(type = IdType.AUTO)
    private Long relationId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 绑定人ID
     */
    private Long bindUserId;

    /**
     * 解绑时间
     */
    private LocalDateTime unbindTime;

    /**
     * 解绑人ID
     */
    private Long unbindUserId;

    /**
     * 绑定备注
     */
    private String bindRemark;

    /**
     * 状态：1-绑定，0-解绑
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deletedFlag;

}
