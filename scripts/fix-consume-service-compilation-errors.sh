#!/bin/bash

# 消费服务模块编译错误批量修复脚本
# 作者: IOE-DREAM架构委员会
# 版本: 1.0.0
# 日期: 2025-12-22

set -e

echo "========================================"
echo "🔧 消费服务模块编译错误修复脚本"
echo "========================================"

CONSUME_SERVICE_DIR="microservices/ioedream-consume-service"
BACKUP_DIR="backup/$(date +%Y%m%d_%H%M%S)"

# 创建备份目录
echo "📁 创建备份目录: $BACKUP_DIR"
mkdir -p "$BACKUP_DIR"

# 备份即将修改的文件
echo "💾 备份原始文件..."
FILES_TO_MODIFY=(
    "$CONSUME_SERVICE_DIR/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductImportExportService.java"
    "$CONSUME_SERVICE_DIR/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductPriceService.java"
    "$CONSUME_SERVICE_DIR/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductQueryService.java"
    "$CONSUME_SERVICE_DIR/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductServiceImpl_Refactored.java"
    "$CONSUME_SERVICE_DIR/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductStockService.java"
    "$CONSUME_SERVICE_DIR/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductValidationService.java"
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        cp "$file" "$BACKUP_DIR/$(basename $file)"
        echo "  ✓ 备份: $(basename $file)"
    fi
done

echo ""
echo "🔨 开始修复编译错误..."

# 修复1: 异常类构造函数错误
echo "📝 修复异常类构造函数错误..."

# 定义需要修复的异常使用模式
EXCEPTION_PATTERNS=(
    's/new ConsumeProductException\("\([^"]*\)"\)/new ConsumeProductException(ConsumeProductException.ErrorCode.INVALID_PARAMETER, "\1")/g'
    's/new ConsumeProductException\("\([^"]*\)", \([^)]*\)\)/new ConsumeProductException(ConsumeProductException.ErrorCode.INVALID_PARAMETER, "\1", \2)/g'
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        echo "  修复: $(basename $file)"
        for pattern in "${EXCEPTION_PATTERNS[@]}"; do
            sed -i "$pattern" "$file"
        done
    fi
done

# 修复2: 工具类导入
echo "📝 修复工具类导入问题..."

IMPORT_FIXES=(
    's/import net\.lab1024\.sa\.consume\.util\.*;//g'
    's/BeanUtil\.copyProperties/BeanUtils.copyProperties/g'
    's/BeanUtil\./BeanUtils\./g'
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        echo "  修复导入: $(basename $file)"

        # 删除错误的导入
        sed -i '/import net\.lab1024\.sa\.consume\.util\./d' "$file"

        # 添加正确的导入（如果不存在）
        if ! grep -q "import org.springframework.beans.BeanUtils;" "$file"; then
            if grep -q "BeanUtils\." "$file"; then
                sed -i '/import lombok\./a import org.springframework.beans.BeanUtils;' "$file"
            fi
        fi

        # 添加ArrayList导入（如果不存在且需要）
        if grep -q "ArrayList" "$file" && ! grep -q "import java.util.ArrayList;" "$file"; then
            sed -i '/import java\./a import java.util.ArrayList;' "$file"
        fi

        # 修复方法调用
        sed -i 's/BeanUtil\./BeanUtils\./g' "$file"
    fi
done

# 修复3: 类型引用错误
echo "📝 修复类型引用错误..."

TYPE_FIXES=(
    's/ConsumeAddForm/ConsumeProductAddForm/g'
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        echo "  修复类型: $(basename $file)"
        for pattern in "${TYPE_FIXES[@]}"; do
            sed -i "$pattern" "$file"
        done
    fi
done

# 修复4: 实体类字段映射
echo "📝 修复实体类字段映射..."

FIELD_FIXES=(
    's/\.getStock()/\.getStockQuantity()/g'
    's/\.setStock(\([^)]*\))/\.setStockQuantity(\1)/g'
    's/\.getProductSort()/\.getRecommendSort()/g'
    's/\.setProductSort(\([^)]*\))/\.setRecommendSort(\1)/g'
    's/ConsumeProductEntity::getStock/ConsumeProductEntity::getStockQuantity/g'
    's/ConsumeProductEntity::getProductSort/ConsumeProductEntity::getRecommendSort/g'
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        echo "  修复字段: $(basename $file)"
        for pattern in "${FIELD_FIXES[@]}"; do
            sed -i "$pattern" "$file"
        done
    fi
done

# 修复5: MyBatis-Plus使用错误
echo "📝 修复MyBatis-Plus使用错误..."

MYBATIS_FIXES=(
    's/selectPage(\([^,]*\)QueryForm, \([^)]*\))/selectPage(new Page<>(\1.getPageNum(), \1.getPageSize()), \2)/g'
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        echo "  修复MyBatis: $(basename $file)"
        for pattern in "${MYBATIS_FIXES[@]}"; do
            sed -i "$pattern" "$file"
        done

        # 确保有Page导入
        if grep -q "Page<" "$file" && ! grep -q "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;" "$file"; then
            sed -i '/import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;/a import com.baomidou.mybatisplus.extension.plugins.pagination.Page;' "$file"
        fi
    fi
done

# 修复6: BigDecimal过时API
echo "📝 修复BigDecimal过时API..."

BIGDECIMAL_FIXES=(
    's/BigDecimal\.ROUND_HALF_UP/RoundingMode.HALF_UP/g'
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        echo "  修复BigDecimal: $(basename $file)"
        for pattern in "${BIGDECIMAL_FIXES[@]}"; do
            sed -i "$pattern" "$file"
        done

        # 添加RoundingMode导入
        if grep -q "RoundingMode\." "$file" && ! grep -q "import java.math.RoundingMode;" "$file"; then
            sed -i '/import java.math.BigDecimal;/a import java.math.RoundingMode;' "$file"
        fi
    fi
done

# 修复7: Form类方法调用
echo "📝 修复Form类方法调用..."

FORM_FIXES=(
    's/\.getMinStock()/\.getStockStatus()/g'
    's/\.getOrderField()/\.getSortBy()/g'
)

for file in "${FILES_TO_MODIFY[@]}"; do
    if [ -f "$file" ]; then
        echo "  修复Form: $(basename $file)"
        for pattern in "${FORM_FIXES[@]}"; do
            sed -i "$pattern" "$file"
        done
    fi
done

echo ""
echo "✅ 编译错误修复完成！"
echo ""
echo "📋 修复摘要:"
echo "  - 异常类构造函数: 统一使用ErrorCode模式"
echo "  - 工具类导入: 替换为Spring标准BeanUtils"
echo "  - 类型引用: 修正Form类名称"
echo "  - 字段映射: 统一Entity字段名"
echo "  - MyBatis-Plus: 修正分页查询语法"
echo "  - BigDecimal: 使用现代RoundingMode"
echo "  - Form类: 修正方法调用"
echo ""
echo "📁 备份位置: $BACKUP_DIR"
echo "🔄 如需回滚，请使用备份文件"
echo ""
echo "🧪 建议下一步操作:"
echo "  1. 运行编译验证: mvn clean compile"
echo "  2. 运行单元测试: mvn test"
echo "  3. 运行集成测试: mvn integration-test"
echo ""

echo "========================================"
echo "🎉 修复脚本执行完成"
echo "========================================"