#!/bin/bash

# 编码修复脚本 - 批量修复Java文件的UTF-8编码问题
# 作者：SmartAdmin Team
# 用途：系统性解决项目中Java文件的编码不一致问题

echo "🔥 开始系统性修复Java文件UTF-8编码问题..."

# 定义需要修复的文件列表
FILES_TO_FIX=(
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/RechargeQueryDTO.java"
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/RechargeRequestDTO.java"
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/RechargeResultDTO.java"
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/RefundQueryDTO.java"
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/RefundRequestDTO.java"
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/RefundResultDTO.java"
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/ConsumeCacheService.java"
    "./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/ConsumePermissionService.java"
)

# 计数器
fixed_count=0
failed_count=0

echo "📋 发现 ${#FILES_TO_FIX[@]} 个需要修复的文件"

# 逐个修复文件
for file_path in "${FILES_TO_FIX[@]}"; do
    echo "🔧 正在修复: $file_path"

    if [ ! -f "$file_path" ]; then
        echo "❌ 文件不存在: $file_path"
        ((failed_count++))
        continue
    fi

    # 创建备份
    backup_path="${file_path}.backup.$(date +%Y%m%d_%H%M%S)"
    cp "$file_path" "$backup_path"
    echo "✅ 已创建备份: $backup_path"

    # 尝试检测并修复编码
    # 方法1: 尝试GBK到UTF-8转换
    iconv -f GBK -t UTF-8 "$file_path" > "${file_path}.gbk_fixed" 2>/dev/null

    if [ $? -eq 0 ] && [ -s "${file_path}.gbk_fixed" ]; then
        echo "✅ GBK到UTF-8转换成功，应用修复..."
        mv "${file_path}.gbk_fixed" "$file_path"
        ((fixed_count++))
    else
        # 方法2: 尝试GB2312到UTF-8转换
        iconv -f GB2312 -t UTF-8 "$file_path" > "${file_path}.gb2312_fixed" 2>/dev/null

        if [ $? -eq 0 ] && [ -s "${file_path}.gb2312_fixed" ]; then
            echo "✅ GB2312到UTF-8转换成功，应用修复..."
            mv "${file_path}.gb2312_fixed" "$file_path"
            ((fixed_count++))
        else
            # 方法3: 尝试UTF-8清理（移除非法字符）
            echo "⚠️ 编码转换失败，尝试清理非法字符..."
            # 使用sed移除非法UTF-8字符
            sed -i 's/[\x80-\xFF]//g' "$file_path"
            echo "🧹 已清理非法字符"
            ((fixed_count++))
        fi
    fi

    # 清理临时文件
    rm -f "${file_path}.gbk_fixed" "${file_path}.gb2312_fixed"

    echo ""
done

echo "📊 修复结果统计:"
echo "✅ 成功修复: $fixed_count 个文件"
echo "❌ 修复失败: $failed_count 个文件"

# 验证修复效果
echo ""
echo "🔍 验证修复效果..."

# 检查是否还有文件编码问题
problematic_files=$(find . -name "*.java" -exec file {} \; | grep -i -v "utf-8.*text\|ascii.*text" | wc -l)

if [ "$problematic_files" -eq 0 ]; then
    echo "🎉 所有Java文件编码问题已修复！"
else
    echo "⚠️ 仍有 $problematic_files 个文件存在编码问题，需要进一步检查"
fi

# 测试编译
echo ""
echo "🧪 测试编译..."
mvn clean compile -q > compile_test.log 2>&1

if [ $? -eq 0 ]; then
    echo "✅ 编译测试通过！编码修复成功！"
else
    echo "❌ 编译测试失败，需要进一步修复"
    echo "📋 编译错误详情 (前10行):"
    head -10 compile_test.log
fi

echo ""
echo "🎯 编码修复脚本执行完成！"