package net.lab1024.sa.base.module.biometric.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.base.module.biometric.entity.BiometricTemplateEntity;

/**
 * 生物特征模板DAO
 * <p>
 * 提供生物特征模板数据的完整访问，支持模板的存储、查询、更新和删除
 * 核心查询能力：按人员ID、生物特征类型、模板状态、质量分数、有效期等维度查询
 * 支持SM4加密数据的存储和检索
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Mapper
@Repository
public interface BiometricTemplateDao extends BaseMapper<BiometricTemplateEntity> {

    /**
     * 根据人员ID查询所有生物特征模板
     *
     * @param personId 人员ID
     * @return 生物特征模板列表
     */
    default List<BiometricTemplateEntity> selectByPersonId(Long personId) {
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getPersonId, personId)
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .orderByAsc(BiometricTemplateEntity::getBiometricType)
                .orderByAsc(BiometricTemplateEntity::getTemplateIndex));
    }

    /**
     * 根据人员ID和生物特征类型查询模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @return 生物特征模板列表
     */
    default List<BiometricTemplateEntity> selectByPersonIdAndType(Long personId, String biometricType) {
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getPersonId, personId)
                .eq(BiometricTemplateEntity::getBiometricType, biometricType)
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .orderByAsc(BiometricTemplateEntity::getTemplateIndex));
    }

    /**
     * 根据人员ID、生物特征类型和模板索引查询特定模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型
     * @param templateIndex 模板索引
     * @return 生物特征模板
     */
    default BiometricTemplateEntity selectByPersonIdTypeAndIndex(Long personId, String biometricType, Integer templateIndex) {
        return selectOne(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getPersonId, personId)
                .eq(BiometricTemplateEntity::getBiometricType, biometricType)
                .eq(BiometricTemplateEntity::getTemplateIndex, templateIndex)
                .eq(BiometricTemplateEntity::getDeletedFlag, 0));
    }

    /**
     * 根据生物特征类型查询模板
     *
     * @param biometricType 生物特征类型
     * @return 生物特征模板列表
     */
    default List<BiometricTemplateEntity> selectByBiometricType(String biometricType) {
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getBiometricType, biometricType)
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .orderByDesc(BiometricTemplateEntity::getCreateTime));
    }

    /**
     * 根据模板状态查询模板
     *
     * @param templateStatus 模板状态
     * @return 生物特征模板列表
     */
    default List<BiometricTemplateEntity> selectByTemplateStatus(String templateStatus) {
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getTemplateStatus, templateStatus)
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .orderByDesc(BiometricTemplateEntity::getUpdateTime));
    }

    /**
     * 查询活跃的生物特征模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型（可选）
     * @return 活跃模板列表
     */
    default List<BiometricTemplateEntity> selectActiveTemplates(@Param("personId") Long personId,
                                                              @Param("biometricType") String biometricType) {
        LambdaQueryWrapper<BiometricTemplateEntity> wrapper = new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .eq(BiometricTemplateEntity::getTemplateStatus, BiometricTemplateEntity.TemplateStatus.ACTIVE.getValue());

        if (personId != null) {
            wrapper.eq(BiometricTemplateEntity::getPersonId, personId);
        }

        if (biometricType != null && !biometricType.trim().isEmpty()) {
            wrapper.eq(BiometricTemplateEntity::getBiometricType, biometricType);
        }

        wrapper.orderByAsc(BiometricTemplateEntity::getBiometricType)
               .orderByAsc(BiometricTemplateEntity::getTemplateIndex);

        return selectList(wrapper);
    }

    /**
     * 查询过期的生物特征模板
     *
     * @param currentTime 当前时间
     * @return 过期模板列表
     */
    default List<BiometricTemplateEntity> selectExpiredTemplates(@Param("currentTime") LocalDateTime currentTime) {
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .ne(BiometricTemplateEntity::getTemplateStatus, BiometricTemplateEntity.TemplateStatus.EXPIRED.getValue())
                .isNotNull(BiometricTemplateEntity::getValidTo)
                .lt(BiometricTemplateEntity::getValidTo, currentTime)
                .orderByAsc(BiometricTemplateEntity::getValidTo));
    }

    /**
     * 查询即将过期的生物特征模板（指定天数内）
     *
     * @param currentTime 当前时间
     * @param days 天数
     * @return 即将过期模板列表
     */
    default List<BiometricTemplateEntity> selectSoonToExpire(@Param("currentTime") LocalDateTime currentTime,
                                                           @Param("days") int days) {
        LocalDateTime threshold = currentTime.plusDays(days);
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .ne(BiometricTemplateEntity::getTemplateStatus, BiometricTemplateEntity.TemplateStatus.EXPIRED.getValue())
                .isNotNull(BiometricTemplateEntity::getValidTo)
                .gt(BiometricTemplateEntity::getValidTo, currentTime)
                .le(BiometricTemplateEntity::getValidTo, threshold)
                .orderByAsc(BiometricTemplateEntity::getValidTo));
    }

    /**
     * 查询质量分数低于阈值的模板
     *
     * @param threshold 质量分数阈值
     * @return 低质量模板列表
     */
    default List<BiometricTemplateEntity> selectLowQuality(@Param("threshold") Double threshold) {
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .lt(BiometricTemplateEntity::getQualityScore, threshold)
                .orderByAsc(BiometricTemplateEntity::getQualityScore));
    }

    /**
     * 查询主模板
     *
     * @param personId 人员ID
     * @param biometricType 生物特征类型（可选）
     * @return 主模板列表
     */
    default List<BiometricTemplateEntity> selectPrimaryTemplates(@Param("personId") Long personId,
                                                                @Param("biometricType") String biometricType) {
        LambdaQueryWrapper<BiometricTemplateEntity> wrapper = new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .eq(BiometricTemplateEntity::getIsPrimary, 1);

        if (personId != null) {
            wrapper.eq(BiometricTemplateEntity::getPersonId, personId);
        }

        if (biometricType != null && !biometricType.trim().isEmpty()) {
            wrapper.eq(BiometricTemplateEntity::getBiometricType, biometricType);
        }

        wrapper.orderByAsc(BiometricTemplateEntity::getBiometricType);

        return selectList(wrapper);
    }

    /**
     * 查询需要重新注册的模板
     *
     * @param personId 人员ID（可选）
     * @return 需要重新注册的模板列表
     */
    default List<BiometricTemplateEntity> selectTemplatesNeedingReEnrollment(@Param("personId") Long personId) {
        LambdaQueryWrapper<BiometricTemplateEntity> wrapper = new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0);

        if (personId != null) {
            wrapper.eq(BiometricTemplateEntity::getPersonId, personId);
        }

        // 需要重新注册的条件：已过期、已损坏、质量不合格
        wrapper.and(w -> w
                .eq(BiometricTemplateEntity::getTemplateStatus, BiometricTemplateEntity.TemplateStatus.EXPIRED.getValue())
                .or()
                .eq(BiometricTemplateEntity::getTemplateStatus, BiometricTemplateEntity.TemplateStatus.CORRUPTED.getValue())
                .or()
                .lt(BiometricTemplateEntity::getQualityScore, 0.8)
        );

        wrapper.orderByAsc(BiometricTemplateEntity::getPersonId)
               .orderByAsc(BiometricTemplateEntity::getBiometricType);

        return selectList(wrapper);
    }

    /**
     * 分页查询生物特征模板
     *
     * @param page 分页参数
     * @param personId 人员ID（可选）
     * @param biometricType 生物特征类型（可选）
     * @param templateStatus 模板状态（可选）
     * @param qualityThreshold 质量分数阈值（可选）
     * @param includeExpired 是否包含过期模板（可选）
     * @return 分页结果
     */
    default Page<BiometricTemplateEntity> selectPageByCondition(
            Page<BiometricTemplateEntity> page,
            @Param("personId") Long personId,
            @Param("biometricType") String biometricType,
            @Param("templateStatus") String templateStatus,
            @Param("qualityThreshold") Double qualityThreshold,
            @Param("includeExpired") Boolean includeExpired) {

        LambdaQueryWrapper<BiometricTemplateEntity> wrapper = new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0);

        if (personId != null) {
            wrapper.eq(BiometricTemplateEntity::getPersonId, personId);
        }

        if (biometricType != null && !biometricType.trim().isEmpty()) {
            wrapper.eq(BiometricTemplateEntity::getBiometricType, biometricType);
        }

        if (templateStatus != null && !templateStatus.trim().isEmpty()) {
            wrapper.eq(BiometricTemplateEntity::getTemplateStatus, templateStatus);
        }

        if (qualityThreshold != null) {
            if (includeExpired != null && includeExpired) {
                wrapper.ge(BiometricTemplateEntity::getQualityScore, qualityThreshold);
            } else {
                // 不包含过期模板，要求状态为活跃且质量达标
                wrapper.eq(BiometricTemplateEntity::getTemplateStatus, BiometricTemplateEntity.TemplateStatus.ACTIVE.getValue())
                       .ge(BiometricTemplateEntity::getQualityScore, qualityThreshold);
            }
        }

        wrapper.orderByAsc(BiometricTemplateEntity::getBiometricType)
               .orderByAsc(BiometricTemplateEntity::getTemplateIndex);

        return selectPage(page, wrapper);
    }

    /**
     * 查询使用频率统计
     *
     * @param personId 人员ID（可选）
     * @param biometricType 生物特征类型（可选）
     * @param days 统计天数
     * @return 使用频率统计结果
     */
    default List<BiometricTemplateEntity> selectUsageStatistics(@Param("personId") Long personId,
                                                              @Param("biometricType") String biometricType,
                                                              @Param("days") int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<BiometricTemplateEntity> wrapper = new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .gt(BiometricTemplateEntity::getUsageCount, 0)
                .ge(BiometricTemplateEntity::getLastUsedTime, cutoffTime);

        if (personId != null) {
            wrapper.eq(BiometricTemplateEntity::getPersonId, personId);
        }

        if (biometricType != null && !biometricType.trim().isEmpty()) {
            wrapper.eq(BiometricTemplateEntity::getBiometricType, biometricType);
        }

        wrapper.orderByDesc(BiometricTemplateEntity::getUsageCount)
               .orderByDesc(BiometricTemplateEntity::getLastUsedTime);

        return selectList(wrapper);
    }

    /**
     * 查询失败次数过多的模板
     *
     * @param failureThreshold 失败次数阈值
     * @return 失败次数过多的模板列表
     */
    default List<BiometricTemplateEntity> selectHighFailureTemplates(@Param("failureThreshold") Integer failureThreshold) {
        int threshold = failureThreshold != null ? failureThreshold : 10;

        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .ge(BiometricTemplateEntity::getFailureCount, threshold)
                .orderByDesc(BiometricTemplateEntity::getFailureCount));
    }

    /**
     * 查询最近更新的模板
     *
     * @param hours 最近小时数
     * @param personId 人员ID（可选）
     * @return 最近更新的模板列表
     */
    default List<BiometricTemplateEntity> selectRecentlyUpdated(@Param("hours") int hours,
                                                              @Param("personId") Long personId) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);

        LambdaQueryWrapper<BiometricTemplateEntity> wrapper = new LambdaQueryWrapper<BiometricTemplateEntity>()
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .ge(BiometricTemplateEntity::getUpdateTime, cutoffTime);

        if (personId != null) {
            wrapper.eq(BiometricTemplateEntity::getPersonId, personId);
        }

        wrapper.orderByDesc(BiometricTemplateEntity::getUpdateTime);

        return selectList(wrapper);
    }

    /**
     * 查询算法版本统计
     *
     * @return 按算法版本统计的结果
     */
    default List<BiometricTemplateEntity> selectAlgorithmStatistics() {
        return selectList(new LambdaQueryWrapper<BiometricTemplateEntity>()
                .select(
                    BiometricTemplateEntity::getBiometricType,
                    BiometricTemplateEntity::getTemplateVersion,
                    BiometricTemplateEntity::getAlgorithmInfo
                )
                .eq(BiometricTemplateEntity::getDeletedFlag, 0)
                .isNotNull(BiometricTemplateEntity::getAlgorithmInfo)
                .groupBy(BiometricTemplateEntity::getBiometricType,
                        BiometricTemplateEntity::getTemplateVersion,
                        BiometricTemplateEntity::getAlgorithmInfo));
    }

    /**
     * 批量更新模板状态
     *
     * @param ids 模板ID列表
     * @param newStatus 新状态
     * @return 更新记录数
     */
    default int updateTemplateStatusBatch(@Param("ids") List<Long> ids,
                                         @Param("newStatus") String newStatus) {
        // 注意：这里需要在XML中实现批量更新
        return 0;
    }

    /**
     * 批量重置失败次数
     *
     * @param ids 模板ID列表
     * @return 更新记录数
     */
    default int resetFailureCountBatch(@Param("ids") List<Long> ids) {
        // 注意：这里需要在XML中实现批量更新
        return 0;
    }
}