#!/bin/bash

# 修复操作日志常量和其他缺失类的脚本
echo "🔧 开始修复操作日志常量和其他缺失类..."

BASE_DIR="D:/IOE-DREAM/smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base"

# 1. 修复操作日志常量
echo "修复操作日志常量..."
mkdir -p "$BASE_DIR/module/support/operatelog/constant"

cat > "$BASE_DIR/module/support/operatelog/constant/OperateLogConstant.java" << 'EOF'
package net.lab1024.sa.base.module.support.operatelog.constant;

/**
 * 操作日志常量
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
public interface OperateLogConstant {

    /**
     * 操作类型枚举值
     */
    interface OperateTypeEnum {
        String ADD = "ADD";
        String UPDATE = "UPDATE";
        String DELETE = "DELETE";
        String QUERY = "QUERY";
        String EXPORT = "EXPORT";
        String IMPORT = "IMPORT";
        String LOGIN = "LOGIN";
        String LOGOUT = "LOGOUT";
        String OTHER = "OTHER";
    }

    /**
     * 操作状态枚举值
     */
    interface OperateStatusEnum {
        String SUCCESS = "SUCCESS";
        String FAIL = "FAIL";
        String PROCESSING = "PROCESSING";
    }

    /**
     * 日志模块名称
     */
    interface ModuleName {
        String SYSTEM = "系统管理";
        String USER = "用户管理";
        String DEVICE = "设备管理";
        String ACCESS = "门禁管理";
        String ATTENDANCE = "考勤管理";
        String CONSUME = "消费管理";
        String VIDEO = "视频管理";
        String NOTIFICATION = "通知管理";
        String CONFIG = "配置管理";
    }

    /**
     * 缓存Key
     */
    interface CacheKey {
        String OPERATE_LOG_PREFIX = "operate:log:";
        String OPERATE_LOG_LIST = "operate:log:list";
    }

    /**
     * 字段长度限制
     */
    interface FieldLength {
        int MODULE_NAME_MAX_LENGTH = 100;
        int OPERATION_TYPE_MAX_LENGTH = 50;
        int OPERATION_DESC_MAX_LENGTH = 1000;
        int REQUEST_URL_MAX_LENGTH = 500;
        int REQUEST_PARAM_MAX_LENGTH = 2000;
        int RESPONSE_DATA_MAX_LENGTH = 2000;
        int USER_AGENT_MAX_LENGTH = 500;
        int IP_MAX_LENGTH = 50;
    }
}
EOF

# 2. 创建SmartDeviceEntity
echo "修复SmartDeviceEntity..."
mkdir -p "$BASE_DIR/common/device/domain/entity"

cat > "$BASE_DIR/common/device/domain/entity/SmartDeviceEntity.java" << 'EOF'
package net.lab1024.sa.base.common.device.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 智能设备实体类
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_device")
public class SmartDeviceEntity extends BaseEntity {

    /**
     * 设备ID
     */
    @TableId(value = "device_id", type = IdType.AUTO)
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备类型
     */
    @TableField("device_type")
    private Integer deviceType;

    /**
     * 设备状态
     */
    @TableField("device_status")
    private Integer deviceStatus;

    /**
     * 设备位置
     */
    @TableField("device_location")
    private String deviceLocation;

    /**
     * 设备描述
     */
    @TableField("device_desc")
    private String deviceDesc;

    /**
     * 设备配置
     */
    @TableField("device_config")
    private String deviceConfig;

    /**
     * 安装时间
     */
    @TableField("install_time")
    private LocalDateTime installTime;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    /**
     * 版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
EOF

# 3. 创建SmartDeviceVO
echo "修复SmartDeviceVO..."
mkdir -p "$BASE_DIR/common/device/domain/vo"

cat > "$BASE_DIR/common/device/domain/vo/SmartDeviceVO.java" << 'EOF'
package net.lab1024.sa.base.common.device.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能设备视图对象
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Data
public class SmartDeviceVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 设备类型名称
     */
    private String deviceTypeName;

    /**
     * 设备状态
     */
    private Integer deviceStatus;

    /**
     * 设备状态名称
     */
    private String deviceStatusName;

    /**
     * 设备位置
     */
    private String deviceLocation;

    /**
     * 设备描述
     */
    private String deviceDesc;

    /**
     * 设备配置
     */
    private String deviceConfig;

    /**
     * 安装时间
     */
    private LocalDateTime installTime;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 在线状态
     */
    private Boolean onlineStatus;

    /**
     * 电池电量
     */
    private Integer batteryLevel;

    /**
     * 信号强度
     */
    private Integer signalStrength;

    /**
     * 设备温度
     */
    private Double deviceTemperature;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 创建用户名称
     */
    private String createUserName;

    /**
     * 版本号
     */
    private Integer version;
}
EOF

echo "✅ 操作日志常量和其他缺失类修复完成！"