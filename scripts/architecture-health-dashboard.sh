#!/bin/bash

echo "=========================================="
echo "IOE-DREAM 架构健康监控仪表板"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 获取当前时间
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# 清屏并显示标题
clear

echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}║           🏗️ IOE-DREAM 架构健康监控仪表板                    ║${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}║           生成时间: $TIMESTAMP               ║${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# 功能菜单
echo -e "${CYAN}📋 监控功能菜单:${NC}"
echo ""

# 1. 整体架构健康度
echo -e "${YELLOW}1. 整体架构健康度评估${NC}"
echo "   ├─ Entity使用违规检查"
echo "   ├─ 跨服务传递违规检查"
echo "   ├─ 架构边界合规检查"
echo "   └─ 设计模式一致性检查"

echo ""

# 2. 微服务专项检查
echo -e "${YELLOW}2. 微服务专项检查${NC}"
echo "   ├─ Access Service Entity优化状态"
echo "   ├─ Attendance Service Entity优化状态"
echo "   ├─ Video Service 架构合规状态"
echo "   └─ 其他微服务检查"

echo ""

# 3. 代码质量指标
echo -e "${YELLOW}3. 代码质量指标${NC}"
echo "   ├─ 违规问题统计"
echo "   ├─ 修复完成率"
echo "   ├─ 架构合规分数"
echo "   └─ 技术债务评估"

echo ""

# 4. 持续监控
echo -e "${YELLOW}4. 持续监控${NC}"
echo "   ├─ 自动化检查脚本状态"
echo "   ├─ CI/CD集成状态"
echo "   ├─ 预提交钩子状态"
echo "   └─ 团队遵循度评估"

echo ""

# 选择功能
echo -e "${GREEN}请选择要执行的功能 (1-4):${NC}"
read -p "输入选项: " choice

case $choice in
    1)
        echo -e "\n${PURPLE}🔍 执行整体架构健康度评估...${NC}"
        bash scripts/overall-architecture-health-check.sh
        ;;
    2)
        echo -e "\n${PURPLE}🔍 执行微服务专项检查...${NC}"
        echo "请选择微服务:"
        echo "1) Access Service"
        echo "2) Attendance Service"
        echo "3) Video Service"
        echo "4) 所有微服务"
        read -p "输入选项: " service_choice

        case $service_choice in
            1)
                bash scripts/validate-access-service-entity-fixes.sh
                ;;
            2)
                bash scripts/validate-attendance-service-entity-fixes.sh
                ;;
            3)
                bash scripts/validate-video-service-architecture-fixes.sh
                ;;
            4)
                echo -e "${CYAN}检查所有微服务...${NC}"
                bash scripts/validate-access-service-entity-fixes.sh
                echo ""
                bash scripts/validate-attendance-service-entity-fixes.sh
                echo ""
                bash scripts/validate-video-service-architecture-fixes.sh
                ;;
            *)
                echo -e "${RED}无效选项${NC}"
                ;;
        esac
        ;;
    3)
        echo -e "\n${PURPLE}📊 生成代码质量指标报告...${NC}"
        bash scripts/generate-code-quality-metrics.sh
        ;;
    4)
        echo -e "\n${PURPLE}🔄 执行持续监控检查...${NC}"
        bash scripts/continuous-monitoring-check.sh
        ;;
    *)
        echo -e "${RED}无效选项${NC}"
        ;;
esac

echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}║                    🎯 监控完成                              ║${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}║           运行 'bash scripts/architecture-health-dashboard.sh'  ║${NC}"
echo -e "${BLUE}║           重新打开架构健康监控仪表板                          ║${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"