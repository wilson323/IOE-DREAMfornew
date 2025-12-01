#!/bin/bash

# 更新文档中@Autowired为@Resource的脚本
echo "🔄 开始更新文档中的依赖注入规范..."

# 需要更新的文档列表
docs=(
    "docs/ARCHITECTURE_STANDARDS.md"
    "docs/CHECKLISTS/智能视频系统开发检查清单.md"
    "docs/CHECKLISTS/消费系统开发检查清单.md"
    "docs/CHECKLISTS/考勤系统开发检查清单.md"
    "docs/CHECKLISTS/通用开发检查清单.md"
    "docs/CHECKLISTS/门禁系统开发检查清单.md"
    "docs/COMMON_MODULES/smart-area.md"
    "docs/COMMON_MODULES/smart-device.md"
    "docs/COMMON_MODULES/smart-permission.md"
    "docs/COMMON_MODULES/smart-realtime.md"
    "docs/COMMON_MODULES/smart-workflow.md"
    "docs/CRITICAL_PROJECT_STATUS_REPORT.md"
    "docs/PROJECT_GUIDE.md"
    "docs/repowiki/zh/content/开发规范体系/IOE-DREAM统一开发规范技能指南.md"
    "docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md"
    "docs/SmartAdmin规范体系_v4/01-核心规范层/编码规范.md"
    "docs/SmartAdmin规范体系_v4/03-AI专用层/AI开发指令集.md"
    "docs/SmartAdmin规范体系_v4/03-AI专用层/AI约束检查清单.md"
    "docs/STANDARDS_EXECUTION_FRAMEWORK.md"
    "docs/TECH_STACK_CONSISTENCY_VERIFICATION.md"
)

# 统计更新数量
updated_count=0

# 遍历文档并更新
for doc in "${docs[@]}"; do
    if [ -f "$doc" ]; then
        echo "更新文档: $doc"
        # 备份原文件
        cp "$doc" "$doc.backup"

        # 替换@Autowired为@Resource
        sed -i 's/@Autowired/@Resource/g' "$doc"

        # 替换import javax.annotation.Resource为import jakarta.annotation.Resource
        sed -i 's/import javax\.annotation\.Resource/import jakarta.annotation.Resource/g' "$doc"

        # 验证更新
        if grep -q "@Resource" "$doc"; then
            echo "✅ 成功更新: $doc"
            updated_count=$((updated_count + 1))
        else
            echo "⚠️ 未找到@Resource，恢复备份: $doc"
            mv "$doc.backup" "$doc"
        fi

        # 删除备份文件
        rm -f "$doc.backup"
    else
        echo "⚠️ 文件不存在: $doc"
    fi
done

echo ""
echo "🎉 文档更新完成！"
echo "✅ 更新文档数量: $updated_count"
echo "📚 所有示例代码现在都使用@Resource注入方式"