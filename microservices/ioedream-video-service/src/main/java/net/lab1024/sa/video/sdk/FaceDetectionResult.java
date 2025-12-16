package net.lab1024.sa.video.sdk;

import lombok.Data;

import java.util.List;

/**
 * 人脸检测结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class FaceDetectionResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 检测到的人脸列表
     */
    private List<FaceInfo> faces;

    /**
     * 处理耗时（毫秒）
     */
    private long costTime;

    /**
     * 人脸信息
     */
    @Data
    public static class FaceInfo {
        /**
         * 人脸ID
         */
        private String faceId;

        /**
         * 人脸位置X
         */
        private int x;

        /**
         * 人脸位置Y
         */
        private int y;

        /**
         * 人脸宽度
         */
        private int width;

        /**
         * 人脸高度
         */
        private int height;

        /**
         * 置信度
         */
        private double confidence;

        /**
         * 人脸角度-俯仰
         */
        private float pitch;

        /**
         * 人脸角度-偏航
         */
        private float yaw;

        /**
         * 人脸角度-翻滚
         */
        private float roll;

        /**
         * 人脸图像数据
         */
        private byte[] faceImage;

        /**
         * 人脸特征
         */
        private byte[] feature;
    }
}
