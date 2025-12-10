package net.lab1024.sa.visitor.domain.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.visitor.domain.dao.VisitorAreaDao;
import net.lab1024.sa.visitor.domain.entity.VisitorAreaEntity;
import net.lab1024.sa.visitor.domain.service.VisitorAreaService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 访客区域管理服务实现类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorAreaServiceImpl implements VisitorAreaService {

    @Resource
    private VisitorAreaDao visitorAreaDao;

    @Resource
    private AreaUnifiedService areaUnifiedService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Redis缓存前缀
    private static final String CACHE_PREFIX = "visitor:area:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    public boolean createVisitorArea(VisitorAreaEntity visitorArea) {
        log.info("[访客区域管理] 创建访客区域配置, areaId={}", visitorArea.getId());

        // 验证区域存在且支持访客业务
        if (!areaUnifiedService.isAreaSupportBusiness(visitorArea.getId(), "visitor")) {
            log.warn("[访客区域管理] 区域不支持访客业务, areaId={}", visitorArea.getId());
            return false;
        }

        // 验证配置
        Map<String, String> validationResult = validateVisitorAreaConfig(visitorArea);
        if (!validationResult.isEmpty() && !"valid".equals(validationResult.get("status"))) {
            log.warn("[访客区域管理] 访客区域配置验证失败: {}", validationResult.get("message"));
            return false;
        }

        // 设置默认值
        if (visitorArea.getCurrentVisitors() == null) {
            visitorArea.setCurrentVisitors(0);
        }
        if (visitorArea.getEnabled() == null) {
            visitorArea.setEnabled(true);
        }
        if (visitorArea.getEffectiveTime() == null) {
            visitorArea.setEffectiveTime(LocalDateTime.now());
        }

        try {
            int result = visitorAreaDao.insert(visitorArea);

            if (result > 0) {
                clearAreaCache(visitorArea.getId());
                log.info("[访客区域管理] 访客区域配置创建成功, visitorAreaId={}", visitorArea.getVisitorAreaId());
                return true;
            }
        } catch (Exception e) {
            log.error("[访客区域管理] 访客区域配置创建失败, areaId={}", visitorArea.getId(), e);
        }

        return false;
    }

    @Override
    public boolean updateVisitorArea(VisitorAreaEntity visitorArea) {
        log.info("[访客区域管理] 更新访客区域配置, visitorAreaId={}", visitorArea.getVisitorAreaId());

        // 验证配置
        Map<String, String> validationResult = validateVisitorAreaConfig(visitorArea);
        if (!validationResult.isEmpty() && !"valid".equals(validationResult.get("status"))) {
            log.warn("[访客区域管理] 访客区域配置验证失败: {}", validationResult.get("message"));
            return false;
        }

        try {
            int result = visitorAreaDao.updateById(visitorArea);

            if (result > 0) {
                clearAreaCache(visitorArea.getId());
                log.info("[访客区域管理] 访客区域配置更新成功, visitorAreaId={}", visitorArea.getVisitorAreaId());
                return true;
            }
        } catch (Exception e) {
            log.error("[访客区域管理] 访客区域配置更新失败, visitorAreaId={}", visitorArea.getVisitorAreaId(), e);
        }

        return false;
    }

    @Override
    public boolean deleteVisitorArea(Long visitorAreaId) {
        log.info("[访客区域管理] 删除访客区域配置, visitorAreaId={}", visitorAreaId);

        try {
            VisitorAreaEntity visitorArea = visitorAreaDao.selectById(visitorAreaId);
            if (visitorArea == null) {
                return false;
            }

            int result = visitorAreaDao.deleteById(visitorAreaId);

            if (result > 0) {
                clearAreaCache(visitorArea.getId());
                log.info("[访客区域管理] 访客区域配置删除成功, visitorAreaId={}", visitorAreaId);
                return true;
            }
        } catch (Exception e) {
            log.error("[访客区域管理] 访客区域配置删除失败, visitorAreaId={}", visitorAreaId, e);
        }

        return false;
    }

    @Override
    public VisitorAreaEntity getVisitorAreaByAreaId(Long areaId) {
        log.debug("[访客区域管理] 获取访客区域配置, areaId={}", areaId);

        String cacheKey = CACHE_PREFIX + "config:" + areaId;
        VisitorAreaEntity cachedArea = getCachedVisitorArea(cacheKey);

        if (cachedArea != null) {
            return cachedArea;
        }

        VisitorAreaEntity visitorArea = visitorAreaDao.selectByAreaId(areaId);

        if (visitorArea != null) {
            cacheVisitorArea(cacheKey, visitorArea);
        }

        return visitorArea;
    }

    @Override
    public List<VisitorAreaEntity> getVisitorAreasByVisitType(Integer visitType) {
        log.debug("[访客区域管理] 根据访问类型获取访客区域, visitType={}", visitType);

        String cacheKey = CACHE_PREFIX + "visit_type:" + visitType;
        List<VisitorAreaEntity> cachedAreas = getCachedVisitorAreaList(cacheKey);

        if (cachedAreas != null) {
            return cachedAreas;
        }

        List<VisitorAreaEntity> visitorAreas = visitorAreaDao.selectByVisitType(visitType);

        cacheVisitorAreaList(cacheKey, visitorAreas);

        return visitorAreas;
    }

    @Override
    public List<VisitorAreaEntity> getVisitorAreasByAccessLevel(Integer accessLevel) {
        log.debug("[访客区域管理] 根据访问权限级别获取访客区域, accessLevel={}", accessLevel);

        String cacheKey = CACHE_PREFIX + "access_level:" + accessLevel;
        List<VisitorAreaEntity> cachedAreas = getCachedVisitorAreaList(cacheKey);

        if (cachedAreas != null) {
            return cachedAreas;
        }

        List<VisitorAreaEntity> visitorAreas = visitorAreaDao.selectByAccessLevel(accessLevel);

        cacheVisitorAreaList(cacheKey, visitorAreas);

        return visitorAreas;
    }

    @Override
    public List<VisitorAreaEntity> getReceptionRequiredAreas() {
        log.debug("[访客区域管理] 获取需要接待人员的访客区域");

        String cacheKey = CACHE_PREFIX + "reception_required";
        List<VisitorAreaEntity> cachedAreas = getCachedVisitorAreaList(cacheKey);

        if (cachedAreas != null) {
            return cachedAreas;
        }

        List<VisitorAreaEntity> visitorAreas = visitorAreaDao.selectReceptionRequiredAreas();

        cacheVisitorAreaList(cacheKey, visitorAreas);

        return visitorAreas;
    }

    @Override
    public List<VisitorAreaEntity> getVisitorAreasByReceptionistId(Long receptionistId) {
        log.debug("[访客区域管理] 根据接待人员获取访客区域, receptionistId={}", receptionistId);

        String cacheKey = CACHE_PREFIX + "receptionist:" + receptionistId;
        List<VisitorAreaEntity> cachedAreas = getCachedVisitorAreaList(cacheKey);

        if (cachedAreas != null) {
            return cachedAreas;
        }

        List<VisitorAreaEntity> visitorAreas = visitorAreaDao.selectByReceptionistId(receptionistId);

        cacheVisitorAreaList(cacheKey, visitorAreas);

        return visitorAreas;
    }

    @Override
    public List<VisitorAreaEntity> getOverCapacityAreas() {
        log.debug("[访客区域管理] 获取访客数量超限的区域");

        String cacheKey = CACHE_PREFIX + "over_capacity";
        List<VisitorAreaEntity> cachedAreas = getCachedVisitorAreaList(cacheKey);

        if (cachedAreas != null) {
            return cachedAreas;
        }

        List<VisitorAreaEntity> visitorAreas = visitorAreaDao.selectOverCapacityAreas();

        cacheVisitorAreaList(cacheKey, visitorAreas);

        return visitorAreas;
    }

    @Override
    public List<VisitorAreaEntity> getOpenVisitorAreas() {
        log.debug("[访客区域管理] 获取当前时段开放的访客区域");

        String cacheKey = CACHE_PREFIX + "open_areas";
        List<VisitorAreaEntity> cachedAreas = getCachedVisitorAreaList(cacheKey);

        if (cachedAreas != null) {
            return cachedAreas;
        }

        List<VisitorAreaEntity> visitorAreas = visitorAreaDao.selectOpenAreas(LocalDateTime.now());

        cacheVisitorAreaList(cacheKey, visitorAreas, 5); // 短期缓存5分钟

        return visitorAreas;
    }

    @Override
    public boolean isSupportVisitType(Long areaId, Integer visitType) {
        log.debug("[访客区域管理] 检查区域是否支持访问类型, areaId={}, visitType={}", areaId, visitType);

        return visitorAreaDao.isSupportVisitType(areaId, visitType);
    }

    @Override
    public boolean hasManagePermission(Long userId, Long areaId) {
        log.debug("[访客区域管理] 检查用户是否有访客区域管理权限, userId={}, areaId={}", userId, areaId);

        return visitorAreaDao.hasManagePermission(userId, areaId);
    }

    @Override
    public boolean updateCurrentVisitors(Long areaId, Integer visitorCount) {
        log.info("[访客区域管理] 更新区域访客数量, areaId={}, visitorCount={}", areaId, visitorCount);

        try {
            int result = visitorAreaDao.updateCurrentVisitors(areaId, visitorCount);

            if (result > 0) {
                clearAreaCache(areaId);
                log.info("[访客区域管理] 区域访客数量更新成功, areaId={}, visitorCount={}", areaId, visitorCount);
                return true;
            }
        } catch (Exception e) {
            log.error("[访客区域管理] 区域访客数量更新失败, areaId={}, visitorCount={}", areaId, visitorCount, e);
        }

        return false;
    }

    @Override
    public boolean incrementVisitors(Long areaId, Integer increment) {
        log.info("[访客区域管理] 增加区域访客数量, areaId={}, increment={}", areaId, increment);

        try {
            int result = visitorAreaDao.incrementVisitors(areaId, increment);

            if (result > 0) {
                clearAreaCache(areaId);
                log.info("[访客区域管理] 区域访客数量增加成功, areaId={}, increment={}", areaId, increment);
                return true;
            } else {
                log.warn("[访客区域管理] 区域访客数量增加失败，可能超出容量限制, areaId={}, increment={}", areaId, increment);
            }
        } catch (Exception e) {
            log.error("[访客区域管理] 区域访客数量增加异常, areaId={}, increment={}", areaId, increment, e);
        }

        return false;
    }

    @Override
    public boolean decrementVisitors(Long areaId, Integer decrement) {
        log.info("[访客区域管理] 减少区域访客数量, areaId={}, decrement={}", areaId, decrement);

        try {
            int result = visitorAreaDao.decrementVisitors(areaId, decrement);

            if (result > 0) {
                clearAreaCache(areaId);
                log.info("[访客区域管理] 区域访客数量减少成功, areaId={}, decrement={}", areaId, decrement);
                return true;
            }
        } catch (Exception e) {
            log.error("[访客区域管理] 区域访客数量减少失败, areaId={}, decrement={}", areaId, decrement, e);
        }

        return false;
    }

    @Override
    public Map<String, Object> getVisitorAreaStatistics() {
        log.debug("[访客区域管理] 获取访客区域统计信息");

        return visitorAreaDao.selectVisitorAreaStatistics();
    }

    @Override
    public List<Map<String, Object>> getAreaStatisticsByVisitType() {
        log.debug("[访客区域管理] 按访问类型统计访客区域分布");

        return visitorAreaDao.selectAreaStatisticsByVisitType();
    }

    @Override
    public List<VisitorAreaEntity> getUserManageableVisitorAreas(Long userId) {
        log.debug("[访客区域管理] 获取用户可管理的访客区域, userId={}", userId);

        // 获取用户可访问的区域
        List<Long> accessibleAreaIds = areaUnifiedService.getUserAccessibleAreas(userId).stream()
                .map(area -> area.getId())
                .collect(Collectors.toList());

        if (accessibleAreaIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询访客区域配置
        return accessibleAreaIds.stream()
                .map(this::getVisitorAreaByAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> validateVisitorAreaConfig(VisitorAreaEntity visitorArea) {
        Map<String, String> result = new HashMap<>();

        // 验证最大访客数量
        if (visitorArea.getMaxVisitors() != null && visitorArea.getMaxVisitors() <= 0) {
            result.put("status", "error");
            result.put("message", "最大访客数量必须大于0");
            return result;
        }

        // 验证当前访客数量
        if (visitorArea.getCurrentVisitors() != null && visitorArea.getCurrentVisitors() < 0) {
            result.put("status", "error");
            result.put("message", "当前访客数量不能小于0");
            return result;
        }

        // 验证访问时间限制
        if (visitorArea.getVisitTimeLimit() != null && visitorArea.getVisitTimeLimit() <= 0) {
            result.put("status", "error");
            result.put("message", "访问时间限制必须大于0");
            return result;
        }

        // 验证预约提前天数限制
        if (visitorArea.getAppointmentDaysLimit() != null && visitorArea.getAppointmentDaysLimit() <= 0) {
            result.put("status", "error");
            result.put("message", "预约提前天数限制必须大于0");
            return result;
        }

        result.put("status", "valid");
        return result;
    }

    @Override
    public List<String> getVisitorAreaAccessSuggestions(Long areaId, Integer visitType) {
        log.debug("[访客区域管理] 获取访客区域访问建议, areaId={}, visitType={}", areaId, visitType);

        List<String> suggestions = new ArrayList<>();

        // 根据访问类型生成建议
        switch (visitType) {
            case 1: // 预约访问
                suggestions.add("建议提前至少1个工作日进行预约");
                suggestions.add("建议准备有效身份证件进行登记");
                suggestions.add("建议确认接待人员在岗时间");
                break;

            case 2: // 临时访问
                suggestions.add("建议现场登记时提供完整个人信息");
                suggestions.add("建议确认访问事由和被访人信息");
                suggestions.add("建议遵守区域安全规定");
                break;

            case 3: // VIP访问
                suggestions.add("建议安排专人陪同接待");
                suggestions.add("建议提前准备好相关文件和资料");
                suggestions.add("建议提供专属休息区域");
                break;

            case 4: // 供应商访问
                suggestions.add("建议提前获取供应商资质证明");
                suggestions.add("建议指定专人陪同监督");
                suggestions.add("建议限制活动范围和访问时间");
                break;

            case 5: // 维修访问
                suggestions.add("建议确认维修人员资质和工具");
                suggestions.add("建议配备安全监督人员");
                suggestions.add("建议提前准备应急预案");
                break;
        }

        return suggestions;
    }

    @Override
    public boolean batchUpdateVisitorAreaStatus(List<Long> areaIds, Boolean enabled) {
        log.info("[访客区域管理] 批量更新访客区域状态, areaIds={}, enabled={}", areaIds, enabled);

        if (areaIds == null || areaIds.isEmpty()) {
            return false;
        }

        int successCount = 0;
        for (Long areaId : areaIds) {
            VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
            if (visitorArea != null) {
                visitorArea.setEnabled(enabled);
                if (updateVisitorArea(visitorArea)) {
                    successCount++;
                }
            }
        }

        log.info("[访客区域管理] 批量状态更新完成, successCount={}, totalCount={}", successCount, areaIds.size());
        return successCount == areaIds.size();
    }

    @Override
    public Map<String, Object> checkAreaCapacityStatus(Long areaId, Integer additionalVisitors) {
        log.debug("[访客区域管理] 检查区域访客容量状态, areaId={}, additionalVisitors={}", areaId, additionalVisitors);

        Map<String, Object> status = new HashMap<>();
        VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);

        if (visitorArea == null) {
            status.put("status", "error");
            status.put("message", "访客区域配置不存在");
            return status;
        }

        status.put("areaId", areaId);
        status.put("currentVisitors", visitorArea.getCurrentVisitors());
        status.put("maxVisitors", visitorArea.getMaxVisitors());
        status.put("availableCapacity", visitorArea.getMaxVisitors() - visitorArea.getCurrentVisitors());
        status.put("additionalVisitors", additionalVisitors);

        if (additionalVisitors > visitorArea.getMaxVisitors() - visitorArea.getCurrentVisitors()) {
            status.put("status", "over_capacity");
            status.put("message", "超出区域容量限制");
        } else {
            status.put("status", "available");
            status.put("message", "容量充足");
        }

        return status;
    }

    @Override
    public Map<String, String> getAreaVisitorDevices(Long areaId) {
        log.debug("[访客区域管理] 获取区域访客设备配置, areaId={}", areaId);

        VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
        if (visitorArea == null || visitorArea.getVisitorDevices() == null) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(visitorArea.getVisitorDevices(), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析访客设备配置失败, areaId={}", areaId, e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean updateAreaVisitorDevices(Long areaId, Map<String, String> devices) {
        log.info("[访客区域管理] 更新区域访客设备配置, areaId={}", areaId);

        try {
            VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
            if (visitorArea == null) {
                return false;
            }

            String devicesJson = objectMapper.writeValueAsString(devices);
            visitorArea.setVisitorDevices(devicesJson);

            return updateVisitorArea(visitorArea);
        } catch (Exception e) {
            log.error("[访客区域管理] 更新区域访客设备配置失败, areaId={}", areaId, e);
            return false;
        }
    }

    @Override
    public Map<String, Boolean> getAreaHealthCheckStandard(Long areaId) {
        log.debug("[访客区域管理] 获取区域健康检查标准, areaId={}", areaId);

        VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
        if (visitorArea == null || visitorArea.getHealthCheckStandard() == null) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(visitorArea.getHealthCheckStandard(), new TypeReference<Map<String, Boolean>>() {});
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析健康检查标准失败, areaId={}", areaId, e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, String> getAreaOpenHours(Long areaId) {
        log.debug("[访客区域管理] 获取区域开放时间配置, areaId={}", areaId);

        VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
        if (visitorArea == null || visitorArea.getOpenHours() == null) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(visitorArea.getOpenHours(), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析开放时间配置失败, areaId={}", areaId, e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean isAreaCurrentlyOpen(Long areaId) {
        log.debug("[访客区域管理] 检查区域当前是否开放, areaId={}", areaId);

        Map<String, String> openHours = getAreaOpenHours(areaId);
        if (openHours.isEmpty()) {
            return true; // 默认开放
        }

        LocalDateTime now = LocalDateTime.now();
        String dayKey = getDayKey(now.getDayOfWeek().getValue());
        String hoursKey = openHours.get(dayKey);

        if (hoursKey == null) {
            return true; // 默认开放
        }

        try {
            LocalTime currentTime = now.toLocalTime();
            String[] timeRange = hoursKey.split("-");
            if (timeRange.length == 2) {
                LocalTime startTime = LocalTime.parse(timeRange[0]);
                LocalTime endTime = LocalTime.parse(timeRange[1]);
                return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            }
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析开放时间失败, areaId={}, hoursKey={}", areaId, hoursKey, e);
        }

        return true; // 默认开放
    }

    @Override
    public String getAreaVisitorInstructions(Long areaId) {
        log.debug("[访客区域管理] 获取区域访客须知, areaId={}", areaId);

        VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
        return visitorArea != null ? visitorArea.getVisitorInstructions() : "";
    }

    @Override
    public String getAreaSafetyNotes(Long areaId) {
        log.debug("[访客区域管理] 获取区域安全注意事项, areaId={}", areaId);

        VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
        return visitorArea != null ? visitorArea.getSafetyNotes() : "";
    }

    @Override
    public Map<String, String> getAreaEmergencyContact(Long areaId) {
        log.debug("[访客区域管理] 获取区域紧急联系人信息, areaId={}", areaId);

        VisitorAreaEntity visitorArea = getVisitorAreaByAreaId(areaId);
        if (visitorArea == null || visitorArea.getEmergencyContact() == null) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(visitorArea.getEmergencyContact(), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析紧急联系人信息失败, areaId={}", areaId, e);
            return new HashMap<>();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 获取星期几的键值
     */
    private String getDayKey(int dayOfWeek) {
        // DayOfWeek.getValue(): 1=Monday, 2=Tuesday, ..., 7=Sunday
        switch (dayOfWeek) {
            case 1: return "monday";
            case 2: return "tuesday";
            case 3: return "wednesday";
            case 4: return "thursday";
            case 5: return "friday";
            case 6: return "saturday";
            case 7: return "sunday";
            default: return "workdays";
        }
    }

    /**
     * 缓存相关方法
     */
    @SuppressWarnings("null")
    private VisitorAreaEntity getCachedVisitorArea(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            return cached != null ? objectMapper.readValue(cached, VisitorAreaEntity.class) : null;
        } catch (Exception e) {
            log.warn("[访客区域管理] 缓存读取失败", e);
            return null;
        }
    }

    @SuppressWarnings("null")
    private void cacheVisitorArea(String cacheKey, VisitorAreaEntity visitorArea) {
        try {
            String json = objectMapper.writeValueAsString(visitorArea);
            stringRedisTemplate.opsForValue().set(cacheKey, json, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[访客区域管理] 缓存写入失败", e);
        }
    }

    @SuppressWarnings("null")
    private List<VisitorAreaEntity> getCachedVisitorAreaList(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            return cached != null ? objectMapper.readValue(cached, new TypeReference<List<VisitorAreaEntity>>() {}) : null;
        } catch (Exception e) {
            log.warn("[访客区域管理] 缓存读取失败", e);
            return null;
        }
    }

    private void cacheVisitorAreaList(String cacheKey, List<VisitorAreaEntity> visitorAreas) {
        cacheVisitorAreaList(cacheKey, visitorAreas, CACHE_EXPIRE_MINUTES);
    }

    @SuppressWarnings("null")
    private void cacheVisitorAreaList(String cacheKey, List<VisitorAreaEntity> visitorAreas, long expireMinutes) {
        try {
            String json = objectMapper.writeValueAsString(visitorAreas);
            stringRedisTemplate.opsForValue().set(cacheKey, json, expireMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[访客区域管理] 缓存写入失败", e);
        }
    }

    private void clearAreaCache(Long areaId) {
        try {
            Set<String> keys = stringRedisTemplate.keys(CACHE_PREFIX + "*:" + areaId + "*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("[访客区域管理] 清除区域缓存失败", e);
        }
    }
}
