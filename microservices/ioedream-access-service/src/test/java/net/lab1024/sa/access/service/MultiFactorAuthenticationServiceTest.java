package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.MultiFactorAuthenticationForm;
import net.lab1024.sa.access.domain.vo.MultiFactorAuthenticationResultVO;
import net.lab1024.sa.access.service.impl.MultiFactorAuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 多因子认证服务测试类
 * <p>
 * 测试场景：
 * 1. 严格模式：所有必需因子通过
 * 2. 宽松模式：至少一个因子通过
 * 3. 优先模式：按优先级验证
 * 4. 失败场景：认证失败处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@DisplayName("多因子认证服务测试")
class MultiFactorAuthenticationServiceTest {

    private MultiFactorAuthenticationService multiFactorAuthenticationService;

    @BeforeEach
    void setUp() {
        multiFactorAuthenticationService = new MultiFactorAuthenticationServiceImpl();
    }

    @Test
    @DisplayName("严格模式：人脸+指纹双重认证成功")
    void testStrictMode_FaceAndFingerprint_Success() {
        // Given
        MultiFactorAuthenticationForm form = createMultiFactorForm(
                "STRICT",
                List.of(
                        createFactor("FACE", "base64_face_image_data", 1, true),
                        createFactor("FINGERPRINT", "fingerprint_data", 2, true)
                )
        );

        // When
        MultiFactorAuthenticationResultVO result = multiFactorAuthenticationService.authenticate(form);

        // Then
        assertNotNull(result);
        assertTrue(result.getAuthenticated());
        assertEquals("STRICT", result.getAuthenticationMode());
        assertNotNull(result.getFactorResults());
        assertEquals(2, result.getFactorResults().size());
        assertTrue(result.getFactorResults().get(0).getPassed());
        assertTrue(result.getFactorResults().get(1).getPassed());
        assertNotNull(result.getDuration());
    }

    @Test
    @DisplayName("宽松模式：至少一个因子通过")
    void testRelaxedMode_AtLeastOneFactor_Success() {
        // Given
        MultiFactorAuthenticationForm form = createMultiFactorForm(
                "RELAXED",
                List.of(
                        createFactor("FACE", "base64_face_image_data", 1, true),
                        createFactor("CARD", "card_data", 2, false)
                )
        );

        // When
        MultiFactorAuthenticationResultVO result = multiFactorAuthenticationService.authenticate(form);

        // Then
        assertNotNull(result);
        assertTrue(result.getAuthenticated());
        assertEquals("RELAXED", result.getAuthenticationMode());
        assertTrue(result.getScore() > 0);
    }

    @Test
    @DisplayName("优先模式：高优先级因子通过")
    void testPriorityMode_HighPriorityFactor_Success() {
        // Given
        MultiFactorAuthenticationForm form = createMultiFactorForm(
                "PRIORITY",
                List.of(
                        createFactor("FACE", "base64_face_image_data", 1, true),
                        createFactor("FINGERPRINT", "fingerprint_data", 2, false)
                )
        );

        // When
        MultiFactorAuthenticationResultVO result = multiFactorAuthenticationService.authenticate(form);

        // Then
        assertNotNull(result);
        assertTrue(result.getAuthenticated());
        assertEquals("PRIORITY", result.getAuthenticationMode());
    }

    @Test
    @DisplayName("失败场景：不支持的认证模式")
    void testInvalidAuthenticationMode_Failure() {
        // Given
        MultiFactorAuthenticationForm form = createMultiFactorForm(
                "INVALID_MODE",
                List.of(createFactor("FACE", "data", 1, true))
        );

        // When
        MultiFactorAuthenticationResultVO result = multiFactorAuthenticationService.authenticate(form);

        // Then
        assertNotNull(result);
        assertFalse(result.getAuthenticated());
        assertNotNull(result.getFailureReason());
        assertTrue(result.getFailureReason().contains("不支持的认证模式"));
    }

    @Test
    @DisplayName("人脸验证单独测试")
    void testVerifyFace_Success() {
        // Given
        Long userId = 1L;
        String faceImageData = "base64_encoded_face_image";

        // When
        boolean result = multiFactorAuthenticationService.verifyFace(userId, faceImageData);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("指纹验证单独测试")
    void testVerifyFingerprint_Success() {
        // Given
        Long userId = 1L;
        byte[] fingerprintData = "fingerprint_template_data".getBytes();

        // When
        boolean result = multiFactorAuthenticationService.verifyFingerprint(userId, fingerprintData);

        // Then
        assertTrue(result);
    }

    /**
     * 创建多因子认证表单
     */
    private MultiFactorAuthenticationForm createMultiFactorForm(
            String mode,
            List<MultiFactorAuthenticationForm.AuthenticationFactor> factors) {
        MultiFactorAuthenticationForm form = new MultiFactorAuthenticationForm();
        form.setUserId(1L);
        form.setDeviceId("DEV001");
        form.setAuthenticationMode(mode);
        form.setFactors(factors);
        return form;
    }

    /**
     * 创建认证因子
     */
    private MultiFactorAuthenticationForm.AuthenticationFactor createFactor(
            String type,
            String data,
            Integer priority,
            Boolean required) {
        MultiFactorAuthenticationForm.AuthenticationFactor factor =
                new MultiFactorAuthenticationForm.AuthenticationFactor();
        factor.setType(type);
        factor.setData(data);
        factor.setPriority(priority);
        factor.setRequired(required);
        return factor;
    }
}
