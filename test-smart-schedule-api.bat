@echo off
REM =====================================================
REM 智能排班引擎 - API联调测试脚本（Windows版本）
REM 用途：快速测试后端API接口是否正常工作
REM =====================================================

setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8091
set TOTAL=0
set PASSED=0
set FAILED=0

echo.
echo ================================================
echo   智能排班引擎 - API联调测试
echo ================================================
echo.
echo 后端服务地址: %BASE_URL%
echo.

REM 检查服务是否启动
echo [检查] 后端服务状态...
curl -s -o nul -w "%%{http_code}" %BASE_URL%/actuator/health >nul 2>&1
set HEALTH_CODE=%%ERRORLEVEL%%
if %HEALTH_CODE% GEQ 200 (
    if %HEALTH_CODE% LSS 300 (
        echo [成功] 后端服务运行正常
    ) else (
        echo [失败] 后端服务未启动 (HTTP %HEALTH_CODE%)
        echo 请先启动后端服务：
        echo   cd microservices/ioedream-attendance-service
        echo   mvn spring-boot:run
        pause
        exit /b 1
    )
) else (
    echo [失败] 无法连接到后端服务
    echo 请检查服务是否已启动
    pause
    exit /b 1
)

echo.
echo ================================================
echo 开始API接口测试
echo ================================================
echo.

REM 测试1：查询计划列表
echo [1/16] 测试: 查询计划列表
curl -s -X GET "%BASE_URL%/api/v1/schedule/plan/page?pageNum=1&pageSize=10"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 查询计划列表
    set /a FAILED+=1
) else (
    echo [通过] 查询计划列表
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试2：创建排班计划
echo [2/16] 测试: 创建排班计划
curl -s -X POST "%BASE_URL%/api/v1/schedule/plan" ^
  -H "Content-Type: application/json" ^
  -d "{\"planName\":\"API测试计划\",\"description\":\"集成测试\",\"startDate\":\"2025-01-27\",\"endDate\":\"2025-02-02\",\"employeeIds\":[1,2,3],\"shiftIds\":[1,2,3],\"optimizationGoal\":5,\"maxConsecutiveWorkDays\":7,\"minRestDays\":2,\"minDailyStaff\":2,\"fairnessWeight\":0.4,\"costWeight\":0.3,\"efficiencyWeight\":0.2,\"satisfactionWeight\":0.1,\"algorithmType\":4,\"populationSize\":20,\"maxGenerations\":50,\"crossoverRate\":0.8,\"mutationRate\":0.1}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 创建排班计划
    set /a FAILED+=1
) else (
    echo [通过] 创建排班计划
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 假设计划ID为1
set PLAN_ID=1

REM 测试3：查询计划详情
echo [3/16] 测试: 查询计划详情
curl -s -X GET "%BASE_URL%/api/v1/schedule/plan/%PLAN_ID%"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 查询计划详情
    set /a FAILED+=1
) else (
    echo [通过] 查询计划详情
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试4：执行优化
echo [4/16] 测试: 执行排班优化
curl -s -X POST "%BASE_URL%/api/v1/schedule/plan/%PLAN_ID%/execute" ^
  -H "Content-Type: application/json" ^
  -d "{}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 执行排班优化
    set /a FAILED+=1
) else (
    echo [通过] 执行排班优化
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试5：查询排班结果
echo [5/16] 测试: 查询排班结果
curl -s -X GET "%BASE_URL%/api/v1/schedule/result/page?planId=%PLAN_ID%&pageNum=1&pageSize=20"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 查询排班结果
    set /a FAILED+=1
) else (
    echo [通过] 查询排班结果
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试6：查询结果列表
echo [6/16] 测试: 查询结果列表
curl -s -X GET "%BASE_URL%/api/v1/schedule/result/list/%PLAN_ID%"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 查询结果列表
    set /a FAILED+=1
) else (
    echo [通过] 查询结果列表
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试7：确认计划
echo [7/16] 测试: 确认排班计划
curl -s -X POST "%BASE_URL%/api/v1/schedule/plan/%PLAN_ID%/confirm" ^
  -H "Content-Type: application/json" ^
  -d "{}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 确认排班计划
    set /a FAILED+=1
) else (
    echo [通过] 确认排班计划
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试8：查询规则列表
echo [8/16] 测试: 查询规则列表
curl -s -X GET "%BASE_URL%/api/v1/schedule/rule/page?pageNum=1&pageSize=10"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 查询规则列表
    set /a FAILED+=1
) else (
    echo [通过] 查询规则列表
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试9：验证规则表达式
echo [9/16] 测试: 验证规则表达式
curl -s -X POST "%BASE_URL%/api/v1/schedule/rule/validate?expression=consecutiveWorkDays+%3E+3"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 验证规则表达式
    set /a FAILED+=1
) else (
    echo [通过] 验证规则表达式
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试10：测试规则执行
echo [10/16] 测试: 测试规则执行
curl -s -X POST "%BASE_URL%/api/v1/schedule/rule/test" ^
  -H "Content-Type: application/json" ^
  -d "{\"expression\":\"consecutiveWorkDays >= 3\",\"context\":{\"employeeId\":1,\"date\":\"2025-01-30\",\"shiftId\":1,\"consecutiveWorkDays\":5}}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 测试规则执行
    set /a FAILED+=1
) else (
    echo [通过] 测试规则执行
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试11：遗传算法优化
echo [11/16] 测试: 遗传算法优化
curl -s -X POST "%BASE_URL%/api/v1/schedule/optimize/genetic" ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeIds\":[1,2,3],\"startDate\":\"2025-01-27\",\"endDate\":\"2025-02-02\",\"shiftIds\":[1,2,3]}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 遗传算法优化
    set /a FAILED+=1
) else (
    echo [通过] 遗传算法优化
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试12：模拟退火优化
echo [12/16] 测试: 模拟退火优化
curl -s -X POST "%BASE_URL%/api/v1/schedule/optimize/annealing" ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeIds\":[1,2,3],\"startDate\":\"2025-01-27\",\"endDate\":\"2025-02-02\",\"shiftIds\":[1,2,3]}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 模拟退火优化
    set /a FAILED+=1
) else (
    echo [通过] 模拟退火优化
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试13：混合算法优化
echo [13/16] 测试: 混合算法优化
curl -s -X POST "%BASE_URL%/api/v1/schedule/optimize/hybrid" ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeIds\":[1,2,3],\"startDate\":\"2025-01-27\",\"endDate\":\"2025-02-02\",\"shiftIds\":[1,2,3]}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 混合算法优化
    set /a FAILED+=1
) else (
    echo [通过] 混合算法优化
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试14：自动算法选择
echo [14/16] 测试: 自动算法选择
curl -s -X POST "%BASE_URL%/api/v1/schedule/optimize/auto" ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeIds\":[1,2,3],\"startDate\":\"2025-01-27\",\"endDate\":\"2025-02-02\",\"shiftIds\":[1,2,3]}"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 自动算法选择
    set /a FAILED+=1
) else (
    echo [通过] 自动算法选择
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试15：获取规则优先级
echo [15/16] 测试: 获取规则优先级
curl -s -X GET "%BASE_URL%/api/v1/schedule/rule/priority"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 获取规则优先级
    set /a FAILED+=1
) else (
    echo [通过] 获取规则优先级
    set /a PASSED+=1
)
set /a TOTAL+=1

REM 测试16：查询规则列表（不分页）
echo [16/16] 测试: 查询规则列表（不分页）
curl -s -X GET "%BASE_URL%/api/v1/schedule/rule/list"
if !ERRORLEVEL! EQU 0 (
    echo [失败] 查询规则列表（不分页）
    set /a FAILED+=1
) else (
    echo [通过] 查询规则列表（不分页）
    set /a PASSED+=1
)
set /a TOTAL+=1

echo.
echo ================================================
echo 测试总结
echo ================================================
echo.
echo 总测试数: %TOTAL%
echo 通过: %PASSED%
echo 失败: %FAILED%
echo.

REM 计算通过率（整数除法）
if %TOTAL% GTR 0 (
    set /a PASS_RATE=PASSED*100/TOTAL
    echo 通过率: %%PASS_RATE%%%
    echo.

    if %%PASS_RATE%% GEQ 95 (
        echo [成功] API联调测试通过！可以进行下一步测试。
    ) else if %%PASS_RATE%% GEQ 80 (
        echo [警告] API联调测试基本通过，但仍有部分问题需要修复。
    ) else (
        echo [失败] API联调测试未通过，请修复问题后重试。
    )
)

echo.
echo ================================================
echo.

pause
