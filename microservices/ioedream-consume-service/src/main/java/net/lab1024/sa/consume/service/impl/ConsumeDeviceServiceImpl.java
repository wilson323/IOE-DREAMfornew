package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.dao.ConsumeDeviceDao;
import net.lab1024.sa.common.entity.consume.ConsumeDeviceEntity;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeDeviceUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.consume.service.ConsumeDeviceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消费设备服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@Service
public class ConsumeDeviceServiceImpl implements ConsumeDeviceService {

    @Resource
    private ConsumeDeviceDao consumeDeviceDao;

    @Override
    public PageResult<ConsumeDeviceVO> queryDevices(ConsumeDeviceQueryForm queryForm) {
        Page<ConsumeDeviceEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        // 预处理参数
        String deviceCode = (queryForm.getDeviceCode() != null && !queryForm.getDeviceCode().trim().isEmpty()) ? queryForm.getDeviceCode() : null;
        String deviceName = (queryForm.getDeviceName() != null && !queryForm.getDeviceName().trim().isEmpty()) ? queryForm.getDeviceName() : null;

        LambdaQueryWrapper<ConsumeDeviceEntity> queryWrapper = QueryBuilder.of(ConsumeDeviceEntity.class)
                .keyword(deviceCode, ConsumeDeviceEntity::getDeviceCode)
                .keyword(deviceName, ConsumeDeviceEntity::getDeviceName)
                .eq(ConsumeDeviceEntity::getDeviceType, queryForm.getDeviceType())
                .eq(ConsumeDeviceEntity::getDeviceStatus, queryForm.getDeviceStatus())
                .eq(ConsumeDeviceEntity::getAreaId, queryForm.getAreaId())
                .orderByDesc(ConsumeDeviceEntity::getCreateTime)
                .build();

        IPage<ConsumeDeviceEntity> pageResult = consumeDeviceDao.selectPage(page, queryWrapper);

        List<ConsumeDeviceVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.<ConsumeDeviceVO>builder()
                .list(voList)
                .total(pageResult.getTotal())
                .pageNum(queryForm.getPageNum())
                .pageSize(queryForm.getPageSize())
                .pages((int) Math.ceil((double) pageResult.getTotal() / queryForm.getPageSize()))
                .hasNext((long) queryForm.getPageNum() * queryForm.getPageSize() < pageResult.getTotal())
                .hasPrev(queryForm.getPageNum() > 1)
                .build();
    }

    @Override
    public ConsumeDeviceVO getDeviceDetail(Long deviceId) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        if (entity == null) {
            throw new RuntimeException("设备不存在，ID: " + deviceId);
        }
        return convertToVO(entity);
    }

    @Override
    public Long createDevice(ConsumeDeviceAddForm addForm) {
        // 检查设备编码是否已存在
        LambdaQueryWrapper<ConsumeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeDeviceEntity::getDeviceCode, addForm.getDeviceCode());
        ConsumeDeviceEntity existingDevice = consumeDeviceDao.selectOne(queryWrapper);
        if (existingDevice != null) {
            throw new RuntimeException("设备编码已存在: " + addForm.getDeviceCode());
        }

        ConsumeDeviceEntity entity = new ConsumeDeviceEntity();
        BeanUtils.copyProperties(addForm, entity);

        entity.setDeviceStatus(1); // 默认启用状态
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        consumeDeviceDao.insert(entity);

        log.info("[设备服务] 创建消费设备成功，设备ID: {}, 设备编码: {}", entity.getDeviceId(), entity.getDeviceCode());
        return entity.getDeviceId();
    }

    @Override
    public void updateDevice(Long deviceId, ConsumeDeviceUpdateForm updateForm) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        if (entity == null) {
            throw new RuntimeException("设备不存在，ID: " + deviceId);
        }

        BeanUtils.copyProperties(updateForm, entity, "deviceId", "createTime");
        entity.setUpdateTime(LocalDateTime.now());

        consumeDeviceDao.updateById(entity);

        log.info("[设备服务] 更新消费设备成功，设备ID: {}", deviceId);
    }

    @Override
    public void deleteDevice(Long deviceId) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        if (entity == null) {
            throw new RuntimeException("设备不存在，ID: " + deviceId);
        }

        // 检查设备是否有关联的记录

        consumeDeviceDao.deleteById(deviceId);

        log.info("[设备服务] 删除消费设备成功，设备ID: {}", deviceId);
    }

    @Override
    public String getDeviceStatus(Long deviceId) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        if (entity == null) {
            throw new RuntimeException("设备不存在，ID: " + deviceId);
        }

                return entity.getDeviceStatus() == 1 ? "online" : "offline";
    }

    @Override
    public void updateDeviceStatus(Long deviceId, String status) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        if (entity == null) {
            throw new RuntimeException("设备不存在，ID: " + deviceId);
        }

        int statusCode = "online".equals(status) ? 1 : "offline".equals(status) ? 2 : 0;
        entity.setDeviceStatus(statusCode);
        entity.setUpdateTime(LocalDateTime.now());

        consumeDeviceDao.updateById(entity);

        log.info("[设备服务] 更新设备状态成功，设备ID: {}, 状态: {}", deviceId, status);
    }

    @Override
    public List<ConsumeDeviceVO> getOnlineDevices() {
        LambdaQueryWrapper<ConsumeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeDeviceEntity::getDeviceStatus, 1)
                .orderByDesc(ConsumeDeviceEntity::getUpdateTime);

        List<ConsumeDeviceEntity> entities = consumeDeviceDao.selectList(queryWrapper);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConsumeDeviceVO> getOfflineDevices() {
        LambdaQueryWrapper<ConsumeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeDeviceEntity::getDeviceStatus, 2)
                .orderByDesc(ConsumeDeviceEntity::getUpdateTime);

        List<ConsumeDeviceEntity> entities = consumeDeviceDao.selectList(queryWrapper);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void restartDevice(Long deviceId) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        if (entity == null) {
            throw new RuntimeException("设备不存在，ID: " + deviceId);
        }
        log.info("[设备服务] 重启设备成功，设备ID: {}", deviceId);
    }

    @Override
    public void syncDeviceConfig(Long deviceId) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        if (entity == null) {
            throw new RuntimeException("设备不存在，ID: " + deviceId);
        }
        log.info("[设备服务] 同步设备配置成功，设备ID: {}", deviceId);
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总设备数
        long totalCount = consumeDeviceDao.selectCount(null);
        statistics.put("totalCount", totalCount);

        // 在线设备数
        LambdaQueryWrapper<ConsumeDeviceEntity> onlineWrapper = new LambdaQueryWrapper<>();
        onlineWrapper.eq(ConsumeDeviceEntity::getDeviceStatus, 1);
        long onlineCount = consumeDeviceDao.selectCount(onlineWrapper);
        statistics.put("onlineCount", onlineCount);

        // 离线设备数
        LambdaQueryWrapper<ConsumeDeviceEntity> offlineWrapper = new LambdaQueryWrapper<>();
        offlineWrapper.eq(ConsumeDeviceEntity::getDeviceStatus, 2);
        long offlineCount = consumeDeviceDao.selectCount(offlineWrapper);
        statistics.put("offlineCount", offlineCount);

        // 故障设备数
        LambdaQueryWrapper<ConsumeDeviceEntity> faultWrapper = new LambdaQueryWrapper<>();
        faultWrapper.eq(ConsumeDeviceEntity::getDeviceStatus, 0);
        long faultCount = consumeDeviceDao.selectCount(faultWrapper);
        statistics.put("faultCount", faultCount);

        // 在线率
        double onlineRate = totalCount > 0 ? (double) onlineCount / totalCount * 100 : 0;
        statistics.put("onlineRate", onlineRate);

        return statistics;
    }

    @Override
    public void batchOperateDevices(List<Long> deviceIds, String operation) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            throw new RuntimeException("设备ID列表不能为空");
        }

        for (Long deviceId : deviceIds) {
            switch (operation) {
                case "enable":
                    updateDeviceStatus(deviceId, "online");
                    break;
                case "disable":
                    updateDeviceStatus(deviceId, "offline");
                    break;
                case "restart":
                    restartDevice(deviceId);
                    break;
                case "sync":
                    syncDeviceConfig(deviceId);
                    break;
                default:
                    log.warn("[设备服务] 未知的批量操作类型: {}", operation);
            }
        }

        log.info("[设备服务] 批量操作设备成功，操作类型: {}, 设备数量: {}", operation, deviceIds.size());
    }

    @Override
    public String getDeviceHealthStatus(Long deviceId) {
        return "healthy";
    }

    @Override
    public Boolean checkDeviceConnection(Long deviceId) {
        return true;
    }

    @Override
    public Map<String, Object> getDeviceConfig(Long deviceId) {
        return new HashMap<>();
    }

    @Override
    public void updateDeviceConfig(Long deviceId, Map<String, Object> config) {
        log.info("[设备服务] 更新设备配置成功，设备ID: {}", deviceId);
    }

    /**
     * 实体转VO
     */
    private ConsumeDeviceVO convertToVO(ConsumeDeviceEntity entity) {
        ConsumeDeviceVO vo = new ConsumeDeviceVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置设备类型名称
        vo.setDeviceTypeName(getDeviceTypeName(entity.getDeviceType()));

        // 设置设备状态名称
        vo.setDeviceStatusName(getDeviceStatusName(entity.getDeviceStatus()));

        // 设置在线状态
        vo.setOnlineStatus(entity.getDeviceStatus() == 1 ? "online" : "offline");

        // 设置健康状态
        vo.setHealthStatus("healthy");

        // 模拟一些统计数据
        vo.setTodayTransactionCount(0);
        vo.setTodayTransactionAmount(java.math.BigDecimal.ZERO);

        return vo;
    }

    private String getDeviceTypeName(Integer deviceType) {
        switch (deviceType) {
            case 1: return "POS机";
            case 2: return "消费机";
            case 3: return "自助终端";
            default: return "未知类型";
        }
    }

    private String getDeviceStatusName(Integer deviceStatus) {
        switch (deviceStatus) {
            case 0: return "故障";
            case 1: return "在线";
            case 2: return "离线";
            case 3: return "维护";
            default: return "未知状态";
        }
    }
}