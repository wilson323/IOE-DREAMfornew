<!--
 * 访客预约管理页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-appointment-page">
    <!-- 搜索表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="访客姓名">
          <a-input v-model:value="searchForm.visitorName" placeholder="请输入访客姓名" allow-clear />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="searchForm.phone" placeholder="请输入手机号" allow-clear />
        </a-form-item>
        <a-form-item label="预约状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option value="PENDING">待审批</a-select-option>
            <a-select-option value="APPROVED">已批准</a-select-option>
            <a-select-option value="REJECTED">已拒绝</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" @click="showAppointmentModal">
              <template #icon><PlusOutlined /></template>
              创建预约
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 预约列表 -->
    <a-card :bordered="false" class="table-card">
      <!-- 骨架屏 -->
      <SkeletonLoader v-if="loadingAppointments && appointmentList.length === 0" type="table" :rowCount="8" />

      <!-- 实际内容 -->
      <a-table
        v-else
        :columns="appointmentColumns"
        :data-source="appointmentList"
        :loading="loadingAppointments && appointmentList.length > 0"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="appointmentId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'appointmentTime'">
            {{ formatDateTime(record.appointmentTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewDetail(record)">
                详情
              </a-button>
              <a-button
                type="link"
                size="small"
                v-if="record.status === 'PENDING'"
                @click="editAppointment(record)"
              >
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                v-if="record.status === 'PENDING' || record.status === 'APPROVED'"
                @click="cancelAppointment(record)"
              >
                取消
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 预约表单弹窗 -->
    <AppointmentFormModal
      v-model:visible="appointmentModalVisible"
      :appointment-data="currentAppointment"
      @success="handleAppointmentSuccess"
    />

    <!-- 访客详情弹窗 -->
    <VisitorDetailModal
      v-model:visible="detailModalVisible"
      :appointment-id="currentAppointmentId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { SearchOutlined, PlusOutlined } from '@ant-design/icons-vue';
import { visitorApi } from '/@/api/business/visitor/visitor-api';
import AppointmentFormModal from './components/AppointmentFormModal.vue';
import VisitorDetailModal from './components/VisitorDetailModal.vue';
import SkeletonLoader from '/@/components/SkeletonLoader.vue';
import dayjs from 'dayjs';

// 搜索表单
const searchForm = reactive({
  visitorName: '',
  phone: '',
  status: undefined
});

// 预约列表
const appointmentList = ref([]);
const loadingAppointments = ref(false);
const currentAppointment = ref(null);
const currentAppointmentId = ref(null);
const appointmentModalVisible = ref(false);
const detailModalVisible = ref(false);

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 表格列配置
const appointmentColumns = [
  {
    title: '预约编号',
    dataIndex: 'appointmentCode',
    key: 'appointmentCode',
    width: 150
  },
  {
    title: '访客姓名',
    dataIndex: 'visitorName',
    key: 'visitorName',
    width: 120
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
    width: 130
  },
  {
    title: '被访人',
    dataIndex: 'visiteeName',
    key: 'visiteeName',
    width: 120
  },
  {
    title: '来访事由',
    dataIndex: 'purpose',
    key: 'purpose',
    ellipsis: true
  },
  {
    title: '预约时间',
    dataIndex: 'appointmentTime',
    key: 'appointmentTime',
    width: 170
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right'
  }
];

// 加载预约列表
const loadAppointments = async () => {
  loadingAppointments.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    };
    const result = await visitorApi.queryVisitors(params);
    if (result.code === 200 && result.data) {
      appointmentList.value = result.data.list || [];
      pagination.total = result.data.total || 0;
    }
  } catch (error) {
    message.error('加载预约列表失败');
    console.error('加载预约列表失败:', error);
  } finally {
    loadingAppointments.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadAppointments();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    visitorName: '',
    phone: '',
    status: undefined
  });
  pagination.current = 1;
  loadAppointments();
};

// 表格分页变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadAppointments();
};

// 显示预约表单
const showAppointmentModal = () => {
  currentAppointment.value = null;
  appointmentModalVisible.value = true;
};

// 编辑预约
const editAppointment = (record) => {
  currentAppointment.value = record;
  appointmentModalVisible.value = true;
};

// 查看详情
const viewDetail = (record) => {
  currentAppointmentId.value = record.appointmentId;
  detailModalVisible.value = true;
};

// 取消预约
const cancelAppointment = async (record) => {
  try {
    const userId = 1; // 从用户状态中获取
    const result = await visitorApi.cancelAppointment(record.appointmentId, userId);
    if (result.code === 200) {
      message.success('取消预约成功');
      loadAppointments();
    } else {
      message.error(result.message || '取消预约失败');
    }
  } catch (error) {
    message.error('取消预约失败');
    console.error('取消预约失败:', error);
  }
};

// 预约成功回调
const handleAppointmentSuccess = () => {
  loadAppointments();
};

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-';
  return dayjs(datetime).format('YYYY-MM-DD HH:mm');
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'orange',
    'APPROVED': 'green',
    'REJECTED': 'red',
    'CANCELLED': 'default',
    'CHECKED_IN': 'blue',
    'CHECKED_OUT': 'cyan'
  };
  return colorMap[status] || 'default';
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消',
    'CHECKED_IN': '已签到',
    'CHECKED_OUT': '已签退'
  };
  return textMap[status] || status;
};

// 初始化
onMounted(() => {
  loadAppointments();
});
</script>

<style lang="scss" scoped>
.visitor-appointment-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  :deep(.ant-table) {
    font-size: 14px;
  }
}
</style>

