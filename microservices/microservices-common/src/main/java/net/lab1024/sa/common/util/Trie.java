package net.lab1024.sa.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Trie树（字典树）数据结构
 * <p>
 * 提供字符串的快速插入、查找和删除操作
 * 适用于前缀匹配、单词搜索等场景
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
public class Trie {

    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    /**
     * Trie节点
     */
    @Data
    public static class TrieNode {
        /**
         * 子节点映射
         */
        private Map<Character, TrieNode> children;

        /**
         * 是否为单词结尾
         */
        private boolean isEnd;

        /**
         * 单词频率（用于统计）
         */
        private int frequency;

        public TrieNode() {
            this.children = new HashMap<>();
            this.isEnd = false;
            this.frequency = 0;
        }
    }

    /**
     * 插入单词
     *
     * @param word 单词
     */
    public void insert(String word) {
        if (SmartStringUtil.isEmpty(word)) {
            return;
        }

        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.getChildren().putIfAbsent(c, new TrieNode());
            node = node.getChildren().get(c);
        }
        node.setEnd(true);
        node.setFrequency(node.getFrequency() + 1);
    }

    /**
     * 搜索单词是否存在
     *
     * @param word 单词
     * @return 是否存在
     */
    public boolean search(String word) {
        if (SmartStringUtil.isEmpty(word)) {
            return false;
        }

        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.getChildren().containsKey(c)) {
                return false;
            }
            node = node.getChildren().get(c);
        }
        return node.isEnd();
    }

    /**
     * 检查是否存在以指定前缀开头的单词
     *
     * @param prefix 前缀
     * @return 是否存在
     */
    public boolean startsWith(String prefix) {
        if (SmartStringUtil.isEmpty(prefix)) {
            return false;
        }

        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.getChildren().containsKey(c)) {
                return false;
            }
            node = node.getChildren().get(c);
        }
        return true;
    }

    /**
     * 获取单词频率
     *
     * @param word 单词
     * @return 频率
     */
    public int getFrequency(String word) {
        if (SmartStringUtil.isEmpty(word)) {
            return 0;
        }

        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.getChildren().containsKey(c)) {
                return 0;
            }
            node = node.getChildren().get(c);
        }
        return node.isEnd() ? node.getFrequency() : 0;
    }

    /**
     * 删除单词
     *
     * @param word 单词
     * @return 是否删除成功
     */
    public boolean delete(String word) {
        if (SmartStringUtil.isEmpty(word)) {
            return false;
        }

        return deleteHelper(root, word, 0);
    }

    /**
     * 递归删除辅助方法
     */
    private boolean deleteHelper(TrieNode node, String word, int index) {
        if (index == word.length()) {
            if (!node.isEnd()) {
                return false;
            }
            node.setEnd(false);
            node.setFrequency(0);
            return node.getChildren().isEmpty();
        }

        char c = word.charAt(index);
        TrieNode childNode = node.getChildren().get(c);
        if (childNode == null) {
            return false;
        }

        boolean shouldDeleteChild = deleteHelper(childNode, word, index + 1);
        if (shouldDeleteChild) {
            node.getChildren().remove(c);
            return !node.isEnd() && node.getChildren().isEmpty();
        }

        return false;
    }

    /**
     * 获取以指定前缀开头的所有单词
     *
     * @param prefix 前缀
     * @return 单词列表
     */
    public java.util.List<String> getWordsWithPrefix(String prefix) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (SmartStringUtil.isEmpty(prefix)) {
            return result;
        }

        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.getChildren().containsKey(c)) {
                return result;
            }
            node = node.getChildren().get(c);
        }

        collectWords(node, prefix, result);
        return result;
    }

    /**
     * 收集单词辅助方法
     */
    private void collectWords(TrieNode node, String currentWord, java.util.List<String> result) {
        if (node.isEnd()) {
            result.add(currentWord);
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            collectWords(entry.getValue(), currentWord + entry.getKey(), result);
        }
    }

    /**
     * 清空Trie
     */
    public void clear() {
        root.getChildren().clear();
        root.setEnd(false);
        root.setFrequency(0);
    }

    /**
     * 获取Trie中的单词总数
     *
     * @return 单词总数
     */
    public int size() {
        return countWords(root);
    }

    /**
     * 递归计数辅助方法
     */
    private int countWords(TrieNode node) {
        int count = node.isEnd() ? 1 : 0;
        for (TrieNode child : node.getChildren().values()) {
            count += countWords(child);
        }
        return count;
    }

    /**
     * 检查Trie是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return root.getChildren().isEmpty();
    }

    /**
     * 获取Trie的统计信息
     *
     * @return 统计信息
     */
    public TrieStats getStats() {
        TrieStats stats = new TrieStats();
        calculateStats(root, stats);
        return stats;
    }

    /**
     * 计算统计信息辅助方法
     */
    private void calculateStats(TrieNode node, TrieStats stats) {
        stats.nodeCount++;

        if (node.isEnd()) {
            stats.wordCount++;
            stats.totalFrequency += node.getFrequency();
        }

        stats.branchCount = Math.max(stats.branchCount, node.getChildren().size());

        for (TrieNode child : node.getChildren().values()) {
            calculateStats(child, stats);
        }
    }

    /**
     * Trie统计信息
     */
    @Data
    public static class TrieStats {
        private int nodeCount = 0;      // 节点总数
        private int wordCount = 0;      // 单词总数
        private int totalFrequency = 0; // 总频率
        private int branchCount = 0;    // 最大分支数
        private double avgBranchCount = 0; // 平均分支数

        public double getAvgBranchCount() {
            return nodeCount > 0 ? (double) branchCount / nodeCount : 0;
        }
    }
}