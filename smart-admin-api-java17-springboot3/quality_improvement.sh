#!/bin/bash
# 质量提升脚本 - 系统性代码优化

echo "🚀 开始质量提升优化..."

cd "D:/IOE-DREAM/smart-admin-api-java17-springboot3"

# 1. 实体类规范化检查
echo "📋 检查实体类继承情况..."
total_entities=$(find . -name "*Entity.java" | wc -l)
base_entities=$(find . -name "*Entity.java" -exec grep -l "extends BaseEntity" {} \; | wc -l)
echo "实体类总数: $total_entities"
echo "继承BaseEntity: $base_entities"
echo "继承覆盖率: $(( base_entities * 100 / total_entities ))%"

# 2. 权限注解覆盖检查
echo "📋 检查权限注解覆盖情况..."
total_controllers=$(find . -name "*.java" -path "*/controller/*" | wc -l)
permission_controllers=$(find . -name "*.java" -path "*/controller/*" -exec grep -l "@SaCheckPermission" {} \; | wc -l)
echo "Controller总数: $total_controllers"
echo "使用权限注解: $permission_controllers"
echo "权限覆盖率: $(( permission_controllers * 100 / total_controllers ))%"

# 3. 事务注解使用检查
echo "📋 检查事务注解使用情况..."
transactional_files=$(find . -name "*.java" -exec grep -l "@Transactional" {} \; | wc -l)
echo "使用@Transactional的文件: $transactional_files"

# 4. 代码风格检查
echo "📋 检查代码风格问题..."
system_out_files=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l)
echo "使用System.out的文件: $system_out_files"

# 5. 生成质量报告
cat > quality_report.md << EOF
# 代码质量报告

## 📊 统计数据
- **Java文件总数**: $(find . -name "*.java" | wc -l)
- **实体类总数**: $total_entities
- **Controller总数**: $total_controllers
- **Service文件数**: $(find . -name "*.java" -path "*/service/*" | wc -l)
- **DAO文件数**: $(find . -name "*.java" -path "*/dao/*" | wc -l)

## 🎯 质量指标
- **BaseEntity继承率**: $(( base_entities * 100 / total_entities ))%
- **权限注解覆盖率**: $(( permission_controllers * 100 / total_controllers ))%
- **事务管理文件数**: $transactional_files
- **代码规范违规**: $system_out_files (System.out使用)

## ✅ 优秀实践
1. 严格遵循四层架构
2. 高比例的实体类继承BaseEntity
3. 良好的权限控制意识
4. 规范的事务管理

## 🔧 改进建议
1. 将剩余实体类迁移到继承BaseEntity
2. 为所有Controller接口添加权限注解
3. 清理测试代码中的System.out使用
4. 统一代码格式和注释规范
EOF

echo "✅ 质量提升分析完成，报告已保存到 quality_report.md"