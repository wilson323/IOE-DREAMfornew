# Change: Optimize Memory and Resource Usage

## Why

当前项目启动全部9个微服务时内存占用高达15-20GB，严重影响开发体验和资源利用率。根据深度分析，内存占用过高的根本原因包括：

1. **架构层面（60%）**：微服务架构的内存放大效应，9个独立JVM进程，每个都加载完整的Spring Boot框架和公共库
2. **配置层面（25%）**：开发环境使用了生产环境的内存配置（Xms1g-Xmx2g），启动时即分配最大内存
3. **代码层面（10%）**：类加载过多，所有服务都扫描了整个`net.lab1024.sa.common`包，加载了不需要的模块
4. **基础设施层面（5%）**：Docker容器和基础设施的内存开销

**目标**：在不减少业务功能的前提下，通过架构优化、配置优化和代码优化，将内存占用从15-20GB降低至8-12GB（节省40-50%）。

## What Changes

### P0 - 立即执行（快速见效，节省30-40%内存）

1. **JVM内存配置优化**
   - 开发环境降低内存配置：gateway/common从1GB降至512MB，其他服务从2GB降至1GB，video-service从4GB降至2GB
   - 使用较小的-Xms，让JVM动态增长
   - 添加内存优化参数：UseStringDeduplication、MaxMetaspaceSize等

2. **类加载优化**
   - 精确配置scanBasePackages，只扫描需要的公共包
   - 排除不需要的自动配置类
   - 使用@ConditionalOnProperty控制组件加载

### P1 - 短期执行（额外节省20-30%内存）

3. **公共库模块化拆分优化**
   - 确保服务按需依赖公共库模块（core/security/business/monitor）
   - 将业务专属代码（workflow/consume/visitor）从common迁移到对应服务
   - 优化scanBasePackages配置，只扫描实际使用的模块

### P2 - 长期考虑（额外节省30-50%内存）

4. **GraalVM Native Image评估**
   - 先在一个服务试点（gateway-service）
   - 验证稳定性和兼容性
   - 逐步推广到其他服务

## Impact

- **Affected specs**: 
  - `microservices-architecture` - 内存配置和模块依赖规范
  - `performance-optimization` (新增) - 性能优化规范

- **Affected code**:
  - `microservices/*/src/main/resources/bootstrap.yml` - JVM内存配置
  - `microservices/*/src/main/java/*/XxxServiceApplication.java` - 扫描包配置
  - `microservices/pom.xml` - 公共库依赖配置
  - `microservices/*/pom.xml` - 服务依赖配置

- **Breaking changes**: 
  - **无** - 所有优化都是配置层面的，不改变API和业务逻辑
  - 开发环境内存配置降低，但功能完全保留

- **Migration**: 
  - 无需迁移，配置优化自动生效
  - 生产环境保持当前配置（通过Profile区分）

## Success Criteria

- ✅ 开发环境内存占用从15-20GB降至8-12GB
- ✅ 所有业务功能正常工作（无功能缺失）
- ✅ 启动时间不显著增加（<10%）
- ✅ 运行时性能不下降（保持或提升）
- ✅ 生产环境配置不受影响（通过Profile隔离）
