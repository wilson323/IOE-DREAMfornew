# 临时文件清理计划

## 📋 清理范围

### 1. 日志文件 (.log) - 19个文件
所有编译日志和错误日志，这些是临时文件，可以安全删除。

### 2. 临时修复脚本 - 需要识别
- `ioedream-attendance-service/fix-compilation-errors.sh` - 临时修复脚本 ✅ 删除
- `ioedream-attendance-service/comprehensive-fix.sh` - 临时修复脚本 ✅ 删除
- `ioedream-attendance-service/final-simplified-compile.sh` - 临时编译脚本 ✅ 删除
- `ioedream-attendance-service/minimal-compile.sh` - 临时编译脚本 ✅ 删除
- `ioedream-consume-service/debug_compile.bat` - 临时调试脚本 ✅ 删除
- `fix-utf8-encoding.bat` - 临时编码修复脚本 ✅ 删除

### 3. 保留的正式脚本
- `batch_compile_all.sh` - 批量编译脚本（正式工具）
- `compile_all_services.sh` - 服务编译脚本（正式工具）
- `check-services-status.bat` - 服务状态检查（正式工具）
- `start-microservices.bat` - 启动脚本（正式工具）
- `scripts/` 目录下的脚本（正式运维脚本）
- `docker/` 目录下的脚本（正式部署脚本）
- `k8s/` 目录下的脚本（正式K8s脚本）
- `test/` 目录下的脚本（正式测试脚本）
- `performance-scripts/` 目录下的脚本（正式性能测试脚本）

## 🎯 清理策略

1. **删除所有.log文件** - 编译日志不再需要
2. **删除临时修复脚本** - 问题已修复，脚本不再需要
3. **保留正式工具脚本** - 用于日常开发和运维

