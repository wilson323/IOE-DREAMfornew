package net.lab1024.sa.base.module.biometric.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 人员生物特征实体
 * <p>
 * 以人为中心的生物特征管理核心实体，记录人员所有生物特征的汇总信息
 * 一个人员对应一条记录，包含该人员的所有生物特征类型和数量统计
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_person_biometric")
public class PersonBiometricEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员ID（关联人员表）
     */
    private Long personId;

    /**
     * 人员类型
     * EMPLOYEE-员工
     * VISITOR-访客
     * STUDENT-学生
     * CONTRACTOR-承包商
     */
    private String personType;

    /**
     * 人员姓名（冗余字段，便于查询）
     */
    private String personName;

    /**
     * 人员编号（冗余字段，便于查询）
     */
    private String personCode;

    /**
     * 人脸特征数量
     */
    private Integer faceCount;

    /**
     * 指纹特征数量
     */
    private Integer fingerprintCount;

    /**
     * 虹膜特征数量
     */
    private Integer irisCount;

    /**
     * 掌纹特征数量
     */
    private Integer palmprintCount;

    /**
     * 声纹特征数量
     */
    private Integer voiceCount;

    /**
     * 整体质量分数
     * 基于所有生物特征模板的平均质量分数
     */
    private BigDecimal overallQualityScore;

    /**
     * 注册状态
     * COMPLETE-完整：至少有一种必需的生物特征已注册
     * INCOMPLETE-未完成：部分生物特征已注册
     * EXPIRED-已过期：所有生物特征已过期
     * EMPTY-空：没有任何生物特征
     */
    private String enrollmentStatus;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 启用状态
     * 0-禁用
     * 1-启用
     */
    private Integer enableStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 人员类型枚举
     */
    public enum PersonType {
        EMPLOYEE("EMPLOYEE", "员工"),
        VISITOR("VISITOR", "访客"),
        STUDENT("STUDENT", "学生"),
        CONTRACTOR("CONTRACTOR", "承包商");

        private final String value;
        private final String description;

        PersonType(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static PersonType fromValue(String value) {
            for (PersonType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown person type: " + value);
        }
    }

    /**
     * 注册状态枚举
     */
    public enum EnrollmentStatus {
        COMPLETE("COMPLETE", "完整"),
        INCOMPLETE("INCOMPLETE", "未完成"),
        EXPIRED("EXPIRED", "已过期"),
        EMPTY("EMPTY", "空");

        private final String value;
        private final String description;

        EnrollmentStatus(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static EnrollmentStatus fromValue(String value) {
            for (EnrollmentStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown enrollment status: " + value);
        }
    }

    /**
     * 检查是否已完整注册
     */
    public boolean isComplete() {
        return EnrollmentStatus.COMPLETE.getValue().equals(enrollmentStatus);
    }

    /**
     * 检查是否有生物特征
     */
    public boolean hasAnyBiometric() {
        int totalCount = (faceCount != null ? faceCount : 0)
                + (fingerprintCount != null ? fingerprintCount : 0)
                + (irisCount != null ? irisCount : 0)
                + (palmprintCount != null ? palmprintCount : 0)
                + (voiceCount != null ? voiceCount : 0);
        return totalCount > 0;
    }

    /**
     * 获取总生物特征数量
     */
    public int getTotalBiometricCount() {
        return (faceCount != null ? faceCount : 0)
                + (fingerprintCount != null ? fingerprintCount : 0)
                + (irisCount != null ? irisCount : 0)
                + (palmprintCount != null ? palmprintCount : 0)
                + (voiceCount != null ? voiceCount : 0);
    }

    /**
     * 检查是否有指定的生物特征类型
     */
    public boolean hasBiometricType(String biometricType) {
        switch (biometricType) {
            case "FACE":
                return faceCount != null && faceCount > 0;
            case "FINGERPRINT":
                return fingerprintCount != null && fingerprintCount > 0;
            case "IRIS":
                return irisCount != null && irisCount > 0;
            case "PALMPRINT":
                return palmprintCount != null && palmprintCount > 0;
            case "VOICE":
                return voiceCount != null && voiceCount > 0;
            default:
                return false;
        }
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enableStatus != null && enableStatus == 1;
    }

    /**
     * 检查质量分数是否合格
     */
    public boolean isQualityAcceptable(BigDecimal threshold) {
        return overallQualityScore != null
                && overallQualityScore.compareTo(threshold) >= 0;
    }

    /**
     * 检查质量分数是否优秀
     */
    public boolean isQualityExcellent() {
        return overallQualityScore != null
                && overallQualityScore.compareTo(new BigDecimal("0.90")) >= 0;
    }

    /**
     * 增加生物特征数量
     */
    public void incrementBiometricCount(String biometricType) {
        switch (biometricType) {
            case "FACE":
                faceCount = (faceCount == null ? 0 : faceCount) + 1;
                break;
            case "FINGERPRINT":
                fingerprintCount = (fingerprintCount == null ? 0 : fingerprintCount) + 1;
                break;
            case "IRIS":
                irisCount = (irisCount == null ? 0 : irisCount) + 1;
                break;
            case "PALMPRINT":
                palmprintCount = (palmprintCount == null ? 0 : palmprintCount) + 1;
                break;
            case "VOICE":
                voiceCount = (voiceCount == null ? 0 : voiceCount) + 1;
                break;
        }
        updateLastUpdateTime();
    }

    /**
     * 减少生物特征数量
     */
    public void decrementBiometricCount(String biometricType) {
        switch (biometricType) {
            case "FACE":
                faceCount = Math.max((faceCount == null ? 0 : faceCount) - 1, 0);
                break;
            case "FINGERPRINT":
                fingerprintCount = Math.max((fingerprintCount == null ? 0 : fingerprintCount) - 1, 0);
                break;
            case "IRIS":
                irisCount = Math.max((irisCount == null ? 0 : irisCount) - 1, 0);
                break;
            case "PALMPRINT":
                palmprintCount = Math.max((palmprintCount == null ? 0 : palmprintCount) - 1, 0);
                break;
            case "VOICE":
                voiceCount = Math.max((voiceCount == null ? 0 : voiceCount) - 1, 0);
                break;
        }
        updateLastUpdateTime();
    }

    /**
     * 更新最后更新时间
     */
    public void updateLastUpdateTime() {
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * 更新注册状态
     */
    public void updateEnrollmentStatus() {
        if (!hasAnyBiometric()) {
            this.enrollmentStatus = EnrollmentStatus.EMPTY.getValue();
        } else if (hasBiometricType("FACE") || hasBiometricType("FINGERPRINT")) {
            this.enrollmentStatus = EnrollmentStatus.COMPLETE.getValue();
        } else {
            this.enrollmentStatus = EnrollmentStatus.INCOMPLETE.getValue();
        }
    }

    /**
     * 获取生物特征类型统计信息
     */
    public String getBiometricSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("总计: ").append(getTotalBiometricCount());

        if (faceCount != null && faceCount > 0) {
            summary.append(" 人脸: ").append(faceCount);
        }
        if (fingerprintCount != null && fingerprintCount > 0) {
            summary.append(" 指纹: ").append(fingerprintCount);
        }
        if (irisCount != null && irisCount > 0) {
            summary.append(" 虹膜: ").append(irisCount);
        }
        if (palmprintCount != null && palmprintCount > 0) {
            summary.append(" 掌纹: ").append(palmprintCount);
        }
        if (voiceCount != null && voiceCount > 0) {
            summary.append(" 声纹: ").append(voiceCount);
        }

        return summary.toString();
    }
}