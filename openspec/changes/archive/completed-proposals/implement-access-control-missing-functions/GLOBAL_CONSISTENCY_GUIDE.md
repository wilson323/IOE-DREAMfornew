# 全局一致性确保指南

## 1. 架构一致性确保

### 1.1 四层架构强制执行
```
Controller层 → Service层 → Manager层 → DAO层
     ↓            ↓           ↓        ↓
  参数验证    业务逻辑处理  复杂流程编排  数据访问
  统一响应    异常处理    规则引擎     数据持久化
  权限检查    事务管理    外部服务调用  SQL优化
```

**检查清单：**
- [ ] Controller层只能调用Service层，禁止直接调用Manager或DAO
- [ ] Service层只能调用Manager层，禁止直接调用DAO
- [ ] Manager层负责复杂业务流程编排，可调用多个Service和DAO
- [ ] DAO层只负责数据访问，不包含业务逻辑
- [ ] 所有跨层调用必须通过接口进行

### 1.2 命名规范统一
```
实体类：XXXEntity
表单类：XXXForm
视图类：XXXVO
查询类：XXXQuery / XXXQueryForm
服务接口：XXXService
服务实现：XXXServiceImpl
管理器：XXXManager
数据访问：XXXDao / XXXRepository
控制器：XXXController
```

### 1.3 包名规范统一
```
net.lab1024.sa.access.controller/
net.lab1024.sa.access.service/
net.lab1024.sa.access.service.impl/
net.lab1024.sa.access.manager/
net.lab1024.sa.access.domain.entity/
net.lab1024.sa.access.domain.form/
net.lab1024.sa.access.domain.vo/
net.lab1024.sa.access.domain.query/
net.lab1024.sa.access.repository/
```

## 2. 技术栈一致性确保

### 2.1 依赖注入规范
```java
// ✅ 统一使用@Resource注解
@Resource
private SomeService someService;

// ❌ 禁止使用@Autowired或构造函数注入
@Autowired  // 禁止
private SomeService someService;
```

### 2.2 包名规范严格检查
```java
// ✅ 使用jakarta包名
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;

// ❌ 禁止使用javax包名
import javax.annotation.Resource;     // 禁止
import javax.validation.Valid;       // 禁止
import javax.persistence.Entity;     // 禁止
```

### 2.3 代码风格统一
- 缩进：4个空格
- 行长：不超过120字符
- 方法长度：不超过50行
- 类长度：不超过500行
- 圈复杂度：不超过10

## 3. 数据库设计一致性

### 3.1 表设计规范
```sql
-- 表名：小写字母+下划线
CREATE TABLE access_approval_process

-- 字段名：小写字母+下划线
process_id BIGINT PRIMARY KEY AUTO_INCREMENT,
process_type VARCHAR(50) NOT NULL,
applicant_id BIGINT NOT NULL,
status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### 3.2 索引设计规范
```sql
-- 单字段索引：idx_字段名
CREATE INDEX idx_applicant_id ON access_approval_process(applicant_id);

-- 复合索引：idx_字段1_字段2（按查询频率排序）
CREATE INDEX idx_applicant_status ON access_approval_process(applicant_id, status);

-- 唯一索引：uk_字段名
CREATE UNIQUE INDEX uk_process_no ON access_approval_process(process_no);
```

### 3.3 数据类型规范
- 主键：BIGINT AUTO_INCREMENT
- 外键：BIGINT
- 状态：VARCHAR(20)
- 时间：TIMESTAMP
- 金额：DECIMAL(18,2)
- 描述：TEXT

## 4. API设计一致性

### 4.1 RESTful规范
```
GET    /api/access/approval/processes     # 查询列表
GET    /api/access/approval/processes/{id} # 查询详情
POST   /api/access/approval/processes     # 创建
PUT    /api/access/approval/processes/{id} # 更新
DELETE /api/access/approval/processes/{id} # 删除
```

### 4.2 统一响应格式
```java
{
  "code": 200,           // 状态码：200成功，其他失败
  "message": "操作成功",   // 响应消息
  "data": {}            // 响应数据
}
```

### 4.3 分页参数规范
```java
// 统一分页参数
@RequestParam(defaultValue = "1") Integer pageNum,
@RequestParam(defaultValue = "20") Integer pageSize

// 统一分页结果
{
  "pageNum": 1,
  "pageSize": 20,
  "total": 100,
  "pages": 5,
  "list": []
}
```

## 5. 异常处理一致性

### 5.1 异常分类
```java
// 业务异常
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException

// 参数验证异常
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException

// 权限异常
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionException extends RuntimeException

// 系统异常
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SystemException extends RuntimeException
```

### 5.2 异常处理规范
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseDTO.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<String> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseDTO.error("系统异常，请稍后重试");
    }
}
```

## 6. 日志规范一致性

### 6.1 日志级别使用
```java
log.debug("调试信息");    // 详细调试信息
log.info("普通信息");     // 重要业务流程
log.warn("警告信息");     // 预期异常情况
log.error("错误信息");    // 系统异常和错误
```

### 6.2 日志格式规范
```java
// 包含关键业务信息
log.info("开始处理审批申请，流程ID: {}, 申请人ID: {}", processId, applicantId);

// 异常日志包含堆栈信息
log.error("处理审批申请失败，流程ID: {}", processId, e);

// 敏感信息脱敏
log.info("用户登录成功，用户名: {}, IP: {}", maskUsername(username), maskIp(ip));
```

## 7. 测试规范一致性

### 7.1 单元测试规范
```java
@ExtendWith(MockitoExtension.class)
class SomeServiceTest {

    @Mock
    private SomeDao someDao;

    @InjectMocks
    private SomeServiceImpl someService;

    @Test
    void testSomeMethod_Success() {
        // Given - 准备测试数据
        // When - 执行测试方法
        // Then - 验证结果
    }
}
```

### 7.2 测试覆盖率要求
- Controller层：100%覆盖率
- Service层：≥90%覆盖率
- Manager层：≥80%覆盖率
- 整体项目：≥80%覆盖率

## 8. 配置管理一致性

### 8.1 配置文件规范
```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: ioedream-access-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ioedream_access?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
```

### 8.2 环境配置分离
- application.yml：通用配置
- application-dev.yml：开发环境配置
- application-test.yml：测试环境配置
- application-prod.yml：生产环境配置

## 9. 缓存策略一致性

### 9.1 缓存命名规范
```java
// 缓存Key格式：业务:模块:标识
@Cacheable(value = "access:approval:process", key = "#processId")
public ApprovalProcessVO getProcess(Long processId) {
}

@Cacheable(value = "access:user:permissions", key = "#userId")
public List<Permission> getUserPermissions(Long userId) {
}
```

### 9.2 缓存过期策略
- 配置数据：1小时
- 用户权限：30分钟
- 热点数据：5分钟
- 统计数据：10分钟

## 10. 安全规范一致性

### 10.1 权限检查规范
```java
@SaCheckPermission("access:approval:query")
@GetMapping("/processes")
public ResponseDTO<List<ApprovalProcessVO>> queryProcesses() {
}

@RequireResource("access:approval:process:{processId}")
@GetMapping("/processes/{processId}")
public ResponseDTO<ApprovalProcessVO> getProcess(@PathVariable Long processId) {
}
```

### 10.2 数据加密规范
```java
// 敏感数据加密存储
@Entity
public class UserInfoEntity {
    private String encryptedIdCard;  // 加密的身份证号
    private String encryptedPhone;   // 加密的手机号
}
```

## 11. 性能优化一致性

### 11.1 数据库查询优化
- 禁止SELECT *
- 合理使用索引
- 避免N+1查询
- 使用批量操作

### 11.2 内存使用优化
- 及时释放大对象
- 合理使用缓存
- 避免内存泄漏
- 控制集合大小

## 12. 代码审查检查清单

### 12.1 架构规范检查
- [ ] 严格遵循四层架构
- [ ] 没有跨层访问
- [ ] 正确使用@Resource注解
- [ ] 使用jakarta包名

### 12.2 代码质量检查
- [ ] 方法长度 ≤ 50行
- [ ] 类长度 ≤ 500行
- [ ] 圈复杂度 ≤ 10
- [ ] 代码注释完整

### 12.3 性能检查
- [ ] 数据库查询使用索引
- [ ] 避免N+1查询问题
- [ ] 合理使用缓存
- [ ] 批量操作优化

### 12.4 安全检查
- [ ] SQL注入防护
- [ ] XSS攻击防护
- [ ] 权限检查完整
- [ ] 敏感数据加密

### 12.5 测试检查
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试完整
- [ ] 性能测试通过
- [ ] 安全测试通过

## 13. 持续集成要求

### 13.1 代码提交要求
- 代码必须通过编译检查
- 单元测试必须通过
- 代码覆盖率必须达标
- 代码规范检查必须通过

### 13.2 部署要求
- 所有环境配置统一
- 数据库迁移脚本完整
- 健康检查接口完善
- 监控告警配置完整

通过严格执行以上全局一致性确保指南，可以确保整个门禁微服务项目的代码质量、架构一致性和系统稳定性，为项目的长期维护和扩展奠定坚实基础。