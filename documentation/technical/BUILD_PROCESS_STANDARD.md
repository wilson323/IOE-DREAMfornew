# IOE-DREAM 构建流程标准

**版本**: v1.0.0  
**生效日期**: 2025-01-30  
**适用范围**: 所有构建场景

---

## 构建场景

### 1. 本地开发构建（Maven多模块）

**使用场景**: 本地开发和调试

**构建命令**:
```powershell
# 构建所有模块
.\scripts\build-all.ps1

# 构建指定服务
.\scripts\build-all.ps1 -Service ioedream-access-service

# 跳过测试
.\scripts\build-all.ps1 -SkipTests

# 清理并构建
.\scripts\build-all.ps1 -Clean
```

**构建顺序**:
1. `microservices-common` (必须先构建)
2. 基础设施服务（gateway-service）
3. 公共业务服务（common-service）
4. 业务服务（可并行）

---

### 2. Docker构建（单模块构建）

**使用场景**: Docker镜像构建

**构建命令**:
```bash
# 单模块构建（使用-N参数跳过父模块）
mvn clean package -N -pl microservices/ioedream-access-service -am
```

**构建配置**:
- 使用 `-N` 参数跳过父POM执行
- 使用 `-pl` 指定要构建的模块
- 使用 `-am` 同时构建依赖的模块

---

### 3. CI/CD构建

**使用场景**: GitHub Actions自动化构建

**构建流程**:
1. Checkout代码
2. Setup Java环境
3. 缓存Maven依赖
4. 执行构建和测试
5. 生成构建产物

**配置要点**:
- 使用Maven缓存加速构建
- 并行执行测试
- 生成测试报告和覆盖率报告

---

## 构建规范

### 必须遵循

1. **构建顺序**: 必须先构建 `microservices-common`
2. **依赖管理**: 使用父POM的properties引用版本
3. **测试执行**: 构建时必须执行测试（除非使用 `-SkipTests`）
4. **版本管理**: 使用统一版本号管理

### 禁止事项

1. ❌ 直接构建业务服务（跳过common）
2. ❌ 只编译不安装（必须使用 `install`）
3. ❌ 硬编码依赖版本（应使用 `${property.version}`）

---

## 构建检查清单

### 构建前

- [ ] 确认构建顺序正确
- [ ] 检查依赖配置完整
- [ ] 确认Java版本正确（Java 17）

### 构建中

- [ ] 监控构建日志
- [ ] 检查警告信息
- [ ] 验证测试执行

### 构建后

- [ ] 验证构建产物生成
- [ ] 检查测试覆盖率报告
- [ ] 验证JAR文件完整性

---

## 常见问题

### 问题1: 依赖解析失败

**原因**: `microservices-common` 未安装到本地仓库

**解决方案**:
```powershell
# 先构建并安装common模块
mvn clean install -pl microservices/microservices-common -am
```

### 问题2: 编译错误

**原因**: 可能存在架构违规或依赖问题

**解决方案**:
1. 运行架构合规性检查
2. 检查依赖版本一致性
3. 清理并重新构建

### 问题3: 测试失败

**原因**: 测试代码或配置问题

**解决方案**:
1. 检查测试配置
2. 运行单个测试排查
3. 更新测试代码

---

**维护责任人**: IOE-DREAM DevOps团队

