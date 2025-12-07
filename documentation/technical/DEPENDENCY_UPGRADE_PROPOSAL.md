# 依赖升级提案

> **基于**: [DEPENDENCY_ANALYSIS_REPORT.md](./DEPENDENCY_ANALYSIS_REPORT.md)  
> **优先级**: P0-P1（紧急和高优先级）  
> **预计影响**: 低风险，向后兼容

---

## 🚨 P0级：紧急修复（立即执行）

### MySQL Connector迁移

**问题**: `mysql:mysql-connector-java` 已停止维护（1003天，存在安全风险

**解决方案**: 迁移到新的官方依赖

#### 步骤1：更新根pom.xml

```xml
<properties>
    <!-- 旧版本（删除） -->
    <!-- <mysql.version>8.0.33</mysql.version> -->
    
    <!-- 新版本 -->
    <mysql.version>8.3.0</mysql.version>
</properties>
```

#### 步骤2：更新dependencyManagement

```xml
<dependencyManagement>
    <dependencies>
        <!-- 删除旧依赖 -->
        <!--
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.version}</version>
        </dependency>
        -->
        
        <!-- 添加新依赖 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>${mysql.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 步骤3：更新各微服务的pom.xml

在所有使用MySQL连接器的微服务中，更新依赖声明：

```xml
<!-- 旧依赖 -->
<!--
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
-->

<!-- 新依赖 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

**验证步骤**:
1. 运行单元测试，确保数据库连接正常
2. 检查是否有使用内部API的代码
3. 验证连接池配置是否兼容

---

## ✅ P1级：高优先级升级（1-2周内）

### 补丁版本升级（低风险）

#### 更新根pom.xml的properties

```xml
<properties>
    <!-- 数据库相关 -->
    <mybatis-plus.version>3.5.15</mybatis-plus.version>  <!-- 3.5.7 → 3.5.15 -->
    <druid.version>1.2.27</druid.version>                  <!-- 1.2.21 → 1.2.27 -->
    
    <!-- 工具库 -->
    <hutool.version>5.8.42</hutool.version>                <!-- 5.8.39 → 5.8.42 -->
    <fastjson2.version>2.0.60</fastjson2.version>          <!-- 2.0.57 → 2.0.60 -->
    <lombok.version>1.18.42</lombok.version>               <!-- 1.18.34 → 1.18.42 -->
    
    <!-- Apache POI -->
    <poi.version>5.5.1</poi.version>                       <!-- 5.4.1 → 5.5.1 -->
    
    <!-- 其他 -->
    <mapstruct.version>1.6.3</mapstruct.version>           <!-- 1.5.5.Final → 1.6.3 -->
    <jjwt.version>0.13.0</jjwt.version>                    <!-- 0.12.3 → 0.13.0 -->
</properties>
```

**注意**: 
- Fastjson2使用标准版本 `2.0.60`，而非Android版本 `2.0.60.android8`
- MapStruct从 `1.5.5.Final` 升级到 `1.6.3`，需要检查是否有破坏性变更

---

## 📋 完整升级后的pom.xml片段

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

    <!-- Spring Boot版本 -->
    <spring-boot.version>3.5.8</spring-boot.version>
    <spring-cloud.version>2023.0.3</spring-cloud.version>
    <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>

    <!-- 数据库相关 -->
    <mybatis-plus.version>3.5.15</mybatis-plus.version>
    <druid.version>1.2.27</druid.version>
    <mysql.version>8.3.0</mysql.version>  <!-- 已迁移到mysql-connector-j -->

    <!-- 工具库 -->
    <hutool.version>5.8.42</hutool.version>
    <fastjson.version>2.0.60</fastjson.version>
    <fastjson2.version>2.0.60</fastjson2.version>
    <lombok.version>1.18.42</lombok.version>

    <!-- Apache POI -->
    <poi.version>5.5.1</poi.version>

    <!-- 其他 -->
    <knife4j.version>4.4.0</knife4j.version>
    <sa-token.version>1.44.0</sa-token.version>
    <mapstruct.version>1.6.3</mapstruct.version>
    <jjwt.version>0.13.0</jjwt.version>
    <sleuth.version>3.1.11</sleuth.version>
</properties>
```

---

## 🔍 升级验证清单

### 编译验证
- [ ] 项目能够成功编译
- [ ] 无编译错误
- [ ] 无编译警告（关键警告需处理）

### 功能验证
- [ ] 数据库连接正常
- [ ] MyBatis-Plus查询功能正常
- [ ] JSON序列化/反序列化正常
- [ ] JWT token生成和验证正常
- [ ] Excel导入导出功能正常

### 性能验证
- [ ] 数据库连接池性能正常
- [ ] 无明显性能退化

### 集成测试
- [ ] 所有微服务启动正常
- [ ] 服务间调用正常
- [ ] API接口测试通过

---

## 📝 升级执行记录

### 执行日期
- **计划开始**: 2025-01-30
- **预计完成**: 2025-02-13（2周）

### 执行步骤
1. **第1天**: MySQL Connector迁移（P0）
2. **第2-3天**: 补丁版本升级（P1）
3. **第4-5天**: 功能验证和测试
4. **第6-7天**: 性能测试和优化
5. **第8-10天**: 集成测试和问题修复
6. **第11-14天**: 生产环境验证

### 回滚计划
- 所有升级操作在独立分支进行
- 保留升级前的pom.xml备份
- 如有问题，立即回滚到稳定版本

---

## ⚠️ 已知问题和注意事项

### 1. Fastjson2版本选择
- **问题**: Maven Central显示最新版本为 `2.0.60.android8`
- **解决**: 使用标准版本 `2.0.60`，检查Maven Central确认可用性

### 2. MapStruct升级注意事项
- **版本跨度**: `1.5.5.Final` → `1.6.3`
- **建议**: 检查[MapStruct迁移指南](https://mapstruct.org/documentation/stable/reference/html/#migration)
- **风险**: 低（向后兼容）

### 3. JJWT升级注意事项
- **版本跨度**: `0.12.3` → `0.13.0`
- **建议**: 检查[JJWT变更日志](https://github.com/jwtk/jjwt/releases)
- **风险**: 低（API兼容）

---

## 📊 升级影响评估

### 代码变更
- **预计文件数**: 1个（根pom.xml）
- **预计代码行数**: ~20行
- **预计影响范围**: 所有微服务模块

### 测试范围
- **单元测试**: 100%覆盖
- **集成测试**: 核心功能覆盖
- **性能测试**: 关键路径覆盖

### 风险评估
- **P0升级风险**: 低（MySQL Connector迁移）
- **P1升级风险**: 极低（补丁版本升级）
- **总体风险**: 低

---

**提案状态**: 📋 待审批  
**审批人**: 架构团队  
**执行人**: 开发团队
