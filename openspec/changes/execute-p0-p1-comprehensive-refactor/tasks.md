# P0/P1阶段全局重构执行任务清单

## 阶段一: P0级编译错误清零（3-5天）

### 1.1 全局BOM字符清理（1-2天）
- [ ] **任务1.1.1**: 扫描所有Java文件识别BOM字符
  - 执行PowerShell脚本扫描microservices目录
  - 生成BOM字符文件清单
  - 验证扫描结果准确性
  - 输出: `bom-files-report.txt`

- [ ] **任务1.1.2**: 备份原始文件
  - 创建备份目录结构
  - 复制所有包含BOM的文件到备份目录
  - 验证备份完整性
  - 输出: `backup-integrity-report.txt`

- [ ] **任务1.1.3**: 批量清理BOM字符
  - 使用PowerShell脚本批量处理
  - 验证UTF-8无BOM格式
  - 检查文件内容完整性
  - 输出: `bom-cleanup-results.txt`

- [ ] **任务1.1.4**: BOM清理验证
  - 重新扫描确认无BOM字符
  - 随机抽查10个文件验证编码
  - IDE编码设置验证
  - 输出: `bom-verification-report.txt`

### 1.2 Maven依赖架构修复（1天）
- [ ] **任务1.2.1**: 分析当前依赖违规
  - 检查12个服务的pom.xml文件
  - 识别microservices-common聚合依赖违规
  - 生成依赖违规报告
  - 输出: `maven-violations-report.txt`

- [ ] **任务1.2.2**: 修复device-comm-service依赖
  - 移除microservices-common聚合依赖
  - 添加所需的细粒度模块依赖
  - 验证依赖解析成功
  - 输出: `device-comm-dependencies-fixed.xml`

- [ ] **任务1.2.3**: 修复其他10个服务依赖
  - 逐一修复每个服务的pom.xml
  - 遵循细粒度依赖原则
  - 确保版本一致性
  - 输出: 10个修复后的pom.xml文件

- [ ] **任务1.2.4**: 依赖架构验证
  - 验证无循环依赖
  - 检查依赖传递关系
  - 运行依赖树分析
  - 输出: `dependency-architecture-report.txt`

### 1.3 基础编译验证（1天）
- [ ] **任务1.3.1**: 构建公共模块
  - 编译microservices-common-core
  - 安装到本地Maven仓库
  - 验证JAR文件正确性
  - 输出: `common-modules-build.log`

- [ ] **任务1.3.2**: 编译网关服务
  - 编译ioedream-gateway-service
  - 验证依赖解析成功
  - 检查启动类配置
  - 输出: `gateway-service-build.log`

- [ ] **任务1.3.3**: 批量编译业务服务
  - 并行编译access-service
  - 并行编译attendance-service
  - 并行编译consume-service
  - 并行编译video-service
  - 输出: `business-services-build.log`

- [ ] **任务1.3.4**: 编译结果验证
  - 统计编译成功/失败数量
  - 分析编译错误根因
  - 生成编译报告
  - 输出: `compilation-final-report.txt`

## 阶段二: P1级架构规范统一（1-2周）

### 2.1 依赖注入规范统一（2-3天）
- [ ] **任务2.1.1**: 扫描@Autowired违规
  - 全局搜索@Autowired注解使用
  - 生成违规实例清单
  - 分类违规类型
  - 输出: `autowired-violations-report.txt`

- [ ] **任务2.1.2**: 修复@Autowired违规
  - 批量替换为@Resource注解
  - 验证依赖注入正确性
  - 更新相关测试代码
  - 输出: `autowired-fix-results.txt`

- [ ] **任务2.1.3**: 扫描@Repository违规
  - 搜索DAO接口@Repository使用
  - 生成违规清单
  - 检查MyBatis-Plus兼容性
  - 输出: `repository-violations-report.txt`

- [ ] **任务2.1.4**: 修复@Repository违规
  - 批量替换为@Mapper注解
  - 验证MyBatis扫描配置
  - 更新相关配置文件
  - 输出: `repository-fix-results.txt`

### 2.2 Service接口返回类型修复（3-4天）
- [ ] **任务2.2.1**: 扫描Service接口
  - 识别所有Service接口和实现类
  - 分析ResponseDTO返回类型使用
  - 生成接口清单
  - 输出: `service-interfaces-catalog.txt`

- [ ] **任务2.2.2**: 修复Service接口定义
  - 移除ResponseDTO包装类型
  - 更新方法签名
  - 保持业务语义
  - 输出: 修复后的Service接口文件

- [ ] **任务2.2.3**: 修复Service实现类
  - 更新实现类方法返回类型
  - 移除ResponseDTO包装逻辑
  - 保持异常处理机制
  - 输出: 修复后的Service实现类文件

- [ ] **任务2.2.4**: 更新Controller响应处理
  - 修改Controller调用逻辑
  - 添加ResponseDTO包装
  - 统一异常处理
  - 输出: 修复后的Controller文件

### 2.3 四层架构边界检查（2-3天）
- [ ] **任务2.3.1**: 扫描跨层访问违规
  - 检查Controller直接注入DAO
  - 检查Controller直接注入Manager
  - 分析跨层调用路径
  - 输出: `architecture-violations-report.txt`

- [ ] **任务2.3.2**: 修复四层架构违规
  - 创建缺失的Service层
  - 重构跨层调用逻辑
  - 保持功能完整性
  - 输出: 新增的Service接口和实现类

- [ ] **任务2.3.3**: 验证架构合规性
  - 静态代码分析
  - 分层调用图验证
  - 依赖关系检查
  - 输出: `architecture-compliance-report.txt`

### 2.4 Manager类注解规范修复（2-3天）
- [ ] **任务2.4.1**: 扫描Manager类Spring注解
  - 识别所有Manager类
  - 检查@Component等注解使用
  - 分析依赖注入方式
  - 输出: `manager-classes-catalog.txt`

- [ ] **任务2.4.2**: 修复Manager类注解
  - 移除Spring注解
  - 改为构造函数注入
  - 在配置类中注册Bean
  - 输出: 修复后的Manager类文件

- [ ] **任务2.4.3**: 更新配置类
  - 创建或更新Manager配置类
  - 注册Manager为Spring Bean
  - 验证依赖注入正确性
  - 输出: 更新后的配置类文件

### 2.5 日志规范统一实施（2-3天）
- [ ] **任务2.5.1**: 扫描日志规范违规
  - 检查LoggerFactory使用
  - 分析日志格式不统一
  - 识别敏感信息泄露
  - 输出: `logging-violations-report.txt`

- [ ] **任务2.5.2**: 统一日志注解
  - 添加@Slf4j注解
  - 移除LoggerFactory使用
  - 更新import语句
  - 输出: 修复后的日志使用代码

- [ ] **任务2.5.3**: 统一日志格式
  - 应用标准日志模板
  - 添加模块名称标识
  - 参数化日志消息
  - 输出: 标准化的日志记录代码

## 验证和测试（贯穿全程）

### 质量检查任务
- [ ] **任务Q1**: 每日编译验证
  - 检查编译成功数量
  - 分析新增编译错误
  - 生成日报
  - 输出: `daily-build-report.txt`

- [ ] **任务Q2**: 单元测试验证
  - 运行相关单元测试
  - 检查测试通过率
  - 修复测试失败问题
  - 输出: `unit-test-results.txt`

- [ ] **任务Q3**: 功能验证测试
  - 验证核心功能正常
  - 检查API响应正确性
  - 数据库操作验证
  - 输出: `functional-test-report.txt`

### 文档更新任务
- [ ] **任务D1**: 更新技术文档
  - 更新CLAUDE.md规范
  - 更新架构设计文档
  - 记录修复过程
  - 输出: 更新后的技术文档

- [ ] **任务D2**: 生成执行报告
  - 生成详细执行记录
  - 统计修复成果
  - 分析改进效果
  - 输出: `execution-final-report.txt`

## 交付物清单

### 代码交付物
- [x] 修复后的所有Java源代码文件
- [x] 更新后的pom.xml依赖配置
- [x] 新增的Service接口和实现类
- [x] 更新后的配置类文件

### 文档交付物
- [x] 详细的执行过程记录
- [x] 修复前后对比报告
- [x] 架构合规性验证报告
- [x] 质量改进评估报告

### 工具和脚本交付物
- [x] BOM字符清理PowerShell脚本
- [x] 依赖注入修复脚本
- [x] 架构违规检查脚本
- [x] 自动化验证脚本

---

**总任务数**: 46个任务项
**预估总工时**: 15-20个工作日
**关键里程碑**: 每个子阶段完成后进行验收