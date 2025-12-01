package net.lab1024.sa.device.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁设备实体类 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 统一继承BaseEntity获取审计字段
 * - 使用MyBatis-Plus注解进行ORM映射
 * - 完整的字段注释和说明
 * - 门禁设备特有字段完整覆盖
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_device")
public class AccessDeviceEntity extends BaseEntity {

    /**
     * 门禁设备ID
     */
    @TableId(type = IdType.AUTO)
    private Long accessDeviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 门禁设备类型 (1-门禁控制器, 2-读卡器, 3-生物识别设备, 4-出门按钮)
     */
    private Integer accessDeviceType;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 在线状态 (0-离线, 1-在线)
     */
    private Integer onlineStatus;

    /**
     * 启用状态 (0-禁用, 1-启用)
     */
    private Integer enabled;

    /**
     * 工作模式 (1-正常模式, 2-维护模式, 3-紧急模式, 4-锁闭模式)
     */
    private Integer workMode;

    /**
     * 设备厂商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 协议类型 (TCP, UDP, HTTP, HTTPS, MQTT)
     */
    private String protocolType;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 安装位置
     */
    private String installLocation;

    /**
     * 安装日期
     */
    private LocalDateTime installDate;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 配置信息(JSON格式)
     */
    private String configJson;

    /**
     * 备注
     */
    private String remark;
}