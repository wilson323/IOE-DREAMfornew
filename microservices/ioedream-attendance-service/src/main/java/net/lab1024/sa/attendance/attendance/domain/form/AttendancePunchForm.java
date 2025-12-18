package net.lab1024.sa.attendance.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考勤打卡表单
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 用于考勤处理模板方法的输入参数
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Schema(description = "考勤打卡表单")
public class AttendancePunchForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 用户ID（可选，如果提供则跳过识别步骤）
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 生物特征数据（用于识别）
     */
    @Schema(description = "生物特征数据", example = "base64_encoded_face_image")
    private String biometricData;

    /**
     * 打卡时间
     */
    @Schema(description = "打卡时间", example = "2025-01-30T08:00:00")
    private LocalDateTime punchTime;

    /**
     * 打卡类型（0-上班打卡，1-下班打卡）
     */
    @Schema(description = "打卡类型：0-上班打卡，1-下班打卡", example = "0")
    private Integer punchType;

    /**
     * 打卡位置（经度）
     */
    @Schema(description = "打卡位置（经度）", example = "116.397128")
    private BigDecimal longitude;

    /**
     * 打卡位置（纬度）
     */
    @Schema(description = "打卡位置（纬度）", example = "39.916527")
    private BigDecimal latitude;

    /**
     * 打卡地址
     */
    @Schema(description = "打卡地址", example = "北京市朝阳区")
    private String punchAddress;
}
