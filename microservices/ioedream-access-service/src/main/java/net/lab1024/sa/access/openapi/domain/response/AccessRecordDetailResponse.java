package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁记录详情响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁记录详情响应")
public class AccessRecordDetailResponse {

    @Schema(description = "记录ID", example = "100001")
    private Long recordId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "手机号", example = "13812345678")
    private String phone;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "设备ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主门禁")
    private String deviceName;

    @Schema(description = "设备类型", example = "access", allowableValues = {"access", "turnstile", "gate"})
    private String deviceType;

    @Schema(description = "设备型号", example = "MODEL-A100")
    private String deviceModel;

    @Schema(description = "设备厂商", example = "海康威视")
    private String deviceManufacturer;

    @Schema(description = "设备IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "一楼大厅")
    private String areaName;

    @Schema(description = "区域类型", example = "entrance", allowableValues = {"entrance", "exit", "office", "factory"})
    private String areaType;

    @Schema(description = "通行时间", example = "2025-12-16T15:30:00")
    private LocalDateTime accessTime;

    @Schema(description = "通行状态", example = "1", allowableValues = {"0", "1"})
    private Integer accessStatus;

    @Schema(description = "通行状态名称", example = "成功")
    private String accessStatusName;

    @Schema(description = "通行方向", example = "in", allowableValues = {"in", "out"})
    private String direction;

    @Schema(description = "通行方向名称", example = "进入")
    private String directionName;

    @Schema(description = "验证方式", example = "face", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
    private String verifyType;

    @Schema(description = "验证方式名称", example = "人脸识别")
    private String verifyTypeName;

    @Schema(description = "体温数据", example = "36.5")
    private Double temperature;

    @Schema(description = "体温状态", example = "normal", allowableValues = {"normal", "fever", "low"})
    private String temperatureStatus;

    @Schema(description = "是否佩戴口罩", example = "true")
    private Boolean wearingMask;

    @Schema(description = "口罩检测置信度", example = "95.2")
    private Double maskConfidence;

    @Schema(description = "活体检测结果", example = "true")
    private Boolean livenessCheckResult;

    @Schema(description = "活体检测置信度", example = "98.7")
    private Double livenessConfidence;

    @Schema(description = "通行照片URL", example = "https://example.com/access_photo.jpg")
    private String photoUrl;

    @Schema(description = "通行照片缩略图URL", example = "https://example.com/access_photo_thumb.jpg")
    private String photoThumbUrl;

    @Schema(description = "卡号", example = "1234567890")
    private String cardNumber;

    @Schema(description = "指纹ID", example = "FINGER_001")
    private String fingerprintId;

    @Schema(description = "人脸ID", example = "FACE_001")
    private String faceId;

    @Schema(description = "处理耗时（毫秒）", example = "500")
    private Long processTime;

    @Schema(description = "匹配得分", example = "98.5")
    private Double matchScore;

    @Schema(description = "匹配阈值", example = "80.0")
    private Double matchThreshold;

    @Schema(description = "异常原因", example = "")
    private String abnormalReason;

    @Schema(description = "是否异常记录", example = "false")
    private Boolean isAbnormal;

    @Schema(description = "通行权限级别", example = "normal")
    private String permissionLevel;

    @Schema(description = "权限状态", example = "valid", allowableValues = {"valid", "expired", "revoked"})
    private String permissionStatus;

    @Schema(description = "权限过期时间", example = "2025-12-31T23:59:59")
    private LocalDateTime permissionExpireTime;

    @Schema(description = "验证详情列表")
    private List<VerifyDetail> verifyDetails;

    @Schema(description = "设备状态信息")
    private Map<String, Object> deviceStatusInfo;

    @Schema(description = "操作日志")
    private List<OperationLog> operationLogs;

    @Schema(description = "备注", example = "")
    private String remark;

    @Schema(description = "创建时间", example = "2025-12-16T15:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-16T15:30:00")
    private LocalDateTime updateTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "验证详情")
    public static class VerifyDetail {

        @Schema(description = "验证步骤", example = "1")
        private Integer step;

        @Schema(description = "验证类型", example = "face")
        private String verifyType;

        @Schema(description = "验证类型名称", example = "人脸识别")
        private String verifyTypeName;

        @Schema(description = "验证结果", example = "success", allowableValues = {"success", "fail", "skip"})
        private String verifyResult;

        @Schema(description = "验证结果名称", example = "成功")
        private String verifyResultName;

        @Schema(description = "匹配得分", example = "98.5")
        private Double matchScore;

        @Schema(description = "耗时（毫秒）", example = "200")
        private Long duration;

        @Schema(description = "详情信息", example = "人脸特征匹配成功")
        private String detail;

        @Schema(description = "图片URL", example = "https://example.com/verify_photo.jpg")
        private String photoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "操作日志")
    public static class OperationLog {

        @Schema(description = "操作时间", example = "2025-12-16T15:30:00")
        private LocalDateTime operationTime;

        @Schema(description = "操作类型", example = "verify", allowableValues = {"verify", "open", "close", "alarm"})
        private String operationType;

        @Schema(description = "操作类型名称", example = "验证")
        private String operationTypeName;

        @Schema(description = "操作结果", example = "success", allowableValues = {"success", "fail", "pending"})
        private String operationResult;

        @Schema(description = "操作结果名称", example = "成功")
        private String operationResultName;

        @Schema(description = "操作描述", example = "用户验证通过")
        private String description;

        @Schema(description = "操作人ID", example = "1001")
        private Long operatorId;

        @Schema(description = "操作人姓名", example = "系统")
        private String operatorName;
    }
}
