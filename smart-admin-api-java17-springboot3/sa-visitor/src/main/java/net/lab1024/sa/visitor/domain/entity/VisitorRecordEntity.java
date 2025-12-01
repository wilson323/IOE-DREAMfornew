package net.lab1024.sa.visitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 访客记录实体类
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseEntity，使用审计字段
 * - 使用jakarta包名
 * - 完整的字段注解和验证
 * - 日期时间格式化
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_record")
@Schema(description = "访客记录实体")

public class VisitorRecordEntity extends net.lab1024.sa.base.common.entity.BaseEntity {

    /**
     * 记录ID
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    /**
     * 预约ID
     */
    @TableField("reservation_id")
    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    /**
     * 访客姓名
     */
    @TableField("visitor_name")
    @NotBlank(message = "访客姓名不能为空")
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    /**
     * 访客手机号
     */
    @TableField("visitor_phone")
    @NotBlank(message = "访客手机号不能为空")
    @Schema(description = "访客手机号", example = "13800138000")
    private String visitorPhone;

    /**
     * 访问区域ID
     */
    @TableField("visit_area_id")
    @NotNull(message = "访问区域ID不能为空")
    @Schema(description = "访问区域ID", example = "1")
    private Long visitAreaId;

    /**
     * 访问区域名称
     */
    @TableField("visit_area_name")
    @NotBlank(message = "访问区域名称不能为空")
    @Schema(description = "访问区域名称", example = "办公楼A栋")
    private String visitAreaName;

    /**
     * 门禁设备ID
     */
    @TableField("access_device_id")
    @Schema(description = "门禁设备ID", example = "1001")
    private Long accessDeviceId;

    /**
     * 门禁设备名称
     */
    @TableField("access_device_name")
    @Schema(description = "门禁设备名称", example = "大门门禁")
    private String accessDeviceName;

    /**
     * 通行方式: 1-二维码, 2-人脸, 3-身份证, 4-人工登记
     */
    @TableField("access_method")
    @NotNull(message = "通行方式不能为空")
    @Schema(description = "通行方式", example = "1")
    private Integer accessMethod;

    /**
     * 通行结果: 0-成功, 1-失败
     */
    @TableField("access_result")
    @NotNull(message = "通行结果不能为空")
    @Schema(description = "通行结果", example = "0")
    private Integer accessResult;

    /**
     * 失败原因
     */
    @TableField("fail_reason")
    @Schema(description = "失败原因", example = "二维码已过期")
    private String failReason;

    /**
     * 通行时间
     */
    @TableField("access_time")
    @NotNull(message = "通行时间不能为空")
    
    @Schema(description = "通行时间", example = "2025-11-25 09:30:00")
    private LocalDateTime accessTime;

    /**
     * 现场照片路径
     */
    @TableField("photo_path")
    @Schema(description = "现场照片路径", example = "/upload/visitor/record/20251125/001.jpg")
    private String photoPath;

    /**
     * 体温测量值
     */
    @TableField("temperature")
    @Schema(description = "体温测量值", example = "36.5")
    private BigDecimal temperature;

    /**
     * 健康状态: 0-正常, 1-异常
     */
    @TableField("health_status")
    @Schema(description = "健康状态", example = "0")
    private Integer healthStatus;

    /**
     * 备注信息
     */
    @TableField("remarks")
    @Schema(description = "备注信息", example = "体温正常")
    private String remarks;

    // ========== 应用考勤服务验证的字段模式 ==========

    /**
     * 验证方式
     */
    @TableField("verification_method")
    @Schema(description = "验证方式", example = "FACE")
    private String verificationMethod;

    /**
     * 设备编码
     */
    @TableField("device_code")
    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    /**
     * 访问精确时间
     */
    @TableField("visit_date_time")
    @NotNull(message = "访问精确时间不能为空")
    @Schema(description = "访问精确时间", example = "2025-11-25 09:30:00")
    private LocalDateTime visitDateTime;

    /**
     * GPS纬度
     */
    @TableField("gps_latitude")
    @Schema(description = "GPS纬度", example = "39.9042")
    private Double gpsLatitude;

    /**
     * GPS经度
     */
    @TableField("gps_longitude")
    @Schema(description = "GPS经度", example = "116.4074")
    private Double gpsLongitude;

    /**
     * 终端标识
     */
    @TableField("terminal_id")
    @Schema(description = "终端标识", example = "TERM001")
    private String terminalId;

    /**
     * 操作员ID
     */
    @TableField("operator_id")
    @Schema(description = "操作员ID", example = "1001")
    private Long operatorId;

    /**
     * 操作员姓名
     */
    @TableField("operator_name")
    @Schema(description = "操作员姓名", example = "张管理员")
    private String operatorName;

    /**
     * 验证状态
     */
    @TableField("verification_status")
    @Schema(description = "验证状态", example = "VERIFIED")
    private String verificationStatus;

    /**
     * 通行方式枚举
     */
    public enum AccessMethod {
        QR_CODE(1, "二维码"),
        FACE_RECOGNITION(2, "人脸识别"),
        ID_CARD(3, "身份证"),
        MANUAL_REGISTRATION(4, "人工登记");

        private final Integer code;
        private final String description;

        AccessMethod(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AccessMethod fromCode(Integer code) {
            for (AccessMethod method : values()) {
                if (method.code.equals(code)) {
                    return method;
                }
            }
            return null;
        }
    }

    /**
     * 通行结果枚举
     */
    public enum AccessResult {
        SUCCESS(0, "成功"),
        FAILED(1, "失败");

        private final Integer code;
        private final String description;

        AccessResult(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AccessResult fromCode(Integer code) {
            for (AccessResult result : values()) {
                if (result.code.equals(code)) {
                    return result;
                }
            }
            return null;
        }
    }

    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        NORMAL(0, "正常"),
        ABNORMAL(1, "异常");

        private final Integer code;
        private final String description;

        HealthStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static HealthStatus fromCode(Integer code) {
            for (HealthStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 是否通行成功
     */
    public boolean isAccessSuccess() {
        return Integer.valueOf(0).equals(accessResult);
    }

    /**
     * 是否通行失败
     */
    public boolean isAccessFailed() {
        return Integer.valueOf(1).equals(accessResult);
    }

    /**
     * 获取通行方式描述
     */
    public String getAccessMethodDescription() {
        AccessMethod method = AccessMethod.fromCode(accessMethod);
        return method != null ? method.getDescription() : "未知";
    }

    /**
     * 获取通行结果描述
     */
    public String getAccessResultDescription() {
        AccessResult result = AccessResult.fromCode(accessResult);
        return result != null ? result.getDescription() : "未知";
    }

    /**
     * 获取健康状态描述
     */
    public String getHealthStatusDescription() {
        HealthStatus status = HealthStatus.fromCode(healthStatus);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 是否体温异常
     */
    public boolean hasTemperatureAbnormal() {
        if (temperature == null) {
            return false;
        }
        // 体温超过37.3°C认为异常
        return temperature.compareTo(new BigDecimal("37.3")) > 0;
    }

    /**
     * 是否健康状态异常
     */
    public boolean hasHealthAbnormal() {
        return Integer.valueOf(1).equals(healthStatus);
    }

    /**
     * 是否有预约
     */
    public boolean hasReservation() {
        return reservationId != null && reservationId > 0;
    }
}