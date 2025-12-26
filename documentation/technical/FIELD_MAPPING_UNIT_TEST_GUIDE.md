# IOE-DREAM 字段映射单元测试指南

> **版本**: v1.0.0  
> **生效日期**: 2025-01-30  
> **适用范围**: IOE-DREAM项目所有字段映射方法  
> **规范等级**: 强制执行 - P0级质量保障

---

## 📋 一、测试目标

### 1.1 核心目标

- ✅ **确保字段映射正确**: 验证Entity、DTO、VO之间的字段映射准确性
- ✅ **防止字段遗漏**: 确保所有字段都被正确映射
- ✅ **验证类型转换**: 确保类型转换正确（如LocalDateTime → String）
- ✅ **处理边界情况**: 验证null值、空值、边界值的处理

### 1.2 测试覆盖率要求

| 映射类型 | 最低覆盖率 | 目标覆盖率 |
|---------|-----------|-----------|
| Entity → VO | 80% | 90% |
| DTO → Entity | 80% | 90% |
| Entity → DTO | 70% | 80% |
| 批量转换 | 75% | 85% |

---

## 🏗️ 二、测试框架配置

### 2.1 依赖配置

```xml
<!-- pom.xml -->
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>${mockito.version}</version>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ (推荐，更易读的断言) -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.2 测试类命名规范

| 测试类型 | 命名规范 | 示例 |
|---------|---------|------|
| 映射测试 | `XxxMappingTest` | `EmployeeMappingTest` |
| 转换测试 | `XxxConverterTest` | `EmployeeConverterTest` |
| 服务测试 | `XxxServiceTest` | `EmployeeServiceTest` |

---

## ✅ 三、测试用例模板

### 3.1 Entity → VO 转换测试模板

```java
package net.lab1024.sa.common.system.employee.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Employee实体到VO转换测试
 * 
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee实体到VO转换测试")
class EmployeeMappingTest {
    
    @Mock
    private DepartmentDao departmentDao;
    
    @Mock
    private EmployeeManager employeeManager;
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    private EmployeeEntity entity;
    private DepartmentEntity department;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        entity = new EmployeeEntity();
        entity.setId(1001L);
        entity.setEmployeeName("张三");
        entity.setEmployeeNo("EMP001");
        entity.setPhone("13800138000");
        entity.setEmail("zhangsan@example.com");
        entity.setGender(1);
        entity.setStatus(1);
        entity.setDepartmentId(2001L);
        entity.setHireDate(LocalDate.of(2020, 1, 1));
        entity.setCreateTime(LocalDateTime.of(2020, 1, 1, 10, 0, 0));
        entity.setUpdateTime(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        
        department = new DepartmentEntity();
        department.setId(2001L);
        department.setDepartmentName("技术部");
        
        // Mock依赖
        when(departmentDao.selectById(2001L)).thenReturn(department);
        when(employeeManager.calculateWorkYears(anyLocalDate())).thenReturn(4);
    }
    
    @Test
    @DisplayName("正常转换：所有字段正确映射")
    void testConvertToVO_Success() {
        // When
        EmployeeVO vo = employeeService.convertToVO(entity);
        
        // Then
        assertThat(vo).isNotNull();
        
        // ID字段映射
        assertThat(vo.getEmployeeId()).isEqualTo(1001L);
        
        // 基本字段映射
        assertThat(vo.getEmployeeName()).isEqualTo("张三");
        assertThat(vo.getEmployeeNo()).isEqualTo("EMP001");
        assertThat(vo.getPhone()).isEqualTo("13800138000");
        assertThat(vo.getEmail()).isEqualTo("zhangsan@example.com");
        assertThat(vo.getGender()).isEqualTo(1);
        assertThat(vo.getStatus()).isEqualTo(1);
        
        // 关联字段映射
        assertThat(vo.getDepartmentId()).isEqualTo(2001L);
        assertThat(vo.getDepartmentName()).isEqualTo("技术部");
        
        // 计算字段映射
        assertThat(vo.getWorkYears()).isEqualTo(4);
        
        // 描述字段映射
        assertThat(vo.getGenderDesc()).isEqualTo("男");
        assertThat(vo.getStatusDesc()).isEqualTo("启用");
        
        // 时间字段映射
        assertThat(vo.getCreateTime()).isEqualTo("2020-01-01 10:00:00");
        assertThat(vo.getUpdateTime()).isEqualTo("2024-01-01 10:00:00");
    }
    
    @Test
    @DisplayName("null值处理：Entity为null时返回null")
    void testConvertToVO_NullEntity() {
        // When
        EmployeeVO vo = employeeService.convertToVO(null);
        
        // Then
        assertThat(vo).isNull();
    }
    
    @Test
    @DisplayName("部分字段为null：正确处理null字段")
    void testConvertToVO_PartialNullFields() {
        // Given
        entity.setPhone(null);
        entity.setEmail(null);
        entity.setDepartmentId(null);
        entity.setHireDate(null);
        
        // When
        EmployeeVO vo = employeeService.convertToVO(entity);
        
        // Then
        assertThat(vo).isNotNull();
        assertThat(vo.getPhone()).isNull();
        assertThat(vo.getEmail()).isNull();
        assertThat(vo.getDepartmentId()).isNull();
        assertThat(vo.getDepartmentName()).isNull();
        assertThat(vo.getWorkYears()).isNull();
    }
    
    @Test
    @DisplayName("关联字段为null：部门不存在时正确处理")
    void testConvertToVO_DepartmentNotFound() {
        // Given
        when(departmentDao.selectById(2001L)).thenReturn(null);
        
        // When
        EmployeeVO vo = employeeService.convertToVO(entity);
        
        // Then
        assertThat(vo).isNotNull();
        assertThat(vo.getDepartmentId()).isEqualTo(2001L);
        assertThat(vo.getDepartmentName()).isNull();
    }
    
    @Test
    @DisplayName("边界值测试：最小值和最大值")
    void testConvertToVO_BoundaryValues() {
        // Given
        entity.setId(1L);  // 最小值
        entity.setId(Long.MAX_VALUE);  // 最大值
        
        // When
        EmployeeVO vo = employeeService.convertToVO(entity);
        
        // Then
        assertThat(vo).isNotNull();
        assertThat(vo.getEmployeeId()).isEqualTo(Long.MAX_VALUE);
    }
}
```

### 3.2 DTO → Entity 转换测试模板

```java
package net.lab1024.sa.common.system.employee.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Employee DTO到Entity转换测试
 * 
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee DTO到Entity转换测试")
class EmployeeDTOMappingTest {
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    private EmployeeAddDTO addDTO;
    private EmployeeUpdateDTO updateDTO;
    
    @BeforeEach
    void setUp() {
        addDTO = new EmployeeAddDTO();
        addDTO.setEmployeeName("李四");
        addDTO.setEmployeeNo("EMP002");
        addDTO.setPhone("13900139000");
        addDTO.setEmail("lisi@example.com");
        addDTO.setGender(2);
        addDTO.setDepartmentId(2002L);
        addDTO.setHireDate(LocalDate.of(2021, 6, 1));
        
        updateDTO = new EmployeeUpdateDTO();
        updateDTO.setEmployeeId(1001L);
        updateDTO.setEmployeeName("李四（更新）");
        updateDTO.setPhone("13900139001");
    }
    
    @Test
    @DisplayName("AddDTO转换：所有字段正确映射")
    void testConvertAddDTOToEntity_Success() {
        // When
        EmployeeEntity entity = employeeService.convertToEntity(addDTO);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getEmployeeName()).isEqualTo("李四");
        assertThat(entity.getEmployeeNo()).isEqualTo("EMP002");
        assertThat(entity.getPhone()).isEqualTo("13900139000");
        assertThat(entity.getEmail()).isEqualTo("lisi@example.com");
        assertThat(entity.getGender()).isEqualTo(2);
        assertThat(entity.getDepartmentId()).isEqualTo(2002L);
        assertThat(entity.getHireDate()).isEqualTo(LocalDate.of(2021, 6, 1));
        
        // 默认值检查
        assertThat(entity.getStatus()).isEqualTo(1);  // 默认启用
        
        // 审计字段检查
        assertThat(entity.getCreateTime()).isNotNull();
        assertThat(entity.getUpdateTime()).isNotNull();
    }
    
    @Test
    @DisplayName("UpdateDTO转换：ID字段正确映射")
    void testConvertUpdateDTOToEntity_Success() {
        // When
        EmployeeEntity entity = employeeService.convertToEntity(updateDTO);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1001L);  // ID字段映射
        assertThat(entity.getEmployeeName()).isEqualTo("李四（更新）");
        assertThat(entity.getPhone()).isEqualTo("13900139001");
    }
    
    @Test
    @DisplayName("null值处理：DTO为null时返回null")
    void testConvertToEntity_NullDTO() {
        // When
        EmployeeEntity entity = employeeService.convertToEntity((EmployeeAddDTO) null);
        
        // Then
        assertThat(entity).isNull();
    }
    
    @Test
    @DisplayName("部分字段为null：正确处理null字段")
    void testConvertToEntity_PartialNullFields() {
        // Given
        addDTO.setPhone(null);
        addDTO.setEmail(null);
        addDTO.setDepartmentId(null);
        
        // When
        EmployeeEntity entity = employeeService.convertToEntity(addDTO);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getPhone()).isNull();
        assertThat(entity.getEmail()).isNull();
        assertThat(entity.getDepartmentId()).isNull();
    }
}
```

### 3.3 批量转换测试模板

```java
package net.lab1024.sa.common.system.employee.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

/**
 * Employee批量转换测试
 * 
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee批量转换测试")
class EmployeeBatchMappingTest {
    
    @Mock
    private DepartmentDao departmentDao;
    
    @Mock
    private EmployeeManager employeeManager;
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    private List<EmployeeEntity> entities;
    private Map<Long, DepartmentEntity> departmentMap;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        EmployeeEntity entity1 = createEmployee(1001L, "张三", 2001L);
        EmployeeEntity entity2 = createEmployee(1002L, "李四", 2002L);
        EmployeeEntity entity3 = createEmployee(1003L, "王五", 2001L);
        entities = Arrays.asList(entity1, entity2, entity3);
        
        DepartmentEntity dept1 = createDepartment(2001L, "技术部");
        DepartmentEntity dept2 = createDepartment(2002L, "销售部");
        departmentMap = Map.of(
            2001L, dept1,
            2002L, dept2
        );
        
        // Mock批量查询
        when(departmentDao.selectBatchIds(anySet())).thenReturn(
            departmentMap.values().stream().collect(Collectors.toList())
        );
    }
    
    @Test
    @DisplayName("批量转换：所有实体正确转换")
    void testConvertToVOList_Success() {
        // When
        List<EmployeeVO> voList = employeeService.convertToVOList(entities);
        
        // Then
        assertThat(voList).isNotNull();
        assertThat(voList).hasSize(3);
        
        // 验证第一个实体
        EmployeeVO vo1 = voList.get(0);
        assertThat(vo1.getEmployeeId()).isEqualTo(1001L);
        assertThat(vo1.getEmployeeName()).isEqualTo("张三");
        assertThat(vo1.getDepartmentId()).isEqualTo(2001L);
        assertThat(vo1.getDepartmentName()).isEqualTo("技术部");
        
        // 验证第二个实体
        EmployeeVO vo2 = voList.get(1);
        assertThat(vo2.getEmployeeId()).isEqualTo(1002L);
        assertThat(vo2.getEmployeeName()).isEqualTo("李四");
        assertThat(vo2.getDepartmentId()).isEqualTo(2002L);
        assertThat(vo2.getDepartmentName()).isEqualTo("销售部");
        
        // 验证第三个实体（相同部门）
        EmployeeVO vo3 = voList.get(2);
        assertThat(vo3.getEmployeeId()).isEqualTo(1003L);
        assertThat(vo3.getDepartmentId()).isEqualTo(2001L);
        assertThat(vo3.getDepartmentName()).isEqualTo("技术部");
    }
    
    @Test
    @DisplayName("空列表转换：返回空列表")
    void testConvertToVOList_EmptyList() {
        // When
        List<EmployeeVO> voList = employeeService.convertToVOList(List.of());
        
        // Then
        assertThat(voList).isNotNull();
        assertThat(voList).isEmpty();
    }
    
    @Test
    @DisplayName("null列表转换：返回空列表")
    void testConvertToVOList_NullList() {
        // When
        List<EmployeeVO> voList = employeeService.convertToVOList(null);
        
        // Then
        assertThat(voList).isNotNull();
        assertThat(voList).isEmpty();
    }
    
    @Test
    @DisplayName("批量查询优化：只查询一次数据库")
    void testConvertToVOList_BatchQueryOptimization() {
        // When
        employeeService.convertToVOList(entities);
        
        // Then
        // 验证只调用一次批量查询（不是N次单独查询）
        verify(departmentDao, times(1)).selectBatchIds(anySet());
        verify(departmentDao, never()).selectById(anyLong());
    }
    
    private EmployeeEntity createEmployee(Long id, String name, Long deptId) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(id);
        entity.setEmployeeName(name);
        entity.setDepartmentId(deptId);
        entity.setHireDate(LocalDate.of(2020, 1, 1));
        return entity;
    }
    
    private DepartmentEntity createDepartment(Long id, String name) {
        DepartmentEntity dept = new DepartmentEntity();
        dept.setId(id);
        dept.setDepartmentName(name);
        return dept;
    }
}
```

### 3.4 类型转换测试模板

```java
package net.lab1024.sa.common.system.employee.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Employee类型转换测试
 * 
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee类型转换测试")
class EmployeeTypeConversionTest {
    
    @InjectMocks
    private EmployeeServiceImpl employeeService;
    
    @Test
    @DisplayName("日期时间转换：LocalDateTime → String")
    void testDateTimeConversion() {
        // Given
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(1001L);
        entity.setCreateTime(LocalDateTime.of(2024, 1, 15, 14, 30, 45));
        entity.setUpdateTime(LocalDateTime.of(2024, 1, 20, 16, 45, 30));
        
        // When
        EmployeeVO vo = employeeService.convertToVO(entity);
        
        // Then
        assertThat(vo.getCreateTime()).isEqualTo("2024-01-15 14:30:45");
        assertThat(vo.getUpdateTime()).isEqualTo("2024-01-20 16:45:30");
    }
    
    @Test
    @DisplayName("日期转换：LocalDate → String")
    void testDateConversion() {
        // Given
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(1001L);
        entity.setHireDate(LocalDate.of(2020, 6, 1));
        
        // When
        EmployeeVO vo = employeeService.convertToVO(entity);
        
        // Then
        assertThat(vo.getHireDate()).isEqualTo("2020-06-01");
    }
    
    @Test
    @DisplayName("枚举转换：Integer → String描述")
    void testEnumConversion() {
        // Given
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(1001L);
        entity.setGender(1);  // 男
        entity.setStatus(1);  // 启用
        
        // When
        EmployeeVO vo = employeeService.convertToVO(entity);
        
        // Then
        assertThat(vo.getGenderDesc()).isEqualTo("男");
        assertThat(vo.getStatusDesc()).isEqualTo("启用");
    }
    
    @Test
    @DisplayName("金额转换：BigDecimal → String")
    void testAmountConversion() {
        // Given
        ConsumeRecordEntity entity = new ConsumeRecordEntity();
        entity.setId(1001L);
        entity.setAmount(new BigDecimal("99.99"));
        
        // When
        ConsumeRecordVO vo = consumeService.convertToVO(entity);
        
        // Then
        assertThat(vo.getAmountStr()).isEqualTo("99.99");
    }
}
```

---

## 🔍 四、测试检查清单

### 4.1 基本映射检查

- [ ] 所有基本字段都已映射
- [ ] ID字段映射正确（Entity.id → VO.xxxId）
- [ ] 时间字段转换正确（LocalDateTime → String）
- [ ] 金额字段精度保持（BigDecimal）
- [ ] 状态字段描述正确（Integer → String）

### 4.2 关联字段检查

- [ ] 关联字段ID映射正确
- [ ] 关联字段名称查询正确
- [ ] 关联字段为null时正确处理
- [ ] 批量转换使用批量查询优化

### 4.3 计算字段检查

- [ ] 计算字段计算逻辑正确
- [ ] 计算字段输入为null时正确处理
- [ ] 计算字段边界值测试通过

### 4.4 边界情况检查

- [ ] Entity为null时返回null
- [ ] DTO为null时返回null
- [ ] 列表为null或空时返回空列表
- [ ] 部分字段为null时正确处理
- [ ] 关联数据不存在时正确处理

### 4.5 性能检查

- [ ] 批量转换使用批量查询（不是N+1查询）
- [ ] 避免在循环中进行数据库查询
- [ ] 使用缓存减少重复查询

---

## 📊 五、测试报告模板

### 5.1 测试覆盖率报告

```xml
<!-- pom.xml -->
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 5.2 测试报告生成

```bash
# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 查看报告
# target/site/jacoco/index.html
```

---

## 🚀 六、CI/CD集成

### 6.1 测试执行配置

```yaml
# .github/workflows/test.yml
name: Unit Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run tests
        run: mvn clean test
      
      - name: Generate coverage report
        run: mvn jacoco:report
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./target/site/jacoco/jacoco.xml
```

### 6.2 覆盖率阈值配置

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>CLASS</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

---

## 📚 七、最佳实践

### 7.1 测试数据准备

**使用Builder模式**:
```java
// ✅ 推荐：使用Builder模式
EmployeeEntity entity = EmployeeEntity.builder()
    .id(1001L)
    .employeeName("张三")
    .employeeNo("EMP001")
    .phone("13800138000")
    .email("zhangsan@example.com")
    .gender(1)
    .status(1)
    .departmentId(2001L)
    .hireDate(LocalDate.of(2020, 1, 1))
    .build();
```

### 7.2 断言使用

**使用AssertJ**:
```java
// ✅ 推荐：使用AssertJ（更易读）
assertThat(vo.getEmployeeId()).isEqualTo(1001L);
assertThat(vo.getEmployeeName()).isEqualTo("张三");
assertThat(vo.getDepartmentName()).isNotNull();

// ❌ 不推荐：使用JUnit断言（可读性差）
assertEquals(1001L, vo.getEmployeeId());
assertNotNull(vo.getDepartmentName());
```

### 7.3 Mock使用

**只Mock外部依赖**:
```java
// ✅ 正确：只Mock外部依赖（DAO、Manager）
@Mock
private DepartmentDao departmentDao;

@Mock
private EmployeeManager employeeManager;

// ❌ 错误：不要Mock被测试的类
@Mock
private EmployeeServiceImpl employeeService;  // 错误！
```

---

## 🔗 相关文档

- **字段映射规范**: [FIELD_MAPPING_STANDARDS.md](./FIELD_MAPPING_STANDARDS.md)
- **开发规范**: [UNIFIED_DEVELOPMENT_STANDARDS.md](./UNIFIED_DEVELOPMENT_STANDARDS.md)
- **编译错误修复总结**: [COMPILATION_FIX_COMPLETE_SUMMARY.md](./COMPILATION_FIX_COMPLETE_SUMMARY.md)

---

**制定人**: IOE-DREAM 架构委员会  
**版本**: v1.0.0  
**最后更新**: 2025-01-30

