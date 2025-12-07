# GitHub代码推送完整指南

## 🎯 目标

将本地代码推送到: https://github.com/wilson323/IOE-DREAMfornew

## ✅ 当前配置状态

- **远程仓库**: 已配置为 `https://github.com/wilson323/IOE-DREAMfornew.git`
- **协议**: HTTPS（已从SSH切换回HTTPS，避免连接问题）

## 🚀 推送步骤

### 方法1: 使用自动化脚本（推荐）

```powershell
# 执行推送脚本
.\scripts\push-all-to-github.ps1
```

脚本会自动：
1. 检查远程仓库配置
2. 检查工作区状态
3. 提示提交未保存的更改
4. 推送代码到GitHub

### 方法2: 手动推送

#### 步骤1: 检查当前状态

```powershell
# 查看当前分支
git branch --show-current

# 查看工作区状态
git status

# 查看远程仓库
git remote -v
```

#### 步骤2: 提交未保存的更改（如果有）

```powershell
# 添加所有更改
git add .

# 提交更改
git commit -m "Update: 更新代码"
```

#### 步骤3: 推送代码

```powershell
# 推送当前分支
git push -u origin main

# 或推送所有分支
git push --all origin

# 推送所有标签
git push --tags origin
```

## 🔐 认证配置

### 使用Personal Access Token（推荐）

GitHub已不再支持密码认证，必须使用Personal Access Token。

#### 1. 生成Token

1. 访问: https://github.com/settings/tokens
2. 点击 **"Generate new token"** → **"Generate new token (classic)"**
3. 填写信息:
   - **Note**: 例如 "Windows PC - IOE-DREAM"
   - **Expiration**: 选择过期时间（建议90天或自定义）
   - **Select scopes**: 勾选 `repo` 权限（完整仓库访问权限）
4. 点击 **"Generate token"**
5. **重要**: 立即复制Token（只显示一次）

#### 2. 使用Token推送

当Git提示输入密码时：
- **用户名**: 输入你的GitHub用户名（wilson323）
- **密码**: 粘贴刚才复制的Personal Access Token（不是账户密码）

#### 3. 保存凭据（可选）

Windows可以使用Git Credential Manager保存凭据：

```powershell
# 配置Git使用Credential Manager
git config --global credential.helper manager-core
```

之后第一次推送时输入Token，系统会自动保存。

### 使用GitHub CLI（备选方案）

```powershell
# 安装GitHub CLI（如果还没有）
# 下载: https://cli.github.com/

# 登录
gh auth login

# 选择GitHub.com
# 选择HTTPS
# 选择使用浏览器登录或输入Token

# 推送代码
git push -u origin main
```

## 🔍 故障排查

### 问题1: 认证失败

**错误信息**:
```
remote: Support for password authentication was removed on August 13, 2021.
```

**解决方案**: 使用Personal Access Token代替密码

### 问题2: 权限被拒绝

**错误信息**:
```
remote: Permission denied
```

**解决方案**:
1. 确认Token有 `repo` 权限
2. 确认有仓库的推送权限
3. 检查Token是否过期

### 问题3: 远程仓库不存在

**错误信息**:
```
remote: Repository not found
```

**解决方案**:
1. 确认仓库URL正确
2. 确认仓库已创建
3. 确认有访问权限

### 问题4: 网络连接问题

**错误信息**:
```
fatal: unable to access 'https://github.com/...': getaddrinfo() thread failed to start
```

**解决方案**:
1. 检查网络连接
2. 配置代理（如果需要）:
   ```powershell
   git config --global http.proxy http://proxy.example.com:8080
   git config --global https.proxy https://proxy.example.com:8080
   ```
3. 尝试使用SSH（需要配置SSH密钥）

## 📋 推送检查清单

- [ ] 远程仓库已正确配置
- [ ] 工作区状态已检查
- [ ] 未提交的更改已处理
- [ ] Personal Access Token已生成
- [ ] Token有 `repo` 权限
- [ ] 可以成功推送代码

## 🎯 快速推送命令

```powershell
# 一键推送（需要先配置Token）
git add .
git commit -m "Update: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
git push -u origin main
```

## 📚 相关文档

- [GitHub远程仓库更新](./GITHUB_REMOTE_UPDATE.md)
- [SSH配置指南](./GITHUB_SSH_SETUP_GUIDE.md)
- [连接问题修复](./GITHUB_CONNECTION_FIX_SUMMARY.md)

## ✅ 推送成功后

推送成功后，可以在GitHub查看：
- 仓库地址: https://github.com/wilson323/IOE-DREAMfornew
- 代码已同步到云端
- 可以继续使用 `git push` 推送后续更改

---

**最后更新**: 2025-01-30  
**状态**: 配置完成，等待推送操作
