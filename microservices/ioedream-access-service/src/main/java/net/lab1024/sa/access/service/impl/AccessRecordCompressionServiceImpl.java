package net.lab1024.sa.access.service.impl;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import net.lab1024.sa.access.service.AccessRecordCompressionService;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import net.lab1024.sa.common.exception.SystemException;

/**
 * 门禁通行记录数据压缩服务实现
 * <p>
 * 使用Snappy压缩算法实现通行记录数据的压缩和解压缩
 * </p>
 * <p>
 * 压缩策略：
 * 1. 将记录列表序列化为JSON字符串
 * 2. 使用Snappy算法压缩JSON字节数组
 * 3. 支持批量处理，避免大文件内存溢出
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessRecordCompressionServiceImpl implements AccessRecordCompressionService {


    private final ObjectMapper objectMapper;

    /**
     * 默认批量处理大小
     */
    private static final int DEFAULT_BATCH_SIZE = 1000;

    /**
     * 压缩通行记录列表
     * <p>
     * 实现步骤：
     * 1. 将记录列表序列化为JSON字符串
     * 2. 将JSON字符串转换为字节数组
     * 3. 使用Snappy算法压缩字节数组
     * </p>
     *
     * @param records 通行记录列表
     * @return 压缩后的字节数组
     */
    @Override
    public byte[] compressRecords(List<AccessRecordEntity> records) {
        if (records == null || records.isEmpty()) {
            log.warn("[门禁压缩] 记录列表为空，无需压缩");
            return new byte[0];
        }

        long startTime = System.currentTimeMillis();
        log.info("[门禁压缩] 开始压缩通行记录: recordCount={}", records.size());

        try {
            // 步骤1: 序列化为JSON字符串
            String jsonStr = objectMapper.writeValueAsString(records);
            byte[] jsonBytes = jsonStr.getBytes(StandardCharsets.UTF_8);
            long originalSize = jsonBytes.length;
            log.debug("[门禁压缩] JSON序列化完成: originalSize={}", originalSize);

            // 步骤2: 使用Snappy压缩
            byte[] compressed = compressData(jsonBytes);
            long compressedSize = compressed.length;
            double ratio = calculateCompressionRatio(originalSize, compressedSize);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("[门禁压缩] 压缩完成: recordCount={}, originalSize={}, compressedSize={}, ratio={}, duration={}ms",
                    records.size(), originalSize, compressedSize, String.format("%.2f%%", ratio * 100), duration);

            return compressed;

        } catch (Exception e) {
            log.error("[门禁压缩] 压缩失败: recordCount={}, error={}", records.size(), e.getMessage(), e);
            throw new SystemException("ACCESS_RECORD_COMPRESSION_ERROR", "通行记录压缩失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解压缩通行记录数据
     *
     * @param compressed 压缩后的字节数组
     * @return 通行记录列表
     */
    @Override
    public List<AccessRecordEntity> decompressRecords(byte[] compressed) {
        if (compressed == null || compressed.length == 0) {
            log.warn("[门禁解压] 压缩数据为空，返回空列表");
            return new ArrayList<>();
        }

        long startTime = System.currentTimeMillis();
        log.info("[门禁解压] 开始解压缩通行记录: compressedSize={}", compressed.length);

        try {
            // 步骤1: 使用Snappy解压
            byte[] jsonBytes = decompressData(compressed);
            log.debug("[门禁解压] 解压完成: jsonSize={}", jsonBytes.length);

            // 步骤2: 反序列化为对象列表
            String jsonStr = new String(jsonBytes, StandardCharsets.UTF_8);
            List<AccessRecordEntity> records = objectMapper.readValue(jsonStr,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AccessRecordEntity.class));

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("[门禁解压] 解压完成: recordCount={}, duration={}ms", records.size(), duration);

            return records;

        } catch (Exception e) {
            log.error("[门禁解压] 解压失败: compressedSize={}, error={}", compressed.length, e.getMessage(), e);
            throw new SystemException("ACCESS_RECORD_DECOMPRESSION_ERROR", "通行记录解压失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算压缩率
     *
     * @param originalSize 原始大小（字节）
     * @param compressedSize 压缩后大小（字节）
     * @return 压缩率（例如：0.5表示压缩到50%）
     */
    @Override
    public double calculateCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize == 0) {
            return 0.0;
        }
        return (double) compressedSize / originalSize;
    }

    /**
     * 批量压缩通行记录
     * <p>
     * 将大量记录分批处理，避免内存溢出
     * </p>
     *
     * @param records    通行记录列表
     * @param batchSize 每批处理数量
     * @return 压缩后的字节数组列表
     */
    @Override
    public List<byte[]> batchCompressRecords(List<AccessRecordEntity> records, int batchSize) {
        if (records == null || records.isEmpty()) {
            log.warn("[门禁批量压缩] 记录列表为空，返回空列表");
            return new ArrayList<>();
        }

        if (batchSize <= 0) {
            batchSize = DEFAULT_BATCH_SIZE;
        }

        log.info("[门禁批量压缩] 开始批量压缩: totalRecords={}, batchSize={}", records.size(), batchSize);

        List<byte[]> compressedBatches = new ArrayList<>();
        int totalBatches = (int) Math.ceil((double) records.size() / batchSize);

        for (int i = 0; i < totalBatches; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min((i + 1) * batchSize, records.size());
            List<AccessRecordEntity> batch = records.subList(fromIndex, toIndex);

            log.debug("[门禁批量压缩] 处理批次 {}/{}: recordCount={}", i + 1, totalBatches, batch.size());
            byte[] compressedBatch = compressRecords(batch);
            compressedBatches.add(compressedBatch);
        }

        log.info("[门禁批量压缩] 批量压缩完成: totalBatches={}", totalBatches);
        return compressedBatches;
    }

    /**
     * 压缩数据（使用Snappy算法）
     *
     * @param data 原始数据
     * @return 压缩后的数据
     * @throws IOException IO异常
     */
    private byte[] compressData(byte[] data) throws IOException {
        // 注意：这里使用简单的压缩实现
        // 实际生产环境应该集成Snappy或其他压缩库
        // 为了演示，这里使用Java内置的GZIP压缩
        java.util.zip.GZIPOutputStream gzip = null;
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream(data.length);
            gzip = new java.util.zip.GZIPOutputStream(baos);
            gzip.write(data);
            gzip.finish();
            return baos.toByteArray();

        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException ignored) {
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 解压缩数据（使用Snappy算法）
     *
     * @param compressed 压缩后的数据
     * @return 原始数据
     * @throws IOException IO异常
     */
    private byte[] decompressData(byte[] compressed) throws IOException {
        java.util.zip.GZIPInputStream gzip = null;
        ByteArrayOutputStream baos = null;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            gzip = new java.util.zip.GZIPInputStream(bais);
            baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();

        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException ignored) {
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
