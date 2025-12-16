package net.lab1024.sa.common.recommend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 智能推荐引擎
 *
 * @Author IOE-DREAM Team
 * @Date 2025-12-05
 * @Copyright IOE-DREAM智慧园区一卡通管理平台
 *
 *            推荐算法：
 *            1. 协同过滤（User-Based CF）
 *            2. 内容推荐（Content-Based）
 *            3. 混合推荐（Hybrid）
 *            4. 热度推荐（Trending）
 *
 *            应用场景：
 *            - 食堂菜品推荐
 *            - 会议室推荐
 *            - 停车位推荐
 *            - 访客路线推荐
 *
 *            注意：此类在microservices-common中不使用Spring注解，保持为纯Java类
 *            在微服务中通过配置类注册为Spring Bean使用
 */
@Slf4j
public class RecommendationEngine {

    /**
     * 基于用户的协同过滤推荐
     *
     * @param userId        目标用户ID
     * @param userBehaviors 所有用户行为数据 Map<UserId, Set<ItemId>>
     * @param topN          推荐数量
     * @return 推荐物品ID列表
     */
    public List<Long> userBasedCollaborativeFiltering(
            Long userId,
            Map<Long, Set<Long>> userBehaviors,
            int topN) {

        log.info("[协同过滤] 开始推荐, userId={}, topN={}", userId, topN);

        // 1. 获取目标用户的行为集合
        Set<Long> targetUserItems = userBehaviors.getOrDefault(userId, new HashSet<>());
        if (targetUserItems.isEmpty()) {
            log.warn("[协同过滤] 用户无历史行为, userId={}", userId);
            return Collections.emptyList();
        }

        // 2. 计算与其他用户的相似度（余弦相似度）
        Map<Long, Double> userSimilarities = new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry : userBehaviors.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) {
                continue;
            }

            Set<Long> otherUserItems = entry.getValue();
            double similarity = calculateCosineSimilarity(targetUserItems, otherUserItems);

            if (similarity > 0) {
                userSimilarities.put(otherUserId, similarity);
            }
        }

        // 3. 找出最相似的K个用户（K=10）
        List<Map.Entry<Long, Double>> topSimilarUsers = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        log.info("[协同过滤] 找到 {} 个相似用户", topSimilarUsers.size());

        // 4. 收集推荐物品（相似用户喜欢但目标用户未接触的物品）
        Map<Long, Double> recommendScores = new HashMap<>();

        for (Map.Entry<Long, Double> similarUser : topSimilarUsers) {
            Long similarUserId = similarUser.getKey();
            Double similarity = similarUser.getValue();

            Set<Long> similarUserItems = userBehaviors.get(similarUserId);

            for (Long itemId : similarUserItems) {
                if (!targetUserItems.contains(itemId)) {
                    recommendScores.merge(itemId, similarity, (v1, v2) -> v1 + v2);
                }
            }
        }

        // 5. 按得分排序返回Top N
        List<Long> recommendations = recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("[协同过滤] 推荐结果: {}", recommendations);
        return recommendations;
    }

    /**
     * 基于内容的推荐
     *
     * @param itemId       目标物品ID
     * @param itemFeatures 所有物品特征 Map<ItemId, Map<Feature, Value>>
     * @param topN         推荐数量
     * @return 相似物品ID列表
     */
    public List<Long> contentBasedRecommendation(
            Long itemId,
            Map<Long, Map<String, Double>> itemFeatures,
            int topN) {

        log.info("[内容推荐] 开始推荐, itemId={}, topN={}", itemId, topN);

        Map<String, Double> targetFeatures = itemFeatures.get(itemId);
        if (targetFeatures == null || targetFeatures.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算与其他物品的相似度
        Map<Long, Double> itemSimilarities = new HashMap<>();

        for (Map.Entry<Long, Map<String, Double>> entry : itemFeatures.entrySet()) {
            Long otherId = entry.getKey();
            if (otherId.equals(itemId)) {
                continue;
            }

            Map<String, Double> otherFeatures = entry.getValue();
            double similarity = calculateFeatureSimilarity(targetFeatures, otherFeatures);

            if (similarity > 0) {
                itemSimilarities.put(otherId, similarity);
            }
        }

        // 按相似度排序返回
        return itemSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 热度推荐（基于流行度）
     *
     * @param itemScores   物品热度分数 Map<ItemId, Score>
     * @param excludeItems 排除的物品ID集合
     * @param topN         推荐数量
     * @return 热门物品ID列表
     */
    public List<Long> trendingRecommendation(
            Map<Long, Double> itemScores,
            Set<Long> excludeItems,
            int topN) {

        log.info("[热度推荐] 开始推荐, topN={}", topN);

        return itemScores.entrySet().stream()
                .filter(entry -> !excludeItems.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 混合推荐（协同过滤 + 内容推荐 + 热度推荐）
     *
     * @param userId        用户ID
     * @param userBehaviors 用户行为数据
     * @param itemFeatures  物品特征数据
     * @param itemScores    物品热度分数
     * @param topN          推荐数量
     * @return 推荐结果列表
     */
    @SuppressWarnings("null")
    public List<RecommendationResult> hybridRecommendation(
            Long userId,
            Map<Long, Set<Long>> userBehaviors,
            Map<Long, Map<String, Double>> itemFeatures,
            Map<Long, Double> itemScores,
            int topN) {

        log.info("[混合推荐] 开始推荐, userId={}, topN={}", userId, topN);

        Set<Long> userItems = userBehaviors.getOrDefault(userId, new HashSet<>());

        // 1. 协同过滤推荐（权重40%）
        List<Long> cfRecommendations = userBasedCollaborativeFiltering(userId, userBehaviors, topN * 2);

        // 2. 热度推荐（权重30%）
        List<Long> trendingRecommendations = trendingRecommendation(itemScores, userItems, topN * 2);

        // 3. 混合评分
        Map<Long, Double> hybridScores = new HashMap<>();

        // 协同过滤得分
        for (int i = 0; i < cfRecommendations.size(); i++) {
            Long itemId = cfRecommendations.get(i);
            double score = (cfRecommendations.size() - i) * 0.4;
            Double existingScore = hybridScores.get(itemId);
            hybridScores.put(itemId, existingScore != null ? existingScore + score : score);
        }

        // 热度得分
        for (int i = 0; i < trendingRecommendations.size(); i++) {
            Long itemId = trendingRecommendations.get(i);
            double score = (trendingRecommendations.size() - i) * 0.3;
            Double existingScore = hybridScores.get(itemId);
            hybridScores.put(itemId, existingScore != null ? existingScore + score : score);
        }

        // 新鲜度加分（最近更新的物品，权重30%）
        hybridScores.forEach((itemId, score) -> {
            Double freshness = itemScores.getOrDefault(itemId, 0.0) * 0.3;
            hybridScores.put(itemId, score + freshness);
        });

        // 4. 排序并返回Top N
        List<RecommendationResult> results = hybridScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> new RecommendationResult(
                        entry.getKey(),
                        entry.getValue(),
                        calculateConfidence(entry.getValue())))
                .collect(Collectors.toList());

        log.info("[混合推荐] 推荐完成, 结果数量={}", results.size());
        return results;
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(Set<Long> set1, Set<Long> set2) {
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        // 计算交集
        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // 余弦相似度 = |A∩B| / sqrt(|A| * |B|)
        double numerator = intersection.size();
        double denominator = Math.sqrt(set1.size() * set2.size());

        return numerator / denominator;
    }

    /**
     * 计算特征相似度（欧几里得距离）
     */
    private double calculateFeatureSimilarity(
            Map<String, Double> features1,
            Map<String, Double> features2) {

        // 获取所有特征维度
        Set<String> allFeatures = new HashSet<>();
        allFeatures.addAll(features1.keySet());
        allFeatures.addAll(features2.keySet());

        // 计算欧几里得距离
        double sumSquares = 0.0;
        for (String feature : allFeatures) {
            double value1 = features1.getOrDefault(feature, 0.0);
            double value2 = features2.getOrDefault(feature, 0.0);
            sumSquares += Math.pow(value1 - value2, 2);
        }

        double distance = Math.sqrt(sumSquares);

        // 转换为相似度（0-1之间）
        return 1.0 / (1.0 + distance);
    }

    /**
     * 计算推荐置信度
     */
    private double calculateConfidence(double score) {
        // 将得分归一化到0-1之间
        double confidence = score / (score + 10); // 使用S型函数
        return BigDecimal.valueOf(confidence)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 推荐结果对象
     */
    public static class RecommendationResult {
        private Long itemId;
        private Double score;
        private Double confidence;

        public RecommendationResult(Long itemId, Double score, Double confidence) {
            this.itemId = itemId;
            this.score = score;
            this.confidence = confidence;
        }

        public Long getItemId() {
            return itemId;
        }

        public Double getScore() {
            return score;
        }

        public Double getConfidence() {
            return confidence;
        }
    }
}
