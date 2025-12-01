# IOE-DREAM项目repowiki路径规范合规化修复设计文档

## 架构设计原则

本修复设计基于以下核心原则：

1. **严格遵循repowiki规范** - 确保100%符合`.qoder/repowiki`要求
2. **最小化影响** - 降低对现有功能的干扰
3. **向后兼容** - 保持API接口稳定性
4. **渐进式改进** - 分阶段实施，每阶段可验证

## 技术架构设计

### 1. 项目结构标准化

#### 当前问题分析
```
❌ 异常嵌套路径
smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/net/lab1024/sa/admin/module/consume/domain/entity/

✅ 标准结构
smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/
```

#### 设计方案
- **删除异常嵌套**: 完全移除错误的嵌套路径结构
- **验证完整性**: 确保删除操作不影响功能代码
- **编译验证**: 删除后立即验证编译通过

### 2. Jakarta包名迁移架构

#### 违规文件分析
```java
// 需要迁移的javax包使用
javax.annotation.*        → jakarta.annotation.*
javax.validation.*        → jakarta.validation.*
javax.persistence.*        → jakarta.persistence.*
javax.servlet.*            → jakarta.servlet.*
javax.crypto.*             → 保留（JDK标准库）
javax.sql.*               → 保留（JDK标准库）
```

#### 迁移策略
- **智能识别**: 区分EE包和JDK标准库
- **批量替换**: 自动化javax到jakarta转换
- **白名单机制**: 保护JDK标准库包不被错误替换
- **验证检查**: 确保迁移后编译通过

### 3. DAO层命名统一设计

#### 当前不一致情况
```
混用命名方式：
- AttendanceDao.java ✅
- AttendanceExceptionRepository.java ❌
- AttendanceRecordDao.java ✅
- AttendanceRecordRepository.java ❌
```

#### 统一标准
- **统一命名**: 全部使用`*Dao.java`命名
- **引用更新**: 更新所有相关的import语句
- **注解适配**: 确保MyBatis注解正确映射
- **测试覆盖**: 验证重命名后的功能完整性

### 4. 前端架构优化设计

#### API目录结构重组
```
当前问题：
src/api/business/consume/
src/api/business/consumption/

优化方案：
src/api/business/consume/    # 统一使用consume
├── account.js
├── device.js
├── record.js
└── report.js
```

#### 模块组织结构
```
标准模块结构：
src/views/
├── system/              # 系统管理模块
├── business/            # 业务功能模块
│   ├── consume/         # 消费系统
│   ├── attendance/      # 考勤系统
│   ├── access/          # 门禁系统
│   └── smart/           # 智能设备
├── support/             # 支撑功能
└── common/              # 通用功能
```

## 实施架构

### 阶段一：紧急修复（高优先级）

#### 1.1 异常嵌套路径修复
```bash
# 删除异常路径
rm -rf smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/net/

# 验证删除结果
find smart-admin-api-java17-springboot3 -path "*module/consume/net*" -type f
# 应该返回空结果
```

#### 1.2 Jakarta包名迁移
```bash
# 智能迁移脚本
#!/bin/bash
# 区分EE包和JDK标准库
EE_PACKAGES=(
    "javax\.annotation"
    "javax\.validation"
    "javax\.persistence"
    "javax\.servlet"
    "javax\.jms"
    "javax\.transaction"
    "javax\.ejb"
    "javax\.xml\.bind"
)

for package in "${EE_PACKAGES[@]}"; do
    find . -name "*.java" -exec sed -i "s/$package/jakarta\./g" {} \;
done

# 验证迁移结果
javax_count=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet|jms|transaction|ejb)" {} \; | wc -l)
echo "Remaining javax violations: $javax_count"
```

#### 1.3 DAO命名统一
```bash
# 批量重命名脚本
find . -name "*Repository.java" | while read file; do
    new_name=$(echo "$file" | sed 's/Repository\.java$/Dao.java/')
    mv "$file" "$new_name"
done

# 更新import语句
find . -name "*.java" -exec sed -i 's/import.*Repository;/import.*Dao;/g' {} \;
```

### 阶段二：结构优化（中优先级）

#### 2.1 前端API目录重组
```javascript
// 目录迁移计划
src/api/business/consumption/* → src/api/business/consume/

// 引用更新
// 将所有consumption相关引用更新为consume
```

#### 2.2 模块结构重组
- **smart-permission** → **business/smart/access**
- **area** → **business/system/area**
- **location** → **business/system/location**
- **realtime** → **business/smart/monitor**

### 阶段三：质量保障（长期优化）

#### 3.1 自动化检查机制
```bash
#!/bin/bash
# repowiki合规性检查脚本
check_repowiki_compliance() {
    # 1. jakarta包名检查
    javax_count=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence)" {} \; | wc -l)

    # 2. @Autowired检查
    autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)

    # 3. 项目结构检查
    nested_paths=$(find . -path "*module/*/net/*" -type f | wc -l)

    # 4. DAO命名检查
    repository_count=$(find . -name "*Repository.java" | wc -l)

    if [ $javax_count -eq 0 ] && [ $autowired_count -eq 0 ] && [ $nested_paths -eq 0 ] && [ $repository_count -eq 0 ]; then
        echo "✅ repowiki合规性检查通过"
        return 0
    else
        echo "❌ 发现不合规问题"
        echo "javax违规: $javax_count"
        echo "@Autowired违规: $autowired_count"
        echo "嵌套路径: $nested_paths"
        echo "Repository文件: $repository_count"
        return 1
    fi
}
```

#### 3.2 持续集成集成
```yaml
# .github/workflows/repowiki-compliance.yml
name: repowiki Compliance Check
on: [push, pull_request]

jobs:
  compliance:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Java 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run repowiki Compliance Check
        run: ./scripts/check-repowiki-compliance.sh
```

## 数据流设计

### 修复前数据流
```
代码编写 → 编译错误 → 手动修复 → 重复验证
```

### 修复后数据流
```
代码编写 → 自动化检查 → 规范提示 → 自动修复 → 验证通过
```

## 错误处理机制

### 1. 回滚策略
- **版本控制**: 每个阶段创建独立分支
- **备份机制**: 重要操作前自动备份
- **快速回滚**: 出现问题时可快速恢复

### 2. 验证机制
- **编译验证**: 每步操作后验证编译通过
- **功能验证**: 关键功能点测试验证
- **性能验证**: 确保修复不影响性能

### 3. 监控机制
- **实时监控**: 操作过程实时记录
- **异常告警**: 异常情况及时通知
- **进度跟踪**: 修复进度可视化展示

## 技术选型说明

### 为什么选择这个设计方案

1. **基于现有基础**: 充分利用现有项目结构
2. **风险可控**: 分阶段实施降低风险
3. **效果可测**: 每个阶段都有明确的验证标准
4. **长期价值**: 建立可持续的质量保障机制

### 技术优势

1. **自动化程度高**: 减少人工操作错误
2. **覆盖面广**: 涵盖所有关键的repowiki规范
3. **可扩展性**: 易于添加新的检查规则
4. **维护性好**: 建立持续的合规性保障

---

**设计版本**: v1.0
**最后更新**: 2025-11-24
**审核状态**: 待审核