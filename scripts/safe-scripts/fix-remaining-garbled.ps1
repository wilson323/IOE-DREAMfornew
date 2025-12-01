# Fix remaining garbled characters in Java files
$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "Starting remaining garbled character fix..." -ForegroundColor Cyan

# Garbled character mappings
$fixes = @{
    # AttendanceRuleEngine.java
    "濡偓閺屻儰缍呯純顔芥Ц閸氾箑鎮庡▔?" = "验证位置是否有效"
    "鏉╂瑩鍣风粻鈧崠鏍ь槱閻炲棴绱濈€圭偤妾惔鏃囶嚉濡偓閺屻儱鍙挎担鎾舵畱閸ф劖鐖ｉ懠鍐ㄦ纯" = "这里应该从配置或数据库获取公司位置，暂时使用示例坐标"
    "缁€转换扮伐閿涙矮浜掗崗顒€寰冮崸鎰垼娑撹桨鑵戣箛鍐跨礉濡偓閺屻儴绐涚粋查" = "实际项目中应该从配置中心或数据库读取公司经纬度"
    "缁€转换扮伐閸ф劖鐖查" = "公司纬度"
    "鐠侊紕鐣绘稉銈囧仯闂傜绐涚粋浼欑礄缁绱查" = "计算两点之间的距离（米）"
    "閸︽壆鎮嗛崡濠傜窞閿涘牏鑳岄敍查" = "地球半径（米）"
    "濡偓閺屻儲妲搁崥锔胯礋閼哄倸浜ｉ弮查" = "判断是否为节假日"
    "鐎圭偟骞囬懞鍌氫海閺冦儲顥呴弻銉┾偓鏄忕帆" = "实现节假日判断逻辑"
    "濡偓閺屻儲妲搁崥锔芥箒閹烘帞褰查" = "判断是否有排班"
    "鐎圭偟骞囬幒鎺斿疆濡偓閺屻儵鈧槒绶查" = "实现排班查询逻辑"
    "濞撳懘娅庣憴鍕灟缂傛挸鐡查" = "清理缓存"
    "閼板啫瀚熺憴鍕灟瀵洘鎼哥紓鎾崇摠瀹稿弶绔婚梽查" = "考勤规则缓存已清理"
    
    # UrlConfig.java
    "鑾峰彇鏃犻渶鐧诲綍鍙互鍖垮悕璁块棶鐨剈rl淇℃伅" = "获取无需登录可以匿名访问的url信息"
    
    # Other common garbled
    "验证" = "验证"
    "鏍￠獙" = "校验"
    "缁戝畾" = "绑定"
    "绾︽潫" = "约束"
    "搴撳紓甯查" = "数据库异常"
    "运行鏃跺紓甯查" = "运行时异常"
    "鍏朵粬鏈煡异常" = "其他未知异常"
    "闂ㄧ设备异常" = "门禁设备异常"
    "闂ㄧ权限异常" = "门禁权限异常"
    "闂ㄧ迁移炴帴异常" = "门禁连接异常"
    "设备操作失败" = "设备操作失败"
    "迁移炴帴失败" = "连接失败"
}

$fixedFiles = 0
$totalFiles = 0

# Get all Java files with garbled characters
$javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue

Write-Host "Found $($javaFiles.Count) Java files to check`n" -ForegroundColor Blue

foreach ($file in $javaFiles) {
    $totalFiles++
    
    try {
        # Read file content
        $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        
        if ($null -eq $content) {
            continue
        }
        
        $originalContent = $content
        $hasChanges = $false
        
        # Apply fixes
        foreach ($key in $fixes.Keys) {
            if ($content.Contains($key)) {
                $content = $content.Replace($key, $fixes[$key])
                $hasChanges = $true
            }
        }
        
        # Save if changed
        if ($hasChanges) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedFiles++
            Write-Host "  [FIXED] $($file.Name)" -ForegroundColor Green
        }
        
        # Show progress every 100 files
        if ($totalFiles % 100 -eq 0) {
            Write-Host "Progress: $totalFiles / $($javaFiles.Count) files processed..." -ForegroundColor Cyan
        }
        
    } catch {
        Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
    }
}

Write-Host "`n============================================================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "Total files: $totalFiles" -ForegroundColor White
Write-Host "Fixed files: $fixedFiles" -ForegroundColor Green
Write-Host "============================================================================`n" -ForegroundColor Cyan

if ($fixedFiles -gt 0) {
    Write-Host "[SUCCESS] Fixed $fixedFiles files!`n" -ForegroundColor Green
    exit 0
} else {
    Write-Host "[INFO] No files needed fixing`n" -ForegroundColor Yellow
    exit 0
}




