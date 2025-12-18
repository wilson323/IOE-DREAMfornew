package net.lab1024.sa.common.edge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 边缘设备实体类
 * 用于表示边缘计算节点的基本信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型 (VIDEO/ACCESS/ATTENDANCE等)
     */
    private String deviceType;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 设备状态 (ONLINE/OFFLINE/BUSY/ERROR)
     */
    private String status;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * AI能力标签 (支持的AI模型类型)
     */
    private String aiCapabilities;

    /**
     * 设备性能配置
     */
    private EdgeConfig config;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 备注信息
     */
    private String remark;
}
