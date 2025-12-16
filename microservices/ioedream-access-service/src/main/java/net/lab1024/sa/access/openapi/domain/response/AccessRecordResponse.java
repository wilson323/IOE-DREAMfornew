package net.lab1024.sa.access.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 门禁记录响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁记录响应")
public class AccessRecordResponse {

    @Schema(description = "记录ID", example = "100001")
    private Long recordId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

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

    @Schema(description = "活体检测结果", example = "true")
    private Boolean livenessCheckResult;

    @Schema(description = "通行照片URL", example = "https://example.com/access_photo.jpg")
    private String photoUrl;

    @Schema(description = "卡号", example = "1234567890")
    private String cardNumber;

    @Schema(description = "设备IP", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "处理耗时（毫秒）", example = "500")
    private Long processTime;

    @Schema(description = "匹配度", example = "98.5")
    private Double matchScore;

    @Schema(description = "异常原因", example = "")
    private String abnormalReason;

    @Schema(description = "是否异常记录", example = "false")
    private Boolean isAbnormal;

    @Schema(description = "备注", example = "")
    private String remark;
}