package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * 移动端登录请求
 * <p>
 * 封装移动端用户登录的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端登录请求")
public class MobileLoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "1234")
    private String captchaCode;

    /**
     * 验证码Key
     */
    @Schema(description = "验证码Key", example = "captcha_key_123")
    private String captchaKey;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息")
    private DeviceInfo deviceInfo;

    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private Map<String, Object> extendParams;

    /**
     * 设备信息
     */
    @Data
    @Schema(description = "设备信息")
    public static class DeviceInfo {

        /**
         * 设备ID
         */
        @Schema(description = "设备ID", example = "device_123")
        private String deviceId;

        /**
         * 设备类型
         */
        @Schema(description = "设备类型", example = "ANDROID", allowableValues = {"ANDROID", "IOS", "H5"})
        private String deviceType;

        /**
         * 设备型号
         */
        @Schema(description = "设备型号", example = "iPhone 14")
        private String deviceModel;

        /**
         * 操作系统版本
         */
        @Schema(description = "操作系统版本", example = "iOS 16.0")
        private String osVersion;

        /**
         * 应用版本
         */
        @Schema(description = "应用版本", example = "1.0.0")
        private String appVersion;

        /**
         * 推送Token
         */
        @Schema(description = "推送Token", example = "push_token_123")
        private String pushToken;

        /**
         * IP地址
         */
        @Schema(description = "IP地址", example = "192.168.1.100")
        private String ipAddress;

        /**
         * 地理位置
         */
        @Schema(description = "地理位置")
        private Location location;

        /**
         * 地理位置
         */
        @Data
        @Schema(description = "地理位置")
        public static class Location {

            /**
             * 纬度
             */
            @Schema(description = "纬度", example = "39.9042")
            private Double latitude;

            /**
             * 经度
             */
            @Schema(description = "经度", example = "116.4074")
            private Double longitude;

            /**
             * 地址
             */
            @Schema(description = "地址", example = "北京市朝阳区")
            private String address;

            /**
             * 精度
             */
            @Schema(description = "精度", example = "10.5")
            private Double accuracy;
        }
    }
}
