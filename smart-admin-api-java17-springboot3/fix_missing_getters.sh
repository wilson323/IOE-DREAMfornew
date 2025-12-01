#!/bin/bash

echo "🔥 修复缺失的getter/setter方法..."

# 修复CreateOrderingRequest类
echo "修复CreateOrderingRequest..."
CREATE_ORDERING="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/CreateOrderingRequest.java"
if [ -f "$CREATE_ORDERING" ]; then
    # 添加缺失的getter方法
    if ! grep -q "getItems" "$CREATE_ORDERING"; then
        sed -i '/^}$/i\
\
    public List<OrderingItemRequest> getItems() {\
        return items;\
    }\
\
    public String getTableNo() {\
        return tableNo;\
    }\
\
    public String getUserId() {\
        return userId;\
    }\
\
    public BigDecimal getDiscountRate() {\
        return discountRate;\
    }' "$CREATE_ORDERING"
    fi
fi

# 修复OrderingItemRequest类
echo "修复OrderingItemRequest..."
ORDERING_ITEM="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/dto/OrderingItemRequest.java"
if [ -f "$ORDERING_ITEM" ]; then
    if ! grep -q "getMenuId" "$ORDERING_ITEM"; then
        sed -i '/^}$/i\
\
    public Long getMenuId() {\
        return menuId;\
    }\
\
    public Integer getQuantity() {\
        return quantity;\
    }' "$ORDERING_ITEM"
    fi
fi

# 修复RequestUser类
echo "修复RequestUser..."
REQUEST_USER="sa-base/src/main/java/net/lab1024/sa/base/common/domain/RequestUser.java"
if [ -f "$REQUEST_USER" ]; then
    if ! grep -q "getRoles" "$REQUEST_USER"; then
        sed -i '/^}$/i\
\
    public List<String> getRoles() {\
        return roles;\
    }' "$REQUEST_USER"
    fi
fi

echo "✅ getter/setter修复完成！"
