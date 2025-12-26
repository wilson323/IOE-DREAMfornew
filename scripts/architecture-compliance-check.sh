#!/bin/bash
###############################################################################
# IOE-DREAM架构合规性检查脚本
#
# 功能: 自动检查四层架构违规情况
# 作者: 四层架构守护专家
# 版本: v1.0.0
# 日期: 2025-12-26
###############################################################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  IOE-DREAM架构合规性检查工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

cd /d/IOE-DREAM/microservices || exit 1

echo -e "${YELLOW}[检查1] @Repository违规检查${NC}"
REPO_COUNT=$(find . -name "*Dao.java" -exec grep -l "@Repository" {} \; 2>/dev/null | wc -l)
echo "发现 ${REPO_COUNT} 处@Repository违规"
echo ""

echo -e "${YELLOW}[检查2] @Autowired使用检查${NC}"
AUTOWIRED_MAIN=$(grep -r "@Autowired" --include="*.java" . 2>/dev/null | grep -v "/src/test/" | wc -l)
echo "主代码@Autowired使用: ${AUTOWIRED_MAIN} 处"
echo ""

echo -e "${YELLOW}[检查3] Manager层事务管理检查${NC}"
MANAGER_TX_COUNT=$(find . -name "*Manager.java" -exec grep -l "@Transactional" {} \; 2>/dev/null | wc -l)
echo "发现 ${MANAGER_TX_COUNT} 处Manager管理事务"
echo ""

echo -e "${YELLOW}[检查4] DAO标准使用检查${NC}"
MAPPER_COUNT=$(find . -name "*Dao.java" -exec grep -l "@Mapper" {} \; 2>/dev/null | wc -l)
DAO_TOTAL=$(find . -name "*Dao.java" 2>/dev/null | wc -l)
echo "DAO规范使用: ${MAPPER_COUNT}/${DAO_TOTAL} ($(( MAPPER_COUNT * 100 / DAO_TOTAL ))%)"
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  检查完成${NC}"
echo -e "${BLUE}========================================${NC}"
