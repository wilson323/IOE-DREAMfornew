#!/bin/bash
# 依赖违规检查脚本
# 检查业务服务是否违反依赖规范

echo "========================================="
echo "IOE-DREAM 依赖违规检查"
echo "========================================="
echo ""

REPORT_FILE="dependency-reports/violations-report.txt"
mkdir -p dependency-reports
echo "依赖违规检查报告" > "$REPORT_FILE"
echo "生成时间: $(date)" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# 检查函数
check_service() {
    local service=$1
    local pom_file="microservices/$service/pom.xml"

    if [ ! -f "$pom_file" ]; then
        return
    fi

    echo "检查 $service..." | tee -a "$REPORT_FILE"

    # 检查是否同时依赖 microservices-common 和细粒度模块
    has_common=$(grep -c "<artifactId>microservices-common</artifactId>" "$pom_file" || true)
    has_fine_grained=$(grep -E "<artifactId>microservices-common-(core|entity|data|cache|security|monitor|storage|export|workflow|business|permission)</artifactId>" "$pom_file" | wc -l)

    if [ $has_common -gt 0 ] && [ $has_fine_grained -gt 0 ]; then
        echo "  ❌ 违规: 同时依赖 microservices-common 和 $has_fine_grained 个细粒度模块" | tee -a "$REPORT_FILE"
        echo "     业务服务应该只依赖细粒度模块，不应依赖聚合模块" | tee -a "$REPORT_FILE"
    elif [ $has_common -gt 0 ]; then
        # 检查是否是网关服务（网关服务可以依赖microservices-common）
        if [[ "$service" == *"gateway"* ]]; then
            echo "  ✅ 网关服务允许依赖 microservices-common" | tee -a "$REPORT_FILE"
        else
            echo "  ⚠️  警告: 业务服务依赖 microservices-common 聚合模块" | tee -a "$REPORT_FILE"
        fi
    else
        echo "  ✅ 符合规范: 依赖 $has_fine_grained 个细粒度模块" | tee -a "$REPORT_FILE"
    fi

    echo "" >> "$REPORT_FILE"
}

# 检查所有业务服务
echo "检查业务服务依赖..." >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

check_service "ioedream-access-service"
check_service "ioedream-attendance-service"
check_service "ioedream-consume-service"
check_service "ioedream-video-service"
check_service "ioedream-visitor-service"

echo "" >> "$REPORT_FILE"
echo "检查完成" >> "$REPORT_FILE"

echo "========================================="
echo "报告已生成: $REPORT_FILE"
echo "========================================="
