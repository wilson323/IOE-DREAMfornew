package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费设备VO
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 提供完整的展示字段
 * - 包含格式化后的显示信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@Accessors(chain = true)
public class ConsumeDeviceVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型代码
     */
    private String deviceTypeCode;

    /**
     * 设备类型名称
     */
    private String deviceTypeName;

    /**
     * 设备状态代码
     */
    private String statusCode;

    /**
     * 设备状态名称
     */
    private String statusName;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 安装地址
     */
    private String installAddress;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备MAC地址
     */
    private String macAddress;

    /**
     * 设备版本
     */
    private String deviceVersion;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 在线状态
     */
    private Boolean isOnline;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 负责人
     */
    private String responsiblePerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 安装日期
     */
    private LocalDateTime installDate;

    /**
     * 维护日期
     */
    private LocalDateTime maintenanceDate;

    /**
     * 下次维护日期
     */
    private LocalDateTime nextMaintenanceDate;

    /**
     * 使用状态
     */
    private Integer usageStatus;

    /**
     * 今日交易次数
     */
    private Integer todayTransactionCount;

    /**
     * 今日交易金额
     */
    private BigDecimal todayTransactionAmount;

    /**
     * 累计交易次数
     */
    private Integer totalTransactionCount;

    /**
     * 累计交易金额
     */
    private BigDecimal totalTransactionAmount;

    /**
     * 设备配置信息
     */
    private String deviceConfig;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    private Integer deletedFlag;
}