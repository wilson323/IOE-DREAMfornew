# 📋 PR审查 - 技术栈一致性检查清单

**版本**: v1.0.0  
**适用范围**: 所有涉及文档的PR  
**检查级别**: P0级（必须通过）

---

## 🎯 检查目标

确保PR中的文档技术栈描述与 [技术栈标准规范](./TECHNOLOGY_STACK_STANDARD.md) 完全一致。

---

## ✅ PR审查检查清单

### 1. 技术栈标准引用检查 (P0级)

- [ ] **新文档必须引用技术栈标准**
  - 检查项: 文档中是否包含对 `TECHNOLOGY_STACK_STANDARD.md` 的引用
  - 标准格式:
    ```markdown
    本项目技术栈遵循 [技术栈标准规范](./technical/TECHNOLOGY_STACK_STANDARD.md)
    ```
  - 违规示例: 文档中直接描述技术栈，未引用标准文档

- [ ] **禁止硬编码技术栈版本**
  - 检查项: 文档中是否直接写死技术栈版本号
  - 违规示例:
    ```markdown
    ❌ Spring Boot 3.2.5  # 错误：硬编码版本
    ❌ Spring Cloud 2023.0.4  # 错误：硬编码版本
    ```
  - 正确示例:
    ```markdown
    ✅ 参考 [技术栈标准规范](./technical/TECHNOLOGY_STACK_STANDARD.md)
    ✅ Spring Boot 3.5.8（引用标准规范）
    ```

### 2. 技术栈版本一致性检查 (P0级)

检查文档中提到的技术栈版本是否与标准规范一致：

- [ ] **Spring Boot版本检查**
  - 标准版本: **3.5.8**
  - 检查项: 文档中所有Spring Boot版本必须为3.5.8
  - 违规示例: `Spring Boot 3.2.5`, `Spring Boot 3.3.x`, `Spring Boot 3.4.x`

- [ ] **Spring Cloud版本检查**
  - 标准版本: **2025.0.0**
  - 检查项: 文档中所有Spring Cloud版本必须为2025.0.0
  - 违规示例: `Spring Cloud 2023.0.4`, `Spring Cloud 2023.0.x`

- [ ] **Spring Cloud Alibaba版本检查**
  - 标准版本: **2025.0.0.0**
  - 检查项: 文档中所有Spring Cloud Alibaba版本必须为2025.0.0.0
  - 违规示例: `Spring Cloud Alibaba 2022.0.0.0`（除非是历史说明）

- [ ] **Java版本检查**
  - 标准版本: **17**
  - 检查项: 文档中所有Java版本必须为17
  - 违规示例: `Java 8`, `Java 11`, `JDK 1.8`

- [ ] **MyBatis-Plus版本检查**
  - 标准版本: **3.5.15**
  - 检查项: 文档中所有MyBatis-Plus版本必须为3.5.15
  - 违规示例: `MyBatis-Plus 3.5.5`, `MyBatis-Plus 3.5.7`

- [ ] **MySQL版本检查**
  - 标准版本: **8.0.35**
  - 检查项: 文档中所有MySQL版本必须为8.0.35
  - 违规示例: `MySQL 8.0.33`, `MySQL 8.0.x`

- [ ] **Druid版本检查**
  - 标准版本: **1.2.25**
  - 检查项: 文档中所有Druid版本必须为1.2.25
  - 违规示例: `Druid 1.2.20`, `Druid 1.2.x`

- [ ] **Lombok版本检查**
  - 标准版本: **1.18.42**
  - 检查项: 文档中所有Lombok版本必须为1.18.42
  - 违规示例: `Lombok 1.18.30`, `Lombok 1.18.x`

### 3. 历史版本说明检查 (P1级)

- [ ] **历史版本标注**
  - 检查项: 如果文档中提及历史版本，必须明确标注为"历史版本"或"已升级"
  - 正确示例:
    ```markdown
    ✅ Spring Cloud Alibaba 2022.0.0.0（历史版本，当前已升级至2025.0.0.0）
    ✅ 在升级前，项目使用Spring Boot 3.2.5（已升级至3.5.8）
    ```

### 4. 前端技术栈检查 (P1级)

- [ ] **Vue版本检查**
  - 标准版本: **3.4.x**
  - 检查项: 文档中Vue版本必须为3.4.x

- [ ] **Vite版本检查**
  - 标准版本: **5.x**
  - 检查项: 文档中Vite版本必须为5.x

- [ ] **Ant Design Vue版本检查**
  - 标准版本: **4.x**
  - 检查项: 文档中Ant Design Vue版本必须为4.x

---

## 🔍 自动化检查命令

### PowerShell检查脚本

```powershell
# 检查文档中的技术栈版本
$docPath = "documentation"
$standardFile = "documentation\technical\TECHNOLOGY_STACK_STANDARD.md"

# 检查Spring Boot版本
Get-ChildItem -Path $docPath -Recurse -Filter "*.md" | 
    Select-String -Pattern "Spring Boot\s+3\.[0-4]\.|Spring Boot\s+3\.2\." | 
    Where-Object { $_.Path -notmatch "TECHNOLOGY_STACK_STANDARD|archive|bak" } |
    Select-Object Path, LineNumber, Line

# 检查是否引用技术栈标准
Get-ChildItem -Path $docPath -Recurse -Filter "*.md" | 
    Where-Object { 
        $content = Get-Content $_.FullName -Raw
        $content -notmatch "TECHNOLOGY_STACK_STANDARD" -and
        $content -match "Spring Boot|技术栈" -and
        $_.FullName -notmatch "TECHNOLOGY_STACK_STANDARD|archive|bak"
    } |
    Select-Object FullName
```

---

## 📊 检查结果处理

### 通过标准

- ✅ **所有P0级检查项通过**
- ✅ **技术栈版本与标准规范一致**
- ✅ **新文档已引用技术栈标准规范**

### 拒绝标准

- ❌ **存在P0级违规**（必须修复后重新提交）
- ❌ **技术栈版本与标准不一致**
- ❌ **新文档未引用技术栈标准规范**

### 有条件通过

- ⚠️ **存在P1级问题**（可在后续PR中修复）
- ⚠️ **历史版本说明未标注**（需补充标注）

---

## 📝 PR审查评论模板

### 发现技术栈不一致时

```markdown
## 🔴 技术栈一致性检查失败

**问题**: 文档中的技术栈版本与标准规范不一致

**发现的问题**:
- Spring Boot版本: 文档中为 `3.2.5`，标准为 `3.5.8`
- Spring Cloud版本: 文档中为 `2023.0.4`，标准为 `2025.0.0`

**修复建议**:
1. 引用 [技术栈标准规范](./technical/TECHNOLOGY_STACK_STANDARD.md)
2. 更新所有技术栈版本为标准版本
3. 或使用标准引用模板，避免硬编码版本

**参考**: [技术栈标准规范](./technical/TECHNOLOGY_STACK_STANDARD.md)
```

### 发现未引用标准时

```markdown
## 🔴 技术栈标准引用缺失

**问题**: 新文档未引用技术栈标准规范

**修复建议**:
在文档中添加以下内容：

```markdown
## 技术栈

本项目技术栈遵循 [技术栈标准规范](./technical/TECHNOLOGY_STACK_STANDARD.md)：

- **后端**: Spring Boot 3.5.8 + Java 17 + MyBatis-Plus 3.5.15
- **前端**: Vue 3.4.x + Vite 5.x + Ant Design Vue 4.x

详细版本信息请参考 [技术栈标准规范](./technical/TECHNOLOGY_STACK_STANDARD.md)。
```

**参考**: [技术栈标准规范](./technical/TECHNOLOGY_STACK_STANDARD.md)
```

---

## 📚 相关文档

- [技术栈标准规范](./TECHNOLOGY_STACK_STANDARD.md) - **唯一真相源**
- [文档管理规范](../DOCUMENTATION_MANAGEMENT_STANDARDS.md)
- [文档审查机制](../DOCUMENT_REVIEW_MECHANISM.md)

---

**最后更新**: 2025-01-30  
**维护团队**: IOE-DREAM 架构委员会
