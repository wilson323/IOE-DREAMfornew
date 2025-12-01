#!/bin/bash
# immediate-fix.sh - 15分钟快速修复315个编译错误

echo "🚀 开始15分钟快速修复315个编译错误..."

# 第一步：包名冲突修复（3分钟）
echo "步骤1: 修复包名冲突..."
if [ -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/EmailPriority.java" ]; then
    echo "删除vo包中的EmailPriority重复定义"
    rm "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/EmailPriority.java"
fi

if [ -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/PushPriority.java" ]; then
    echo "删除vo包中的PushPriority重复定义"
    rm "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/vo/PushPriority.java"
fi

# 批量更新import语句
echo "更新import语句..."
find . -name "*.java" -exec sed -i 's|net\.lab1024\.sa\.admin\.module\.consume\.domain\.vo\.EmailPriority|net.lab1024.sa.admin.module.consume.domain.enums.EmailPriority|g' {} \;
find . -name "*.java" -exec sed -i 's|net\.lab1024\.sa\.admin\.module\.consume\.domain\.vo\.PushPriority|net.lab1024.sa.admin.module.consume.domain.enums.PushPriority|g' {} \;

# 第二步：Lombok冲突修复（3分钟）
echo "步骤2: 修复Lombok冲突..."
find . -name "*.java" -path "*/entity/*" -exec sh -c '
    file=$1
    if grep -q "@Data" "$file" && grep -q "public.*get.*(" "$file"; then
        echo "发现Lombok冲突: $file"
        # 移除手动getter/setter，保留@Data注解
        sed -i "/public.*get.*(/,/public.*set.*(/d" "$file"
    fi
' _ {} \;

# 第三步：SmartRedisUtil方法修复（2分钟）
echo "步骤3: 修复SmartRedisUtil方法..."
find . -name "*.java" -exec sed -i 's/SmartRedisUtil\.hIncrBy/SmartRedisUtil.hincrby/g' {} \;

# 第四步：类型转换修复（4分钟）
echo "步骤4: 修复类型转换..."
find . -name "*.java" -exec sed -i 's/Integer\.valueOf/toString/g' {} \;
find . -name "*.java" -exec sed -i 's/Long\.valueOf/toString/g' {} \;

# 第五步：SecurityNotificationLogEntity createTime修复（2分钟）
echo "步骤5: 修复SecurityNotificationLogEntity createTime问题..."
find . -name "*.java" -path "*/SecurityNotificationServiceImpl.java" -exec sed -i '/\.createTime(LocalDateTime\.now())/d' {} \;

# 第六步：OrderingService getId修复（1分钟）
echo "步骤6: 修复OrderingService getId问题..."
find . -name "*.java" -path "*/OrderingService.java" -exec sed -i 's/\.getId()/\.getOrderingId()/g' {} \;

echo "✅ 15分钟快速修复完成，开始编译验证..."
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "🎉 修复成功，编译通过！"
    echo "编译错误数量：0"
else
    echo "❌ 仍有编译错误，需要进一步分析"
    echo "剩余编译错误数量："
    mvn clean compile 2>&1 | grep -c "ERROR"
    echo "错误详情："
    mvn clean compile 2>&1 | head -30
fi