# 导出模块架构重构文档 (v2.0.0)

## 📋 概述

**更新时间**: 2025-12-26
**版本**: 2.0.0
**状态**: ✅ 企业级架构合规

### 重构目标

解决 v1.0.0 的架构违规问题（依赖 Servlet API），实现职责分离的细粒度架构。

---

## 🏗️ 架构设计

### 核心原则

```
关注点分离:
├── 核心脱敏引擎 (Pure Java)  ← 不依赖任何 Web API
├── 结果封装 (Pure Java)       ← 元数据和统计数据
└── Controller辅助层 (业务服务) ← 处理 HTTP 响应
```

### 模块职责

| 组件 | 职责 | 包路径 | 依赖 |
|------|------|--------|------|
| **DataMaskingExporter** | 扫描@Masked注解并脱敏 | `util/` | 纯Java，只依赖common-core |
| **ExportResult** | 封装导出结果和元数据 | `domain/vo/` | 纯Java，Lombok |
| **ExportHelper** | Controller层HTTP响应处理 | `util/` | EasyExcel，业务服务使用 |

---

## 📦 使用指南

### 1. Controller层使用（推荐方式）

#### 1.1 Excel导出（自动脱敏）

```java
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 导出用户列表（自动脱敏）
     */
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) throws IOException {
        // 1. 查询数据
        List<UserEntity> users = userService.getAllUsers();

        // 2. 设置响应头
        String fileName = ExportHelper.generateTimestampFileName("用户列表");
        String contentDisposition = ExportHelper.generateContentDisposition(fileName, ".xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", contentDisposition);
        response.setCharacterEncoding("UTF-8");

        // 3. 导出（自动脱敏）
        ExportHelper.exportExcelWithMasking(
            response.getOutputStream(),
            users,
            UserEntity.class,
            "用户列表",
            fileName
        );
    }
}
```

#### 1.2 使用ExportResult（获取导出统计）

```java
@GetMapping("/export-with-stats")
public ResponseDTO<ExportResultVO> exportWithStats(HttpServletResponse response) {
    // 1. 查询数据
    List<UserEntity> users = userService.getAllUsers();

    // 2. 脱敏处理
    List<UserEntity> maskedUsers = DataMaskingExporter.maskDataList(users, UserEntity.class);

    // 3. 构建结果
    ExportResult<UserEntity> result = ExportResult.success(
        maskedUsers,
        UserEntity.class,
        users.size(),
        users.size(),  // 假设全部成功
        0             // 跳过数量
    );

    // 4. 记录日志
    log.info("[导出统计] {}", result.getStatistics());
    // 输出: 总数: 100, 成功: 100, 跳过: 0, 完全成功: true

    // 5. 写入响应
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", ExportHelper.generateContentDisposition(
        result.getSuggestedFileName(), ".xlsx"
    ));

    ExportHelper.exportExcelWithMasking(
        response.getOutputStream(),
        users,
        UserEntity.class
    );

    return ResponseDTO.ok(ExportResultVO.from(result));
}
```

#### 1.3 自定义脱敏策略

```java
@GetMapping("/export-custom")
public void exportWithCustomMasking(HttpServletResponse response) throws IOException {
    List<UserEntity> users = userService.getAllUsers();

    // 使用仅脱敏手机号的策略
    DataMaskingExporter.MaskingStrategy<UserEntity> strategy =
        new DataMaskingExporter.PhoneOnlyMaskingStrategy<>(UserEntity.class);

    List<UserEntity> maskedUsers = DataMaskingExporter.maskDataListWithStrategy(
        users,
        UserEntity.class,
        strategy
    );

    // 导出...
    ExportHelper.exportExcelWithMasking(response.getOutputStream(), maskedUsers, UserEntity.class);
}
```

#### 1.4 大数据量导出（流式处理）

```java
@GetMapping("/export-large")
public void exportLargeDataset(HttpServletResponse response) throws IOException {
    // 分批查询，避免内存溢出
    int pageSize = 10000;
    int pageNum = 1;

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", ExportHelper.generateContentDisposition("大数据量导出", ".xlsx"));

    try (OutputStream outputStream = response.getOutputStream()) {
        // 使用流式处理
        while (true) {
            List<UserEntity> batch = userService.getUsersByPage(pageNum, pageSize);
            if (batch.isEmpty()) break;

            // 流式脱敏
            List<UserEntity> maskedBatch = DataMaskingExporter.maskDataListStream(
                batch,
                UserEntity.class
            );

            // 分批写入（需要自定义分批写入逻辑）
            // ExcelWriter.write(maskedBatch)

            pageNum++;
        }
    }
}
```

### 2. Entity定义（使用@Masked注解）

```java
@Data
@TableName("t_user")
public class UserEntity {

    @TableId
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    /**
     * 手机号（自动脱敏）
     */
    @Schema(description = "手机号")
    @Masked(Masked.MaskType.PHONE)
    private String phone;

    /**
     * 身份证号（自动脱敏）
     */
    @Schema(description = "身份证号")
    @Masked(Masked.MaskType.ID_CARD)
    private String idCard;

    /**
     * 真实姓名（自动脱敏）
     */
    @Schema(description = "真实姓名")
    @Masked(Masked.MaskType.NAME)
    private String realName;

    @Schema(description = "邮箱")
    @Masked(Masked.MaskType.EMAIL)
    private String email;

    // 普通字段不脱敏
    @Schema(description = "部门名称")
    private String departmentName;
}
```

### 3. 自定义脱敏策略

```java
/**
 * 自定义脱敏策略：只脱敏特定字段
 */
public class CustomMaskingStrategy<T> implements DataMaskingExporter.MaskingStrategy<T> {

    private final Class<T> modelClass;

    public CustomMaskingStrategy(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    @Override
    public T mask(T data) {
        try {
            T target = modelClass.getDeclaredConstructor().newInstance();
            Field[] fields = modelClass.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(data);

                // 自定义脱敏逻辑
                if (value != null && value instanceof String) {
                    Masked masked = field.getAnnotation(Masked.class);
                    if (masked != null) {
                        // 只处理特定的脱敏类型
                        if (masked.value() == Masked.MaskType.PHONE) {
                            value = DataMaskingUtil.maskPhone((String) value);
                        } else {
                            // 其他类型不脱敏
                            // value = value; // 保持原值
                        }
                    }
                }

                field.set(target, value);
            }

            return target;
        } catch (Exception e) {
            log.warn("[自定义脱敏] 处理失败: error={}", e.getMessage());
            return data;
        }
    }
}
```

---

## 🔍 脱敏类型说明

### 支持的脱敏类型

| 脱敏类型 | @Masked注解值 | 脱敏示例 | 说明 |
|---------|--------------|---------|------|
| 手机号 | `MaskType.PHONE` | `138****1234` | 保留前3位和后4位 |
| 身份证 | `MaskType.ID_CARD` | `110101********1234` | 保留前6位和后4位 |
| 姓名 | `MaskType.NAME` | `张*` 或 `张**三` | 2字保留首字符，3字保留首尾 |
| 邮箱 | `MaskType.EMAIL` | `u***@example.com` | 保留首字符和域名 |
| 银行卡 | `MaskType.BANK_CARD` | `6222***********1234` | 保留前4位和后4位 |
| 密码 | `MaskType.PASSWORD` | `********` | 全部掩码 |
| 地址 | `MaskType.ADDRESS` | `北京市朝阳区****` | 保留省市区信息 |
| 默认 | `MaskType.DEFAULT` | `****` | 全部掩码 |

### 脱敏规则实现

```java
// DataMaskingUtil 中的实现规则

public static String maskPhone(String phone) {
    if (!hasText(phone) || phone.length() < 11) return phone;
    return phone.substring(0, 3) + "****" + phone.substring(7);
}

public static String maskIdCard(String idCard) {
    if (!hasText(idCard) || idCard.length() < 15) return idCard;
    int len = idCard.length();
    return idCard.substring(0, 6) + "*".repeat(len - 10) + idCard.substring(len - 4);
}

public static String maskName(String name) {
    if (!hasText(name)) return name;
    int len = name.length();
    if (len == 2) {
        return String.valueOf(name.charAt(0)) + MASK_CHAR;
    } else if (len == 3) {
        return String.valueOf(name.charAt(0)) + MASK_CHAR + name.charAt(2);
    } else {
        return name.charAt(0) + "*".repeat(len - 1);
    }
}

public static String maskEmail(String email) {
    if (!hasText(email) || !email.contains("@")) return email;
    int atIndex = email.indexOf("@");
    String prefix = email.substring(0, atIndex);
    if (prefix.length() <= 1) return email;
    return prefix.charAt(0) + "***" + email.substring(atIndex);
}
```

---

## 🎯 高级用法

### 1. 条件脱敏

```java
/**
 * 基于权限的条件脱敏
 */
public class PermissionBasedMaskingStrategy<T> implements DataMaskingExporter.MaskingStrategy<T> {

    private final Class<T> modelClass;
    private final boolean canViewSensitiveData;  // 权限标志

    public PermissionBasedMaskingStrategy(Class<T> modelClass, boolean canViewSensitiveData) {
        this.modelClass = modelClass;
        this.canViewSensitiveData = canViewSensitiveData;
    }

    @Override
    public T mask(T data) {
        if (canViewSensitiveData) {
            // 有权限，不脱敏
            return data;
        }

        // 无权限，执行脱敏
        return DataMaskingExporter.maskDataList(
            Collections.singletonList(data),
            modelClass
        ).get(0);
    }
}
```

### 2. 多级脱敏

```java
/**
 * 多级脱敏：根据数据敏感度选择脱敏级别
 */
public class MultiLevelMaskingStrategy<T> implements DataMaskingExporter.MaskingStrategy<T> {

    public enum SensitivityLevel {
        PUBLIC,      // 公开数据：不脱敏
        INTERNAL,    // 内部数据：轻度脱敏
        CONFIDENTIAL // 机密数据：重度脱敏
    }

    private final Class<T> modelClass;
    private final SensitivityLevel level;

    public MultiLevelMaskingStrategy(Class<T> modelClass, SensitivityLevel level) {
        this.modelClass = modelClass;
        this.level = level;
    }

    @Override
    public T mask(T data) {
        switch (level) {
            case PUBLIC:
                return data;  // 不脱敏
            case INTERNAL:
                return new DataMaskingExporter.PhoneOnlyMaskingStrategy<>(modelClass).mask(data);
            case CONFIDENTIAL:
                return DataMaskingExporter.maskDataList(
                    Collections.singletonList(data),
                    modelClass
                ).get(0);
            default:
                return data;
        }
    }
}
```

### 3. 导出任务异步化

```java
@Service
public class AsyncExportService {

    @Async("exportTaskExecutor")
    public void exportAsync(Long userId, String exportType) {
        try {
            // 1. 查询数据
            List<UserEntity> users = userService.getAllUsers();

            // 2. 脱敏处理
            List<UserEntity> maskedUsers = DataMaskingExporter.maskDataList(
                users,
                UserEntity.class
            );

            // 3. 生成文件
            String fileName = ExportHelper.generateTimestampFileName("用户列表");
            String filePath = tempDir + fileName + ".xlsx";

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                ExportHelper.exportExcelWithMasking(
                    fos,
                    maskedUsers,
                    UserEntity.class
                );
            }

            // 4. 保存导出记录
            exportTaskService.updateTaskStatus(userId, filePath, "COMPLETED");

        } catch (Exception e) {
            log.error("[异步导出] 失败: userId={}, error={}", userId, e.getMessage(), e);
            exportTaskService.updateTaskStatus(userId, null, "FAILED");
        }
    }
}
```

---

## 📊 性能优化建议

### 1. 大数据量处理

```java
// ✅ 推荐：分批处理 + 流式脱敏
List<T> allData = new ArrayList<>();
int batchSize = 1000;

for (int i = 0; i < totalRecords; i += batchSize) {
    List<T> batch = queryData(i, batchSize);
    List<T> maskedBatch = DataMaskingExporter.maskDataListStream(batch, modelClass);
    allData.addAll(maskedBatch);
}
```

### 2. 缓存反射结果

```java
// DataMaskingExporter 内部已优化
// - 使用 Field.setAccessible(true) 缓存
// - 避免重复的反射调用
```

### 3. 并行处理

```java
// ✅ 大数据集可使用并行流
List<T> maskedData = dataList.parallelStream()
    .map(data -> {
        try {
            return copyAndMaskObject(data, modelClass);
        } catch (Exception e) {
            return data;
        }
    })
    .collect(Collectors.toList());
```

---

## ⚠️ 注意事项

### 1. 架构合规性

```java
// ✅ 正确：在Controller层使用ExportHelper
@RestController
public class UserController {
    public void export(HttpServletResponse response) {
        ExportHelper.exportExcelWithMasking(response.getOutputStream(), data, UserEntity.class);
    }
}

// ❌ 错误：在common-export中依赖Servlet API
// 违反细粒度模块架构原则
```

### 2. Entity字段映射

```java
// ⚠️ 注意：EasyExcel使用 @ExcelProperty 注解
@Data
@TableName("t_user")
public class UserEntity {

    @ExcelProperty("用户ID")
    @TableId
    private Long userId;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("手机号")
    @Masked(Masked.MaskType.PHONE)
    private String phone;
}
```

### 3. 异常处理

```java
// ✅ 推荐：捕获并记录异常
try {
    ExportHelper.exportExcelWithMasking(outputStream, dataList, UserEntity.class);
} catch (IOException e) {
    log.error("[导出失败] error={}", e.getMessage(), e);
    throw new SystemException("EXPORT_ERROR", "导出失败", e);
}

// ⚠️ 注意：脱敏失败时保留原数据
// DataMaskingExporter 内部已处理，不会因单个对象失败而导致整体失败
```

---

## 🧪 测试建议

### 1. 单元测试

```java
@SpringBootTest
class DataMaskingExporterTest {

    @Test
    void testMaskPhone() {
        UserEntity user = new UserEntity();
        user.setPhone("13812345678");

        List<UserEntity> masked = DataMaskingExporter.maskDataList(
            Collections.singletonList(user),
            UserEntity.class
        );

        assertEquals("138****5678", masked.get(0).getPhone());
    }

    @Test
    void testMaskName() {
        UserEntity user = new UserEntity();
        user.setRealName("张三");

        List<UserEntity> masked = DataMaskingExporter.maskDataList(
            Collections.singletonList(user),
            UserEntity.class
        );

        assertEquals("张*三", masked.get(0).getRealName());
    }
}
```

### 2. 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testExportEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/user/export"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Disposition"));
    }
}
```

---

## 📚 相关文档

- **[数据脱敏规范](../../documentation/technical/DATA_MASKING_STANDARD.md)**
- **[细粒度模块架构](../../CLAUDE.md#-细粒度模块架构状态)**
- **[MyBatis-Plus使用指南](../../documentation/technical/MYBATIS_PLUS_GUIDE.md)**
- **[EasyExcel官方文档](https://easyexcel.opensource.alibaba.com/)**

---

## 📝 更新日志

### v2.0.0 (2025-12-26)

**✅ 架构重构**:
- 移除 Servlet API 依赖，实现纯Java核心引擎
- 创建 DataMaskingExporter（`util/`包 - 纯Java脱敏引擎）
- 创建 ExportResult（`domain/vo/`包 - 结果封装）
- 创建 ExportHelper（`util/`包 - Controller层辅助工具）

**📦 包结构标准化**:
- 调整为标准包结构：`util/`和`domain/vo/`
- 符合CLAUDE.md细粒度模块架构规范
- 所有组件遵循统一的包路径约定

**🎯 功能增强**:
- 支持自定义脱敏策略（MaskingStrategy接口）
- 支持流式处理（maskDataListStream）
- 支持导出统计（ExportResult）
- 预定义策略（PhoneOnlyMaskingStrategy、SensitiveInfoMaskingStrategy）

**🔧 质量提升**:
- 企业级异常处理和日志记录
- 完整的JavaDoc文档
- 丰富的使用示例

### v1.0.0 (已废弃)

⚠️ **架构违规**: ExcelExportMaskingUtil.java 依赖 jakarta.servlet.http.HttpServletResponse
❌ **状态**: 已重命名为 .java.disabled

---

**👥 维护团队**: IOE-DREAM 架构委员会
**📅 最后更新**: 2025-12-26
**✅ 架构合规**: 已通过细粒度模块架构验证
