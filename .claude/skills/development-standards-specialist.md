# 📖 开发规范体系专家技能

**技能名称**: 开发规范体系专家
**技能等级**: 高级
**适用角色**: 全栈开发工程师、技术负责人、架构师
**前置技能**: 基础编程能力、项目开发经验
**预计学时**: 40小时

---

## 📚 知识要求

### 理论知识
- **软件工程规范**: 理解编码规范、版本控制、文档标准的重要性
- **架构设计原则**: 掌握SOLID、KISS、DRY、YAGNI等设计原则
- **质量保证体系**: 了解代码审查、测试驱动开发、持续集成等概念

### 业务理解
- **SmartAdmin架构**: 深入理解SmartAdmin v3的四层架构设计
- **业务模块关系**: 理解门禁、消费、考勤、视频监控等业务模块间的关系
- **repowiki规范体系**: 熟悉repowiki下的所有规范要求和约束条件

### 技术背景
- **Java 17 + Spring Boot 3**: 熟练掌握现代Java开发技术栈
- **Vue3 + TypeScript**: 掌握现代前端开发技术
- **数据库设计**: 理解MySQL数据库设计规范和最佳实践

---

## 🛠️ 操作步骤

### 1. repowiki规范体系梳理

#### 步骤1: 识别规范层级
```bash
# 🔴 一级规范（绝对禁止，违反将导致编译失败）
- 包名规范：必须使用jakarta.*，禁止javax.*
- 依赖注入：必须使用@Resource，禁止@Autowired
- 架构规范：严格遵循四层架构，禁止跨层访问
- 字符编码：必须使用UTF-8编码，禁止乱码

# 🟡 二级规范（应该遵守，影响代码质量）
- 日志规范：使用SLF4J，禁止System.out
- 权限控制：所有接口必须添加@SaCheckPermission
- 参数验证：使用@Valid进行参数校验
- 异常处理：使用SmartException统一异常处理

# 🟢 三级规范（建议遵守，最佳实践）
- 代码注释：添加完整的JavaDoc和注释
- 单元测试：测试覆盖率≥80%
- 性能优化：SQL优化、缓存使用
- 代码风格：统一的代码格式化
```

#### 步骤2: 规范符合性检查
```bash
# 检查javax包使用（必须为0）
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)

# 检查@Autowired使用（必须为0）
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)

# 检查架构违规（必须为0）
architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)

# 检查System.out使用（应该为0）
system_out_count=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l)
```

### 2. 四层架构严格实施

#### 步骤1: 架构层级识别
- **Controller层**: 接收HTTP请求，参数校验，调用Service层
- **Service层**: 业务逻辑处理，事务管理，调用Manager层
- **Manager层**: 复杂业务逻辑封装，跨模块调用，调用DAO层
- **DAO层**: 数据访问，使用MyBatis Plus，直接操作数据库

#### 步骤2: 架构合规验证
```java
// ✅ 正确的架构实现
@RestController
public class DeviceController {
    @Resource
    private DeviceService deviceService;  // Controller → Service

    @PostMapping("/add")
    @SaCheckPermission("device:add")
    public ResponseDTO<String> add(@Valid @RequestBody DeviceAddDTO dto) {
        return ResponseDTO.ok(deviceService.add(dto));
    }
}

@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceServiceImpl implements DeviceService {
    @Resource
    private DeviceManager deviceManager;  // Service → Manager

    public String add(DeviceAddDTO dto) {
        return deviceManager.add(dto);
    }
}

@Component
public class DeviceManager {
    @Resource
    private DeviceDao deviceDao;  // Manager → DAO

    public String add(DeviceAddDTO dto) {
        // 复杂业务逻辑处理
        return deviceDao.insert(dto);
    }
}

@Mapper
public interface DeviceDao {
    // 数据访问方法
    int insert(DeviceAddDTO dto);
}
```

#### 步骤3: 架构违规检查
```bash
# 检查Controller直接访问DAO（必须为0）
controller_direct_dao=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)

# 检查Service层管理事务（应该有@Transactional注解）
service_transaction=$(grep -r "@Transactional" --include="*Service*.java" . | wc -l)

# 检查DAO层复杂业务逻辑（应该避免）
dao_business_logic=$(grep -r "if\|for\|while" --include="*Dao.java" . | wc -l)
```

### 3. 编码标准严格执行

#### 步骤1: 包名迁移规范
```bash
# 批量修复EE包名（必须执行）
find . -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;

# 注意：以下JDK标准包不替换（白名单）
# java.* （JDK核心包）
# javax.crypto.* （JCE加密包）
# javax.net.* （网络包）
# javax.security.* （安全包）
# javax.naming.* （JNDI包）
```

#### 步骤2: 依赖注入标准化
```bash
# 批量替换@Autowired为@Resource
find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;

# 手动检查特殊情况
# 1. 构造器注入保持不变
# 2. @Qualifier注解需要配合@Resource使用
# 3. 检查循环依赖问题
```

#### 步骤3: 字符编码标准化
```bash
# 检查文件编码（必须为UTF-8）
find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII"

# 检查BOM标记（必须移除）
find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \;

# 检查乱码字符（必须修复）
find . -name "*.java" -exec grep -l "????\|涓?\|鏂?\|锟斤拷" {} \;
```

### 4. 质量门禁实施

#### 步骤1: 编译验证
```bash
# 完整编译检查
mvn clean compile -DskipTests

# 检查编译错误数量
error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR")
if [ $error_count -gt 0 ]; then
    echo "❌ 发现 $error_count 个编译错误"
    exit 1
fi
```

#### 步骤2: 测试验证
```bash
# 运行单元测试
mvn test

# 检查测试覆盖率
mvn jacoco:report
coverage=$(cat target/site/jacoco/index.html | grep -o "Total.*[0-9]*%" | grep -o "[0-9]*")
if [ $coverage -lt 80 ]; then
    echo "⚠️ 测试覆盖率不足80%: $coverage%"
fi
```

#### 步骤3: 代码质量检查
```bash
# 运行代码质量检查
./scripts/enforce-standards.sh

# 运行AI代码验证
./scripts/ai-code-validation.sh

# 运行编码标准验证
./scripts/verify-encoding.sh
```

---

## ⚠️ 注意事项

### 安全提醒
- **敏感信息**: 禁止在代码中硬编码密码、密钥等敏感信息
- **SQL注入**: 使用MyBatis参数化查询，防止SQL注入攻击
- **XSS防护**: 前端输出必须进行转义处理
- **权限控制**: 所有敏感操作必须添加权限注解

### 质量要求
- **编译通过**: 所有代码必须100%编译通过
- **测试覆盖**: 单元测试覆盖率≥80%
- **规范遵循**: 100%遵循repowiki规范要求
- **文档完整**: 重要API必须有完整的文档注释

### 最佳实践
- **代码审查**: 所有代码变更必须经过代码审查
- **增量开发**: 小步快跑，频繁提交，便于问题定位
- **自动化**: 尽可能使用自动化工具保证代码质量
- **持续学习**: 持续关注技术发展和规范更新

---

## 📊 评估标准

### 操作时间
- **规范梳理**: 4小时内完成repowiki规范体系梳理
- **架构设计**: 8小时内完成四层架构设计
- **代码实现**: 16小时内完成模块代码实现
- **质量验证**: 4小时内完成所有质量检查
- **文档编写**: 8小时内完成技术文档编写

### 准确率要求
- **规范遵循**: 100%符合repowiki规范要求
- **架构合规**: 100%符合四层架构设计原则
- **编译通过**: 100%编译通过，无任何错误
- **测试覆盖**: ≥80%单元测试覆盖率

### 质量标准
- **代码质量**: 通过所有质量门禁检查
- **性能标准**: API响应时间P95≤200ms
- **安全标准**: 通过安全扫描，无高危漏洞
- **文档标准**: 技术文档完整度≥90%

---

## 🔗 相关技能

### 相关技能
- **[数据库设计规范专家](database-design-specialist.md)**: 数据库设计和优化技能
- **[Spring Boot Jakarta守护专家](spring-boot-jakarta-guardian.md)**: Spring Boot 3.x迁移技能
- **[四层架构守护专家](four-tier-architecture-guardian.md)**: 架构设计和合规检查技能
- **[代码质量和编码规范守护专家](code-quality-protector.md)**: 代码质量保证技能

### 进阶路径
- **系统架构师**: 负责整体系统架构设计和技术选型
- **技术专家**: 深入特定技术领域，提供技术解决方案
- **团队技术负责人**: 带领开发团队，把控技术方向和质量

### 参考资料
- **[repowiki规范体系](../docs/repowiki/)**: 权威规范文档
- **[CLAUDE.md](../CLAUDE.md)**: 项目开发指南
- **[技术迁移规范](../docs/TECHNOLOGY_MIGRATION.md)**: 技术升级指南
- **[架构设计规范](../docs/ARCHITECTURE_STANDARDS.md)**: 架构设计标准

---

## 📋 检查清单

### 开发前检查
- [ ] 已阅读repowiki相关规范文档
- [ ] 已识别任务类型和必须遵循的规范
- [ ] 已通过repowiki一级规范符合性评估
- [ ] 已选择合适的代码模板
- [ ] 已理解业务状态机和验证规则

### 开发中检查
- [ ] 使用@Resource而非@Autowired
- [ ] 使用jakarta.*而非javax.*
- [ ] 使用SLF4J而非System.out
- [ ] Entity继承BaseEntity且未重复定义审计字段
- [ ] Controller只做参数验证和调用Service
- [ ] Service包含@Transactional注解
- [ ] Controller返回ResponseDTO
- [ ] 添加@SaCheckPermission权限注解
- [ ] 使用@Valid进行参数验证

### 开发后检查
- [ ] 运行repowiki规范符合性检查
- [ ] 运行./scripts/quick-check.sh
- [ ] 运行./scripts/ai-code-validation.sh
- [ ] Pre-commit Hook检查通过
- [ ] 单元测试全部通过
- [ ] 代码审查通过
- [ ] 技术文档更新完成

---

**💡 核心理念**: 严格按照repowiki规范执行开发工作，确保代码质量和架构合规性，为AI辅助开发和团队协作提供坚实基础。
