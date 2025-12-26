# 全局代码分析系统 API 文档

**版本**: v1.0.0  
**创建日期**: 2025-12-20  
**API前缀**: `/api/v1/code-analysis`  
**认证方式**: JWT Bearer Token

---

## 📋 API 概览

全局代码分析系统提供RESTful API接口，支持项目代码的全面分析、问题检测和自动修复功能。

## 🔧 核心API接口

### 1. 架构分析接口

#### 1.1 执行架构分析
```http
POST /api/v1/code-analysis/architecture/analyze
Content-Type: application/json
Authorization: Bearer <token>

{
  "projectPath": "/path/to/project",
  "analysisOptions": {
    "checkDependencies": true,
    "detectCycles": true,
    "checkLayerCompliance": true,
    "generateHealthScore": true
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "架构分析完成",
  "data": {
    "analysisId": "arch_20251220_001",
    "projectPath": "/path/to/project",
    "dependencyGraph": {
      "nodes": ["service-a", "service-b", "service-c"],
      "edges": [
        {"from": "service-a", "to": "service-b", "type": "dependency"}
      ]
    },
    "cyclePaths": [
      {
        "path": ["service-a", "service-b", "service-a"],
        "severity": "HIGH"
      }
    ],
    "layerViolations": [
      {
        "violationType": "CROSS_LAYER_CALL",
        "location": {
          "filePath": "src/main/java/Controller.java",
          "lineNumber": 25
        },
        "description": "Controller直接调用DAO层"
      }
    ],
    "healthScore": {
      "overall": 83.5,
      "dependency": 90.0,
      "layerCompliance": 75.0,
      "modularity": 85.0
    }
  }
}
```

#### 1.2 获取架构健康度报告
```http
GET /api/v1/code-analysis/architecture/health-report/{analysisId}
```

### 2. 编译错误分析接口

#### 2.1 分析编译错误
```http
POST /api/v1/code-analysis/compilation/analyze
Content-Type: application/json

{
  "projectPath": "/path/to/project",
  "errorLog": "编译错误日志内容",
  "analysisOptions": {
    "classifyErrors": true,
    "detectEncoding": true,
    "prioritizeErrors": true
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "编译错误分析完成",
  "data": {
    "analysisId": "comp_20251220_001",
    "errorClassification": {
      "realCompilationErrors": 15,
      "ideErrors": 8,
      "encodingIssues": 3
    },
    "prioritizedErrors": [
      {
        "priority": "P0",
        "errorType": "COMPILATION_ERROR",
        "location": {
          "filePath": "src/main/java/Service.java",
          "lineNumber": 42
        },
        "description": "找不到符号: javax.annotation.Resource",
        "suggestion": "替换为 jakarta.annotation.Resource"
      }
    ],
    "encodingIssues": [
      {
        "filePath": "src/main/java/Controller.java",
        "currentEncoding": "GBK",
        "suggestedEncoding": "UTF-8"
      }
    ]
  }
}
```

### 3. 代码质量分析接口

#### 3.1 执行质量检查
```http
POST /api/v1/code-analysis/quality/check
Content-Type: application/json

{
  "projectPath": "/path/to/project",
  "checkOptions": {
    "checkAnnotations": true,
    "verifyLombok": true,
    "assessQuality": true
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "代码质量检查完成",
  "data": {
    "analysisId": "quality_20251220_001",
    "annotationViolations": [
      {
        "violationType": "AUTOWIRED_USAGE",
        "location": {
          "filePath": "src/main/java/Service.java",
          "lineNumber": 15
        },
        "description": "使用了@Autowired注解",
        "suggestion": "替换为@Resource注解"
      }
    ],
    "lombokStatus": {
      "configured": true,
      "version": "1.18.30",
      "issues": []
    },
    "qualityMetrics": {
      "codeComplexity": 7.2,
      "testCoverage": 85.5,
      "duplicateCodeRate": 2.1,
      "maintainabilityIndex": 78.3
    }
  }
}
```

### 4. 自动修复接口

#### 4.1 执行自动修复
```http
POST /api/v1/code-analysis/fix/execute
Content-Type: application/json

{
  "projectPath": "/path/to/project",
  "fixOptions": {
    "replaceAnnotations": true,
    "convertEncoding": true,
    "replacePackages": true,
    "createBackup": true
  },
  "targetIssues": ["issue_001", "issue_002"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "自动修复执行完成",
  "data": {
    "fixId": "fix_20251220_001",
    "backupInfo": {
      "backupId": "backup_20251220_001",
      "backupPath": "/tmp/backup/project_20251220_001",
      "timestamp": "2025-12-20T10:30:00Z"
    },
    "fixResults": [
      {
        "issueId": "issue_001",
        "fixType": "ANNOTATION_REPLACEMENT",
        "status": "SUCCESS",
        "filesModified": 5,
        "description": "成功替换@Autowired为@Resource"
      }
    ],
    "verificationResult": {
      "compilationSuccess": true,
      "testsPass": true,
      "qualityImproved": true
    }
  }
}
```

#### 4.2 回滚修复
```http
POST /api/v1/code-analysis/fix/rollback
Content-Type: application/json

{
  "backupId": "backup_20251220_001"
}
```

### 5. 配置文件解析接口

#### 5.1 解析配置文件
```http
POST /api/v1/code-analysis/config/parse
Content-Type: application/json

{
  "configContent": "配置文件内容",
  "configType": "YAML",
  "parseOptions": {
    "validateSyntax": true,
    "extractProperties": true
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "配置文件解析成功",
  "data": {
    "parseResult": {
      "valid": true,
      "configObject": {
        "spring": {
          "application": {
            "name": "ioedream-service"
          }
        }
      },
      "properties": [
        {
          "key": "spring.application.name",
          "value": "ioedream-service",
          "type": "STRING"
        }
      ]
    },
    "prettyPrint": "格式化后的配置内容"
  }
}
```

### 6. 监控与报告接口

#### 6.1 获取项目健康度趋势
```http
GET /api/v1/code-analysis/monitoring/health-trend
?projectPath=/path/to/project
&timeRange=30d
```

#### 6.2 生成质量报告
```http
POST /api/v1/code-analysis/reports/generate
Content-Type: application/json

{
  "reportType": "COMPREHENSIVE",
  "projectPath": "/path/to/project",
  "timeRange": "30d",
  "includeMetrics": true,
  "includeTrends": true
}
```

---

## 🔧 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 1001 | 项目路径不存在 | 检查项目路径是否正确 |
| 1002 | 配置文件格式错误 | 检查配置文件语法 |
| 1003 | 分析引擎初始化失败 | 检查系统资源和权限 |
| 1004 | 备份创建失败 | 检查磁盘空间和写权限 |
| 1005 | 修复操作失败 | 查看详细错误信息 |

---

## 📊 性能指标

- **分析响应时间**: P99 < 30s（大型项目）
- **修复操作时间**: P99 < 60s
- **并发支持**: 最大10个并发分析任务
- **内存占用**: < 2GB per 分析任务

---

## 🔗 相关文档

- [全局代码分析需求规格](../.kiro/specs/global-code-analysis/requirements.md)
- [全局代码分析设计文档](../.kiro/specs/global-code-analysis/design.md)
- [全局代码分析专家技能](../.claude/skills/global-code-analysis-expert.md)

---

**重要提醒**: 所有API调用都需要有效的JWT Token，修复操作会自动创建备份以确保数据安全。