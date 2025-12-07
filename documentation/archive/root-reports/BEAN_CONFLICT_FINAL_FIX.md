# Bean冲突最终修复方案

## 🔍 问题根本原因

### 错误信息
```
ConflictingBeanDefinitionException: Annotation-specified bean name 'userDao' for bean class 
[net.lab1024.sa.common.security.dao.UserDao] conflicts with existing, non-compatible bean 
definition of same name and class [net.lab1024.sa.common.auth.dao.UserDao]
```

### 核心问题
1. **MyBatis的`@Mapper`注解不支持`value()`属性** - 这是标记注解，不能指定Bean名称
2. **Spring默认Bean命名规则** - 使用类名（首字母小写）作为Bean名称
3. **同名DAO接口冲突** - 两个`UserDao`和两个`EmployeeDao`接口

## ✅ 最终修复方案

### 方案：自定义Bean名称生成器

使用`@MapperScan`的`nameGenerator`属性，创建自定义的Bean名称生成器，根据包路径生成唯一的Bean名称。

---

**报告生成时间**: 2025-01-30  
**执行人员**: IOE-DREAM架构委员会  
**报告版本**: v1.0.0
