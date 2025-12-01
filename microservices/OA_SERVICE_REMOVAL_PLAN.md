# OA Service 删除计划

## 📋 删除原因

1. **功能重复**: oa-service 与 enterprise-service 功能完全重复
2. **架构优化**: enterprise-service 已包含所有 OA 功能
3. **维护成本**: 两个服务维护相同功能，成本高

## ✅ 删除前确认

### 功能对比
- ✅ enterprise-service 包含: document、workflow、approval、meeting
- ✅ oa-service 包含: document、workflow、approval、meeting
- **结论**: 功能完全重复

### 网关路由
- ✅ enterprise-service 路由: `/api/enterprise/**`, `/api/oa/**`, `/api/hr/**`
- ✅ oa-service 路由: `/api/oa/**` (在 k8s 配置中)
- **结论**: enterprise-service 已处理 OA 路由

## 🗑️ 需要删除的内容

### 1. 代码目录
- `microservices/ioedream-oa-service/` 整个目录

### 2. 配置文件
- `k8s/k8s-deployments/configmaps/gateway-config.yaml` 中的 oa-service 路由（已更新）
- `docker/extended-services.yml` 中的 oa-service 配置
- `pom.xml` 中的 oa-service 模块（如果存在）

### 3. 文档引用
- 所有提到 oa-service 的文档

## ⚠️ 注意事项

1. **数据迁移**: 如果 oa-service 有独立数据库，需要迁移到 enterprise-service
2. **功能验证**: 确保 enterprise-service 的所有 OA 功能正常
3. **路由验证**: 确保网关路由 `/api/oa/**` 正确指向 enterprise-service

## 📝 删除步骤

1. ✅ 更新网关配置（k8s）
2. ⏳ 更新 docker-compose 配置
3. ⏳ 删除 oa-service 代码目录
4. ⏳ 更新相关文档

