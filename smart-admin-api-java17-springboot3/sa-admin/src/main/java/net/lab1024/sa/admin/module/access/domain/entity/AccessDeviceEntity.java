package net.lab1024.sa.admin.module.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;

/**
 * 门禁设备实体类
 *
 * 继承SmartDeviceEntity，包含门禁特有字段
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_device")
public class AccessDeviceEntity extends SmartDeviceEntity {

    /**
     * 门禁设备类型 (FINGERPRINT-指纹机, FACE-人脸识别, CARD-刷卡机, PASSWORD-密码机)
     */
    private String accessDeviceType;

    /**
     * 开门方式 (BIOMETRIC-生物识别, CARD-刷卡, PASSWORD-密码, QR-二维码, REMOTE-远程)
     */
    private String openMethod;

    /**
     * 识别时间阈值(秒)
     */
    private Integer recognitionThreshold;

    /**
     * 是否支持活体检测 (0-不支持, 1-支持)
     */
    private Integer liveDetectionEnabled;

    /**
     * 防拆报警启用 (0-禁用, 1-启用)
     */
    private Integer tamperAlarmEnabled;

    /**
     * 开门延迟时间(秒)
     */
    private Integer openDelay;

    /**
     * 关门延迟时间(秒)
     */
    private Integer closeDelay;

    /**
     * 最大用户容量
     */
    private Integer maxUserCapacity;

    /**
     * 当前用户数量
     */
    private Integer currentUserCount;

    /**
     * 门禁区域ID
     */
    private Long accessAreaId;

    /**
     * 门锁类型 (ELECTRIC-电控锁, ELECTROMAGNETIC-电磁锁, ELECTRIC_STRIKE-电插锁)
     */
    private String lockType;

    /**
     * 锁状态 (LOCKED-锁定, UNLOCKED-解锁, FAULT-故障)
     */
    private String lockStatus;

    /**
     * 门磁状态 (CLOSE-关闭, OPEN-打开, FAULT-故障)
     */
    private String doorSensorStatus;

    /**
     * 是否支持远程开门 (0-不支持, 1-支持)
     */
    private Integer remoteOpenEnabled;

    /**
     * 是否支持胁迫报警 (0-不支持, 1-支持)
     */
    private Integer duressAlarmEnabled;

    /**
     * 是否支持多卡开门 (0-不支持, 1-支持)
     */
    private Integer multiCardOpenEnabled;

    /**
     * 多卡开门数量
     */
    private Integer multiCardCount;

    /**
     * 首卡开门启用 (0-禁用, 1-启用)
     */
    private Integer firstCardOpenEnabled;

    /**
     * 常开模式启用 (0-禁用, 1-启用)
     */
    private Integer normallyOpenEnabled;

    /**
     * 常开模式时间段(JSON格式)
     */
    private String normallyOpenPeriod;

    /**
     * 通信密钥
     */
    private String commKey;

    // ==================== 业务方法 ====================

    /**
     * 获取门禁设备ID
     * 兼容原有代码调用
     */
    public Long getAccessDeviceId() {
        return this.getDeviceId();
    }

    /**
     * 获取协议类型
     * 兼容原有代码调用
     */
    public String getProtocol() {
        return this.getProtocolType();
    }

    /**
     * 获取启用状态
     * 兼容原有代码调用
     */
    public Integer getEnabled() {
        return this.getEnabledFlag();
    }

    /**
     * 设置门禁设备类型（整数类型兼容方法）
     * 兼容原有代码调用
     */
    public void setAccessDeviceType(int accessDeviceType) {
        // 将整数转换为对应的门禁设备类型字符串
        switch (accessDeviceType) {
            case 1:
                this.accessDeviceType = "FINGERPRINT";
                break;
            case 2:
                this.accessDeviceType = "FACE";
                break;
            case 3:
                this.accessDeviceType = "CARD";
                break;
            case 4:
                this.accessDeviceType = "PASSWORD";
                break;
            default:
                this.accessDeviceType = "CARD"; // 默认为刷卡机
                break;
        }
    }

  }