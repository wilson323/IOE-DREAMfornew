package net.lab1024.sa.hr.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.hr.domain.request.PhotoQualityCheckRequest;
import net.lab1024.sa.hr.domain.response.PhotoQualityCheckResponse;
import net.lab1024.sa.hr.service.EmployeePhotoQualityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 员工照片质量验证服务实现
 * 根据用户指导，仅实现照片质量验证，不实现生物识别算法
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Service
public class EmployeePhotoQualityServiceImpl implements EmployeePhotoQualityService {

    // 人脸识别照片质量要求
    private static final int FACE_MIN_WIDTH = 200;
    private static final int FACE_MIN_HEIGHT = 200;
    private static final int FACE_MAX_WIDTH = 4000;
    private static final int FACE_MAX_HEIGHT = 4000;
    private static final long FACE_MIN_FILE_SIZE = 1024; // 1KB
    private static final long FACE_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // 员工证件照质量要求
    private static final int EMPLOYEE_MIN_WIDTH = 295;
    private static final int EMPLOYEE_MIN_HEIGHT = 413;
    private static final int EMPLOYEE_MAX_WIDTH = 1200;
    private static final int EMPLOYEE_MAX_HEIGHT = 1600;
    private static final long EMPLOYEE_MIN_FILE_SIZE = 5 * 1024; // 5KB
    private static final long EMPLOYEE_MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @Override
    public PhotoQualityCheckResponse checkPhotoQuality(PhotoQualityCheckRequest request) {
        try {
            String imageBase64 = request.getImageBase64();

            // 基本验证
            if (StringUtils.isBlank(imageBase64)) {
                return PhotoQualityCheckResponse.builder()
                        .pass(false)
                        .score(0.0)
                        .reason("照片数据为空")
                        .suggestion("请重新上传照片")
                        .build();
            }

            // 移除Base64前缀（如果有）
            String cleanBase64 = imageBase64;
            String format = "unknown";
            if (imageBase64.startsWith("data:image/")) {
                format = extractFormat(imageBase64);
                cleanBase64 = imageBase64.substring(imageBase64.indexOf(",") + 1);
            }

            // 验证图片格式
            if (!isValidImageFormat(cleanBase64)) {
                return PhotoQualityCheckResponse.builder()
                        .pass(false)
                        .score(0.0)
                        .reason("不支持的图片格式")
                        .suggestion("请使用JPG、PNG等常见格式的照片")
                        .format(format)
                        .build();
            }

            // 获取图片尺寸
            ImageDimensions dimensions = getImageDimensions(cleanBase64);

            // 验证分辨率
            if (!meetsMinimumResolution(dimensions.width(), dimensions.height(), request.getUsageType())) {
                PhotoQualityCheckResponse.PhotoUsageRequirements requirements = getUsageRequirements(request.getUsageType());
                return PhotoQualityCheckResponse.builder()
                        .pass(false)
                        .score(0.0)
                        .reason(String.format("图片分辨率过低，要求至少%d×%d，实际为%d×%d",
                                requirements.minWidth, requirements.minHeight, dimensions.width(), dimensions.height()))
                        .suggestion("请上传更高分辨率的照片")
                        .dimensions(String.format("%d×%d", dimensions.width(), dimensions.height()))
                        .format(format)
                        .build();
            }

            // 验证图片大小
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            if (!isReasonableImageSize(imageBytes.length, request.getUsageType())) {
                PhotoQualityCheckResponse.PhotoUsageRequirements requirements = getUsageRequirements(request.getUsageType());
                return PhotoQualityCheckResponse.builder()
                        .pass(false)
                        .score(0.0)
                        .reason(String.format("图片大小不合理，要求%dKB-%dMB，实际为%dKB",
                                requirements.minSizeKB, requirements.maxSizeMB, imageBytes.length / 1024))
                        .suggestion("请调整照片大小后重新上传")
                        .fileSize((long) imageBytes.length)
                        .dimensions(String.format("%d×%d", dimensions.width(), dimensions.height()))
                        .format(format)
                        .build();
            }

            // 检查宽高比
            double aspectRatio = (double) dimensions.width() / dimensions.height();
            if (!isGoodAspectRatio(aspectRatio, request.getUsageType())) {
                PhotoQualityCheckResponse.PhotoUsageRequirements requirements = getUsageRequirements(request.getUsageType());
                return PhotoQualityCheckResponse.builder()
                        .pass(false)
                        .score(50.0)
                        .reason(String.format("照片宽高比不符合要求，建议比例%.1f-%.1f，实际为%.2f",
                                requirements.minAspectRatio, requirements.maxAspectRatio, aspectRatio))
                        .suggestion("请调整照片比例后重新上传")
                        .fileSize((long) imageBytes.length)
                        .dimensions(String.format("%d×%d", dimensions.width(), dimensions.height()))
                        .aspectRatio(aspectRatio)
                        .format(format)
                        .build();
            }

            // 计算质量得分
            double score = calculateQualityScore(dimensions, imageBytes.length, aspectRatio, request.getUsageType());
            boolean pass = score >= 70.0; // 70分以上认为合格

            return PhotoQualityCheckResponse.builder()
                    .pass(pass)
                    .score(score)
                    .dimensions(String.format("%d×%d", dimensions.width(), dimensions.height()))
                    .fileSize((long) imageBytes.length)
                    .aspectRatio(aspectRatio)
                    .format(format)
                    .reason(pass ? "照片质量合格" : "照片质量未达到标准")
                    .suggestion(pass ? null : "请尝试重新拍摄或调整照片")
                    .build();

        } catch (Exception e) {
            log.error("照片质量检查失败", e);
            return PhotoQualityCheckResponse.builder()
                    .pass(false)
                    .score(0.0)
                    .reason("照片处理异常: " + e.getMessage())
                    .suggestion("请检查照片格式或重新上传")
                    .build();
        }
    }

    @Override
    public boolean isValidImageFormat(String imageBase64) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            return image != null;
        } catch (Exception e) {
            log.debug("图片格式验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ImageDimensions getImageDimensions(String imageBase64) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (image == null) {
            throw new IOException("无法读取图片数据");
        }

        return new ImageDimensions(image.getWidth(), image.getHeight());
    }

    @Override
    public boolean meetsMinimumResolution(int width, int height, PhotoQualityCheckRequest.PhotoUsageType usageType) {
        PhotoQualityCheckResponse.PhotoUsageRequirements requirements = getUsageRequirements(usageType);
        return width >= requirements.minWidth && height >= requirements.minHeight
                && width <= requirements.maxWidth && height <= requirements.maxHeight;
    }

    @Override
    public boolean isReasonableImageSize(long imageSizeBytes, PhotoQualityCheckRequest.PhotoUsageType usageType) {
        PhotoQualityCheckResponse.PhotoUsageRequirements requirements = getUsageRequirements(usageType);
        return imageSizeBytes >= requirements.minSizeKB * 1024 && imageSizeBytes <= requirements.maxSizeMB * 1024 * 1024;
    }

    private String extractFormat(String imageBase64) {
        if (imageBase64.contains("jpeg") || imageBase64.contains("jpg")) {
            return "jpg";
        } else if (imageBase64.contains("png")) {
            return "png";
        } else if (imageBase64.contains("gif")) {
            return "gif";
        } else if (imageBase64.contains("bmp")) {
            return "bmp";
        }
        return "unknown";
    }

    private PhotoQualityCheckResponse.PhotoUsageRequirements getUsageRequirements(PhotoQualityCheckRequest.PhotoUsageType usageType) {
        return switch (usageType) {
            case FACE_RECOGNITION -> new PhotoQualityCheckResponse.PhotoUsageRequirements(
                    FACE_MIN_WIDTH, FACE_MIN_HEIGHT, FACE_MAX_WIDTH, FACE_MAX_HEIGHT,
                    FACE_MIN_FILE_SIZE / 1024, FACE_MAX_FILE_SIZE / (1024 * 1024),
                    0.7, 1.5);
            case EMPLOYEE_PHOTO -> new PhotoQualityCheckResponse.PhotoUsageRequirements(
                    EMPLOYEE_MIN_WIDTH, EMPLOYEE_MIN_HEIGHT, EMPLOYEE_MAX_WIDTH, EMPLOYEE_MAX_HEIGHT,
                    EMPLOYEE_MIN_FILE_SIZE / 1024, EMPLOYEE_MAX_FILE_SIZE / (1024 * 1024),
                    0.6, 0.8); // 证件照建议比例接近3:4
            case VISITOR_PHOTO -> new PhotoQualityCheckResponse.PhotoUsageRequirements(
                    FACE_MIN_WIDTH, FACE_MIN_HEIGHT, FACE_MAX_WIDTH, FACE_MAX_HEIGHT,
                    FACE_MIN_FILE_SIZE / 1024, FACE_MAX_FILE_SIZE / (1024 * 1024),
                    0.7, 1.5);
        };
    }

    private boolean isGoodAspectRatio(double aspectRatio, PhotoQualityCheckRequest.PhotoUsageType usageType) {
        PhotoQualityCheckResponse.PhotoUsageRequirements requirements = getUsageRequirements(usageType);
        return aspectRatio >= requirements.minAspectRatio && aspectRatio <= requirements.maxAspectRatio;
    }

    private double calculateQualityScore(ImageDimensions dimensions, long fileSizeBytes, double aspectRatio, PhotoQualityCheckRequest.PhotoUsageType usageType) {
        PhotoQualityCheckResponse.PhotoUsageRequirements requirements = getUsageRequirements(usageType);
        double score = 0.0;

        // 分辨率得分（0-40分）
        int minDimension = Math.min(dimensions.width(), dimensions.height());
        if (minDimension >= 1080) {
            score += 40;
        } else if (minDimension >= 720) {
            score += 35;
        } else if (minDimension >= 480) {
            score += 30;
        } else if (minDimension >= requirements.minWidth * 1.5) {
            score += 25;
        } else {
            score += 15;
        }

        // 文件大小得分（0-30分）
        long fileSizeKB = fileSizeBytes / 1024;
        long optimalSizeKB = (requirements.minSizeKB + requirements.maxSizeMB * 1024) / 2;
        if (fileSizeKB >= optimalSizeKB * 0.8 && fileSizeKB <= optimalSizeKB * 1.2) {
            score += 30;
        } else if (fileSizeKB >= optimalSizeKB * 0.5) {
            score += 25;
        } else if (fileSizeKB >= requirements.minSizeKB * 2) {
            score += 20;
        } else {
            score += 10;
        }

        // 宽高比得分（0-30分）
        double optimalRatio = (requirements.minAspectRatio + requirements.maxAspectRatio) / 2;
        double ratioDeviation = Math.abs(aspectRatio - optimalRatio);
        if (ratioDeviation <= 0.1) {
            score += 30;
        } else if (ratioDeviation <= 0.2) {
            score += 25;
        } else if (ratioDeviation <= 0.3) {
            score += 20;
        } else {
            score += 10;
        }

        return Math.min(100.0, score);
    }
}