# 测试脚本示例

> 版本: v1.0.0  
> 责任人: 测试保障组  
> 最后更新: 2025-11-12

## 1. 后端单元测试
```powershell
Set-Location D:\IOE-DREAM\smart-admin-api-java17-springboot3
mvn clean test -DskipITs=false
```
- 生成覆盖率报告后, 查看 `target/site/jacoco/index.html`。
- 重点关注 `net.lab1024.sa.admin.module.device`、`permission` 等核心模块。

## 2. 后端集成测试
```powershell
Set-Location D:\IOE-DREAM\smart-admin-api-java17-springboot3
mvn verify -Pintegration-test
```
- 使用专用配置 `application-integration.yaml`, 数据库指向测试实例。

## 3. 前端单元测试
```powershell
Set-Location D:\IOE-DREAM\smart-admin-web-javascript
npm run test:unit -- --coverage
```
- 覆盖率结果输出 `coverage/index.html`。
- 保证与后端字段命名一致, 尤其是 `deviceId`, `roleId` 等关键字段。

## 4. 前端端到端测试 (Playwright)
```powershell
Set-Location D:\IOE-DREAM\smart-admin-web-javascript
npm run test:e2e
```
- 需提前启动后端与前端服务。
- Playwright 脚本位于 `tests/e2e/`，脚本内使用 `data-testid` 保持稳定性。

## 5. API 回归 (PowerShell + RestClient)
```powershell
$headers = @{ "satoken" = $env:SATOKEN }
Invoke-RestMethod -Method Get -Uri "http://localhost:1024/api/admin/permission/roles" -Headers $headers
```
- 与 `docs/SAMPLES/API_EXAMPLES.md` 中的请求保持一致。

## 6. 日志与断言
- 后端断言使用 `org.assertj.core.api.Assertions`。
- 前端使用 `vitest expect` 与 `toMatchSnapshot`，命名遵循驼峰。
- 失败用例需记录于 `docs/testing/reports/` 并同步到 `COVERAGE_REPORT_TEMPLATE.md`。
