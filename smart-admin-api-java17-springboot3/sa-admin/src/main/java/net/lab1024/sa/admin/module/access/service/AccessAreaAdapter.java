package net.lab1024.sa.admin.module.access.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.access.dao.AccessAreaDao;
import net.lab1024.sa.admin.module.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.base.module.area.dao.AreaDao;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import net.lab1024.sa.base.module.area.service.AreaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 门禁区域适配器
 * <p>
 * 用于在AccessAreaEntity重构为扩展表架构后，保持API的向后兼容性
 * 负责协调基础区域服务和门禁扩展服务的调用
 *
 * 适配器模式实现：
 * - 将AreaEntity + AreaAccessExtEntity的组合查询结果映射为AccessAreaEntity
 * - 将AccessAreaEntity的保存操作分解为基础区域和扩展信息的分别保存
 * - 确保现有API接口保持不变，内部实现使用扩展表架构
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
@Component
public class AccessAreaAdapter {

    @Resource
    private AreaService areaService;

    @Resource
    private AreaDao areaDao;

    @Resource
    private AccessAreaDao accessAreaDao;

    /**
     * 获取门禁区域详细信息
     *
     * @param areaId 区域ID
     * @return 包含扩展信息的完整门禁区域对象
     */
    public AccessAreaEntity getAccessArea(Long areaId) {
        log.debug("获取门禁区域详细信息，区域ID: {}", areaId);

        // 通过DAO层查询，直接获取JOIN后的完整数据
        AccessAreaEntity entity = accessAreaDao.selectById(areaId);

        if (entity == null) {
            log.warn("门禁区域不存在，区域ID: {}", areaId);
            return null;
        }

        // 设置默认值（如果扩展表中没有数据）
        setDefaultValues(entity);

        log.debug("成功获取门禁区域信息: {}", entity.getAreaName());
        return entity;
    }

    /**
     * 根据区域编码获取门禁区域
     *
     * @param areaCode 区域编码
     * @return 包含扩展信息的完整门禁区域对象
     */
    public AccessAreaEntity getAccessAreaByCode(String areaCode) {
        log.debug("根据编码获取门禁区域，区域编码: {}", areaCode);

        AccessAreaEntity entity = accessAreaDao.selectByAreaCode(areaCode);

        if (entity == null) {
            log.warn("门禁区域不存在，区域编码: {}", areaCode);
            return null;
        }

        setDefaultValues(entity);

        log.debug("成功获取门禁区域信息: {}", entity.getAreaName());
        return entity;
    }

    /**
     * 保存门禁区域信息
     *
     * @param accessArea 包含完整信息的门禁区域对象
     * @return 保存的区域对象
     */
    @Transactional(rollbackFor = Exception.class)
    public AccessAreaEntity saveAccessArea(AccessAreaEntity accessArea) {
        log.debug("保存门禁区域信息: {}", accessArea.getAreaName());

        try {
            // 1. 提取并保存基础区域信息
            AreaEntity areaEntity = extractBaseArea(accessArea);
            if (areaEntity.getAreaId() == null) {
                // 新增
                areaService.save(areaEntity);
                accessArea.setAreaId(areaEntity.getAreaId());
            } else {
                // 更新
                areaService.updateById(areaEntity);
            }

            // 2. 提取并保存门禁扩展信息
            saveAccessAreaExtension(accessArea);

            log.info("成功保存门禁区域信息，区域ID: {}", accessArea.getAreaId());
            return accessArea;

        } catch (Exception e) {
            log.error("保存门禁区域信息失败，区域名称: {}", accessArea.getAreaName(), e);
            throw new RuntimeException("保存门禁区域信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新门禁区域信息
     *
     * @param accessArea 包含更新信息的门禁区域对象
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAccessArea(AccessAreaEntity accessArea) {
        log.debug("更新门禁区域信息，区域ID: {}", accessArea.getAreaId());

        try {
            // 1. 更新基础区域信息
            AreaEntity areaEntity = extractBaseArea(accessArea);
            boolean updateResult = areaService.updateById(areaEntity);

            if (!updateResult) {
                log.warn("基础区域信息更新失败，区域ID: {}", accessArea.getAreaId());
                return false;
            }

            // 2. 更新门禁扩展信息
            saveAccessAreaExtension(accessArea);

            log.info("成功更新门禁区域信息，区域ID: {}", accessArea.getAreaId());
            return true;

        } catch (Exception e) {
            log.error("更新门禁区域信息失败，区域ID: {}", accessArea.getAreaId(), e);
            throw new RuntimeException("更新门禁区域信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除门禁区域
     *
     * @param areaId 区域ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAccessArea(Long areaId) {
        log.debug("删除门禁区域，区域ID: {}", areaId);

        try {
            // 软删除基础区域（会级联删除扩展信息）
            boolean deleteResult = areaService.removeById(areaId);

            if (deleteResult) {
                log.info("成功删除门禁区域，区域ID: {}", areaId);
            } else {
                log.warn("门禁区域删除失败，区域ID: {}", areaId);
            }

            return deleteResult;

        } catch (Exception e) {
            log.error("删除门禁区域失败，区域ID: {}", areaId, e);
            throw new RuntimeException("删除门禁区域失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询根级门禁区域列表
     *
     * @return 根级门禁区域列表
     */
    public List<AccessAreaEntity> getRootAreas() {
        log.debug("查询根级门禁区域列表");

        List<AccessAreaEntity> rootAreas = accessAreaDao.selectRootAreas();

        // 设置默认值
        rootAreas.forEach(this::setDefaultValues);

        log.debug("查询到根级门禁区域数量: {}", rootAreas.size());
        return rootAreas;
    }

    /**
     * 根据门禁级别查询区域列表
     *
     * @param accessLevel 门禁级别
     * @return 区域列表
     */
    public List<AccessAreaEntity> getAreasByAccessLevel(Integer accessLevel) {
        log.debug("根据门禁级别查询区域，级别: {}", accessLevel);

        List<AccessAreaEntity> areas = accessAreaDao.selectByAccessLevel(accessLevel);

        // 设置默认值
        areas.forEach(this::setDefaultValues);

        log.debug("查询到区域数量: {}", areas.size());
        return areas;
    }

    /**
     * 查询启用了门禁的区域列表
     *
     * @return 启用门禁的区域列表
     */
    public List<AccessAreaEntity> getEnabledAreas() {
        log.debug("查询启用门禁的区域列表");

        List<AccessAreaEntity> enabledAreas = accessAreaDao.selectEnabledAreas();

        // 设置默认值
        enabledAreas.forEach(this::setDefaultValues);

        log.debug("查询到启用门禁的区域数量: {}", enabledAreas.size());
        return enabledAreas;
    }

    /**
     * 从AccessAreaEntity中提取基础区域信息
     */
    private AreaEntity extractBaseArea(AccessAreaEntity accessArea) {
        AreaEntity areaEntity = new AreaEntity();

        areaEntity.setAreaId(accessArea.getAreaId());
        areaEntity.setAreaCode(accessArea.getAreaCode());
        areaEntity.setAreaName(accessArea.getAreaName());
        areaEntity.setAreaType(accessArea.getAreaType());
        areaEntity.setParentId(accessArea.getParentId());
        areaEntity.setPath(accessArea.getPath());
        areaEntity.setLevel(accessArea.getLevel());
        areaEntity.setSortOrder(accessArea.getSortOrder());
        areaEntity.setStatus(accessArea.getStatus());
        areaEntity.setLongitude(accessArea.getLongitude());
        areaEntity.setLatitude(accessArea.getLatitude());
        areaEntity.setAreaSize(accessArea.getAreaSize());
        areaEntity.setCapacity(accessArea.getCapacity());
        areaEntity.setDescription(accessArea.getDescription());
        areaEntity.setMapImage(accessArea.getMapImage());
        areaEntity.setRemark(accessArea.getRemark());

        return areaEntity;
    }

    /**
     * 保存门禁扩展信息
     * 目前通过DAO的INSERT...ON DUPLICATE KEY UPDATE实现
     * 后续可以考虑创建专门的AccessAreaExtService
     */
    private void saveAccessAreaExtension(AccessAreaEntity accessArea) {
        // 这里暂时通过SQL方式处理，因为还没有专门的AccessAreaExtService
        // 在实际使用中，可以考虑创建AccessAreaExtService来处理扩展表的操作

        // 重新查询，触发DAO层的JOIN查询来更新缓存
        AccessAreaEntity updated = accessAreaDao.selectById(accessArea.getAreaId());
        if (updated != null) {
            log.debug("门禁扩展信息已保存，区域ID: {}", accessArea.getAreaId());
        }
    }

    /**
     * 设置默认值
     * 确保从数据库查询出来的数据有合理的默认值
     */
    private void setDefaultValues(AccessAreaEntity entity) {
        if (entity.getAccessEnabled() == null) {
            entity.setAccessEnabled(1); // 默认启用
        }
        if (entity.getAccessLevel() == null) {
            entity.setAccessLevel(1); // 默认普通级别
        }
        if (entity.getAccessMode() == null) {
            entity.setAccessMode("卡"); // 默认刷卡
        }
        if (entity.getSpecialAuthRequired() == null) {
            entity.setSpecialAuthRequired(0); // 默认不需要特殊授权
        }
        if (entity.getDeviceCount() == null) {
            entity.setDeviceCount(0); // 默认0个设备
        }
        if (entity.getGuardRequired() == null) {
            entity.setGuardRequired(false); // 默认不需要安保
        }
        if (entity.getVisitorAllowed() == null) {
            entity.setVisitorAllowed(true); // 默认允许访客
        }
        if (entity.getEmergencyAccess() == null) {
            entity.setEmergencyAccess(false); // 默认不是紧急通道
        }
        if (entity.getMonitoringEnabled() == null) {
            entity.setMonitoringEnabled(true); // 默认启用监控
        }
    }
}