---
name: frontend-api-development
description: IOE-DREAM前端Vue.js API开发规范和最佳实践
version: 1.0.0
---

# IOE-DREAM前端API开发规范

> **技术栈**: Vue 3.4 + Ant Design Vue 4 + Vite 5 + TypeScript
> **目标**: 统一前端API调用规范，确保PC端体验一致性
> **适用**: IOE-DREAM智慧园区一卡通管理后台

## 🎯 快速开始

### 1. 前端API调用规范

```yaml
# 标准路径格式
{base_url}/api/v1/{module}/{entity}/{action}

# 示例
/api/v1/access/record/query          # 查询门禁记录
/api/v1/consume/transaction/execute    # 执行消费交易
/api/v1/system/user/query            # 查询用户列表
```

### 2. 统一请求配置

```javascript
// 必需请求头
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer {token}',
  'X-Request-ID': '{unique_request_id}',
  'X-Timestamp': '{unix_timestamp}'
}
```

## 🖥️ 核心前端API模块

### 1. 用户认证 (/api/v1/auth)

```javascript
// src/api/auth.js
export const authApi = {
  // 登录
  login: (credentials) => {
    return api.post('/auth/login', credentials, {
      encrypt: true // 登录信息加密
    });
  },

  // 退出登录
  logout: () => {
    return api.post('/auth/logout');
  },

  // 刷新Token
  refreshToken: () => {
    return api.post('/auth/refresh-token');
  },

  // 获取验证码
  getCaptcha: () => {
    return api.get('/auth/captcha');
  },

  // 双因子认证
  twoFactorAuth: (code) => {
    return api.post('/auth/two-factor', { code });
  }
};
```

### 2. 门禁管理 (/api/v1/access)

```javascript
// src/api/access.js
export const accessApi = {
  // 门禁记录
  queryRecords: (params) => {
    return api.post('/access/record/query', params);
  },

  getRecordDetail: (id) => {
    return api.get(`/access/record/${id}`);
  },

  // 设备管理
  queryDevices: (params) => {
    return api.post('/access/device/query', params);
  },

  addDevice: (data) => {
    return api.post('/access/device/add', data);
  },

  updateDevice: (id, data) => {
    return api.put(`/access/device/${id}`, data);
  },

  deleteDevice: (id) => {
    return api.delete(`/access/device/${id}`);
  },

  // 权限管理
  queryPermissions: (params) => {
    return api.post('/access/permission/query', params);
  },

  grantPermission: (data) => {
    return api.post('/access/permission/grant', data);
  },

  revokePermission: (data) => {
    return api.post('/access/permission/revoke', data);
  }
};
```

### 3. 消费管理 (/api/v1/consume)

```javascript
// src/api/consume.js
export const consumeApi = {
  // 交易管理
  executeTransaction: (data) => {
    return api.post('/consume/transaction/execute', data, {
      encrypt: true // 交易数据加密
    });
  },

  queryTransactions: (params) => {
    return api.post('/consume/transaction/query', params);
  },

  getTransactionDetail: (id) => {
    return api.get(`/consume/transaction/${id}`);
  },

  // 账户管理
  queryAccounts: (params) => {
    return api.post('/consume/account/query', params);
  },

  getAccountBalance: (accountId) => {
    return api.get(`/consume/account/balance/${accountId}`);
  },

  // 退款管理
  createRefund: (data) => {
    return api.post('/consume/refund/create', data);
  },

  queryRefunds: (params) => {
    return api.post('/consume/refund/query', params);
  },

  // 统计报表
  getTransactionStatistics: (params) => {
    return api.get('/consume/statistics/transaction', { params });
  },

  getAccountStatistics: (params) => {
    return api.get('/consume/statistics/account', { params });
  }
};
```

### 4. 访客管理 (/api/v1/visitor)

```javascript
// src/api/visitor.js
export const visitorApi = {
  // 访客预约
  queryAppointments: (params) => {
    return api.post('/visitor/appointment/query', params);
  },

  createAppointment: (data) => {
    return api.post('/visitor/appointment/create', data);
  },

  updateAppointment: (id, data) => {
    return api.put(`/visitor/appointment/${id}`, data);
  },

  approveAppointment: (id, data) => {
    return api.post(`/visitor/appointment/${id}/approve`, data);
  },

  rejectAppointment: (id, data) => {
    return api.post(`/visitor/appointment/${id}/reject`, data);
  },

  // 访客登记
  queryRegistrations: (params) => {
    return api.post('/visitor/registration/query', params);
  },

  createRegistration: (data) => {
    return api.post('/visitor/registration/create', data);
  },

  // 访问记录
  queryVisitRecords: (params) => {
    return api.post('/visitor/record/query', params);
  }
};
```

### 5. 考勤管理 (/api/v1/attendance)

```javascript
// src/api/attendance.js
export const attendanceApi = {
  // 考勤记录
  queryRecords: (params) => {
    return api.post('/attendance/record/query', params);
  },

  getRecordDetail: (id) => {
    return api.get(`/attendance/record/${id}`);
  },

  // 排班管理
  querySchedules: (params) => {
    return api.post('/attendance/schedule/query', params);
  },

  createSchedule: (data) => {
    return api.post('/attendance/schedule/create', data);
  },

  updateSchedule: (id, data) => {
    return api.put(`/attendance/schedule/${id}`, data);
  },

  // 加班管理
  queryOvertimes: (params) => {
    return api.post('/attendance/overtime/query', params);
  },

  createOvertime: (data) => {
    return api.post('/attendance/overtime/create', data);
  },

  // 考勤统计
  getAttendanceStatistics: (params) => {
    return api.get('/attendance/statistics', { params });
  }
};
```

### 6. 系统管理 (/api/v1/system)

```javascript
// src/api/system.js
export const systemApi = {
  // 用户管理
  queryUsers: (params) => {
    return api.post('/system/user/query', params);
  },

  createUser: (data) => {
    return api.post('/system/user/create', data);
  },

  updateUser: (id, data) => {
    return api.put(`/system/user/${id}`, data);
  },

  deleteUser: (id) => {
    return api.delete(`/system/user/${id}`);
  },

  // 角色管理
  queryRoles: (params) => {
    return api.post('/system/role/query', params);
  },

  createRole: (data) => {
    return api.post('/system/role/create', data);
  },

  // 字典管理
  getDictTypes: () => {
    return api.get('/system/dict/types');
  },

  getDictData: (dictType) => {
    return api.get(`/system/dict/data/${dictType}`);
  },

  // 文件上传
  uploadFile: (file, type = 'common') => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);

    return api.post('/system/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
};
```

## 🔧 开发实现模板

### 1. Axios配置

```javascript
// src/lib/api.js
import axios from 'axios';
import { getToken, removeToken } from '@/lib/auth';
import { message, Modal } from 'ant-design-vue';
import router from '@/router';

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL + '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  }
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加token
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 添加请求ID
    config.headers['X-Request-ID'] = generateRequestId();

    // 添加时间戳
    config.headers['X-Timestamp'] = Date.now();

    // 数据加密
    if (config.encrypt) {
      config.data = encryptData(config.data);
      config.headers['X-Encrypted'] = 'true';
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 数据解密
    if (response.config.headers['x-encrypted'] === 'true' && res.encryptData) {
      res.data = decryptData(res.encryptData);
    }

    // 处理成功响应
    if (res.code === 200 || res.success === true) {
      return res;
    }

    // 处理业务错误
    handleBusinessError(res);

    return Promise.reject(new Error(res.message));
  },
  (error) => {
    // 网络错误处理
    handleNetworkError(error);

    return Promise.reject(error);
  }
);

// 业务错误处理
const handleBusinessError = (res) => {
  // Token过期处理
  if (res.code === 30007 || res.code === 30008 || res.code === 30012) {
    Modal.confirm({
      title: '登录过期',
      content: '您的登录已过期，请重新登录',
      okText: '重新登录',
      cancelText: '取消',
      onOk() {
        removeToken();
        router.push('/login');
      }
    });
    return;
  }

  // 等保安全提醒
  if (res.code === 30010 || res.code === 30011) {
    Modal.warning({
      title: '安全提醒',
      content: res.message,
      okText: '我知道了'
    });
    return;
  }

  // 其他业务错误
  message.error(res.message || '操作失败');
};

// 网络错误处理
const handleNetworkError = (error) => {
  console.error('请求错误:', error);

  let message = '网络错误，请稍后重试';

  if (error.code === 'ECONNABORTED') {
    message = '请求超时，请稍后重试';
  } else if (error.response) {
    const status = error.response.status;
    switch (status) {
      case 401:
        message = '未认证，请登录';
        break;
      case 403:
        message = '无权限访问';
        break;
      case 404:
        message = '请求的资源不存在';
        break;
      case 422:
        message = '请求参数错误';
        break;
      case 500:
        message = '服务器内部错误';
        break;
      case 502:
        message = '网关错误';
        break;
      case 503:
        message = '服务暂时不可用';
        break;
      default:
        message = `网络错误 (${status})`;
    }
  }

  message.error(message);
};

// 生成请求ID
const generateRequestId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2);
};

export default api;
```

### 2. API服务封装

```javascript
// src/api/base.js
import api from '@/lib/api';

export class BaseApiService {
  constructor(module) {
    this.module = module;
  }

  // 通用查询方法
  query(params) {
    return api.post(`/${this.module}/query`, params);
  }

  // 通用详情方法
  getDetail(id) {
    return api.get(`/${this.module}/${id}`);
  }

  // 通用创建方法
  create(data, options = {}) {
    return api.post(`/${this.module}/create`, data, options);
  }

  // 通用更新方法
  update(id, data, options = {}) {
    return api.put(`/${this.module}/${id}`, data, options);
  }

  // 通用删除方法
  delete(id) {
    return api.delete(`/${this.module}/${id}`);
  }

  // 通用批量删除方法
  batchDelete(ids) {
    return api.post(`/${this.module}/batch-delete`, { ids });
  }

  // 通用状态修改方法
  updateStatus(id, status) {
    return api.put(`/${this.module}/${id}/status`, { status });
  }
}
```

### 3. 具体API服务

```javascript
// src/api/user.js
import { BaseApiService } from './base';

class UserService extends BaseApiService {
  constructor() {
    super('system/user');
  }

  // 用户登录
  login(credentials) {
    return api.post('/auth/login', credentials, {
      encrypt: true
    });
  }

  // 获取用户信息
  getProfile() {
    return api.get('/system/user/profile');
  }

  // 修改密码
  changePassword(data) {
    return api.put('/system/user/password', data, {
      encrypt: true
    });
  }

  // 重置密码
  resetPassword(userId) {
    return api.post(`/system/user/${userId}/reset-password`);
  }

  // 分配角色
  assignRoles(userId, roleIds) {
    return api.post(`/system/user/${userId}/roles`, { roleIds });
  }

  // 导入用户
  importUsers(file) {
    const formData = new FormData();
    formData.append('file', file);

    return api.post('/system/user/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }

  // 导出用户
  exportUsers(params) {
    return api.post('/system/user/export', params, {
      responseType: 'blob'
    });
  }
}

export const userService = new UserService();
```

### 4. Vue组件中使用

```vue
<template>
  <div class="user-management">
    <!-- 搜索表单 -->
    <a-form
      :model="searchForm"
      layout="inline"
      @finish="handleSearch"
    >
      <a-form-item label="用户名">
        <a-input
          v-model:value="searchForm.username"
          placeholder="请输入用户名"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
          v-model:value="searchForm.status"
          placeholder="请选择状态"
          allow-clear
        >
          <a-select-option value="1">启用</a-select-option>
          <a-select-option value="0">禁用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">
            搜索
          </a-button>
          <a-button @click="handleReset">重置</a-button>
          <a-button type="primary" @click="showCreateModal">
            新增用户
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <!-- 数据表格 -->
    <a-table
      :columns="columns"
      :data-source="tableData"
      :pagination="pagination"
      :loading="loading"
      row-key="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-popconfirm
              title="确定要删除这个用户吗？"
              @confirm="handleDelete(record)"
            >
              <a-button type="link" size="small" danger>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item label="用户名" name="username">
          <a-input
            v-model:value="form.username"
            placeholder="请输入用户名"
          />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input
            v-model:value="form.email"
            placeholder="请输入邮箱"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { userService } from '@/api/user';

// 响应式数据
const searchForm = reactive({
  username: '',
  status: undefined
});

const tableData = ref([]);
const loading = ref(false);
const modalVisible = ref(false);
const modalLoading = ref(false);
const modalTitle = ref('');
const editId = ref(null);

const form = reactive({
  username: '',
  email: '',
  status: 1
});

const formRef = ref();

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条数据`
});

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username'
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email'
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
    width: 200
  }
];

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 生命周期
onMounted(() => {
  loadData();
});

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    };

    const response = await userService.query(params);
    if (response.code === 200) {
      tableData.value = response.data.list;
      pagination.total = response.data.total;
    }
  } catch (error) {
    console.error('加载数据失败:', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    status: undefined
  });
  handleSearch();
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadData();
};

const showCreateModal = () => {
  modalTitle.value = '新增用户';
  modalVisible.value = true;
  editId.value = null;
  resetForm();
};

const handleEdit = (record) => {
  modalTitle.value = '编辑用户';
  modalVisible.value = true;
  editId.value = record.id;
  Object.assign(form, {
    username: record.username,
    email: record.email,
    status: record.status
  });
};

const handleDelete = async (record) => {
  try {
    const response = await userService.delete(record.id);
    if (response.code === 200) {
      message.success('删除成功');
      loadData();
    }
  } catch (error) {
    console.error('删除失败:', error);
  }
};

const handleModalOk = async () => {
  try {
    await formRef.value.validate();
    modalLoading.value = true;

    let response;
    if (editId.value) {
      response = await userService.update(editId.value, form);
    } else {
      response = await userService.create(form);
    }

    if (response.code === 200) {
      message.success(editId.value ? '更新成功' : '创建成功');
      modalVisible.value = false;
      loadData();
    }
  } catch (error) {
    console.error('操作失败:', error);
  } finally {
    modalLoading.value = false;
  }
};

const handleModalCancel = () => {
  modalVisible.value = false;
  resetForm();
};

const resetForm = () => {
  Object.assign(form, {
    username: '',
    email: '',
    status: 1
  });
  if (formRef.value) {
    formRef.value.resetFields();
  }
};
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.ant-form {
  margin-bottom: 20px;
}
</style>
```

## 🔒 前端安全规范

### 1. 数据加密

```javascript
// src/lib/crypto.js
import CryptoJS from 'crypto-js';

const SECRET_KEY = process.env.VUE_APP_ENCRYPT_KEY;

// 加密
export const encryptData = (data) => {
  if (!data) return data;

  const text = typeof data === 'string' ? data : JSON.stringify(data);
  return CryptoJS.AES.encrypt(text, SECRET_KEY).toString();
};

// 解密
export const decryptData = (encryptedData) => {
  if (!encryptedData) return encryptedData;

  try {
    const bytes = CryptoJS.AES.decrypt(encryptedData, SECRET_KEY);
    const decryptedText = bytes.toString(CryptoJS.enc.Utf8);
    return JSON.parse(decryptedText);
  } catch (error) {
    console.error('解密失败:', error);
    return encryptedData;
  }
};
```

### 2. 权限控制

```javascript
// src/utils/permission.js
import { useUserStore } from '@/stores/user';

// 权限检查
export const hasPermission = (permission) => {
  const userStore = useUserStore();
  return userStore.permissions.includes(permission);
};

// 角色检查
export const hasRole = (role) => {
  const userStore = useUserStore();
  return userStore.roles.includes(role);
};

// 权限指令
export const permission = {
  mounted(el, binding) {
    const { value } = binding;
    if (!hasPermission(value)) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};

// 在main.js中注册指令
app.directive('permission', permission);
```

### 3. 安全审计

```javascript
// src/utils/audit.js
export const auditLog = {
  // 记录用户操作
  logUserAction: (action, data = {}) => {
    const userStore = useUserStore();
    const logData = {
      userId: userStore.userInfo.id,
      userName: userStore.userInfo.userName,
      action,
      data,
      timestamp: Date.now(),
      url: window.location.href,
      userAgent: navigator.userAgent
    };

    // 发送到后端
    api.post('/system/audit/log', logData).catch(error => {
      console.error('审计日志记录失败:', error);
    });
  },

  // 记录页面访问
  logPageView: (pageName) => {
    auditLog.logUserAction('PAGE_VIEW', { pageName });
  },

  // 记录API调用
  logApiCall: (method, url, params = {}) => {
    auditLog.logUserAction('API_CALL', {
      method,
      url,
      params: JSON.stringify(params)
    });
  }
};
```

## 📊 性能优化

### 1. 请求优化

```javascript
// src/utils/request-optimization.js
import { debounce, throttle } from 'lodash-es';

// 请求防抖
export const debouncedRequest = debounce((requestFunc, ...args) => {
  return requestFunc(...args);
}, 300);

// 请求节流
export const throttledRequest = throttle((requestFunc, ...args) => {
  return requestFunc(...args);
}, 1000);

// 请求缓存
const requestCache = new Map();

export const cachedRequest = (key, requestFunc, ttl = 60000) => {
  const cached = requestCache.get(key);
  if (cached && Date.now() - cached.timestamp < ttl) {
    return Promise.resolve(cached.data);
  }

  return requestFunc().then(data => {
    requestCache.set(key, {
      data,
      timestamp: Date.now()
    });
    return data;
  });
};
```

### 2. 数据处理优化

```javascript
// src/utils/data-optimization.js
// 虚拟滚动
export const virtualScroll = {
  data: [],
  itemHeight: 50,
  containerHeight: 500,
  scrollTop: 0,

  get visibleData() {
    const startIndex = Math.floor(this.scrollTop / this.itemHeight);
    const endIndex = Math.min(
      startIndex + Math.ceil(this.containerHeight / this.itemHeight) + 1,
      this.data.length
    );

    return this.data.slice(startIndex, endIndex);
  }
};

// 分页加载
export const paginationLoader = {
  hasMore: true,
  loading: false,
  page: 1,
  pageSize: 20,

  async loadMore(requestFunc, callback) {
    if (this.loading || !this.hasMore) return;

    this.loading = true;
    try {
      const response = await requestFunc(this.page, this.pageSize);

      if (response.data.list.length < this.pageSize) {
        this.hasMore = false;
      }

      callback(response.data.list);
      this.page++;
    } catch (error) {
      console.error('加载失败:', error);
    } finally {
      this.loading = false;
    }
  },

  reset() {
    this.hasMore = true;
    this.loading = false;
    this.page = 1;
  }
};
```

## 🧪 单元测试

### 1. API服务测试

```javascript
// tests/unit/user.service.test.js
import { userService } from '@/api/user';
import MockAdapter from 'axios-mock-adapter';

const mock = new MockAdapter(api);

describe('UserService', () => {
  beforeEach(() => {
    mock.reset();
  });

  test('query users successfully', async () => {
    const mockResponse = {
      code: 200,
      data: {
        list: [
          { id: 1, username: 'test1', email: 'test1@example.com' }
        ],
        total: 1
      }
    };

    mock.onPost('/system/user/query').reply(200, mockResponse);

    const result = await userService.query({ pageNum: 1, pageSize: 20 });

    expect(result.code).toBe(200);
    expect(result.data.list).toHaveLength(1);
    expect(result.data.list[0].username).toBe('test1');
  });

  test('create user successfully', async () => {
    const userData = {
      username: 'testuser',
      email: 'test@example.com'
    };

    const mockResponse = {
      code: 200,
      data: { id: 1, ...userData }
    };

    mock.onPost('/system/user/create').reply(200, mockResponse);

    const result = await userService.create(userData);

    expect(result.code).toBe(200);
    expect(result.data.id).toBe(1);
    expect(result.data.username).toBe('testuser');
  });
});
```

### 2. 组件测试

```javascript
// tests/unit/UserManagement.test.vue
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import UserManagement from '@/views/system/UserManagement.vue';

describe('UserManagement', () => {
  let pinia;

  beforeEach(() => {
    pinia = createPinia();
    setActivePinia(pinia);
  });

  test('renders user table', async () => {
    const wrapper = mount(UserManagement, {
      global: {
        plugins: [pinia]
      }
    });

    await wrapper.vm.loadData();

    expect(wrapper.find('.user-management').exists()).toBe(true);
    expect(wrapper.find('a-table').exists()).toBe(true);
  });

  test('shows create modal when button clicked', async () => {
    const wrapper = mount(UserManagement, {
      global: {
        plugins: [pinia]
      }
    });

    await wrapper.find('[data-test="create-button"]').trigger('click');

    expect(wrapper.vm.modalVisible).toBe(true);
    expect(wrapper.vm.modalTitle).toBe('新增用户');
  });
});
```

## 📝 检查清单

### 开发检查

- [ ] API路径符合规范
- [ ] 请求参数验证
- [ ] 错误处理完善
- [ ] 加载状态处理
- [ ] 权限控制实现
- [ ] 数据安全传输

### 测试检查

- [ ] 单元测试覆盖
- [ ] 组件测试通过
- [ ] API集成测试
- [ ] 性能测试达标
- [ ] 安全测试通过

### 代码质量

- [ ] TypeScript类型安全
- [ ] ESLint规范通过
- [ ] 代码可读性
- [ ] 组件可复用性
- [ ] 文档完整性

---

**维护团队**: IOE-DREAM前端开发团队
**更新频率**: 版本迭代时更新
**技术支持**: 项目Issue或开发团队