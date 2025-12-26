#!/bin/bash

# 修复 access-service 的 LoggerFactory 违规

echo "🔧 开始修复 access-service LoggerFactory 违规..."
echo "==============================================="

service="ioedream-access-service"
service_path="microservices/$service"

# 检查服务是否存在
if [ ! -d "$service_path" ]; then
    echo "❌ 服务不存在: $service"
    exit 1
fi

# 获取所有违规文件
violation_files=$(find "$service_path" -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null)

if [ -z "$violation_files" ]; then
    echo "   ✅ 没有发现 LoggerFactory 违规"
    exit 0
fi

total_violations=$(echo "$violation_files" | wc -l)
echo "   📊 发现 $total_violations 个违规文件"

# 处理每个文件
fixed_count=0
echo "$violation_files" | while read file; do
    echo "   修复: $(basename "$file")"

    # 使用 PowerShell 脚本进行精确修复
    powershell.exe -Command "
    \$file = '$file'
    \$content = Get-Content \$file -Raw

    # 检查是否已有 @Slf4j
    if (\$content -match '@Slf4j') {
        # 移除 LoggerFactory 相关行
        \$content = \$content -replace '(?m)^import org\.slf4j\.Logger;.*\$', ''
        \$content = \$content -replace '(?m)^import org\.slf4j\.LoggerFactory;.*\$', ''
        \$content = \$content -replace '(?m)^.*private static final Logger.*= LoggerFactory\.getLogger.*\$', ''
    } else {
        # 添加 lombok.extern.slf4j.Slf4j 导入
        if (\$content -match 'import lombok') {
            \$content = \$content -replace '(import lombok.*\r?\n)', '$1`r`nimport lombok.extern.slf4j.Slf4j;'
        } else {
            \$content = \"import lombok.extern.slf4j.Slf4j;\`r\`n\" + \$content
        }

        # 移除 LoggerFactory 相关行
        \$content = \$content -replace '(?m)^import org\.slf4j\.Logger;.*\$', ''
        \$content = \$content -replace '(?m)^import org\.slf4j\.LoggerFactory;.*\$', ''
        \$content = \$content -replace '(?m)^.*private static final Logger.*= LoggerFactory\.getLogger.*\$', ''

        # 在类声明前添加 @Slf4j
        \$content = \$content -replace '(?m)(^@\w+.*\r?\n)*(\r?\n)(public\s+class\s+\w+)', '@Slf4j`r`n`$2$3'
    }

    # 清理多余空行
    \$content = \$content -replace '\r?\n\s*\r?\n\s*\r?\n', '\r\n\r\n'

    Set-Content \$file \$content -NoNewline
    "

    fixed_count=$((fixed_count + 1))

    # 显示进度
    if [ $((fixed_count % 5)) -eq 0 ]; then
        echo "   进度: $fixed_count/$total_violations"
    fi
done

echo "   ✅ 修复完成: $total_violations 个文件"
echo ""
echo "📋 验证修复结果..."
echo "==============================================="

# 再次检查是否还有违规
remaining_violations=$(find "$service_path" -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | wc -l)

if [ $remaining_violations -eq 0 ]; then
    echo "   ✅ access-service LoggerFactory 违规已全部修复！"
    echo ""
    echo "📋 后续步骤:"
    echo "1. 运行编译检查:"
    echo "   ./scripts/build-all.ps1 -Service $service"
    echo ""
    echo "2. 运行验证检查:"
    echo "   bash scripts/scan-logger-violations.sh | grep $service"
else
    echo "   ⚠️  仍有 $remaining_violations 个违规文件需要手动处理"
    echo ""
    echo "📋 剩余违规文件:"
    find "$service_path" -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \; 2>/dev/null | head -5
fi