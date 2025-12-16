package net.lab1024.sa.access.service;

import lombok.Data;

/**
 * 高级门禁控制服务接口
 * <p>
 * 提供高级门禁控制相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AdvancedAccessControlService {

    /**
     * 执行门禁控制检查
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
         */
        private String accessLevel;

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

