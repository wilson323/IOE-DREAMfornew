# Change: 最优七微服务架构重构 - 全面整合实施方案

## Why

基于2025-12-02全局架构深度分析和用户明确要求，IOE-DREAM项目需要从当前分散的微服务架构严格重构为最优的7微服务架构。

**核心问题**：
1. **架构混乱**：当前存在22+个微服务，职责边界不清，代码冗余严重
2. **技术债务**：96个@Repository违规、64个明文密码、65%接口滥用POST
3. **性能瓶颈**：65%查询缺少索引、缓存命中率仅65%
4. **监控缺失**：完全缺少分布式追踪实现

**用户明确要求**：
- "把所有的都整合在公共微服务中"
- "不要再放到其他微服务了，不要oa微服务了"
- "前端和移动端尽可能不要变"
- "确保其他小的微服务完整功能百分百迁移到公共微服务后删掉这些微服务"

## What Changes

### 架构重构 (7微服务严格限制)

| 微服务 | 端口 | 职责 | 整合来源 |
|-------|------|------|---------|
| **ioedream-gateway-service** | 8080 | API网关、路由、限流 | 保持不变 |
| **ioedream-common-service** | 8088 | 公共业务、认证、权限、配置、监控、审计 | auth + identity + notification + audit + monitor + scheduler + system + config + enterprise + infrastructure |
| **ioedream-device-comm-service** | 8087 | 设备协议、连接管理、数据采集 | device-service |
| **ioedream-access-service** | 8090 | 门禁控制、通行记录 | 保持独立 |
| **ioedream-attendance-service** | 8091 | 考勤打卡、排班管理 | 保持独立 |
| **ioedream-consume-service** | 8094 | 消费管理、账户管理 | 保持独立 |
| **ioedream-visitor-service** | 8095 | 访客预约、访客登记 | 保持独立 |
| **ioedream-video-service** | 8092 | 视频监控、录像回放 | 保持独立 |

### 模块职责划分

**microservices-common (公共JAR库)**：
- Entity、DAO、Manager、Form、VO、Config、Constant、Enum、Exception、Util
- ❌ 禁止包含 @Service实现、@RestController、spring-boot-starter-web

**ioedream-common-service (公共业务微服务)**：
- Controller、Service接口、ServiceImpl、服务配置
- 整合所有公共业务功能（认证、权限、配置、监控、审计、通知、调度等）

### 前端/移动端 (保持不变)

- **smart-admin-web-javascript**: Vue 3.4 + Ant Design Vue 4 + Vite 5
- **microservices/frontend/web-main**: Vue 3.4 + qiankun 2.10 微前端
- **smart-app**: uni-app 3.0 + Vue 3 移动端

## Impact

### Affected specs
- microservice-consolidation (新建：微服务整合规格)
- common-service-functions (新建：公共服务功能规格)
- device-communication (修改：设备通讯规格)
- code-quality-standards (新建：代码质量标准规格)

### Affected code
- **整合到common-service**: auth、identity、notification、audit、monitor、scheduler、system、config、enterprise、infrastructure (共10个服务)
- **整合到device-comm-service**: device-service (1个服务)
- **保持独立**: gateway、access、attendance、consume、visitor、video (6个服务)
- **前端/移动端**: 无变更 (API路径保持兼容)

### **BREAKING CHANGES**
- 微服务数量从22+减少到8个 (含网关)
- 废弃的微服务将归档到 `microservices/archive/`
- 数据库结构不变，仅服务整合
- API路径保持向后兼容

## Implementation Scope

### 严格约束
1. **微服务数量**：严格控制在8个 (含网关)
2. **功能完整性**：所有被整合服务功能100%迁移，无遗漏
3. **架构合规性**：四层架构、@Resource注入、@Mapper+Dao命名
4. **代码质量**：单元测试覆盖率≥80%、编译0错误
5. **前端兼容**：API接口100%向后兼容

### 开发规范 (强制执行)
- Jakarta EE 3.0+ 包名 (禁止javax.*)
- @Resource依赖注入 (禁止@Autowired)
- @Mapper + Dao后缀 (禁止@Repository)
- 四层架构 Controller → Service → Manager → DAO
- 通过GatewayServiceClient调用其他服务 (禁止FeignClient直连)
- Druid连接池 (禁止HikariCP)
- UTF-8编码无BOM

## Expected Outcomes

1. **架构清晰**：8个微服务职责明确，边界清晰
2. **代码精简**：消除重复代码，提高可维护性
3. **性能提升**：查询响应时间从800ms降至150ms
4. **安全加固**：0个明文密码，100%加密配置
5. **监控完善**：100%服务调用链可追踪
6. **文档同步**：CLAUDE.md、.cursorrules、架构文档100%更新

