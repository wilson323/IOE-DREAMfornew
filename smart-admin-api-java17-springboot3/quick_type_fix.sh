#!/bin/bash

echo "🔥 快速修复类型转换错误..."

# 修复ProductService中的类型转换
echo "修复ProductService类型转换..."
PRODUCT_SERVICE="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/product/ProductService.java"
if [ -f "$PRODUCT_SERVICE" ]; then
    # 修复String到Integer的转换
    sed -i 's/Integer.parseInt(\([^)]*\))/Integer.parseInt(\1.trim())/g' "$PRODUCT_SERVICE"
    sed -i 's/pageResult.setTotalCount((long) \([^)]*\))/pageResult.setTotalCount(\1.longValue())/g' "$PRODUCT_SERVICE"
fi

# 修复OrderingService中的类型转换
echo "修复OrderingService类型转换..."
ORDERING_SERVICE="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/ordering/OrderingService.java"
if [ -f "$ORDERING_SERVICE" ]; then
    sed -i 's/Integer.parseInt(\([^)]*\))/Integer.parseInt(\1.trim())/g' "$ORDERING_SERVICE"
    sed -i 's/Integer.valueOf(\([^)]*\))/Integer.valueOf(\1.trim())/g' "$ORDERING_SERVICE"
fi

# 修复DatabaseIndexAnalyzer中的OptionalDouble转换
echo "修复DatabaseIndexAnalyzer..."
DB_ANALYZER="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/tool/DatabaseIndexAnalyzer.java"
if [ -f "$DB_ANALYZER" ]; then
    sed -i 's/\.getAsDouble()/\.orElse(0.0)/g' "$DB_ANALYZER"
fi

echo "✅ 类型转换修复完成！"
