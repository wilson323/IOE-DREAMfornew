package net.lab1024.sa.base.module.device.service;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.module.device.enums.DeviceStatusEnum;
import net.lab1024.sa.base.module.device.enums.DeviceTypeEnum;
import net.lab1024.sa.base.module.device.enums.ProtocolTypeEnum;

import java.time.LocalDateTime;

/**
 * 智能设备业务服务
 * <p>
 * 处理设备的业务逻辑和状态管理
 * 将原来SmartDeviceEntity中的业务方法移到Service层，遵循单一职责原则
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
@Service
public class SmartDeviceBusinessService {

    /**
     * 检查设备是否在线
     *
     * @param device 设备实体
     * @return 是否在线
     */
    public boolean isOnline(SmartDeviceEntity device) {
        if (device == null || device.getDeviceStatus() == null) {
            return false;
        }
        return DeviceStatusEnum.ONLINE.getValue().equals(device.getDeviceStatus());
    }

    /**
     * 检查设备是否离线
     *
     * @param device 设备实体
     * @return 是否离线
     */
    public boolean isOffline(SmartDeviceEntity device) {
        if (device == null || device.getDeviceStatus() == null) {
            return false;
        }
        return DeviceStatusEnum.OFFLINE.getValue().equals(device.getDeviceStatus());
    }

    /**
     * 检查设备是否有故障
     *
     * @param device 设备实体
     * @return 是否有故障
     */
    public boolean hasFault(SmartDeviceEntity device) {
        if (device == null || device.getDeviceStatus() == null) {
            return false;
        }
        return DeviceStatusEnum.FAULT.getValue().equals(device.getDeviceStatus());
    }

    /**
     * 检查设备是否在维护中
     *
     * @param device 设备实体
     * @return 是否在维护中
     */
    public boolean isInMaintenance(SmartDeviceEntity device) {
        if (device == null || device.getDeviceStatus() == null) {
            return false;
        }
        return DeviceStatusEnum.MAINTAIN.getValue().equals(device.getDeviceStatus());
    }

    /**
     * 检查设备是否启用
     *
     * @param device 设备实体
     * @return 是否启用
     */
    public boolean isEnabled(SmartDeviceEntity device) {
        if (device == null || device.getEnabledFlag() == null) {
            return false;
        }
        return device.getEnabledFlag() == 1;
    }

    /**
     * 检查设备是否为指定类型
     *
     * @param device 设备实体
     * @param deviceType 设备类型
     * @return 是否为指定类型
     */
    public boolean isDeviceType(SmartDeviceEntity device, DeviceTypeEnum deviceType) {
        if (device == null || device.getDeviceType() == null) {
            return false;
        }
        return deviceType.getValue().equals(device.getDeviceType());
    }

    /**
     * 检查设备是否为门禁设备
     *
     * @param device 设备实体
     * @return 是否为门禁设备
     */
    public boolean isAccessDevice(SmartDeviceEntity device) {
        return isDeviceType(device, DeviceTypeEnum.ACCESS);
    }

    /**
     * 检查设备是否为考勤设备
     *
     * @param device 设备实体
     * @return 是否为考勤设备
     */
    public boolean isAttendanceDevice(SmartDeviceEntity device) {
        return isDeviceType(device, DeviceTypeEnum.ATTENDANCE);
    }

    /**
     * 检查设备是否为消费设备
     *
     * @param device 设备实体
     * @return 是否为消费设备
     */
    public boolean isConsumeDevice(SmartDeviceEntity device) {
        return isDeviceType(device, DeviceTypeEnum.CONSUME);
    }

    /**
     * 检查设备是否为视频设备
     *
     * @param device 设备实体
     * @return 是否为视频设备
     */
    public boolean isVideoDevice(SmartDeviceEntity device) {
        return isDeviceType(device, DeviceTypeEnum.VIDEO);
    }

    /**
     * 更新设备状态为在线
     *
     * @param device 设备实体
     */
    public void updateOnlineStatus(SmartDeviceEntity device) {
        if (device == null) {
            return;
        }
        device.setDeviceStatus(DeviceStatusEnum.ONLINE.getValue());
        device.setLastOnlineTime(LocalDateTime.now());
    }

    /**
     * 更新设备状态为离线
     *
     * @param device 设备实体
     */
    public void updateOfflineStatus(SmartDeviceEntity device) {
        if (device == null) {
            return;
        }
        device.setDeviceStatus(DeviceStatusEnum.OFFLINE.getValue());
    }

    /**
     * 更新设备状态为故障
     *
     * @param device 设备实体
     */
    public void updateFaultStatus(SmartDeviceEntity device) {
        if (device == null) {
            return;
        }
        device.setDeviceStatus(DeviceStatusEnum.FAULT.getValue());
    }

    /**
     * 更新设备状态为维护中
     *
     * @param device 设备实体
     */
    public void updateMaintenanceStatus(SmartDeviceEntity device) {
        if (device == null) {
            return;
        }
        device.setDeviceStatus(DeviceStatusEnum.MAINTAIN.getValue());
    }

    /**
     * 获取设备连接地址
     *
     * @param device 设备实体
     * @return 连接地址
     */
    public String getConnectionAddress(SmartDeviceEntity device) {
        if (device == null || device.getIpAddress() == null || device.getIpAddress().trim().isEmpty()) {
            return null;
        }

        StringBuilder address = new StringBuilder();
        address.append(device.getIpAddress());

        if (device.getPort() != null && device.getPort() > 0 &&
            device.getPort() != 80 && device.getPort() != 443) {
            address.append(":").append(device.getPort());
        }

        return address.toString();
    }

    /**
     * 获取设备描述信息
     *
     * @param device 设备实体
     * @return 设备描述
     */
    public String getDeviceDescription(SmartDeviceEntity device) {
        if (device == null || device.getDeviceType() == null) {
            return "未知设备";
        }

        try {
            DeviceTypeEnum type = DeviceTypeEnum.fromValue(device.getDeviceType());
            StringBuilder desc = new StringBuilder();
            desc.append(type.getDescription())
                .append(" - ").append(device.getDeviceName());

            if (device.getIpAddress() != null && !device.getIpAddress().trim().isEmpty()) {
                desc.append(" (").append(device.getIpAddress());
                if (device.getPort() != null && device.getPort() > 0) {
                    desc.append(":").append(device.getPort());
                }
                desc.append(")");
            }

            if (!isEnabled(device)) {
                desc.append(" [已禁用]");
            }

            return desc.toString();
        } catch (IllegalArgumentException e) {
            log.warn("无效的设备类型: {}", device.getDeviceType(), e);
            return device.getDeviceName() + " (未知类型)";
        }
    }

    /**
     * 检查设备是否需要更新固件
     *
     * @param device 设备实体
     * @param latestVersion 最新版本号
     * @return 是否需要更新
     */
    public boolean needsFirmwareUpdate(SmartDeviceEntity device, String latestVersion) {
        if (device == null || device.getFirmwareVersion() == null || latestVersion == null) {
            return false;
        }
        return !device.getFirmwareVersion().equals(latestVersion);
    }

    /**
     * 检查设备连接是否超时
     *
     * @param device 设备实体
     * @param timeoutMinutes 超时分钟数
     * @return 是否超时
     */
    public boolean isConnectionTimeout(SmartDeviceEntity device, long timeoutMinutes) {
        if (device == null || device.getLastOnlineTime() == null) {
            return true;
        }
        return device.getLastOnlineTime().isBefore(LocalDateTime.now().minusMinutes(timeoutMinutes));
    }

    /**
     * 设置设备的显示信息
     * 用于填充非数据库字段的显示信息
     *
     * @param device 设备实体
     */
    public void setDisplayInfo(SmartDeviceEntity device) {
        if (device == null) {
            return;
        }

        // 设置设备类型名称
        if (device.getDeviceType() != null) {
            try {
                DeviceTypeEnum type = DeviceTypeEnum.fromValue(device.getDeviceType());
                device.setDeviceTypeName(type.getDescription());
            } catch (IllegalArgumentException e) {
                device.setDeviceTypeName("未知类型");
                log.warn("无效的设备类型: {}", device.getDeviceType(), e);
            }
        }

        // 设置设备状态名称
        if (device.getDeviceStatus() != null) {
            try {
                DeviceStatusEnum status = DeviceStatusEnum.fromValue(device.getDeviceStatus());
                device.setDeviceStatusName(status.getDescription());
            } catch (IllegalArgumentException e) {
                device.setDeviceStatusName("未知状态");
                log.warn("无效的设备状态: {}", device.getDeviceStatus(), e);
            }
        }

        // 设置协议类型名称
        if (device.getProtocolType() != null) {
            try {
                ProtocolTypeEnum protocol = ProtocolTypeEnum.fromValue(device.getProtocolType());
                device.setProtocolTypeName(protocol.getDescription());
            } catch (IllegalArgumentException e) {
                device.setProtocolTypeName("未知协议");
                log.warn("无效的协议类型: {}", device.getProtocolType(), e);
            }
        }

        // 设置连接地址
        device.setConnectionAddress(getConnectionAddress(device));

        // 设置设备描述
        device.setDeviceDescription(getDeviceDescription(device));

        // 设置连接状态
        if (device.getDeviceStatus() != null) {
            device.setConnectionStatus(DeviceStatusEnum.ONLINE.getValue().equals(device.getDeviceStatus()) ? "连接正常" : "连接异常");
        }

        // 设置是否超时（默认超时时间10分钟）
        device.setIsTimeout(isConnectionTimeout(device, 10));

        // 设置是否需要固件更新（暂时设为false，需要业务逻辑确定）
        device.setNeedsFirmwareUpdate(false);
    }
}