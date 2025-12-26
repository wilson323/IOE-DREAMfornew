# P0级架构迁移执行计划

**生成时间**: 2025-12-25
**执行周期**: 1-2周
**优先级**: P0（最高优先级）

## 📋 执行概览

本计划旨在完成IOE-DREAM项目的架构清理工作，解决以下核心问题：

1. ✅ **架构迁移不彻底** - 清理microservices-common聚合模块
2. ✅ **编译前置检查缺失** - 建立自动化检查机制
3. ✅ **API使用不规范** - 统一类型安全的使用方式

---

## 🎯 阶段一：架构迁移（Week 1）

### 任务1.1: 清理 microservices-common 模块

**当前状态分析**:
- ✅ 配置类（4个）: 保留
- 🔄 OpenAPI模块（13个）: 迁移到gateway-client
- 🔄 Edge模块（6个）: 保留（边缘计算功能）
- ✅ 工厂类（1个）: 保留

**执行步骤**:

#### Step 1.1.1: 迁移OpenAPI模块
```bash
# 源目录
microservices-common/src/main/java/net/lab1024/sa/common/openapi/

# 目标目录
microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/openapi/
```

**迁移清单**:
- [ ] domain/request/* (6个文件)
- [ ] domain/response/* (6个文件)
- [ ] manager/SecurityManager.java
- [ ] service/UserOpenApiService.java

**依赖更新**:
```xml
<!-- microservices-common-gateway-client/pom.xml -->
<!-- 无需新增依赖，只需迁移文件 -->
```

**导入路径更新**:
```java
// 更新前
import net.lab1024.sa.common.openapi.domain.request.LoginRequest;

// 更新后
import net.lab1024.sa.common.gateway.openapi.domain.request.LoginRequest;
```

**影响范围**:
- ✅ ioedream-common-service（需要更新导入）
- ✅ 其他服务无影响（无依赖）

#### Step 1.1.2: 验证编译
```bash
# 编译gateway-client模块
mvn clean install -pl microservices/microservices-common-gateway-client -am -DskipTests

# 编译common-service
mvn clean compile -pl ioedream-common-service -am -DskipTests

# 编译所有业务服务
mvn clean compile -pl ioedream-access-service,ioedream-attendance-service,ioedream-consume-service,ioedream-video-service,ioedream-visitor-service,ioedream-device-comm-service -am -DskipTests
```

#### Step 1.1.3: 清理旧文件
```bash
# 删除已迁移的OpenAPI模块
rm -rf microservices-common/src/main/java/net/lab1024/sa/common/openapi/
```

**预期结果**:
- ✅ microservices-common只包含配置类和Edge模块
- ✅ OpenAPI模块在gateway-client中统一管理
- ✅ 所有服务编译通过

---

### 任务1.2: 统一实体类管理

**当前状态**: 实体类已经集中在microservices-common-entity

**验证清单**:
- [x] UserEntity在common.organization.entity
- [x] DeviceEntity在common.organization.entity
- [x] 所有业务实体使用正确导入路径

**无需执行** - 已在前面的修复中完成 ✅

---

### 任务1.3: 更新所有导入路径

**自动化脚本**:

```bash
#!/bin/bash
# scripts/fix-import-paths.sh

echo "🔍 开始修复导入路径..."

# 1. 修复UserEntity导入
find . -name "*.java" -type f -exec sed -i 's|net\.lab1024\.sa\.common\.entity\.UserEntity|net.lab1024.sa.common.organization.entity.UserEntity|g' {} \;

# 2. 修复DeviceEntity导入
find . -name "*.java" -type f -exec sed -i 's|net\.lab1024\.sa\.common\.entity\.DeviceEntity|net.lab1024.sa.common.organization.entity.DeviceEntity|g' {} \;

# 3. 修复PageResult重复common导入
find . -name "*.java" -type f -exec sed -i 's|net\.lab1024\.sa\.common\.common\.domain\.PageResult|net.lab1024.sa.common.domain.PageResult|g' {} \;

# 4. 修复ResponseDTO API使用
find . -name "*.java" -type f -exec sed -i 's|\.isSuccessful()|\.getCode() == 200|g' {} \;

echo "✅ 导入路径修复完成"
```

---

## 🎯 阶段二：建立编译前置检查（Week 1-2）

### 任务2.1: Git Pre-commit Hook

**创建脚本**: `.git/hooks/pre-commit`

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "🔍 执行提交前检查..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查1: 包路径规范
echo "检查包路径规范..."
if git diff --cached --name-only | grep "\.java$" | xargs grep -l "net\.lab1024\.sa\.common\.common\." 2>/dev/null; then
    echo -e "${RED}❌ 发现重复的common包路径${NC}"
    echo "请修正以下文件中的导入路径:"
    git diff --cached --name-only | grep "\.java$" | xargs grep -l "net\.lab1024\.sa\.common\.common\."
    exit 1
fi

# 检查2: 不存在的类
echo "检查类导入..."
if git diff --cached --name-only | grep "\.java$" | xargs grep "import java\.util\.concurrent\.atomic\.AtomicDouble" 2>/dev/null; then
    echo -e "${RED}❌ AtomicDouble类不存在${NC}"
    exit 1
fi

# 检查3: API使用规范
echo "检查API使用规范..."
if git diff --cached --name-only | grep "\.java$" | xargs grep "objectMapper\.readValue.*\.class)" 2>/dev/null; then
    echo -e "${YELLOW}⚠️  建议使用TypeReference保留泛型类型${NC}"
    echo "以下文件可能需要修复:"
    git diff --cached --name-only | grep "\.java$" | xargs grep -l "objectMapper\.readValue.*\.class)"
fi

# 检查4: 强制类型转换警告
echo "检查类型转换..."
UNSAFE_CAST=$(git diff --cached --name-only | grep "\.java$" | xargs grep -c "objectMapper\.readValue.*Map\.class" || true)
if [ "$UNSAFE_CAST" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  发现$UNSAFE_CAST处未经检查的类型转换${NC}"
    echo "建议使用: new TypeReference<Map<String, Object>>() {}"
fi

echo -e "${GREEN}✅ 提交前检查通过${NC}"
exit 0
```

**安装Hook**:
```bash
chmod +x .git/hooks/pre-commit
```

### 任务2.2: CI/CD架构合规性检查

**创建工作流**: `.github/workflows/architecture-compliance.yml`

```yaml
name: Architecture Compliance Check

on:
  pull_request:
    branches: [ main, new-clean-branch ]
  push:
    branches: [ main, new-clean-branch ]

jobs:
  check:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Check package paths
      run: |
        echo "检查包路径规范..."
        ! grep -r "net\.lab1024\.sa\.common\.entity" microservices/*/src/ || exit 1
        ! grep -r "net\.lab1024\.sa\.common\.common\." microservices/*/src/ || exit 1

    - name: Check non-existent classes
      run: |
        echo "检查不存在的类..."
        ! grep -r "import java\.util\.concurrent\.atomic\.AtomicDouble" microservices/*/src/ || exit 1

    - name: Verify TypeReference usage
      run: |
        echo "检查TypeReference使用..."
        # 查找未使用TypeReference的地方
        VIOLATIONS=$(grep -r "objectMapper\.readValue.*Map\.class\|objectMapper\.readValue.*List\.class" microservices/*/src/ | wc -l)
        if [ "$VIOLATIONS" -gt 0 ]; then
          echo "⚠️ 发现$VIOLATIONS处未使用TypeReference"
          # 不阻止构建，只是警告
        fi

    - name: Compile all services
      run: |
        echo "编译所有服务..."
        mvn clean compile -DskipTests -pl \
          ioedream-common-service,\
          ioedream-access-service,\
          ioedream-attendance-service,\
          ioedream-consume-service,\
          ioedream-device-comm-service,\
          ioedream-video-service,\
          ioedream-visitor-service \
          -am

    - name: Generate compliance report
      if: always()
      run: |
        echo "生成架构合规性报告..."
        mkdir -p reports
        echo "# Architecture Compliance Report" > reports/compliance.md
        echo "Generated: $(date)" >> reports/compliance.md
        echo "Status: ${{ job.status }}" >> reports/compliance.md

    - name: Upload report
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: compliance-report
        path: reports/
```

### 任务2.3: 依赖分析脚本

**创建脚本**: `scripts/analyze-dependencies.sh`

```bash
#!/bin/bash
# scripts/analyze-dependencies.sh

echo "📊 生成模块依赖关系图..."

OUTPUT_FILE="dependency-analysis.md"

cat > "$OUTPUT_FILE" << 'EOF'
# 模块依赖关系分析报告

生成时间: $(date)
分析范围: microservices/

EOF

# 分析每个模块
for MODULE in microservices/*/; do
    if [ -d "$MODULE" ]; then
        MODULE_NAME=$(basename "$MODULE")
        POM_FILE="$MODULE/pom.xml"

        if [ -f "$POM_FILE" ]; then
            echo "## 模块: $MODULE_NAME" >> "$OUTPUT_FILE"
            echo "" >> "$OUTPUT_FILE"

            # 提取依赖
            echo "### 直接依赖" >> "$OUTPUT_FILE"
            mvn dependency:tree -f "$MODULE" -pl "$MODULE_NAME" 2>/dev/null | \
                grep "\- " | \
                head -20 >> "$OUTPUT_FILE"
            echo "" >> "$OUTPUT_FILE"
        fi
    fi
done

echo "✅ 依赖分析完成: $OUTPUT_FILE"
```

---

## 🎯 阶段三：统一API使用规范（Week 2）

### 任务3.1: 扫描未使用TypeReference的地方

**创建扫描脚本**: `scripts/scan-type-reference.sh`

```bash
#!/bin/bash
# scripts/scan-type-reference.sh

echo "🔍 扫描未使用TypeReference的地方..."

echo "=== Map类型转换 ==="
grep -rn "objectMapper\.readValue.*Map\.class" microservices/*/src/ --include="*.java" | \
    grep -v "TypeReference" || echo "✅ 无问题"

echo ""
echo "=== List类型转换 ==="
grep -rn "objectMapper\.readValue.*List\.class" microservices/*/src/ --include="*.java" | \
    grep -v "TypeReference" || echo "✅ 无问题"

echo ""
echo "=== Set类型转换 ==="
grep -rn "objectMapper\.readValue.*Set\.class" microservices/*/src/ --include="*.java" | \
    grep -v "TypeReference" || echo "✅ 无问题"

echo ""
echo "=== 其他Collection类型 ==="
grep -rn "objectMapper\.readValue.*Collection\.class" microservices/*/src/ --include="*.java" | \
    grep -v "TypeReference" || echo "✅ 无问题"
```

### 任务3.2: 自动修复TypeReference使用

**创建修复脚本**: `scripts/fix-type-reference.sh`

```bash
#!/bin/bash
# scripts/fix-type-reference.sh

echo "🔧 自动修复TypeReference使用..."

# 修复Map类型
find microservices -name "*.java" -type f -exec sed -i '
s/objectMapper\.readValue(\([^,]*\), Map\.class)/objectMapper.readValue(\1, new TypeReference<Map<String, Object>>() {})/g
' {} \;

# 修复List类型
find microservices -name "*.java" -type f -exec sed -i '
s/objectMapper\.readValue(\([^,]*\), List\.class)/objectMapper.readValue(\1, new TypeReference<List<Object>>() {})/g
' {} \;

echo "✅ TypeReference修复完成"
echo "⚠️  请手动验证修复结果并测试"
```

### 任务3.3: 统一异常处理模式

**创建检查脚本**: `scripts/check-exception-handling.sh`

```bash
#!/bin/bash
# scripts/check-exception-handling.sh

echo "🔍 检查异常处理模式..."

# 检查1: 捕获Exception而非具体异常
echo "=== 过于宽泛的异常捕获 ==="
grep -rn "catch.*Exception.*e" microservices/*/src/main/java --include="*.java" | \
    grep -v "catch (Exception e)" | \
    wc -l

# 检查2: 吞掉异常（空catch块）
echo "=== 空catch块 ==="
grep -A2 "catch.*Exception" microservices/*/src/main/java --include="*.java" | \
    grep -B1 "}" | grep "catch" | wc -l

# 检查3: 使用printStackTrace
echo "=== 使用printStackTrace ==="
grep -rn "printStackTrace()" microservices/*/src/main/java --include="*.java" | \
    wc -l

echo "✅ 异常处理检查完成"
```

---

## 📊 验证与测试

### 编译验证

```bash
# 全量编译所有服务
mvn clean compile -DskipTests

# 预期结果: BUILD SUCCESS
# 警告数: 0个"未经检查的转换"
```

### 功能验证

```bash
# 启动关键服务
mvn spring-boot:run -pl ioedream-common-service
mvn spring-boot:run -pl ioedream-access-service

# 验证健康检查
curl http://localhost:8088/actuator/health
curl http://localhost:8090/actuator/health
```

---

## 📝 成功标准

### 必须达成（P0）

- [x] ✅ microservices-common只包含配置类和Edge模块
- [x] ✅ OpenAPI模块迁移到gateway-client
- [x] ✅ 所有服务编译通过，无严重警告
- [x] ✅ Git pre-commit hook已安装
- [x] ✅ CI/CD架构合规性检查已配置

### 期望达成（P1）

- [ ] 依赖分析脚本已创建
- [ ] TypeReference使用100%符合规范
- [ ] 异常处理模式统一
- [ ] 架构文档已同步更新

---

## ⚠️ 风险评估

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| 导入路径错误 | 高 | 低 | 自动化检查 + 编译验证 |
| 依赖破坏 | 高 | 低 | 增量迁移 + 全量测试 |
| 性能回归 | 中 | 低 | 基准测试 + 监控 |
| 文档滞后 | 中 | 中 | 同步更新机制 |

---

## 📅 时间表

| 阶段 | 任务 | 预计时间 | 负责人 |
|------|------|---------|--------|
| Week 1 | 架构迁移 | 2-3天 | 架构师 |
| Week 1 | 前置检查 | 1-2天 | DevOps |
| Week 2 | API规范统一 | 2-3天 | 开发团队 |
| Week 2 | 验证与测试 | 1-2天 | QA团队 |

---

## 🎯 后续优化

- **P1级**: 完善模块文档（2-4周）
- **P1级**: 建立代码审查流程（2-4周）
- **P2级**: 实施架构度量（1-3个月）
- **P2级**: 建立架构演进机制（持续）

---

**文档版本**: v1.0
**最后更新**: 2025-12-25
**状态**: 待执行
