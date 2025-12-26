# 架构合规性修复规格

## 变更描述
修复IOE-DREAM项目中的架构违规问题，包括依赖注入规范、分层架构边界、Service接口设计等方面，确保代码完全符合企业级架构标准。

## 变更理由
当前项目存在大量架构违规问题：16个@Autowired违规、11个@Repository违规、100+个Service接口ResponseDTO违规、50+个四层架构边界违规，严重影响代码质量和可维护性。

## 规格定义

### ADDED Requirements

#### R1: 依赖注入规范统一功能
**描述**: 系统必须能够检测和修复依赖注入规范违规，确保全局统一使用@Resource注解。

**Scenarios**:
- **Scenario 1.1**: @Autowired违规检测
  **Given**: 项目中的Java源代码文件
  **When**: 扫描依赖注入注解使用
  **Then**: 系统应该识别所有使用@Autowired注解的位置
  **And**: 生成包含文件路径、行号、类名、字段名的详细违规清单
  **And**: 统计违规总数和分布情况

- **Scenario 1.2**: @Autowired批量修复
  **Given**: 包含@Autowired违规的源代码文件
  **When**: 执行批量修复操作
  **Then**: 系统应该将所有@Autowired注解替换为@Resource注解
  **And**: 保持import语句的正确性
  **And**: 验证修复后的依赖注入正确性

- **Scenario 1.3**: Repository违规检测
  **Given**: 项目中的DAO接口文件
  **When**: 扫描Repository注解使用
  **Then**: 系统应该识别所有使用@Repository注解的DAO接口
  **And**: 验证MyBatis-Plus框架兼容性要求
  **And**: 生成违规清单和处理建议

- **Scenario 1.4**: Repository批量修复
  **Given**: 包含@Repository违规的DAO接口
  **When**: 执行批量修复操作
  **Then**: 系统应该将所有@Repository注解替换为@Mapper注解
  **And**: 更新相关的MyBatis扫描配置
  **And**: 验证DAO接口功能正常

#### R2: Service接口架构规范功能
**描述**: 系统必须修复Service接口和实现类中的架构违规，移除ResponseDTO包装，恢复纯粹的分层职责。

**Scenarios**:
- **Scenario 2.1**: Service接口ResponseDTO检测
  **Given**: 项目中的Service接口和实现类
  **When**: 扫描方法返回类型
  **Then**: 系统应该识别所有使用ResponseDTO包装的接口方法
  **And**: 分析包装类型和泛型参数
  **And**: 生成接口违规清单和修复建议

- **Scenario 2.2**: Service接口类型修复
  **Given**: 包含ResponseDTO违规的Service接口
  **When**: 执行接口类型修复
  **Then**: 系统应该移除ResponseDTO包装，保留核心业务类型
  **And**: 更新方法签名定义
  **And**: 保持接口语义的完整性

- **Scenario 2.3**: Service实现类修复
  **Given**: Service实现类中的ResponseDTO使用
  **When**: 执行实现类修复
  **Then**: 系统应该更新方法返回类型
  **And**: 移除ResponseDTO包装逻辑
  **And**: 保持异常处理机制不变

- **Scenario 2.4**: Controller层适配
  **Given**: 修复后的Service接口和实现类
  **When**: 更新Controller层调用
  **Then**: 系统应该添加ResponseDTO包装逻辑
  **And**: 统一异常处理机制
  **And**: 保持API响应格式一致性

#### R3: 四层架构边界控制功能
**描述**: 系统必须检测和修复四层架构边界违规，确保严格的分层调用关系。

**Scenarios**:
- **Scenario 3.1**: 跨层访问检测
  **Given**: 项目中的Controller类
  **When**: 扫描依赖注入字段
  **Then**: 系统应该识别Controller直接注入DAO的违规
  **And**: 识别Controller直接注入Manager的违规
  **And**: 分析跨层访问的类型和影响范围

- **Scenario 3.2**: 缺失Service层修复
  **Given**: Controller直接调用DAO或Manager的场景
  **When**: 执行架构边界修复
  **Then**: 系统应该创建缺失的Service接口
  **And**: 实现对应的Service实现类
  **And**: 将跨层调用逻辑移动到Service层

- **Scenario 3.3**: 调用路径重构
  **Given**: 存在跨层访问的代码
  **When**: 重构调用路径
  **Then**: 系统应该建立Controller→Service调用关系
  **And**: 在Service中实现DAO/Manager调用
  **And**: 保持原有功能逻辑不变

- **Scenario 3.4**: 架构合规验证
  **Given**: 重构后的代码结构
  **When**: 执行架构合规检查
  **Then**: 系统应该验证无跨层访问违规
  **And**: 确认分层依赖关系正确
  **And**: 生成架构合规性报告

#### R4: Manager类注解规范功能
**描述**: 系统必须修复Manager类的Spring注解违规，实现纯Java类设计模式。

**Scenarios**:
- **Scenario 4.1**: Manager类Spring注解检测
  **Given**: 项目中的Manager类文件
  **When**: 扫描Spring注解使用
  **Then**: 系统应该识别使用@Component、@Service等注解的Manager类
  **And**: 分析依赖注入方式是否正确
  **And**: 生成注解违规清单

- **Scenario 4.2**: Manager类纯Java化
  **Given**: 包含Spring注解的Manager类
  **When**: 执行注解清理
  **Then**: 系统应该移除所有Spring注解
  **And**: 将依赖注入改为构造函数注入
  **And**: 保持类为纯Java类

- **Scenario 4.3**: 配置类Bean注册
  **Given**: 纯Java化的Manager类
  **When**: 在配置类中注册Bean
  **Then**: 系统应该创建对应的@Bean方法
  **And**: 使用构造函数注入Manager依赖
  **And**: 使用@ConditionalOnMissingBean避免重复注册

- **Scenario 4.4**: Service层使用验证
  **Given**: 注册为Spring Bean的Manager类
  **When**: 在Service层中使用
  **Then**: 系统应该能够通过@Resource正常注入
  **And**: 验证依赖注入正确性
  **And**: 确认功能调用正常

#### R5: 日志规范统一功能
**描述**: 系统必须统一日志记录规范，使用@Slf4j注解和标准化日志模板。

**Scenarios**:
- **Scenario 5.1**: 日志记录方式检测
  **Given**: 项目中的Java类文件
  **When**: 扫描日志记录代码
  **Then**: 系统应该识别使用LoggerFactory的代码
  **And**: 识别System.out.println等不规范的日志方式
  **And**: 生成日志违规清单

- **Scenario 5.2**: @Slf4j注解统一
  **Given**: 使用LoggerFactory的类
  **When**: 执行日志注解统一
  **Then**: 系统应该添加@Slf4j注解
  **And**: 移除LoggerFactory相关代码
  **And**: 更新import语句

- **Scenario 5.3**: 日志格式标准化
  **Given**: 项目中的日志记录代码
  **When**: 应用标准化日志模板
  **Then**: 系统应该使用统一的日志格式：[模块名] 操作描述: 参数={}
  **And**: 使用参数化日志消息
  **And**: 添加模块名称标识

- **Scenario 5.4**: 敏感信息过滤
  **Given**: 日志记录中可能包含敏感信息
  **When**: 执行敏感信息检查
  **Then**: 系统应该识别密码、token等敏感信息
  **And**: 应用脱敏处理逻辑
  **And**: 确保日志符合安全规范

### MODIFIED Requirements

### M1: 分层架构严格性要求
**原有要求**: 遵循基本的分层架构原则
**修改后**: 严格执行四层架构边界，禁止任何形式的跨层访问，包括间接调用和反射调用。

### M2: 依赖注入注解选择
**原有要求**: 优先使用@Resource注解
**修改后**: 强制统一使用@Resource注解，完全禁用@Autowired注解，特殊情况需要架构师审批。

## REMOVED Requirements

### R1: @Configuration注解使用
**原有要求**: Manager类可以使用@Configuration注解
**移除原因**: Manager类应该保持为纯Java类，配置类应独立管理。

## 验收标准

### 功能验收
- [x] 零个@Autowired违规实例
- [x] 零个@Repository违规实例
- [x] 100%Service接口符合架构规范
- [x] 零个四层架构边界违规
- [x] 100%日志符合企业级标准
- [x] 100%Manager类符合规范

### 质量验收
- [x] SonarQube架构合规性评分≥95%
- [x] 代码可维护性评分≥85%
- [x] 代码可读性评分≥90%
- [x] 分层耦合度评分≥90%

### 性能验收
- [x] 修复后系统性能不降级
- [x] 启动时间增加不超过10%
- [x] 内存使用增加不超过5%
- [x] API响应时间影响不超过5%

## 相关能力引用

- `代码扫描分析能力` - 用于识别架构违规
- `自动化代码修复能力` - 用于批量修复违规
- `分层架构验证能力` - 用于架构合规性检查
- `代码质量评估能力` - 用于质量门禁验证

---

**规格版本**: v1.0
**创建日期**: 2025-12-22
**规格负责人**: 架构重构专项组