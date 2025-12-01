# CLAUDE.md - IOE-DREAM项目核心指南（简化版）

> **完整版**: [CLAUDE.md](../../CLAUDE.md) | **快速入门**: [10分钟上手指南](10分钟上手指南.md)

---

## ⚡ 项目概览

**项目**: IOE-DREAM 智能企业管理系统
**技术栈**: Java 17 + Spring Boot 3.x + Vue3 + MySQL + Redis
**架构**: 四层架构 (Controller → Service → Manager → DAO)
**核心特性**: 基于AI辅助的高质量0异常开发

### 🎯 业务模块
- 🚪 **门禁系统** - 设备管理、权限控制、实时监控
- 💳 **消费系统** - 账户管理、消费记录、充值退款
- ⏰ **考勤系统** - 排班管理、打卡记录、数据统计
- 📹 **视频监控** - 设备接入、实时预览、录像回放

---

## 🛠️ 快速开始

### 环境要求
```bash
Java 17+          # 必须是Java 17
Maven 3.8+        # 项目构建工具
MySQL 8.0+        # 数据库
Redis 6.0+        # 缓存
Node.js 18+       # 前端开发（可选）
```

### 启动命令
```bash
# 后端启动
cd smart-admin-api-java17-springboot3/sa-admin
mvn spring-boot:run
# 访问: http://localhost:1024

# 前端启动（可选）
cd ../../smart-admin-web-javascript
npm install && npm run localhost
# 访问: http://localhost:8081
```

### 智能开发助手
```bash
# 一键环境检查
./scripts/smart-dev-helper.sh quick-check

# 开始开发前检查
./scripts/smart-dev-helper.sh start-work

# 完成开发后验证
./scripts/smart-dev-helper.sh finish-work

# 自动修复问题
./scripts/smart-dev-helper.sh auto-fix

# 提交前检查
./scripts/smart-dev-helper.sh commit
```

---

## 🔥 核心规范（零容忍）

### ❌ 绝对禁止
```java
import javax.*;              // 必须使用 jakarta.*
@Autowired                   // 必须使用 @Resource
System.out.println();        // 必须使用 log.info()
Controller直接访问DAO        // 必须通过Service层
```

### ✅ 必须遵守
```java
import jakarta.*;             // Spring Boot 3.x要求
@Resource                    // 依赖注入
@Slf4j + log.info()          // 日志记录
四层架构调用链               // Controller→Service→Manager→DAO
实体类继承BaseEntity          // 自动审计字段
```

### 🔐 权限控制
```java
@RestController
public class UserController {
    @GetMapping("/list")
    @SaCheckPermission("user:list")  // 必须加权限注解
    public ResponseDTO<List<UserVO>> list() {
        return ResponseDTO.ok(userService.getList());
    }
}
```

---

## 🏗️ 四层架构

```
┌─────────────────┐
│   Controller    │ ← 接收请求，参数校验，权限控制
├─────────────────┤
│    Service      │ ← 业务逻辑，事务管理
├─────────────────┤
│    Manager      │ ← 复杂业务封装，跨模块调用
├─────────────────┤
│      DAO        │ ← 数据访问，MyBatis-Plus操作
└─────────────────┘
```

**调用规则**:
- ✅ Controller → Service → Manager → DAO
- ❌ 禁止跨层访问（如Controller直接访问DAO）
- ❌ 禁止反向调用（如DAO调用Service）

---

## 📋 开发流程

### 1. 开发前
```bash
./scripts/smart-dev-helper.sh start-work
```

### 2. 开发中
```bash
# 实时编译检查
mvn clean compile -q

# 规范检查
./scripts/dev-standards-check.sh
```

### 3. 开发后
```bash
./scripts/smart-dev-helper.sh finish-work
```

### 4. 提交前
```bash
./scripts/smart-dev-helper.sh commit
```

---

## 🚨 常见问题

### 编译错误？
```bash
# 检查包名问题
./scripts/smart-dev-helper.sh fix-javax

# 自动修复
./scripts/smart-dev-helper.sh auto-fix
```

### 规范检查失败？
```bash
# 修复编码规范
./scripts/smart-dev-helper.sh fix-standards

# 质量检查
./scripts/smart-dev-helper.sh check-quality
```

### 遇到问题？
```bash
# 查看项目状态
./scripts/smart-dev-helper.sh status

# 获取帮助
./scripts/smart-dev-helper.sh help

# AI辅助技能
Skill("compilation-error-specialist")    # 编译问题
Skill("code-quality-protector")          # 代码质量
Skill("four-tier-architecture-guardian") # 架构问题
```

---

## 🔧 开发技巧

### IDE配置
- **IntelliJ IDEA**: 安装Lombok插件
- **代码风格**: 导入项目代码风格配置
- **实时检查**: 启用编译时错误检查

### 实用命令
```bash
# 查看所有可用命令
./scripts/smart-dev-helper.sh help

# 全面质量检查
./scripts/smart-dev-helper.sh full-check

# Docker部署
./scripts/smart-dev-helper.sh deploy

# 运行测试
./scripts/smart-dev-helper.sh test
```

### 技能调用
```bash
# 核心技能
Skill("spring-boot-jakarta-guardian")     # Spring Boot问题
Skill("four-tier-architecture-guardian")  # 架构问题
Skill("code-quality-protector")           # 代码质量

# 业务技能
Skill("business-module-developer")        # 业务模块开发
Skill("access-control-business-specialist") # 门禁业务
Skill("consume-module-specialist")        # 消费模块
```

---

## 📚 核心文档

### 🔴 必读
- **[核心规范10条](核心规范10条.md)** - 必须遵守的规范
- **[10分钟上手指南](10分钟上手指南.md)** - 新手入门
- **[四层架构详解](../repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md)** - 架构说明

### 🟡 重要
- **[代码模板库](../repowiki/zh/content/开发规范体系/代码模板库.md)** - 开发模板
- **[API设计规范](../repowiki/zh/content/开发规范体系/API设计规范.md)** - 接口设计
- **[单元测试指南](../repowiki/zh/content/开发规范体系/单元测试指南.md)** - 测试编写

### 🟢 参考
- **[完整CLAUDE.md](../../CLAUDE.md)** - 完整项目文档
- **[repowiki规范体系](../repowiki/zh/content/开发规范体系.md)** - 权威规范
- **[技能体系](../../.claude/skills/README.md)** - AI辅助技能

---

## 💡 最佳实践

### 代码编写
1. **先写测试**: 保证代码质量
2. **小步提交**: 便于代码审查
3. **及时重构**: 保持代码整洁
4. **遵循规范**: 避免技术债务

### 问题解决
1. **查看日志**: 定位问题根源
2. **使用技能**: 获取AI辅助
3. **查阅文档**: 寻找最佳方案
4. **团队协作**: 及时沟通求助

---

## ✅ 检查清单

### 开发前
- [ ] 环境检查通过
- [ ] 阅读核心规范10条
- [ ] 了解四层架构
- [ ] 知道如何获取帮助

### 开发中
- [ ] 使用jakarta包名
- [ ] 使用@Resource注入
- [ ] 遵循四层架构
- [ ] 添加权限注解
- [ ] 编写单元测试

### 提交前
- [ ] 代码编译通过
- [ ] 质量检查通过
- [ ] 测试验证通过
- [ ] 规范检查通过

---

## 🆘 获取帮助

### 自助工具
```bash
./scripts/smart-dev-helper.sh status     # 项目状态
./scripts/smart-dev-helper.sh help       # 命令帮助
./scripts/smart-dev-helper.sh learn-fix  # 学习修复
```

### 技能帮助
```bash
Skill("help")                           # 通用帮助
Skill("development-standards-specialist") # 开发规范
Skill("business-module-developer")       # 业务开发
```

### 联系方式
- **技术负责人**: [联系方式]
- **文档问题**: 提交Issue到项目仓库
- **紧急问题**: 团队群聊或邮件

---

**记住**: 遇到任何问题都可以使用智能开发助手或AI技能获取帮助！

**更新时间**: 2025-11-21
**维护者**: IOE-DREAM开发团队
**完整文档**: [CLAUDE.md](../../CLAUDE.md)