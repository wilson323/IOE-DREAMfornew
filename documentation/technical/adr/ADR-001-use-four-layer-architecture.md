# ADR-001: 使用四层架构

## 状态
**已接受 (Accepted)** - 2025-11-14

## 背景
SmartAdmin v3项目需要一个清晰的分层架构来：
- 明确职责分离
- 提升代码可测试性
- 降低模块间耦合
- 支持团队协作开发
- 便于AI辅助开发

## 决策
采用**四层架构**：Controller → Service → Manager → DAO

### 各层职责

#### Controller层
- 接收HTTP请求
- 参数验证（使用@Valid）
- 调用Service层
- 返回ResponseDTO统一格式
- **禁止**：编写业务逻辑、直接访问DAO

#### Service层
- 业务逻辑处理
- **事务管理（必须在此层）**
- 调用Manager层处理复杂业务
- 调用DAO层访问数据
- **禁止**：直接操作数据库细节

#### Manager层
- 复杂业务逻辑封装
- 跨模块业务协调
- 缓存处理
- **禁止**：管理事务（事务必须在Service层）

#### DAO层
- 数据访问操作
- 继承MyBatis Plus的BaseMapper
- 自定义复杂查询

## 理由

### 1. 职责清晰
每层有明确的职责边界，降低认知负担

### 2. 可测试性强
- Controller层：测试HTTP接口
- Service层：测试业务逻辑（Mock Manager和DAO）
- Manager层：测试复杂业务（Mock DAO）
- DAO层：测试数据访问（H2内存数据库）

### 3. AI友好
明确的分层规范使AI能够：
- 按模板生成符合规范的代码
- 自动验证层级调用关系
- 快速定位违规代码

### 4. 维护性好
- 修改影响范围小
- 代码易于理解
- 便于团队协作

## 约束

### 强制约束
1. ❌ **禁止跨层访问**：Controller不能直接访问DAO
2. ❌ **禁止逆向调用**：DAO不能调用Service
3. ✅ **事务边界**：@Transactional必须在Service层
4. ✅ **统一响应**：Controller必须返回ResponseDTO

### 命名约束
- Controller：{Module}Controller
- Service：{Module}Service
- Manager：{Module}Manager（可选）
- DAO：{Module}Dao

### 依赖注入约束
- 必须使用@Resource注解
- 禁止使用@Autowired

## 后果

### 正面影响
- ✅ 代码结构清晰，新人易上手
- ✅ 单元测试覆盖率提升
- ✅ 代码审查效率提升
- ✅ AI生成代码质量提升

### 负面影响
- ⚠️ 简单CRUD也需要多层调用（可接受的性能损耗）
- ⚠️ 文件数量增加（通过模板减轻负担）

### 风险缓解
- 提供完整的代码模板（docs/templates/）
- 提供自动化检查脚本（scripts/enforce-standards.sh）
- 使用ArchUnit进行架构检查

## 实施示例

### 正确示例 ✅

```java
// Controller层
@RestController
public class DeviceController {
    @Resource
    private DeviceService deviceService;  // ✅ 只注入Service
    
    @PostMapping("/add")
    @SaCheckPermission("device:add")
    public ResponseDTO<String> add(@Valid @RequestBody DeviceAddForm form) {
        return deviceService.add(form);  // ✅ 直接调用Service
    }
}

// Service层
@Service
public class DeviceService {
    @Resource
    private DeviceDao deviceDao;
    
    @Transactional(rollbackFor = Exception.class)  // ✅ 事务在Service层
    public ResponseDTO<String> add(DeviceAddForm form) {
        DeviceEntity entity = new DeviceEntity();
        BeanUtils.copyProperties(form, entity);
        deviceDao.insert(entity);  // ✅ 调用DAO
        return ResponseDTO.ok();
    }
}
```

### 错误示例 ❌

```java
// 错误1: Controller直接访问DAO
@RestController
public class DeviceController {
    @Resource
    private DeviceDao deviceDao;  // ❌ 错误：跨层访问
}

// 错误2: Manager层管理事务
@Component
public class DeviceManager {
    @Transactional  // ❌ 错误：事务不应在Manager层
    public void process() {}
}

// 错误3: Controller编写业务逻辑
@PostMapping("/add")
public ResponseDTO<String> add(@RequestBody DeviceAddForm form) {
    DeviceEntity entity = new DeviceEntity();
    entity.setDeviceName(form.getDeviceName());
    deviceDao.insert(entity);  // ❌ 错误：业务逻辑应在Service层
    return ResponseDTO.ok();
}
```

## 自动化验证

### ArchUnit架构检查
```java
@ArchTest
static final ArchRule controllers_should_only_depend_on_services =
    classes()
        .that().resideInAPackage("..controller..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage("..service..", "..vo..", "..dto..");
```

### 检查脚本
```bash
# 检查Controller是否直接访问DAO
grep -r "@Resource.*Dao" */controller/*.java
```

## 相关决策
- [ADR-002: 依赖注入使用@Resource](ADR-002-use-resource-annotation.md)
- [ADR-003: 统一响应格式使用ResponseDTO](ADR-003-use-response-dto.md)

## 参考资料
- [SmartAdmin开发规范](../DEV_STANDARDS.md)
- [四层架构最佳实践](../repowiki/zh/content/技术架构/四层架构设计.md)
