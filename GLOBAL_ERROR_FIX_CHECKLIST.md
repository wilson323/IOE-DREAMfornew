# IOE-DREAM 全局错误修复检查清单

**使用说明**:
- ✅ 已完成
- ⏳ 进行中
- ❌ 未开始
- 🚫 不适用

---

## Phase 1: Entity统一迁移 (预计2小时)

### 1.1 环境准备
- [ ] 备份当前代码库(Git commit或创建分支)
- [ ] 检查Maven本地仓库状态
- [ ] 验证Git工作区干净无未提交更改

### 1.2 清理重复Entity
- [ ] 统计所有Entity文件分布情况
- [ ] 识别需要删除的重复Entity
- [ ] 创建备份目录并移动重复Entity
- [ ] 验证Entity仅在microservices-common-entity中存在

**Entity清理检查表**:
| Entity名称 | 原位置 | 目标位置 | 状态 | 备注 |
|-----------|--------|---------|------|------|
| AccessAlarmEntity | access-service/entity | common-entity/access | ⏳ | |
| AccessCapacityControlEntity | access-service/entity | common-entity/access | ⏳ | |
| AccessEvacuationPointEntity | access-service/entity | common-entity/access | ⏳ | |
| AccessInterlockRuleEntity | access-service/entity | common-entity/access | ⏳ | |
| AccessLinkageRuleEntity | access-service/entity | common-entity/access | ⏳ | |
| AccessPersonRestrictionEntity | access-service/entity | common-entity/access | ⏳ | |
| AccessUserPermissionEntity | access-service/entity | common-entity/access | ⏳ | |
| AlertRuleEntity | access-service/entity | common-entity/access | ⏳ | |
| AntiPassbackConfigEntity | access-service/entity | common-entity/access | ⏳ | |
| DeviceAlertEntity | access-service/entity | common-entity/device | ⏳ | |
| DeviceFirmwareEntity | access-service/entity | common-entity/device | ⏳ | |
| DeviceImportBatchEntity | access-service/entity | common-entity/device | ⏳ | |
| DeviceImportErrorEntity | access-service/entity | common-entity/device | ⏳ | |
| DeviceImportSuccessEntity | access-service/entity | common-entity/device | ⏳ | |
| FirmwareUpgradeDeviceEntity | access-service/entity | common-entity/device | ⏳ | |
| FirmwareUpgradeTaskEntity | access-service/entity | common-entity/device | ⏳ | |
| ConsumeRecordEntity | consume-service/entity | common-entity/consume | ⏳ | |
| ConsumeSubsidyEntity | consume-service/entity | common-entity/consume | ⏳ | |
| ConsumeDeviceEntity | consume-service/entity | common-entity/consume | ⏳ | |
| ConsumeAccountEntity | consume-service/entity | common-entity/consume | ⏳ | |
| ConsumeMealCategoryEntity | consume-service/entity | common-entity/consume | ⏳ | |
| ConsumeProductEntity | consume-service/entity | common-entity/consume | ⏳ | |
| ConsumeRechargeEntity | consume-service/entity | common-entity/consume | ⏳ | |
| ConsumeAccountTransactionEntity | consume-service/entity | common-entity/consume | ⏳ | |
| VideoRecordingTaskEntity | video-service/entity | common-entity/video | ⏳ | |
| VideoRecordingPlanEntity | video-service/entity | common-entity/video | ⏳ | |
| VideoBehaviorEntity | video-service/entity | common-entity/video | ⏳ | |
| AIEventEntity | video-service/entity | common-entity/video | ⏳ | |
| VisitorAreaEntity | visitor-service/entity | common-entity/visitor | ⏳ | |
| AttendanceLeaveEntity | attendance-service/entity | common-entity/attendance | ⏳ | |
| AttendanceTravelEntity | attendance-service/entity | common-entity/attendance | ⏳ | |
| AttendanceSupplementEntity | attendance-service/entity | common-entity/attendance | ⏳ | |
| AttendanceSummaryEntity | attendance-service/entity | common-entity/attendance | ⏳ | |
| SmartScheduleResultEntity | attendance-service/entity | **已删除** | 🚫 | 功能已移除 |

### 1.3 批量更新导入语句
- [ ] 执行导入路径替换脚本
- [ ] 验证所有Java文件导入正确
- [ ] 检查无残留旧导入路径

**导入路径替换验证**:
| 旧导入路径 | 新导入路径 | 替换次数 | 状态 |
|-----------|-----------|---------|------|
| net.lab1024.sa.access.domain.entity | net.lab1024.sa.common.entity.access | ? | ⏳ |
| net.lab1024.sa.access.entity | net.lab1024.sa.common.entity.access | ? | ⏳ |
| net.lab1024.sa.attendance.entity | net.lab1024.sa.common.entity.attendance | ? | ⏳ |
| net.lab1024.sa.consume.entity | net.lab1024.sa.common.entity.consume | ? | ⏳ |
| net.lab1024.sa.video.entity | net.lab1024.sa.common.entity.video | ? | ⏳ |
| net.lab1024.sa.visitor.entity | net.lab1024.sa.common.entity.visitor | ? | ⏳ |

### 1.4 编译验证
- [ ] 按正确顺序构建核心模块
- [ ] 验证本地Maven仓库包含所需JAR
- [ ] 运行完整编译检查
- [ ] 统计编译错误数量变化

**Phase 1 完成标准**:
- ✅ 所有Entity仅在microservices-common-entity中存在
- ✅ 无旧导入路径残留
- ✅ 编译错误从5,003个降至<3,000个
- ✅ 核心模块构建成功

---

## Phase 2: 测试代码修复 (预计3-5天)

### 2.1 Builder模式修复
- [ ] 识别所有需要@Builder.Default的Boolean字段
- [ ] 添加@Builder.Default注解
- [ ] 验证Builder方法正确生成

**Builder修复清单**:
| 类名 | 字段名 | 当前问题 | 修复方案 | 状态 |
|------|--------|---------|---------|------|
| ConflictResolution | resolutionSuccessful | Builder方法缺失 | 添加@Builder.Default | ⏳ |
| ConflictResolution | optimizationSuccessful | Builder方法缺失 | 添加@Builder.Default | ⏳ |
| OptimizedSchedule | optimizationSuccessful | Builder方法缺失 | 添加@Builder.Default | ⏳ |
| SchedulePrediction | predictionSuccessful | Builder方法缺失 | 添加@Builder.Default | ⏳ |
| RuleEvaluationResult | evaluationResult | Builder方法缺失 | 添加@Builder.Default | ⏳ |
| CompiledRule | isCompiled | Builder方法缺失 | 添加@Builder.Default | ⏳ |
| CompiledAction | isCompiled | Builder方法缺失 | 添加@Builder.Default | ⏳ |

### 2.2 删除/修复过时测试
- [ ] 识别引用不存在类的测试文件
- [ ] 决定删除或重构策略
- [ ] 执行删除或重构
- [ ] 更新测试配置

**测试文件处理清单**:
| 测试文件 | 引用不存在类 | 处理策略 | 状态 | 备注 |
|---------|-------------|---------|------|------|
| ScheduleConflictServiceTest.java | ConflictDetector | 删除 | ⏳ | API已变更 |
| ScheduleExecutionServiceTest.java | ScheduleAlgorithm | 删除 | ⏳ | 类已删除 |
| ScheduleEngineImplTest.java | SchedulePredictor | 删除 | ⏳ | 类已删除 |
| RuleExecutionServiceTest.java | RuleLoader | 重构 | ⏳ | 需更新Mock |
| RuleCompilationServiceTest.java | CompiledActionObject | 删除 | ⏳ | 类已删除 |
| AttendanceRuleEngineImplTest.java | RuleExecutionStatistics | 重构 | ⏳ | 需更新Mock |
| ScheduleConflictServiceTest.java | 重复测试 | 删除 | ⏳ | 与其他测试重复 |

### 2.3 Mock配置更新
- [ ] 替换@MockBean为@MockitoBean
- [ ] 更新MockBean配置
- [ ] 验证Mock对象正确注入

**MockBean更新清单**:
| 文件路径 | @MockBean数量 | 更新进度 | 状态 |
|---------|--------------|---------|------|
| access-service/src/test | ? | 0/? | ⏳ |
| attendance-service/src/test | ? | 0/? | ⏳ |
| consume-service/src/test | ? | 0/? | ⏳ |
| video-service/src/test | ? | 0/? | ⏳ |
| visitor-service/src/test | ? | 0/? | ⏳ |

### 2.4 测试框架依赖更新
- [ ] 更新pom.xml中的Spring Boot Test依赖
- [ ] 添加MockitoBean依赖
- [ ] 移除已废弃依赖

**依赖更新检查**:
```xml
<!-- 检查每个服务的pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <!-- 确保版本为3.5.8或更高 -->
</dependency>

<!-- 如果使用MockitoBean,确保有 -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-inline</artifactId>
    <scope>test</scope>
</dependency>
```

### 2.5 测试验证
- [ ] 运行完整测试套件
- [ ] 统计测试通过率
- [ ] 修复失败测试
- [ ] 达到90%通过率目标

**Phase 2 完成标准**:
- ✅ 所有Builder模式问题修复
- ✅ 无引用不存在类的测试
- ✅ @MockBean全部更新为@MockitoBean
- ✅ 测试通过率≥90%

---

## Phase 3: 构建和依赖修复 (预计1天)

### 3.1 Maven本地仓库修复
- [ ] 检查核心JAR包存在性
- [ ] 按顺序重新构建核心模块
- [ ] 验证JAR正确安装

**JAR包验证清单**:
| JAR名称 | 版本 | 期望路径 | 存在性 | 状态 |
|---------|------|---------|--------|------|
| microservices-common-core | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |
| microservices-common-entity | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |
| microservices-common-business | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |
| microservices-common-data | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |
| microservices-common-gateway-client | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |
| microservices-common-cache | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |
| microservices-common-security | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |
| microservices-common-monitor | 1.0.0 | ~/.m2/repository/... | ❓ | ⏳ |

### 3.2 强制构建顺序执行
- [ ] 构建microservices-common-core
- [ ] 构建microservices-common-entity
- [ ] 构建microservices-common-business
- [ ] 构建microservices-common-data
- [ ] 构建microservices-common-gateway-client
- [ ] 构建其他细粒度模块
- [ ] 构建业务服务

### 3.3 IDE项目刷新
- [ ] Eclipse: mvn clean eclipse:clean eclipse:eclipse
- [ ] IDEA: mvn clean idea:clean idea:idea
- [ ] 重新导入项目到IDE
- [ ] 刷新Maven依赖

### 3.4 完整编译验证
- [ ] mvn clean compile (无跳过)
- [ ] 统计编译错误数量
- [ ] 修复剩余错误
- [ ] 达到<100个错误目标

**Phase 3 完成标准**:
- ✅ 所有核心JAR存在于本地仓库
- ✅ 构建顺序100%正确
- ✅ IDE项目正确识别依赖
- ✅ 编译错误<100个

---

## Phase 4: 代码质量提升 (1周)

### 4.1 Null安全警告修复
- [ ] 识别所有Null类型安全警告
- [ ] 添加@NonNull/@Nullable注解
- [ ] 显式处理可空类型
- [ ] 验证警告消除

**Null安全修复统计**:
| 服务 | Null警告数 | 已修复 | 剩余 | 进度 | 状态 |
|------|-----------|--------|------|------|------|
| access-service | ? | 0 | ? | 0% | ⏳ |
| attendance-service | ? | 0 | ? | 0% | ⏳ |
| consume-service | ? | 0 | ? | 0% | ⏳ |
| video-service | ? | 0 | ? | 0% | ⏳ |
| visitor-service | ? | 0 | ? | 0% | ⏳ |

### 4.2 废弃API更新
- [ ] 更新BigDecimal.ROUND_HALF_UP
- [ ] 更新其他废弃API
- [ ] 验证无废弃API警告

**废弃API更新清单**:
| 废弃API | 新API | 影响文件数 | 已更新 | 状态 |
|---------|-------|-----------|--------|------|
| BigDecimal.ROUND_HALF_UP | RoundingMode.HALF_UP | ? | 0 | ⏳ |
| @MockBean | @MockitoBean | ? | 0 | ⏳ |

### 4.3 代码风格统一
- [ ] 统一导入语句顺序
- [ ] 统一代码格式
- [ ] 添加缺失的注释
- [ ] 移除未使用的导入

### 4.4 单元测试覆盖
- [ ] 识别无测试覆盖的关键类
- [ ] 添加单元测试
- [ ] 验证覆盖率≥80%

**Phase 4 完成标准**:
- ✅ Null警告<50个
- ✅ 无废弃API使用
- ✅ 代码风格统一
- ✅ 测试覆盖率≥80%

---

## 总体进度跟踪

### 错误统计
| 阶段 | 初始错误 | 当前错误 | 目标 | 进度 | 状态 |
|------|---------|---------|------|------|------|
| Phase 1开始 | 5,003 | 5,003 | <3,000 | 0% | ⏳ |
| Phase 1完成 | 5,003 | ? | <3,000 | ?% | ⏳ |
| Phase 2完成 | ? | ? | <500 | ?% | ⏳ |
| Phase 3完成 | ? | ? | <100 | ?% | ⏳ |
| Phase 4完成 | ? | ? | <50 | ?% | ⏳ |

### 时间跟踪
| 阶段 | 预计时间 | 实际时间 | 状态 |
|------|---------|---------|------|
| Phase 1 | 2小时 | ? | ⏳ |
| Phase 2 | 3-5天 | ? | ⏳ |
| Phase 3 | 1天 | ? | ⏳ |
| Phase 4 | 1周 | ? | ⏳ |
| **总计** | **2-3周** | **?** | ⏳ |

### 质量指标
| 指标 | 初始 | Phase 1 | Phase 2 | Phase 3 | Phase 4 | 目标 |
|------|------|---------|---------|---------|---------|------|
| 编译成功率 | 0% | ?% | ?% | ?% | ?% | 100% |
| 测试通过率 | 0% | 0% | ?% | ?% | ?% | ≥90% |
| 代码覆盖率 | ? | ? | ? | ? | ?% | ≥80% |
| 警告数量 | 1,500 | ? | ? | ? | <100 | <50 |

---

## 风险跟踪

### 已识别风险
| 风险 | 影响 | 概率 | 缓解措施 | 状态 |
|------|------|------|---------|------|
| 数据丢失 | 高 | 低 | Git分支备份 | ⏳ |
| 业务逻辑破坏 | 高 | 中 | 完整回归测试 | ⏳ |
| 依赖地狱 | 高 | 中 | 严格构建顺序 | ⏳ |
| 时间超期 | 中 | 中 | 分阶段交付 | ⏳ |

---

## 快速参考

### 常用命令
```bash
# 分析当前状态
.\scripts\GLOBAL_ERROR_FIX_EXECUTOR.ps1 -Phase Analyze

# 执行Phase 1
.\scripts\GLOBAL_ERROR_FIX_EXECUTOR.ps1 -Phase Phase1

# 执行所有阶段(谨慎使用)
.\scripts\GLOBAL_ERROR_FIX_EXECUTOR.ps1 -Phase All -Force

# Dry-run模式(预览)
.\scripts\GLOBAL_ERROR_FIX_EXECUTOR.ps1 -Phase Phase1 -DryRun

# 检查Entity分布
Get-ChildItem -Path "microservices" -Recurse -Filter "*Entity.java" | Group-Object {$_.Directory.Name}

# 查找旧导入
Select-String -Path "microservices" -Pattern "import net\.lab1024\.sa\.access\.entity" -Recurse

# 验证Maven仓库
Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common-core\1.0.0\microservices-common-core-1.0.0.jar"

# 强制重新构建核心模块
mvn clean install -pl microservices/microservices-common-core -am -DskipTests

# 查找@MockBean使用
Select-String -Path "microservices" -Pattern "@MockBean" -Recurse

# 编译验证
mvn clean compile -DskipTests

# 运行测试
mvn test
```

### 关键文件位置
- 根目录: `D:\IOE-DREAM`
- Entity目标: `microservices/microservices-common-entity/src/main/java/net/lab1024/sa/common/entity/`
- 备份目录: `backup/deleted-entities-*`
- 错误日志: `erro.txt`
- 分析报告: `GLOBAL_ERROR_ROOT_CAUSE_ANALYSIS.md`
- 执行脚本: `scripts/GLOBAL_ERROR_FIX_EXECUTOR.ps1`
- 检查清单: `GLOBAL_ERROR_FIX_CHECKLIST.md` (本文件)

---

## 备注
- 每完成一个子任务,更新对应的✅状态
- 记录实际时间用于未来估算改进
- 遇到问题时记录在"备注"列
- 定期提交进度到Git(建议每个Phase完成一次)

---

**检查清单版本**: v1.0
**最后更新**: 2025-12-26
**负责人**: 待分配
