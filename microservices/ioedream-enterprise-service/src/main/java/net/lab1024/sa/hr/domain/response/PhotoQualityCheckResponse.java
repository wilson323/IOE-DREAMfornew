package net.lab1024.sa.hr.domain.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * 照片质量检查响应
 *
 * @author IOE-DREAM Team
 */
@Data
@Builder
public class PhotoQualityCheckResponse {

    /**
     * 是否通过质量检查
     */
    private boolean pass;

    /**
     * 质量得分（0-100）
     */
    private double score;

    /**
     * 检查失败原因
     */
    private String reason;

    /**
     * 图片尺寸（宽x高）
     */
    private String dimensions;

    /**
     * 图片文件大小（字节）
     */
    private Long fileSize;

    /**
     * 宽高比
     */
    private Double aspectRatio;

    /**
     * 图片格式
     */
    private String format;

    /**
     * 建议信息
     */
    private String suggestion;

    /**
     * 照片用途要求
     */
    @AllArgsConstructor
    public static class PhotoUsageRequirements {
        public final int minWidth;
        public final int minHeight;
        public final int maxWidth;
        public final int maxHeight;
        public final long minSizeKB;
        public final long maxSizeMB;
        public final double minAspectRatio;
        public final double maxAspectRatio;
    }
}