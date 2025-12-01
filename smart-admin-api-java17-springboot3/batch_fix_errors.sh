#!/bin/bash

echo "🔥 开始批量修复编译错误..."

# 1. 修复ResponseDTO缺失error(String,String)方法
echo "修复ResponseDTO..."
RESPONSE_DTO="sa-base/src/main/java/net/lab1024/sa/base/common/domain/ResponseDTO.java"
if grep -q "public static <T> ResponseDTO<T> error(String errorCode, String msg)" "$RESPONSE_DTO"; then
    echo "✅ ResponseDTO.error(String,String)已存在"
else
    echo "🔧 添加ResponseDTO.error(String,String)方法..."
    sed -i '/public static <T> ResponseDTO<T> error(String msg) {/,/}/c\
    public static <T> ResponseDTO<T> error(String msg) {\
        return new ResponseDTO<>(UserErrorCode.PARAM_ERROR.getCode(), UserErrorCode.PARAM_ERROR.getLevel(), false, msg, null);\
    }\
\
    public static <T> ResponseDTO<T> error(String errorCode, String msg) {\
        return new ResponseDTO<>(errorCode, false, msg, null);\
    }' "$RESPONSE_DTO"
fi

# 2. 批量添加缺失的DAO方法
echo "修复DAO方法..."

# ProductDao
PRODUCT_DAO="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/dao/ProductDao.java"
if [ -f "$PRODUCT_DAO" ]; then
    echo "🔧 修复ProductDao..."
    sed -i '/^}$/i\
    \
    /**\
     * 扣减库存\
     */\
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);\
\
    /**\
     * 恢复库存\
     */\
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);\
\
    /**\
     * 根据查询参数查询产品\
     */\
    List<ProductEntity> queryProduct(ProductQueryParam param);\
\
    /**\
     * 查询产品数量\
     */\
    int queryProductCount(ProductQueryParam param);\
\
    /**\
     * 搜索产品\
     */\
    List<ProductEntity> searchProduct(@Param("keyword") String keyword);\
\
    /**\
     * 查询热门产品\
     */\
    List<ProductEntity> selectHotProducts(@Param("limit") Integer limit);\
\
    /**\
     * 按状态统计数量\
     */\
    int countByStatus(@Param("status") String status);' "$PRODUCT_DAO"
fi

echo "✅ 批量修复完成！"
