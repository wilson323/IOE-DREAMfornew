# Spring Boot 3.5 + Jakarta规范守护专家

## 核心职责
作为IOE-DREAM项目的Spring Boot 3.5和Jakarta包名规范守护专家，预防编译错误，确保技术栈合规性。

## 核心能力

### Jakarta包名合规守护

#### 严格包名规则
**必须使用**:
- `jakarta.annotation.*` (注解)
- `jakarta.validation.*` (验证)
- `jakarta.persistence.*` (持久化)
- `jakarta.servlet.*` (Servlet)
- `jakarta.xml.bind.*` (XML绑定)

**允许保留** (JDK标准库，非Jakarta迁移范围):
- `java.*` (例如 `java.sql.*`)
- `javax.crypto.*` (JCE加密)
- `javax.net.*` (网络)
- `javax.security.*` (安全)
- `javax.naming.*` (命名)
- `javax.xml.parsers.*` (XML解析)
- `org.w3c.dom.*` (DOM)

**严禁使用** (必须迁移到jakarta):
- `javax.annotation.*` → `jakarta.annotation.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.xml.bind.*` → `jakarta.xml.bind.*`

#### 自动检测和修复
```bash
# 检测违规使用（必须返回0）
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)

# 自动修复常见问题
find . -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.xml\.bind/jakarta.xml.bind/g' {} \;
```

#### 验证脚本
```bash
#!/bin/bash
# jakarta-compliance-check.sh

echo "🔍 执行Jakarta包名合规性检查..."

# 1. 检查违规包使用
echo "步骤1: 检查违规javax包使用"
violations=$(find . -name "*.java" -exec grep -lE "javax\.(annotation|validation|persistence|servlet|xml\.bind)" {} \;)

if [ ! -z "$violations" ]; then
    echo "❌ 发现违规javax包使用:"
    echo "$violations"
    echo ""
    echo "自动修复建议:"
    echo "find . -name '*.java' -exec sed -i 's/javax\\.annotation/jakarta.annotation/g' {} \\;"
    echo "find . -name '*.java' -exec sed -i 's/javax\\.validation/jakarta.validation/g' {} \\;"
    exit 1
fi

# 2. 验证jakarta包使用
echo "步骤2: 验证jakarta包使用"
jakarta_files=$(find . -name "*.java" -exec grep -l "jakarta\." {} \; | wc -l)
echo "✅ Jakarta包使用文件数: $jakarta_files"

# 3. 检查白名单包保留情况
echo "步骤3: 检查JDK标准库包保留情况"
jdk_packages=("javax.crypto" "javax.net" "javax.security" "javax.naming" "javax.xml.parsers")

for pkg in "${jdk_packages[@]}"; do
    count=$(find . -name "*.java" -exec grep -l "$pkg" {} \; | wc -l)
    if [ $count -gt 0 ]; then
        echo "✅ $pkg 正确保留: $count 个文件"
    fi
done

echo "🎉 Jakarta包名合规性检查通过！"
```

### 依赖注入规范守护

#### @Resource强制使用规则
**必须使用**:
```java
@Resource
private UserService userService;

@Resource
private SmartDeviceMapper deviceMapper;
```

**禁止使用**:
```java
@Autowired  // ❌ 禁止使用
private UserService userService;
```

#### 检测和修复
```bash
# 检测@Autowired使用（必须返回0）
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)

# 自动修复
find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
```

#### 验证脚本
```bash
#!/bin/bash
# resource-injection-check.sh

echo "🔍 执行依赖注入规范检查..."

# 1. 检查@Autowired违规
echo "步骤1: 检查@Autowired违规使用"
autowired_files=$(find . -name "*.java" -exec grep -l "@Autowired" {} \;)

if [ ! -z "$autowired_files" ]; then
    echo "❌ 发现@Autowired违规使用:"
    echo "$autowired_files"
    echo ""
    echo "自动修复命令:"
    echo "find . -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;"
    exit 1
fi

# 2. 验证@Resource使用
echo "步骤2: 验证@Resource使用"
resource_files=$(find . -name "*.java" -exec grep -l "@Resource" {} \; | wc -l)
echo "✅ @Resource使用文件数: $resource_files"

echo "🎉 依赖注入规范检查通过！"
```

### 编译预防守护

#### 预编译检查
```bash
#!/bin/bash
# pre-compilation-check.sh

echo "🔍 执行预编译检查..."

# 1. 包名合规检查
echo "检查1: Jakarta包名合规"
javax_count=$(find . -name "*.java" -exec grep -lE "javax\.(annotation|validation|persistence|servlet|xml\.bind)" {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 $javax_count 个违规javax包使用"
    exit 1
fi

# 2. 依赖注入检查
echo "检查2: @Resource依赖注入"
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 $autowired_count 个@Autowired违规使用"
    exit 1
fi

# 3. 基础类检查
echo "检查3: BaseEntity继承"
baseentity_check=$(find . -name "*Entity.java" -exec grep -L "extends BaseEntity" {} \;)
if [ ! -z "$baseentity_check" ]; then
    echo "⚠️ 发现未继承BaseEntity的实体类:"
    echo "$baseentity_check"
fi

# 4. 编译测试
echo "检查4: Maven编译测试"
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ Maven编译失败"
    mvn clean compile
    exit 1
fi

echo "🎉 预编译检查全部通过！"
```

#### 增量验证
```bash
#!/bin/bash
# incremental-validation.sh

# 只检查Git staged文件
echo "🔍 执行增量验证..."

staged_files=$(git diff --cached --name-only --diff-filter=ACM | grep "\.java$")

if [ -z "$staged_files" ]; then
    echo "✅ 没有Java文件变更"
    exit 0
fi

echo "检查文件:"
echo "$staged_files"

for file in $staged_files; do
    echo "检查文件: $file"

    # 检查javax违规
    javax_violations=$(grep -E "javax\.(annotation|validation|persistence|servlet|xml\.bind)" "$file" | wc -l)
    if [ $javax_violations -gt 0 ]; then
        echo "❌ $file 包含 $javax_violations 个javax违规"
        grep -n "javax\." "$file"
        exit 1
    fi

    # 检查@Autowired违规
    autowired_violations=$(grep "@Autowired" "$file" | wc -l)
    if [ $autowired_violations -gt 0 ]; then
        echo "❌ $file 包含 $autowired_violations 个@Autowired违规"
        grep -n "@Autowired" "$file"
        exit 1
    fi

    echo "✅ $file 检查通过"
done

echo "🎉 增量验证全部通过！"
```

### 错误恢复机制

#### 自动修复脚本
```bash
#!/bin/bash
# auto-fix-jakarta-issues.sh

echo "🔧 开始自动修复Jakarta相关问题..."

# 1. 备份原文件
echo "步骤1: 备份原文件"
mkdir -p backup/$(date +%Y%m%d_%H%M%S)
cp -r smart-admin-api-java17-springboot3 backup/$(date +%Y%m%d_%H%M%S)/

# 2. 修复javax包名
echo "步骤2: 修复javax包名违规"
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.xml\.bind/jakarta.xml.bind/g' {} \;

# 3. 修复依赖注入
echo "步骤3: 修复依赖注入注解"
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;

# 4. 验证修复结果
echo "步骤4: 验证修复结果"
javax_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -lE "javax\.(annotation|validation|persistence|servlet|xml\.bind)" {} \; | wc -l)
autowired_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)

if [ $javax_count -eq 0 ] && [ $autowired_count -eq 0 ]; then
    echo "🎉 自动修复成功！"
else
    echo "❌ 修复不完整，还存在违规:"
    echo "javax违规: $javax_count"
    echo "@Autowired违规: $autowired_count"
fi
```

### 编译错误快速定位

#### 错误分析脚本
```bash
#!/bin/bash
# analyze-compilation-errors.sh

echo "🔍 分析编译错误..."

# 执行编译并捕获错误
mvn clean compile 2>&1 | tee compilation_errors.log

# 分析错误类型
echo "=== 错误分析 ==="

# 包名错误
package_errors=$(grep -c "package.*does not exist" compilation_errors.log)
echo "包名错误: $package_errors"

# 找不到符号
symbol_errors=$(grep -c "cannot find symbol" compilation_errors.log)
echo "符号错误: $symbol_errors"

# 类型不匹配
type_errors=$(grep -c "incompatible types" compilation_errors.log)
echo "类型错误: $type_errors"

# 方法不存在
method_errors=$(grep -c "method.*does not exist" compilation_errors.log)
echo "方法错误: $method_errors"

# 显示具体错误
if [ $package_errors -gt 0 ]; then
    echo ""
    echo "=== 包名错误详情 ==="
    grep "package.*does not exist" compilation_errors.log | head -5
fi

if [ $symbol_errors -gt 0 ]; then
    echo ""
    echo "=== 符号错误详情 ==="
    grep "cannot find symbol" compilation_errors.log | head -5
fi
```

### Spring Boot 3.5 特定约束

#### 配置类约束
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 必须使用jakarta.annotation.*
    import jakarta.annotation.Resource;

    @Resource
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // HttpSecurity来自jakarta.servlet.*
        return http.build();
    }
}
```

#### Controller层约束
```java
@RestController
@RequestMapping("/api/access")
@Tag(name = "门禁管理", description = "门禁设备管理接口")
public class AccessControlController {

    @Resource
    private AccessControlService accessControlService;

    @PostMapping("/device/add")
    @SaCheckPermission("access:device:add")
    @Operation(summary = "添加门禁设备")
    public ResponseDTO<String> addDevice(@Valid @RequestBody DeviceAddRequest request) {
        // 参数验证使用jakarta.validation.Valid
        return ResponseDTO.ok(accessControlService.addDevice(request));
    }
}
```

### 集成验证钩子

#### Pre-commit Hook
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "🔍 执行pre-commit检查..."

# 1. Jakarta包名检查
echo "检查Jakarta包名合规性..."
./scripts/jakarta-compliance-check.sh
if [ $? -ne 0 ]; then
    echo "❌ Jakarta包名检查失败，提交被阻止"
    exit 1
fi

# 2. 依赖注入检查
echo "检查依赖注入规范..."
./scripts/resource-injection-check.sh
if [ $? -ne 0 ]; then
    echo "❌ 依赖注入检查失败，提交被阻止"
    exit 1
fi

# 3. 编译检查
echo "检查编译状态..."
./scripts/pre-compilation-check.sh
if [ $? -ne 0 ]; then
    echo "❌ 编译检查失败，提交被阻止"
    exit 1
fi

echo "🎉 所有检查通过，允许提交"
```

### 项目特定模板

#### 标准Controller模板
```java
package net.lab1024.sa.admin.module.{module}.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import {module}.service.{Module}Service;
import {module}.domain.request.{Module}QueryRequest;
import {module}.domain.request.{Module}AddRequest;
import {module}.domain.request.{Module}UpdateRequest;

@RestController
@RequestMapping("/api/{module}")
@Tag(name = "{Module}管理", description = "{Module}管理接口")
public class {Module}Controller {

    @Resource
    private {Module}Service {module}Service;

    @GetMapping("/query")
    @SaCheckPermission("{module}:query")
    @Operation(summary = "查询{module}")
    public ResponseDTO<PageResult<{Module}VO>> query{Module}(@Valid {Module}QueryRequest request) {
        return ResponseDTO.ok({module}Service.query{Module}(request));
    }

    @PostMapping("/add")
    @SaCheckPermission("{module}:add")
    @Operation(summary = "添加{module}")
    public ResponseDTO<String> add{Module}(@Valid @RequestBody {Module}AddRequest request) {
        return ResponseDTO.ok({module}Service.add{Module}(request));
    }
}
```

---

*最后更新: 2025-11-16*
*维护者: IOE-DREAM开发团队*