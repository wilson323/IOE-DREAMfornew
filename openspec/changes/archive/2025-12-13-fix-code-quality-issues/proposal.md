# Change: 修复代码质量问题

## Why

基于 2025-12-14 全局项目深度分析报告，发现以下代码规范问题需要修复：
- 25 个文件使用 `@Repository` 注解（应使用 `@Mapper`）
- 12 个文件使用 `@Autowired` 注解（应使用 `@Resource`）
- 多个文件存在未使用的 import 和字段

## What Changes

- 将 DAO 层的 `@Repository` 注解替换为 `@Mapper`
- 将 `@Autowired` 注解替换为 `@Resource`
- 清理未使用的 import 和字段
- 更新相关文档

## Impact

- Affected specs: code-standards
- Affected code: 
  - `ioedream-access-service/dao/*`
  - `ioedream-oa-service/dao/*`
  - `microservices-common/*/dao/*`
  - `ioedream-common-service/service/impl/*`
  - `ioedream-gateway-service/config/*`
