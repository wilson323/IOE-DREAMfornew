# BaseEntity 问题分析报告

## BaseEntity 结构

`BaseEntity` 位于 `microservices-common/src/main/java/net/lab1024/sa/common/entity/BaseEntity.java`，使用了 `@Data` 注解。

### 字段定义
```java
@Data
public abstract class BaseEntity implements Serializable {
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUserId;
    private Long updateUserId;
    private Integer deletedFlag;
    private Integer version;
}
```

### Lombok 自动生成的方法
使用 `@Data` 注解后，Lombok 会自动生成以下方法：
- `getCreateTime()` / `setCreateTime(LocalDateTime)`
- `getUpdateTime()` / `setUpdateTime(LocalDateTime)`
- `getCreateUserId()` / `setCreateUserId(Long)`
- `getUpdateUserId()` / `setUpdateUserId(Long)`
- `getDeletedFlag()` / `setDeletedFlag(Integer)`
- `getVersion()` / `setVersion(Integer)`
- `toString()`, `equals()`, `hashCode()`

## 问题分析

### 1. 编译时 vs 运行时
- **编译时**：Lombok 在编译阶段会生成这些方法
- **运行时**：这些方法是真实存在的，可以被调用
- **IDE 识别**：如果 IDE 没有安装 Lombok 插件，可能无法识别这些方法

### 2. 实体类继承情况
所有实体类都正确继承了 BaseEntity：
- `AttendanceRecordEntity extends BaseEntity`
- `AttendanceRuleEntity extends BaseEntity`
- `AttendanceScheduleEntity extends BaseEntity`
- 等等...

### 3. 方法调用情况
以下文件中使用了 BaseEntity 的方法：
- `AttendanceSyncService.java`: `setCreateTime()`, `setUpdateTime()`, `setDeletedFlag()`
- `AttendanceExceptionService.java`: `setCreateTime()`, `setUpdateTime()`, `setDeletedFlag()`
- `ShiftsServiceImpl.java`: `setUpdateTime()`, `setCreateTime()`, `setDeletedFlag()`
- `AttendanceRuleServiceImpl.java`: `getDeletedFlag()`, `setCreateTime()`, `setUpdateTime()`, `setDeletedFlag()`
- 等等...

## 解决方案

### 方案1：确保 Lombok 配置正确 ✅（推荐）
1. **检查 pom.xml**：确保 Lombok 依赖正确配置
2. **检查 IDE**：确保安装了 Lombok 插件
3. **重新构建**：执行 `mvn clean compile` 让 Lombok 生成方法

### 方案2：如果 Lombok 不工作，手动添加方法（不推荐）
如果 Lombok 无法正常工作，可以在 BaseEntity 中手动添加这些方法：
```java
public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
}
// ... 其他方法
```

但这种方式不推荐，因为：
- 违反了 DRY 原则
- 需要维护大量样板代码
- Lombok 已经提供了解决方案

### 方案3：检查 Lombok 配置
确保 `pom.xml` 中有正确的 Lombok 配置：
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## 验证步骤

1. **构建 common 模块**：
   ```bash
   cd microservices-common
   mvn clean install -DskipTests
   ```

2. **检查编译结果**：
   ```bash
   cd ../ioedream-attendance-service
   mvn clean compile
   ```

3. **查看生成的类文件**：
   编译后，可以在 `target/classes` 目录下查看生成的字节码

## 结论

BaseEntity 的设计是正确的。如果出现方法未找到的错误，通常是因为：
1. Lombok 未正确配置
2. IDE 未安装 Lombok 插件
3. 需要重新编译项目

**建议操作**：
1. 确保 Lombok 依赖和插件配置正确
2. 重新构建 common 模块和 attendance-service
3. 如果使用 IDE，安装并启用 Lombok 插件

## 相关文件

- BaseEntity: `microservices-common/src/main/java/net/lab1024/sa/common/entity/BaseEntity.java`
- 使用示例: `microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/domain/entity/AttendanceRecordEntity.java`

