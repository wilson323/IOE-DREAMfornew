<!--
  异常申请组件

  @Author:    Claude Code
  @Date:      2025-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="exception-application">
    <!-- 申请类型选择 -->
    <div class="application-types">
      <h4>选择申请类型</h4>
      <div class="type-grid">
        <div
          v-for="type in applicationTypes"
          :key="type.key"
          class="type-card"
          :class="{ active: selectedType === type.key }"
          @click="handleTypeSelect(type.key)"
        >
          <div class="type-icon">
            <component :is="type.icon" />
          </div>
          <div class="type-info">
            <h5>{{ type.name }}</h5>
            <p>{{ type.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的申请 -->
    <div class="my-applications">
      <div class="section-header">
        <h4>我的申请</h4>
        <div class="filters">
          <a-select
            v-model:value="filters.type"
            placeholder="申请类型"
            style="width: 120px; margin-right: 8px"
            @change="handleFilterChange"
          >
            <a-select-option value="">所有类型</a-select-option>
            <a-select-option value="leave">请假申请</a-select-option>
            <a-select-option value="overtime">加班申请</a-select-option>
            <a-select-option value="makeup">补签申请</a-select-option>
            <a-select-option value="shift">调班申请</a-select-option>
            <a-select-option value="cancel">销假申请</a-select-option>
          </a-select>
          <a-select
            v-model:value="filters.status"
            placeholder="申请状态"
            style="width: 120px; margin-right: 8px"
            @change="handleFilterChange"
          >
            <a-select-option value="">所有状态</a-select-option>
            <a-select-option value="pending">待审批</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="rejected">已拒绝</a-select-option>
            <a-select-option value="cancelled">已撤销</a-select-option>
          </a-select>
        </div>
      </div>

      <a-table
        :columns="columns"
        :data-source="applications"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <div class="type-cell">
              <component :is="getTypeIcon(record.type)" class="type-icon" />
              <span>{{ record.typeName }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'timeRange'">
            <span>{{ record.timeRange }}</span>
          </template>
          <template v-else-if="column.key === 'duration'">
            <span>{{ record.duration }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.statusName }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'currentApprover'">
            <span>{{ record.currentApprover }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="handleView(record)">查看</a-button>
              <a-button
                v-if="record.status === 'pending'"
                size="small"
                danger
                @click="handleCancel(record)"
              >
                撤销
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 右侧滑出面板 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="selectedTypeName + '申请'"
      width="600"
      placement="right"
    >
      <ApplicationForm
        :type="selectedType"
        @submit="handleSubmit"
        @cancel="drawerVisible = false"
      />
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  CalendarOutlined,
  ClockCircleOutlined,
  RedoOutlined,
  SwapOutlined,
  UndoOutlined,
} from '@ant-design/icons-vue';

import ApplicationForm from './ApplicationForm.vue';

const emit = defineEmits(['view-application']);

// 响应式数据
const loading = ref(false);
const drawerVisible = ref(false);
const selectedType = ref('');
const applications = ref([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

const filters = ref({
  type: '',
  status: '',
});

// 申请类型配置
const applicationTypes = [
  {
    key: 'leave',
    name: '请假申请',
    description: '年假、病假、事假等',
    icon: CalendarOutlined,
  },
  {
    key: 'overtime',
    name: '加班申请',
    description: '平时、周末、节假日',
    icon: ClockCircleOutlined,
  },
  {
    key: 'makeup',
    name: '补签申请',
    description: '签到、签退补签',
    icon: RedoOutlined,
  },
  {
    key: 'shift',
    name: '调班申请',
    description: '员工间调班、个人调班',
    icon: SwapOutlined,
  },
  {
    key: 'cancel',
    name: '销假申请',
    description: '提前销假、部分销假',
    icon: UndoOutlined,
  },
];

// 计算属性
const selectedTypeName = computed(() => {
  const type = applicationTypes.find(t => t.key === selectedType.value);
  return type ? type.name : '';
});

// 表格列定义
const columns = [
  {
    title: '申请类型',
    dataIndex: 'type',
    key: 'type',
    width: 150,
  },
  {
    title: '申请时间',
    dataIndex: 'applyTime',
    key: 'applyTime',
    width: 150,
  },
  {
    title: '时间范围',
    dataIndex: 'timeRange',
    key: 'timeRange',
    width: 200,
  },
  {
    title: '时长',
    dataIndex: 'duration',
    key: 'duration',
    width: 100,
  },
  {
    title: '审批状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '当前审批人',
    dataIndex: 'currentApprover',
    key: 'currentApprover',
    width: 120,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
  },
];

// 方法
const getTypeIcon = (type) => {
  const typeMap = {
    'leave': CalendarOutlined,
    'overtime': ClockCircleOutlined,
    'makeup': RedoOutlined,
    'shift': SwapOutlined,
    'cancel': UndoOutlined,
  };
  return typeMap[type] || CalendarOutlined;
};

const getStatusColor = (status) => {
  const colors = {
    'pending': 'orange',
    'approved': 'green',
    'rejected': 'red',
    'cancelled': 'default',
  };
  return colors[status] || 'default';
};

const handleTypeSelect = (type) => {
  selectedType.value = type;
  drawerVisible.value = true;
};

const handleFilterChange = () => {
  loadApplications();
};

const handleTableChange = (pag) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadApplications();
};

const handleView = (record) => {
  emit('view-application', record);
};

const handleCancel = (record) => {
  message.success(`申请 ${record.id} 已撤销`);
  loadApplications();
};

const handleSubmit = (formData) => {
  message.success('申请提交成功');
  drawerVisible.value = false;
  loadApplications();
};

// 加载申请列表
const loadApplications = async () => {
  loading.value = true;
  try {
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 300));

    // Mock 数据
    applications.value = [
      {
        id: 'APP001',
        type: 'leave',
        typeName: '请假申请',
        applyTime: '2024-01-15 09:30',
        timeRange: '2024-01-16 至 2024-01-18',
        duration: '3.0天',
        status: 'pending',
        statusName: '待审批',
        currentApprover: '张经理',
      },
      {
        id: 'APP002',
        type: 'overtime',
        typeName: '加班申请',
        applyTime: '2024-01-14 16:45',
        timeRange: '2024-01-13 19:00-22:00',
        duration: '3.0小时',
        status: 'approved',
        statusName: '已通过',
        currentApprover: '李主管',
      },
      {
        id: 'APP003',
        type: 'makeup',
        typeName: '补签申请',
        applyTime: '2024-01-13 10:20',
        timeRange: '2024-01-12 09:05',
        duration: '补签',
        status: 'rejected',
        statusName: '已拒绝',
        currentApprover: '王主任',
      },
    ];
    pagination.value.total = applications.value.length;
  } catch (error) {
    console.error('加载失败:', error);
    message.error('加载申请列表失败');
  } finally {
    loading.value = false;
  }
};

// 公共方法
const refresh = () => {
  loadApplications();
};

// 初始化
onMounted(() => {
  loadApplications();
});

defineExpose({
  refresh,
});
</script>

<style lang="less" scoped>
.exception-application {
  .application-types {
    margin-bottom: 24px;

    h4 {
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 600;
    }

    .type-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 16px;

      .type-card {
        border: 1px solid #d9d9d9;
        border-radius: 8px;
        padding: 16px;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;

        &:hover {
          border-color: #1890ff;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        &.active {
          border-color: #1890ff;
          background: #e6f7ff;
        }

        .type-icon {
          font-size: 32px;
          color: #666;
          margin-bottom: 8px;

          :deep(svg) {
            font-size: 32px;
          }
        }

        .type-info {
          h5 {
            margin: 0 0 4px;
            font-size: 14px;
            font-weight: 600;
          }

          p {
            margin: 0;
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
  }

  .my-applications {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }

      .filters {
        display: flex;
      }
    }

    .type-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .type-icon {
        font-size: 18px;
        color: #666;
      }
    }
  }
}
</style>
