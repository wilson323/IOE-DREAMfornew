package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessCapacityControlDao;
import net.lab1024.sa.access.domain.form.AccessCapacityControlAddForm;
import net.lab1024.sa.access.domain.form.AccessCapacityControlQueryForm;
import net.lab1024.sa.access.domain.form.AccessCapacityControlUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessCapacityControlVO;
import net.lab1024.sa.access.domain.entity.AccessCapacityControlEntity;
import net.lab1024.sa.access.service.AccessCapacityService;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 门禁容量控制Service实现
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Slf4j
@Service
public class AccessCapacityServiceImpl implements AccessCapacityService {

    @Resource
    private AccessCapacityControlDao capacityControlDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CAPACITY_CACHE_KEY_PREFIX = "access:capacity:area:";
    private static final long CAPACITY_CACHE_EXPIRE_SECONDS = 3600; // 1小时过期

    @Override
    public PageResult<AccessCapacityControlVO> queryPage(AccessCapacityControlQueryForm queryForm) {
        log.info("[容量控制] 分页查询容量控制规则: {}", queryForm);

        Page<AccessCapacityControlEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<AccessCapacityControlEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 区域ID过滤
        if (queryForm.getAreaId() != null) {
            queryWrapper.eq(AccessCapacityControlEntity::getAreaId, queryForm.getAreaId());
        }

        // 控制模式过滤
        if (queryForm.getControlMode() != null && !queryForm.getControlMode().trim().isEmpty()) {
            queryWrapper.eq(AccessCapacityControlEntity::getControlMode, queryForm.getControlMode());
        }

        // 启用状态过滤
        if (queryForm.getEnabled() != null) {
            queryWrapper.eq(AccessCapacityControlEntity::getEnabled, queryForm.getEnabled());
        }

        // 按区域ID排序
        queryWrapper.orderByAsc(AccessCapacityControlEntity::getAreaId)
                .orderByDesc(AccessCapacityControlEntity::getCreateTime);

        IPage<AccessCapacityControlEntity> pageResult = capacityControlDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<AccessCapacityControlVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("[容量控制] 查询到{}条容量控制记录", pageResult.getTotal());
        return PageResult.of(voList, pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    @Override
    public AccessCapacityControlVO getById(Long controlId) {
        log.info("[容量控制] 查询容量控制详情: controlId={}", controlId);
        AccessCapacityControlEntity entity = capacityControlDao.selectById(controlId);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addControl(AccessCapacityControlAddForm addForm) {
        log.info("[容量控制] 新增容量控制规则: areaId={}, maxCapacity={}", addForm.getAreaId(), addForm.getMaxCapacity());

        AccessCapacityControlEntity entity = new AccessCapacityControlEntity();
        entity.setAreaId(addForm.getAreaId());
        entity.setMaxCapacity(addForm.getMaxCapacity());
        entity.setCurrentCount(0);
        entity.setControlMode(addForm.getControlMode());
        entity.setAlertThreshold(addForm.getAlertThreshold());
        entity.setEntryBlocked(0);
        entity.setEnabled(addForm.getEnabled() != null ? addForm.getEnabled() : 1);
        entity.setDescription(addForm.getDescription());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        capacityControlDao.insert(entity);

        // 初始化缓存
        String cacheKey = CAPACITY_CACHE_KEY_PREFIX + addForm.getAreaId();
        stringRedisTemplate.opsForValue().set(cacheKey, "0", CAPACITY_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.info("[容量控制] 新增容量控制成功: controlId={}", entity.getControlId());
        return entity.getControlId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateControl(Long controlId, AccessCapacityControlUpdateForm updateForm) {
        log.info("[容量控制] 更新容量控制规则: controlId={}", controlId);

        AccessCapacityControlEntity entity = capacityControlDao.selectById(controlId);
        if (entity == null) {
            log.warn("[容量控制] 容量控制规则不存在: controlId={}", controlId);
            return false;
        }

        // 更新字段
        if (updateForm.getMaxCapacity() != null) {
            entity.setMaxCapacity(updateForm.getMaxCapacity());
        }
        if (updateForm.getControlMode() != null) {
            entity.setControlMode(updateForm.getControlMode());
        }
        if (updateForm.getAlertThreshold() != null) {
            entity.setAlertThreshold(updateForm.getAlertThreshold());
        }
        if (updateForm.getDescription() != null) {
            entity.setDescription(updateForm.getDescription());
        }
        entity.setUpdateTime(LocalDateTime.now());

        int result = capacityControlDao.updateById(entity);
        log.info("[容量控制] 更新容量控制成功: controlId={}, result={}", controlId, result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteControl(Long controlId) {
        log.info("[容量控制] 删除容量控制规则: controlId={}", controlId);

        AccessCapacityControlEntity entity = capacityControlDao.selectById(controlId);
        if (entity != null) {
            // 清除缓存
            String cacheKey = CAPACITY_CACHE_KEY_PREFIX + entity.getAreaId();
            stringRedisTemplate.delete(cacheKey);
        }

        int result = capacityControlDao.deleteById(controlId);
        log.info("[容量控制] 删除容量控制成功: controlId={}, result={}", controlId, result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEnabled(Long controlId, Integer enabled) {
        log.info("[容量控制] 更新容量控制启用状态: controlId={}, enabled={}", controlId, enabled);

        AccessCapacityControlEntity entity = capacityControlDao.selectById(controlId);
        if (entity == null) {
            log.warn("[容量控制] 容量控制规则不存在: controlId={}", controlId);
            return false;
        }

        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());
        int result = capacityControlDao.updateById(entity);
        log.info("[容量控制] 更新启用状态成功: controlId={}, enabled={}, result={}", controlId, enabled, result);
        return result > 0;
    }

    @Override
    public String incrementCount(Long areaId, Integer count) {
        log.info("[容量控制] 增加区域内人数: areaId={}, count={}", areaId, count);

        AccessCapacityControlEntity control = getActiveControlByArea(areaId);
        if (control == null) {
            log.warn("[容量控制] 区域未配置容量控制: areaId={}", areaId);
            return "区域未配置容量控制";
        }

        if (control.getEnabled() != 1) {
            log.warn("[容量控制] 容量控制未启用: areaId={}", areaId);
            return "容量控制未启用";
        }

        Integer currentCount = getCurrentCountFromCache(areaId, control.getCurrentCount());
        int newCount = currentCount + count;

        // 检查是否超过最大容量
        if (newCount > control.getMaxCapacity()) {
            // 检查控制模式
            if ("STRICT".equals(control.getControlMode())) {
                log.warn("[容量控制] 超过最大容量，禁止进入: areaId={}, current={}, max={}",
                        areaId, newCount, control.getMaxCapacity());
                return String.format("超过最大容量%d人，禁止进入", control.getMaxCapacity());
            } else if ("WARNING".equals(control.getControlMode())) {
                log.warn("[容量控制] 超过最大容量，仅告警: areaId={}, current={}, max={}",
                        areaId, newCount, control.getMaxCapacity());
                // 更新数据库和缓存
                updateCountToCache(areaId, newCount);
                updateCountToDatabase(control.getControlId(), newCount);
                return String.format("警告：当前人数%d人，超过最大容量%d人", newCount, control.getMaxCapacity());
            }
        }

        // 检查告警阈值
        if (control.getAlertThreshold() > 0) {
            int alertThreshold = control.getMaxCapacity() * control.getAlertThreshold() / 100;
            if (newCount >= alertThreshold) {
                log.info("[容量控制] 人数达到告警阈值: areaId={}, current={}, threshold={}",
                        areaId, newCount, alertThreshold);
            }
        }

        // 更新数据库和缓存
        updateCountToCache(areaId, newCount);
        updateCountToDatabase(control.getControlId(), newCount);

        String result = String.format("人数增加成功，当前人数: %d/%d", newCount, control.getMaxCapacity());
        log.info("[容量控制] 增加人数成功: areaId={}, result={}", areaId, result);
        return result;
    }

    @Override
    public String decrementCount(Long areaId, Integer count) {
        log.info("[容量控制] 减少区域内人数: areaId={}, count={}", areaId, count);

        AccessCapacityControlEntity control = getActiveControlByArea(areaId);
        if (control == null) {
            return "区域未配置容量控制";
        }

        Integer currentCount = getCurrentCountFromCache(areaId, control.getCurrentCount());
        int newCount = Math.max(0, currentCount - count);

        // 更新数据库和缓存
        updateCountToCache(areaId, newCount);
        updateCountToDatabase(control.getControlId(), newCount);

        // 解锁进入限制（如果之前被锁定）
        if (control.getEntryBlocked() == 1 && newCount < control.getMaxCapacity()) {
            control.setEntryBlocked(0);
            capacityControlDao.updateById(control);
            log.info("[容量控制] 解除进入限制: areaId={}", areaId);
        }

        String result = String.format("人数减少成功，当前人数: %d/%d", newCount, control.getMaxCapacity());
        log.info("[容量控制] 减少人数成功: areaId={}, result={}", areaId, result);
        return result;
    }

    @Override
    public String resetCount(Long controlId) {
        log.info("[容量控制] 重置区域内人数: controlId={}", controlId);

        AccessCapacityControlEntity control = capacityControlDao.selectById(controlId);
        if (control == null) {
            return "容量控制规则不存在";
        }

        // 重置为0
        control.setCurrentCount(0);
        control.setEntryBlocked(0);
        control.setUpdateTime(LocalDateTime.now());
        capacityControlDao.updateById(control);

        // 清除缓存
        String cacheKey = CAPACITY_CACHE_KEY_PREFIX + control.getAreaId();
        stringRedisTemplate.delete(cacheKey);

        String result = String.format("人数已重置，当前人数: 0/%d", control.getMaxCapacity());
        log.info("[容量控制] 重置人数成功: controlId={}, result={}", controlId, result);
        return result;
    }

    @Override
    public Integer getCurrentCount(Long areaId) {
        AccessCapacityControlEntity control = getActiveControlByArea(areaId);
        if (control == null) {
            return 0;
        }
        return getCurrentCountFromCache(areaId, control.getCurrentCount());
    }

    @Override
    public Boolean checkEntryAllowed(Long areaId) {
        AccessCapacityControlEntity control = getActiveControlByArea(areaId);
        if (control == null || control.getEnabled() != 1) {
            return true; // 未配置或未启用，允许进入
        }

        Integer currentCount = getCurrentCountFromCache(areaId, control.getCurrentCount());
        return currentCount < control.getMaxCapacity();
    }

    /**
     * 根据区域ID获取启用的容量控制
     */
    private AccessCapacityControlEntity getActiveControlByArea(Long areaId) {
        LambdaQueryWrapper<AccessCapacityControlEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessCapacityControlEntity::getAreaId, areaId)
                .eq(AccessCapacityControlEntity::getEnabled, 1)
                .orderByDesc(AccessCapacityControlEntity::getCreateTime)
                .last("LIMIT 1");
        return capacityControlDao.selectOne(queryWrapper);
    }

    /**
     * 从缓存获取当前人数
     */
    private Integer getCurrentCountFromCache(Long areaId, Integer defaultValue) {
        try {
            String cacheKey = CAPACITY_CACHE_KEY_PREFIX + areaId;
            String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                return Integer.parseInt(cachedValue);
            }
        } catch (Exception e) {
            log.error("[容量控制] 从缓存获取人数失败: areaId={}, error={}", areaId, e.getMessage());
        }
        return defaultValue != null ? defaultValue : 0;
    }

    /**
     * 更新人数到缓存
     */
    private void updateCountToCache(Long areaId, Integer count) {
        try {
            String cacheKey = CAPACITY_CACHE_KEY_PREFIX + areaId;
            stringRedisTemplate.opsForValue().set(cacheKey, String.valueOf(count),
                    CAPACITY_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[容量控制] 更新缓存失败: areaId={}, count={}, error={}", areaId, count, e.getMessage());
        }
    }

    /**
     * 更新人数到数据库
     */
    private void updateCountToDatabase(Long controlId, Integer count) {
        try {
            AccessCapacityControlEntity entity = new AccessCapacityControlEntity();
            entity.setControlId(controlId);
            entity.setCurrentCount(count);
            entity.setUpdateTime(LocalDateTime.now());
            capacityControlDao.updateById(entity);
        } catch (Exception e) {
            log.error("[容量控制] 更新数据库失败: controlId={}, count={}, error={}", controlId, count, e.getMessage());
        }
    }

    /**
     * 转换为VO
     */
    private AccessCapacityControlVO convertToVO(AccessCapacityControlEntity entity) {
        AccessCapacityControlVO vo = new AccessCapacityControlVO();
        vo.setControlId(entity.getControlId());
        vo.setAreaId(entity.getAreaId());
        vo.setMaxCapacity(entity.getMaxCapacity());
        vo.setCurrentCount(entity.getCurrentCount());
        vo.setControlMode(entity.getControlMode());
        vo.setAlertThreshold(entity.getAlertThreshold());
        vo.setEntryBlocked(entity.getEntryBlocked());
        vo.setEnabled(entity.getEnabled());
        vo.setRemarks(entity.getDescription());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
