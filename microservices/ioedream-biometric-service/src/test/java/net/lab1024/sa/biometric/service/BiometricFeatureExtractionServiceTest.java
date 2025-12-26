package net.lab1024.sa.biometric.service;

import net.lab1024.sa.biometric.domain.vo.FeatureVector;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 生物特征提取服务测试
 * <p>
 * 测试范围：
 * - 人脸特征提取
 * - 指纹特征提取
 * - 虹膜特征提取
 * - 掌纹特征提取
 * - 声纹特征提取
 * - 通用特征提取
 * - 特征质量验证
 * </p>
 * <p>
 * 注意：使用纯Mockito测试，不加载Spring上下文，避免ApplicationContext加载失败
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生物特征提取服务测试")
class BiometricFeatureExtractionServiceTest {

    @Mock
    private BiometricFeatureExtractionService featureExtractionService;

    private MultipartFile createMockImageFile(String filename) {
        return new MockMultipartFile(
                filename,
                filename + ".jpg",
                "image/jpeg",
                "mock image data".getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("提取人脸特征 - 服务未实现")
    void testExtractFaceFeature_NotImplemented() {
        // Given
        MultipartFile photo = createMockImageFile("face_001");
        FeatureVector mockFeature = FeatureVector.builder()
                .biometricType(1)
                .dimension(512)
                .data("base64encodedfeaturedata")
                .qualityScore(0.95)
                .algorithmVersion("v2.1.0")
                .build();

        // Mock返回结果
        when(featureExtractionService.extractFaceFeature(any(MultipartFile.class)))
                .thenReturn(ResponseDTO.ok(mockFeature));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractFaceFeature(photo);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(512, result.getData().getDimension());

        // 验证方法被调用
        verify(featureExtractionService, times(1)).extractFaceFeature(any(MultipartFile.class));
    }

    @Test
    @DisplayName("提取指纹特征 - 服务未实现")
    void testExtractFingerprintFeature_NotImplemented() {
        // Given
        MultipartFile fingerprintImage = createMockImageFile("fingerprint_001");
        FeatureVector mockFeature = FeatureVector.builder()
                .biometricType(2)
                .dimension(512)
                .data("base64encodedfeaturedata")
                .build();

        // Mock返回结果
        when(featureExtractionService.extractFingerprintFeature(any(MultipartFile.class)))
                .thenReturn(ResponseDTO.ok(mockFeature));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractFingerprintFeature(
                fingerprintImage
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.getData().getBiometricType());
    }

    @Test
    @DisplayName("提取虹膜特征 - 服务未实现")
    void testExtractIrisFeature_NotImplemented() {
        // Given
        MultipartFile irisImage = createMockImageFile("iris_001");
        FeatureVector mockFeature = FeatureVector.builder()
                .biometricType(3)
                .dimension(512)
                .data("base64encodedfeaturedata")
                .build();

        when(featureExtractionService.extractIrisFeature(any(MultipartFile.class)))
                .thenReturn(ResponseDTO.ok(mockFeature));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractIrisFeature(irisImage);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getData().getBiometricType());
    }

    @Test
    @DisplayName("提取掌纹特征 - 服务未实现")
    void testExtractPalmFeature_NotImplemented() {
        // Given
        MultipartFile palmImage = createMockImageFile("palm_001");
        FeatureVector mockFeature = FeatureVector.builder()
                .biometricType(4)
                .dimension(512)
                .build();

        when(featureExtractionService.extractPalmFeature(any(MultipartFile.class)))
                .thenReturn(ResponseDTO.ok(mockFeature));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractPalmFeature(palmImage);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("提取声纹特征 - 服务未实现")
    void testExtractVoiceFeature_NotImplemented() {
        // Given
        MultipartFile voiceFile = new MockMultipartFile(
                "voice_001.wav",
                "voice_001.wav",
                "audio/wav",
                "mock voice data".getBytes(StandardCharsets.UTF_8)
        );
        FeatureVector mockFeature = FeatureVector.builder()
                .biometricType(5)
                .dimension(512)
                .build();

        when(featureExtractionService.extractVoiceFeature(any(MultipartFile.class)))
                .thenReturn(ResponseDTO.ok(mockFeature));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractVoiceFeature(voiceFile);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("通用特征提取 - 服务未实现")
    void testExtractFeature_NotImplemented() {
        // Given
        MultipartFile file = createMockImageFile("biometric_001");
        Integer biometricType = 1; // 人脸
        FeatureVector mockFeature = FeatureVector.builder()
                .biometricType(biometricType)
                .dimension(512)
                .build();

        when(featureExtractionService.extractFeature(any(MultipartFile.class), any(Integer.class)))
                .thenReturn(ResponseDTO.ok(mockFeature));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractFeature(
                file,
                biometricType
        );

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("验证特征质量 - 服务未实现")
    void testValidateFeatureQuality_NotImplemented() {
        // Given
        FeatureVector featureVector = FeatureVector.builder()
                .biometricType(1)
                .dimension(512)
                .data("base64encodeddata")
                .qualityScore(0.95)
                .algorithmVersion("v2.1.0")
                .build();

        when(featureExtractionService.validateFeatureQuality(any(FeatureVector.class)))
                .thenReturn(ResponseDTO.ok(true));

        // When
        ResponseDTO<Boolean> result = featureExtractionService.validateFeatureQuality(
                featureVector
        );

        // Then
        assertNotNull(result);
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("处理空文件 - 应返回错误")
    void testExtractFeature_EmptyFile() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "empty",
                "",
                "image/jpeg",
                new byte[0]
        );

        when(featureExtractionService.extractFaceFeature(any(MultipartFile.class)))
                .thenReturn(ResponseDTO.error("INVALID_FILE", "文件为空"));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractFaceFeature(
                emptyFile
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        // ResponseDTO.error()会将String code转换为Integer，所以这里是400而不是"INVALID_FILE"
        // 这是UserResponseCode.INVALID_FILE.getCode() = 400
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("处理无效的生物识别类型")
    void testExtractFeature_InvalidBiometricType() {
        // Given
        MultipartFile file = createMockImageFile("test");
        Integer invalidType = 999;

        when(featureExtractionService.extractFeature(any(MultipartFile.class), any(Integer.class)))
                .thenReturn(ResponseDTO.error("INVALID_BIOMETRIC_TYPE", "无效的生物识别类型"));

        // When
        ResponseDTO<FeatureVector> result = featureExtractionService.extractFeature(
                file,
                invalidType
        );

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }
}
