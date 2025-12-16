package net.lab1024.sa.access.service;

import java.util.List;

import lombok.Data;

/**
 * 高级门禁控制服务接口
 * <p>
 * 提供高级门禁控制相关业务功能，包括：
 * - 智能风险评估
 * - 动态权限调整
 * - 异常行为检测
 * - 访问模式分析
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 清晰的方法注释
 * - 统一的数据传输对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
public interface AdvancedAccessControlService {

    /**
     * 执行门禁控制检查
     * <p>
     * 基础的访问控制检查，包括权限验证和风险评估
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param verificationData 验证数据
     * @param accessType 访问类型
     * @return 控制结果
     */
    AccessControlResult performAccessControlCheck(
            Long userId,
            Long deviceId,
            Long areaId,
            String verificationData,
            String accessType);

    /**
     * 执行高级智能访问控制
     * <p>
     * 增强的访问控制，包括模式分析、动态权限调整和异常检测
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param verificationData 验证数据
     * @param accessType 访问类型
     * @return 控制结果
     */
    AccessControlResult performIntelligentAccessControl(
            Long userId,
            Long deviceId,
            Long areaId,
            String verificationData,
            String accessType);

    /**
     * 门禁控制结果
     */
    @Data
    class AccessControlResult {
        /**
         * 是否允许访问
         */
        private Boolean allowed;

        /**
         * 拒绝原因
         */
        private String denyReason;

        /**
         * 是否需要二次验证
         */
        private Boolean requireSecondaryVerification;

        /**
         * 访问级别
         * DENIED - 拒绝
         * RESTRICTED - 受限
         * NORMAL - 正常
         * ENHANCED - 增强
         */
        private String accessLevel;

        /**
         * 额外安全措施
         */
        private List<String> additionalSecurityMeasures;

        /**
         * 是否检测到异常行为
         */
        private Boolean anomalousBehavior;

        /**
         * 异常行为描述
         */
        private String anomalyDescription;

        /**
         * 风险评分
         */
        private Integer riskScore;

        /**
         * 风险等级
         */
        private String riskLevel;

        /**
         * 判断是否允许访问
         *
         * @return 是否允许访问
         */
        public boolean isAllowed() {
            return allowed != null && allowed;
        }
    }
}

