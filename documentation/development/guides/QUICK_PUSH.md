# 🚀 快速推送代码到GitHub

## ✅ 已完成的配置

- ✅ 远程仓库已配置: `https://github.com/wilson323/IOE-DREAMfornew.git`
- ✅ 已切换到HTTPS协议（避免SSH配置问题）

## 📝 推送步骤

### 步骤1: 生成GitHub Personal Access Token

1. 访问: https://github.com/settings/tokens
2. 点击 **"Generate new token"** → **"Generate new token (classic)"**
3. 填写:
   - **Note**: `IOE-DREAM-Push`
   - **Expiration**: 90 days（或自定义）
   - **Select scopes**: ✅ 勾选 `repo`（完整仓库权限）
4. 点击 **"Generate token"**
5. **立即复制Token**（只显示一次！）

### 步骤2: 推送代码

在PowerShell中执行：

```powershell
cd D:\IOE-DREAM

# 检查状态
git status

# 如果有未提交的更改，先提交
git add .
git commit -m "Update: 更新代码到GitHub"

# 推送代码（会提示输入用户名和密码）
git push -u origin main
```

**当提示输入时**:
- **Username**: 输入 `wilson323`
- **Password**: 粘贴刚才复制的 **Personal Access Token**（不是账户密码！）

### 步骤3: 推送所有分支（可选）

```powershell
# 推送所有分支
git push --all origin

# 推送所有标签
git push --tags origin
```

## 🔧 使用自动化脚本

```powershell
.\scripts\push-all-to-github.ps1
```

脚本会自动处理所有步骤。

## ⚠️ 重要提示

1. **Token安全**: 不要分享Token，妥善保管
2. **Token权限**: 必须勾选 `repo` 权限才能推送
3. **Token过期**: 如果Token过期，需要重新生成

## 🔍 如果推送失败

### 错误: 认证失败
- 确认使用Token而不是密码
- 确认Token有 `repo` 权限

### 错误: 网络问题
- 检查网络连接
- 尝试使用代理

### 错误: 权限被拒绝
- 确认有仓库的推送权限
- 确认Token未过期

## 📚 详细文档

完整指南: `documentation/technical/GITHUB_PUSH_GUIDE.md`
