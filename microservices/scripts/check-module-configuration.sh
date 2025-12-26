#!/bin/bash
# 配置一致性检查脚本
# 检查各服务配置文件的一致性和完整性

echo "========================================="
echo "IOE-DREAM 配置一致性检查"
echo "========================================="
echo ""

REPORT_FILE="dependency-reports/configuration-check-report.txt"
mkdir -p dependency-reports
echo "配置一致性检查报告" > "$REPORT_FILE"
echo "生成时间: $(date)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查函数
check_service_config() {
    local service=$1
    local service_dir="microservices/$service"

    if [ ! -d "$service_dir" ]; then
        return
    fi

    echo "检查 $service..." | tee -a "$REPORT_FILE"

    # 1. 检查 application.yml 是否存在
    if [ -f "$service_dir/src/main/resources/application.yml" ]; then
        echo "  ✅ application.yml 存在" | tee -a "$REPORT_FILE"
    else
        echo "  ❌ application.yml 缺失" | tee -a "$REPORT_FILE"
    fi

    # 2. 检查配置文件命名规范
    config_files=$(find "$service_dir/src/main/resources" -name "application-*.yml" 2>/dev/null | wc -l)
    echo "  📄 配置文件数量: $config_files" | tee -a "$REPORT_FILE"

    # 3. 检查关键配置项
    app_yml="$service_dir/src/main/resources/application.yml"
    if [ -f "$app_yml" ]; then
        # 检查服务名称配置
        if grep -q "spring.application.name" "$app_yml"; then
            echo "  ✅ spring.application.name 已配置" | tee -a "$REPORT_FILE"
        else
            echo "  ⚠️  spring.application.name 未配置" | tee -a "$REPORT_FILE"
        fi

        # 检查端口配置
        if grep -q "server.port" "$app_yml"; then
            port=$(grep "server.port" "$app_yml" | head -1 | awk '{print $2}')
            echo "  ✅ server.port: $port" | tee -a "$REPORT_FILE"
        else
            echo "  ⚠️  server.port 未配置" | tee -a "$REPORT_FILE"
        fi

        # 检查Nacos配置
        if grep -q "spring.cloud.nacos" "$app_yml"; then
            echo "  ✅ Nacos配置已配置" | tee -a "$REPORT_FILE"
        else
            echo "  ⚠️  Nacos配置未配置" | tee -a "$REPORT_FILE"
        fi

        # 检查数据源配置
        if grep -q "spring.datasource" "$app_yml"; then
            echo "  ✅ 数据源配置已配置" | tee -a "$REPORT_FILE"
        else
            echo "  ℹ️  数据源配置未配置（可能使用默认配置）" | tee -a "$REPORT_FILE"
        fi
    fi

    echo "" >> "$REPORT_FILE"
}

# 检查公共配置模板
echo "检查公共配置模板..." | tee -a "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

if [ -d "microservices/common-config" ]; then
    echo "✅ common-config 目录存在" | tee -a "$REPORT_FILE"
    config_templates=$(find microservices/common-config -name "*.yml" -o -name "*.yaml" | wc -l)
    echo "  公共配置模板数量: $config_templates" | tee -a "$REPORT_FILE"
else
    echo "⚠️  common-config 目录不存在" | tee -a "$REPORT_FILE"
fi

echo "" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 检查所有业务服务
echo "检查业务服务配置..." | tee -a "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

check_service_config "ioedream-gateway-service"
check_service_config "ioedream-common-service"
check_service_config "ioedream-access-service"
check_service_config "ioedream-attendance-service"
check_service_config "ioedream-consume-service"
check_service_config "ioedream-video-service"
check_service_config "ioedream-visitor-service"
check_service_config "ioedream-device-comm-service"
check_service_config "ioedream-oa-service"

# 检查配置模板使用情况
echo "" >> "$REPORT_FILE"
echo "检查配置模板引用情况..." | tee -a "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

for service in ioedream-access-service ioedream-attendance-service ioedream-consume-service; do
    if [ -f "microservices/$service/src/main/resources/application.yml" ]; then
        if grep microservices/common-config "microservices/$service/src/main/resources/application.yml" >/dev/null 2>&1; then
            echo "  ❌ $service: 硬编码 common-config 路径" | tee -a "$REPORT_FILE"
        else
            echo "  ✅ $service: 使用配置模板" | tee -a "$REPORT_FILE"
        fi
    fi
done

echo "" >> "$REPORT_FILE"
echo "检查完成" >> "$REPORT_FILE"

echo "========================================="
echo "报告已生成: $REPORT_FILE"
echo "========================================="

# 显示统计摘要
total_checks=$(grep -c "检查\|✅\|❌\|⚠️" "$REPORT_FILE" || true)
passed=$(grep -c "✅" "$REPORT_FILE" || true)
warnings=$(grep -c "⚠️" "$REPORT_FILE" || true)
errors=$(grep -c "❌" "$REPORT_FILE" || true)

echo ""
echo "检查统计："
echo "  总检查项: $total_checks"
echo "  通过: $passed"
echo "  警告: $warnings"
echo "  错误: $errors"
echo ""
