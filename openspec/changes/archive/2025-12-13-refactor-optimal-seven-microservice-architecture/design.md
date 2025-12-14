# 最优七微服务架构设计文档

## Context

### 背景
IOE-DREAM是一个企业级智能管理系统，当前架构存在以下问题：
- 微服务数量过多（22+个），职责边界不清
- 代码冗余严重，多个服务实现相同功能
- 技术债务积累（Repository违规、明文密码等）
- 缺少统一的监控和追踪体系

### 约束条件
1. **用户明确要求**：所有功能整合到公共微服务，不要OA微服务
2. **前端不变**：前端和移动端尽可能不变
3. **功能完整**：被整合服务功能100%迁移
4. **质量标准**：单元测试覆盖率≥80%

### 利益相关者
- 架构师团队：负责架构设计和规范制定
- 开发团队：负责功能迁移和代码实现
- 运维团队：负责部署和监控
- 业务团队：确保业务功能完整性

## Goals / Non-Goals

### Goals
1. ✅ 将22+微服务整合为8个核心微服务（含网关）
2. ✅ 明确microservices-common和ioedream-common-service的职责边界
3. ✅ 实现100%功能迁移，无遗漏
4. ✅ 保持前端/移动端API兼容性
5. ✅ 建立统一的开发规范和质量标准
6. ✅ 实现分布式追踪和监控体系

### Non-Goals
1. ❌ 重构前端架构
2. ❌ 重构移动端架构
3. ❌ 更改数据库表结构
4. ❌ 引入新的技术栈
5. ❌ 更改API接口路径（保持兼容）

## Decisions

### Decision 1: 微服务整合策略

**决策**：将10个公共服务整合到common-service，1个服务整合到device-comm-service

**理由**：
- 公共服务（auth、identity、notification等）功能高度相关，整合后便于维护
- 设备通讯服务专业性强，需要独立处理协议和连接管理
- 业务服务（门禁、考勤、消费等）业务独立，保持独立部署

**替代方案考虑**：
- 方案A：保持22+微服务 → 否决，维护成本高，职责不清
- 方案B：整合为3个微服务 → 否决，粒度过粗，不利于独立扩展
- 方案C：整合为8个微服务 → 采纳，平衡了维护成本和业务独立性

### Decision 2: microservices-common定位

**决策**：microservices-common作为纯Java库，不包含@Service和@Controller

**理由**：
- 公共库应该是无状态的，可被任意微服务依赖
- @Service和@Controller会引入Spring Boot依赖，增加库的复杂度
- 清晰的职责划分有利于代码复用和测试

**允许包含**：
- Entity、DAO、Manager、Form、VO
- Config、Constant、Enum、Exception、Util

**禁止包含**：
- @Service实现类
- @RestController
- spring-boot-starter-web依赖

### Decision 3: 前端兼容策略

**决策**：保持所有API接口路径不变，前端无需修改

**理由**：
- 用户明确要求"前端和移动端尽可能不要变"
- API路径变更会影响所有前端调用，风险高
- 通过网关路由可以透明地将请求转发到新服务

**实现方式**：
- 网关配置路由规则，将旧路径映射到新服务
- Controller层保持原有API路径
- 内部实现可以重构，但对外接口不变

### Decision 4: 数据迁移策略

**决策**：不迁移数据库表结构，仅整合服务代码

**理由**：
- 数据库迁移风险高，可能导致数据丢失
- 多个服务可以共享同一数据库（通过配置）
- 后续可以根据需要进行数据库拆分

**实现方式**：
- common-service连接原有的多个数据库
- 通过配置区分不同模块的数据源
- 使用MyBatis-Plus的多数据源支持

## Risks / Trade-offs

### Risk 1: 功能遗漏
- **风险**：被整合服务的某些功能可能被遗漏
- **缓解**：建立完整的功能对比矩阵，逐一验证每个API端点

### Risk 2: 性能下降
- **风险**：整合后服务负载增加，可能导致性能下降
- **缓解**：实施多级缓存策略，优化数据库索引，进行性能压测

### Risk 3: 编译错误
- **风险**：代码整合过程中可能产生编译错误
- **缓解**：分阶段整合，每阶段验证编译通过后再进行下一阶段

### Risk 4: 回滚困难
- **风险**：整合后如果出现问题，回滚困难
- **缓解**：保留原服务代码30天，使用Git标签备份

## Migration Plan

### Phase 1: 准备阶段 (1周)
1. 创建功能对比矩阵文档
2. 扫描所有被整合服务的API端点
3. 建立测试用例基线
4. 配置CI/CD流水线

### Phase 2: 公共模块整合 (2周)
1. 整合auth-service到common-service
2. 整合identity-service到common-service
3. 整合notification-service到common-service
4. 整合audit-service到common-service
5. 整合monitor-service到common-service
6. 整合scheduler-service到common-service
7. 整合system-service到common-service
8. 整合config-service到common-service
9. 整合enterprise-service到common-service
10. 整合infrastructure-service到common-service

### Phase 3: 设备服务整合 (1周)
1. 整合device-service到device-comm-service
2. 优化设备协议适配器
3. 实现设备连接池管理

### Phase 4: 测试验证 (1周)
1. 执行单元测试（覆盖率≥80%）
2. 执行集成测试
3. 执行性能压测
4. 验证API兼容性

### Phase 5: 归档清理 (0.5周)
1. 将已整合服务移动到archive目录
2. 更新所有相关文档
3. 更新CI/CD配置

### 回滚方案
1. 保留原服务代码30天
2. 使用Git标签标记整合前状态
3. 网关配置支持快速切换路由
4. 数据库不做变更，无需数据回滚

## Open Questions

1. **Q**: 是否需要保留废弃服务的Docker镜像？
   - **A**: 建议保留30天，之后可以删除

2. **Q**: 整合后的common-service是否需要拆分数据源？
   - **A**: 初期可以使用单数据源，后续根据性能需求决定

3. **Q**: 如何处理服务间的循环依赖？
   - **A**: 通过GatewayServiceClient调用，避免直接依赖

4. **Q**: 前端缓存是否需要清理？
   - **A**: 由于API路径不变，前端缓存无需特殊处理

