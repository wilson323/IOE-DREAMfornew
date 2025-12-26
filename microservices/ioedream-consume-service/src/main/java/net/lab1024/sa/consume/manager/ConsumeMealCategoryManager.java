package net.lab1024.sa.consume.manager;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.consume.dao.ConsumeMealCategoryDao;
import net.lab1024.sa.consume.domain.vo.ConsumeMealCategoryVO;
import net.lab1024.sa.common.entity.consume.ConsumeMealCategoryEntity;
import net.lab1024.sa.consume.exception.ConsumeMealCategoryException;

/**
 * 消费餐次分类业务管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 负责复杂的业务逻辑编排
 * - 处理层级结构和业务规则验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeMealCategoryManager {

    private final ConsumeMealCategoryDao consumeMealCategoryDao;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     *
     * @param consumeMealCategoryDao 餐次分类数据访问对象
     * @param objectMapper JSON处理工具
     */
    public ConsumeMealCategoryManager(ConsumeMealCategoryDao consumeMealCategoryDao, ObjectMapper objectMapper) {
        this.consumeMealCategoryDao = consumeMealCategoryDao;
        this.objectMapper = objectMapper;
    }

    /**
     * 验证分类编码唯一性
     *
     * @param categoryCode 分类编码
     * @param excludeId 排除的分类ID
     * @return 是否唯一
     */
    public boolean isCategoryCodeUnique(String categoryCode, Long excludeId) {
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            return false;
        }

        int count = consumeMealCategoryDao.countByCode(categoryCode.trim(), excludeId);
        return count == 0;
    }

    /**
     * 验证分类层级合法性
     *
     * @param parentId 父分类ID
     * @param categoryLevel 分类层级
     * @return 是否合法
     */
    public boolean validateCategoryLevel(Long parentId, Integer categoryLevel) {
        if (categoryLevel == null || categoryLevel < 1 || categoryLevel > 3) {
            return false;
        }

        if (categoryLevel == 1) {
            // 根分类不应该有父分类
            return parentId == null || parentId == 0;
        }

        // 检查父分类是否存在且层级合法
        if (parentId != null && parentId != 0) {
            ConsumeMealCategoryEntity parentCategory = consumeMealCategoryDao.selectById(parentId);
            if (parentCategory == null) {
                return false;
            }

            // 子分类的层级应该是父分类层级+1
            return categoryLevel.equals(parentCategory.getCategoryLevel() + 1);
        }

        return false;
    }

    /**
     * 获取下一个可用的排序号
     *
     * @param parentId 父分类ID
     * @return 下一个排序号
     */
    public Integer getNextSortOrder(Long parentId) {
        Integer maxSortOrder = consumeMealCategoryDao.getMaxSortOrder(parentId);
        return (maxSortOrder == null || maxSortOrder < 0) ? 1 : maxSortOrder + 1;
    }

    /**
     * 检查分类是否可以删除
     *
     * @param categoryId 分类ID
     * @return 检查结果，包含是否可删除和相关记录数
     */
    public Map<String, Object> checkDeleteCategory(Long categoryId) {
        Map<String, Object> result = new HashMap<>();

        ConsumeMealCategoryEntity category = consumeMealCategoryDao.selectById(categoryId);
        if (category == null) {
            result.put("canDelete", false);
            result.put("reason", "分类不存在");
            return result;
        }

        // 系统预设分类不能删除
        if (category.isSystem()) {
            result.put("canDelete", false);
            result.put("reason", "系统预设分类不能删除");
            return result;
        }

        // 检查是否有子分类
        int childrenCount = consumeMealCategoryDao.countChildren(categoryId);
        if (childrenCount > 0) {
            result.put("canDelete", false);
            result.put("reason", "存在子分类，无法删除");
            result.put("childrenCount", childrenCount);
            return result;
        }

        // 检查是否有关联的业务记录
        Map<String, Long> relatedRecords = consumeMealCategoryDao.countRelatedRecords(categoryId);
        result.put("canDelete", true);
        result.put("relatedRecords", relatedRecords);

        return result;
    }

    /**
     * 构建分类树结构
     *
     * @param categories 分类列表
     * @return 树形结构分类列表
     */
    public List<ConsumeMealCategoryVO> buildCategoryTree(List<ConsumeMealCategoryVO> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        // 先构建父分类映射
        Map<Long, List<ConsumeMealCategoryVO>> childrenMap = new HashMap<>();
        List<ConsumeMealCategoryVO> rootCategories = new ArrayList<>();

        for (ConsumeMealCategoryVO category : categories) {
            Long parentId = category.getParentId() == null ? 0 : category.getParentId();

            if (parentId == 0) {
                // 根分类
                rootCategories.add(category);
            } else {
                // 子分类
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
            }
        }

        // 递归构建树结构
        for (ConsumeMealCategoryVO rootCategory : rootCategories) {
            buildSubtree(rootCategory, childrenMap);
        }

        // 按排序号排序
        rootCategories.sort((a, b) -> {
            int sortA = a.getSortOrder() == null ? 0 : a.getSortOrder();
            int sortB = b.getSortOrder() == null ? 0 : b.getSortOrder();
            return Integer.compare(sortA, sortB);
        });

        return rootCategories;
    }

    /**
     * 递归构建子树
     */
    private void buildSubtree(ConsumeMealCategoryVO parent, Map<Long, List<ConsumeMealCategoryVO>> childrenMap) {
        List<ConsumeMealCategoryVO> children = childrenMap.get(parent.getCategoryId());
        if (children == null || children.isEmpty()) {
            return;
        }

        // 按排序号排序
        children.sort((a, b) -> {
            int sortA = a.getSortOrder() == null ? 0 : a.getSortOrder();
            int sortB = b.getSortOrder() == null ? 0 : b.getSortOrder();
            return Integer.compare(sortA, sortB);
        });

        parent.setChildren(children);

        // 递归构建子节点的子树
        for (ConsumeMealCategoryVO child : children) {
            buildSubtree(child, childrenMap);
        }
    }

    /**
     * 检查分类是否在当前时间可用
     *
     * @param category 分类信息
     * @param currentTime 当前时间
     * @return 是否可用
     */
    public boolean isAvailableAtTime(ConsumeMealCategoryEntity category, LocalDateTime currentTime) {
        if (category == null || currentTime == null) {
            return false;
        }

        if (category.getAvailableTimePeriods() == null || category.getAvailableTimePeriods().trim().isEmpty()) {
            return true; // 没有时间限制，一直可用
        }

        try {
            List<String> timePeriods = parseTimePeriods(category.getAvailableTimePeriods());
            LocalTime currentLocalTime = currentTime.toLocalTime();

            for (String timePeriod : timePeriods) {
                if (isTimeInRange(currentLocalTime, timePeriod)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("解析时间段失败: category={}, timePeriods={}, error={}",
                category.getCategoryId(), category.getAvailableTimePeriods(), e.getMessage(), e);
            return true; // 解析失败时默认可用，避免影响业务
        }
    }

    /**
     * 解析时间段JSON
     */
    private List<String> parseTimePeriods(String timePeriodsJson) throws JsonProcessingException {
        return objectMapper.readValue(timePeriodsJson, new TypeReference<List<String>>() {});
    }

    /**
     * 检查时间是否在指定范围内
     */
    private boolean isTimeInRange(LocalTime currentTime, String timeRange) {
        try {
            String[] times = timeRange.split("-");
            if (times.length != 2) {
                return false;
            }

            LocalTime startTime = LocalTime.parse(times[0].trim());
            LocalTime endTime = LocalTime.parse(times[1].trim());

            return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        } catch (Exception e) {
            log.error("解析时间范围失败: timeRange={}, error={}", timeRange, e.getMessage());
            return false;
        }
    }

    /**
     * 验证分类业务规则
     *
     * @param category 分类信息
     * @throws ConsumeMealCategoryException 业务规则验证失败时抛出
     */
    public void validateCategoryRules(ConsumeMealCategoryEntity category) {
        List<String> errors = category.validateBusinessRules();

        if (!errors.isEmpty()) {
            throw ConsumeMealCategoryException.invalidParameter(errors.toString());
        }

        // 额外的业务规则验证
        if (category.getParentId() != null && category.getParentId() > 0) {
            // 验证父分类存在性
            ConsumeMealCategoryEntity parentCategory = consumeMealCategoryDao.selectById(category.getParentId());
            if (parentCategory == null) {
                throw ConsumeMealCategoryException.parentNotFound(category.getParentId());
            }

            // 验证层级关系
            if (!validateCategoryLevel(category.getParentId(), category.getCategoryLevel())) {
                throw ConsumeMealCategoryException.invalidCategoryLevel(
                    String.format("父分类层级为%d，子分类层级不能为%d",
                        parentCategory.getCategoryLevel(), category.getCategoryLevel()));
            }
        }

        // 验证编码唯一性
        if (!isCategoryCodeUnique(category.getCategoryCode(), category.getCategoryId())) {
            throw ConsumeMealCategoryException.duplicateCode(category.getCategoryCode());
        }
    }

    /**
     * 计算分类路径
     *
     * @param category 分类信息
     * @return 完整的分类路径
     */
    public String calculateCategoryPath(ConsumeMealCategoryEntity category) {
        List<String> pathNames = new ArrayList<>();
        calculateCategoryPathRecursive(category, pathNames);

        return String.join(" > ", pathNames);
    }

    /**
     * 递归计算分类路径
     */
    private void calculateCategoryPathRecursive(ConsumeMealCategoryEntity category, List<String> pathNames) {
        if (category == null) {
            return;
        }

        if (category.getParentId() != null && category.getParentId() > 0) {
            ConsumeMealCategoryEntity parentCategory = consumeMealCategoryDao.selectById(category.getParentId());
            calculateCategoryPathRecursive(parentCategory, pathNames);
        }

        pathNames.add(category.getCategoryName());
    }

    /**
     * 获取分类统计数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    public Map<String, Object> getCategoryStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = consumeMealCategoryDao.getCategoryStatistics(startDate, endDate);

        if (statistics == null) {
            statistics = new HashMap<>();
        }

        // 计算额外的统计指标
        List<ConsumeMealCategoryVO> categories = consumeMealCategoryDao.selectAllEnabled();
        statistics.put("totalCategories", categories.size());

        int systemCategories = categories.stream()
                .mapToInt(cat -> cat.getIsSystem() != null && cat.getIsSystem() == 1 ? 1 : 0)
                .sum();
        statistics.put("systemCategories", systemCategories);
        statistics.put("customCategories", categories.size() - systemCategories);

        return statistics;
    }
}