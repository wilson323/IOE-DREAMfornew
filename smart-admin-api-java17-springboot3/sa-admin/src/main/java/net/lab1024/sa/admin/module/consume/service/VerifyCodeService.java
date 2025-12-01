/*
 * 验证码服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-20
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;

/**
 * 验证码服务接口
 * 提供验证码生成、发送、验证等功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/20
 */
public interface VerifyCodeService {

    /**
     * 生成并发送验证码
     *
     * @param personId     人员ID
     * @param businessType 业务类型（如：PAYMENT_PASSWORD_RESET）
     * @return 是否发送成功
     */
    boolean generateAndSendCode(@NotNull Long personId, @NotNull String businessType);

    /**
     * 验证验证码
     *
     * @param personId     人员ID
     * @param verifyCode   验证码
     * @param businessType 业务类型
     * @return 是否验证成功
     */
    boolean verifyCode(@NotNull Long personId, @NotNull String verifyCode, @NotNull String businessType);

    /**
     * 检查验证码是否存在且未过期
     *
     * @param personId     人员ID
     * @param businessType 业务类型
     * @return 是否存在
     */
    boolean hasCode(@NotNull Long personId, @NotNull String businessType);

    /**
     * 删除验证码（验证成功后调用）
     *
     * @param personId     人员ID
     * @param businessType 业务类型
     */
    void deleteCode(@NotNull Long personId, @NotNull String businessType);
}
