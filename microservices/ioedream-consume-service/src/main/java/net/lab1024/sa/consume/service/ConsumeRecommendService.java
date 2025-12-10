package net.lab1024.sa.consume.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.recommend.RecommendationEngine;
import net.lab1024.sa.common.util.RedisUtil;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.entity.ConsumeAreaEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeProductEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;

/**
 * 消费智能推荐服务
 *
 * @Author IOE-DREAM Team
 * @Date 2025-12-05
 * @Copyright IOE-DREAM智慧园区一卡通管理平台
 *
 * 推荐功能：
 * - 菜品推荐（基于历史消费）
 * - 餐厅推荐（基于位置和偏好）
 * - 消费金额预测
 * - 优惠活动推荐
 */
@Slf4j
@Service
public class ConsumeRecommendService {

    @Resource
    private RecommendationEngine recommendationEngine;

    @Resource
    private ConsumeTransactionDao consumeTransactionDao;

    @Resource
    private ConsumeProductDao consumeProductDao;

    @Resource
    private ConsumeAreaManager consumeAreaManager;

    @Resource
    private CacheService cacheService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 推荐菜品
     *
     * @param userId 用户ID
     * @param topN 推荐数量
     * @return 推荐菜品ID列表（带置信度）
     */
    public List<DishRecommendation> recommendDishes(Long userId, int topN) {
        log.info("[菜品推荐] userId={}, topN={}", userId, topN);

        try {
            // 1. 获取用户历史消费数据
            Map<Long, Set<Long>> userDishBehaviors = loadUserDishBehaviors();

            // 2. 获取菜品特征数据（类别、价格、热量等）
            Map<Long, Map<String, Double>> dishFeatures = loadDishFeatures();

            // 3. 获取菜品热度数据
            Map<Long, Double> dishScores = loadDishPopularity();

            // 4. 混合推荐
            List<RecommendationEngine.RecommendationResult> results =
                recommendationEngine.hybridRecommendation(
                    userId,
                    userDishBehaviors,
                    dishFeatures,
                    dishScores,
                    topN
                );

            // 5. 转换为业务对象
            return results.stream()
                    .map(r -> new DishRecommendation(
                            r.getItemId(),
                            r.getConfidence(),
                            "基于您的消费偏好推荐"
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[菜品推荐] 推荐失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 推荐餐厅
     *
     * @param userId 用户ID
     * @param userLocation 用户位置（可选）
     * @param topN 推荐数量
     * @return 推荐餐厅列表
     */
    public List<RestaurantRecommendation> recommendRestaurants(
            Long userId,
            Location userLocation,
            int topN) {

        log.info("[餐厅推荐] userId={}, location={}, topN={}", userId, userLocation, topN);

        try {
            // 1. 获取用户餐厅偏好
            Map<Long, Set<Long>> userRestaurantBehaviors = loadUserRestaurantBehaviors();

            // 2. 餐厅特征（菜系、价位、环境）
            Map<Long, Map<String, Double>> restaurantFeatures = loadRestaurantFeatures();

            // 3. 餐厅评分
            Map<Long, Double> restaurantScores = loadRestaurantRatings();

            // 4. 混合推荐
            List<RecommendationEngine.RecommendationResult> results =
                recommendationEngine.hybridRecommendation(
                    userId,
                    userRestaurantBehaviors,
                    restaurantFeatures,
                    restaurantScores,
                    topN * 2  // 先获取更多候选
                );

            // 5. 如果有位置信息，按距离排序
            if (userLocation != null) {
                results = results.stream()
                        .sorted(Comparator.comparing(r ->
                            calculateDistance(userLocation, getRestaurantLocation(r.getItemId()))
                        ))
                        .limit(topN)
                        .collect(Collectors.toList());
            } else {
                results = results.stream().limit(topN).collect(Collectors.toList());
            }

            return results.stream()
                    .map(r -> new RestaurantRecommendation(
                            r.getItemId(),
                            r.getConfidence(),
                            "综合评分推荐"
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[餐厅推荐] 推荐失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 预测消费金额
     *
     * @param userId 用户ID
     * @param timeOfDay 时间段（早餐/午餐/晚餐）
     * @return 预测金额
     */
    public Double predictConsumeAmount(Long userId, String timeOfDay) {
        log.info("[金额预测] userId={}, timeOfDay={}", userId, timeOfDay);

        try {
            // 1. 获取用户历史消费金额数据
            List<Double> historicalAmounts = loadUserHistoricalAmounts(userId, timeOfDay);

            if (historicalAmounts.isEmpty()) {
                return 15.0; // 默认金额
            }

            // 2. 计算加权平均（近期权重更高）
            double weightedSum = 0.0;
            double weightSum = 0.0;

            for (int i = 0; i < historicalAmounts.size(); i++) {
                double weight = Math.pow(0.9, historicalAmounts.size() - 1 - i); // 指数衰减
                weightedSum += historicalAmounts.get(i) * weight;
                weightSum += weight;
            }

            double predictedAmount = weightedSum / weightSum;

            log.info("[金额预测] 预测金额={}", predictedAmount);
            return Math.round(predictedAmount * 100.0) / 100.0;

        } catch (Exception e) {
            log.error("[金额预测] 预测失败", e);
            return 15.0;
        }
    }

    // ==================== 辅助方法（真实数据加载） ====================

    /**
     * 从数据库加载用户菜品行为数据
     * 根据用户历史消费记录，统计用户购买过的菜品
     *
     * @return 用户ID -> 菜品ID集合的映射
     */
    private Map<Long, Set<Long>> loadUserDishBehaviors() {
        log.debug("[推荐服务] 从数据库加载用户菜品行为数据");

        try {
            Map<Long, Set<Long>> userDishBehaviors = new HashMap<>();

            // 1. 查询最近3个月的成功交易记录
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao
                    .selectByTimeRange(threeMonthsAgo, LocalDateTime.now());

            // 2. 过滤成功交易（状态为2表示成功）
            List<ConsumeTransactionEntity> successTransactions = transactions.stream()
                    .filter(t -> t.getTransactionStatus() != null && t.getTransactionStatus() == 2)
                    .collect(Collectors.toList());

            // 3. 解析商品明细，统计用户购买的商品
            for (ConsumeTransactionEntity transaction : successTransactions) {
                try {
                    Long userId = transaction.getUserId();
                    String productDetailsJson = transaction.getProductDetails();

                    if (productDetailsJson != null && !productDetailsJson.trim().isEmpty()) {
                        // 解析商品明细JSON
                        List<Map<String, Object>> productDetails = objectMapper.readValue(productDetailsJson,
                                new TypeReference<List<Map<String, Object>>>() {
                                });

                        // 获取用户购买的商品ID集合
                        Set<Long> dishIds = userDishBehaviors.computeIfAbsent(userId, k -> new HashSet<>());

                        for (Map<String, Object> detail : productDetails) {
                            Object productIdObj = detail.get("productId");
                            if (productIdObj != null) {
                                try {
                                    Long productId = Long.parseLong(productIdObj.toString());
                                    dishIds.add(productId);
                                } catch (NumberFormatException e) {
                                    log.warn("[推荐服务] 解析商品ID失败: {}", productIdObj, e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("[推荐服务] 解析交易记录商品明细失败: transactionNo={}", transaction.getTransactionNo(), e);
                }
            }

            log.info("[推荐服务] 用户菜品行为数据加载完成，用户数：{}", userDishBehaviors.size());
            return userDishBehaviors;

        } catch (Exception e) {
            log.error("[推荐服务] 加载用户菜品行为数据失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 从数据库加载菜品特征数据
     * 包括类别、价格、评分、销量等特征
     *
     * @return 菜品ID -> 特征映射的映射
     */
    private Map<Long, Map<String, Double>> loadDishFeatures() {
        log.debug("[推荐服务] 从数据库加载菜品特征数据");

        try {
            Map<Long, Map<String, Double>> dishFeatures = new HashMap<>();

            // 1. 查询所有上架的商品
            List<ConsumeProductEntity> products = consumeProductDao.selectOnShelfProducts();

            // 2. 提取菜品特征
            for (ConsumeProductEntity product : products) {
                try {
                    Long productId = Long.parseLong(product.getId().toString());
                    Map<String, Double> features = new HashMap<>();

                    // 价格特征（归一化到0-1范围，假设最高价格为100元）
                    if (product.getSalePrice() != null) {
                        double normalizedPrice = Math.min(product.getSalePrice().doubleValue() / 100.0, 1.0);
                        features.put("price", normalizedPrice);
                    }

                    // 评分特征（归一化到0-1范围，5分制）
                    if (product.getRating() != null) {
                        features.put("rating", product.getRating().doubleValue() / 5.0);
                    } else {
                        features.put("rating", 0.5); // 默认评分
                    }

                    // 销量特征（归一化，假设最高销量为10000）
                    if (product.getSaleQuantity() != null) {
                        double normalizedSales = Math.min(product.getSaleQuantity().doubleValue() / 10000.0, 1.0);
                        features.put("sales", normalizedSales);
                    } else {
                        features.put("sales", 0.0);
                    }

                    // 浏览特征（归一化，假设最高浏览量为50000）
                    if (product.getViewQuantity() != null) {
                        double normalizedViews = Math.min(product.getViewQuantity().doubleValue() / 50000.0, 1.0);
                        features.put("views", normalizedViews);
                    } else {
                        features.put("views", 0.0);
                    }

                    // 类别特征（使用类别ID的哈希值归一化）
                    if (product.getCategoryId() != null) {
                        int categoryHash = product.getCategoryId().hashCode();
                        features.put("category", Math.abs(categoryHash % 100) / 100.0);
                    }

                    // 是否推荐商品
                    features.put("recommended", product.getIsRecommended() != null && product.getIsRecommended() ? 1.0 : 0.0);

                    // 是否热销商品
                    features.put("hotSale", product.getIsHotSale() != null && product.getIsHotSale() ? 1.0 : 0.0);

                    // 是否新品
                    features.put("isNew", product.getIsNew() != null && product.getIsNew() ? 1.0 : 0.0);

                    dishFeatures.put(productId, features);

                } catch (Exception e) {
                    log.warn("[推荐服务] 提取菜品特征失败: productId={}", product.getId(), e);
                }
            }

            log.info("[推荐服务] 菜品特征数据加载完成，菜品数：{}", dishFeatures.size());
            return dishFeatures;

        } catch (Exception e) {
            log.error("[推荐服务] 加载菜品特征数据失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 从Redis加载菜品热度数据
     * 热度数据存储在Redis中，键格式：consume:product:popularity:{productId}
     *
     * @return 菜品ID -> 热度分数的映射
     */
    private Map<Long, Double> loadDishPopularity() {
        log.debug("[推荐服务] 从Redis加载菜品热度数据");

        try {
            Map<Long, Double> dishPopularity = new HashMap<>();

            // 1. 查询所有上架的商品
            List<ConsumeProductEntity> products = consumeProductDao.selectOnShelfProducts();

            // 2. 从Redis获取每个商品的热度分数
            for (ConsumeProductEntity product : products) {
                try {
                    Long productId = Long.parseLong(product.getId().toString());
                    String redisKey = "consume:product:popularity:" + productId;

                    // 从Redis获取热度分数
                    Object popularityObj = RedisUtil.get(redisKey);
                    if (popularityObj != null) {
                        double popularity = 0.0;
                        if (popularityObj instanceof Number) {
                            popularity = ((Number) popularityObj).doubleValue();
                        } else if (popularityObj instanceof String) {
                            popularity = Double.parseDouble((String) popularityObj);
                        }

                        // 归一化热度分数（假设最高热度为1000）
                        double normalizedPopularity = Math.min(popularity / 1000.0, 1.0);
                        dishPopularity.put(productId, normalizedPopularity);
                    } else {
                        // 如果Redis中没有热度数据，使用商品销量作为热度
                        if (product.getSaleQuantity() != null && product.getSaleQuantity() > 0) {
                            double normalizedSales = Math.min(product.getSaleQuantity().doubleValue() / 10000.0, 1.0);
                            dishPopularity.put(productId, normalizedSales);
                        } else {
                            dishPopularity.put(productId, 0.0);
                        }
                    }

                } catch (Exception e) {
                    log.warn("[推荐服务] 获取菜品热度失败: productId={}", product.getId(), e);
                }
            }

            log.info("[推荐服务] 菜品热度数据加载完成，菜品数：{}", dishPopularity.size());
            return dishPopularity;

        } catch (Exception e) {
            log.error("[推荐服务] 加载菜品热度数据失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 从数据库加载用户历史消费金额数据
     * 根据时间段（早餐/午餐/晚餐）查询用户的历史消费金额
     *
     * @param userId   用户ID
     * @param timeOfDay 时间段（BREAKFAST/LUNCH/DINNER）
     * @return 历史消费金额列表（按时间倒序）
     */
    private List<Double> loadUserHistoricalAmounts(Long userId, String timeOfDay) {
        log.debug("[推荐服务] 从数据库加载用户历史消费金额: userId={}, timeOfDay={}", userId, timeOfDay);

        try {
            List<Double> historicalAmounts = new ArrayList<>();

            // 1. 确定时间段范围（查询最近30天的数据）
            LocalDateTime startTime = LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = LocalDateTime.now();

            // 2. 查询用户的历史交易记录
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao
                    .selectByUserIdAndTimeRange(userId.toString(), startTime, endTime);

            // 3. 根据时间段过滤交易记录
            LocalTime breakfastStart = LocalTime.of(6, 0);
            LocalTime breakfastEnd = LocalTime.of(9, 0);
            LocalTime lunchStart = LocalTime.of(11, 0);
            LocalTime lunchEnd = LocalTime.of(14, 0);
            LocalTime dinnerStart = LocalTime.of(17, 0);
            LocalTime dinnerEnd = LocalTime.of(20, 0);

            for (ConsumeTransactionEntity transaction : transactions) {
                // 只统计成功交易
                if (transaction.getTransactionStatus() == null || transaction.getTransactionStatus() != 2) {
                    continue;
                }

                LocalTime transactionTime = transaction.getTransactionTime().toLocalTime();
                boolean matchTimeOfDay = false;

                switch (timeOfDay.toUpperCase()) {
                    case "BREAKFAST":
                        matchTimeOfDay = !transactionTime.isBefore(breakfastStart)
                                && transactionTime.isBefore(breakfastEnd);
                        break;
                    case "LUNCH":
                        matchTimeOfDay = !transactionTime.isBefore(lunchStart)
                                && transactionTime.isBefore(lunchEnd);
                        break;
                    case "DINNER":
                        matchTimeOfDay = !transactionTime.isBefore(dinnerStart)
                                && transactionTime.isBefore(dinnerEnd);
                        break;
                    default:
                        matchTimeOfDay = true; // 如果不指定时间段，统计所有
                        break;
                }

                if (matchTimeOfDay && transaction.getAmount() != null) {
                    historicalAmounts.add(transaction.getAmount().doubleValue());
                }
            }

            // 4. 按时间倒序排序（最新的在前）
            historicalAmounts.sort(Collections.reverseOrder());

            // 5. 限制返回最近20条记录
            if (historicalAmounts.size() > 20) {
                historicalAmounts = historicalAmounts.subList(0, 20);
            }

            log.info("[推荐服务] 用户历史消费金额加载完成: userId={}, timeOfDay={}, 记录数={}",
                    userId, timeOfDay, historicalAmounts.size());
            return historicalAmounts;

        } catch (Exception e) {
            log.error("[推荐服务] 加载用户历史消费金额失败: userId={}, timeOfDay={}", userId, timeOfDay, e);
            return new ArrayList<>();
        }
    }

    /**
     * 查询餐厅位置
     * 从ConsumeAreaEntity中获取区域的GPS位置信息
     *
     * @param restaurantId 餐厅ID（实际是区域ID）
     * @return 位置信息（经纬度）
     */
    private Location getRestaurantLocation(Long restaurantId) {
        log.debug("[推荐服务] 查询餐厅位置: restaurantId={}", restaurantId);

        try {
            // 1. 通过ConsumeAreaManager获取区域信息
            ConsumeAreaEntity area = consumeAreaManager.getAreaById(restaurantId.toString());

            if (area == null) {
                log.warn("[推荐服务] 区域不存在: restaurantId={}", restaurantId);
                return new Location(0.0, 0.0);
            }

            // 2. 解析GPS位置信息
            String gpsLocation = area.getGpsLocation();
            if (gpsLocation != null && !gpsLocation.trim().isEmpty()) {
                // GPS位置格式可能是 "latitude,longitude" 或 JSON格式
                try {
                    // 尝试解析为逗号分隔的经纬度
                    String[] parts = gpsLocation.split(",");
                    if (parts.length == 2) {
                        double latitude = Double.parseDouble(parts[0].trim());
                        double longitude = Double.parseDouble(parts[1].trim());
                        return new Location(latitude, longitude);
                    }

                    // 尝试解析为JSON格式
                    Map<String, Object> locationMap = objectMapper.readValue(gpsLocation,
                            new TypeReference<Map<String, Object>>() {
                            });
                    Object latObj = locationMap.get("latitude");
                    Object lngObj = locationMap.get("longitude");
                    if (latObj != null && lngObj != null) {
                        double latitude = Double.parseDouble(latObj.toString());
                        double longitude = Double.parseDouble(lngObj.toString());
                        return new Location(latitude, longitude);
                    }
                } catch (Exception e) {
                    log.warn("[推荐服务] 解析GPS位置失败: gpsLocation={}", gpsLocation, e);
                }
            }

            // 3. 如果没有GPS位置，返回默认位置（0,0）
            log.warn("[推荐服务] 区域没有GPS位置信息: restaurantId={}", restaurantId);
            return new Location(0.0, 0.0);

        } catch (Exception e) {
            log.error("[推荐服务] 查询餐厅位置失败: restaurantId={}", restaurantId, e);
            return new Location(0.0, 0.0);
        }
    }

    /**
     * 加载用户餐厅行为数据（预留方法）
     */
    private Map<Long, Set<Long>> loadUserRestaurantBehaviors() {
        // 预留：未来可以基于用户在不同区域的消费记录统计餐厅偏好
        return new HashMap<>();
    }

    /**
     * 加载餐厅特征数据（预留方法）
     */
    private Map<Long, Map<String, Double>> loadRestaurantFeatures() {
        // 预留：未来可以从区域配置中提取餐厅特征
        return new HashMap<>();
    }

    /**
     * 加载餐厅评分数据（预留方法）
     */
    private Map<Long, Double> loadRestaurantRatings() {
        // 预留：未来可以从区域评分表中加载餐厅评分
        return new HashMap<>();
    }

    /**
     * 计算两点之间的距离（使用Haversine公式）
     * 返回距离（单位：公里）
     *
     * @param loc1 位置1
     * @param loc2 位置2
     * @return 距离（公里）
     */
    private double calculateDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return Double.MAX_VALUE; // 如果位置信息缺失，返回最大距离
        }

        // 使用Haversine公式计算地球表面两点间的大圆距离
        final double EARTH_RADIUS_KM = 6371.0; // 地球半径（公里）

        double lat1Rad = Math.toRadians(loc1.getLatitude());
        double lat2Rad = Math.toRadians(loc2.getLatitude());
        double deltaLatRad = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double deltaLonRad = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                        * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    // ==================== 内部类 ====================

    public static class DishRecommendation {
        private Long dishId;
        private Double confidence;
        private String reason;

        public DishRecommendation(Long dishId, Double confidence, String reason) {
            this.dishId = dishId;
            this.confidence = confidence;
            this.reason = reason;
        }

        public Long getDishId() { return dishId; }
        public Double getConfidence() { return confidence; }
        public String getReason() { return reason; }
    }

    public static class RestaurantRecommendation {
        private Long restaurantId;
        private Double confidence;
        private String reason;

        public RestaurantRecommendation(Long restaurantId, Double confidence, String reason) {
            this.restaurantId = restaurantId;
            this.confidence = confidence;
            this.reason = reason;
        }

        public Long getRestaurantId() { return restaurantId; }
        public Double getConfidence() { return confidence; }
        public String getReason() { return reason; }
    }

    public static class Location {
        private Double latitude;
        private Double longitude;

        public Location(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }
}

