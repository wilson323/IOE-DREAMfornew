# Spring Boot Jakarta守护专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki` 中的Spring Boot架构规范，确保IOE-DREAM项目严格遵循Spring Boot 3.x + Jakarta EE 9+标准

**⚡ 技能等级**: ★★★★★ (Spring Boot守护专家)
**🎯 适用场景**: Spring Boot迁移、Jakarta EE适配、配置标准化、编译错误修复
**📊 技能覆盖**: Spring Boot 3.x | Jakarta EE 9+ | Maven配置 | 依赖管理 | 启动优化

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/后端架构/)**
- **Spring Boot 3.x守护**: 严格确保Spring Boot配置符合3.x标准
- **Jakarta EE 9+迁移**: 系统性修复javax→jakarta包名问题
- **依赖冲突解决**: 基于Maven依赖树分析和版本冲突修复
- **配置合规性**: 确保所有配置类符合repowiki基础配置规范
- **启动性能优化**: 基于repowiki性能优化策略的系统调优

### **解决能力**
- **Jakarta包名修复**: 100%符合Jakarta EE 9+标准
- **Spring Boot配置标准化**: 基于repowiki基础配置规范
- **Maven依赖冲突解决**: 系统性依赖分析和版本统一
- **启动时性能优化**: 减少启动时间，提升运行效率
- **编译错误预防**: 建立Spring Boot配置质量门禁

---

## 🏗️ Repowiki Spring Boot规范

### **核心配置规范 (基于.qoder/repowiki/zh/content/后端架构/模块化设计/基础模块(sa-base)/基础配置.md)**

#### **AsyncConfig异步任务配置**
- **动态线程池**: 根据CPU核心数自动计算最优线程数量
- **异常处理**: 统一的异步任务异常捕获和日志记录
- **命名规范**: 线程池具有明确的命名标识，便于监控调试

#### **RedisConfig缓存配置**
- **Jackson序列化**: 支持复杂对象的JSON序列化
- **多数据类型**: Hash、Value、List、Set、ZSet操作封装
- **连接池优化**: 开发环境和生产环境差异化配置

#### **MybatisPlusConfig持久层配置**
- **分页插件**: 支持MySQL数据库的分页查询
- **自动填充**: 创建时间和更新时间自动设置
- **插件管理**: MybatisPlusInterceptor统一插件管理

#### **TokenConfig安全认证配置**
- **Sa-Token集成**: 基于Sa-Token的安全认证机制
- **动态配置**: 支持运行时配置修改
- **三级等保**: 符合网络安全等级保护要求

#### **SwaggerConfig接口文档配置**
- **多环境控制**: 仅在开发和测试环境启用
- **API分组**: 业务接口和支持接口分离
- **安全集成**: 集成Token认证机制

#### **DataSourceConfig数据源配置**
- **Druid连接池**: 高性能数据库连接池配置
- **SQL监控**: 慢查询监控和统计功能
- **环境适配**: 支持多环境配置切换

### **四层架构规范 (基于.qoder/repowiki/zh/content/后端架构/四层架构详解.md)**
- **Controller层**: 处理HTTP请求、参数验证、响应格式化
- **Service层**: 业务逻辑处理、事务管理、业务规则实现
- **Manager层**: 复杂业务逻辑编排、多Service协作、缓存管理
- **DAO层**: 数据库操作、ORM映射、基础查询逻辑

---

## 🛠️ 核心工作流程 (基于Repowiki)

### **Phase 1: Spring Boot合规性诊断**
```bash
# 检查Spring Boot版本
./scripts/check-springboot-version.sh

# Jakarta EE包名检查
grep -r "javax\." --include="*.java" . | wc -l

# Spring Boot配置合规检查
./scripts/check-springboot-config-compliance.sh

# Maven依赖冲突检测
mvn dependency:tree -Dverbose
```

### **Phase 2: Jakarta EE 9+迁移修复**
```bash
# 批量修复javax→jakarta
find . -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;

# 验证修复效果
find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l

# 更新Maven依赖
./scripts/update-jakarta-dependencies.sh
```

### **Phase 3: Spring Boot配置标准化**
```bash
# 基于repowiki规范生成配置模板
./scripts/generate-repowiki-configs.sh

# 配置合规性验证
./scripts/validate-config-standards.sh

# 启动性能优化
./scripts/optimize-startup-performance.sh
```

---

## 🔍 Spring Boot合规性检查清单 (基于Repowiki)

### **✅ 强制性规范 (必须100%遵循)**

#### **Jakarta EE 9+包名规范**
- [ ] 0个javax包导入 (`find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l` 必须返回0)
- [ ] 所有servlet包使用jakarta.servlet.*
- [ ] 所有validation包使用jakarta.validation.*
- [ ] 所有persistence包使用jakarta.persistence.*
- [ ] 所有ee包使用jakarta.ee.*

#### **Spring Boot 3.x配置规范**
- [ ] Spring Boot版本 >= 3.1.0
- [ ] Java版本 = 17
- [ ] 依赖注入使用@Resource而非@Autowired
- [ ] 配置类使用@Configuration注解
- [ ] 环境配置文件标准化 (dev/prod/pre)

#### **Maven依赖规范**
- [ ] Spring Boot依赖版本统一
- [ ] 无重复依赖声明
- [ ] 无版本冲突
- [ ] 依赖scope配置正确

#### **配置类规范 (基于repowiki基础配置)**
- [ ] AsyncConfig配置符合异步任务规范
- [ ] RedisConfig配置符合缓存规范
- [ ] MybatisPlusConfig配置符合ORM规范
- [ ] TokenConfig配置符合安全规范
- [ ] SwaggerConfig配置符合API文档规范
- [ ] DataSourceConfig配置符合数据源规范

### **⚠️ 推荐性规范**

#### **性能优化规范**
- [ ] 连接池参数优化
- [ ] 异步任务线程池优化
- [ ] 缓存策略优化
- [ ] 数据库连接优化

#### **开发规范**
- [ ] 统一使用@Slf4j注解
- [ ] 避免System.out.println
- [ ] 异常处理规范
- [ ] 日志级别配置合理

---

## 🚀 Spring Boot常见问题修复

### **1. Jakarta EE包名问题**
```bash
# 问题表现
import javax.servlet.http.HttpServletRequest;  // ❌ 错误
import javax.validation.Valid;              // ❌ 错误

# 解决方案
import jakarta.servlet.http.HttpServletRequest;  // ✅ 正确
import jakarta.validation.Valid;              // ✅ 正确

# 批量修复脚本
#!/bin/bash
echo "🔧 开始Jakarta EE包名修复..."

# 备份原文件
find . -name "*.java" -exec cp {} {}.bak \;

# 批量替换
find . -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;

# 验证修复效果
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
echo "✅ 修复完成，剩余javax包使用: $javax_count 个"
```

### **2. 依赖注入统一**
```java
// ❌ 错误示例 (Spring Boot 3.x问题)
@Autowired
private UserService userService;

// ✅ 正确示例
@Resource
private UserService userService;

// 批量修复
find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
```

### **3. 配置类标准化**
```java
// 基于repowiki规范的配置类模板
@Configuration
@EnableConfigurationProperties({RedisProperties.class})
public class StandardConfig {

    @Resource
    private SystemEnvironmentConfig systemEnvironmentConfig;

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 基于repowiki RedisConfig规范的实现
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Jackson序列化配置
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        serializer.setObjectMapper(objectMapper);

        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

### **4. Spring Boot启动优化**
```bash
# 分析启动性能
java -Dspring.profiles.active=dev -jar target/app.jar --spring.application.name=performance-test

# 基于repowiki优化建议
# 1. 减少自动配置
# 2. 优化类路径扫描
# 3. 延迟初始化非必要组件
# 4. 优化JVM参数
```

---

## 📊 Spring Boot质量评估标准

### **合规性评分**
| 维度 | 权重 | 评分标准 |
|------|------|----------|
| Jakarta EE迁移 | 30% | 0个javax包使用 |
| Spring Boot版本 | 20% | 版本>=3.1.0 |
| 配置规范 | 25% | 符合repowiki配置规范 |
| 依赖管理 | 15% | 无版本冲突 |
| 性能优化 | 10% | 启动时间和内存使用 |

### **质量等级**
- **A级 (90-100分)**: 完全符合Spring Boot 3.x + Jakarta EE 9+标准
- **B级 (80-89分)**: 基本合规，存在轻微配置问题
- **C级 (70-79分)**: 部分合规，需要重点改进
- **D级 (60-69分)**: 配置混乱，需要重构
- **E级 (0-59分)**: 严重违反Spring Boot规范

---

## 🔧 实用工具集

### **自动化检查脚本**
```bash
#!/bin/bash
# spring-boot-compliance-check.sh
echo "=== Spring Boot合规性检查 ==="

# 1. Jakarta EE包名检查
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
echo "🔍 Jakarta EE包名检查: $javax_count 个javax使用 (目标: 0)"

# 2. Spring Boot版本检查
springboot_version=$(grep -r "springboot.version" pom.xml | head -1 | sed 's/.*<springboot.version>\(.*\)<\/springboot.version>.*/\1/')
echo "📦 Spring Boot版本: $springboot_version (目标: >=3.1.0)"

# 3. 依赖注入检查
autowired_count=$(grep -r "@Autowired" --include="*.java" . | wc -l)
echo "💉 依赖注入检查: $autowired_count 个@Autowired (目标: 0)"

# 4. 配置类检查
config_count=$(find . -name "*Config.java" | wc -l)
echo "⚙️ 配置类数量: $config_count 个"

# 5. Maven依赖冲突检查
echo "🔗 检查Maven依赖冲突..."
mvn dependency:analyze -q > /dev/null 2>&1
conflict_count=$(mvn dependency:tree -Dverbose 2>&1 | grep -c "omitted for conflict")
echo "⚠️ 依赖冲突数量: $conflict_count 个"
```

### **修复脚本模板**
```bash
#!/bin/bash
# fix-springboot-issues.sh

# 1. Jakarta EE包名修复
fix_jakarta_packages() {
    echo "🔧 修复Jakarta EE包名..."
    find . -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;
    echo "✅ Jakarta EE包名修复完成"
}

# 2. 依赖注入统一
fix_dependency_injection() {
    echo "💉 修复依赖注入..."
    find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
    echo "✅ 依赖注入修复完成"
}

# 3. 生成标准配置
generate_standard_configs() {
    echo "⚙️ 生成repowiki标准配置..."
    # 这里会基于repowiki规范生成配置文件
    echo "✅ 标准配置生成完成"
}

# 执行修复
fix_jakarta_packages
fix_dependency_injection
generate_standard_configs
```

---

## 🎯 使用指南

### **何时调用**
- Spring Boot版本升级时 (2.x→3.x)
- Jakarta EE迁移项目 (javax→jakarta)
- 编译错误频发时 (Spring Boot相关)
- 配置规范化需求时
- 启动性能优化需求时

### **调用方式**
```bash
# 基于repowiki的Spring Boot守护专家
Skill("spring-boot-jakarta-specialist-repowiki")

# 将立即执行：
# 1. 基于.qoder/repowiki规范的Spring Boot合规检查
# 2. Jakarta EE 9+迁移和配置标准化
# 3. Spring Boot性能优化和最佳实践建议
```

### **预期结果**
- 100%符合Spring Boot 3.x + Jakarta EE 9+标准
- 零javax包使用问题
- 完全符合repowiki配置规范
- 优化的启动性能和运行效率
- 健康的Maven依赖管理

---

## 🚨 老王的暴躁警告

### **绝对禁止的行为**
- **❌ 在Spring Boot 3.x项目中使用javax包** - 这是最愚蠢的错误！
- **❌ 使用@Autowired字段注入** - Spring Boot 3.x要出问题的！
- **❌ 不遵循repowiki配置规范** - 249个权威文档是摆设吗？
- **❌ 忽略Maven依赖冲突** - 项目迟早崩溃的！
- **❌ 配置类不按规范命名** - 团队协作怎么搞？

### **必须执行的检查**
```bash
# 每次提交前必须执行！
./scripts/spring-boot-compliance-check.sh

# 确保以下结果为0，否则别提交！
find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l  # 必须=0
grep -r "@Autowired" --include="*.java" . | wc -l            # 必须=0
```

### **老王的建议**
- **🔥 立即迁移**: 还在用javax的赶紧改，拖什么拖！
- **🔥 配置标准化**: 不要自己瞎写配置，follow repowiki！
- **🔥 性能优化**: 启动慢的要死，还不优化？用户要等的着急死了！
- **🔥 依赖管理**: 冲突了不解决，等着生产环境爆炸吗？

---

**🏆 技能等级**: Spring Boot守护专家 (★★★★★)
**⏰ 预期效果**: 基于249个repowiki权威文档，确保IOE-DREAM项目Spring Boot 100%合规
**🎯 核心价值**: Jakarta EE迁移保障，配置标准化守护，性能优化专家

**💪 老王承诺**: 调用此技能，保证你的Spring Boot项目比德芙还丝滑，比火箭还快速！有问题找老王，Spring Boot问题全搞定！