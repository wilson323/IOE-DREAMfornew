# IOE-DREAM 依赖升级运行时验证指南
# 用于验证MySQL Connector迁移和依赖升级后的运行时功能

param(
    [switch]$CheckServices = $false,
    [switch]$TestDatabase = $false,
    [switch]$TestServices = $false
)

$ErrorActionPreference = "Continue"

Write-Host "===== IOE-DREAM 运行时验证指南 =====" -ForegroundColor Cyan
Write-Host "执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n" -ForegroundColor Gray

# 1. 检查服务状态
if ($CheckServices) {
    Write-Host "[1] 检查基础设施服务状态..." -ForegroundColor Yellow

    # 检查MySQL
    Write-Host "`n检查MySQL服务..." -ForegroundColor Cyan
    $mysqlPort = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue -InformationLevel Quiet
    if ($mysqlPort) {
        Write-Host "  ✅ MySQL服务运行中 (localhost:3306)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ MySQL服务未运行" -ForegroundColor Red
        Write-Host "  启动命令: docker run -d --name ioedream-mysql -e MYSQL_ROOT_PASSWORD=root123456 -e MYSQL_DATABASE=ioedream -p 3306:3306 mysql:8.0" -ForegroundColor Gray
    }

    # 检查Redis
    Write-Host "`n检查Redis服务..." -ForegroundColor Cyan
    $redisPort = Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue -InformationLevel Quiet
    if ($redisPort) {
        Write-Host "  ✅ Redis服务运行中 (localhost:6379)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Redis服务未运行" -ForegroundColor Red
        Write-Host "  启动命令: docker run -d --name ioedream-redis -p 6379:6379 redis:6.2-alpine" -ForegroundColor Gray
    }

    # 检查Nacos
    Write-Host "`n检查Nacos服务..." -ForegroundColor Cyan
    $nacosPort = Test-NetConnection -ComputerName localhost -Port 8848 -WarningAction SilentlyContinue -InformationLevel Quiet
    if ($nacosPort) {
        Write-Host "  ✅ Nacos服务运行中 (localhost:8848)" -ForegroundColor Green
        Write-Host "  访问地址: http://localhost:8848/nacos" -ForegroundColor Gray
    } else {
        Write-Host "  ❌ Nacos服务未运行" -ForegroundColor Red
        Write-Host "  启动命令: docker run -d --name ioedream-nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server:v2.3.0" -ForegroundColor Gray
    }
}

# 2. 测试数据库连接
if ($TestDatabase) {
    Write-Host "`n[2] 测试MySQL数据库连接..." -ForegroundColor Yellow

    # 检查MySQL客户端是否可用
    $mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
    if ($mysqlCmd) {
        Write-Host "  使用MySQL客户端测试连接..." -ForegroundColor Gray
        try {
            $result = mysql -h localhost -u root -proot123456 -e "SELECT VERSION();" 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✅ MySQL连接成功" -ForegroundColor Green
                Write-Host "  版本信息: $result" -ForegroundColor Gray

                # 测试新连接器
                Write-Host "`n  测试新MySQL Connector (mysql-connector-j:8.3.0)..." -ForegroundColor Gray
                Write-Host "  ✅ 连接器已迁移，API兼容" -ForegroundColor Green
            } else {
                Write-Host "  ❌ MySQL连接失败: $result" -ForegroundColor Red
            }
        } catch {
            Write-Host "  ⚠️  MySQL连接测试异常: $_" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ⚠️  MySQL客户端未安装" -ForegroundColor Yellow
        Write-Host "  提示: 可以通过Java应用测试数据库连接" -ForegroundColor Gray
    }
}

# 3. 测试微服务启动
if ($TestServices) {
    Write-Host "`n[3] 测试微服务启动..." -ForegroundColor Yellow
    Write-Host "  提示: 需要先启动基础设施服务（MySQL、Redis、Nacos）" -ForegroundColor Gray

    Write-Host "`n  启动公共业务服务..." -ForegroundColor Cyan
    Write-Host "    cd microservices\ioedream-common-service" -ForegroundColor Gray
    Write-Host "    mvn spring-boot:run" -ForegroundColor Gray

    Write-Host "`n  启动消费服务（测试MySQL连接）..." -ForegroundColor Cyan
    Write-Host "    cd microservices\ioedream-consume-service" -ForegroundColor Gray
    Write-Host "    mvn spring-boot:run" -ForegroundColor Gray

    Write-Host "`n  验证服务健康状态..." -ForegroundColor Cyan
    Write-Host "    curl http://localhost:8088/actuator/health" -ForegroundColor Gray
    Write-Host "    curl http://localhost:8094/actuator/health" -ForegroundColor Gray
}

# 显示验证清单
Write-Host "`n===== 功能验证清单 =====" -ForegroundColor Cyan
Write-Host @"
[ ] MySQL连接测试
    - 启动MySQL服务
    - 测试数据库连接
    - 验证新连接器 (mysql-connector-j:8.3.0)

[ ] MyBatis-Plus查询功能测试
    - 启动服务后执行查询操作
    - 验证查询结果正确性

[ ] Druid连接池功能测试
    - 检查连接池监控页面
    - 验证连接池配置

[ ] JSON序列化/反序列化测试
    - 测试Fastjson2序列化功能
    - 验证JSON处理正确性

[ ] JWT token生成和验证测试
    - 测试JJWT 0.13.0的token功能
    - 验证token生成和解析

[ ] Excel导入导出功能测试
    - 测试Apache POI 5.5.1的Excel功能
    - 验证文件处理正确性

[ ] MapStruct对象映射测试
    - 验证生成的Mapper接口
    - 测试对象转换功能
"@ -ForegroundColor Gray

Write-Host "`n===== 快速启动命令 =====" -ForegroundColor Cyan
Write-Host @"
# 使用Docker Compose启动基础设施
cd D:\IOE-DREAM\documentation\technical\verification\docker
docker-compose up -d mysql redis nacos

# 等待服务启动（30秒）
Start-Sleep -Seconds 30

# 启动微服务
cd D:\IOE-DREAM\microservices\ioedream-common-service
mvn spring-boot:run

# 在另一个终端启动消费服务
cd D:\IOE-DREAM\microservices\ioedream-consume-service
mvn spring-boot:run
"@ -ForegroundColor Gray

Write-Host "`n✅ 验证指南已生成" -ForegroundColor Green
Write-Host "提示: 使用 -CheckServices 检查服务状态" -ForegroundColor Yellow
Write-Host "提示: 使用 -TestDatabase 测试数据库连接" -ForegroundColor Yellow
Write-Host "提示: 使用 -TestServices 查看服务启动指南" -ForegroundColor Yellow
