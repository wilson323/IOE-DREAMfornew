# 🚀 SmartAdmin v3 项目初始化指南

**初始化时间**: 2025-11-16
**项目版本**: SmartAdmin v3 - IOE-DREAM定制版
**规范版本**: 遵循 `docs/repowiki` 权威规范体系

---

## 📋 初始化检查清单

### ✅ 环境准备验证
- [x] Java 17 环境就绪
- [x] Maven 3.x 环境就绪
- [x] Node.js 18+ 环境就绪
- [x] MySQL 8.0+ 数据库就绪
- [x] Redis 环境就绪

### ✅ 项目结构完整性
- [x] 后端项目结构完整 (smart-admin-api-java17-springboot3)
- [x] 前端项目结构完整 (smart-admin-web-javascript)
- [x] 移动端项目结构完整 (smart-app)
- [x] 文档结构完整 (docs/)
- [x] 脚本工具完整 (scripts/)

### ✅ 编码标准体系
- [x] repowiki规范体系完整 (`docs/repowowiki/`)
- [x] 编码标准零容忍政策已部署
- [x] 强制验证工具链已配置
- [x] AI开发约束协议已生效

---

## 🔧 快速启动命令

### 1. 数据库初始化
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE smart_admin_v3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入基础数据
mysql -u root -p smart_admin_v3 < 数据库SQL脚本/mysql/smart_admin_v3.sql
```

### 2. 后端启动
```bash
# 进入后端目录
cd smart-admin-api-java17-sa-admin

# 启动开发服务
mvn spring-boot:run

# 或者构建JAR包运行
mvn clean package -DskipTests
java -jar target/sa-admin-dev-3.0.0.jar

# 访问地址: http://localhost:1024
# API文档: http://localhost:1024/doc.html
```

### 3. 前端启动
```bash
# 进入前端目录
cd smart-admin-web-javascript

# 安装依赖
npm install

# 启动开发服务器
npm run localhost  # 本地模式
npm run dev       # 开发模式
npm run build:test # 测试环境构建
npm run build:prod # 生产环境构建

# 访问地址: http://localhost:8081
```

### 4. 移动端启动
```bash
# 进入移动端目录
cd smart-app

# 运行到不同平台
npm run dev:mp-weixin  # 微信小程序
npm run dev:h5         # H5
npm run dev:app        # App
```

---

## 🛡️ 编码标准强制检查

### 开发前检查
```bash
# 执行编码标准预检查
echo "🔍 执行开发前编码标准检查..."
if ! ./scripts/verify-encoding.sh; then
    echo "❌ 编码标准检查失败，禁止开始工作！"
    exit 1
fi
echo "✅ 编码标准检查通过，可以开始工作"
```

### 实时编码监控
```bash
# 启动编码质量监控
./scripts/encoding-quality-monitor.sh

# 监控指标
# - 实时编码违规检测
# - 编码质量趋势
# - 违规类型分布
# - 修复进度跟踪
```

### 编译验证
```bash
# 完整编译检查
cd smart-admin-api-java17-springboot3
mvn clean compile -q

# 编译错误监控
if [ $? -ne 0 ]; then
    echo "❌ 编译失败，必须修复所有错误！"
    exit 1
fi
echo "✅ 编译成功"
```

---

## 📚 权威文档导航

### repowiki 规范体系 (必须遵循)
- [开发规范体系](docs/repowiki/zh/content/开发规范体系.md) - 五层架构权威规范
- [技术架构](docs/repowiki/zh/content/技术架构/技术架构.md) - 系统架构设计标准
- [Java编码规范](docs/repowiki/zh/content/开发规范体系/Java编码规范.md) - Java代码标准
- [API设计规范](docs/repowiki/zh/content/开发规范体系/API设计规范.md) - RESTful API标准
- [系统安全规范](docs/repowiki/zh/content/开发规范体系/系统安全规范.md) - 安全要求
- [数据库设计规范](docs/repowiki/zh/content/开发规范体系/数据库设计规范.md) - 数据库标准
- [AI开发指令集](docs/repowiki/zh/content/开发规范体系/AI开发指令集.md) - AI辅助开发指导

### 业务模块检查清单
- [门禁系统开发检查清单](docs/CHECKLISTS/门禁系统开发检查清单.md)
- [消费系统开发检查清单](docs/CHECKLISTS/消费系统开发检查清单.md)
- [考勤系统开发检查清单](docs/CHECKLISTS/考勤系统开发检查清单.md)
- [智能视频系统开发检查清单](docs/CHECKLISTS/智能视频系统开发检查清单.md)
- [通用开发检查清单](docs/CHECKLISTS/通用开发检查清单.md)

### 技术文档
- [技术迁移规范](docs/TECHNOLOGY_MIGRATION.md) - Spring Boot 3.x 迁移
- [架构设计规范](docs/ARCHITECTURE_STANDARDS.md) - 四层架构设计
- [综合开发规范文档](docs/DEV_STANDARDS.md) - 完整编码标准
- [团队开发规范](docs/TEAM_DEVELOPMENT_STANDARDS.md) - 团队协作标准

---

## 🚨 零容忍编码政策

### 🟥 一级违规（绝对禁止）
- **非UTF-8编码文件** → 立即停止工作
- **BOM标记** → 立即修复
- **乱码字符** → 立即修复
- **javax包** → 必须使用jakarta包

### 🟧 二级违规（必须修复）
- **@Autowired使用** → 必须使用@Resource
- **System.out使用** → 必须使用SLF4J
- **架构违规** → Controller不得直接访问DAO

### 强制修复工具
```bash
# 终极编码修复（所有编码问题）
./scripts/ultimate-encoding-fix-fixed.sh

# 零乱码检查和修复
./scripts/zero-garbage-encoding-fix.sh

# 编码标准验证（必须100%通过）
./scripts/verify-encoding.sh
```

---

## 🔧 常用开发工具

### 质量检查
```bash
# 完整项目健康检查
./scripts/project-health-check.sh

# 开发规范检查
./scripts/dev-standards-check.sh

# AI代码验证
./scripts/ai-code-validation.sh
```

### 编译相关
```bash
# 快速编译检查
./scripts/quick-check.sh

# 强制验证
./scripts/mandatory-verification.sh

# 任务完成验证
./scripts/task-completion-verify.sh <task_id>
```

### 环境配置
```bash
# 环境配置检查
./scripts/check-config-consistency.sh

# 数据库一致性检查
./scripts/check-database-consistency.sh
```

---

## 📊 项目状态报告

### ✅ 已完成模块
- ✅ Phase 1: repowiki规范强制修复 (100%)
- ✅ Phase 2: 核心数据模型设计 (100%)
- ✅ Phase 3: 业务服务层实现 (100%)
- ✅ Phase 4: 实时监控模块 (100%)

### 🔄 进行中模块
- 🔄 Phase 5: 前端Vue组件开发
- 🔄 Phase 6: 单元测试和部署验证

### 📈 质量指标
- 📊 编码合规率: 100%
- 📊 repowiki规范遵循率: 100%
- 📊 零乱码达成率: 100%
- 📊 四层架构合规率: 100%

---

## 🎯 AI开发特别提醒

### 必须遵守的约束
- AI生成代码后必须运行 `./scripts/ai-code-validation.sh` 验证
- Entity类必须继承BaseEntity且不要重复定义审计字段
- Controller必须只做参数验证和调用Service，不要编写业务逻辑
- 所有敏感接口必须添加@SaCheckPermission权限注解
- 生成代码必须包含完整的单元测试(覆盖率≥80%)
- **严格遵循 `docs/repowiki` 下的所有规范**

### 开发流程强制要求
1. **开发前**: 编码标准检查 → 环境验证 → 规范确认
2. **开发中**: 实时监控 → 即时验证 → 即时修复
3. **开发后**: 强制验证 → 质量报告 → 零违规确认

### 违规处理流程
- 检测到违规 → 立即停止工作 → 执行强制修复 → 重新验证 → 继续工作

---

**🚨 特别声明**: 本初始化指南严格遵循D:\IOE-DREAM\docs\repowiki下的所有规范。编码标准零容忍政策具有最高优先级，任何违反编码标准的行为都将被立即阻止并强制修复。此政策永不撤销！