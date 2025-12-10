# IOE-DREAM 数据库初始化脚本统一管理

## 目录结构

```
ioedream-db-init/
├── README.md                      # 说明文档
├── init-all.sql                  # 总初始化脚本
├── sql/                          # SQL脚本目录
│   ├── 01-create-databases.sql   # 创建数据库
│   ├── 02-common-schema.sql      # 公共数据库结构
│   ├── 03-business-schema.sql    # 业务数据库结构
│   └── 99-flyway-schema.sql      # Flyway版本管理表
├── data/                         # 初始化数据目录
│   ├── common-data.sql           # 公共数据
│   ├── business-data.sql         # 业务数据
│   └── test-data.sql             # 测试数据
├── migration/                    # 数据库迁移脚本
│   ├── V1.0.0__Initial_Setup.sql
│   ├── V1.0.1__Add_Business_Tables.sql
│   └── V1.0.2__Add_Indexes.sql
└── scripts/                      # 工具脚本
    ├── init-database.sh          # Linux初始化脚本
    ├── init-database.ps1         # PowerShell初始化脚本
    └── validate-schema.sql       # 结构验证脚本
```

## 使用说明

### 1. 完整初始化

```bash
# 使用总初始化脚本
mysql -u root -p < init-all.sql

# 或者使用工具脚本
./scripts/init-database.sh
```

### 2. 分步初始化

```bash
# 1. 创建数据库
mysql -u root -p < sql/01-create-databases.sql

# 2. 创建表结构
mysql -u root -p < sql/02-common-schema.sql
mysql -u root -p < sql/03-business-schema.sql

# 3. 初始化数据
mysql -u root -p < data/common-data.sql
mysql -u root -p < data/business-data.sql
```

### 3. 使用Flyway迁移

```bash
# 配置Flyway后执行迁移
flyway -url=jdbc:mysql://localhost:3306/ioedream_database -user=root -password=123456 migrate
```

## 脚本规范

1. **命名规范**：
   - 数据库创建：`01-create-databases.sql`
   - 表结构：`02-模块-schema.sql`
   - 数据初始化：`模块-data.sql`
   - 迁移脚本：`V版本号__描述.sql`

2. **执行顺序**：
   - 按文件名前缀数字顺序执行
   - V前缀的迁移脚本由Flyway管理

3. **内容规范**：
   - 每个脚本包含明确的注释
   - 支持重复执行（CREATE IF NOT EXISTS）
   - 包含适当的错误处理

## 版本管理

- **当前版本**: 1.0.0
- **版本策略**: 语义化版本控制
- **迁移管理**: 使用Flyway进行数据库版本管理

## 数据库清单

### 核心数据库
- `ioedream_database` - 数据库管理服务
- `ioedream_common_db` - 公共数据库

### 业务数据库
- `ioedream_access_db` - 门禁管理
- `ioedream_attendance_db` - 考勤管理
- `ioedream_consume_db` - 消费管理
- `ioedream_visitor_db` - 访客管理
- `ioedream_video_db` - 视频监控
- `ioedream_device_db` - 设备管理

## 维护说明

1. **新增表**：
   - 在相应的schema脚本中添加
   - 创建对应的迁移脚本
   - 更新本文档

2. **修改表结构**：
   - 使用ALTER TABLE语句
   - 创建迁移脚本
   - 确保向后兼容

3. **数据更新**：
   - 添加到相应的数据脚本中
   - 考虑数据版本控制
   - 提供回滚方案

---

**维护团队**: IOE-DREAM 架构团队
**更新时间**: 2025-12-08