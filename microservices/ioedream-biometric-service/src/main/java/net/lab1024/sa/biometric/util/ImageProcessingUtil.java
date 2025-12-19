package net.lab1024.sa.biometric.util;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * 图像处理工具类
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 工具类，不依赖Spring框架
 * - 提供静态方法进行图像处理
 * - 完整的错误处理
 * </p>
 * <p>
 * 核心功能:
 * - 图像格式转换
 * - 图像尺寸验证
 * - 图像质量基础检测
 * - 为OpenCV集成预留接口
 * </p>
 * <p>
 * ⚠️ 重要说明:
 * - 当前为基础实现，待集成OpenCV后增强
 * - 主要用于特征提取前的图像预处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class ImageProcessingUtil {

    /**
     * 最小图像尺寸（宽度）
     */
    private static final int MIN_IMAGE_WIDTH = 64;

    /**
     * 最小图像尺寸（高度）
     */
    private static final int MIN_IMAGE_HEIGHT = 64;

    /**
     * 最大图像尺寸（宽度）
     */
    private static final int MAX_IMAGE_WIDTH = 4096;

    /**
     * 最大图像尺寸（高度）
     */
    private static final int MAX_IMAGE_HEIGHT = 4096;

    /**
     * 从字节数组读取图像
     * <p>
     * 将字节数组转换为BufferedImage对象
     * </p>
     *
     * @param imageBytes 图像字节数组
     * @return BufferedImage对象
     * @throws BusinessException 如果图像格式不支持或读取失败
     */
    public static BufferedImage readImageFromBytes(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException("PARAM_ERROR", "图像数据不能为空");
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                throw new BusinessException("IMAGE_PROCESSING_ERROR", "无法读取图像，请检查图像格式是否支持");
            }

            log.debug("[图像处理] 读取图像成功, width={}, height={}", image.getWidth(), image.getHeight());
            return image;

        } catch (IOException e) {
            log.error("[图像处理] 读取图像失败", e);
            throw new BusinessException("IMAGE_PROCESSING_ERROR", "读取图像失败: " + e.getMessage());
        }
    }

    /**
     * 从Base64字符串读取图像
     * <p>
     * 将Base64编码的字符串转换为BufferedImage对象
     * </p>
     *
     * @param base64Image Base64编码的图像字符串
     * @return BufferedImage对象
     * @throws BusinessException 如果Base64格式错误或图像读取失败
     */
    public static BufferedImage readImageFromBase64(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "Base64图像数据不能为空");
        }

        try {
            // 移除Base64前缀（如果有）
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            return readImageFromBytes(imageBytes);

        } catch (IllegalArgumentException e) {
            log.error("[图像处理] Base64解码失败", e);
            throw new BusinessException("IMAGE_PROCESSING_ERROR", "Base64格式错误: " + e.getMessage());
        }
    }

    /**
     * 验证图像尺寸
     * <p>
     * 检查图像尺寸是否符合要求
     * </p>
     *
     * @param image 图像对象
     * @throws BusinessException 如果图像尺寸不符合要求
     */
    public static void validateImageSize(BufferedImage image) {
        if (image == null) {
            throw new BusinessException("PARAM_ERROR", "图像对象不能为空");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width < MIN_IMAGE_WIDTH || height < MIN_IMAGE_HEIGHT) {
            throw new BusinessException("IMAGE_PROCESSING_ERROR",
                    String.format("图像尺寸过小，最小尺寸: %dx%d，当前尺寸: %dx%d",
                            MIN_IMAGE_WIDTH, MIN_IMAGE_HEIGHT, width, height));
        }

        if (width > MAX_IMAGE_WIDTH || height > MAX_IMAGE_HEIGHT) {
            throw new BusinessException("IMAGE_PROCESSING_ERROR",
                    String.format("图像尺寸过大，最大尺寸: %dx%d，当前尺寸: %dx%d",
                            MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, width, height));
        }

        log.debug("[图像处理] 图像尺寸验证通过, width={}, height={}", width, height);
    }

    /**
     * 检测图像中的人脸数量
     * <p>
     * TODO: 集成OpenCV后实现真正的人脸检测
     * 当前实现为基础验证，返回模拟结果
     * </p>
     *
     * @param image 图像对象
     * @return 检测到的人脸数量（当前为模拟实现）
     * @throws BusinessException 如果图像处理失败
     */
    public static int detectFaceCount(BufferedImage image) {
        if (image == null) {
            throw new BusinessException("PARAM_ERROR", "图像对象不能为空");
        }

        // TODO: 集成OpenCV实现真正的人脸检测
        // Mat mat = convertToMat(image);
        // CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
        // MatOfRect faceDetections = new MatOfRect();
        // faceDetector.detectMultiScale(mat, faceDetections);
        // return faceDetections.toArray().length;

        // 临时实现：返回模拟结果（假设检测到1个人脸）
        log.warn("[图像处理] 使用模拟人脸检测，待集成OpenCV");
        return 1;
    }

    /**
     * 对齐人脸图像
     * <p>
     * TODO: 集成OpenCV后实现真正的人脸对齐
     * 当前实现为基础验证，返回原图像
     * </p>
     *
     * @param image 原始图像
     * @param faceRect 人脸区域（待OpenCV集成后使用）
     * @return 对齐后的人脸图像（当前返回原图像）
     * @throws BusinessException 如果图像处理失败
     */
    public static BufferedImage alignFace(BufferedImage image, Object faceRect) {
        if (image == null) {
            throw new BusinessException("PARAM_ERROR", "图像对象不能为空");
        }

        // TODO: 集成OpenCV实现真正的人脸对齐
        // Mat mat = convertToMat(image);
        // Mat alignedFace = alignFaceWithLandmarks(mat, faceRect);
        // return convertToBufferedImage(alignedFace);

        // 临时实现：返回原图像
        log.warn("[图像处理] 使用模拟人脸对齐，待集成OpenCV");
        return image;
    }

    /**
     * 评估图像质量
     * <p>
     * 评估图像的基本质量指标（清晰度、亮度等）
     * TODO: 集成OpenCV后实现更精确的质量评估
     * </p>
     *
     * @param image 图像对象
     * @return 质量分数（0.0-1.0，1.0表示质量最好）
     */
    public static double assessImageQuality(BufferedImage image) {
        if (image == null) {
            return 0.0;
        }

        try {
            // 基础质量评估
            double qualityScore = 0.0;

            // 1. 尺寸质量（尺寸越大，质量基础分越高）
            int width = image.getWidth();
            int height = image.getHeight();
            double sizeScore = Math.min(1.0, (width * height) / (640.0 * 480.0)) * 0.3;

            // 2. 清晰度评估（TODO: 集成OpenCV实现拉普拉斯算子）
            double sharpnessScore = 0.5; // 临时值

            // 3. 亮度评估（TODO: 集成OpenCV实现直方图分析）
            double brightnessScore = 0.5; // 临时值

            // 4. 对比度评估（TODO: 集成OpenCV实现对比度分析）
            double contrastScore = 0.5; // 临时值

            qualityScore = sizeScore + sharpnessScore * 0.3 + brightnessScore * 0.2 + contrastScore * 0.2;

            log.debug("[图像处理] 图像质量评估完成, qualityScore={}", qualityScore);
            return Math.min(1.0, qualityScore);

        } catch (Exception e) {
            log.error("[图像处理] 图像质量评估失败", e);
            return 0.5; // 默认质量分数
        }
    }

    /**
     * 将BufferedImage转换为字节数组
     * <p>
     * 用于将处理后的图像转换为字节数组
     * </p>
     *
     * @param image 图像对象
     * @param format 图像格式（如"jpg", "png"）
     * @return 图像字节数组
     * @throws BusinessException 如果转换失败
     */
    public static byte[] imageToBytes(BufferedImage image, String format) {
        if (image == null) {
            throw new BusinessException("PARAM_ERROR", "图像对象不能为空");
        }

        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            ImageIO.write(image, format, outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("[图像处理] 图像转换为字节数组失败", e);
            throw new BusinessException("IMAGE_PROCESSING_ERROR", "图像转换失败: " + e.getMessage());
        }
    }

    /**
     * 将图像转换为Base64字符串
     * <p>
     * 用于将处理后的图像转换为Base64编码
     * </p>
     *
     * @param image 图像对象
     * @param format 图像格式（如"jpg", "png"）
     * @return Base64编码的图像字符串
     * @throws BusinessException 如果转换失败
     */
    public static String imageToBase64(BufferedImage image, String format) {
        byte[] imageBytes = imageToBytes(image, format);
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
