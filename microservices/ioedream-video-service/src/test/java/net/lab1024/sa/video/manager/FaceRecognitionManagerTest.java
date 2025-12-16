package net.lab1024.sa.video.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import net.lab1024.sa.video.sdk.AiSdkProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 人脸识别管理器单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@DisplayName("人脸识别管理器测试")
@ExtendWith(MockitoExtension.class)
class FaceRecognitionManagerTest {

    @Mock
    private AiSdkProvider aiSdkProvider;

    private FaceRecognitionManager faceRecognitionManager;

    @BeforeEach
    void setUp() {
        faceRecognitionManager = new FaceRecognitionManager(aiSdkProvider);
    }

    @Test
    @DisplayName("人脸检测 - 返回结果列表")
    void detectFaces_shouldReturnList() {
        byte[] imageData = new byte[1024];

        List<FaceRecognitionManager.FaceDetectResult> results = faceRecognitionManager.detectFaces(imageData);

        assertNotNull(results);
        // 当前实现返回空列表，实际需要AI模型
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("特征提取 - 返回特征向量")
    void extractFeature_shouldReturnFeatureVector() {
        byte[] faceImage = new byte[512];

        byte[] feature = faceRecognitionManager.extractFeature(faceImage);

        assertNotNull(feature);
        assertEquals(512, feature.length);
    }

    @Test
    @DisplayName("人脸比对 - 返回相似度")
    void compareFaces_shouldReturnSimilarity() {
        byte[] feature1 = new byte[512];
        byte[] feature2 = new byte[512];

        double similarity = faceRecognitionManager.compareFaces(feature1, feature2);

        assertTrue(similarity >= 0.0 && similarity <= 1.0);
    }

    @Test
    @DisplayName("1:N人脸搜索 - 返回匹配结果")
    void searchFace_shouldReturnMatchResults() {
        byte[] feature = new byte[512];

        List<FaceRecognitionManager.FaceMatchResult> results = faceRecognitionManager.searchFace(feature, 0.8);

        assertNotNull(results);
    }

    @Test
    @DisplayName("注册人脸 - 成功")
    void registerFace_shouldSucceed() {
        byte[] faceImage = new byte[512];

        boolean result = faceRecognitionManager.registerFace(1L, faceImage);

        assertTrue(result);
    }

    @Test
    @DisplayName("黑名单比对 - 返回结果")
    void checkBlacklist_shouldReturnResult() {
        byte[] faceImage = new byte[512];

        FaceRecognitionManager.BlacklistCheckResult result = faceRecognitionManager.checkBlacklist(faceImage);

        assertNotNull(result);
        assertFalse(result.inBlacklist());
    }
}
