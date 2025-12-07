# Git Pre-commit Hook设置指南

**用途**: 在代码提交前自动检查编码规范和架构规则  
**应用位置**: `.git/hooks/pre-commit`  
**生效时机**: git commit时自动执行

---

## 📋 Hook脚本内容

### Windows PowerShell版本

创建文件：`.git/hooks/pre-commit.ps1`

```powershell
#!/usr/bin/env pwsh
# IOE-DREAM Git Pre-commit Hook
# 检查代码编码规范和架构合规性

Write-Host "🔍 IOE-DREAM Pre-commit检查开始..." -ForegroundColor Cyan

$exitCode = 0

# 检查1：禁止在业务服务中提交DAO文件
Write-Host "`n检查1：DAO文件位置合规性..." -ForegroundColor Yellow
$daoFiles = git diff --cached --name-only --diff-filter=ACM | Select-String "ioedream-.*-service.*Dao\.java"
if ($daoFiles) {
    Write-Host "❌ 错误：检测到在业务服务中定义DAO！" -ForegroundColor Red
    Write-Host "违规文件：" -ForegroundColor Red
    $daoFiles | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host "`n📋 规范：所有DAO必须在microservices-common中定义" -ForegroundColor Yellow
    Write-Host "📖 参考：CLAUDE.md 第1条架构规范" -ForegroundColor Yellow
    $exitCode = 1
}

# 检查2：禁止在业务服务中提交Entity文件
Write-Host "`n检查2：Entity文件位置合规性..." -ForegroundColor Yellow
$entityFiles = git diff --cached --name-only --diff-filter=ACM | Select-String "ioedream-.*-service.*Entity\.java"
if ($entityFiles) {
    Write-Host "❌ 错误：检测到在业务服务中定义Entity！" -ForegroundColor Red
    Write-Host "违规文件：" -ForegroundColor Red
    $entityFiles | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host "`n📋 规范：所有Entity必须在microservices-common中定义" -ForegroundColor Yellow
    $exitCode = 1
}

# 检查3：禁止提交包含@Repository注解的文件
Write-Host "`n检查3：@Repository注解检查..." -ForegroundColor Yellow
$javaFiles = git diff --cached --name-only --diff-filter=ACM | Where-Object { $_ -match "\.java$" }
foreach ($file in $javaFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -match "@Repository") {
            Write-Host "❌ 错误：检测到@Repository注解！" -ForegroundColor Red
            Write-Host "违规文件：$file" -ForegroundColor Red
            Write-Host "`n📋 规范：必须使用@Mapper注解" -ForegroundColor Yellow
            $exitCode = 1
        }
    }
}

# 检查4：禁止提交包含@Autowired注解的文件
Write-Host "`n检查4：@Autowired注解检查..." -ForegroundColor Yellow
foreach ($file in $javaFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -match "@Autowired") {
            Write-Host "❌ 错误：检测到@Autowired注解！" -ForegroundColor Red
            Write-Host "违规文件：$file" -ForegroundColor Red
            Write-Host "`n📋 规范：必须使用@Resource注解" -ForegroundColor Yellow
            $exitCode = 1
        }
    }
}

# 检查5：检查文件编码格式
Write-Host "`n检查5：文件编码格式检查..." -ForegroundColor Yellow
foreach ($file in $javaFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        # 检查是否包含全角字符（除中文汉字）
        if ($content -match "[：，（）【】！？；]") {
            Write-Host "⚠️  警告：检测到全角标点符号！" -ForegroundColor Yellow
            Write-Host "文件：$file" -ForegroundColor Yellow
            Write-Host "建议：将全角标点替换为半角" -ForegroundColor Yellow
            # 不阻止提交，仅警告
        }
    }
}

# 检查6：检查SQL中的deleted字段
Write-Host "`n检查6：SQL删除标记检查..." -ForegroundColor Yellow
foreach ($file in $javaFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -match "deleted\s*=\s*[01]") {
            Write-Host "❌ 错误：检测到非标准的deleted字段！" -ForegroundColor Red
            Write-Host "违规文件：$file" -ForegroundColor Red
            Write-Host "`n📋 规范：必须使用deleted_flag字段" -ForegroundColor Yellow
            $exitCode = 1
        }
    }
}

# 总结
Write-Host "`n" + "="*60 -ForegroundColor Cyan
if ($exitCode -eq 0) {
    Write-Host "✅ 所有检查通过！可以提交。" -ForegroundColor Green
} else {
    Write-Host "❌ 发现违规问题，请修复后再提交！" -ForegroundColor Red
    Write-Host "`n💡 提示：" -ForegroundColor Yellow
    Write-Host "  1. 参考CLAUDE.md了解架构规范" -ForegroundColor Yellow
    Write-Host "  2. 运行 mvn validate 进行本地验证" -ForegroundColor Yellow
    Write-Host "  3. 如需帮助，请查阅架构文档" -ForegroundColor Yellow
}
Write-Host "="*60 -ForegroundColor Cyan

exit $exitCode
```

---

## 🚀 安装方法

### 方法1：手动安装（推荐）

```powershell
# 1. 切换到项目根目录
cd D:\IOE-DREAM

# 2. 创建hooks目录（如果不存在）
if (!(Test-Path ".git\hooks")) {
    New-Item -ItemType Directory -Path ".git\hooks"
}

# 3. 复制pre-commit脚本
Copy-Item "GIT_HOOKS_SETUP.md" -Destination ".git\hooks\pre-commit.ps1"

# 4. 配置Git使用PowerShell执行hooks
git config core.hooksPath .git/hooks
git config core.hookExecutable powershell.exe
```

### 方法2：使用安装脚本

创建`install-git-hooks.ps1`：

```powershell
Write-Host "安装Git Pre-commit Hook..." -ForegroundColor Cyan

# 复制hook脚本
$hookContent = Get-Content "GIT_HOOKS_SETUP.md" -Raw
$hookContent | Out-File ".git\hooks\pre-commit" -Encoding UTF8

# 配置Git
git config core.hooksPath .git/hooks

Write-Host "✅ Git hooks安装完成！" -ForegroundColor Green
```

---

## 🧪 测试Hook

### 测试方法

```powershell
# 测试1：尝试提交一个违规DAO
# 在业务服务中创建测试文件
New-Item -ItemType File -Path "microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\dao\TestDao.java"

# 尝试提交
git add .
git commit -m "test: 测试pre-commit hook"

# 预期结果：
# ❌ 错误：检测到在业务服务中定义DAO！
# 提交被拒绝
```

### 验证Hook工作

```powershell
# 应该看到类似输出：
🔍 IOE-DREAM Pre-commit检查开始...

检查1：DAO文件位置合规性...
❌ 错误：检测到在业务服务中定义DAO！
违规文件：
  - microservices/ioedream-access-service/src/.../TestDao.java

📋 规范：所有DAO必须在microservices-common中定义
📖 参考：CLAUDE.md 第1条架构规范
```

---

## ⚙️ Hook配置选项

### 跳过Hook（紧急情况）

```powershell
# 临时跳过hook检查（不推荐）
git commit -m "message" --no-verify

# ⚠️ 警告：跳过检查后必须确保代码符合规范！
```

### 只做警告不阻止

修改脚本中的`$exitCode = 1`为`$exitCode = 0`

### 自定义检查规则

在脚本中添加自己的检查逻辑

---

## 📊 预期效果

### 防止的问题

| 问题类型 | 检查机制 | 效果 |
|---------|---------|------|
| DAO在业务服务 | 文件路径检查 | ❌ 拦截提交 |
| Entity在业务服务 | 文件路径检查 | ❌ 拦截提交 |
| @Repository使用 | 注解检查 | ❌ 拦截提交 |
| @Autowired使用 | 注解检查 | ❌ 拦截提交 |
| 全角字符 | 编码检查 | ⚠️  警告 |
| deleted字段 | SQL检查 | ❌ 拦截提交 |

### 提升的质量

1. ✅ **架构合规性**: 100%（自动拦截违规）
2. ✅ **编码规范性**: 提前发现问题
3. ✅ **开发效率**: 减少返工
4. ✅ **代码质量**: 提交前保证质量

---

## 🔄 持续改进

### 可以添加的其他检查

```powershell
# 检查7：禁止提交包含中文的变量名
if ($content -match '(private|public|protected).*[\u4e00-\u9fa5].*[=;]') {
    Write-Host "❌ 变量名包含中文！"
}

# 检查8：禁止提交调试代码
if ($content -match '(System\.out\.println|console\.log|debugger)') {
    Write-Host "⚠️  发现调试代码！"
}

# 检查9：检查文件大小
$size = (Get-Item $file).Length / 1KB
if ($size -gt 400) {  # 超过400KB
    Write-Host "⚠️  文件过大：$([math]::Round($size, 2))KB"
}

# 检查10：检查方法行数
$methods = ($content | Select-String -Pattern "public.*\{" -AllMatches).Matches
foreach ($method in $methods) {
    # 检查方法是否超过50行
}
```

---

## 📝 使用建议

### 开发工作流

```
1. 编写代码
2. 本地测试
3. git add添加文件
4. git commit提交
   ↓
   自动触发pre-commit hook
   ↓
   检查通过✅ → 提交成功
   检查失败❌ → 提示修复

5. 修复问题
6. 重新commit
```

### 团队约定

1. **不要绕过hook**: 除非紧急情况，不使用--no-verify
2. **修复而不是跳过**: 发现问题立即修复
3. **理解规则意义**: 规则是为了质量保障
4. **及时反馈**: 如规则不合理，及时提出

---

## ✅ 验证清单

Hook设置完成后，验证：

- [ ] .git/hooks/pre-commit文件存在
- [ ] 文件有执行权限
- [ ] 测试违规提交被拦截
- [ ] 测试正常提交可通过
- [ ] 错误提示信息清晰
- [ ] 团队成员都已配置

---

**文档维护人**: IOE-DREAM架构委员会  
**最后更新**: 2025-12-03  
**状态**: ✅ 脚本ready，待安装配置

