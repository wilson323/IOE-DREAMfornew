#!/bin/bash

# =============================================================================
# IOE-DREAM 质量改进预测模型
# 功能：基于机器学习算法预测编译错误修复趋势
# 创建时间：2025-11-18
# 版本：v1.0.0
# =============================================================================

PROJECT_ROOT="D:\IOE-DREAM"
MONITORING_DIR="$PROJECT_ROOT/monitoring"
MODEL_DIR="$PROJECT_ROOT/models"
PREDICTIONS_FILE="$MONITORING_DIR/predictions.json"
HISTORY_FILE="$MONITORING_DIR/quality_history.json"

# 创建模型目录
mkdir -p "$MODEL_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

echo -e "${CYAN}🤖 启动质量改进预测模型...${NC}"

# 1. 数据收集和预处理
collect_training_data() {
    echo -e "${BLUE}📊 收集训练数据...${NC}"

    # 获取当前编译错误详情
    cd "$PROJECT_ROOT/smart-admin-api-java17-springboot3"

    # 执行编译并收集数据
    local start_time=$(date +%s)
    mvn compile -q 2>&1 > compile_output.log

    local end_time=$(date +%s)
    local compile_duration=$((end_time - start_time))

    # 提取错误指标
    local total_errors=$(grep -c "ERROR" compile_output.log)
    local cannot_find_symbol=$(grep -c "cannot find symbol" compile_output.log)
    local package_not_found=$(grep -c "package.*does not exist" compile_output.log)
    local duplicate_method=$(grep -c "duplicate method" compile_output.log)
    local cannot_resolve=$(grep -c "cannot resolve" compile_output.log)
    local jakarta_issues=$(grep -c "javax\." compile_output.log)
    local autowired_issues=$(grep -c "@Autowired" compile_output.log)

    # 计算错误密度
    local java_files_count=$(find . -name "*.java" | wc -l)
    local error_density=$(echo "scale=3; $total_errors / $java_files_count" | bc -l)

    # 创建训练数据点
    cat <<EOF > training_data_point.json
{
  "timestamp": "$(date -Iseconds)",
  "compile_time": $compile_duration,
  "metrics": {
    "total_errors": $total_errors,
    "error_density": $error_density,
    "java_files_count": $java_files_count,
    "error_breakdown": {
      "cannot_find_symbol": $cannot_find_symbol,
      "package_not_found": $package_not_found,
      "duplicate_method": $duplicate_method,
      "cannot_resolve": $cannot_resolve,
      "jakarta_issues": $jakarta_issues,
      "autowired_issues": $autowired_issues
    }
  }
}
EOF

    # 添加到历史数据
    if [ ! -f "$HISTORY_FILE" ]; then
        echo "[]" > "$HISTORY_FILE"
    fi

    local temp_file=$(mktemp)
    jq ". + [$(cat training_data_point.json)]" "$HISTORY_FILE" > "$temp_file" && mv "$temp_file" "$HISTORY_FILE"

    rm -f training_data_point.json compile_output.log
    echo -e "${GREEN}✅ 训练数据收集完成${NC}"
}

# 2. 特征工程
feature_engineering() {
    echo -e "${BLUE}🔧 执行特征工程...${NC}"

    python3 << 'EOF' > "$MODEL_DIR/features.json"
import json
import numpy as np
from datetime import datetime, timedelta

# 加载历史数据
with open("D:/IOE-DREAM/monitoring/quality_history.json", 'r', encoding='utf-8') as f:
    history_data = json.load(f)

if len(history_data) < 5:
    print(json.dumps({"status": "insufficient_data", "message": "需要至少5个数据点"}))
    exit()

# 特征提取
features = []
labels = []

for i in range(1, len(history_data)):
    current = history_data[i]['metrics']
    previous = history_data[i-1]['metrics']

    # 基础特征
    total_errors = current['total_errors']
    error_density = current['error_density']

    # 变化率特征
    error_change = current['total_errors'] - previous['total_errors']
    error_change_rate = error_change / max(previous['total_errors'], 1)

    # 趋势特征（最近3个点）
    trend_data = []
    for j in range(max(0, i-2), i+1):
        trend_data.append(history_data[j]['metrics']['total_errors'])

    # 计算趋势斜率
    if len(trend_data) >= 2:
        x = np.arange(len(trend_data))
        y = np.array(trend_data)
        trend_slope = np.polyfit(x, y, 1)[0]
    else:
        trend_slope = 0

    # 错误类型分布特征
    breakdown = current['error_breakdown']
    jakarta_ratio = breakdown.get('jakarta_issues', 0) / max(total_errors, 1)
    autowired_ratio = breakdown.get('autowired_issues', 0) / max(total_errors, 1)
    symbol_ratio = breakdown.get('cannot_find_symbol', 0) / max(total_errors, 1)

    # 编译时间特征
    compile_time = history_data[i].get('compile_time', 0)

    feature_vector = {
        "total_errors": total_errors,
        "error_density": error_density,
        "error_change": error_change,
        "error_change_rate": error_change_rate,
        "trend_slope": trend_slope,
        "jakarta_ratio": jakarta_ratio,
        "autowired_ratio": autowired_ratio,
        "symbol_ratio": symbol_ratio,
        "compile_time": compile_time
    }

    features.append(feature_vector)
    # 标签：下一时间点的错误数量变化
    if i < len(history_data) - 1:
        next_errors = history_data[i+1]['metrics']['total_errors']
        labels.append(next_errors - total_errors)

# 保存特征数据
result = {
    "features": features,
    "labels": labels,
    "feature_count": len(features),
    "feature_names": list(features[0].keys()) if features else []
}

print(json.dumps(result, indent=2, ensure_ascii=False))
EOF

    echo -e "${GREEN}✅ 特征工程完成${NC}"
}

# 3. 预测模型训练
train_prediction_model() {
    echo -e "${BLUE}🎯 训练预测模型...${NC}"

    python3 << 'EOF'
import json
import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error, r2_score
from sklearn.model_selection import train_test_split
import pickle

# 加载特征数据
with open("D:/IOE-DREAM/models/features.json", 'r', encoding='utf-8') as f:
    data = json.load(f)

if data['feature_count'] < 10:
    print(json.dumps({"status": "insufficient_data", "message": "需要至少10个特征数据点"}))
    exit()

# 准备训练数据
features = np.array([[f[name] for name in data['feature_names']] for f in data['features']])
labels = np.array(data['labels'])

# 分割训练和测试数据
X_train, X_test, y_train, y_test = train_test_split(features, labels, test_size=0.2, random_state=42)

# 训练多个模型
models = {}

# 1. 线性回归模型
lr_model = LinearRegression()
lr_model.fit(X_train, y_train)
lr_pred = lr_model.predict(X_test)
lr_score = r2_score(y_test, lr_pred)
models['linear_regression'] = {
    'model': 'linear_regression',
    'r2_score': lr_score,
    'mse': mean_squared_error(y_test, lr_pred)
}

# 2. 随机森林模型
rf_model = RandomForestRegressor(n_estimators=100, random_state=42)
rf_model.fit(X_train, y_train)
rf_pred = rf_model.predict(X_test)
rf_score = r2_score(y_test, rf_pred)
models['random_forest'] = {
    'model': 'random_forest',
    'r2_score': rf_score,
    'mse': mean_squared_error(y_test, rf_pred)
}

# 选择最佳模型
best_model_name = max(models.keys(), key=lambda k: models[k]['r2_score'])
best_model = rf_model if best_model_name == 'random_forest' else lr_model

# 保存最佳模型
model_path = f"D:/IOE-DREAM/models/{best_model_name}_model.pkl"
with open(model_path, 'wb') as f:
    pickle.dump(best_model, f)

# 保存模型信息
model_info = {
    "best_model": best_model_name,
    "feature_names": data['feature_names'],
    "performance": models[best_model_name],
    "all_models": models,
    "training_samples": len(X_train),
    "test_samples": len(X_test)
}

with open("D:/IOE-DREAM/models/model_info.json", 'w', encoding='utf-8') as f:
    json.dump(model_info, f, indent=2, ensure_ascii=False)

print(json.dumps({"status": "success", "model_info": model_info}, ensure_ascii=False))
EOF

    echo -e "${GREEN}✅ 预测模型训练完成${NC}"
}

# 4. 生成预测
make_predictions() {
    echo -e "${BLUE}🔮 生成质量改进预测...${NC}"

    python3 << 'EOF'
import json
import numpy as np
import pickle
from datetime import datetime, timedelta

# 加载模型信息
try:
    with open("D:/IOE-DREAM/models/model_info.json", 'r', encoding='utf-8') as f:
        model_info = json.load(f)

    model_name = model_info['best_model']
    feature_names = model_info['feature_names']

    # 加载训练好的模型
    with open(f"D:/IOE-DREAM/models/{model_name}_model.pkl", 'rb') as f:
        model = pickle.load(f)

except Exception as e:
    print(json.dumps({"status": "error", "message": f"无法加载模型: {str(e)}"}))
    exit()

# 获取最新特征数据
with open("D:/IOE-DREAM/models/features.json", 'r', encoding='utf-8') as f:
    features_data = json.load(f)

latest_features = features_data['features'][-1]
feature_vector = [latest_features[name] for name in feature_names]

# 预测未来多个时间点
predictions = []
current_errors = latest_features['total_errors']

# 预测未来20小时的错误变化（假设每小时一个时间点）
for hours_ahead in range(1, 21):
    # 模拟特征变化（简化假设）
    future_features = feature_vector.copy()

    # 基于历史趋势调整特征
    if 'trend_slope' in feature_names:
        trend_idx = feature_names.index('trend_slope')
        future_features[trend_idx] *= 1.05  # 假设趋势略有改善

    # 进行预测
    predicted_change = model.predict([future_features])[0]
    predicted_errors = max(0, current_errors + predicted_change)

    predictions.append({
        "hours_ahead": hours_ahead,
        "predicted_errors": int(predicted_errors),
        "predicted_change": int(predicted_change),
        "confidence_interval": {
            "lower": max(0, int(predicted_errors - abs(predicted_change) * 0.5)),
            "upper": int(predicted_errors + abs(predicted_change) * 0.5)
        }
    })

    current_errors = predicted_errors

# 分析预测结果
final_predicted = predictions[-1]['predicted_errors']
target_errors = 120
improvement_needed = latest_features['total_errors'] - target_errors
target_achievable = final_predicted <= target_errors

# 计算达成概率
better_predictions = [p for p in predictions if p['predicted_errors'] <= target_errors]
achievement_probability = len(better_predictions) / len(predictions) * 100

# 生成优化建议
recommendations = []

if final_predicted > target_errors:
    if latest_features['jakarta_ratio'] > 0.1:
        recommendations.append({
            "action": "批量修复Jakarta包名问题",
            "expected_reduction": int(latest_features['total_errors'] * latest_features['jakarta_ratio']),
            "priority": "high"
        })

    if latest_features['autowired_ratio'] > 0.05:
        recommendations.append({
            "action": "替换@Autowired注解为@Resource",
            "expected_reduction": int(latest_features['total_errors'] * latest_features['autowired_ratio']),
            "priority": "high"
        })

    if latest_features['symbol_ratio'] > 0.5:
        recommendations.append({
            "action": "补充缺失的类和符号定义",
            "expected_reduction": int(latest_features['total_errors'] * 0.3),
            "priority": "medium"
        })

prediction_result = {
    "timestamp": datetime.now().isoformat(),
    "current_errors": latest_features['total_errors'],
    "target_errors": target_errors,
    "final_predicted_errors": final_predicted,
    "improvement_needed": improvement_needed,
    "target_achievable": target_achievable,
    "achievement_probability": round(achievement_probability, 1),
    "model_used": model_name,
    "model_confidence": round(model_info['performance']['r2_score'] * 100, 1),
    "predictions": predictions,
    "recommendations": recommendations,
    "key_insights": {
        "error_density": latest_features['error_density'],
        "trend_direction": "decreasing" if latest_features.get('trend_slope', 0) < 0 else "increasing",
        "primary_error_type": max([
            ("jakarta", latest_features.get('jakarta_ratio', 0)),
            ("autowired", latest_features.get('autowired_ratio', 0)),
            ("symbol", latest_features.get('symbol_ratio', 0))
        ], key=lambda x: x[1])[0]
    }
}

# 保存预测结果
with open("D:/IOE-DREAM/monitoring/predictions.json", 'w', encoding='utf-8') as f:
    json.dump(prediction_result, f, indent=2, ensure_ascii=False)

print(json.dumps({"status": "success", "predictions": prediction_result}, ensure_ascii=False))
EOF

    echo -e "${GREEN}✅ 预测生成完成${NC}"
}

# 5. 显示预测报告
show_prediction_report() {
    if [ ! -f "$PREDICTIONS_FILE" ]; then
        echo -e "${RED}❌ 预测结果文件不存在，请先运行预测${NC}"
        return
    fi

    echo -e "\n${CYAN}========================================${NC}"
    echo -e "${WHITE}🔮 质量改进预测报告${NC}"
    echo -e "${CYAN}========================================${NC}\n"

    # 读取预测结果
    local current_errors=$(jq -r '.current_errors' "$PREDICTIONS_FILE")
    local target_errors=$(jq -r '.target_errors' "$PREDICTIONS_FILE")
    local final_predicted=$(jq -r '.final_predicted_errors' "$PREDICTIONS_FILE")
    local achievement_prob=$(jq -r '.achievement_probability' "$PREDICTIONS_FILE")
    local model_confidence=$(jq -r '.model_confidence' "$PREDICTIONS_FILE")
    local model_used=$(jq -r '.model_used' "$PREDICTIONS_FILE")
    local target_achievable=$(jq -r '.target_achievable' "$PREDICTIONS_FILE")

    # 当前状态
    echo -e "${BLUE}📊 当前状态${NC}"
    echo -e "当前错误数: ${RED}$current_errors${NC}"
    echo -e "目标错误数: ${GREEN}$target_errors${NC}"
    echo -e "需要改进: ${RED}$((current_errors - target_errors))${NC} 个错误"
    echo ""

    # 预测结果
    echo -e "${PURPLE}🎯 20小时后预测${NC}"
    echo -e "预测错误数: ${WHITE}$final_predicted${NC}"

    if [ "$target_achievable" = "true" ]; then
        echo -e "目标达成: ${GREEN}✅ 可以达成${NC}"
    else
        echo -e "目标达成: ${RED}❌ 无法达成${NC}"
    fi

    echo -e "达成概率: ${WHITE}$achievement_prob%${NC}"
    echo -e "模型置信度: ${WHITE}$model_confidence%${NC}"
    echo -e "使用模型: ${WHITE}$model_used${NC}"
    echo ""

    # 关键洞察
    echo -e "${BLUE}🔍 关键洞察${NC}"
    local error_density=$(jq -r '.key_insights.error_density' "$PREDICTIONS_FILE")
    local trend_direction=$(jq -r '.key_insights.trend_direction' "$PREDICTIONS_FILE")
    local primary_error_type=$(jq -r '.key_insights.primary_error_type' "$PREDICTIONS_FILE")

    echo -e "错误密度: ${WHITE}$error_density${NC} 错误/文件"

    if [ "$trend_direction" = "decreasing" ]; then
        echo -e "错误趋势: ${GREEN}📉 下降中${NC}"
    else
        echo -e "错误趋势: ${RED}📈 上升中${NC}"
    fi

    echo -e "主要错误类型: ${WHITE}$primary_error_type${NC}"
    echo ""

    # 优化建议
    echo -e "${YELLOW}💡 优化建议${NC}"
    jq -r '.recommendations[] | "• \(.action) (预计减少: \(.expected_reduction)个错误, 优先级: \(.priority))"' "$PREDICTIONS_FILE"
    echo ""

    # 时间轴预测
    echo -e "${BLUE}📈 时间轴预测 (关键时间点)${NC}"
    echo "当前 → 6小时 → 12小时 → 20小时"

    # 提取关键预测点
    local prediction_6h=$(jq -r '.predictions[5].predicted_errors // "N/A"' "$PREDICTIONS_FILE")
    local prediction_12h=$(jq -r '.predictions[11].predicted_errors // "N/A"' "$PREDICTIONS_FILE")
    local prediction_20h=$(jq -r '.predictions[19].predicted_errors // "N/A"' "$PREDICTIONS_FILE")

    echo -e "错误数: $current_errors → $prediction_6h → $prediction_12h → $prediction_20h"

    # 目标线
    echo -e "目标线: ─────────────────── $target_errors (目标线)"
}

# 6. 风险评估
assess_risks() {
    if [ ! -f "$PREDICTIONS_FILE" ]; then
        return
    fi

    echo -e "\n${YELLOW}⚠️ 风险评估${NC}"

    local achievement_prob=$(jq -r '.achievement_probability' "$PREDICTIONS_FILE")
    local current_errors=$(jq -r '.current_errors' "$PREDICTIONS_FILE")

    if [ "$achievement_prob" -lt 30 ]; then
        echo -e "风险等级: ${RED}🔴 高风险${NC}"
        echo -e "建议措施:"
        echo -e "• 增加开发人员到2-3人"
        echo -e "• 优先批量修复自动化程度高的问题"
        echo -e "• 考虑降低目标标准或延长时间窗口"
    elif [ "$achievement_prob" -lt 70 ]; then
        echo -e "风险等级: ${YELLOW}🟡 中等风险${NC}"
        echo -e "建议措施:"
        echo -e "• 加强开发效率监控"
        echo -e "• 重点解决主要错误类型"
        echo -e "• 准备备用修复策略"
    else
        echo -e "风险等级: ${GREEN}🟢 低风险${NC}"
        echo -e "建议措施:"
        echo -e "• 保持当前修复节奏"
        echo -e "• 关注代码质量"
        echo -e "• 准备最终验收测试"
    fi
}

# 主程序
main() {
    local action="${1:-predict}"

    case "$action" in
        "collect")
            collect_training_data
            ;;
        "features")
            feature_engineering
            ;;
        "train")
            train_prediction_model
            ;;
        "predict"|"")
            collect_training_data
            feature_engineering
            train_prediction_model
            make_predictions
            show_prediction_report
            assess_risks
            ;;
        "report")
            show_prediction_report
            assess_risks
            ;;
        *)
            echo "用法: $0 [collect|features|train|predict|report]"
            exit 1
            ;;
    esac
}

# 检查Python依赖
if ! python3 -c "import sklearn, numpy, pickle" 2>/dev/null; then
    echo -e "${RED}❌ 需要安装机器学习依赖: pip install scikit-learn numpy${NC}"
    exit 1
fi

# 执行主程序
main "$@"

echo -e "\n${GREEN}🎉 质量改进预测完成！${NC}"