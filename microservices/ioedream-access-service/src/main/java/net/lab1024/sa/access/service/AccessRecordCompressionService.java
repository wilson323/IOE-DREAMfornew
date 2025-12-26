package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.common.organization.entity.AccessRecordEntity;

/**
 * 门禁通行记录数据压缩服务
 * <p>
 * 用于压缩和解压缩通行记录数据，减少存储空间，提升查询性能
 * </p>
 * <p>
 * 技术方案：
 * - 使用Snappy压缩算法（高压缩速度，中等压缩率）
 * - 压缩目标：存储空间减少50%
 * - 性能要求：压缩/解压速度<100ms (1000条记录)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
public interface AccessRecordCompressionService {

    /**
     * 压缩通行记录列表
     *
     * @param records 通行记录列表
     * @return 压缩后的字节数组
     */
    byte[] compressRecords(List<AccessRecordEntity> records);

    /**
     * 解压缩通行记录数据
     *
     * @param compressed 压缩后的字节数组
     * @return 通行记录列表
     */
    List<AccessRecordEntity> decompressRecords(byte[] compressed);

    /**
     * 计算压缩率
     *
     * @param originalSize 原始大小（字节）
     * @param compressedSize 压缩后大小（字节）
     * @return 压缩率（例如：0.5表示压缩到50%）
     */
    double calculateCompressionRatio(long originalSize, long compressedSize);

    /**
     * 批量压缩通行记录（分批处理，避免内存溢出）
     *
     * @param records 通行记录列表
     * @param batchSize 每批处理数量
     * @return 压缩后的字节数组列表
     */
    List<byte[]> batchCompressRecords(List<AccessRecordEntity> records, int batchSize);
}
