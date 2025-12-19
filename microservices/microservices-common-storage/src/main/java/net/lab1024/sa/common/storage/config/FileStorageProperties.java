package net.lab1024.sa.common.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 文件存储配置属性
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /**
     * 存储类型: minio/local/aliyun-oss
     * 默认: local (本地文件系统)
     */
    private String type = "local";

    /**
     * 本地文件系统配置
     */
    private LocalConfig local = new LocalConfig();

    /**
     * MinIO配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * 阿里云OSS配置
     */
    private AliyunOssConfig aliyunOss = new AliyunOssConfig();

    /**
     * 允许的文件类型
     */
    private String[] allowedTypes = {"image/jpeg", "image/png", "video/mp4", "video/avi", "application/pdf"};

    /**
     * 最大图片大小(MB)
     */
    private Integer maxImageSize = 10;

    /**
     * 最大视频大小(MB)
     */
    private Integer maxVideoSize = 100;

    /**
     * 本地文件系统配置
     */
    @Data
    public static class LocalConfig {
        /**
         * 文件存储根目录
         */
        private String basePath = "/data/ioedream/files";

        /**
         * URL访问前缀
         */
        private String urlPrefix = "http://localhost:8080/files";

        /**
         * 自动清理配置
         */
        private FileCleanupProperties cleanup = new FileCleanupProperties();
    }

    /**
     * MinIO配置
     */
    @Data
    public static class MinioConfig {
        /**
         * MinIO服务地址
         */
        private String endpoint = "http://localhost:9000";

        /**
         * 访问密钥
         */
        private String accessKey = "minioadmin";

        /**
         * 密钥
         */
        private String secretKey = "minioadmin";

        /**
         * 默认存储桶
         */
        private String bucketName = "ioedream";

        /**
         * URL有效期(秒)
         */
        private Integer urlExpiry = 604800; // 7天

        /**
         * 分块上传大小(MB)
         */
        private Integer partSize = 10;
    }

    /**
     * 阿里云OSS配置
     */
    @Data
    public static class AliyunOssConfig {
        /**
         * Endpoint
         */
        private String endpoint;

        /**
         * AccessKey ID
         */
        private String accessKeyId;

        /**
         * AccessKey Secret
         */
        private String accessKeySecret;

        /**
         * Bucket名称
         */
        private String bucketName;

        /**
         * CDN域名
         */
        private String cdnDomain;
    }
}
