package net.lab1024.sa.video.edge.ai.model;

/**
 * 模型信息（边缘AI引擎内部模型）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class ModelInfo {

    private String modelName;
    private long modelSize;
    private long loadTime;
    private long inferenceCount;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public long getModelSize() {
        return modelSize;
    }

    public void setModelSize(long modelSize) {
        this.modelSize = modelSize;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public long getInferenceCount() {
        return inferenceCount;
    }

    public void setInferenceCount(long inferenceCount) {
        this.inferenceCount = inferenceCount;
    }
}

