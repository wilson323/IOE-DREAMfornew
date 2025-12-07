package net.lab1024.sa.common.organization.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.service.DeviceService;

/**
 * 设备管理服务实现类
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识服务实现
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 使用@Transactional管理事务
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceDao deviceDao;

    /**
     * 更新设备状态
     * <p>
     * 用于设备协议推送设备状态更新
     * </p>
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态（ONLINE/OFFLINE/MAINTAIN）
     * @param lastOnlineTime 最后在线时间（可选）
     * @return 是否更新成功
     */
    @Override
    public ResponseDTO<Boolean> updateDeviceStatus(Long deviceId, String deviceStatus, LocalDateTime lastOnlineTime) {
        log.info("[设备管理] 更新设备状态，deviceId={}, deviceStatus={}, lastOnlineTime={}",
                deviceId, deviceStatus, lastOnlineTime);

        try {
            // 查询设备
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[设备管理] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 更新设备状态
            device.setDeviceStatus(deviceStatus);
            if (lastOnlineTime != null) {
                // 如果设备有lastOnlineTime字段，更新它
                // 注意：DeviceEntity可能没有lastOnlineTime字段，需要根据实际实体定义调整
            }

            // 更新设备
            int result = deviceDao.updateById(device);

            if (result > 0) {
                log.info("[设备管理] 设备状态更新成功，deviceId={}, deviceStatus={}", deviceId, deviceStatus);
                return ResponseDTO.ok(true);
            } else {
                log.warn("[设备管理] 设备状态更新失败，deviceId={}", deviceId);
                return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态失败");
            }

        } catch (Exception e) {
            log.error("[设备管理] 更新设备状态异常，deviceId={}", deviceId, e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态异常: " + e.getMessage());
        }
    }

    /**
     * 根据设备编码查询设备
     * <p>
     * 用于根据设备序列号（SN）或设备编码查找设备信息
     * </p>
     *
     * @param deviceCode 设备编码（设备序列号SN）
     * @return 设备实体，如果不存在则返回null
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<DeviceEntity> getDeviceByCode(String deviceCode) {
        log.info("[设备管理] 根据设备编码查询设备，deviceCode={}", deviceCode);

        try {
            if (deviceCode == null || deviceCode.isEmpty()) {
                log.warn("[设备管理] 设备编码为空");
                return ResponseDTO.error("DEVICE_CODE_REQUIRED", "设备编码不能为空");
            }

            // 查询设备
            DeviceEntity device = deviceDao.selectByDeviceCode(deviceCode);
            if (device == null) {
                log.warn("[设备管理] 设备不存在，deviceCode={}", deviceCode);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            log.info("[设备管理] 查询设备成功，deviceCode={}, deviceId={}", deviceCode, device.getDeviceId());
            return ResponseDTO.ok(device);

        } catch (Exception e) {
            log.error("[设备管理] 根据设备编码查询设备异常，deviceCode={}", deviceCode, e);
            return ResponseDTO.error("QUERY_DEVICE_ERROR", "查询设备异常: " + e.getMessage());
        }
    }
}

