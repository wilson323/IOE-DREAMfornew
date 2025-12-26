# Consume Service Controllers 修复脚本
# 修复BOM字符和类声明问题

$controllerPath = "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller"

# 获取所有Controller文件
$controllers = Get-ChildItem -Path $controllerPath -Filter "*Controller.java"

Write-Host "开始修复 consume-service Controller 文件..." -ForegroundColor Green
Write-Host "找到 $($controllers.Count) 个 Controller 文件" -ForegroundColor Yellow

foreach ($controller in $controllers) {
    Write-Host "正在修复: $($controller.Name)" -ForegroundColor Cyan

    try {
        # 读取文件内容，去除BOM
        $content = Get-Content -Path $controller.FullName -Raw -Encoding UTF8
        if ($content -match "^\uFEFF") {
            $content = $content -replace "^\uFEFF", ""
        }

        # 修复类声明 - 查找孤立的大括号并添加类声明
        $lines = $content -split "`n"
        $newLines = @()

        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]

            # 查找模式：@PermissionCheck(...) {
            if ($line -match "@PermissionCheck\([^)]+\)\s*\{\s*$") {
                # 提取类名从文件名
                $className = $controller.BaseName  # 去掉 .java 后缀
                $line = $line -replace "\{$", "public class $className {"
            }

            $newLines += $line
        }

        # 检查并替换 @Slf4j 为标准 Logger
        $newContent = $newLines -join "`n"
        if ($newContent -match "@Slf4j") {
            # 找到Logger声明行并替换
            $newContent = $newContent -replace "@Slf4j\s*\n\s*private static final Logger log = LoggerFactory\.getLogger\([^)]+\);", "private static final Logger log = LoggerFactory.getLogger($className.class);"
        }

        # 写回文件
        $newContent | Out-File -FilePath $controller.FullName -Encoding UTF8 -NoNewline

        Write-Host "✓ 修复完成: $($controller.Name)" -ForegroundColor Green

    } catch {
        Write-Host "✗ 修复失败: $($controller.Name) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Consume Service Controller 修复完成!" -ForegroundColor Green