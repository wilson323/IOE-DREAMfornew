package net.lab1024.sa.admin.module.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁读头实体类
 * <p>
 * 基于设备ID和门ID的读头表，实现门禁读卡器的管理
 * 严格遵循扩展表架构设计，避免重复建设，基于现有设备管理增强和完善
 *
 * 关联架构：SmartDeviceEntity + SmartAccessDoorEntity + SmartAccessReaderEntity
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_access_reader")
public class SmartAccessReaderEntity extends BaseEntity {

    /**
     * 读头ID（主键）
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long readerId;

    /**
     * 设备ID（关联SmartDeviceEntity.deviceId）
     */
    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    /**
     * 门ID（关联SmartAccessDoorEntity.doorId）
     */
    @NotNull(message = "门ID不能为空")
    @TableField("door_id")
    private Long doorId;

    /**
     * 读头编号
     */
    @TableField("reader_number")
    private String readerNumber;

    /**
     * 读头名称
     */
    @TableField("reader_name")
    private String readerName;

    /**
     * 读头类型 (READ-读卡器, WRITE-写卡器, READ_WRITE-读写器, BIOMETRIC-生物识别)
     */
    @TableField("reader_type")
    private String readerType;

    /**
     * 读头品牌 (HID-海德, LEGIC-莱格斯, MIFARE-飞利浦, SAM-三星, WIEGAND-韦根)
     */
    @TableField("reader_brand")
    private String readerBrand;

    /**
     * 读头型号
     */
    @TableField("reader_model")
    private String readerModel;

    /**
     * 读头通信协议 (WIEGAND-韦根, OSDP-开放式设备供应协议, TCP-传输控制协议, UDP-用户数据报协议)
     */
    @TableField("communication_protocol")
    private String communicationProtocol;

    /**
     * 读头状态 (ONLINE-在线, OFFLINE-离线, FAULT-故障, MAINTENANCE-维护)
     */
    @TableField("reader_status")
    private String readerStatus;

    /**
     * 支持的卡类型 (JSON格式: [\"ID_CARD\", \"IC_CARD\", \"NFC_CARD\", \"QR_CODE\", \"BARCODE\"]）
     */
    @TableField("supported_card_types")
    private String supportedCardTypes;

    /**
     * 支持的生物特征类型 (JSON格式: [\"FINGERPRINT\", \"FACE\", \"IRIS\", \"VOICE\", \"PALM\"])
     */
    @TableField("supported_biometric_types")
    private String supportedBiometricTypes;

    /**
     * 读头功能配置 (JSON格式: {readCard: true, writeCard: false, verifyBiometric: true})
     */
    @TableField("function_config")
    private String functionConfig;

    /**
     * 读头IP地址
     */
    @TableField("reader_ip")
    private String readerIp;

    /**
     * 读头端口
     */
    @TableField("reader_port")
    private Integer readerPort;

    /**
     * 读头地址或ID
     */
    @TableField("reader_address")
    private String readerAddress;

    /**
     * 读头密钥
     */
    @TableField("reader_key")
    private String readerKey;

    /**
     * 波特率
     */
    @TableField("baud_rate")
    private Integer baudRate;

    /**
     * 数据位 (7-数据位, 8-数据位)
     */
    @TableField("data_bits")
    private Integer dataBits;

    /**
     * 停止位 (1-停止位, 2-停止位)
     */
    @TableField("stop_bits")
    private Integer stopBits;

    /**
     * 校验位 (NONE-无校验, ODD-奇校验, EVEN-偶校验)
     */
    @TableField("parity_bit")
    private String parityBit;

    /**
     * 读卡距离(厘米)
     */
    @TableField("reading_distance")
    private Integer readingDistance;

    /**
     * 读卡响应时间(毫秒)
     */
    @TableField("reading_response_time")
    private Integer readingResponseTime;

    /**
     * 多卡检测 (0-不支持, 1-支持)
     */
    @TableField("multi_card_detection")
    private Integer multiCardDetection;

    /**
     * 防冲突功能 (0-不支持, 1-支持)
     */
    @TableField("anti_collision")
    private Integer antiCollision;

    /**
     * 读头安装位置
     */
    @TableField("installation_location")
    private String installationLocation;

    /**
     * 读头安装高度(厘米)
     */
    @TableField("installation_height")
    private Integer installationHeight;

    /**
     * 读头角度
     */
    @TableField("installation_angle")
    private Integer installationAngle;

    /**
     * 读头坐标信息 (JSON格式: {x: 100, y: 200, z: 50})
     */
    @TableField("reader_coordinates")
    private String readerCoordinates;

    /**
     * LED指示灯配置 (JSON格式: {powerLed: true, statusLed: true, buzzer: true})
     */
    @TableField("led_config")
    private String ledConfig;

    /**
     * 蜂鸣器配置 (0-静音, 1-短鸣, 2-长鸣, 3-连续鸣)
     */
    @TableField("buzzer_config")
    private Integer buzzerConfig;

    /**
     * 读头固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * 读头序列号
     */
    @TableField("serial_number")
    private String serialNumber;

    /**
     * 最后读卡时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_read_time")
    private LocalDateTime lastReadTime;

    /**
     * 今日读卡次数
     */
    @TableField("today_read_count")
    private Integer todayReadCount;

    /**
     * 本月读卡次数
     */
    @TableField("month_read_count")
    private Integer monthReadCount;

    /**
     * 异常读卡次数
     */
    @TableField("abnormal_read_count")
    private Integer abnormalReadCount;

    /**
     * 读头配置参数 (JSON格式)
     */
    @TableField("reader_config")
    private String readerConfig;

    /**
     * 读头维护状态 (NORMAL-正常, MAINTENANCE-维护, MALFUNCTION-故障)
     */
    @TableField("maintenance_status")
    private String maintenanceStatus;

    /**
     * 扩展配置1
     */
    @TableField("ext_config1")
    private String extConfig1;

    /**
     * 扩展配置2
     */
    @TableField("ext_config2")
    private String extConfig2;

    /**
     * 扩展配置3
     */
    @TableField("ext_config3")
    private String extConfig3;

    // Getter和Setter方法
    public Long getReaderId() {
        return readerId;
    }

    public void setReaderId(Long readerId) {
        this.readerId = readerId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getDoorId() {
        return doorId;
    }

    public void setDoorId(Long doorId) {
        this.doorId = doorId;
    }
}