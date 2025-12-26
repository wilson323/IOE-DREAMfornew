#!/bin/bash

# 消费服务模块日志标准化修复脚本
# 统一日志格式为：[模块名] 操作描述: 参数={}

echo "开始执行消费服务模块日志标准化修复..."

# 定义模块映射
declare -A module_mapping=(
    ["ConsumeRechargeServiceImpl"]="充值服务"
    ["ConsumeSubsidyServiceImpl"]="补贴服务"
    ["ConsumeAccountServiceImpl"]="账户服务"
    ["ConsumeProductServiceImpl"]="产品服务"
    ["ConsumeProductBasicService"]="产品基础服务"
    ["ConsumeProductQueryService"]="产品查询服务"
    ["ConsumeProductStockService"]="库存服务"
    ["ConsumeProductPriceService"]="价格服务"
    ["ConsumeProductStatisticsService"]="统计服务"
    ["ConsumeProductValidationService"]="验证服务"
    ["ConsumeProductImportExportService"]="导入导出服务"
    ["ConsumeTransactionServiceImpl"]="交易服务"
    ["ConsumeMealCategoryServiceImpl"]="餐类服务"
    ["ConsumeDeviceServiceImpl"]="设备服务"
    ["ConsumeReportServiceImpl"]="报表服务"
)

# 批量修复日志格式
for file in $(find microservices/ioedream-consume-service/src/main/java -name "*ServiceImpl.java" -type f); do
    echo "处理文件: $file"

    # 确定模块名
    basename_file=$(basename "$file" .java)
    module_name=${module_mapping[$basename_file]}

    if [ -z "$module_name" ]; then
        module_name=${basename_file//ServiceImpl/}
        module_name="${module_name}服务"
    fi

    echo "  模块名: $module_name"

    # 使用sed进行批量替换，避免重复替换
    # 1. log.info -> log.info("[模块名]
    sed -i.bak "s/log\.info(\"/log\.info(\"[$module_name] /g" "$file"

    # 2. log.error -> log.error("[模块名] - 只替换没有模块前缀的
    sed -i "s/log\.error(\"/log\.error(\"[$module_name] /g" "$file"

    # 3. log.warn -> log.warn("[模块名]
    sed -i "s/log\.warn(\"/log\.warn(\"[$module_name] /g" "$file"

    # 4. log.debug -> log.debug("[模块名]
    sed -i "s/log\.debug(\"/log\.debug(\"[$module_name] /g" "$file"

    # 5. 修复重复的模块名前缀
    sed -i "s/\[\([^\]]*\)\] \[\1\]/[\1]/g" "$file"

    echo "  ✓ 已修复日志格式"
done

echo ""
echo "日志标准化修复完成！"
echo ""
echo "修复统计:"
echo "处理的文件数量: $(find microservices/ioedream-consume-service/src/main/java -name "*ServiceImpl.java" -type f | wc -l)"
echo ""
echo "请检查修复结果，如有问题可使用.bak备份文件恢复。"