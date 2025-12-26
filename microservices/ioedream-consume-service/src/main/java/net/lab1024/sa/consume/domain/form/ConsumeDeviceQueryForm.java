package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import net.lab1024.sa.common.domain.form.PageForm;

/**
 * 消费设备查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费设备查询表单")
public class ConsumeDeviceQueryForm extends PageForm {

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编码", example = "CONSUME_DEVICE_001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "餐厅POS机1号")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "设备状态", example = "1")
    private Integer deviceStatus;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    @Schema(description = "设备位置", example = "餐厅一楼")
    private String deviceLocation;

    @Schema(description = "设备型号", example = "POS-3000")
    private String deviceModel;

    @Schema(description = "设备厂商", example = "智慧设备科技")
    private String deviceManufacturer;

    @Schema(description = "是否支持离线", example = "true")
    private Boolean supportOffline;

    @Schema(description = "安装区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "是否在线", example = "true")
    private Boolean isOnline;

    @Schema(description = "设备健康状态", example = "healthy")
    private String healthStatus;

    @Schema(description = "关键词搜索", example = "POS机")
    private String keyword;

    @Schema(description = "开始创建时间")
    private String createTimeBegin;

    @Schema(description = "结束创建时间")
    private String createTimeEnd;

    @Schema(description = "开始更新时间")
    private String updateTimeBegin;

    @Schema(description = "结束更新时间")
    private String updateTimeEnd;
}