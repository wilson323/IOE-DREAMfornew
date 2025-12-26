# start.ps1 完整功能说明

## ✅ 已实现的所有功能

### 📦 Build (Compile) - 编译功能

#### 1. Build Backend (Maven)
- **功能**: 编译后端微服务
- **步骤**:
  1. 检查Maven和Java环境
  2. 构建microservices-common模块
  3. 构建所有微服务
- **命令**: `mvn clean install/package -DskipTests`

#### 2. Build Frontend (npm)
- **功能**: 编译前端应用
- **步骤**:
  1. 检查Node.js和npm环境
  2. 安装依赖（如需要）
  3. 执行生产构建 `npm run build:prod`
- **输出**: `smart-admin-web-javascript/dist/`

#### 3. Build Mobile (uni-app)
- **功能**: 编译移动端应用
- **步骤**:
  1. 检查Node.js环境
  2. 安装依赖（如需要）
  3. 执行H5构建 `npm run build:h5`
- **输出**: `smart-app/dist/`

### 🚀 Start Services - 启动服务

#### 4. Start Backend Services
- **功能**: 启动后端服务
- **方式**: Docker Compose
- **服务**: Gateway, Common, Device, OA, Access, Attendance, Video, Consume, Visitor

#### 5. Start Frontend Service
- **功能**: 启动前端服务
- **方式**: 
  - 优先使用Docker Compose
  - 备选：本地npm启动（提示命令）
- **端口**: 3000

#### 6. Start Mobile Service
- **功能**: 启动移动端H5服务
- **方式**: 
  - 优先使用Docker Compose
  - 备选：本地npm启动（提示命令）
- **端口**: 8081

#### 7. Start Frontend + Backend
- **功能**: 同时启动前端和后端服务
- **步骤**:
  1. 启动基础设施（MySQL, Redis, Nacos）
  2. 启动后端微服务
  3. 启动前端服务

#### 8. Start All Services
- **功能**: 启动所有服务（完整系统）
- **步骤**:
  1. 启动基础设施服务
  2. 启动后端微服务
  3. 启动前端服务
  4. 启动移动端服务（如配置）

### 🐳 Docker Deployment - Docker部署

#### 9. Docker Deploy (Build & Start)
- **功能**: 完整的Docker部署流程
- **步骤**:
  1. 构建所有Docker镜像（`docker-compose build`）
  2. 启动所有服务（`docker-compose up -d`）
- **时间**: 首次构建需要10-30分钟
- **适用**: 生产环境部署

### 🔧 Management - 管理功能

#### S. Check Service Status
- **功能**: 检查所有服务状态
- **检查**: 11个服务的端口状态
- **显示**: 运行中/已停止统计

#### T. Stop All Services
- **功能**: 停止所有Docker服务
- **命令**: `docker-compose down`

#### R. Restart All Services
- **功能**: 重启所有服务
- **步骤**: 先停止，再启动

#### U. View Access URLs
- **功能**: 显示访问地址
- **地址**:
  - Web管理后台: http://localhost:3000
  - 移动端H5: http://localhost:8081
  - API网关: http://localhost:8080

## 📋 菜单结构

```
=== Build (Compile) ===
  1. Build Backend (Maven)
  2. Build Frontend (npm)
  3. Build Mobile (uni-app)

=== Start Services ===
  4. Start Backend Services
  5. Start Frontend Service
  6. Start Mobile Service
  7. Start Frontend + Backend
  8. Start All Services

=== Docker Deployment ===
  9. Docker Deploy (Build & Start)

=== Management ===
  S. Check Service Status
  T. Stop All Services
  R. Restart All Services
  U. View Access URLs
  0. Exit
```

## 🎯 使用场景

### 开发环境
1. **首次启动**: 选择 `9` (Docker Deploy) - 完整构建和启动
2. **日常开发**: 
   - 修改后端: 选择 `1` (Build Backend) → `4` (Start Backend)
   - 修改前端: 选择 `2` (Build Frontend) → `5` (Start Frontend)
   - 修改移动端: 选择 `3` (Build Mobile) → `6` (Start Mobile)

### 生产环境
1. **完整部署**: 选择 `9` (Docker Deploy)
2. **分步部署**:
   - 先编译: `1` → `2` → `3`
   - 再启动: `8` (Start All Services)

### 快速检查
- 选择 `S` (Check Service Status) - 查看服务状态
- 选择 `U` (View Access URLs) - 查看访问地址

## ⚙️ 技术特性

- ✅ 环境检测：自动检测Docker、Maven、Node.js等工具
- ✅ 错误处理：多层异常防护，确保脚本稳定
- ✅ 进度提示：显示每个步骤的执行状态
- ✅ 友好提示：提供后续操作建议
- ✅ 编码兼容：支持PowerShell 5.1和7.x
- ✅ IOE艺术字：美观的ASCII艺术字体动效

## 📝 注意事项

1. **首次使用**: 需要安装Docker Desktop、Maven、Node.js
2. **构建时间**: 
   - 后端首次构建: 5-10分钟
   - 前端首次构建: 2-5分钟
   - Docker首次构建: 10-30分钟
3. **服务启动**: 所有服务完全就绪需要1-2分钟
4. **端口占用**: 确保端口8080, 8087-8095, 3000, 8081未被占用

---

**版本**: v5.2.0 - Production Ready Edition  
**更新日期**: 2025-01-30

