<!--
 * 访客记录查询页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-record-page">
    <!-- 搜索表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="访客姓名">
          <a-input v-model:value="searchForm.visitorName" placeholder="请输入访客姓名" allow-clear />
        </a-form-item>
        <a-form-item label="日期范围">
          <a-range-picker v-model:value="searchForm.dateRange" format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button @click="handleExport" :loading="exporting">
              <template #icon><ExportOutlined /></template>
              导出
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 记录列表 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="recordColumns"
        :data-source="recordList"
        :loading="loadingRecords"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="recordId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'checkInTime'">
            {{ formatDateTime(record.checkInTime) }}
          </template>
          <template v-if="column.key === 'checkOutTime'">
            {{ formatDateTime(record.checkOutTime) }}
          </template>
          <template v-if="column.key === 'duration'">
            {{ formatDuration(record.actualDuration) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewAccessRecords(record)">
                通行记录
              </a-button>
              <a-button type="link" size="small" @click="viewVisitorHistory(record)">
                历史记录
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 通行记录弹窗 -->
    <a-modal
      v-model:visible="accessRecordsModalVisible"
      title="通行记录"
      width="800px"
      :footer="null"
    >
      <a-table
        :columns="accessRecordColumns"
        :data-source="accessRecords"
        :loading="loadingAccessRecords"
        :pagination="false"
        size="small"
      />
    </a-modal>

    <!-- 历史记录弹窗 -->
    <a-modal
      v-model:visible="historyModalVisible"
      title="历史记录"
      width="800px"
      :footer="null"
    >
      <a-table
        :columns="historyColumns"
        :data-source="historyRecords"
        :loading="loadingHistory"
        :pagination="false"
        size="small"
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { SearchOutlined, ExportOutlined } from '@ant-design/icons-vue';
import { visitorApi } from '/@/api/business/visitor/visitor-api';
import dayjs from 'dayjs';

// 搜索表单
const searchForm = reactive({
  visitorName: '',
  dateRange: []
});

// 记录列表
const recordList = ref([]);
const loadingRecords = ref(false);
const exporting = ref(false);

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
const recordColumns = [
  { title: '访客姓名', dataIndex: 'visitorName', key: 'visitorName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '被访人', dataIndex: 'visiteeName', key: 'visiteeName', width: 120 },
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 170 },
  { title: '签退时间', dataIndex: 'checkOutTime', key: 'checkOutTime', width: 170 },
  { title: '停留时长', dataIndex: 'duration', key: 'duration', width: 100 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
];

// 通行记录
const accessRecordsModalVisible = ref(false);
const accessRecords = ref([]);
const loadingAccessRecords = ref(false);
const accessRecordColumns = [
  { title: '通行时间', dataIndex: 'accessTime', key: 'accessTime' },
  { title: '区域', dataIndex: 'areaName', key: 'areaName' },
  { title: '设备', dataIndex: 'deviceName', key: 'deviceName' },
  { title: '通行方向', dataIndex: 'direction', key: 'direction' }
];

// 历史记录
const historyModalVisible = ref(false);
const historyRecords = ref([]);
const loadingHistory = ref(false);
const historyColumns = [
  { title: '访问时间', dataIndex: 'visitTime', key: 'visitTime' },
  { title: '被访人', dataIndex: 'visiteeName', key: 'visiteeName' },
  { title: '来访事由', dataIndex: 'purpose', key: 'purpose' },
  { title: '停留时长', dataIndex: 'duration', key: 'duration' }
];

// 加载记录列表
const loadRecords = async () => {
  loadingRecords.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    };
    const result = await visitorApi.queryVisitors(params);
    if (result.code === 200 && result.data) {
      recordList.value = result.data.list || [];
      pagination.total = result.data.total || 0;
    }
  } catch (error) {
    message.error('加载记录列表失败');
    console.error('加载记录列表失败:', error);
  } finally {
    loadingRecords.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadRecords();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    visitorName: '',
    dateRange: []
  });
  pagination.current = 1;
  loadRecords();
};

// 导出
const handleExport = async () => {
  if (!searchForm.dateRange || searchForm.dateRange.length !== 2) {
    message.warning('请选择日期范围');
    return;
  }

  exporting.value = true;
  try {
    const startDate = dayjs(searchForm.dateRange[0]).format('YYYY-MM-DD');
    const endDate = dayjs(searchForm.dateRange[1]).format('YYYY-MM-DD');
    const result = await visitorApi.exportRecords(startDate, endDate);
    if (result.code === 200) {
      message.success('导出成功');
    } else {
      message.error(result.message || '导出失败');
    }
  } catch (error) {
    message.error('导出失败');
    console.error('导出失败:', error);
  } finally {
    exporting.value = false;
  }
};

// 表格分页变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadRecords();
};

// 验证二维码
const verifyQRCode = async () => {
  if (!qrCodeValue.value) {
    message.warning('请输入二维码内容');
    return;
  }

  verifying.value = true;
  try {
    const result = await visitorApi.checkInByQRCode(qrCodeValue.value);
    if (result.code === 200) {
      verifyResult.success = true;
      verifyResult.message = '验证成功';
      verifyResult.data = result.data;
      resultModalVisible.value = true;
      qrCodeValue.value = '';
    } else {
      verifyResult.success = false;
      verifyResult.message = result.message || '验证失败';
      verifyResult.data = null;
      resultModalVisible.value = true;
    }
  } catch (error) {
    message.error('验证失败');
    console.error('验证失败:', error);
  } finally {
    verifying.value = false;
  }
};

// 验证身份证
const verifyIdCard = async () => {
  if (!idCardForm.idCardNumber || !idCardForm.phoneNumber) {
    message.warning('请输入身份证号和手机号');
    return;
  }

  verifying.value = true;
  try {
    const result = await visitorApi.validateVisitorInfo(
      idCardForm.idCardNumber,
      idCardForm.phoneNumber
    );
    if (result.code === 200 && result.data) {
      verifyResult.success = result.data.isValid;
      verifyResult.message = result.data.isValid ? '验证成功' : '访客信息不存在';
      verifyResult.data = result.data;
      resultModalVisible.value = true;
      if (result.data.isValid) {
        // 重置表单
        idCardForm.idCardNumber = '';
        idCardForm.phoneNumber = '';
      }
    } else {
      message.error(result.message || '验证失败');
    }
  } catch (error) {
    message.error('验证失败');
    console.error('验证失败:', error);
  } finally {
    verifying.value = false;
  }
};

// 查看通行记录
const viewAccessRecords = async (record) => {
  loadingAccessRecords.value = true;
  accessRecordsModalVisible.value = true;
  try {
    const result = await visitorApi.getAccessRecords(record.appointmentId);
    if (result.code === 200 && result.data) {
      accessRecords.value = result.data;
    }
  } catch (error) {
    message.error('加载通行记录失败');
    console.error('加载通行记录失败:', error);
  } finally {
    loadingAccessRecords.value = false;
  }
};

// 查看历史记录
const viewVisitorHistory = async (record) => {
  loadingHistory.value = true;
  historyModalVisible.value = true;
  try {
    const result = await visitorApi.getVisitorHistory(record.visitorId, 10);
    if (result.code === 200 && result.data) {
      historyRecords.value = result.data;
    }
  } catch (error) {
    message.error('加载历史记录失败');
    console.error('加载历史记录失败:', error);
  } finally {
    loadingHistory.value = false;
  }
};

// 关闭结果弹窗
const closeResultModal = () => {
  resultModalVisible.value = false;
};

// 查看预约详情
const viewAppointmentDetail = () => {
  resultModalVisible.value = false;
};

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-';
  return dayjs(datetime).format('YYYY-MM-DD HH:mm:ss');
};

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes) return '-';
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}小时${mins}分钟`;
};

// 初始化
onMounted(() => {
  loadRecords();
});
</script>

<style lang="scss" scoped>
.visitor-record-page {
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

