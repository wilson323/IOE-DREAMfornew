# SmartAdmin四层架构开发规范指南

## 🚨 核心原则：先验证再编码

### 第一层：Entity验证层（必须最先执行）

#### 1.1 Entity字段验证清单
```bash
# 开发任何业务逻辑前，必须先执行：
echo "🔍 步骤1：验证Entity字段定义"
find . -name "*Entity.java" -path "*/domain/entity/*" | head -5

# 对每个Entity执行验证：
echo "检查字段名和Getter/Setter方法"
grep -n "private.*;" src/main/java/net/lab1024/sa/base/common/domain/entity/VideoDeviceEntity.java
```

#### 1.2 Entity验证标准流程
```java
// ❌ 错误方式：假设字段名存在
device.getVideoDeviceId(); // 可能编译错误

// ✅ 正确方式：先验证Entity类
// 1. 读取Entity类确认字段名
// 2. 使用实际存在的getter方法
device.getDeviceId(); // 正确
```

### 第二层：DAO接口验证层

#### 2.1 DAO方法验证清单
```bash
# 验证DAO接口中是否存在目标方法
echo "🔍 步骤2：验证DAO方法定义"
grep -n "interface.*Dao" src/main/java/net/lab1024/sa/admin/module/*/dao/*Dao.java

# 检查具体方法是否存在
grep -n "selectByDeviceId\|pageQuery" src/main/java/net/lab1024/sa/admin/module/*/dao/*Dao.java
```

#### 2.2 DAO使用标准流程
```java
// ❌ 错误方式：假设DAO方法存在
videoDeviceDao.pageQuery(entity, queryForm); // 可能不存在

// ✅ 正确方式：先验证DAO接口
// 1. 读取DAO接口确认方法签名
// 2. 使用实际存在的方法
videoDeviceDao.selectPage(pageParam, condition); // 正确
```

### 第三层：导入路径验证层

#### 3.1 导入路径标准规范
```java
// SmartAdmin标准导入路径模式：
// Entity: net.lab1024.sa.base.common.domain.entity.*
// VO: net.lab1024.sa.admin.module.{module}.domain.vo.*
// Form: net.lab1024.sa.admin.module.{module}.domain.form.*
// DAO: net.lab1024.sa.admin.module.{module}.dao.*
// Service: net.lab1024.sa.admin.module.{module}.service.*
// Manager: net.lab1024.sa.admin.module.{module}.manager.*
// Controller: net.lab1024.sa.admin.module.{module}.controller.*
```

#### 3.2 导入路径验证脚本
```bash
#!/bin/bash
# import-path-validator.sh

echo "🔍 验证导入路径规范性"

# 检查Entity导入
entity_import_pattern="net\.lab1024\.sa\.base\.common\.domain\.entity"
find . -name "*.java" -exec grep -l "import.*entity" {} \; | xargs grep -H "net\.lab1024\.sa\.base\.common\.entity" | head -5

# 如果发现错误导入路径，立即修复
find . -name "*.java" -exec sed -i 's/net\.lab1024\.sa\.base\.common\.entity\./net.lab1024.sa.base.common.domain.entity./g' {} \;
```

### 第四层：工具类使用验证层

#### 4.1 SmartBeanUtil使用规范
```java
// ❌ 错误方式：参数类型错误
SmartBeanUtil.copy(updateForm, VideoDeviceEntity.class); // 编译错误

// ✅ 正确方式：检查源码确认方法签名
// 读取SmartBeanUtil源码确认copy方法签名
SmartBeanUtil.copy(updateForm, existingDevice); // 正确：对象到对象
```

#### 4.2 工具类验证流程
```bash
# 验证SmartBeanUtil方法签名
echo "🔍 检查SmartBeanUtil方法"
find . -name "SmartBeanUtil.java" -exec grep -A 5 "public.*copy" {} \;
```

## 🛠️ 开发前强制检查清单

### Phase 1：Entity层验证（必须100%通过）

```bash
#!/bin/bash
# entity-validator.sh - 必须在编码前执行

echo "🔍 Phase 1：Entity层验证"

# 1.1 验证Entity存在性
entity_files=$(find . -name "*Entity.java" -path "*/domain/entity/*")
if [ -z "$entity_files" ]; then
    echo "❌ 未找到Entity文件"
    exit 1
fi

# 1.2 验证Entity字段完整性
for entity_file in $entity_files; do
    echo "检查Entity: $entity_file"

    # 检查是否有private字段
    field_count=$(grep -c "private.*;" "$entity_file")
    if [ $field_count -eq 0 ]; then
        echo "❌ Entity缺少字段定义: $entity_file"
        exit 1
    fi

    # 检查是否继承BaseEntity
    if ! grep -q "extends BaseEntity" "$entity_file"; then
        echo "❌ Entity未继承BaseEntity: $entity_file"
        exit 1
    fi

    # 检查Lombok注解
    if ! grep -q "@Data" "$entity_file"; then
        echo "❌ Entity缺少@Data注解: $entity_file"
        exit 1
    fi
done

echo "✅ Entity层验证通过"
```

### Phase 2：DAO层验证

```bash
#!/bin/bash
# dao-validator.sh

echo "🔍 Phase 2：DAO层验证"

# 2.1 验证DAO接口存在性
dao_files=$(find . -name "*Dao.java" -path "*/dao/*")
if [ -z "$dao_files" ]; then
    echo "❌ 未找到DAO文件"
    exit 1
fi

# 2.2 验证DAO继承BaseMapper
for dao_file in $dao_files; do
    if ! grep -q "extends BaseMapper" "$dao_file"; then
        echo "❌ DAO未继承BaseMapper: $dao_file"
        exit 1
    fi

    if ! grep -q "@Mapper" "$dao_file"; then
        echo "❌ DAO缺少@Mapper注解: $dao_file"
        exit 1
    fi
done

echo "✅ DAO层验证通过"
```

### Phase 3：编译验证

```bash
#!/bin/bash
# compile-validator.sh

echo "🔍 Phase 3：编译验证"

# 3.1 检查javax包名使用（必须为0）
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现javax包使用: $javax_count 个文件"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi

# 3.2 检查@Autowired使用（必须为0）
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现@Autowired使用: $autowired_count 个文件"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi

echo "✅ 编译规范验证通过"
```

## 🚨 强制执行流程

### 开发新功能的执行顺序

```bash
# 步骤1：Entity验证（必须最先执行）
./scripts/entity-validator.sh

# 步骤2：DAO验证
./scripts/dao-validator.sh

# 步骤3：编译规范验证
./scripts/compile-validator.sh

# 步骤4：尝试编译
mvn clean compile -DskipTests -q

# 只有以上全部通过，才能开始写业务逻辑代码
```

### 验证脚本使用示例

```bash
# 完整验证流程
echo "🔍 开始SmartAdmin四层架构验证"

# Phase 1: Entity验证
./scripts/entity-validator.sh || exit 1

# Phase 2: DAO验证
./scripts/dao-validator.sh || exit 1

# Phase 3: 导入路径验证
./scripts/import-path-validator.sh || exit 1

# Phase 4: 编译验证
mvn clean compile -DskipTests -q || {
    echo "❌ 编译失败，检查错误信息"
    exit 1
}

echo "🎉 所有验证通过，可以开始业务逻辑开发"
```

## 📋 常见错误及解决方案

### 错误1：Entity字段名错误
```java
// ❌ 错误：假设字段名
device.getVideoDeviceId(); // 编译错误

// ✅ 解决：先读取Entity确认字段名
// 1. 读取VideoDeviceEntity.java确认字段是deviceId
device.getDeviceId(); // 正确
```

### 错误2：DAO方法不存在
```java
// ❌ 错误：假设DAO方法
videoDeviceDao.pageQuery(entity, form); // 方法不存在

// ✅ 解决：先检查DAO接口
// 1. 读取VideoDeviceDao.java确认可用方法
videoDeviceDao.selectPage(pageParam, condition); // 正确
```

### 错误3：导入路径错误
```java
// ❌ 错误：导入路径
import net.lab1024.sa.base.common.entity.VideoDeviceEntity;

// ✅ 解决：使用正确路径
import net.lab1024.sa.base.common.domain.entity.VideoDeviceEntity;
```

## 🎯 核心准则

1. **先验证后编码** - 永远不要假设Entity字段或DAO方法存在
2. **逐层验证** - Entity → DAO → Service → Manager → Controller
3. **编译驱动** - 每写完一层立即编译验证
4. **规范优先** - 严格遵守SmartAdmin框架规范
5. **工具辅助** - 使用验证脚本避免人为错误

**记住：在SmartAdmin框架中，Entity的字段名和DAO的方法名是固定的，不能猜测或假设！**