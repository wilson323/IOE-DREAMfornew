package net.lab1024.sa.consume.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 设备实体类
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@TableName("device")
public class DeviceEntity {

    /**
     * 设备ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备位置
     */
    @TableField("device_location")
    private String deviceLocation;

    /**
     * 设备状态
     */
    @TableField("device_status")
    private String deviceStatus;

    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 删除标记（0-未删除，1-已删除）
     */
    @TableField("deleted")
    private Integer deleted;
}