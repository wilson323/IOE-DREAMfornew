package net.lab1024.sa.attendance.domain.form;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 考勤记录创建表单
 * <p>
 * 用于设备协议推送考勤记录
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 使用@Schema注解描述字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "考勤记录创建表单")
public class AttendanceRecordAddForm {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编号
     */
    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 班次ID（可选，用于跨天班次计算考勤日期）
     */
    @Schema(description = "班次ID（可选，用于跨天班次计算考勤日期）", example = "1")
    private Long shiftId;

    /**
     * 打卡时间（时间戳或LocalDateTime）
     */
    @Schema(description = "打卡时间（时间戳或LocalDateTime）", example = "2025-01-30T08:00:00")
    private Object punchTime;

    /**
     * 打卡类型
     * <p>
     * 0-上班打卡
     * 1-下班打卡
     * </p>
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

    /**
     * 备注
     */
    @Schema(description = "备注", example = "设备自动推送")
    private String remark;
}



