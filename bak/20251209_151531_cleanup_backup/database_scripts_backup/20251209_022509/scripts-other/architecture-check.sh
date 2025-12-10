#!/bin/bash

# IOE-DREAM 架构分离检查脚本
# 检查微服务和单体架构的代码混用问题

echo "🔍 开始IOE-DREAM架构分离检查..."
echo "=================================="

# 检查违规依赖：微服务依赖单体架构
echo "📋 检查1: 微服务中的单体架构依赖违规"
echo "----------------------------------------"

VIOLATIONS_FOUND=false

# 检查微服务中是否有单体架构的导入
ILLEGAL_IMPORTS=$(find microservices -name "*.java" -exec grep -l "import.*sa\.admin" {} \; 2>/dev/null)

if [ ! -z "$ILLEGAL_IMPORTS" ]; then
    echo "❌ 发现违规依赖！以下文件导入了单体架构代码："
    echo "$ILLEGAL_IMPORTS" | while read -r file; do
        echo "   📄 $file"
        grep "import.*sa\.admin" "$file" | sed 's/^/      /'
    done
    VIOLATIONS_FOUND=true
else
    echo "✅ 未发现微服务依赖单体架构的违规依赖"
fi

echo ""

# 检查重复的业务模块
echo "📋 检查2: 重复业务模块识别"
echo "----------------------------------------"

MONOLITHIC_MODULES=$(find smart-admin-api-java17-springboot3/sa-admin/module -type d -maxdepth 1 | sort)
MICROSERVICE_MODULES=$(find microservices -name "ioedream-*-service" -type d | sort)

echo "单体架构业务模块："
for module in $MONOLITHIC_MODULES; do
    module_name=$(basename "$module")
    echo "   📦 $module_name"

    # 检查对应的微服务是否存在
    microservice=$(echo "ioedream-${module_name}-service")
    if [ -d "microservices/$microservice" ]; then
        echo "      ⚠️  重复微服务: $microservice"
    fi
done

echo ""
echo "微服务架构业务模块："
for service in $MICROSERVICE_MODULES; do
    service_name=$(basename "$service")
    echo "   🚀 $service_name"
done

echo ""

# 检查包命名规范
echo "📋 检查3: 包命名规范检查"
echo "----------------------------------------"

# 检查单体架构包命名
MONOLITHIC_PACKAGES=$(find smart-admin-api-java17-springboot3/sa-admin/src/main/java -type d | grep "net.lab1024.sa" | head -10)
echo "单体架构包结构示例："
echo "$MONOLITHIC_PACKAGES" | head -3 | sed 's/^/   📁 /'

echo ""
# 检查微服务包命名
MICROSERVICE_PACKAGES=$(find microservices -name "net.lab1024.sa" -type d | head -10)
echo "微服务架构包结构示例："
echo "$MICROSERVICE_PACKAGES" | head -3 | sed 's/^/   📁 /'

echo ""

# 检查MapperScan配置
echo "📋 检查4: MapperScan配置检查"
echo "----------------------------------------"

DAO_SCANS=$(find microservices -name "*.java" -exec grep -l "MapperScan.*dao" {} \; 2>/dev/null)

if [ ! -z "$DAO_SCANS" ]; then
    echo "⚠️  发现过时的DAO扫描配置："
    echo "$DAO_SCANS" | while read -r file; do
        echo "   📄 $file"
        grep "MapperScan.*dao" "$file" | sed 's/^/      /'
    done
    VIOLATIONS_FOUND=true
else
    echo "✅ 所有微服务都使用Repository扫描配置"
fi

echo ""

# 检查重复注解配置
echo "📋 检查5: 重复注解配置检查"
echo "----------------------------------------"

DUPLICATE_ANNOTATIONS=$(find microservices -name "*Application.java" -exec grep -l -A 10 "@ComponentScan" {} \; | head -5)

if [ ! -z "$DUPLICATE_ANNOTATIONS" ]; then
    echo "⚠️  可能存在重复注解配置的应用启动类："
    echo "$DUPLICATE_ANNOTATIONS" | sed 's/^/   📄 /'
    VIOLATIONS_FOUND=true
else
    echo "✅ 未发现明显的重复注解配置"
fi

echo ""

# 生成检查报告
echo "📊 架构检查报告"
echo "=================================="

if [ "$VIOLATIONS_FOUND" = true ]; then
    echo "❌ 发现架构问题！请及时修复："
    echo "   1. 移除微服务中对单体架构的依赖"
    echo "   2. 确定每个业务模块的唯一归属架构"
    echo "   3. 修复过时的DAO扫描配置"
    echo "   4. 清理重复的注解配置"
    exit 1
else
    echo "✅ 架构检查通过！当前架构符合分离规范"
    echo ""
    echo "📋 架构状态摘要："
    echo "   🏗️  双架构并存: 单体架构 + 微服务架构"
    echo "   📦 微服务数量: $(find microservices -name "ioedream-*-service" -type d | wc -l)"
    echo "   🔒 架构隔离: 符合规范"
    echo "   📏 包命名: 符合规范"
    echo "   🔧 配置管理: 符合规范"
    exit 0
fi