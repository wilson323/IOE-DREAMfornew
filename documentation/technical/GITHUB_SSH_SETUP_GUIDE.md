# GitHub SSH配置指南

## ✅ 已完成的配置

**远程仓库已切换到SSH协议**:
```
[remote "origin"]
    url = git@github.com:wilson323/IOE-DREAMfornew.git
```

## 🔧 SSH密钥配置步骤

### 1. 检查是否已有SSH密钥

```powershell
# 检查ed25519密钥
Test-Path $env:USERPROFILE\.ssh\id_ed25519

# 检查RSA密钥
Test-Path $env:USERPROFILE\.ssh\id_rsa
```

### 2. 生成SSH密钥（如果没有）

```powershell
# 推荐使用ed25519算法
ssh-keygen -t ed25519 -C "your_email@example.com"

# 或者使用RSA算法（兼容性更好）
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
```

**操作提示**:
- 按回车使用默认路径: `C:\Users\YourName\.ssh\id_ed25519`
- 设置密码（可选，建议设置）
- 再次确认密码

### 3. 查看公钥内容

```powershell
# ed25519密钥
cat $env:USERPROFILE\.ssh\id_ed25519.pub

# 或RSA密钥
cat $env:USERPROFILE\.ssh\id_rsa.pub
```

**复制完整的公钥内容**（以 `ssh-ed25519` 或 `ssh-rsa` 开头）

### 4. 添加SSH密钥到GitHub

1. 访问: https://github.com/settings/keys
2. 点击 **"New SSH key"** 按钮
3. 填写信息:
   - **Title**: 给密钥起个名字（如: "My Windows PC"）
   - **Key**: 粘贴刚才复制的公钥内容
4. 点击 **"Add SSH key"**

### 5. 测试SSH连接

```powershell
ssh -T git@github.com
```

**成功提示**:
```
Hi wilson323! You've successfully authenticated, but GitHub does not provide shell access.
```

## 🚀 推送代码

配置完成后，可以推送代码:

```powershell
# 推送当前分支
git push -u origin main

# 推送所有分支
git push --all origin

# 推送所有标签
git push --tags origin
```

## 🔍 故障排查

### 问题1: SSH连接失败

**错误信息**:
```
Permission denied (publickey)
```

**解决方案**:
1. 确认SSH密钥已添加到GitHub
2. 检查密钥路径是否正确
3. 尝试使用ssh-agent:
   ```powershell
   # 启动ssh-agent
   Start-Service ssh-agent
   
   # 添加密钥
   ssh-add $env:USERPROFILE\.ssh\id_ed25519
   ```

### 问题2: 仍然使用HTTPS

**检查当前配置**:
```powershell
git remote get-url origin
```

**切换到SSH**:
```powershell
git remote set-url origin git@github.com:wilson323/IOE-DREAMfornew.git
```

### 问题3: 多个SSH密钥

如果使用多个GitHub账户，需要配置SSH config:

创建文件: `C:\Users\YourName\.ssh\config`

```
# GitHub账户1
Host github.com
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519

# GitHub账户2（如果需要）
Host github-work
    HostName github.com
    User git
    IdentityFile ~/.ssh/id_ed25519_work
```

然后使用不同的Host:
```powershell
git remote set-url origin git@github-work:wilson323/IOE-DREAMfornew.git
```

## 📝 自动化脚本

项目提供了快速切换脚本:

```powershell
# 快速切换到SSH
.\scripts\switch-to-ssh.ps1

# 连接诊断
.\scripts\diagnose-github-connection.ps1

# 连接修复
.\scripts\fix-github-connection.ps1
```

## ✅ 验证清单

- [ ] SSH密钥已生成
- [ ] 公钥已添加到GitHub
- [ ] SSH连接测试成功
- [ ] 远程仓库已切换到SSH
- [ ] 可以成功推送代码

## 📚 相关文档

- [GitHub SSH文档](https://docs.github.com/en/authentication/connecting-to-github-with-ssh)
- [Git配置总结](./GIT_CONFIGURATION_SUMMARY.md)
- [GitHub远程仓库更新](./GITHUB_REMOTE_UPDATE.md)
