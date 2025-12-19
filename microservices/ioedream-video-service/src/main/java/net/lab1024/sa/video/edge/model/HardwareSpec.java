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

    public void setGpu(Boolean gpu) {
        this.gpu = gpu;
    }
}
