package net.lab1024.sa.common.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统一区域空间管理服务实现类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AreaUnifiedServiceImpl implements AreaUnifiedService {

    @Resource
    private AreaDao areaDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Observed(name = "area.getAreaTree", contextualName = "area-get-tree")
    @Cacheable(value = "area:unified:tree", unless = "#result == null || #result.isEmpty()")
    public List<AreaEntity> getAreaTree() {
        log.debug("[区域统一服务] 获取完整区域树");
        List<AreaEntity> allAreas = areaDao.selectList(null);
        List<AreaEntity> areaTree = buildAreaTree(allAreas);
        log.debug("[区域统一服务] 区域树构建完成，根节点数量: {}", areaTree.size());
        return areaTree;
    }

    @Override
    @Observed(name = "area.getUserAccessibleAreas", contextualName = "area-get-user-accessible")
    @Cacheable(value = "area:unified:user_areas", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<AreaEntity> getUserAccessibleAreas(Long userId) {
        log.debug("[区域统一服务] 获取用户可访问区域, userId={}", userId);

        // 从用户-区域关联表获取可访问区域
        List<Long> accessibleAreaIds = areaDao.selectAccessibleAreaIds(userId);

        if (accessibleAreaIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用selectList方法替代已废弃的selectBatchIds方法
        // AreaEntity主键字段已统一为id，符合实体类主键命名规范
        LambdaQueryWrapper<AreaEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AreaEntity::getId, accessibleAreaIds);
        List<AreaEntity> accessibleAreas = areaDao.selectList(wrapper);

        // 过滤禁用的区域
        List<AreaEntity> activeAreas = accessibleAreas.stream()
                .filter(area -> area.getStatus() != null && area.getStatus() == 1)
                .collect(Collectors.toList());

        log.debug("[区域统一服务] 用户可访问区域获取完成, userId={}, areaCount={}",
                userId, activeAreas.size());
        return activeAreas;
    }

    @Override
    @Observed(name = "area.hasAreaAccess", contextualName = "area-has-access")
    @Cacheable(value = "area:unified:access", key = "#userId + ':' + #areaId")
    public boolean hasAreaAccess(Long userId, Long areaId) {
        log.debug("[区域统一服务] 检查用户区域权限, userId={}, areaId={}", userId, areaId);

        // 检查用户是否有该区域的直接权限
        boolean hasDirectAccess = areaDao.hasDirectAccess(userId, areaId);

        if (hasDirectAccess) {
            return true;
        }

        // 检查用户是否有父区域的权限（继承父区域权限）
        AreaEntity currentArea = areaDao.selectById(areaId);
        if (currentArea == null || currentArea.getParentId() == null || currentArea.getParentId() == 0) {
            return false;
        }

        return hasAreaAccess(userId, currentArea.getParentId());
    }

    @Override
    @Observed(name = "area.getAreaByCode", contextualName = "area-get-by-code")
    @Cacheable(value = "area:unified:code", key = "#areaCode", unless = "#result == null")
    public AreaEntity getAreaByCode(String areaCode) {
        log.debug("[区域统一服务] 根据编码获取区域, areaCode={}", areaCode);
        return areaDao.selectByCode(areaCode);
    }

    @Override
    @Observed(name = "area.getAreaPath", contextualName = "area-get-path")
    @Cacheable(value = "area:unified:path", key = "#areaId", unless = "#result == null || #result.isEmpty()")
    public List<AreaEntity> getAreaPath(Long areaId) {
        log.debug("[区域统一服务] 获取区域路径, areaId={}", areaId);

        // 优化：使用单次查询获取所有父区域，避免循环查询
        AreaEntity currentArea = areaDao.selectById(areaId);
        if (currentArea == null) {
            return new ArrayList<>();
        }

        // TODO: 优化为单次查询获取所有父区域（使用递归CTE或批量查询）
        // 当前实现：循环查询父区域（性能较差，但逻辑清晰）
        List<AreaEntity> path = new ArrayList<>();
        path.add(currentArea);

        Long parentId = currentArea.getParentId();
        while (parentId != null && parentId > 0) {
            AreaEntity parentArea = areaDao.selectById(parentId);
            if (parentArea == null) {
                break;
            }

            path.add(0, parentArea); // 插入到列表开头
            parentId = parentArea.getParentId();
        }

        return path;
    }

    @Override
    @Observed(name = "area.getChildAreas", contextualName = "area-get-children")
    @Cacheable(value = "area:unified:children", key = "#parentAreaId", unless = "#result == null || #result.isEmpty()")
    public List<AreaEntity> getChildAreas(Long parentAreaId) {
        log.debug("[区域统一服务] 获取子区域, parentAreaId={}", parentAreaId);

        return areaDao.selectChildAreas(parentAreaId);
    }

    @Override
    @Observed(name = "area.getAreaBusinessAttributes", contextualName = "area-get-business-attributes")
    @Cacheable(value = "area:unified:business_attrs", key = "#areaId + ':' + #businessModule", unless = "#result == null || #result.isEmpty()")
    public Map<String, Object> getAreaBusinessAttributes(Long areaId, String businessModule) {
        log.debug("[区域统一服务] 获取区域业务属性, areaId={}, businessModule={}", areaId, businessModule);

        String attributesStr = areaDao.selectBusinessAttributes(areaId, businessModule);
        Map<String, Object> attributes = null;

        if (attributesStr != null && !attributesStr.trim().isEmpty()) {
            try {
                attributes = objectMapper.readValue(attributesStr, new TypeReference<Map<String, Object>>() {});
            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[区域统一服务] 解析业务属性参数异常: areaId={}, businessModule={}, error={}", areaId, businessModule, e.getMessage());
            } catch (Exception e) {
                log.warn("[区域统一服务] 解析业务属性系统异常: areaId={}, businessModule={}", areaId, businessModule, e);
            }
        }

        return attributes;
    }

    @Override
    @Observed(name = "area.setAreaBusinessAttributes", contextualName = "area-set-business-attributes")
    public boolean setAreaBusinessAttributes(Long areaId, String businessModule, Map<String, Object> attributes) {
        log.info("[区域统一服务] 设置区域业务属性, areaId={}, businessModule={}", areaId, businessModule);

        try {
            String attributesJson = objectMapper.writeValueAsString(attributes);
            int result = areaDao.updateBusinessAttributes(areaId, businessModule, attributesJson);

            if (result > 0) {
                // 清除相关缓存
                evictAreaBusinessAttributesCache(areaId, businessModule);
                log.info("[区域统一服务] 区域业务属性设置成功, areaId={}, businessModule={}", areaId, businessModule);
                return true;
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[区域统一服务] 设置区域业务属性参数异常, areaId={}, businessModule={}, error={}", areaId, businessModule, e.getMessage());
        } catch (BusinessException e) {
            log.warn("[区域统一服务] 设置区域业务属性业务异常, areaId={}, businessModule={}, error={}", areaId, businessModule, e.getMessage());
        } catch (Exception e) {
            log.error("[区域统一服务] 设置区域业务属性系统异常, areaId={}, businessModule={}", areaId, businessModule, e);
        }

        return false;
    }

    @Override
    @Observed(name = "area.getAreaDevices", contextualName = "area-get-devices")
    @Cacheable(value = "area:unified:devices", key = "#areaId + ':' + (#deviceType != null ? #deviceType : 'all')", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getAreaDevices(Long areaId, String deviceType) {
        log.debug("[区域统一服务] 获取区域设备, areaId={}, deviceType={}", areaId, deviceType);

            // 获取设备ID列表
        List<Long> deviceIds = areaDao.selectAreaDevices(areaId, deviceType);

        // 转换为 Map<String, Object> 格式
        return deviceIds.stream()
                .map(deviceId -> {
                    Map<String, Object> deviceMap = new java.util.HashMap<>();
                    deviceMap.put("deviceId", deviceId);
                    return deviceMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Observed(name = "area.getAreaStatistics", contextualName = "area-get-statistics")
    public Map<String, Object> getAreaStatistics(Long areaId) {
        log.debug("[区域统一服务] 获取区域统计信息, areaId={}", areaId);

        Map<String, Object> statistics = new HashMap<>();

        // 基础统计信息
        statistics.put("areaId", areaId);
        statistics.put("childAreaCount", areaDao.countChildAreas(areaId));

        // 各业务模块统计信息
        statistics.put("accessStats", getAccessModuleStats(areaId));
        statistics.put("consumeStats", getConsumeModuleStats(areaId));
        statistics.put("attendanceStats", getAttendanceModuleStats(areaId));
        statistics.put("visitorStats", getVisitorModuleStats(areaId));
        statistics.put("videoStats", getVideoModuleStats(areaId));

        return statistics;
    }

    @Override
    @Observed(name = "area.getAreasByBusinessType", contextualName = "area-get-by-business-type")
    public List<AreaEntity> getAreasByBusinessType(String businessType) {
        log.debug("[区域统一服务] 根据业务类型获取区域, businessType={}", businessType);

        return areaDao.selectAreasByBusinessType(businessType);
    }

    @Override
    @Observed(name = "area.isAreaSupportBusiness", contextualName = "area-is-support-business")
    public boolean isAreaSupportBusiness(Long areaId, String businessModule) {
        log.debug("[区域统一服务] 检查区域业务支持, areaId={}, businessModule={}", areaId, businessModule);

        Map<String, Object> attributes = getAreaBusinessAttributes(areaId, businessModule);
        return attributes != null && !attributes.isEmpty();
    }

    @Override
    @Observed(name = "area.getAreaSupportedBusinessModules", contextualName = "area-get-supported-modules")
    public Set<String> getAreaSupportedBusinessModules(Long areaId) {
        log.debug("[区域统一服务] 获取区域支持的业务模块, areaId={}", areaId);

        Set<String> supportedModules = new HashSet<>();

        // 检查各业务模块的支持情况
        String[] modules = {"access", "consume", "attendance", "visitor", "video", "oa"};

        for (String module : modules) {
            if (isAreaSupportBusiness(areaId, module)) {
                supportedModules.add(module);
            }
        }

        return supportedModules;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建区域树
     */
    private List<AreaEntity> buildAreaTree(List<AreaEntity> allAreas) {
        if (allAreas == null || allAreas.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, AreaEntity> areaMap = allAreas.stream()
                .collect(Collectors.toMap(AreaEntity::getId, area -> area));

        List<AreaEntity> tree = new ArrayList<>();

        for (AreaEntity area : allAreas) {
            if (area.getParentId() == null || area.getParentId() == 0) {
                tree.add(area);
            } else {
                AreaEntity parent = areaMap.get(area.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(area);
                }
            }
        }

        return tree;
    }

    /**
     * 清除区域业务属性缓存
     * <p>
     * 使用@CacheEvict注解清除指定区域和业务模块的缓存
     * </p>
     */
    @CacheEvict(value = "area:unified:business_attrs", key = "#areaId + ':' + #businessModule")
    public void evictAreaBusinessAttributesCache(Long areaId, String businessModule) {
        log.debug("[区域统一服务] 清除区域业务属性缓存, areaId={}, businessModule={}", areaId, businessModule);
        // 缓存清除由@CacheEvict注解自动处理
    }

    /**
     * 获取门禁模块统计信息
     */
    private Map<String, Object> getAccessModuleStats(Long areaId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("deviceCount", areaDao.countAccessDevices(areaId));
        stats.put("recordCount", areaDao.countAccessRecords(areaId));
        stats.put("activePermissions", areaDao.countActiveAccessPermissions(areaId));
        return stats;
    }

    /**
     * 获取消费模块统计信息
     */
    private Map<String, Object> getConsumeModuleStats(Long areaId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("posDeviceCount", areaDao.countConsumeDevices(areaId));
        stats.put("consumeAmount", areaDao.sumConsumeAmount(areaId));
        stats.put("consumeCount", areaDao.countConsumeRecords(areaId));
        return stats;
    }

    /**
     * 获取考勤模块统计信息
     */
    private Map<String, Object> getAttendanceModuleStats(Long areaId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("attendanceDeviceCount", areaDao.countAttendanceDevices(areaId));
        stats.put("recordCount", areaDao.countAttendanceRecords(areaId));
        stats.put("userCount", areaDao.countAreaUsers(areaId));
        return stats;
    }

    /**
     * 获取访客模块统计信息
     */
    private Map<String, Object> getVisitorModuleStats(Long areaId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("visitorCount", areaDao.countVisitorRecords(areaId));
        stats.put("appointmentCount", areaDao.countVisitorAppointments(areaId));
        return stats;
    }

    /**
     * 获取视频模块统计信息
     */
    private Map<String, Object> getVideoModuleStats(Long areaId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cameraCount", areaDao.countVideoDevices(areaId));
        stats.put("recordSize", areaDao.getVideoRecordSize(areaId));
        return stats;
    }
}
