# 消费服务模块编译错误修复指南

## 修复优先级和步骤

### P0级修复 (阻塞编译)

#### 1. 异常类构造函数修复
```java
// ❌ 错误用法 (需要修复的文件)
- ConsumeProductImportExportService.java:64,79,93,98,149,171,211
- ConsumeProductPriceService.java:50,53,58,87,137,151,156,159,162,167,170,177,189,226,283
- ConsumeProductStockService.java:49,64,112,124,129,141,153,158,176,188,205,251,301
- ConsumeProductValidationService.java:47,65,79,84,117,122,148,160,163,168,173,180,185,191,197,208

// ✅ 正确修复方案
// 将所有
throw new ConsumeProductException("错误消息");
// 修改为
throw new ConsumeProductException(ConsumeProductException.ErrorCode.INVALID_PARAMETER, "错误消息");

// 或使用便捷工厂方法
throw new ConsumeProductException.invalidParameter("错误消息");
```

#### 2. 工具类导入修复
```java
// ❌ 需要删除的导入
import net.lab1024.sa.consume.util.*;

// ✅ 需要添加的导入
import org.springframework.beans.BeanUtils;
import java.util.ArrayList;

// ❌ 需要修复的方法调用
BeanUtil.copyProperties(source, target);

// ✅ 正确的方法调用
BeanUtils.copyProperties(source, target);
```

#### 3. 类型引用修复
```java
// ❌ 错误类型
ConsumeAddForm
ConsumeProductException(String message)

// ✅ 正确类型
ConsumeProductAddForm
ConsumeProductException(ErrorCode errorCode, String message)
```

### P1级修复 (功能修复)

#### 4. 实体类字段映射修复
```java
// ❌ 错误字段名映射
entity.getStock()           -> entity.getStockQuantity()
entity.setStock(value)      -> entity.setStockQuantity(value)
entity.getProductSort()     -> entity.getRecommendSort()
entity.sort                 -> entity.recommendSort
form.getMinStock()          -> form.getStockStatus() 或新增字段
form.getOrderField()        -> form.getSortBy()
form.getOrderDirection()    -> form.getSortDirection() // 已存在
```

#### 5. MyBatis-Plus使用修复
```java
// ❌ 错误的分页查询
userDao.selectPage(queryForm, lambdaWrapper);

// ✅ 正确的分页查询
Page<ConsumeProductEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
userDao.selectPage(page, lambdaWrapper);

// ❌ 错误的Lambda引用
orderByAsc(ConsumeProductEntity::getProductSort)

// ✅ 正确的Lambda引用
orderByAsc(ConsumeProductEntity::getRecommendSort)
```

#### 6. BigDecimal过时API修复
```java
// ❌ 过时用法
price.setScale(2, BigDecimal.ROUND_HALF_UP)
price.divide(divisor, 2, BigDecimal.ROUND_HALF_UP)

// ✅ 现代用法
price.setScale(2, RoundingMode.HALF_UP)
price.divide(divisor, 2, RoundingMode.HALF_UP)

// 需要添加的导入
import java.math.RoundingMode;
```

## 修复文件清单

### 需要修复的文件列表
1. ConsumeProductImportExportService.java (8个错误)
2. ConsumeProductPriceService.java (15个错误 + 4个警告)
3. ConsumeProductQueryService.java (25个错误)
4. ConsumeProductServiceImpl_Refactored.java (2个错误)
5. ConsumeProductStockService.java (19个错误)
6. ConsumeProductValidationService.java (15个错误 + 2个警告)

### 修复优先级排序
1. **P0级 (阻塞编译)**: 异常构造函数、工具类导入、类型引用
2. **P1级 (功能问题)**: 字段映射、MyBatis-Plus使用
3. **P2级 (兼容性)**: BigDecimal过时API

## 预防措施

### 1. 编码规范强化
- 统一异常类使用模式
- 建立字段命名规范
- 加强代码审查

### 2. 自动化检查
- IDE模板配置
- 静态代码分析工具集成
- CI编译检查增强

### 3. 文档完善
- API使用示例文档
- 编码规范文档更新
- 新人培训材料

## 验证清单

修复完成后需要验证：
- [ ] 编译零错误
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 功能验证正常
- [ ] 性能测试通过