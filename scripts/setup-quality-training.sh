#!/bin/bash

# IOE-DREAM 质量培训环境设置脚本
# 功能：为新员工和培训设置完整的学习环境

echo "🎓 IOE-DREAM 质量培训环境设置"
echo "============================="
echo "设置时间: $(date)"
echo "目标用户: ${USER:-开发人员}"
echo ""

# 检查必要的脚本和文档
check_requirements() {
    echo "🔍 检查培训环境要求..."

    local missing_files=()

    # 检查核心脚本
    local required_scripts=(
        "scripts/precise-quality-check.sh"
        "scripts/optimized-quality-check.sh"
        "scripts/comprehensive-quality-check.sh"
        "scripts/quality-trend-analysis.sh"
        "scripts/daily-quality-collector.sh"
    )

    for script in "${required_scripts[@]}"; do
        if [ ! -f "$script" ]; then
            missing_files+=("$script")
        fi
    done

    # 检查核心文档
    local required_docs=(
        "CLAUDE.md"
        "documentation/QUALITY_TRAINING_GUIDE.md"
        "documentation/final-quality-gate-summary.md"
    )

    for doc in "${required_docs[@]}"; do
        if [ ! -f "$doc" ]; then
            missing_files+=("$doc")
        fi
    done

    if [ ${#missing_files[@]} -eq 0 ]; then
        echo "   ✅ 所有必需文件存在"
        return 0
    else
        echo "   ❌ 缺少以下文件:"
        for file in "${missing_files[@]}"; do
            echo "      - $file"
        done
        return 1
    fi
}

# 函数：创建个人培训环境
create_training_environment() {
    echo ""
    echo "🏗️ 创建个人培训环境..."

    # 创建培训目录结构
    local training_dir="training/${USER:-new-developer}"
    mkdir -p "$training_dir"
    mkdir -p "$training_dir/practice"
    mkdir -p "$training_dir/exercises"
    mkdir -p "$training_dir/reports"
    mkdir -p "$training_dir/notes"

    echo "   📁 培训目录: $training_dir"
    echo "   📁 练习目录: $training_dir/practice"
    echo "   📁 练习题目录: $training_dir/exercises"
    echo "   📁 报告目录: $training_dir/reports"
    echo "   📁 笔记目录: $training_dir/notes"

    # 复制培训资料
    echo ""
    echo "📚 复制培训资料..."

    # 复制核心文档
    cp "CLAUDE.md" "$training_dir/"
    cp "documentation/QUALITY_TRAINING_GUIDE.md" "$training_dir/"
    cp "documentation/final-quality-gate-summary.md" "$training_dir/"

    echo "   ✅ 核心文档已复制"

    # 创建快捷脚本
    echo ""
    echo "🔧 创建培训快捷脚本..."

    # 质量检查快捷脚本
    cat > "$training_dir/quick-check.sh" << 'EOF'
#!/bin/bash
echo "🚀 快速质量检查"
echo "================"
cd "$(dirname "$0")/../.."
bash scripts/precise-quality-check.sh
EOF

    # 练习环境检查脚本
    cat > "$training_dir/practice-check.sh" << 'EOF'
#!/bin/bash
echo "🧪 练习环境质量检查"
echo "=================="
cd "$(dirname "$0")/../.."
bash scripts/comprehensive-quality-check.sh
EOF

    # 培训进度检查脚本
    cat > "$training_dir/progress-check.sh" << 'EOF'
#!/bin/bash
echo "📊 培训进度检查"
echo "=============="
cd "$(dirname "$0")/../.."

echo "当前代码质量状态:"
bash scripts/precise-quality-check.sh

echo ""
echo "质量趋势分析:"
bash scripts/quality-trend-analysis.sh

echo ""
echo "个人培训报告:"
echo "培训开始时间: $(date)"
echo "练习文件数: $(find training/*/practice -name "*.java" 2>/dev/null | wc -l)"
echo "报告文件数: $(find training/*/reports -name "*.txt" 2>/dev/null | wc -l)"
EOF

    chmod +x "$training_dir"/*.sh
    echo "   ✅ 快捷脚本已创建"
}

# 函数：创建练习题
create_exercises() {
    echo ""
    echo "📝 创建质量练习题..."

    local exercises_dir="training/${USER:-new-developer}/exercises"

    # 练习题1: SLF4J修复
    cat > "$exercises_dir/exercise1-slf4j.java" << 'EOF'
package com.example.exercise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Exercise1 {
    private static final Logger logger = LoggerFactory.getLogger(Exercise1.class);

    public void doSomething() {
        logger.info("这是一个练习");
        // TODO: 请修复SLF4J使用方式
    }
}
EOF

    # 练习题2: 依赖注入修复
    cat > "$exercises_dir/exercise2-autowired.java" << 'EOF'
package com.example.exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Exercise2 {
    @Autowired
    private SomeService someService;

    public void doSomething() {
        someService.process();
        // TODO: 请修复依赖注入方式
    }
}

interface SomeService {
    void process();
}
EOF

    # 练习题3: Repository修复
    cat > "$exercises_dir/exercise3-repository.java" << 'EOF'
package com.example.exercise;

import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;

@Repository  // TODO: 请修复注解使用
public interface Exercise3Repository {
    // 这是一个DAO接口
}
EOF

    echo "   ✅ 已创建3个基础练习题"
    echo "   📁 练习文件位置: $exercises_dir/"
}

# 函数：创建培训计划
create_training_plan() {
    echo ""
    echo "📅 创建个人培训计划..."

    local plan_file="training/${USER:-new-developer}/TRAINING_PLAN.md"

    cat > "$plan_file" << 'EOF'
# 个人质量培训计划

## 培训目标
- 掌握IOE-DREAM架构规范
- 熟练使用质量检查工具
- 建立质量意识
- 通过质量认证

## 培训阶段

### 第一周：基础学习
- [ ] 阅读CLAUDE.md架构规范文档
- [ ] 学习质量培训指南
- [ ] 理解四层架构设计
- [ ] 掌握依赖注入规范

### 第二周：工具实践
- [ ] 运行质量检查脚本
- [ ] 理解检查结果
- [ ] 完成练习题1-3
- [ ] 学习问题修复方法

### 第三周：深度实践
- [ ] 分析项目质量问题
- [ ] 参与代码审查
- [ ] 制定改进计划
- [ ] 实施质量改进

### 第四周：综合评估
- [ ] 完成综合练习
- [ ] 通过知识测试
- [ ] 参与项目实践
- [ ] 获得质量认证

## 学习资源
- [CLAUDE.md](./CLAUDE.md) - 项目架构规范
- [质量培训指南](./QUALITY_TRAINING_GUIDE.md) - 详细培训内容
- [质量门禁总结](./final-quality-gate-summary.md) - 质量体系建设成果

## 进度跟踪
- 开始日期: [填写]
- 预计完成: [填写]
- 实际完成: [填写]
- 认证状态: [填写]
EOF

    echo "   ✅ 培训计划已创建: $plan_file"
}

# 函数：设置Git hooks
setup_git_hooks() {
    echo ""
    echo "🔧 设置Git质量检查hooks..."

    # 检查是否已经有pre-commit hook
    if [ -f ".git/hooks/pre-commit" ]; then
        echo "   ⚠️ 已存在pre-commit hook，跳过设置"
    else
        # 创建pre-commit hook
        cat > ".git/hooks/pre-commit" << 'EOF'
#!/bin/bash
# IOE-DREAM Git Pre-commit Quality Check

echo "🔍 运行提交前质量检查..."
echo "=========================="

# 运行精确质量检查
if bash scripts/precise-quality-check.sh; then
    echo "✅ 质量检查通过，可以提交"
    exit 0
else
    echo "❌ 质量检查失败，请修复问题后再提交"
    exit 1
fi
EOF

        chmod +x ".git/hooks/pre-commit"
        echo "   ✅ Pre-commit hook已设置"
    fi
}

# 函数：生成培训启动脚本
create_launch_script() {
    echo ""
    echo "🚀 创建培训启动脚本..."

    local launch_script="training/${USER:-new-developer}/START_TRAINING.sh"

    cat > "$launch_script" << 'EOF'
#!/bin/bash

echo "🎓 IOE-DREAM 质量培训启动"
echo "========================="
echo "培训时间: $(date)"
echo "培训用户: $USER"
echo ""

# 检查培训环境
echo "🔍 检查培训环境..."
cd "$(dirname "$0")/../.."

if [ ! -f "scripts/precise-quality-check.sh" ]; then
    echo "❌ 质量检查脚本不存在，请确保在正确的项目目录中运行"
    exit 1
fi

echo "✅ 培训环境检查通过"
echo ""

# 显示当前质量状态
echo "📊 当前项目质量状态:"
echo "======================"
bash scripts/precise-quality-check.sh

echo ""
echo "📚 培训资源导航:"
echo "==============="
echo "1. 架构规范文档: ./CLAUDE.md"
echo "2. 培训指南: ./documentation/QUALITY_TRAINING_GUIDE.md"
echo "3. 质量总结: ./documentation/final-quality-gate-summary.md"
echo ""

echo "🧪 培训练习:"
echo "==========="
echo "1. 快速质量检查: ./quick-check.sh"
echo "2. 练习环境检查: ./practice-check.sh"
echo "3. 进度跟踪: ./progress-check.sh"
echo ""

echo "📝 建议的学习步骤:"
echo "================"
echo "1. 阅读CLAUDE.md文档，理解架构规范"
echo "2. 学习质量培训指南，了解培训内容"
echo "3. 运行质量检查脚本，理解检查结果"
echo "4. 完成练习题，实践质量修复"
echo "5. 定期检查进度，跟踪学习成果"
echo ""

echo "🚀 开始您的质量培训之旅！"
EOF

    chmod +x "$launch_script"
    echo "   ✅ 培训启动脚本: $launch_script"
}

# 函数：显示使用说明
show_usage_instructions() {
    echo ""
    echo "📋 培训环境使用说明"
    echo "=================="
    echo ""
    echo "🚀 启动培训:"
    echo "   cd training/${USER:-new-developer}"
    echo "   ./START_TRAINING.sh"
    echo ""
    echo "🧪 日常练习:"
    echo "   ./quick-check.sh          # 快速质量检查"
    echo "   ./practice-check.sh        # 练习环境检查"
    echo "   ./progress-check.sh        # 进度跟踪"
    echo ""
    echo "📚 学习资料:"
    echo "   CLAUDE.md                   # 项目架构规范"
    echo "   QUALITY_TRAINING_GUIDE.md  # 详细培训指南"
    echo "   final-quality-gate-summary.md # 质量建设总结"
    echo ""
    echo "📝 练习题位置:"
    echo "   exercises/exercise1-slf4j.java"
    echo "   exercises/exercise2-autowired.java"
    echo "   exercises/exercise3-repository.java"
    echo ""
    echo "📊 报告输出:"
    echo "   reports/                     # 个人练习报告"
    echo "   monitoring-reports/          # 项目质量报告"
}

# 主执行流程
main() {
    echo "开始设置质量培训环境..."

    # 检查要求
    if ! check_requirements; then
        echo "❌ 环境要求检查失败，请确保所有必需文件存在"
        exit 1
    fi

    # 创建培训环境
    create_training_environment

    # 创建练习题
    create_exercises

    # 创建培训计划
    create_training_plan

    # 设置Git hooks
    setup_git_hooks

    # 创建启动脚本
    create_launch_script

    # 显示使用说明
    show_usage_instructions

    echo ""
    echo "============================="
    echo "🎉 质量培训环境设置完成！"
    echo ""
    echo "🚀 下一步操作:"
    echo "1. cd training/${USER:-new-developer}"
    echo "2. ./START_TRAINING.sh"
    echo ""
    echo "📞 如需帮助，请查看培训文档或联系质量培训团队"
}

# 执行主函数
main