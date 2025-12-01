#!/bin/bash

# 快速修复编译错误脚本
echo "🔧 开始快速修复编译错误..."

cd "$(dirname "$0")"

# 1. 修复ProductService缺失方法问题
echo "修复ProductService缺失方法..."
PRODUCT_DAO="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/dao/ProductDao.java"
if [ -f "$PRODUCT_DAO" ]; then
    # 检查是否缺少selectByQrCode方法
    if ! grep -q "selectByQrCode" "$PRODUCT_DAO"; then
        echo "添加selectByQrCode方法到ProductDao..."
        # 在适当位置添加缺失的方法
        sed -i '/^}/i\\n    /**\n     * 根据二维码查询产品\n     *\n     * @param qrCode 二维码\n     * @return 产品列表\n     */\n    List<ProductEntity> selectByQrCode(String qrCode);\n' "$PRODUCT_DAO"
    fi
fi

# 2. 修复ProductCategoryDao缺失方法问题
PRODUCT_CATEGORY_DAO="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/dao/ProductCategoryDao.java"
if [ -f "$PRODUCT_CATEGORY_DAO" ]; then
    # 检查是否缺少selectActiveCategories方法
    if ! grep -q "selectActiveCategories" "$PRODUCT_CATEGORY_DAO"; then
        echo "添加selectActiveCategories方法到ProductCategoryDao..."
        sed -i '/^}/i\\n    /**\n     * 查询激活的分类\n     *\n     * @return 分类列表\n     */\n    List<ProductCategoryEntity> selectActiveCategories();\n    \n    /**\n     * 统计激活分类数量\n     *\n     * @return 数量\n     */\n    int countActiveCategories();\n' "$PRODUCT_CATEGORY_DAO"
    fi
fi

# 3. 修复ProductDao缺失方法问题
if [ -f "$PRODUCT_DAO" ]; then
    # 检查是否缺少countHotProducts方法
    if ! grep -q "countLowStockProducts" "$PRODUCT_DAO"; then
        echo "添加统计方法到ProductDao..."
        sed -i '/^}/i\\n    /**\n     * 统计低库存产品数量\n     *\n     * @return 数量\n     */\n    int countLowStockProducts();\n    \n    /**\n     * 统计热门产品数量\n     *\n     * @return 数量\n     */\n    int countHotProducts();\n' "$PRODUCT_DAO"
    fi
fi

# 4. 修复ConsumptionMode接口缺失方法问题
CONSUMPTION_MODE="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/engine/mode/ConsumptionMode.java"
if [ -f "$CONSUMPTION_MODE" ]; then
    # 检查是否缺少isApplicableToDevice方法
    if ! grep -q "isApplicableToDevice" "$CONSUMPTION_MODE"; then
        echo "添加isApplicableToDevice方法到ConsumptionMode..."
        sed -i '/^}$/i\\n    /**\n     * 检查是否适用于指定设备\n     *\n     * @param deviceId 设备ID\n     * @return 是否适用\n     */\n    boolean isApplicableToDevice(Long deviceId);\n' "$CONSUMPTION_MODE"
    fi
fi

# 5. 修复ConsumeCacheService缺失方法问题
CONSUME_CACHE_SERVICE="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/ConsumeCacheService.java"
if [f "$CONSUME_CACHE_SERVICE" ]; then
    # 检查是否缺少getTodayConsumeAmount方法
    if ! grep -q "getTodayConsumeAmount" "$CONSUME_CACHE_SERVICE"; then
        echo "添加getTodayConsumeAmount方法到ConsumeCacheService..."
        sed -i '/^}$/i\\n    /**\n     * 获取今日消费金额\n     *\n     * @param userId 用户ID\n     * @return 消费金额\n     */\n    Long getTodayConsumeAmount(Long userId);\n' "$CONSUME_CACHE_SERVICE"
    fi
fi

echo "✅ 快速修复完成，请重新编译检查..."

# 重新编译检查
cd smart-admin-api-java17-springboot3
mvn compile -q
if [ $? -eq 0 ]; then
    echo "🎉 编译成功！"
else
    echo "❌ 仍有编译错误，需要手动修复"
fi