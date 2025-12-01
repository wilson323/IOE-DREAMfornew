#!/bin/bash

echo "开始批量修复 @Slf4j import 问题..."

cd /d/IOE-DREAM/smart-admin-api-java17-springboot3

# 找到所有需要修复的文件
files="./sa-admin/src/main/java/net/lab1024/sa/admin/config/OperateLogAspectConfig.java
./sa-admin/src/main/java/net/lab1024/sa/admin/interceptor/AdminInterceptor.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/controller/AdvancedReportController.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/controller/ConsistencyValidationController.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/controller/ConsumptionModeController.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/engine/mode/ConsumptionModeEngine.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/engine/mode/ConsumptionModeEngineInitializer.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/engine/mode/ConsumptionModeFactory.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/service/consistency/DataConsistencyManager.java
./sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/video/manager/VideoPreviewManager.java
./sa-base/src/main/java/net/lab1024/sa/base/module/support/operationlog/domain/OperationLogEntity.java"

for file in $files; do
    if [ -f "$file" ]; then
        echo "添加 lombok.extern.slf4j.Slf4j import 到: $file"
        # 在package行后添加import
        sed -i '/^package/a\\nimport lombok.extern.slf4j.Slf4j;' "$file"
    fi
done

echo "@Slf4j import 修复完成"