# 门禁微服务开发规范和注意事项

## 1. 代码架构规范

### 1.1 四层架构严格执行
```java
// ✅ 正确示例：严格遵循四层架构
@Controller
public class AccessApprovalController {
    @Resource
    private AccessApprovalService approvalService; // 只依赖Service层

    @PostMapping("/apply")
    public ResponseDTO<String> submitApplication(@Valid @RequestBody ApprovalApplicationForm form) {
        // Controller层只做参数验证和调用Service
        return approvalService.submitApplication(form);
    }
}

@Service
public class AccessApprovalServiceImpl implements AccessApprovalService {
    @Resource
    private ApprovalProcessManager processManager; // 只依赖Manager层

    public ResponseDTO<String> submitApplication(ApprovalApplicationForm form) {
        // Service层处理业务逻辑，不直接操作数据库
        return processManager.processApplication(form);
    }
}

@Component
public class ApprovalProcessManager {
    @Resource
    private ApprovalProcessDao approvalProcessDao; // 只依赖DAO层

    public ApprovalResult processApplication(ApprovalApplicationForm form) {
        // Manager层处理复杂业务流程编排
        // 可以调用多个Service和DAO
    }
}

@Repository
public interface ApprovalProcessDao {
    // DAO层只负责数据访问
    int insert(ApprovalProcessEntity entity);
}
```

### 1.2 禁止跨层访问
```java
// ❌ 错误示例：Controller直接调用Manager或DAO
@Controller
public class BadController {
    @Resource
    private ApprovalProcessDao approvalProcessDao; // 禁止！

    @Resource
    private ApprovalProcessManager processManager; // 禁止！
}

// ❌ 错误示例：Service直接调用DAO
@Service
public class BadService {
    @Resource
    private ApprovalProcessDao approvalProcessDao; // 禁止！
}
```

### 1.3 依赖注入规范
```java
// ✅ 正确：使用@Resource注解
@Component
public class CorrectClass {
    @Resource
    private SomeService someService;

    @Resource
    private SomeDao someDao;
}

// ❌ 错误：使用@Autowired或构造函数注入
@Component
public class WrongClass {
    @Autowired
    private SomeService someService; // 禁止！

    private final SomeDao someDao;

    public WrongClass(SomeDao someDao) { // 禁止！
        this.someDao = someDao;
    }
}
```

## 2. 包名和类名规范

### 2.1 严格使用Jakarta包名
```java
// ✅ 正确：使用jakarta包名
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletRequest;
import @Data
@TableName("table_name");
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

// ❌ 错误：使用javax包名
import javax.annotation.Resource; // 禁止！
import javax.validation.Valid; // 禁止！
import javax.servlet.http.HttpServletRequest; // 禁止！
import javax.persistence.Entity; // 禁止！
```

### 2.2 实体类命名规范
```java
// ✅ 正确：实体类以Entity结尾
@Entity
@Table(name = "access_approval_process")
public class ApprovalProcessEntity {
    private Long processId;
    private String processType;
    private String status;
}

// ✅ 正确：表单类以Form结尾
public class ApprovalApplicationForm {
    @NotNull(message = "申请人不能为空")
    private Long applicantId;

    @NotBlank(message = "申请类型不能为空")
    private String applicationType;
}

// ✅ 正确：VO类以VO结尾
public class ApprovalStatusVO {
    private Long processId;
    private String status;
    private String statusName;
}

// ✅ 正确：查询类以Query或QueryForm结尾
public class ApprovalQueryForm {
    private String applicantName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

## 3. 数据库设计规范

### 3.1 表设计规范
```sql
-- ✅ 正确：表名使用下划线分隔，全小写
CREATE TABLE access_approval_process (
    process_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流程ID',
    process_type VARCHAR(50) NOT NULL COMMENT '流程类型',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_applicant_status (applicant_id, status),
    INDEX idx_status_time (status, created_time)
) COMMENT '审批流程表';
```

### 3.2 字段命名规范
- 使用下划线分隔的小写命名
- 主键统一使用 `id` 或 `表名_id`
- 时间字段统一使用 `created_time`, `updated_time`
- 状态字段使用 `status`
- 标识字段使用 `is_` 前缀

### 3.3 索引设计规范
```sql
-- ✅ 正确：为常用查询条件创建索引
-- 单字段索引
CREATE INDEX idx_applicant_id ON access_approval_process(applicant_id);
CREATE INDEX idx_status ON access_approval_process(status);

-- 复合索引：根据查询频率排序
CREATE INDEX idx_applicant_status ON access_approval_process(applicant_id, status);
CREATE INDEX idx_status_time ON access_approval_process(status, created_time);

-- 唯一索引
CREATE UNIQUE INDEX uk_process_no ON access_approval_process(process_no);
```

## 4. API设计规范

### 4.1 RESTful API规范
```java
// ✅ 正确：RESTful风格API
@RestController
@RequestMapping("/api/access/approval")
public class AccessApprovalController {

    @GetMapping("/processes")                    // 查询列表
    public ResponseDTO<PageResult<ApprovalProcessVO>> queryProcesses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            ApprovalQueryForm queryForm) {
    }

    @GetMapping("/processes/{processId}")        // 查询详情
    public ResponseDTO<ApprovalProcessVO> getProcess(
            @PathVariable Long processId) {
    }

    @PostMapping("/processes")                   // 创建
    public ResponseDTO<Long> createProcess(
            @Valid @RequestBody ApprovalApplicationForm form) {
    }

    @PutMapping("/processes/{processId}")        // 更新
    public ResponseDTO<String> updateProcess(
            @PathVariable Long processId,
            @Valid @RequestBody ApprovalUpdateForm form) {
    }

    @DeleteMapping("/processes/{processId}")     // 删除
    public ResponseDTO<String> deleteProcess(
            @PathVariable Long processId) {
    }
}
```

### 4.2 统一响应格式
```java
// ✅ 正确：统一使用ResponseDTO
public class ResponseDTO<T> {
    private Integer code;        // 状态码：200成功，其他失败
    private String message;     // 响应消息
    private T data;            // 响应数据

    // 静态方法
    public static <T> ResponseDTO<T> ok(T data) {
        return ResponseDTO.ok(data, "操作成功");
    }

    public static <T> ResponseDTO<T> ok(T data, String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static ResponseDTO<String> error(String message) {
        ResponseDTO<String> response = new ResponseDTO<>();
        response.setCode(500);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}
```

### 4.3 参数验证规范
```java
// ✅ 正确：完整的参数验证
public class ApprovalApplicationForm {

    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;

    @NotBlank(message = "申请类型不能为空")
    @Size(max = 50, message = "申请类型长度不能超过50个字符")
    private String applicationType;

    @NotBlank(message = "申请内容不能为空")
    @Size(max = 1000, message = "申请内容长度不能超过1000个字符")
    private String applicationContent;

    @NotNull(message = "申请区域不能为空")
    private Long areaId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
```

## 5. 业务逻辑规范

### 5.1 事务管理
```java
// ✅ 正确：使用@Transactional注解
@Service
public class AccessApprovalServiceImpl implements AccessApprovalService {

    @Transactional(rollbackFor = Exception.class)  // 指定回滚异常
    public ResponseDTO<String> submitApplication(ApprovalApplicationForm form) {
        // 业务逻辑处理
        // 异常时会自动回滚
    }

    @Transactional(readOnly = true)  // 只读事务
    public ResponseDTO<PageResult<ApprovalProcessVO>> queryProcesses(
            ApprovalQueryForm queryForm) {
        // 查询逻辑
    }
}
```

### 5.2 异常处理
```java
// ✅ 正确：统一的异常处理
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseDTO<String> handleValidationException(ValidationException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        return ResponseDTO.error(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<String> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return ResponseDTO.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<String> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseDTO.error("系统异常，请稍后重试");
    }
}
```

### 5.3 缓存使用规范
```java
// ✅ 正确：合理使用缓存
@Service
public class AccessConfigService {

    @Cacheable(value = "access:config", key = "#configKey", unless = "#result == null")
    public String getConfigValue(String configKey) {
        // 从数据库读取配置
        return configDao.getValueByKey(configKey);
    }

    @CacheEvict(value = "access:config", key = "#configKey")
    public void updateConfig(String configKey, String configValue) {
        // 更新配置并清除缓存
        configDao.updateValue(configKey, configValue);
    }

    @Cacheable(value = "access:user:permissions", key = "#userId")
    public List<Permission> getUserPermissions(Long userId) {
        // 获取用户权限（缓存）
        return permissionDao.findByUserId(userId);
    }
}
```

## 6. 安全规范

### 6.1 权限控制
```java
// ✅ 正确：使用权限注解
@RestController
@RequestMapping("/api/access/approval")
public class AccessApprovalController {

    @SaCheckPermission("access:approval:query")  // 权限检查
    @GetMapping("/processes")
    public ResponseDTO<PageResult<ApprovalProcessVO>> queryProcesses() {
        // 查询逻辑
    }

    @SaCheckPermission("access:approval:create")
    @PostMapping("/processes")
    public ResponseDTO<Long> createProcess(@Valid @RequestBody ApprovalApplicationForm form) {
        // 创建逻辑
    }

    @RequireResource("access:approval:process:{processId}")  // 资源权限检查
    @GetMapping("/processes/{processId}")
    public ResponseDTO<ApprovalProcessVO> getProcess(@PathVariable Long processId) {
        // 查询详情逻辑
    }
}
```

### 6.2 数据安全
```java
// ✅ 正确：敏感数据加密
@Component
public class DataEncryptionService {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    public String encryptSensitiveData(String data) {
        // 对敏感数据进行AES加密
        return AESUtil.encrypt(data, encryptionKey);
    }

    public String decryptSensitiveData(String encryptedData) {
        // 解密敏感数据
        return AESUtil.decrypt(encryptedData, encryptionKey);
    }
}

@Service
public class UserInfoService {

    @Resource
    private DataEncryptionService encryptionService;

    public void saveUserInfo(UserInfoForm form) {
        UserInfoEntity entity = new UserInfoEntity();
        entity.setIdCardNumber(encryptionService.encryptSensitiveData(form.getIdCardNumber()));
        entity.setPhoneNumber(encryptionService.encryptSensitiveData(form.getPhoneNumber()));
        userInfoDao.insert(entity);
    }
}
```

### 6.3 SQL注入防护
```java
// ✅ 正确：使用参数化查询
@Repository
public interface ApprovalProcessDao {

    // 使用MyBatis参数化查询
    @Select("SELECT * FROM access_approval_process WHERE applicant_id = #{applicantId} AND status = #{status}")
    List<ApprovalProcessEntity> findByApplicantAndStatus(@Param("applicantId") Long applicantId,
                                                       @Param("status") String status);

    // 避免动态SQL拼接
    @Select("<script>" +
            "SELECT * FROM access_approval_process WHERE 1=1 " +
            "<if test='applicantId != null'> AND applicant_id = #{applicantId} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "<if test='startTime != null'> AND created_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND created_time &lt;= #{endTime} </if>" +
            "ORDER BY created_time DESC" +
            "</script>")
    List<ApprovalProcessEntity> findByConditions(@Param("applicantId") Long applicantId,
                                               @Param("status") String status,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
```

## 7. 性能优化规范

### 7.1 数据库查询优化
```java
// ✅ 正确：避免N+1查询问题
@Service
public class ApprovalProcessServiceImpl implements ApprovalProcessService {

    @Resource
    private ApprovalProcessDao approvalProcessDao;

    @Resource
    private ApprovalStepDao approvalStepDao;

    public ApprovalProcessVO getProcessWithSteps(Long processId) {
        // ❌ 错误：会导致N+1查询问题
        /*
        ApprovalProcessEntity process = approvalProcessDao.selectById(processId);
        List<ApprovalStepEntity> steps = approvalStepDao.findByProcessId(processId); // N+1问题
        */

        // ✅ 正确：使用联表查询或批量查询
        ApprovalProcessVO processVO = approvalProcessDao.selectProcessWithSteps(processId);
        return processVO;
    }
}
```

### 7.2 批量操作优化
```java
// ✅ 正确：使用批量操作
@Service
public class DeviceStatusServiceImpl implements DeviceStatusService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Transactional
    public void batchUpdateDeviceStatus(List<DeviceStatusUpdate> updates) {
        // ✅ 正确：使用批量操作
        List<AccessDeviceEntity> devices = updates.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        accessDeviceDao.batchUpdateStatus(devices);

        // ❌ 错误：循环单条更新
        /*
        for (DeviceStatusUpdate update : updates) {
            accessDeviceDao.updateStatus(update.getDeviceId(), update.getStatus());
        }
        */
    }
}
```

### 7.3 缓存策略
```java
// ✅ 正确：合理的缓存策略
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionDao permissionDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存热点数据
    @Cacheable(value = "permission:user", key = "#userId", unless = "#result.isEmpty()")
    public List<Permission> getUserPermissions(Long userId) {
        return permissionDao.findByUserId(userId);
    }

    // 缓存计算结果
    public boolean hasPermission(Long userId, String resource) {
        String cacheKey = "permission:check:" + userId + ":" + resource;
        Boolean cachedResult = (Boolean) redisTemplate.opsForValue().get(cacheKey);

        if (cachedResult != null) {
            return cachedResult;
        }

        boolean hasPermission = checkPermissionFromDB(userId, resource);
        redisTemplate.opsForValue().set(cacheKey, hasPermission, Duration.ofMinutes(5));
        return hasPermission;
    }
}
```

## 8. 测试规范

### 8.1 单元测试规范
```java
// ✅ 正确：完整的单元测试
@ExtendWith(MockitoExtension.class)
class ApprovalProcessServiceImplTest {

    @Mock
    private ApprovalProcessDao approvalProcessDao;

    @Mock
    private ApprovalProcessManager approvalProcessManager;

    @InjectMocks
    private ApprovalProcessServiceImpl approvalProcessService;

    @Test
    void testSubmitApplication_Success() {
        // Given
        ApprovalApplicationForm form = new ApprovalApplicationForm();
        form.setApplicantId(1L);
        form.setApplicationType("PERMISSION_APPLY");

        ApprovalProcessEntity expectedProcess = new ApprovalProcessEntity();
        expectedProcess.setProcessId(1L);

        when(approvalProcessManager.processApplication(form)).thenReturn(expectedProcess);

        // When
        ResponseDTO<Long> result = approvalProcessService.submitApplication(form);

        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1L);
        verify(approvalProcessManager, times(1)).processApplication(form);
    }

    @Test
    void testSubmitApplication_ValidationFailed() {
        // Given
        ApprovalApplicationForm form = new ApprovalApplicationForm();
        // 设置无效数据

        // When & Then
        assertThrows(ValidationException.class, () -> {
            approvalProcessService.submitApplication(form);
        });
    }
}
```

### 8.2 集成测试规范
```java
// ✅ 正确：集成测试
@SpringBootTest
@Transactional
@Rollback
class ApprovalProcessIntegrationTest {

    @Resource
    private ApprovalProcessService approvalProcessService;

    @Resource
    private ApprovalProcessDao approvalProcessDao;

    @Test
    void testSubmitApplication_Integration() {
        // Given
        ApprovalApplicationForm form = new ApprovalApplicationForm();
        form.setApplicantId(1L);
        form.setApplicationType("PERMISSION_APPLY");
        form.setApplicationContent("申请访问办公区");

        // When
        ResponseDTO<Long> result = approvalProcessService.submitApplication(form);

        // Then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();

        // 验证数据库中的数据
        ApprovalProcessEntity process = approvalProcessDao.selectById(result.getData());
        assertThat(process).isNotNull();
        assertThat(process.getApplicantId()).isEqualTo(1L);
        assertThat(process.getProcessType()).isEqualTo("PERMISSION_APPLY");
    }
}
```

## 9. 日志规范

### 9.1 日志级别使用
```java
// ✅ 正确：合理使用日志级别
@Slf4j
@Service
public class ApprovalProcessServiceImpl implements ApprovalProcessService {

    public ResponseDTO<Long> submitApplication(ApprovalApplicationForm form) {
        log.info("开始提交审批申请，申请人ID: {}, 申请类型: {}", form.getApplicantId(), form.getApplicationType());

        try {
            // 业务逻辑处理
            ApprovalProcessEntity process = processApplication(form);

            log.info("审批申请提交成功，流程ID: {}", process.getProcessId());
            return ResponseDTO.ok(process.getProcessId());

        } catch (BusinessException e) {
            log.warn("审批申请提交失败，申请人ID: {}, 失败原因: {}", form.getApplicantId(), e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("审批申请提交异常，申请人ID: {}", form.getApplicantId(), e);
            throw new BusinessException("系统异常，请稍后重试");
        }
    }
}
```

### 9.2 敏感信息处理
```java
// ✅ 正确：敏感信息脱敏
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public void createUser(UserCreateForm form) {
        // ❌ 错误：直接记录敏感信息
        // log.info("创建用户，身份证号: {}, 手机号: {}", form.getIdCard(), form.getPhone());

        // ✅ 正确：敏感信息脱敏
        log.info("创建用户，身份证号: {}, 手机号: {}",
                maskIdCard(form.getIdCard()),
                maskPhone(form.getPhone()));
    }

    private String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard) || idCard.length() < 8) {
            return "****";
        }
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }

    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 11) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
```

## 10. 代码质量检查清单

### 10.1 提交前检查
- [ ] 代码符合四层架构规范
- [ ] 使用@Resource注解进行依赖注入
- [ ] 使用jakarta包名而不是javax
- [ ] 所有公共方法都有完整的JavaDoc注释
- [ ] 所有输入参数都进行了验证
- [ ] 所有数据库操作都有事务注解
- [ ] 所有API接口都有权限检查
- [ ] 敏感信息都有加密处理
- [ ] 异常都有适当的处理和日志记录
- [ ] 单元测试覆盖率 ≥ 80%

### 10.2 性能检查
- [ ] 数据库查询都使用了索引
- [ ] 避免了N+1查询问题
- [ ] 大数据量操作使用了批量处理
- [ ] 合理使用了缓存
- [ ] 避免了内存泄漏

### 10.3 安全检查
- [ ] SQL注入防护
- [ ] XSS攻击防护
- [ ] CSRF攻击防护
- [ ] 敏感数据加密
- [ ] 权限检查完整
- [ ] 审计日志记录

### 10.4 可维护性检查
- [ ] 代码结构清晰
- [ ] 命名规范统一
- [ ] 注释完整准确
- [ ] 配置外部化
- [ ] 错误处理完善
- [ ] 日志记录合理

通过严格遵循以上开发规范，可以确保门禁微服务的代码质量、安全性和可维护性，为系统的长期稳定运行奠定基础。