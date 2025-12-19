package net.lab1024.sa.video.edge.communication;

import net.lab1024.sa.video.edge.model.EdgeDevice;

/**
 * 边缘通信管理器
 * <p>
 * 说明：
 * - 负责与边缘设备建立/断开连接（最小接口，满足编译）
 * - 具体协议通讯由设备通讯服务承担，本模块只保留抽象
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public interface EdgeCommunicationManager {

    /**
     * 建立与边缘设备的连接
     *
     * @param device 边缘设备
     * @return 是否连接成功
     */
    boolean connectToDevice(EdgeDevice device);

    /**
     * 断开与边缘设备的连接
     *
     * @param device 边缘设备
     */
    void disconnectFromDevice(EdgeDevice device);
}

