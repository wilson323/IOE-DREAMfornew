package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 门禁设备扩展实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_device_access_extension")
public class SmartDeviceAccessExtensionEntity extends BaseEntity {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 门禁设备类型
     */
    private String accessDeviceType;

    /**
     * 开门方式
     */
    private String openMethod;

    /**
     * 验证模式
     */
    private String verificationMode;

    /**
     * 门禁参数配置
     */
    private String accessConfig;
}