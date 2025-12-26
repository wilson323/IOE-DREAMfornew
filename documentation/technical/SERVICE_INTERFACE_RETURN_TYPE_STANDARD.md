# Service接口返回类型统一规范

## 📋 规范目标

统一IOE-DREAM项目中所有Service接口的返回类型，解决编译错误，提高代码一致性和类型安全性。

## 🎯 核心原则

### 1. Controller层职责明确化
- Controller负责处理HTTP请求/响应
- Service层负责业务逻辑，不直接处理HTTP响应包装
- Controller调用Service后进行ResponseDTO包装

### 2. Service接口返回类型标准化
所有Service接口方法统一返回以下类型之一：
- `PageResult<T>` - 分页查询
- `T` - 单个对象
- `List<T>` - 列表
- `Map<String, Object>` - 复杂数据结构（仅限报表类）
- `void` - 无返回值操作
- `Boolean` - 布尔结果
- `Long/Integer` - 数值结果

### 3. ResponseDTO包装层统一
只在Controller层进行ResponseDTO包装，Service层和Manager层不使用ResponseDTO。

## 🔧 具体实施规范

### A. CRUD操作返回类型

```java
// 查询类
PageResult<EntityVO> queryPage(QueryForm form);
EntityVO getDetail(Long id);
List<EntityVO> getList(Map<String, Object> params);
Map<String, Object> getStatistics(...);

// 修改类
Long addEntity(AddForm form);        // 返回新增ID
void updateEntity(Long id, UpdateForm form);
void deleteEntity(Long id);

// 状态操作
void enableEntity(Long id);
void disableEntity(Long id);
```

### B. 业务操作返回类型

```java
// 业务执行类
BusinessResultVO executeBusiness(BusinessForm form);
Boolean validateBusiness(BusinessForm form);

// 报表类（特殊处理）
Map<String, Object> generateReport(ReportForm form);
List<Map<String, Object>> exportData(ExportForm form);
```

### C. Controller层包装规范

```java
@RestController
public class EntityController {

    @Resource
    private EntityService entityService;

    @GetMapping("/page")
    public ResponseDTO<PageResult<EntityVO>> queryPage(EntityQueryForm form) {
        PageResult<EntityVO> result = entityService.queryPage(form);
        return ResponseDTO.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseDTO<EntityVO> getDetail(@PathVariable Long id) {
        EntityVO result = entityService.getDetail(id);
        return ResponseDTO.ok(result);
    }

    @PostMapping
    public ResponseDTO<Long> addEntity(@Valid @RequestBody EntityAddForm form) {
        Long newId = entityService.addEntity(form);
        return ResponseDTO.ok(newId);
    }
}
```

## 📊 现有问题分析

### 1. ConsumeSubsidyService返回类型不一致

**问题**：
```java
// 接口定义
PageResult<ConsumeSubsidyVO> querySubsidyPage(ConsumeSubsidyQueryForm queryForm);

// 实现类
public ResponseDTO<PageResult<ConsumeSubsidyVO>> querySubsidyPage(ConsumeSubsidyQueryForm queryForm) {
    // 实现逻辑
    return ResponseDTO.ok(result);
}
```

**修复方案**：
```java
// 实现类应改为
public PageResult<ConsumeSubsidyVO> querySubsidyPage(ConsumeSubsidyQueryForm queryForm) {
    // 实现逻辑，直接返回PageResult
    return pageResult;
}
```

### 2. ConsumeReportServiceMap返回缺乏类型安全

**问题**：
```java
// 当前实现
Map<String, Object> generateDailyConsumptionReport(LocalDate date, String format);

// 返回数据结构不明确，容易出错
```

**修复方案**：
```java
// 方案1：保持Map但提供标准结构
public class ReportResult {
    private String reportType;
    private LocalDate reportDate;
    private Map<String, Object> data;
    private String status;
    private String message;
}

// 方案2：定义专门的报表VO
public class DailyConsumptionReportVO {
    private String reportType;
    private LocalDate date;
    private BigDecimal totalAmount;
    private Integer totalCount;
    private List<ConsumptionDetailVO> details;
}
```

## 🚀 分阶段实施计划

### Phase 1: Service接口修复（P0）
1. 修复ConsumeSubsidyService所有方法的返回类型
2. 统一CRUD操作的返回模式
3. 移除Service层的ResponseDTO包装

### Phase 2: 报表Service类型化（P1）
1. 为报表类Service定义专门的VO类
2. 替换Map<String, Object>返回类型
3. 提供类型安全的报表数据结构

### Phase 3: Controller层标准化（P1）
1. 确保Controller正确包装Service返回结果
2. 统一异常处理
3. 标准化API响应格式

## 📋 检查清单

- [ ] Service接口方法返回类型符合规范
- [ ] Service实现类返回类型与接口一致
- [ ] Controller层正确包装ResponseDTO
- [ ] 移除Service层的ResponseDTO使用
- [ ] 报表类Service使用类型安全的VO
- [ ] 所有新增Service遵循统一规范

## 🔍 代码审查要点

1. **Service层**：检查方法签名，确保不返回ResponseDTO
2. **Controller层**：检查是否正确包装Service返回结果
3. **测试用例**：验证返回类型一致性
4. **API文档**：确保Swagger文档反映正确的返回类型

---

**制定人**: IOE-DREAM架构委员会
**版本**: v1.0
**生效日期**: 2025-12-22