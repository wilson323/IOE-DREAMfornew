package net.lab1024.sa.attendance.engine.rule.loader.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.entity.AttendanceRuleEntity;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 规则加载器实现类
 * <p>
 * 规则加载器核心实现，支持从数据库加载规则配置
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component("ruleLoader")
public class RuleLoaderImpl implements RuleLoader {

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Resource
    private ObjectMapper objectMapper;

    // 规则缓存
    private final Map<Long, AttendanceRuleEntity> ruleCache = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> categoryCache = new ConcurrentHashMap<>();

    /**
     * 加载所有启用的规则
     */
    @Override
    public List<Long> loadAllActiveRules() {
        log.debug("[规则加载器] 加载所有启用的规则");

        try {
            // 从数据库查询启用的规则
            List<AttendanceRuleEntity> activeRules = attendanceRuleDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRuleEntity>()
                    .eq(AttendanceRuleEntity::getRuleStatus, 1)
                    .eq(AttendanceRuleEntity::getDeletedFlag, 0)
                    .orderByAsc(AttendanceRuleEntity::getRulePriority)
            );

            // 提取规则ID列表
            List<Long> ruleIds = activeRules.stream()
                .map(AttendanceRuleEntity::getRuleId)
                .collect(Collectors.toList());

            // 更新缓存
            ruleCache.clear();
            activeRules.forEach(rule -> ruleCache.put(rule.getRuleId(), rule));

            // 更新分类缓存
            updateCategoryCache(activeRules);

            log.debug("[规则加载器] 加载完成，规则数量: {}", ruleIds.size());
            return ruleIds;

        } catch (Exception e) {
            log.error("[规则加载器] 加载规则失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据规则分类加载规则
     */
    @Override
    public List<Long> getRulesByCategory(String ruleCategory) {
        log.debug("[规则加载器] 加载分类规则: {}", ruleCategory);

        try {
            // 先从缓存获取
            List<Long> cachedRules = categoryCache.get(ruleCategory);
            if (cachedRules != null) {
                return cachedRules;
            }

            // 缓存未命中，从数据库查询
            List<AttendanceRuleEntity> categoryRules = attendanceRuleDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRuleEntity>()
                    .eq(AttendanceRuleEntity::getRuleCategory, ruleCategory)
                    .eq(AttendanceRuleEntity::getRuleStatus, 1)
                    .eq(AttendanceRuleEntity::getDeletedFlag, 0)
                    .orderByAsc(AttendanceRuleEntity::getRulePriority)
            );

            List<Long> ruleIds = categoryRules.stream()
                .map(AttendanceRuleEntity::getRuleId)
                .collect(Collectors.toList());

            // 更新缓存
            categoryCache.put(ruleCategory, ruleIds);

            log.debug("[规则加载器] 分类规则加载完成，分类: {}, 数量: {}", ruleCategory, ruleIds.size());
            return ruleIds;

        } catch (Exception e) {
            log.error("[规则加载器] 加载分类规则失败: {}", ruleCategory, e);
            return Collections.emptyList();
        }
    }

    /**
     * 加载规则配置
     */
    @Override
    public Map<String, Object> loadRuleConfig(Long ruleId) {
        log.debug("[规则加载器] 加载规则配置: {}", ruleId);

        try {
            AttendanceRuleEntity rule = getRuleEntity(ruleId);
            if (rule == null) {
                log.warn("[规则加载器] 规则不存在: {}", ruleId);
                return Collections.emptyMap();
            }

            // 构建配置Map
            Map<String, Object> config = new HashMap<>();
            config.put("ruleId", rule.getRuleId());
            config.put("ruleCode", rule.getRuleCode());
            config.put("ruleName", rule.getRuleName());
            config.put("ruleCategory", rule.getRuleCategory());
            config.put("ruleType", rule.getRuleType());
            config.put("ruleCondition", rule.getRuleCondition());
            config.put("ruleAction", rule.getRuleAction());
            config.put("rulePriority", rule.getRulePriority());
            config.put("departmentIds", rule.getDepartmentIds());
            config.put("userIds", rule.getUserIds());
            config.put("effectiveStartTime", rule.getEffectiveStartTime());
            config.put("effectiveEndTime", rule.getEffectiveEndTime());
            config.put("ruleScope", rule.getRuleScope());

            log.debug("[规则加载器] 规则配置加载完成: {}", ruleId);
            return config;

        } catch (Exception e) {
            log.error("[规则加载器] 加载规则配置失败: {}", ruleId, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 加载规则条件定义
     */
    @Override
    public String loadRuleCondition(Long ruleId) {
        log.debug("[规则加载器] 加载规则条件: {}", ruleId);

        try {
            AttendanceRuleEntity rule = getRuleEntity(ruleId);
            if (rule == null) {
                return null;
            }

            String condition = rule.getRuleCondition();
            log.debug("[规则加载器] 规则条件加载完成: {}, 条件: {}", ruleId, condition);
            return condition;

        } catch (Exception e) {
            log.error("[规则加载器] 加载规则条件失败: {}", ruleId, e);
            return null;
        }
    }

    /**
     * 加载规则动作定义
     */
    @Override
    public Map<String, Object> loadRuleAction(Long ruleId) {
        log.debug("[规则加载器] 加载规则动作: {}", ruleId);

        try {
            AttendanceRuleEntity rule = getRuleEntity(ruleId);
            if (rule == null || !StringUtils.hasText(rule.getRuleAction())) {
                return Collections.emptyMap();
            }

            // 解析动作配置JSON
            Map<String, Object> action = objectMapper.readValue(
                rule.getRuleAction(),
                new TypeReference<Map<String, Object>>() {}
            );

            log.debug("[规则加载器] 规则动作加载完成: {}", ruleId);
            return action;

        } catch (Exception e) {
            log.error("[规则加载器] 加载规则动作失败: {}", ruleId, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 检查规则是否存在
     */
    @Override
    public boolean ruleExists(Long ruleId) {
        try {
            return getRuleEntity(ruleId) != null;
        } catch (Exception e) {
            log.error("[规则加载器] 检查规则存在性失败: {}", ruleId, e);
            return false;
        }
    }

    /**
     * 获取规则版本
     */
    @Override
    public String getRuleVersion(Long ruleId) {
        try {
            AttendanceRuleEntity rule = getRuleEntity(ruleId);
            if (rule == null) {
                return null;
            }

            // 从规则配置或实体中获取版本信息
            Map<String, Object> config = loadRuleConfig(ruleId);
            String version = (String) config.get("version");
            if (version == null) {
                version = "1.0.0"; // 默认版本
            }

            return version;

        } catch (Exception e) {
            log.error("[规则加载器] 获取规则版本失败: {}", ruleId, e);
            return "1.0.0"; // 默认版本
        }
    }

    /**
     * 重新加载规则缓存
     */
    @Override
    public void reloadRule(Long ruleId) {
        log.info("[规则加载器] 重新加载规则缓存: {}", ruleId);

        try {
            // 清除指定规则的缓存
            ruleCache.remove(ruleId);

            // 重新加载规则
            AttendanceRuleEntity rule = attendanceRuleDao.selectById(ruleId);
            if (rule != null) {
                ruleCache.put(ruleId, rule);

                // 更新分类缓存
                String category = rule.getRuleCategory();
                if (StringUtils.hasText(category)) {
                    categoryCache.remove(category);
                }
            }

            log.info("[规则加载器] 规则缓存重新加载完成: {}", ruleId);

        } catch (Exception e) {
            log.error("[规则加载器] 重新加载规则缓存失败: {}", ruleId, e);
        }
    }

    /**
     * 重新加载所有规则缓存
     */
    @Override
    public void reloadAllRules() {
        log.info("[规则加载器] 重新加载所有规则缓存");

        try {
            ruleCache.clear();
            categoryCache.clear();
            loadAllActiveRules();

            log.info("[规则加载器] 所有规则缓存重新加载完成");

        } catch (Exception e) {
            log.error("[规则加载器] 重新加载所有规则缓存失败", e);
        }
    }

    /**
     * 获取规则统计信息
     */
    @Override
    public Map<String, Object> getRuleStatistics() {
        log.debug("[规则加载器] 获取规则统计信息");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总规则数
            long totalRules = attendanceRuleDao.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRuleEntity>()
                    .eq(AttendanceRuleEntity::getDeletedFlag, 0)
            );

            // 启用规则数
            long activeRules = attendanceRuleDao.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRuleEntity>()
                    .eq(AttendanceRuleEntity::getRuleStatus, 1)
                    .eq(AttendanceRuleEntity::getDeletedFlag, 0)
            );

            // 按分类统计
            List<Map<String, Object>> categoryStats = attendanceRuleDao.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRuleEntity>()
                    .select(AttendanceRuleEntity::getRuleCategory)
                    .groupBy(AttendanceRuleEntity::getRuleCategory)
            );

            // 缓存统计
            int cachedRules = ruleCache.size();
            int cachedCategories = categoryCache.size();

            statistics.put("totalRules", totalRules);
            statistics.put("activeRules", activeRules);
            statistics.put("inactiveRules", totalRules - activeRules);
            statistics.put("cachedRules", cachedRules);
            statistics.put("cachedCategories", cachedCategories);
            statistics.put("categoryCount", categoryStats.size());
            statistics.put("lastUpdateTime", LocalDateTime.now());

            log.debug("[规则加载器] 规则统计信息: {}", statistics);
            return statistics;

        } catch (Exception e) {
            log.error("[规则加载器] 获取规则统计信息失败", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 检查规则是否在有效期内
     */
    @Override
    public boolean isRuleEffective(Long ruleId) {
        try {
            AttendanceRuleEntity rule = getRuleEntity(ruleId);
            if (rule == null) {
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime effectiveTime = rule.getEffectiveStartTime() != null ?
                LocalDateTime.parse(rule.getEffectiveStartTime()) : null;
            LocalDateTime expireTime = rule.getEffectiveEndTime() != null ?
                LocalDateTime.parse(rule.getEffectiveEndTime()) : null;

            // 检查生效时间
            if (effectiveTime != null && now.isBefore(effectiveTime)) {
                return false;
            }

            // 检查过期时间
            if (expireTime != null && now.isAfter(expireTime)) {
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("[规则加载器] 检查规则有效性失败: {}", ruleId, e);
            return false;
        }
    }

    /**
     * 获取规则实体
     */
    private AttendanceRuleEntity getRuleEntity(Long ruleId) {
        // 先从缓存获取
        AttendanceRuleEntity cachedRule = ruleCache.get(ruleId);
        if (cachedRule != null) {
            return cachedRule;
        }

        // 缓存未命中，从数据库查询
        try {
            AttendanceRuleEntity rule = attendanceRuleDao.selectById(ruleId);
            if (rule != null && rule.getDeletedFlag() == 0) {
                ruleCache.put(ruleId, rule);
            }
            return rule;
        } catch (Exception e) {
            log.error("[规则加载器] 获取规则实体失败: {}", ruleId, e);
            return null;
        }
    }

    /**
     * 更新分类缓存
     */
    private void updateCategoryCache(List<AttendanceRuleEntity> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        // 清除旧缓存
        categoryCache.clear();

        // 按分类分组
        Map<String, List<Long>> categoryMap = rules.stream()
            .filter(rule -> StringUtils.hasText(rule.getRuleCategory()))
            .collect(Collectors.groupingBy(
                AttendanceRuleEntity::getRuleCategory,
                Collectors.mapping(AttendanceRuleEntity::getRuleId, Collectors.toList())
            ));

        // 更新缓存
        categoryCache.putAll(categoryMap);

        log.debug("[规则加载器] 分类缓存更新完成，分类数量: {}", categoryMap.size());
    }
}
