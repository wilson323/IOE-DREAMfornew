package net.lab1024.sa.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 考勤事件数据传输对象
 * <p>
 * 用于RabbitMQ消息传递
 * 包含考勤事件的详细信息
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "考勤事件")
public class AttendanceEventVO {

    /**
     * 事件ID（唯一标识）
     */
    @Schema(description = "事件ID", example = "evt_20250130_001")
    private String eventId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "1号考勤机")
    private String deviceName;

    /**
     * 事件类型
     * PUNCH_SUCCESS: 打卡成功
     * PUNCH_FAILED: 打卡失败
     * BIOMETRIC_FAILED: 生物识别失败
     * DEVICE_OFFLINE: 设备离线
     */
    @Schema(description = "事件类型", example = "PUNCH_SUCCESS")
    private String eventType;

    /**
     * 事件时间
     */
    @Schema(description = "事件时间", example = "2025-01-30T08:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    /**
     * 打卡类型（0-上班，1-下班）
     */
    @Schema(description = "打卡类型：0-上班，1-下班", example = "0")
    private Integer punchType;

    /**
     * 打卡地址
     */
    @Schema(description = "打卡地址", example = "北京市朝阳区")
    private String punchAddress;

    /**
     * 错误信息（事件失败时）
     */
    @Schema(description = "错误信息", example = "生物识别失败")
    private String errorMessage;

    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据")
    private Object extendedData;
}
