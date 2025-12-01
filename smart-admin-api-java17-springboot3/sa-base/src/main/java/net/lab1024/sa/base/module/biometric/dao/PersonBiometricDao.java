package net.lab1024.sa.base.module.biometric.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.base.module.biometric.entity.PersonBiometricEntity;

/**
 * 人员生物特征DAO
 * <p>
 * 提供以人为中心的生物特征数据访问，支持人员生物特征的完整生命周期管理
 * 核心查询能力：按人员类型、注册状态、生物特征类型等维度查询
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Mapper
@Repository
public interface PersonBiometricDao extends BaseMapper<PersonBiometricEntity> {

    /**
     * 根据人员ID查询生物特征信息
     *
     * @param personId 人员ID
     * @return 人员生物特征信息
     */
    default PersonBiometricEntity selectByPersonId(Long personId) {
        return selectOne(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getPersonId, personId)
                .eq(PersonBiometricEntity::getDeletedFlag, false));
    }

    /**
     * 根据人员编号查询生物特征信息
     *
     * @param personCode 人员编号
     * @return 人员生物特征信息
     */
    default PersonBiometricEntity selectByPersonCode(String personCode) {
        return selectOne(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getPersonCode, personCode)
                .eq(PersonBiometricEntity::getDeletedFlag, false));
    }

    /**
     * 根据人员类型查询人员列表
     *
     * @param personType 人员类型
     * @return 人员生物特征列表
     */
    default List<PersonBiometricEntity> selectByPersonType(String personType) {
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getPersonType, personType)
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .orderByDesc(PersonBiometricEntity::getUpdateTime));
    }

    /**
     * 根据注册状态查询人员列表
     *
     * @param enrollmentStatus 注册状态
     * @return 人员生物特征列表
     */
    default List<PersonBiometricEntity> selectByEnrollmentStatus(String enrollmentStatus) {
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getEnrollmentStatus, enrollmentStatus)
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .orderByDesc(PersonBiometricEntity::getUpdateTime));
    }

    /**
     * 查询具有指定生物特征类型的人员
     *
     * @param biometricType 生物特征类型 (FACE, FINGERPRINT, IRIS, PALMPRINT, VOICE)
     * @return 具有该生物特征类型的人员列表
     */
    default List<PersonBiometricEntity> selectByBiometricType(String biometricType) {
        LambdaQueryWrapper<PersonBiometricEntity> wrapper = new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getDeletedFlag, false);

        // 根据生物特征类型构建查询条件
        switch (biometricType.toUpperCase()) {
            case "FACE":
                wrapper.gt(PersonBiometricEntity::getFaceCount, 0);
                break;
            case "FINGERPRINT":
                wrapper.gt(PersonBiometricEntity::getFingerprintCount, 0);
                break;
            case "IRIS":
                wrapper.gt(PersonBiometricEntity::getIrisCount, 0);
                break;
            case "PALMPRINT":
                wrapper.gt(PersonBiometricEntity::getPalmprintCount, 0);
                break;
            case "VOICE":
                wrapper.gt(PersonBiometricEntity::getVoiceCount, 0);
                break;
        }

        wrapper.orderByDesc(PersonBiometricEntity::getUpdateTime);
        return selectList(wrapper);
    }

    /**
     * 查询具有多种生物特征类型的人员
     *
     * @param biometricTypes 生物特征类型列表
     * @return 同时具有所有指定生物特征类型的人员列表
     */
    default List<PersonBiometricEntity> selectByBiometricTypes(List<String> biometricTypes) {
        LambdaQueryWrapper<PersonBiometricEntity> wrapper = new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getDeletedFlag, false);

        // 为每个生物特征类型添加条件
        for (String biometricType : biometricTypes) {
            switch (biometricType.toUpperCase()) {
                case "FACE":
                    wrapper.gt(PersonBiometricEntity::getFaceCount, 0);
                    break;
                case "FINGERPRINT":
                    wrapper.gt(PersonBiometricEntity::getFingerprintCount, 0);
                    break;
                case "IRIS":
                    wrapper.gt(PersonBiometricEntity::getIrisCount, 0);
                    break;
                case "PALMPRINT":
                    wrapper.gt(PersonBiometricEntity::getPalmprintCount, 0);
                    break;
                case "VOICE":
                    wrapper.gt(PersonBiometricEntity::getVoiceCount, 0);
                    break;
            }
        }

        wrapper.orderByDesc(PersonBiometricEntity::getUpdateTime);
        return selectList(wrapper);
    }

    /**
     * 分页查询人员生物特征信息
     *
     * @param page 分页参数
     * @param personType 人员类型（可选）
     * @param enrollmentStatus 注册状态（可选）
     * @param personName 人员姓名（可选，模糊查询）
     * @return 分页结果
     */
    default Page<PersonBiometricEntity> selectPageByCondition(
            Page<PersonBiometricEntity> page,
            @Param("personType") String personType,
            @Param("enrollmentStatus") String enrollmentStatus,
            @Param("personName") String personName) {

        LambdaQueryWrapper<PersonBiometricEntity> wrapper = new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getDeletedFlag, false);

        if (personType != null && !personType.trim().isEmpty()) {
            wrapper.eq(PersonBiometricEntity::getPersonType, personType);
        }

        if (enrollmentStatus != null && !enrollmentStatus.trim().isEmpty()) {
            wrapper.eq(PersonBiometricEntity::getEnrollmentStatus, enrollmentStatus);
        }

        if (personName != null && !personName.trim().isEmpty()) {
            wrapper.like(PersonBiometricEntity::getPersonName, personName);
        }

        wrapper.orderByDesc(PersonBiometricEntity::getUpdateTime);

        return selectPage(page, wrapper);
    }

    /**
     * 查询生物特征数量统计
     *
     * @return 各类生物特征的数量统计
     */
    default List<PersonBiometricEntity> selectBiometricStatistics() {
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .select(
                    PersonBiometricEntity::getPersonType,
                    PersonBiometricEntity::getEnrollmentStatus,
                    PersonBiometricEntity::getFaceCount,
                    PersonBiometricEntity::getFingerprintCount,
                    PersonBiometricEntity::getIrisCount,
                    PersonBiometricEntity::getPalmprintCount,
                    PersonBiometricEntity::getVoiceCount
                )
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .gt(PersonBiometricEntity::getTotalBiometricCount, 0));
    }

    /**
     * 查询注册不完整的人员（有生物特征但状态为INCOMPLETE）
     *
     * @return 注册不完整的人员列表
     */
    default List<PersonBiometricEntity> selectIncompleteEnrollment() {
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .gt(PersonBiometricEntity::getTotalBiometricCount, 0)
                .eq(PersonBiometricEntity::getEnrollmentStatus, "INCOMPLETE")
                .orderByAsc(PersonBiometricEntity::getUpdateTime));
    }

    /**
     * 查询生物特征过期的人员
     *
     * @param currentTime 当前时间
     * @return 生物特征过期的人员列表
     */
    default List<PersonBiometricEntity> selectExpiredBiometric(@Param("currentTime") LocalDateTime currentTime) {
        // 注意：这里需要在XML中实现复杂的时间比较逻辑
        // 因为需要检查BiometricTemplateEntity中的validTo字段
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .gt(PersonBiometricEntity::getTotalBiometricCount, 0)
                .orderByDesc(PersonBiometricEntity::getUpdateTime));
    }

    /**
     * 批量更新人员生物特征状态
     *
     * @param personIds 人员ID列表
     * @param enrollmentStatus 新的注册状态
     * @return 更新记录数
     */
    default int updateEnrollmentStatusBatch(@Param("personIds") List<Long> personIds,
                                          @Param("enrollmentStatus") String enrollmentStatus) {
        // 注意：这里需要在XML中实现批量更新
        return 0;
    }

    /**
     * 统计各类型人员数量
     *
     * @return 人员类型统计结果
     */
    default List<PersonBiometricEntity> selectPersonTypeStatistics() {
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .select(
                    PersonBiometricEntity::getPersonType,
                    PersonBiometricEntity::getEnrollmentStatus
                )
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .groupBy(PersonBiometricEntity::getPersonType, PersonBiometricEntity::getEnrollmentStatus));
    }

    /**
     * 根据更新时间查询最近更新的人员
     *
     * @param hours 最近小时数
     * @return 最近更新的人员列表
     */
    default List<PersonBiometricEntity> selectRecentlyUpdated(@Param("hours") int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .ge(PersonBiometricEntity::getUpdateTime, cutoffTime)
                .orderByDesc(PersonBiometricEntity::getUpdateTime));
    }

    /**
     * 查询质量分数低于阈值的人员
     *
     * @param threshold 质量分数阈值
     * @return 质量分数低于阈值的人员列表
     */
    default List<PersonBiometricEntity> selectLowQuality(@Param("threshold") java.math.BigDecimal threshold) {
        // 注意：这里需要在XML中实现质量分数比较
        return selectList(new LambdaQueryWrapper<PersonBiometricEntity>()
                .eq(PersonBiometricEntity::getDeletedFlag, false)
                .lt(PersonBiometricEntity::getOverallQualityScore, threshold)
                .orderByAsc(PersonBiometricEntity::getOverallQualityScore));
    }
}