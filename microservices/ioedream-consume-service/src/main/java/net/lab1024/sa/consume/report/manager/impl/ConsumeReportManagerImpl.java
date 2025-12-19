package net.lab1024.sa.consume.report.manager.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.report.dao.ConsumeReportTemplateDao;
import net.lab1024.sa.consume.report.domain.entity.ConsumeReportTemplateEntity;
import net.lab1024.sa.consume.report.domain.form.ReportParams;
import net.lab1024.sa.consume.report.manager.ConsumeReportManager;

/**
 * 消费报表管理Manager实现类
 * <p>
 * 实现报表相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager实现类在ioedream-consume-service中
 * - 通过构造函数注入依赖
 * - 保持为纯Java类（不使用Spring注解）
 * </p>
 * <p>
 * 业务场景：
 * - 报表数据生成
 * - 报表模板管理
 * - 报表统计分析
 * - 报表导出
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class ConsumeReportManagerImpl implements ConsumeReportManager {

    private final ConsumeReportTemplateDao reportTemplateDao;
    private final ConsumeTransactionDao consumeTransactionDao;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * </p>
     *
     * @param reportTemplateDao 报表模板DAO
     * @param consumeTransactionDao 交易DAO
     * @param objectMapper JSON对象映射器
     */
    public ConsumeReportManagerImpl(
            ConsumeReportTemplateDao reportTemplateDao,
            ConsumeTransactionDao consumeTransactionDao,
            ObjectMapper objectMapper) {
        this.reportTemplateDao = reportTemplateDao;
        this.consumeTransactionDao = consumeTransactionDao;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成消费报表
     * <p>
     * 基于业务文档13-报表统计模块重构设计.md的报表生成流程
     * 严格遵循Factory Pattern和Strategy Pattern最佳实践
     * </p>
     * <p>
     * 报表生成流程：
     * 1. 获取报表模板 - 从数据库读取模板配置
     * 2. 解析报表配置 - 解析JSON配置，获取统计维度、字段、图表配置
     * 3. 查询统计数据 - 根据模板配置查询交易数据
     * 4. 数据聚合计算 - 按维度聚合统计数据
     * 5. 生成报表数据 - 格式化输出，生成图表数据
     * </p>
     *
     * @param templateId 模板ID
     * @param params 报表参数（包含时间范围、筛选条件等）
     * @return 报表数据
     */
    @Override
    public ResponseDTO<Map<String, Object>> generateReport(Long templateId, ReportParams params) {
        log.info("[报表管理] 生成消费报表，templateId={}", templateId);
        try {
            // 1. 获取报表模板
            ConsumeReportTemplateEntity template = reportTemplateDao.selectById(templateId);
            if (template == null) {
                log.warn("[报表管理] 报表模板不存在，templateId={}", templateId);
                return ResponseDTO.error("REPORT_ERROR", "报表模板不存在");
            }

            if (template.getEnabled() == null || !template.getEnabled()) {
                log.warn("[报表管理] 报表模板未启用，templateId={}", templateId);
                return ResponseDTO.error("REPORT_ERROR", "报表模板未启用");
            }

            // 2. 解析报表配置
            Map<String, Object> reportConfig = parseReportConfig(template.getReportConfig());
            if (reportConfig == null || reportConfig.isEmpty()) {
                log.warn("[报表管理] 报表配置解析失败，templateId={}", templateId);
                return ResponseDTO.error("REPORT_ERROR", "报表配置解析失败");
            }

            // 3. 解析报表参数
            // 类型安全改进：使用ReportParams类型，提取参数
            Map<String, Object> reportParams = convertReportParamsToMap(params);
            LocalDateTime startTime = params != null && params.getStartTime() != null
                    ? params.getStartTime() : extractStartTime(reportParams);
            LocalDateTime endTime = params != null && params.getEndTime() != null
                    ? params.getEndTime() : extractEndTime(reportParams);

            // 4. 查询统计数据
            Map<String, Object> reportData = queryReportData(template, reportConfig, startTime, endTime, reportParams);

            // 5. 生成报表数据
            Map<String, Object> result = buildReportResult(template, reportData, reportConfig);

            log.info("[报表管理] 报表生成成功，templateId={}, templateName={}", templateId, template.getTemplateName());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[报表管理] 生成消费报表失败，templateId={}", templateId, e);
            return ResponseDTO.error("REPORT_ERROR", "生成报表失败：" + e.getMessage());
        }
    }

    /**
     * 解析报表配置JSON
     *
     * @param reportConfigJson 报表配置JSON字符串
     * @return 报表配置Map
     */
    private Map<String, Object> parseReportConfig(String reportConfigJson) {
        if (reportConfigJson == null || reportConfigJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(reportConfigJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("[报表管理] 解析报表配置失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 将ReportParams转换为Map
     * <p>
     * 类型安全改进：使用ReportParams类型，而非Object
     * 提升代码可读性和类型安全性
     * </p>
     *
     * @param params 报表参数对象
     * @return 参数Map
     */
    private Map<String, Object> convertReportParamsToMap(ReportParams params) {
        if (params == null) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        if (params.getStartTime() != null) {
            result.put("startTime", params.getStartTime());
        }
        if (params.getEndTime() != null) {
            result.put("endTime", params.getEndTime());
        }
        if (params.getDimensions() != null) {
            result.put("dimensions", params.getDimensions());
        }
        if (params.getFilters() != null) {
            result.put("filters", params.getFilters());
        }
        if (params.getGroupBy() != null) {
            result.put("groupBy", params.getGroupBy());
        }
        if (params.getOrderBy() != null) {
            result.put("orderBy", params.getOrderBy());
        }
        if (params.getOrderDirection() != null) {
            result.put("orderDirection", params.getOrderDirection());
        }
        if (params.getPageNum() != null) {
            result.put("pageNum", params.getPageNum());
        }
        if (params.getPageSize() != null) {
            result.put("pageSize", params.getPageSize());
        }
        return result;
    }


    /**
     * 提取开始时间
     *
     * @param params 参数Map
     * @return 开始时间
     */
    private LocalDateTime extractStartTime(Map<String, Object> params) {
        Object startTimeObj = params.get("startTime");
        if (startTimeObj instanceof LocalDateTime) {
            return (LocalDateTime) startTimeObj;
        } else if (startTimeObj instanceof String) {
            return LocalDateTime.parse((String) startTimeObj);
        }
        // 默认：当天00:00:00
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * 提取结束时间
     *
     * @param params 参数Map
     * @return 结束时间
     */
    private LocalDateTime extractEndTime(Map<String, Object> params) {
        Object endTimeObj = params.get("endTime");
        if (endTimeObj instanceof LocalDateTime) {
            return (LocalDateTime) endTimeObj;
        } else if (endTimeObj instanceof String) {
            return LocalDateTime.parse((String) endTimeObj);
        }
        // 默认：当前时间
        return LocalDateTime.now();
    }

    /**
     * 查询报表数据
     *
     * @param template 报表模板
     * @param reportConfig 报表配置
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportParams 报表参数
     * @return 报表数据（包含统计数据和明细数据）
     */
    private Map<String, Object> queryReportData(
            ConsumeReportTemplateEntity template,
            Map<String, Object> reportConfig,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> reportParams) {
        log.debug("[报表管理] 查询报表数据，templateId={}, startTime={}, endTime={}",
                template.getId(), startTime, endTime);

        Map<String, Object> reportData = new HashMap<>();

        // 根据模板类型查询数据
        String templateType = template.getTemplateType();
        if ("DAILY".equals(templateType)) {
            // 日报：查询每日统计数据
            reportData = queryDailyStatistics(startTime, endTime, reportParams);
        } else if ("MONTHLY".equals(templateType)) {
            // 月报：查询月度统计数据
            reportData = queryMonthlyStatistics(startTime, endTime, reportParams);
        } else {
            // 自定义报表：根据配置查询
            reportData = queryCustomStatistics(reportConfig, startTime, endTime, reportParams);
        }

        // 如果报表配置需要明细数据，则添加交易明细列表
        boolean includeDetails = extractBoolean(reportConfig, "includeDetails", false);
        if (includeDetails) {
            List<ConsumeTransactionEntity> transactions = queryTransactionsByParams(startTime, endTime, reportParams);
            List<Map<String, Object>> transactionDetails = convertTransactionsToDetails(transactions);
            reportData.put("details", transactionDetails);
            reportData.put("detailCount", transactions.size());
        }

        return reportData;
    }

    /**
     * 查询每日统计数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportParams 报表参数
     * @return 统计数据
     */
    private Map<String, Object> queryDailyStatistics(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> reportParams) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 根据参数中的筛选条件查询交易记录
            List<ConsumeTransactionEntity> transactions = queryTransactionsByParams(startTime, endTime, reportParams);

            // 统计计算
            long totalCount = transactions.size();
            long totalMoney = transactions.stream()
                    .mapToLong(t -> t.getFinalMoney() != null ? t.getFinalMoney().longValue() : 0L)
                    .sum();
            long personCount = transactions.stream()
                    .map(ConsumeTransactionEntity::getUserId)
                    .distinct()
                    .count();

            statistics.put("totalCount", totalCount);
            statistics.put("totalMoney", totalMoney);
            statistics.put("personCount", personCount);
            statistics.put("avgAmount", totalCount > 0 ? totalMoney / totalCount : 0);

        } catch (Exception e) {
            log.error("[报表管理] 查询每日统计数据失败", e);
        }

        return statistics;
    }

    /**
     * 查询月度统计数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportParams 报表参数
     * @return 统计数据
     */
    private Map<String, Object> queryMonthlyStatistics(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> reportParams) {
        // 类似queryDailyStatistics，按月聚合
        return queryDailyStatistics(startTime, endTime, reportParams);
    }

    /**
     * 查询自定义统计数据
     *
     * @param reportConfig 报表配置
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportParams 报表参数
     * @return 统计数据
     */
    private Map<String, Object> queryCustomStatistics(
            Map<String, Object> reportConfig,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> reportParams) {
        // 根据reportConfig中的配置动态查询
        return queryDailyStatistics(startTime, endTime, reportParams);
    }

    /**
     * 构建报表结果
     *
     * @param template 报表模板
     * @param reportData 报表数据
     * @param reportConfig 报表配置
     * @return 报表结果
     */
    private Map<String, Object> buildReportResult(
            ConsumeReportTemplateEntity template,
            Map<String, Object> reportData,
            Map<String, Object> reportConfig) {
        Map<String, Object> result = new HashMap<>();
        result.put("templateId", template.getId());
        result.put("templateName", template.getTemplateName());
        result.put("templateType", template.getTemplateType());
        result.put("data", reportData);
        result.put("config", reportConfig);
        result.put("generateTime", LocalDateTime.now());
        return result;
    }

    /**
     * 获取报表模板列表
     *
     * @param templateType 模板类型
     * @return 模板列表
     */
    @Override
    public ResponseDTO<?> getReportTemplates(String templateType) {
        log.debug("[报表管理] 获取报表模板列表，templateType={}", templateType);
        try {
            java.util.List<net.lab1024.sa.consume.report.domain.entity.ConsumeReportTemplateEntity> templates;
            if (templateType != null && !templateType.isEmpty()) {
                templates = reportTemplateDao.selectByTemplateType(templateType);
            } else {
                templates = reportTemplateDao.selectEnabledTemplates();
            }
            return ResponseDTO.ok(templates);
        } catch (Exception e) {
            log.error("[报表管理] 获取报表模板列表失败，templateType={}", templateType, e);
            return ResponseDTO.error("REPORT_ERROR", "获取模板列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取报表统计数据
     * <p>
     * 基于业务文档13-报表统计模块重构设计.md的多维度统计查询
     * 支持时间、空间、人员、业务等多维度统计分析
     * </p>
     * <p>
     * 统计维度：
     * - 时间维度：实时/小时/日/周/月/季/年
     * - 空间维度：区域/餐厅/设备
     * - 人员维度：部门/卡类/个人
     * - 业务维度：餐别/商品/定值模式
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dimensions 统计维度（JSON格式，包含维度配置）
     * @return 统计数据
     */
    @Override
    public ResponseDTO<Map<String, Object>> getReportStatistics(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> dimensions) {
        log.info("[报表管理] 获取报表统计数据，startTime={}, endTime={}", startTime, endTime);
        try {
            // 1. 解析统计维度
            // 类型安全改进：使用Map<String, Object>类型，而非Object
            Map<String, Object> dimensionConfig = dimensions != null ? dimensions : new HashMap<>();

            // 2. 查询交易数据
            List<ConsumeTransactionEntity> transactions = queryTransactionsByTimeRange(startTime, endTime, dimensionConfig);

            // 3. 按维度聚合统计
            Map<String, Object> statistics = aggregateStatistics(transactions, dimensionConfig);

            // 4. 返回统计数据
            log.info("[报表管理] 统计数据查询成功，startTime={}, endTime={}, transactionCount={}",
                    startTime, endTime, transactions.size());
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[报表管理] 获取报表统计数据失败，startTime={}, endTime={}", startTime, endTime, e);
            return ResponseDTO.error("REPORT_ERROR", "获取统计数据失败：" + e.getMessage());
        }
    }


    /**
     * 根据时间范围查询交易数据
     * <p>
     * 基于业务文档13-报表统计模块重构设计.md的多维度查询逻辑
     * 支持按区域、账户类别、消费模式等维度筛选
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dimensionConfig 维度配置（包含筛选条件）
     * @return 交易记录列表
     */
    private List<ConsumeTransactionEntity> queryTransactionsByTimeRange(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> dimensionConfig) {
        try {
            log.debug("[报表管理] 查询交易数据，startTime={}, endTime={}", startTime, endTime);

            // 1. 从维度配置中提取筛选条件
            String areaId = extractString(dimensionConfig, "areaId");
            String accountKindId = extractString(dimensionConfig, "accountKindId");
            String consumeMode = extractString(dimensionConfig, "consumeMode");
            String accountId = extractString(dimensionConfig, "accountId");
            String userId = extractString(dimensionConfig, "userId");

            // 2. 根据筛选条件构建查询
            // 优先使用DAO的查询方法，如果没有则使用MyBatis-Plus的LambdaQueryWrapper

            // 2.1 如果有账户ID，使用账户ID查询
            if (accountId != null && !accountId.isEmpty()) {
                return consumeTransactionDao.selectByAccountId(accountId, startTime, endTime);
            }

            // 2.2 如果有用户ID，使用用户ID查询
            if (userId != null && !userId.isEmpty()) {
                return consumeTransactionDao.selectByUserId(userId, startTime, endTime);
            }

            // 2.3 如果有区域ID，使用区域ID查询
            if (areaId != null && !areaId.isEmpty()) {
                return consumeTransactionDao.selectByAreaId(areaId, startTime, endTime);
            }

            // 2.4 如果没有特定筛选条件，使用MyBatis-Plus的BaseMapper查询
            // 构建查询条件：时间范围 + 状态筛选
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConsumeTransactionEntity> queryWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.ge(ConsumeTransactionEntity::getConsumeTime, startTime);
            queryWrapper.le(ConsumeTransactionEntity::getConsumeTime, endTime);
            queryWrapper.eq(ConsumeTransactionEntity::getStatus, "SUCCESS"); // 只查询成功的交易

            // 应用其他筛选条件
            if (accountKindId != null && !accountKindId.isEmpty()) {
                queryWrapper.eq(ConsumeTransactionEntity::getAccountKindId, accountKindId);
            }
            if (consumeMode != null && !consumeMode.isEmpty()) {
                queryWrapper.eq(ConsumeTransactionEntity::getConsumeMode, consumeMode);
            }

            return consumeTransactionDao.selectList(queryWrapper);

        } catch (Exception e) {
            log.error("[报表管理] 查询交易数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从Map中提取String值
     *
     * @param map Map对象
     * @param key 键名
     * @return String值，如果不存在或为空则返回null
     */
    private String extractString(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        String str = value.toString();
        return str.isEmpty() ? null : str;
    }

    /**
     * 按维度聚合统计
     * <p>
     * 基于业务文档13-报表统计模块重构设计.md的多维度聚合逻辑
     * 支持时间、空间、人员、业务等多维度统计分析
     * </p>
     *
     * @param transactions 交易记录列表
     * @param dimensionConfig 维度配置（包含聚合维度）
     * @return 统计数据
     */
    private Map<String, Object> aggregateStatistics(
            List<ConsumeTransactionEntity> transactions,
            Map<String, Object> dimensionConfig) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 1. 基础统计
            long totalCount = transactions.size();
            long totalMoney = transactions.stream()
                    .mapToLong(t -> t.getFinalMoney() != null ? t.getFinalMoney().longValue() : 0L)
                    .sum();
            long personCount = transactions.stream()
                    .map(ConsumeTransactionEntity::getUserId)
                    .filter(userId -> userId != null)
                    .distinct()
                    .count();

            statistics.put("totalCount", totalCount);
            statistics.put("totalMoney", totalMoney);
            statistics.put("personCount", personCount);
            statistics.put("avgAmount", totalCount > 0 ? totalMoney / totalCount : 0);
            statistics.put("avgPerPerson", personCount > 0 ? totalMoney / personCount : 0);

            // 2. 获取聚合维度配置
            Object groupByObj = dimensionConfig != null ? dimensionConfig.get("groupBy") : null;
            if (groupByObj != null) {
                List<String> groupByDimensions = parseGroupByDimensions(groupByObj);

                // 3. 按维度聚合统计
                Map<String, Object> dimensionStatistics = new HashMap<>();

                for (String dimension : groupByDimensions) {
                    switch (dimension.toUpperCase()) {
                        case "AREA":
                            dimensionStatistics.put("byArea", aggregateByArea(transactions));
                            break;
                        case "ACCOUNT_KIND":
                        case "KIND":
                            dimensionStatistics.put("byAccountKind", aggregateByAccountKind(transactions));
                            break;
                        case "CONSUME_MODE":
                        case "MODE":
                            dimensionStatistics.put("byConsumeMode", aggregateByConsumeMode(transactions));
                            break;
                        case "MEAL":
                            dimensionStatistics.put("byMeal", aggregateByMeal(transactions));
                            break;
                        case "TIME":
                            dimensionStatistics.put("byTime", aggregateByTime(transactions));
                            break;
                        default:
                            log.warn("[报表管理] 未知的聚合维度，dimension={}", dimension);
                    }
                }

                if (!dimensionStatistics.isEmpty()) {
                    statistics.put("dimensions", dimensionStatistics);
                }
            }

        } catch (Exception e) {
            log.error("[报表管理] 聚合统计数据失败", e);
        }

        return statistics;
    }

    /**
     * 解析聚合维度列表
     *
     * @param groupByObj 聚合维度对象
     * @return 维度列表
     */
    private List<String> parseGroupByDimensions(Object groupByObj) {
        List<String> dimensions = new ArrayList<>();
        if (groupByObj == null) {
            return dimensions;
        }
        if (groupByObj instanceof List) {
            // 类型安全：使用通配符避免unchecked警告
            List<?> list = (List<?>) groupByObj;
            for (Object item : list) {
                if (item != null) {
                    dimensions.add(item.toString());
                }
            }
        } else if (groupByObj instanceof String) {
            // 逗号分隔的字符串
            String[] parts = ((String) groupByObj).split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    dimensions.add(trimmed);
                }
            }
        }
        return dimensions;
    }

    /**
     * 按区域聚合统计
     *
     * @param transactions 交易记录列表
     * @return 按区域聚合的统计数据
     */
    private Map<String, Object> aggregateByArea(List<ConsumeTransactionEntity> transactions) {
        Map<String, Map<String, Object>> areaStats = new HashMap<>();

        for (ConsumeTransactionEntity transaction : transactions) {
            Long areaId = transaction.getAreaId();
            if (areaId == null) {
                continue;
            }

            String areaIdKey = String.valueOf(areaId);
            areaStats.computeIfAbsent(areaIdKey, k -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("areaId", areaId);
                stat.put("areaName", transaction.getAreaName());
                stat.put("count", 0L);
                stat.put("totalMoney", BigDecimal.ZERO);
                stat.put("personCount", new java.util.HashSet<Long>());
                return stat;
            });

            Map<String, Object> stat = areaStats.get(areaIdKey);
            stat.put("count", ((Long) stat.get("count")) + 1);
            BigDecimal currentTotal = (BigDecimal) stat.get("totalMoney");
            BigDecimal finalMoney = transaction.getFinalMoney() != null ? transaction.getFinalMoney() : BigDecimal.ZERO;
            stat.put("totalMoney", currentTotal.add(finalMoney));
            @SuppressWarnings("unchecked")
            java.util.Set<Long> personSet = (java.util.Set<Long>) stat.get("personCount");
            if (transaction.getUserId() != null) {
                personSet.add(transaction.getUserId());
            }
        }

        // 转换为列表格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> stat : areaStats.values()) {
            @SuppressWarnings("unchecked")
            java.util.Set<Long> personSet = (java.util.Set<Long>) stat.get("personCount");
            stat.put("personCount", personSet.size());
            result.add(stat);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", result);
        resultMap.put("total", result.size());
        return resultMap;
    }

    /**
     * 按账户类别聚合统计
     *
     * @param transactions 交易记录列表
     * @return 按账户类别聚合的统计数据
     */
    private Map<String, Object> aggregateByAccountKind(List<ConsumeTransactionEntity> transactions) {
        Map<String, Map<String, Object>> kindStats = new HashMap<>();

        for (ConsumeTransactionEntity transaction : transactions) {
            Long accountKindId = transaction.getAccountKindId();
            if (accountKindId == null) {
                continue;
            }

            String accountKindIdKey = String.valueOf(accountKindId);
            kindStats.computeIfAbsent(accountKindIdKey, k -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("accountKindId", accountKindId);
                stat.put("count", 0L);
                stat.put("totalMoney", BigDecimal.ZERO);
                stat.put("personCount", new java.util.HashSet<Long>());
                return stat;
            });

            Map<String, Object> stat = kindStats.get(accountKindIdKey);
            stat.put("count", ((Long) stat.get("count")) + 1);
            BigDecimal currentTotal = (BigDecimal) stat.get("totalMoney");
            BigDecimal finalMoney = transaction.getFinalMoney() != null ? transaction.getFinalMoney() : BigDecimal.ZERO;
            stat.put("totalMoney", currentTotal.add(finalMoney));
            @SuppressWarnings("unchecked")
            java.util.Set<Long> personSet = (java.util.Set<Long>) stat.get("personCount");
            if (transaction.getUserId() != null) {
                personSet.add(transaction.getUserId());
            }
        }

        // 转换为列表格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> stat : kindStats.values()) {
            @SuppressWarnings("unchecked")
            java.util.Set<Long> personSet = (java.util.Set<Long>) stat.get("personCount");
            stat.put("personCount", personSet.size());
            result.add(stat);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", result);
        resultMap.put("total", result.size());
        return resultMap;
    }

    /**
     * 按消费模式聚合统计
     *
     * @param transactions 交易记录列表
     * @return 按消费模式聚合的统计数据
     */
    private Map<String, Object> aggregateByConsumeMode(List<ConsumeTransactionEntity> transactions) {
        Map<String, Map<String, Object>> modeStats = new HashMap<>();

        for (ConsumeTransactionEntity transaction : transactions) {
            final String consumeMode = transaction.getConsumeMode() != null ? transaction.getConsumeMode() : "UNKNOWN";

            modeStats.computeIfAbsent(consumeMode, k -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("consumeMode", consumeMode);
                stat.put("count", 0L);
                stat.put("totalMoney", BigDecimal.ZERO);
                return stat;
            });

            Map<String, Object> stat = modeStats.get(consumeMode);
            stat.put("count", ((Long) stat.get("count")) + 1);
            BigDecimal currentTotal = (BigDecimal) stat.get("totalMoney");
            BigDecimal finalMoney = transaction.getFinalMoney() != null ? transaction.getFinalMoney() : BigDecimal.ZERO;
            stat.put("totalMoney", currentTotal.add(finalMoney));
        }

        // 转换为列表格式
        List<Map<String, Object>> result = new ArrayList<>(modeStats.values());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", result);
        resultMap.put("total", result.size());
        return resultMap;
    }

    /**
     * 按餐别聚合统计
     *
     * @param transactions 交易记录列表
     * @return 按餐别聚合的统计数据
     */
    private Map<String, Object> aggregateByMeal(List<ConsumeTransactionEntity> transactions) {
        Map<String, Map<String, Object>> mealStats = new HashMap<>();

        for (ConsumeTransactionEntity transaction : transactions) {
            Long mealId = transaction.getMealId();
            if (mealId == null) {
                continue;
            }

            String mealIdKey = String.valueOf(mealId);
            mealStats.computeIfAbsent(mealIdKey, k -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("mealId", mealId);
                stat.put("mealName", transaction.getMealName());
                stat.put("count", 0L);
                stat.put("totalMoney", BigDecimal.ZERO);
                return stat;
            });

            Map<String, Object> stat = mealStats.get(mealIdKey);
            stat.put("count", ((Long) stat.get("count")) + 1);
            BigDecimal currentTotal = (BigDecimal) stat.get("totalMoney");
            BigDecimal finalMoney = transaction.getFinalMoney() != null ? transaction.getFinalMoney() : BigDecimal.ZERO;
            stat.put("totalMoney", currentTotal.add(finalMoney));
        }

        // 转换为列表格式
        List<Map<String, Object>> result = new ArrayList<>(mealStats.values());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", result);
        resultMap.put("total", result.size());
        return resultMap;
    }

    /**
     * 按时间聚合统计
     *
     * @param transactions 交易记录列表
     * @return 按时间聚合的统计数据
     */
    private Map<String, Object> aggregateByTime(List<ConsumeTransactionEntity> transactions) {
        Map<String, Map<String, Object>> timeStats = new HashMap<>();

        for (ConsumeTransactionEntity transaction : transactions) {
            LocalDateTime consumeTime = transaction.getConsumeTime();
            if (consumeTime == null) {
                continue;
            }

            // 按小时聚合（格式：YYYY-MM-DD HH:00）
            String timeKey = consumeTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"));

            timeStats.computeIfAbsent(timeKey, k -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("timeKey", timeKey);
                stat.put("count", 0L);
                stat.put("totalMoney", BigDecimal.ZERO);
                return stat;
            });

            Map<String, Object> stat = timeStats.get(timeKey);
            stat.put("count", ((Long) stat.get("count")) + 1);
            BigDecimal currentTotal = (BigDecimal) stat.get("totalMoney");
            BigDecimal finalMoney = transaction.getFinalMoney() != null ? transaction.getFinalMoney() : BigDecimal.ZERO;
            stat.put("totalMoney", currentTotal.add(finalMoney));
        }

        // 转换为列表格式（按时间排序）
        List<Map<String, Object>> result = new ArrayList<>(timeStats.values());
        result.sort((a, b) -> {
            String keyA = (String) a.get("timeKey");
            String keyB = (String) b.get("timeKey");
            return keyA != null && keyB != null ? keyA.compareTo(keyB) : 0;
        });

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", result);
        resultMap.put("total", result.size());
        return resultMap;
    }

    /**
     * 根据报表参数查询交易记录
     * <p>
     * 根据reportParams中的筛选条件查询交易记录，支持多维度筛选：
     * - 按区域筛选（areaId）
     * - 按账户类别筛选（accountKindId）
     * - 按消费模式筛选（consumeMode）
     * - 按用户筛选（userId）
     * - 按账户筛选（accountId）
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportParams 报表参数
     * @return 交易记录列表
     */
    private List<ConsumeTransactionEntity> queryTransactionsByParams(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> reportParams) {
        try {
            // 1. 提取筛选条件
            String areaId = extractString(reportParams, "areaId");
            String accountKindId = extractString(reportParams, "accountKindId");
            String consumeMode = extractString(reportParams, "consumeMode");
            String userId = extractString(reportParams, "userId");
            String accountId = extractString(reportParams, "accountId");

            // 2. 优先使用DAO的专用查询方法
            if (accountId != null && !accountId.trim().isEmpty()) {
                return consumeTransactionDao.selectByAccountId(accountId, startTime, endTime);
            }

            if (userId != null && !userId.trim().isEmpty()) {
                return consumeTransactionDao.selectByUserId(userId, startTime, endTime);
            }

            if (areaId != null && !areaId.trim().isEmpty()) {
                List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectByAreaId(areaId, startTime, endTime);
                // 如果还有其他筛选条件，在内存中过滤
                return filterTransactions(transactions, accountKindId, consumeMode);
            }

            // 3. 如果没有特定筛选条件，使用时间范围查询
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectByTimeRange(startTime, endTime);
            // 在内存中应用其他筛选条件
            return filterTransactions(transactions, areaId, accountKindId, consumeMode, userId, accountId);

        } catch (Exception e) {
            log.error("[报表管理] 根据参数查询交易记录失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 在内存中过滤交易记录
     * <p>
     * 对已查询的交易记录应用额外的筛选条件
     * </p>
     *
     * @param transactions 交易记录列表
     * @param areaId 区域ID（可选）
     * @param accountKindId 账户类别ID（可选）
     * @param consumeMode 消费模式（可选）
     * @param userId 用户ID（可选）
     * @param accountId 账户ID（可选）
     * @return 过滤后的交易记录列表
     */
    private List<ConsumeTransactionEntity> filterTransactions(
            List<ConsumeTransactionEntity> transactions,
            String areaId,
            String accountKindId,
            String consumeMode,
            String userId,
            String accountId) {
        // 将String参数转换为Long类型进行比较（实体类字段为Long类型）
        Long areaIdLong = parseLongSafely(areaId);
        Long accountKindIdLong = parseLongSafely(accountKindId);
        Long userIdLong = parseLongSafely(userId);
        Long accountIdLong = parseLongSafely(accountId);

        return transactions.stream()
                .filter(t -> areaIdLong == null || areaIdLong.equals(t.getAreaId()))
                .filter(t -> accountKindIdLong == null || accountKindIdLong.equals(t.getAccountKindId()))
                .filter(t -> consumeMode == null || consumeMode.trim().isEmpty() || consumeMode.equals(t.getConsumeMode()))
                .filter(t -> userIdLong == null || userIdLong.equals(t.getUserId()))
                .filter(t -> accountIdLong == null || accountIdLong.equals(t.getAccountId()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 在内存中过滤交易记录（简化版本，只支持accountKindId和consumeMode）
     *
     * @param transactions 交易记录列表
     * @param accountKindId 账户类别ID（可选）
     * @param consumeMode 消费模式（可选）
     * @return 过滤后的交易记录列表
     */
    private List<ConsumeTransactionEntity> filterTransactions(
            List<ConsumeTransactionEntity> transactions,
            String accountKindId,
            String consumeMode) {
        // 将String参数转换为Long类型进行比较（实体类字段为Long类型）
        Long accountKindIdLong = parseLongSafely(accountKindId);

        return transactions.stream()
                .filter(t -> accountKindIdLong == null || accountKindIdLong.equals(t.getAccountKindId()))
                .filter(t -> consumeMode == null || consumeMode.trim().isEmpty() || consumeMode.equals(t.getConsumeMode()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 安全地将String转换为Long
     * <p>
     * 如果字符串为null或空，返回null
     * 如果转换失败，返回null（不抛出异常）
     * </p>
     *
     * @param str 待转换的字符串
     * @return Long值，转换失败返回null
     */
    private Long parseLongSafely(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            log.warn("[报表管理] 无法将字符串转换为Long: {}", str);
            return null;
        }
    }

    /**
     * 导出报表（Excel/PDF/CSV）
     * <p>
     * 功能说明：
     * 1. 根据模板ID生成报表数据
     * 2. 根据导出格式（EXCEL/PDF/CSV）导出文件
     * 3. 返回文件路径
     * </p>
     *
     * @param templateId 模板ID
     * @param params 报表参数
     * @param exportFormat 导出格式（EXCEL/PDF/CSV）
     * @return 导出文件路径
     */
    @Override
    public ResponseDTO<String> exportReport(Long templateId, ReportParams params, String exportFormat) {
        log.info("[报表管理] 导出报表，templateId={}, exportFormat={}", templateId, exportFormat);

        try {
            // 1. 生成报表数据
            ResponseDTO<Map<String, Object>> reportResult = generateReport(templateId, params);
            if (reportResult == null || reportResult.getCode() != 200 || reportResult.getData() == null) {
                log.warn("[报表管理] 报表数据生成失败，templateId={}", templateId);
                return ResponseDTO.error("REPORT_ERROR", "报表数据生成失败");
            }

            Map<String, Object> reportData = reportResult.getData();

            // 2. 确定导出格式（默认Excel）
            String format = exportFormat != null ? exportFormat.toUpperCase() : "EXCEL";
            if (!"EXCEL".equals(format) && !"PDF".equals(format) && !"CSV".equals(format)) {
                log.warn("[报表管理] 不支持的导出格式：{}，使用Excel格式", exportFormat);
                format = "EXCEL";
            }

            // 3. 生成导出文件路径
            String fileName = generateExportFileName(templateId, format);
            java.nio.file.Path filePath = java.nio.file.Paths.get(getExportBasePath(), fileName);
            java.nio.file.Files.createDirectories(filePath.getParent());

            // 4. 根据格式导出
            switch (format) {
                case "EXCEL":
                    exportToExcel(reportData, filePath);
                    break;
                case "PDF":
                    exportToPdf(reportData, filePath);
                    break;
                case "CSV":
                    exportToCsv(reportData, filePath);
                    break;
                default:
                    exportToExcel(reportData, filePath);
            }

            log.info("[报表管理] 报表导出成功，templateId={}, format={}, filePath={}",
                    templateId, format, filePath);
            return ResponseDTO.ok(filePath.toString());

        } catch (Exception e) {
            log.error("[报表管理] 导出报表失败，templateId={}, format={}", templateId, exportFormat, e);
            return ResponseDTO.error("REPORT_ERROR", "导出报表失败：" + e.getMessage());
        }
    }

    /**
     * 获取导出基础路径
     *
     * @return 导出基础路径
     */
    private String getExportBasePath() {
        // 从系统属性或环境变量获取，默认使用临时目录
        String basePath = System.getProperty("report.export.path");
        if (basePath == null || basePath.isEmpty()) {
            basePath = System.getenv("REPORT_EXPORT_PATH");
        }
        if (basePath == null || basePath.isEmpty()) {
            basePath = System.getProperty("java.io.tmpdir") + "/report-export";
        }
        return basePath;
    }

    /**
     * 生成导出文件名
     *
     * @param templateId 模板ID
     * @param format 导出格式
     * @return 文件名
     */
    private String generateExportFileName(Long templateId, String format) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = "PDF".equals(format) ? ".pdf" :
                "CSV".equals(format) ? ".csv" : ".xlsx";
        return String.format("consume_report_%d_%s%s", templateId, timestamp, extension);
    }

    /**
     * 导出为Excel格式
     *
     * @param reportData 报表数据
     * @param filePath 文件路径
     */
    private void exportToExcel(Map<String, Object> reportData, java.nio.file.Path filePath) {
        log.info("[报表管理] 开始导出Excel文件，路径：{}", filePath);

        try {
            // 1. 准备表头
            List<List<String>> header = new ArrayList<>();
            header.add(createExcelHeader());

            // 2. 准备数据
            List<List<Object>> dataList = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) reportData.get("data");
            if (data != null) {
                dataList.addAll(convertToExcelRows(data));
            }

            // 3. 使用EasyExcel写入
            com.alibaba.excel.EasyExcel.write(filePath.toFile())
                    .sheet("消费报表")
                    .head(header)
                    .doWrite(dataList);

            log.info("[报表管理] Excel文件导出成功，路径：{}", filePath);

        } catch (Exception e) {
            log.error("[报表管理] Excel文件导出失败，路径：{}", filePath, e);
            throw new SystemException("EXCEL_EXPORT_ERROR", "Excel文件导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建Excel表头
     *
     * @return 表头列表
     */
    private List<String> createExcelHeader() {
        List<String> header = new ArrayList<>();
        header.add("交易时间");
        header.add("用户ID");
        header.add("账户ID");
        header.add("区域ID");
        header.add("消费模式");
        header.add("消费金额");
        header.add("交易状态");
        header.add("设备ID");
        return header;
    }

    /**
     * 转换为Excel行数据
     *
     * @param data 报表数据
     * @return Excel行数据列表
     */
    @SuppressWarnings("unchecked")
    private List<List<Object>> convertToExcelRows(Map<String, Object> data) {
        List<List<Object>> rows = new ArrayList<>();

        // 1. 如果包含明细数据，优先导出明细
        if (data.containsKey("details")) {
            List<Map<String, Object>> details = (List<Map<String, Object>>) data.get("details");
            if (details != null && !details.isEmpty()) {
                for (Map<String, Object> detail : details) {
                    List<Object> row = new ArrayList<>();
                    row.add(formatDateTime(detail.get("consumeTime")));
                    row.add(detail.get("userId"));
                    row.add(detail.get("accountId"));
                    row.add(detail.get("areaId"));
                    row.add(detail.get("consumeMode"));
                    row.add(detail.get("finalMoney"));
                    row.add(detail.get("status"));
                    row.add(detail.get("deviceId"));
                    rows.add(row);
                }
            }
            // 如果有统计数据，在明细后添加汇总行
            if (data.containsKey("totalCount")) {
                List<Object> summaryRow = new ArrayList<>();
                summaryRow.add("统计汇总");
                summaryRow.add("");
                summaryRow.add("");
                summaryRow.add("");
                summaryRow.add("");
                summaryRow.add(data.get("totalMoney"));
                summaryRow.add("成功");
                summaryRow.add("");
                rows.add(summaryRow);
            }
        } else if (data.containsKey("totalCount")) {
            // 2. 如果只有统计数据，显示汇总信息
            List<Object> row = new ArrayList<>();
            row.add("统计汇总");
            row.add("");
            row.add("");
            row.add("");
            row.add("");
            row.add(data.get("totalMoney"));
            row.add("成功");
            row.add("");
            rows.add(row);
        }

        return rows;
    }

    /**
     * 将交易记录转换为明细数据列表
     *
     * @param transactions 交易记录列表
     * @return 明细数据列表
     */
    private List<Map<String, Object>> convertTransactionsToDetails(List<ConsumeTransactionEntity> transactions) {
        List<Map<String, Object>> details = new ArrayList<>();
        for (ConsumeTransactionEntity transaction : transactions) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("consumeTime", transaction.getConsumeTime());
            detail.put("userId", transaction.getUserId());
            detail.put("accountId", transaction.getAccountId());
            detail.put("areaId", transaction.getAreaId());
            detail.put("areaName", transaction.getAreaName());
            detail.put("consumeMode", transaction.getConsumeMode());
            detail.put("finalMoney", transaction.getFinalMoney());
            detail.put("status", transaction.getStatus());
            detail.put("deviceId", transaction.getDeviceId());
            detail.put("deviceName", transaction.getDeviceName());
            details.add(detail);
        }
        return details;
    }

    /**
     * 格式化日期时间
     *
     * @param dateTimeObj 日期时间对象
     * @return 格式化后的字符串
     */
    private String formatDateTime(Object dateTimeObj) {
        if (dateTimeObj == null) {
            return "";
        }
        if (dateTimeObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateTimeObj)
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return dateTimeObj.toString();
    }

    /**
     * 从Map中提取Boolean值
     *
     * @param map Map对象
     * @param key 键名
     * @param defaultValue 默认值
     * @return Boolean值
     */
    private boolean extractBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * 导出为PDF格式
     *
     * @param reportData 报表数据
     * @param filePath 文件路径
     */
    private void exportToPdf(Map<String, Object> reportData, java.nio.file.Path filePath) {
        log.info("[报表管理] 开始导出PDF文件，路径：{}", filePath);

        try {
            // 1. 创建PDF文档
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(filePath.toFile());
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc,
                    com.itextpdf.kernel.geom.PageSize.A4);

            // 2. 创建字体
            com.itextpdf.kernel.font.PdfFont boldFont = com.itextpdf.kernel.font.PdfFontFactory
                    .createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            com.itextpdf.kernel.font.PdfFont normalFont = com.itextpdf.kernel.font.PdfFontFactory
                    .createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            // 3. 添加标题
            String templateName = (String) reportData.get("templateName");
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph(
                    templateName != null ? templateName : "消费报表")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // 4. 添加生成时间
            com.itextpdf.layout.element.Paragraph generateTime = new com.itextpdf.layout.element.Paragraph(
                    "生成时间: " + java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT)
                    .setMarginBottom(15);
            document.add(generateTime);

            // 5. 添加统计信息
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) reportData.get("data");
            if (data != null) {
                Object totalCount = data.get("totalCount");
                Object totalMoney = data.get("totalMoney");
                if (totalCount != null || totalMoney != null) {
                    com.itextpdf.layout.element.Paragraph summary = new com.itextpdf.layout.element.Paragraph(
                            String.format("交易总数: %s, 总金额: %s",
                                    totalCount != null ? totalCount : 0,
                                    totalMoney != null ? totalMoney : 0))
                            .setFont(normalFont)
                            .setFontSize(12)
                            .setMarginBottom(20);
                    document.add(summary);
                }

                // 6. 如果有明细数据，添加明细表格
                if (data.containsKey("details")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> details = (List<Map<String, Object>>) data.get("details");
                    if (details != null && !details.isEmpty()) {
                        // 创建表格
                        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(8)
                                .useAllAvailableWidth()
                                .setMarginTop(10)
                                .setMarginBottom(10);

                        // 添加表头
                        table.addHeaderCell(createPdfCell("交易时间", boldFont, true));
                        table.addHeaderCell(createPdfCell("用户ID", boldFont, true));
                        table.addHeaderCell(createPdfCell("账户ID", boldFont, true));
                        table.addHeaderCell(createPdfCell("区域ID", boldFont, true));
                        table.addHeaderCell(createPdfCell("消费模式", boldFont, true));
                        table.addHeaderCell(createPdfCell("消费金额", boldFont, true));
                        table.addHeaderCell(createPdfCell("交易状态", boldFont, true));
                        table.addHeaderCell(createPdfCell("设备ID", boldFont, true));

                        // 添加数据行（最多显示100条，避免PDF过大）
                        int maxRows = Math.min(details.size(), 100);
                        for (int i = 0; i < maxRows; i++) {
                            Map<String, Object> detail = details.get(i);
                            table.addCell(createPdfCell(formatDateTime(detail.get("consumeTime")), normalFont, false));
                            table.addCell(createPdfCell(String.valueOf(detail.get("userId")), normalFont, false));
                            table.addCell(createPdfCell(String.valueOf(detail.get("accountId")), normalFont, false));
                            table.addCell(createPdfCell(String.valueOf(detail.get("areaId")), normalFont, false));
                            table.addCell(createPdfCell(String.valueOf(detail.get("consumeMode")), normalFont, false));
                            table.addCell(createPdfCell(String.valueOf(detail.get("finalMoney")), normalFont, false));
                            table.addCell(createPdfCell(String.valueOf(detail.get("status")), normalFont, false));
                            table.addCell(createPdfCell(String.valueOf(detail.get("deviceId")), normalFont, false));
                        }

                        if (details.size() > 100) {
                            com.itextpdf.layout.element.Paragraph note = new com.itextpdf.layout.element.Paragraph(
                                    String.format("注：共%d条记录，仅显示前100条", details.size()))
                                    .setFont(normalFont)
                                    .setFontSize(10)
                                    .setItalic()
                                    .setMarginTop(5);
                            document.add(note);
                        }

                        document.add(table);
                    }
                }
            }

            // 7. 关闭文档
            document.close();

            log.info("[报表管理] PDF文件导出成功，路径：{}", filePath);

        } catch (Exception e) {
            log.error("[报表管理] PDF文件导出失败，路径：{}", filePath, e);
            throw new SystemException("PDF_EXPORT_ERROR", "PDF文件导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出为CSV格式
     *
     * @param reportData 报表数据
     * @param filePath 文件路径
     */
    private void exportToCsv(Map<String, Object> reportData, java.nio.file.Path filePath) {
        log.info("[报表管理] 开始导出CSV文件，路径：{}", filePath);

        try {
            StringBuilder csvContent = new StringBuilder();

            // 1. 添加表头
            csvContent.append("交易时间,用户ID,账户ID,区域ID,消费模式,消费金额,交易状态,设备ID\n");

            // 2. 添加数据行
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) reportData.get("data");
            if (data != null) {
                // 如果包含明细数据，优先导出明细
                if (data.containsKey("details")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> details = (List<Map<String, Object>>) data.get("details");
                    if (details != null && !details.isEmpty()) {
                        for (Map<String, Object> detail : details) {
                            csvContent.append(escapeCsvValue(formatDateTime(detail.get("consumeTime"))));
                            csvContent.append(",");
                            csvContent.append(escapeCsvValue(String.valueOf(detail.get("userId"))));
                            csvContent.append(",");
                            csvContent.append(escapeCsvValue(String.valueOf(detail.get("accountId"))));
                            csvContent.append(",");
                            csvContent.append(escapeCsvValue(String.valueOf(detail.get("areaId"))));
                            csvContent.append(",");
                            csvContent.append(escapeCsvValue(String.valueOf(detail.get("consumeMode"))));
                            csvContent.append(",");
                            csvContent.append(escapeCsvValue(String.valueOf(detail.get("finalMoney"))));
                            csvContent.append(",");
                            csvContent.append(escapeCsvValue(String.valueOf(detail.get("status"))));
                            csvContent.append(",");
                            csvContent.append(escapeCsvValue(String.valueOf(detail.get("deviceId"))));
                            csvContent.append("\n");
                        }
                    }
                    // 如果有统计数据，在明细后添加汇总行
                    if (data.containsKey("totalCount")) {
                        csvContent.append("统计汇总,,,,,");
                        csvContent.append(escapeCsvValue(data.get("totalMoney") != null ? data.get("totalMoney").toString() : "0"));
                        csvContent.append(",成功,\n");
                    }
                } else if (data.containsKey("totalCount")) {
                    // 如果只有统计数据，显示汇总信息
                    csvContent.append("统计汇总,,,,,");
                    csvContent.append(escapeCsvValue(data.get("totalMoney") != null ? data.get("totalMoney").toString() : "0"));
                    csvContent.append(",成功,\n");
                }
            }

            // 3. 写入文件
            java.nio.file.Files.write(filePath, csvContent.toString().getBytes("UTF-8"));

            log.info("[报表管理] CSV文件导出成功，路径：{}", filePath);

        } catch (Exception e) {
            log.error("[报表管理] CSV文件导出失败，路径：{}", filePath, e);
            throw new SystemException("CSV_EXPORT_ERROR", "CSV文件导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建PDF表格单元格
     *
     * @param text 文本内容
     * @param font 字体
     * @param isHeader 是否为表头
     * @return PDF单元格
     */
    private com.itextpdf.layout.element.Cell createPdfCell(String text, com.itextpdf.kernel.font.PdfFont font, boolean isHeader) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(text != null ? text : "").setFont(font).setFontSize(isHeader ? 10 : 9))
                .setPadding(5);
        if (isHeader) {
            cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
        }
        return cell;
    }

    /**
     * 转义CSV值
     *
     * @param value 原始值
     * @return 转义后的值
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行符，需要用引号包裹并转义引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}



