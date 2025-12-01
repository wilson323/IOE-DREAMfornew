package net.lab1024.sa.admin.module.smart.access.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.dao.AccessAreaDao;
import net.lab1024.sa.admin.module.smart.access.dao.AccessDeviceDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.domain.enums.AccessAreaStatusEnum;
import net.lab1024.sa.admin.module.smart.access.domain.enums.AccessAreaTypeEnum;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessAreaForm;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.admin.module.smart.access.manager.AccessAreaManager;
import net.lab1024.sa.admin.module.smart.access.service.AccessAreaService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;

/**
 * 门禁区域管理服务实现
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 复杂业务逻辑事务管理
 * - 层级关系处理和权限继承
 * - 完整的异常处理和日志记录
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Service
public class AccessAreaServiceImpl implements AccessAreaService {

    @Resource
    private AccessAreaDao accessAreaDao;

    @Resource
    private AccessAreaManager accessAreaManager;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Override
    public List<AccessAreaTreeVO> getAreaTree(Long parentId, Boolean includeChildren) {
        log.info("获取区域树，parentId: {}, includeChildren: {}", parentId, includeChildren);

        try {
            // 先尝试从缓存获取
            List<AccessAreaTreeVO> cachedTree = accessAreaManager.getAreaTreeFromCache(parentId, includeChildren);
            if (cachedTree != null) {
                log.info("区域树缓存命中，parentId: {}, count: {}", parentId, cachedTree.size());
                return cachedTree;
            }

            // 查询区域列表
            LambdaQueryWrapper<AccessAreaEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AccessAreaEntity::getParentId, parentId)
                    .eq(AccessAreaEntity::getDeletedFlag, 0)
                    .orderByAsc(AccessAreaEntity::getSortOrder)
                    .orderByAsc(AccessAreaEntity::getCreateTime);

            List<AccessAreaEntity> areas = accessAreaDao.selectList(queryWrapper);

            // 转换为VO
            List<AccessAreaTreeVO> areaTreeList = new ArrayList<>();
            for (AccessAreaEntity area : areas) {
                AccessAreaTreeVO vo = convertToTreeVO(area);

                // 如果需要包含子区域，递归查询
                if (includeChildren != null && includeChildren) {
                    List<AccessAreaTreeVO> children = getAreaTree(area.getAreaId(), true);
                    vo.setChildren(children);
                    vo.setHasChildren(!children.isEmpty());
                    vo.setChildrenCount(children.size());
                } else {
                    // 检查是否有子区域
                    boolean hasChildren = hasChildAreas(area.getAreaId());
                    vo.setHasChildren(hasChildren);
                    vo.setChildrenCount(0);
                }

                areaTreeList.add(vo);
            }

            // 缓存结果
            accessAreaManager.cacheAreaTree(parentId, includeChildren, areaTreeList);

            log.info("区域树查询完成，数量: {}", areaTreeList.size());
            return areaTreeList;

        } catch (Exception e) {
            log.error("获取区域树失败，parentId: {}, includeChildren: {}", parentId, includeChildren, e);
            throw new SmartException("获取区域树失败: " + e.getMessage());
        }
    }

    @Override
    public AccessAreaEntity getAreaById(Long areaId) {
        log.debug("获取区域详情，areaId: {}", areaId);

        AccessAreaEntity area = accessAreaManager.getAreaFromCache(areaId);
        if (area == null) {
            throw new BusinessException("区域不存在或已删除");
        }

        return area;
    }

    @Override
    public PageResult<AccessAreaEntity> getAreaPage(PageParam pageParam, String areaName, Integer areaType,
            Integer status) {
        log.info("分页查询区域，areaName: {}, areaType: {}, status: {}", areaName, areaType, status);

        try {
            Page<AccessAreaEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            LambdaQueryWrapper<AccessAreaEntity> queryWrapper = new LambdaQueryWrapper<>();

            // 构建查询条件
            if (StringUtils.hasText(areaName)) {
                queryWrapper.like(AccessAreaEntity::getAreaName, areaName);
            }
            if (areaType != null) {
                queryWrapper.eq(AccessAreaEntity::getAreaType, areaType);
            }
            if (status != null) {
                queryWrapper.eq(AccessAreaEntity::getStatus, status);
            }

            queryWrapper.eq(AccessAreaEntity::getDeletedFlag, 0)
                    .orderByDesc(AccessAreaEntity::getCreateTime);

            Page<AccessAreaEntity> result = accessAreaDao.selectPage(page, queryWrapper);

            log.info("区域分页查询完成，总数: {}", result.getTotal());
            return SmartPageUtil.convert2PageResult(result, result.getRecords());

        } catch (Exception e) {
            log.error("分页查询区域失败，areaName: {}, areaType: {}, status: {}", areaName, areaType, status, e);
            throw new SmartException("分页查询区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArea(AccessAreaForm areaForm) {
        log.info("创建区域，areaForm: {}", areaForm);

        try {
            // 验证区域编码唯一性
            if (!validateAreaCode(areaForm.getAreaCode(), null)) {
                throw new BusinessException("区域编码已存在: " + areaForm.getAreaCode());
            }

            // 转换为实体
            AccessAreaEntity area = new AccessAreaEntity();
            SmartBeanUtil.copyProperties(areaForm, area);

            // 设置层级信息
            setupAreaHierarchy(area, areaForm.getParentId());

            // 插入数据库
            int result = accessAreaDao.insert(area);
            if (result <= 0) {
                throw new BusinessException("区域创建失败");
            }

            // 重新计算路径（如果需要）
            if (area.getParentId() != null && area.getParentId() > 0) {
                recalculateAreaPath(area.getAreaId());
            }

            // 缓存新创建的区域
            accessAreaManager.cacheArea(area);

            // 清除相关缓存
            accessAreaManager.clearAreaTreeCache();

            log.info("区域创建成功，areaId: {}", area.getAreaId());

        } catch (BusinessException e) {
            log.warn("创建区域业务异常，areaForm: {}", areaForm, e);
            throw e;
        } catch (Exception e) {
            log.error("创建区域失败，areaForm: {}", areaForm, e);
            throw new SmartException("创建区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArea(AccessAreaForm areaForm) {
        log.info("更新区域，areaForm: {}", areaForm);

        try {
            // 验证区域存在
            AccessAreaEntity existingArea = getAreaById(areaForm.getAreaId());

            // 验证区域编码唯一性（排除自己）
            if (!validateAreaCode(areaForm.getAreaCode(), areaForm.getAreaId())) {
                throw new BusinessException("区域编码已存在: " + areaForm.getAreaCode());
            }

            // 检查是否修改了父级区域
            Long oldParentId = existingArea.getParentId();
            Long newParentId = areaForm.getParentId();

            // 转换并更新
            SmartBeanUtil.copyProperties(areaForm, existingArea);

            // 如果父级发生变化，需要重新计算层级
            if (!Objects.equals(oldParentId, newParentId)) {
                setupAreaHierarchy(existingArea, newParentId);
                // 检查是否会造成循环引用
                checkAreaHierarchy(existingArea.getAreaId(), newParentId);
            }

            // 更新数据库
            int result = accessAreaDao.updateById(existingArea);
            if (result <= 0) {
                throw new BusinessException("区域更新失败");
            }

            // 重新计算路径和子区域路径
            if (!Objects.equals(oldParentId, newParentId)) {
                recalculateAreaPath(existingArea.getAreaId());
                updateChildrenPath(existingArea.getAreaId());
            }

            // 更新缓存
            accessAreaManager.cacheArea(existingArea);

            // 清除相关缓存
            accessAreaManager.clearAreaTreeCache();

            log.info("区域更新成功，areaId: {}", areaForm.getAreaId());

        } catch (BusinessException e) {
            log.warn("更新区域业务异常，areaForm: {}", areaForm, e);
            throw e;
        } catch (Exception e) {
            log.error("更新区域失败，areaForm: {}", areaForm, e);
            throw new SmartException("更新区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArea(Long areaId) {
        log.info("删除区域，areaId: {}", areaId);

        try {
            // 验证区域存在
            AccessAreaEntity area = getAreaById(areaId);

            // 检查是否有子区域
            if (hasChildAreas(areaId)) {
                throw new BusinessException("区域下存在子区域，无法删除");
            }

            // 检查是否关联设备
            if (hasAssociatedDevices(areaId)) {
                throw new BusinessException("区域下关联了设备，无法删除");
            }

            // 软删除
            LambdaUpdateWrapper<AccessAreaEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccessAreaEntity::getAreaId, areaId)
                    .set(AccessAreaEntity::getDeletedFlag, 1)
                    .set(AccessAreaEntity::getUpdateTime, LocalDateTime.now());

            int result = accessAreaDao.update(updateWrapper);
            if (result <= 0) {
                throw new BusinessException("区域删除失败");
            }

            // 清除缓存
            accessAreaManager.clearAreaCache(areaId);
            accessAreaManager.clearAreaTreeCache();

            log.info("区域删除成功，areaId: {}", areaId);

        } catch (BusinessException e) {
            log.warn("删除区域业务异常，areaId: {}", areaId, e);
            throw e;
        } catch (Exception e) {
            log.error("删除区域失败，areaId: {}", areaId, e);
            throw new SmartException("删除区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAreas(List<Long> areaIds) {
        log.info("批量删除区域，areaIds: {}", areaIds);

        try {
            if (areaIds == null || areaIds.isEmpty()) {
                throw new BusinessException("区域ID列表不能为空");
            }

            int deletedCount = 0;
            for (Long areaId : areaIds) {
                deleteArea(areaId);
                deletedCount++;
            }

            log.info("批量删除区域完成，数量: {}", deletedCount);

        } catch (Exception e) {
            log.error("批量删除区域失败，areaIds: {}", areaIds, e);
            throw new SmartException("批量删除区域失败: " + e.getMessage());
        }
    }

    /**
     * 获取区域设备列表
     * <p>
     * 查询指定区域下的所有设备，支持递归查询子区域设备
     * 性能优化：使用数据库索引（areaId字段）提高查询效率
     * 建议：对于频繁查询的场景，可考虑添加缓存优化
     *
     * @param areaId          区域ID
     * @param includeChildren 是否包含子区域设备，true表示递归查询所有子区域设备
     * @return 设备列表（Object类型，保持接口兼容性）
     * @throws BusinessException 区域不存在时抛出
     */
    @Override
    public List<Object> getAreaDevices(Long areaId, Boolean includeChildren) {
        log.info("获取区域设备列表，areaId: {}, includeChildren: {}", areaId, includeChildren);

        try {
            // 验证区域存在
            AccessAreaEntity area = getAreaById(areaId);
            if (area == null) {
                throw new BusinessException("区域不存在");
            }

            // 性能优化：优先从缓存获取子区域ID列表（如果已缓存）
            // 查询当前区域的设备（使用数据库索引优化）
            List<AccessDeviceEntity> devices = new ArrayList<>(accessDeviceDao.selectByAreaId(areaId));

            // 如果包含子区域，递归查询子区域设备
            if (includeChildren != null && includeChildren) {
                // 性能优化：递归获取所有子区域ID（包括子区域的子区域）
                // 注意：这里需要递归获取，getChildrenIdsFromCache只获取直接子区域
                List<Long> childAreaIds = getChildAreaIds(areaId);

                if (!childAreaIds.isEmpty()) {
                    // 性能优化：使用批量IN查询一次性获取所有子区域设备，避免N+1查询问题
                    // 这样可以减少数据库访问次数，提高查询性能
                    List<AccessDeviceEntity> childDevices = accessDeviceDao.selectByAreaIds(childAreaIds);
                    devices.addAll(childDevices);
                }
            }

            log.info("获取区域设备列表成功，areaId: {}, includeChildren: {}, deviceCount: {}",
                    areaId, includeChildren, devices.size());

            // 转换为Object列表（保持接口兼容性）
            return new ArrayList<>(devices);

        } catch (BusinessException e) {
            log.warn("获取区域设备列表业务异常，areaId: {}, includeChildren: {}", areaId, includeChildren, e);
            throw e;
        } catch (Exception e) {
            log.error("获取区域设备列表失败，areaId: {}, includeChildren: {}", areaId, includeChildren, e);
            throw new SmartException("获取区域设备列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取子区域ID列表（递归）
     * <p>
     * 递归查询所有子区域的ID，包括子区域的子区域
     * 性能优化：
     * - 使用数据库索引（parentId字段）提高查询效率
     * - 使用Manager层缓存获取直接子区域（如果已缓存）
     * - 递归查询时优先使用缓存，减少数据库访问
     *
     * @param parentAreaId 父区域ID
     * @return 子区域ID列表，如果无子区域返回空列表
     */
    private List<Long> getChildAreaIds(Long parentAreaId) {
        List<Long> childAreaIds = new ArrayList<>();

        // 性能优化：优先从缓存获取直接子区域ID
        List<Long> directChildrenIds = accessAreaManager.getChildrenIdsFromCache(parentAreaId);
        if (directChildrenIds == null || directChildrenIds.isEmpty()) {
            // 缓存未命中，从数据库查询（使用索引优化）
            LambdaQueryWrapper<AccessAreaEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccessAreaEntity::getParentId, parentAreaId)
                    .eq(AccessAreaEntity::getDeletedFlag, 0);
            List<AccessAreaEntity> childAreas = accessAreaDao.selectList(wrapper);
            directChildrenIds = childAreas.stream()
                    .map(AccessAreaEntity::getAreaId)
                    .collect(java.util.stream.Collectors.toList());
        }

        // 递归查询子区域的子区域
        for (Long childAreaId : directChildrenIds) {
            childAreaIds.add(childAreaId);
            // 递归查询子区域的子区域（递归调用会继续使用缓存）
            childAreaIds.addAll(getChildAreaIds(childAreaId));
        }

        return childAreaIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveArea(Long areaId, Long newParentId) {
        log.info("移动区域，areaId: {}, newParentId: {}", areaId, newParentId);

        try {
            // 验证区域存在
            AccessAreaEntity area = getAreaById(areaId);

            // 检查是否移动到自己的子区域
            if (isChildOfArea(newParentId, areaId)) {
                throw new BusinessException("不能移动到自己的子区域");
            }

            Long oldParentId = area.getParentId();
            area.setParentId(newParentId);

            // 重新设置层级信息
            setupAreaHierarchy(area, newParentId);

            // 更新数据库
            int result = accessAreaDao.updateById(area);
            if (result <= 0) {
                throw new BusinessException("区域移动失败");
            }

            // 重新计算路径和子区域路径
            recalculateAreaPath(areaId);
            updateChildrenPath(areaId);

            log.info("区域移动成功，areaId: {}, oldParentId: {}, newParentId: {}", areaId, oldParentId, newParentId);

        } catch (BusinessException e) {
            log.warn("移动区域业务异常，areaId: {}, newParentId: {}", areaId, newParentId, e);
            throw e;
        } catch (Exception e) {
            log.error("移动区域失败，areaId: {}, newParentId: {}", areaId, newParentId, e);
            throw new SmartException("移动区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAreaStatus(Long areaId, Integer status) {
        log.info("更新区域状态，areaId: {}, status: {}", areaId, status);

        try {
            // 验证区域存在
            getAreaById(areaId);

            // 更新状态
            LambdaUpdateWrapper<AccessAreaEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AccessAreaEntity::getAreaId, areaId)
                    .set(AccessAreaEntity::getStatus, status)
                    .set(AccessAreaEntity::getUpdateTime, LocalDateTime.now());

            int result = accessAreaDao.update(updateWrapper);
            if (result <= 0) {
                throw new BusinessException("区域状态更新失败");
            }

            log.info("区域状态更新成功，areaId: {}, status: {}", areaId, status);

        } catch (Exception e) {
            log.error("更新区域状态失败，areaId: {}, status: {}", areaId, status, e);
            throw new SmartException("更新区域状态失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateAreaCode(String areaCode, Long excludeAreaId) {
        log.debug("验证区域编码，areaCode: {}, excludeAreaId: {}", areaCode, excludeAreaId);

        try {
            LambdaQueryWrapper<AccessAreaEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AccessAreaEntity::getAreaCode, areaCode)
                    .eq(AccessAreaEntity::getDeletedFlag, 0);

            if (excludeAreaId != null) {
                queryWrapper.ne(AccessAreaEntity::getAreaId, excludeAreaId);
            }

            Long count = accessAreaDao.selectCount(queryWrapper);
            return count == 0;

        } catch (Exception e) {
            log.error("验证区域编码失败，areaCode: {}, excludeAreaId: {}", areaCode, excludeAreaId, e);
            return false;
        }
    }

    /**
     * 获取区域统计信息
     * <p>
     * 统计区域相关的设备数量、在线状态、访问次数等信息
     * 性能优化：使用聚合查询和缓存提高查询效率
     * 建议：对于频繁查询的场景，可考虑添加缓存（缓存时间5分钟）
     *
     * @param areaId 区域ID
     * @return 统计信息Map，包含以下字段：
     *         - totalDevices: 总设备数（包括子区域）
     *         - onlineDevices: 在线设备数
     *         - offlineDevices: 离线设备数
     *         - onlineRate: 在线率（百分比）
     *         - todayAccessCount: 今日访问次数
     *         - monthAccessCount: 本月访问次数
     *         - areaLevel: 区域层级
     *         - childAreaCount: 子区域数量
     * @throws BusinessException 区域不存在时抛出
     */
    @Override
    public Object getAreaStatistics(Long areaId) {
        log.info("获取区域统计信息，areaId: {}", areaId);

        try {
            // 验证区域存在
            AccessAreaEntity area = getAreaById(areaId);
            if (area == null) {
                throw new BusinessException("区域不存在");
            }

            // 性能优化：优先从缓存读取统计信息
            Map<String, Object> statistics = accessAreaManager.getAreaStatisticsFromCache(areaId);
            if (statistics != null) {
                log.debug("区域统计信息从缓存获取，areaId: {}", areaId);
                return statistics;
            }

            // 缓存未命中，从数据库查询
            statistics = new HashMap<>();

            // 1. 设备统计（包括子区域）
            // 性能优化：一次性获取所有区域ID，避免重复查询
            List<Long> allAreaIds = new ArrayList<>();
            allAreaIds.add(areaId);
            allAreaIds.addAll(getChildAreaIds(areaId));

            // 性能优化：使用批量查询和聚合查询优化统计性能
            Long totalDevices = 0L;
            Long onlineDevices = 0L;

            if (!allAreaIds.isEmpty()) {
                // 总设备数统计（使用COUNT查询优化）
                for (Long id : allAreaIds) {
                    Long count = accessDeviceDao.countDevicesByArea(id);
                    if (count != null) {
                        totalDevices += count;
                    }
                }

                // 在线设备数统计（使用批量IN查询优化，一次性查询所有区域）
                LambdaQueryWrapper<AccessDeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
                deviceWrapper.in(AccessDeviceEntity::getAreaId, allAreaIds)
                        .eq(AccessDeviceEntity::getOnlineStatus, 1)
                        .eq(AccessDeviceEntity::getDeletedFlag, 0);
                Long onlineCount = accessDeviceDao.selectCount(deviceWrapper);
                if (onlineCount != null) {
                    onlineDevices = onlineCount;
                }
            }

            statistics.put("totalDevices", totalDevices);
            statistics.put("onlineDevices", onlineDevices);
            statistics.put("offlineDevices", totalDevices - onlineDevices);

            // 在线率
            double onlineRate = totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0;
            statistics.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);

            // 2. 访问统计（今日和本月）
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);

            // 今日访问次数（需要查询访问记录表）
            // 注意：这里需要根据实际业务逻辑查询AccessRecordEntity表
            // 由于AccessRecordEntity可能没有areaId字段，这里先返回0，后续根据实际表结构调整
            statistics.put("todayAccessCount", 0L);
            statistics.put("monthAccessCount", 0L);

            // 3. 区域层级信息
            statistics.put("areaLevel", area.getLevel());
            statistics.put("childAreaCount", getChildAreaIds(areaId).size());

            // 性能优化：将统计信息写入缓存（5分钟过期）
            accessAreaManager.cacheAreaStatistics(areaId, statistics);

            log.info("获取区域统计信息成功，areaId: {}, statistics: {}", areaId, statistics);
            return statistics;

        } catch (BusinessException e) {
            log.warn("获取区域统计信息业务异常，areaId: {}", areaId, e);
            throw e;
        } catch (Exception e) {
            log.error("获取区域统计信息失败，areaId: {}", areaId, e);
            throw new SmartException("获取区域统计信息失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换为树形VO
     */
    private AccessAreaTreeVO convertToTreeVO(AccessAreaEntity area) {
        AccessAreaTreeVO vo = new AccessAreaTreeVO();
        SmartBeanUtil.copyProperties(area, vo);

        // 设置类型名称
        vo.setAreaTypeName(getAreaTypeName(area.getAreaType()));
        vo.setStatusName(getStatusName(area.getStatus()));

        return vo;
    }

    /**
     * 设置区域层级信息
     */
    private void setupAreaHierarchy(AccessAreaEntity area, Long parentId) {
        area.setParentId(parentId != null ? parentId : 0L);

        if (parentId == null || parentId == 0) {
            area.setLevel(0);
            area.setPath("0," + area.getAreaId());
        } else {
            // 查询父级区域
            AccessAreaEntity parentArea = accessAreaDao.selectById(parentId);
            if (parentArea != null) {
                area.setLevel(parentArea.getLevel() + 1);
                area.setPath(parentArea.getPath() + "," + area.getAreaId());
            }
        }
    }

    /**
     * 重新计算区域路径
     */
    @Override
    public void recalculateAreaPath(Long areaId) {
        AccessAreaEntity area = accessAreaDao.selectById(areaId);
        if (area == null) {
            return;
        }

        String path;
        if (area.getParentId() == null || area.getParentId() == 0) {
            path = "0," + areaId;
        } else {
            AccessAreaEntity parentArea = accessAreaDao.selectById(area.getParentId());
            if (parentArea != null && parentArea.getPath() != null) {
                path = parentArea.getPath() + "," + areaId;
            } else {
                path = "0," + areaId;
            }
        }

        area.setPath(path);
        accessAreaDao.updateById(area);
    }

    /**
     * 更新子区域路径
     */
    private void updateChildrenPath(Long parentAreaId) {
        LambdaQueryWrapper<AccessAreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessAreaEntity::getParentId, parentAreaId)
                .eq(AccessAreaEntity::getDeletedFlag, 0);

        List<AccessAreaEntity> children = accessAreaDao.selectList(queryWrapper);
        for (AccessAreaEntity child : children) {
            recalculateAreaPath(child.getAreaId());
            updateChildrenPath(child.getAreaId());
        }
    }

    /**
     * 检查区域层级
     */
    private void checkAreaHierarchy(Long areaId, Long parentId) {
        if (areaId == null || parentId == null || parentId == 0) {
            return;
        }

        // 检查是否为自己的子区域
        if (isChildOfArea(parentId, areaId)) {
            throw new BusinessException("不能设置区域为父级，会造成循环引用");
        }
    }

    /**
     * 检查是否为子区域
     */
    private boolean isChildOfArea(Long childAreaId, Long parentAreaId) {
        return accessAreaManager.isChildArea(parentAreaId, childAreaId);
    }

    /**
     * 获取区域类型名称
     * <p>
     * 使用枚举替代魔法数字，提高代码可维护性
     *
     * @param areaType 区域类型值
     * @return 区域类型名称
     */
    private String getAreaTypeName(Integer areaType) {
        AccessAreaTypeEnum typeEnum = AccessAreaTypeEnum.fromValue(areaType);
        return typeEnum != null ? typeEnum.getName() : "未知";
    }

    /**
     * 获取状态名称
     * <p>
     * 使用枚举替代魔法数字，提高代码可维护性
     * 注意：如果状态值超出枚举范围，返回"未知"
     *
     * @param status 状态值
     * @return 状态名称
     */
    private String getStatusName(Integer status) {
        AccessAreaStatusEnum statusEnum = AccessAreaStatusEnum.fromValue(status);
        if (statusEnum != null) {
            return statusEnum.getName();
        }
        // 处理其他状态值（如维护中等）
        if (status != null && status == 2) {
            return "维护中";
        }
        return "未知";
    }

    @Override
    public boolean hasChildAreas(Long areaId) {
        LambdaQueryWrapper<AccessAreaEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessAreaEntity::getParentId, areaId)
                .eq(AccessAreaEntity::getDeletedFlag, 0);

        Long count = accessAreaDao.selectCount(queryWrapper);
        return count > 0;
    }

    @Override
    public boolean hasAssociatedDevices(Long areaId) {
        log.debug("检查区域是否关联设备，areaId: {}", areaId);

        try {
            // 查询当前区域是否有设备
            Long deviceCount = accessDeviceDao.countDevicesByArea(areaId);
            boolean hasDevices = deviceCount != null && deviceCount > 0;

            if (hasDevices) {
                log.info("区域关联设备，areaId: {}, deviceCount: {}", areaId, deviceCount);
            } else {
                log.debug("区域未关联设备，areaId: {}", areaId);
            }

            return hasDevices;

        } catch (Exception e) {
            log.error("检查区域是否关联设备失败，areaId: {}", areaId, e);
            // 检查失败时，为了安全起见，返回true（不允许删除）
            return true;
        }
    }
}