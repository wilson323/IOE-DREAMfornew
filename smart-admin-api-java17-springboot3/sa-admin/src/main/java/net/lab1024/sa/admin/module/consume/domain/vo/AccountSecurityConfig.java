package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 账户安全配置VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户安全配置")
public class AccountSecurityConfig {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "是否开启双因子认证")
    private Boolean enableTwoFactorAuth;

    @Schema(description = "是否开启异常检测")
    private Boolean enableAbnormalDetection;

    @Schema(description = "是否开启设备绑定")
    private Boolean enableDeviceBinding;

    @Schema(description = "是否开启地理位置验证")
    private Boolean enableLocationVerification;

    @Schema(description = "最大登录失败次数")
    private Integer maxLoginFailures;

    @Schema(description = "账户锁定时间（分钟）")
    private Integer lockoutDuration;

    @Schema(description = "配置更新时间")
    private LocalDateTime updateTime;

    // 手动添加的getter/setter方法 (Lombok失效备用)


    public Boolean isEnableTwoFactorAuth() {
        return enableTwoFactorAuth;
    }


    public Boolean isEnableAbnormalDetection() {
        return enableAbnormalDetection;
    }


    public Boolean isEnableDeviceBinding() {
        return enableDeviceBinding;
    }


    public Boolean isEnableLocationVerification() {
        return enableLocationVerification;
    }








}
