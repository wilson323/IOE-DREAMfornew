#!/bin/bash

# 快速批量修复中文乱码问题
set -e

echo "🔧 开始批量修复中文乱码文件..."

# 需要修复的文件列表（包含常见乱码字符）
FILES_TO_FIX=(
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/config/MvcConfig.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/controller/EmployeeController.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/interceptor/AdminInterceptor.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/attendance/manager/AttendanceCacheManager.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/controller/ConsumeController.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/manager/ConsumeCacheManager.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/hr/manager/EmployeeCacheManager.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/hr/service/impl/EmployeeServiceImpl.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/workflow/manager/WorkflowEngineManager.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/controller/AccessRecordController.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/manager/AccessRecordManager.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/service/impl/AccessRecordServiceImpl.java"
    "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/service/impl/SmartAccessControlServiceImpl.java"
)

# 基础模块文件
BASE_FILES=(
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/CacheService.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/cache/RedisUtil.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/manager/BaseCacheManager.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/util/SmartBeanUtil.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/util/SmartResponseUtil.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/config/HeartBeatConfig.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/config/RedisConfig.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/config/RepeatSubmitConfig.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/config/TokenConfig.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/exception/AccessControlExceptionHandler.java"
    "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/handler/GlobalExceptionHandler.java"
)

# 创建备份
BACKUP_DIR="encoding_backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# 修复单个文件的函数
fix_file() {
    local file="$1"
    if [ -f "$file" ]; then
        # 备份原文件
        cp "$file" "$BACKUP_DIR/$(basename "$(dirname "$file")")_$(basename "$file").bak"

        # 移除BOM字符
        sed -i '1s/^\xEF\xBB\xBF//' "$file" 2>/dev/null || true

        # 使用iconv修复编码 - 尝试从GBK转换
        iconv -f GBK -t UTF-8 "$file" 2>/dev/null > "$file.tmp" && mv "$file.tmp" "$file" && {
            echo "✅ 已修复: $(basename "$file")"
            return 0
        }

        # 如果GBK转换失败，尝试GB2312
        iconv -f GB2312 -t UTF-8 "$file" 2>/dev/null > "$file.tmp" && mv "$file.tmp" "$file" && {
            echo "✅ 已修复: $(basename "$file") (GB2312)"
            return 0
        }

        echo "⚠️  无法自动修复: $(basename "$file")"
        return 1
    else
        echo "❌ 文件不存在: $file"
        return 1
    fi
}

# 修复文件
FIXED_COUNT=0
ALL_FILES=("${FILES_TO_FIX[@]}" "${BASE_FILES[@]}")

for file in "${ALL_FILES[@]}"; do
    if fix_file "$file"; then
        ((FIXED_COUNT++))
    fi
done

echo ""
echo "📊 修复统计:"
echo "   总文件数: ${#ALL_FILES[@]}"
echo "   修复成功: $FIXED_COUNT"
echo "   备份位置: $BACKUP_DIR"

echo ""
echo "✅ 批量修复完成！"