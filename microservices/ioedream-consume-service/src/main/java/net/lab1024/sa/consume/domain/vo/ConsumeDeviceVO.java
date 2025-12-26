package net.lab1024.sa.consume.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 消费设备信息VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费设备信息")
public class ConsumeDeviceVO {

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编码", example = "CONSUME_DEVICE_001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "餐厅POS机1号")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "POS机")
    private String deviceTypeName;

    @Schema(description = "设备位置", example = "餐厅一楼")
    private String deviceLocation;

    @Schema(description = "设备状态", example = "1")
    private Integer deviceStatus;

    @Schema(description = "设备状态名称", example = "在线")
    private String deviceStatusName;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    @Schema(description = "设备型号", example = "POS-3000")
    private String deviceModel;

    @Schema(description = "设备厂商", example = "智慧设备科技")
    private String deviceManufacturer;

    @Schema(description = "固件版本", example = "V1.2.3")
    private String firmwareVersion;

    @Schema(description = "是否支持离线", example = "true")
    private Boolean supportOffline;

    @Schema(description = "设备描述", example = "用于餐厅消费支付的POS终端")
    private String deviceDescription;

    @Schema(description = "业务属性")
    private String businessAttributes;

    @Schema(description = "最后通信时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastCommunicationTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "设备运行良好")
    private String remark;

    @Schema(description = "今日交易笔数", example = "156")
    private Integer todayTransactionCount;

    @Schema(description = "今日交易金额", example = "4580.50")
    private BigDecimal todayTransactionAmount;

    @Schema(description = "设备在线状态", example = "online")
    private String onlineStatus;

    @Schema(description = "设备健康状态", example = "healthy")
    private String healthStatus;

    @Schema(description = "安装区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "安装区域名称", example = "餐厅区域")
    private String areaName;
}