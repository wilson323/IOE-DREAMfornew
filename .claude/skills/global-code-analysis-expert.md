# 全局代码分析专家技能

**技能版本**: v1.0.0  
**创建日期**: 2025-12-20  
**适用项目**: IOE-DREAM智慧园区一卡通管理平台  
**技能类型**: P0级核心技能  
**维护团队**: 老王(架构师团队)

---

## 🎯 技能定位

作为IOE-DREAM项目的全局代码分析专家，我专注于系统性地诊断和修复项目中的各类代码异常问题，包括架构违规、编译错误、代码质量问题、技术迁移问题和模块拆分问题。

## 🔧 核心能力

### 1. 架构问题诊断与修复
- ✅ **依赖关系分析**: 识别所有模块间的依赖关系
- ✅ **循环依赖检测**: 检测并提供循环依赖解决方案
- ✅ **四层架构合规性**: 验证Controller→Service→Manager→DAO调用关系
- ✅ **跨层调用检测**: 标记违规位置并提供修复建议
- ✅ **架构健康度评分**: 生成量化的架构质量报告

### 2. 编译错误根因分析与修复
- ✅ **错误分类**: 区分真实编译错误和IDE诊断错误
- ✅ **字符编码检测**: 自动检测文件编码并提供转换方案
- ✅ **包名导入验证**: 验证Jakarta EE迁移完整性
- ✅ **优先级分类**: 按P0/P1/P2优先级分类编译错误
- ✅ **修复效果验证**: 验证修复效果并生成修复报告

### 3. 代码质量问题检测与修复
- ✅ **注解违规检测**: 检测@Autowired/@Repository违规使用
- ✅ **Manager类注解检查**: 检查Manager类Spring注解使用
- ✅ **Lombok配置验证**: 验证Lombok注解处理器配置
- ✅ **代码规范检查**: 检查代码规范符合度
- ✅ **质量报告生成**: 生成详细的代码质量报告

### 4. 技术迁移问题诊断与修复
- ✅ **javax包名扫描**: 扫描所有Java文件中的javax.*包名使用
- ✅ **Jakarta替换方案**: 提供javax.*到jakarta.*的替换方案
- ✅ **依赖版本兼容性**: 验证依赖版本与Jakarta EE的兼容性
- ✅ **注解处理器检查**: 检查注解处理器配置的正确性
- ✅ **启动兼容性验证**: 验证所有服务的启动兼容性

### 5. 模块拆分问题诊断与修复
- ✅ **职责边界分析**: 分析公共模块的职责边界和依赖关系
- ✅ **职责重叠检测**: 检测模块职责重叠并提供重构建议
- ✅ **编译完整性验证**: 验证模块拆分后的编译完整性
- ✅ **渐进式迁移**: 提供渐进式迁移方案
- ✅ **重构后验证**: 确保重构后所有业务服务正常编译

## 🛠️ 分析工具与方法

### 1. 静态代码分析
```python
# 架构分析引擎
class ArchitectureAnalysisEngine:
    def analyze_dependencies(self) -> DependencyGraph
    def detect_cycles(self, graph: DependencyGraph) -> List[CyclePath]
    def check_layer_compliance(self) -> List[LayerViolation]
    def calculate_health_score(self) -> ArchitectureHealthScore
```

### 2. 编译错误分析
```python
# 编译错误分析引擎
class CompilationErrorAnalysisEngine:
    def classify_errors(self, error_log: str) -> ErrorClassification
    def detect_encoding_issues(self, file_path: str) -> EncodingIssue
    def analyze_package_imports(self) -> List[PackageIssue]
    def prioritize_errors(self, errors: List[CompilationError]) -> List[PrioritizedError]
```

### 3. 代码质量分析
```python
# 代码质量分析引擎
class CodeQualityAnalysisEngine:
    def check_annotation_violations(self) -> List[AnnotationViolation]
    def verify_lombok_configuration(self) -> LombokConfigStatus
    def assess_code_quality(self) -> QualityReport
```

## 📊 正确性属性验证

### 核心属性列表
1. **依赖关系分析完整性**: 识别出的模块依赖关系包含项目中所有实际存在的依赖关系
2. **循环依赖检测准确性**: 能够检测出所有的循环依赖路径，且不产生误报
3. **四层架构合规性检查**: 准确识别违反Controller→Service→Manager→DAO调用顺序的跨层调用
4. **编译错误分类准确性**: 准确区分真实的Maven编译错误和IDE诊断错误，分类准确率≥95%
5. **字符编码检测准确性**: 准确检测文件的字符编码，并在检测到编码问题时提供正确的转换方案

### 属性测试框架
```python
from hypothesis import given, strategies as st

@given(st.lists(st.text(), min_size=1, max_size=20))
def test_dependency_analysis_completeness(module_names):
    """属性测试：依赖关系分析完整性"""
    # 生成随机的模块依赖关系
    # 验证分析结果的完整性
    pass

@given(st.lists(st.tuples(st.text(), st.text()), min_size=3, max_size=10))
def test_cycle_detection_accuracy(dependencies):
    """属性测试：循环依赖检测准确性"""
    # 构造依赖关系图
    # 验证循环依赖检测的准确性
    pass
```

## 🔍 使用场景

### 1. 项目健康度检查
```bash
# 执行全局代码分析
python global_code_analyzer.py --project-path /path/to/ioedream --mode full

# 生成架构健康度报告
python global_code_analyzer.py --project-path /path/to/ioedream --mode architecture --output report.html
```

### 2. 编译错误诊断
```bash
# 分析编译错误
python global_code_analyzer.py --project-path /path/to/ioedream --mode compilation --error-log compile.log

# 自动修复编译错误
python global_code_analyzer.py --project-path /path/to/ioedream --mode fix --priority P0
```

### 3. 代码质量检查
```bash
# 检查代码质量问题
python global_code_analyzer.py --project-path /path/to/ioedream --mode quality

# 生成质量改善报告
python global_code_analyzer.py --project-path /path/to/ioedream --mode quality --compare-with baseline.json
```

## 📈 性能指标

### 分析性能
- **大型项目分析时间**: < 10分钟（10000+文件）
- **内存占用**: < 2GB
- **并发分析**: 支持多线程并发分析
- **增量分析**: 支持基于Git diff的增量分析

### 准确性指标
- **依赖关系识别准确率**: ≥ 99%
- **循环依赖检测准确率**: ≥ 98%
- **编译错误分类准确率**: ≥ 95%
- **代码质量问题检测覆盖率**: ≥ 90%

## 🔧 配置与定制

### 分析配置
```yaml
# global_code_analyzer.yml
analysis:
  architecture:
    enabled: true
    check_cycles: true
    check_layer_compliance: true
    generate_health_score: true
  
  compilation:
    enabled: true
    classify_errors: true
    detect_encoding: true
    prioritize_errors: true
  
  quality:
    enabled: true
    check_annotations: true
    verify_lombok: true
    assess_quality: true
  
  migration:
    enabled: true
    scan_javax_packages: true
    check_compatibility: true
    verify_startup: true
  
  refactoring:
    enabled: true
    analyze_boundaries: true
    detect_overlaps: true
    plan_refactoring: true
```

### 自定义规则
```python
# 自定义架构规则
class CustomArchitectureRule:
    def check_compliance(self, code_element):
        # 自定义合规性检查逻辑
        pass
    
    def get_violation_message(self):
        return "违反自定义架构规则"
```

## 📚 相关文档

- [全局代码分析需求规格](.kiro/specs/global-code-analysis/requirements.md)
- [全局代码分析设计文档](.kiro/specs/global-code-analysis/design.md)
- [CLAUDE.md - 全局架构规范](../CLAUDE.md)
- [统一开发标准](../documentation/technical/UNIFIED_DEVELOPMENT_STANDARDS.md)

## 🔄 更新日志

### v1.0.0 (2025-12-20)
- ✅ 初始版本创建
- ✅ 定义核心分析能力
- ✅ 建立正确性属性验证框架
- ✅ 配置分析工具和方法

---

**重要提醒**: 本技能文档基于IOE-DREAM项目的实际需求制定，所有分析和修复操作都必须严格遵循项目的架构规范和开发标准。