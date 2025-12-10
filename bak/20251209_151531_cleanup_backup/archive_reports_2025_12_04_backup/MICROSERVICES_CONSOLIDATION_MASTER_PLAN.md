# 微服务整合主计划 - 功能完整性优先

**创建时间**: 2025-12-02 19:32  
**核心原则**: ⚠️ **禁止在功能100%迁移验证前删除任何服务**  
**执行状态**: 📋 规划阶段

---

## 🎯 整合目标架构（基于CLAUDE.md）

### 7个核心微服务架构

| 序号 | 目标服务 | 整合来源 | 端口 | 状态 |
|------|---------|---------|------|------|
| 1 | **ioedream-common-service** | auth + identity + notification + audit + monitor + system + scheduler | 8088 | 🟡 待整合 |
| 2 | **ioedream-device-comm-service** | device-service | 8087 | 🟡 待整合 |
| 3 | **ioedream-oa-service** | enterprise + infrastructure | 8089 | 🟡 待整合 |
| 4 | ioedream-access-service | (保留) | 8090 | ✅ 已存在 |
| 5 | ioedream-attendance-service | (保留) | 8091 | ✅ 已存在 |
| 6 | ioedream-video-service | (保留) | 8092 | ✅ 已存在 |
| 7 | ioedream-consume-service | (保留) | 8094 | ✅ 已存在 |
| 8 | ioedream-visitor-service | (保留) | 8095 | ✅ 已存在 |

### 需要废弃的服务（共12个）

| 待废弃服务 | 整合到 | 标记状态 | 功能扫描 | 迁移状态 |
|-----------|--------|---------|---------|---------|
| ioedream-auth-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-identity-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-notification-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-audit-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-monitor-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-system-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-scheduler-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-config-service | common-service | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-device-service | device-comm | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-enterprise-service | oa-service | 🟢 未标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-infrastructure-service | oa-service | 🟢 未标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-integration-service | 各业务服务 | 🟢 未标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| ioedream-report-service | 各业务服务 | 🟢 未标记 | ⏳ 待扫描 | ⏳ 待迁移 |
| analytics (legacy) | 废弃 | 🔴 已标记 | ⏳ 待扫描 | ⏳ 待迁移 |

---

## 📋 功能迁移工作流程

### 阶段0：准备工作 ✅
- [x] 创建主计划文档
- [x] 定义功能完整性标准
- [x] 制定验证检查清单

### 阶段1：功能扫描（针对每个待废弃服务）

**对于每个服务，必须完成以下任务**:

#### 1.1 功能清单扫描
```
扫描目标: <服务名>
- [ ] 列出所有Controller和API端点
- [ ] 列出所有Service业务逻辑
- [ ] 列出所有Entity数据模型
- [ ] 列出所有Manager复杂逻辑
- [ ] 列出所有Dao数据访问
- [ ] 列出所有配置和常量
- [ ] 列出所有工具类和帮助类
```

#### 1.2 功能分类
```
功能类型分类:
- 核心业务功能（必须迁移）
- 辅助功能（必须迁移）
- 配置和常量（必须迁移）
- 重复功能（对比后选择保留）
- 废弃功能（可以丢弃）
```

#### 1.3 依赖关系分析
```
依赖分析:
- [ ] 列出所有外部依赖
- [ ] 列出服务间调用关系
- [ ] 列出数据库表依赖
- [ ] 列出Redis/MQ依赖
```

### 阶段2：功能对比（针对每个功能项）

**对比检查清单**:
```
功能名称: <具体功能>
源服务: <原服务名>
目标服务: <目标服务名>

对比结果:
[ ] 目标服务已有完全相同功能 → 跳过迁移
[ ] 目标服务有部分功能 → 补充缺失部分
[ ] 目标服务完全没有 → 完整迁移
[ ] 功能重复但实现不同 → 评估选择最优实现

记录:
- 功能描述: <详细描述>
- 代码位置: <包名.类名.方法名>
- 迁移决策: <保留/迁移/合并/废弃>
- 迁移理由: <详细说明>
```

### 阶段3：功能迁移（针对每个需要迁移的功能）

**迁移执行清单**:
```
功能: <功能名>
源文件: <源路径>
目标文件: <目标路径>

迁移步骤:
[ ] 1. 复制源代码到目标位置
[ ] 2. 调整包名和import
[ ] 3. 修复依赖引用
[ ] 4. 符合目标服务架构规范
[ ] 5. 添加必要的注释和文档
[ ] 6. 代码格式化和规范检查

依赖处理:
[ ] 迁移相关Entity
[ ] 迁移相关DTO
[ ] 迁移相关VO
[ ] 迁移相关Mapper
[ ] 迁移相关配置

验证步骤:
[ ] 编译通过
[ ] 单元测试通过
[ ] 集成测试通过
[ ] API接口测试通过
```

### 阶段4：功能验证（100%完整性验证）

**验证检查清单**:
```
服务名: <待废弃服务>

功能验证:
[ ] 所有API端点在新服务中可访问
[ ] 所有业务逻辑在新服务中可执行
[ ] 所有数据模型在新服务中可使用
[ ] 所有配置在新服务中可用
[ ] 所有工具类在新服务中可调用

接口兼容性:
[ ] 请求参数兼容
[ ] 响应格式兼容
[ ] 错误码兼容
[ ] 业务逻辑兼容

数据兼容性:
[ ] 数据库表结构兼容
[ ] 数据迁移脚本准备
[ ] 缓存键兼容
[ ] 消息队列兼容

测试验证:
[ ] 单元测试覆盖率≥80%
[ ] 集成测试全部通过
[ ] 压力测试性能达标
[ ] 回归测试无问题

文档更新:
[ ] API文档更新
[ ] 部署文档更新
[ ] 配置文档更新
[ ] 迁移说明文档
```

### 阶段5：服务清理（仅在100%验证后执行）

**清理前最终确认**:
```
⚠️ 删除服务最终确认清单

服务名: <待删除服务>

□ 已完成功能扫描
□ 已完成功能对比
□ 已完成功能迁移
□ 已完成功能验证
□ 已完成测试验证
□ 已完成文档更新
□ 已通过团队评审
□ 已进行生产环境灰度测试

最终签字:
- 架构师签字: ___________
- 开发负责人签字: ___________
- 测试负责人签字: ___________
- 运维负责人签字: ___________

⚠️ 只有所有□都打✓且所有人签字后才可删除
```

**清理操作**:
```bash
# 1. 创建备份（强制）
git tag backup/<service-name>-$(date +%Y%m%d)

# 2. 移动到archive目录（不是直接删除）
mv microservices/<service-name> microservices/archive/<service-name>

# 3. 添加废弃说明
echo "已迁移到: <target-service>" > microservices/archive/<service-name>/MIGRATION_NOTICE.md

# 4. 更新父pom.xml
# 注释掉但不删除module配置

# 5. 记录到迁移日志
echo "<service-name> migrated to <target-service> on $(date)" >> MIGRATION_LOG.md
```

---

## 📊 整合计划详细清单

### 优先级1: ioedream-common-service整合（7个服务）

#### 1.1 ioedream-auth-service → common-service

**功能扫描任务**:
```
[ ] 扫描Controller: 列出所有认证授权API
[ ] 扫描Service: 列出所有认证授权业务逻辑
[ ] 扫描Entity: 列出所有认证相关实体
[ ] 扫描Security配置: JWT、OAuth2等
[ ] 扫描Filter/Interceptor: 认证拦截器
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] SecurityManager - 安全管理
[ ] UserEntity - 用户实体
[ ] RoleEntity - 角色实体
[ ] PermissionEntity - 权限实体
[ ] UserDao/RoleDao - 数据访问

待验证功能:
[ ] JWT Token生成和验证
[ ] OAuth2集成
[ ] Session管理
[ ] 单点登录SSO
[ ] 密码加密和验证
[ ] 权限验证注解
[ ] 认证过滤器
```

**迁移任务**:
```
迁移优先级P0（核心功能）:
[ ] TokenService - JWT生成验证
[ ] AuthenticationFilter - 认证过滤器
[ ] PasswordEncoder - 密码加密

迁移优先级P1（重要功能）:
[ ] SessionManager - 会话管理
[ ] OAuthService - OAuth2集成
[ ] SsoService - 单点登录

迁移优先级P2（辅助功能）:
[ ] 认证配置类
[ ] 认证工具类
[ ] 认证异常类
```

---

#### 1.2 ioedream-identity-service → common-service

**功能扫描任务**:
```
[ ] 扫描用户管理API
[ ] 扫描组织架构API
[ ] 扫描人员管理API
[ ] 扫描部门管理API
[ ] 扫描角色权限API
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] PersonEntity/PersonDao/PersonManager
[ ] DepartmentEntity/DepartmentDao
[ ] AreaEntity/AreaDao/AreaManager
[ ] UserEntity/RoleEntity相关

待验证功能:
[ ] 用户CRUD完整实现
[ ] 组织架构树形结构
[ ] 人员调动流程
[ ] 部门合并拆分
[ ] 角色权限分配
```

---

#### 1.3 ioedream-notification-service → common-service

**功能扫描任务**:
```
[ ] 扫描通知发送API
[ ] 扫描通知模板管理
[ ] 扫描通知历史查询
[ ] 扫描通知配置
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] NotificationService接口
[ ] NotificationSendDTO

待验证功能:
[ ] 邮件通知发送
[ ] 短信通知发送
[ ] 站内消息通知
[ ] 推送通知
[ ] 通知模板引擎
[ ] 批量通知发送
[ ] 通知失败重试
```

---

#### 1.4 ioedream-audit-service → common-service

**功能扫描任务**:
```
[ ] 扫描审计日志API
[ ] 扫描日志查询API
[ ] 扫描日志统计API
[ ] 扫描日志导出功能
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] AuditLogEntity (已完整)
[ ] AuditLogDao
[ ] AuditLogService/AuditLogServiceImpl

待验证功能:
[ ] 完整的审计日志记录功能
[ ] 审计日志查询和过滤
[ ] 审计日志统计分析
[ ] 审计日志导出
[ ] 敏感操作审计
[ ] 合规性审计报告
```

---

#### 1.5 ioedream-monitor-service → common-service

**功能扫描任务**:
```
[ ] 扫描系统监控API
[ ] 扫描性能指标采集
[ ] 扫描健康检查API
[ ] 扫描告警配置
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] HealthCheckController
[ ] SystemHealthVO

待验证功能:
[ ] JVM监控
[ ] 数据库连接池监控
[ ] Redis监控
[ ] 接口性能监控
[ ] 业务指标监控
[ ] 告警规则配置
[ ] 告警通知发送
```

---

#### 1.6 ioedream-system-service → common-service

**功能扫描任务**:
```
[ ] 扫描系统配置API
[ ] 扫描字典管理API
[ ] 扫描文件管理API
[ ] 扫描日志管理API
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] ConfigEntity/ConfigDao
[ ] DictDataEntity/DictDao
[ ] FileEntity/FileDao

待验证功能:
[ ] 系统参数配置CRUD
[ ] 字典数据CRUD
[ ] 文件上传下载
[ ] 文件预览
[ ] 系统日志管理
```

---

#### 1.7 ioedream-scheduler-service → common-service

**功能扫描任务**:
```
[ ] 扫描定时任务API
[ ] 扫描任务调度配置
[ ] 扫描任务执行历史
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] (待验证)

待验证功能:
[ ] 定时任务配置
[ ] Cron表达式解析
[ ] 任务执行调度
[ ] 任务执行日志
[ ] 任务失败重试
[ ] 任务依赖管理
```

---

#### 1.8 ioedream-config-service → common-service

**功能扫描任务**:
```
[ ] 扫描Nacos配置集成
[ ] 扫描配置刷新API
[ ] 扫描配置版本管理
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] ConfigEntity/ConfigDao完整实现

待验证功能:
[ ] Nacos配置同步
[ ] 配置动态刷新
[ ] 配置版本回滚
[ ] 配置加密解密
```

---

### 优先级2: ioedream-device-comm-service整合（1个服务）

#### 2.1 ioedream-device-service → device-comm-service

**功能扫描任务**:
```
[ ] 扫描设备管理API
[ ] 扫描设备通讯协议
[ ] 扫描设备状态监控
[ ] 扫描设备命令下发
```

**功能对比任务**:
```
microservices-common已有功能:
[ ] DeviceEntity完整实现
[ ] CommonDeviceService
[ ] DeviceManager
[ ] 设备配置类(Access/Attendance/Consume/Video)

device-comm-service已有功能:
[ ] (待扫描)

待验证功能:
[ ] 设备协议适配器
[ ] 设备连接管理
[ ] 设备数据采集
[ ] 设备命令下发
[ ] 设备心跳监控
[ ] 设备故障告警
```

---

### 优先级3: ioedream-oa-service整合（2个服务）

#### 3.1 ioedream-enterprise-service → oa-service

**功能扫描任务**:
```
[ ] 扫描企业信息管理
[ ] 扫描组织架构管理
[ ] 扫描审批流程
[ ] 扫描文档管理
```

#### 3.2 ioedream-infrastructure-service → oa-service

**功能扫描任务**:
```
[ ] 扫描基础设施管理
[ ] 扫描资产管理
[ ] 扫描运维工具
```

---

### 优先级4: 特殊服务处理（2个服务）

#### 4.1 ioedream-integration-service → 拆分到各业务服务

**功能扫描任务**:
```
[ ] 扫描第三方集成功能
[ ] 按业务域分类集成功能
[ ] 规划拆分目标
```

#### 4.2 ioedream-report-service → 拆分到各业务服务

**功能扫描任务**:
```
[ ] 扫描报表生成功能
[ ] 按业务域分类报表
[ ] 规划拆分目标
```

---

## 🔍 功能扫描模板

### 使用方法
```powershell
# 对每个服务执行功能扫描
cd D:\IOE-DREAM\microservices\<service-name>

# 1. 列出所有Java文件
Get-ChildItem -Recurse -Filter "*.java" | Select-Object FullName > files.txt

# 2. 统计功能
$controllers = (Get-ChildItem -Recurse -Filter "*Controller.java").Count
$services = (Get-ChildItem -Recurse -Filter "*Service*.java").Count
$entities = (Get-ChildItem -Recurse -Filter "*Entity.java").Count
$daos = (Get-ChildItem -Recurse -Filter "*Dao.java").Count

Write-Host "Controllers: $controllers"
Write-Host "Services: $services"
Write-Host "Entities: $entities"
Write-Host "Daos: $daos"
```

---

## 📈 整合进度跟踪表

| 阶段 | 服务名 | 功能扫描 | 功能对比 | 功能迁移 | 功能验证 | 服务清理 | 状态 |
|------|--------|---------|---------|---------|---------|---------|------|
| P1 | auth-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P1 | identity-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P1 | notification-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P1 | audit-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P1 | monitor-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P1 | system-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P1 | scheduler-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P1 | config-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P2 | device-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P3 | enterprise-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P3 | infrastructure-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P4 | integration-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P4 | report-service | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |
| P4 | analytics (legacy) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 📋 待开始 |

---

## 🚨 风险控制措施

### 风险1: 功能遗漏
**预防措施**:
- ✅ 使用自动化脚本扫描所有代码文件
- ✅ 多人交叉检查
- ✅ 建立功能清单对照表
- ✅ 逐项验证和测试

### 风险2: 接口不兼容
**预防措施**:
- ✅ 保持API接口完全兼容
- ✅ 使用API版本控制
- ✅ 提供兼容性适配层
- ✅ 灰度发布验证

### 风险3: 数据丢失
**预防措施**:
- ✅ 迁移前完整备份
- ✅ 数据迁移脚本测试
- ✅ 数据一致性验证
- ✅ 支持快速回滚

### 风险4: 性能下降
**预防措施**:
- ✅ 迁移后性能测试
- ✅ 对比基准性能指标
- ✅ 优化慢查询
- ✅ 缓存策略优化

---

## 📝 执行标准

### 必须遵循的原则

1. ⚠️ **功能完整性第一** - 100%确认功能完整才可删除
2. ⚠️ **测试验证必须** - 所有迁移功能必须测试通过
3. ⚠️ **文档同步更新** - 迁移过程必须更新文档
4. ⚠️ **可回滚设计** - 保留回滚能力至少30天
5. ⚠️ **团队评审必须** - 重大迁移必须团队评审

### 禁止的操作

- ❌ 未扫描功能就开始迁移
- ❌ 未对比功能就删除服务
- ❌ 未测试验证就清理代码
- ❌ 未备份数据就执行迁移
- ❌ 未更新文档就上线新服务

---

## 📊 质量门禁

### 每个服务整合必须通过的检查点

#### Gate 1: 功能扫描完成度
- [ ] 扫描覆盖率 = 100%
- [ ] 功能清单文档完整
- [ ] 依赖关系图清晰

#### Gate 2: 功能对比完成度
- [ ] 对比覆盖率 = 100%
- [ ] 缺失功能清单明确
- [ ] 迁移决策合理

#### Gate 3: 功能迁移质量
- [ ] 代码迁移完整
- [ ] 编译0错误
- [ ] 架构规范100%符合
- [ ] 代码质量评分≥90

#### Gate 4: 功能验证通过
- [ ] 单元测试通过率100%
- [ ] 集成测试通过率100%
- [ ] API测试通过率100%
- [ ] 性能测试达标

#### Gate 5: 上线准备就绪
- [ ] 文档更新完成
- [ ] 部署脚本准备
- [ ] 回滚方案就绪
- [ ] 团队评审通过

**⚠️ 只有通过所有5个Gate才可进入清理阶段**

---

## 🎯 下一步行动

### 立即执行

**第一步**: 从最简单的服务开始扫描
```bash
# 推荐顺序（由简到难）
1. ioedream-config-service (配置最简单)
2. ioedream-scheduler-service (功能独立)
3. ioedream-audit-service (已有基础)
4. ioedream-notification-service (功能清晰)
5. ioedream-monitor-service (监控功能)
6. ioedream-system-service (系统功能)
7. ioedream-identity-service (组织架构)
8. ioedream-auth-service (认证授权)
```

**第二步**: 创建第一个服务的详细扫描报告
```
创建: ioedream-config-service_FUNCTION_SCAN_REPORT.md
内容: 完整的功能清单、代码清单、依赖清单
```

**第三步**: 执行功能对比和迁移计划制定

---

## ✅ 检查清单总结

### 迁移前检查
- [ ] 功能扫描100%完成
- [ ] 功能对比100%完成
- [ ] 迁移计划详细制定
- [ ] 团队评审通过

### 迁移中检查
- [ ] 代码迁移逐项完成
- [ ] 编译持续通过
- [ ] 测试持续通过
- [ ] 文档实时更新

### 迁移后检查
- [ ] 功能验证100%通过
- [ ] 性能指标达标
- [ ] 安全测试通过
- [ ] 灰度验证通过

### 清理前检查（⚠️ 最重要）
- [ ] 所有功能已迁移
- [ ] 所有测试已通过
- [ ] 所有文档已更新
- [ ] 团队最终确认
- [ ] 备份已创建
- [ ] 回滚方案就绪

---

**执行人员**: AI架构整合专家  
**执行时间**: 预计3-5天（取决于功能复杂度）  
**质量标准**: 100%功能完整性保证  
**核心原则**: ⚠️ **先迁移、后验证、再删除 - 绝不跳步骤！**

---

## 📞 需要用户确认

**请用户确认整合优先级**:
1. 建议从最简单的 **ioedream-config-service** 开始？
2. 还是从核心的 **ioedream-auth-service** 开始？
3. 或者先完成 **ioedream-audit-service**（因为已有基础）？

**⚠️ 等待用户指令后再开始第一个服务的功能扫描工作**

