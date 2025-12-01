# 团队开发规范手册

## 🎯 开发流程规范

### Git 工作流程

#### 分支策略
```
main (生产分支)
├── develop (开发分支)
├── feature/xxx (功能分支)
├── hotfix/xxx (热修复分支)
└── release/xxx (发布分支)
```

#### 提交规范
```bash
# 提交格式：<type>(<scope>): <subject>

feat(user): 添加用户头像上传功能
fix(device): 修复设备状态更新异常
docs(api): 更新接口文档
style(format): 统一代码格式
refactor(auth): 重构认证模块逻辑
test(login): 添加登录功能单元测试
chore(deps): 更新依赖版本
```

#### 提交检查清单
- [ ] 代码编译通过
- [ ] 单元测试通过
- [ ] 代码格式化完成
- [ ] 已添加必要注释
- [ ] 已更新相关文档

### 代码审查规范

#### Pull Request 模板
```markdown
## 📝 变更描述
简要描述本次变更的内容和目的

## 🎯 变更类型
- [ ] 新功能 (feature)
- [ ] 问题修复 (bugfix)
- [ ] 重构 (refactor)
- [ ] 文档更新 (docs)
- [ ] 样式调整 (style)
- [ ] 测试相关 (test)
- [ ] 构建工具 (chore)

## ✅ 检查清单
- [ ] 代码遵循项目编码规范
- [ ] 已添加或更新了单元测试
- [ ] 已通过所有现有测试
- [ ] 已更新相关文档
- [ ] 已手动测试功能
- [ ] 性能影响评估完成

## 📋 相关 Issue
Closes #(issue number)

## 🖼️ 截图
如适用，添加功能截图

## 📚 参考资料
相关文档链接
```

#### 审查要点
1. **代码质量**
   - 逻辑是否清晰
   - 命名是否规范
   - 注释是否充分

2. **架构设计**
   - 是否遵循分层架构
   - 是否有循环依赖
   - 是否符合设计原则

3. **安全性**
   - 是否有安全漏洞
   - 权限控制是否完善
   - 敏感信息是否泄露

4. **性能影响**
   - 是否有性能问题
   - 数据库查询是否优化
   - 缓存使用是否合理

## 💻 开发环境配置

### IDE 配置

#### IntelliJ IDEA 配置
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <package name="java" withSubpackages="true" static="false"/>
          <package name="jakarta" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="org" withSubpackages="true" static="false"/>
          <package name="com" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="net.lab1024" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="" withSubpackages="true" static="true"/>
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
```

#### VS Code 配置
```json
// .vscode/settings.json
{
  "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
  "java.saveActions.organizeImports": true,
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.organizeImports": true
  },
  "files.exclude": {
    "**/target": true,
    "**/node_modules": true,
    "**/.git": true,
    "**/.DS_Store": true
  }
}
```

### 环境变量配置

#### 开发环境 (.env.development)
```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=33060
DB_NAME=smart_admin_v3
DB_USERNAME=root
DB_PASSWORD=

# Redis 配置
REDIS_HOST=127.0.0.1
REDIS_PORT=6389
REDIS_PASSWORD=zkteco3100

# 应用配置
SERVER_PORT=1024
LOG_LEVEL=DEBUG
PROFILE=dev
```

#### 测试环境 (.env.test)
```bash
# 数据库配置
DB_HOST=test-db-server
DB_PORT=3306
DB_NAME=smart_admin_test
DB_USERNAME=test_user
DB_PASSWORD=test_password

# Redis 配置
REDIS_HOST=test-redis-server
REDIS_PORT=6379
REDIS_PASSWORD=test_redis_password

# 应用配置
SERVER_PORT=8080
LOG_LEVEL=INFO
PROFILE=test
```

## 🔧 开发工具和脚本

### 本地开发脚本

#### 启动脚本 (start-dev.sh)
```bash
#!/bin/bash

echo "🚀 启动 SmartAdmin 开发环境..."

# 启动后端服务
echo "启动后端服务..."
cd smart-admin-api-java17-springboot3
mvn clean install -DskipTests -q
cd sa-admin
mvn spring-boot:run -Dspring.profiles.active=dev &
BACKEND_PID=$!

# 等待后端服务启动
echo "等待后端服务启动..."
sleep 30

# 启动前端服务
echo "启动前端服务..."
cd ../../smart-admin-web-javascript
npm install
npm run localhost &
FRONTEND_PID=$!

echo "✅ 开发环境启动完成！"
echo "📱 后端服务: http://localhost:1024"
echo "🌐 前端服务: http://localhost:8081"
echo "📚 API 文档: http://localhost:1024/doc.html"

# 等待用户输入停止服务
read -p "按回车键停止所有服务..."

# 停止服务
kill $BACKEND_PID $FRONTEND_PID
echo "🛑 所有服务已停止"
```

#### 数据库脚本 (init-db.sh)
```bash
#!/bin/bash

echo "🗄️ 初始化数据库..."

# 检查 MySQL 连接
mysql -h$DB_HOST -P$DB_PORT -u$DB_USERNAME -p$DB_PASSWORD -e "SELECT 1" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ 数据库连接失败"
    exit 1
fi

# 创建数据库
mysql -h$DB_HOST -P$DB_PORT -u$DB_USERNAME -p$DB_PASSWORD -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

# 执行初始化脚本
mysql -h$DB_HOST -P$DB_PORT -u$DB_USERNAME -p$DB_PASSWORD $DB_NAME < 数据库SQL脚本/mysql/smart_admin_v3.sql

# 执行公共模块脚本
mysql -h$DB_HOST -P$DB_PORT -u$DB_USERNAME -p$DB_PASSWORD $DB_NAME < 数据库SQL脚本/smart_device_management.sql
mysql -h$DB_HOST -P$DB_PORT -u$DB_USERNAME -p$DB_PASSWORD $DB_NAME < 数据库SQL脚本/smart_area_management.sql

echo "✅ 数据库初始化完成"
```

### 代码生成工具

#### MyBatis Plus 代码生成器
```java
public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:33060/smart_admin_v3", "root", "")
                .globalConfig(builder -> {
                    builder.author("SmartAdmin")
                           .enableSwagger()
                           .fileOverride()
                           .outputDir("smart-admin-api-java17-springboot3/sa-base/src/main/java");
                })
                .packageConfig(builder -> {
                    builder.parent("net.lab1024.sa.base.module")
                           .moduleName("device")
                           .entity("entity")
                           .service("service")
                           .mapper("mapper");
                })
                .strategyConfig(builder -> {
                    builder.addInclude("t_smart_device")
                           .entityBuilder()
                           .superClass(BaseEntity.class)
                           .addSuperEntityColumns("create_time", "update_time", "create_user_id", "deleted_flag")
                           .logicDeleteColumnName("deleted_flag")
                           .versionColumnName("version")
                           .naming(NamingStrategy.underline_to_camel)
                           .columnNaming(underline_to_camel)
                           .controllerBuilder()
                           .enableRestStyle();
                })
                .execute();
    }
}
```

## 📚 文档管理

### API 文档规范

#### 接口注释标准
```java
@RestController
@RequestMapping("/api/smart/device")
@Tag(name = "设备管理", description = "设备管理相关接口")
public class SmartDeviceController {

    @Operation(summary = "分页查询设备列表",
               description = "支持按设备类型、状态等条件筛选")
    @Parameter(name = "queryForm", description = "查询条件", required = true)
    @PostMapping("/page")
    @SaCheckPermission("smart:device:query")
    public ResponseDTO<PageResult<SmartDeviceVO>> queryDevicePage(
            @RequestBody @Valid SmartDeviceQueryForm queryForm) {

        // 接口实现...

        return ResponseDTO.ok(result);
    }
}
```

#### 数据字典维护
```markdown
## 设备类型枚举

| 枚举值 | 描述 | 说明 |
|--------|------|------|
| CAMERA | 摄像头 | 网络摄像头设备 |
| ACCESS_CONTROLLER | 门禁控制器 | 门禁控制设备 |
| CONSUMPTION_TERMINAL | 消费终端 | 消费POS设备 |
| ATTENDANCE_MACHINE | 考勤机 | 考勤打卡设备 |
```

### 技术文档模板

#### 功能设计文档模板
```markdown
# [功能名称] 设计文档

## 1. 需求背景
描述功能需求的背景和目标

## 2. 功能概述
简要描述功能的主要特性和用户价值

## 3. 技术方案
### 3.1 架构设计
描述技术架构和模块划分

### 3.2 数据库设计
包含表结构设计、索引设计等

### 3.3 接口设计
包含API接口定义和参数说明

### 3.4 安全设计
描述权限控制、数据加密等安全措施

## 4. 实现计划
列出开发任务和时间计划

## 5. 测试方案
描述测试策略和测试用例

## 6. 部署方案
描述部署步骤和环境要求

## 7. 运维方案
描述监控、日志、备份等运维要求
```

## 🎓 团队培训

### 新成员入职清单

#### 环境搭建
- [ ] 安装 JDK 17+
- [ ] 安装 Maven 3.6+
- [ ] 安装 Node.js 18+
- [ ] 安装 IntelliJ IDEA 或 VS Code
- [ ] 配置 Git 和 SSH 密钥
- [ ] 克隆项目到本地

#### 项目熟悉
- [ ] 阅读项目 README.md
- [ ] 阅读 CLAUDE.md 开发指南
- [ ] 了解项目架构和技术栈
- [ ] 运行项目并测试基础功能
- [ ] 了解开发规范和流程

#### 开发工具配置
- [ ] 配置 IDE 代码格式化规则
- [ ] 配置 Git 提交模板
- [ ] 安装必要的插件
- [ ] 配置数据库连接

#### 实践任务
- [ ] 完成一个简单的 Bug 修复
- [ ] 添加一个新的 API 接口
- [ ] 编写单元测试
- [ ] 参与 Code Review

### 技能提升计划

#### 初级开发工程师 (0-1年)
- **目标**: 掌握基础开发技能
- **学习内容**:
  - Java 基础和 Spring Boot 框架
  - MySQL 数据库基础
  - Git 版本控制
  - 前端基础 (Vue.js)
- **实践项目**: 参与简单功能开发

#### 中级开发工程师 (1-3年)
- **目标**: 掌握系统设计和架构
- **学习内容**:
  - 微服务架构设计
  - 数据库优化
  - 缓存技术 (Redis)
  - 消息队列
  - 容器化部署
- **实践项目**: 独立负责模块开发

#### 高级开发工程师 (3-5年)
- **目标**: 掌握复杂系统设计
- **学习内容**:
  - 分布式系统设计
  - 高并发处理
  - 系统性能优化
  - 架构重构
  - 技术团队管理
- **实践项目**: 负责系统架构设计

## 🚀 持续改进

### 定期回顾机制

#### 每周技术分享
- 时间: 每周五下午
- 内容: 新技术、问题解决、最佳实践
- 参与者: 全体开发人员

#### 月度代码审查
- 时间: 每月最后一周
- 内容: 代码质量、架构设计、性能优化
- 参与者: 技术负责人、资深工程师

#### 季度技术规划
- 时间: 每季度初
- 内容: 技术栈升级、架构演进、技术债务
- 参与者: 技术团队、产品经理

### 技术债务管理

#### 技术债务分类
1. **代码质量债务**: 命名不规范、注释缺失
2. **架构债务**: 设计不合理、模块耦合度高
3. **测试债务**: 测试覆盖率不足、缺少集成测试
4. **安全债务**: 存在安全漏洞、权限控制不完善

#### 债务偿还计划
```markdown
## Q1 2024 技术债务偿还计划

### 高优先级
- [ ] 统一异常处理机制
- [ ] 完善单元测试覆盖率 (目标: 80%+)
- [ ] 修复已知安全漏洞

### 中优先级
- [ ] 重构核心模块，降低耦合度
- [ ] 优化数据库查询性能
- [ ] 完善API文档

### 低优先级
- [ ] 统一代码格式
- [ ] 升级过时依赖
- [ ] 清理无用代码
```

---

📋 **总结**: 本文档旨在建立规范的开发流程和质量管控机制，确保团队能够高效、高质量地交付软件产品。所有团队成员都应该熟悉并遵循这些规范。