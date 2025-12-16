# IOE-DREAM Entity设计规范

**版本**: v1.0.0
**生效日期**: 2025-12-15
**适用范围**: IOE-DREAM智慧园区一卡通管理平台所有Entity
**规范定位**: 企业级Entity设计标准，确保数据模型的一致性和可维护性

---

## 📋 核心设计原则

### 1. 架构合规原则

**严格遵循四层架构**: Controller → Service → Manager → DAO
- Entity位于数据访问层，职责单一
- 禁止在Entity中包含业务逻辑
- 禁止跨层访问，确保架构边界清晰

### 2. 命名规范原则

#### 2.1 Entity命名
- **统一后缀**: 所有Entity使用`XxxEntity`后缀
- **驼峰命名**: 使用驼峰命名法，首字母大写
- **业务导向**: 名称清晰表达业务概念

```java
// ✅ 正确示例
public class DeviceEntity extends BaseEntity
public class AttendanceRecordEntity extends BaseEntity
public class ConsumeTransactionEntity extends BaseEntity

// ❌ 错误示例
public class Device            // 缺少Entity后缀
public class deviceRecord     // 未遵循驼峰命名
public class CRM_Device       // 业务概念不清晰
```

#### 2.2 数据库表命名
- **统一前缀**: `t_模块名_`
- **小写下划线**: 全部小写，使用下划线分隔
- **业务导向**: 表名清晰表达业务含义

```sql
-- ✅ 正确示例
t_common_device          -- 公共设备表
t_consume_transaction    -- 消费交易表
t_attendance_record      -- 考勤记录表
t_access_record          -- 门禁记录表
t_visitor_appointment    -- 访客预约表
t_video_device           -- 视频设备表

-- ❌ 错误示例
device                   -- 缺少前缀
ConsumeTransaction      -- 大小写混合
POSID_TRANSACTION        -- 表名不一致
```

### 3. 继承设计原则

#### 3.1 强制继承BaseEntity
所有Entity必须继承BaseEntity，获取标准审计字段：

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_example")
public class ExampleEntity extends BaseEntity {
    // 业务字段
    private String businessField;

    // 自动继承以下审计字段：
    // - Long createUserId
    // - LocalDateTime createTime
    // - Long updateUserId
    // - LocalDateTime updateTime
    // - Integer deletedFlag (逻辑删除)
}
```

#### 3.2 继承层次
- **一级继承**: 继承BaseEntity获取审计能力
- **禁止多重继承**: 避免复杂的继承关系
- **组合优于继承**: 复杂场景使用组合设计

### 4. 字段设计原则

#### 4.1 字段数量控制
- **理想标准**: Entity字段数 ≤ 20个
- **可接受上限**: Entity字段数 ≤ 30个
- **禁止超标**: Entity字段数 > 30个必须拆分

#### 4.2 字段命名规范
- **驼峰命名**: 使用驼峰命名法，首字母小写
- **语义明确**: 字段名清晰表达含义
- **类型一致**: 相同概念使用相同数据类型

```java
// ✅ 正确示例
private Long userId;           // 用户ID
private String userName;         // 用户姓名
private LocalDateTime createTime;  // 创建时间
private Integer status;          // 状态
private BigDecimal amount;       // 金额

// ❌ 错误示例
private Long user_id;           // 下划线命名
private String name;            // 含义不明确
private Date time;              // 类型不具体
private int status;             // 基本类型
private Double amount;           // 金额精度问题
```

#### 4.3 数据类型规范

**金额字段**: 统一使用BigDecimal
```java
private BigDecimal consumeAmount;      // 消费金额
private BigDecimal discountAmount;      // 折扣金额
private BigDecimal actualAmount;        // 实际金额
```

**时间字段**: 统一使用LocalDateTime
```java
private LocalDateTime createTime;        // 创建时间
private LocalDateTime updateTime;        // 更新时间
private LocalDateTime expireTime;        // 过期时间
```

**ID字段**: 统一使用Long类型
```java
@TableId(type = IdType.ASSIGN_ID)
private Long id;                           // 主键ID
private Long userId;                       // 用户ID
private Long areaId;                       // 区域ID
```

### 5. 注解使用规范

#### 5.1 MyBatis-Plus注解
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_example")
public class ExampleEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务字段
     */
    @TableField("business_field")
    private String businessField;

    /**
     * 逻辑删除字段
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    /**
     * 乐观锁字段
     */
    @Version
    @TableField("version")
    private Integer version;
}
```

#### 5.2 验证注解
```java
@Data
@TableName("t_example")
public class ExampleEntity extends BaseEntity {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100个字符")
    @TableField("name")
    private String name;

    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值必须大于0")
    @TableField("status")
    private Integer status;

    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "金额格式不正确")
    @TableField("amount")
    private BigDecimal amount;
}
```

### 6. 扩展性设计原则

#### 6.1 JSON扩展字段
对于业务中可能变化或不确定的字段，使用JSON格式存储：

```java
/**
 * 扩展属性（JSON格式）
 * 用于存储业务特定配置，支持动态扩展
 */
@TableField("extended_attributes")
private String extendedAttributes;

// 使用示例
{
  "businessType": "RETENTION",
  "retentionDays": 180,
  "autoApprove": true,
  "notificationConfig": {
    "email": true,
    "sms": false
  }
}
```

#### 6.2 枚举字段使用
```java
/**
 * 设备状态：1-在线 2-离线 3-故障 4-维护
 */
@TableField("device_status")
private Integer deviceStatus;

// 建议使用枚举类管理
public enum DeviceStatus {
    ONLINE(1, "在线"),
    OFFLINE(2, "离线"),
    FAULT(3, "故障"),
    MAINTENANCE(4, "维护");

    private final Integer code;
    private final String description;
}
```

### 7. 业务领域设计原则

#### 7.1 单一职责
每个Entity只负责一个核心业务概念：

```java
// ✅ 正确：单一职责
public class UserEntity extends BaseEntity {
    // 用户基础信息
}

public class UserRoleEntity extends BaseEntity {
    // 用户角色关系
}

public class PermissionEntity extends BaseEntity {
    // 权限定义
}

// ❌ 错误：职责混乱
public class UserPermissionRoleEntity extends BaseEntity {
    // 用户、角色、权限混在一起，职责不清
}
```

#### 7.2 领域边界
- **公共模块**: 设备、区域、用户、权限等共享Entity
- **业务模块**: 考勤、消费、门禁、访客、视频等业务Entity
- **禁止跨域**: 业务模块Entity不依赖其他业务模块Entity

#### 7.3 依赖关系
- **向下依赖**: 业务模块可以依赖公共模块
- **禁止循环**: 模块间禁止循环依赖
- **接口隔离**: 通过DAO接口定义依赖关系

### 8. 性能优化原则

#### 8.1 索引设计
```java
/**
 * 设备ID
 * 建议添加索引：idx_device_id
 */
@TableField("device_id")
private String deviceId;

/**
 * 用户ID + 创建时间
 * 建议添加复合索引：idx_user_create_time
 */
@TableField("user_id")
private Long userId;

@TableField("create_time")
private LocalDateTime createTime;
```

#### 8.2 分表设计
对于大数据量表，按规则分表：

```java
/**
 * 考勤记录表
 * 按月分表：t_attendance_record_202501, t_attendance_record_202502
 */
@Data
@TableName("t_attendance_record")
public class AttendanceRecordEntity extends BaseEntity {

    /**
     * 考勤日期（包含年月信息，用于分表路由）
     */
    @TableField("attendance_date")
    private LocalDate attendanceDate;

    /**
     * 考勤月份（用于分表路由）
     */
    @TableField("attendance_month")
    private String attendanceMonth;  // 格式：yyyy-MM
}
```

### 9. 文档规范原则

#### 9.1 类注释
```java
/**
 * 设备实体类
 * <p>
 * 统一设备管理实体，支持多种设备类型和跨模块设备管理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>统一设备信息管理</li>
 *   <li>设备状态监控</li>
 *   <li>设备配置管理</li>
 *   <li>设备生命周期管理</li>
 * </ul>
 *
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * DeviceEntity device = new DeviceEntity();
 * device.setDeviceType("CAMERA");
 * device.setBusinessModule("video");
 * deviceService.save(device);
 * }</pre>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 * @see DeviceService 设备服务接口
 * @see DeviceDao 设备数据访问接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {
    // 实现代码
}
```

#### 9.2 字段注释
```java
/**
 * 设备状态：1-在线 2-离线 3-故障 4-维护
 * <p>
 * 在线：设备正常连接，可以正常使用<br>
 * 离线：设备失去连接，无法使用<br>
 * 故障：设备硬件或软件故障<br>
 * 维护：设备正在维护中
 * </p>
 */
@TableField("device_status")
private Integer deviceStatus;
```

### 10. 测试规范原则

#### 10.1 单元测试
```java
@ExtendWith(MockitoExtension.class)
class DeviceEntityTest {

    @Test
    void testDeviceEntityCreation() {
        // Given
        DeviceEntity device = new DeviceEntity();

        // When
        device.setDeviceCode("DEV001");
        device.setDeviceName("测试设备");
        device.setDeviceStatus(1);

        // Then
        assertEquals("DEV001", device.getDeviceCode());
        assertEquals("测试设备", device.getDeviceName());
        assertEquals(Integer.valueOf(1), device.getDeviceStatus());
    }
}
```

#### 10.2 数据库测试
```java
@DataJpaTest
class DeviceEntityRepositoryTest {

    @Autowired
    private DeviceDao deviceDao;

    @Test
    void testSaveAndFindDevice() {
        // Given
        DeviceEntity device = new DeviceEntity();
        device.setDeviceCode("DEV001");

        // When
        deviceDao.insert(device);
        DeviceEntity found = deviceDao.selectById(device.getDeviceId());

        // Then
        assertNotNull(found);
        assertEquals("DEV001", found.getDeviceCode());
    }
}
```

---

## 🔍 设计检查清单

### 设计阶段检查
- [ ] Entity名称符合命名规范
- [ ] 继承BaseEntity获取审计能力
- [ ] 字段数量控制在30个以内
- [ ] 数据类型使用规范正确
- [ ] 注解使用完整正确
- [ ] 扩展字段设计合理

### 编码阶段检查
- [ ] 类注释完整规范
- [ ] 字段注释清晰准确
- [ ] 验证注解使用正确
- [ ] 逻辑删除字段配置
- [ ] 乐观锁字段配置
- [ ] 业务逻辑未包含在Entity中

### 测试阶段检查
- [ ] 单元测试覆盖率≥80%
- [ ] 数据库操作测试完整
- [ ] 异常场景测试覆盖
- [ ] 性能测试符合要求

---

## 📝 最佳实践总结

### DO - 推荐做法
✅ 继承BaseEntity获取审计能力
✅ 使用BigDecimal处理金额字段
✅ 使用LocalDateTime处理时间字段
✅ 添加完整的字段注释和类注释
✅ 控制Entity字段数量在合理范围
✅ 使用JSON字段支持业务扩展
✅ 遵循统一的命名规范
✅ 添加必要的验证注解

### DON'T - 禁止做法
❌ 在Entity中包含业务逻辑
❌ 使用基本数据类型（int、double等）
❌ 字段数量超过30个不拆分
❌ 使用下划线命名Java字段
❌ 省略字段注释和类注释
❌ 违反单一职责原则
❌ 创建循环依赖关系
❌ 硬编码业务规则

---

## 🚀 持续改进

### 版本更新
- v1.0.0: 初始版本，建立基础规范
- 后续版本：根据实际使用情况持续优化

### 反馈机制
- 开发团队定期回顾规范执行情况
- 收集开发过程中的问题和建议
- 根据反馈持续优化规范内容

---

**📋 制定人**: IOE-DREAM架构团队
**🏗️ 技术审核**: SmartAdmin核心团队
**✅ 最终解释权**: IOE-DREAM项目架构委员会
**📅 版本**: v1.0.0 - 企业级Entity设计规范