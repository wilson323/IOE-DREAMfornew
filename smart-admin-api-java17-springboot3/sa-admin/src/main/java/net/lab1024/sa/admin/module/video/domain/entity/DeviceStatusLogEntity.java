package net.lab1024.sa.admin.module.video.domain.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 设备状态日志实体（占位实现）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_status_log")
public class DeviceStatusLogEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private Long deviceId;

    private String logType;

    private String logLevel;

    private String logMessage;

    private LocalDateTime logTime;

    private String logData;

    private Long operateUserId;

    private String operateType;

    private String remark;
}