# IOE-DREAM 微服务测试指南

## 📋 概述

本文档提供了IOE-DREAM微服务架构的完整测试指南，包括测试环境搭建、测试执行、结果分析等详细说明。

## 🏗️ 测试架构

### 测试目录结构

```
microservices/test/
├── integration-test/          # 集成测试
│   ├── integration-test-suite.sh       # 集成测试主脚本
│   ├── business-flow-tests.sh          # 业务流程测试
│   ├── e2e/                           # 端到端测试
│   └── contract/                      # 契约测试
├── performance-test/         # 性能测试
│   ├── performance-test-suite.sh       # 性能测试主脚本
│   ├── load/                          # 负载测试
│   ├── stress/                        # 压力测试
│   └── capacity/                      # 容量测试
├── test-data/               # 测试数据
│   ├── generator/test-data-generator.py # 数据生成器
│   └── scripts/setup-test-environment.sh  # 环境配置
├── reports/                 # 测试报告
│   ├── integration/         # 集成测试报告
│   ├── performance/         # 性能测试报告
│   └── business-flows/      # 业务流程报告
├── config/                  # 配置文件
│   ├── jmeter/              # JMeter配置
│   ├── docker/              # Docker配置
│   └── testcontainers/      # 测试容器配置
└── scripts/                 # 执行脚本
    └── run-all-tests.sh      # 完整测试套件
```

## 🎯 测试目标和标准

### 性能目标
- **响应时间**: <200ms (95%请求)
- **吞吐量**: >1000 TPS
- **并发用户**: 支持2000+并发
- **错误率**: <0.1%
- **系统稳定性**: 30分钟持续负载无崩溃

### 集成测试目标
- **服务发现成功率**: 100%
- **API契约一致性**: 100%
- **熔断器触发**: 正常
- **重试机制**: 有效

## 🚀 快速开始

### 1. 环境准备

```bash
# 克隆项目
git clone <repository-url>
cd IOE-DREAM/microservices/test

# 安装依赖
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install curl jq python3 docker docker-compose

# CentOS/RHEL
sudo yum update
sudo yum install curl jq python3 docker docker-compose
```

### 2. 执行完整测试

```bash
# 一键执行所有测试
./scripts/run-all-tests.sh

# 指定部署模式
./scripts/run-all-tests.sh --mode docker

# 跳过性能测试
./scripts/run-all-tests.sh --skip-performance
```

### 3. 查看测试报告

测试完成后，报告将生成在 `reports/overall-<timestamp>/` 目录中，包含：
- 综合测试报告 (HTML格式)
- 详细JSON数据
- 各类测试的子报告

## 📊 测试类型详解

### 1. 集成测试

验证微服务间的通信和协作，包括：

#### 服务发现和注册测试
- Nacos服务注册验证
- 服务健康状态检查
- 负载均衡验证

#### API通信测试
- HTTP接口调用测试
- 参数验证测试
- 错误处理测试

#### 熔断器和重试机制测试
- 故障注入测试
- 熔断器触发测试
- 重试机制验证

#### 执行命令

```bash
# 单独运行集成测试
./integration-test/integration-test-suite.sh

# 查看详细日志
./integration-test/integration-test-suite.sh | tee integration-test.log
```

### 2. 性能测试

多维度性能验证，包括：

#### 负载测试
- 并发用户: 100, 500, 1000, 2000
- 持续时间: 60-300秒
- 测试目标: 验证系统在正常负载下的性能表现

#### 压力测试
- 高并发: 3000+用户
- 突发流量: 模拟峰值访问
- 测试目标: 验证系统的极限承载能力

#### 容量测试
- 长时间运行: 30分钟+
- 稳定性验证
- 资源使用监控

#### 执行命令

```bash
# 单独运行性能测试
./performance-test/performance-test-suite.sh

# 使用JMeter测试
jmeter -n -t config/jmeter/load-test.jmx -l results.jtl

# 使用Apache Bench测试
ab -n 10000 -c 100 http://localhost:8080/api/auth/ping
```

### 3. 业务流程测试

端到端业务场景验证：

#### 用户认证流程
1. 用户注册
2. 用户登录
3. Token验证
4. 用户信息获取

#### 门禁控制流程
1. 设备注册
2. 权限分配
3. 门禁验证
4. 记录查询

#### 消费支付流程
1. 账户创建
2. 账户充值
3. 消费支付
4. 记录查询

#### 考勤管理流程
1. 考勤规则设置
2. 打卡签到
3. 记录查询
4. 统计分析

#### 执行命令

```bash
# 单独运行业务流程测试
./integration-test/business-flow-tests.sh

# 查看业务流程报告
cat reports/business-flows/business-flow-test-*.html
```

## 🔧 环境配置

### Docker模式部署

```bash
# 启动Docker测试环境
./test-data/scripts/setup-test-environment.sh --mode docker

# 查看服务状态
docker-compose -f test/docker/docker-compose.test.yml ps

# 查看日志
docker-compose -f test/docker/docker-compose.test.yml logs
```

### 本地模式部署

```bash
# 配置本地测试环境
./test-data/scripts/setup-test-environment.sh --mode local

# 手动启动MySQL
systemctl start mysql

# 手动启动Redis
systemctl start redis
```

### 测试数据生成

```bash
# 生成测试数据
python3 test-data/generator/test-data-generator.py \
    --users 1000 \
    --devices 50 \
    --transactions 5000 \
    --format json

# 导入测试数据
mysql -u test -ptest ioedream_test < generated-data/users.sql
```

## 📈 监控和分析

### 实时监控

Docker模式下提供以下监控服务：

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Nacos**: http://localhost:8848/nacos (nacos/nacos)

### 系统资源监控

测试过程中自动监控系统资源：

```bash
# 查看CPU使用率
top

# 查看内存使用
free -h

# 查看磁盘I/O
iotop

# 查看网络连接
netstat -an | grep :8080
```

### 日志分析

```bash
# 查看应用日志
tail -f logs/application.log

# 查看错误日志
grep ERROR logs/application.log

# 分析访问日志
awk '{print $1}' logs/access.log | sort | uniq -c
```

## 📊 测试报告解读

### 综合测试报告

综合测试报告包含以下信息：

1. **测试摘要**
   - 总体执行情况
   - 成功率统计
   - 关键指标

2. **测试套件详情**
   - 集成测试结果
   - 性能测试结果
   - 业务流程测试结果

3. **环境信息**
   - 测试环境配置
   - 系统版本信息
   - 网络配置

### 性能测试报告

性能测试报告重点关注：

1. **响应时间分布**
   - 平均响应时间
   - P95/P99延迟
   - 最大响应时间

2. **吞吐量指标**
   - TPS (每秒事务数)
   - 并发用户数
   - 系统容量

3. **资源使用情况**
   - CPU使用率
   - 内存使用率
   - 网络I/O
   - 磁盘I/O

### 业务流程测试报告

业务流程报告展示：

1. **流程完整性**
   - 各步骤执行状态
   - 数据一致性验证
   - 异常处理测试

2. **跨服务调用**
   - 服务间通信状态
   - 数据传输准确性
   - 错误处理机制

## 🚨 常见问题和解决

### 环境相关问题

**问题**: Docker容器启动失败
```bash
# 解决方案
docker-compose down
docker system prune -f
docker-compose up -d
```

**问题**: 服务连接超时
```bash
# 解决方案
# 检查网络连接
ping localhost
telnet localhost 8080

# 检查端口占用
netstat -an | grep :8080
```

### 测试执行问题

**问题**: 集成测试失败
```bash
# 解决方案
# 检查服务状态
curl http://localhost:8080/actuator/health

# 查看服务日志
docker-compose logs gateway
```

**问题**: 性能测试结果不达标
```bash
# 解决方案
# 检查系统资源
free -h
df -h

# 调整测试参数
# 降低并发用户数
# 增加测试间隔时间
```

### 数据相关问题

**问题**: 测试数据不足
```bash
# 解决方案
# 生成更多测试数据
python3 test-data/generator/test-data-generator.py \
    --users 5000 \
    --devices 200
```

## 🔧 自定义测试配置

### 修改测试参数

编辑相应的测试脚本：

```bash
# 集成测试配置
vim integration-test/integration-test-suite.sh

# 性能测试配置
vim performance-test/performance-test-suite.sh
```

### 添加新的测试用例

1. **集成测试用例**
```bash
# 在integration-test-suite.sh中添加
test_new_feature() {
    log "开始新功能测试..."

    if make_api_request "GET" "$GATEWAY_URL/api/new/endpoint" "" "" "200" "新功能测试"; then
        log_success "新功能测试通过"
    else
        log_error "新功能测试失败"
    fi
}
```

2. **业务流程测试用例**
```bash
# 在business-flow-tests.sh中添加
test_new_business_flow() {
    log_flow "开始新业务流程测试..."

    # 实现测试逻辑
}
```

### 配置JMeter测试计划

```xml
<!-- 在config/jmeter/目录下创建新的测试计划 -->
<jmeterTestPlan>
    <hashTree>
        <ThreadGroup>
            <stringProp name="ThreadGroup.num_threads">100</stringProp>
            <stringProp name="ThreadGroup.ramp_time">10</stringProp>
            <stringProp name="ThreadGroup.duration">300</stringProp>
        </ThreadGroup>

        <HTTPSamplerProxy>
            <stringProp name="HTTPSampler.domain">localhost</stringProp>
            <stringProp name="HTTPSampler.port">8080</stringProp>
            <stringProp name="HTTPSampler.path">/api/test</stringProp>
        </HTTPSamplerProxy>
    </hashTree>
</jmeterTestPlan>
```

## 📚 参考资源

### 工具文档

- [Apache JMeter 官方文档](https://jmeter.apache.org/)
- [Docker 官方文档](https://docs.docker.com/)
- [TestContainers 文档](https://www.testcontainers.org/)

### 最佳实践

1. **测试环境隔离**
   - 使用独立的测试数据库
   - 容器化测试环境
   - 测试数据清理

2. **测试数据管理**
   - 使用真实的数据格式
   - 可重现的测试数据
   - 数据隐私保护

3. **持续集成**
   - 自动化测试执行
   - 测试报告集成
   - 失败通知机制

## 📞 技术支持

如遇到问题，请通过以下方式获取帮助：

- 查看详细日志文件
- 检查系统环境配置
- 参考本文档的常见问题部分
- 联系IOE-DREAM测试团队

---

**文档版本**: v1.0.0
**最后更新**: 2025-11-29
**维护团队**: IOE-DREAM测试团队