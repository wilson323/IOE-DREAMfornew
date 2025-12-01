package net.lab1024.sa.admin.module.smart.device.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.device.dao.SmartDeviceDao;
import net.lab1024.sa.admin.module.smart.device.domain.entity.SmartDeviceEntity;
import net.lab1024.sa.admin.module.smart.device.service.SmartDeviceService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;

/**
 * 智能设备服务实现类
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@Service
@Slf4j
public class SmartDeviceServiceImpl extends ServiceImpl<SmartDeviceDao, SmartDeviceEntity>
        implements SmartDeviceService {

    @Override
    public PageResult<SmartDeviceEntity> queryDevicePage(PageParam pageParam, String deviceType,
            String deviceStatus, String deviceName) {
        LambdaQueryWrapper<SmartDeviceEntity> wrapper = new LambdaQueryWrapper<>();
        if (deviceType != null && !deviceType.isEmpty()) {
            wrapper.eq(SmartDeviceEntity::getDeviceType, deviceType);
        }
        if (deviceStatus != null && !deviceStatus.isEmpty()) {
            wrapper.eq(SmartDeviceEntity::getDeviceStatus, deviceStatus);
        }
        if (deviceName != null && !deviceName.isEmpty()) {
            wrapper.like(SmartDeviceEntity::getDeviceName, deviceName);
        }
        wrapper.orderByDesc(SmartDeviceEntity::getCreateTime);

        Page<SmartDeviceEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        Page<SmartDeviceEntity> pageData = this.page(page, wrapper);

        PageResult<SmartDeviceEntity> result = new PageResult<>();
        result.setList(pageData.getRecords());
        result.setTotal(pageData.getTotal());
        result.setPageNum(pageData.getCurrent());
        result.setPageSize(pageData.getSize());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> registerDevice(SmartDeviceEntity deviceEntity) {
        try {
            // 基本字段保护
            if (deviceEntity.getEnabledFlag() == null) {
                deviceEntity.setEnabledFlag(1);
            }
            if (deviceEntity.getDeviceStatus() == null
                    || deviceEntity.getDeviceStatus().isEmpty()) {
                deviceEntity.setDeviceStatus("OFFLINE");
            }
            this.save(deviceEntity);
            return SmartResponseUtil.success("设备注册成功");
        } catch (Exception e) {
            log.error("注册设备失败", e);
            return SmartResponseUtil.error("设备注册失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDevice(SmartDeviceEntity deviceEntity) {
        try {
            this.updateById(deviceEntity);
            return SmartResponseUtil.success("设备更新成功");
        } catch (Exception e) {
            log.error("更新设备失败", e);
            return SmartResponseUtil.error("设备更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteDevice(Long deviceId) {
        try {
            this.removeById(deviceId);
            return SmartResponseUtil.success("设备删除成功");
        } catch (Exception e) {
            log.error("删除设备失败", e);
            return SmartResponseUtil.error("设备删除失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<SmartDeviceEntity> getDeviceDetail(Long deviceId) {
        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null) {
                return ResponseDTO.userErrorParam("设备不存在");
            }
            return ResponseDTO.ok(device);
        } catch (Exception e) {
            log.error("获取设备详情失败", e);
            return ResponseDTO.userErrorParam("获取设备详情失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> enableDevice(Long deviceId) {
        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device != null) {
                device.setDeviceStatus("ONLINE");
                this.updateById(device);
            }
            return SmartResponseUtil.success("设备启用成功");
        } catch (Exception e) {
            log.error("启用设备失败", e);
            return SmartResponseUtil.error("启用设备失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> disableDevice(Long deviceId) {
        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device != null) {
                device.setDeviceStatus("OFFLINE");
                this.updateById(device);
            }
            return SmartResponseUtil.success("设备禁用成功");
        } catch (Exception e) {
            log.error("禁用设备失败", e);
            return SmartResponseUtil.error("禁用设备失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> batchOperateDevices(List<Long> deviceIds, String operation) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return SmartResponseUtil.error("设备ID列表不能为空");
        }
        try {
            switch (operation == null ? "" : operation.toLowerCase()) {
                case "enable":
                    for (Long id : deviceIds) {
                        SmartDeviceEntity d = this.getById(id);
                        if (d != null) {
                            d.setDeviceStatus("ONLINE");
                            this.updateById(d);
                        }
                    }
                    break;
                case "disable":
                    for (Long id : deviceIds) {
                        SmartDeviceEntity d = this.getById(id);
                        if (d != null) {
                            d.setDeviceStatus("OFFLINE");
                            this.updateById(d);
                        }
                    }
                    break;
                case "delete":
                    this.removeBatchByIds(deviceIds);
                    break;
                default:
                    return SmartResponseUtil.error("不支持的批量操作类型");
            }
            return SmartResponseUtil.success("批量操作成功");
        } catch (Exception e) {
            log.error("批量操作设备失败", e);
            return SmartResponseUtil.error("批量操作设备失败：" + e.getMessage());
        }
    }

    @Override
    public boolean deviceHeartbeat(Long deviceId) {
        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device == null) {
                return false;
            }
            device.setLastOnlineTime(LocalDateTime.now());
            device.setDeviceStatus("ONLINE");
            return this.updateById(device);
        } catch (Exception e) {
            log.error("设备心跳失败 deviceId={}", deviceId, e);
            return false;
        }
    }

    @Override
    public ResponseDTO<String> updateDeviceStatus(Long deviceId, String deviceStatus) {
        try {
            SmartDeviceEntity device = this.getById(deviceId);
            if (device != null) {
                device.setDeviceStatus(deviceStatus);
                this.updateById(device);
            }
            return SmartResponseUtil.success("设备状态更新成功");
        } catch (Exception e) {
            log.error("更新设备状态失败", e);
            return SmartResponseUtil.error("更新设备状态失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceStatusStatistics() {
        long total = this.count();
        long online = this.count(new LambdaQueryWrapper<SmartDeviceEntity>()
                .eq(SmartDeviceEntity::getDeviceStatus, "ONLINE"));
        long offline = this.count(new LambdaQueryWrapper<SmartDeviceEntity>()
                .eq(SmartDeviceEntity::getDeviceStatus, "OFFLINE"));
        long fault = this.count(new LambdaQueryWrapper<SmartDeviceEntity>()
                .eq(SmartDeviceEntity::getDeviceStatus, "FAULT"));
        long maintain = this.count(new LambdaQueryWrapper<SmartDeviceEntity>()
                .eq(SmartDeviceEntity::getDeviceStatus, "MAINTAIN"));

        Map<String, Object> statistics = new HashMap<>(8);
        statistics.put("total", total);
        statistics.put("online", online);
        statistics.put("offline", offline);
        statistics.put("fault", fault);
        statistics.put("maintain", maintain);
        return statistics;
    }

    @Override
    public Map<String, Object> getDeviceTypeStatistics() {
        Map<String, Object> statistics = new HashMap<>(8);
        // 简单统计常见类型数量
        String[] types = new String[] {"CAMERA", "ACCESS", "CONSUME", "ATTENDANCE"};
        for (String t : types) {
            long c = this.count(new LambdaQueryWrapper<SmartDeviceEntity>()
                    .eq(SmartDeviceEntity::getDeviceType, t));
            statistics.put(t, c);
        }
        statistics.put("others", Math.max(0, this.count() - types.length));
        return statistics;
    }

    @Override
    public ResponseDTO<String> remoteControlDevice(Long deviceId, String command,
            Map<String, Object> params) {
        // 这里仅做占位，实际需对接设备协议（MQTT/HTTP/TCP）
        log.info("远程控制设备 deviceId={}, command={}, params={}", deviceId, command, params);
        return SmartResponseUtil.success("远程控制成功");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceConfig(Long deviceId) {
        SmartDeviceEntity device = this.getById(deviceId);
        if (device == null) {
            return ResponseDTO.userErrorParam("设备不存在");
        }
        Map<String, Object> config = new HashMap<>(4);
        config.put("configJson", device.getConfigJson());
        return ResponseDTO.ok(config);
    }

    @Override
    public ResponseDTO<String> updateDeviceConfig(Long deviceId, Map<String, Object> config) {
        SmartDeviceEntity device = this.getById(deviceId);
        if (device == null) {
            return SmartResponseUtil.error("设备不存在");
        }
        Object cfg = config == null ? null : config.get("configJson");
        device.setConfigJson(cfg == null ? "{}" : String.valueOf(cfg));
        this.updateById(device);
        return SmartResponseUtil.success("配置更新成功");
    }

    @Override
    public List<SmartDeviceEntity> getOnlineDevices() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        return this.list(new LambdaQueryWrapper<SmartDeviceEntity>()
                .eq(SmartDeviceEntity::getDeviceStatus, "ONLINE")
                .ge(SmartDeviceEntity::getLastOnlineTime, threshold)
                .orderByDesc(SmartDeviceEntity::getLastOnlineTime));
    }

    @Override
    public List<SmartDeviceEntity> getOfflineDevices() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        return this.list(new LambdaQueryWrapper<SmartDeviceEntity>()
                .ne(SmartDeviceEntity::getDeviceStatus, "ONLINE")
                .or(w -> w.lt(SmartDeviceEntity::getLastOnlineTime, threshold).or()
                        .isNull(SmartDeviceEntity::getLastOnlineTime))
                .orderByAsc(SmartDeviceEntity::getLastOnlineTime));
    }
}
