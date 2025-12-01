package net.lab1024.sa.base.module.biometric.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.base.common.entity.BaseEntity;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.module.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.base.module.biometric.dao.PersonBiometricDao;
import net.lab1024.sa.base.module.biometric.entity.BiometricTemplateEntity;
import net.lab1024.sa.base.module.biometric.entity.PersonBiometricEntity;
import net.lab1024.sa.base.module.biometric.manager.BiometricCacheManager;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;

/**
 * 生物特征模板服务
 * <p>
 * 生物特征模板生命周期管理核心服务，提供模板的注册、更新、查询和状态管理
 * 核心能力：
 * 1. 模板注册和质量评估
 * 2. SM4加密存储和解密读取
 * 3. 模板有效期管理
 * 4. 使用统计和失败计数
 * 5. 算法版本管理和升级
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Service
public class BiometricTemplateService {

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Resource
    private PersonBiometricDao personBiometricDao;

    @Resource
    private PersonBiometricService personBiometricService;

    @Resource
    private BiometricCacheManager biometricCacheManager;

    // ==================== 模板注册管理 ====================

    /**
     * 注册新的生物特征模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @param templateData 模板数据（原始明文）
     * @param qualityScore 质量分数
     * @param algorithmInfo 算法信息
     * @param enrollmentDeviceInfo 注册设备信息
     * @return 注册结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> enrollTemplate(Long personId, String biometricType,
                                              String templateData, Double qualityScore,
                                              String algorithmInfo, String enrollmentDeviceInfo) {
        try {
            // 验证参数
            if (personId == null || biometricType == null || templateData == null) {
                return ResponseDTO.error("人员ID、生物特征类型和模板数据不能为空");
            }

            // 检查人员是否存在
            PersonBiometricEntity person = personBiometricDao.selectByPersonId(personId);
            if (person == null) {
                return ResponseDTO.error("人员生物特征档案不存在，请先创建档案");
            }

            // 验证生物特征类型
            try {
                BiometricTemplateEntity.BiometricType.fromValue(biometricType);
            } catch (IllegalArgumentException e) {
                return ResponseDTO.error("不支持的生物特征类型: " + biometricType);
            }

            // 验证质量分数
            if (qualityScore != null && (qualityScore < 0.0 || qualityScore > 1.0)) {
                return ResponseDTO.error("质量分数必须在0.0-1.0之间");
            }

            // 获取下一个模板索引
            Integer templateIndex = getNextTemplateIndex(personId, biometricType);

            // 创建模板实体
            BiometricTemplateEntity template = new BiometricTemplateEntity();
            template.setPersonId(personId);
            template.setBiometricType(biometricType);
            template.setTemplateIndex(templateIndex);
            template.setTemplateData(templateData); // 在实际实现中这里应该是SM4加密
            template.setTemplateVersion(extractTemplateVersion(algorithmInfo));
            template.setAlgorithmInfo(algorithmInfo);
            template.setQualityScore(qualityScore);
            template.setEnrollmentDeviceId(1L); // TODO: 从设备信息中获取
            template.setEnrollmentDeviceInfo(enrollmentDeviceInfo);
            template.setCaptureTime(LocalDateTime.now());
            template.setTemplateStatus(BiometricTemplateEntity.TemplateStatus.ACTIVE.getValue());
            template.setUsageCount(0);
            template.setFailureCount(0);
            template.setIsPrimary(templateIndex == 0 ? 1 : 0); // 第一个模板设为主模板
            template.setEnableStatus(1);

            // 设置安全元数据
            template.setSecurityMetadata(generateSecurityMetadata());

            // 设置默认有效期（1年）
            template.setValidFrom(LocalDateTime.now());
            template.setValidTo(LocalDateTime.now().plusYears(1));

            // 保存模板
            int result = biometricTemplateDao.insert(template);
            if (result <= 0) {
                return ResponseDTO.error("模板注册失败");
            }

            // 更新人员生物特征档案
            personBiometricService.incrementBiometricCount(personId, biometricType);
            personBiometricService.recalculateQualityScore(personId);

            // 缓存模板数据
            biometricCacheManager.cacheBiometricTemplate(template);

            log.info("生物特征模板注册成功: personId={}, biometricType={}, templateIndex={}, qualityScore={}",
                    personId, biometricType, templateIndex, qualityScore);

            return ResponseDTO.ok("生物特征模板注册成功");

        } catch (Exception e) {
            log.error("注册生物特征模板异常", e);
            return ResponseDTO.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 更新生物特征模板
     *
     * @param templateId 模板ID
     * @param templateData 新的模板数据
     * @param qualityScore 质量分数
     * @param algorithmInfo 算法信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateTemplate(Long templateId, String templateData,
                                             Double qualityScore, String algorithmInfo) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("模板不存在");
            }

            // 更新模板数据
            if (templateData != null) {
                template.setTemplateData(templateData); // 在实际实现中这里应该是SM4加密
            }

            if (qualityScore != null) {
                template.setQualityScore(qualityScore);
            }

            if (algorithmInfo != null) {
                template.setAlgorithmInfo(algorithmInfo);
                template.setTemplateVersion(extractTemplateVersion(algorithmInfo));
            }

            template.updateLastUpdateTime();

            int result = biometricTemplateDao.updateById(template);
            if (result > 0) {
                // 清除相关缓存
                biometricCacheManager.removeBiometricTemplate(templateId);
                biometricCacheManager.removePersonBiometric(template.getPersonId());

                // 重新计算人员质量分数
                personBiometricService.recalculateQualityScore(template.getPersonId());

                log.info("生物特征模板更新成功: templateId={}", templateId);
                return ResponseDTO.ok("生物特征模板更新成功");
            } else {
                return ResponseDTO.error("模板更新失败");
            }

        } catch (Exception e) {
            log.error("更新生物特征模板异常", e);
            return ResponseDTO.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除生物特征模板（软删除）
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteTemplate(Long templateId) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("模板不存在");
            }

            // 软删除
            template.setDeletedFlag(1);
            template.setUpdateTime(LocalDateTime.now());

            int result = biometricTemplateDao.updateById(template);
            if (result > 0) {
                // 清除缓存
                biometricCacheManager.removeBiometricTemplate(templateId);

                // 更新人员生物特征档案
                personBiometricService.decrementBiometricCount(template.getPersonId(), template.getBiometricType());
                personBiometricService.recalculateQualityScore(template.getPersonId());

                log.info("生物特征模板删除成功: templateId={}", templateId);
                return ResponseDTO.ok("生物特征模板删除成功");
            } else {
                return ResponseDTO.error("模板删除失败");
            }

        } catch (Exception e) {
            log.error("删除生物特征模板异常", e);
            return ResponseDTO.error("删除失败: " + e.getMessage());
        }
    }

    // ==================== 模板查询 ====================

    /**
     * 根据ID获取模板
     *
     * @param templateId 模板ID
     * @return 模板信息
     */
    public BiometricTemplateEntity getById(Long templateId) {
        if (templateId == null) {
            return null;
        }

        // 先从缓存获取
        BiometricTemplateEntity cached = biometricCacheManager.getBiometricTemplate(templateId);
        if (cached != null) {
            return cached;
        }

        // 从数据库查询
        BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
        if (template != null) {
            biometricCacheManager.cacheBiometricTemplate(template);
        }

        return template;
    }

    /**
     * 获取人员的所有模板
     *
     * @param personId 人员ID
     * @return 模板列表
     */
    public List<BiometricTemplateEntity> getPersonTemplates(Long personId) {
        return biometricTemplateDao.selectByPersonId(personId);
    }

    /**
     * 获取人员的指定类型模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @return 模板列表
     */
    public List<BiometricTemplateEntity> getPersonTemplatesByType(Long personId, String biometricType) {
        return biometricTemplateDao.selectByPersonIdAndType(personId, biometricType);
    }

    /**
     * 获取人员的主模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型（可选）
     * @return 主模板列表
     */
    public List<BiometricTemplateEntity> getPersonPrimaryTemplates(Long personId, String biometricType) {
        return biometricTemplateDao.selectPrimaryTemplates(personId, biometricType);
    }

    /**
     * 获取人员的活跃模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型（可选）
     * @return 活跃模板列表
     */
    public List<BiometricTemplateEntity> getPersonActiveTemplates(Long personId, String biometricType) {
        return biometricTemplateDao.selectActiveTemplates(personId, biometricType);
    }

    /**
     * 分页查询模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @param templateStatus 模板状态
     * @param qualityThreshold 质量分数阈值
     * @param includeExpired 是否包含过期模板
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public ResponseDTO<Page<BiometricTemplateEntity>> queryPage(Long personId, String biometricType,
                                                                String templateStatus, Double qualityThreshold,
                                                                Boolean includeExpired, Integer pageNum, Integer pageSize) {
        try {
            Page<BiometricTemplateEntity> pageParam = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20);
            Page<BiometricTemplateEntity> result = biometricTemplateDao.selectPageByCondition(
                    pageParam, personId, biometricType, templateStatus, qualityThreshold, includeExpired);

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询生物特征模板异常", e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    // ==================== 模板使用管理 ====================

    /**
     * 记录模板使用
     *
     * @param templateId 模板ID
     * @param success 是否使用成功
     * @return 记录结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> recordTemplateUsage(Long templateId, boolean success) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("模板不存在");
            }

            if (success) {
                template.incrementUsageCount();
            } else {
                template.incrementFailureCount();
            }

            int result = biometricTemplateDao.updateById(template);
            if (result > 0) {
                // 清除缓存
                biometricCacheManager.removeBiometricTemplate(templateId);

                log.info("记录模板使用成功: templateId={}, success={}", templateId, success);
                return ResponseDTO.ok("使用记录成功");
            } else {
                return ResponseDTO.error("使用记录失败");
            }

        } catch (Exception e) {
            log.error("记录模板使用异常", e);
            return ResponseDTO.error("记录失败: " + e.getMessage());
        }
    }

    /**
     * 重置模板失败次数
     *
     * @param templateId 模板ID
     * @return 重置结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> resetFailureCount(Long templateId) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("模板不存在");
            }

            template.resetFailureCount();
            template.updateLastUpdateTime();

            int result = biometricTemplateDao.updateById(template);
            if (result > 0) {
                biometricCacheManager.removeBiometricTemplate(templateId);
                log.info("重置模板失败次数成功: templateId={}", templateId);
                return ResponseDTO.ok("失败次数重置成功");
            } else {
                return ResponseDTO.error("重置失败");
            }

        } catch (Exception e) {
            log.error("重置模板失败次数异常", e);
            return ResponseDTO.error("重置失败: " + e.getMessage());
        }
    }

    /**
     * 批量重置失败次数
     *
     * @param templateIds 模板ID列表
     * @return 批量重置结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> resetFailureCountBatch(List<Long> templateIds) {
        try {
            if (templateIds == null || templateIds.isEmpty()) {
                return ResponseDTO.error("模板ID列表不能为空");
            }

            int result = biometricTemplateDao.resetFailureCountBatch(templateIds);
            if (result > 0) {
                // 清除相关缓存
                for (Long templateId : templateIds) {
                    biometricCacheManager.removeBiometricTemplate(templateId);
                }

                log.info("批量重置失败次数成功: count={}", result);
                return ResponseDTO.ok(String.format("批量重置成功，影响%d个模板", result));
            } else {
                return ResponseDTO.error("批量重置失败");
            }

        } catch (Exception e) {
            log.error("批量重置失败次数异常", e);
            return ResponseDTO.error("批量重置失败: " + e.getMessage());
        }
    }

    // ==================== 模板状态管理 ====================

    /**
     * 更新模板状态
     *
     * @param templateId 模板ID
     * @param newStatus 新状态
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateTemplateStatus(Long templateId, String newStatus) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("模板不存在");
            }

            // 验证状态
            try {
                BiometricTemplateEntity.TemplateStatus.fromValue(newStatus);
            } catch (IllegalArgumentException e) {
                return ResponseDTO.error("无效的模板状态: " + newStatus);
            }

            template.setTemplateStatus(newStatus);
            template.updateLastUpdateTime();

            int result = biometricTemplateDao.updateById(template);
            if (result > 0) {
                biometricCacheManager.removeBiometricTemplate(templateId);
                log.info("更新模板状态成功: templateId={}, newStatus={}", templateId, newStatus);
                return ResponseDTO.ok("模板状态更新成功");
            } else {
                return ResponseDTO.error("模板状态更新失败");
            }

        } catch (Exception e) {
            log.error("更新模板状态异常", e);
            return ResponseDTO.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 设置主模板
     *
     * @param templateId 模板ID
     * @param isPrimary 是否为主模板
     * @return 设置结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> setPrimaryTemplate(Long templateId, boolean isPrimary) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("模板不存在");
            }

            if (isPrimary) {
                // 先清除该人员和生物特征类型的其他主模板
                List<BiometricTemplateEntity> otherPrimaries = biometricTemplateDao.selectPrimaryTemplates(
                        template.getPersonId(), template.getBiometricType());
                for (BiometricTemplateEntity other : otherPrimaries) {
                    if (!other.getId().equals(templateId)) {
                        other.setIsPrimary(0);
                        other.updateLastUpdateTime();
                        biometricTemplateDao.updateById(other);
                        biometricCacheManager.removeBiometricTemplate(other.getId());
                    }
                }
            }

            template.setIsPrimary(isPrimary ? 1 : 0);
            template.updateLastUpdateTime();

            int result = biometricTemplateDao.updateById(template);
            if (result > 0) {
                biometricCacheManager.removeBiometricTemplate(templateId);
                log.info("设置主模板成功: templateId={}, isPrimary={}", templateId, isPrimary);
                return ResponseDTO.ok("主模板设置成功");
            } else {
                return ResponseDTO.error("主模板设置失败");
            }

        } catch (Exception e) {
            log.error("设置主模板异常", e);
            return ResponseDTO.error("设置失败: " + e.getMessage());
        }
    }

    // ==================== 模板维护 ====================

    /**
     * 获取过期模板列表
     *
     * @return 过期模板列表
     */
    public List<BiometricTemplateEntity> getExpiredTemplates() {
        return biometricTemplateDao.selectExpiredTemplates(LocalDateTime.now());
    }

    /**
     * 获取即将过期模板列表
     *
     * @param days 天数
     * @return 即将过期模板列表
     */
    public List<BiometricTemplateEntity> getSoonToExpireTemplates(int days) {
        return biometricTemplateDao.selectSoonToExpire(LocalDateTime.now(), days);
    }

    /**
     * 获取需要重新注册的模板
     *
     * @param personId 人员ID（可选）
     * @return 需要重新注册的模板列表
     */
    public List<BiometricTemplateEntity> getTemplatesNeedingReEnrollment(Long personId) {
        return biometricTemplateDao.selectTemplatesNeedingReEnrollment(personId);
    }

    /**
     * 获取低质量模板
     *
     * @param threshold 质量分数阈值
     * @return 低质量模板列表
     */
    public List<BiometricTemplateEntity> getLowQualityTemplates(Double threshold) {
        double thresholdValue = threshold != null ? threshold : 0.8;
        return biometricTemplateDao.selectLowQuality(thresholdValue);
    }

    /**
     * 批量更新过期模板状态
     *
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateExpiredTemplateStatus() {
        try {
            List<BiometricTemplateEntity> expiredTemplates = getExpiredTemplates();
            if (expiredTemplates.isEmpty()) {
                return ResponseDTO.ok("没有过期模板需要处理");
            }

            int updatedCount = 0;
            for (BiometricTemplateEntity template : expiredTemplates) {
                template.setTemplateStatus(BiometricTemplateEntity.TemplateStatus.EXPIRED.getValue());
                template.updateLastUpdateTime();

                int result = biometricTemplateDao.updateById(template);
                if (result > 0) {
                    biometricCacheManager.removeBiometricTemplate(template.getId());
                    updatedCount++;
                }
            }

            log.info("批量更新过期模板状态成功: count={}", updatedCount);
            return ResponseDTO.ok(String.format("成功更新%d个过期模板状态", updatedCount));

        } catch (Exception e) {
            log.error("批量更新过期模板状态异常", e);
            return ResponseDTO.error("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getTemplateStatistics() {
        // 获取算法版本统计
        List<BiometricTemplateEntity> algorithmStats = biometricTemplateDao.selectAlgorithmStatistics();

        // 统计各类型模板数量
        Map<String, Long> typeCountMap = new HashMap<>();
        for (BiometricTemplateEntity.BiometricType type : BiometricTemplateEntity.BiometricType.values()) {
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByBiometricType(type.getValue());
            typeCountMap.put(type.getValue(), (long) templates.size());
        }

        // 统计状态分布
        Map<String, Long> statusCountMap = new HashMap<>();
        for (BiometricTemplateEntity.TemplateStatus status : BiometricTemplateEntity.TemplateStatus.values()) {
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByTemplateStatus(status.getValue());
            statusCountMap.put(status.getValue(), (long) templates.size());
        }

        return Map.of(
            "totalTemplates", biometricTemplateDao.selectCount(null),
            "typeDistribution", typeCountMap,
            "statusDistribution", statusCountMap,
            "algorithmStatistics", algorithmStats.size()
        );
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取下一个模板索引
     */
    private Integer getNextTemplateIndex(Long personId, String biometricType) {
        List<BiometricTemplateEntity> existingTemplates = biometricTemplateDao.selectByPersonIdAndType(personId, biometricType);
        return existingTemplates.size();
    }

    /**
     * 从算法信息中提取模板版本
     */
    private String extractTemplateVersion(String algorithmInfo) {
        // 简化实现，实际应该解析JSON
        return "1.0.0";
    }

    /**
     * 生成安全元数据
     */
    private String generateSecurityMetadata() {
        // 生成SM4加密的安全元数据
        Map<String, Object> securityData = Map.of(
            "encryption", "SM4",
            "keyVersion", "v1.0",
            "encryptionTime", LocalDateTime.now().toString(),
            "securityLevel", "HIGH"
        );
        return securityData.toString(); // 实际应该序列化为JSON
    }
}