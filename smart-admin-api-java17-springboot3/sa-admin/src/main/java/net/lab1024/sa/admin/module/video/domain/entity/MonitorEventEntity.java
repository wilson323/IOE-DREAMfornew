package net.lab1024.sa.admin.module.video.domain.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 监控事件实体（占位实现）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_monitor_event")
public class MonitorEventEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long eventId;

    private Long deviceId;

    private String eventType;

    private String eventLevel;

    private String eventStatus;

    private String eventDescription;

    private LocalDateTime eventTime;

    private Boolean isHandled;

    private LocalDateTime handleTime;

    private Long handleUserId;

    private String handleResult;

    private String eventData;

    private String remark;
}