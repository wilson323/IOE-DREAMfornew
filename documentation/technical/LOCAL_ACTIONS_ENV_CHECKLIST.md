# 本地 Action 使用与运行环境清单

## 背景与目标
为避免依赖 GitHub 远程 Actions，本项目使用仓库内复合 Action 替代外部 `uses:`。该清单用于统一运行环境与使用规范，确保 CI/CD 在离线或受限网络下可稳定执行。

## 适用范围
- 适用仓库路径：`.github/actions/*`
- 适用工作流：`.github/workflows/*.yml`
- 适用运行环境：GitHub Hosted Runner / 企业自建 Runner

## 全局约束
- 所有外部 `uses:` 已替换为本地复合 Action
- 产物统一归档到 `artifacts/` 目录（不上传到 GitHub UI）
- 不依赖外网时，功能降级为“可执行优先，输出可追溯”

## 本地 Action 清单与替代关系
| 外部 Action | 本地替代 | 主要行为 |
|---|---|---|
| `actions/checkout@v4` | `./.github/actions/checkout` | 拉取代码或复用现有 `.git` |
| `actions/setup-java@v4` | `./.github/actions/setup-java` | 使用本机 JDK 并注入 `JAVA_HOME` |
| `actions/cache@v4` | `./.github/actions/cache` | 无缓存（noop） |
| `actions/upload-artifact@v4|v3` | `./.github/actions/upload-artifact` | 复制到 `artifacts/<name>/` |
| `dorny/test-reporter@v1` | `./.github/actions/test-reporter` | 复制测试报告到 `artifacts/` |
| `codecov/codecov-action@v3` | `./.github/actions/codecov` | 复制覆盖率到 `artifacts/coverage/` |
| `aquasecurity/trivy-action@master` | `./.github/actions/trivy` | 调用本地 `trivy` CLI |
| `github/codeql-action/upload-sarif@v2|v3` | `./.github/actions/codeql-upload-sarif` | 复制 SARIF 到 `artifacts/sarif/` |
| `SonarSource/sonarcloud-github-action@master` | `./.github/actions/sonarcloud` | 调用 `sonar-scanner` 或 `mvn sonar:sonar` |
| `docker/setup-buildx-action@v3` | `./.github/actions/docker-setup-buildx` | 使用本地 buildx |
| `docker/login-action@v3` | `./.github/actions/docker-login` | 调用 `docker login` |
| `docker/metadata-action@v5` | `./.github/actions/docker-metadata` | 生成简化 tags/labels |
| `docker/build-push-action@v5` | `./.github/actions/docker-build-push` | 调用 `docker buildx build` |

## 运行环境要求
### 必需工具
- Git（用于 `checkout`）
- Java 17（用于 `setup-java`）
- Maven 3.x（用于 `sonarcloud`/构建）
- PowerShell（用于已有 `pwsh` 脚本）
- Docker + buildx（用于构建与推送镜像）

### 可选工具
- `trivy`（安全扫描，未安装会跳过并提示）
- `sonar-scanner`（存在则优先使用）

### 环境变量要求
- `GITHUB_SERVER_URL`、`GITHUB_REPOSITORY`、`GITHUB_SHA`、`GITHUB_REF`（CI 运行时自动注入）
- `GITHUB_TOKEN`（如需私有仓库拉取）
- `SONAR_TOKEN`、`SONAR_HOST_URL`（仅在启用 Sonar 任务时需要）
- `GITHUB_ENV`、`GITHUB_PATH`、`GITHUB_OUTPUT`（Action 输出依赖）

## 产物归档规范
- 所有产物复制到 `artifacts/` 目录
- 结构示例：
  - `artifacts/code-quality-reports/`
  - `artifacts/test-reports/<name>/`
  - `artifacts/coverage/<name>/`
  - `artifacts/sarif/`

## 行为差异说明
- **缓存**：本地 `cache` 为 noop，不进行依赖缓存
- **上传**：`upload-artifact` 不上传到 GitHub UI，仅本地归档
- **安全扫描**：无 `trivy` 时跳过，不阻塞构建
- **Docker 标签**：`metadata` 简化为 `sha` + `ref` 标签

## 验证清单（建议每次 Runner 变更后执行）
1. `java -version` 必须为 17
2. `mvn -v` 正常输出
3. `pwsh -v` 正常输出
4. `docker version` 与 `docker buildx version` 正常输出
5. 执行一次任意工作流的构建步骤，确认 `artifacts/` 产生输出

## 维护规则
- 新增外部 Action 时，必须先创建本地替代并更新此清单
- 本地 Action 只实现当前实际所需功能（YAGNI）
- 变更后需同步更新 `artifacts/` 目录结构说明
