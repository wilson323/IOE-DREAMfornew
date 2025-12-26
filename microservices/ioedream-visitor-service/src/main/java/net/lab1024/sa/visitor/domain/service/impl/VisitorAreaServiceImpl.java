package net.lab1024.sa.visitor.domain.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.visitor.domain.dao.VisitorAreaDao;
import net.lab1024.sa.common.entity.visitor.VisitorAreaEntity;
import net.lab1024.sa.visitor.domain.service.VisitorAreaService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Observed(name = "visitor.area.create", contextualName = "visitor-area-create")
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
                evictVisitorAreaCache(visitorArea.getId());
                log.info("[访客区域管理] 访客区域配置创建成功, visitorAreaId={}", visitorArea.getVisitorAreaId());
                return true;
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 访客区域配置创建参数错误: areaId={}, error={}", visitorArea.getId(), e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 访客区域配置创建业务异常: areaId={}, code={}, message={}", visitorArea.getId(), e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 访客区域配置创建系统异常: areaId={}, code={}, message={}", visitorArea.getId(), e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[访客区域管理] 访客区域配置创建未知异常: areaId={}", visitorArea.getId(), e);
            return false; // For boolean return methods, return false on unknown error
        }

        return false;
    }

    @Override
    @Observed(name = "visitor.area.update", contextualName = "visitor-area-update")
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
                evictVisitorAreaCache(visitorArea.getId());
                log.info("[访客区域管理] 访客区域配置更新成功, visitorAreaId={}", visitorArea.getVisitorAreaId());
                return true;
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 访客区域配置更新参数错误: visitorAreaId={}, error={}", visitorArea.getVisitorAreaId(), e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 访客区域配置更新业务异常: visitorAreaId={}, code={}, message={}", visitorArea.getVisitorAreaId(), e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 访客区域配置更新系统异常: visitorAreaId={}, code={}, message={}", visitorArea.getVisitorAreaId(), e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[访客区域管理] 访客区域配置更新未知异常: visitorAreaId={}", visitorArea.getVisitorAreaId(), e);
            return false; // For boolean return methods, return false on unknown error
        }

        return false;
    }

    @Override
    @Observed(name = "visitor.area.delete", contextualName = "visitor-area-delete")
    public boolean deleteVisitorArea(Long visitorAreaId) {
        log.info("[访客区域管理] 删除访客区域配置, visitorAreaId={}", visitorAreaId);

        try {
            VisitorAreaEntity visitorArea = visitorAreaDao.selectById(visitorAreaId);
            if (visitorArea == null) {
                return false;
            }

            int result = visitorAreaDao.deleteById(visitorAreaId);

            if (result > 0) {
                evictVisitorAreaCache(visitorArea.getId());
                log.info("[访客区域管理] 访客区域配置删除成功, visitorAreaId={}", visitorAreaId);
                return true;
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 访客区域配置删除参数错误: visitorAreaId={}, error={}", visitorAreaId, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 访客区域配置删除业务异常: visitorAreaId={}, code={}, message={}", visitorAreaId, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 访客区域配置删除系统异常: visitorAreaId={}, code={}, message={}", visitorAreaId, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[访客区域管理] 访客区域配置删除未知异常: visitorAreaId={}", visitorAreaId, e);
            return false; // For boolean return methods, return false on unknown error
        }

        return false;
    }

    @Override
    @Cacheable(value = "visitor:area:config", key = "#areaId", unless = "#result == null")
    public VisitorAreaEntity getVisitorAreaByAreaId(Long areaId) {
        log.debug("[访客区域管理] 获取访客区域配置, areaId={}", areaId);
        return visitorAreaDao.selectByAreaId(areaId);
    }

    @Override
    @Observed(name = "visitor.area.getByVisitType", contextualName = "visitor-area-get-by-visit-type")
    @Cacheable(value = "visitor:area:visit_type", key = "#visitType", unless = "#result == null || #result.isEmpty()")
    public List<VisitorAreaEntity> getVisitorAreasByVisitType(Integer visitType) {
        log.debug("[访客区域管理] 根据访问类型获取访客区域, visitType={}", visitType);
        return visitorAreaDao.selectByVisitType(visitType);
    }

    @Override
    @Cacheable(value = "visitor:area:access_level", key = "#accessLevel", unless = "#result == null || #result.isEmpty()")
    public List<VisitorAreaEntity> getVisitorAreasByAccessLevel(Integer accessLevel) {
        log.debug("[访客区域管理] 根据访问权限级别获取访客区域, accessLevel={}", accessLevel);
        return visitorAreaDao.selectByAccessLevel(accessLevel);
    }

    @Override
    @Cacheable(value = "visitor:area:reception_required", unless = "#result == null || #result.isEmpty()")
    public List<VisitorAreaEntity> getReceptionRequiredAreas() {
        log.debug("[访客区域管理] 获取需要接待人员的访客区域");
        return visitorAreaDao.selectReceptionRequiredAreas();
    }

    @Override
    @Cacheable(value = "visitor:area:receptionist", key = "#receptionistId", unless = "#result == null || #result.isEmpty()")
    public List<VisitorAreaEntity> getVisitorAreasByReceptionistId(Long receptionistId) {
        log.debug("[访客区域管理] 根据接待人员获取访客区域, receptionistId={}", receptionistId);
        return visitorAreaDao.selectByReceptionistId(receptionistId);
    }

    @Override
    @Cacheable(value = "visitor:area:over_capacity", unless = "#result == null || #result.isEmpty()")
    public List<VisitorAreaEntity> getOverCapacityAreas() {
        log.debug("[访客区域管理] 获取访客数量超限的区域");
        return visitorAreaDao.selectOverCapacityAreas();
    }

    @Override
    @Cacheable(value = "visitor:area:open_areas", unless = "#result == null || #result.isEmpty()")
    public List<VisitorAreaEntity> getOpenVisitorAreas() {
        log.debug("[访客区域管理] 获取当前时段开放的访客区域");
        return visitorAreaDao.selectOpenAreas(LocalDateTime.now());
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
                evictVisitorAreaCache(areaId);
                log.info("[访客区域管理] 区域访客数量更新成功, areaId={}, visitorCount={}", areaId, visitorCount);
                return true;
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 区域访客数量更新参数错误: areaId={}, visitorCount={}, error={}", areaId, visitorCount, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 区域访客数量更新业务异常: areaId={}, visitorCount={}, code={}, message={}", areaId, visitorCount, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 区域访客数量更新系统异常: areaId={}, visitorCount={}, code={}, message={}", areaId, visitorCount, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[访客区域管理] 区域访客数量更新未知异常: areaId={}, visitorCount={}", areaId, visitorCount, e);
            return false; // For boolean return methods, return false on unknown error
        }

        return false;
    }

    @Override
    public boolean incrementVisitors(Long areaId, Integer increment) {
        log.info("[访客区域管理] 增加区域访客数量, areaId={}, increment={}", areaId, increment);

        try {
            int result = visitorAreaDao.incrementVisitors(areaId, increment);

            if (result > 0) {
                evictVisitorAreaCache(areaId);
                log.info("[访客区域管理] 区域访客数量增加成功, areaId={}, increment={}", areaId, increment);
                return true;
            } else {
                log.warn("[访客区域管理] 区域访客数量增加失败，可能超出容量限制, areaId={}, increment={}", areaId, increment);
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 区域访客数量增加参数错误: areaId={}, increment={}, error={}", areaId, increment, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 区域访客数量增加业务异常: areaId={}, increment={}, code={}, message={}", areaId, increment, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 区域访客数量增加系统异常: areaId={}, increment={}, code={}, message={}", areaId, increment, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[访客区域管理] 区域访客数量增加未知异常: areaId={}, increment={}", areaId, increment, e);
            return false; // For boolean return methods, return false on unknown error
        }

        return false;
    }

    @Override
    public boolean decrementVisitors(Long areaId, Integer decrement) {
        log.info("[访客区域管理] 减少区域访客数量, areaId={}, decrement={}", areaId, decrement);

        try {
            int result = visitorAreaDao.decrementVisitors(areaId, decrement);

            if (result > 0) {
                evictVisitorAreaCache(areaId);
                log.info("[访客区域管理] 区域访客数量减少成功, areaId={}, decrement={}", areaId, decrement);
                return true;
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 区域访客数量减少参数错误: areaId={}, decrement={}, error={}", areaId, decrement, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 区域访客数量减少业务异常: areaId={}, decrement={}, code={}, message={}", areaId, decrement, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 区域访客数量减少系统异常: areaId={}, decrement={}, code={}, message={}", areaId, decrement, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[访客区域管理] 区域访客数量减少未知异常: areaId={}, decrement={}", areaId, decrement, e);
            return false; // For boolean return methods, return false on unknown error
        }

        return false;
    }

    @Override
    @Observed(name = "visitor.area.getStatistics", contextualName = "visitor-area-get-statistics")
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
                .map(AreaEntity::getId)
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 解析访客设备配置参数错误: areaId={}, error={}", areaId, e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 解析访客设备配置业务异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 解析访客设备配置系统异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return new HashMap<>(); // For read-only operations, return empty map on system error
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析访客设备配置未知异常: areaId={}", areaId, e);
            return new HashMap<>(); // For read-only operations, return empty map on unknown error
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 更新区域访客设备配置参数错误: areaId={}, error={}", areaId, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 更新区域访客设备配置业务异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 更新区域访客设备配置系统异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[访客区域管理] 更新区域访客设备配置未知异常: areaId={}", areaId, e);
            return false; // For boolean return methods, return false on unknown error
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 解析健康检查标准参数错误: areaId={}, error={}", areaId, e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 解析健康检查标准业务异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 解析健康检查标准系统异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return new HashMap<>(); // For read-only operations, return empty map on system error
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析健康检查标准未知异常: areaId={}", areaId, e);
            return new HashMap<>(); // For read-only operations, return empty map on unknown error
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 解析开放时间配置参数错误: areaId={}, error={}", areaId, e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 解析开放时间配置业务异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 解析开放时间配置系统异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return new HashMap<>(); // For read-only operations, return empty map on system error
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析开放时间配置未知异常: areaId={}", areaId, e);
            return new HashMap<>(); // For read-only operations, return empty map on unknown error
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 解析开放时间参数错误: areaId={}, hoursKey={}, error={}", areaId, hoursKey, e.getMessage());
            return true; // Default to open on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 解析开放时间业务异常: areaId={}, hoursKey={}, code={}, message={}", areaId, hoursKey, e.getCode(), e.getMessage());
            return true; // Default to open on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 解析开放时间系统异常: areaId={}, hoursKey={}, code={}, message={}", areaId, hoursKey, e.getCode(), e.getMessage(), e);
            return true; // Default to open on system error
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析开放时间未知异常: areaId={}, hoursKey={}", areaId, hoursKey, e);
            return true; // Default to open on unknown error
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
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客区域管理] 解析紧急联系人信息参数错误: areaId={}, error={}", areaId, e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on parameter error
        } catch (BusinessException e) {
            log.warn("[访客区域管理] 解析紧急联系人信息业务异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return new HashMap<>(); // For read-only operations, return empty map on business error
        } catch (SystemException e) {
            log.error("[访客区域管理] 解析紧急联系人信息系统异常: areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return new HashMap<>(); // For read-only operations, return empty map on system error
        } catch (Exception e) {
            log.warn("[访客区域管理] 解析紧急联系人信息未知异常: areaId={}", areaId, e);
            return new HashMap<>(); // For read-only operations, return empty map on unknown error
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
     * 清除访客区域相关缓存
     * <p>
     * 使用@CacheEvict注解清除指定区域的所有相关缓存
     * </p>
     */
    @CacheEvict(value = {"visitor:area:config", "visitor:area:visit_type", "visitor:area:access_level",
            "visitor:area:reception_required", "visitor:area:receptionist", "visitor:area:over_capacity",
            "visitor:area:open_areas"}, allEntries = true)
    public void evictVisitorAreaCache(Long areaId) {
        log.debug("[访客区域管理] 清除访客区域缓存, areaId={}", areaId);
        // 缓存清除由@CacheEvict注解自动处理
    }
}


