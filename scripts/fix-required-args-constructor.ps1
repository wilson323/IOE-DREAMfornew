# 批量修复@RequiredArgsConstructor违规问题脚本
# 作者: IOE-DREAM Team
# 版本: 1.0.0
# 日期: 2025-12-20

param(
    [string]$ProjectRoot = ".",
    [switch]$DryRun = $false,
    [switch]$Verbose = $false
)

# 统计变量
$totalFiles = 0
$fixedFiles = 0
$errors = 0

Write-Host "====================================" -ForegroundColor Green
Write-Host "RequiredArgsConstructor 违规修复工具" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
Write-Host "项目根目录: $ProjectRoot"
Write-Host "试运行模式: $DryRun"
Write-Host ""

# 获取所有包含@RequiredArgsConstructor的Java文件
Write-Host "正在搜索包含@RequiredArgsConstructor的Java文件..." -ForegroundColor Yellow

$javaFiles = Get-ChildItem -Path $ProjectRoot -Recurse -Filter "*.java" |
Where-Object {
    $_.FullName -notmatch "target\\" -and
    $_.FullName -notmatch "\.git\\" -and
    $_.FullName -notmatch "node_modules\\"
} |
Where-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    $content -match "@RequiredArgsConstructor"
}

$totalFiles = $javaFiles.Count
Write-Host "找到 $totalFiles 个包含@RequiredArgsConstructor的Java文件" -ForegroundColor Cyan

if ($totalFiles -eq 0) {
    Write-Host "没有找到包含@RequiredArgsConstructor的文件，脚本执行完成。" -ForegroundColor Green
    exit 0
}

foreach ($file in $javaFiles) {
    try {
        Write-Host "`n处理文件: $($file.FullName)" -ForegroundColor White

        # 读取文件内容
        $content = Get-Content $file.FullName -Raw -Encoding UTF8

        # 检查是否已经使用了@Resource
        if ($content -match "@Resource") {
            Write-Host "  跳过: 文件已经包含@Resource注解" -ForegroundColor Gray
            continue
        }

        if ($DryRun) {
            Write-Host "  [试运行] 将会修复@RequiredArgsConstructor违规" -ForegroundColor Yellow
            $fixedFiles++
            continue
        }

        # 备份原文件
        $backupPath = $file.FullName + ".backup"
        Copy-Item $file.FullName $backupPath -Force
        Write-Host "  已创建备份: $backupPath" -ForegroundColor Gray

        # 执行修复
        $fixedContent = $content

        # 1. 替换import语句
        $fixedContent = $fixedContent -replace "import lombok\.RequiredArgsConstructor;", "import jakarta.annotation.Resource;"

        # 2. 移除@RequiredArgsConstructor注解
        $fixedContent = $fixedContent -replace "@RequiredArgsConstructor\s*", ""

        # 3. 替换private final为@Resource private
        $fixedContent = $fixedContent -replace "private final ([A-Z]\w+(?:<.*?>)?)\s+(\w+);", "@Resource`n    private `$1 `$2;"

        # 写入修复后的内容
        Set-Content $file.FullName $fixedContent -Encoding UTF8 -NoNewline
        Write-Host "  ✓ 成功修复: @RequiredArgsConstructor → @Resource" -ForegroundColor Green
        $fixedFiles++

        # 删除备份文件
        Remove-Item $backupPath -Force
        Write-Host "  已删除备份文件" -ForegroundColor Gray

    }
    catch {
        Write-Host "  ✗ 处理文件时发生错误: $($_.Exception.Message)" -ForegroundColor Red
        $errors++
    }
}

# 输出统计结果
Write-Host "`n====================================" -ForegroundColor Green
Write-Host "修复完成统计" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
Write-Host "总文件数: $totalFiles"
Write-Host "已修复: $fixedFiles"
Write-Host "错误数: $errors"
Write-Host "成功率: $([math]::Round(($fixedFiles / $totalFiles) * 100, 2))%"

if ($errors -gt 0) {
    Write-Host "`n⚠️  发现 $errors 个错误，请检查上述错误信息" -ForegroundColor Red
    exit 1
}
elseif ($fixedFiles -gt 0) {
    Write-Host "`n🎉 成功修复了 $fixedFiles 个文件！" -ForegroundColor Green
}
else {
    Write-Host "`n✅ 没有需要修复的文件" -ForegroundColor Green
}

Write-Host "`n建议后续操作:" -ForegroundColor Cyan
Write-Host "1. 运行 'mvn clean compile' 验证编译" -ForegroundColor White
Write-Host "2. 运行单元测试确保功能正常" -ForegroundColor White
Write-Host "3. 提交代码变更" -ForegroundColor White
