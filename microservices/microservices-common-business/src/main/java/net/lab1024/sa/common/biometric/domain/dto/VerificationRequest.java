package net.lab1024.sa.common.biometric.domain.dto;

import java.time.LocalDateTime;

/**
 * 通用验证请求接口
 * <p>
 * 定义验证请求的通用字段，供认证策略使用
 * </p>
 * <p>
 * 各业务服务可以创建自己的验证请求DTO实现此接口
 * 例如：AccessVerificationRequest实现此接口，添加门禁特定字段
 * </p>
 * <p>
 * ⚠️ 重要说明：不是进行人员识别
 * </p>
 * <p>
 * 设备端已完成人员识别，软件端接收的是用户ID（pin），只记录认证方式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VerificationRequest {

    /**
     * 获取用户ID（设备端已识别）
     *
     * @return 用户ID
     */
    Long getUserId();

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    Long getDeviceId();

    /**
     * 获取区域ID
     *
     * @return 区域ID
     */
    Long getAreaId();

    /**
     * 获取验证方式代码
     *
     * @return 验证方式代码（对应VerifyTypeEnum.code）
     */
    Integer getVerifyType();

    /**
     * 获取验证时间
     *
     * @return 验证时间
     */
    LocalDateTime getVerifyTime();

    /**
     * 获取设备序列号
     *
     * @return 设备序列号
     */
    String getSerialNumber();
}
