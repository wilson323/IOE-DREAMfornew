package net.lab1024.sa.base.module.biometric.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.base.common.entity.BaseEntity;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.module.biometric.dao.PersonBiometricDao;
import net.lab1024.sa.base.module.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.base.module.biometric.entity.PersonBiometricEntity;
import net.lab1024.sa.base.module.biometric.entity.BiometricTemplateEntity;
import net.lab1024.sa.base.module.biometric.manager.BiometricCacheManager;

import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;

/**
 * 人员生物特征服务
 * <p>
 * 以人为中心的生物特征管理核心服务，提供人员生物特征的完整生命周期管理
 * 核心能力：
 * 1. 人员生物特征档案管理（一人一档）
 * 2. 生物特征类型统计和状态管理
 * 3. 生物特征质量评估和监控
 * 4. 生物特征注册状态跟踪
 * 5. 跨模块生物特征数据统一管理
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Service
public class PersonBiometricService {

    @Resource
    private PersonBiometricDao personBiometricDao;

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Resource
    private BiometricCacheManager biometricCacheManager;

    // ==================== 基础CRUD操作 ====================

    /**
     * 根据人员ID获取生物特征档案
     *
     * @param personId 人员ID
     * @return 人员生物特征信息
     */
    public PersonBiometricEntity getByPersonId(Long personId) {
        if (personId == null) {
            return null;
        }

        // 先从缓存获取
        PersonBiometricEntity cached = biometricCacheManager.getPersonBiometric(personId);
        if (cached != null) {
            return cached;
        }

        // 从数据库查询
        PersonBiometricEntity entity = personBiometricDao.selectByPersonId(personId);
        if (entity != null) {
            biometricCacheManager.cachePersonBiometric(entity);
        }

        return entity;
    }

    /**
     * 根据人员编号获取生物特征档案
     *
     * @param personCode 人员编号
     * @return 人员生物特征信息
     */
    public PersonBiometricEntity getByPersonCode(String personCode) {
        if (personCode == null || personCode.trim().isEmpty()) {
            return null;
        }

        return personBiometricDao.selectByPersonCode(personCode.trim());
    }

    /**
     * 创建人员生物特征档案
     *
     * @param personId 人员ID
     * @param personType 人员类型
     * @param personName 人员姓名
     * @param personCode 人员编号
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createPersonBiometric(Long personId, String personType,
                                                    String personName, String personCode) {
        try {
            // 检查是否已存在
            PersonBiometricEntity existing = personBiometricDao.selectByPersonId(personId);
            if (existing != null) {
                return ResponseDTO.error("该人员生物特征档案已存在");
            }

            // 创建新档案
            PersonBiometricEntity entity = new PersonBiometricEntity();
            entity.setPersonId(personId);
            entity.setPersonType(personType);
            entity.setPersonName(personName);
            entity.setPersonCode(personCode);
            entity.setFaceCount(0);
            entity.setFingerprintCount(0);
            entity.setIrisCount(0);
            entity.setPalmprintCount(0);
            entity.setVoiceCount(0);
            entity.setOverallQualityScore(BigDecimal.ZERO);
            entity.setEnrollmentStatus(PersonBiometricEntity.EnrollmentStatus.EMPTY.getValue());
            entity.setEnableStatus(1);
            entity.setLastUpdateTime(LocalDateTime.now());

            int result = personBiometricDao.insert(entity);
            if (result > 0) {
                biometricCacheManager.cachePersonBiometric(entity);
                log.info("创建人员生物特征档案成功: personId={}, personType={}, personName={}",
                        personId, personType, personName);
                return ResponseDTO.ok("人员生物特征档案创建成功");
            } else {
                return ResponseDTO.error("创建人员生物特征档案失败");
            }

        } catch (Exception e) {
            log.error("创建人员生物特征档案异常", e);
            return ResponseDTO.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新人员生物特征档案
     *
     * @param entity 人员生物特征实体
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updatePersonBiometric(PersonBiometricEntity entity) {
        try {
            if (entity.getPersonId() == null) {
                return ResponseDTO.error("人员ID不能为空");
            }

            // 检查是否存在
            PersonBiometricEntity existing = personBiometricDao.selectByPersonId(entity.getPersonId());
            if (existing == null) {
                return ResponseDTO.error("人员生物特征档案不存在");
            }

            // 更新基础信息
            existing.setPersonType(entity.getPersonType());
            existing.setPersonName(entity.getPersonName());
            existing.setPersonCode(entity.getPersonCode());
            existing.setEnableStatus(entity.getEnableStatus());
            existing.setRemark(entity.getRemark());
            existing.updateLastUpdateTime();

            // 重新计算注册状态
            existing.updateEnrollmentStatus();

            int result = personBiometricDao.updateById(existing);
            if (result > 0) {
                biometricCacheManager.cachePersonBiometric(existing);
                log.info("更新人员生物特征档案成功: personId={}", entity.getPersonId());
                return ResponseDTO.ok("人员生物特征档案更新成功");
            } else {
                return ResponseDTO.error("更新人员生物特征档案失败");
            }

        } catch (Exception e) {
            log.error("更新人员生物特征档案异常", e);
            return ResponseDTO.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除人员生物特征档案（软删除）
     *
     * @param personId 人员ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deletePersonBiometric(Long personId) {
        try {
            if (personId == null) {
                return ResponseDTO.error("人员ID不能为空");
            }

            PersonBiometricEntity existing = personBiometricDao.selectByPersonId(personId);
            if (existing == null) {
                return ResponseDTO.error("人员生物特征档案不存在");
            }

            // 软删除
            existing.setDeletedFlag(1);
            existing.updateLastUpdateTime();

            int result = personBiometricDao.updateById(existing);
            if (result > 0) {
                biometricCacheManager.removePersonBiometric(personId);
                log.info("删除人员生物特征档案成功: personId={}", personId);
                return ResponseDTO.ok("人员生物特征档案删除成功");
            } else {
                return ResponseDTO.error("删除人员生物特征档案失败");
            }

        } catch (Exception e) {
            log.error("删除人员生物特征档案异常", e);
            return ResponseDTO.error("删除失败: " + e.getMessage());
        }
    }

    // ==================== 生物特征数量管理 ====================

    /**
     * 增加指定类型的生物特征数量
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> incrementBiometricCount(Long personId, String biometricType) {
        try {
            PersonBiometricEntity entity = getByPersonId(personId);
            if (entity == null) {
                return ResponseDTO.error("人员生物特征档案不存在");
            }

            entity.incrementBiometricCount(biometricType);
            entity.updateEnrollmentStatus();

            int result = personBiometricDao.updateById(entity);
            if (result > 0) {
                biometricCacheManager.cachePersonBiometric(entity);
                log.info("增加生物特征数量成功: personId={}, biometricType={}", personId, biometricType);
                return ResponseDTO.ok("生物特征数量增加成功");
            } else {
                return ResponseDTO.error("生物特征数量增加失败");
            }

        } catch (Exception e) {
            log.error("增加生物特征数量异常", e);
            return ResponseDTO.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 减少指定类型的生物特征数量
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> decrementBiometricCount(Long personId, String biometricType) {
        try {
            PersonBiometricEntity entity = getByPersonId(personId);
            if (entity == null) {
                return ResponseDTO.error("人员生物特征档案不存在");
            }

            entity.decrementBiometricCount(biometricType);
            entity.updateEnrollmentStatus();

            int result = personBiometricDao.updateById(entity);
            if (result > 0) {
                biometricCacheManager.cachePersonBiometric(entity);
                log.info("减少生物特征数量成功: personId={}, biometricType={}", personId, biometricType);
                return ResponseDTO.ok("生物特征数量减少成功");
            } else {
                return ResponseDTO.error("生物特征数量减少失败");
            }

        } catch (Exception e) {
            log.error("减少生物特征数量异常", e);
            return ResponseDTO.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 重新计算人员的生物特征质量分数
     *
     * @param personId 人员ID
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> recalculateQualityScore(Long personId) {
        try {
            PersonBiometricEntity entity = getByPersonId(personId);
            if (entity == null) {
                return ResponseDTO.error("人员生物特征档案不存在");
            }

            // 查询该人员的所有生物特征模板
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByPersonId(personId);

            if (templates.isEmpty()) {
                entity.setOverallQualityScore(BigDecimal.ZERO);
            } else {
                // 计算平均质量分数
                Double totalScore = templates.stream()
                    .filter(template -> template.getQualityScore() != null)
                    .map(BiometricTemplateEntity::getQualityScore)
                    .reduce(0.0, Double::sum);

                BigDecimal averageScore = BigDecimal.valueOf(
                    totalScore / templates.size());

                entity.setOverallQualityScore(averageScore);
            }

            entity.updateLastUpdateTime();

            int result = personBiometricDao.updateById(entity);
            if (result > 0) {
                biometricCacheManager.cachePersonBiometric(entity);
                log.info("重新计算质量分数成功: personId={}, averageScore={}",
                        personId, entity.getOverallQualityScore());
                return ResponseDTO.ok("质量分数重新计算成功");
            } else {
                return ResponseDTO.error("质量分数重新计算失败");
            }

        } catch (Exception e) {
            log.error("重新计算质量分数异常", e);
            return ResponseDTO.error("操作失败: " + e.getMessage());
        }
    }

    // ==================== 查询和统计 ====================

    /**
     * 分页查询人员生物特征信息
     *
     * @param personType 人员类型
     * @param enrollmentStatus 注册状态
     * @param personName 人员姓名
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public ResponseDTO<Page<PersonBiometricEntity>> queryPage(String personType, String enrollmentStatus,
                                                             String personName, Integer pageNum, Integer pageSize) {
        try {
            Page<PersonBiometricEntity> pageParam = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20);
            Page<PersonBiometricEntity> result = personBiometricDao.selectPageByCondition(
                    pageParam, personType, enrollmentStatus, personName);

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询人员生物特征异常", e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据生物特征类型查询人员
     *
     * @param biometricType 生物特征类型
     * @return 人员列表
     */
    public List<PersonBiometricEntity> queryByBiometricType(String biometricType) {
        return personBiometricDao.selectByBiometricType(biometricType);
    }

    /**
     * 根据人员类型查询人员
     *
     * @param personType 人员类型
     * @return 人员列表
     */
    public List<PersonBiometricEntity> queryByPersonType(String personType) {
        return personBiometricDao.selectByPersonType(personType);
    }

    /**
     * 查询注册不完整的人员
     *
     * @return 注册不完整的人员列表
     */
    public List<PersonBiometricEntity> queryIncompleteEnrollment() {
        return personBiometricDao.selectIncompleteEnrollment();
    }

    /**
     * 获取生物特征统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getBiometricStatistics() {
        List<PersonBiometricEntity> statistics = personBiometricDao.selectBiometricStatistics();

        // 统计各类生物特征数量
        int totalPersons = statistics.size();
        long totalFace = statistics.stream().mapToLong(e -> e.getFaceCount() != null ? e.getFaceCount() : 0).sum();
        long totalFingerprint = statistics.stream().mapToLong(e -> e.getFingerprintCount() != null ? e.getFingerprintCount() : 0).sum();
        long totalIris = statistics.stream().mapToLong(e -> e.getIrisCount() != null ? e.getIrisCount() : 0).sum();
        long totalPalmprint = statistics.stream().mapToLong(e -> e.getPalmprintCount() != null ? e.getPalmprintCount() : 0).sum();
        long totalVoice = statistics.stream().mapToLong(e -> e.getVoiceCount() != null ? e.getVoiceCount() : 0).sum();

        // 统计注册状态
        long completeCount = statistics.stream()
                .filter(e -> PersonBiometricEntity.EnrollmentStatus.COMPLETE.getValue().equals(e.getEnrollmentStatus()))
                .count();
        long incompleteCount = statistics.stream()
                .filter(e -> PersonBiometricEntity.EnrollmentStatus.INCOMPLETE.getValue().equals(e.getEnrollmentStatus()))
                .count();
        long emptyCount = statistics.stream()
                .filter(e -> PersonBiometricEntity.EnrollmentStatus.EMPTY.getValue().equals(e.getEnrollmentStatus()))
                .count();

        return Map.of(
            "totalPersons", totalPersons,
            "biometricCounts", Map.of(
                "FACE", totalFace,
                "FINGERPRINT", totalFingerprint,
                "IRIS", totalIris,
                "PALMPRINT", totalPalmprint,
                "VOICE", totalVoice
            ),
            "enrollmentStatus", Map.of(
                "COMPLETE", completeCount,
                "INCOMPLETE", incompleteCount,
                "EMPTY", emptyCount
            )
        );
    }

    // ==================== 业务验证 ====================

    /**
     * 检查人员是否具有指定的生物特征类型
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @return 是否具有该生物特征类型
     */
    public boolean hasBiometricType(Long personId, String biometricType) {
        PersonBiometricEntity entity = getByPersonId(personId);
        return entity != null && entity.hasBiometricType(biometricType);
    }

    /**
     * 检查人员生物特征注册是否完整
     *
     * @param personId 人员ID
     * @return 是否注册完整
     */
    public boolean isEnrollmentComplete(Long personId) {
        PersonBiometricEntity entity = getByPersonId(personId);
        return entity != null && entity.isComplete();
    }

    /**
     * 检查人员是否启用生物特征
     *
     * @param personId 人员ID
     * @return 是否启用
     */
    public boolean isEnabled(Long personId) {
        PersonBiometricEntity entity = getByPersonId(personId);
        return entity != null && entity.isEnabled();
    }

    /**
     * 获取人员生物特征描述信息
     *
     * @param personId 人员ID
     * @return 生物特征描述
     */
    public String getBiometricSummary(Long personId) {
        PersonBiometricEntity entity = getByPersonId(personId);
        return entity != null ? entity.getBiometricSummary() : "无生物特征数据";
    }
}