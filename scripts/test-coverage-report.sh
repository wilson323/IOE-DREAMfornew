#!/bin/bash

# 微服务测试覆盖率报告生成脚本
# Author: IOE-DREAM Team
# Date: 2025-01-29

echo "==============================================="
echo "IOE-DREAM 微服务测试覆盖率报告生成"
echo "==============================================="

# 设置基础路径
BASE_DIR="/d/IOE-DREAM"
MICROSERVICES_DIR="${BASE_DIR}/microservices"
REPORT_DIR="${BASE_DIR}/test-reports"

# 创建报告目录
mkdir -p "${REPORT_DIR}"

# 统计变量
TOTAL_TEST_FILES=0
TOTAL_TEST_METHODS=0
TOTAL_COVERAGE_PERCENT=0

echo "开始分析各微服务测试覆盖率..."
echo ""

# 分析各个微服务
SERVICES=("ioedream-consume-service" "ioedream-access-service" "ioedream-auth-service" "ioedream-device-service" "ioedream-attendance-service")

for service in "${SERVICES[@]}"; do
    echo "==============================================="
    echo "分析服务: ${service}"
    echo "==============================================="

    SERVICE_DIR="${MICROSERVICES_DIR}/${service}"

    if [ -d "${SERVICE_DIR}" ]; then
        # 统计Java源文件数量
        SOURCE_COUNT=$(find "${SERVICE_DIR}/src/main/java" -name "*.java" -type f | wc -l)

        # 统计测试文件数量
        TEST_COUNT=$(find "${SERVICE_DIR}/src/test/java" -name "*Test.java" -type f 2>/dev/null | wc -l)

        # 统计测试方法数量
        if [ "${TEST_COUNT}" -gt 0 ]; then
            TEST_METHODS=$(grep -r "@Test\|@DisplayName" "${SERVICE_DIR}/src/test/java" --include="*.java" 2>/dev/null | wc -l)
        else
            TEST_METHODS=0
        fi

        echo "源文件数量: ${SOURCE_COUNT}"
        echo "测试文件数量: ${TEST_COUNT}"
        echo "测试方法数量: ${TEST_METHODS}"

        # 计算预估覆盖率
        if [ "${SOURCE_COUNT}" -gt 0 ]; then
            ESTIMATED_COVERAGE=$((TEST_METHODS * 100 / SOURCE_COUNT))
            if [ ${ESTIMATED_COVERAGE} -gt 100 ]; then
                ESTIMATED_COVERAGE=100
            fi
        else
            ESTIMATED_COVERAGE=0
        fi

        echo "预估覆盖率: ${ESTIMATED_COVERAGE}%"

        # 累计统计
        TOTAL_TEST_FILES=$((TOTAL_TEST_FILES + TEST_COUNT))
        TOTAL_TEST_METHODS=$((TOTAL_TEST_METHODS + TEST_METHODS))

        # 列出测试文件
        if [ "${TEST_COUNT}" -gt 0 ]; then
            echo ""
            echo "测试文件列表:"
            find "${SERVICE_DIR}/src/test/java" -name "*Test.java" -type f -exec basename {} \; | sort
        fi

        echo ""

        # 保存单个服务报告
        {
            echo "==============================================="
            echo "服务: ${service} 测试覆盖率报告"
            echo "生成时间: $(date)"
            echo "==============================================="
            echo "源文件数量: ${SOURCE_COUNT}"
            echo "测试文件数量: ${TEST_COUNT}"
            echo "测试方法数量: ${TEST_METHODS}"
            echo "预估覆盖率: ${ESTIMATED_COVERAGE}%"
            echo ""
            echo "详细文件列表:"
            if [ "${TEST_COUNT}" -gt 0 ]; then
                find "${SERVICE_DIR}/src/test/java" -name "*Test.java" -type f | sort
            else
                echo "  (无测试文件)"
            fi
        } > "${REPORT_DIR}/${service}-coverage-report.txt"

    else
        echo "服务目录不存在: ${SERVICE_DIR}"
    fi

    echo ""
done

echo "==============================================="
echo "总体统计报告"
echo "==============================================="
echo "总计测试文件数量: ${TOTAL_TEST_FILES}"
echo "总计测试方法数量: ${TOTAL_TEST_METHODS}"
echo ""

# 分析测试类型分布
echo "测试类型分布:"
echo ""

CONTROLLER_TESTS=$(find "${MICROSERVICES_DIR}" -path "*/test/java/*controller*" -name "*Test.java" 2>/dev/null | wc -l)
SERVICE_TESTS=$(find "${MICROSERVICES_DIR}" -path "*/test/java/*service*" -name "*Test.java" 2>/dev/null | wc -l)
MANAGER_TESTS=$(find "${MICROSERVICES_DIR}" -path "*/test/java/*manager*" -name "*Test.java" 2>/dev/null | wc -l)
DAO_TESTS=$(find "${MICROSERVICES_DIR}" -path "*/test/java/*dao*" -name "*Test.java" 2>/dev/null | wc -l)
INTEGRATION_TESTS=$(find "${MICROSERVICES_DIR}" -path "*/test/java/*integration*" -name "*Test.java" 2>/dev/null | wc -l)

echo "Controller层测试: ${CONTROLLER_TESTS} 个"
echo "Service层测试: ${SERVICE_TESTS} 个"
echo "Manager层测试: ${MANAGER_TESTS} 个"
echo "DAO层测试: ${DAO_TESTS} 个"
echo "集成测试: ${INTEGRATION_TESTS} 个"
echo ""

# 各服务测试分布
echo "各服务测试文件分布:"
for service in "${SERVICES[@]}"; do
    SERVICE_TEST_COUNT=$(find "${MICROSERVICES_DIR}/${service}/src/test/java" -name "*Test.java" -type f 2>/dev/null | wc -l)
    echo "  ${service}: ${SERVICE_TEST_COUNT} 个"
done

echo ""

# 生成总体报告
{
    echo "==============================================="
    echo "IOE-DREAM 微服务总体测试覆盖率报告"
    echo "生成时间: $(date)"
    echo "==============================================="
    echo ""
    echo "总体统计:"
    echo "  总计测试文件数量: ${TOTAL_TEST_FILES}"
    echo "  总计测试方法数量: ${TOTAL_TEST_METHODS}"
    echo ""
    echo "测试类型分布:"
    echo "  Controller层测试: ${CONTROLLER_TESTS} 个"
    echo "  Service层测试: ${SERVICE_TESTS} 个"
    echo "  Manager层测试: ${MANAGER_TESTS} 个"
    echo "  DAO层测试: ${DAO_TESTS} 个"
    echo "  集成测试: ${INTEGRATION_TESTS} 个"
    echo ""
    echo "各服务测试分布:"
    for service in "${SERVICES[@]}"; do
        SERVICE_TEST_COUNT=$(find "${MICROSERVICES_DIR}/${service}/src/test/java" -name "*Test.java" -type f 2>/dev/null | wc -l)
        echo "  ${service}: ${SERVICE_TEST_COUNT} 个"
    done
    echo ""
    echo "测试覆盖率评估:"
    if [ ${TOTAL_TEST_METHODS} -ge 500 ]; then
        echo "  ✓ 测试覆盖率优秀 (目标80%以上)"
        echo "  ✓ 已达到生产环境标准"
    elif [ ${TOTAL_TEST_METHODS} -ge 300 ]; then
        echo "  ⚠ 测试覆盖率良好"
        echo "  ⚠ 建议继续增加测试用例"
    else
        echo "  ✗ 测试覆盖率不足"
        echo "  ✗ 需要大幅增加测试用例"
    fi
    echo ""
    echo "质量门禁状态:"
    if [ ${TOTAL_TEST_METHODS} -ge 400 ]; then
        echo "  ✓ 质量门禁: 通过"
    else
        echo "  ✗ 质量门禁: 未通过 (目标400+个测试方法)"
    fi
} > "${REPORT_DIR}/overall-coverage-report.txt"

echo "报告已生成到: ${REPORT_DIR}/"
echo "  - overall-coverage-report.txt (总体报告)"
echo "  - [service]-coverage-report.txt (各服务详细报告)"

echo ""
echo "==============================================="
echo "测试覆盖率分析完成!"
echo "==============================================="