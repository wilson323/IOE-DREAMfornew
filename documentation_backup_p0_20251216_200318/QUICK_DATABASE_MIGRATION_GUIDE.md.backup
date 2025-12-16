# 🚀 IOE-DREAM 数据库迁移快速指南

> **版本**: v1.0.0
> **技术栈**: Spring Boot 3.5.8 + Spring Cloud Alibaba 2025.0.0 + Flyway 9.x
> **创建日期**: 2025-12-15
> **适用对象**: IOE-DREAM项目开发者和运维人员
> **目标**: 快速掌握数据库迁移操作和最佳实践

---

## 📋 快速开始

### 🎯 迁移概述
IOE-DREAM项目采用**统一数据库架构**，所有微服务共享`ioedream`数据库，使用Flyway进行企业级版本管理。

### 🏗️ 技术架构
```
┌─────────────────────────────────────────────────────┐
│                  应用层 (9个微服务)                │
│  ioedream-common-service (8088)                     │
│  ioedream-access-service (8090)                      │
│  ioedream-attendance-service (8091)                    │
│  ioedream-consume-service (8094)                        │
│  ...                                                 │
└─────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────┐
│                 迁移控制层 (Flyway 9.x)             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   V1.x.x         │  │   V2.x.x         │  │   V3.x.x         │ │
│  │  基础架构迁移    │  │  功能增强迁移    │  │  业务优化迁移    │ │
│  └─────────────────┘�  └─────────────────┘   └─────────────────┘ │
└─────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────┐
│                 数据库层 (MySQL 8.0)                   │
│  统一数据库: ioedream                              │
│  实体总数: 76个 (公共模块38个 + 业务模块38个)       │
└─────────────────────────────────────────────────────┘
```

---

## ⚡ 5分钟快速迁移

### 步骤 1: 环境准备
```bash
# 1. 确保数据库连接正常
mysql -h 127.0.0.1 -P 3306 -u root -p123456 -e "SELECT 1 as test;" ioedream

# 2. 检查Flyway依赖
mvn dependency:tree | grep flyway
```

### 步骤 2: 配置Flyway
```yaml
# 在 application.yml 中添加 Flyway 配置
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    table: flyway_schema_history
    validate-on-migrate: true
    clean-disabled: true
```

### 步骤 3: 执行迁移
```bash
# 方式一: 使用Spring Boot启动
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.flyway.enabled=true"

# 方式二: 使用自动化工具（推荐）
cd D:\IOE-DREAM
.\scripts\database\migration-automation.ps1
```

### 步骤 4: 验证结果
```sql
-- 检查迁移历史
SELECT version, description, installed_on
FROM flyway_schema_history
ORDER BY installed_on DESC
LIMIT 5;
```

---

## 🔧 微服务迁移配置

### 🎯 已配置服务状态
| 服务名称 | 端口 | Flyway状态 | 配置文件位置 |
|---------|------|------------|----------------|
| ✅ ioedream-device-comm-service | 8087 | **已配置** | `src/main/resources/application.yml` |
| ✅ ioedream-common-service | 8088 | **已配置** | `src/main/resources/application.yml` |
| ❌ ioedream-oa-service | 8089 | **待配置** | 需要添加配置 |
| ❌ ioedream-access-service | 8090 | **待配置** | 需要添加配置 |
| ❌ ioedream-attendance-service | 8091 | **待配置** | 需要添加配置 |
| ❌ ioedream-video-service | 8092 | **待配置** | 需要添加配置 |
| ❌ ioedream-consume-service | 8094 | **待配置** | 需要添加配置 |
| ❌ ioedream-visitor-service | 8095 | **待配置** | 需要添加配置 |

### 📝 快速配置模板

#### 1. 复制标准配置
```bash
# 复制标准配置模板到服务目录
copy microservices\config-templates\flyway-standard-template.yml microservices\ioedream-xxx-service\src\main\resources\
```

#### 2. 在 application.yml 中导入
```yaml
spring:
  config:
    import:
      - "optional:classpath:flyway-standard-template.yml"
```

#### 3. 环境特定配置
```yaml
# dev环境配置
spring:
  profiles:
    active: dev
  flyway:
      baseline-on-migrate: true
      clean-disabled: false
```

---

## 🛠️ 自动化工具使用

### 🚀 PowerShell 迁移工具

#### 基本用法
```powershell
# 查看帮助
.\scripts\database\migration-automation.ps1 --help

# 迁移所有服务
.\scripts\database\migration-automation.ps1

# 迁移指定服务
.\scripts\database\migration-automation.ps1 -Service ioedream-access-service

# 验证迁移结果
.\scripts\database\migration-automation.ps1 -Action validate

# 生成迁移报告
.\scripts\database\migration-automation.ps1 -Action report

# 生产环境演练
.\scripts\database\migration-automation.ps1 -Environment prod -DryRun
```

#### 高级用法
```powershell
# 仅检查配置
.\scripts\database\migration-automation.ps1 -Action config-check

# 执行数据库备份
.\scripts\database-automation.ps1 -Action backup

# 回滚操作（开发中）
.\scripts\database\migration-automation.ps1 -Action rollback
```

### 📊 自动化报告生成
工具会自动生成详细的HTML报告，包含：
- ✅ 迁移执行统计
- ✅ 性能影响分析
- ✅ 错误诊断信息
- ✅ 改进建议

---

## 🔍 常见问题解决

### ❌ 问题1: Flyway连接失败
**错误信息**: `Found non-empty schema(s) 'ioedream' but no schema history table`

**解决方案**:
```yaml
spring:
  flyway:
    baseline-on-migrate: true  # 自动baseline
    baseline-version: 0
```

### ❌ 问题2: 迁移脚本语法错误
**错误信息**: `Syntax error in migration script`

**解决方案**:
```sql
-- 检查SQL语法
-- 使用SET FOREIGN_KEY_CHECKS = 0;
-- 检查表名和字段名
-- 验证SQL语句完整性
```

### ❌ 问题3: 配置不生效
**错误信息**: `Configuration properties not loaded`

**解决方案**:
```yaml
# 确保配置位置正确
spring:
  config:
    import:
      - "classpath:application.yml"
```

### ❌ 问题4: 数据库权限不足
**错误信息**: `Access denied for user`

**解决方案**:
```sql
-- 授予必要权限
GRANT ALL PRIVILEGES ON ioedream.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

---

## 🎯 最佳实践

### 1. 迁移脚本规范
```sql
-- ✅ 好的示例
-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2_1_9__ENHANCE_TABLE_STRUCTURE
-- 描述: 增强表结构，添加索引优化
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- 变更语句
CREATE TABLE IF NOT EXISTS t_new_table (...);

-- 索引优化
CREATE INDEX idx_name ON t_table (column_name);

-- 验证语句
SELECT COUNT(*) FROM t_new_table;

COMMIT;

-- ❌ 错误示例
-- 没有事务控制
-- 没有错误处理
-- 没有验证语句
```

### 2. 版本号管理
```
版本号格式: V{major}_{minor}_{patch}__{Description}
示例: V2_1_9__ENHANCE_TABLE_STRUCTURE.sql
规则: 语义化版本号，清晰描述变更内容
```

### 3. 环境隔离
```yaml
# 开发环境
dev:
  flyway:
    clean-disabled: false
    validate-on-migrate: false

# 生产环境
prod:
  flyway:
    clean-disabled: true
    validate-on-migrate: true
```

### 4. 备份策略
```powershell
# 每次迁移前自动备份
- 备份位置: scripts\database\backup\
- 保留时间: 30天
- 备份验证: 完整性检查
```

---

## 📚 进阶学习

### 📖 核心文档
1. **完整迁移策略**: [DATABASE_MIGRATION_COMPREHENSIVE_STRATEGY.md](./DATABASE_MIGRATION_COMPREHENSIVE_STRATEGY.md)
2. **Flyway官方文档**: https://flywaydb.org/documentation/
3. **Spring Boot集成**: https://spring.io/guides/gs/using-flyway-with-spring-boot/

### 🛠️ 相关技能
- **database-migration-specialist**: 数据库迁移专家技能
- **four-tier-architecture-guardian**: 四层架构守护
- **spring-boot-jakarta-guardian**: Jakarta EE迁移支持

### 🔗 外部资源
- **Spring Cloud Alibaba文档**: https://spring-cloud-alibaba.github.io/
- **MyBatis-Plus文档**: https://baomidou.com/
- **Druid监控文档**: https://github.com/alibaba/druid

---

## 🆘 故障排除

### 🔍 调试技巧

#### 1. 启用详细日志
```yaml
logging:
  level:
    org.flywaydb: DEBUG
    net.lab1024.sa: DEBUG
```

#### 2. 检查数据库状态
```sql
-- 查看表结构
SHOW TABLES;
DESCRIBE t_table_name;

-- 查看迁移历史
SELECT * FROM flyway_schema_history;
```

#### 3. 验证配置加载
```bash
# 检查Spring Boot配置
mvn spring-boot:run -Ddebug --logging.level.org.springframework.boot=DEBUG
```

### 📞 寻求帮助

1. **查阅文档**: 先查看本文档和相关技术文档
2. **检查日志**: 仔细分析错误日志和调试信息
3. **社区支持**: 在项目issue中提出问题
4. **团队协作**: 联系架构师团队或相关模块负责人

---

## 🎉 成功标准

### ✅ 迁移完成标志
- [ ] 所有服务Flyway配置完整
- [ ] 迁移脚本执行成功
- [ ] 数据库表结构更新正确
- [ ] 迁移历史记录完整
- [ ] 性能测试通过
- [ ] 回滚机制验证成功

### 📊 质量指标
- **成功率**: 100%
- **执行时间**: 单服务 < 5分钟
- **备份时间**: < 2分钟
- **回滚时间**: < 30秒
- **错误率**: < 0.1%

---

**快速指南维护**: IOE-DREAM架构团队
**技术支持**: 老王（数据库架构专家）
**最后更新**: 2025-12-15
**版本**: v1.0.0 - 快速指南版