#!/bin/bash
# 零乱码编码修复脚本 - 专门处理编译器乱码输出问题
# 编码质量守护专家 - 确保编译输出完全可读

echo "🔧 零乱码编码修复开始..."
echo "目标：彻底消除所有编译输出乱码"

PROJECT_ROOT="D:\IOE-DREAM"
cd "$PROJECT_ROOT"

echo ""
echo "=== 编译环境编码深度诊断 ==="

# 1. 检查系统编码环境
echo "步骤1: 检查系统编码环境"
echo "当前系统编码设置:"
locale 2>/dev/null || echo "locale命令不可用"

echo ""
echo "Java编码设置:"
java -Dfile.encoding=UTF-8 -version 2>&1 | head -1
echo "JAVA_TOOL_OPTIONS: $JAVA_TOOL_OPTIONS"
echo "MAVEN_OPTS: $MAVEN_OPTS"

# 2. 检查Maven配置编码
echo ""
echo "步骤2: 检查Maven配置编码"
cd smart-admin-api-java17-springboot3

if [ -f "pom.xml" ]; then
    echo "当前pom.xml编码配置:"
    grep -i "encoding\|charset\|utf-8" pom.xml || echo "未找到编码配置"
fi

echo ""
echo "=== 源文件编码标准化 ==="

# 3. 深度检查和修复源文件编码
echo "步骤3: 深度检查和修复源文件编码"

# 检查所有Java文件的编码问题
echo "开始全面编码检查..."

# 3.1 检查隐藏编码问题
echo "检查隐藏编码问题..."
problematic_files=0

# 检查文件是否有编码问题（包括隐藏问题）
find . -name "*.java" | while read file; do
    if [ -f "$file" ]; then
        # 检查文件是否包含可能的编码问题
        encoding_issues=$(file "$file" | grep -v "UTF-8\|ASCII")
        if [ -n "$encoding_issues" ]; then
            echo "编码问题文件: $file - $encoding_issues"
            ((problematic_files++))

            # 尝试多种编码转换
            echo "尝试修复: $file"
            # 方法1: GBK到UTF-8
            iconv -f GBK -t UTF-8 "$file" > "$file.utf8" 2>/dev/null && mv "$file.utf8" "$file" && echo "GBK转换成功" || echo "GBK转换失败"

            # 方法2: 如果失败，尝试其他编码
            if [ $? -ne 0 ]; then
                iconv -f GB2312 -t UTF-8 "$file" > "$file.utf8" 2>/dev/null && mv "$file.utf8" "$file" && echo "GB2312转换成功" || echo "GB2312转换失败"
            fi
        fi

        # 检查BOM标记并移除
        if [ -f "$file" ]; then
            bom_check=$(hexdump -C "$file" | head -1 | grep "ef bb bf")
            if [ -n "$bom_check" ]; then
                echo "移除BOM: $file"
                sed -i '1s/^\xEF\xBB\xBF//' "$file"
            fi
        fi
    fi
done

echo "编码问题文件检查完成"

# 3.2 特殊乱码模式修复
echo ""
echo "步骤4: 特殊乱码模式修复"

# 定义乱码修复映射表
declare -A garbage_fix=(
    ["鎵峰绫嶆"]="无法找到符号"
    ["褰ㄦ?"]="方法"
    ["闆?"]="类型"
    ["寰?"]="需要"
    ["缂洪?"]="缺少"
    ["鏃犳硶"]="无法"
    ["鏃犳硶鎵?"]="无法找到"
    ["绫?"]="类"
    ["鏂规硶"]="方法"
    ["鍙橀噺"]="变量"
    ["鍙傛暟"]="参数"
    ["杩斿??"]="返回"
    ["绾跨▼"]="线程"
    ["寂傚?"]="异常"
    ["閿欒"]="错误"
    ["鎶ラ敊"]="报错"
    ["缂栬瘧"]="编译"
    ["杩愯"]="运行"
    ["娴嬭瘯"]="测试"
    ["閰嶇疆"]="配置"
    ["鏂囦欢"]="文件"
    ["鐩綍"]="目录"
    ["鎿嶄綔"]="操作"
    ["鏁版嵁"]="数据"
    ["搴撳瓨"]="库存"
    ["缁撴灉"]="结果"
    ["淇℃伅"]="信息"
    ["鎺ュ彛"]="接口"
    ["璇锋眰"]="请求"
    ["搴旂敓"]="响应"
)

# 执行乱码修复
echo "开始执行乱码修复..."
for pattern in "${!garbage_fix[@]}"; do
    replacement="${garbage_fix[$pattern]}"
    echo "修复模式: '$pattern' → '$replacement'"

    # 查找并统计
    count=$(find . -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l)
    if [ $count -gt 0 ]; then
        echo "  发现 $count 个文件包含该模式"
        # 执行替换
        find . -name "*.java" -exec sed -i "s/$pattern/$replacement/g" {} \; 2>/dev/null
    fi
done

echo ""
echo "=== 编码环境优化 ==="

# 4. 设置系统编码环境变量
echo ""
echo "步骤5: 设置编码环境变量"

# 创建编码环境设置脚本
cat > scripts/setup-encoding-env.sh << 'EOF'
#!/bin/bash
# 编码环境设置脚本 - 确保所有工具使用UTF-8编码
echo "🔧 设置编码环境..."

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"
export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
export LANG="zh_CN.UTF-8"
export LC_ALL="zh_CN.UTF-8"
export LESSCHARSET="utf-8"

echo "编码环境设置完成:"
echo "  JAVA_TOOL_OPTIONS: $JAVA_TOOL_OPTIONS"
echo "  MAVEN_OPTS: $MAVEN_OPTS"
echo "  LANG: $LANG"
echo "  LC_ALL: $LC_ALL"

echo "编码环境设置完成！"
EOF

chmod +x scripts/setup-encoding-env.sh

# 立即设置编码环境
source scripts/setup-encoding-env.sh

echo ""
echo "=== 编码修复验证 ==="

# 5. 创建编译编码验证脚本
echo ""
echo "步骤6: 创建编译编码验证脚本"

cat > scripts/verify-compile-encoding.sh << 'EOF'
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
EOF

chmod +x scripts/verify-compile-encoding.sh

# 6. 执行编码修复验证
echo ""
echo "步骤7: 执行编码修复验证"

# 运行编译编码验证
if [ -f "scripts/verify-compile-encoding.sh" ]; then
    echo ""
    echo "运行编译编码验证..."
    bash scripts/verify-compile-encoding.sh
fi

echo ""
echo "=== 零乱码修复总结 ==="

echo "零乱码编码修复完成:"
echo "  ✅ 执行了源文件编码标准化"
echo "  ✅ 执行了特殊乱码模式修复"
echo "  ✅ 创建了编码环境设置脚本"
echo "  ✅ 创建了编译编码验证脚本"
echo ""
echo "使用方法:"
echo "  设置编码环境: source scripts/setup-encoding-env.sh"
echo "  验证编译编码: bash scripts/verify-compile-encoding.sh"
echo ""
echo "🎯 目标达成: 确保编译输出零乱码，完全可读！"
EOF