# Maven Enforcer Plugin架构规则配置

**用途**: 自动检查并拦截违反架构规范的代码  
**应用位置**: 根pom.xml的`<build><plugins>`部分  
**生效时机**: mvn compile/install时自动检查

---

## 📋 配置内容

将以下配置添加到`D:\IOE-DREAM\pom.xml`的`<build><plugins>`section中：

```xml
<!-- Maven Enforcer Plugin：架构规则强制检查 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <executions>
        <!-- 规则1：禁止在业务服务中定义DAO -->
        <execution>
            <id>enforce-no-dao-in-services</id>
            <phase>validate</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <requireFilesDontExist>
                        <message>
❌ 架构违规：禁止在业务服务中定义DAO！
📋 规范：所有DAO必须在microservices-common中定义
📖 参考：CLAUDE.md 第1条架构规范
                        </message>
                        <files>
                            <file>**/ioedream-*-service/**/dao/**Dao.java</file>
                            <file>**/ioedream-*-service/**/repository/**Dao.java</file>
                        </files>
                    </requireFilesDontExist>
                </rules>
            </configuration>
        </execution>

        <!-- 规则2：禁止在业务服务中定义Entity -->
        <execution>
            <id>enforce-no-entity-in-services</id>
            <phase>validate</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <requireFilesDontExist>
                        <message>
❌ 架构违规：禁止在业务服务中定义Entity！
📋 规范：所有Entity必须在microservices-common中定义
📖 参考：CLAUDE.md 第1条架构规范
                        </message>
                        <files>
                            <file>**/ioedream-*-service/**/entity/**Entity.java</file>
                        </files>
                    </requireFilesDontExist>
                </rules>
                <fail>true</fail>
            </configuration>
        </execution>

        <!-- 规则3：强制使用Java 17 -->
        <execution>
            <id>enforce-java-version</id>
            <phase>validate</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <requireJavaVersion>
                        <version>[17,18)</version>
                        <message>
❌ 必须使用Java 17！
📋 当前项目基于Spring Boot 3.x，要求Java 17+
                        </message>
                    </requireJavaVersion>
                </rules>
            </configuration>
        </execution>

        <!-- 规则4：强制使用Maven 3.8+ -->
        <execution>
            <id>enforce-maven-version</id>
            <phase>validate</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <requireMavenVersion>
                        <version>[3.8.0,)</version>
                        <message>
❌ 必须使用Maven 3.8.0或更高版本！
                        </message>
                    </requireMavenVersion>
                </rules>
            </configuration>
        </execution>

        <!-- 规则5：禁止依赖冲突 -->
        <execution>
            <id>enforce-no-dependency-convergence</id>
            <phase>validate</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <dependencyConvergence>
                        <message>
⚠️ 发现依赖版本冲突！
📋 请在根pom.xml的dependencyManagement中统一版本
                        </message>
                    </dependencyConvergence>
                </rules>
                <fail>false</fail> <!-- 警告但不阻止构建 -->
            </configuration>
        </execution>

        <!-- 规则6：禁止快照依赖（生产环境） -->
        <execution>
            <id>enforce-no-snapshots</id>
            <phase>validate</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <requireReleaseDeps>
                        <message>
❌ 生产环境禁止使用SNAPSHOT依赖！
📋 请使用正式发布版本
                        </message>
                        <onlyWhenRelease>true</onlyWhenRelease>
                    </requireReleaseDeps>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## 🎯 使用说明

### 添加配置

1. 打开`D:\IOE-DREAM\pom.xml`
2. 找到`<build><plugins>`部分
3. 将上述配置粘贴到`</plugins>`标签之前
4. 保存文件

### 验证配置

```powershell
# 运行验证
cd D:\IOE-DREAM
mvn validate

# 如果有违规，会看到类似错误：
# [ERROR] ❌ 架构违规：禁止在业务服务中定义DAO！
# [ERROR] 📋 规范：所有DAO必须在microservices-common中定义
```

### 配置效果

配置生效后，如果有人试图在业务服务中创建DAO：

```
❌ BUILD FAILURE
[ERROR] 架构违规：禁止在业务服务中定义DAO！
[ERROR] 违规文件：ioedream-xxx-service/src/.../dao/XxxDao.java
[ERROR] 规范：所有DAO必须在microservices-common中定义
[ERROR] 参考：CLAUDE.md 第1条架构规范
```

---

## 🔒 规则说明

### 规则1：禁止DAO在业务服务中

**检查路径**:
- `**/ioedream-*-service/**/dao/**Dao.java`
- `**/ioedream-*-service/**/repository/**Dao.java`

**允许路径**:
- `microservices-common/src/main/java/.../dao/`

**规范依据**: CLAUDE.md第1条

---

### 规则2：禁止Entity在业务服务中

**检查路径**:
- `**/ioedream-*-service/**/entity/**Entity.java`

**允许路径**:
- `microservices-common/src/main/java/.../entity/`

**规范依据**: CLAUDE.md第1条

---

### 规则3：Java版本检查

**要求**: Java 17  
**原因**: Spring Boot 3.x要求

---

### 规则4：Maven版本检查

**要求**: Maven 3.8.0+  
**原因**: 支持最新特性和安全更新

---

### 规则5：依赖冲突检查

**目的**: 确保依赖版本一致性  
**级别**: 警告（不阻止构建）

---

### 规则6：禁止SNAPSHOT依赖

**目的**: 生产环境稳定性  
**级别**: 仅在发布时检查

---

## 📊 预期效果

### 防止的问题

1. ✅ **防止DAO冗余**: 自动拦截在业务服务中创建DAO
2. ✅ **防止Entity分散**: 自动拦截在业务服务中创建Entity
3. ✅ **确保Java版本**: 避免版本不兼容
4. ✅ **确保Maven版本**: 避免构建工具问题
5. ✅ **依赖管理**: 及时发现依赖冲突
6. ✅ **版本管理**: 避免不稳定的SNAPSHOT依赖

### 提前发现

- 在本地开发时就发现违规
- 在CI/CD时自动检查
- 在Code Review前拦截

---

## 🔄 后续扩展

### 可以添加的其他规则

```xml
<!-- 禁止使用@Repository注解 -->
<bannedAnnotation>
    <annotation>org.springframework.stereotype.Repository</annotation>
    <message>禁止使用@Repository，请使用@Mapper</message>
</bannedAnnotation>

<!-- 禁止使用@Autowired注解 -->
<bannedAnnotation>
    <annotation>org.springframework.beans.factory.annotation.Autowired</annotation>
    <message>禁止使用@Autowired，请使用@Resource</message>
</bannedAnnotation>

<!-- 禁止使用特定依赖 -->
<bannedDependencies>
    <excludes>
        <exclude>commons-logging:commons-logging</exclude>
        <exclude>log4j:log4j</exclude>
    </excludes>
</bannedDependencies>
```

---

## 📝 注意事项

### 配置注意点

1. **不要过度限制**: 规则太严可能影响正常开发
2. **逐步添加**: 先添加核心规则，再逐步完善
3. **团队沟通**: 让团队了解规则目的和意义
4. **适时调整**: 根据实际情况优化规则

### 常见问题

**Q1**: 规则太严，无法构建？
- A: 可以临时添加`<skip>true</skip>`跳过检查
- 但必须在提交前修复违规

**Q2**: 如何排除特定文件？
- A: 使用`<excludes>`标签排除特定路径

**Q3**: 规则不生效？
- A: 检查Maven版本，确保3.8+
- A: 运行`mvn validate`手动触发

---

## ✅ 验证清单

配置完成后，验证：

- [ ] pom.xml语法正确
- [ ] mvn validate执行成功
- [ ] 违规文件被正确拦截
- [ ] 正常文件不受影响
- [ ] 错误提示信息清晰

---

**文档维护人**: IOE-DREAM架构委员会  
**最后更新**: 2025-12-03  
**状态**: ✅ 配置ready，待添加到pom.xml

