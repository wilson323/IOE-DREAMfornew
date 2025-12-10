# IOE-DREAM 缺失前端组件创建指南

## 📋 概述

基于菜单初始化验证结果，以下是需要创建的缺失前端组件清单和创建指南。

## 🎯 需要创建的组件清单

### 1. 系统管理模块（已完整，无需创建）
- ✅ 用户管理 - `/system/account/index.vue`
- ✅ 角色管理 - `/system/role/index.vue`
- ✅ 菜单管理 - `/system/menu/index.vue`
- ✅ 部门管理 - `/system/department/index.vue`
- ✅ 字典管理 - `/system/dict/index.vue`
- ✅ 操作日志 - `/system/operate-log/index.vue`
- ✅ 登录日志 - `/system/login-log/index.vue`

### 2. 企业OA模块（已完整，无需创建）
- ✅ 企业管理 - `/business/oa/enterprise/enterprise-list.vue`
- ✅ 通知公告 - `/business/oa/notice/notice-list.vue`
- ✅ 工作流管理相关组件已完整

### 3. 门禁管理模块（已完整，无需创建）
- ✅ 门禁概览 - `/access/AccessDashboard.vue`
- ✅ 设备管理 - `/business/access/device/index.vue`
- ✅ 通行记录 - `/business/access/record/index.vue`
- ✅ 高级功能 - `/business/access/advanced/GlobalLinkageManagement.vue`

### 4. 考勤管理模块（需要创建以下组件）

#### 4.1 班次管理页面
```vue
<!-- 文件路径: smart-admin-web-javascript/src/views/business/attendance/shift/index.vue -->
<template>
  <div class="shift-management">
    <a-card title="班次管理" :bordered="false">
      <!-- 搜索区域 -->
      <a-row class="search-form" :gutter="16">
        <a-col :span="6">
          <a-input v-model:value="queryForm.shiftName" placeholder="班次名称" />
        </a-col>
        <a-col :span="6">
          <a-select v-model:value="queryForm.shiftType" placeholder="班次类型" allow-clear>
            <a-select-option value="1">固定班次</a-select-option>
            <a-select-option value="2">弹性班次</a-select-option>
            <a-select-option value="3">轮班班次</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-col>
      </a-row>

      <!-- 操作按钮区域 -->
      <div class="operation-area">
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增班次
        </a-button>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';

// 数据定义
const loading = ref(false);
const dataSource = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const queryForm = reactive({
  shiftName: '',
  shiftType: undefined,
});

// 表格列定义
const columns = [
  {
    title: '班次名称',
    dataIndex: 'shiftName',
    key: 'shiftName',
  },
  {
    title: '班次类型',
    dataIndex: 'shiftType',
    key: 'shiftType',
  },
  {
    title: '工作时间',
    dataIndex: 'workTime',
    key: 'workTime',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
];

// 方法定义
const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleReset = () => {
  Object.assign(queryForm, {
    shiftName: '',
    shiftType: undefined,
  });
  handleSearch();
};

const handleAdd = () => {
  // 新增班次逻辑
};

const handleEdit = (record) => {
  // 编辑班次逻辑
};

const handleDelete = (record) => {
  // 删除班次逻辑
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    // 调用API获取数据
    // const response = await shiftApi.list({...queryForm, ...pagination});
    // dataSource.value = response.data.list;
    // pagination.total = response.data.total;
  } catch (error) {
    console.error('获取班次数据失败:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.shift-management {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}

.operation-area {
  margin-bottom: 16px;
}
</style>
```

#### 4.2 排班管理页面
```vue
<!-- 文件路径: smart-admin-web-javascript/src/views/business/attendance/schedule/index.vue -->
<template>
  <div class="schedule-management">
    <a-card title="排班管理" :bordered="false">
      <!-- 排班日历区域 -->
      <div class="schedule-calendar">
        <a-calendar v-model:value="selectedDate" @panelChange="onPanelChange">
          <template #dateCellRender="{ current }">
            <div class="schedule-cell">
              <div v-for="schedule in getSchedulesByDate(current)" :key="schedule.id"
                   class="schedule-item" :class="'schedule-type-' + schedule.type">
                {{ schedule.shiftName }}
              </div>
            </div>
          </template>
        </a-calendar>
      </div>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import dayjs from 'dayjs';

const selectedDate = ref(dayjs());
const scheduleData = ref([]);

const getSchedulesByDate = (date) => {
  const dateStr = date.format('YYYY-MM-DD');
  return scheduleData.value.filter(item => item.date === dateStr);
};

const onPanelChange = (date) => {
  console.log('日历面板切换:', date);
  // 可以在这里加载对应月份的排班数据
};

onMounted(() => {
  // 加载排班数据
});
</script>

<style scoped>
.schedule-management {
  padding: 24px;
}

.schedule-cell {
  height: 80px;
  overflow-y: auto;
}

.schedule-item {
  font-size: 12px;
  padding: 2px 4px;
  margin: 1px 0;
  border-radius: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.schedule-type-1 {
  background-color: #e6f7ff;
  color: #1890ff;
}

.schedule-type-2 {
  background-color: #f6ffed;
  color: #52c41a;
}

.schedule-type-3 {
  background-color: #fff7e6;
  color: #fa8c16;
}
</style>
```

#### 4.3 考勤统计页面
```vue
<!-- 文件路径: smart-admin-web-javascript/src/views/business/attendance/statistics/index.vue -->
<template>
  <div class="attendance-statistics">
    <a-row :gutter="16">
      <!-- 统计卡片 -->
      <a-col :span="6" v-for="stat in statisticsCards" :key="stat.key">
        <a-card>
          <a-statistic
            :title="stat.title"
            :value="stat.value"
            :suffix="stat.suffix"
            :value-style="{ color: stat.color }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card title="考勤趋势图">
          <div ref="trendChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="部门考勤对比">
          <div ref="departmentChart" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';

const statisticsCards = ref([
  {
    key: 'total',
    title: '总人数',
    value: 1234,
    suffix: '人',
    color: '#3f8600',
  },
  {
    key: 'attendance',
    title: '出勤率',
    value: 95.8,
    suffix: '%',
    color: '#1890ff',
  },
  {
    key: 'late',
    title: '迟到人数',
    value: 23,
    suffix: '人',
    color: '#fa8c16',
  },
  {
    key: 'absence',
    title: '缺勤人数',
    value: 8,
    suffix: '人',
    color: '#f5222d',
  },
]);

const trendChart = ref(null);
const departmentChart = ref(null);

const initCharts = () => {
  // 初始化考勤趋势图
  if (trendChart.value) {
    const trendChartInstance = echarts.init(trendChart.value);
    const trendOption = {
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五'],
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '出勤人数',
          data: [1200, 1180, 1210, 1190, 1230],
          type: 'line',
        },
        {
          name: '迟到人数',
          data: [20, 25, 18, 30, 23],
          type: 'line',
        },
      ],
    };
    trendChartInstance.setOption(trendOption);
  }

  // 初始化部门考勤对比图
  if (departmentChart.value) {
    const departmentChartInstance = echarts.init(departmentChart.value);
    const departmentOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      xAxis: {
        type: 'category',
        data: ['技术部', '市场部', '人事部', '财务部'],
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '出勤人数',
          data: [300, 250, 150, 200],
          type: 'bar',
        },
      ],
    };
    departmentChartInstance.setOption(departmentOption);
  }
};

onMounted(() => {
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initCharts();
  }, 100);
});
</script>
```

#### 4.4 请假管理页面
```vue
<!-- 文件路径: smart-admin-web-javascript/src/views/business/attendance/leave/index.vue -->
<template>
  <div class="leave-management">
    <a-card title="请假管理" :bordered="false">
      <!-- 搜索和操作区域 -->
      <a-row class="search-form" :gutter="16">
        <a-col :span="6">
          <a-input v-model:value="queryForm.employeeName" placeholder="员工姓名" />
        </a-col>
        <a-col :span="6">
          <a-select v-model:value="queryForm.leaveType" placeholder="请假类型" allow-clear>
            <a-select-option value="1">事假</a-select-option>
            <a-select-option value="2">病假</a-select-option>
            <a-select-option value="3">年假</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-range-picker v-model:value="queryForm.dateRange" />
        </a-col>
        <a-col :span="6">
          <a-button type="primary" @click="handleSearch">查询</a-button>
        </a-col>
      </a-row>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleApprove(record)" v-if="record.status === 1">
                审批
              </a-button>
              <a-button type="link" @click="handleView(record)">查看</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';

const loading = ref(false);
const dataSource = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const queryForm = reactive({
  employeeName: '',
  leaveType: undefined,
  dateRange: null,
});

const columns = [
  {
    title: '员工姓名',
    dataIndex: 'employeeName',
    key: 'employeeName',
  },
  {
    title: '请假类型',
    dataIndex: 'leaveType',
    key: 'leaveType',
  },
  {
    title: '请假时间',
    dataIndex: 'leaveDate',
    key: 'leaveDate',
  },
  {
    title: '请假天数',
    dataIndex: 'leaveDays',
    key: 'leaveDays',
  },
  {
    title: '申请原因',
    dataIndex: 'reason',
    key: 'reason',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
];

const getStatusColor = (status) => {
  const colorMap = {
    1: 'processing', // 待审批
    2: 'success',    // 已通过
    3: 'error',      // 已拒绝
  };
  return colorMap[status] || 'default';
};

const getStatusText = (status) => {
  const textMap = {
    1: '待审批',
    2: '已通过',
    3: '已拒绝',
  };
  return textMap[status] || '未知';
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleApprove = (record) => {
  // 审批逻辑
};

const handleView = (record) => {
  // 查看详情逻辑
};

const fetchData = async () => {
  loading.value = true;
  try {
    // 调用API获取数据
  } catch (error) {
    console.error('获取请假数据失败:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>
```

### 5. 设备通讯管理模块（需要创建以下组件）

#### 5.1 连接管理页面
```vue
<!-- 文件路径: smart-admin-web-javascript/src/views/infrastructure/device-comm/connections.vue -->
<template>
  <div class="connections-management">
    <a-card title="设备连接管理" :bordered="false">
      <!-- 连接状态统计 -->
      <a-row :gutter="16" class="status-stats">
        <a-col :span="6" v-for="stat in connectionStats" :key="stat.type">
          <a-card :class="'status-card ' + stat.type">
            <a-statistic
              :title="stat.title"
              :value="stat.value"
              :value-style="{ color: stat.color }"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 连接列表 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge :status="getStatusBadge(record.status)" :text="getStatusText(record.status)" />
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleDisconnect(record)" v-if="record.status === 1">
                断开
              </a-button>
              <a-button type="link" @click="handleConnect(record)" v-else>
                连接
              </a-button>
              <a-button type="link" @click="handleView(record)">详情</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';

const loading = ref(false);
const dataSource = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
});

const connectionStats = ref([
  {
    type: 'online',
    title: '在线设备',
    value: 45,
    color: '#52c41a',
  },
  {
    type: 'offline',
    title: '离线设备',
    value: 12,
    color: '#f5222d',
  },
  {
    type: 'connecting',
    title: '连接中',
    value: 3,
    color: '#fa8c16',
  },
  {
    type: 'total',
    title: '设备总数',
    value: 60,
    color: '#1890ff',
  },
]);

const columns = [
  {
    title: '设备ID',
    dataIndex: 'deviceId',
    key: 'deviceId',
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
  },
  {
    title: '设备类型',
    dataIndex: 'deviceType',
    key: 'deviceType',
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
  },
  {
    title: '连接时间',
    dataIndex: 'connectTime',
    key: 'connectTime',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
];

const getStatusBadge = (status) => {
  const badgeMap = {
    1: 'success',    // 在线
    2: 'error',      // 离线
    3: 'processing', // 连接中
  };
  return badgeMap[status] || 'default';
};

const getStatusText = (status) => {
  const textMap = {
    1: '在线',
    2: '离线',
    3: '连接中',
  };
  return textMap[status] || '未知';
};

const handleConnect = (record) => {
  // 连接设备逻辑
};

const handleDisconnect = (record) => {
  // 断开设备逻辑
};

const handleView = (record) => {
  // 查看设备详情
};

const fetchData = async () => {
  loading.value = true;
  try {
    // 调用API获取设备连接数据
  } catch (error) {
    console.error('获取设备连接数据失败:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.connections-management {
  padding: 24px;
}

.status-stats {
  margin-bottom: 24px;
}

.status-card {
  text-align: center;
}

.status-card.online {
  border-left: 4px solid #52c41a;
}

.status-card.offline {
  border-left: 4px solid #f5222d;
}

.status-card.connecting {
  border-left: 4px solid #fa8c16;
}

.status-card.total {
  border-left: 4px solid #1890ff;
}
</style>
```

## 🎯 组件创建步骤

### 1. 创建目录结构
```bash
# 在 smart-admin-web-javascript/src/views/ 目录下创建以下目录结构
mkdir -p business/attendance/shift
mkdir -p business/attendance/schedule
mkdir -p business/attendance/statistics
mkdir -p business/attendance/leave
mkdir -p infrastructure/device-comm
```

### 2. 创建组件文件
将上述代码示例分别保存到对应的文件路径中。

### 3. 注册路由
确保在 Vue Router 配置中注册这些新路由：
```javascript
// 在 router/index.js 中添加
{
  path: '/business/attendance/shift',
  name: '班次管理对应的menu_id',
  component: () => import('@/views/business/attendance/shift/index.vue'),
},
{
  path: '/business/attendance/schedule',
  name: '排班管理对应的menu_id',
  component: () => import('@/views/business/attendance/schedule/index.vue'),
},
// ... 其他路由配置
```

### 4. 创建API接口
在 `src/api/business/` 目录下创建对应的API文件：
```javascript
// src/api/business/attendance-api.js
export const attendanceApi = {
  // 班次管理API
  shiftList: (params) => getRequest('/attendance/shift/list'),
  shiftAdd: (data) => postRequest('/attendance/shift/add', data),
  shiftUpdate: (data) => postRequest('/attendance/shift/update', data),
  shiftDelete: (id) => postRequest(`/attendance/shift/delete/${id}`),

  // 排班管理API
  scheduleList: (params) => getRequest('/attendance/schedule/list'),
  scheduleSave: (data) => postRequest('/attendance/schedule/save', data),

  // 考勤统计API
  statisticsData: (params) => getRequest('/attendance/statistics/data'),
  attendanceReport: (params) => getRequest('/attendance/statistics/report'),

  // 请假管理API
  leaveList: (params) => getRequest('/attendance/leave/list'),
  leaveApprove: (data) => postRequest('/attendance/leave/approve', data),
};
```

## ✅ 验证清单

创建完所有组件后，请验证：

- [ ] 所有组件文件已创建且路径正确
- [ ] 路由配置已更新
- [ ] API接口已创建
- [ ] 菜单可正常点击跳转
- [ ] 页面可正常渲染
- [ ] 数据加载正常
- [ ] 权限控制生效

## 🚀 部署建议

1. **测试环境验证**：先在测试环境验证所有组件功能
2. **权限测试**：测试不同角色的权限是否正确控制
3. **性能优化**：对大数据量页面进行分页和虚拟滚动优化
4. **用户体验**：添加加载状态、错误处理和友好提示