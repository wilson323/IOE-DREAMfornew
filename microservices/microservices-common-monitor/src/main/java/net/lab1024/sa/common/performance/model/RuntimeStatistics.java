package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 运行时统计信息
 * <p>
 * JVM运行时的详细统计信息
 * 包含输入输出、参数、环境等信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class RuntimeStatistics {

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    /**
     * Java版本
     */
    private String javaVersion;

    /**
     * Java厂商
     */
    private String javaVendor;

    /**
     * Java安装路径
     */
    private String javaHome;

    /**
     * JVM名称
     */
    private String jvmName;

    /**
     * JVM版本
     */
    private String jvmVersion;

    /**
     * JVM启动时间
     */
    private LocalDateTime jvmStartTime;

    /**
     * JVM运行时间（毫秒）
     */
    private long jvmUptime;

    /**
     * 程序启动参数
     */
    private java.util.List<String> inputArguments;

    /**
     * 系统环境变量数量
     */
    private int environmentVariableCount;

    /**
     * 系统属性数量
     */
    private int systemPropertyCount;
}