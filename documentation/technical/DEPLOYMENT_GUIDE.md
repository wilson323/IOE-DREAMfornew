# 🚀 IOE-DREAM 智能管理系统部署指南

## 📖 概述

本指南提供了一套完整的部署前检查机制，支持双轨制架构部署（单体 + 微服务），确保每次部署都是0异常状态。该机制解决了你提到的问题：**所有异常都应该在Docker部署前被检测和修复**。

## 🏗️ 支持的部署架构

### 1. 单体架构部署（生产稳定版）
- **适用环境**: 生产环境、测试环境
- **部署路径**: `smart-admin-api-java17-springboot3/`
- **优势**: 稳定可靠，运维简单
- **适用场景**: 对稳定性要求高的生产环境

### 2. 微服务架构部署（未来目标）
- **适用环境**: 开发环境、预发布环境
- **部署路径**: `microservices/`
- **优势**: 服务独立部署、弹性伸缩
- **适用场景**: 需要独立扩展和快速迭代的场景

### 3. 混合架构部署（渐进式迁移）
- **适用环境**: 架构过渡期
- **部署方式**: 单体 + 微服务并存
- **优势**: 平滑过渡，风险可控
- **适用场景**: 从单体向微服务迁移的过渡期

## 🛡️ 检查机制架构

### 三层防护体系

1. **开发阶段**: Git Hooks 自动检查
2. **部署前检查**: 全面的 pre-deploy-check.sh
3. **安全部署**: safe-docker-deploy.sh 保护机制

---

## 🔧 工具脚本说明

### 1. Pre-deploy Check (`scripts/pre-deploy-check.sh`)

**用途**: 在Docker部署前执行全面检查，确保代码质量和服务可用性

**检查内容**:
- 🏗️ 环境基础检查（工具、端口）
- ☕ 后端代码质量检查（javax、@Autowired、System.out）
- 🌐 前端代码质量检查（Vue组件、依赖）
- 🚀 本地启动测试（60秒稳定运行）
- 📦 前端构建测试
- 🐳 Docker环境检查

**使用方法**:
```bash
./scripts/pre-deploy-check.sh
```

### 2. Safe Docker Deploy (`scripts/safe-docker-deploy.sh`)

**用途**: 安全的Docker部署脚本，内置锁文件机制防止重复部署

**特点**:
- 🔒 锁文件机制，防止并发部署
- 🧹 自动清理现有Docker资源
- 🏥 完整的健康检查
- 📊 详细的部署报告

**使用方法**:
```bash
./scripts/safe-docker-deploy.sh
```

### 3. Git Hooks Setup (`scripts/setup-git-hooks.sh`)

**用途**: 配置Git hooks，在提交和推送前自动执行代码检查

**配置的Hooks**:
- **Pre-commit**: 每次提交前检查基础代码规范
- **Pre-push**: 每次推送前执行完整部署前检查

**使用方法**:
```bash
./scripts/setup-git-hooks.sh
```

---

## 🚀 快速开始

### 架构选择

在部署前，请确认要部署的架构类型：

#### 单体架构部署
```bash
export DEPLOY_ARCH=monolith
export DEPLOY_ENV=production  # 或 test/dev
```

#### 微服务架构部署
```bash
export DEPLOY_ARCH=microservice
export DEPLOY_ENV=development  # 或 staging
```

#### 混合架构部署
```bash
export DEPLOY_ARCH=hybrid
export DEPLOY_ENV=transition
```

### 首次使用配置

1. **设置Git Hooks**（一次配置，永久生效）:
```bash
./scripts/setup-git-hooks.sh
```

2. **验证检查机制**:
```bash
./scripts/pre-deploy-check.sh --arch=${DEPLOY_ARCH}
```

### 日常部署流程

#### 方法一：安全部署（推荐）
```bash
# 一键安全部署，自动执行所有检查
./scripts/safe-docker-deploy.sh --arch=${DEPLOY_ARCH} --env=${DEPLOY_ENV}
```

#### 方法二：手动部署

**单体架构手动部署**:
```bash
# 1. 执行部署前检查
./scripts/pre-deploy-check.sh --arch=monolith

# 2. 检查通过后，手动构建和启动
cd smart-admin-api-java17-springboot3
docker-compose build backend
docker-compose up -d

# 3. 验证服务状态
curl -s http://localhost:1024/api/health
```

**微服务架构手动部署**:
```bash
# 1. 执行部署前检查
./scripts/pre-deploy-check.sh --arch=microservice

# 2. 检查通过后，启动基础设施
cd microservices/docker
docker-compose -f infrastructure.yml up -d

# 3. 启动基础服务（按依赖顺序）
docker-compose -f basic-services.yml up -d

# 4. 启动业务服务
docker-compose -f business-services.yml up -d

# 5. 验证网关状态
curl -s http://localhost:8080/api/gateway/health
```

**混合架构手动部署**:
```bash
# 1. 同时部署单体和微服务
./scripts/deploy-hybrid.sh

# 2. 验证服务状态
curl -s http://localhost:1024/api/health          # 单体应用
curl -s http://localhost:8080/api/gateway/health  # 微服务网关
```

---

## ⚠️ 重要说明

### 检查失败的处理

当检查脚本报告错误时：

1. **不要忽略错误** - 每个错误都可能导致部署失败
2. **查看详细报告** - 脚本会提供具体的修复建议
3. **修复后重新检查** - 确保所有问题都已解决

### 绕过检查（紧急情况）

虽然不推荐，但在紧急情况下可以绕过检查：

```bash
# 绕过pre-commit hook
git commit --no-verify -m "紧急提交"

# 绕过pre-push hook
git push --no-verify origin main

# 直接Docker部署（不推荐）
docker-compose build backend
docker-compose up -d
```

---

## 🔍 检查项详细说明

### 单体架构代码质量检查

1. **javax包检查**
   - 应为 `jakarta.*` 而非 `javax.*`
   - 例外：`javax.sql.DataSource` 可保持不变

2. **@Autowired检查**
   - 应使用 `@Resource` 替代 `@Autowired`
   - 符合Spring Boot 3.x最佳实践

3. **System.out检查**
   - 应使用@Slf4j 日志框架
   - 避免 `System.out.println`

### 微服务架构代码质量检查

1. **服务依赖检查**
   - Feign客户端接口完整性
   - 服务注册发现配置正确性
   - 熔断降级机制配置

2. **配置文件检查**
   - Nacos配置中心连接
   - 服务端口冲突检查
   - 数据库连接池配置

3. **服务间调用检查**
   - 网络连通性验证
   - 负载均衡配置
   - 超时时间合理性

### 前端代码质量检查

1. **Vue组件完整性**
   - 检查关键组件文件是否存在
   - 避免运行时404错误

2. **依赖完整性**
   - 验证 `package.json` 依赖
   - 确保构建环境完整

### 服务健康检查

#### 单体应用健康检查
1. **后端启动验证**
   - 60秒稳定运行测试
   - 异常日志扫描
   - API端点可访问性

2. **Docker环境检查**
   - 服务配置验证
   - 端口可用性
   - 镜像依赖检查

#### 微服务健康检查
1. **基础设施检查**
   - Nacos注册中心状态
   - MySQL数据库连接
   - Redis缓存服务状态
   - RabbitMQ消息队列状态

2. **服务启动检查**
   - 各微服务注册状态
   - 服务间调用链路
   - 网关路由配置
   - 熔断器状态监控

3. **服务间通信检查**
   - Feign客户端连通性
   - 负载均衡效果
   - 超时重试机制

---

## 📊 故障排查

### 单体架构常见问题及解决方案

#### 1. javax包错误
```bash
# 批量修复javax包
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;
```

#### 2. @Autowired错误
```bash
# 批量替换@Autowired
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
```

#### 3. 前端Vue组件缺失
```bash
# 检查组件文件
ls -la smart-admin-web-javascript/src/components/realtime/functional/
```

#### 4. 端口占用
```bash
# Windows
netstat -ano | findstr :1024

# Linux/Mac
lsof -i :1024
```

### 微服务架构常见问题及解决方案

#### 1. 服务注册失败
```bash
# 检查Nacos状态
curl -s http://localhost:8848/nacos/v1/ns/instance/list?serviceName=ioedream-auth-service

# 检查网络连通性
telnet localhost 8848

# 查看服务日志
docker logs ioedream-auth-service
```

#### 2. 服务间调用失败
```bash
# 检查Feign客户端配置
grep -r "@FeignClient" microservices/

# 检查熔断器状态
curl -s http://localhost:8080/actuator/health

# 验证服务注册状态
curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=ioedream-{service}-service"
```

#### 3. 配置中心连接失败
```bash
# 验证Nacos配置
curl -s "http://localhost:8848/nacos/v1/cs/configs?dataId=ioedream-auth-service.yml&group=DEFAULT_GROUP"

# 检查bootstrap.yml配置
cat microservices/*/src/main/resources/bootstrap.yml
```

#### 4. 数据库连接问题
```bash
# 检查数据库服务状态
docker exec -it mysql mysql -uroot -p -e "SHOW DATABASES;"

# 验证数据库连接配置
grep -r "datasource" microservices/*/src/main/resources/application.yml
```

#### 5. 端口冲突问题
```bash
# 检查微服务端口占用
netstat -tulpn | grep :808
netstat -tulpn | grep :808[0-9]

# 查看微服务网络配置
docker network ls
docker network inspect microservices-network
```

#### 6. 服务启动顺序问题
```bash
# 按正确顺序启动服务
docker-compose -f infrastructure.yml up -d                    # 基础设施
sleep 30
docker-compose -f basic-services.yml up -d                   # 基础服务
sleep 60
docker-compose -f business-services.yml up -d                 # 业务服务
```

### 混合架构特有问题

#### 1. 路由冲突
```bash
# 检查nginx配置
cat nginx/nginx.conf | grep -E "(location|proxy_pass)"

# 验证路由规则
curl -H "Host: api.ioedream.com" http://localhost/api/health
```

#### 2. 数据同步问题
```bash
# 检查数据同步任务
curl -s http://localhost:8080/api/sync/status

# 查看同步日志
docker logs data-sync-service
```

### 监控和日志命令

#### 微服务监控
```bash
# 查看所有服务状态
docker-compose ps

# 实时查看服务日志
docker-compose logs -f

# 查看特定服务资源使用
docker stats ioedream-auth-service

# 进入服务容器调试
docker exec -it ioedream-auth-service bash
```

#### 健康检查脚本
```bash
#!/bin/bash
# 微服务健康检查
services=("ioedream-auth-service" "ioedream-identity-service" "smart-gateway")
ports=("8081" "8082" "8080")

for i in "${!services[@]}"; do
    service=${services[$i]}
    port=${ports[$i]}
    health=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/actuator/health")
    if [ "$health" = "200" ]; then
        echo "✅ $service is healthy (port: $port)"
    else
        echo "❌ $service is unhealthy (port: $port, status: $health)"
    fi
done
```

---

## 🎯 最佳实践

### 开发阶段

1. **频繁提交** - 利用Git hooks及时发现问题
2. **本地测试** - 提交前运行本地构建测试
3. **代码审查** - 关注检查脚本报告的问题

### 部署阶段

1. **使用安全部署脚本** - 避免手动操作的遗漏
2. **监控部署过程** - 观察脚本输出和健康检查
3. **保留部署日志** - 便于问题追踪和分析

### 维护阶段

1. **定期更新检查脚本** - 适应项目变化
2. **收集反馈** - 根据实际使用情况优化检查项
3. **团队培训** - 确保所有开发者理解和使用检查机制

---

## 📞 技术支持

如果遇到检查机制相关问题：

1. **查看脚本日志** - 详细输出通常能指出问题所在
2. **检查环境配置** - 确保所需工具和依赖正确安装
3. **参考错误信息** - 脚本提供的修复建议通常很准确

---

## 🔄 版本历史

- **v2.0** (2025-11-27): 重大升级，支持双轨制架构部署
  - 新增微服务架构完整支持
  - 支持单体、微服务、混合三种部署模式
  - 增强服务发现、熔断降级、负载均衡检查
  - 完善微服务故障排查和监控体系
  - 新增Nacos、Feign、Hystrix等微服务技术栈检查

- **v1.0** (2025-11-15): 初始版本，建立完整的部署前检查机制
  - 六阶段全面检查
  - Git hooks集成
  - 安全Docker部署
  - 锁文件保护机制

---

## 🌟 IOE-DREAM微服务部署特色功能

### 🔧 智能架构检测
部署脚本自动检测当前架构类型，无需手动配置：
- 自动识别单体 vs 微服务目录结构
- 智能选择对应的检查项和部署流程
- 动态调整依赖服务启动顺序

### 📊 全链路健康检查
- 基础设施状态检查（Nacos、MySQL、Redis、RabbitMQ）
- 微服务注册发现状态检查
- 服务间调用连通性验证
- 熔断器状态实时监控

### 🚀 渐进式部署支持
- 支持从单体架构向微服务架构的平滑过渡
- 混合部署模式下数据一致性保证
- 路由冲突自动检测和解决

### 💡 企业级监控集成
- 集成Spring Boot Actuator健康检查
- 支持Prometheus + Grafana监控栈
- 分布式链路追踪支持
- 容器资源使用实时监控

---

**🎉 恭喜！你现在拥有了一套支持单体+微服务双轨制架构的完整0异常部署保障机制！**