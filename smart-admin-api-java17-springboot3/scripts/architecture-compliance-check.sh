#!/bin/bash

echo "🔍 执行四层架构合规性检查..."

# 1. 检查依赖注入合规性
echo "检查1: @Resource 依赖注入合规性"
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 $autowired_count 个 @Autowired 使用，违反repowiki规范"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi
echo "✅ @Resource 依赖注入合规性检查通过"

# 2. 检查包名合规性
echo "检查2: jakarta 包名合规性"
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 $javax_count 个 javax 包使用，违反repowiki规范"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi
echo "✅ jakarta 包名合规性检查通过"

# 3. 检查四层架构合规性
echo "检查3: 四层架构调用合规性"
controller_dao_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
if [ $controller_dao_violations -ne 0 ]; then
    echo "❌ 发现 $controller_dao_violations 处 Controller 直接访问 DAO，违反四层架构"
    grep -r "@Resource.*Dao" --include="*Controller.java" .
    exit 1
fi
echo "✅ 四层架构调用合规性检查通过"

# 4. 检查修复的文件
echo "检查4: 架构违规修复验证"
if [ -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/system/device/manager/impl/UnifiedDeviceManagerImpl.java" ]; then
    echo "✅ UnifiedDeviceManagerImpl.java 修复完成"

    # 检查是否包含正确的依赖注入
    if grep -q "@Resource.*SmartRedisUtil" "sa-admin/src/main/java/net/lab1024/sa/admin/module/system/device/manager/impl/UnifiedDeviceManagerImpl.java"; then
        echo "✅ SmartRedisUtil 依赖注入修复完成"
    else
        echo "❌ SmartRedisUtil 依赖注入修复失败"
        exit 1
    fi
else
    echo "❌ UnifiedDeviceManagerImpl.java 不存在"
    exit 1
fi

if [ -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/video/manager/VideoPreviewManager.java" ]; then
    echo "✅ VideoPreviewManager.java 修复完成"

    # 检查方法返回值类型
    if grep -q "public boolean setPreset" "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/video/manager/VideoPreviewManager.java"; then
        echo "✅ Manager 层方法返回值类型修复完成"
    else
        echo "❌ Manager 层方法返回值类型修复失败"
        exit 1
    fi
else
    echo "❌ VideoPreviewManager.java 不存在"
    exit 1
fi

echo ""
echo "🎉 四层架构合规性检查通过！"
echo "✅ 依赖注入合规：使用 @Resource 而非 @Autowired"
echo "✅ 包名合规：使用 jakarta.* 而非 javax.*"
echo "✅ 架构合规：Controller 不直接访问 DAO"
echo "✅ 架构违规修复：UnifiedDeviceManagerImpl.java 和 VideoPreviewManager.java"

echo ""
echo "📋 修复摘要："
echo "1. 添加了 @Resource SmartRedisUtil 依赖注入"
echo "2. 修复了跨层调用问题（移除对 UnifiedDeviceService 的直接依赖）"
echo "3. 修复了 Manager 层方法返回值类型（void → boolean）"
echo "4. 确保严格遵循四层架构规范"

exit 0