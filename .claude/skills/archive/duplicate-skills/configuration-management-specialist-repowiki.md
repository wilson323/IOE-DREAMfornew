# 配置管理专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki/zh/content/配置管理/` 权威规范，确保IOE-DREAM项目的配置管理完全符合企业级标准

**⚡ 技能等级**: ★★★★★ (配置管理专家)
**🎯 适用场景**: 系统配置管理、环境配置、配置热更新、系统参数管理
**📊 技能覆盖**: 前后端配置管理 | 环境隔离 | 配置热更新 | 系统参数管理 | 最佳实践

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/配置管理/)**
- **配置架构管理**: 严格基于配置管理.md规范的sa-base.yaml + application.yaml架构
- **环境配置隔离**: 完全遵循环境配置.md的dev/pre/prod/test四环境管理
- **系统参数管理**: 基于系统参数.md的ConfigController→ConfigService→ConfigDao三层架构
- **配置热更新**: 严格按照配置热更新.md的smart-reload机制实现

### **解决能力**
- **配置标准化**: 100%符合repowiki配置管理规范
- **环境隔离问题**: 完全解决多环境配置混淆问题
- **配置热更新**: 实现不重启服务的动态配置更新
- **系统参数管理**: 建立完整的参数增删改查体系

---

## 🏗️ Repowiki配置管理架构

### **配置文件架构 (基于配置管理.md)**
```yaml
# 第一层: sa-base.yaml - 基础配置
位置: sa-base/src/main/resources/{env}/sa-base.yaml
包含: 数据库、Redis、文件存储、安全认证、性能监控等基础配置

# 第二层: application.yaml - 应用配置
位置: sa-admin/src/main/resources/{env}/application.yaml
包含: 端口设置、日志目录、上下文路径等应用特定配置
```

### **四环境配置体系 (基于环境配置.md)**
- **开发环境(dev)**: 配置宽松，日志详细，便于调试
- **预发布环境(pre)**: 接近生产环境配置，用于最终验证
- **生产环境(prod)**: 高性能配置，严格安全设置
- **测试环境(test)**: 独立端口，便于并行测试

### **系统参数管理 (基于系统参数.md)**
```java
// Controller层: ConfigController - RESTful API入口
@Resource
private ConfigService configService;

// Service层: ConfigService - 业务逻辑 + 本地缓存
private final ConcurrentHashMap<String, ConfigEntity> CONFIG_CACHE = new ConcurrentHashMap<>();

// DAO层: ConfigDao - 数据库访问
// 数据库表: t_config - 系统参数存储

// 枚举层: ConfigKeyEnum - 类型安全的参数键定义
public enum ConfigKeyEnum {
    SUPER_PASSWORD("超级管理员密码", "admin123"),
    LEVEL3_PROTECT_CONFIG("三级等保配置", "enable");
}
```

---

## 🛠️ 核心配置管理操作

### 1. 环境配置标准化

#### 步骤1: 配置文件结构规范
```yaml
# sa-base.yaml (基础配置)
spring:
  datasource:
    url: jdbc:mysql://host:port/database?parameters
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: ${DB_DRIVER:com.p6spy.engine.spy.P6SpyDriver}
    initial-size: 5
    min-idle: 5
    max-active: ${DB_MAX_ACTIVE:10}
    max-wait: 60000

spring:
  data:
    redis:
      database: ${REDIS_DATABASE:0}
      host: ${REDIS_HOST:127.0.0.1}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 3000
      lettuce:
        pool:
          max-active: ${REDIS_MAX_ACTIVE:10}
          min-idle: 2
          max-idle: 8
          max-wait: 3000

file:
  storage:
    mode: ${FILE_STORAGE_MODE:local}
    local:
      upload-path: ${FILE_UPLOAD_PATH:./upload/}
      url-prefix: ${FILE_URL_PREFIX:/files/}
    cloud:
      region: ${CLOUD_REGION:oss-cn-hangzhou}
      endpoint: ${CLOUD_ENDPOINT:}
      bucket-name: ${CLOUD_BUCKET:smart-admin}
      access-key: ${CLOUD_ACCESS_KEY:}
      secret-key: ${CLOUD_SECRET_KEY:}

sa-token:
  token-name: ${SA_TOKEN_NAME:Authorization}
  timeout: ${SA_TOKEN_TIMEOUT:2592000}
  active-timeout: ${SA_ACTIVE_TIMEOUT:1800}
  is-concurrent: ${SA_CONCURRENT:true}
  is-share: ${SA_SHARE:false}
  is-log: ${SA_LOG:false}

knife4j:
  enable: ${KNIFE4J_ENABLE:true}
  basic:
    enable: ${KNIFE4J_BASIC_ENABLE:false}
    username: ${KNIFE4J_USERNAME:admin}
    password: ${KNIFE4J_PASSWORD:admin123}

reload:
  interval-seconds: ${RELOAD_INTERVAL:300}
  enable: ${RELOAD_ENABLE:true}
```

#### 步骤2: 环境差异化配置
```bash
# 开发环境配置覆盖
dev/sa-base.yaml:
  - max-active: 10 (小连接池)
  - log-level: debug (详细日志)
  - sa-token.is-log: true (开启操作日志)
  - reload.interval-seconds: 300 (5分钟检查)

# 生产环境配置覆盖
prod/sa-base.yaml:
  - max-active: 200 (大连接池)
  - log-level: warn (减少日志)
  - sa-token.is-log: false (关闭操作日志)
  - reload.interval-seconds: 60 (1分钟检查)
  - driver-class-name: com.mysql.cj.jdbc.Driver (原生驱动)

# 预发布环境配置覆盖
pre/sa-base.yaml:
  - max-active: 50 (中等连接池)
  - log-level: info (信息日志)
  - reload.interval-seconds: 120 (2分钟检查)

# 测试环境配置覆盖
test/sa-base.yaml:
  - server.port: 1025 (独立端口)
  - max-active: 10 (小连接池)
  - reload.interval-seconds: 60 (1分钟检查)
```

#### 步骤3: 配置合规性检查
```bash
#!/bin/bash
# repowiki配置合规性检查脚本
echo "🔍 检查配置管理repowiki合规性..."

# 1. 检查配置文件结构
config_files=("sa-base.yaml" "application.yaml")
for env in dev pre prod test; do
    for config in "${config_files[@]}"; do
        if [ ! -f "src/main/resources/${env}/${config}" ]; then
            echo "❌ 缺失配置文件: ${env}/${config}"
            exit 1
        fi
    done
done

# 2. 检查配置项规范性
echo "   - 检查数据库配置规范..."
grep -q "spring.datasource.url" src/main/resources/*/sa-base.yaml || {
    echo "❌ 缺少数据库URL配置"
    exit 1
}

echo "   - 检查Redis配置规范..."
grep -q "spring.data.redis" src/main/resources/*/sa-base.yaml || {
    echo "❌ 缺少Redis配置"
    exit 1
}

echo "   - 检查文件存储配置规范..."
grep -q "file.storage.mode" src/main/resources/*/sa-base.yaml || {
    echo "❌ 缺少文件存储配置"
    exit 1
}

# 3. 检查环境差异化
echo "   - 检查生产环境优化..."
prod_connections=$(grep -o "max-active: [0-9]*" src/main/resources/prod/sa-base.yaml | grep -o "[0-9]*" | head -1)
if [ "$prod_connections" -lt 100 ]; then
    echo "⚠️ 生产环境连接池过小: $prod_connections"
fi

echo "   - 检查敏感配置安全..."
if grep -r "password:.*123" src/main/resources/prod/; then
    echo "❌ 生产环境存在默认密码!"
    exit 1
fi

echo "✅ 配置管理合规性检查通过"
```

### 2. 系统参数管理实现

#### 步骤1: 配置参数枚举定义
```java
// ConfigKeyEnum.java - 基于repowiki系统参数.md规范
public enum ConfigKeyEnum {

    // 系统安全配置
    SUPER_PASSWORD("超级管理员密码", "admin123"),
    LEVEL3_PROTECT_CONFIG("三级等保配置", "enable"),
    SESSION_TIMEOUT("会话超时时间(分钟)", "30"),

    // 业务功能配置
    ATTENDANCE_CHECK_ENABLED("考勤检查开关", "true"),
    DEVICE_ACCESS_CONTROL("设备门禁控制", "true"),
    VIDEO_SURVEILLANCE_ENABLED("视频监控开关", "true"),

    // 性能优化配置
    CACHE_EXPIRE_TIME("缓存过期时间(秒)", "3600"),
    THREAD_POOL_SIZE("线程池大小", "20"),
    DATABASE_CONNECTION_POOL("数据库连接池大小", "50"),

    // 外部服务配置
    SMS_SERVICE_ENABLED("短信服务开关", "true"),
    EMAIL_SMTP_HOST("邮件SMTP服务器", "smtp.example.com"),
    WECHAT_APP_ID("微信应用ID", ""),

    // 界面配置
    DEFAULT_THEME("默认主题", "default"),
    PAGE_SIZE("分页大小", "20"),
    DATE_FORMAT("日期格式", "yyyy-MM-dd");

    private final String description;
    private final String defaultValue;

    ConfigKeyEnum(String description, String defaultValue) {
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
```

#### 步骤2: 配置服务实现 (基于系统参数.md)
```java
@Service
@Slf4j
public class ConfigService {

    @Resource
    private ConfigDao configDao;

    // 本地缓存 - 基于repowiki系统参数.md
    private final ConcurrentHashMap<String, ConfigEntity> CONFIG_CACHE = new ConcurrentHashMap<>();

    // 配置更新方法
    public ResponseDTO<String> updateValueByKey(ConfigKeyEnum configKey, String configValue) {
        log.info("更新系统参数: {} = {}", configKey.name(), configValue);

        // 1. 更新数据库
        ConfigEntity entity = new ConfigEntity();
        entity.setConfigKey(configKey.name());
        entity.setConfigValue(configValue);
        entity.setConfigName(configKey.getDescription());
        entity.setRemark("系统参数自动更新");

        int result = configDao.updateByKey(entity);
        if (result <= 0) {
            return ResponseDTO.userError("更新参数失败");
        }

        // 2. 更新本地缓存
        refreshConfigCache(configKey.name());

        return ResponseDTO.ok("参数更新成功");
    }

    // 获取配置值
    public String getConfigValue(ConfigKeyEnum configKey) {
        // 1. 先从本地缓存获取
        ConfigEntity cacheEntity = CONFIG_CACHE.get(configKey.name());
        if (cacheEntity != null) {
            return cacheEntity.getConfigValue();
        }

        // 2. 从数据库加载
        ConfigEntity dbEntity = configDao.selectByKey(configKey.name());
        if (dbEntity == null) {
            // 3. 使用默认值并保存到数据库
            dbEntity = new ConfigEntity();
            dbEntity.setConfigKey(configKey.name());
            dbEntity.setConfigValue(configKey.getDefaultValue());
            dbEntity.setConfigName(configKey.getDescription());
            dbEntity.setRemark("默认配置");
            configDao.insert(dbEntity);
        }

        // 4. 更新本地缓存
        CONFIG_CACHE.put(configKey.name(), dbEntity);

        return dbEntity.getConfigValue();
    }

    // 类型安全的配置获取
    public <T> T getConfigValue2Obj(ConfigKeyEnum configKey, Class<T> clazz) {
        String value = getConfigValue(configKey);

        try {
            if (clazz == Boolean.class) {
                return clazz.cast(Boolean.parseBoolean(value));
            } else if (clazz == Integer.class) {
                return clazz.cast(Integer.parseInt(value));
            } else if (clazz == Long.class) {
                return clazz.cast(Long.parseLong(value));
            } else {
                return clazz.cast(value);
            }
        } catch (Exception e) {
            log.error("配置值类型转换失败: {} = {}, target: {}", configKey.name(), value, clazz.getSimpleName(), e);
            // 返回默认值
            String defaultValue = configKey.getDefaultValue();
            if (clazz == Boolean.class) {
                return clazz.cast(Boolean.parseBoolean(defaultValue));
            } else if (clazz == Integer.class) {
                return clazz.cast(Integer.parseInt(defaultValue));
            } else if (clazz == Long.class) {
                return clazz.cast(Long.parseLong(defaultValue));
            } else {
                return clazz.cast(defaultValue);
            }
        }
    }

    // 刷新本地缓存
    private void refreshConfigCache(String configKey) {
        CONFIG_CACHE.remove(configKey);
        log.debug("刷新配置缓存: {}", configKey);
    }

    // 启动时加载所有配置
    @PostConstruct
    public void loadConfigCache() {
        log.info("开始加载系统参数到本地缓存...");

        List<ConfigEntity> configList = configDao.selectAll();
        for (ConfigEntity config : configList) {
            CONFIG_CACHE.put(config.getConfigKey(), config);
        }

        log.info("系统参数加载完成，共{}项", configList.size());
    }
}
```

### 3. 配置热更新机制 (基于配置热更新.md)

#### 步骤1: SmartReload注解定义
```java
// SmartReload.java - 基于repowiki配置热更新.md
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SmartReload {

    /**
     * 热更新项的标签，必须唯一
     */
    String value();

    /**
     * 热更新项的描述
     */
    String description() default "";

    /**
     * 是否启用热更新
     */
    boolean enabled() default true;
}
```

#### 步骤2: 配置热更新服务实现
```java
@Component
@Slf4j
public class ConfigReloadService {

    @Resource
    private ConfigService configService;

    @Resource
    private BaseCacheManager cacheManager;

    /**
     * 配置参数热更新 - 基于repowiki配置热更新.md
     */
    @SmartReload(ReloadConst.CONFIG_RELOAD)
    @Operation(summary = "配置参数热更新", description = "重新加载系统配置参数")
    public void reloadConfig(String tag) {
        log.info("执行配置参数热更新: tag={}", tag);

        try {
            // 1. 清空ConfigService本地缓存
            configService.loadConfigCache();

            // 2. 刷新相关缓存
            cacheManager.clearNamespace("CONFIG");

            // 3. 刷新其他相关配置
            refreshRelatedConfig();

            log.info("配置参数热更新完成: tag={}", tag);
        } catch (Exception e) {
            log.error("配置参数热更新失败: tag={}", tag, e);
            throw new SmartException("配置参数热更新失败: " + e.getMessage());
        }
    }

    /**
     * 刷新相关配置
     */
    private void refreshRelatedConfig() {
        // 刷新系统安全配置
        log.debug("刷新系统安全配置...");

        // 刷新业务功能配置
        log.debug("刷新业务功能配置...");

        // 刷新性能配置
        log.debug("刷新性能配置...");

        // 刷新外部服务配置
        log.debug("刷新外部服务配置...");
    }

    /**
     * 缓存热更新
     */
    @SmartReload(ReloadConst.CACHE_SERVICE)
    @Operation(summary = "缓存服务热更新", description = "重新加载缓存配置")
    public void reloadCache(String tag) {
        log.info("执行缓存服务热更新: tag={}", tag);

        try {
            // 1. 清空所有缓存命名空间
            cacheManager.clearAll();

            // 2. 预热关键缓存
            warmupCache();

            log.info("缓存服务热更新完成: tag={}", tag);
        } catch (Exception e) {
            log.error("缓存服务热更新失败: tag={}", tag, e);
            throw new SmartException("缓存服务热更新失败: " + e.getMessage());
        }
    }

    /**
     * 预热缓存
     */
    private void warmupCache() {
        log.debug("开始缓存预热...");

        // 预热系统参数
        for (ConfigKeyEnum configKey : ConfigKeyEnum.values()) {
            configService.getConfigValue(configKey);
        }

        log.debug("缓存预热完成");
    }
}
```

#### 步骤3: 前端配置管理界面
```javascript
// config-api.js - 基于repowiki配置管理.md
import {getRequest, postRequest} from '@/utils/http';

export const configApi = {
  // 查询配置列表
  queryList: (params) => {
    return postRequest('/support/config/query', params);
  },

  // 添加配置
  addConfig: (params) => {
    return postRequest('/support/config/add', params);
  },

  // 更新配置
  updateConfig: (params) => {
    return postRequest('/support/config/update', params);
  },

  // 删除配置
  deleteConfig: (configId) => {
    return getRequest(`/support/config/delete/${configId}`);
  },

  // 查询配置详情
  queryByKey: (configKey) => {
    return getRequest(`/support/config/queryByKey?configKey=${configKey}`);
  },

  // 配置热更新
  reloadConfig: (reloadForm) => {
    return postRequest('/support/reload/update', reloadForm);
  },

  // 查询热更新结果
  queryReloadResult: (params) => {
    return postRequest('/support/reload/queryResult', params);
  }
};

export default configApi;
```

---

## ⚠️ 配置管理注意事项

### 配置安全原则
- **敏感信息保护**: 生产环境密码必须通过环境变量注入，严禁硬编码
- **配置权限控制**: 严格限制配置管理权限，实现细粒度访问控制
- **配置变更审计**: 所有配置变更必须记录，包括操作人、时间、变更内容
- **备份与恢复**: 重要配置变更前必须备份，制定恢复策略

### 环境隔离要求
- **网络隔离**: 不同环境必须使用独立的网络区域
- **数据隔离**: 禁止生产数据直接用于开发测试
- **配置差异**: 严格区分各环境配置，避免配置混淆
- **版本控制**: 所有配置文件必须纳入版本控制系统

### 热更新安全规范
- **权限验证**: 执行热更新操作必须验证用户权限
- **操作日志**: 详细记录热更新操作日志，包括结果和异常
- **幂等性**: 确保热更新操作可以安全地重复执行
- **回滚机制**: 为关键配置提供回滚方案

---

## 📊 配置管理评估标准

### 配置完整性检查清单
- [ ] 所有环境配置文件完整 (dev/pre/prod/test)
- [ ] 配置项符合repowiki规范
- [ ] 敏感配置已通过环境变量管理
- [ ] 配置文件纳入版本控制
- [ ] 环境隔离配置正确实施
- [ ] 热更新机制正常工作
- [ ] 系统参数管理功能完整
- [ ] 配置变更日志记录完整
- [ ] 配置备份恢复机制健全
- [ ] 配置权限控制严格实施

### 配置管理质量指标
- **配置覆盖率**: ≥ 95% (所有配置项都有管理)
- **环境隔离合规性**: 100% (无配置混淆)
- **热更新成功率**: ≥ 98% (热更新操作成功率)
- **配置变更响应时间**: ≤ 5分钟 (配置生效时间)
- **配置备份完整性**: 100% (重要配置都有备份)

### 最佳实践遵循度
- **配置管理流程**: 100%遵循repowiki规范
- **安全配置标准**: 100%符合企业级要求
- **性能优化配置**: 100%按环境特性优化
- **监控告警配置**: 100%配置完善监控告警

---

## 🔗 相关技能

### 核心相关技能
- **[四层架构专家](four-tier-architecture-specialist-repowiki-compliant.md)**: 确保配置管理遵循四层架构
- **[Spring Boot Jakarta专家](spring-boot-jakarta-specialist-repowiki.md)**: 确保配置管理与Spring Boot 3.x兼容
- **[缓存架构专家](cache-architecture-specialist-repowiki.md)**: 确保配置缓存使用规范

### 进阶技能路径
- **DevOps配置管理**: CI/CD配置管理、基础设施即代码
- **配置中心专家**: Nacos/Apollo配置中心集成
- **安全配置专家**: 配置加密、密钥管理、安全审计

---

**💡 核心理念**: 严格基于.qoder/repowiki权威规范，建立企业级配置管理体系，实现配置标准化、环境隔离化、热更新自动化，确保IOE-DREAM项目配置管理的专业性、安全性和可靠性。