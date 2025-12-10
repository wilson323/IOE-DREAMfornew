#!/bin/bash

# 配置安全检查脚本
# 检查项目中是否存在明文密码

set -e

echo "🔍 开始检查项目中的明文密码..."

# 定义可能包含明文密码的关键词
PASSWORD_KEYWORDS=(
    "password"
    "passwd"
    "secret"
    "key"
    "token"
    "credential"
    "auth"
    "private_key"
    "api_key"
    "access_key"
    "secret_key"
)

# 定义可疑的明文密码模式
SUSPICIOUS_PATTERNS=(
    "password[\"']?[\"']?[\"']?[a-zA-Z0-9]{6,}" # password:"123456"
    "secret[\"']?[\"']?[\"']?[a-zA-Z0-9]{6,}"   # secret:"abc123"
    "key[\"']?[\"']?[\"']?[a-zA-Z0-9]{6,}"     # key:"xyz789"
    "\"123456\""
    "\"password\""
    "\"secret\""
    "\"admin\""
    "\"root\""
)

# 创建报告文件
REPORT_FILE="CONFIG_SECURITY_CHECK_REPORT.md"
cat > "$REPORT_FILE" << EOF
# 配置安全检查报告

**检查日期**: $(date '+%Y-%m-%d %H:%M:%S')
**检查范围**: IOE-DREAM项目全局配置
**检查状态**: 🔍 **检查进行中**
**优先级**: 🔴 P0级安全检查

---

## 📋 检查发现

EOF

# 扫描配置文件
CONFIG_FILES=(
    "*.yml"
    "*.yaml"
    "*.properties"
    "*.json"
    "application*"
    "bootstrap*"
)

TOTAL_VIOLATIONS=0

echo "🔍 扫描配置文件..."

# 检查每个可能包含密码的文件
for file_pattern in "${CONFIG_FILES[@]}"; do
    find . -name "$file_pattern" -type f | while read file; do
        if [ -f "$file" ]; then
            echo "检查文件: $file"

            # 检查明文密码模式
            file_violations=0

            for pattern in "${SUSPICIOUS_PATTERNS[@]}"; do
                if grep -q "$pattern" "$file" 2>/dev/null; then
                    echo "  ❌ 发现可疑明文密码: $pattern"
                    echo "    文件: $file" >> "$REPORT_FILE"
                    grep -n "$pattern" "$file" >> "$REPORT_FILE"
                    echo "" >> "$REPORT_FILE"

                    file_violations=$((file_violations + 1))
                    TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
                fi
            done

            # 检查是否为加密格式
            if grep -q "ENC(" "$file" 2>/dev/null; then
                echo "  ✅ 发现加密配置 (ENC格式)"
            fi
        fi
    done
done

echo ""
echo "📊 检查统计"
echo "总违规数: $TOTAL_VIOLATIONS"

# 更新报告
cat >> "$REPORT_FILE" << EOF

## 📊 检查统计

| 检查项目 | 结果 |
|---------|------|
| **扫描文件类型** | *.yml, *.yaml, *.properties, *.json |
| **总违规数量** | $TOTAL_VIOLATIONS |
| **安全风险等级** | $(if [ $TOTAL_VIOLATIONS -gt 50 ]; then echo "🔴 高风险"; elif [ $TOTAL_VIOLATIONS -gt 10 ]; then echo "🟡 中风险"; else echo "🟢 低风险"; fi) |
| **检查时间** | $(date '+%Y-%m-%d %H:%M:%S') |

## 🚨 发现的安全问题

EOF

# 根据违规数量给出安全等级
if [ $TOTAL_VIOLATIONS -gt 50 ]; then
    echo "🔴 **高风险**: 发现$TOTAL_VIOLATIONS个明文密码，需要立即整改！" >> "$REPORT_FILE"
    RISK_LEVEL="HIGH"
elif [ $TOTAL_VIOLATIONS -gt 10 ]; then
    echo "🟡 **中风险**: 发现$TOTAL_VIOLATIONS个明文密码，建议尽快整改" >> "$REPORT_FILE"
    RISK_LEVEL="MEDIUM"
else
    echo "🟢 **低风险**: 发现$TOTAL_VIOLATIONS个明文密码，建议检查确认" >> "$REPORT_FILE"
    RISK_LEVEL="LOW"
fi

cat >> "$REPORT_FILE" << EOF

## 📋 立即行动建议

### 🔴 P0级立即处理
1. **停止使用明文密码**: 立即停止在配置文件中使用明文密码
2. **实施Nacos加密**: 使用Nacos配置加密功能
3. **密钥管理**: 建立安全的密钥管理机制
4. **权限控制**: 限制配置文件访问权限

### 🔧 技术实施方案
1. **生成加密密钥**: 使用安全的密钥生成工具
2. **批量加密配置**: 对现有配置进行批量加密
3. **更新应用配置**: 修改应用配置以支持加密
4. **验证加密效果**: 确保加密配置正常工作

### 📋 后续工作
1. **定期安全扫描**: 建立定期的安全扫描机制
2. **安全培训**: 对团队进行安全意识培训
3. **流程改进**: 将安全检查纳入开发流程
4. **监控告警**: 建立配置安全监控和告警

---

**检查完成时间**: $(date '+%Y-%m-%d %H:%M:%S')
**检查执行人**: 配置安全检查工具
**风险等级**: $RISK_LEVEL
**下一步**: 根据检查结果制定整改计划

EOF

echo "✅ 检查完成，报告已生成: $REPORT_FILE"
echo "📊 总违规数: $TOTAL_VIOLATIONS"

if [ $TOTAL_VIOLATIONS -gt 0 ]; then
    echo ""
    echo "🚨 发现明文密码违规，请立即整改！"
    echo "📋 详细报告请查看: $REPORT_FILE"
    exit 1
else
    echo ""
    echo "✅ 未发现明显的明文密码违规"
    exit 0
fi