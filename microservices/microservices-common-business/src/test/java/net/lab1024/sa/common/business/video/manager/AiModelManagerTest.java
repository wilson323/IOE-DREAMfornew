package net.lab1024.sa.common.business.video.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI模型管理器单元测试
 * <p>
 * 目标覆盖率：>= 85%
 * 测试范围：AI模型管理的纯业务逻辑方法
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@DisplayName("AI模型管理器单元测试")
class AiModelManagerTest {

    private AiModelManager aiModelManager;

    @BeforeEach
    void setUp() {
        aiModelManager = new AiModelManager();
    }

    // ==================== 文件大小验证测试 ====================

    @Test
    @DisplayName("验证文件大小 - 正常大小")
    void validateFileSize_ValidSize_ReturnTrue() {
        // Given
        Long fileSize = 100L * 1024 * 1024; // 100MB

        // When
        boolean result = aiModelManager.validateFileSize(fileSize);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证文件大小 - 最大边界值")
    void validateFileSize_MaxBoundary_ReturnTrue() {
        // Given
        Long fileSize = 500L * 1024 * 1024; // 500MB

        // When
        boolean result = aiModelManager.validateFileSize(fileSize);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证文件大小 - 超过限制")
    void validateFileSize_ExceedsLimit_ReturnFalse() {
        // Given
        Long fileSize = 501L * 1024 * 1024; // 501MB

        // When
        boolean result = aiModelManager.validateFileSize(fileSize);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证文件大小 - 空值")
    void validateFileSize_Null_ReturnFalse() {
        // When
        boolean result = aiModelManager.validateFileSize(null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证文件大小 - 零字节")
    void validateFileSize_Zero_ReturnFalse() {
        // Given
        Long fileSize = 0L;

        // When
        boolean result = aiModelManager.validateFileSize(fileSize);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证文件大小 - 负数")
    void validateFileSize_Negative_ReturnFalse() {
        // Given
        Long fileSize = -100L;

        // When
        boolean result = aiModelManager.validateFileSize(fileSize);

        // Then
        assertFalse(result);
    }

    // ==================== 版本格式验证测试 ====================

    @Test
    @DisplayName("验证模型版本 - 标准格式")
    void validateModelVersion_StandardFormat_ReturnTrue() {
        // Given
        String version = "1.0.0";

        // When
        boolean result = aiModelManager.validateModelVersion(version);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证模型版本 - 多位版本号")
    void validateModelVersion_MultiDigit_ReturnTrue() {
        // Given
        String version = "10.20.30";

        // When
        boolean result = aiModelManager.validateModelVersion(version);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证模型版本 - 空值")
    void validateModelVersion_Null_ReturnFalse() {
        // When
        boolean result = aiModelManager.validateModelVersion(null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证模型版本 - 空字符串")
    void validateModelVersion_Empty_ReturnFalse() {
        // Given
        String version = "";

        // When
        boolean result = aiModelManager.validateModelVersion(version);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证模型版本 - 缺少次版本号")
    void validateModelVersion_MissingMinor_ReturnFalse() {
        // Given
        String version = "1.0";

        // When
        boolean result = aiModelManager.validateModelVersion(version);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证模型版本 - 包含字母")
    void validateModelVersion_WithLetters_ReturnFalse() {
        // Given
        String version = "1.0.0a";

        // When
        boolean result = aiModelManager.validateModelVersion(version);

        // Then
        assertFalse(result);
    }

    // ==================== 状态转换验证测试 ====================

    @Test
    @DisplayName("验证状态转换 - 草稿到已发布")
    void validateStatusTransition_DraftToPublished_ReturnTrue() {
        // When
        boolean result = aiModelManager.validateStatusTransition(0, 1);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证状态转换 - 草稿到已弃用")
    void validateStatusTransition_DraftToDeprecated_ReturnTrue() {
        // When
        boolean result = aiModelManager.validateStatusTransition(0, 2);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证状态转换 - 已发布到已弃用")
    void validateStatusTransition_PublishedToDeprecated_ReturnTrue() {
        // When
        boolean result = aiModelManager.validateStatusTransition(1, 2);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证状态转换 - 已发布到草稿")
    void validateStatusTransition_PublishedToDraft_ReturnFalse() {
        // When
        boolean result = aiModelManager.validateStatusTransition(1, 0);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证状态转换 - 已弃用到草稿")
    void validateStatusTransition_DeprecatedToDraft_ReturnFalse() {
        // When
        boolean result = aiModelManager.validateStatusTransition(2, 0);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证状态转换 - 无效状态")
    void validateStatusTransition_InvalidStatus_ReturnFalse() {
        // When
        boolean result = aiModelManager.validateStatusTransition(99, 1);

        // Then
        assertFalse(result);
    }

    // ==================== 状态名称获取测试 ====================

    @Test
    @DisplayName("获取状态名称 - 草稿")
    void getStatusName_Draft_ReturnCorrectName() {
        // When
        String name = aiModelManager.getStatusName(0);

        // Then
        assertEquals("草稿", name);
    }

    @Test
    @DisplayName("获取状态名称 - 已发布")
    void getStatusName_Published_ReturnCorrectName() {
        // When
        String name = aiModelManager.getStatusName(1);

        // Then
        assertEquals("已发布", name);
    }

    @Test
    @DisplayName("获取状态名称 - 已弃用")
    void getStatusName_Deprecated_ReturnCorrectName() {
        // When
        String name = aiModelManager.getStatusName(2);

        // Then
        assertEquals("已弃用", name);
    }

    @Test
    @DisplayName("获取状态名称 - 未知状态")
    void getStatusName_Unknown_ReturnUnknown() {
        // When
        String name = aiModelManager.getStatusName(99);

        // Then
        assertEquals("未知", name);
    }

    @Test
    @DisplayName("获取状态名称 - 空值")
    void getStatusName_Null_ReturnUnknown() {
        // When
        String name = aiModelManager.getStatusName(null);

        // Then
        assertEquals("未知", name);
    }

    // ==================== 文件大小格式化测试 ====================

    @Test
    @DisplayName("格式化文件大小 - 字节转MB")
    void formatFileSizeMb_BytesToMb_ReturnFormattedString() {
        // Given
        Long fileSize = 10L * 1024 * 1024; // 10MB

        // When
        String result = aiModelManager.formatFileSizeMb(fileSize);

        // Then
        assertEquals("10.00", result);
    }

    @Test
    @DisplayName("格式化文件大小 - 小数")
    void formatFileSizeMb_Decimal_ReturnFormattedString() {
        // Given
        Long fileSize = 1536L * 1024; // 1.5MB

        // When
        String result = aiModelManager.formatFileSizeMb(fileSize);

        // Then
        assertEquals("1.50", result);
    }

    @Test
    @DisplayName("格式化文件大小 - 空值")
    void formatFileSizeMb_Null_ReturnNA() {
        // When
        String result = aiModelManager.formatFileSizeMb(null);

        // Then
        assertEquals("N/A", result);
    }

    // ==================== 准确率计算测试 ====================

    @Test
    @DisplayName("计算准确率 - 标准值")
    void calculateAccuracyPercent_StandardValue_ReturnPercent() {
        // Given
        BigDecimal accuracyRate = new BigDecimal("0.95");

        // When
        String result = aiModelManager.calculateAccuracyPercent(accuracyRate);

        // Then
        assertEquals("95%", result);
    }

    @Test
    @DisplayName("计算准确率 - 四舍五入")
    void calculateAccuracyPercent_Rounding_ReturnPercent() {
        // Given
        BigDecimal accuracyRate = new BigDecimal("0.945");

        // When
        String result = aiModelManager.calculateAccuracyPercent(accuracyRate);

        // Then
        assertEquals("95%", result);
    }

    @Test
    @DisplayName("计算准确率 - 空值")
    void calculateAccuracyPercent_Null_ReturnNA() {
        // When
        String result = aiModelManager.calculateAccuracyPercent(null);

        // Then
        assertEquals("N/A", result);
    }

    // ==================== 模型路径生成测试 ====================

    @Test
    @DisplayName("生成模型路径 - 标准路径")
    void generateModelPath_StandardInput_ReturnCorrectPath() {
        // Given
        String modelType = "YOLOv8";
        String modelName = "fall_detection";
        String modelVersion = "2.0.0";

        // When
        String path = aiModelManager.generateModelPath(modelType, modelName, modelVersion);

        // Then
        assertEquals("ai-models/YOLOv8/fall_detection/2.0.0/model.onnx", path);
    }

    @Test
    @DisplayName("生成模型路径 - 不同模型类型")
    void generateModelPath_DifferentModelType_ReturnCorrectPath() {
        // Given
        String modelType = "RESNET";
        String modelName = "face_recognition";
        String modelVersion = "1.2.3";

        // When
        String path = aiModelManager.generateModelPath(modelType, modelName, modelVersion);

        // Then
        assertEquals("ai-models/RESNET/face_recognition/1.2.3/model.onnx", path);
    }

    // ==================== 相对路径提取测试 ====================

    @Test
    @DisplayName("提取相对路径 - 标准路径")
    void extractRelativePath_StandardPath_ReturnRelativePath() {
        // Given
        String fullPath = "/ai-models/YOLOv8/fall_detection/2.0.0/model.onnx";

        // When
        String relativePath = aiModelManager.extractRelativePath(fullPath);

        // Then
        assertEquals("ai-models/YOLOv8/fall_detection/2.0.0/model.onnx", relativePath);
    }

    @Test
    @DisplayName("提取相对路径 - 无前导斜杠")
    void extractRelativePath_NoLeadingSlash_ReturnSamePath() {
        // Given
        String fullPath = "ai-models/YOLOv8/fall_detection/2.0.0/model.onnx";

        // When
        String relativePath = aiModelManager.extractRelativePath(fullPath);

        // Then
        assertEquals("ai-models/YOLOv8/fall_detection/2.0.0/model.onnx", relativePath);
    }

    @Test
    @DisplayName("提取相对路径 - 空值")
    void extractRelativePath_Null_ReturnEmpty() {
        // When
        String relativePath = aiModelManager.extractRelativePath(null);

        // Then
        assertEquals("", relativePath);
    }

    @Test
    @DisplayName("提取相对路径 - 空字符串")
    void extractRelativePath_Empty_ReturnEmpty() {
        // Given
        String fullPath = "";

        // When
        String relativePath = aiModelManager.extractRelativePath(fullPath);

        // Then
        assertEquals("", relativePath);
    }
}
