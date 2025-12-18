package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.BiometricRegisterForm;
import net.lab1024.sa.access.domain.form.BiometricAuthForm;
import net.lab1024.sa.access.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.access.domain.vo.BiometricAuthResultVO;
import net.lab1024.sa.access.manager.BiometricTemplateManager;
import net.lab1024.sa.access.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.access.manager.BiometricTemplateManager.BiometricAuthResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 鐢熺墿璇嗗埆璁よ瘉鏈嶅姟鎺ュ彛
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface BiometricAuthService {

    /**
     * 娉ㄥ唽鐢熺墿璇嗗埆妯℃澘
     *
     * @param registerForm 娉ㄥ唽琛ㄥ崟
     * @return 娉ㄥ唽缁撴灉
     */
    ResponseDTO<BiometricTemplateVO> registerTemplate(BiometricRegisterForm registerForm);

    /**
     * 鐢熺墿璇嗗埆楠岃瘉锛?:1锛?
     *
     * @param authForm 楠岃瘉琛ㄥ崟
     * @return 楠岃瘉缁撴灉
     */
    ResponseDTO<BiometricAuthResultVO> authenticate(BiometricAuthForm authForm);

    /**
     * 鐢熺墿璇嗗埆璇嗗埆锛?:N锛?
     *
     * @param authForm 楠岃瘉琛ㄥ崟
     * @return 璇嗗埆缁撴灉鍒楄〃
     */
    ResponseDTO<List<BiometricAuthResultVO>> identify(BiometricAuthForm authForm);

    /**
     * 鑾峰彇鐢ㄦ埛鐢熺墿璇嗗埆妯℃澘鍒楄〃
     *
     * @param userId 鐢ㄦ埛ID
     * @return 妯℃澘鍒楄〃
     */
    ResponseDTO<List<BiometricTemplateVO>> getUserTemplates(Long userId);

    /**
     * 鏇存柊妯℃澘鐘舵€?
     *
     * @param templateId 妯℃澘ID
     * @param status 鐘舵€?
     * @return 鎿嶄綔缁撴灉
     */
    ResponseDTO<Void> updateTemplateStatus(Long templateId, Integer status);

    /**
     * 鍒犻櫎鐢ㄦ埛妯℃澘
     *
     * @param userId 鐢ㄦ埛ID
     * @param biometricType 鐢熺墿璇嗗埆绫诲瀷
     * @return 鎿嶄綔缁撴灉
     */
    ResponseDTO<Void> deleteUserTemplate(Long userId, Integer biometricType);

    /**
     * 鑾峰彇鐢ㄦ埛鐢熺墿璇嗗埆缁熻淇℃伅
     *
     * @param userId 鐢ㄦ埛ID
     * @return 缁熻淇℃伅
     */
    ResponseDTO<BiometricTemplateVO> getUserBiometricStats(Long userId);
}