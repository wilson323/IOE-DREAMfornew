package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessPersonRestrictionDao;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionAddForm;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionQueryForm;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessPersonRestrictionVO;
import net.lab1024.sa.common.entity.access.AccessPersonRestrictionEntity;
import net.lab1024.sa.access.service.AccessPersonRestrictionService;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 门禁人员限制Service实现
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Slf4j
@Service
public class AccessPersonRestrictionServiceImpl implements AccessPersonRestrictionService {

    @Resource
    private AccessPersonRestrictionDao restrictionDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String RESTRICTION_CACHE_KEY_PREFIX = "access:restriction:user:";
    private static final long RESTRICTION_CACHE_EXPIRE_SECONDS = 1800; // 30分钟过期

    @Override
    public PageResult<AccessPersonRestrictionVO> queryPage(AccessPersonRestrictionQueryForm queryForm) {
        log.info("[人员限制] 分页查询人员限制规则: {}", queryForm);

        Page<AccessPersonRestrictionEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<AccessPersonRestrictionEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 用户ID过滤
        if (queryForm.getUserId() != null) {
            queryWrapper.eq(AccessPersonRestrictionEntity::getUserId, queryForm.getUserId());
        }

        // 限制类型过滤
        if (queryForm.getRestrictionType() != null && !queryForm.getRestrictionType().trim().isEmpty()) {
            queryWrapper.eq(AccessPersonRestrictionEntity::getRestrictionType, queryForm.getRestrictionType());
        }

        // 区域ID过滤
        if (queryForm.getAreaId() != null) {
            queryWrapper.apply("area_ids LIKE {0}", "%" + queryForm.getAreaId() + "%");
        }

        // 启用状态过滤
        if (queryForm.getEnabled() != null) {
            queryWrapper.eq(AccessPersonRestrictionEntity::getEnabled, queryForm.getEnabled());
        }

        // 按优先级降序排序
        queryWrapper.orderByDesc(AccessPersonRestrictionEntity::getPriority)
                .orderByDesc(AccessPersonRestrictionEntity::getCreateTime);

        IPage<AccessPersonRestrictionEntity> pageResult = restrictionDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<AccessPersonRestrictionVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("[人员限制] 查询到{}条限制规则记录", pageResult.getTotal());
        return PageResult.of(voList, pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    @Override
    public AccessPersonRestrictionVO getById(Long restrictionId) {
        log.info("[人员限制] 查询人员限制详情: restrictionId={}", restrictionId);
        AccessPersonRestrictionEntity entity = restrictionDao.selectById(restrictionId);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRestriction(AccessPersonRestrictionAddForm addForm) {
        log.info("[人员限制] 新增人员限制规则: userId={}, type={}", addForm.getUserId(), addForm.getRestrictionType());

        AccessPersonRestrictionEntity entity = new AccessPersonRestrictionEntity();
        entity.setUserId(addForm.getUserId());
        entity.setUserName(addForm.getUserName());
        entity.setUserPhone(addForm.getUserPhone());
        entity.setRestrictionType(addForm.getRestrictionType());
        entity.setAreaIds(addForm.getAreaIds() != null ? addForm.getAreaIds().toString() : "[]");
        entity.setDoorIds(addForm.getDoorIds() != null ? addForm.getDoorIds().toString() : "[]");
        entity.setTimeStart(addForm.getTimeStart() != null ? addForm.getTimeStart().toString() : null);
        entity.setTimeEnd(addForm.getTimeEnd() != null ? addForm.getTimeEnd().toString() : null);
        entity.setEffectiveDate(addForm.getEffectiveDate());
        entity.setExpireDate(addForm.getExpireDate());
        entity.setReason(addForm.getReason());
        entity.setPriority(addForm.getPriority());
        entity.setEnabled(addForm.getEnabled() != null ? addForm.getEnabled() : 1);
        entity.setDescription(addForm.getDescription());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        restrictionDao.insert(entity);

        // 清除用户限制缓存
        clearUserRestrictionCache(addForm.getUserId());

        log.info("[人员限制] 新增人员限制成功: restrictionId={}", entity.getRestrictionId());
        return entity.getRestrictionId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRestriction(Long restrictionId, AccessPersonRestrictionUpdateForm updateForm) {
        log.info("[人员限制] 更新人员限制规则: restrictionId={}", restrictionId);

        AccessPersonRestrictionEntity entity = restrictionDao.selectById(restrictionId);
        if (entity == null) {
            log.warn("[人员限制] 限制规则不存在: restrictionId={}", restrictionId);
            return false;
        }

        Long userId = entity.getUserId();

        // 更新字段
        if (updateForm.getUserName() != null) {
            entity.setUserName(updateForm.getUserName());
        }
        if (updateForm.getUserPhone() != null) {
            entity.setUserPhone(updateForm.getUserPhone());
        }
        if (updateForm.getRestrictionType() != null) {
            entity.setRestrictionType(updateForm.getRestrictionType());
        }
        if (updateForm.getAreaIds() != null) {
            entity.setAreaIds(updateForm.getAreaIds().toString());
        }
        if (updateForm.getDoorIds() != null) {
            entity.setDoorIds(updateForm.getDoorIds().toString());
        }
        if (updateForm.getTimeStart() != null) {
            entity.setTimeStart(updateForm.getTimeStart().toString());
        }
        if (updateForm.getTimeEnd() != null) {
            entity.setTimeEnd(updateForm.getTimeEnd().toString());
        }
        if (updateForm.getEffectiveDate() != null) {
            entity.setEffectiveDate(updateForm.getEffectiveDate());
        }
        if (updateForm.getExpireDate() != null) {
            entity.setExpireDate(updateForm.getExpireDate());
        }
        if (updateForm.getReason() != null) {
            entity.setReason(updateForm.getReason());
        }
        if (updateForm.getPriority() != null) {
            entity.setPriority(updateForm.getPriority());
        }
        if (updateForm.getDescription() != null) {
            entity.setDescription(updateForm.getDescription());
        }
        entity.setUpdateTime(LocalDateTime.now());

        int result = restrictionDao.updateById(entity);

        // 清除用户限制缓存
        clearUserRestrictionCache(userId);

        log.info("[人员限制] 更新人员限制成功: restrictionId={}, result={}", restrictionId, result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRestriction(Long restrictionId) {
        log.info("[人员限制] 删除人员限制规则: restrictionId={}", restrictionId);

        AccessPersonRestrictionEntity entity = restrictionDao.selectById(restrictionId);
        Long userId = entity != null ? entity.getUserId() : null;

        int result = restrictionDao.deleteById(restrictionId);

        // 清除用户限制缓存
        if (userId != null) {
            clearUserRestrictionCache(userId);
        }

        log.info("[人员限制] 删除人员限制成功: restrictionId={}, result={}", restrictionId, result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEnabled(Long restrictionId, Integer enabled) {
        log.info("[人员限制] 更新人员限制启用状态: restrictionId={}, enabled={}", restrictionId, enabled);

        AccessPersonRestrictionEntity entity = restrictionDao.selectById(restrictionId);
        if (entity == null) {
            log.warn("[人员限制] 限制规则不存在: restrictionId={}", restrictionId);
            return false;
        }

        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());
        int result = restrictionDao.updateById(entity);

        // 清除用户限制缓存
        clearUserRestrictionCache(entity.getUserId());

        log.info("[人员限制] 更新启用状态成功: restrictionId={}, enabled={}, result={}", restrictionId, enabled, result);
        return result > 0;
    }

    @Override
    public Boolean checkAccessAllowed(Long userId, Long areaId) {
        log.debug("[人员限制] 检查用户访问权限: userId={}, areaId={}", userId, areaId);

        // 获取用户的所有限制规则
        List<AccessPersonRestrictionEntity> restrictions = getUserRestrictionsFromDb(userId);

        // 按优先级排序
        restrictions.sort((r1, r2) -> r2.getPriority().compareTo(r1.getPriority()));

        for (AccessPersonRestrictionEntity restriction : restrictions) {
            // 检查是否启用
            if (restriction.getEnabled() != 1) {
                continue;
            }

            // 检查是否匹配区域
            if (!matchArea(restriction.getAreaIds(), areaId)) {
                continue;
            }

            // 检查有效期
            if (!isValidDateRange(restriction.getEffectiveDate(), restriction.getExpireDate())) {
                continue;
            }

            // 检查时段限制
            if (!isValidTimeRange(restriction.getTimeStart(), restriction.getTimeEnd())) {
                continue;
            }

            // 根据限制类型判断
            if ("BLACKLIST".equals(restriction.getRestrictionType())) {
                log.info("[人员限制] 用户在黑名单中，禁止访问: userId={}, restrictionId={}", userId, restriction.getRestrictionId());
                return false;
            } else if ("WHITELIST".equals(restriction.getRestrictionType())) {
                log.info("[人员限制] 用户在白名单中，允许访问: userId={}, restrictionId={}", userId, restriction.getRestrictionId());
                return true;
            } else if ("TIME_BASED".equals(restriction.getRestrictionType())) {
                // 时段限制，检查是否在有效时段内
                if (!isValidTimeRange(restriction.getTimeStart(), restriction.getTimeEnd())) {
                    log.info("[人员限制] 用户不在有效时段内，禁止访问: userId={}, restrictionId={}", userId, restriction.getRestrictionId());
                    return false;
                }
            }
        }

        // 默认允许访问
        log.debug("[人员限制] 用户无限制规则，允许访问: userId={}", userId);
        return true;
    }

    @Override
    public List<AccessPersonRestrictionVO> getUserRestrictions(Long userId) {
        log.info("[人员限制] 获取用户的所有限制规则: userId={}", userId);

        List<AccessPersonRestrictionEntity> entities = getUserRestrictionsFromDb(userId);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 从数据库获取用户的所有限制规则
     */
    private List<AccessPersonRestrictionEntity> getUserRestrictionsFromDb(Long userId) {
        try {
            // 尝试从缓存获取
            String cacheKey = RESTRICTION_CACHE_KEY_PREFIX + userId;
            String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null && !cachedData.isEmpty()) {
                // TODO: 反序列化JSON（简化处理，实际应使用ObjectMapper）
                log.debug("[人员限制] 从缓存获取用户限制规则: userId={}", userId);
            }

            // 从数据库查询
            LambdaQueryWrapper<AccessPersonRestrictionEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AccessPersonRestrictionEntity::getUserId, userId)
                    .orderByDesc(AccessPersonRestrictionEntity::getPriority)
                    .orderByDesc(AccessPersonRestrictionEntity::getCreateTime);

            List<AccessPersonRestrictionEntity> restrictions = restrictionDao.selectList(queryWrapper);

            // 缓存结果（缓存ID列表）
            String restrictionIds = restrictions.stream()
                    .map(r -> r.getRestrictionId().toString())
                    .collect(Collectors.joining(","));
            stringRedisTemplate.opsForValue().set(cacheKey, restrictionIds,
                    RESTRICTION_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

            return restrictions;
        } catch (Exception e) {
            log.error("[人员限制] 获取用户限制规则失败: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 清除用户限制缓存
     */
    private void clearUserRestrictionCache(Long userId) {
        try {
            String cacheKey = RESTRICTION_CACHE_KEY_PREFIX + userId;
            stringRedisTemplate.delete(cacheKey);
            log.debug("[人员限制] 清除用户限制缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("[人员限制] 清除缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 检查区域是否匹配
     */
    private boolean matchArea(String areaIdsStr, Long areaId) {
        if (areaIdsStr == null || areaIdsStr.trim().isEmpty() || "[]".equals(areaIdsStr)) {
            return true; // 空限制，匹配所有区域
        }
        try {
            String[] parts = areaIdsStr.replaceAll("[\\[\\]\"]", "").split(",");
            return Arrays.stream(parts)
                    .filter(s -> !s.trim().isEmpty())
                    .anyMatch(id -> id.trim().equals(areaId.toString()));
        } catch (Exception e) {
            log.error("[人员限制] 解析区域ID失败: areaIdsStr={}, error={}", areaIdsStr, e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否在有效日期范围内
     */
    private boolean isValidDateRange(LocalDate effectiveDate, LocalDate expireDate) {
        LocalDate now = LocalDate.now();
        if (effectiveDate != null && now.isBefore(effectiveDate)) {
            return false;
        }
        if (expireDate != null && now.isAfter(expireDate)) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否在有效时段内
     */
    private boolean isValidTimeRange(String timeStartStr, String timeEndStr) {
        if (timeStartStr == null || timeEndStr == null) {
            return true; // 无时段限制
        }

        try {
            LocalTime now = LocalTime.now();
            LocalTime timeStart = LocalTime.parse(timeStartStr);
            LocalTime timeEnd = LocalTime.parse(timeEndStr);

            if (timeStart.isBefore(timeEnd)) {
                // 同一天内
                return !now.isBefore(timeStart) && !now.isAfter(timeEnd);
            } else {
                // 跨天（如 22:00-06:00）
                return !now.isBefore(timeStart) || !now.isAfter(timeEnd);
            }
        } catch (Exception e) {
            log.error("[人员限制] 解析时段失败: timeStart={}, timeEnd={}, error={}", timeStartStr, timeEndStr, e.getMessage());
            return true; // 解析失败，默认允许
        }
    }

    /**
     * 转换为VO
     */
    private AccessPersonRestrictionVO convertToVO(AccessPersonRestrictionEntity entity) {
        AccessPersonRestrictionVO vo = new AccessPersonRestrictionVO();
        vo.setRestrictionId(entity.getRestrictionId());
        vo.setUserId(entity.getUserId());
        vo.setUserName(entity.getUserName());
        vo.setUserPhone(entity.getUserPhone());
        vo.setRestrictionType(entity.getRestrictionType());
        vo.setAreaIds(entity.getAreaIds());
        vo.setDoorIds(entity.getDoorIds());
        vo.setTimeStart(entity.getTimeStart());
        vo.setTimeEnd(entity.getTimeEnd());
        vo.setEffectiveDate(entity.getEffectiveDate());
        vo.setExpireDate(entity.getExpireDate());
        vo.setReason(entity.getReason());
        vo.setPriority(entity.getPriority());
        vo.setEnabled(entity.getEnabled());
        vo.setDescription(entity.getDescription());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
