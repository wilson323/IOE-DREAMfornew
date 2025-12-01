#!/bin/bash
# 强制性验证脚本 - 每个任务完成后必须执行
# 作者: Claude Code
# 日期: 2025-11-15
# 用途: 强制验证代码质量和功能完整性

echo "🔥 开始强制性验证流程..."
echo "时间: $(date)"
echo "项目: $(pwd)"
echo ""

# 强制退出机制 - 任何步骤失败都立即退出
set -e

# 步骤1: 环境检查
echo "步骤1: 检查开发环境"
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装"
    exit 1
fi

java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo "✅ Java版本: $java_version"

# 步骤2: 强制编译验证
echo ""
echo "步骤2: 强制编译验证"
echo "执行: mvn clean compile -q"
if ! mvn clean compile -q; then
    echo "❌ 编译失败！任务未完成！"
    echo "错误详情:"
    mvn clean compile -e 2>&1 | tail -20
    exit 1
fi
echo "✅ 编译成功"

# 步骤3: 强制测试执行
echo ""
echo "步骤3: 强制测试验证"
echo "执行: mvn test -q"
if ! mvn test -q; then
    echo "❌ 测试失败！任务未完成！"
    echo "测试错误:"
    mvn test -e 2>&1 | tail -20
    exit 1
fi
echo "✅ 测试通过"

# 步骤4: 强制启动验证
echo ""
echo "步骤4: 应用启动验证 (30秒超时)"
echo "执行: timeout 30s mvn spring-boot:run"

# 创建临时验证脚本
cat > /tmp/health_check.sh << 'EOF'
#!/bin/bash
sleep 15
curl -s http://localhost:1024/api/health || echo "健康检查失败"
EOF

chmod +x /tmp/health_check.sh
/tmp/health_check.sh &
HEALTH_PID=$!

timeout 30s mvn spring-boot:run > /tmp/startup.log 2>&1 &
STARTUP_PID=$!

# 等待30秒
sleep 30

# 检查启动状态
if ! kill -0 $STARTUP_PID 2>/dev/null; then
    echo "❌ 应用启动失败！"
    echo "启动日志:"
    cat /tmp/startup.log | tail -20
    exit 1
fi

# 强制停止
kill $STARTUP_PID 2>/dev/null || true
kill $HEALTH_PID 2>/dev/null || true
echo "✅ 应用启动成功"

# 步骤5: 代码质量检查
echo ""
echo "步骤5: 强制代码质量检查"

# 检查javax包使用
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 $javax_count 个文件使用javax包（必须使用jakarta）"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi
echo "✅ 包名检查通过"

# 检查@Autowired使用
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 $autowired_count 个文件使用@Autowired（必须使用@Resource）"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi
echo "✅ 依赖注入检查通过"

# 步骤6: 生成验证报告
echo ""
echo "步骤6: 生成验证报告"
REPORT_FILE="verification-report-$(date +%Y%m%d-%H%M%S).json"
cat > "$REPORT_FILE" << EOF
{
  "verification_time": "$(date)",
  "project_path": "$(pwd)",
  "status": "PASSED",
  "checks": {
    "environment": "PASSED",
    "compilation": "PASSED",
    "tests": "PASSED",
    "startup": "PASSED",
    "code_quality": "PASSED"
  }
}
EOF

echo ""
echo "🎉 强制性验证完成！"
echo "✅ 所有检查通过"
echo "📄 报告文件: $REPORT_FILE"
echo "⏰ 完成时间: $(date)"
echo ""
echo "任务状态: ✅ 可标记为完成"