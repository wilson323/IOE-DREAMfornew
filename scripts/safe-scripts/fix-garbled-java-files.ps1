# Fix garbled characters in Java files
$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "`n开始修复Java文件中的乱码..." -ForegroundColor Cyan

# GBK乱码到UTF-8的修复映射
$fixMappings = @{
    "考勤" = "考勤"
    "服务" = "服务"
    "实现" = "实现"
    "管理" = "管理"
    "查询" = "查询"
    "打卡" = "打卡"
    "员工" = "员工"
    "记录" = "记录"
    "不能" = "不能"
    "为空" = "为空"
    "失败" = "失败"
    "验证" = "验证"
    "位置" = "位置"
    "超出" = "超出"
    "允许" = "允许"
    "范围" = "范围"
    "设备" = "设备"
    "列表" = "列表"
    "日期" = "日期"
    "分页" = "分页"
    "条件" = "条件"
    "倒序" = "倒序"
    "排列" = "排列"
    "执行" = "执行"
    "转换" = "转换"
    "根据" = "根据"
    "参数" = "参数"
    "异常" = "异常"
    "统一" = "统一"
    "响应" = "响应"
    "格式" = "格式"
    "集成" = "集成"
    "缓存" = "缓存"
    "管理器" = "管理器"
    "规则" = "规则"
    "引入" = "引入"
    "严格" = "严格"
    "遵循" = "遵循"
    "规范" = "规范"
    "负责" = "负责"
    "业务" = "业务"
    "逻辑" = "逻辑"
    "处理" = "处理"
    "事务" = "事务"
    "边界" = "边界"
    "完整" = "完整"
    "绉诲姩绔查" = "移动端"
    "GPS瀹氫綅" = "GPS定位"
    "绂荤嚎" = "离线"
    "鍚屾" = "同步"
    "缓存" = "缓存"
    "验证" = "验证"
    "鍐呴儴" = "内部"
    "璇锋眰" = "请求"
    "绫查" = "类"
}

$javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
$fixedCount = 0
$errorCount = 0

foreach ($file in $javaFiles) {
    try {
        # 读取文件（尝试UTF-8）
        $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        
        if ($null -eq $content) {
            # 尝试GBK
            try {
                $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
            } catch {
                $errorCount++
                continue
            }
        }
        
        $original = $content
        $changed = $false
        
        # 应用修复映射
        foreach ($key in $fixMappings.Keys) {
            if ($content.Contains($key)) {
                $content = $content.Replace($key, $fixMappings[$key])
                $changed = $true
            }
        }
        
        # 移除BOM
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
            $changed = $true
        }
        
        # 如果有修改，保存为UTF-8无BOM
        if ($changed) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedCount++
            Write-Host "  [FIXED] $($file.Name)" -ForegroundColor Green
        }
        
    } catch {
        $errorCount++
        Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
    }
}

Write-Host "`n修复完成: 修复 $fixedCount 个文件, 错误 $errorCount 个文件" -ForegroundColor Cyan




