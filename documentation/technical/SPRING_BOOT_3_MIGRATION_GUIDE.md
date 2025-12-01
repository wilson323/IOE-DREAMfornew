# Spring Boot 3.x 迁移指南

## 概述
本文档指导项目从Spring Boot 2.x升级到3.x的完整迁移过程，确保所有代码符合3.x规范。

## 🚨 必须执行的迁移步骤

### 1. 包名迁移 (Critical)
```bash
# 全量替换javax.* → jakarta.*
find . -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;

# 验证替换结果
find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l  # 应该输出0
```

### 2. 依赖注入规范 (Critical)
```java
// ❌ 禁止使用
@Autowired
private SomeService someService;

// ✅ 必须使用
@Resource
private SomeService someService;
```

### 3. API变更处理 (Critical)

#### TransactionInterceptor 构造函数变更
```java
// Spring Boot 2.x (❌ 废弃)
return new TransactionInterceptor(transactionManager, transactionAttribute);

// Spring Boot 3.x (✅ 正确)
return new TransactionInterceptor(transactionManager, transactionAttributeSource);
```

#### JPA 包名变更
```java
// ❌ 废弃
import javax.persistence.*;
import javax.validation.Valid;

// ✅ 正确
import jakarta.persistence.*;
import jakarta.validation.Valid;
```

### 4. 枚举类规范 (Critical)
```java
// ❌ 错误 - 不使用Lombok注解
@AllArgsConstructor
public enum ConfigKeyEnum {
    VALUE("code", "desc");
}

// ✅ 正确 - 手动实现
public enum ConfigKeyEnum {
    VALUE("code", "desc");

    private final String code;
    private final String desc;

    ConfigKeyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
```

### 5. ResponseDTO 使用规范 (Critical)
```java
// ❌ 错误方法调用
ResponseDTO.error("错误消息");  // 此方法不存在

// ✅ 正确方法调用
ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "错误消息");
ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "错误消息", data);
```

## 📋 迁移检查清单

### Phase 1: 包名和基础迁移
- [ ] 所有javax.*包替换为jakarta.*
- [ ] @Autowired替换为@Resource
- [ ] 实体类继承BaseEntity验证
- [ ] 四层架构调用规范检查

### Phase 2: API兼容性处理
- [ ] TransactionInterceptor构造函数更新
- [ ] 所有Spring API变更处理
- [ ] 第三方库兼容性验证
- [ ] 配置类注解更新

### Phase 3: 代码质量检查
- [ ] 枚举类构造函数实现
- [ ] Lombok注解使用规范检查
- [ ] 异常处理API使用规范
- [ ] ResponseDTO API调用验证

### Phase 4: 构建和测试
- [ ] Maven编译通过
- [ ] 单元测试执行
- [ ] 集成测试验证
- [ ] 功能回归测试

## 🔧 工具和脚本

### 编译检查脚本
```bash
#!/bin/bash
# spring-boot-3-migration-check.sh

echo "🔍 Spring Boot 3.x 迁移检查..."

# 检查javax包使用
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -gt 0 ]; then
    echo "❌ 发现 $javax_count 个文件仍使用javax包"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi

# 检查@Autowired使用
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -gt 0 ]; then
    echo "❌ 发现 $autowired_count 个文件仍使用@Autowired"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi

# 检查编译
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✅ 编译检查通过"
else
    echo "❌ 编译失败，请检查日志"
    exit 1
fi

echo "🎉 迁移检查完成！"
```

### IDE配置同步
```xml
<!-- .idea/compiler.xml -->
<component name="CompilerConfiguration">
  <bytecodeTargetLevel>
    <module name="sa-base" target="17" />
  </bytecodeTargetLevel>
</component>
```

## 🚨 常见问题和解决方案

### 问题1: 找不到符号 jakarta.*
**解决方案**: 确保Spring Boot版本≥3.0.0

### 问题2: 实体类不继承BaseEntity
**解决方案**: 检查实体类继承关系

### 问题3: 枚举构造函数错误
**解决方案**: 移除Lombok注解，手动实现构造函数

### 问题4: ResponseDTO API错误
**解决方案**: 使用ErrorCode枚举而非字符串

## 📚 参考资源

- [Spring Boot 3.x 迁移指南](https://spring.io/blog/2022/02/21/spring-boot-3-0-0-m1-available)
- [Jakarta EE 9+ 规范](https://jakarta.ee/)
- [项目开发规范体系](./repowiki/zh/content/开发规范体系.md)