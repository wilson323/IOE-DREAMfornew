# 工具类清理计划

## 📋 发现的重复工具类

### 1. SmartPageUtil
- **consume-service**: `net.lab1024.sa.consume.common.SmartPageUtil` - 临时类，功能简单
- **common模块**: `net.lab1024.sa.common.util.SmartPageUtil` - 完整功能，包含SQL注入检测

**建议**: 删除consume-service中的临时类，使用common模块版本

### 2. SmartBeanUtil
- **consume-service**: `net.lab1024.sa.consume.common.SmartBeanUtil` - 临时类，功能简单
- **common模块**: `net.lab1024.sa.common.util.SmartBeanUtil` - 完整功能，包含验证功能

**建议**: 删除consume-service中的临时类，使用common模块版本

### 3. RedisUtil
- **consume-service**: `net.lab1024.sa.consume.common.RedisUtil` - 临时类，功能简单
- **common模块**: `net.lab1024.sa.common.util.RedisUtil` - 完整功能，支持多种数据结构
- **common/cache**: `net.lab1024.sa.common.cache.RedisUtil` - 缓存专用版本
- **video-service**: `net.lab1024.sa.video.util.RedisTemplateUtil` - RedisTemplate封装

**建议**: 
- 删除consume-service中的临时类
- 统一使用common模块的RedisUtil
- 评估video-service的RedisTemplateUtil是否可以合并

## 🎯 清理步骤

1. 检查所有使用这些临时工具类的地方
2. 更新import语句，改为使用common模块
3. 验证功能是否正常
4. 删除临时工具类文件

