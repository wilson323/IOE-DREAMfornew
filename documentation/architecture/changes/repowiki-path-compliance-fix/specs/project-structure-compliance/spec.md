# Project Structure Compliance Specification

## ADDED Requirements

### Requirement: Automated Compliance Verification System
**Description**: 建立自动化repowiki合规性验证系统，确保项目持续符合规范要求

#### Scenario: Execute Automated Compliance Check
**Given**: repowiki合规性检查系统已部署

**When**: 执行自动化合规性检查

**Then**: 系统应该能够：
1. 检查jakarta包名合规性：
   ```bash
   javax_count=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; | wc -l)
   ```
   - 期望结果：javax_count = 0

2. 检查@Autowired使用情况：
   ```bash
   autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
   ```
   - 期望结果：autowired_count = 0

3. 检查项目结构合规性：
   ```bash
   nested_paths=$(find . -path "*module/*/net/*" -type f | wc -l)
   ```
   - 期望结果：nested_paths = 0

4. 检查DAO命名规范性：
   ```bash
   repository_count=$(find . -name "*Repository.java" | wc -l)
   ```
   - 期望结果：repository_count = 0

**And**: 生成详细的合规性报告，包括：
- 违规项目列表
- 合规性评分
- 修复建议
- 历史趋势分析

## ADDED Requirements

### Requirement: Continuous Compliance Monitoring
**Description**: 建立持续的repowiki合规性监控机制，确保项目长期保持规范符合性

#### Scenario: Monitor Compliance Trends
**Given**: 持续合规性监控系统已建立

**When**: 系统定期执行合规性检查

**Then**: 系统应该能够：
1. 记录合规性检查历史数据
2. 生成合规性趋势报告
3. 识别合规性风险点
4. 预警潜在的合规性问题
5. 提供改进建议

**And**: 监控结果包括：
- 合规性评分趋势
- 违规类型分布
- 修复进度跟踪
- 团队合规性表现

## MODIFIED Requirements

### Requirement: Remove Abnormal Nested Path Structure
**Description**: 消除项目中异常的嵌套路径结构，确保项目目录严格遵循repowiki规范

#### Scenario: Detect and Remove Nested Path Anomalies
**Given**: 项目中存在异常的嵌套路径结构
```text
异常路径: smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/net/lab1024/sa/admin/module/consume/domain/entity/
```

**When**: 执行项目结构清理操作

**Then**: 系统应该能够：
1. 识别所有异常的嵌套路径模式
2. 安全删除错误的嵌套目录结构
3. 验证删除操作不影响正常功能代码
4. 确保项目结构符合标准规范：
   ```text
   标准路径: smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/domain/entity/
   ```

**And**: 验证结果包括：
- 异常嵌套路径完全删除
- 编译测试通过
- 消费模块功能正常
- 无任何遗留的net子目录

#### Scenario: Validate Project Structure Compliance
**Given**: 项目结构修复完成后

**When**: 执行项目结构合规性验证

**Then**: 验证结果应该显示：
1. 0个异常嵌套路径
2. 100%符合repowiki模块路径规范
3. 所有模块路径格式正确：`net.lab1024.sa.admin.module.{module}`
4. 项目编译成功，无结构相关错误

---

### Requirement: Normalize DAO Layer Naming Convention
**Description**: 统一DAO层命名规范，消除Repository和Dao混用情况，确保命名一致性

#### Scenario: Identify and Rename Repository Files
**Given**: 项目中存在DAO层命名不统一的情况
```text
混用情况示例:
- AttendanceDao.java ✅ (符合规范)
- AttendanceExceptionRepository.java ❌ (不符合规范)
- AttendanceRecordDao.java ✅ (符合规范)
- AttendanceRecordRepository.java ❌ (不符合规范)
```

**When**: 执行DAO命名统一化操作

**Then**: 系统应该能够：
1. 识别所有使用Repository命名的DAO文件
2. 批量重命名为Dao命名规范
3. 更新所有相关的import语句
4. 更新MyBatis映射文件中的类引用
5. 更新Service层中的依赖注入引用

**And**: 验证结果包括：
- 0个Repository文件存在
- 所有DAO文件统一使用`*Dao.java`命名
- MyBatis映射正确引用
- Service层注入正确
- 编译测试通过

#### Scenario: Validate DAO Naming Consistency
**Given**: DAO命名统一化完成后

**When**: 执行DAO命名一致性验证

**Then**: 验证结果应该显示：
1. 100%的DAO文件使用统一命名规范
2. 0个Repository文件存在
3. 所有引用关系正确更新
4. MyBatis映射配置正确
5. 数据访问功能正常

---

### Requirement: Standardize Frontend Module Organization
**Description**: 统一前端模块组织结构，消除目录重复和模块分散问题，建立清晰的模块层次

#### Scenario: Consolidate Duplicate API Directories
**Given**: 前端项目中存在API目录重复问题
```text
当前问题:
src/api/business/consume/
src/api/business/consumption/
```

**When**: 执行API目录整合操作

**Then**: 系统应该能够：
1. 分析consume和consumption目录的差异
2. 合并重复的API文件
3. 统一API接口命名规范
4. 更新所有组件中的import引用
5. 更新相关的路由配置
6. 删除重复的consumption目录

**And**: 验证结果包括：
- consume目录包含所有必要的API文件
- consumption目录完全删除
- 所有组件引用正确更新
- 路由配置正确
- 前端构建成功

#### Scenario: Reorganize Frontend Module Structure
**Given**: 前端模块分散在多个位置
```text
当前分散情况:
- views/business/
- views/smart-permission/
- views/area/
- views/location/
- views/realtime/
```

**When**: 执行模块结构重组操作

**Then**: 系统应该能够：
1. 制定统一的模块组织标准
2. 按照业务类型重新组织模块：
   ```text
   标准结构:
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
3. 迁移相关文件到正确位置
4. 更新路由配置
5. 更新导航菜单结构
6. 更新权限配置

**And**: 验证结果包括：
- 模块按照统一标准组织
- 业务模块集中在business目录下
- 系统模块集中在system目录下
- 智能设备模块集中在smart目录下
- 路由配置正确
- 导航菜单正确显示
- 所有功能正常访问

#### Scenario: Unify Frontend-Backend Module Granularity
**Given**: 前后端模块粒度不一致的情况
```text
不一致情况:
- 大模块：consume、system
- 小模块：hr、oa
- 混合模块：smart/*下的多个子模块
```

**When**: 执行模块粒度统一化操作

**Then**: 系统应该能够：
1. 分析前后端模块对应关系
2. 制定统一的模块粒度标准
3. 调整前端模块组织以匹配后端结构
4. 确保前后端模块命名一致性
5. 更新相关文档和配置

**And**: 验证结果包括：
- 前后端模块命名一致
- 模块粒度统一标准
- 模块依赖关系清晰
- 开发文档准确反映实际结构

---

## ADDED Requirements

### Requirement: Automated Compliance Verification System
**Description**: 建立自动化repowiki合规性验证系统，确保项目持续符合规范要求

#### Scenario: Execute Automated Compliance Check
**Given**: repowiki合规性检查系统已部署

**When**: 执行自动化合规性检查

**Then**: 系统应该能够：
1. 检查jakarta包名合规性：
   ```bash
   javax_count=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; | wc -l)
   ```
   - 期望结果：javax_count = 0

2. 检查@Autowired使用情况：
   ```bash
   autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
   ```
   - 期望结果：autowired_count = 0

3. 检查项目结构合规性：
   ```bash
   nested_paths=$(find . -path "*module/*/net/*" -type f | wc -l)
   ```
   - 期望结果：nested_paths = 0

4. 检查DAO命名规范性：
   ```bash
   repository_count=$(find . -name "*Repository.java" | wc -l)
   ```
   - 期望结果：repository_count = 0

**And**: 生成详细的合规性报告，包括：
- 违规项目列表
- 合规性评分
- 修复建议
- 历史趋势分析

#### Scenario: Integrate Compliance Check into Development Workflow
**Given**: 开发工作流程需要集成合规性检查

**When**: 开发人员进行代码提交

**Then**: 系统应该能够：
1. 自动触发合规性检查
2. 阻止不合规代码合并
3. 提供详细的违规信息
4. 建议修复方案
5. 记录检查历史

**And**: 开发体验包括：
- 实时的合规性反馈
- 清晰的违规提示
- 自动化的修复建议
- 无缝的开发流程集成

---

### Requirement: Continuous Compliance Monitoring
**Description**: 建立持续的repowiki合规性监控机制，确保项目长期保持规范符合性

#### Scenario: Monitor Compliance Trends
**Given**: 持续合规性监控系统已建立

**When**: 系统定期执行合规性检查

**Then**: 系统应该能够：
1. 记录合规性检查历史数据
2. 生成合规性趋势报告
3. 识别合规性风险点
4. 预警潜在的合规性问题
5. 提供改进建议

**And**: 监控结果包括：
- 合规性评分趋势
- 违规类型分布
- 修复进度跟踪
- 团队合规性表现

#### Scenario: Generate Compliance Reports
**Given**: 需要生成项目合规性报告

**When**: 执行合规性报告生成

**Then**: 系统应该能够生成包含以下内容的报告：
1. **总体合规性评分**
2. **详细违规清单**
3. **修复进度跟踪**
4. **历史趋势分析**
5. **改进建议**

**And**: 报告格式支持：
- HTML格式用于浏览器查看
- JSON格式用于API集成
- PDF格式用于文档归档
- 数据格式用于进一步分析

---

## Cross-Reference Relationships

- **project-structure-compliance** → **code-quality-compliance**: 项目结构修复为代码质量提升提供基础
- **frontend-module-organization** → **backend-module-alignment**: 前端模块重组需要与后端模块结构保持一致
- **automated-compliance-verification** → **continuous-compliance-monitoring**: 自动化检查为持续监控提供数据支持

---

## Success Metrics

### Quantitative Metrics
- **0个异常嵌套路径**: 100%消除项目结构异常
- **0个javax违规**: 100%完成Jakarta迁移
- **0个Repository文件**: 100%统一DAO命名
- **100%编译通过**: 所有修改编译无错误
- **95%+合规性评分**: 自动化检查评分达标

### Qualitative Metrics
- **代码维护性提升**: 统一规范减少认知负担
- **开发效率改善**: 清晰结构提高开发速度
- **团队协作增强**: 统一标准减少沟通成本
- **技术债务减少**: 规范化提升代码质量

---

**Specification Version**: v1.0
**Last Updated**: 2025-11-24
**Status**: Ready for Implementation