# SmartAdmin 项目开发指南

> **📋 版本**: v1.0.0
> **📋 创建时间**: 2025-01-10
> **📋 说明**: 本指南为SmartAdmin项目开发的快速参考和最佳实践

---

## 🚀 快速参考部分

### 📝 常用命名规范速查表

#### 后端命名规范

```markdown
# 包名规范
net.lab1024.sa.{module}.{layer}

# 类名规范
Controller: {Module}Controller         → UserController, DeviceController
Service:    {Module}Service           → UserService, DeviceService
Manager:    {Module}Manager           → UserManager, DeviceManager
DAO:        {Module}Dao               → UserDao, DeviceDao
Entity:     {TableName}               → User, Device
VO:         {BusinessName}VO          → UserLoginVO, DeviceListVO
DTO:        {BusinessName}DTO         → UserCreateDTO, DeviceUpdateDTO

# 方法名规范
查询: get/query/find/list          → getUser, queryUsers, findDevice
新增: add/create/save/insert       → addUser, createDevice, saveUser
修改: update/edit/modify           → updateUser, editDevice
删除: delete/remove                → deleteUser, removeDevice

# 变量名规范
驼峰命名，见名知意
Boolean类型: is/has/can开头        → isActive, hasPermission, canEdit
集合类型: 复数形式或List/Map后缀  → users, userList, deviceMap
```

#### 数据库命名规范

```markdown
# 表名规范
t_{business}_{entity}  → t_sys_user, t_access_device, t_attendance_record

# 字段名规范
主键: {table_name}_id  → user_id, device_id, record_id
外键: {ref_table}_id   → user_id, device_id, area_id
时间字段: xxx_time      → create_time, update_time, login_time
状态字段: xxx_status    → device_status, user_status, attendance_status
标志字段: xxx_flag      → deleted_flag, enabled_flag, online_flag

# 索引命名
主键索引: pk_{table}
唯一索引: uk_{table}_{column}
普通索引: idx_{table}_{column}
组合索引: idx_{table}_{column1}_{column2}
```

#### 前端命名规范

```markdown
# 文件名规范
组件: PascalCase                    → UserManagement.vue, DeviceList.vue
API: camelCase.js                   → user.js, device.js, attendance.js
Store: useXxxStore                  → useUserStore, useDeviceStore
路由: kebab-case                    → user-management, device-list

# 变量名规范
常量: UPPER_SNAKE_CASE             → API_BASE_URL, PAGE_SIZE
变量: camelCase                     → userName, deviceList, isLoggedIn
函数: camelCase                     → getUserInfo, handleSubmit, validateForm
CSS类: kebab-case                   → user-management, device-list-item
```

### 🛠️ 代码模板和示例

#### 后端Controller模板

```java
@RestController
@RequestMapping("/api/{module}")
@Tag(name = "{module}管理", description = "{module}相关接口")
public class {Module}Controller {

    @Resource
    private {Module}Service {moduleVar}Service;

    @GetMapping("/page")
    @Operation(summary = "分页查询{module}")
    @SaCheckPermission("{module}:page")
    public ResponseDTO<PageResult<{Module}VO>> queryPage({Module}QueryDTO queryDTO) {
        PageResult<{Module}VO> result = {moduleVar}Service.queryPage(queryDTO);
        return ResponseDTO.ok(result);
    }

    @PostMapping
    @Operation(summary = "新增{module}")
    @SaCheckPermission("{module}:add")
    public ResponseDTO<String> add(@Valid @RequestBody {Module}CreateDTO createDTO) {
        {moduleVar}Service.add(createDTO);
        return ResponseDTO.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改{module}")
    @SaCheckPermission("{module}:update")
    public ResponseDTO<String> update(@PathVariable Long id,
                                     @Valid @RequestBody {Module}UpdateDTO updateDTO) {
        updateDTO.set{id}(id);
        {moduleVar}Service.update(updateDTO);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除{module}")
    @SaCheckPermission("{module}:delete")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        {moduleVar}Service.delete(id);
        return ResponseDTO.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取{module}详情")
    @SaCheckPermission("{module}:detail")
    public ResponseDTO<{Module}DetailVO> getDetail(@PathVariable Long id) {
        {Module}DetailVO detail = {moduleVar}Service.getDetail(id);
        return ResponseDTO.ok(detail);
    }
}
```

#### 后端Service模板

```java
@Service
@Transactional(readOnly = true)
public class {Module}Service {

    @Resource
    private {Module}Manager {moduleVar}Manager;
    @Resource
    private {Module}Dao {moduleVar}Dao;

    public PageResult<{Module}VO> queryPage({Module}QueryDTO queryDTO) {
        // 1. 参数验证
        // 2. 调用Manager层处理业务逻辑
        // 3. 返回分页结果
        return {moduleVar}Manager.queryPage(queryDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add({Module}CreateDTO createDTO) {
        // 1. 业务规则验证
        // 2. 数据转换
        // 3. 调用Manager层处理
        // 4. 记录操作日志
        {moduleVar}Manager.add(createDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update({Module}UpdateDTO updateDTO) {
        // 1. 验证数据存在性
        // 2. 验证业务规则
        // 3. 调用Manager层处理
        // 4. 记录操作日志
        {moduleVar}Manager.update(updateDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 验证数据存在性
        // 2. 验证删除条件
        // 3. 软删除处理
        // 4. 记录操作日志
        {moduleVar}Manager.delete(id);
    }

    public {Module}DetailVO getDetail(Long id) {
        // 1. 验证数据存在性
        // 2. 权限验证
        // 3. 返回详细信息
        return {moduleVar}Manager.getDetail(id);
    }
}
```

#### 后端Manager模板

```java
@Component
public class {Module}Manager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private {Module}Dao {moduleVar}Dao;
    @Resource
    private {OtherModule}Manager {otherModuleVar}Manager;

    // 缓存key前缀
    private static final String CACHE_PREFIX = "{module}:";

    public PageResult<{Module}VO> queryPage({Module}QueryDTO queryDTO) {
        // 1. 缓存查询
        // 2. 数据库查询
        // 3. 数据组装
        // 4. 缓存存储
        return {moduleVar}Dao.queryPage(queryDTO);
    }

    @CacheEvict(value = CACHE_PREFIX, allEntries = true)
    public void add({Module}CreateDTO createDTO) {
        // 1. 复杂业务逻辑处理
        // 2. 跨模块数据操作
        // 3. 缓存更新
        // 4. 事件发布
        {Module}Entity entity = BeanUtil.copyProperties(createDTO, {Module}Entity.class);
        {moduleVar}Dao.insert(entity);
    }

    @CacheEvict(value = CACHE_PREFIX, allEntries = true)
    public void update({Module}UpdateDTO updateDTO) {
        // 1. 数据存在性验证
        // 2. 业务规则检查
        // 3. 跨模块数据同步
        // 4. 缓存更新
        {Module}Entity entity = BeanUtil.copyProperties(updateDTO, {Module}Entity.class);
        {moduleVar}Dao.updateById(entity);
    }

    @CacheEvict(value = CACHE_PREFIX, allEntries = true)
    public void delete(Long id) {
        // 1. 关联数据检查
        // 2. 软删除处理
        // 3. 跨模块数据清理
        // 4. 缓存清理
        {moduleVar}Dao.deleteById(id);
    }

    @Cacheable(value = CACHE_PREFIX, key = "#id")
    public {Module}DetailVO getDetail(Long id) {
        // 1. 数据查询
        // 2. 数据组装
        // 3. 权限过滤
        // 4. 缓存存储
        return {moduleVar}Dao.getDetail(id);
    }
}
```

#### 前端组件模板

```vue
<template>
  <div class="{module-name}-management">
    <!-- 搜索区域 -->
    <a-card class="search-card" :bordered="false">
      <a-form :model="queryForm" layout="inline">
        <a-form-item label="名称">
          <a-input v-model:value="queryForm.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作区域 -->
    <a-card class="table-card" :bordered="false">
      <div class="table-operator">
        <a-button type="primary" @click="handleAdd" v-permission="['{module}:add']">
          <template #icon><PlusOutlined /></template>
          新增
        </a-button>
        <a-button
          type="primary"
          danger
          :disabled="!selectedRowKeys.length"
          @click="handleBatchDelete"
          v-permission="['{module}:delete']"
        >
          <template #icon><DeleteOutlined /></template>
          批量删除
        </a-button>
      </div>

      <!-- 表格区域 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record.id)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record.id)" v-permission="['{module}:update']">编辑</a-button>
              <a-popconfirm title="确定删除吗？" @confirm="handleDelete(record.id)">
                <a-button type="link" size="small" danger v-permission="['{module}:delete']">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { {moduleVar}Api } from '/@/api/{module}'
import { use{Module}Store } from '/@/store/{module}'

// 状态管理
const { moduleVar }Store = use{Module}Store()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const modalTitle = ref('')
const selectedRowKeys = ref([])

// 查询表单
const queryForm = reactive({
  name: '',
  status: undefined
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
})

// 表格数据
const tableData = ref([])

// 表单数据
const formData = reactive({
  id: undefined,
  name: '',
  status: 1
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: 50, message: '名称长度不能超过50个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '名称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 表单引用
const formRef = ref()

// 方法定义
const handleQuery = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(queryForm, {
    name: '',
    status: undefined
  })
  handleQuery()
}

const handleAdd = () => {
  modalTitle.value = '新增{module}'
  modalVisible.value = true
  Object.assign(formData, {
    id: undefined,
    name: '',
    status: 1
  })
}

const handleEdit = (id) => {
  modalTitle.value = '编辑{module}'
  modalVisible.value = true
  fetchDetail(id)
}

const handleView = (id) => {
  // 实现查看逻辑
}

const handleDelete = (id) => {
  {moduleVar}Api.delete(id).then(() => {
    message.success('删除成功')
    fetchData()
  })
}

const handleBatchDelete = () => {
  // 实现批量删除逻辑
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (formData.id) {
      await {moduleVar}Api.update(formData)
      message.success('更新成功')
    } else {
      await {moduleVar}Api.add(formData)
      message.success('新增成功')
    }

    modalVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const onSelectChange = (keys) => {
  selectedRowKeys.value = keys
}

const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    const result = await {moduleVar}Api.queryPage(params)
    tableData.value = result.data.records
    pagination.total = result.data.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchDetail = async (id) => {
  try {
    const result = await {moduleVar}Api.getDetail(id)
    Object.assign(formData, result.data)
  } catch (error) {
    console.error('获取详情失败:', error)
  }
}

const getStatusColor = (status) => {
  return status === 1 ? 'green' : 'red'
}

const getStatusText = (status) => {
  return status === 1 ? '启用' : '禁用'
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style lang="less" scoped>
.{module-name}-management {
  padding: 24px;

  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .table-operator {
      margin-bottom: 16px;
    }
  }
}
</style>
```

#### API封装模板

```javascript
// /@/api/{module}.js
import { request } from '/@/utils/request'

export const {moduleVar}Api = {
  // 分页查询
  queryPage: (params) => {
    return request({
      url: '/api/{module}/page',
      method: 'GET',
      params
    })
  },

  // 获取详情
  getDetail: (id) => {
    return request({
      url: `/api/{module}/${id}`,
      method: 'GET'
    })
  },

  // 新增
  add: (data) => {
    return request({
      url: '/api/{module}',
      method: 'POST',
      data
    })
  },

  // 更新
  update: (data) => {
    return request({
      url: '/api/{module}',
      method: 'PUT',
      data
    })
  },

  // 删除
  delete: (id) => {
    return request({
      url: `/api/{module}/${id}`,
      method: 'DELETE'
    })
  },

  // 批量删除
  batchDelete: (ids) => {
    return request({
      url: '/api/{module}/batch',
      method: 'DELETE',
      data: { ids }
    })
  },

  // 导出
  export: (params) => {
    return request({
      url: '/api/{module}/export',
      method: 'GET',
      params,
      responseType: 'blob'
    })
  }
}
```

### 🎯 常见场景的标准实现方式

#### 1. 树形结构处理

```java
// 实体类
@Data
@TableName("t_sys_menu")
public class MenuEntity {
    @TableId(type = IdType.AUTO)
    private Long menuId;

    private String menuName;
    private Long parentId;
    private Integer sort;
    private String menuType;
    // ... 其他字段

    @TableField(exist = false)
    private List<MenuEntity> children;
}

// Manager层处理树形结构
public List<MenuTreeVO> getMenuTree() {
    // 1. 查询所有菜单
    List<MenuEntity> allMenus = menuDao.selectList(null);

    // 2. 构建树形结构
    return buildMenuTree(allMenus, 0L);
}

private List<MenuTreeVO> buildMenuTree(List<MenuEntity> menus, Long parentId) {
    return menus.stream()
        .filter(menu -> parentId.equals(menu.getParentId()))
        .map(menu -> {
            MenuTreeVO treeVO = BeanUtil.copyProperties(menu, MenuTreeVO.class);
            List<MenuTreeVO> children = buildMenuTree(menus, menu.getMenuId());
            treeVO.setChildren(children);
            return treeVO;
        })
        .sorted(Comparator.comparing(MenuTreeVO::getSort))
        .collect(Collectors.toList());
}
```

#### 2. 导入导出功能

```java
// Controller层
@PostMapping("/import")
@Operation(summary = "导入Excel")
public ResponseDTO<String> importExcel(@RequestParam("file") MultipartFile file) {
    {moduleVar}Service.importExcel(file);
    return ResponseDTO.ok();
}

@GetMapping("/export")
@Operation(summary = "导出Excel")
public void exportExcel({Module}QueryDTO queryDTO, HttpServletResponse response) {
    {moduleVar}Service.exportExcel(queryDTO, response);
}

// Service层
public void importExcel(MultipartFile file) {
    try (InputStream is = file.getInputStream();
         Workbook workbook = WorkbookFactory.create(is)) {

        Sheet sheet = workbook.getSheetAt(0);
        List<{Module}ImportDTO> importList = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            {Module}ImportDTO importDTO = new {Module}ImportDTO();
            // 解析Excel行数据
            parseRowData(row, importDTO);
            importList.add(importDTO);
        }

        // 批量保存
        batchImport(importList);

    } catch (Exception e) {
        throw new BusinessException("导入失败: " + e.getMessage());
    }
}

public void exportExcel({Module}QueryDTO queryDTO, HttpServletResponse response) {
    try {
        // 1. 查询数据
        List<{Module}ExportVO> exportList = {moduleVar}Manager.getExportData(queryDTO);

        // 2. 创建Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("{Module}数据");

        // 3. 创建表头
        createHeaderRow(sheet);

        // 4. 填充数据
        fillDataRows(sheet, exportList);

        // 5. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename={module}.xlsx");

        // 6. 输出文件
        workbook.write(response.getOutputStream());
        workbook.close();

    } catch (Exception e) {
        throw new BusinessException("导出失败: " + e.getMessage());
    }
}
```

#### 3. 权限数据过滤

```java
// Service层实现数据权限过滤
public PageResult<{Module}VO> queryPage({Module}QueryDTO queryDTO) {
    // 1. 获取当前用户信息
    SmartUser currentUser = SmartRequestUtil.getCurrentUser();

    // 2. 构建数据权限条件
    DataPermissionScope permissionScope = dataPermissionService.getPermissionScope(
        currentUser.getUserId(),
        "{module}"
    );

    // 3. 设置权限过滤条件
    queryDTO.setDataPermissionScope(permissionScope);

    // 4. 执行查询
    return {moduleVar}Manager.queryPage(queryDTO);
}

// MyBatis Plus数据权限拦截器
@Component
public class DataPermissionInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler,
                           BoundSql boundSql) throws SQLException {

        if (parameter instanceof BaseQueryDTO) {
            BaseQueryDTO queryDTO = (BaseQueryDTO) parameter;
            DataPermissionScope scope = queryDTO.getDataPermissionScope();

            if (scope != null && scope.hasPermission()) {
                // 添加数据权限SQL条件
                String originalSql = boundSql.getSql();
                String permissionSql = buildPermissionSql(originalSql, scope);
                setSql(boundSql, permissionSql);
            }
        }
    }

    private String buildPermissionSql(String originalSql, DataPermissionScope scope) {
        StringBuilder sqlBuilder = new StringBuilder(originalSql);

        if (scope.getDeptIds() != null && !scope.getDeptIds().isEmpty()) {
            sqlBuilder.append(" AND dept_id IN (")
                     .append(scope.getDeptIds().stream()
                         .map(String::valueOf)
                         .collect(Collectors.joining(",")))
                     .append(")");
        }

        if (scope.getUserIds() != null && !scope.getUserIds().isEmpty()) {
            sqlBuilder.append(" AND create_user_id IN (")
                     .append(scope.getUserIds().stream()
                         .map(String::valueOf)
                         .collect(Collectors.joining(",")))
                     .append(")");
        }

        return sqlBuilder.toString();
    }
}
```

#### 4. 缓存使用示例

```java
// Manager层缓存使用
@Component
public class {Module}Manager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存常量
    private static final String CACHE_KEY_PREFIX = "{module}:";
    private static final Duration CACHE_EXPIRE = Duration.ofMinutes(30);

    @Cacheable(value = "{module}", key = "#id", unless = "#result == null")
    public {Module}VO getById(Long id) {
        return {moduleVar}Dao.selectById(id);
    }

    // 手动缓存控制
    public List<{Module}VO> getHotData() {
        String cacheKey = CACHE_KEY_PREFIX + "hot_data";

        // 1. 尝试从缓存获取
        List<{Module}VO> cachedData = (List<{Module}VO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }

        // 2. 从数据库查询
        List<{Module}VO> data = {moduleVar}Dao.selectHotData();

        // 3. 存入缓存
        redisTemplate.opsForValue().set(cacheKey, data, CACHE_EXPIRE);

        return data;
    }

    // 缓存清除
    @CacheEvict(value = "{module}", allEntries = true)
    public void clearCache() {
        // 清除所有相关缓存
        String pattern = CACHE_KEY_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

#### 5. 异步任务处理

```java
// 异步任务配置
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("{module}-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

// 异步任务使用
@Service
public class {Module}Service {

    @Async("taskExecutor")
    public CompletableFuture<Void> processAsync({Module}ProcessDTO processDTO) {
        try {
            // 异步处理业务逻辑
            doProcess(processDTO);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public void batchProcess(List<{Module}ProcessDTO> processList) {
        List<CompletableFuture<Void>> futures = processList.stream()
            .map(this::processAsync)
            .collect(Collectors.toList());

        // 等待所有异步任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                log.info("所有异步任务处理完成");
            })
            .exceptionally(throwable -> {
                log.error("异步任务处理失败", throwable);
                return null;
            });
    }
}
```

---

## 🔄 开发流程

### 📋 新增功能模块的标准步骤

#### 第一步：需求分析和技术设计

```markdown
1. **需求分析**
   □ 明确业务需求和功能边界
   □ 梳理数据模型和业务流程
   □ 识别权限和安全要求
   □ 确定性能和技术指标

2. **技术设计**
   □ 设计数据库表结构
   □ 定义API接口规范
   □ 设计前后端交互流程
   □ 确定缓存和存储策略
   □ 制定测试计划

3. **设计评审**
   □ 架构师评审设计方案
   □ 产品经理确认业务逻辑
   □ 技术负责人评估技术可行性
   □ 安全评审（如涉及敏感数据）
```

#### 第二步：数据库设计

```markdown
1. **创建表结构**
   □ 遵循命名规范：t_{business}_{entity}
   □ 包含标准审计字段
   □ 设计合适的索引策略
   □ 添加字段注释

2. **示例SQL**
   ```sql
   CREATE TABLE t_{business}_{entity} (
       {entity}_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
       {entity}_name VARCHAR(100) NOT NULL COMMENT '名称',
       {entity}_code VARCHAR(50) UNIQUE COMMENT '编码',
       parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
       sort_order INT DEFAULT 0 COMMENT '排序',
       status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
       create_user_id BIGINT COMMENT '创建人ID',
       deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除'
   ) COMMENT = '{entity}表';

   -- 创建索引
   CREATE INDEX idx_{entity}_parent_id ON t_{business}_{entity}(parent_id);
   CREATE INDEX idx_{entity}_status ON t_{business}_{entity}(status);
   CREATE INDEX idx_{entity}_create_time ON t_{business}_{entity}(create_time);
   ```

3. **权限表关联**
   □ 在权限表中添加对应权限编码
   □ 配置数据权限规则
   □ 设置角色权限关联
```

#### 第三步：后端开发

```markdown
1. **创建四层架构文件**
   □ Entity实体类（对应数据库表）
   □ DAO层（继承BaseMapper）
   □ Manager层（复杂业务逻辑）
   □ Service层（事务管理）
   □ Controller层（接口定义）

2. **代码生成顺序**
   □ Entity → DAO → Manager → Service → Controller
   □ 单元测试（DAO层测试优先）
   □ 集成测试（Service层测试）
   □ 接口测试（Controller层测试）

3. **关键检查点**
   □ 四层架构是否严格分离
   □ 事务边界是否在Service层
   □ 权限注解是否正确配置
   □ 异常处理是否完善
   □ 日志记录是否规范
```

#### 第四步：前端开发

```markdown
1. **文件创建顺序**
   □ API接口文件（/api/{module}.js）
   □ Store状态管理（/store/{module}.js）
   □ 路由配置（/router/{module}.js）
   □ 页面组件（/views/{module}/）
   □ 公共组件（如有需要）

2. **组件开发原则**
   □ 遵循单一职责原则
   □ 使用Vue 3 Composition API
   □ 合理使用Pinia状态管理
   □ 统一错误处理
   □ 响应式设计

3. **关键检查点**
   □ TypeScript类型定义是否完整
   □ 权限控制是否正确
   □ 表单验证是否完善
   □ 用户体验是否良好
   □ 移动端适配是否考虑
```

#### 第五步：测试和集成

```markdown
1. **单元测试**
   □ DAO层CRUD操作测试
   □ Service层业务逻辑测试
   □ Manager层复杂逻辑测试
   □ 工具类方法测试

2. **集成测试**
   □ 接口功能测试
   □ 权限控制测试
   □ 数据一致性测试
   □ 异常场景测试

3. **系统测试**
   □ 业务流程端到端测试
   □ 性能压力测试
   □ 安全渗透测试
   □ 兼容性测试
```

### 📊 数据库变更流程

#### 变更申请和设计

```markdown
1. **变更申请**
   □ 提交数据库变更申请单
   □ 说明变更原因和影响范围
   □ 评估变更风险
   □ 制定回滚方案

2. **变更设计**
   □ 设计新的表结构或字段
   □ 编写DDL语句
   □ 设计数据迁移脚本
   □ 制定兼容性方案
```

#### 变更脚本编写规范

```sql
-- 变更脚本命名规范
-- V{版本号}__{变更描述}.sql
-- 示例：V1.2.0__add_user_avatar_field.sql

-- 标准变更脚本模板
-- ============================================================================
-- 变更说明：添加用户头像字段
-- 影响表：t_sys_user
-- 变更类型：ADD COLUMN
-- 兼容性：向后兼容
-- ============================================================================

-- 1. 添加字段
ALTER TABLE t_sys_user
ADD COLUMN avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL' AFTER email;

-- 2. 创建索引（如需要）
CREATE INDEX idx_user_avatar ON t_sys_user(avatar_url);

-- 3. 数据迁移（如需要）
UPDATE t_sys_user SET avatar_url = '/default/avatar.png' WHERE avatar_url IS NULL;

-- 4. 验证变更
SELECT COUNT(*) as affected_rows FROM t_sys_user WHERE avatar_url IS NOT NULL;
```

#### 变更执行和验证

```markdown
1. **测试环境验证**
   □ 备份测试数据库
   □ 执行变更脚本
   □ 验证数据完整性
   □ 运行回归测试

2. **生产环境执行**
   □ 备份生产数据库
   □ 在低峰期执行变更
   □ 监控执行过程
   □ 验证变更结果

3. **变更后检查**
   □ 检查应用程序是否正常
   □ 验证数据一致性
   □ 监控系统性能
   □ 确认业务功能正常
```

### 📝 代码提交规范

#### Git提交信息规范

```markdown
# 提交信息格式
<type>(<scope>): <subject>

# 示例
feat(user): 添加用户头像上传功能
fix(device): 修复设备状态更新异常问题
docs(api): 更新接口文档
style(code): 统一代码格式
refactor(order): 重构订单计算逻辑
test(unit): 添加用户管理单元测试
chore(deps): 升级Spring Boot版本

# 类型说明
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建工具或辅助工具的变动
perf: 性能优化
revert: 回滚提交
```

#### 分支管理规范

```markdown
# 分支命名规范
master: 主分支，生产环境代码
develop: 开发分支，集成最新功能
feature/xxx: 功能分支
hotfix/xxx: 热修复分支
release/xxx: 发布分支

# 分支操作流程
1. 从develop创建功能分支
   git checkout -b feature/user-management develop

2. 开发完成后提交到功能分支
   git add .
   git commit -m "feat(user): 完成用户管理功能"
   git push origin feature/user-management

3. 合并到develop分支
   git checkout develop
   git merge --no-ff feature/user-management
   git push origin develop

4. 发布时创建release分支
   git checkout -b release/v1.2.0 develop

5. 发布完成后合并到master和develop
   git checkout master
   git merge --no-ff release/v1.2.0
   git tag -a v1.2.0 -m "Release version 1.2.0"
   git push origin master --tags
```

#### 代码审查清单

```markdown
# 代码审查要点

## 架构和设计
□ 是否遵循四层架构规范？
□ 职责分离是否清晰？
□ 设计模式使用是否合理？
□ 代码复用性是否良好？

## 代码质量
□ 命名是否规范和有意义？
□ 方法是否过长（超过50行需要拆分）？
□ 复杂度是否过高（圈复杂度≤10）？
□ 注释是否完整和准确？

## 安全性
□ 权限控制是否正确配置？
□ 敏感信息是否妥善处理？
□ SQL注入防护是否完善？
□ 输入验证是否严格？

## 性能
□ 数据库查询是否优化？
□ 缓存使用是否合理？
□ 循环和递归是否高效？
□ 内存泄漏风险是否存在？

## 测试
□ 单元测试覆盖率是否≥80%？
□ 测试用例是否覆盖边界条件？
□ 异常场景是否测试？
□ 集成测试是否通过？

## 文档
□ API文档是否完整？
□ 注释是否清晰？
□ 变更记录是否详细？
□ 使用示例是否提供？
```

---

## ⚠️ 注意事项

### 🚫 SmartAdmin框架的限制和约束

#### 架构约束

```markdown
❌ 禁止的操作：
- Controller层直接访问DAO层
- 在Controller层编写业务逻辑
- 在Manager层管理事务
- 跨层直接调用（如Service直接调用另一个DAO）

✅ 必须遵守：
- 严格的四层架构调用链：Controller → Service → Manager → DAO
- 事务边界必须在Service层
- 复杂业务逻辑封装在Manager层
- 使用@Resource进行依赖注入，禁止@Autowired
```

#### 技术栈约束

```markdown
❌ 不允许的技术：
- MyBatis XML配置（必须使用注解）
- 直接使用JdbcTemplate
- 自定义缓存实现（必须使用Redis+Caffeine）
- 前端使用jQuery
- 直接操作数据库连接池

✅ 强制要求：
- MyBatis-Plus进行数据库操作
- Sa-Token进行权限认证
- Redis作为缓存中心
- Vue 3 + TypeScript前端开发
- Ant Design Vue组件库
```

#### 编码规范约束

```markdown
❌ 禁止的编码方式：
- 硬编码业务参数
- 在代码中写SQL语句
- 使用System.out.println输出日志
- 忽略异常处理
- 魔法数字和字符串

✅ 必须的编码实践：
- 使用常量定义业务参数
- 复杂SQL在XML或Mapper中定义
- 使用SLF4J进行日志记录
- 完善的异常处理机制
- 有意义的命名和注释
```

### 📋 必须遵守的核心规范

#### 1. 数据库设计核心规范

```markdown
🔴 强制要求：
□ 表名必须使用 t_{business}_{entity} 格式
□ 主键必须使用 {table}_id 格式，类型为BIGINT AUTO_INCREMENT
□ 必须包含标准审计字段：create_time, update_time, create_user_id, deleted_flag
□ 字符集必须使用 utf8mb4，存储引擎使用InnoDB
□ 软删除必须使用 deleted_flag 字段，禁止物理删除
□ JSON字段必须添加格式验证和默认值
□ 索引数量单表不超过8个，选择性≥0.8

🟡 重要建议：
□ 时间字段统一使用 DATETIME 类型
□ 金额字段使用 DECIMAL 类型，禁止使用FLOAT/DOUBLE
□ 状态字段使用 TINYINT 类型，配合枚举使用
□ 树形结构必须有 parent_id, sort_order 字段
```

#### 2. 后端开发核心规范

```markdown
🔴 强制要求：
□ 所有类必须使用 @Resource 注入，禁止 @Autowired
□ 所有Controller方法必须使用 @SaCheckPermission 权限控制
□ 所有Service类必须添加 @Transactional 注解
□ 所有接口返回必须使用 ResponseDTO 统一格式
□ 所有参数必须使用 @Valid 进行验证
□ 所有异常必须使用 SmartException 及其子类
□ 所有日志必须使用 SLF4J，禁止 System.out

🟡 重要建议：
□ Controller层方法不超过20行
□ Service层方法不超过50行
□ Manager层负责复杂业务逻辑和第三方集成
□ 复杂查询必须使用MyBatis-Plus的QueryWrapper
□ 批量操作必须考虑性能和事务边界
```

#### 3. 前端开发核心规范

```markdown
🔴 强制要求：
□ 必须使用 Vue 3 Composition API
□ 必须使用 TypeScript 严格模式
□ 必须使用 Pinia 进行状态管理
□ 必须使用 Ant Design Vue 4.x 组件库
□ 所有API调用必须统一封装在 api/ 目录
□ 所有页面必须有权限控制 v-permission 指令
□ 所有表单必须有完整的验证规则

🟡 重要建议：
□ 组件文件名使用 PascalCase
□ API文件名使用 camelCase
□ 路由配置使用 kebab-case
□ 样式使用 Less 预处理器
□ 使用 @/ 路径别名指代 src/ 目录
□ 合理使用 computed 和 watch
```

#### 4. 安全开发核心规范

```markdown
🔴 强制要求：
□ 所有接口必须使用 Sa-Token 认证
□ 敏感接口必须使用 @SaCheckPermission 权限控制
□ 密码必须使用 BCrypt 加密存储
□ 敏感信息传输必须使用 HTTPS
□ SQL注入防护必须完善
□ XSS攻击防护必须实现
□ 文件上传必须进行安全检查

🟡 重要建议：
□ 敏感操作需要二次确认
□ 重要操作记录审计日志
□ 数据脱敏显示敏感信息
□ 接口访问频率限制
□ 定期进行安全漏洞扫描
```

### 🐛 常见错误和避免方法

#### 1. 架构层面错误

```markdown
❌ 常见错误：
- Controller中直接调用DAO
- Service中直接操作Redis
- Manager中管理事务
- 跨层访问导致代码耦合

✅ 避免方法：
- 严格遵守四层架构调用规范
- 复杂操作放在Manager层
- 缓存操作在Manager层
- 事务边界控制在Service层
- 使用依赖注入而非直接new对象
```

#### 2. 数据库层面错误

```markdown
❌ 常见错误：
- 外键约束导致死锁
- 大事务导致锁等待
- 索引使用不当导致慢查询
- 软删除忘记考虑业务逻辑

✅ 避免方法：
- 避免使用外键约束，使用应用层逻辑保证
- 大事务拆分为小事务
- 合理设计索引，定期分析执行计划
- 所有查询都要考虑 deleted_flag 条件
- 批量操作使用MyBatis-Plus的批量方法
```

#### 3. 缓存层面错误

```markdown
❌ 常见错误：
- 缓存穿透（查询不存在的数据）
- 缓存雪崩（大量缓存同时失效）
- 缓存击穿（热点数据失效）
- 缓存不一致问题

✅ 避免方法：
- 使用空值缓存防止穿透
- 设置随机过期时间防止雪崩
- 使用分布式锁防止击穿
- 遵循缓存更新模式（先更新数据库，再删除缓存）
- 定期监控缓存命中率
```

#### 4. 前端层面错误

```markdown
❌ 常见错误：
- 组件过度复杂导致维护困难
- 状态管理混乱
- 内存泄漏未处理
- 权限控制不严格

✅ 避免方法：
- 遵循单一职责原则拆分组件
- 合理使用Pinia状态管理
- 及时清理定时器和事件监听
- 严格控制页面和按钮级权限
- 使用 TypeScript 防止类型错误
- 合理使用 Vue 的生命周期钩子
```

#### 5. 测试层面错误

```markdown
❌ 常见错误：
- 单元测试覆盖率低
- 测试数据不真实
- 只测试正常流程
- 集成测试不充分

✅ 避免方法：
- 确保单元测试覆盖率≥80%
- 使用真实业务数据进行测试
- 测试各种异常和边界情况
- 编写完整的集成测试
- 定期运行回归测试
- 使用Mock对象隔离依赖
```

### 🔍 开发检查清单

#### 功能开发前检查

```markdown
□ 是否已阅读相关业务文档？
□ 是否已理解技术架构要求？
□ 是否已确认数据库设计方案？
□ 是否已了解相关接口规范？
□ 是否已确认权限和安全要求？
□ 是否已制定开发和测试计划？
```

#### 代码提交前检查

```markdown
□ 代码是否遵循命名规范？
□ 是否已添加必要的注释？
□ 是否已处理所有异常情况？
□ 是否已添加权限控制？
□ 是否已编写单元测试？
□ 代码审查是否已通过？
□ 是否已更新相关文档？
```

#### 功能上线前检查

```markdown
□ 所有功能测试是否通过？
□ 性能测试是否达标？
□ 安全测试是否通过？
□ 数据库脚本是否已执行？
□ 配置文件是否已更新？
□ 监控配置是否已完成？
□ 回滚方案是否已准备？
□ 用户文档是否已更新？
```

---

**📞 技术支持**：

- **架构问题**: 联系架构师
- **业务问题**: 联系产品经理
- **安全问题**: 联系安全团队
- **运维问题**: 联系运维团队

**📚 相关文档**：

- [综合开发规范文档](./DEV_STANDARDS.md)
- [文档分析报告](./DOCUMENT_ANALYSIS_REPORT.md)
- [通用开发检查清单](./CHECKLISTS/通用开发检查清单.md)
- [各业务模块专用清单](./CHECKLISTS/)

---

*最后更新：2025-01-10*
*维护者：SmartAdmin开发团队*