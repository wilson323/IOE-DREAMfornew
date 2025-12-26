# Logging Standards Guardian

**Expert Type**: Code Quality Standards Enforcer
**Version**: 1.0.0
**Last Updated**: 2025-01-22
**Scope**: IOE-DREAM Project Logging Standards

## 🎯 核心使命

确保IOE-DREAM项目所有Java代码严格遵循统一的日志规范，全部使用 `@Slf4j` 注解方式，杜绝传统Logger实例声明的混用情况。

## 📋 强制日志规范

### ✅ 标准日志模式（唯一允许的方式）

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SomeClass {
    public void someMethod() {
        log.info("[模块名] 操作描述, 参数={}", parameter);
        log.error("[模块名] 错误描述, error={}", e.getMessage(), e);
    }
}
```

### ❌ 严格禁止的日志模式

1. **传统Logger实例声明**（禁止）
```java
// ❌ 禁止：这些import必须移除
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SomeClass {
    // ❌ 禁止：这种Logger声明必须移除
    private static final Logger log = LoggerFactory.getLogger(SomeClass.class);
    private final Logger logger = LoggerFactory.getLogger(SomeClass.class);
}
```

2. **混合使用模式**（禁止）
```java
// ❌ 禁止：同时使用两种方式
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class SomeClass {
    private static final Logger log = LoggerFactory.getLogger(SomeClass.class); // 冲突！
}
```

## 🔍 检查清单

### 必须包含的元素
- [x] `import lombok.extern.slf4j.Slf4j;`
- [x] 类级别注解 `@Slf4j`
- [x] 使用 `log.xxx()` 而非 `logger.xxx()`

### 必须移除的元素
- [x] 所有 `import org.slf4j.Logger;`
- [x] 所有 `import org.slf4j.LoggerFactory;`
- [x] 所有 `private static final Logger log = LoggerFactory.getLogger(...);`
- [x] 所有 `private final Logger logger = LoggerFactory.getLogger(...);`

### 日志格式标准
- [x] 使用中括号标记模块：`[模块名]`
- [x] 参数使用 `{}` 占位符
- [x] 异常信息包含堆栈：`, e`

## 🔧 修复策略

### 策略1：传统Logger → @Slf4j 转换
```bash
# 识别目标文件
grep -r "import org\.slf4j\.Logger" --include="*.java"

# 自动修复脚本
sed -i '/import org\.slf4j\.Logger/d' filename.java
sed -i '/import org\.slf4j\.LoggerFactory/d' filename.java
sed -i '/private.*Logger.*getLogger/d' filename.java
sed -i '/^public class/a @Slf4j' filename.java
sed -i '/^package/a import lombok.extern.slf4j.Slf4j;' filename.java
```

### 策略2：清理混合使用
```bash
# 识别混合文件
grep -l "import lombok\.extern\.slf4j\.Slf4j" $(grep -l "import org\.slf4j\.Logger" --include="*.java" -r .)

# 清理传统Logger，保留@Slf4j
# 只移除传统Logger相关代码，保留@Slf4j注解和import
```

### 策略3：验证修复结果
```bash
# 确保无传统Logger
find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \;  # 应为空

# 确保全部使用@Slf4j
find . -name "*.java" -exec grep -l "import lombok\.extern\.slf4j\.Slf4j" {} \;  # 应包含所有使用日志的文件
```

## 🚨 企业级质量门禁

### Pre-commit Hook 检查
```bash
#!/bin/bash
# 检查是否有违反日志规范的文件提交
if git diff --cached --name-only --diff-filter=ACM | xargs grep -l "import org\.slf4j\.Logger" 2>/dev/null; then
    echo "❌ 发现违反日志规范的文件，请使用 @Slf4j 方式"
    exit 1
fi
```

### CI/CD 流水线检查
```yaml
logging-standards-check:
  stage: quality-gate
  script:
    - find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; | wc -l
    # 违规数量必须为0
    - if [ $(find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; | wc -l) -ne 0 ]; then exit 1; fi
```

## 📊 质量指标

- **合规率目标**: 100%
- **允许偏差**: 0%（严禁任何传统Logger方式）
- **检查范围**: 所有Java文件
- **修复时限**: 发现即修复

## 🔗 相关资源

- **修复脚本**: `microservices/fix-logging-patterns.sh`
- **质量门禁**: `microservices/establish-quality-gates.sh`
- **编码规范**: `CLAUDE.md` 日志章节
- **技术文档**: `documentation/technical/SLF4J_UNIFIED_STANDARD.md`

## ⚡ 快速诊断命令

```bash
# 快速检查日志规范合规性
echo "🔍 检查日志规范合规性..."
TRADITIONAL_COUNT=$(find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null | wc -l)
SLF4J_COUNT=$(find . -name "*.java" -exec grep -l "import lombok\.extern\.slf4j\.Slf4j" {} \; 2>/dev/null | wc -l)

echo "📊 统计结果:"
echo "  ❌ 传统Logger文件: $TRADITIONAL_COUNT"
echo "  ✅ @Slf4j文件: $SLF4J_COUNT"
echo "  🎯 合规率: $((100 - TRADITIONAL_COUNT))%"

if [ $TRADITIONAL_COUNT -eq 0 ]; then
    echo "🎉 日志规范完全合规！"
else
    echo "⚠️ 需要修复 $TRADITIONAL_COUNT 个文件"
fi
```

---

**维护责任**: IOE-DREAM 架构委员会
**执行优先级**: P0（最高优先级）
**合规要求**: 100%（零容忍）