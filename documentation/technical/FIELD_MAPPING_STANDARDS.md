# IOE-DREAM 字段映射规范

> **版本**: v1.0.0  
> **生效日期**: 2025-01-30  
> **适用范围**: IOE-DREAM项目所有Entity、DTO、VO字段映射  
> **规范等级**: 强制执行 - P0级架构规范

---

## 📋 一、核心原则

### 1.1 字段映射黄金法则

**映射优先级**:

1. ✅ **自动映射优先**: 相同字段名自动映射（使用BeanUtils.copyProperties）
2. ✅ **手动映射补充**: 字段名不同或需要计算的字段手动映射
3. ✅ **类型转换**: 自动处理基本类型转换，复杂类型手动转换
4. ✅ **null值处理**: 所有映射方法必须处理null值情况

### 1.2 映射场景分类

| 映射场景 | 推荐方式 | 说明 |
|---------|---------|------|
| **Entity → VO** | BeanUtils + 手动映射 | 需要计算字段（如工龄、描述字段） |
| **DTO → Entity** | BeanUtils + 手动映射 | 需要字段转换（如ID字段） |
| **Entity → DTO** | BeanUtils + 手动映射 | 需要字段过滤或转换 |
| **复杂对象映射** | MapStruct | 性能要求高的场景 |

---

## 🏗️ 二、字段命名规范

### 2.1 Entity字段命名规范

**核心原则**: Entity字段名与数据库字段名保持一致（使用@TableField映射）

```java
// ✅ 正确示例
@Data
@TableName("t_employee")
public class EmployeeEntity extends BaseEntity {
    @TableId(value = "employee_id", type = IdType.AUTO)
    private Long id;                    // 主键统一使用id
    
    @TableField("employee_name")
    private String employeeName;       // 业务字段使用驼峰命名
    
    @TableField("department_id")
    private Long departmentId;         // 外键字段使用xxxId格式
    
    @TableField("hire_date")
    private LocalDate hireDate;        // 日期字段使用Date后缀
}
```

**命名规则**:

- ✅ 主键字段：统一使用 `id`（Long类型）
- ✅ 业务字段：使用驼峰命名，与数据库字段对应
- ✅ 外键字段：使用 `xxxId` 格式（如 `departmentId`, `userId`）
- ✅ 时间字段：使用 `xxxTime` 或 `xxxDate` 格式
- ✅ 状态字段：使用 `status` 或 `xxxStatus` 格式
- ✅ 布尔字段：使用 `isXxx` 或 `xxxFlag` 格式

### 2.2 DTO字段命名规范

**核心原则**: DTO字段名与业务概念保持一致，便于API使用

```java
// ✅ 正确示例
@Data
public class EmployeeAddDTO {
    private String employeeName;       // 与Entity字段名一致
    private String employeeNo;         // 与Entity字段名一致
    private Long departmentId;         // 与Entity字段名一致
    private String phone;              // 与Entity字段名一致
    private String email;              // 与Entity字段名一致
}

@Data
public class EmployeeUpdateDTO extends EmployeeAddDTO {
    private Long employeeId;          // 更新操作需要ID字段
}
```

**命名规则**:

- ✅ 与Entity字段名保持一致（便于自动映射）
- ✅ 更新DTO必须包含ID字段（使用 `xxxId` 格式）
- ✅ 查询DTO使用 `XxxQueryDTO` 命名
- ✅ 创建DTO使用 `XxxAddDTO` 或 `XxxCreateDTO` 命名

### 2.3 VO字段命名规范

**核心原则**: VO字段名面向前端展示，可包含计算字段和描述字段

```java
// ✅ 正确示例
@Data
public class EmployeeVO {
    private Long employeeId;          // VO使用employeeId，Entity使用id
    private String employeeName;      // 与Entity字段名一致
    private String departmentName;    // 关联字段（需要查询）
    private Integer workYears;        // 计算字段（需要计算）
    private String genderDesc;        // 描述字段（需要转换）
    private String statusDesc;        // 描述字段（需要转换）
}
```

**命名规则**:

- ✅ ID字段：使用 `xxxId` 格式（如 `employeeId`, `userId`）
- ✅ 关联字段：使用 `xxxName` 格式（如 `departmentName`, `userName`）
- ✅ 计算字段：使用业务含义命名（如 `workYears`, `totalAmount`）
- ✅ 描述字段：使用 `xxxDesc` 格式（如 `statusDesc`, `genderDesc`）

---

## 🔄 三、字段映射规则

### 3.1 自动映射规则

**适用场景**: 字段名相同且类型兼容

```java
// ✅ 自动映射示例
EmployeeEntity entity = new EmployeeEntity();
entity.setEmployeeName("张三");
entity.setPhone("13800138000");
entity.setEmail("zhangsan@example.com");

EmployeeVO vo = new EmployeeVO();
BeanUtils.copyProperties(entity, vo);
// 自动映射：employeeName, phone, email
```

**自动映射字段**:

- ✅ 字段名完全相同
- ✅ 类型兼容（基本类型自动转换）
- ✅ 继承字段（继承自BaseEntity的字段）

### 3.2 手动映射规则

**适用场景**: 字段名不同、需要计算、需要类型转换

#### 3.2.1 ID字段映射

```java
// ✅ Entity → VO: id → employeeId
private EmployeeVO convertToVO(EmployeeEntity entity) {
    EmployeeVO vo = new EmployeeVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 手动映射ID字段
    vo.setEmployeeId(entity.getId());
    
    return vo;
}

// ✅ DTO → Entity: employeeId → id
private EmployeeEntity convertToEntity(EmployeeUpdateDTO dto) {
    EmployeeEntity entity = new EmployeeEntity();
    BeanUtils.copyProperties(dto, entity);
    
    // 手动映射ID字段
    entity.setId(dto.getEmployeeId());
    
    return entity;
}
```

#### 3.2.2 计算字段映射

```java
// ✅ 计算字段映射
private EmployeeVO convertToVO(EmployeeEntity entity) {
    EmployeeVO vo = new EmployeeVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 计算工龄
    if (entity.getHireDate() != null) {
        int workYears = employeeManager.calculateWorkYears(entity.getHireDate());
        vo.setWorkYears(workYears);
    }
    
    return vo;
}
```

#### 3.2.3 描述字段映射

```java
// ✅ 描述字段映射
private EmployeeVO convertToVO(EmployeeEntity entity) {
    EmployeeVO vo = new EmployeeVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 性别描述
    vo.setGenderDesc(getGenderDesc(entity.getGender()));
    
    // 状态描述
    vo.setStatusDesc(getStatusDesc(entity.getStatus()));
    
    return vo;
}

private String getGenderDesc(Integer gender) {
    if (gender == null) return "未知";
    switch (gender) {
        case 1: return "男";
        case 2: return "女";
        default: return "未知";
    }
}
```

#### 3.2.4 关联字段映射

```java
// ✅ 关联字段映射
private EmployeeVO convertToVO(EmployeeEntity entity) {
    EmployeeVO vo = new EmployeeVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 关联查询部门名称
    if (entity.getDepartmentId() != null) {
        DepartmentEntity department = departmentDao.selectById(entity.getDepartmentId());
        if (department != null) {
            vo.setDepartmentName(department.getDepartmentName());
        }
    }
    
    return vo;
}
```

### 3.3 类型转换规则

#### 3.3.1 基本类型转换

```java
// ✅ 自动转换（BeanUtils自动处理）
Integer status = 1;
String statusStr = String.valueOf(status);  // 手动转换

Long id = 1001L;
String idStr = id != null ? id.toString() : null;  // null安全转换
```

#### 3.3.2 日期时间转换

```java
// ✅ LocalDateTime → String
private String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) return null;
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}

// ✅ String → LocalDateTime
private LocalDateTime parseDateTime(String dateTimeStr) {
    if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}
```

#### 3.3.3 枚举转换

```java
// ✅ Integer → Enum
private String getStatusDesc(Integer status) {
    if (status == null) return "未知";
    return EmployeeStatus.fromCode(status).getDesc();
}

// ✅ Enum → Integer
private Integer getStatusCode(EmployeeStatus status) {
    return status != null ? status.getCode() : null;
}
```

---

## 🛡️ 四、特殊字段处理规则

### 4.1 ID字段处理

**规则**: Entity使用 `id`，DTO/VO使用 `xxxId`

```java
// ✅ Entity
public class EmployeeEntity {
    private Long id;  // 主键统一使用id
}

// ✅ DTO
public class EmployeeUpdateDTO {
    private Long employeeId;  // DTO使用employeeId
}

// ✅ VO
public class EmployeeVO {
    private Long employeeId;  // VO使用employeeId
}

// ✅ 映射方法
private EmployeeVO convertToVO(EmployeeEntity entity) {
    EmployeeVO vo = new EmployeeVO();
    BeanUtils.copyProperties(entity, vo);
    vo.setEmployeeId(entity.getId());  // 手动映射
    return vo;
}
```

### 4.2 时间字段处理

**规则**: 统一使用LocalDateTime，VO中可转换为String

```java
// ✅ Entity
public class EmployeeEntity {
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDate hireDate;
}

// ✅ VO
public class EmployeeVO {
    private String createTime;  // 转换为String格式
    private String updateTime;  // 转换为String格式
    private String hireDate;    // 转换为String格式
}

// ✅ 映射方法
private EmployeeVO convertToVO(EmployeeEntity entity) {
    EmployeeVO vo = new EmployeeVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 时间字段转换
    if (entity.getCreateTime() != null) {
        vo.setCreateTime(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    return vo;
}
```

### 4.3 金额字段处理

**规则**: 统一使用BigDecimal，保持精度

```java
// ✅ Entity
public class ConsumeRecordEntity {
    private BigDecimal amount;  // 使用BigDecimal
}

// ✅ VO
public class ConsumeRecordVO {
    private BigDecimal amount;  // 保持BigDecimal类型
    private String amountStr;   // 可选：格式化字符串
}

// ✅ 映射方法
private ConsumeRecordVO convertToVO(ConsumeRecordEntity entity) {
    ConsumeRecordVO vo = new ConsumeRecordVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 金额格式化（可选）
    if (entity.getAmount() != null) {
        vo.setAmountStr(entity.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
    }
    
    return vo;
}
```

### 4.4 状态字段处理

**规则**: 统一使用Integer，VO中提供描述字段

```java
// ✅ Entity
public class EmployeeEntity {
    private Integer status;  // 1-启用 2-禁用
}

// ✅ VO
public class EmployeeVO {
    private Integer status;      // 状态码
    private String statusDesc;    // 状态描述
}

// ✅ 映射方法
private EmployeeVO convertToVO(EmployeeEntity entity) {
    EmployeeVO vo = new EmployeeVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 状态描述
    vo.setStatusDesc(getStatusDesc(entity.getStatus()));
    
    return vo;
}
```

### 4.5 关联字段处理

**规则**: Entity存储ID，VO中查询并填充名称

```java
// ✅ Entity
public class EmployeeEntity {
    private Long departmentId;  // 存储部门ID
}

// ✅ VO
public class EmployeeVO {
    private Long departmentId;      // 部门ID
    private String departmentName;  // 部门名称（需要查询）
}

// ✅ 映射方法（批量查询优化）
private List<EmployeeVO> convertToVOList(List<EmployeeEntity> entities) {
    // 1. 提取所有部门ID
    Set<Long> departmentIds = entities.stream()
        .map(EmployeeEntity::getDepartmentId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    
    // 2. 批量查询部门信息
    Map<Long, String> departmentMap = departmentDao.selectBatchIds(departmentIds)
        .stream()
        .collect(Collectors.toMap(DepartmentEntity::getId, DepartmentEntity::getDepartmentName));
    
    // 3. 转换VO并填充部门名称
    return entities.stream().map(entity -> {
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setDepartmentName(departmentMap.get(entity.getDepartmentId()));
        return vo;
    }).collect(Collectors.toList());
}
```

---

## 📝 五、映射方法模板

### 5.1 Entity → VO 转换模板

```java
/**
 * 转换Entity为VO
 * 
 * @param entity Entity对象
 * @return VO对象
 */
private EmployeeVO convertToVO(EmployeeEntity entity) {
    if (entity == null) {
        return null;
    }
    
    EmployeeVO vo = new EmployeeVO();
    
    // 1. 自动映射（字段名相同的字段）
    BeanUtils.copyProperties(entity, vo);
    
    // 2. 手动映射ID字段
    vo.setEmployeeId(entity.getId());
    
    // 3. 计算字段
    if (entity.getHireDate() != null) {
        int workYears = employeeManager.calculateWorkYears(entity.getHireDate());
        vo.setWorkYears(workYears);
    }
    
    // 4. 描述字段
    vo.setGenderDesc(getGenderDesc(entity.getGender()));
    vo.setStatusDesc(getStatusDesc(entity.getStatus()));
    
    // 5. 关联字段（需要查询）
    if (entity.getDepartmentId() != null) {
        DepartmentEntity department = departmentDao.selectById(entity.getDepartmentId());
        if (department != null) {
            vo.setDepartmentName(department.getDepartmentName());
        }
    }
    
    return vo;
}
```

### 5.2 DTO → Entity 转换模板

```java
/**
 * 转换DTO为Entity
 * 
 * @param dto DTO对象
 * @return Entity对象
 */
private EmployeeEntity convertToEntity(EmployeeAddDTO dto) {
    if (dto == null) {
        return null;
    }
    
    EmployeeEntity entity = new EmployeeEntity();
    
    // 1. 自动映射（字段名相同的字段）
    BeanUtils.copyProperties(dto, entity);
    
    // 2. 手动映射特殊字段
    // （AddDTO通常不需要ID字段，由数据库自动生成）
    
    // 3. 设置默认值
    if (entity.getStatus() == null) {
        entity.setStatus(1);  // 默认启用
    }
    
    // 4. 设置审计字段
    entity.setCreateTime(LocalDateTime.now());
    entity.setUpdateTime(LocalDateTime.now());
    
    return entity;
}
```

### 5.3 批量转换模板

```java
/**
 * 批量转换Entity为VO（性能优化版本）
 * 
 * @param entities Entity列表
 * @return VO列表
 */
private List<EmployeeVO> convertToVOList(List<EmployeeEntity> entities) {
    if (entities == null || entities.isEmpty()) {
        return Collections.emptyList();
    }
    
    // 1. 提取所有关联ID（批量查询优化）
    Set<Long> departmentIds = entities.stream()
        .map(EmployeeEntity::getDepartmentId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    
    // 2. 批量查询关联数据
    Map<Long, String> departmentMap = departmentDao.selectBatchIds(departmentIds)
        .stream()
        .collect(Collectors.toMap(DepartmentEntity::getId, DepartmentEntity::getDepartmentName));
    
    // 3. 批量转换
    return entities.stream()
        .map(entity -> {
            EmployeeVO vo = new EmployeeVO();
            BeanUtils.copyProperties(entity, vo);
            
            // 手动映射
            vo.setEmployeeId(entity.getId());
            vo.setDepartmentName(departmentMap.get(entity.getDepartmentId()));
            
            // 计算字段
            if (entity.getHireDate() != null) {
                vo.setWorkYears(employeeManager.calculateWorkYears(entity.getHireDate()));
            }
            
            // 描述字段
            vo.setGenderDesc(getGenderDesc(entity.getGender()));
            vo.setStatusDesc(getStatusDesc(entity.getStatus()));
            
            return vo;
        })
        .collect(Collectors.toList());
}
```

---

## 🚀 六、MapStruct集成指南

### 6.1 MapStruct优势

**性能优势**:

- ✅ 编译时生成代码，零运行时开销
- ✅ 类型安全，编译期检查
- ✅ 支持复杂映射规则
- ✅ 自动生成null值检查

### 6.2 MapStruct依赖配置

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 6.3 MapStruct映射器示例

```java
// ✅ MapStruct映射器
@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    
    /**
     * Entity → VO
     */
    @Mapping(source = "id", target = "employeeId")
    @Mapping(source = "departmentId", target = "departmentId")
    @Mapping(target = "departmentName", ignore = true)  // 需要手动查询
    @Mapping(target = "workYears", ignore = true)      // 需要计算
    @Mapping(target = "genderDesc", ignore = true)      // 需要转换
    @Mapping(target = "statusDesc", ignore = true)      // 需要转换
    EmployeeVO toVO(EmployeeEntity entity);
    
    /**
     * DTO → Entity
     */
    @Mapping(source = "employeeId", target = "id")
    @Mapping(target = "createTime", ignore = true)      // 自动设置
    @Mapping(target = "updateTime", ignore = true)     // 自动设置
    EmployeeEntity toEntity(EmployeeUpdateDTO dto);
    
    /**
     * 批量转换
     */
    List<EmployeeVO> toVOList(List<EmployeeEntity> entities);
}
```

### 6.4 MapStruct使用示例

```java
// ✅ Service中使用MapStruct
@Service
public class EmployeeServiceImpl implements EmployeeService {
    
    @Resource
    private EmployeeMapper employeeMapper;  // MapStruct自动生成实现
    
    @Override
    public EmployeeVO getEmployeeDetail(Long employeeId) {
        EmployeeEntity entity = employeeDao.selectById(employeeId);
        if (entity == null) {
            return null;
        }
        
        // 使用MapStruct转换
        EmployeeVO vo = employeeMapper.toVO(entity);
        
        // 手动填充需要查询或计算的字段
        if (entity.getDepartmentId() != null) {
            DepartmentEntity department = departmentDao.selectById(entity.getDepartmentId());
            if (department != null) {
                vo.setDepartmentName(department.getDepartmentName());
            }
        }
        
        if (entity.getHireDate() != null) {
            vo.setWorkYears(employeeManager.calculateWorkYears(entity.getHireDate()));
        }
        
        vo.setGenderDesc(getGenderDesc(entity.getGender()));
        vo.setStatusDesc(getStatusDesc(entity.getStatus()));
        
        return vo;
    }
}
```

---

## ✅ 七、检查清单

### 7.1 字段映射检查清单

**代码实现前检查**:

- [ ] 确认Entity、DTO、VO字段命名符合规范
- [ ] 确认ID字段映射规则（Entity使用id，DTO/VO使用xxxId）
- [ ] 确认时间字段处理规则（LocalDateTime ↔ String）
- [ ] 确认金额字段处理规则（BigDecimal精度保持）
- [ ] 确认状态字段处理规则（Integer + 描述字段）

**代码实现后检查**:

- [ ] 所有自动映射字段已通过BeanUtils.copyProperties处理
- [ ] 所有手动映射字段已正确实现
- [ ] 所有计算字段已正确计算
- [ ] 所有描述字段已正确转换
- [ ] 所有关联字段已正确查询和填充
- [ ] null值处理完整（所有映射方法都处理null情况）
- [ ] 批量转换已优化（使用批量查询减少数据库访问）

### 7.2 性能优化检查清单

- [ ] 批量转换使用批量查询优化关联字段
- [ ] 避免在循环中进行数据库查询
- [ ] 使用缓存减少重复查询
- [ ] 复杂映射考虑使用MapStruct提升性能

---

## 📚 八、最佳实践

### 8.1 性能优化实践

**批量转换优化**:

```java
// ❌ 错误示例：在循环中查询数据库
List<EmployeeVO> voList = entities.stream()
    .map(entity -> {
        EmployeeVO vo = convertToVO(entity);
        // 在循环中查询数据库 - 性能差！
        DepartmentEntity dept = departmentDao.selectById(entity.getDepartmentId());
        vo.setDepartmentName(dept.getDepartmentName());
        return vo;
    })
    .collect(Collectors.toList());

// ✅ 正确示例：批量查询优化
Set<Long> departmentIds = entities.stream()
    .map(EmployeeEntity::getDepartmentId)
    .filter(Objects::nonNull)
    .collect(Collectors.toSet());

Map<Long, String> departmentMap = departmentDao.selectBatchIds(departmentIds)
    .stream()
    .collect(Collectors.toMap(DepartmentEntity::getId, DepartmentEntity::getDepartmentName()));

List<EmployeeVO> voList = entities.stream()
    .map(entity -> {
        EmployeeVO vo = convertToVO(entity);
        vo.setDepartmentName(departmentMap.get(entity.getDepartmentId()));
        return vo;
    })
    .collect(Collectors.toList());
```

### 8.2 代码复用实践

**提取公共转换方法**:

```java
// ✅ 公共转换工具类
public class MappingUtils {
    
    /**
     * 格式化日期时间
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * 格式化日期
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * 格式化金额
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO.toString();
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
```

---

## 🔗 相关文档

- **Entity设计规范**: [ENTITY_DESIGN_STANDARDS.md](./ENTITY_DESIGN_STANDARDS.md)
- **开发规范**: [UNIFIED_DEVELOPMENT_STANDARDS.md](./UNIFIED_DEVELOPMENT_STANDARDS.md)
- **编译错误修复总结**: [COMPILATION_FIX_COMPLETE_SUMMARY.md](./COMPILATION_FIX_COMPLETE_SUMMARY.md)

---

**制定人**: IOE-DREAM 架构委员会  
**版本**: v1.0.0  
**最后更新**: 2025-01-30
