package net.lab1024.sa.biometric.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;

/**
 * 特征向量对象池
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 管理特征向量对象的复用，减少内存分配和GC压力
 * - 使用Apache Commons Pool2实现
 * - 512维向量对象池（FaceNet标准）
 * - 对象复用机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class FeatureVectorPool {

    /**
     * 特征向量对象池
     * <p>
     * 使用float[]数组存储512维特征向量（FaceNet标准维度）
     * </p>
     */
    private GenericObjectPool<float[]> vectorPool;

    /**
     * 初始化对象池
     */
    @PostConstruct
    public void init() {
        GenericObjectPoolConfig<float[]> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(1000); // 最大对象数
        config.setMaxIdle(100); // 最大空闲对象数
        config.setMinIdle(10); // 最小空闲对象数
        config.setTestOnBorrow(true); // 借用时测试对象
        config.setTestOnReturn(false); // 归还时测试对象
        config.setTestWhileIdle(true); // 空闲时测试对象
        config.setMaxWaitMillis(5000); // 最大等待时间（毫秒）

        this.vectorPool = new GenericObjectPool<>(
                new FeatureVectorFactory(),
                config
        );

        log.info("[特征向量对象池] 初始化完成，最大对象数: {}, 最大空闲: {}, 最小空闲: {}",
                config.getMaxTotal(), config.getMaxIdle(), config.getMinIdle());
    }

    /**
     * 借用特征向量
     * <p>
     * 从对象池中获取一个特征向量对象
     * </p>
     *
     * @return 特征向量（512维float数组）
     * @throws Exception 获取对象失败
     */
    public float[] borrowVector() throws Exception {
        return vectorPool.borrowObject();
    }

    /**
     * 归还特征向量
     * <p>
     * 将特征向量归还到对象池，清空数据供后续复用
     * </p>
     *
     * @param vector 特征向量
     */
    public void returnVector(float[] vector) {
        if (vector != null) {
            // 清空数据
            Arrays.fill(vector, 0.0f);
            vectorPool.returnObject(vector);
        }
    }

    /**
     * 获取对象池统计信息
     *
     * @return 统计信息
     */
    public PoolStatistics getStatistics() {
        return PoolStatistics.builder()
                .active(vectorPool.getNumActive())
                .idle(vectorPool.getNumIdle())
                .maxTotal(vectorPool.getMaxTotal())
                .maxIdle(vectorPool.getMaxIdle())
                .minIdle(vectorPool.getMinIdle())
                .build();
    }

    /**
     * 关闭对象池
     */
    @PreDestroy
    public void close() {
        if (vectorPool != null) {
            vectorPool.close();
            log.info("[特征向量对象池] 已关闭");
        }
    }

    /**
     * 特征向量工厂
     */
    private static class FeatureVectorFactory extends org.apache.commons.pool2.BasePooledObjectFactory<float[]> {

        /**
         * 特征向量维度（FaceNet标准）
         */
        private static final int VECTOR_DIMENSION = 512;

        @Override
        public float[] create() throws Exception {
            return new float[VECTOR_DIMENSION];
        }

        @Override
        public org.apache.commons.pool2.PooledObject<float[]> wrap(float[] obj) {
            return new org.apache.commons.pool2.impl.DefaultPooledObject<>(obj);
        }

        @Override
        public boolean validateObject(org.apache.commons.pool2.PooledObject<float[]> p) {
            float[] vector = p.getObject();
            return vector != null && vector.length == VECTOR_DIMENSION;
        }

        @Override
        public void passivateObject(org.apache.commons.pool2.PooledObject<float[]> p) throws Exception {
            // 归还时清空数据
            Arrays.fill(p.getObject(), 0.0f);
        }
    }

    /**
     * 对象池统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class PoolStatistics {
        private int active;
        private int idle;
        private int maxTotal;
        private int maxIdle;
        private int minIdle;
    }
}
