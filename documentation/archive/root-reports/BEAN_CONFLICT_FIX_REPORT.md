# Bean冲突修复报告

## 🔍 问题分析

### 根本原因
Spring Boot测试上下文加载失败，错误信息：
```
ConflictingBeanDefinitionException: Annotation-specified bean name 'userDao' for bean class 
[net.lab1024.sa.common.security.dao.UserDao] conflicts with existing, non-compatible bean 
definition of same name and class [net.lab1024.sa.common.auth.dao.UserDao]
```

**核心问题**：
- 存在两个`UserDao`接口，都使用`@Mapper`注解
- Spring默认使用类名（首字母小写）作为Bean名称，两个都注册为`userDao`
- MyBatis的`@MapperScan`扫描到两个同名Bean，导致冲突

## ✅ 修复方案

为每个DAO接口指定唯一的Bean名称。

---

**报告生成时间**: 2025-01-30  
**执行人员**: IOE-DREAM架构委员会  
**报告版本**: v1.0.0
