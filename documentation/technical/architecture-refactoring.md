# 考勤模块AttendanceCacheManager架构重构方案

## 问题分析

当前架构状态：AttendanceCacheManager重复定义在两个位置
- sa-base版本：完整版698行，有@Slf4j，功能完整
- sa-admin版本：简化版348行，缺@Slf4j（编译错误源），功能简化

依赖混乱：
- AttendanceServiceImpl使用sa-admin版本  
- AttendanceRuleEngine使用sa-admin版本
- sa-base中的Manager和Service使用sa-base版本

## 架构重构方案

保留sa-base版本，删除sa-admin版本

理由：
1. sa-base版本功能更完整
2. sa-base版本有正确的@Slf4j注解
3. 符合repowiki架构规范（基础设施在base层）
4. sa-admin版本缺少@Slf4j导致编译错误
5. 减少代码重复

## 实施步骤

阶段一：在sa-base版本中补充兼容方法
阶段二：更新sa-admin中的导入引用  
阶段三：删除sa-admin版本
阶段四：编译验证

## 验收标准

- 编译零错误
- 无找不到符号log的错误
- 无重复类定义警告
- 所有引用指向sa-base版本
