package net.lab1024.sa.common.edge.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 边缘设备实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class EdgeDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String deviceName;
    private Integer deviceType;
    private String deviceStatus;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String description;
    private String ipAddress;
    private Integer port;
}
