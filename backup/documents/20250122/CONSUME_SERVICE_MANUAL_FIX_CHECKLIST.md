# 消费服务模块编译错误手动修复检查清单

## 🚨 重要原则：禁止自动修改代码

**核心原则**:
- ❌ **禁止使用脚本自动修改代码**
- ❌ **禁止使用正则表达式批量替换**
- ✅ **所有修复必须手动完成**
- ✅ **确保代码质量和全局一致性**

## 📋 修复检查清单

### 阶段一：异常类构造函数修复 (P0级)

#### 需要修复的文件和具体位置

**1. ConsumeProductImportExportService.java**
- [ ] 第64行: `new ConsumeProductException("产品不存在")` → `ConsumeProductException.notFound()`
- [ ] 第79行: `new ConsumeProductException("导入数据为空")` → `ConsumeProductException.invalidParameter()`
- [ ] 第93行: `new ConsumeProductException("产品编码已存在")` → `ConsumeProductException.duplicateCode()`
- [ ] 第98行: `new ConsumeProductException("产品名称已存在")` → `ConsumeProductException.duplicateName()`
- [ ] 第149行: `new ConsumeProductException("产品状态不正确")` → `ConsumeProductException.invalidParameter()`
- [ ] 第171行: `new ConsumeProductException("产品信息更新失败")` → `ConsumeProductException.operationNotSupported()`
- [ ] 第211行: `new ConsumeProductException("产品删除失败")` → `ConsumeProductException.usedInTransaction()`

**2. ConsumeProductPriceService.java**
- [ ] 第50行: `new ConsumeProductException("产品不存在")` → `ConsumeProductException.notFound()`
- [ ] 第53行: `new ConsumeProductException("产品价格无效")` → `ConsumeProductException.invalidPrice()`
- [ ] 第58行: `new ConsumeProductException("折扣比例无效")` → `ConsumeProductException.discountRateExceeded()`
- [ ] 第87行: `new ConsumeProductException("价格更新失败")` → `ConsumeProductException.priceViolation()`
- [ ] 第137行: `new ConsumeProductException("价格计算错误")` → `ConsumeProductException.invalidParameter()`
- [ ] 第151行: `new ConsumeProductException("价格设置无效")` → `ConsumeProductException.invalidPrice()`
- [ ] 第156行: `new ConsumeProductException("成本价格无效")` → `ConsumeProductException.invalidParameter()`
- [ ] 第159行: `new ConsumeProductException("基础价格无效")` → `ConsumeProductException.invalidPrice()`
- [ ] 第162行: `new ConsumeProductException("销售价格无效")` → `ConsumeProductException.invalidPrice()`
- [ ] 第167行: `new ConsumeProductException("最大折扣率无效")` → `ConsumeProductException.invalidParameter()`
- [ ] 第170行: `new ConsumeProductException("折扣设置无效")` → `ConsumeProductException.discountNotAllowed()`
- [ ] 第177行: `new ConsumeProductException("价格验证失败")` → `ConsumeProductException.priceViolation()`
- [ ] 第189行: `new ConsumeProductException("价格策略错误")` → `ConsumeProductException.businessRuleViolation()`
- [ ] 第226行: `new ConsumeProductException("价格同步失败")` → `ConsumeProductException.operationNotSupported()`
- [ ] 第283行: `new ConsumeProductException("价格历史不存在")` → `ConsumeProductException.notFound()`

**3. ConsumeProductStockService.java**
- [ ] 第49行: `new ConsumeProductException("产品不存在")` → `ConsumeProductException.notFound()`
- [ ] 第64行: `new ConsumeProductException("库存不足")` → `ConsumeProductException.insufficientStock()`
- [ ] 第112行: `new ConsumeProductException("库存更新失败")` → `ConsumeProductException.stockUpdateFailed()`
- [ ] 第124行: `new ConsumeProductException("库存减少失败")` → `ConsumeProductException.stockUpdateFailed()`
- [ ] 第129行: `new ConsumeProductException("库存增加失败")` → `ConsumeProductException.stockUpdateFailed()`
- [ ] 第141行: `new ConsumeProductException("库存检查失败")` → `ConsumeProductException.stockViolation()`
- [ ] 第153行: `new ConsumeProductException("库存重置失败")` → `ConsumeProductException.stockUpdateFailed()`
- [ ] 第158行: `new ConsumeProductException("库存清零失败")` → `ConsumeProductException.stockUpdateFailed()`
- [ ] 第176行: `new ConsumeProductException("库存同步失败")` → `ConsumeProductException.operationNotSupported()`
- [ ] 第188行: `new ConsumeProductException("库存验证失败")` → `ConsumeProductException.stockViolation()`
- [ ] 第205行: `new ConsumeProductException("库存调整失败")` → `ConsumeProductException.stockUpdateFailed()`
- [ ] 第251行: `new ConsumeProductException("库存预警失败")` → `ConsumeProductException.stockViolation()`
- [ ] 第301行: `new ConsumeProductException("库存状态错误")` → `ConsumeProductException.stockViolation()`

**4. ConsumeProductValidationService.java**
- [ ] 第47行: `new ConsumeProductException("产品验证失败")` → `ConsumeProductException.validationFailed()`
- [ ] 第65行: `new ConsumeProductException("产品基本信息无效")` → `ConsumeProductException.invalidParameter()`
- [ ] 第79行: `new ConsumeProductException("产品编码已存在")` → `ConsumeProductException.duplicateCode()`
- [ ] 第84行: `new ConsumeProductException("产品名称已存在")` → `ConsumeProductException.duplicateName()`
- [ ] 第117行: `new ConsumeProductException("产品编码无效")` → `ConsumeProductException.codeEmpty()`
- [ ] 第122行: `new ConsumeProductException("产品名称无效")` → `ConsumeProductException.nameEmpty()`
- [ ] 第148行: `new ConsumeProductException("产品价格设置错误")` → `ConsumeProductException.invalidPrice()`
- [ ] 第160行: `new ConsumeProductException("产品库存设置错误")` → `ConsumeProductException.stockViolation()`
- [ ] 第163行: `new ConsumeProductException("产品分类不存在")` → `ConsumeProductException.categoryNotFound()`
- [ ] 第168行: `new ConsumeProductException("产品类型错误")` → `ConsumeProductException.invalidProductType()`
- [ ] 第173行: `new ConsumeProductException("产品状态错误")` → `ConsumeProductException.invalidParameter()`
- [ ] 第180行: `new ConsumeProductException("产品折扣设置错误")` → `ConsumeProductException.discountNotAllowed()`
- [ ] 第185行: `new ConsumeProductException("产品折扣比例错误")` → `ConsumeProductException.discountRateExceeded()`
- [ ] 第191行: `new ConsumeProductException("产品时间段错误")` → `ConsumeProductException.invalidTimePeriod()`
- [ ] 第197行: `new ConsumeProductException("产品规格错误")` → `ConsumeProductException.invalidParameter()`
- [ ] 第208行: `new ConsumeProductException("产品验证失败")` → `ConsumeProductException.validationFailed()`

### 阶段二：工具类导入修复 (P0级)

#### 需要修复的文件

**1. ConsumeProductImportExportService.java**
- [ ] 删除第28行: `import net.lab1024.sa.consume.util;`
- [ ] 添加导入: `import org.springframework.beans.BeanUtils;`
- [ ] 添加导入: `import java.util.ArrayList;`

**2. ConsumeProductQueryService.java**
- [ ] 删除第22行: `import net.lab1024.sa.consume.util;`
- [ ] 添加导入: `import org.springframework.beans.BeanUtils;`
- [ ] 添加导入: `import java.util.ArrayList;`

### 阶段三：字段映射修复 (P1级)

#### 实体类字段映射

**需要修复的所有文件中的字段使用：**

- [ ] `entity.getStock()` → `entity.getStockQuantity()`
- [ ] `entity.setStock(value)` → `entity.setStockQuantity(value)`
- [ ] `entity.getProductSort()` → `entity.getRecommendSort()`
- [ ] `ConsumeProductEntity::getStock` → `ConsumeProductEntity::getStockQuantity`
- [ ] `ConsumeProductEntity::getProductSort` → `ConsumeProductEntity::getRecommendSort`

#### Form类字段映射

- [ ] `form.getMinStock()` → `form.getStockStatus()` 或新增相应字段
- [ ] `form.getOrderField()` → `form.getSortBy()`
- [ ] `form.getOrderDirection()` → `form.getSortDirection()` (已存在，保持不变)

### 阶段四：类型引用修复 (P0级)

**需要修复的所有文件：**

- [ ] `ConsumeAddForm` → `ConsumeProductAddForm`
- [ ] 检查其他可能的类型引用错误

### 阶段五：MyBatis-Plus使用修复 (P1级)

**需要修复的文件：**

**1. ConsumeProductImportExportService.java**
- [ ] 第60行: `selectPage(queryForm, lambdaWrapper)` → 创建Page对象并传递

**2. ConsumeProductQueryService.java**
- [ ] 第53行: `selectPage(queryForm, lambdaWrapper)` → 创建Page对象并传递

### 阶段六：BigDecimal过时API修复 (P2级)

**需要修复的文件：**

**1. ConsumeProductPriceService.java**
- [ ] 第83行: `BigDecimal.ROUND_HALF_UP` → `RoundingMode.HALF_UP`
- [ ] 第175行: `BigDecimal.ROUND_HALF_UP` → `RoundingMode.HALF_UP`
- [ ] 第270行: `BigDecimal.ROUND_HALF_UP` → `RoundingMode.HALF_UP`

**2. ConsumeProductValidationService.java**
- [ ] 第189行: `BigDecimal.ROUND_HALF_UP` → `RoundingMode.HALF_UP`

## 🔍 修复验证清单

### 修复前准备
- [ ] 备份原始文件到安全位置
- [ ] 理解每个错误的具体原因
- [ ] 查看相关的成功代码示例
- [ ] 准备必要的环境和工具

### 修复过程检查
- [ ] 逐个文件手动修复，不批量操作
- [ ] 保持代码格式和风格一致性
- [ ] 确保添加必要的导入语句
- [ ] 验证每个修复的逻辑正确性
- [ ] 保持代码注释的准确性

### 修复后验证
- [ ] 文件编译无错误
- [ ] 导入语句正确
- [ ] 方法调用正确
- [ ] 逻辑流程无误
- [ ] 代码风格一致

## 📊 修复进度跟踪

### 文件修复状态
- [ ] ConsumeProductImportExportService.java (0/8)
- [ ] ConsumeProductPriceService.java (0/15)
- [ ] ConsumeProductQueryService.java (0/25)
- [ ] ConsumeProductStockService.java (0/19)
- [ ] ConsumeProductValidationService.java (0/15)
- [ ] ConsumeProductServiceImpl_Refactored.java (0/2)

### 总体进度
- 异常构造函数修复: 0/61
- 工具类导入修复: 0/2
- 字段映射修复: 0/多个
- 类型引用修复: 0/多个
- MyBatis-Plus修复: 0/2
- BigDecimal修复: 0/4

**总进度**: 0/91 错误已修复

## 🛡️ 质量保证措施

### 代码审查要点
- [ ] 异常处理逻辑正确
- [ ] 字段映射准确无误
- [ ] 导入语句完整正确
- [ ] 代码风格符合规范
- [ ] 注释信息准确有效

### 测试验证要点
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 功能验证正常
- [ ] 性能测试通过
- [ ] 安全性验证通过

## 📝 修复记录模板

```
修复日期: ___________
修复人员: ___________
修复文件: ___________
修复内容:
- [ ] 异常构造函数修复: __个
- [ ] 工具类导入修复: __个
- [ ] 字段映射修复: __个
- [ ] 类型引用修复: __个
- [ ] MyBatis-Plus修复: __个
- [ ] BigDecimal修复: __个

验证结果:
- [ ] 编译通过
- [ ] 单元测试通过
- [ ] 功能验证正常

备注: __________________
```

---

**重要提醒**: 必须严格按照手动修复原则，确保每个修复都经过仔细思考和验证！