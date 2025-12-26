package net.lab1024.sa.common.organization.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.manager.RegionalHierarchyManager;
import net.lab1024.sa.common.organization.service.RegionalHierarchyService;

/**
 * 区域层级管理服务实现
 * 提供五级层级架构：园区→建筑→楼层→区域→房间
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Service
@Slf4j
public class RegionalHierarchyServiceImpl implements RegionalHierarchyService {


    @Resource
    private RegionalHierarchyManager regionalHierarchyManager;
    @Resource
    private AreaDao areaDao;

    @Override
    public ResponseDTO<String> getAreaHierarchyPath(Long areaId) {
        try {
            String path = regionalHierarchyManager.getAreaHierarchyPath(areaId);
            return ResponseDTO.ok(path);
        } catch (Exception e) {
            log.error("获取区域层级路径失败, areaId={}", areaId, e);
            return ResponseDTO.error("GET_PATH_ERROR", "获取区域层级路径失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaEntity>> getChildAreas(Long parentAreaId, Integer areaLevel) {
        try {
            List<AreaEntity> children = regionalHierarchyManager.getChildAreas(parentAreaId, areaLevel);
            return ResponseDTO.ok(children);
        } catch (Exception e) {
            log.error("获取子区域失败, parentAreaId={}, areaLevel={}", parentAreaId, areaLevel, e);
            return ResponseDTO.error("GET_CHILDREN_ERROR", "获取子区域失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaEntity>> getAllChildAreas(Long parentAreaId) {
        try {
            List<AreaEntity> allChildren = regionalHierarchyManager.getAllChildAreas(parentAreaId);
            return ResponseDTO.ok(allChildren);
        } catch (Exception e) {
            log.error("获取所有下级区域失败, parentAreaId={}", parentAreaId, e);
            return ResponseDTO.error("GET_ALL_CHILDREN_ERROR", "获取所有下级区域失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaEntity>> getAllParentAreas(Long areaId) {
        try {
            List<AreaEntity> parents = regionalHierarchyManager.getAllParentAreas(areaId);
            return ResponseDTO.ok(parents);
        } catch (Exception e) {
            log.error("获取所有上级区域失败, areaId={}", areaId, e);
            return ResponseDTO.error("GET_PARENTS_ERROR", "获取所有上级区域失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<RegionalHierarchyManager.HierarchyValidationResult> validateHierarchy(Long areaId) {
        try {
            RegionalHierarchyManager.HierarchyValidationResult result = regionalHierarchyManager
                    .validateHierarchy(areaId);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("验证区域层级结构失败, areaId={}", areaId, e);
            return ResponseDTO.error("VALIDATE_ERROR", "验证区域层级结构失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaEntity>> getSiblingAreas(Long areaId) {
        try {
            List<AreaEntity> siblings = regionalHierarchyManager.getSiblingAreas(areaId);
            return ResponseDTO.ok(siblings);
        } catch (Exception e) {
            log.error("获取同级区域失败, areaId={}", areaId, e);
            return ResponseDTO.error("GET_SIBLINGS_ERROR", "获取同级区域失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<RegionalHierarchyManager.AreaHierarchyTree>> getAreaHierarchyTree(Long rootParentId) {
        try {
            List<RegionalHierarchyManager.AreaHierarchyTree> tree = regionalHierarchyManager
                    .getAreaHierarchyTree(rootParentId);
            return ResponseDTO.ok(tree);
        } catch (Exception e) {
            log.error("获取区域层级树失败, rootParentId={}", rootParentId, e);
            return ResponseDTO.error("GET_TREE_ERROR", "获取区域层级树失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<RegionalHierarchyManager.AreaStatistics> getAreaStatistics(Long parentAreaId) {
        try {
            RegionalHierarchyManager.AreaStatistics statistics = regionalHierarchyManager
                    .getAreaStatistics(parentAreaId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取区域统计信息失败, parentAreaId={}", parentAreaId, e);
            return ResponseDTO.error("GET_STATS_ERROR", "获取区域统计信息失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> canDeleteArea(Long areaId) {
        try {
            boolean canDelete = regionalHierarchyManager.canDeleteArea(areaId);
            return ResponseDTO.ok(canDelete);
        } catch (Exception e) {
            log.error("检查区域是否可删除失败, areaId={}", areaId, e);
            return ResponseDTO.error("CHECK_DELETE_ERROR", "检查区域是否可删除失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaEntity>> getAreasByType(Integer areaType) {
        try {
            List<AreaEntity> areas = regionalHierarchyManager.getAreasByType(areaType);
            return ResponseDTO.ok(areas);
        } catch (Exception e) {
            log.error("根据类型获取区域失败, areaType={}", areaType, e);
            return ResponseDTO.error("GET_BY_TYPE_ERROR", "根据类型获取区域失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<AreaEntity> getAreaByCode(String areaCode) {
        try {
            AreaEntity area = regionalHierarchyManager.getAreaByCode(areaCode);
            if (area == null) {
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }
            return ResponseDTO.ok(area);
        } catch (Exception e) {
            log.error("根据编码获取区域失败, areaCode={}", areaCode, e);
            return ResponseDTO.error("GET_BY_CODE_ERROR", "根据编码获取区域失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaEntity>> searchAreas(String keyword) {
        try {
            // 使用AreaDao进行搜索
            List<AreaEntity> allAreas = areaDao.selectList(null);
            List<AreaEntity> areas = allAreas.stream()
                    .filter(area -> area.getAreaName() != null && area.getAreaName().contains(keyword))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseDTO.ok(areas);
        } catch (Exception e) {
            log.error("搜索区域失败, keyword={}", keyword, e);
            return ResponseDTO.error("SEARCH_ERROR", "搜索区域失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "area:unified:tree", "area:unified:code", "area:unified:path", "area:unified:children",
            "area:unified:access", "area:unified:devices", "area:unified:business_attrs" }, allEntries = true)
    public ResponseDTO<Long> createArea(AreaEntity areaEntity) {
        try {
            // 验证区域编码唯一性
            if (!validateAreaCodeUnique(areaEntity.getAreaCode(), null).getData()) {
                return ResponseDTO.error("AREA_CODE_EXISTS", "区域编码已存在");
            }

            // 验证区域名称在同级下的唯一性
            if (!validateAreaNameUnique(areaEntity.getAreaName(), areaEntity.getParentId(),
                    areaEntity.getLevel(), null).getData()) {
                return ResponseDTO.error("AREA_NAME_EXISTS", "同级区域下已存在相同名称");
            }

            // 验证层级结构
            if (areaEntity.getParentId() != null) {
                RegionalHierarchyManager.HierarchyValidationResult validation = validateHierarchy(
                        areaEntity.getParentId()).getData();
                if (!validation.isValid()) {
                    return ResponseDTO.error("HIERARCHY_INVALID", validation.getMessage());
                }
            }

            // 设置默认值
            areaEntity.setStatus(1); // 默认启用
            areaEntity.setSortOrder(areaEntity.getSortOrder() != null ? areaEntity.getSortOrder() : 0);

            // 插入数据库
            areaDao.insert(areaEntity);

            log.info("创建区域成功, areaId={}, areaName={}", areaEntity.getAreaId(), areaEntity.getAreaName());
            return ResponseDTO.ok(areaEntity.getAreaId());

        } catch (Exception e) {
            log.error("创建区域失败", e);
            return ResponseDTO.error("CREATE_ERROR", "创建区域失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "area:unified:tree", "area:unified:code", "area:unified:path", "area:unified:children",
            "area:unified:access", "area:unified:devices", "area:unified:business_attrs" }, allEntries = true)
    public ResponseDTO<Void> updateArea(AreaEntity areaEntity) {
        try {
            // 检查区域是否存在
            AreaEntity existingArea = areaDao.selectById(areaEntity.getAreaId());
            if (existingArea == null) {
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            // 验证区域编码唯一性（排除自身）
            if (!validateAreaCodeUnique(areaEntity.getAreaCode(), areaEntity.getAreaId()).getData()) {
                return ResponseDTO.error("AREA_CODE_EXISTS", "区域编码已存在");
            }

            // 验证区域名称在同级下的唯一性（排除自身）
            if (!validateAreaNameUnique(areaEntity.getAreaName(), areaEntity.getParentId(),
                    areaEntity.getLevel(), areaEntity.getAreaId()).getData()) {
                return ResponseDTO.error("AREA_NAME_EXISTS", "同级区域下已存在相同名称");
            }

            // 更新数据库
            areaDao.updateById(areaEntity);

            log.info("更新区域成功, areaId={}", areaEntity.getAreaId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("更新区域失败, areaId={}", areaEntity.getAreaId(), e);
            return ResponseDTO.error("UPDATE_ERROR", "更新区域失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "area:unified:tree", "area:unified:code", "area:unified:path", "area:unified:children",
            "area:unified:access", "area:unified:devices", "area:unified:business_attrs" }, allEntries = true)
    public ResponseDTO<Void> deleteArea(Long areaId) {
        try {
            // 检查是否存在子区域
            if (!regionalHierarchyManager.canDeleteArea(areaId)) {
                return ResponseDTO.error("HAS_CHILDREN", "存在子区域，无法删除");
            }

            // 软删除
            AreaEntity areaEntity = new AreaEntity();
            areaEntity.setAreaId(areaId);
            areaEntity.setDeletedFlag(1);
            areaDao.updateById(areaEntity);

            log.info("删除区域成功, areaId={}", areaId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("删除区域失败, areaId={}", areaId, e);
            return ResponseDTO.error("DELETE_ERROR", "删除区域失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = { "area:unified:tree", "area:unified:code", "area:unified:path", "area:unified:children",
            "area:unified:access", "area:unified:devices", "area:unified:business_attrs" }, allEntries = true)
    public ResponseDTO<Void> moveArea(Long areaId, Long newParentId, Integer newLevel) {
        try {
            AreaEntity area = areaDao.selectById(areaId);
            if (area == null) {
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            // 验证新的层级结构
            if (newParentId != null) {
                AreaEntity newParent = areaDao.selectById(newParentId);
                if (newParent == null) {
                    return ResponseDTO.error("PARENT_NOT_FOUND", "新父区域不存在");
                }

                // 验证层级关系
                if (newLevel == null) {
                    newLevel = newParent.getLevel() + 1;
                } else if (newLevel != newParent.getLevel() + 1) {
                    return ResponseDTO.error("INVALID_LEVEL", "新层级级别与父区域层级不匹配");
                }
            }

            // 更新区域信息
            area.setParentId(newParentId);
            if (newLevel != null) {
                area.setLevel(newLevel);
            }
            areaDao.updateById(area);

            log.info("移动区域成功, areaId={}, newParentId={}, newLevel={}", areaId, newParentId, newLevel);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("移动区域失败, areaId={}, newParentId={}", areaId, newParentId, e);
            return ResponseDTO.error("MOVE_ERROR", "移动区域失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> batchOperation(List<Long> areaIds, String operation) {
        try {
            for (Long areaId : areaIds) {
                switch (operation.toLowerCase()) {
                    case "enable":
                        enableArea(areaId);
                        break;
                    case "disable":
                        disableArea(areaId);
                        break;
                    case "delete":
                        deleteArea(areaId);
                        break;
                    default:
                        return ResponseDTO.error("INVALID_OPERATION", "不支持的操作类型：" + operation);
                }
            }

            log.info("批量操作区域成功, operation={}, count={}", operation, areaIds.size());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("批量操作区域失败, operation={}, areaIds={}", operation, areaIds, e);
            return ResponseDTO.error("BATCH_ERROR", "批量操作区域失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getHierarchyConfig() {
        try {
            Map<String, Object> config = new HashMap<>();

            // 区域类型配置
            Map<String, Object> areaTypes = new HashMap<>();
            areaTypes.put("1", "园区");
            areaTypes.put("2", "建筑");
            areaTypes.put("3", "楼层");
            areaTypes.put("4", "区域");
            areaTypes.put("5", "房间");
            config.put("areaTypes", areaTypes);

            // 层级级别配置
            Map<String, Object> areaLevels = new HashMap<>();
            areaLevels.put("1", "第一级（园区）");
            areaLevels.put("2", "第二级（建筑）");
            areaLevels.put("3", "第三级（楼层）");
            areaLevels.put("4", "第四级（区域）");
            areaLevels.put("5", "第五级（房间）");
            config.put("areaLevels", areaLevels);

            // 规则配置
            config.put("maxLevel", 5);
            config.put("allowSameNameInDifferentLevel", false);
            config.put("allowEmptyParentInTopLevel", true);

            return ResponseDTO.ok(config);

        } catch (Exception e) {
            log.error("获取层级配置失败", e);
            return ResponseDTO.error("GET_CONFIG_ERROR", "获取层级配置失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> validateAreaNameUnique(String areaName, Long parentAreaId, Integer areaLevel,
            Long excludeAreaId) {
        try {
            // 手动实现唯一性验证
            List<AreaEntity> areas = areaDao.selectByParentId(parentAreaId);
            boolean exists = areas.stream()
                    .anyMatch(area -> area.getAreaName().equals(areaName)
                            && areaLevel.equals(area.getLevel())
                            && (excludeAreaId == null || !area.getAreaId().equals(excludeAreaId)));
            return ResponseDTO.ok(!exists);
        } catch (Exception e) {
            log.error("验证区域名称唯一性失败, areaName={}, parentAreaId={}, areaLevel={}", areaName, parentAreaId, areaLevel, e);
            return ResponseDTO.error("VALIDATE_NAME_ERROR", "验证区域名称唯一性失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> validateAreaCodeUnique(String areaCode, Long excludeAreaId) {
        try {
            // 手动实现唯一性验证
            AreaEntity existing = areaDao.selectByAreaCode(areaCode);
            boolean exists = existing != null && (excludeAreaId == null || !existing.getAreaId().equals(excludeAreaId));
            return ResponseDTO.ok(!exists);
        } catch (Exception e) {
            log.error("验证区域编码唯一性失败, areaCode={}", areaCode, e);
            return ResponseDTO.error("VALIDATE_CODE_ERROR", "验证区域编码唯一性失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Long>> getAreaPathIds(Long areaId) {
        try {
            // 手动实现路径获取
            List<AreaEntity> pathAreas = regionalHierarchyManager.getAllParentAreas(areaId);
            AreaEntity currentArea = areaDao.selectById(areaId);
            if (currentArea != null) {
                pathAreas.add(currentArea);
            }
            List<Long> pathIds = pathAreas.stream()
                    .sorted(Comparator.comparing(AreaEntity::getLevel))
                    .map(AreaEntity::getAreaId)
                    .collect(Collectors.toList());
            return ResponseDTO.ok(pathIds);
        } catch (Exception e) {
            log.error("获取区域路径ID列表失败, areaId={}", areaId, e);
            return ResponseDTO.error("GET_PATH_IDS_ERROR", "获取区域路径ID列表失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> checkUserAreaPermission(Long userId, Long areaId) {
        try {
            // 检查直接权限
            boolean hasDirectAccess = areaDao.hasDirectAccess(userId, areaId);
            if (hasDirectAccess) {
                return ResponseDTO.ok(true);
            }

            // 检查继承权限（如果对父区域有权限，则对子区域也有权限）
            List<AreaEntity> parentAreas = getAllParentAreas(areaId).getData();
            for (AreaEntity parentArea : parentAreas) {
                if (areaDao.hasDirectAccess(userId, parentArea.getAreaId())) {
                    return ResponseDTO.ok(true);
                }
            }

            return ResponseDTO.ok(false);

        } catch (Exception e) {
            log.error("检查用户区域权限失败, userId={}, areaId={}", userId, areaId, e);
            return ResponseDTO.error("CHECK_PERMISSION_ERROR", "检查用户区域权限失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaEntity>> getUserAccessibleAreas(Long userId) {
        try {
            List<Long> accessibleAreaIds = areaDao.selectAccessibleAreaIds(userId);
            if (accessibleAreaIds.isEmpty()) {
                return ResponseDTO.ok(new ArrayList<>());
            }

            // 使用selectList和LambdaQueryWrapper替代已弃用的selectBatchIds
            List<AreaEntity> accessibleAreas = new ArrayList<>();
            for (Long areaId : accessibleAreaIds) {
                AreaEntity area = areaDao.selectById(areaId);
                if (area != null) {
                    accessibleAreas.add(area);
                }
            }
            return ResponseDTO.ok(accessibleAreas);

        } catch (Exception e) {
            log.error("获取用户可访问区域列表失败, userId={}", userId, e);
            return ResponseDTO.error("GET_ACCESSIBLE_AREAS_ERROR", "获取用户可访问区域列表失败：" + e.getMessage());
        }
    }

    /**
     * 启用区域
     */
    private void enableArea(Long areaId) {
        AreaEntity area = new AreaEntity();
        area.setAreaId(areaId);
        area.setStatus(1);
        areaDao.updateById(area);
    }

    /**
     * 禁用区域
     */
    private void disableArea(Long areaId) {
        AreaEntity area = new AreaEntity();
        area.setAreaId(areaId);
        area.setStatus(0);
        areaDao.updateById(area);
    }
}
