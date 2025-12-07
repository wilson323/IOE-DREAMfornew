# 后端移动端API实施指南

**生成时间**: 2025-01-30  
**版本**: v1.0.0  
**状态**: 📋 **待实施**

---

## 📋 概述

本文档提供后端移动端API接口的详细实施指南，包括代码模板、实施步骤和验收标准。

---

## 🎯 需要实现的接口

### 1. 消费模块 - 用户统计接口

**接口路径**: `GET /api/v1/consume/mobile/stats/{userId}`

**功能描述**: 获取指定用户的消费统计数据（总交易笔数、总金额、今日统计、本月统计）

---

## 📝 实施步骤

### 步骤1: 创建VO类

**文件**: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/domain/vo/ConsumeMobileUserStatsVO.java`

```java
package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 移动端用户消费统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端用户消费统计")
public class ConsumeMobileUserStatsVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "总交易笔数")
    private Integer totalCount;

    @Schema(description = "总消费金额")
    private BigDecimal totalAmount;

    @Schema(description = "今日交易笔数")
    private Integer todayCount;

    @Schema(description = "今日消费金额")
    private BigDecimal todayAmount;

    @Schema(description = "本月交易笔数")
    private Integer monthCount;

    @Schema(description = "本月消费金额")
    private BigDecimal monthAmount;
}
```

### 步骤2: 在Service接口中添加方法

**文件**: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/service/ConsumeMobileService.java`

在接口中添加：

```java
/**
 * 获取用户统计
 *
 * @param userId 用户ID
 * @return 统计数据
 */
ConsumeMobileUserStatsVO getUserStats(Long userId);
```

### 步骤3: 实现Service方法

**文件**: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeMobileServiceImpl.java`

```java
@Override
public ConsumeMobileUserStatsVO getUserStats(Long userId) {
    log.info("获取用户统计: userId={}", userId);
    
    ConsumeMobileUserStatsVO stats = new ConsumeMobileUserStatsVO();
    stats.setUserId(userId);
    
    try {
        // 1. 查询总交易统计
        // 使用ConsumeTransactionDao查询
        // SELECT COUNT(*) as totalCount, SUM(amount) as totalAmount 
        // FROM consume_transaction 
        // WHERE user_id = ? AND status = 'SUCCESS'
        
        // 2. 查询今日交易统计
        // SELECT COUNT(*) as todayCount, SUM(amount) as todayAmount 
        // FROM consume_transaction 
        // WHERE user_id = ? AND status = 'SUCCESS' 
        // AND DATE(create_time) = CURDATE()
        
        // 3. 查询本月交易统计
        // SELECT COUNT(*) as monthCount, SUM(amount) as monthAmount 
        // FROM consume_transaction 
        // WHERE user_id = ? AND status = 'SUCCESS' 
        // AND YEAR(create_time) = YEAR(CURDATE()) 
        // AND MONTH(create_time) = MONTH(CURDATE())
        
        // 4. 设置统计结果
        // stats.setTotalCount(...);
        // stats.setTotalAmount(...);
        // ...
        
        return stats;
    } catch (Exception e) {
        log.error("获取用户统计失败: userId={}", userId, e);
        throw new BusinessException("获取用户统计失败：" + e.getMessage());
    }
}
```

### 步骤4: 在Controller中添加接口

**文件**: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/controller/ConsumeMobileController.java`

在Controller中添加：

```java
/**
 * 获取用户统计
 *
 * @param userId 用户ID
 * @return 统计数据
 */
@GetMapping("/stats/{userId}")
@Operation(summary = "获取用户统计", description = "获取指定用户的消费统计数据")
@SaCheckLogin
public ResponseDTO<ConsumeMobileUserStatsVO> getUserStats(@PathVariable Long userId) {
    log.info("获取用户统计: userId={}", userId);
    ConsumeMobileUserStatsVO stats = consumeMobileService.getUserStats(userId);
    return ResponseDTO.ok(stats);
}
```

---

## 🎯 接口2: 门禁模块 - 区域列表接口

**接口路径**: `GET /api/v1/mobile/access/areas`

**功能描述**: 获取用户有权限访问的区域列表（包含区域详情）

---

## 📝 实施步骤

### 步骤1: 创建VO类（内部类）

**文件**: `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java`

在AccessMobileController中添加内部类：

```java
/**
 * 移动端区域项
 */
@Data
public static class MobileAreaItem {
    private Long areaId;           // 区域ID
    private String areaName;       // 区域名称
    private String areaType;       // 区域类型
    private Integer deviceCount;    // 设备数量
    private Integer permissionCount; // 权限数量
    private String description;    // 区域描述
    private Boolean active;        // 是否有效
}
```

### 步骤2: 在Service接口中添加方法

**文件**: `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/AccessDeviceService.java`

在接口中添加：

```java
/**
 * 获取移动端区域列表
 *
 * @param userId 用户ID（可选）
 * @return 区域列表
 */
ResponseDTO<List<MobileAreaItem>> getMobileAreas(Long userId);
```

### 步骤3: 实现Service方法

**文件**: `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/impl/AccessDeviceServiceImpl.java`

```java
@Override
public ResponseDTO<List<MobileAreaItem>> getMobileAreas(Long userId) {
    log.info("获取移动端区域列表: userId={}", userId);
    
    try {
        List<MobileAreaItem> areas = new ArrayList<>();
        
        // 1. 获取用户有权限的区域ID列表
        // 通过AreaPersonDao查询用户权限
        // SELECT DISTINCT area_id FROM area_person WHERE user_id = ? AND status = 1
        
        // 2. 查询区域详情
        // 通过AccessAreaDao查询区域信息
        // SELECT * FROM access_area WHERE area_id IN (...) AND deleted_flag = 0
        
        // 3. 统计每个区域的设备数量
        // SELECT area_id, COUNT(*) as device_count 
        // FROM access_device 
        // WHERE area_id IN (...) AND deleted_flag = 0
        // GROUP BY area_id
        
        // 4. 构建MobileAreaItem列表
        // for (AccessAreaEntity area : areaList) {
        //     MobileAreaItem item = new MobileAreaItem();
        //     item.setAreaId(area.getAreaId());
        //     item.setAreaName(area.getAreaName());
        //     item.setAreaType(area.getAreaType());
        //     item.setDeviceCount(deviceCountMap.get(area.getAreaId()));
        //     item.setPermissionCount(1); // 用户有权限
        //     item.setDescription(area.getDescription());
        //     item.setActive(area.getStatus() == 1);
        //     areas.add(item);
        // }
        
        return ResponseDTO.ok(areas);
    } catch (Exception e) {
        log.error("获取移动端区域列表失败: userId={}", userId, e);
        return ResponseDTO.error("获取区域列表失败：" + e.getMessage());
    }
}
```

### 步骤4: 在Controller中添加接口

**文件**: `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java`

在Controller中添加：

```java
/**
 * 获取区域列表
 *
 * @param userId 用户ID（可选，不传则从Token获取）
 * @return 区域列表
 */
@GetMapping("/areas")
@Operation(summary = "获取区域列表", description = "获取用户有权限访问的区域列表")
@SaCheckLogin
public ResponseDTO<List<MobileAreaItem>> getAreas(@RequestParam(required = false) Long userId) {
    log.info("获取区域列表: userId={}", userId);
    
    // 如果userId为空，从Token获取
    if (userId == null) {
        // 从SaToken获取当前用户ID
        // userId = StpUtil.getLoginIdAsLong();
    }
    
    return accessDeviceService.getMobileAreas(userId);
}
```

---

## ✅ 验收标准

### 功能验收

1. **接口可访问**: 接口路径正确，可正常访问
2. **数据准确性**: 统计数据准确无误
3. **权限控制**: 用户只能查询自己的统计数据
4. **错误处理**: 完善的异常处理和错误提示

### 性能验收

1. **响应时间**: 接口响应时间 < 200ms
2. **并发支持**: 支持100+并发请求
3. **数据库优化**: 使用索引优化查询性能

### 代码质量

1. **规范遵循**: 严格遵循CLAUDE.md规范
2. **注释完整**: 方法注释完整，包含参数说明
3. **日志记录**: 关键操作记录日志
4. **Swagger文档**: Swagger注解完整

---

## 🧪 测试用例

### 测试用例1: 获取用户统计

**请求**:
```http
GET /api/v1/consume/mobile/stats/1
Authorization: Bearer {token}
```

**预期响应**:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "totalCount": 150,
    "totalAmount": 5000.00,
    "todayCount": 5,
    "todayAmount": 200.00,
    "monthCount": 50,
    "monthAmount": 2000.00
  }
}
```

### 测试用例2: 获取区域列表

**请求**:
```http
GET /api/v1/mobile/access/areas?userId=1
Authorization: Bearer {token}
```

**预期响应**:
```json
{
  "success": true,
  "data": [
    {
      "areaId": 1,
      "areaName": "办公区A",
      "areaType": "办公区",
      "deviceCount": 5,
      "permissionCount": 1,
      "description": "办公区域A",
      "active": true
    }
  ]
}
```

---

## 📊 数据库查询优化建议

### 用户统计查询优化

1. **创建索引**:
```sql
-- 用户ID和状态索引
CREATE INDEX idx_user_status ON consume_transaction(user_id, status);

-- 用户ID和时间索引
CREATE INDEX idx_user_time ON consume_transaction(user_id, create_time);
```

2. **使用缓存**: 统计数据可以缓存5分钟，减少数据库查询

### 区域列表查询优化

1. **创建索引**:
```sql
-- 用户权限索引
CREATE INDEX idx_user_area ON area_person(user_id, status);

-- 区域ID索引
CREATE INDEX idx_area_id ON access_area(area_id);
```

2. **批量查询**: 使用IN查询批量获取区域信息

---

## 🔧 实施注意事项

1. **参数校验**: 验证userId是否有效
2. **权限控制**: 确保用户只能查询自己的数据
3. **异常处理**: 完善的异常处理和错误提示
4. **日志记录**: 记录关键操作和错误信息
5. **性能优化**: 使用索引和缓存优化查询性能

---

**文档生成人**: IOE-DREAM 架构委员会  
**版本**: v1.0.0  
**状态**: 📋 待实施

