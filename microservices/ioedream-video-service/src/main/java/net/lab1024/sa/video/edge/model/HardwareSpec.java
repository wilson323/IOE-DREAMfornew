package net.lab1024.sa.video.edge.model;

/**
 * 边缘设备硬件规格
 * <p>
 * 说明：用于边缘AI引擎的硬件兼容性判断（最小字段集）。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class HardwareSpec {

    private Integer cpuCores;
    private Integer memoryMB;
    private Boolean gpu;

    /**
     * CPU型号（可选，用于展示/兼容）
     */
    private String cpuModel;

    /**
     * GPU型号（可选，用于展示/兼容）
     */
    private String gpuModel;

    /**
     * 内存大小（GB，可选；设置时会同步到 memoryMB）
     */
    private Integer memorySize;

    /**
     * 存储大小（GB，可选）
     */
    private Integer storageSize;

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Integer getMemoryMB() {
        return memoryMB;
    }

    public void setMemoryMB(Integer memoryMB) {
        this.memoryMB = memoryMB;
    }

    public boolean hasGPU() {
        return Boolean.TRUE.equals(gpu);
    }

    public Boolean getGpu() {
        return gpu;
    }

    public void setGpu(Boolean gpu) {
        this.gpu = gpu;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public String getGpuModel() {
        return gpuModel;
    }

    public void setGpuModel(String gpuModel) {
        this.gpuModel = gpuModel;
    }

    public Integer getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(Integer memorySize) {
        this.memorySize = memorySize;
        // 同步更新 memoryMB（若尚未设置）
        if (this.memoryMB == null && memorySize != null) {
            this.memoryMB = memorySize * 1024;
        }
    }

    public Integer getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(Integer storageSize) {
        this.storageSize = storageSize;
    }
}
