package net.lab1024.sa.device.comm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备通讯日志实体
 * <p>
 * 记录设备通讯的日志信息,包括请求/响应/错误等
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@TableName("t_device_comm_log")
public class DeviceCommLogEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 通讯类型 (REQUEST/RESPONSE/ERROR)
     */
    @TableField("comm_type")
    private String commType;

    /**
     * 通讯方向 (UP/DOWN)
     */
    @TableField("comm_direction")
    private String commDirection;

    /**
     * 协议类型
     */
    @TableField("protocol_type")
    private String protocolType;

    /**
     * 请求内容
     */
    @TableField("request_content")
    private String requestContent;

    /**
     * 响应内容
     */
    @TableField("response_content")
    private String responseContent;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 耗时(毫秒)
     */
    @TableField("duration")
    private Long duration;

    /**
     * 状态 (SUCCESS/FAILED)
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
