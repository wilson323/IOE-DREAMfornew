# DAO Completion Specification

## Purpose
This specification defines the requirements for implementing missing Data Access Object (DAO) classes in the IOE-DREAM project to resolve compilation errors and enable proper data access functionality.

## ADDED Requirements

### Requirement: AreaPersonDao Implementation
系统 SHALL 提供AreaPersonDao接口和实现，支持基于区域的人员数据访问和权限验证功能。

#### Scenario: Area-Based Person Data Access
- **WHEN** ConsumePermissionService需要基于区域验证权限时
- **AND** 系统需要查询特定区域的人员信息
- **THEN** 系统 SHALL 通过AreaPersonDao提供人员区域数据访问能力

The system requires area-based permission control to manage data access based on geographic or logical areas. The ConsumePermissionService and other components need to query person-area relationships to validate permissions, but the AreaPersonDao interface and implementation are missing, causing compilation errors.

#### Scenario: Person Area Relationship Queries
- **WHEN** 系统管理人员的区域访问权限时
- **AND** 需要验证人员与区域的关系
- **THEN** 系统 SHALL 提供高效的区域人员关系查询功能

The system manages person access to specific areas and needs to validate these relationships for permission control. Efficient query capabilities are essential for performance and scalability.

#### Scenario: Permission-Based Data Filtering
- **WHEN** RBAC系统需要基于区域权限过滤数据访问时
- **AND** 需要验证用户对特定区域的权限
- **THEN** 系统 SHALL 提供高效的权限验证查询机制

Area-based data filtering is crucial for implementing proper data security and ensuring users can only access data they are authorized to view based on their area permissions.

### Requirement: Maintain DAO Interface Consistency
系统 SHALL 确保新实现的DAO类遵循项目既定的DAO模式和架构规范。

#### Scenario: DAO Pattern Compliance
- **WHEN** 实现新的DAO类时
- **AND** 项目已建立统一的DAO开发模式
- **THEN** 系统 SHALL 遵循相同的模式和约定

The project has established patterns for DAO implementation using MyBatis Plus and specific naming conventions. New DAO classes must adhere to these established patterns to maintain code consistency and prevent architectural drift.

## Implementation Details

### AreaPersonDao Interface Structure
```java
public interface AreaPersonDao extends BaseMapper<AreaPersonEntity> {

    /**
     * 根据区域ID查询人员列表
     */
    List<AreaPersonEntity> selectByAreaId(Long areaId);

    /**
     * 根据人员ID查询区域列表
     */
    List<AreaPersonEntity> selectByPersonId(Long personId);

    /**
     * 检查人员是否有区域访问权限
     */
    boolean hasAreaAccess(Long personId, Long areaId);

    /**
     * 批量检查人员区域权限
     */
    Map<Long, List<Long>> batchGetPersonAreas(List<Long> personIds);

    /**
     * 分页查询区域人员关系
     */
    Page<AreaPersonEntity> selectPage(Page<AreaPersonEntity> page, AreaPersonQuery query);
}
```

### Implementation Requirements
- **MyBatis Plus Integration**: Use BaseMapper for basic CRUD operations
- **Custom Queries**: Implement complex queries using @Select annotations
- **Performance Optimization**: Add appropriate database indexes
- **Caching Strategy**: Integrate with project's L1/L2 cache system
- **Error Handling**: Proper exception handling and logging

### Entity Structure Requirements
```java
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_area_person")
@Schema(description = "区域人员关系")
public class AreaPersonEntity extends BaseEntity {

    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;

    @TableField("person_id")
    @Schema(description = "人员ID")
    private Long personId;

    @TableField("access_level")
    @Schema(description = "访问级别")
    private Integer accessLevel;

    @TableField("is_active")
    @Schema(description = "是否激活")
    private Boolean isActive;
}
```

### Dependency Integration
- **Service Layer Injection**: Proper @Resource injection in service classes
- **Transaction Management**: Service-level transaction boundaries
- **Cache Integration**: Use project's established cache patterns
- **Validation**: Input parameter validation and business rule enforcement

## Cross-Reference Requirements
- **Related to**: Permission validation functionality
- **Related to**: AccessControl domain model
- **Related to**: SmartArea business logic
- **References**: DataScope enum (AREA value completion)

## Testing Requirements
- Unit tests for all DAO methods
- Integration tests with database
- Performance tests for query optimization
- Cache integration testing