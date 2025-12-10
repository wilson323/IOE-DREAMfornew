<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# 🏢 IOE-DREAM 智慧园区一卡通管理平台

> **项目定位**: 企业级智慧安防管理平台  
> **核心价值**: 多模态生物识别 + 一卡通 + 智能安防一体化解决方案  
> **技术架构**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0 + Vue3 + 微服务架构  
> **安全等级**: 国家三级等保合规 + 金融级安全防护

---

## 📋 项目概述

### 项目简介

**IOE-DREAM**（Intelligent Operations & Enterprise - Digital Resource & Enterprise Application Management）是IOE基于Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0 + Sa-Token + MyBatis-Plus和Vue3 + Ant Design Vue + Uni-App构建的新一代**智慧园区一卡通管理平台**。

该平台专注于园区一卡通和生物识别安全管理，是国内首个集成多种生物识别技术（人脸、指纹、掌纹、虹膜、声纹等）并满足《网络安全-三级等保》、《数据安全》功能要求的开源项目，支持多模态身份认证、智能门禁控制、无感消费结算、自动考勤管理、智能访客管理、视频监控联动等一系列智慧安防功能。

### 核心定位

| 定位维度 | 说明 |
|---------|------|
| **业务定位** | 智慧园区一卡通管理平台，提供完整的一卡通和生物识别安防解决方案 |
| **技术定位** | 企业级微服务架构，支持高并发、高可用、水平扩展 |
| **安全定位** | 满足国家三级等保要求，具备金融级安全防护体系 |
| **集成定位** | 标准化API接口，支持与各类第三方系统无缝集成 |

### 目标用户

- **园区管理方**: 智慧园区、产业园区、科技园区的管理部门
- **企业安全部门**: 大型企业、学校、医院、政府机构的安全管理部门
- **系统集成商**: 需要生物识别和一卡通管理的各类组织
- **高安全场景**: 对安全性和实时性要求极高的安防应用场景

---

## 🎯 业务场景与解决方案

### 核心业务场景

#### 1. 智慧园区综合管理场景

**业务痛点**:
- ❌ 园区内多个系统独立运行，数据孤岛严重
- ❌ 员工需要携带多张卡片，管理不便
- ❌ 访客管理流程繁琐，安全风险高
- ❌ 缺乏统一的数据分析和决策支持

**IOE-DREAM解决方案**:
- ✅ **一卡通统一管理**: 一张卡片/一个生物特征，通行全园区
- ✅ **多系统数据融合**: 门禁、考勤、消费、访客数据统一管理
- ✅ **智能访客系统**: 预约、审批、识别、授权全流程自动化
- ✅ **数据驱动决策**: 基于大数据的园区运营分析和预测

**典型应用**:
```
智慧园区 → 员工一卡通 → 门禁通行 + 考勤打卡 + 食堂消费 + 访客管理
         ↓
    统一身份认证（人脸/指纹/卡片）
         ↓
    数据统一分析 → 运营决策支持
```

#### 2. 企业安全防护场景

**业务痛点**:
- ❌ 传统门禁系统安全性低，易被破解
- ❌ 无法识别冒名顶替，安全风险高
- ❌ 视频监控与门禁系统独立，无法联动
- ❌ 缺乏异常行为检测和预警机制

**IOE-DREAM解决方案**:
- ✅ **多模态生物识别**: 人脸、指纹、掌纹、虹膜多重验证
- ✅ **活体检测技术**: 防止照片、视频、硅胶面具攻击
- ✅ **视频监控联动**: 生物识别与视频监控智能联动
- ✅ **异常行为检测**: AI智能分析，实时报警推送

**典型应用**:
```
企业办公楼 → 多模态生物识别门禁 → 活体检测 + 权限验证
           ↓
       视频监控联动 → 异常行为检测 → 实时报警
           ↓
       通行记录 + 视频录像 → 安全审计追溯
```

#### 3. 无感消费结算场景

**业务痛点**:
- ❌ 食堂排队时间长，支付效率低
- ❌ 现金管理不便，存在找零问题
- ❌ 消费数据无法实时统计和分析
- ❌ 补贴发放流程复杂，易出错

**IOE-DREAM解决方案**:
- ✅ **无感支付**: 刷脸/刷卡/手机NFC，秒级完成支付
- ✅ **离线消费**: 支持网络中断情况下的离线消费
- ✅ **实时统计**: 消费数据实时统计，经营状况一目了然
- ✅ **智能补贴**: 自动发放员工补贴，支持多种补贴策略

**典型应用**:
```
企业食堂 → 员工刷脸/刷卡 → 身份识别 + 余额检查
         ↓
    秒级支付完成 → 消费记录 → 实时统计
         ↓
    补贴自动发放 → 财务报表 → 经营分析
```

#### 4. 智能考勤管理场景

**业务痛点**:
- ❌ 传统打卡方式易代打卡，考勤数据不准确
- ❌ 排班管理复杂，规则配置困难
- ❌ 考勤数据统计繁琐，报表生成慢
- ❌ 无法与门禁、消费系统数据联动

**IOE-DREAM解决方案**:
- ✅ **生物识别打卡**: 人脸/指纹识别，防止代打卡
- ✅ **灵活排班管理**: 支持固定班次、弹性时间、轮班等多种模式
- ✅ **自动考勤统计**: 自动计算出勤、迟到、早退、加班
- ✅ **多系统数据融合**: 与门禁、消费数据联动分析

**典型应用**:
```
企业考勤 → 员工刷脸打卡 → 生物识别 + 位置验证
         ↓
    考勤记录 → 排班规则匹配 → 自动统计
         ↓
    考勤报表 → 与门禁数据联动 → 异常分析
```

#### 5. 访客智能管理场景

**业务痛点**:
- ❌ 访客登记流程繁琐，等待时间长
- ❌ 无法提前预约，临时访客管理困难
- ❌ 访客身份无法验证，安全风险高
- ❌ 访客轨迹无法追踪，事后追溯困难

**IOE-DREAM解决方案**:
- ✅ **在线预约**: 访客提前预约，审批流程自动化
- ✅ **身份验证**: 人脸识别验证访客身份
- ✅ **临时授权**: 支持临时门禁权限发放和回收
- ✅ **轨迹追踪**: 完整记录访客在园区的活动轨迹

**典型应用**:
```
访客管理 → 在线预约 → 审批通过 → 人脸识别登记
         ↓
    临时权限发放 → 门禁通行 → 轨迹记录
         ↓
    访问结束 → 权限回收 → 访问报告
```

#### 6. 视频监控智能分析场景

**业务痛点**:
- ❌ 视频监控被动，需要人工查看
- ❌ 无法自动识别异常行为
- ❌ 视频检索困难，查找特定目标耗时
- ❌ 无法与门禁、访客系统联动

**IOE-DREAM解决方案**:
- ✅ **智能分析**: AI人脸识别、行为分析、异常检测
- ✅ **目标搜索**: 快速检索特定人员或车辆的历史轨迹
- ✅ **实时告警**: 异常行为自动检测，实时推送告警
- ✅ **多系统联动**: 与门禁、访客系统智能联动

**典型应用**:
```
视频监控 → 实时监控 → AI智能分析 → 异常检测
         ↓
    目标搜索 → 人脸识别 → 轨迹追踪
         ↓
    门禁联动 → 访客联动 → 告警推送
```

---

## 💼 业务价值

### 核心业务价值

| 价值维度 | 具体价值 |
|---------|---------|
| **身份统一管理** | 建立统一的数字身份体系，支持多模态生物识别，一张脸/一张卡通行全园区 |
| **无感通行体验** | 实现刷脸、刷卡、NFC等多种方式的便捷通行，秒级识别，无需等待 |
| **智能安全保障** | 通过AI分析和多系统联动，提供全方位安全保障，满足三级等保要求 |
| **数据驱动决策** | 基于大数据分析，为园区管理提供决策支持，提升运营效率 |
| **运营效率提升** | 自动化处理流程，降低运营成本，提升管理效率，减少人工干预 |

### 解决的核心问题

1. **数据孤岛问题** → 统一数据平台，多系统数据融合
2. **身份管理分散** → 统一身份认证，多模态生物识别
3. **安全防护薄弱** → 多层级安全防护，智能异常检测
4. **管理效率低下** → 自动化流程，智能化管理
5. **用户体验差** → 无感支付，便捷通行，智能服务

---

## 🏗️ 技术架构亮点

### 微服务架构

**7个核心微服务**:
- **ioedream-gateway-service** (8080): API网关，统一入口
- **ioedream-common-service** (8088): 公共业务服务
- **ioedream-device-comm-service** (8087): 设备通讯服务
- **ioedream-oa-service** (8089): OA办公服务
- **ioedream-access-service** (8090): 门禁管理服务
- **ioedream-attendance-service** (8091): 考勤管理服务
- **ioedream-video-service** (8092): 视频监控服务
- **ioedream-consume-service** (8094): 消费管理服务
- **ioedream-visitor-service** (8095): 访客管理服务

### 技术栈优势

- **Spring Boot 3.5.8**: 现代化框架，支持虚拟线程，性能优异
- **Spring Cloud 2025.0.0**: 最新微服务框架，完全兼容Spring Boot 3.5.8
- **Spring Cloud Alibaba 2025.0.0.0**: 最新稳定版，完全兼容当前技术栈，支持完整的`optional:nacos:`功能
- **Java 17**: LTS版本，长期支持，性能优化
- **Vue3 + Vite5**: 前端现代化，开发体验优秀
- **多级缓存**: L1本地缓存 + L2 Redis缓存 + L3网关缓存
- **分布式事务**: SAGA模式，确保数据一致性
- **微服务治理**: Nacos注册中心 + 配置中心（支持可选配置加载）

---

## 📚 IOE-DREAM 文档导航中心

> **📋 文档管理状态**: ✅ 已完成全面清理和重组 (2025-12-02)
> **📊 文档总数**: 已从1935个优化整理，建立清晰的文档体系
> **🗂️ 文档架构**: 统一使用 `documentation/` 作为唯一文档目录
> **👥 维护责任人**: 老王(架构师团队) + 各业务模块负责人

---

## 🎯 快速导航（按开发场景）

### 🚀 新手入门
- **📖 项目快速开始**: [documentation/technical/00-快速开始/](./documentation/technical/00-快速开始/)
- **🔧 开发环境配置**: [documentation/technical/开发指南.md](./documentation/technical/repowiki/zh/content/开发指南.md)
- **📋 核心规范10条**: [documentation/technical/00-快速开始/核心规范10条.md](./documentation/technical/00-快速开始/核心规范10条.md)

### 🏗️ 架构设计
- **📐 四层架构详解**: [documentation/technical/四层架构详解.md](./documentation/technical/repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md)
- **🏛️ 微服务架构**: [documentation/architecture/](./documentation/architecture/)
- **🔗 API设计规范**: [documentation/api/](./documentation/api/)

### 💻 开发规范
- **📜 Java编码规范**: [documentation/technical/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md](./documentation/technical/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md)
- **🎨 Vue3开发规范**: [documentation/technical/repowiki/zh/content/开发规范体系/Vue3开发规范.md](./documentation/technical/repowiki/zh/content/开发规范体系/Vue3开发规范.md)
- **📋 统一开发标准**: [documentation/technical/UNIFIED_DEVELOPMENT_STANDARDS.md](./documentation/technical/UNIFIED_DEVELOPMENT_STANDARDS.md)

### 🗄️ 数据库设计
- **🗃️ 数据库设计规范**: [documentation/technical/repowiki/zh/content/后端架构/数据模型与ORM/](./documentation/technical/repowiki/zh/content/后端架构/数据模型与ORM/)
- **📊 SQL性能优化**: [documentation/technical/repowiki/zh/content/后端架构/数据模型与ORM/SQL映射与动态SQL/SQL性能优化.md](./documentation/technical/repowiki/zh/content/后端架构/数据模型与ORM/SQL映射与动态SQL/SQL性能优化.md)

### 🔒 安全体系
- **🛡️ 安全体系规范**: [documentation/security/](./documentation/security/)
- **🔐 接口加解密**: [documentation/technical/repowiki/zh/content/安全体系/接口加解密/接口加解密.md](./documentation/technical/repowiki/zh/content/安全体系/接口加解密/接口加解密.md)
- **🔒 数据脱敏**: [documentation/technical/repowiki/zh/content/安全体系/数据脱敏.md](./documentation/technical/repowiki/zh/content/安全体系/数据脱敏.md)

### 📦 业务模块
- **🏢 企业OA系统**: [documentation/technical/repowiki/zh/content/核心功能模块/企业OA系统/](./documentation/technical/repowiki/zh/content/核心功能模块/企业OA系统/)
- **🚪 智能门禁**: [documentation/business/](./documentation/business/) (门禁相关文档)
- **💳 消费管理**: [documentation/business/](./documentation/business/) (消费相关文档)
- **⏰ 考勤系统**: [documentation/business/](./documentation/business/) (考勤相关文档)
- **👥 访客管理**: [documentation/business/](./documentation/business/) (访客相关文档)
- **📹 视频监控**: [documentation/business/](./documentation/business/) (视频相关文档)

### 🚀 部署运维
- **🐳 Docker部署**: [documentation/deployment/docker/](./documentation/deployment/docker/)
- **☸️ Kubernetes**: [documentation/deployment/k8s/](./documentation/deployment/k8s/)
- **📊 监控运维**: [documentation/maintenance/](./documentation/maintenance/)
- **📋 部署指南**: [DEPLOYMENT-GUIDE.md](./documentation/project/archive/root-reports/DEPLOYMENT-GUIDE.md)

### 🔧 开发工具
- **⚙️ 开发检查清单**: [documentation/technical/CHECKLISTS/](./documentation/technical/CHECKLISTS/)
- **🤖 AI辅助开发**: [documentation/development/training/](./documentation/development/training/)
- **📋 文档管理规范**: [DOCUMENTATION_MANAGEMENT_STANDARDS.md](./documentation/DOCUMENTATION_MANAGEMENT_STANDARDS.md)

---

## 🗂️ 完整文档目录结构

### 📁 documentation/ (主文档目录)
```
documentation/
├── 📋 DOCUMENTATION_MANAGEMENT_STANDARDS.md    # 文档管理规范
├── 🏗️ architecture/                            # 架构设计文档
├── 💻 api/                                    # API接口文档
├── 🏢 business/                               # 业务需求文档
├── 🛠️ development/                            # 开发指南文档
├── 🚀 deployment/                            # 部署运维文档
├── 🔒 security/                              # 安全相关文档
├── 📊 maintenance/                           # 监控维护文档
├── 📈 project/                               # 项目管理文档
└── 🗂️ archive/                               # 归档历史文档
    ├── docs-legacy/                          # 原docs目录归档
    ├── legacy-standards/                      # 旧版标准归档
    ├── legacy-workflows/                      # 旧版流程归档
    ├── legacy-tech-stack/                     # 旧版技术栈归档
    ├── root-reports/                         # 根目录报告归档
    └── ...
```

### 📁 项目专项文档
```
├── 📋 CLAUDE.md                              # 项目核心指导文档 (本文件)
├── 📋 openspec/                              # OpenSpec规范管理
├── 🔧 scripts/                              # 自动化脚本
├── 🎯 .claude/skills/                        # AI技能体系
└── 📦 microservices/                         # 微服务文档
```

---

## 🔍 文档搜索技巧

### 按文档类型搜索
- **规范文档**: 搜索 `规范`、`STANDARD`、`GUIDE`
- **API文档**: 搜索 `API`、`接口`、`REST`
- **架构文档**: 搜索 `架构`、`ARCHITECTURE`、`设计`
- **部署文档**: 搜索 `部署`、`DEPLOY`、`DOCKER`

### 按业务模块搜索
- **门禁**: 搜索 `门禁`、`ACCESS`、`智能门禁`
- **消费**: 搜索 `消费`、`CONSUME`、`一卡通`
- **考勤**: 搜索 `考勤`、`ATTENDANCE`、`排班`
- **视频**: 搜索 `视频`、`VIDEO`、`监控`

### 按技术栈搜索
- **Java**: 搜索 `JAVA`、`SPRING`、`MYBATIS`
- **Vue**: 搜索 `VUE`、`前端`、`COMPONENT`
- **数据库**: 搜索 `MYSQL`、`REDIS`、`SQL`

---

## 📞 文档支持和反馈

### 🆘 遇到文档问题？
1. **文档缺失**: 在项目issue中提交 "文档缺失" 标签
2. **内容错误**: 联系对应模块负责人或架构师团队
3. **格式问题**: 参考 [文档管理规范](./documentation/DOCUMENTATION_MANAGEMENT_STANDARDS.md)

### 📝 贡献文档
1. **遵循规范**: 严格按照 [文档管理规范](./documentation/DOCUMENTATION_MANAGEMENT_STANDARDS.md) 创建
2. **内容准确**: 确保技术内容准确无误
3. **格式统一**: 使用标准Markdown格式
4. **及时更新**: 保持内容与项目同步

### 🔄 文档更新机制
- **定期清理**: 每月自动清理过期和重复文档
- **质量检查**: 每季度进行文档质量评估
- **用户反馈**: 持续收集开发者反馈并改进

---

**💡 重要提醒**:
- 本导航中心是IOE-DREAM项目的唯一文档入口
- 所有文档都遵循统一的管理规范和质量标准
- 发现文档问题请及时反馈，让我们共同维护优质的文档环境

---

# IOE-DREAM 项目全局统一架构规范

**版本**: v4.0.0 - 七微服务重构版
**生效日期**: 2025-12-02
**重要更新**: 严格按照用户要求重构为7个微服务架构：公共模块+设备通讯+OA+考勤+门禁+消费+访客+视频
**适用范围**: IOE-DREAM智能管理系统所有微服务和模块
**规范优先级**: 本规范为项目唯一架构规范，所有开发必须严格遵循
**规范定位**: 企业级生产环境架构标准，涵盖高可用、高性能、高扩展性要求

**深度分析基础**: 基于2025-12-01全局架构深度分析结果，综合评分83/100，全面反映项目现状和优化方向

> **分析团队**: 老王（企业级架构分析专家团队）
> **分析范围**: 22个微服务，9个关键维度，500+文件深度分析
> **分析结果**: 识别了关键问题和优化机会，制定了详细的改进路线图

---

## 📋 核心架构原则

### 1. 四层架构规范（强制执行）

```
Controller → Service → Manager → DAO
```

**严格分层职责**：

#### 🎯 Controller层 - 接口控制层
**核心职责**：
- 接收HTTP请求，参数验证(@Valid)
- 调用Service层，暴露REST API
- 封装ResponseDTO，处理HTTP状态码
- 异常统一处理和错误码返回

**代码模板**：
```java
@RestController
@RequestMapping("/api/v1/consume")
public class ConsumeController {

    @Resource
    private ConsumeService consumeService;

    @PostMapping("/consume")
    public ResponseDTO<ConsumeResultDTO> consume(@Valid @RequestBody ConsumeRequestDTO request) {
        ConsumeResultDTO result = consumeService.consume(request);
        return ResponseDTO.ok(result);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }
}
```

#### ⚙️ Service层 - 核心业务层
**核心职责**：
- 核心业务逻辑实现
- 事务管理(@Transactional)
- 调用Manager层进行复杂流程编排
- 业务规则验证和数据转换

**代码模板**：
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeManager consumeManager;
    @Resource
    private AccountDao accountDao;

    @Override
    public ConsumeResultDTO consume(ConsumeRequestDTO request) {
        // 业务规则验证
        validateConsumeRequest(request);

        // 核心业务逻辑
        return consumeManager.executeConsumption(request);
    }
}
```

#### 🔧 Manager层 - 复杂流程管理层
**核心职责**：
- 复杂业务流程编排
- 多DAO数据组装和计算
- 缓存策略管理
- 第三方服务集成
- SAGA分布式事务协调

**代码模板**：
```java
// ✅ 正确：Manager类在microservices-common中不使用Spring注解
// Manager类通过构造函数注入依赖，保持为纯Java类
public class ConsumeManagerImpl implements ConsumeManager {

    private final AccountDao accountDao;
    private final ConsumeRecordDao consumeRecordDao;
    private final GatewayServiceClient gatewayServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    // 构造函数注入依赖
    public ConsumeManagerImpl(
            AccountDao accountDao,
            ConsumeRecordDao consumeRecordDao,
            GatewayServiceClient gatewayServiceClient,
            RedisTemplate<String, Object> redisTemplate) {
        this.accountDao = accountDao;
        this.consumeRecordDao = consumeRecordDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResultDTO executeConsumption(ConsumeRequestDTO request) {
        // 多级缓存查询
        AccountEntity account = getAccountWithCache(request.getAccountId());

        // 复杂业务流程
        // 1. 权限验证
        // 2. 余额计算
        // 3. 交易记录
        // 4. 通知发送

        return result;
    }

    private AccountEntity getAccountWithCache(Long accountId) {
        // L1本地缓存 + L2 Redis缓存 + L3网关调用
        return cacheManager.getWithRefresh(
            "account:" + accountId,
            () -> accountDao.selectById(accountId),
            Duration.ofMinutes(30)
        );
    }
}
```

#### 🗄️ DAO层 - 数据访问层
**核心职责**：
- 数据库CRUD操作
- 复杂SQL查询实现
- 继承BaseMapper<Entity>
- 数据库事务边界控制

**代码模板**：
```java
@Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {

    @Transactional(readOnly = true)
    AccountEntity selectByUserId(@Param("userId") Long userId);

    @Transactional(rollbackFor = Exception.class)
    int updateBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    @Select("SELECT * FROM account WHERE user_id = #{userId} AND status = 1 FOR UPDATE")
    AccountEntity selectByUserIdForUpdate(@Param("userId") Long userId);
}
```

**架构边界铁律**：
- ❌ **禁止跨层访问**（如Controller直接调用DAO）
- ❌ **禁止DAO包含业务逻辑**（只处理数据访问）
- ❌ **禁止Controller处理事务**（事务只在Service和DAO层）
- ❌ **禁止Service直接访问数据库**（通过DAO层访问）

### 2. 依赖注入规范（强制执行）

**强制要求**：
- ✅ **统一使用 `@Resource` 注解**
- ❌ **禁止使用 `@Autowired`**
- ❌ **禁止使用构造函数注入**

```java
// ✅ 正确示例
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeManager consumeManager;

    @Resource
    private AccountDao accountDao;
}

// ❌ 错误示例
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Autowired  // 禁止使用
    private ConsumeManager consumeManager;
}
```

### 3. DAO层命名规范（强制执行）

**强制要求**：
- ✅ **数据访问层接口统一使用 `Dao` 后缀**
- ✅ **必须使用 `@Mapper` 注解标识**
- ✅ **必须继承 `BaseMapper<Entity>`**
- ❌ **禁止使用 `Repository` 后缀**
- ❌ **禁止使用 `@Repository` 注解**

```java
// ✅ 正确示例
@Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {

    @Transactional(readOnly = true)
    AccountEntity selectByUserId(@Param("userId") Long userId);

    @Transactional(rollbackFor = Exception.class)
    int updateBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);
}

// ❌ 错误示例
@Repository  // 禁止使用
public interface AccountRepository extends BaseMapper<AccountEntity> {  // 禁止使用Repository后缀
}
```

### 4. 事务管理规范（强制执行）

**事务注解使用**：
- **Service层写操作**: `@Transactional(rollbackFor = Exception.class)`
- **DAO层查询方法**: `@Transactional(readOnly = true)`
- **DAO层写操作**: `@Transactional(rollbackFor = Exception.class)`

```java
// ✅ Service层示例
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeServiceImpl implements ConsumeService {
    // 写操作方法自动继承类级别事务
}

// ✅ DAO层示例
@Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {

    @Transactional(readOnly = true)
    AccountEntity selectByUserId(@Param("userId") Long userId);

    @Transactional(rollbackFor = Exception.class)
    int updateBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);
}
```

### 5. Jakarta EE包名规范（强制执行）

**强制使用Jakarta EE 3.0+包名**：
- ✅ `jakarta.annotation.Resource`
- ✅ `jakarta.validation.Valid`
- ✅ `jakarta.persistence.Entity`
- ✅ `jakarta.servlet.http.HttpServletRequest`
- ✅ `jakarta.transaction.Transactional`

**禁止使用javax包名**：
- ❌ `javax.annotation.Resource`
- ❌ `javax.validation.Valid`
- ❌ `javax.persistence.Entity`

### 6. 微服务间调用规范（强制执行）

**统一通过网关调用**：
- ✅ **所有服务间调用必须通过API网关**
- ✅ **使用 `GatewayServiceClient` 统一调用**
- ❌ **禁止使用 FeignClient 直接调用**
- ❌ **禁止直接访问其他服务数据库**

```java
// ✅ 正确示例
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    public AreaEntity getAreaInfo(Long areaId) {
        ResponseDTO<AreaEntity> result = gatewayServiceClient.callCommonService(
            "/api/v1/area/" + areaId,
            HttpMethod.GET,
            null,
            AreaEntity.class
        );
        return result.getData();
    }
}

// ❌ 错误示例
// @FeignClient(name = "ioedream-identity-service")  // 禁止使用
// public interface AreaServiceClient {
//     @GetMapping("/api/v1/area/{id}")
//     AreaEntity getArea(@PathVariable Long id);
// }
```

### 7. 服务注册发现规范（强制执行）

**统一使用Nacos**：
- ✅ **所有微服务必须使用 Nacos 作为服务注册发现中心**
- ❌ **禁止使用 Consul、Eureka 等其他注册中心**

```yaml
# bootstrap.yml 标准配置
spring:
  application:
    name: ${SERVICE_NAME:ioedream-xxx-service}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        enabled: true
        register-enabled: true
      config:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        file-extension: yaml
```

### 8. 数据库连接池规范（强制执行）

**统一使用Druid连接池**：
- ✅ **统一使用 Druid 连接池**
- ❌ **禁止使用 HikariCP**

```yaml
# ✅ 标准Druid配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
```

### 9. 缓存使用规范（强制执行）

**统一使用Redis缓存**：
- ✅ **Redis数据库统一使用 db=0**
- ✅ **合理设置缓存过期时间**
- ✅ **使用L1本地缓存+L2 Redis缓存的多级缓存策略**

```yaml
# ✅ 标准Redis配置
spring:
  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0  # 统一使用数据库0
    timeout: 3000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

---

## 🏗️ 企业级架构特性（强制执行）

### 10. 多级缓存架构（强制执行）

**三级缓存策略**：
- **L1本地缓存**: Caffeine本地缓存，毫秒级响应
- **L2 Redis缓存**: 分布式缓存，数据一致性
- **L3网关缓存**: 服务间调用缓存，减少网络开销

**实现模板**：
```java
// ✅ 正确：CacheManager在microservices-common中不使用Spring注解
public class CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    // 构造函数注入依赖
    public CacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T getWithRefresh(String key, Supplier<T> loader, Duration ttl) {
        // L1本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // L2 Redis缓存
        value = (T) redisTemplate.opsForValue().get(key);
        if (value != null) {
            localCache.put(key, value);
            return value;
        }

        // 从数据库加载
        value = loader.get();
        if (value != null) {
            localCache.put(key, value);
            redisTemplate.opsForValue().set(key, value, ttl);
        }

        return value;
    }
}
```

### 11. SAGA分布式事务（强制执行）

**SAGA实现要求**：
- ✅ **使用SAGA模式实现最终一致性**
- ✅ **每个步骤都有对应的补偿操作**
- ✅ **事务状态跟踪和监控**
- ✅ **失败自动重试和人工干预**

**实现模板**：
```java
// ✅ 正确：ConsumeSagaManager在microservices-common中不使用Spring注解
public class ConsumeSagaManager {

    private final SagaManager sagaManager;

    // 构造函数注入依赖
    public ConsumeSagaManager(SagaManager sagaManager) {
        this.sagaManager = sagaManager;
    }

    public ResponseDTO<ConsumeResultDTO> executeConsumeSaga(ConsumeRequestDTO request) {
        SagaTransaction saga = sagaManager.createSaga("consume", request.getOrderId())
                .step("balanceDeduct", this::deductBalance, this::refundBalance)
                .step("recordConsume", this::createConsumeRecord, this::deleteConsumeRecord)
                .step("sendNotification", this::sendNotification, this::cancelNotification)
                .build();

        return saga.execute();
    }

    // 业务步骤
    private SagaStep deductBalance(ConsumeRequestDTO request) {
        // 扣减余额逻辑
        return SagaStep.success();
    }

    // 补偿步骤
    private SagaStep refundBalance(ConsumeRequestDTO request) {
        // 退还余额逻辑
        return SagaStep.success();
    }
}
```

### 12. 服务降级熔断（强制执行）

**容错机制要求**：
- ✅ **使用Hystrix或Sentinel实现熔断**
- ✅ **关键服务配置降级策略**
- ✅ **实现服务调用超时控制**
- ✅ **配置熔断后的恢复机制**

**实现模板**：
```java
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeManager consumeManager;

    @CircuitBreaker(name = "consumeService", fallbackMethod = "consumeFallback")
    @TimeLimiter(name = "consumeService")
    public CompletableFuture<ConsumeResultDTO> consume(ConsumeRequestDTO request) {
        return CompletableFuture.completedFuture(consumeManager.executeConsumption(request));
    }

    public CompletableFuture<ConsumeResultDTO> consumeFallback(ConsumeRequestDTO request, Exception ex) {
        log.error("消费服务降级", ex);
        ConsumeResultDTO fallbackResult = new ConsumeResultDTO();
        fallbackResult.setStatus("DEGRADED");
        fallbackResult.setMessage("系统繁忙，请稍后重试");
        return CompletableFuture.completedFuture(fallbackResult);
    }
}
```

### 13. 异步处理机制（强制执行）

**异步处理要求**：
- ✅ **使用线程池处理耗时操作**
- ✅ **消息队列处理异步任务**
- ✅ **实现事件驱动架构**
- ✅ **配置合理的队列大小和超时**

**实现模板**：
```java
@Service
public class ConsumeAsyncService {

    @Resource
    private TaskExecutor taskExecutor;
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void asyncProcessConsume(ConsumeRequestDTO request) {
        // 异步处理消费记录
        taskExecutor.execute(() -> {
            processConsumeRecord(request);
        });

        // 发送异步通知
        rabbitTemplate.convertAndSend("consume.notification", request);
    }

    @RabbitListener(queues = "consume.notification")
    public void handleNotification(ConsumeRequestDTO request) {
        // 处理通知逻辑
        sendNotification(request);
    }
}
```

### 14. 监控告警体系（强制执行）

**监控指标要求**：
- ✅ **业务指标监控**（消费量、成功率、响应时间）
- ✅ **系统指标监控**（CPU、内存、GC、线程池）
- ✅ **调用链监控**（分布式链路追踪）
- ✅ **错误监控**（异常统计、错误告警）

**实现模板**：
```java
// ✅ 正确：ConsumeMonitor在microservices-common中不使用Spring注解
public class ConsumeMonitor {

    private final MeterRegistry meterRegistry;

    // 构造函数注入依赖
    public ConsumeMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public ConsumeMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordConsume(String type, double amount, long duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("consume.duration")
                .tag("type", type)
                .register(meterRegistry));

        meterRegistry.counter("consume.count", "type", type).increment();
        meterRegistry.gauge("consume.amount", amount);
    }

    @EventListener
    public void handleConsumeEvent(ConsumeEvent event) {
        if (event.isSuccess()) {
            recordConsume(event.getType(), event.getAmount(), event.getDuration());
        } else {
            meterRegistry.counter("consume.error", "type", event.getType()).increment();
        }
    }
}
```

---

## 🔌 端口分配规范（强制执行）

**严格按照7微服务架构分配端口：**

| 服务名称 | 端口 | 类型 | 说明 |
|---------|------|------|------|
| ioedream-gateway-service | 8080 | 基础设施 | API网关 |
| **ioedream-common-service** | **8088** | **核心** | **公共模块微服务** |
| **ioedream-device-comm-service** | **8087** | **核心** | **设备通讯微服务** |
| **ioedream-oa-service** | **8089** | **核心** | **OA微服务** |
| ioedream-access-service | 8090 | 核心 | 门禁服务 |
| ioedream-attendance-service | 8091 | 核心 | 考勤服务 |
| ioedream-video-service | 8092 | 核心 | 视频服务 |
| ioedream-consume-service | 8094 | 核心 | 消费服务 |
| ioedream-visitor-service | 8095 | 核心 | 访客服务 |
| ioedream-config-service | 8888 | 基础设施 | Nacos配置中心 |

**注意：** 以下服务已整合到7个核心微服务中，不再独立存在：
- ioedream-auth-service → 整合到 ioedream-common-service
- ioedream-identity-service → 整合到 ioedream-common-service
- ioedream-device-service → 整合到 ioedream-device-comm-service
- ioedream-enterprise-service → 整合到 ioedream-oa-service
- ioedream-notification-service → 整合到 ioedream-common-service
- ioedream-audit-service → 整合到 ioedream-common-service
- ioedream-monitor-service → 整合到 ioedream-common-service
- ioedream-integration-service → 拆分到各业务服务
- ioedream-system-service → 整合到 ioedream-common-service
- ioedream-report-service → 拆分到各业务服务
- ioedream-scheduler-service → 整合到 ioedream-common-service
- ioedream-infrastructure-service → 整合到 ioedream-oa-service

---

## 📝 代码质量标准（强制执行）

### 质量指标
- ✅ 代码覆盖率 ≥ 80%
- ✅ 核心业务覆盖率 = 100%
- ✅ 重复代码率 ≤ 3%
- ✅ 圈复杂度 ≤ 10
- ✅ 代码行数/方法 ≤ 50
- ✅ 类行数 ≤ 500

### 编码规范
- ✅ 使用UTF-8编码
- ✅ 统一代码格式化规则
- ✅ 完整的JavaDoc注释
- ✅ 合理的日志记录
- ✅ 完善的异常处理

---

## ⚡ 性能优化策略（基于深度分析结果的强制执行）

### 🎯 深度分析发现的性能问题
**性能现状评估**: 全局性能维度评分3.2/5.0，存在明显性能瓶颈
**分析依据**: 2025-12-01全局架构深度分析结果，性能维度评分严重偏低

**关键性能问题**:
- 🔴 **数据库查询性能差**: 65%的查询缺少合适索引，存在全表扫描
- 🔴 **缓存命中率低**: 平均缓存命中率仅65%，远低于企业级标准85%
- 🔴 **连接池配置不当**: 12个服务使用HikariCP，违反统一Druid规范
- 🔴 **深度分页问题**: 38%的分页查询存在深度分页性能问题

### 15. 数据库性能优化（强制执行）

**基于分析结果的优化要求**：
- ✅ **立即解决65%查询缺少索引问题**（P1优先级，直接影响性能）
- ✅ **消除所有全表扫描**（发现23个全表扫描查询，必须优化）
- ✅ **使用复合索引优化多条件查询**（提升查询效率300%）
- ✅ **立即优化38个深度分页查询**（使用游标分页替代）

**分析发现的问题与解决方案**：
```sql
-- ❌ 分析发现的性能问题 - 全表扫描
SELECT * FROM consume_record WHERE create_time > '2024-01-01';  -- 全表扫描！

-- ✅ 优化方案 - 添加复合索引
CREATE INDEX idx_consume_record_create_time_status ON consume_record(create_time, status, deleted_flag);

-- ❌ 分析发现的性能问题 - 深度分页
SELECT * FROM consume_record ORDER BY create_time DESC LIMIT 10000, 20;  -- 深度分页！

-- ✅ 优化方案 - 游标分页
SELECT * FROM consume_record
WHERE create_time < #{lastCreateTime}
ORDER BY create_time DESC
LIMIT 20;
```

**性能优化的量化目标**：
- **查询性能提升**: 平均响应时间从800ms降至150ms（81%提升）
- **并发处理能力**: TPS从500提升至2000（300%提升）
- **数据库连接优化**: 连接利用率从60%提升至90%
- **索引优化覆盖**: 从35%提升至100%全覆盖

**SQL优化模板**：
```sql
-- ✅ 正确示例：使用索引优化
CREATE INDEX idx_user_area_status ON consume_record(user_id, area_id, status, create_time);

-- 分页查询优化（避免深度分页问题）
SELECT * FROM consume_record
WHERE user_id = #{userId} AND create_time < #{lastCreateTime}
ORDER BY create_time DESC
LIMIT 20;

-- 批量操作优化
INSERT INTO consume_record (user_id, amount, create_time) VALUES
(#{user1}, #{amount1}, NOW()),
(#{user2}, #{amount2}, NOW()),
(#{user3}, #{amount3}, NOW());
```

**连接池优化配置**：
```yaml
spring:
  datasource:
    druid:
      # 核心连接池配置
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000

      # 性能监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*

      # 慢查询监控
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000
          log-slow-sql: true
```

### 16. 缓存性能优化（强制执行）

**缓存策略优化**：
- ✅ **合理设置缓存大小和过期时间**
- ✅ **使用缓存预热避免冷启动**
- ✅ **实现缓存击穿、雪崩防护**
- ✅ **监控缓存命中率和性能**

**缓存优化实现**：
```java
// ✅ 正确：ConsumeCacheOptimizer在microservices-common中不使用Spring注解
public class ConsumeCacheOptimizer {

    private final RedisTemplate<String, Object> redisTemplate;

    // 构造函数注入依赖
    public ConsumeCacheOptimizer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 缓存预热
    @PostConstruct
    public void warmUpCache() {
        // 预加载热点数据
        loadHotAccounts();
        loadHotProducts();
    }

    // 防止缓存击穿（使用互斥锁）
    public AccountEntity getAccountWithLock(Long accountId) {
        String lockKey = "lock:account:" + accountId;
        String value = UUID.randomUUID().toString();

        try {
            // 尝试获取锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, value, Duration.ofSeconds(10));
            if (locked) {
                // 双重检查
                AccountEntity account = (AccountEntity) redisTemplate.opsForValue().get("account:" + accountId);
                if (account == null) {
                    account = accountDao.selectById(accountId);
                    redisTemplate.opsForValue().set("account:" + accountId, account, Duration.ofMinutes(30));
                }
                return account;
            }
        } finally {
            // 释放锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(lockKey), value);
        }

        // 降级查询数据库
        return accountDao.selectById(accountId);
    }
}
```

### 17. JVM性能调优（强制执行）

**JVM参数标准配置**：
```bash
# 生产环境推荐配置
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/app/
-Dfile.encoding=UTF-8
-Duser.timezone=Asia/Shanghai
```

**性能监控配置**：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.9,0.95,0.99
```

---

## 📈 系统扩展性设计（强制执行）

### 18. 水平扩展设计（强制执行）

**无状态服务设计**：
- ✅ **所有服务必须设计为无状态**
- ✅ **会话信息存储在Redis中**
- ✅ **文件存储使用分布式文件系统**
- ✅ **支持动态扩缩容**

**无状态服务模板**：
```java
@RestController
public class ConsumeController {

    // 无状态Controller，不存储任何实例变量
    @Resource
    private ConsumeService consumeService;

    // 使用ThreadLocal处理线程安全的临时数据
    private final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    @PostMapping("/consume")
    public ResponseDTO<ConsumeResultDTO> consume(@Valid @RequestBody ConsumeRequestDTO request) {
        try {
            // 从Token中获取用户信息
            UserContext context = getUserContextFromToken();
            userContext.set(context);

            return consumeService.consume(request);
        } finally {
            userContext.remove(); // 清理ThreadLocal
        }
    }
}
```

### 19. 数据库分库分表策略（强制执行）

**分片策略要求**：
- ✅ **按业务域进行垂直分库**
- ✅ **按数据量进行水平分表**
- ✅ **使用分布式ID生成器**
- ✅ **实现跨库查询解决方案**

**分库分表实现**：
```java
// ✅ 正确：ShardingStrategy在microservices-common中不使用Spring注解
// 工具类通常不需要依赖注入，可以直接使用静态方法或实例方法
public class ShardingStrategy {

    // 数据库分片策略
    public String getDatabaseName(Long userId) {
        // 按用户ID哈希值分库
        int dbIndex = (int) (userId % 4);
        return "ioedream_consume_" + dbIndex;
    }

    // 表分片策略
    public String getTableName(String tableName, Long id) {
        // 按ID范围分表
        int tableIndex = (int) (id % 16);
        return tableName + "_" + tableIndex;
    }

    // 分布式ID生成（雪花算法）
    public Long generateId() {
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(1, 1);
        return idWorker.nextId();
    }
}
```

### 20. 微服务扩展性设计（强制执行）

**服务拆分原则**：
- ✅ **按业务能力进行服务拆分**
- ✅ **每个服务独立数据库**
- ✅ **服务间通过API网关通信**
- ✅ **支持独立部署和扩展**

**服务扩展配置**：
```yaml
# Kubernetes扩展配置
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: consume-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: consume-service
  minReplicas: 2
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## 🔒 安全设计规范（强制执行）

### 21. 接口安全设计（强制执行）

**接口安全要求**：
- ✅ **所有接口必须进行身份认证**
- ✅ **关键接口进行权限校验**
- ✅ **敏感数据传输使用HTTPS**
- ✅ **实现接口防刷和限流**

**安全实现模板**：
```java
@RestController
@RequestMapping("/api/v1/consume")
public class ConsumeController {

    @Resource
    private ConsumeService consumeService;

    @PreAuthorize("hasRole('CONSUME_USER')")
    @RateLimiter(name = "consume-api", fallbackMethod = "consumeRateLimitFallback")
    @PostMapping("/consume")
    public ResponseDTO<ConsumeResultDTO> consume(@Valid @RequestBody ConsumeRequestDTO request) {
        // 数据脱敏处理
        request.setAccount(maskAccount(request.getAccount()));

        ConsumeResultDTO result = consumeService.consume(request);

        // 返回结果脱敏
        result.setAccount(maskAccount(result.getAccount()));

        return ResponseDTO.ok(result);
    }

    // 防刷限流降级
    public ResponseDTO<ConsumeResultDTO> consumeRateLimitFallback(ConsumeRequestDTO request, Exception ex) {
        return ResponseDTO.error("RATE_LIMIT", "请求过于频繁，请稍后重试");
    }

    // 敏感信息脱敏
    private String maskAccount(String account) {
        if (account == null || account.length() <= 4) {
            return "****";
        }
        return account.substring(0, 2) + "****" + account.substring(account.length() - 2);
    }
}
```

### 22. 数据安全治理（强制执行）

**数据安全要求**：
- ✅ **敏感数据加密存储**
- ✅ **数据库连接加密**
- ✅ **操作审计日志记录**
- ✅ **数据备份和恢复策略**

**数据安全实现**：
```java
// ✅ 正确：DataSecurityManager在microservices-common中不使用Spring注解
public class DataSecurityManager {

    private final AESUtil aesUtil;

    // 构造函数注入依赖
    public DataSecurityManager(AESUtil aesUtil) {
        this.aesUtil = aesUtil;
    }

    // 敏感字段加密
    public String encryptSensitiveData(String data) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }
        return aesUtil.encrypt(data);
    }

    // 敏感字段解密
    public String decryptSensitiveData(String encryptedData) {
        if (StringUtils.isEmpty(encryptedData)) {
            return encryptedData;
        }
        return aesUtil.decrypt(encryptedData);
    }

    // 审计日志记录
    @EventListener
    public void recordDataAccess(DataAccessEvent event) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUserId(event.getUserId());
        auditLog.setAction(event.getAction());
        auditLog.setResource(event.getResource());
        auditLog.setIp(event.getClientIp());
        auditLog.setCreateTime(LocalDateTime.now());

        auditLogDao.insert(auditLog);
    }
}
```

---

## 🚨 P0级关键问题清单（基于深度分析结果）

### 🔴 配置安全问题（64个明文密码 - P0级）
**发现严重问题**: 全局扫描发现64个配置文件使用明文密码，存在严重安全风险
**分析依据**: 2025-12-01全局架构深度分析结果，安全维度评分76/100

**立即整改要求**:
- ❌ **禁止配置文件中出现明文密码**（发现64个实例，必须立即整改）
- ❌ **禁止使用环境变量传递敏感配置**（需使用Nacos加密配置）
- ❌ **禁止将数据库连接信息硬编码**（必须通过安全配置中心管理）
- ❌ **禁止缺少配置文件访问权限控制**（需要严格的文件权限管理）

**安全解决方案**:
```yaml
# ❌ 错误示例 - 明文密码
spring:
  datasource:
    password: "123456"  # 禁止！

# ✅ 正确示例 - 加密配置
spring:
  datasource:
    password: "ENC(AES256:encrypted_password_hash)"  # Nacos加密配置
    druid:
      connection-properties: "config.decrypt=true;config.decrypt.key=${nacos.config.key}"
```

### 🔴 分布式追踪缺失（完全缺失 - P0级）
**发现严重问题**: 项目完全缺少分布式追踪实现，无法有效监控服务调用链
**分析依据**: 2025-12-01全局架构深度分析结果，监控维度评分52/100

**立即整改要求**:
- ❌ **禁止缺少分布式追踪实现**（22个微服务都未实现，P0优先级）
- ❌ **禁止服务间调用无链路追踪**（无法定位性能瓶颈和故障点）
- ❌ **禁止缺少Span和Trace标识**（需要完整的调用链路监控）
- ❌ **禁止缺少业务操作追踪**（需要关键业务操作的完整追踪）

**追踪实现要求**:
```java
// ✅ 必须实现的分布式追踪
@RestController
public class ConsumeController {

    @NewSpan(name = "consume-api")
    @PostMapping("/consume")
    public ResponseDTO<ConsumeResultDTO> consume(@Valid @RequestBody ConsumeRequestDTO request) {
        // 自动生成Trace ID和Span ID
        Span span = tracer.nextSpan().name("consume-business").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            return consumeService.consume(request);
        } finally {
            span.end();
        }
    }
}
```

### 🔴 Repository命名违规（96个违规实例 - P0级）
**发现严重问题**: 全项目存在96个@Repository注解使用实例，违反RepoWiki规范
**分析依据**: 2025-12-01全局架构深度分析结果，架构合规性评分81/100

**立即整改要求**:
- ❌ **禁止使用@Repository注解**（发现96个实例，必须全部替换为@Mapper）
- ❌ **禁止使用Repository后缀命名**（所有接口必须使用Dao后缀）
- ❌ **禁止JPA+Repository技术栈**（必须统一使用MyBatis-Plus+DAO）
- ❌ **禁止数据访问层使用Spring Data JPA**（架构违规，必须统一技术栈）

**整改标准模板**:
```java
// ❌ 错误示例 - Repository违规
@Repository  // 禁止使用！
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    // JPA方法 - 禁止！
}

// ✅ 正确示例 - DAO合规
@Mapper  // 必须使用！
public interface AccountDao extends BaseMapper<AccountEntity> {
    // MyBatis-Plus方法 - 符合规范！
}
```

### 🔴 API设计不规范（65%接口滥用POST - P0级）
**发现严重问题**: 65%的REST接口错误使用POST方法，违反RESTful设计原则
**分析依据**: 2025-12-01全局架构深度分析结果，API设计维度评分72/100

**立即整改要求**:
- ❌ **禁止查询接口使用POST方法**（65%接口存在此问题，必须整改）
- ❌ **禁止缺少HTTP状态码语义化**（需要完整的HTTP状态码体系）
- ❌ **禁止接口缺少版本控制**（必须实现API版本管理）
- ❌ **禁止接口设计不符合RESTful规范**（需要系统性重构）

**RESTful设计规范**:
```java
// ❌ 错误示例 - 违反RESTful
@PostMapping("/getUserInfo")  // 查询用POST - 错误！
@PostMapping("/updateUser")   // 更新用POST - 错误！

// ✅ 正确示例 - 符合RESTful
@GetMapping("/v1/users/{userId}")           // 查询用户
@PutMapping("/v1/users/{userId}")           // 更新用户
@DeleteMapping("/v1/users/{userId}")        // 删除用户
@GetMapping("/v1/users")                    // 列表查询（支持分页）
```

---

## 🚫 严格禁止事项清单

### 架构违规
- ❌ 禁止跨层访问（如Controller直接调用DAO）
- ❌ 禁止循环依赖（服务间、组件间）
- ❌ 禁止直接访问其他服务数据库
- ❌ 禁止在Controller中包含业务逻辑
- ❌ 禁止在DAO中包含业务逻辑
- ❌ 禁止破坏四层架构边界

### 代码规范违规
- ❌ 禁止使用 `@Autowired`（统一使用 `@Resource`）
- ❌ 禁止使用 `Repository` 后缀（统一使用 `Dao`）
- ❌ 禁止使用 `@Repository` 注解（统一使用 `@Mapper`）
- ❌ 禁止使用 javax 包名（统一使用 Jakarta）
- ❌ 禁止硬编码配置值（使用配置中心）
- ❌ 禁止状态服务设计（必须无状态）

### 技术选型违规
- ❌ 禁止使用 FeignClient 直接调用（统一通过GatewayServiceClient）
- ❌ 禁止使用 HikariCP 连接池（统一使用 Druid）
- ❌ 禁止使用除 Nacos 外的注册中心
- ❌ 禁止使用除 Redis 外的缓存技术
- ❌ 禁止绕过多级缓存策略

### 性能优化违规
- ❌ 禁止数据库全表扫描
- ❌ 禁止深度分页查询（LIMIT 100000, 20）
- ❌ 禁止不合理的索引使用
- ❌ 禁止缓存雪崩和击穿问题
- ❌ 禁止内存泄漏和资源未释放

### 安全设计违规
- ❌ 禁止明文传输敏感数据
- ❌ 禁止未授权访问接口
- ❌ 禁止SQL注入和XSS攻击
- ❌ 禁止未脱敏的敏感数据输出
- ❌ 禁止缺少审计日志的操作

### 企业级特性违规
- ❌ 禁止缺少降级熔断机制
- ❌ 禁止缺少分布式事务设计
- ❌ 禁止缺少监控告警机制
- ❌ 禁止缺少幂等性设计
- ❌ 禁止缺少异步处理机制

---

## ✅ 合规检查清单

### 代码实现前检查（架构设计阶段）
- [ ] 确认遵循四层架构规范（Controller → Service → Manager → DAO）
- [ ] 确认使用 @Resource 依赖注入
- [ ] 确认使用 Dao 命名规范（@Mapper注解）
- [ ] 确认使用 Jakarta EE 3.0+ 包名
- [ ] 确认服务设计为无状态
- [ ] 确认制定多级缓存策略
- [ ] 确认设计SAGA分布式事务
- [ ] 确认规划降级熔断机制

### 代码实现后检查（代码质量阶段）
- [ ] 无跨层访问问题（Controller不直接调用Manager/DAO）
- [ ] 无 Repository 后缀使用（统一使用Dao）
- [ ] 无 @Repository 注解使用（统一使用@Mapper）
- [ ] 无 @Autowired 使用（统一使用@Resource）
- [ ] 事务注解使用正确（@Transactional配置）
- [ ] 无硬编码配置值（使用Nacos配置中心）
- [ ] 异常处理完善，包含业务异常和系统异常
- [ ] 日志记录合理，包含关键操作日志
- [ ] 参数验证完整（@Valid注解使用）
- [ ] 返回结果统一封装（ResponseDTO）

### 企业级特性检查（高可用阶段）
- [ ] 实现多级缓存架构（L1本地 + L2Redis + L3网关）
- [ ] 实现SAGA分布式事务管理
- [ ] 实现服务降级熔断机制（@CircuitBreaker）
- [ ] 实现异步处理机制（@Async、消息队列）
- [ ] 实现监控告警体系（Micrometer、Prometheus）
- [ ] 实现幂等性设计（防重复提交）
- [ ] 实现接口限流防刷（@RateLimiter）

### 性能优化检查（性能调优阶段）
- [ ] 数据库索引优化（覆盖所有查询条件）
- [ ] SQL查询优化（避免全表扫描、深度分页）
- [ ] 连接池配置优化（Druid参数调优）
- [ ] 缓存性能优化（命中率、击穿防护）
- [ ] JVM参数调优（G1GC、内存配置）
- [ ] 线程池配置优化（合理队列大小）

### 安全设计检查（安全合规阶段）
- [ ] 接口身份认证（JWT Token验证）
- [ ] 接口权限校验（@PreAuthorize注解）
- [ ] 敏感数据加密存储（AES加密）
- [ ] 敏感数据传输加密（HTTPS）
- [ ] 敏感数据脱敏输出（手机号、身份证等）
- [ ] SQL注入防护（参数化查询）
- [ ] XSS攻击防护（输入验证、输出编码）
- [ ] 操作审计日志（数据访问记录）

### 微服务集成检查（服务治理阶段）
- [ ] 服务间调用通过GatewayServiceClient
- [ ] 无跨服务直接数据库访问
- [ ] 使用 Nacos 注册发现中心
- [ ] 端口配置符合标准分配表
- [ ] 服务拆分合理（按业务能力划分）
- [ ] 服务间无循环依赖
- [ ] 服务配置统一管理（Nacos Config）

### 部署运维检查（DevOps阶段）
- [ ] Docker镜像构建规范
- [ ] Kubernetes部署配置完整
- [ ] 健康检查配置（/actuator/health）
- [ ] 日志输出规范（JSON格式、日志级别）
- [ ] 监控指标暴露（/actuator/metrics）
- [ ] 环境变量配置完整
- [ ] 启动脚本优化（JVM参数、内存配置）

### 文档规范检查（知识管理阶段）
- [ ] API接口文档完整（Swagger/Knife4j）
- [ ] 架构设计文档齐全
- [ ] 数据库设计文档清晰
- [ ] 部署运维文档详细
- [ ] 故障排查手册完善
- [ ] 性能测试报告充分

### 质量保障检查（测试阶段）
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试场景完整
- [ ] 接口测试自动化
- [ ] 性能压测达标
- [ ] 安全测试通过
- [ ] 代码质量扫描通过（SonarQube）

---

## 📈 量化改进路线图（基于深度分析结果）

### 🎯 改进目标设定
**现状基准**: 83/100（良好级别）
**目标期望**: 95/100（企业级优秀水平）
**改进幅度**: +12分（14.5%提升）

### ⏰ P0级立即执行（1-2周内完成）
**安全关键问题 - 直接影响生产环境**

1. **配置安全加固**（64个明文密码）
   - **任务**: 使用Nacos加密配置替换所有明文密码
   - **预期改进**: 安全评分从76→95 (+25%)
   - **完成标准**: 0个明文密码，100%加密配置

2. **分布式追踪实现**（22个微服务缺失）
   - **任务**: 实现完整的Spring Cloud Sleuth + Zipkin追踪体系
   - **预期改进**: 监控评分从52→90 (+73%)
   - **完成标准**: 100%服务调用链可追踪

3. **Repository命名整改**（96个违规实例）
   - **任务**: 将所有@Repository替换为@Mapper，统一使用DAO
   - **预期改进**: 架构合规性从81→95 (+17%)
   - **完成标准**: 0个Repository命名违规

4. **RESTful API重构**（65%接口滥用POST）
   - **任务**: 重构所有不符合RESTful规范的接口
   - **预期改进**: API设计评分从72→92 (+28%)
   - **完成标准**: 100%接口符合RESTful规范

### ⚡ P1级快速优化（2-4周内完成）
**性能优化问题 - 直接影响用户体验**

5. **数据库性能优化**（65%查询缺少索引）
   - **任务**: 为所有查询条件添加合适的复合索引
   - **预期改进**: 性能评分从3.2→4.2 (+31%)
   - **量化目标**: 查询响应时间从800ms→150ms

6. **缓存架构优化**（命中率仅65%）
   - **任务**: 实现三级缓存体系，优化缓存策略
   - **预期改进**: 缓存命中率从65%→90% (+38%)
   - **量化目标**: 缓存响应时间从50ms→5ms

7. **连接池统一**（12个服务使用HikariCP）
   - **任务**: 将所有HikariCP替换为Druid连接池
   - **预期改进**: 连接池性能提升40%
   - **量化目标**: 连接利用率从60%→90%

### 🔧 P2级架构完善（1-2个月内完成）
**架构标准化问题 - 长期健康发展**

8. **微服务边界优化**（边界不清，循环依赖）
   - **任务**: 重新梳理微服务边界，消除循环依赖
   - **预期改进**: 架构清晰度提升50%
   - **量化目标**: 服务间调用复杂度降低30%

9. **配置管理统一**（配置不一致）
   - **任务**: 统一所有服务配置管理，建立标准模板
   - **预期改进**: 配置一致性从70%→100%
   - **量化目标**: 配置错误率降低80%

10. **日志标准化**（日志格式不统一）
    - **任务**: 实现统一的日志格式和收集体系
    - **预期改进**: 日志分析效率提升200%
    - **量化目标**: 故障定位时间从60分钟→15分钟

### 📊 预期总体改进效果

**改进前后对比表**:
| 评估维度 | 当前评分 | 目标评分 | 改进幅度 | 优先级 |
|---------|---------|---------|---------|--------|
| **整体架构** | 83/100 | 95/100 | +14.5% | P0 |
| **安全性** | 76/100 | 95/100 | +25% | P0 |
| **性能** | 64/100 | 90/100 | +40% | P1 |
| **监控** | 52/100 | 90/100 | +73% | P0 |
| **API设计** | 72/100 | 92/100 | +28% | P0 |
| **配置管理** | 70/100 | 95/100 | +36% | P1 |
| **合规性** | 81/100 | 98/100 | +21% | P0 |

**业务价值量化**:
- **系统稳定性**: MTBF从48小时→168小时（+250%）
- **开发效率**: 新功能开发周期缩短40%
- **运维成本**: 故障处理时间减少60%
- **用户体验**: 接口响应时间提升70%
- **安全等级**: 从中等风险提升至企业级安全

### 🚀 执行保障机制

**组织保障**:
- **架构委员会**: 每周评审改进进度
- **技术专项**: 成立P0问题攻坚小组
- **质量门禁**: 所有改进必须通过自动化验证

**技术保障**:
- **自动化测试**: 改进前后性能对比测试
- **监控告警**: 实时监控改进效果
- **回滚机制**: 确保改进过程安全可控

**时间保障**:
- **P0任务**: 每日站会跟踪，确保2周内完成
- **P1任务**: 每周评审，确保1个月内完成
- **P2任务**: 双周回顾，确保2个月内完成

---

## 🖥️ 前端与移动端架构规范 (2025-12-02新增)

### 1. 前端项目概览

**项目保持稳定，无需重构**

| 项目名称 | 技术栈 | 端口 | 说明 |
|---------|-------|------|------|
| **smart-admin-web-javascript** | Vue 3.4 + Ant Design Vue 4 + Vite 5 | 3000 | 主管理后台 |
| **microservices/frontend/web-main** | Vue 3.4 + qiankun 2.10 | 3000 | 微前端主应用 |
| **smart-app** | uni-app 3.0 + Vue 3 | - | 移动端应用 |

### 2. 前端技术栈规范

```yaml
# 前端技术栈 (保持不变)
框架: Vue 3.4.x
构建工具: Vite 5.x
状态管理: Pinia 2.x
路由: Vue Router 4.x
UI组件: Ant Design Vue 4.x
HTTP客户端: Axios 1.6.x
图表: ECharts 5.4.x
国际化: Vue I18n 9.x
代码规范: ESLint + Prettier
```

### 3. 移动端技术栈规范

```yaml
# 移动端技术栈 (保持不变)
框架: uni-app 3.0.x
语言: Vue 3.2.x
状态管理: Pinia 2.0.x
UI组件: uni-ui 1.5.x
构建工具: Vite 4.x
样式预处理: Sass 1.69.x

# 支持平台
- H5 (Web)
- 微信小程序
- 支付宝小程序
- iOS App
- Android App
```

### 4. 前后端API契约

**核心原则**: 后端重构不影响前端，API接口保持100%兼容

```javascript
// 前端API调用示例 (保持不变)
// smart-admin-web-javascript/src/api/support/dict-api.js

export const dictApi = {
  // API路径保持不变
  getTypeList() {
    return request.get('/api/v1/dict/type/list');
  },
  
  getDataList(typeCode) {
    return request.get('/api/v1/dict/data/list', { params: { typeCode } });
  }
};
```

### 5. 前端目录结构规范

```
smart-admin-web-javascript/
├── src/
│   ├── api/                    # API接口定义 (按模块组织)
│   │   ├── business/           # 业务模块API
│   │   │   ├── access/         # 门禁
│   │   │   ├── attendance/     # 考勤
│   │   │   ├── consume/        # 消费
│   │   │   └── ...
│   │   ├── system/             # 系统管理API
│   │   └── support/            # 支撑功能API
│   ├── components/             # 公共组件
│   ├── views/                  # 页面组件
│   │   ├── business/           # 业务页面 (151个文件)
│   │   ├── system/             # 系统管理页面 (65个文件)
│   │   └── support/            # 支撑功能页面 (52个文件)
│   ├── store/                  # 状态管理
│   ├── router/                 # 路由配置
│   └── utils/                  # 工具函数
```

### 6. 移动端目录结构规范

```
smart-app/
├── src/
│   ├── api/                    # API接口
│   ├── components/             # 公共组件
│   ├── pages/                  # 页面
│   │   ├── attendance/         # 考勤页面
│   │   ├── biometric/          # 生物识别
│   │   ├── home/               # 首页
│   │   ├── login/              # 登录
│   │   └── mine/               # 个人中心
│   ├── store/                  # 状态管理
│   └── utils/                  # 工具函数
├── manifest.json               # 配置文件
└── pages.json                  # 页面配置
```

---

## 📦 模块职责边界规范 (2025-12-02新增)

### 1. microservices-common (公共JAR库)

**定位**: 纯Java库，被所有微服务依赖

**✅ 允许包含**:
| 类型 | 说明 | 示例 |
|------|------|------|
| Entity | 数据实体 | `UserEntity`, `DepartmentEntity` |
| DAO | 数据访问接口 | `UserDao`, `DepartmentDao` |
| Form | 表单对象 | `UserAddForm`, `UserUpdateForm` |
| VO | 视图对象 | `UserVO`, `UserDetailVO` |
| Manager | 业务编排（纯Java类，不使用Spring注解，通过构造函数注入依赖） | `UserManager`, `DepartmentManager` |
| Config | 配置类 | `MyBatisConfig`, `RedisConfig` |
| Constant | 常量 | `CommonConstant` |
| Enum | 枚举 | `StatusEnum`, `GenderEnum` |
| Exception | 异常 | `BusinessException` |
| Util | 工具类 | `DateUtil`, `StringUtil` |

**Manager类使用说明**：
- Manager类在 `microservices-common` 中是纯Java类，不使用 `@Component` 或 `@Resource`
- Manager类通过构造函数接收依赖（DAO、GatewayServiceClient等）
- 在微服务中，通过配置类将Manager注册为Spring Bean，或由Service层手动创建实例
- 示例配置类：
```java
@Configuration
public class ManagerConfiguration {
    
    @Bean
    public UserManager userManager(UserDao userDao, DepartmentDao departmentDao) {
        return new UserManager(userDao, departmentDao);
    }
}
```

**❌ 禁止包含**:
| 类型 | 原因 |
|------|------|
| @Service实现类 | Service实现应在具体微服务中 |
| @RestController | Controller应在具体微服务中 |
| @Component注解 | Manager类不使用Spring注解，保持为纯Java类 |
| @Resource/@Autowired | Manager类通过构造函数注入依赖，不使用Spring依赖注入 |
| spring-boot-starter-web | 公共库不应依赖Web框架 |
| spring-boot-starter | 公共库不应依赖Spring Boot框架（可依赖spring-core等基础框架） |

### 2. ioedream-common-service (公共业务微服务)

**定位**: Spring Boot微服务，提供公共业务API

**✅ 允许包含**:
| 类型 | 说明 | 示例 |
|------|------|------|
| Controller | REST控制器 | `UserController`, `DictController` |
| Service接口 | 服务接口 | `UserService`, `DictService` |
| ServiceImpl | 服务实现 | `UserServiceImpl`, `DictServiceImpl` |
| 服务配置 | 微服务配置 | `application.yml` |

**核心功能模块**:
- 用户认证与授权 (auth)
- 组织架构管理 (organization)
- 权限管理 (security)
- 字典管理 (dict)
- 菜单管理 (menu)
- 审计日志 (audit)
- 系统配置 (config)
- 通知管理 (notification)
- 任务调度 (scheduler)
- 监控告警 (monitor)
- 文件管理 (file)
- 工作流管理 (workflow)

**Manager类使用方式**：
- Manager类在 `microservices-common` 中是纯Java类，不使用Spring注解
- 在 `ioedream-common-service` 中，通过 `@Configuration` 类将Manager注册为Spring Bean
- Service层通过 `@Resource` 注入Manager实例（由Spring容器管理）
- 示例：
```java
// microservices-common中的Manager（纯Java类）
public class UserManager {
    private final UserDao userDao;
    
    public UserManager(UserDao userDao) {
        this.userDao = userDao;
    }
}

// ioedream-common-service中的配置类
@Configuration
public class ManagerConfig {
    @Bean
    public UserManager userManager(UserDao userDao) {
        return new UserManager(userDao);
    }
}

// Service层使用
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserManager userManager;  // 由Spring容器注入
}
```

### 3. 业务微服务职责

| 微服务 | 端口 | 职责范围 | 依赖公共模块 |
|-------|------|---------|------------|
| ioedream-access-service | 8090 | 门禁控制、通行记录 | ✅ |
| ioedream-attendance-service | 8091 | 考勤打卡、排班管理 | ✅ |
| ioedream-consume-service | 8094 | 消费管理、账户管理 | ✅ |
| ioedream-visitor-service | 8095 | 访客预约、访客登记 | ✅ |
| ioedream-video-service | 8092 | 视频监控、录像回放 | ✅ |
| ioedream-device-comm-service | 8087 | 设备协议、连接管理 | ✅ |

---

## 🏗️ 设备管理架构整合规范 (2025-12-02新增)

### 1. 设备管理统一架构原则

**核心原则**: 设备管理作为横切关注点，必须在公共模块统一实现，禁止重复实现。

#### ✅ 正确架构模式
```
公共模块 (microservices-common):
├── DeviceEntity                    # 统一设备实体
├── CommonDeviceService            # 统一设备管理服务
├── DeviceDao                      # 统一设备数据访问
└── 设备配置类 (4种设备类型)

设备微服务 (ioedream-device-service):
├── DeviceProtocolAdapter         # 协议适配器 (专业化)
├── DeviceConnectionManager       # 连接管理 (专业化)
├── DeviceCommunicationService    # 设备通信 (专业化)
└── DeviceHealthService           # 设备健康监控 (专业化)
```

#### ❌ 禁止的架构模式
```
❌ 重复的SmartDeviceEntity
❌ 重复的设备服务实现
❌ 设备管理逻辑分散在多个微服务
❌ 混用JPA和MyBatis-Plus
❌ Repository违规使用
```

### 2. 设备实体统一标准

**唯一设备实体**: `net.lab1024.sa.common.organization.entity.DeviceEntity`

**数据库表**: `t_common_device` (统一设备表)

**支持的设备类型**:
- `CAMERA` - 摄像头
- `ACCESS` - 门禁设备
- `CONSUME` - 消费机
- `ATTENDANCE` - 考勤机
- `BIOMETRIC` - 生物识别设备
- `INTERCOM` - 对讲机
- `ALARM` - 报警器
- `SENSOR` - 传感器

**扩展字段**: `extendedAttributes` (JSON格式，存储业务特定字段)

### 3. 设备服务调用标准

**业务微服务调用设备管理**:
```java
@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private CommonDeviceService commonDeviceService;  // 使用公共设备服务

    public ResponseDTO<Void> setupAccessDevice(Long deviceId) {
        // 通过公共服务获取设备信息
        DeviceEntity device = commonDeviceService.getById(deviceId);
        // 业务逻辑处理
        return ResponseDTO.ok();
    }
}
```

**设备协议通信**:
```java
@Service
public class DeviceProtocolServiceImpl implements DeviceProtocolService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    public ResponseDTO<String> sendCommand(Long deviceId, String command) {
        // 通过网关调用设备微服务的协议功能
        return gatewayServiceClient.callDeviceService(
            "/api/device/protocol/send",
            HttpMethod.POST,
            deviceCommand,
            String.class
        );
    }
}
```

### 4. 微服务职责边界

| 服务类型 | 职责范围 | 禁止功能 |
|---------|---------|---------|
| **公共模块** | 设备CRUD、设备状态管理、设备配置 | 协议通信、连接管理 |
| **设备微服务** | 协议适配、连接管理、数据采集 | 业务设备管理、CRUD操作 |
| **业务微服务** | 业务逻辑处理 | 直接设备管理、协议通信 |

### 5. 违规检查清单

**代码提交前检查**:
- [ ] 没有创建新的设备实体类
- [ ] 使用CommonDeviceService而非重复服务
- [ ] 设备相关代码在正确模块
- [ ] 没有Repository违规使用
- [ ] 遵循四层架构规范

**持续集成检查**:
- [ ] 扫描重复的设备管理代码
- [ ] 检查设备实体引用
- [ ] 验证微服务调用模式
- [ ] Repository合规性检查

## 🏗️ 区域-设备关联架构规范 (2025-12-08新增)

### 1. 区域设备关联核心概念

**设计目标**: 通过区域与设备的双向关联，串联各个业务场景，实现统一的智慧园区空间管理。

**核心原则**:
- ✅ **区域作为空间概念**: 统一公共区域设置，各业务模块有对应属性
- ✅ **设备区域关联**: 设备部署在具体区域中，支持跨区域服务
- ✅ **业务属性管理**: 设备在区域中有特定的业务属性和配置
- ✅ **权限继承机制**: 通过区域权限控制设备访问权限

### 2. 区域设备关联实体设计

**核心实体**: `net.lab1024.sa.common.organization.entity.AreaDeviceEntity`

**数据库表**: `t_area_device_relation`

**关键字段**:
```java
// 关联标识
@TableId(type = IdType.ASSIGN_ID)
private String relationId;           // 关联ID

// 区域关联
private Long areaId;                 // 区域ID
private String deviceId;             // 设备ID
private String deviceCode;           // 设备编码
private String deviceName;           // 设备名称

// 设备分类
private Integer deviceType;          // 设备类型 (1-门禁 2-考勤 3-消费 4-视频 5-访客)
private Integer deviceSubType;       // 设备子类型
private String businessModule;       // 业务模块 (access/attendance/consume/visitor/video)

// 业务属性
private String businessAttributes;   // 业务属性(JSON格式)
private Integer relationStatus;      // 关联状态 (1-正常 2-维护 3-故障 4-离线 5-停用)
private Integer priority;            // 优先级 (1-主设备 2-辅助设备 3-备用设备)

// 时间控制
private LocalDateTime effectiveTime; // 生效时间
private LocalDateTime expireTime;     // 失效时间
```

### 3. 区域设备管理服务架构

**服务接口**: `net.lab1024.sa.common.organization.service.AreaDeviceManager`

**核心功能**:
- **设备关联管理**: 添加、移除、批量管理区域设备
- **权限控制**: 基于用户权限获取可访问设备
- **业务属性**: 设备在区域中的业务属性管理
- **状态同步**: 设备状态同步到区域关联
- **统计分析**: 区域设备统计和分布分析

**实现类**: `net.lab1024.sa.common.organization.service.impl.AreaDeviceManagerImpl`

**数据访问**: `net.lab1024.sa.common.organization.dao.AreaDeviceDao`

### 4. 业务场景应用模式

#### 4.1 门禁区域设备关联
```java
// 门禁设备部署到区域
areaDeviceManager.addDeviceToArea(
    areaId,           // A栋1楼大厅
    deviceId,         // 门禁控制器DEV001
    deviceCode,       // ACCESS_CTRL_001
    deviceName,       // 主入口门禁
    1,                // 设备类型：门禁设备
    "access"          // 业务模块：门禁管理
);

// 设置门禁业务属性
Map<String, Object> accessAttributes = new HashMap<>();
accessAttributes.put("accessMode", "card");
accessAttributes.put("antiPassback", true);
accessAttributes.put("openTime", 3000);
areaDeviceManager.setDeviceBusinessAttributes(deviceId, areaId, accessAttributes);
```

#### 4.2 考勤区域设备关联
```java
// 考勤设备部署到办公区域
areaDeviceManager.addDeviceToArea(
    officeAreaId,     // 办公区域
    attendanceDeviceId, // 考勤机ATT001
    "ATTEND_001",     // 设备编码
    "办公区考勤机",   // 设备名称
    2,                // 设备类型：考勤设备
    "attendance"      // 业务模块：考勤管理
);
```

#### 4.3 消费区域设备关联
```java
// 消费设备部署到餐厅区域
areaDeviceManager.addDeviceToArea(
    canteenAreaId,     // 餐厅区域
    posDeviceId,       // POS机POS001
    "POS_001",        // 设备编码
    "餐厅POS机",      // 设备名称
    3,                // 设备类型：消费设备
    "consume"         // 业务模块：消费管理
);
```

### 5. 统一区域管理服务集成

**区域统一服务**: `net.lab1024.sa.common.organization.service.AreaUnifiedService`

**集成功能**:
- **区域层级管理**: 支持多级区域结构和权限继承
- **业务属性管理**: 各业务模块在区域中的专属配置
- **设备关联查询**: 通过区域获取关联的设备信息
- **权限验证**: 用户区域权限验证和设备访问控制

```java
// 获取区域的所有设备
List<AreaDeviceEntity> areaDevices = areaDeviceManager.getAreaDevices(areaId);

// 获取区域中指定业务模块的设备
List<AreaDeviceEntity> accessDevices = areaDeviceManager.getAreaDevicesByModule(areaId, "access");

// 获取用户可访问的设备
List<AreaDeviceEntity> userDevices = areaDeviceManager.getUserAccessibleDevices(userId, "access");

// 检查设备是否在区域中
boolean inArea = areaDeviceManager.isDeviceInArea(areaId, deviceId);
```

### 6. 设备业务属性模板

**模板机制**: 为不同设备类型提供标准化的业务属性模板

**支持模板**:
- **门禁设备**: 访问模式、反潜回、胁迫码、开关门时间
- **考勤设备**: 工作模式、位置验证、拍照采集
- **消费设备**: 支付模式、离线模式、小票打印
- **视频设备**: 分辨率、录像模式、AI分析、存储类型

```java
// 获取设备属性模板
Map<String, Object> template = areaDeviceManager.getDeviceAttributeTemplate(1, 11); // 门禁控制器

// 应用模板到设备关联
areaDeviceManager.addDeviceToArea(areaId, deviceId, deviceCode, deviceName, deviceType, businessModule);
```

### 7. 缓存和性能优化

**多级缓存策略**:
- **L1本地缓存**: 设备关联关系缓存(30分钟)
- **L2 Redis缓存**: 分布式缓存支持
- **L3数据库**: 持久化存储

**缓存键规范**:
```
area:device:area:{areaId}              # 区域设备列表
area:device:area:{areaId}:type:{type}   # 区域指定类型设备
area:device:area:{areaId}:module:{module} # 区域业务模块设备
area:device:user:{userId}:devices       # 用户可访问设备
```

### 8. 业务场景串联示例

#### 8.1 用户进门场景
```
用户刷卡 → 区域设备关联查询 → 权限验证 → 门禁控制 → 记录生成 → 视频联动
    ↓           ↓              ↓         ↓         ↓         ↓
  刷卡设备    查找区域关联    验证区域权限  控制门禁   通行记录   关联摄像头
```

#### 8.2 考勤打卡场景
```
用户打卡 → 区域定位 → 设备验证 → 考勤记录 → 数据统计 → 异常检测
    ↓        ↓        ↓        ↓        ↓        ↓
  考勤机   确定办公区域  验证权限  记录打卡  汇总统计  异常告警
```

#### 8.3 消费结算场景
```
用户消费 → 区域验证 → 账户检查 → 支付处理 → 记录生成 → 通知推送
    ↓        ↓        ↓        ↓        ↓        ↓
  POS机   验证消费区域  检查余额  扣款支付  消费记录   消费通知
```

### 9. 规范检查清单

**代码实现检查**:
- [ ] 使用AreaDeviceEntity进行区域设备关联
- [ ] 通过AreaDeviceManager管理设备关联关系
- [ ] 遵循四层架构规范(Controller→Service→Manager→DAO)
- [ ] 使用@Mapper注解而非@Repository
- [ ] 设备业务属性使用JSON格式存储

**业务逻辑检查**:
- [ ] 区域权限验证机制完整
- [ ] 设备状态同步机制正确
- [ ] 缓存策略合理有效
- [ ] 业务属性模板标准化
- [ ] 跨业务场景串联支持

**性能优化检查**:
- [ ] 多级缓存策略实施
- [ ] 数据库查询优化
- [ ] 批量操作支持
- [ ] 异步处理机制
- [ ] 监控指标完善

---

## 📝 详细开发规范 (2025-12-02新增)

### 1. Java编码规范详解

#### 1.1 类命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| Entity | `XxxEntity` | `UserEntity`, `DepartmentEntity` |
| DAO | `XxxDao` + `@Mapper` | `UserDao`, `DepartmentDao` |
| Service接口 | `XxxService` | `UserService`, `DictService` |
| Service实现 | `XxxServiceImpl` | `UserServiceImpl`, `DictServiceImpl` |
| Manager | `XxxManager` | `UserManager`, `DictManager` |
| Controller | `XxxController` | `UserController`, `DictController` |
| Form | `XxxAddForm`, `XxxUpdateForm`, `XxxQueryForm` | `UserAddForm`, `UserUpdateForm` |
| VO | `XxxVO`, `XxxDetailVO`, `XxxListVO` | `UserVO`, `UserDetailVO` |

#### 1.2 包结构规范

```java
// microservices-common 包结构
net.lab1024.sa.common.{module}/
├── entity/          // 实体类
├── dao/             // 数据访问层
├── manager/         // 业务编排层
├── service/         // 服务接口
│   └── impl/        // 服务实现
├── domain/
│   ├── form/        // 表单对象
│   └── vo/          // 视图对象
└── config/          // 配置类

// ioedream-common-service 包结构
net.lab1024.sa.common.{module}/
└── controller/      // 控制器
```

#### 1.3 注解使用规范

```java
// ✅ 正确的Entity注解
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted_flag")
    private Boolean deletedFlag;
    
    @Version
    private Integer version;
}

// ✅ 正确的DAO注解
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    // 使用LambdaQueryWrapper进行查询
}

// ✅ 正确的Manager类（在microservices-common中，不使用Spring注解）
// Manager类通过构造函数注入依赖，保持为纯Java类
public class UserManager {
    
    private final UserDao userDao;
    private final DepartmentDao departmentDao;
    
    // 构造函数注入依赖
    public UserManager(UserDao userDao, DepartmentDao departmentDao) {
        this.userDao = userDao;
        this.departmentDao = departmentDao;
    }
    
    // 业务方法
    public UserEntity getUserWithDepartment(Long userId) {
        // 复杂业务逻辑
        return userDao.selectById(userId);
    }
}

// ✅ 正确的Service注解（在微服务中）
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private UserManager userManager;  // 由配置类注册为Spring Bean
}

// ✅ 正确的Controller注解
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理")
public class UserController {
    
    @Resource
    private UserService userService;
}
```

#### 1.4 方法命名规范

| 操作类型 | 命名规范 | 示例 |
|---------|---------|------|
| 新增 | `add`, `create`, `insert` | `addUser()`, `createDepartment()` |
| 删除 | `delete`, `remove` | `deleteUser()`, `removeDepartment()` |
| 更新 | `update`, `modify` | `updateUser()`, `modifyDepartment()` |
| 查询单个 | `get`, `query`, `find` | `getUserById()`, `findByUsername()` |
| 查询列表 | `list`, `queryList`, `findAll` | `listUsers()`, `queryAllDepartments()` |
| 分页查询 | `page`, `queryPage` | `pageUsers()`, `queryPageDepartments()` |
| 统计 | `count`, `statistics` | `countUsers()`, `statisticsAttendance()` |

#### 1.5 实体类设计规范（2025-12-04新增）

**黄金法则**：
- ✅ Entity≤200行（理想标准）
- ⚠️ Entity≤400行（可接受上限）
- ❌ Entity>400行（必须拆分）

**设计原则**：
1. **纯数据模型**: Entity只包含数据字段，不包含业务逻辑
2. **合理字段数**: 建议≤30个字段，超过需考虑拆分
3. **单一职责**: 一个Entity对应一个核心业务概念
4. **关联设计**: 复杂关系使用@OneToOne、@OneToMany

**禁止事项**：
- ❌ 禁止在Entity中包含业务计算逻辑
- ❌ 禁止Entity超过400行
- ❌ 禁止在Entity中包含static方法（工具方法）
- ❌ 禁止Entity包含过多的瞬态字段（@TableField(exist = false)）

**拆分策略**：

```java
// ❌ 错误示例：超大Entity包含80+字段（772行）
@Data
@TableName("t_work_shift")
public class WorkShiftEntity {
    // 基础信息 (10字段)
    // 工作时间 (15字段)  
    // 弹性时间 (12字段)
    // 加班规则 (10字段)
    // 休息规则 (8字段)
    // 午休规则 (6字段)
    // 考勤规则 (12字段)
    // 节假日规则 (8字段)
    // ... 共80+字段，772行
    
    // ❌ 业务逻辑不应在Entity中
    public BigDecimal calculateOvertimePay() {
        return overtimeHours.multiply(overtimeRate);
    }
}

// ✅ 正确示例：拆分为多个Entity

// 1. 核心Entity - 只包含基础信息（约120行）
@Data
@TableName("t_work_shift")
public class WorkShiftEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long shiftId;
    
    @NotBlank(message = "班次名称不能为空")
    @Size(max = 100)
    @TableField("shift_name")
    private String shiftName;
    
    @TableField("shift_type")
    private Integer shiftType; // 1-固定 2-弹性 3-轮班
    
    @NotNull
    @TableField("work_start_time")
    private LocalTime workStartTime;
    
    @NotNull
    @TableField("work_end_time")
    private LocalTime workEndTime;
    
    // 基础审计字段
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deletedFlag;
}

// 2. 规则配置Entity（约150行）
@Data
@TableName("t_work_shift_rule")
public class WorkShiftRuleEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long ruleId;
    
    @TableField("shift_id")
    private Long shiftId; // 外键关联
    
    // 弹性时间规则
    @TableField("flexible_enabled")
    private Integer flexibleEnabled;
    
    @TableField("flexible_start_time")
    private LocalTime flexibleStartTime;
    
    // 加班规则
    @TableField("overtime_enabled")
    private Integer overtimeEnabled;
    
    @TableField("overtime_rate")
    private BigDecimal overtimeRate;
    
    // ... 其他规则字段
}

// 3. Manager层组装数据
@Component
public class WorkShiftManager {
    @Resource
    private WorkShiftDao workShiftDao;
    @Resource
    private WorkShiftRuleDao workShiftRuleDao;
    
    /**
     * 获取完整班次信息
     */
    public WorkShiftFullVO getFullWorkShift(Long shiftId) {
        WorkShiftEntity shift = workShiftDao.selectById(shiftId);
        WorkShiftRuleEntity rule = workShiftRuleDao.selectByShiftId(shiftId);
        
        return WorkShiftFullVO.builder()
            .shift(shift)
            .rule(rule)
            .build();
    }
    
    /**
     * 计算加班费（业务逻辑在Manager层）
     */
    public BigDecimal calculateOvertimePay(Long shiftId, BigDecimal overtimeHours) {
        WorkShiftRuleEntity rule = workShiftRuleDao.selectByShiftId(shiftId);
        return overtimeHours.multiply(rule.getOvertimeRate());
    }
}
```

**注释优化规范**：

```java
// ❌ 冗余注释：每个字段占用8-10行
@NotBlank(message = "班次名称不能为空")
@Size(max = 100, message = "班次名称长度不能超过100个字符")
@TableField("shift_name")
@Schema(description = "班次名称", example = "正常班")
private String shiftName;

// ✅ 优化注释：合并注解，保留核心信息（占用3-4行）
@TableField("shift_name") @Schema(description = "班次名称")
@NotBlank @Size(max = 100)
private String shiftName;
```

**实体类检查清单**：
- [ ] Entity行数≤200行（理想）或≤400行（上限）
- [ ] 字段数≤30个
- [ ] 无业务逻辑方法
- [ ] 无static工具方法
- [ ] 合理使用@TableField
- [ ] 完整的审计字段（createTime, updateTime, deletedFlag）
- [ ] 合理使用Lombok注解

### 2. API设计规范详解

#### 2.1 RESTful API规范

```yaml
# URL设计规范
基础路径: /api/v1/{module}

# HTTP方法语义
GET:    查询资源 (幂等)
POST:   创建资源
PUT:    全量更新资源
PATCH:  部分更新资源
DELETE: 删除资源

# 示例
GET    /api/v1/users           # 获取用户列表
GET    /api/v1/users/{id}      # 获取单个用户
POST   /api/v1/users           # 创建用户
PUT    /api/v1/users/{id}      # 更新用户
DELETE /api/v1/users/{id}      # 删除用户
GET    /api/v1/users/{id}/roles  # 获取用户角色
```

#### 2.2 请求响应规范

```java
// ✅ 统一响应格式
@Data
public class ResponseDTO<T> {
    private Integer code;        // 业务状态码
    private String message;      // 提示信息
    private T data;              // 响应数据
    private Long timestamp;      // 时间戳
    
    public static <T> ResponseDTO<T> ok(T data) {
        return new ResponseDTO<>(200, "success", data, System.currentTimeMillis());
    }
    
    public static <T> ResponseDTO<T> error(String code, String message) {
        return new ResponseDTO<>(Integer.parseInt(code), message, null, System.currentTimeMillis());
    }
}

// ✅ 分页响应格式
@Data
public class PageResult<T> {
    private List<T> list;        // 数据列表
    private Long total;          // 总记录数
    private Integer pageNum;     // 当前页码
    private Integer pageSize;    // 每页大小
    private Integer pages;       // 总页数
}
```

#### 2.3 错误码规范

| 错误码范围 | 类型 | 示例 |
|-----------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、未授权、禁止访问 |
| 500-599 | 服务端错误 | 服务器内部错误 |
| 1000-1999 | 业务通用错误 | 数据不存在、重复操作 |
| 2000-2999 | 用户模块错误 | 用户名已存在、密码错误 |
| 3000-3999 | 权限模块错误 | 无权限、角色不存在 |
| 4000-4999 | 业务模块错误 | 门禁/考勤/消费等业务错误 |

### 3. 数据库设计规范详解

#### 3.1 表命名规范

| 类型 | 前缀 | 示例 |
|------|------|------|
| 公共表 | `t_common_` | `t_common_user`, `t_common_department` |
| 门禁表 | `t_access_` | `t_access_record`, `t_access_device` |
| 考勤表 | `t_attendance_` | `t_attendance_record`, `t_attendance_shift` |
| 消费表 | `t_consume_` | `t_consume_record`, `t_consume_account` |
| 访客表 | `t_visitor_` | `t_visitor_record`, `t_visitor_appointment` |
| 视频表 | `t_video_` | `t_video_device`, `t_video_record` |
| 设备表 | `t_device_` | `t_device_info`, `t_device_protocol` |

#### 3.2 字段命名规范

```sql
-- ✅ 标准字段命名
id                  BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 主键
create_time         DATETIME NOT NULL,                   -- 创建时间
update_time         DATETIME NOT NULL,                   -- 更新时间
create_user_id      BIGINT,                              -- 创建人ID
update_user_id      BIGINT,                              -- 更新人ID
deleted_flag        TINYINT DEFAULT 0,                   -- 删除标记 0-未删除 1-已删除
version             INT DEFAULT 0,                       -- 乐观锁版本号
status              TINYINT DEFAULT 1,                   -- 状态 1-启用 0-禁用
remark              VARCHAR(500),                        -- 备注

-- ✅ 外键字段命名
user_id             BIGINT NOT NULL,                     -- 用户ID
department_id       BIGINT NOT NULL,                     -- 部门ID
role_id             BIGINT NOT NULL,                     -- 角色ID
```

#### 3.3 索引设计规范

```sql
-- ✅ 索引命名规范
-- 主键索引: pk_{表名}
-- 唯一索引: uk_{表名}_{字段名}
-- 普通索引: idx_{表名}_{字段名}
-- 联合索引: idx_{表名}_{字段1}_{字段2}

-- ✅ 索引设计示例
CREATE TABLE t_common_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    department_id BIGINT,
    status TINYINT DEFAULT 1,
    create_time DATETIME NOT NULL,
    
    -- 唯一索引
    UNIQUE INDEX uk_user_username (username),
    UNIQUE INDEX uk_user_phone (phone),
    
    -- 普通索引
    INDEX idx_user_department (department_id),
    INDEX idx_user_status (status),
    
    -- 联合索引 (覆盖常用查询条件)
    INDEX idx_user_dept_status_time (department_id, status, create_time)
);
```

### 4. 日志规范详解

#### 4.1 日志级别使用

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| ERROR | 系统错误、异常捕获 | 数据库连接失败、第三方服务调用失败 |
| WARN | 警告信息、潜在问题 | 参数异常、重试操作 |
| INFO | 业务关键节点 | 用户登录、订单创建、支付成功 |
| DEBUG | 调试信息 | 方法入参、中间计算结果 |
| TRACE | 详细追踪 | 循环迭代、详细流程 |

#### 4.2 日志格式规范

```java
// ✅ 正确的日志记录
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Override
    public ResponseDTO<UserVO> getUserById(Long userId) {
        log.info("[用户查询] 开始查询用户, userId={}", userId);
        
        try {
            UserEntity user = userDao.selectById(userId);
            if (user == null) {
                log.warn("[用户查询] 用户不存在, userId={}", userId);
                return ResponseDTO.error("USER_NOT_FOUND", "用户不存在");
            }
            
            log.info("[用户查询] 查询成功, userId={}, username={}", userId, user.getUsername());
            return ResponseDTO.ok(convertToVO(user));
            
        } catch (Exception e) {
            log.error("[用户查询] 查询异常, userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException("USER_QUERY_ERROR", "查询用户失败");
        }
    }
}

// ❌ 错误的日志记录
log.info("查询用户" + userId);  // 字符串拼接
log.debug("user: " + user.toString());  // 可能NPE
log.error("error");  // 信息不足
```

### 5. 异常处理规范详解

#### 5.1 异常分类

```java
// ✅ 业务异常 (可预期)
public class BusinessException extends RuntimeException {
    private String code;
    private String message;
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}

// ✅ 系统异常 (不可预期)
public class SystemException extends RuntimeException {
    private String code;
    private String message;
    
    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
```

#### 5.2 全局异常处理

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // 业务异常处理
    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }
    
    // 参数验证异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[参数验证异常] message={}", message);
        return ResponseDTO.error("VALIDATION_ERROR", message);
    }
    
    // 系统异常处理
    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("[系统异常] error={}", e.getMessage(), e);
        return ResponseDTO.error("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
```

### 6. 单元测试规范详解

#### 6.1 测试类命名

| 测试类型 | 命名规范 | 示例 |
|---------|---------|------|
| 单元测试 | `XxxTest` | `UserServiceTest`, `UserDaoTest` |
| 集成测试 | `XxxIntegrationTest` | `UserControllerIntegrationTest` |
| 性能测试 | `XxxPerformanceTest` | `UserServicePerformanceTest` |

#### 6.2 测试方法命名

```java
// ✅ 测试方法命名规范: test_{方法名}_{场景}_{预期结果}
@Test
void test_getUserById_userExists_returnUserVO() {
    // given
    Long userId = 1L;
    UserEntity mockUser = createMockUser(userId);
    when(userDao.selectById(userId)).thenReturn(mockUser);
    
    // when
    ResponseDTO<UserVO> result = userService.getUserById(userId);
    
    // then
    assertNotNull(result);
    assertEquals(200, result.getCode());
    assertNotNull(result.getData());
    assertEquals(userId, result.getData().getId());
}

@Test
void test_getUserById_userNotExists_returnError() {
    // given
    Long userId = 999L;
    when(userDao.selectById(userId)).thenReturn(null);
    
    // when
    ResponseDTO<UserVO> result = userService.getUserById(userId);
    
    // then
    assertNotNull(result);
    assertEquals("USER_NOT_FOUND", result.getCode().toString());
}
```

#### 6.3 测试覆盖率要求

| 模块类型 | 最低覆盖率 | 目标覆盖率 |
|---------|-----------|-----------|
| Service层 | 80% | 90% |
| Manager层 | 75% | 85% |
| DAO层 | 70% | 80% |
| Controller层 | 60% | 75% |
| 工具类 | 90% | 95% |

---

## 🔗 相关文档参考

### 📋 核心规范文档
- **🏆 本规范**: [CLAUDE.md - 全局架构标准](./CLAUDE.md) - **最高架构规范**
- [OpenSpec工作流程](@/openspec/AGENTS.md)
- [微服务统一规范](./microservices/UNIFIED_MICROSERVICES_STANDARDS.md)

### 🏗️ 架构实施指导
- [📖 消费模块实施指南](./microservices/ioedream-consume-service/CONSUME_MODULE_IMPLEMENTATION_GUIDE.md)
- [🎯 OpenSpec消费模块提案](./openspec/changes/complete-consume-module-implementation/)
- [📐 四层架构详解](./documentation/technical/四层架构详解.md)
- [🔄 SmartAdmin开发规范](./documentation/technical/SmartAdmin规范体系_v4/)

### 📚 技术专题文档
- [📦 RepoWiki编码规范](./documentation/technical/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md)
- [🛡️ 安全体系规范](./documentation/technical/repowiki/zh/content/安全体系/)
- [📊 数据库设计规范](./documentation/technical/repowiki/zh/content/后端架构/数据模型与ORM/)
- [⚡ 缓存架构设计](./documentation/architecture/archive/cache-architecture-unification/)

### 🎯 企业级特性指导
- [🔥 SAGA分布式事务设计](./documentation/technical/分布式事务设计指南.md)
- [⚙️ 服务降级熔断指南](./documentation/technical/服务容错设计指南.md)
- [📈 监控告警体系建设](./documentation/technical/监控体系建设指南.md)
- [🚀 性能优化最佳实践](./documentation/technical/性能优化最佳实践.md)

### 🔧 部署运维文档
- [🐳 Docker部署指南](./documentation/technical/Docker部署指南.md)
- [☸️ Kubernetes部署指南](./documentation/technical/Kubernetes部署指南.md)
- [🔧 CI/CD流水线配置](./documentation/technical/CI-CD配置指南.md)
- [📊 监控运维手册](./documentation/technical/监控运维手册.md)

---

## 📞 规范执行支持

### 🎯 架构委员会
- **首席架构师**: 负责规范制定和架构决策
- **技术专家**: 各领域技术专家（数据库、缓存、安全等）
- **质量保障**: 代码质量和架构合规性检查

### 📋 规范更新流程
1. **需求收集**: 收集团队反馈和技术发展需求
2. **草案制定**: 架构委员会制定规范草案
3. **团队评审**: 各开发团队评审和提供反馈
4. **版本发布**: 正式发布新版本规范
5. **培训推广**: 团队培训和规范推广

### ⚡ 快速支持渠道
- **架构咨询**: 架构委员会技术咨询
- **规范答疑**: 定期规范答疑会议
- **最佳实践**: 技术最佳实践分享
- **问题反馈**: 规范问题反馈渠道

---

**👥 制定人**: IOE-DREAM 架构委员会
**🏗️ 技术架构师**: SmartAdmin 核心团队
**✅ 最终解释权**: IOE-DREAM 项目架构委员会
**📅 版本**: v2.0.0 - 企业级增强版

## 🔨 构建顺序强制标准（2025-12-05新增）

### 🚨 黄金法则（强制执行）

> **microservices-common 必须在任何业务服务构建之前完成构建和安装**

**违反此规则将导致**:
- ❌ 依赖解析失败（`The import net.lab1024.sa.common.device cannot be resolved`）
- ❌ IDE无法识别类（`DeviceEntity cannot be resolved to a type`）
- ❌ 编译错误（200+ 错误）
- ❌ 构建失败

### 📋 强制构建顺序

```
1. microservices-common          ← 必须先构建（P0级）
   ↓
2. ioedream-gateway-service      ← 基础设施服务
   ↓
3. ioedream-common-service       ← 公共业务服务
   ↓
4. ioedream-device-comm-service  ← 设备通讯服务
   ↓
5. ioedream-oa-service          ← OA服务
   ↓
6. 业务服务（可并行构建）
   ├── ioedream-access-service
   ├── ioedream-attendance-service
   ├── ioedream-video-service
   ├── ioedream-consume-service
   └── ioedream-visitor-service
```

### 🔧 标准构建方法（强制执行）

#### ✅ 方法1: 使用统一构建脚本（推荐）

```powershell
# 构建所有服务（自动确保顺序）
.\scripts\build-all.ps1

# 构建指定服务（自动先构建common）
.\scripts\build-all.ps1 -Service ioedream-access-service

# 清理并构建
.\scripts\build-all.ps1 -Clean

# 跳过测试
.\scripts\build-all.ps1 -SkipTests
```

#### ✅ 方法2: Maven命令（手动）

```powershell
# 步骤1: 强制先构建 common（必须）
mvn clean install -pl microservices/microservices-common -am -DskipTests

# 步骤2: 构建业务服务
mvn clean install -pl microservices/ioedream-access-service -am -DskipTests
```

**关键参数说明**:
- `-pl`: 指定要构建的模块
- `-am`: also-make，同时构建依赖的模块
- `install`: 必须使用install而非compile，确保JAR安装到本地仓库

### ❌ 禁止事项

```powershell
# ❌ 禁止：直接构建业务服务（跳过common）
mvn clean install -pl microservices/ioedream-access-service

# ❌ 禁止：只编译不安装
mvn clean compile -pl microservices/microservices-common

# ❌ 禁止：跳过common构建检查
mvn clean install -rf microservices/ioedream-access-service
```

### 🔍 构建后验证

```powershell
# 检查JAR文件是否存在
Test-Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

# 检查关键类是否存在
jar -tf "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar" | Select-String "DeviceEntity"
```

### 📚 详细文档

- **构建顺序强制标准**: [BUILD_ORDER_MANDATORY_STANDARD.md](./documentation/technical/BUILD_ORDER_MANDATORY_STANDARD.md)
- **构建脚本**: [scripts/build-all.ps1](./scripts/build-all.ps1)
- **预构建检查**: [scripts/pre-build-check.ps1](./scripts/pre-build-check.ps1)

---

## 🔧 架构修复与合规性保障（2025-01-30新增）

### ⚠️ 重要原则：禁止自动修改代码

**核心原则**:
- ❌ **禁止使用脚本自动修改代码**
- ❌ **禁止使用正则表达式批量替换**
- ✅ **所有修复必须手动完成**
- ✅ **确保代码质量和全局一致性**

### 架构违规检查

**检查脚本**（仅检查，不修改）:
```powershell
# 检查架构违规并生成修复报告
.\scripts\fix-architecture-violations.ps1

# 架构合规性检查
.\scripts\architecture-compliance-check.ps1
```

**检查范围**:
- ✅ 检查@Autowired违规（114个实例）
- ✅ 检查@Repository违规（78个实例）
- ✅ 检查Repository命名违规（4个实例）
- ✅ 生成详细修复报告

**手动修复流程**:
1. 运行检查脚本生成报告
2. 查看修复报告了解需要修复的文件
3. 使用IDE逐个文件手动修复
4. 参考手动修复指南确保规范
5. 验证修复后提交代码

### 架构合规性检查

**检查项**:
- ✅ @Autowired使用检查
- ✅ @Repository使用检查
- ✅ Repository命名规范检查
- ✅ 四层架构边界检查
- ✅ 跨层访问检查

**集成点**:
- Git pre-commit钩子
- CI/CD构建流程
- PR合并前强制检查

### 相关文档

- **全局深度分析**: [GLOBAL_DEEP_ANALYSIS_ROOT_CAUSE_SOLUTION.md](./documentation/technical/GLOBAL_DEEP_ANALYSIS_ROOT_CAUSE_SOLUTION.md)
- **执行计划**: [ARCHITECTURE_FIX_EXECUTION_PLAN.md](./documentation/technical/ARCHITECTURE_FIX_EXECUTION_PLAN.md)
- **手动修复指南**: [MANUAL_FIX_GUIDE.md](./documentation/technical/MANUAL_FIX_GUIDE.md)
- **检查脚本**: [scripts/fix-architecture-violations.ps1](./scripts/fix-architecture-violations.ps1)（仅检查，不修改）
- **合规性检查**: [scripts/architecture-compliance-check.ps1](./scripts/architecture-compliance-check.ps1)

---

## 🚨 重要提醒

⚠️ **本规范为项目唯一架构规范，所有开发人员必须严格遵循**

- ✅ **强制执行**: 任何违反本规范的代码都将被拒绝合并
- ✅ **架构审查**: 所有重要模块必须通过架构委员会审查
- ✅ **构建顺序**: 必须严格遵循构建顺序，违反将导致构建失败
- ✅ **架构合规**: 必须通过架构合规性检查，违规代码禁止合并
- ✅ **持续优化**: 根据技术发展和项目实践持续优化规范
- ✅ **团队协作**: 遵循规范是团队协作的基础和保障

**让我们一起构建高质量、高可用、高性能的IOE-DREAM智能管理系统！** 🚀