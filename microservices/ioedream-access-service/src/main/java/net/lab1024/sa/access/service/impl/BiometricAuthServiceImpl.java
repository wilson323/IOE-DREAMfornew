package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.BiometricTemplateDao;
import net.lab1024.sa.access.service.BiometricAuthService;
import net.lab1024.sa.access.manager.BiometricTemplateManager;
import net.lab1024.sa.access.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.access.domain.form.BiometricRegisterForm;
import net.lab1024.sa.access.domain.form.BiometricAuthForm;
import net.lab1024.sa.access.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.access.domain.vo.BiometricAuthResultVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.core.exception.BusinessException;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartBase64Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 鐢熺墿璇嗗埆璁よ瘉鏈嶅姟瀹炵幇
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BiometricAuthServiceImpl implements BiometricAuthService {

    @Resource
    private BiometricTemplateManager biometricTemplateManager;

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Override
    public ResponseDTO<BiometricTemplateVO> registerTemplate(BiometricRegisterForm registerForm) {
        try {
            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 娉ㄥ唽妯℃澘 userId={}, biometricType={}",
                    registerForm.getUserId(), registerForm.getBiometricType());

            // 1. 楠岃瘉琛ㄥ崟鏁版嵁
            validateRegisterForm(registerForm);

            // 2. 瑙ｇ爜鐗瑰緛鏁版嵁
            byte[] featureData = SmartBase64Util.decodeToBytes(registerForm.getFeatureData());

            // 3. 璋冪敤Manager娉ㄥ唽妯℃澘
            BiometricTemplateEntity template = biometricTemplateManager.registerTemplate(
                    registerForm.getUserId(),
                    registerForm.getBiometricType(),
                    featureData,
                    registerForm.getDeviceId()
            );

            // 4. 杞崲涓篤O
            BiometricTemplateVO templateVO = convertToTemplateVO(template);

            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 妯℃澘娉ㄥ唽鎴愬姛 templateId={}", template.getTemplateId());
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆鏈嶅姟] 妯℃澘娉ㄥ唽澶辫触 userId={}, biometricType={}",
                    registerForm.getUserId(), registerForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_REGISTER_ERROR", "妯℃澘娉ㄥ唽澶辫触: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<BiometricAuthResultVO> authenticate(BiometricAuthForm authForm) {
        try {
            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 1:1楠岃瘉 userId={}, biometricType={}",
                    authForm.getUserId(), authForm.getBiometricType());

            // 1. 楠岃瘉琛ㄥ崟鏁版嵁
            validateAuthForm(authForm);

            // 2. 瑙ｇ爜鐗瑰緛鏁版嵁
            byte[] featureData = SmartBase64Util.decodeToBytes(authForm.getFeatureData());

            // 3. 璋冪敤Manager杩涜楠岃瘉
            BiometricAuthResult authResult = biometricTemplateManager.authenticate(
                    authForm.getUserId(),
                    authForm.getBiometricType(),
                    featureData,
                    authForm.getDeviceId()
            );

            // 4. 杞崲涓篤O
            BiometricAuthResultVO resultVO = convertToAuthResultVO(authResult);

            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 1:1楠岃瘉瀹屾垚 userId={}, success={}, matchScore={}",
                    authForm.getUserId(), authResult.isSuccess(), authResult.getMatchScore());
            return ResponseDTO.ok(resultVO);

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆鏈嶅姟] 1:1楠岃瘉澶辫触 userId={}, biometricType={}",
                    authForm.getUserId(), authForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_AUTH_ERROR", "鐢熺墿璇嗗埆楠岃瘉澶辫触: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<BiometricAuthResultVO>> identify(BiometricAuthForm authForm) {
        try {
            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 1:N璇嗗埆 biometricType={}", authForm.getBiometricType());

            // 1. 楠岃瘉琛ㄥ崟鏁版嵁
            validateIdentifyForm(authForm);

            // 2. 瑙ｇ爜鐗瑰緛鏁版嵁
            byte[] featureData = SmartBase64Util.decodeToBytes(authForm.getFeatureData());

            // 3. 璋冪敤Manager杩涜璇嗗埆
            List<BiometricTemplateManager.BiometricAuthResult> authResults = biometricTemplateManager.identify(
                    authForm.getBiometricType(),
                    featureData,
                    authForm.getDeviceId(),
                    authForm.getLimit()
            );

            // 4. 杞崲涓篤O鍒楄〃
            List<BiometricAuthResultVO> resultVOs = authResults.stream()
                    .map(this::convertToAuthResultVO)
                    .collect(Collectors.toList());

            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 1:N璇嗗埆瀹屾垚 biometricType={}, resultCount={}",
                    authForm.getBiometricType(), resultVOs.size());
            return ResponseDTO.ok(resultVOs);

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆鏈嶅姟] 1:N璇嗗埆澶辫触 biometricType={}", authForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_IDENTIFY_ERROR", "鐢熺墿璇嗗埆澶辫触: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<BiometricTemplateVO>> getUserTemplates(Long userId) {
        try {
            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 鏌ヨ鐢ㄦ埛妯℃澘 userId={}", userId);

            // 1. 鏌ヨ鐢ㄦ埛鎵€鏈夋ā锟?
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserId(userId);

            // 2. 杞崲涓篤O鍒楄〃
            List<BiometricTemplateVO> templateVOs = templates.stream()
                    .map(this::convertToTemplateVO)
                    .collect(Collectors.toList());

            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 鐢ㄦ埛妯℃澘鏌ヨ瀹屾垚 userId={}, templateCount={}",
                    userId, templateVOs.size());
            return ResponseDTO.ok(templateVOs);

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆鏈嶅姟] 鐢ㄦ埛妯℃澘鏌ヨ澶辫触 userId={}", userId, e);
            throw new BusinessException("BIOMETRIC_QUERY_ERROR", "鏌ヨ鐢ㄦ埛妯℃澘澶辫触: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateTemplateStatus(Long templateId, Integer status) {
        try {
            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 鏇存柊妯℃澘鐘讹拷?templateId={}, status={}", templateId, status);

            // 1. 楠岃瘉妯℃澘ID
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                throw new BusinessException("TEMPLATE_NOT_FOUND", "妯℃澘涓嶅瓨锟?);
            }

            // 2. 璋冪敤Manager鏇存柊鐘讹拷?
            biometricTemplateManager.updateTemplateStatus(templateId, status);

            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 妯℃澘鐘舵€佹洿鏂版垚锟?templateId={}", templateId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆鏈嶅姟] 鏇存柊妯℃澘鐘舵€佸け锟?templateId={}, status={}", templateId, status, e);
            throw new BusinessException("TEMPLATE_UPDATE_ERROR", "鏇存柊妯℃澘鐘舵€佸け锟? " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteUserTemplate(Long userId, Integer biometricType) {
        try {
            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 鍒犻櫎鐢ㄦ埛妯℃澘 userId={}, biometricType={}", userId, biometricType);

            // 1. 璋冪敤Manager鍒犻櫎妯℃澘
            biometricTemplateManager.deleteUserTemplates(userId, biometricType);

            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 鐢ㄦ埛妯℃澘鍒犻櫎鎴愬姛 userId={}, biometricType={}", userId, biometricType);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆鏈嶅姟] 鍒犻櫎鐢ㄦ埛妯℃澘澶辫触 userId={}, biometricType={}", userId, biometricType, e);
            throw new BusinessException("TEMPLATE_DELETE_ERROR", "鍒犻櫎鐢ㄦ埛妯℃澘澶辫触: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<BiometricTemplateVO> getUserBiometricStats(Long userId) {
        try {
            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 鏌ヨ鐢ㄦ埛鐢熺墿璇嗗埆缁熻 userId={}", userId);

            // 1. 鏌ヨ鐢ㄦ埛鎵€鏈夋縺娲绘ā锟?
            List<BiometricTemplateEntity> templates = biometricTemplateDao
                    .selectByUserId(userId)
                    .stream()
                    .filter(t -> t.getTemplateStatus().equals(BiometricTemplateEntity.TemplateStatus.ACTIVE.getCode()))
                    .collect(Collectors.toList());

            // 2. 璁＄畻缁熻淇℃伅
            int totalTemplates = templates.size();
            int totalUseCount = templates.stream().mapToInt(t -> t.getUseCount() != null ? t.getUseCount() : 0).sum();
            int totalSuccessCount = templates.stream().mapToInt(t -> t.getSuccessCount() != null ? t.getSuccessCount() : 0).sum();
            int totalFailCount = templates.stream().mapToInt(t -> t.getFailCount() != null ? t.getFailCount() : 0).sum();

            double successRate = totalUseCount > 0 ? (double) totalSuccessCount / totalUseCount * 100 : 0;

            // 3. 鏋勫缓缁熻VO
            BiometricTemplateVO statsVO = new BiometricTemplateVO();
            statsVO.setUserId(userId);
            statsVO.setTotalTemplates(totalTemplates);
            statsVO.setTotalUseCount(totalUseCount);
            statsVO.setTotalSuccessCount(totalSuccessCount);
            statsVO.setTotalFailCount(totalFailCount);
            statsVO.setSuccessRate(String.format("%.2f%%", successRate));

            log.info("[鐢熺墿璇嗗埆鏈嶅姟] 鐢ㄦ埛鐢熺墿璇嗗埆缁熻鏌ヨ瀹屾垚 userId={}, templateCount={}, successRate={}",
                    userId, totalTemplates, successRate);
            return ResponseDTO.ok(statsVO);

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆鏈嶅姟] 鏌ヨ鐢ㄦ埛鐢熺墿璇嗗埆缁熻澶辫触 userId={}", userId, e);
            throw new BusinessException("BIOMETRIC_STATS_ERROR", "鏌ヨ鐢熺墿璇嗗埆缁熻澶辫触: " + e.getMessage());
        }
    }

    // ========== 绉佹湁鏂规硶 ==========

    /**
     * 楠岃瘉娉ㄥ唽琛ㄥ崟
     */
    private void validateRegisterForm(BiometricRegisterForm form) {
        if (form.getUserId() == null) {
            throw new BusinessException("PARAM_ERROR", "鐢ㄦ埛ID涓嶈兘涓虹┖");
        }
        if (form.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "鐢熺墿璇嗗埆绫诲瀷涓嶈兘涓虹┖");
        }
        if (form.getFeatureData() == null || form.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "鐗瑰緛鏁版嵁涓嶈兘涓虹┖");
        }
        if (form.getDeviceId() == null || form.getDeviceId().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "璁惧ID涓嶈兘涓虹┖");
        }
    }

    /**
     * 楠岃瘉璁よ瘉琛ㄥ崟
     */
    private void validateAuthForm(BiometricAuthForm form) {
        if (form.getUserId() == null) {
            throw new BusinessException("PARAM_ERROR", "鐢ㄦ埛ID涓嶈兘涓虹┖");
        }
        if (form.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "鐢熺墿璇嗗埆绫诲瀷涓嶈兘涓虹┖");
        }
        if (form.getFeatureData() == null || form.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "鐗瑰緛鏁版嵁涓嶈兘涓虹┖");
        }
        if (form.getDeviceId() == null || form.getDeviceId().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "璁惧ID涓嶈兘涓虹┖");
        }
    }

    /**
     * 楠岃瘉璇嗗埆琛ㄥ崟
     */
    private void validateIdentifyForm(BiometricAuthForm form) {
        if (form.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "鐢熺墿璇嗗埆绫诲瀷涓嶈兘涓虹┖");
        }
        if (form.getFeatureData() == null || form.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "鐗瑰緛鏁版嵁涓嶈兘涓虹┖");
        }
        if (form.getDeviceId() == null || form.getDeviceId().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "璁惧ID涓嶈兘涓虹┖");
        }
        // limit鏄彲閫夌殑锛屽鏋滀笉璁剧疆鍒欎娇鐢ㄩ粯璁わ拷?
        if (form.getLimit() == null) {
            form.setLimit(10);
        }
    }

    /**
     * 杞崲妯℃澘瀹炰綋涓篤O
     */
    private BiometricTemplateVO convertToTemplateVO(BiometricTemplateEntity entity) {
        BiometricTemplateVO vo = SmartBeanUtil.copy(entity, BiometricTemplateVO.class);

        // 璁剧疆鏋氫妇鎻忚堪
        if (entity.getBiometricType() != null) {
            BiometricTemplateEntity.BiometricType type = BiometricTemplateEntity.BiometricType.fromCode(entity.getBiometricType());
            vo.setBiometricTypeDesc(type.getDescription());
        }

        if (entity.getTemplateStatus() != null) {
            BiometricTemplateEntity.TemplateStatus status = BiometricTemplateEntity.TemplateStatus.fromCode(entity.getTemplateStatus());
            vo.setTemplateStatusDesc(status.getDescription());
        }

        // 璁＄畻鎴愬姛锟?
        if (entity.getUseCount() != null && entity.getUseCount() > 0) {
            int successCount = entity.getSuccessCount() != null ? entity.getSuccessCount() : 0;
            double successRate = (double) successCount / entity.getUseCount() * 100;
            vo.setSuccessRate(String.format("%.2f%%", successRate));
        } else {
            vo.setSuccessRate("0.00%");
        }

        // 涓嶈繑鍥炴晱鎰熺殑鐗瑰緛鏁版嵁
        vo.setFeatureData(null);
        vo.setFeatureVector(null);

        return vo;
    }

    /**
     * 杞崲璁よ瘉缁撴灉涓篤O
     */
    private BiometricAuthResultVO convertToAuthResultVO(BiometricTemplateManager.BiometricAuthResult authResult) {
        BiometricAuthResultVO vo = new BiometricAuthResultVO();
        vo.setSuccess(authResult.isSuccess());
        vo.setMatchScore(authResult.getMatchScore());
        vo.setLivenessPassed(authResult.getLivenessResult().isPassed());
        vo.setLivenessScore(authResult.getLivenessResult().getScore());
        vo.setMessage(authResult.getMessage());
        vo.setDuration(authResult.getDuration());

        if (authResult.getMatchedTemplate() != null) {
            vo.setTemplateId(authResult.getMatchedTemplate().getTemplateId());
            vo.setUserId(authResult.getMatchedTemplate().getUserId());
            vo.setTemplateName(authResult.getMatchedTemplate().getTemplateName());

            if (authResult.getMatchedTemplate().getBiometricType() != null) {
                BiometricTemplateEntity.BiometricType type = BiometricTemplateEntity.BiometricType.fromCode(
                        authResult.getMatchedTemplate().getBiometricType());
                vo.setBiometricTypeDesc(type.getDescription());
            }
        }

        return vo;
    }
}
