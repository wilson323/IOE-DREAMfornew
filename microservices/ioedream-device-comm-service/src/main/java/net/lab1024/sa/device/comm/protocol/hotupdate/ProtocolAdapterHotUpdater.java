package net.lab1024.sa.device.comm.protocol.hotupdate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.factory.ProtocolAdapterFactory;
import net.lab1024.sa.device.comm.dao.ProtocolConfigDao;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.nio.file.*;
import java.lang.reflect.Constructor;

/**
 * 协议适配器热更新管理器
 * <p>
 * 提供协议适配器的动态加载和热更新功能：
 * 1. 支持运行时加载新的协议适配器JAR包
 * 2. 支持协议适配器的热替换和版本升级
 * 3. 支持适配器配置的动态更新
 * 4. 支持适配器的回滚和版本管理
 * 5. 提供安全的更新验证和回滚机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component("protocolAdapterHotUpdater")
@Schema(description = "协议适配器热更新管理器")
public class ProtocolAdapterHotUpdater {

    @Resource
    private ProtocolAdapterFactory protocolAdapterFactory;

    @Resource
    private ProtocolConfigDao protocolConfigDao;

    // 适配器缓存
    private final Map<String, ProtocolAdapterInstance> adapterCache = new ConcurrentHashMap<>();

    // 适配器配置缓存
    private final Map<String, Map<String, Object>> adapterConfigCache = new ConcurrentHashMap<>();

    // 更新历史记录
    private final Map<String, List<UpdateRecord>> updateHistory = new ConcurrentHashMap<>();

    // 临时目录
    private final String tempDir = System.getProperty("java.io.tmpdir") + "/protocol-updates";

    /**
     * 初始化热更新管理器
     */
    public void initialize() {
        try {
            // 创建临时目录
            Path tempPath = Paths.get(tempDir);
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
            }

            // 启动定期清理临时文件
            startTempFileCleanup();

            log.info("[协议热更新] 初始化完成, 临时目录: {}", tempDir);

        } catch (Exception e) {
            log.error("[协议热更新] 初始化失败", e);
        }
    }

    /**
     * 热更新协议适配器
     *
     * @param protocolType 协议类型
     * @param jarFilePath JAR文件路径
     * @param className 适配器类名
     * @param config 配置参数
     * @return 更新结果
     */
    @Async("rs485TaskExecutor")
    public CompletableFuture<HotUpdateResult> hotUpdateAdapter(String protocolType,
                                                             String jarFilePath,
                                                             String className,
                                                             Map<String, Object> config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[协议热更新] 开始热更新适配器, protocolType={}, jarFilePath={}, className={}",
                        protocolType, jarFilePath, className);

                // 验证参数
                HotUpdateResult validation = validateUpdateParameters(protocolType, jarFilePath, className);
                if (!validation.isSuccess()) {
                    return validation;
                }

                // 备份当前适配器
                ProtocolAdapterInstance backup = backupCurrentAdapter(protocolType);
                log.debug("[协议热更新] 当前适配器备份完成, protocolType={}", protocolType);

                // 验证JAR文件
                validateJarFile(jarFilePath);

                // 加载新适配器
                ProtocolAdapter newAdapter = loadNewAdapter(jarFilePath, className);
                log.debug("[协议热更新] 新适配器加载成功, protocolType={}", protocolType);

                // 初始化新适配器
                initializeNewAdapter(newAdapter, config);

                // 验证新适配器
                HotUpdateResult validation2 = validateNewAdapter(newAdapter, protocolType);
                if (!validation2.isSuccess()) {
                    // 恢复备份的适配器
                    restoreBackupAdapter(backup, protocolType);
                    return validation2;
                }

                // 执行热更新
                executeHotUpdate(protocolType, newAdapter, backup);

                // 记录更新历史
                recordUpdateHistory(protocolType, jarFilePath, className, true);

                // 更新配置缓存
                if (config != null) {
                    adapterConfigCache.put(protocolType, new HashMap<>(config));
                }

                log.info("[协议热更新] 热更新成功, protocolType={}", protocolType);
                return HotUpdateResult.success("协议适配器热更新成功");

            } catch (Exception e) {
                log.error("[协议热更新] 热更新失败, protocolType={}, error={}", protocolType, e.getMessage(), e);
                recordUpdateHistory(protocolType, jarFilePath, className, false);
                return HotUpdateResult.failure("协议适配器热更新失败: " + e.getMessage());
            }
        });
    }

    /**
     * 动态更新适配器配置
     *
     * @param protocolType 协议类型
     * @param config 新的配置参数
     * @return 更新结果
     */
    @Async("rs485MonitorExecutor")
    public CompletableFuture<HotUpdateResult> updateAdapterConfig(String protocolType, Map<String, Object> config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[协议热更新] 开始更新适配器配置, protocolType={}", protocolType);

                ProtocolAdapterInstance adapterInstance = adapterCache.get(protocolType);
                if (adapterInstance == null) {
                    return HotUpdateResult.failure("协议适配器不存在: " + protocolType);
                }

                // 验证配置
                HotUpdateResult validation = validateConfig(adapterInstance.getAdapter(), config);
                if (!validation.isSuccess()) {
                    return validation;
                }

                // 备份当前配置
                Map<String, Object> backupConfig = new HashMap<>(adapterConfigCache.getOrDefault(protocolType, new HashMap<>()));

                // 应用新配置
                applyNewConfig(adapterInstance.getAdapter(), config);

                // 验证配置应用结果
                HotUpdateResult validation2 = validateConfigApplied(adapterInstance.getAdapter(), config);
                if (!validation2.isSuccess()) {
                    // 恢复备份配置
                    applyNewConfig(adapterInstance.getAdapter(), backupConfig);
                    return validation2;
                }

                // 更新配置缓存
                adapterConfigCache.put(protocolType, new HashMap<>(config));

                // 记录配置更新历史
                recordConfigUpdateHistory(protocolType, backupConfig, config, true);

                log.info("[协议热更新] 适配器配置更新成功, protocolType={}", protocolType);
                return HotUpdateResult.success("适配器配置更新成功");

            } catch (Exception e) {
                log.error("[协议热更新] 配置更新失败, protocolType={}, error={}", protocolType, e.getMessage(), e);
                recordConfigUpdateHistory(protocolType, null, config, false);
                return HotUpdateResult.failure("配置更新失败: " + e.getMessage());
            }
        });
    }

    /**
     * 回滚协议适配器到指定版本
     *
     * @param protocolType 协议类型
     * @param version 目标版本
     * @return 回滚结果
     */
    @Async("rs485MonitorExecutor")
    public CompletableFuture<HotUpdateResult> rollbackAdapter(String protocolType, String version) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[协议热更新] 开始回滚适配器, protocolType={}, version={}", protocolType, version);

                List<UpdateRecord> history = updateHistory.get(protocolType);
                if (history == null || history.isEmpty()) {
                    return HotUpdateResult.failure("没有可用的更新历史记录");
                }

                // 查找指定版本的更新记录
                UpdateRecord targetRecord = null;
                for (int i = history.size() - 1; i >= 0; i--) {
                    UpdateRecord record = history.get(i);
                    if (version.equals(record.getVersion())) {
                        targetRecord = record;
                        break;
                    }
                }

                if (targetRecord == null) {
                    return HotUpdateResult.failure("未找到指定版本的更新记录: " + version);
                }

                // 恢复指定版本
                HotUpdateResult rollbackResult = restoreVersion(targetRecord);
                if (!rollbackResult.isSuccess()) {
                    return rollbackResult;
                }

                log.info("[协议热更新] 适配器回滚成功, protocolType={}, version={}", protocolType, version);
                return HotUpdateResult.success("协议适配器回滚成功");

            } catch (Exception e) {
                log.error("[协议热更新] 适配器回滚失败, protocolType={}, version={}, error={}",
                        protocolType, version, e.getMessage(), e);
                return HotUpdateResult.failure("适配器回滚失败: " + e.getMessage());
            }
        });
    }

    /**
     * 获取适配器版本信息
     *
     * @param protocolType 协议类型
     * @return 版本信息
     */
    public AdapterVersionInfo getAdapterVersion(String protocolType) {
        try {
            ProtocolAdapterInstance adapterInstance = adapterCache.get(protocolType);
            if (adapterInstance == null) {
                return new AdapterVersionInfo(protocolType, "UNKNOWN", "适配器不存在", new Date());
            }

            ProtocolAdapter adapter = adapterInstance.getAdapter();
            String version = adapter.getVersion();
            String manufacturer = adapter.getManufacturer();

            return new AdapterVersionInfo(protocolType, version, manufacturer, new Date());

        } catch (Exception e) {
            log.error("[协议热更新] 获取适配器版本信息失败, protocolType={}, error={}", protocolType, e.getMessage(), e);
            return new AdapterVersionInfo(protocolType, "ERROR", "获取版本信息失败", new Date());
        }
    }

    /**
     * 获取所有适配器版本信息
     *
     * @return 适配器版本信息列表
     */
    public Map<String, AdapterVersionInfo> getAllAdapterVersions() {
        Map<String, AdapterVersionInfo> versions = new HashMap<>();

        for (String protocolType : adapterCache.keySet()) {
            AdapterVersionInfo versionInfo = getAdapterVersion(protocolType);
            versions.put(protocolType, versionInfo);
        }

        return versions;
    }

    /**
     * 获取更新历史
     *
     * @param protocolType 协议类型
     * @return 更新历史列表
     */
    public List<UpdateRecord> getUpdateHistory(String protocolType) {
        return updateHistory.getOrDefault(protocolType, new ArrayList<>());
    }

    /**
     * 清理临时文件
     */
    public void cleanupTempFiles() {
        try {
            Path tempPath = Paths.get(tempDir);
            if (Files.exists(tempPath)) {
                Files.walk(tempPath)
                    .filter(path -> Files.isRegularFile(path))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("[协议热更新] 删除临时文件: {}", path);
                        } catch (IOException e) {
                            log.warn("[协议热更新] 删除临时文件失败: {}", path, e);
                        }
                    });
            }
        } catch (Exception e) {
            log.error("[协议热更新] 清理临时文件失败", e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证更新参数
     */
    private HotUpdateResult validateUpdateParameters(String protocolType, String jarFilePath, String className) {
        if (protocolType == null || protocolType.trim().isEmpty()) {
            return HotUpdateResult.failure("协议类型不能为空");
        }

        if (jarFilePath == null || jarFilePath.trim().isEmpty()) {
            return HotUpdateResult.failure("JAR文件路径不能为空");
        }

        if (className == null || className.trim().isEmpty()) {
            return HotUpdateResult.failure("适配器类名不能为空");
        }

        Path jarPath = Paths.get(jarFilePath);
        if (!Files.exists(jarPath)) {
            return HotUpdateResult.failure("JAR文件不存在: " + jarFilePath);
        }

        if (!Files.isRegularFile(jarPath)) {
            return HotUpdateResult.failure("指定的路径不是JAR文件: " + jarFilePath);
        }

        return HotUpdateResult.success("参数验证通过");
    }

    /**
     * 备份当前适配器
     */
    private ProtocolAdapterInstance backupCurrentAdapter(String protocolType) {
        ProtocolAdapterInstance current = adapterCache.get(protocolType);
        if (current != null) {
            // 创建深拷贝备份
            ProtocolAdapterInstance backup = new ProtocolAdapterInstance(
                    current.getAdapter(),
                    current.getClassName(),
                    current.getJarFilePath(),
                    current.getVersion()
            );

            // 备份配置
            Map<String, Object> config = adapterConfigCache.get(protocolType);
            if (config != null) {
                backup.setConfig(new HashMap<>(config));
            }

            return backup;
        }
        return null;
    }

    /**
     * 验证JAR文件
     */
    private void validateJarFile(String jarFilePath) throws IOException {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            // 验证JAR文件完整性
            if (jarFile.size() == 0) {
                throw new IOException("JAR文件为空");
            }

            // 检查关键条目
            boolean hasManifest = false;
            boolean hasClassFile = false;

            java.util.Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.equals("META-INF/MANIFEST.MF")) {
                    hasManifest = true;
                } else if (name.endsWith(".class")) {
                    hasClassFile = true;
                }
            }

            if (!hasManifest) {
                throw new IOException("JAR文件缺少MANIFEST.MF文件");
            }

            if (!hasClassFile) {
                throw new IOException("JAR文件缺少class文件");
            }
        }
    }

    /**
     * 加载新适配器
     */
    private ProtocolAdapter loadNewAdapter(String jarFilePath, String className) throws Exception {
        // 创建自定义类加载器
        Path jarPath = Paths.get(jarFilePath);
        ClassLoader classLoader = new CustomClassLoader(jarPath.toString());

        // 加载适配器类
        @SuppressWarnings("unchecked")
        Class<? extends ProtocolAdapter> adapterClass = (Class<? extends ProtocolAdapter>) classLoader.loadClass(className);

        // 实例化适配器
        Constructor<? extends ProtocolAdapter> constructor = adapterClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        ProtocolAdapter adapter = constructor.newInstance();

        return adapter;
    }

    /**
     * 初始化新适配器
     */
    private void initializeNewAdapter(ProtocolAdapter adapter, Map<String, Object> config) {
        adapter.initialize();

        // 应用配置
        if (config != null) {
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                try {
                    // 使用反射设置配置参数
                    java.lang.reflect.Field field = adapter.getClass().getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(adapter, entry.getValue());
                } catch (Exception e) {
                    log.warn("[协议热更新] 设置适配器配置失败, field={}, value={}", entry.getKey(), entry.getValue(), e);
                }
            }
        }
    }

    /**
     * 验证新适配器
     */
    private HotUpdateResult validateNewAdapter(ProtocolAdapter adapter, String protocolType) {
        try {
            // 验证协议类型
            if (!protocolType.equals(adapter.getProtocolType())) {
                return HotUpdateResult.failure("新适配器的协议类型不匹配");
            }

            // 验证基本功能
            String[] supportedModels = adapter.getSupportedDeviceModels();
            if (supportedModels == null || supportedModels.length == 0) {
                return HotUpdateResult.failure("新适配器没有支持的设备型号");
            }

            // 验证厂商信息
            String manufacturer = adapter.getManufacturer();
            if (manufacturer == null || manufacturer.trim().isEmpty()) {
                return HotUpdateResult.failure("新适配器厂商信息无效");
            }

            return HotUpdateResult.success("新适配器验证通过");

        } catch (Exception e) {
            return HotUpdateResult.failure("新适配器验证失败: " + e.getMessage());
        }
    }

    /**
     * 执行热更新
     */
    private void executeHotUpdate(String protocolType, ProtocolAdapter newAdapter, ProtocolAdapterInstance backup) {
        try {
            // 销毁旧适配器
            ProtocolAdapterInstance oldAdapterInstance = adapterCache.remove(protocolType);
            if (oldAdapterInstance != null && oldAdapterInstance.getAdapter() != null) {
                try {
                    oldAdapterInstance.getAdapter().destroy();
                } catch (Exception e) {
                    log.warn("[协议热更新] 销毁旧适配器失败", e);
                }
            }

            // 创建新的适配器实例
            ProtocolAdapterInstance newAdapterInstance = new ProtocolAdapterInstance(
                    newAdapter,
                    newAdapter.getClass().getName(),
                    "",  // 运行时加载，没有JAR路径
                    newAdapter.getVersion()
            );

            // 设置配置
            Map<String, Object> config = adapterConfigCache.get(protocolType);
            if (config != null) {
                newAdapterInstance.setConfig(new HashMap<>(config));
            }

            // 注册到缓存
            adapterCache.put(protocolType, newAdapterInstance);

            log.info("[协议热更新] 热更新执行完成, protocolType={}", protocolType);

        } catch (Exception e) {
            log.error("[协议热更新] 执行热更新失败, protocolType={}", protocolType, e);
            throw new RuntimeException("执行热更新失败", e);
        }
    }

    /**
     * 恢复备份适配器
     */
    private void restoreBackupAdapter(ProtocolAdapterInstance backup, String protocolType) {
        if (backup == null) {
            log.warn("[协议热更新] 没有可用的备份适配器, protocolType={}", protocolType);
            return;
        }

        try {
            executeHotUpdate(protocolType, backup.getAdapter(), null);
            log.info("[协议热更新] 备份适配器恢复成功, protocolType={}", protocolType);

        } catch (Exception e) {
            log.error("[协议热更新] 恢复备份适配器失败, protocolType={}", protocolType, e);
        }
    }

    /**
     * 验证配置
     */
    private HotUpdateResult validateConfig(ProtocolAdapter adapter, Map<String, Object> config) {
        try {
            // 这里可以根据具体协议类型进行配置验证
            // 例如：验证必需的配置项、配置值的合法性等

            if (config == null || config.isEmpty()) {
                return HotUpdateResult.success("配置为空，跳过验证");
            }

            // 示例：验证RS485协议配置
            if ("RS485".equals(adapter.getProtocolType())) {
                return validateRS485Config(config);
            }

            // 其他协议类型的验证逻辑...

            return HotUpdateResult.success("配置验证通过");

        } catch (Exception e) {
            return HotUpdateResult.failure("配置验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证RS485配置
     */
    private HotUpdateResult validateRS485Config(Map<String, Object> config) {
        // 验证波特率
        Object baudRate = config.get("baudRate");
        if (baudRate != null) {
            int rate = Integer.parseInt(baudRate.toString());
            if (rate != 300 && rate != 600 && rate != 1200 && rate != 2400 &&
                rate != 4800 && rate != 9600 && rate != 19200 && rate != 38400) {
                return HotUpdateResult.failure("RS485波特率不支持: " + rate);
            }
        }

        // 验证数据位
        Object dataBits = config.get("dataBits");
        if (dataBits != null) {
            int bits = Integer.parseInt(dataBits.toString());
            if (bits != 7 && bits != 8) {
                return HotUpdateResult.failure("RS485数据位不支持: " + bits);
            }
        }

        // 验证停止位
        Object stopBits = config.get("stopBits");
        if (stopBits != null) {
            int bits = Integer.parseInt(stopBits.toString());
            if (bits != 1 && bits != 2) {
                return HotUpdateResult.failure("RS485停止位不支持: " + bits);
            }
        }

        return HotUpdateResult.success("RS485配置验证通过");
    }

    /**
     * 应用新配置
     */
    private void applyNewConfig(ProtocolAdapter adapter, Map<String, Object> config) {
        try {
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                try {
                    java.lang.reflect.Field field = adapter.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(adapter, value);
                } catch (NoSuchFieldException e) {
                    log.warn("[协议热更新] 适配器没有配置字段: {}", key);
                } catch (Exception e) {
                    log.error("[协议热更新] 设置配置字段失败, field={}, value={}", key, value, e);
                }
            }
        } catch (Exception e) {
            log.error("[协议热更新] 应用新配置失败", e);
        }
    }

    /**
     * 验证配置应用结果
     */
    private HotUpdateResult validateConfigApplied(ProtocolAdapter adapter, Map<String, Object> config) {
        try {
            // 这里可以验证配置是否正确应用
            // 例如：检查配置值是否已正确设置到适配器实例中

            if (config == null || config.isEmpty()) {
                return HotUpdateResult.success("配置为空，跳过验证");
            }

            // 示例：验证RS485配置应用
            if ("RS485".equals(adapter.getProtocolType())) {
                return validateRS485ConfigApplied(adapter, config);
            }

            // 其他协议类型的验证逻辑...

            return HotUpdateResult.success("配置应用验证通过");

        } catch (Exception e) {
            return HotUpdateResult.failure("配置应用验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证RS485配置应用结果
     */
    private HotUpdateResult validateRS485ConfigApplied(ProtocolAdapter adapter, Map<String, Object> config) {
        try {
            // 验证配置是否已正确应用
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                String key = entry.getKey();
                Object expectedValue = entry.getValue();

                try {
                    java.lang.reflect.Field field = adapter.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    Object actualValue = field.get(adapter);

                    if (!Objects.equals(expectedValue, actualValue)) {
                        return HotUpdateResult.failure(
                                String.format("配置项应用失败: %s, 期望值: %s, 实际值: %s",
                                        key, expectedValue, actualValue));
                    }
                } catch (NoSuchFieldException e) {
                    // 字段不存在，可能配置项不支持
                    log.debug("[协议热更新] 配置项不存在: {}", key);
                }
            }

            return HotUpdateResult.success("RS485配置应用验证通过");

        } catch (Exception e) {
            return HotUpdateResult.failure("RS485配置应用验证失败: " + e.getMessage());
        }
    }

    /**
     * 恢复指定版本
     */
    private HotUpdateResult restoreVersion(UpdateRecord record) {
        try {
            // 这里需要根据历史记录恢复适配器
            // 实际实现中可能需要从JAR仓库重新下载指定版本的适配器

            String jarFilePath = record.getJarFilePath();
            String className = record.getClassName();
            Map<String, Object> config = record.getNewConfig();

            // 重新加载适配器
            ProtocolAdapter adapter = loadNewAdapter(jarFilePath, className);
            initializeNewAdapter(adapter, config);

            // 执行更新
            executeHotUpdate(record.getProtocolType(), adapter, null);

            log.info("[协议热更新] 版本恢复成功, protocolType={}, version={}",
                    record.getProtocolType(), record.getVersion());

            return HotUpdateResult.success("版本恢复成功");

        } catch (Exception e) {
            log.error("[协议热更新] 版本恢复失败, protocolType={}, version={}, error={}",
                    record.getProtocolType(), record.getVersion(), e.getMessage(), e);
            return HotUpdateResult.failure("版本恢复失败: " + e.getMessage());
        }
    }

    /**
     * 记录更新历史
     */
    private void recordUpdateHistory(String protocolType, String jarFilePath, String className, boolean success) {
        UpdateRecord record = new UpdateRecord();
        record.setProtocolType(protocolType);
        record.setJarFilePath(jarFilePath);
        record.setClassName(className);
        record.setVersion("unknown"); // 实际实现中应该从适配器获取
        record.setUpdateTime(new Date());
        record.setSuccess(success);

        updateHistory.computeIfAbsent(protocolType, k -> new ArrayList<>()).add(record);

        // 限制历史记录数量
        List<UpdateRecord> history = updateHistory.get(protocolType);
        if (history.size() > 50) {
            history.remove(0);
        }
    }

    /**
     * 记录配置更新历史
     */
    private void recordConfigUpdateHistory(String protocolType, Map<String, Object> oldConfig,
                                            Map<String, Object> newConfig, boolean success) {
        UpdateRecord record = new UpdateRecord();
        record.setProtocolType(protocolType);
        record.setJarFilePath("");  // 配置更新没有JAR文件
        record.setClassName("");  // 配置更新没有类名
        record.setVersion("config-update-" + System.currentTimeMillis());
        record.setUpdateTime(new Date());
        record.setSuccess(success);
        record.setOldConfig(oldConfig != null ? new HashMap<>(oldConfig) : null);
        record.setNewConfig(newConfig != null ? new HashMap<>(newConfig) : null);

        updateHistory.computeIfAbsent(protocolType, k -> new ArrayList<>()).add(record);

        // 限制历史记录数量
        List<UpdateRecord> history = updateHistory.get(protocolType);
        if (history.size() > 50) {
            history.remove(0);
        }
    }

    /**
     * 启动定期清理临时文件
     */
    private void startTempFileCleanup() {
        // 每小时清理一次临时文件
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanupTempFiles, 1, 1, java.util.concurrent.TimeUnit.HOURS);
    }

    // ==================== 内部类 ====================

    /**
     * 适配器实例信息
     */
    public static class ProtocolAdapterInstance {
        private final ProtocolAdapter adapter;
        private final String className;
        private final String jarFilePath;
        private final String version;
        private Map<String, Object> config;

        public ProtocolAdapterInstance(ProtocolAdapter adapter, String className,
                                        String jarFilePath, String version) {
            this.adapter = adapter;
            this.className = className;
            this.jarFilePath = jarFilePath;
            this.version = version;
        }

        // getters and setters
        public ProtocolAdapter getAdapter() { return adapter; }
        public String getClassName() { return className; }
        public String getJarFilePath() { return jarFilePath; }
        public String getVersion() { return version; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }

    /**
     * 更新记录
     */
    public static class UpdateRecord {
        private String protocolType;
        private String jarFilePath;
        private String className;
        private String version;
        private Date updateTime;
        private boolean success;
        private Map<String, Object> oldConfig;
        private Map<String, Object> newConfig;

        // getters and setters
        public String getProtocolType() { return protocolType; }
        public void setProtocolType(String protocolType) { this.protocolType = protocolType; }
        public String getJarFilePath() { return jarFilePath; }
        public void setJarFilePath(String jarFilePath) { this.jarFilePath = jarFilePath; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public Date getUpdateTime() { return updateTime; }
        public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Map<String, Object> getOldConfig() { return oldConfig; }
        public void setOldConfig(Map<String, Object> oldConfig) { this.oldConfig = oldConfig; }
        public Map<String, Object> getNewConfig() { return newConfig; }
        public void setNewConfig(Map<String, Object> newConfig) { this.newConfig = newConfig; }
    }

    /**
     * 适配器版本信息
     */
    public static class AdapterVersionInfo {
        private String protocolType;
        private String version;
        private String manufacturer;
        private Date updateTime;

        public AdapterVersionInfo(String protocolType, String version, String manufacturer, Date updateTime) {
            this.protocolType = protocolType;
            this.version = version;
            this.manufacturer = manufacturer;
            this.updateTime = updateTime;
        }

        // getters and setters
        public String getProtocolType() { return protocolType; }
        public void setProtocolType(String protocolType) { this.protocolType = protocolType; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        public Date getUpdateTime() { return updateTime; }
        public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    }

    /**
     * 热更新结果
     */
    public static class HotUpdateResult {
        private boolean success;
        private String message;

        public HotUpdateResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static HotUpdateResult success(String message) {
            return new HotUpdateResult(true, message);
        }

        public static HotUpdateResult failure(String message) {
            return new HotUpdateResult(false, message);
        }

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 自定义类加载器
     */
    private static class CustomClassLoader extends ClassLoader {
        private final String jarFilePath;

        public CustomClassLoader(String jarFilePath) {
            super(Thread.currentThread().getContextClassLoader());
            this.jarFilePath = jarFilePath;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                // 从JAR文件中查找类
                if (name.startsWith("net.lab1024.sa.device.comm.protocol.")) {
                    return loadClassFromJar(name);
                }
                throw e;
            }
        }

        private Class<?> loadClassFromJar(String name) throws ClassNotFoundException {
            try {
                Path jarPath = Paths.get(jarFilePath);
                try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                    String classFileName = name.replace('.', '/') + ".class";
                    JarEntry entry = jarFile.getJarEntry(classFileName);
                    if (entry != null) {
                        try (java.io.InputStream is = jarFile.getInputStream(entry)) {
                            byte[] bytes = is.readAllBytes();
                            return defineClass(name, bytes, 0, bytes.length);
                        }
                    } else {
                        throw new ClassNotFoundException("类文件未在JAR中找到: " + name);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new ClassNotFoundException("无法从JAR文件加载类: " + name, e);
            }
        }
    }
}
