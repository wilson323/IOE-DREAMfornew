# IOE-DREAM 技能文档更新清单

**版本**: v1.0.0
**创建日期**: 2025-12-15
**更新范围**: .claude/skills/ 目录下26个技能文档
**目标**: 技术栈标准化、格式统一、内容完善

---

## 📋 更新优先级和时间表

### 🔴 P0级更新（严重问题 - 1周内完成）

#### 1. common-service-specialist.md
**问题类型**: P0禁止项
**具体问题**:
- ❌ 第427行: 使用@Autowired注解
- ❌ 第438行: 使用@Repository注解
- ❌ 第440行: 使用JPA技术栈

**更新内容**:
```markdown
### ❌ 当前问题代码
@Autowired  // 禁止使用@Autowired
private UserService userService;

@Repository  // 禁止使用@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 禁止使用Repository和JPA
}

### ✅ 标准化修复代码
@Resource  // 必须使用@Resource
private UserService userService;

@Mapper  // 必须使用@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    // 使用MyBatis-Plus
}
```

**技术栈补充**:
```markdown
## 🛠️ 技术栈要求

### ✅ 推荐技术栈
- **Spring Boot**: 3.5.8+
- **Java**: 17 LTS
- **Jakarta EE**: 3.0+
- **MyBatis-Plus**: 3.5.15+
- **依赖注入**: @Resource (强制)
- **DAO层**: @Mapper + Dao后缀 (强制)

### ❌ 禁止技术栈
- **@Autowired**: 严格禁止
- **@Repository**: 严格禁止
- **javax包名**: 严格禁止
- **JPA**: 严格禁止
```

#### 2. device-comm-service-specialist.md
**问题类型**: 技术栈描述缺失
**更新内容**:
```markdown
## 🛠️ 技术栈要求

### ✅ 推荐技术栈
- **Spring Boot**: 3.5.8+
- **Spring Cloud**: 2023.0.4+
- **Netty**: 4.1.100+
- **Protocol Buffers**: 3.24+
- **依赖注入**: @Resource (强制)
- **通信协议**: WebSocket + TCP/UDP

### ❌ 禁止技术栈
- **@Autowired**: 严格禁止
- **阻塞式IO**: 禁止阻塞通信
- **硬编码端口**: 严格禁止
```

#### 3. consume-service-specialist.md
**问题类型**: Jakarta EE缺失
**更新内容**:
```markdown
### Jakarta EE包名规范
```java
// ✅ 正确的Jakarta EE使用
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;

// ❌ 禁止的javax包名
import javax.annotation.Resource;  // 禁止
import javax.validation.Valid;     // 禁止
```
```

#### 4. oa-service-specialist.md
**问题类型**: 技术栈描述缺失
**更新内容**: 同device-comm-service-specialist.md格式

#### 5. gateway-service-specialist.md
**问题类型**: 架构规范缺失
**更新内容**:
```markdown
## 🏗️ 四层架构要求

### Controller层
- ✅ 使用@RestController
- ✅ 使用@Resource依赖注入
- ✅ 统一ResponseDTO响应格式

### Service层
- ✅ 使用@Service注解
- ✅ 使用@Transactional事务管理
- ✅ 调用Manager层进行复杂编排

### Manager层
- ✅ 纯Java类，不使用Spring注解
- ✅ 构造函数注入依赖
- ✅ 复杂业务流程编排

### DAO层
- ✅ 使用@Mapper注解
- ✅ 继承BaseMapper<Entity>
- ✅ 使用Dao后缀命名
```

---

### 🟡 P1级更新（重要问题 - 2周内完成）

#### 1. access-service-specialist.md
**问题类型**: 技术栈不完整
**更新内容**:
```markdown
## 🔧 设备通讯技术栈

### 核心技术
- **Spring Boot**: 3.5.8+ (设备服务框架)
- **Netty**: 4.1.100+ (高性能网络通信)
- **Protocol Buffers**: 3.24+ (设备协议序列化)
- **WebSocket**: 长连接通讯
- **MQTT**: IoT设备消息协议

### 设备协议支持
- **门禁控制器**: TCP/IP协议
- **考勤设备**: WebSocket + REST API
- **消费终端**: HTTPS + JSON
- **监控摄像头**: RTSP + HTTP API
```

#### 2. attendance-service-specialist.md
**问题类型**: 版本信息缺失
**更新内容**:
```markdown
### 版本要求
- **Spring Boot**: 3.5.8+ (强制)
- **MyBatis-Plus**: 3.5.15+ (强制)
- **Quartz**: 2.3.2+ (任务调度)
- **EasyExcel**: 3.3.4+ (Excel处理)
- **Hutool**: 5.8.36+ (工具库)
```

#### 3. video-service-specialist.md
**问题类型**: 技术栈不统一
**更新内容**:
```markdown
## 🎥 视频技术栈标准化

### 媒体处理
- **FFmpeg**: 6.0+ (视频处理)
- **OpenCV**: 4.8+ (图像分析)
- **GStreamer**: 1.22+ (流媒体)
- **WebRTC**: 实时视频通讯

### 存储和流媒体
- **RTMP**: 实时消息协议
- **HLS**: HTTP Live Streaming
- **WebRTC**: 浏览器实时通讯
- **RTSP**: 实时流协议
```

#### 4. visitor-service-specialist.md
**问题类型**: 最佳实践缺失
**更新内容**:
```markdown
## 🎯 访客管理最佳实践

### 访客流程设计
1. **预约管理**: 在线预约 + 审批流程
2. **身份验证**: 人脸识别 + 证件核验
3. **权限发放**: 临时门禁 + 时间控制
4. **访问监控**: 实时定位 + 异常告警
5. **访问结束**: 权限回收 + 访问报告

### 安全最佳实践
- 访客信息加密存储
- 人脸特征脱敏处理
- 访问日志完整记录
- 异常行为实时检测
```

---

### 🟢 P2级更新（一般问题 - 3周内完成）

#### 1. biometric-architecture-specialist.md
**问题类型**: 格式不统一
**更新内容**:
```markdown
# 生物识别架构专家技能

**🎯 技能定位**: IOE-DREAM智慧园区生物识别架构专家
**⚡ 技能等级**: ★★★★★★ (顶级专家)
**🎯 适用场景**: 生物识别架构设计、多模态认证、算法集成
**📊 技能覆盖**: 人脸识别 | 指纹识别 | 虹膜识别 | 声纹识别 | 多模态融合
```

#### 2. code-quality-protector.md
**问题类型**: 示例代码更新
**更新内容**:
```java
// ✅ 标准化代码示例
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理")
public class UserController {

    @Resource  // 使用Resource注解
    private UserService userService;

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    public ResponseDTO<UserVO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        return ResponseDTO.ok(userService.createUser(request));
    }
}
```

#### 3. database-version-manager.md
**问题类型**: 技术栈引用
**更新内容**:
```markdown
## 🛠️ 数据库技术栈

### 推荐技术栈
- **MySQL**: 8.0+ (主数据库)
- **Redis**: 7.2+ (缓存数据库)
- **Flyway**: 9.0+ (数据库版本管理)
- **Druid**: 1.2.20+ (数据库连接池)

### 禁用技术栈
- **HikariCP**: 严格禁止
- **JPA/Hibernate**: 严格禁止
- **自动建表**: 严格禁止
```

#### 4. powershell-script-generator.md
**问题类型**: 技术栈关联
**更新内容**:
```markdown
## 🔧 PowerShell与IOE-DREAM技术栈集成

### 技术栈关联
- **PowerShell**: 7.3+ (脚本执行环境)
- **Spring Boot**: 3.5.8+ (应用服务)
- **Docker**: 24.0+ (容器化部署)
- **Kubernetes**: 1.28+ (容器编排)

### 集成最佳实践
- PowerShell脚本调用Spring Boot管理端点
- 自动化部署与CI/CD集成
- 容器化环境下的脚本适配
```

---

## 🔍 更新检查清单

### 文档格式检查
- [ ] 标题格式统一 (技能名称 + 英文对照)
- [ ] 技能等级统一 (★★★★★★ 顶级专家)
- [ ] 技术栈声明完整
- [ ] 禁止项明确标注
- [ ] 代码示例标准化

### 技术栈内容检查
- [ ] Spring Boot版本要求明确 (3.5.8+)
- [ ] Java版本要求明确 (17 LTS)
- [ ] Jakarta EE包名规范
- [ ] 依赖注入规范 (@Resource)
- [ ] DAO层规范 (@Mapper + Dao后缀)

### 架构规范检查
- [ ] 四层架构要求
- [ ] 微服务调用规范
- [ ] 缓存策略规范
- [ ] 配置管理规范
- [ ] 安全规范要求

### 代码示例检查
- [ ] 所有示例使用@Resource
- [ ] 所有DAO使用@Mapper
- [ ] 所有包名使用jakarta
- [ ] 架构分层正确
- [ ] 最佳实践体现

---

## 📊 更新进度跟踪

### 当前状态统计
| 文档类别 | 总数 | 已更新 | 进行中 | 待开始 | 完成率 |
|---------|------|--------|--------|--------|--------|
| **P0级文档** | 5 | 0 | 0 | 5 | 0% |
| **P1级文档** | 4 | 0 | 0 | 4 | 0% |
| **P2级文档** | 4 | 0 | 0 | 4 | 0% |
| **其他文档** | 13 | 0 | 0 | 13 | 0% |
| **总计** | 26 | 0 | 0 | 26 | 0% |

### 里程碑目标
- **第一周**: P0级文档全部更新完成
- **第二周**: P1级文档全部更新完成
- **第三周**: P2级文档全部更新完成
- **第四周**: 其他文档检查和优化

---

## 🛠️ 更新工具和脚本

### 自动化检查脚本
```powershell
# 使用技术栈一致性检查脚本
.\scripts\technology-stack-consistency-check.ps1 -SkillsPath .claude\skills -Report -Verbose

# 生成更新报告
.\scripts\skills-update-progress.ps1
```

### 批量更新脚本
```powershell
# 批量添加技术栈声明
.\scripts\add-technology-stack-section.ps1 -SkillsPath .claude\skills

# 批量修复@Autowired问题
.\scripts\fix-autowired-issues.ps1 -SkillsPath .claude\skills
```

### 质量检查脚本
```powershell
# 检查文档格式一致性
.\scripts\check-document-format.ps1 -SkillsPath .claude\skills

# 验证代码示例规范
.\scripts\validate-code-examples.ps1 -SkillsPath .claude\skills
```

---

## 📞 支持和联系

### 更新支持团队
- **技术栈专家**: 负责技术栈标准制定和问题解答
- **文档维护团队**: 负责文档格式和内容质量
- **质量保障团队**: 负责更新质量检查和验证

### 问题反馈渠道
- **更新问题**: 在项目issue中提交"技能文档更新"标签
- **技术栈问题**: 联系技术栈专家
- **格式问题**: 联系文档维护团队

### 更新完成后验证
1. 运行技术栈一致性检查脚本
2. 验证所有文档格式统一
3. 检查代码示例规范
4. 测试技能文档加载
5. 生成最终更新报告

---

## 🎯 总结

本次技能文档更新涉及26个文档，分为3个优先级：

1. **P0级 (5个)**: 严重技术栈违规，1周内完成
2. **P1级 (4个)**: 重要技术栈缺失，2周内完成
3. **P2级 (4个)**: 一般格式问题，3周内完成
4. **其他 (13个)**: 检查和优化，4周内完成

**预期成果**:
- ✅ 所有技能文档技术栈标准化
- ✅ 代码示例完全符合规范
- ✅ 文档格式统一专业
- ✅ 自动化检查工具完备

**让我们一起建设规范、高质量的技能文档体系！** 🚀

---
**清单版本**: v1.0.0
**创建时间**: 2025-12-15
**预计完成**: 2026-01-15
**维护团队**: IOE-DREAM架构委员会