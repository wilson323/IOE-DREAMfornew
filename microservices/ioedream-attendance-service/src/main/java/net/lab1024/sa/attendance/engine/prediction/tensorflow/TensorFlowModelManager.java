package net.lab1024.sa.attendance.engine.prediction.tensorflow;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.prediction.model.PredictionModel;
import org.nd4j.autodiff.samediff.SameDiff;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TensorFlow模型管理器
 *
 * 负责模型的加载、保存、缓存和管理
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Component
public class TensorFlowModelManager {

    /**
     * 模型缓存 (modelPath -> SameDiff)
     */
    private final Map<String, SameDiff> modelCache = new ConcurrentHashMap<>();

    /**
     * 加载模型
     *
     * @param model 预测模型元数据
     * @return SameDiff计算图
     */
    public SameDiff loadModel(PredictionModel model) {
        String modelPath = (String) model.getParameters().get("modelPath");

        if (modelPath == null || modelPath.isEmpty()) {
            throw new IllegalArgumentException("模型路径为空: " + model.getModelType());
        }

        return loadModel(modelPath);
    }

    /**
     * 加载模型
     *
     * @param modelPath 模型文件路径
     * @return SameDiff计算图
     */
    public SameDiff loadModel(String modelPath) {
        log.info("[模型管理] 加载模型: path={}", modelPath);

        try {
            // 检查缓存
            if (modelCache.containsKey(modelPath)) {
                log.info("[模型管理] 从缓存加载模型: path={}", modelPath);
                return modelCache.get(modelPath);
            }

            // 从文件加载
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                throw new IllegalArgumentException("模型文件不存在: " + modelPath);
            }

            SameDiff sd = SameDiff.load(modelFile, true);

            // 缓存模型
            modelCache.put(modelPath, sd);

            log.info("[模型管理] 模型加载完成: path={}", modelPath);
            return sd;

        } catch (IOException e) {
            log.error("[模型管理] 加载模型失败: path={}, error={}",
                modelPath, e.getMessage(), e);
            throw new RuntimeException("加载模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存模型
     *
     * @param sd SameDiff计算图
     * @param modelPath 模型文件路径
     */
    public void saveModel(SameDiff sd, String modelPath) {
        log.info("[模型管理] 保存模型: path={}", modelPath);

        try {
            // 创建目录
            File modelFile = new File(modelPath);
            File parentDir = modelFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 保存模型
            sd.saveAsFile(modelFile);

            // 更新缓存
            modelCache.put(modelPath, sd);

            log.info("[模型管理] 模型保存完成: path={}", modelPath);

        } catch (IOException e) {
            log.error("[模型管理] 保存模型失败: path={}, error={}",
                modelPath, e.getMessage(), e);
            throw new RuntimeException("保存模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从缓存移除模型
     *
     * @param modelPath 模型文件路径
     */
    public void evictFromCache(String modelPath) {
        log.info("[模型管理] 从缓存移除模型: path={}", modelPath);
        modelCache.remove(modelPath);
    }

    /**
     * 清空所有缓存
     */
    public void clearCache() {
        log.info("[模型管理] 清空模型缓存: size={}", modelCache.size());
        modelCache.clear();
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStatistics getCacheStatistics() {
        return CacheStatistics.builder()
            .cachedModelCount(modelCache.size())
            .cachedModelPaths(modelCache.keySet())
            .build();
    }

    /**
     * 验证模型文件是否存在
     *
     * @param modelPath 模型文件路径
     * @return 是否存在
     */
    public boolean validateModelExists(String modelPath) {
        File modelFile = new File(modelPath);
        return modelFile.exists();
    }

    /**
     * 删除模型文件
     *
     * @param modelPath 模型文件路径
     */
    public void deleteModel(String modelPath) {
        log.info("[模型管理] 删除模型: path={}", modelPath);

        try {
            // 从缓存移除
            evictFromCache(modelPath);

            // 删除文件
            File modelFile = new File(modelPath);
            if (modelFile.exists()) {
                boolean deleted = modelFile.delete();
                if (deleted) {
                    log.info("[模型管理] 模型文件已删除: path={}", modelPath);
                } else {
                    log.warn("[模型管理] 模型文件删除失败: path={}", modelPath);
                }
            } else {
                log.warn("[模型管理] 模型文件不存在: path={}", modelPath);
            }

        } catch (Exception e) {
            log.error("[模型管理] 删除模型失败: path={}, error={}",
                modelPath, e.getMessage(), e);
            throw new RuntimeException("删除模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 缓存统计信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CacheStatistics {
        private int cachedModelCount;
        private java.util.Set<String> cachedModelPaths;
    }
}
