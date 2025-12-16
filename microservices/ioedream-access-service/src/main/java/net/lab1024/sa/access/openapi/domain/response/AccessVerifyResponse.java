package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁通行验证响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁通行验证响应")
public class AccessVerifyResponse {

    @Schema(description = "是否允许通行", example = "true")
    private Boolean allowAccess;

    @Schema(description = "通行状态码", example = "200")
    private String statusCode;

    @Schema(description = "通行状态消息", example = "验证成功，允许通行")
    private String statusMessage;

    @Schema(description = "通行记录ID", example = "100001")
    private Long recordId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "设备ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主门禁")
    private String deviceName;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "一楼大厅")
    private String areaName;

    @Schema(description = "通行时间", example = "2025-12-16T15:30:00")
    private LocalDateTime accessTime;

    @Schema(description = "通行方向", example = "in")
    private String direction;

    @Schema(description = "验证方式", example = "face")
    private String verifyMethod;

    @Schema(description = "验证结果详情", example = "人脸识别匹配度：98.5%")
    private String verifyResultDetail;

    @Schema(description = "体温数据", example = "36.5")
    private Double temperature;

    @Schema(description = "体温状态", example = "normal", allowableValues = {"normal", "fever", "low"})
    private String temperatureStatus;

    @Schema(description = "是否佩戴口罩", example = "true")
    private Boolean wearingMask;

    @Schema(description = "活体检测结果", example = "true")
    private Boolean livenessCheckResult;

    @Schema(description = "通行权限级别", example = "normal")
    private String permissionLevel;

    @Schema(description = "剩余通行次数", example = "95")
    private Integer remainingAccessCount;

    @Schema(description = "权限过期时间", example = "2025-12-31T23:59:59")
    private LocalDateTime permissionExpireTime;

    @Schema(description = "图片URL", example = "https://example.com/access_photo.jpg")
    private String photoUrl;

    @Schema(description = "语音提示", example = "欢迎进入")
    private String voicePrompt;

    @Schema(description = "警告信息", example = "")
    private String warningMessage;

    @Schema(description = "扩展信息", example = "{\"key1\":\"value1\"}")
    private String extendedInfo;

    @Schema(description = "用户权限列表")
    private List<PermissionInfo> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限信息")
    public static class PermissionInfo {

        @Schema(description = "设备ID", example = "ACCESS_001")
        private String deviceId;

        @Schema(description = "设备名称", example = "主门禁")
        private String deviceName;

        @Schema(description = "权限类型", example = "permanent")
        private String permissionType;

        @Schema(description = "权限级别", example = "normal")
        private String permissionLevel;

        @Schema(description = "允许通行时间", example = "全天")
        private String allowedTime;

        @Schema(description = "是否在有效期内", example = "true")
        private Boolean isValid;
    }
}