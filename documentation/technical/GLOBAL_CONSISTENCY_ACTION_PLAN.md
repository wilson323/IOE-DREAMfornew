# IOE-DREAM 全局一致性行动方案

> **版本**: v1.0  
> **创建时间**: 2025-11-20  
> **基准规范**: `.qoder/repowiki`  
> **目标**: 确保全局一致性，消除冗余，严格遵循repowiki规范

---

## 🎯 执行原则

### 核心原则
1. **repowiki规范优先** - 所有修改必须严格遵循 `.qoder/repowiki` 规范
2. **全局一致性** - 确保代码、架构、命名规范全局统一
3. **消除冗余** - 识别并清理重复代码和架构体系
4. **渐进式修复** - 按优先级分阶段执行，确保不影响现有功能

### 修复策略
- **系统性修复** - 批量修复同类问题，避免遗漏
- **验证驱动** - 每个阶段完成后进行编译验证
- **文档同步** - 修复过程中同步更新相关文档

---

## 📋 执行阶段规划

### 阶段一: 紧急修复（P0 - 阻塞问题）

**目标**: 解决编译阻塞问题，恢复项目可编译状态  
**预计时间**: 2-3天  
**验收标准**: 编译错误减少至 <100个

#### 任务1.1: 包结构补全

**操作清单**:
```bash
# 1. 创建 consume 模块标准包结构
cd smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume

# 创建缺失的包
mkdir -p dao
mkdir -p domain/dto
mkdir -p domain/entity  # 如缺失则创建
mkdir -p manager        # 补全缺失的Manager实现
```

**具体文件**:
- [ ] 创建 `dao/ConsumeRecordDao.java`
- [ ] 创建 `dao/ProductDao.java`（如缺失）
- [ ] 创建 `domain/dto/ConsumeRequestDTO.java`
- [ ] 创建 `domain/dto/ConsumeResultDTO.java`
- [ ] 创建 `domain/dto/ConsumeValidationResult.java`
- [ ] 创建 `domain/entity/ProductEntity.java`（如缺失）
- [ ] 创建 `domain/enums/ConsumeModeEnum.java`
- [ ] 创建 `domain/enums/ConsumeStatusEnum.java`

**repowiki规范遵循**:
- 遵循 [实体类设计规范](.qoder/repowiki/zh/content/后端架构/数据模型与ORM/实体类设计规范/实体类设计规范.md)
- 遵循 [DAO层设计规范](.qoder/repowiki/zh/content/后端架构/数据模型与ORM/DAO层与Mapper映射/DAO接口设计.md)
- 遵循 [DTO设计规范](.qoder/repowiki/zh/content/数据库设计/数据模型与ORM/数据传输对象/数据传输对象.md)

#### 任务1.2: 类型定义补全

**操作清单**:
- [ ] 修复 `MeteringUnitEnum` 重复构造函数问题
- [ ] 创建缺失的枚举类: `ProductStatusEnum`, `OrderingStatusEnum`
- [ ] 创建缺失的配置类: `ConsumeModeConfig`
- [ ] 统一缓存服务接口定义

**修复文件**:
- `MeteringUnitEnum.java` - 删除重复构造函数
- `FreeAmountConsumeEngine.java` - 修复类型引用
- `OrderingConsumeEngine.java` - 修复类型引用

#### 任务1.3: 方法签名修复

**操作清单**:
- [ ] 修复 `ConsumeCacheService` 方法签名
  - `getTodayConsumeAmount(Long)` - 如缺失则添加
  - `getCachedValue(String, Class<T>)` - 修复泛型支持
- [ ] 修复 `SmartPageUtil.convert2PageResult` 方法签名
- [ ] 修复 `PageResult.of` 方法签名

**修复文件**:
- `ConsumeCacheService.java` - 补全缺失方法
- `SmartPageUtil.java` - 修复方法签名
- `PageResult.java` - 修复泛型支持

---

### 阶段二: 架构规范化（P0 - 架构违规）

**目标**: 修复四层架构违规，确保架构符合repowiki规范  
**预计时间**: 1-2天  
**验收标准**: 无架构违规，四层架构清晰

#### 任务2.1: Engine类架构修复

**问题**: Engine类直接注入DAO，违反四层架构

**修复方案**:
```java
// ❌ 违规代码
@Resource
private ProductDao productDao;

// ✅ 规范代码
@Resource
private ConsumeService consumeService;
```

**修复文件**:
- [ ] `OrderingConsumeEngine.java` - 移除DAO注入，通过Service访问
- [ ] `FreeAmountConsumeEngine.java` - 统一架构层次
- [ ] `MeteringConsumeEngine.java` - 统一架构层次
- [ ] 其他Engine实现类 - 统一检查修复

**repowiki规范遵循**:
- 遵循 [四层架构详解](.qoder/repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md)
- 遵循 [Service层规范](.qoder/repowiki/zh/content/后端架构/四层架构详解/Service层/Service层.md)

#### 任务2.2: Manager层补全

**操作清单**:
- [ ] 创建 `ConsumeManager.java` - 消费业务管理器
- [ ] 创建 `VideoManager.java` - 视频业务管理器
- [ ] 创建 `PermissionManager.java` - 权限业务管理器
- [ ] 确保Manager层职责清晰

**Manager层职责**:
- 跨模块业务协调
- 复杂业务逻辑封装
- 事务边界管理

**repowiki规范遵循**:
- 遵循 [Manager层规范](.qoder/repowiki/zh/content/后端架构/四层架构详解/Manager层.md)

---

### 阶段三: 代码清理（P1 - 重要问题）

**目标**: 清理冗余代码和架构体系  
**预计时间**: 2-3天  
**验收标准**: 无重复架构，代码行数减少 ~30%

#### 任务3.1: 架构体系统一

**问题**: 存在 `ConsumptionModeEngine` 和 `ConsumeModeEngine` 两套体系

**决策**: 保留 `ConsumptionModeEngine` 体系（已在Controller使用）

**操作清单**:
- [ ] 删除 `ConsumeModeEngine` 接口（如存在）
- [ ] 删除 `OrderingConsumeEngine` 等基于 `ConsumeModeEngine` 的实现
- [ ] 迁移业务逻辑至 `ConsumptionModeEngine` 体系
- [ ] 更新所有引用

**删除文件清单**:
- `consume/engine/ConsumeModeEngine.java`（如存在）
- `consume/engine/impl/OrderingConsumeEngine.java`
- `consume/engine/impl/FreeAmountConsumeEngine.java`
- 其他基于 `ConsumeModeEngine` 的实现类

**迁移清单**:
- 将 `ConsumeRequestDTO` / `ConsumeResultDTO` 逻辑迁移至 `ConsumeRequest` / `ConsumeResult`
- 统一使用 `ConsumptionMode` 接口体系

#### 任务3.2: 重复代码清理

**操作清单**:
- [ ] 修复 `MeteringUnitEnum` 重复构造函数
- [ ] 统一缓存服务实现（`ConsumeCacheService` vs `BaseCacheManager`）
- [ ] 清理重复的业务逻辑

---

### 阶段四: TODO实现（P1 - 重要功能）

**目标**: 实现核心业务逻辑TODO项  
**预计时间**: 3-5天  
**验收标准**: P0级TODO全部实现

#### 任务4.1: P0级TODO实现

**优先级**: 核心业务功能

**TODO项清单**:
- [ ] `ResourcePermissionInterceptor.java:127` - 从Sa-Token获取登录用户信息
- [ ] `ResourcePermissionInterceptor.java:171` - 查询用户角色
- [ ] `ResourcePermissionInterceptor.java:187` - 查询用户区域权限
- [ ] `ResourcePermissionService.java:373` - 验证权限配置完整性

**实现规范**:
- 遵循 [认证与授权规范](.qoder/repowiki/zh/content/安全体系/认证与授权/认证与授权.md)
- 遵循 [权限校验规范](.qoder/repowiki/zh/content/安全体系/认证与授权/权限校验/权限校验.md)

#### 任务4.2: P1级TODO实现

**优先级**: 业务功能

**TODO项清单**:
- [ ] `VideoAnalyticsServiceImpl.java` - 视频分析相关TODO（20个）
- [ ] `FreeAmountConsumeEngine.java` - 消费引擎配置TODO（5个）
- [ ] 其他业务模块TODO（15个）

---

### 阶段五: 规范体系建设（P2 - 长期优化）

**目标**: 建立自动化规范检查和持续改进机制  
**预计时间**: 持续改进  
**验收标准**: 自动化检查覆盖所有规范项

#### 任务5.1: 自动化规范检查

**操作清单**:
- [x] 创建编译错误检查脚本 ✅
- [x] 创建架构违规检查脚本 ✅
  - ✅ 创建Bash版本：scripts/architecture-compliance-check.sh
  - ✅ 创建PowerShell版本：scripts/architecture-compliance-check.ps1
  - ✅ 检查项：Controller/Service/Engine层DAO访问、依赖注入规范、命名规范、冗余文件、重复类定义、编码问题
  - ✅ 自动生成检查报告
- [x] 创建命名规范检查脚本 ✅（已包含在架构检查脚本中）
- [ ] 集成到CI/CD流程（待执行）

#### 任务5.2: 文档完善

**操作清单**:
- [ ] 更新repowiki规范文档
- [ ] 创建规范遵循检查清单
- [ ] 建立代码审查标准

---

## 📊 执行进度跟踪

### 阶段一: 紧急修复（P0）
- [ ] 任务1.1: 包结构补全
- [ ] 任务1.2: 类型定义补全
- [ ] 任务1.3: 方法签名修复

### 阶段二: 架构规范化（P0）
- [ ] 任务2.1: Engine类架构修复
- [ ] 任务2.2: Manager层补全

### 阶段三: 代码清理（P1）
- [ ] 任务3.1: 架构体系统一
- [ ] 任务3.2: 重复代码清理

### 阶段四: TODO实现（P1）
- [ ] 任务4.1: P0级TODO实现
- [ ] 任务4.2: P1级TODO实现

### 阶段五: 规范体系建设（P2）
- [x] 任务5.1: 自动化规范检查 ✅
  - ✅ 创建架构违规检查脚本（Bash + PowerShell版本）
  - ✅ 创建使用指南文档
- [ ] 任务5.2: 文档完善（部分完成）

---

## 🔍 验证检查清单

### 编译验证
- [ ] 执行 `mvn clean compile` - 编译错误 <100
- [ ] 执行 `mvn test` - 单元测试通过
- [ ] 检查 `Untitled-1.js` - 错误数量大幅减少

### 架构验证
- [ ] 无Engine类直接访问DAO
- [ ] 四层架构清晰完整
- [ ] 包结构符合repowiki规范

### 规范验证
- [ ] 命名规范统一
- [ ] 代码无重复
- [ ] 符合repowiki所有规范要求

---

## 📝 执行记录

### 2025-11-20
- ✅ 创建全局项目深度分析报告
- ✅ 创建全局一致性行动方案
- ⏳ 待执行: 阶段一紧急修复

---

## 🔗 相关文档

- [全局项目深度分析报告](GLOBAL_PROJECT_DEEP_ANALYSIS.md)
- [repowiki规范体系](.qoder/repowiki/zh/content/)
- [项目系统优化计划](PROJECT_SYSTEM_OPTIMIZATION_PLAN.md)

---

**最后更新**: 2025-11-20  
**维护人**: SmartAdmin规范治理委员会

