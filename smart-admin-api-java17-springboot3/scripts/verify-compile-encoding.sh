#!/bin/bash
# 编译编码验证脚本 - 确保编译输出完全可读
echo "🔍 验证编译编码质量..."

PROJECT_ROOT="D:\IOE-DREAM"
cd "$PROJECT_ROOT/smart-admin-api-java17-springboot3"

# 设置编码环境
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
export LANG="zh_CN.UTF-8"
export LC_ALL="zh_CN.UTF-8"

echo "执行编译测试..."
echo "编码环境设置:"
echo "  JAVA_TOOL_OPTIONS: $JAVA_TOOL_OPTIONS"
echo "  MAVEN_OPTS: $MAVEN_OPTS"
echo "  LANG: $LANG"
echo "  LC_ALL: $LC_ALL"

# 执行编译并捕获输出
compile_output=$(mvn clean compile -DskipTests 2>&1)
compile_status=$?

echo ""
echo "编译状态: $compile_status"
echo ""

# 检查输出中是否还有乱码
garbage_patterns=("????" "涓?" "鏂?" "锟斤拷" "鎵峰绫嶆" "褰ㄦ?" "闆?" "寰?" "缂洪?")
has_garbage=0

for pattern in "${garbage_patterns[@]}"; do
    if echo "$compile_output" | grep -q "$pattern"; then
        echo "❌ 发现乱码模式: $pattern"
        has_garbage=1
        # 显示包含乱码的行
        echo "$compile_output" | grep "$pattern" | head -3
    fi
done

if [ $has_garbage -eq 0 ]; then
    echo "✅ 编译输出编码检查通过"
else
    echo "❌ 编译输出仍有乱码问题"
fi

# 如果编译成功，显示部分输出
if [ $compile_status -eq 0 ]; then
    echo ""
    echo "编译成功，输出样例（前10行）:"
    echo "$compile_output" | head -10
else
    echo ""
    echo "编译失败，错误信息:"
    echo "$compile_output" | head -20
fi

exit $compile_status
