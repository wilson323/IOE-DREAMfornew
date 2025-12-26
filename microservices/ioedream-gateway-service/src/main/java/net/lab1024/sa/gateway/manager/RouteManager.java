package net.lab1024.sa.gateway.manager;

import lombok.extern.slf4j.Slf4j;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 路由管理器
 * <p>
 * 管理网关动态路由配置
 * 严格遵循CLAUDE.md规范：
 * - Manager层负责复杂业务逻辑
 * - 保持纯Java特性，不使用Spring注解
 * - 通过Configuration类注册为Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class RouteManager {


    private final Map<String, RouteDefinition> routeCache = new ConcurrentHashMap<>();

    /**
     * 获取所有路由定义
     *
     * @return 路由定义列表
     */
    public List<RouteDefinition> getAllRoutes() {
        log.debug("[路由管理] 获取所有路由定义，当前缓存数量={}", routeCache.size());
        return new ArrayList<>(routeCache.values());
    }

    /**
     * 根据ID获取路由定义
     *
     * @param routeId 路由ID
     * @return 路由定义
     */
    public RouteDefinition getRouteById(String routeId) {
        log.debug("[路由管理] 获取路由定义，routeId={}", routeId);
        return routeCache.get(routeId);
    }

    /**
     * 添加或更新路由
     *
     * @param route 路由定义
     */
    public void saveRoute(RouteDefinition route) {
        log.info("[路由管理] 保存路由定义，routeId={}, uri={}", route.getId(), route.getUri());
        routeCache.put(route.getId(), route);
    }

    /**
     * 删除路由
     *
     * @param routeId 路由ID
     * @return 是否删除成功
     */
    public boolean deleteRoute(String routeId) {
        log.info("[路由管理] 删除路由定义，routeId={}", routeId);
        return routeCache.remove(routeId) != null;
    }

    /**
     * 刷新路由缓存
     */
    public void refreshRoutes() {
        log.info("[路由管理] 刷新路由缓存");
        // 实际实现中从配置中心或数据库重新加载路由
    }

    /**
     * 检查路由是否存在
     *
     * @param routeId 路由ID
     * @return 是否存在
     */
    public boolean routeExists(String routeId) {
        return routeCache.containsKey(routeId);
    }

    /**
     * 获取路由统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getRouteStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRoutes", routeCache.size());
        stats.put("activeRoutes", routeCache.values().stream()
                .filter(RouteDefinition::isEnabled)
                .count());
        return stats;
    }

    /**
     * 路由定义
     */
    @lombok.Data
    public static class RouteDefinition {
        private String id;
        private String uri;
        private String path;
        private int order;
        private boolean enabled = true;
        private Map<String, String> predicates = new HashMap<>();
        private Map<String, String> filters = new HashMap<>();
    }
}
