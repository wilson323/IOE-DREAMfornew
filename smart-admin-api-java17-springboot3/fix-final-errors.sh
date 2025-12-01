#!/bin/bash
echo "修复最终编译错误..."

cd "D:\IOE-DREAM\smart-admin-api-java17-springboot3"

# 修复缺少的符号问题 - 移除残留的Lombok注解
echo "1. 修复残留的Lombok注解..."

# 批量移除 @Accessors 注解
find sa-admin/src/main/java -name "*.java" -exec sed -i '/^@Accessors/d' {} \;

# 批量移除单独的 @Accessor 注解
find sa-admin/src/main/java -name "*.java" -exec sed -i '/^@Accessor/d' {} \;

# 修复 Import 问题 - 移除 lombok.experimental.Accessors 导入
find sa-admin/src/main/java -name "*.java" -exec sed -i '/import lombok\.experimental\.Accessors/d' {} \;

# 修复 WechatPaymentService 中的 Request 导入问题
echo "2. 修复 Request 导入问题..."
wechat_file="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/payment/WechatPaymentService.java"
if [ -f "$wechat_file" ]; then
    # 检查是否需要添加 Request 导入
    if grep -q "Request" "$wechat_file" && ! grep -q "import.*Request" "$wechat_file"; then
        sed -i '11a import jakarta.servlet.http.HttpServletRequest;' "$wechat_file"
        echo "已添加 HttpServletRequest 导入到 WechatPaymentService"
    fi
fi

# 修复 EmailPriority 和 PushPriority 缺失问题
echo "3. 修复优先级枚举导入问题..."
priority_files=(
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/impl/SecurityNotificationServiceImpl.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/EmailService.java"
    "sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/impl/AccountSecurityServiceImpl.java"
)

for file in "${priority_files[@]}"; do
    if [ -f "$file" ]; then
        # 添加缺失的导入
        if grep -q "EmailPriority" "$file" && ! grep -q "import.*EmailPriority" "$file"; then
            sed -i '/^package/a import net.lab1024.sa.admin.module.consume.domain.enums.EmailPriority;' "$file"
        fi
        if grep -q "PushPriority" "$file" && ! grep -q "import.*PushPriority" "$file"; then
            sed -i '/^package/a import net.lab1024.sa.admin.module.consume.domain.enums.PushPriority;' "$file"
        fi
        if grep -q "EmailServiceStatus" "$file" && ! grep -q "import.*EmailServiceStatus" "$file"; then
            sed -i '/^package/a import net.lab1024.sa.admin.module.consume.domain.vo.EmailServiceStatus;' "$file"
        fi
        if grep -q "EmailStatistics" "$file" && ! grep -q "import.*EmailStatistics" "$file"; then
            sed -i '/^package/a import net.lab1024.sa.admin.module.consume.domain.vo.EmailStatistics;' "$file"
        fi
        if grep -q "AccountFreezeHistory" "$file" && ! grep -q "import.*AccountFreezeHistory" "$file"; then
            sed -i '/^package/a import net.lab1024.sa.admin.module.consume.domain.vo.AccountFreezeHistory;' "$file"
        fi
    fi
done

# 修复 RequestEmployee.java 的 @Override 重复问题
echo "4. 修复 RequestEmployee Override 问题..."
employee_file="sa-admin/src/main/java/net/lab1024/sa/admin/module/system/login/domain/RequestEmployee.java"
if [ -f "$employee_file" ]; then
    # 移除重复的 @Override 注解
    sed -i 'N; s/@Override\n\s*@Override/@Override/g' "$employee_file"
    echo "已修复 RequestEmployee Override 重复问题"
fi

# 修复 AdvancedReportServiceImpl 的接口问题
echo "5. 修复 AdvancedReportServiceImpl 接口问题..."
advanced_file="sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/report/impl/AdvancedReportServiceImpl.java"
if [ -f "$advanced_file" ]; then
    # 检查缺失的接口并添加导入
    if grep -q "AdvancedReportService" "$advanced_file" && ! grep -q "import.*AdvancedReportService" "$advanced_file"; then
        sed -i '/^package/a import net.lab1024.sa.admin.module.consume.service.report.AdvancedReportService;' "$advanced_file"
        echo "已添加 AdvancedReportService 导入"
    fi
fi

echo "最终修复完成！现在开始编译验证..."
mvn clean compile -q 2>&1 | grep -c "ERROR"