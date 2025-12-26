package net.lab1024.sa.common.entity.consume;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.NoArgsConstructor;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费设备实体类
 * <p>
 * 管理消费终端设备信息，包括POS机、自助充值机等
 * 支持设备状态监控、配置管理和通信管理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - POS终端管理
 * - 自助充值机管理
 * - 设备状态监控
 * - 设备配置管理
 * - 设备通信管理
 * - 设备统计报表
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@TableName("t_consume_device")
@Schema(description = "消费设备实体")
public class ConsumeDeviceEntity extends BaseEntity {

    /**
     * 设备ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备编码（业务唯一）
     */
    @Schema(description = "设备编码")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 设备类型（1-POS机 2-自助充值机 3-自助消费机 4-充值终端）
     */
    @Schema(description = "设备类型")
    private Integer deviceType;

    /**
     * 设备类型名称
     */
    @Schema(description = "设备类型名称")
    private String deviceTypeName;

    /**
     * 设备型号
     */
    @Schema(description = "设备型号")
    private String deviceModel;

    /**
     * 设备厂商
     */
    @Schema(description = "设备厂商")
    private String deviceManufacturer;

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号")
    private String deviceSerialNumber;

    /**
     * 设备状态（1-正常 2-故障 3-离线 4-维护中 5-已停用）
     */
    @Schema(description = "设备状态")
    private Integer deviceStatus;

    /**
     * 设备状态描述
     */
    @Schema(description = "设备状态描述")
    private String deviceStatusDesc;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String areaName;

    /**
     * 安装位置
     */
    @Schema(description = "安装位置")
    private String installationLocation;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;

    /**
     * MAC地址
     */
    @Schema(description = "MAC地址")
    private String macAddress;

    /**
     * 端口号
     */
    @Schema(description = "端口号")
    private Integer port;

    /**
     * 通信协议（TCP-UDP-HTTP-HTTPS-MQTT）
     */
    @Schema(description = "通信协议")
    private String communicationProtocol;

    /**
     * 最后通信时间
     */
    @Schema(description = "最后通信时间")
    private LocalDateTime lastCommunicationTime;

    /**
     * 最后心跳时间
     */
    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 设备版本号
     */
    @Schema(description = "设备版本号")
    private String deviceVersion;

    /**
     * 固件版本号
     */
    @Schema(description = "固件版本号")
    private String firmwareVersion;

    /**
     * 是否在线（0-离线 1-在线）
     */
    @Schema(description = "是否在线")
    private Integer isOnline;

    /**
     * 经度
     */
    @Schema(description = "经度")
    private String longitude;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private String latitude;

    /**
     * 责任人ID
     */
    @Schema(description = "责任人ID")
    private Long responsiblePersonId;

    /**
     * 责任人姓名
     */
    @Schema(description = "责任人姓名")
    private String responsiblePersonName;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 采购日期
     */
    @Schema(description = "采购日期")
    private LocalDateTime purchaseDate;

    /**
     * 保修到期日期
     */
    @Schema(description = "保修到期日期")
    private LocalDateTime warrantyExpiryDate;

    /**
     * 设备备注
     */
    @Schema(description = "设备备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
