package net.lab1024.sa.device.comm.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.UUID;

/**
 * 设备连接工厂
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 用于创建和销毁设备连接，支持连接健康检查
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public class DeviceConnectionFactory extends BasePooledObjectFactory<DeviceConnectionPoolManager.DeviceConnection> {

    private final Long deviceId;

    /**
     * 构造函数
     *
     * @param deviceId 设备ID
     */
    public DeviceConnectionFactory(Long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public DeviceConnectionPoolManager.DeviceConnection create() throws Exception {
        String connectionId = UUID.randomUUID().toString();
        log.debug("[设备连接工厂] 创建连接 deviceId={}, connectionId={}", deviceId, connectionId);
        return new DeviceConnectionPoolManager.DeviceConnection(deviceId, connectionId);
    }

    @Override
    public PooledObject<DeviceConnectionPoolManager.DeviceConnection> wrap(DeviceConnectionPoolManager.DeviceConnection obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public boolean validateObject(PooledObject<DeviceConnectionPoolManager.DeviceConnection> p) {
        // 验证连接是否有效
        DeviceConnectionPoolManager.DeviceConnection connection = p.getObject();
        // 连接创建时间不超过1小时
        long age = System.currentTimeMillis() - connection.getCreateTime();
        return age < 3600000; // 1小时
    }

    @Override
    public void destroyObject(PooledObject<DeviceConnectionPoolManager.DeviceConnection> p) throws Exception {
        DeviceConnectionPoolManager.DeviceConnection connection = p.getObject();
        log.debug("[设备连接工厂] 销毁连接 deviceId={}, connectionId={}",
                connection.getDeviceId(), connection.getConnectionId());
    }
}
