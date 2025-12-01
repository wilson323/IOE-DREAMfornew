#!/bin/bash

# =============================================================================
# IOE-DREAM 实时错误追踪器
# 功能：详细分析和分类405个编译错误，提供精准修复指导
# 创建时间：2025-11-18
# 版本：v1.0.0
# =============================================================================

PROJECT_ROOT="D:\IOE-DREAM"
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"
ERRORS_DIR="$PROJECT_ROOT/errors_analysis"
DETAILED_ERRORS_FILE="$ERRORS_DIR/detailed_errors.json"
ERRORS_BY_FILE="$ERRORS_DIR/errors_by_file.json"
ERRORS_BY_TYPE="$ERRORS_DIR/errors_by_type.json"
FIX_RECOMMENDATIONS="$ERRORS_DIR/fix_recommendations.json"

# 创建分析目录
mkdir -p "$ERRORS_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

echo -e "${CYAN}🔍 开始详细分析405个编译错误...${NC}"

# 1. 获取详细编译错误信息
get_detailed_errors() {
    cd "$BACKEND_DIR"

    echo -e "${BLUE}📋 正在获取详细编译错误...${NC}"

    # 获取完整编译输出
    mvn compile -q 2>&1 > full_compile_output.log

    # 提取所有错误行
    grep -n "ERROR\|cannot find symbol\|package.*does not exist\|duplicate method\|cannot resolve" full_compile_output.log > errors_lines.log

    local error_count=$(wc -l < errors_lines.log)
    echo -e "发现 ${RED}$error_count${NC} 个错误行"

    # 分析每个错误的详细信息
    python3 << 'EOF' > "$DETAILED_ERRORS_FILE"
import re
import json
from collections import defaultdict

with open('errors_lines.log', 'r', encoding='utf-8') as f:
    lines = f.readlines()

errors = []
current_error = None
error_id = 1

for line in lines:
    line = line.strip()

    # 匹配错误位置 [ERROR] /path/to/file.java:[line]
    location_match = re.search(r'\[ERROR\] (.*\.java):(\d+)', line)
    if location_match:
        if current_error:
            errors.append(current_error)

        file_path = location_match.group(1)
        line_number = location_match.group(2)

        current_error = {
            "id": error_id,
            "file": file_path,
            "line": int(line_number),
            "raw_error": line,
            "error_lines": [line]
        }
        error_id += 1
        continue

    # 匹配错误类型
    if 'cannot find symbol' in line:
        current_error["type"] = "cannot_find_symbol"
    elif 'package.*does not exist' in line:
        current_error["type"] = "package_not_found"
    elif 'duplicate method' in line:
        current_error["type"] = "duplicate_method"
    elif 'cannot resolve' in line:
        current_error["type"] = "cannot_resolve"
    elif 'javax\.' in line:
        current_error["type"] = "jakarta_migration"
    elif '@Autowired' in line:
        current_error["type"] = "autowired_issue"

    if current_error:
        current_error["error_lines"].append(line)

# 添加最后一个错误
if current_error:
    errors.append(current_error)

print(json.dumps(errors, indent=2, ensure_ascii=False))
EOF

    rm -f errors_lines.log
    echo -e "${GREEN}✅ 详细错误分析完成${NC}"
}

# 2. 按文件分组分析
analyze_errors_by_file() {
    echo -e "${BLUE}📁 按文件分析错误分布...${NC}"

    python3 << 'EOF' > "$ERRORS_BY_FILE.json"
import json

with open("$DETAILED_ERRORS_FILE", 'r', encoding='utf-8') as f:
    errors = json.load(f)

errors_by_file = defaultdict(lambda: {
    "file": "",
    "error_count": 0,
    "error_types": defaultdict(int),
    "lines": [],
    "module": ""
})

for error in errors:
    file_path = error["file"]
    error_type = error.get("type", "unknown")

    # 确定模块
    if "controller" in file_path.lower():
        module = "controller"
    elif "service" in file_path.lower():
        module = "service"
    elif "dao" in file_path.lower() or "mapper" in file_path.lower():
        module = "dao"
    elif "entity" in file_path.lower():
        module = "entity"
    elif "manager" in file_path.lower():
        module = "manager"
    else:
        module = "other"

    errors_by_file[file_path]["file"] = file_path
    errors_by_file[file_path]["error_count"] += 1
    errors_by_file[file_path]["error_types"][error_type] += 1
    errors_by_file[file_path]["lines"].append(error["line"])
    errors_by_file[file_path]["module"] = module

# 转换为列表并排序
result = []
for file_path, data in errors_by_file.items():
    result.append(data)

# 按错误数量排序
result.sort(key=lambda x: x["error_count"], reverse=True)

print(json.dumps(result, indent=2, ensure_ascii=False))
EOF

    echo -e "${GREEN}✅ 文件错误分布分析完成${NC}"
}

# 3. 按类型分组分析
analyze_errors_by_type() {
    echo -e "${BLUE}🏷️ 按类型分析错误分布...${NC}"

    python3 << 'EOF' > "$ERRORS_BY_TYPE.json"
import json
from collections import defaultdict

with open("$DETAILED_ERRORS_FILE", 'r', encoding='utf-8') as f:
    errors = json.load(f)

errors_by_type = defaultdict(lambda: {
    "type": "",
    "count": 0,
    "description": "",
    "files": set(),
    "fix_difficulty": "medium",
    "estimated_fix_time": 30,
    "examples": []
})

type_descriptions = {
    "cannot_find_symbol": {
        "desc": "找不到符号错误，通常是缺少导入或类定义",
        "difficulty": "medium",
        "time": 20
    },
    "package_not_found": {
        "desc": "包不存在错误，通常是依赖问题或包名错误",
        "difficulty": "easy",
        "time": 10
    },
    "duplicate_method": {
        "desc": "重复方法定义，需要删除或重命名",
        "difficulty": "easy",
        "time": 5
    },
    "cannot_resolve": {
        "desc": "无法解析符号，类型或方法不存在",
        "difficulty": "medium",
        "time": 15
    },
    "jakarta_migration": {
        "desc": "Jakarta迁移问题，需要修改包名",
        "difficulty": "easy",
        "time": 5
    },
    "autowired_issue": {
        "desc": "Autowired注解问题，需要替换为Resource",
        "difficulty": "easy",
        "time": 5
    },
    "unknown": {
        "desc": "未知类型错误，需要详细分析",
        "difficulty": "hard",
        "time": 45
    }
}

for error in errors:
    error_type = error.get("type", "unknown")
    file_path = error["file"]

    errors_by_type[error_type]["type"] = error_type
    errors_by_type[error_type]["count"] += 1
    errors_by_type[error_type]["files"].add(file_path)

    if error_type in type_descriptions:
        errors_by_type[error_type]["description"] = type_descriptions[error_type]["desc"]
        errors_by_type[error_type]["fix_difficulty"] = type_descriptions[error_type]["difficulty"]
        errors_by_type[error_type]["estimated_fix_time"] = type_descriptions[error_type]["time"]

    # 添加示例（最多3个）
    if len(errors_by_type[error_type]["examples"]) < 3:
        errors_by_type[error_type]["examples"].append({
            "file": file_path,
            "line": error["line"],
            "raw_error": error["raw_error"]
        })

# 转换set为list并排序
result = []
for error_type, data in errors_by_type.items():
    data["files"] = list(data["files"])
    data["affected_files_count"] = len(data["files"])
    result.append(data)

# 按错误数量排序
result.sort(key=lambda x: x["count"], reverse=True)

print(json.dumps(result, indent=2, ensure_ascii=False))
EOF

    echo -e "${GREEN}✅ 错误类型分析完成${NC}"
}

# 4. 生成修复建议
generate_fix_recommendations() {
    echo -e "${BLUE}💡 生成修复建议...${NC}"

    python3 << 'EOF' > "$FIX_RECOMMENDATIONS.json"
import json

with open("$ERRORS_BY_TYPE.json", 'r', encoding='utf-8') as f:
    errors_by_type = json.load(f)

recommendations = []

for error_type_data in errors_by_type:
    error_type = error_type_data["type"]
    count = error_type_data["count"]
    difficulty = error_type_data["fix_difficulty"]
    estimated_time = error_type_data["estimated_fix_time"]
    affected_files = error_type_data["affected_files_count"]

    if error_type == "jakarta_migration":
        recommendations.append({
            "priority": 1,
            "error_type": error_type,
            "count": count,
            "description": "批量修复javax包名到jakarta",
            "command": "find . -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;",
            "estimated_time": estimated_time * count / 60,  # 分钟
            "difficulty": difficulty,
            "success_rate": 95,
            "prerequisites": [],
            "impact": "high"
        })

    elif error_type == "autowired_issue":
        recommendations.append({
            "priority": 2,
            "error_type": error_type,
            "count": count,
            "description": "批量替换@Autowired为@Resource",
            "command": "find . -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;",
            "estimated_time": estimated_time * count / 60,
            "difficulty": difficulty,
            "success_rate": 90,
            "prerequisites": [],
            "impact": "high"
        })

    elif error_type == "duplicate_method":
        recommendations.append({
            "priority": 3,
            "error_type": error_type,
            "count": count,
            "description": "修复重复方法定义",
            "command": "需要手动检查每个重复方法",
            "estimated_time": estimated_time * count,
            "difficulty": difficulty,
            "success_rate": 85,
            "prerequisites": ["代码审查", "理解业务逻辑"],
            "impact": "medium"
        })

    elif error_type == "package_not_found":
        recommendations.append({
            "priority": 4,
            "error_type": error_type,
            "count": count,
            "description": "修复包导入问题",
            "command": "检查和添加缺失的依赖包",
            "estimated_time": estimated_time * count,
            "difficulty": difficulty,
            "success_rate": 80,
            "prerequisites": ["Maven依赖检查"],
            "impact": "medium"
        })

    elif error_type == "cannot_find_symbol":
        recommendations.append({
            "priority": 5,
            "error_type": error_type,
            "count": count,
            "description": "补充缺失的类和方法定义",
            "command": "需要分析每个符号缺失的原因",
            "estimated_time": estimated_time * count * 2,
            "difficulty": difficulty,
            "success_rate": 75,
            "prerequisites": ["完整的项目理解"],
            "impact": "high"
        })

# 按优先级排序
recommendations.sort(key=lambda x: x["priority"])

print(json.dumps(recommendations, indent=2, ensure_ascii=False))
EOF

    echo -e "${GREEN}✅ 修复建议生成完成${NC}"
}

# 5. 显示分析报告
show_analysis_report() {
    echo -e "\n${CYAN}========================================${NC}"
    echo -e "${WHITE}📊 IOE-DREAM 编译错误详细分析报告${NC}"
    echo -e "${CYAN}========================================${NC}\n"

    # 总体统计
    if [ -f "$DETAILED_ERRORS_FILE" ]; then
        local total_errors=$(jq '. | length' "$DETAILED_ERRORS_FILE")
        echo -e "${BLUE}📈 总体统计${NC}"
        echo -e "总错误数量: ${RED}$total_errors${NC}"
        echo -e "分析时间: $(date '+%Y-%m-%d %H:%M:%S')\n"
    fi

    # 错误类型分布
    if [ -f "$ERRORS_BY_TYPE.json" ]; then
        echo -e "${BLUE}🏷️ 错误类型分布${NC}"
        jq -r '.[] | "  • \(.type): \(.count) 个 (\(.description))"' "$ERRORS_BY_TYPE.json"
        echo ""
    fi

    # 高频错误文件（Top 10）
    if [ -f "$ERRORS_BY_FILE.json" ]; then
        echo -e "${BLUE}📁 高频错误文件 (Top 10)${NC}"
        jq -r '.[:10] | to_entries[] | "  \(.key + 1). \(.value.file | split("/") | .[-1]): \(.value.error_count) 个错误 (模块: \(.value.module))"' "$ERRORS_BY_FILE.json"
        echo ""
    fi

    # 修复建议优先级
    if [ -f "$FIX_RECOMMENDATIONS.json" ]; then
        echo -e "${BLUE}💡 修复建议 (按优先级排序)${NC}"
        jq -r '.[] | "  优先级\(.priority): \(.description) (影响文件: \(.count), 预计时间: \(.estimated_time)分钟, 成功率: \(.success_rate)%)"' "$FIX_RECOMMENDATIONS.json"
        echo ""
    fi

    # 预估修复时间
    if [ -f "$FIX_RECOMMENDATIONS.json" ]; then
        local total_estimated_time=$(jq '[.[] | .estimated_time] | add' "$FIX_RECOMMENDATIONS.json")
        local avg_success_rate=$(jq '[.[] | .success_rate] | add / length' "$FIX_RECOMMENDATIONS.json")

        echo -e "${PURPLE}⏱️ 修复时间预估${NC}"
        echo -e "预计总时间: ${WHITE}$(echo "scale=1; $total_estimated_time / 60" | bc -l)${NC} 小时"
        echo -e "平均成功率: ${WHITE}$avg_success_rate${NC}%"
        echo -e "建议并行开发: ${WHITE}$(echo "scale=0; $total_estimated_time / 20" | bc -l)${NC} 人"
    fi
}

# 6. 生成修复脚本
generate_fix_scripts() {
    echo -e "\n${BLUE}🛠️ 生成自动化修复脚本...${NC}"

    cat > "$PROJECT_ROOT/scripts/auto-fix-batch.sh" << 'EOF'
#!/bin/bash

echo "🚀 开始批量修复编译错误..."

# 1. 修复Jakarta包名问题
echo "步骤1: 修复javax包名 → jakarta包名"
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;
echo "✅ Jakarta包名修复完成"

# 2. 修复Autowired问题
echo "步骤2: 替换@Autowired → @Resource"
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
echo "✅ Autowired替换完成"

# 3. 验证修复结果
echo "步骤3: 验证修复结果"
cd smart-admin-api-java17-springboot3
mvn clean compile -q 2>&1 | grep -c "ERROR"

echo "🎉 批量修复完成！"
EOF

    chmod +x "$PROJECT_ROOT/scripts/auto-fix-batch.sh"
    echo -e "${GREEN}✅ 自动修复脚本已生成: $PROJECT_ROOT/scripts/auto-fix-batch.sh${NC}"
}

# 主执行流程
main() {
    local action="${1:-analyze}"

    case "$action" in
        "analyze"|"")
            get_detailed_errors
            analyze_errors_by_file
            analyze_errors_by_type
            generate_fix_recommendations
            show_analysis_report
            generate_fix_scripts
            ;;
        "report")
            show_analysis_report
            ;;
        "scripts")
            generate_fix_scripts
            ;;
        *)
            echo "用法: $0 [analyze|report|scripts]"
            exit 1
            ;;
    esac
}

# 检查依赖
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ 需要Python3来执行详细分析${NC}"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ 需要jq来处理JSON数据${NC}"
    exit 1
fi

# 执行主程序
main "$@"

echo -e "\n${GREEN}🎉 错误分析完成！详细数据保存在: $ERRORS_DIR/${NC}"