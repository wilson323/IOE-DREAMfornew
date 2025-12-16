#!/bin/bash
# ============================================================
# IOE-DREAM 微服务边界检查工具
# 检查服务职责边界清晰度，识别架构违规
# ============================================================

echo "🔍 开始微服务边界检查..."
echo "检查时间: $(date)"
echo "=================================="

# 创建检查报告
report_file="scripts/architecture/p1-optimization/SERVICE_BOUNDARY_CHECK_REPORT.md"
cat > "$report_file" << 'REPORT_EOF'
# IOE-DREAM 微服务边界检查报告

## 📊 检查概览

- **检查时间**: $(date)
- **检查范围**: 全部微服务
- **检查标准**: 服务职责边界清晰度
- **严重等级**: P1级架构优化

## 🔍 边界违规检查结果

### 1. 跨域业务逻辑违规

#### 违规统计
- **总违规数**: ${boundary_violations}
- **涉及服务**: ${violated_services}
- **严重等级**: HIGH

#### 违规详情
REPORT_EOF

boundary_violations=0
violated_services=0

# 检查各微服务边界
for service_dir in microservices/ioedream-*; do
    if [ -d "$service_dir" ]; then
        service_name=$(basename "$service_dir")
        service_violations=0

        echo "检查服务: $service_name"

        # 检查Controller中的跨域调用
        controller_files=$(find "$service_dir" -name "*Controller.java" 2>/dev/null)
        for controller in $controller_files; do
            # 检查是否有跨域服务调用
            if grep -q "accessService\|attendanceService\|consumeService\|visitorService" "$controller"; then
                echo "  ❌ 发现跨域调用: $(basename "$controller")"
                ((boundary_violations++))
                ((service_violations++))

                # 记录到报告
                echo "- **$service_name**: $(basename "$controller")" >> "$report_file"
                echo "  - 违规类型: Controller跨域调用" >> "$report_file"
                echo "  - 建议: 通过GatewayServiceClient调用" >> "$report_file"
                echo "" >> "$report_file"
            fi
        done

        # 检查Service中的跨域数据访问
        service_files=$(find "$service_dir" -name "*Service*.java" 2>/dev/null)
        for service in $service_files; do
            # 检查是否有跨域数据库访问
            if grep -q "AccessDao\|AttendanceDao\|ConsumeDao\|VisitorDao" "$service"; then
                echo "  ❌ 发现跨域数据访问: $(basename "$service")"
                ((boundary_violations++))
                ((service_violations++))

                # 记录到报告
                echo "- **$service_name**: $(basename "$service")" >> "$report_file"
                echo "  - 违规类型: Service跨域数据访问" >> "$report_file"
                echo "  - 建议: 通过API Gateway调用" >> "$report_file"
                echo "" >> "$report_file"
            fi
        done

        if [ $service_violations -gt 0 ]; then
            ((violated_services++))
        fi
    fi
done

# 继续生成报告
cat >> "$report_file" << REPORT_EOF

### 2. 重复服务实现

#### 重复统计
- **重复服务数**: ${duplicate_services}
- **重复实例数**: ${duplicate_instances}

#### 重复详情
REPORT_EOF

duplicate_services=0
duplicate_instances=0

# 检查重复服务实现
service_patterns=("UserService" "AuthService" "CommonService" "ConfigService")
for pattern in "${service_patterns[@]}"; do
    service_count=$(find microservices -name "*${pattern}*.java" 2>/dev/null | wc -l)
    if [ $service_count -gt 1 ]; then
        ((duplicate_services++))
        ((duplicate_instances+=service_count))

        echo "❌ 发现重复服务: $pattern ($service_count 个实现)"

        # 记录到报告
        echo "- **$pattern**: $service_count 个重复实现" >> "$report_file"
        find microservices -name "*${pattern}*.java" 2>/dev/null | sed 's/^/  - /' >> "$report_file"
        echo "" >> "$report_file"
    fi
done

# 生成修复建议
cat >> "$report_file" << REPORT_EOF

## 🔧 修复建议

### 高优先级修复

1. **消除跨域服务调用**
   - 所有跨服务调用必须通过GatewayServiceClient
   - 移除直接的Service注入
   - 实现服务解耦

2. **统一公共服务实现**
   - 将重复的UserService合并到common-service
   - 移除业务服务中的公共服务实现
   - 建立统一的服务接口

3. **明确数据边界**
   - 每个服务只能访问自己的数据库
   - 跨域数据访问通过API调用
   - 实现数据所有权管理

### 中优先级修复

1. **服务职责重新划分**
   - 基于业务能力重新设计服务边界
   - 消除服务间的职责重叠
   - 建立清晰的职责矩阵

2. **API网关统一调用**
   - 所有东西向调用通过网关
   - 实现服务调用监控和追踪
   - 建立调用链路管理

## 📈 优化预期

- **架构清晰度**: 提升50%
- **服务耦合度**: 降低60%
- **维护复杂度**: 降低40%
- **扩展性**: 提升70%

---

**检查完成时间**: $(date)
**检查工具**: IOE-DREAM Service Boundary Checker
**下次检查**: 建议每周执行一次
REPORT_EOF

echo "=================================="
echo "✅ 微服务边界检查完成！"
echo "=================================="

echo "📊 检查结果统计:"
echo "边界违规: $boundary_violations 个"
echo "重复服务: $duplicate_services 个"
echo "重复实例: $duplicate_instances 个"
echo "涉及服务: $violated_services 个"
echo "=================================="

echo "📄 详细报告: $report_file"
echo "🔧 修复建议: 请参考报告中的修复建议"
echo "=================================="

echo "🚨 立即行动项:"
echo "1. 修复跨域服务调用 (P1优先级)"
echo "2. 合并重复服务实现 (P1优先级)"
echo "3. 明确数据访问边界 (P2优先级)"
echo "4. 建立服务调用规范 (P2优先级)"
echo "=================================="
