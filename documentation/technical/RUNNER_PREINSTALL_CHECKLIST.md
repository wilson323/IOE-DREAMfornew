# Runner 预装脚本清单（Windows / Linux）

## 目的
确保本地复合 Action 在离线/受限网络环境稳定运行，统一 Runner 预装工具与版本基线。

## 基线版本要求
- Java: 17
- Maven: 3.x
- Git: 最新稳定版
- PowerShell: 7.x（Linux 必须，Windows 已自带）
- Docker: 24.x+
- Docker Buildx: 与 Docker 同版本
- Trivy: 最新稳定版（可选）
- SonarScanner: 最新稳定版（可选）

## Windows Runner 预装清单
### 必装
- Git for Windows
- Temurin JDK 17
- Maven 3.x
- Docker Desktop（含 buildx）
- PowerShell 7.x（若系统未预装）

### 可选
- Trivy
- SonarScanner

### 安装方式建议
- 企业统一制品库/离线安装包
- 禁止在线安装时使用内网镜像

### 验证命令
```powershell
java -version
mvn -v
git --version
pwsh -v
docker version
docker buildx version
trivy --version
sonar-scanner --version
```

## Linux Runner 预装清单
### 必装
- git
- Temurin JDK 17
- Maven 3.x
- PowerShell 7.x
- Docker Engine + buildx

### 可选
- Trivy
- SonarScanner

### 验证命令
```bash
java -version
mvn -v
git --version
pwsh -v
docker version
docker buildx version
trivy --version
sonar-scanner --version
```

## 目录与权限要求
- Runner 需具备仓库工作目录写权限
- 允许创建 `artifacts/` 归档目录
- 允许执行 `scripts/` 目录下的 `ps1/sh` 脚本

## 运行前检查清单
- `JAVA_HOME` 正确指向 JDK 17
- `PATH` 包含 `java`、`mvn`、`git`、`docker`、`pwsh`
- Docker 守护进程已启动

## 异常排查要点
- `setup-java` 失败：优先检查 `JAVA_HOME` 与 JDK 版本
- `docker-*` 失败：检查 Docker 服务与 buildx
- `trivy`/`sonar` 缺失：按需安装或保持跳过策略

## 维护规则
- 新增工具版本时必须同步到本清单
- Runner 镜像更新需记录版本与变更说明
