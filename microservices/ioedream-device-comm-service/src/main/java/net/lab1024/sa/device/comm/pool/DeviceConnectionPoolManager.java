package net.lab1024.sa.device.comm.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备连接池管理器
 * <p>
 * 管理设备连接的复用，提升性能和资源利用率
 * 严格遵循CLAUDE.md规范：
 * - 使用连接池模式实现
 * - 支持连接复用机制
 * - 配置优化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public class DeviceConnectionPoolManager {

    /**
     * 设备连接池映射表
     * Key: 设备ID, Value: 连接池
     */
    private final Map<Long, GenericObjectPool<DeviceConnection>> connectionPools = new ConcurrentHashMap<>();

    /**
     * 连接池配置
     */
    private final GenericObjectPoolConfig<DeviceConnection> poolConfig;

    /**
     * 构造函数
     * <p>
     * 初始化连接池配置
     * </p>
     */
    public DeviceConnectionPoolManager() {
        this.poolConfig = createPoolConfig();
    }

    /**
     * 获取设备连接
     * <p>
     * 从连接池中获取可用连接，如果不存在则创建新连接池
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备连接
     * @throws Exception 获取连接失败
     */
    public DeviceConnection borrowConnection(Long deviceId) throws Exception {
        GenericObjectPool<DeviceConnection> pool = connectionPools.computeIfAbsent(
                deviceId,
                k -> createConnectionPool(deviceId)
        );

        return pool.borrowObject();
    }

    /**
     * 归还设备连接
     * <p>
     * 将连接归还到连接池，供后续复用
     * </p>
     *
     * @param deviceId 设备ID
     * @param connection 设备连接
     */
    public void returnConnection(Long deviceId, DeviceConnection connection) {
        GenericObjectPool<DeviceConnection> pool = connectionPools.get(deviceId);
        if (pool != null) {
            pool.returnObject(connection);
        }
    }

    /**
     * 创建连接池配置
     */
    private GenericObjectPoolConfig<DeviceConnection> createPoolConfig() {
        GenericObjectPoolConfig<DeviceConnection> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(50); // 最大连接数
        config.setMaxIdle(10); // 最大空闲连接数
        config.setMinIdle(2); // 最小空闲连接数
        config.setTestOnBorrow(true); // 借用时测试连接
        config.setTestOnReturn(false); // 归还时测试连接
        config.setTestWhileIdle(true); // 空闲时测试连接
        config.setMaxWaitMillis(5000); // 最大等待时间（毫秒）
        return config;
    }

    /**
     * 创建设备连接池
     */
    private GenericObjectPool<DeviceConnection> createConnectionPool(Long deviceId) {
        log.info("[设备连接池] 创建连接池 deviceId={}", deviceId);
        return new GenericObjectPool<>(
                new DeviceConnectionFactory(deviceId),
                poolConfig
        );
    }

    /**
     * 关闭设备连接池
     *
     * @param deviceId 设备ID
     */
    public void closePool(Long deviceId) {
        GenericObjectPool<DeviceConnection> pool = connectionPools.remove(deviceId);
        if (pool != null) {
            pool.close();
            log.info("[设备连接池] 关闭连接池 deviceId={}", deviceId);
        }
    }

    /**
     * 关闭所有连接池
     */
    public void closeAllPools() {
        connectionPools.values().forEach(GenericObjectPool::close);
        connectionPools.clear();
        log.info("[设备连接池] 关闭所有连接池");
    }

    /**
     * 获取连接池统计信息
     *
     * @param deviceId 设备ID
     * @return 统计信息
     */
    public PoolStatistics getPoolStatistics(Long deviceId) {
        GenericObjectPool<DeviceConnection> pool = connectionPools.get(deviceId);
        if (pool == null) {
            return null;
        }

        return PoolStatistics.builder()
                .deviceId(deviceId)
                .active(pool.getNumActive())
                .idle(pool.getNumIdle())
                .maxTotal(pool.getMaxTotal())
                .maxIdle(pool.getMaxIdle())
                .minIdle(pool.getMinIdle())
                .build();
    }

    /**
     * 设备连接
     */
    public static class DeviceConnection {
        private final Long deviceId;
        private final String connectionId;
        private final long createTime;

        public DeviceConnection(Long deviceId, String connectionId) {
            this.deviceId = deviceId;
            this.connectionId = connectionId;
            this.createTime = System.currentTimeMillis();
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public long getCreateTime() {
            return createTime;
        }
    }

    /**
     * 连接池统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class PoolStatistics {
        private Long deviceId;
        private int active;
        private int idle;
        private int maxTotal;
        private int maxIdle;
        private int minIdle;
    }
}
