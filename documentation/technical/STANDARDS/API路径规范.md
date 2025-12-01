# API路径规范

## 📋 文档说明

本文档规定SmartAdmin项目中前后端API路径的统一规范，确保接口路径清晰、一致、易维护。

**强制执行**：所有新开发的Controller和前端API调用必须严格遵守本规范。

---

## 🎯 Controller @RequestMapping规范

### 基本原则
- **简洁明了**：路径应该简短、语义清晰
- **统一风格**：全项目保持一致的命名风格
- **RESTful风格**：尽量遵循RESTful设计原则
- **不加冗余前缀**：不使用`/api`、`/v1`等前缀

---

## 📐 路径格式规范

### 系统模块（System）

**格式**：`/{module}`

```java
@RestController
@RequestMapping("/{module}")  // 不加前缀，直接使用模块名
@SaCheckLogin
public class XxxController {
    // ...
}
```

**标准示例**：

| 模块 | Controller注解 | 完整路径示例 |
|------|---------------|-------------|
| 菜单管理 | `@RequestMapping("/menu")` | `/menu/query` |
| 员工管理 | `@RequestMapping("/employee")` | `/employee/add` |
| 角色管理 | `@RequestMapping("/role")` | `/role/query` |
| 部门管理 | `@RequestMapping("/department")` | `/department/tree` |
| 区域管理 | `@RequestMapping("/area")` | `/area/query` |

### 支持模块（Support）

**格式**：`/{module}`（与系统模块相同，不加support前缀）

```java
@RestController
@RequestMapping("/{module}")
public class XxxController {
    // ...
}
```

**标准示例**：

| 模块 | Controller注解 | 完整路径示例 |
|------|---------------|-------------|
| 数据字典 | `@RequestMapping("/dict")` | `/dict/query` |
| 参数配置 | `@RequestMapping("/config")` | `/config/list` |
| 文件管理 | `@RequestMapping("/file")` | `/file/upload` |
| 定时任务 | `@RequestMapping("/job")` | `/job/list` |
| 消息管理 | `@RequestMapping("/message")` | `/message/query` |

### 业务模块（Business）

**格式**：`/{business}/{module}` 或 `/{module}`

```java
// 方式1：有业务域前缀
@RestController
@RequestMapping("/oa/{module}")
public class XxxController {
    // ...
}

// 方式2：无前缀（推荐）
@RestController
@RequestMapping("/{module}")
public class XxxController {
    // ...
}
```

**标准示例**：

| 模块 | Controller注解 | 完整路径示例 |
|------|---------------|-------------|
| 商品管理 | `@RequestMapping("/goods")` | `/goods/query` |
| 订单管理 | `@RequestMapping("/order")` | `/order/list` |
| 通知公告 | `@RequestMapping("/notice")` | `/notice/query` |
| 企业管理 | `@RequestMapping("/oa/enterprise")` | `/oa/enterprise/query` |

---

## 🔧 路由方法规范

### 查询类接口

| 操作 | HTTP方法 | 路径 | 说明 |
|------|---------|------|------|
| 列表查询 | `GET` | `/list` | 简单列表（无条件） |
| 条件查询 | `POST` | `/query` | 复杂条件查询（推荐） |
| 分页查询 | `POST` | `/page` | 分页列表（不推荐，建议用query） |
| 树形查询 | `GET` | `/tree` | 获取树形结构 |
| 详情查询 | `GET` | `/detail/{id}` | 获取单条记录 |

**推荐用法**：
```java
// ✅ 推荐：使用query进行条件查询（支持分页）
@PostMapping("/query")
public ResponseDTO<PageResult<AreaVO>> queryPage(@RequestBody AreaQueryForm form) {
    // ...
}

// ✅ 推荐：简单列表用GET /list
@GetMapping("/list")
public ResponseDTO<List<AreaVO>> list() {
    // ...
}

// ✅ 推荐：树形结构用GET /tree
@GetMapping("/tree")
public ResponseDTO<List<AreaTreeVO>> tree() {
    // ...
}

// ❌ 不推荐：使用page
@PostMapping("/page")
public ResponseDTO<PageResult<AreaVO>> page(@RequestBody AreaQueryForm form) {
    // 建议改为 /query
}
```

### 写入类接口

| 操作 | HTTP方法 | 路径 | 说明 |
|------|---------|------|------|
| 新增 | `POST` | `/add` | 创建新记录 |
| 更新 | `POST` | `/update` | 更新记录 |
| 删除 | `POST` | `/delete/{id}` | 删除单条 |
| 批量删除 | `POST` | `/batchDelete` | 批量删除 |
| 保存 | `POST` | `/save` | 新增或更新（不推荐） |

**推荐用法**：
```java
// ✅ 推荐：新增用add
@PostMapping("/add")
public ResponseDTO<Long> add(@RequestBody AreaAddForm form) {
    // ...
}

// ✅ 推荐：更新用update
@PostMapping("/update")
public ResponseDTO<Void> update(@RequestBody AreaUpdateForm form) {
    // ...
}

// ✅ 推荐：删除用delete
@PostMapping("/delete/{id}")
public ResponseDTO<Void> delete(@PathVariable Long id) {
    // ...
}

// ❌ 不推荐：使用save（新增还是更新不明确）
@PostMapping("/save")
public ResponseDTO<Long> save(@RequestBody AreaForm form) {
    // 建议拆分为 add 和 update
}
```

### 其他操作

| 操作 | HTTP方法 | 路径 | 说明 |
|------|---------|------|------|
| 导出 | `POST` | `/export` | 导出数据 |
| 导入 | `POST` | `/import` | 导入数据 |
| 上传 | `POST` | `/upload` | 上传文件 |
| 下载 | `GET` | `/download/{id}` | 下载文件 |
| 启用 | `POST` | `/enable/{id}` | 启用记录 |
| 禁用 | `POST` | `/disable/{id}` | 禁用记录 |

---

## 💻 前后端对应关系

### 后端Controller示例
```java
@RestController
@RequestMapping("/area")
@Tag(name = "区域管理")
@SaCheckLogin
public class AreaController {

    @Resource
    private AreaService areaService;

    // 条件查询（含分页）
    @PostMapping("/query")
    @SaCheckPermission("system:area:query")
    public ResponseDTO<PageResult<AreaVO>> queryPage(@RequestBody AreaQueryForm form) {
        return ResponseDTO.ok(areaService.queryPage(form));
    }

    // 树形查询
    @GetMapping("/tree")
    @SaCheckPermission("system:area:query")
    public ResponseDTO<List<AreaTreeVO>> getTree() {
        return ResponseDTO.ok(areaService.getTree());
    }

    // 详情查询
    @GetMapping("/detail/{id}")
    @SaCheckPermission("system:area:query")
    public ResponseDTO<AreaVO> getDetail(@PathVariable Long id) {
        return ResponseDTO.ok(areaService.getDetail(id));
    }

    // 新增
    @PostMapping("/add")
    @SaCheckPermission("system:area:add")
    public ResponseDTO<Long> add(@RequestBody AreaAddForm form) {
        return ResponseDTO.ok(areaService.add(form));
    }

    // 更新
    @PostMapping("/update")
    @SaCheckPermission("system:area:update")
    public ResponseDTO<Void> update(@RequestBody AreaUpdateForm form) {
        areaService.update(form);
        return ResponseDTO.ok();
    }

    // 删除
    @PostMapping("/delete/{id}")
    @SaCheckPermission("system:area:delete")
    public ResponseDTO<Void> delete(@PathVariable Long id) {
        areaService.delete(id);
        return ResponseDTO.ok();
    }
}
```

### 前端API封装示例
```javascript
/*
 * 区域管理API
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const areaApi = {
  /**
   * 分页查询区域
   */
  queryPage: (param) => {
    return postRequest('/area/query', param);  // ✅ 与后端路径一致
  },

  /**
   * 查询区域树
   */
  getAreaTree: () => {
    return getRequest('/area/tree');  // ✅ 与后端路径一致
  },

  /**
   * 查询区域详情
   */
  getDetail: (areaId) => {
    return getRequest(`/area/detail/${areaId}`);  // ✅ 与后端路径一致
  },

  /**
   * 新增区域
   */
  add: (param) => {
    return postRequest('/area/add', param);  // ✅ 与后端路径一致
  },

  /**
   * 更新区域
   */
  update: (param) => {
    return postRequest('/area/update', param);  // ✅ 与后端路径一致
  },

  /**
   * 删除区域
   */
  delete: (areaId) => {
    return postRequest(`/area/delete/${areaId}`);  // ✅ 与后端路径一致
  },

  /**
   * 批量删除区域
   */
  batchDelete: (areaIds) => {
    return postRequest('/area/batchDelete', areaIds);  // ✅ 与后端路径一致
  },
};
```

---

## ⚠️ 常见错误示例

### ❌ 错误1：使用多余的前缀
```java
// ❌ 错误：不要使用 /api 前缀
@RequestMapping("/api/system/area")

// ❌ 错误：不要使用 /v1 版本前缀
@RequestMapping("/v1/area")

// ✅ 正确：直接使用模块名
@RequestMapping("/area")
```

### ❌ 错误2：路径层级过深
```java
// ❌ 错误：路径层级太深
@RequestMapping("/system/management/area")

// ✅ 正确：保持简洁
@RequestMapping("/area")
```

### ❌ 错误3：前后端路径不一致
```java
// 后端
@RequestMapping("/api/system/area")
@PostMapping("/page")
// → 完整路径：/api/system/area/page

// 前端 ❌ 错误
postRequest('/area/query', param)  // 不匹配

// 前端 ✅ 正确
postRequest('/api/system/area/page', param)  // 或者修改后端
```

### ❌ 错误4：HTTP方法使用不当
```java
// ❌ 错误：查询操作应该用GET
@PostMapping("/list")  // list应该用GET

// ✅ 正确
@GetMapping("/list")

// ❌ 错误：删除操作不应该用GET
@GetMapping("/delete/{id}")  // 有副作用的操作不应该用GET

// ✅ 正确
@PostMapping("/delete/{id}")
```

---

## 🔍 RESTful风格对比（可选）

### 标准RESTful风格
```java
// RESTful风格（严格遵循REST规范）
@GetMapping("")  // 查询列表
@GetMapping("/{id}")  // 查询详情
@PostMapping("")  // 新增
@PutMapping("/{id}")  // 更新
@DeleteMapping("/{id}")  // 删除
```

### SmartAdmin风格（当前项目）
```java
// SmartAdmin风格（更直观，推荐使用）
@PostMapping("/query")  // 查询列表
@GetMapping("/detail/{id}")  // 查询详情
@PostMapping("/add")  // 新增
@PostMapping("/update")  // 更新
@PostMapping("/delete/{id}")  // 删除
```

**说明**：
- SmartAdmin采用更直观的路径命名方式
- 虽然不完全符合RESTful规范，但更易理解和维护
- 项目统一使用SmartAdmin风格

---

## 📝 新模块API路径定义流程

1. **确定模块名称**：使用简洁的英文单词（如：area, employee, goods）
2. **定义Controller注解**：`@RequestMapping("/{module}")`
3. **列出功能清单**：列出所有接口
4. **定义方法路径**：从标准路径词汇表中选择
5. **前端同步**：确保前端API路径与后端一致
6. **文档更新**：在Swagger中添加清晰的注释

### 示例：新增"考勤管理"模块

```java
// 后端Controller
@RestController
@RequestMapping("/attendance")  // 步骤2：定义Controller路径
@Tag(name = "考勤管理")
@SaCheckLogin
public class AttendanceController {

    @PostMapping("/query")  // 步骤4：定义方法路径
    @SaCheckPermission("system:attendance:query")
    public ResponseDTO<PageResult<AttendanceVO>> queryPage(@RequestBody AttendanceQueryForm form) {
        // ...
    }

    @PostMapping("/add")
    @SaCheckPermission("system:attendance:add")
    public ResponseDTO<Long> add(@RequestBody AttendanceAddForm form) {
        // ...
    }

    @PostMapping("/clockIn")  // 特殊操作：打卡
    @SaCheckPermission("system:attendance:clockIn")
    public ResponseDTO<Void> clockIn(@RequestBody ClockInForm form) {
        // ...
    }
}
```

```javascript
// 前端API封装
export const attendanceApi = {
  queryPage: (param) => postRequest('/attendance/query', param),  // 步骤5：前端同步
  add: (param) => postRequest('/attendance/add', param),
  clockIn: (param) => postRequest('/attendance/clockIn', param),
};
```

---

## 🎓 最佳实践建议

1. **保持简洁**：路径应该简短明了，避免冗余
2. **语义清晰**：路径名称应该清楚表达接口用途
3. **统一风格**：全项目使用相同的命名风格
4. **前后端一致**：确保前端调用路径与后端定义完全一致
5. **及时文档化**：使用Swagger注解清晰描述接口
6. **定期审查**：定期检查API路径规范性，清理不规范的接口

---

## 🔗 路径与权限对应关系

| 后端路径 | HTTP方法 | 权限标识 | 前端调用 |
|---------|---------|---------|---------|
| `/area/query` | POST | `system:area:query` | `areaApi.queryPage()` |
| `/area/tree` | GET | `system:area:query` | `areaApi.getAreaTree()` |
| `/area/add` | POST | `system:area:add` | `areaApi.add()` |
| `/area/update` | POST | `system:area:update` | `areaApi.update()` |
| `/area/delete/{id}` | POST | `system:area:delete` | `areaApi.delete()` |

---

## 📖 相关文档

- [权限命名规范](./权限命名规范.md)
- [前端组件开发规范](./前端组件开发规范.md)
- [AI辅助开发检查清单](../AI_DEV_CHECKLIST.md)

---

**版本**：v1.0
**更新时间**：2025-11-11
**维护人**：开发团队
