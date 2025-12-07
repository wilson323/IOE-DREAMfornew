# ============================================
# IOE-DREAM 数据库索引优化执行脚本 (PowerShell)
# 版本: 1.0.0
# 日期: 2025-01-30
# 说明: 执行所有模块的索引优化SQL
# ============================================

# 配置变量
$DB_HOST = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DB_PORT = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DB_USER = if ($env:DB_USER) { $env:DB_USER } else { "root" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "" }
$DB_NAME = if ($env:DB_NAME) { $env:DB_NAME } else { "ioedream" }

# 颜色输出函数
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# 检查MySQL连接
function Test-MySQLConnection {
    Write-Info "检查MySQL连接..."
    
    $connectionString = "server=$DB_HOST;port=$DB_PORT;uid=$DB_USER;pwd=$DB_PASSWORD;database=$DB_NAME;"
    
    try {
        # 使用MySQL命令行工具测试连接
        $testQuery = "SELECT 1;"
        $result = & mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "$testQuery" "$DB_NAME" 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Info "MySQL连接成功"
            return $true
        } else {
            Write-Error "MySQL连接失败: $result"
            return $false
        }
    } catch {
        Write-Error "MySQL连接异常: $_"
        return $false
    }
}

# 执行SQL文件
function Invoke-SqlFile {
    param(
        [string]$SqlFile,
        [string]$ModuleName
    )
    
    if (-not (Test-Path $SqlFile)) {
        Write-Warn "SQL文件不存在: $SqlFile"
        return $false
    }
    
    Write-Info "执行 $ModuleName 索引优化SQL: $SqlFile"
    
    try {
        # 读取SQL文件内容
        $sqlContent = Get-Content $SqlFile -Raw -Encoding UTF8
        
        # 执行SQL
        if ($DB_PASSWORD) {
            $result = $sqlContent | & mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" 2>&1
        } else {
            $result = $sqlContent | & mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" "$DB_NAME" 2>&1
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Info "$ModuleName 索引优化执行成功"
            return $true
        } else {
            Write-Error "$ModuleName 索引优化执行失败: $result"
            return $false
        }
    } catch {
        Write-Error "$ModuleName 索引优化执行异常: $_"
        return $false
    }
}

# 验证索引
function Test-Indexes {
    Write-Info "验证索引创建情况..."
    
    $verifyQuery = @"
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = '$DB_NAME'
    AND INDEX_NAME LIKE 'idx_%'
GROUP BY 
    TABLE_NAME, INDEX_NAME
ORDER BY 
    TABLE_NAME, INDEX_NAME;
"@
    
    try {
        if ($DB_PASSWORD) {
            & mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "$verifyQuery" "$DB_NAME"
        } else {
            & mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -e "$verifyQuery" "$DB_NAME"
        }
    } catch {
        Write-Error "验证索引失败: $_"
    }
}

# 主函数
function Main {
    Write-Info "开始执行数据库索引优化..."
    
    # 检查MySQL连接
    if (-not (Test-MySQLConnection)) {
        Write-Error "MySQL连接失败，请检查配置"
        exit 1
    }
    
    # 获取脚本所在目录
    $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    $ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptDir)
    
    # 执行各模块索引优化
    $sqlFiles = @(
        @{ File = "$ProjectRoot\microservices\ioedream-access-service\src\main\resources\sql\access_index_optimization.sql"; Module = "门禁模块" },
        @{ File = "$ProjectRoot\microservices\ioedream-attendance-service\src\main\resources\sql\attendance_index_optimization.sql"; Module = "考勤模块" },
        @{ File = "$ProjectRoot\microservices\ioedream-visitor-service\src\main\resources\sql\visitor_index_optimization.sql"; Module = "访客模块" },
        @{ File = "$ProjectRoot\microservices\ioedream-video-service\src\main\resources\sql\video_index_optimization.sql"; Module = "视频模块" },
        @{ File = "$ProjectRoot\microservices\ioedream-consume-service\src\main\resources\sql\consume_index_optimization.sql"; Module = "消费模块" }
    )
    
    $successCount = 0
    $failCount = 0
    
    foreach ($sqlFile in $sqlFiles) {
        if (Invoke-SqlFile -SqlFile $sqlFile.File -ModuleName $sqlFile.Module) {
            $successCount++
        } else {
            $failCount++
        }
    }
    
    Write-Info "数据库索引优化执行完成 (成功: $successCount, 失败: $failCount)"
    
    # 验证索引
    Test-Indexes
}

# 执行主函数
Main

