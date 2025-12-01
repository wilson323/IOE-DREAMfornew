# IOE-DREAM 微服务启动验证指南

**创建时间**: 2025-11-29
**适用版本**: Spring Boot 3.x + Spring Cloud 2023.0.3
**验证范围**: 18个核心微服务

## 🎯 验证目标

确保所有微服务能够正常启动、注册到服务发现中心，并提供健康检查接口。

## 📋 服务清单和端口配置

| 服务名称 | 端口 | 健康检查路径 | 优先级 |
|---------|------|----------------|--------|
| **基础服务** |
| ioedream-auth-service | 8091 | `/actuator/health` | P0 |
| ioedream-identity-service | 8092 | `/actuator/health` | P0 |
| ioedream-system-service | 8093 | `/actuator/health` | P1 |
| **业务服务** |
| ioedream-device-service | 8094 | `/actuator/health` | P1 |
| ioedream-access-service | 8097 | `/actuator/health` | P1 |
| ioedream-consume-service | 8098 | `/actuator/health` | P1 |
| ioedream-attendance-service | 8099 | `/actuator/health` | P2 |
| ioedream-visitor-service | 8101 | `/actuator/health` | P2 |
| **支撑服务** |
| ioedream-notification-service | 8095 | `/actuator/health` | P1 |
| ioedream-audit-service | 8096 | `/actuator/health` | P2 |
| ioedream-video-service | 8100 | `/actuator/health` | P2 |
| ioedream-monitor-service | 8102 | `/actuator/health` | P3 |
| ioedream-file-service | 8103 | `/actuator/health` | P3 |
| ioedream-report-service | 8104 | `/actuator/health` | P3 |
| ioedream-hr-service | 8105 | `/actuator/health` | P3 |
| ioedream-oa-service | 8106 | `/actuator/health` | P3 |
| **管理服务** |
| ioedream-config-service | 8107 | `/actuator/health` | P4 |
| ioedream-smart-service | 8108 | `/actuator/health` | P4 |
| **网关服务** |
| smart-gateway | 8080 | `/actuator/gateway/routes` | P0 |

## 🔧 验证前准备

### 1. 环境检查
```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查Redis连接
redis-cli ping

# 检查Eureka是否运行
curl http://localhost:8761/eureka/apps
```

### 2. 依赖服务启动
```bash
# 启动Redis (如果未运行)
redis-server

# 启动Eureka服务发现中心
cd smart-gateway && mvn spring-boot:run &

# 等待Eureka启动完成
sleep 30
```

### 3. 编译项目
```bash
# 编译所有服务
cd microservices && mvn clean install -DskipTests

# 或编译特定服务
cd ioedream-auth-service && mvn clean install -DskipTests
```

## 🚀 启动验证流程

### 方式一：使用自动化脚本（推荐）
```bash
# 赋到微服务目录
cd D:\IOE-DREAM\microservices

# 执行验证脚本
chmod +x scripts/verify-microservices.sh
./scripts/verify-microservices.sh

# 查看验证报告
ls -la reports/
cat reports/microservice-verification-report-*.md
```

### 方式二：手动分步验证

#### 步骤1：启动基础服务（高优先级）
```bash
# 启动认证服务
cd ioedream-auth-service
mvn spring-boot:run &
sleep 10

# 启动身份服务
cd ../ioedream-identity-service
mvn spring-boot:run &
sleep 10

# 启动系统服务
cd ../ioedream-system-service
mvn spring-boot:run &
sleep 10
```

#### 步骤2：启动业务服务
```bash
# 启动设备管理服务
cd ../ioedream-device-service
mvn spring-boot:run &
sleep 5

# 启动门禁服务
cd ../ioedream-access-service
mvn spring-boot:run &
sleep 5

# 启动其他业务服务...
```

#### 步骤3：启动支撑服务
```bash
# 启动通知服务
cd ../ioedream-notification-service
mvn spring-boot:run &
sleep 5

# 启动审计服务
cd ../ioedream-audit-service
mvn spring-boot:run &
sleep 5
```

## ✅ 健康检查验证

### HTTP健康检查
```bash
# 检查服务健康状态
curl http://localhost:8091/actuator/health
curl http://localhost:8092/actuator/health
curl http://localhost:8093/actuator/health

# 批量检查
for port in 8091 8092 8093 8094 8095 8096; do
    if curl -s http://localhost:$port/actuator/health | grep -q "UP"; then
        echo "Port $port: ✅ UP"
    else
        echo "Port $port: ❌ DOWN"
    fi
done
```

### Eureka注册检查
```bash
# 获取注册的服务列表
curl http://localhost:8761/eureka/apps

# 检查特定服务注册状态
curl http://localhost:8761/eureka/apps | grep ioedream-auth-service
```

### 网关路由检查
```bash
# 检查网关路由
curl http://localhost:8080/actuator/gateway/routes

# 检查路由过滤器
curl http://localhost:8080/actuator/gateway/globalfilters
```

## 🔍 故障排查指南

### 常见启动问题

#### 1. 端口冲突
```bash
# 检查端口占用
netstat -tlnp | grep :8091
lsof -i :8091

# 解决方案：修改application.yml中的server.port
```

#### 2. 依赖注入失败
```bash
# 错误示例：NoSuchBeanDefinitionException
# 解决方案：检查@ComponentScan路径和依赖配置

# 验证Bean注册
grep -r "@Component\|@Service\|@Repository" src/main/java/
```

#### 3. 数据库连接失败
```bash
# 检查数据库配置
cat src/main/resources/application.yml | grep -A 10 "datasource"

# 验证H2数据库
curl http://localhost:9092/
```

#### 4. Redis连接失败
```bash
# 检查Redis服务状态
redis-cli ping

# 检查Redis配置
netstat -tlnp | grep :6379
```

#### 5. Eureka注册失败
```bash
# 检查Eureka服务状态
curl http://localhost:8761/eureka/

# 检查网络连接
telnet localhost 8761

# 验证配置文件
grep -r "eureka.client" src/main/resources/
```

### 日志分析

#### 查看应用日志
```bash
# 实时查看日志
tail -f logs/*.log

# 查看错误日志
grep -i "error\|exception" logs/*.log
```

#### JVM分析
```bash
# 查看JVM参数
jps -v

# 堆内存分析
jmap -heap <pid> heap_dump.hprof
```

## 📊 性能基准

### 启动时间基准
- **小型服务** (< 5MB): 启动时间 < 10秒
- **中型服务** (5-20MB): 启动时间 < 20秒
- **大型服务** (> 20MB): 启动时间 < 30秒

### 内存使用基准
- **基础服务**: 200-500MB
- **业务服务**: 300-800MB
- **支撑服务**: 400-1000MB

### 响应时间基准
- **健康检查**: < 100ms
- **API接口**: < 200ms
- **数据库查询**: < 500ms

## 📈 监控配置

### Prometheus集成
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Grafana Dashboard
- 服务健康状态概览
- 请求响应时间趋势
- 内存和CPU使用率
- 错误率统计

## ✅ 验证检查清单

### 启动验证
- [ ] 所有服务能够正常启动
- [ ] 没有启动错误或异常
- [ ] 启动时间在可接受范围内
- [ ] 应用端口正常监听

### 注册验证
- [ ] 所有服务都注册到Eureka
- [ ] 服务实例状态为UP
- [ ] 服务间能够相互发现
- [ ] 负载均衡配置正确

### 健康验证
- [ ] 所有健康检查接口返回UP
- [ ] 数据库连接正常
- [ ] Redis连接正常
- [ ] 自定义健康检查正常

### 网关验证
- [ ] 网关服务正常启动
- [ ] 所有路由配置生效
- [ ] 请求转发正常
- [ ] 负载均衡策略工作

## 🚨 告警配置

### 关键告警规则
1. **服务宕机告警**: 服务健康状态连续3次DOWN
2. **高响应时间告警**: P95响应时间超过2秒
3. **内存使用率告警**: JVM内存使用率超过90%
4. **注册失败告警**: 服务无法注册到Eureka
5. **错误率告警**: 5分钟内错误率超过5%

## 📋 维护建议

### 日常维护
- 定期检查服务健康状态
- 监控资源使用情况
- 及时处理异常告警
- 定期清理日志文件

### 优化建议
- 调整JVM内存配置
- 优化数据库连接池设置
- 配置合理的超时时间
- 实施服务熔断和降级

### 扩展建议
- 引入容器化部署
- 配置自动扩缩容
- 实施蓝绿部署
- 建立CI/CD流水线

---

**更新时间**: 2025-11-29
**文档维护**: IOE-DREAM Team
**版本**: v1.0.0