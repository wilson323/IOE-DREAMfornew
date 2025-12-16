package net.lab1024.sa.attendance.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 补卡申请请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "补卡申请请求")
public class SupplementApplicationRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001", required = true)
    private Long userId;

    @NotBlank(message = "补卡日期不能为空")
    @Schema(description = "补卡日期", example = "2025-12-16", required = true)
    private String supplementDate;

    @NotBlank(message = "打卡类型不能为空")
    @Schema(description = "打卡类型", example = "on", required = true,
            allowableValues = {"on", "off", "break_start", "break_end"})
    private String clockType;

    @Schema(description = "补卡时间", example = "2025-12-16T09:00:00")
    private LocalDateTime supplementTime;

    @NotBlank(message = "申请原因不能为空")
    @Schema(description = "申请原因", example = "出差", required = true)
    private String reason;

    @Schema(description = "详细说明", example = "因出差到客户现场，忘记打卡")
    private String description;

    @Schema(description = "证明文件URL", example = "https://example.com/proof.pdf")
    private String proofFileUrl;

    @Schema(description = "证明文件名称", example = "出差证明.pdf")
    private String proofFileName;

    @Schema(description = "位置信息", example = "客户现场")
    private String location;

    @Schema(description = "经度", example = "116.397128")
    private Double longitude;

    @Schema(description = "纬度", example = "39.916527")
    private Double latitude;

    @Schema(description = "证明人姓名", example = "张经理")
    private String witnessName;

    @Schema(description = "证明人联系方式", example = "13800138000")
    private String witnessContact;

    @Schema(description = "紧急程度", example = "normal", allowableValues = {"low", "normal", "high", "urgent"})
    private String urgencyLevel = "normal";

    @Schema(description = "期望处理时间", example = "2025-12-17T18:00:00")
    private LocalDateTime expectedProcessTime;

    @Schema(description = "是否需要人脸验证", example = "true")
    private Boolean requireFaceVerify = true;

    @Schema(description = "人脸特征数据", example = "base64_encoded_face_data")
    private String faceData;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}