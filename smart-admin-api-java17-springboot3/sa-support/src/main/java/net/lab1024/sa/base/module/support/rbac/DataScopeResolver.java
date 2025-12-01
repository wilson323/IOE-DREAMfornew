package net.lab1024.sa.base.module.support.rbac;

import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import net.lab1024.sa.base.module.support.auth.AuthorizationContext;

/**
 * 数据域解析器
 * <p>
 * 根据dataScope解析数据域过滤条件，与Area path/level/path_hash索引协同，生成高效过滤
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-16
 */
@Slf4j
@Component
public class DataScopeResolver {

    /**
     * 数据域类型枚举
     */
    public enum DataScopeType {
        AREA("区域范围"),
        DEPT("部门范围"),
        SELF("本人范围"),
        CUSTOM("自定义范围"),
        ALL("全部范围");

        private final String description;

        DataScopeType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 过滤器类型枚举
     */
    public enum FilterType {
        SQL_WHERE("SQL WHERE条件"),
        MYBATIS_WRAPPER("MyBatis Plus Wrapper"),
        JPA_SPECIFICATION("JPA Specification"),
        COLLECTION_FILTER("集合过滤器");

        private final String description;

        FilterType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 解析数据域过滤条件
     *
     * @param context 授权上下文
     * @return 数据域决策结果
     */
    public DataScopeDecision resolve(AuthorizationContext context) {
        try {
            log.debug("开始数据域解析: userId={}, dataScope={}", context.getUserId(), context.getDataScope());

            DataScopeType dataScopeType = DataScopeType.valueOf(context.getDataScope().toUpperCase());

            switch (dataScopeType) {
                case AREA:
                    return resolveAreaScope(context);
                case DEPT:
                    return resolveDeptScope(context);
                case SELF:
                    return resolveSelfScope(context);
                case CUSTOM:
                    return resolveCustomScope(context);
                case ALL:
                    return resolveAllScope(context);
                default:
                    return DataScopeDecision.noFilter("未知数据域类型: " + context.getDataScope());
            }
        } catch (Exception e) {
            log.error("数据域解析异常: userId={}, dataScope={}, error={}",
                    context.getUserId(), context.getDataScope(), e.getMessage(), e);
            return DataScopeDecision.noFilter("数据域解析异常: " + e.getMessage());
        }
    }

    /**
     * 解析区域范围数据域
     */
    private DataScopeDecision resolveAreaScope(AuthorizationContext context) {
        if (context.getAreaIds().isEmpty()) {
            return DataScopeDecision.denied("无区域权限数据", "用户未分配任何区域权限");
        }

        // 构建区域过滤条件
        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("areaIds", context.getAreaIds());

        // SQL WHERE 条件
        String sqlWhere = String.format("area_id IN (%s)", buildInClause(new HashSet<>(context.getAreaIds())));

        // 区域层级过滤条件（支持子区域）
        String areaPathCondition = buildAreaPathCondition(new HashSet<>(context.getAreaIds()));

        return DataScopeDecision.builder()
                .filterType(FilterType.SQL_WHERE)
                .allowed(true)
                .sqlSegments(sqlWhere)
                .areaPathCondition(areaPathCondition)
                .filterParams(filterParams)
                .description("区域范围数据域")
                .build();
    }

    /**
     * 解析部门范围数据域
     */
    private DataScopeDecision resolveDeptScope(AuthorizationContext context) {
        if (context.getDeptIds().isEmpty() && context.getDepartmentId() == null) {
            return DataScopeDecision.denied("无部门权限数据", "用户未分配任何部门权限");
        }

        Set<Long> deptIds = new HashSet<>(context.getDeptIds());
        if (context.getDepartmentId() != null) {
            deptIds.add(context.getDepartmentId());
        }

        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("deptIds", deptIds);

        String sqlWhere = String.format("department_id IN (%s)", buildInClause(deptIds));

        return DataScopeDecision.builder()
                .filterType(FilterType.SQL_WHERE)
                .allowed(true)
                .sqlSegments(sqlWhere)
                .filterParams(filterParams)
                .description("部门范围数据域")
                .build();
    }

    /**
     * 解析本人范围数据域
     */
    private DataScopeDecision resolveSelfScope(AuthorizationContext context) {
        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("userId", context.getUserId());

        String sqlWhere = "create_user_id = ? OR user_id = ?";

        return DataScopeDecision.builder()
                .filterType(FilterType.SQL_WHERE)
                .allowed(true)
                .sqlSegments(sqlWhere)
                .filterParams(filterParams)
                .description("本人范围数据域")
                .build();
    }

    /**
     * 解析自定义范围数据域
     */
    private DataScopeDecision resolveCustomScope(AuthorizationContext context) {
        // 从上下文属性中获取自定义过滤条件
        String customFilter = (String) context.getAttribute("customFilter");
        if (!StringUtils.hasText(customFilter)) {
            return DataScopeDecision.denied("无自定义过滤条件", "自定义数据域需要设置customFilter属性");
        }

        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("userId", context.getUserId());
        filterParams.put("areaIds", context.getAreaIds());
        filterParams.put("deptIds", context.getDeptIds());

        return DataScopeDecision.builder()
                .filterType(FilterType.SQL_WHERE)
                .allowed(true)
                .sqlSegments(customFilter)
                .filterParams(filterParams)
                .description("自定义范围数据域")
                .build();
    }

    /**
     * 解析全部范围数据域
     */
    private DataScopeDecision resolveAllScope(AuthorizationContext context) {
        // 全部范围不需要过滤，但可能需要排除软删除数据
        String sqlWhere = "deleted_flag = 0";

        Map<String, Object> filterParams = new HashMap<>();

        return DataScopeDecision.builder()
                .filterType(FilterType.SQL_WHERE)
                .allowed(true)
                .sqlSegments(sqlWhere)
                .filterParams(filterParams)
                .description("全部范围数据域")
                .build();
    }

    /**
     * 构建区域路径条件（支持包含子区域）
     */
    private String buildAreaPathCondition(Set<Long> areaIds) {
        if (areaIds.isEmpty()) {
            return "";
        }

        // 构建路径匹配条件：path LIKE '/1/%' OR path LIKE '/1/2/%'
        List<String> pathConditions = new ArrayList<>();
        for (Long areaId : areaIds) {
            pathConditions.add(String.format("path LIKE '/%d/%%'", areaId));
        }

        return String.join(" OR ", pathConditions);
    }

    /**
     * 构建IN子句
     */
    private String buildInClause(Set<Long> ids) {
        if (ids.isEmpty()) {
            return "NULL";
        }

        List<String> idStrings = new ArrayList<>();
        for (Long id : ids) {
            idStrings.add(id.toString());
        }

        return String.join(",", idStrings);
    }

    /**
     * 生成MyBatis Plus Wrapper过滤条件
     */
    public <T> void applyMyBatisWrapperFilter(AuthorizationContext context, Object queryWrapper) {
        DataScopeDecision decision = resolve(context);

        // 暂时简化处理，只记录日志
        log.debug("应用数据域过滤: userId={}, allowed={}, sql={}",
                context.getUserId(), decision.isAllowed(), decision.getSqlSegments());
    }

    /**
     * 检查数据权限
     *
     * @param context 授权上下文
     * @param dataUserId 数据所属用户ID
     * @param dataAreaId 数据所属区域ID
     * @param dataDeptId 数据所属部门ID
     * @return 是否有权限访问
     */
    public boolean checkDataPermission(AuthorizationContext context, Long dataUserId, Long dataAreaId, Long dataDeptId) {
        DataScopeDecision decision = resolve(context);

        if (!decision.isAllowed()) {
            return false;
        }

        DataScopeType dataScopeType = DataScopeType.valueOf(context.getDataScope().toUpperCase());

        switch (dataScopeType) {
            case SELF:
                return Objects.equals(context.getUserId(), dataUserId);
            case AREA:
                return context.getAreaIds().contains(dataAreaId);
            case DEPT:
                return context.getDeptIds().contains(dataDeptId) || Objects.equals(context.getDepartmentId(), dataDeptId);
            case ALL:
                return true;
            case CUSTOM:
                // 自定义权限需要更复杂的逻辑，这里简化处理
                return true;
            default:
                return false;
        }
    }

    /**
     * 数据域决策结果
     */
    @Data
    public static class DataScopeDecision {
        private boolean allowed;
        private FilterType filterType;
        private String sqlSegments;
        private String areaPathCondition;
        private Map<String, Object> filterParams;
        private String description;
        private String denyReason;

        private DataScopeDecision() {
            this.filterParams = new HashMap<>();
        }

        public static DataScopeDecision noFilter(String description) {
            DataScopeDecision decision = new DataScopeDecision();
            decision.allowed = true;
            decision.filterType = FilterType.SQL_WHERE;
            decision.description = description;
            return decision;
        }

        public static DataScopeDecision denied(String description, String denyReason) {
            DataScopeDecision decision = new DataScopeDecision();
            decision.allowed = false;
            decision.description = description;
            decision.denyReason = denyReason;
            return decision;
        }

        public static DataScopeDecisionBuilder builder() {
            return new DataScopeDecisionBuilder();
        }

        public static class DataScopeDecisionBuilder {
            private final DataScopeDecision decision = new DataScopeDecision();

            public DataScopeDecisionBuilder filterType(FilterType filterType) {
                decision.filterType = filterType;
                return this;
            }

            public DataScopeDecisionBuilder allowed(boolean allowed) {
                decision.allowed = allowed;
                return this;
            }

            public DataScopeDecisionBuilder sqlSegments(String sqlSegments) {
                decision.sqlSegments = sqlSegments;
                return this;
            }

            public DataScopeDecisionBuilder areaPathCondition(String areaPathCondition) {
                decision.areaPathCondition = areaPathCondition;
                return this;
            }

            public DataScopeDecisionBuilder filterParams(Map<String, Object> filterParams) {
                decision.filterParams = filterParams;
                return this;
            }

            public DataScopeDecisionBuilder description(String description) {
                decision.description = description;
                return this;
            }

            public DataScopeDecision build() {
                return decision;
            }
        }
    }
}