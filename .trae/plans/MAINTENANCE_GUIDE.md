# IOE-DREAM 全局一致性维护指南

> **版本**: v1.0.0  
> **更新日期**: 2025-12-14  
> **维护团队**: IOE-DREAM 架构委员会

---

## 🎯 维护目标

确保项目在**架构、技术栈、开发规范、代码规范**四个维度持续保持全局一致，避免技术债务累积。

---

## 📋 日常维护检查

### 代码提交前检查

**使用检查清单**:
- 参考: `.trae/plans/global-consistency-checklist.md`
- 确保所有P0级检查项通过

**快速检查命令**:
```powershell
# 检查Feign违规
Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@EnableFeignClients|@FeignClient" |
    Where-Object { $_.Path -notmatch "Test\.java$" }

# 检查@Autowired违规
Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "^\s*@Autowired\b" |
    Where-Object { $_.Path -notmatch "Test\.java$" }

# 检查POST查询接口
Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java" |
    Select-String -Pattern '@PostMapping\("/query|@PostMapping\("/get|@PostMapping\("/list'
```

---

## 🔄 定期维护任务

### 每周检查

1. **配置文件安全扫描**
   - 检查是否有新增明文密码
   - 验证环境变量配置正确性

2. **架构合规性检查**
   - 确认无新增微服务
   - 确认服务间调用规范

### 每月检查

1. **技术栈版本一致性**
   - 验证父POM版本管理
   - 检查依赖版本冲突

2. **代码规范一致性**
   - 扫描@Autowired使用
   - 检查RESTful API规范

3. **可观测性指标**
   - 验证tracing配置完整性
   - 检查指标命名规范性

---

## 🚨 常见问题处理

### 问题1: 发现新的@Autowired使用

**处理步骤**:
1. 确认是否为测试类（测试类允许使用）
2. 如果是业务代码，替换为@Resource
3. 更新检查清单

### 问题2: 发现新的POST查询接口

**处理步骤**:
1. 评估接口复杂度（简单查询改为GET）
2. 重构为GET方法，参数改为@RequestParam
3. 保持Service层接口不变（向后兼容）

### 问题3: 发现新的Feign使用

**处理步骤**:
1. 评估是否必须使用Feign（通常不需要）
2. 改为使用GatewayServiceClient
3. 移除@EnableFeignClients和Feign依赖

---

## 📚 参考文档

1. **全局一致性优化路线图**
   - `.trae/plans/global-consistency-optimization-roadmap.md`

2. **全局一致性检查清单**
   - `.trae/plans/global-consistency-checklist.md`

3. **微服务边界文档**
   - `documentation/architecture/MICROSERVICES_BOUNDARIES.md`

4. **可观测性指标命名标准**
   - `documentation/architecture/OBSERVABILITY_METRICS_NAMING_STANDARD.md`

---

**最后更新**: 2025-12-14  
**维护团队**: IOE-DREAM 架构委员会
