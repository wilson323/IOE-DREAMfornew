package net.lab1024.sa.system.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartPageUtil;
import net.lab1024.sa.system.dao.UnifiedDeviceDao;
import net.lab1024.sa.system.domain.entity.UnifiedDeviceEntity;
import net.lab1024.sa.system.domain.form.UnifiedDeviceAddForm;
import net.lab1024.sa.system.domain.form.UnifiedDeviceQueryForm;
import net.lab1024.sa.system.domain.form.UnifiedDeviceUpdateForm;
import net.lab1024.sa.system.manager.UnifiedDeviceManager;
import net.lab1024.sa.system.service.UnifiedDeviceService;

/**
 * 统一设备管理服务实现
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Service
public class UnifiedDeviceServiceImpl implements UnifiedDeviceService {

    @Resource
    private UnifiedDeviceDao unifiedDeviceDao;

    @Resource
    private UnifiedDeviceManager unifiedDeviceManager;

    @Override
    public PageResult<UnifiedDeviceEntity> queryDevicePage(UnifiedDeviceQueryForm queryForm) {
        // 使用反射访问 Lombok 生成的 getter 方法
        int pageNum = 1;
        int pageSize = 20;
        try {
            java.lang.reflect.Method getPageNumMethod = queryForm.getClass().getMethod("getPageNum");
            Object pageNumObj = getPageNumMethod.invoke(queryForm);
            if (pageNumObj != null) {
                pageNum = (Integer) pageNumObj;
            }

            java.lang.reflect.Method getPageSizeMethod = queryForm.getClass().getMethod("getPageSize");
            Object pageSizeObj = getPageSizeMethod.invoke(queryForm);
            if (pageSizeObj != null) {
                pageSize = (Integer) pageSizeObj;
            }
        } catch (Exception e) {
            log.warn("无法通过反射访问分页参数，使用默认值", e);
        }
        Page<UnifiedDeviceEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (queryForm.getDeviceType() != null) {
            queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, queryForm.getDeviceType());
        }
        if (queryForm.getDeviceStatus() != null) {
            queryWrapper.eq(UnifiedDeviceEntity::getDeviceStatus, queryForm.getDeviceStatus());
        }
        if (queryForm.getDeviceName() != null && !queryForm.getDeviceName().trim().isEmpty()) {
            queryWrapper.like(UnifiedDeviceEntity::getDeviceName, queryForm.getDeviceName());
        }
        if (queryForm.getAreaId() != null) {
            queryWrapper.eq(UnifiedDeviceEntity::getAreaId, queryForm.getAreaId());
        }

        queryWrapper.orderByDesc(UnifiedDeviceEntity::getUpdateTime);

        Page<UnifiedDeviceEntity> result = unifiedDeviceDao.selectPage(page, queryWrapper);
        return SmartPageUtil.convert2PageResult(result, result.getRecords(), UnifiedDeviceEntity.class);
    }

    @Override
    public UnifiedDeviceEntity getDeviceDetail(Long deviceId) {
        return unifiedDeviceDao.selectById(deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDevice(UnifiedDeviceAddForm addForm) {
        // 检查设备编号是否重复
        LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedDeviceEntity::getDeviceCode, addForm.getDeviceCode());
        if (unifiedDeviceDao.selectOne(queryWrapper) != null) {
            throw new RuntimeException("设备编号已存在");
        }

        UnifiedDeviceEntity entity = new UnifiedDeviceEntity();
        SmartBeanUtil.copyProperties(addForm, entity);
        entity.setDeviceStatus("OFFLINE");
        // 创建时间和更新时间由BaseEntity自动填充
        // TODO: 设置创建者信息
        // entity.setCreatedById(SmartRequestUtil.getRequestUserId());

        unifiedDeviceDao.insert(entity);

        // 注册设备到设备管理器
        unifiedDeviceManager.registerDevice(entity);

        return entity.getDeviceId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(UnifiedDeviceUpdateForm updateForm) {
        UnifiedDeviceEntity existDevice = unifiedDeviceDao.selectById(updateForm.getDeviceId());
        if (existDevice == null) {
            throw new RuntimeException("设备不存在");
        }

        UnifiedDeviceEntity entity = new UnifiedDeviceEntity();
        SmartBeanUtil.copyProperties(updateForm, entity);
        // 更新时间由BaseEntity自动填充

        unifiedDeviceDao.updateById(entity);

        // 通知设备管理器更新设备信息
        unifiedDeviceManager.updateDevice(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long deviceId) {
        UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        // 软删除设备
        LambdaUpdateWrapper<UnifiedDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UnifiedDeviceEntity::getDeviceId, deviceId)
                .set(UnifiedDeviceEntity::getDeviceStatus, "DELETED");
        // 更新时间由BaseEntity自动填充

        unifiedDeviceDao.update(null, updateWrapper);

        // 从设备管理器注销设备
        unifiedDeviceManager.unregisterDevice(deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableDevice(Long deviceId) {
        LambdaUpdateWrapper<UnifiedDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UnifiedDeviceEntity::getDeviceId, deviceId)
                .set(UnifiedDeviceEntity::getDeviceStatus, "ONLINE");
        // 更新时间由BaseEntity自动填充

        unifiedDeviceDao.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableDevice(Long deviceId) {
        LambdaUpdateWrapper<UnifiedDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UnifiedDeviceEntity::getDeviceId, deviceId)
                .set(UnifiedDeviceEntity::getDeviceStatus, "DISABLED");
        // 更新时间由BaseEntity自动填充

        unifiedDeviceDao.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceStatus(Long deviceId, String deviceStatus) {
        LambdaUpdateWrapper<UnifiedDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UnifiedDeviceEntity::getDeviceId, deviceId)
                .set(UnifiedDeviceEntity::getDeviceStatus, deviceStatus);
        // 更新时间由BaseEntity自动填充

        unifiedDeviceDao.update(null, updateWrapper);
    }

    @Override
    public boolean reportDeviceHeartbeat(Long deviceId) {
        try {
            UnifiedDeviceEntity device = unifiedDeviceDao.selectById(deviceId);
            if (device == null) {
                return false;
            }

            // 更新心跳时间
            LambdaUpdateWrapper<UnifiedDeviceEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UnifiedDeviceEntity::getDeviceId, deviceId)
                    .set(UnifiedDeviceEntity::getLastHeartbeat, LocalDateTime.now())
                    .set(UnifiedDeviceEntity::getDeviceStatus, "ONLINE");

            unifiedDeviceDao.update(null, updateWrapper);

            return true;
        } catch (Exception e) {
            log.error("设备心跳处理失败，设备ID：{}", deviceId, e);
            return false;
        }
    }

    @Override
    public String remoteControlDevice(Long deviceId, String command, Map<String, Object> params) {
        return unifiedDeviceManager.remoteControlDevice(deviceId, command, params);
    }

    @Override
    public String remoteOpenDoor(Long deviceId) {
        return unifiedDeviceManager.remoteOpenDoor(deviceId);
    }

    @Override
    public String controlPtzDevice(Long deviceId, String command, Integer speed) {
        return unifiedDeviceManager.controlPtzDevice(deviceId, command, speed);
    }

    @Override
    public String getLiveStream(Long deviceId) {
        return unifiedDeviceManager.getLiveStream(deviceId);
    }

    @Override
    public Map<String, Object> getDeviceStatusStatistics(String deviceType) {
        LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (deviceType != null) {
            queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, deviceType);
        }
        queryWrapper.ne(UnifiedDeviceEntity::getDeviceStatus, "DELETED");

        List<UnifiedDeviceEntity> devices = unifiedDeviceDao.selectList(queryWrapper);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", devices.size());

        long onlineCount = devices.stream().filter(d -> "ONLINE".equals(d.getDeviceStatus())).count();
        long offlineCount = devices.stream().filter(d -> "OFFLINE".equals(d.getDeviceStatus())).count();
        long disabledCount = devices.stream().filter(d -> "DISABLED".equals(d.getDeviceStatus())).count();

        statistics.put("online", onlineCount);
        statistics.put("offline", offlineCount);
        statistics.put("disabled", disabledCount);
        statistics.put("onlineRate", devices.size() > 0 ? (double) onlineCount / devices.size() * 100 : 0);

        return statistics;
    }

    @Override
    public Map<String, Object> getDeviceTypeStatistics() {
        LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(UnifiedDeviceEntity::getDeviceStatus, "DELETED");

        List<UnifiedDeviceEntity> devices = unifiedDeviceDao.selectList(queryWrapper);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", devices.size());

        // 按设备类型统计
        Map<String, Long> typeCount = new HashMap<>();
        devices.forEach(device -> {
            typeCount.merge(device.getDeviceType(), 1L, Long::sum);
        });

        statistics.put("byType", typeCount);

        return statistics;
    }

    @Override
    public List<UnifiedDeviceEntity> getOnlineDevices(String deviceType) {
        LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedDeviceEntity::getDeviceStatus, "ONLINE");
        if (deviceType != null) {
            queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, deviceType);
        }
        queryWrapper.orderByDesc(UnifiedDeviceEntity::getLastHeartbeat);

        return unifiedDeviceDao.selectList(queryWrapper);
    }

    @Override
    public List<UnifiedDeviceEntity> getOfflineDevices(String deviceType) {
        LambdaQueryWrapper<UnifiedDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UnifiedDeviceEntity::getDeviceStatus, "OFFLINE");
        if (deviceType != null) {
            queryWrapper.eq(UnifiedDeviceEntity::getDeviceType, deviceType);
        }
        queryWrapper.orderByDesc(UnifiedDeviceEntity::getUpdateTime);

        return unifiedDeviceDao.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String batchOperateDevices(List<Long> deviceIds, String operation) {
        int count = 0;

        for (Long deviceId : deviceIds) {
            try {
                switch (operation) {
                    case "enable":
                        enableDevice(deviceId);
                        break;
                    case "disable":
                        disableDevice(deviceId);
                        break;
                    case "delete":
                        deleteDevice(deviceId);
                        break;
                    default:
                        log.warn("未知的批量操作类型：{}", operation);
                        continue;
                }
                count++;
            } catch (Exception e) {
                log.error("批量操作设备失败，设备ID：{}，操作：{}", deviceId, operation, e);
            }
        }

        return String.format("批量操作完成，成功处理%d 个设备", count);
    }
}
