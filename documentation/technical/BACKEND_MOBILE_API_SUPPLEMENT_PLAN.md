# 后端移动端API补充计划

**生成时间**: 2025-01-30  
**版本**: v1.0.0  
**状态**: 📋 **待实施**

---

## 📋 需要补充的移动端API接口

### 1. 消费模块 - 用户统计接口（P1级）

**接口路径**: `GET /api/v1/consume/mobile/stats/{userId}`

**功能描述**: 获取指定用户的消费统计数据

**请求参数**:
- `userId` (Path): 用户ID

**响应数据**:
```java
public class ConsumeMobileUserStatsVO {
    private Long userId;              // 用户ID
    private Integer totalCount;        // 总交易笔数
    private BigDecimal totalAmount;    // 总消费金额
    private Integer todayCount;        // 今日交易笔数
    private BigDecimal todayAmount;    // 今日消费金额
    private Integer monthCount;        // 本月交易笔数
    private BigDecimal monthAmount;    // 本月消费金额
}
```

**Controller方法**:
```java
@GetMapping("/stats/{userId}")
@Operation(summary = "获取用户统计", description = "获取指定用户的消费统计数据")
@SaCheckLogin
public ResponseDTO<ConsumeMobileUserStatsVO> getUserStats(@PathVariable Long userId) {
    log.info("获取用户统计: userId={}", userId);
    ConsumeMobileUserStatsVO stats = consumeMobileService.getUserStats(userId);
    return ResponseDTO.ok(stats);
}
```

**Service方法**:
```java
/**
 * 获取用户统计
 *
 * @param userId 用户ID
 * @return 统计数据
 */
ConsumeMobileUserStatsVO getUserStats(Long userId);
```

**预计工作量**: 1-2天

---

### 2. 门禁模块 - 区域列表接口（P1级）

**接口路径**: `GET /api/v1/mobile/access/areas`

**功能描述**: 获取用户有权限访问的区域列表（包含区域详情）

**请求参数**:
- `userId` (Query): 用户ID（可选，不传则从Token获取）

**响应数据**:
```java
public class MobileAreaItem {
    private Long areaId;           // 区域ID
    private String areaName;       // 区域名称
    private String areaType;       // 区域类型
    private Integer deviceCount;    // 设备数量
    private Integer permissionCount; // 权限数量
    private String description;    // 区域描述
    private Boolean active;        // 是否有效
}
```

**Controller方法**:
```java
@GetMapping("/areas")
@Operation(summary = "获取区域列表", description = "获取用户有权限访问的区域列表")
@SaCheckLogin
public ResponseDTO<List<MobileAreaItem>> getAreas(@RequestParam(required = false) Long userId) {
    log.info("获取区域列表: userId={}", userId);
    List<MobileAreaItem> areas = accessDeviceService.getMobileAreas(userId);
    return ResponseDTO.ok(areas);
}
```

**Service方法**:
```java
/**
 * 获取移动端区域列表
 *
 * @param userId 用户ID（可选）
 * @return 区域列表
 */
ResponseDTO<List<MobileAreaItem>> getMobileAreas(Long userId);
```

**预计工作量**: 1-2天

---

## 📝 实施步骤

### 步骤1: 消费模块用户统计接口

1. **创建VO类**
   - 文件: `ConsumeMobileUserStatsVO.java`
   - 位置: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/domain/vo/`

2. **添加Service方法**
   - 接口: `ConsumeMobileService.getUserStats()`
   - 实现: `ConsumeMobileServiceImpl.getUserStats()`
   - 调用Manager层进行数据统计

3. **添加Controller方法**
   - 文件: `ConsumeMobileController.java`
   - 路径: `/api/v1/consume/mobile/stats/{userId}`

### 步骤2: 门禁模块区域列表接口

1. **创建VO类**
   - 文件: `MobileAreaItem.java`
   - 位置: `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java`（内部类）

2. **添加Service方法**
   - 接口: `AccessDeviceService.getMobileAreas()`
   - 实现: `AccessDeviceServiceImpl.getMobileAreas()`
   - 查询用户有权限的区域，并获取区域详情

3. **添加Controller方法**
   - 文件: `AccessMobileController.java`
   - 路径: `/api/v1/mobile/access/areas`

---

## ✅ 验收标准

1. **接口实现完整**: 所有接口按照CLAUDE.md规范实现
2. **数据准确性**: 统计数据准确无误
3. **性能要求**: 接口响应时间<200ms
4. **错误处理**: 完善的异常处理和错误提示
5. **文档完善**: Swagger文档完整

---

## 📊 优先级说明

| 接口 | 优先级 | 影响范围 | 预计工作量 |
|------|--------|---------|-----------|
| 用户统计接口 | P1 | 消费模块移动端 | 1-2天 |
| 区域列表接口 | P1 | 门禁模块移动端 | 1-2天 |

---

**计划制定人**: IOE-DREAM 架构委员会  
**版本**: v1.0.0  
**状态**: 📋 待实施

