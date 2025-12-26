package net.lab1024.sa.common.storage.config;

import lombok.extern.slf4j.Slf4j;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * MinIO配置类
 *
 * 仅当 file.storage.type=minio 时生效
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Configuration
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "minio")
@Slf4j
public class MinIOConfig {

    @Resource
    private FileStorageProperties storageProperties;

    /**
     * 创建MinIO客户端
     */
    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                .endpoint(storageProperties.getMinio().getEndpoint())
                .credentials(
                    storageProperties.getMinio().getAccessKey(),
                    storageProperties.getMinio().getSecretKey()
                )
                .build();

            log.info("[MinIO配置] MinioClient创建成功, endpoint={}",
                    storageProperties.getMinio().getEndpoint());

            return client;
        } catch (Exception e) {
            log.error("[MinIO配置] MinioClient创建失败", e);
            throw new RuntimeException("MinioClient创建失败: " + e.getMessage());
        }
    }
}