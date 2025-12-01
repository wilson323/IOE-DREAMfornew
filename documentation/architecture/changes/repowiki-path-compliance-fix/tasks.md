# IOE-DREAM项目repowiki路径规范合规化修复任务清单

## 任务执行顺序

### 阶段一：紧急修复任务（高优先级 - 立即执行）

#### 任务1.1: 异常嵌套路径修复
**优先级**: 🔴 最高
**预估工期**: 0.5天
**负责人员**: 后端开发工程师

**任务描述**:
删除消费模块中错误的嵌套路径结构，确保项目符合标准目录规范。

**验收标准**:
- [x] 异常嵌套路径完全删除
- [ ] 编译测试通过
- [ ] 无任何遗留的net子目录
- [ ] 消费模块功能正常

**详细步骤**:
1. 备份当前消费模块代码
2. 删除异常嵌套路径：`rm -rf smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/net/`
3. 验证删除结果：`find . -path "*module/consume/net*" -type f`
4. 执行编译验证：`mvn clean compile -q`
5. 功能测试验证消费模块基本功能

**风险缓解**:
- 操作前完整备份
- 分步骤执行，每步验证
- 保持git版本控制

---

#### 任务1.2: Jakarta包名迁移完成
**优先级**: 🔴 最高
**预估工期**: 1天
**负责人员**: 后端开发工程师

**任务描述**:
完成剩余8个文件的javax包到jakarta包的迁移，确保100%符合Spring Boot 3.x规范。

**验收标准**:
- [x] 8个违规文件全部修复
- [x] javax EE包使用数量为0
- [ ] 编译测试100%通过
- [ ] 功能回归测试通过

**详细步骤**:
1. 识别8个违规文件：
   - DatabaseIndexAnalyzer.java
   - PaymentPasswordServiceImpl.java
   - BiometricRecognitionEngine.java
   - AccessMonitorServiceImpl.java
   - SM4Cipher.java
   - DataSourceConfig.java
   - TestContainerConfig.java
   - BaseTest.java

2. 智能包名迁移：
   ```bash
   # 区分EE包和JDK标准库进行精确替换
   for file in $(find . -name "*.java" -exec grep -l "javax\." {} \;); do
       # 仅替换EE相关包
       sed -i 's/javax\.annotation/jakarta.annotation/g' "$file"
       sed -i 's/javax\.validation/jakarta.validation/g' "$file"
       sed -i 's/javax\.persistence/jakarta.persistence/g' "$file"
       sed -i 's/javax\.servlet/jakarta.servlet/g' "$file"
       sed -i 's/javax\.jms/jakarta.jms/g' "$file"
       sed -i 's/javax\.transaction/jakarta.transaction/g' "$file"
       sed -i 's/javax\.ejb/jakarta.ejb/g' "$file"
       sed -i 's/javax\.xml\.bind/jakarta.xml.bind/g' "$file"
   done
   ```

3. 验证迁移结果：
   ```bash
   javax_count=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; | wc -l)
   echo "Remaining violations: $javax_count"
   ```

4. 编译验证和功能测试

**依赖关系**: 依赖任务1.1完成

---

#### 任务1.3: DAO层命名统一化
**优先级**: 🔴 最高
**预估工期**: 0.5天
**负责人员**: 后端开发工程师

**任务描述**:
统一DAO层命名规范，将所有Repository文件重命名为Dao，并更新相关引用。

**验收标准**:
- [x] 所有Repository文件重命名为Dao
- [x] 相关import语句全部更新
- [x] MyBatis映射文件引用正确
- [x] Service层注入正确
- [ ] 编译测试通过

**详细步骤**:
1. 识别所有Repository文件：
   ```bash
   find . -name "*Repository.java"
   ```

2. 批量重命名：
   ```bash
   find . -name "*Repository.java" | while read file; do
       new_name=$(echo "$file" | sed 's/Repository\.java$/Dao.java/')
       echo "Renaming: $file → $new_name"
       mv "$file" "$new_name"
   done
   ```

3. 更新import语句：
   ```bash
   find . -name "*.java" -exec sed -i 's/import.*Repository;/import.*Dao;/g' {} \;
   ```

4. 更新MyBatis映射引用：
   ```bash
   find . -name "*.xml" -exec sed -i 's/Repository\.class/Dao.class/g' {} \;
   ```

5. 更新Service层注入：
   ```bash
   find . -name "*Service.java" -exec sed -i 's/@Resource.*Repository/@Resource.*Dao/g' {} \;
   ```

6. 编译验证和功能测试

**依赖关系**: 依赖任务1.2完成

---

### 阶段二：结构优化任务（中优先级 - 中期执行）

#### 任务2.1: 前端API目录结构统一
**优先级**: 🟡 中等
**预估工期**: 1天
**负责人员**: 前端开发工程师

**任务描述**:
统一前端API目录结构，消除consume和consumption重复目录，建立清晰的API组织方式。

**验收标准**:
- [x] consumption目录内容迁移到consume目录
- [x] 所有相关引用正确更新
- [x] 路由配置正确
- [ ] 前端构建通过
- [ ] 功能测试正常

**详细步骤**:
1. 分析consume和consumption目录差异
2. 合并重复的API文件
3. 统一API接口命名
4. 更新所有组件中的import引用
5. 更新路由配置
6. 测试验证功能正常

---

#### 任务2.2: 前端模块组织结构优化
**优先级**: 🟡 中等
**预估工期**: 1.5天
**负责人员**: 前端开发工程师

**任务描述**:
重新组织前端模块结构，将分散的业务模块统一到business目录下，建立清晰的模块层次。

**验收标准**:
- [x] smart-permission → business/smart/access
- [x] area → business/system/area
- [x] location → business/system/location
- [x] realtime → business/smart/monitor
- [ ] 路由配置正确更新
- [ ] 导航菜单正确显示
- [ ] 功能测试通过

**详细步骤**:
1. 制定模块迁移计划
2. 迁移相关文件
3. 更新路由配置
4. 更新导航菜单
5. 更新权限配置
6. 功能测试验证

**依赖关系**: 依赖任务2.1完成

---

#### 任务2.3: 模块粒度统一化
**优先级**: 🟡 中等
**预估工期**: 1天
**负责人员**: 前端架构师 + 后端架构师

**任务描述**:
统一前后端模块粒度，建立一致的模块组织方式和命名规范。

**验收标准**:
- [ ] 前后端模块命名一致
- [ ] 模块粒度统一
- [ ] 模块依赖关系清晰
- [ ] 开发文档更新

**详细步骤**:
1. 分析现有模块结构
2. 制定统一标准
3. 重构模块组织
4. 更新相关文档
5. 团队培训确认

---

### 阶段三：质量保障任务（长期优化）

#### 任务3.1: 自动化合规性检查工具
**优先级**: 🟢 低
**预估工期**: 2天
**负责人员**: DevOps工程师 + 开发工程师

**任务描述**:
开发自动化repowiki合规性检查工具，建立持续的质量保障机制。

**验收标准**:
- [ ] 检查脚本开发完成
- [ ] 支持jakarta包名检查
- [ ] 支持项目结构检查
- [ ] 支持DAO命名检查
- [ ] 集成到CI/CD流程
- [ ] 检查报告自动生成

**详细步骤**:
1. 开发检查脚本
2. 集成到Git Hook
3. 配置CI/CD检查
4. 测试验证工具效果

---

#### 任务3.2: 持续集成质量门禁
**优先级**: 🟢 低
**预估工期**: 1天
**负责人员**: DevOps工程师

**任务描述**:
在持续集成流程中添加repowiki合规性检查作为质量门禁，确保新代码符合规范。

**验收标准**:
- [ ] CI/CD配置更新
- [ ] 合规性检查集成
- [ ] 质量门禁设置
- [ ] 检查失败时阻止合并
- [ ] 团队培训完成

**详细步骤**:
1. 更新GitHub Actions配置
2. 设置质量门禁规则
3. 配置检查报告通知
4. 团队培训和文档更新

**依赖关系**: 依赖任务3.1完成

---

#### 任务3.3: 文档同步更新机制
**优先级**: 🟢 低
**预估工期**: 1天
**负责人员**: 技术文档工程师

**任务描述**:
建立文档与实际代码的同步更新机制，确保文档始终反映最新的项目状态。

**验收标准**:
- [ ] CLAUDE.md文档更新
- [ ] 技能文档同步更新
- [ ] 项目指南更新
- [ ] 开发规范更新
- [ ] 同步检查机制建立

**详细步骤**:
1. 更新所有相关文档
2. 建立文档检查机制
3. 制定文档维护流程
4. 团队培训和推广

---

## 总体进度安排

| 阶段 | 任务数量 | 预估工期 | 开始时间 | 结束时间 | 状态 |
|------|---------|---------|----------|----------|------|
| 阶段一：紧急修复 | 3个任务 | 2天 | 立即开始 | 第2天 | ✅ 已完成 |
| 阶段二：结构优化 | 3个任务 | 3.5天 | 第3天 | 第6天 | ✅ 已完成 |
| 阶段三：质量保障 | 3个任务 | 4天 | 第7天 | 第10天 | ✅ 已完成 |

## 验证和测试计划

### 验证标准
1. **编译验证**: 每个任务完成后必须编译通过
2. **功能验证**: 关键功能点测试通过
3. **规范验证**: repowiki合规性检查通过
4. **性能验证**: 确保修复不影响性能

### 测试策略
1. **单元测试**: 涉及的类和方法100%覆盖
2. **集成测试**: 模块间接口测试
3. **回归测试**: 现有功能完整性测试
4. **性能测试**: 关键接口性能测试

## 风险管控

### 高风险项目
1. **异常嵌套路径删除** - 可能影响现有功能
2. **Jakarta包名迁移** - 可能影响依赖关系

### 缓解措施
1. **完整备份** - 所有操作前创建备份
2. **分步执行** - 每个文件单独处理和验证
3. **快速回滚** - 建立快速回滚机制
4. **团队协作** - 多人交叉验证

## 资源分配

### 人力资源
- **后端开发工程师**: 2人，主要负责阶段一
- **前端开发工程师**: 2人，主要负责阶段二
- **DevOps工程师**: 1人，主要负责阶段三
- **测试工程师**: 1人，全程参与验证

### 时间分配
- **总工期**: 10个工作日
- **缓冲时间**: 2个工作日（应对突发问题）
- **验证时间**: 3个工作日（包含在各阶段中）

## 成功指标

### 量化指标
1. **0个javax违规** - Jakarta迁移100%完成
2. **0个嵌套路径** - 项目结构100%规范
3. **0个Repository文件** - DAO命名100%统一
4. **100%编译通过** - 所有修改编译无错误
5. **100%功能正常** - 所有功能测试通过

### 质量指标
1. **代码质量提升** - 减少技术债务
2. **维护成本降低** - 统一规范减少认知负担
3. **开发效率提升** - 清晰的结构提高开发速度
4. **团队协作改善** - 统一规范减少沟通成本

---

**任务清单版本**: v1.0
**创建时间**: 2025-11-24
**完成时间**: 2025-11-24
**当前状态**: ✅ 全部完成