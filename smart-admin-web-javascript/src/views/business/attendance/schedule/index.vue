<!--
  * Attendance Schedule
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
-->
<template>
  <div class="attendance-schedule-page">
    <a-card size="small" :bordered="false" class="query-card">
      <a-form layout="inline" class="smart-query-form">
        <a-row :gutter="[16, 16]" class="smart-query-form-row">
          <a-form-item label="Keyword" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.keyword"
              placeholder="Schedule name / code"
              style="width: 220px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="Status" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              placeholder="Select status"
              style="width: 140px"
              :allow-clear="true"
            >
              <a-select-option :value="1">Enabled</a-select-option>
              <a-select-option :value="0">Disabled</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryScheduleList">
                <template #icon><SearchOutlined /></template>
                Search
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                Reset
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>Schedule List</span>
          <a-tag v-if="selectedRowKeys.length > 0" color="blue">
            Selected {{ selectedRowKeys.length }}
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            New Schedule
          </a-button>
          <a-button @click="handleBatchEnable" :disabled="selectedRowKeys.length === 0">
            <template #icon><CheckCircleOutlined /></template>
            Enable
          </a-button>
          <a-button @click="handleBatchDisable" :disabled="selectedRowKeys.length === 0" danger>
            <template #icon><StopOutlined /></template>
            Disable
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        row-key="scheduleId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? 'Enabled' : 'Disabled'"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewDetail(record)">View</a-button>
              <a-button type="link" size="small" @click="editSchedule(record)">Edit</a-button>
              <a-button type="link" size="small" @click="deleteSchedule(record)" danger>Delete</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formVisible"
      :title="isEdit ? 'Edit Schedule' : 'New Schedule'"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      destroy-on-close
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="Name" name="name">
          <a-input v-model:value="formData.name" placeholder="Enter name" />
        </a-form-item>
        <a-form-item label="Code" name="code">
          <a-input v-model:value="formData.code" placeholder="Enter code" />
        </a-form-item>
        <a-form-item label="Cycle" name="cycle">
          <a-select v-model:value="formData.cycle">
            <a-select-option value="WEEKLY">Weekly</a-select-option>
            <a-select-option value="MONTHLY">Monthly</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Working Days" name="workdays">
          <a-select v-model:value="formData.workdays" mode="multiple" :options="weekdayOptions" />
        </a-form-item>
        <a-form-item label="Shift Time" name="timeRange">
          <a-time-range-picker
            v-model:value="formData.timeRange"
            format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="Status" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">Enabled</a-radio>
            <a-radio :value="0">Disabled</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:open="detailVisible"
      title="Schedule Detail"
      width="420"
    >
      <a-descriptions :column="1" bordered size="small" v-if="currentSchedule">
        <a-descriptions-item label="Name">{{ currentSchedule.name }}</a-descriptions-item>
        <a-descriptions-item label="Code">{{ currentSchedule.code }}</a-descriptions-item>
        <a-descriptions-item label="Cycle">{{ currentSchedule.cycle }}</a-descriptions-item>
        <a-descriptions-item label="Working Days">{{ currentSchedule.workdays.join(', ') }}</a-descriptions-item>
        <a-descriptions-item label="Shift Time">{{ currentSchedule.timeRange.join(' - ') }}</a-descriptions-item>
        <a-descriptions-item label="Status">
          <a-badge :status="currentSchedule.status === 1 ? 'success' : 'error'" :text="currentSchedule.status === 1 ? 'Enabled' : 'Disabled'" />
        </a-descriptions-item>
        <a-descriptions-item label="Updated">{{ currentSchedule.updateTime }}</a-descriptions-item>
      </a-descriptions>
      <a-empty v-else />
    </a-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  CheckCircleOutlined,
  StopOutlined,
} from '@ant-design/icons-vue';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { smartSentry } from '/@/lib/smart-sentry';

const queryFormState = {
  pageNum: 1,
  pageSize: PAGE_SIZE,
  keyword: null,
  status: null,
};

const queryForm = reactive({ ...queryFormState });
const tableData = ref([]);
const loading = ref(false);
const selectedRowKeys = ref([]);

const pagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: PAGE_SIZE_OPTIONS,
  showTotal: (total) => `Total ${total} items`,
});

const columns = [
  { title: 'Name', dataIndex: 'name', key: 'name', width: 180 },
  { title: 'Code', dataIndex: 'code', key: 'code', width: 120 },
  { title: 'Cycle', dataIndex: 'cycle', key: 'cycle', width: 120 },
  { title: 'Working Days', dataIndex: 'workdays', key: 'workdays', width: 200 },
  { title: 'Time', dataIndex: 'timeRange', key: 'timeRange', width: 160 },
  { title: 'Status', key: 'status', width: 100 },
  { title: 'Updated At', dataIndex: 'updateTime', key: 'updateTime', width: 160 },
  { title: 'Action', key: 'action', width: 200, fixed: 'right' },
];

const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
};

const weekdayOptions = [
  { label: 'Mon', value: 'Mon' },
  { label: 'Tue', value: 'Tue' },
  { label: 'Wed', value: 'Wed' },
  { label: 'Thu', value: 'Thu' },
  { label: 'Fri', value: 'Fri' },
  { label: 'Sat', value: 'Sat' },
  { label: 'Sun', value: 'Sun' },
];

const formVisible = ref(false);
const formRef = ref();
const submitLoading = ref(false);
const isEdit = ref(false);
const currentSchedule = ref(null);
const detailVisible = ref(false);

const formData = reactive({
  scheduleId: null,
  name: '',
  code: '',
  cycle: 'WEEKLY',
  workdays: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
  timeRange: ['09:00', '18:00'],
  status: 1,
});

const formRules = {
  name: [{ required: true, message: 'Please input name', trigger: 'blur' }],
  code: [{ required: true, message: 'Please input code', trigger: 'blur' }],
  workdays: [{ required: true, message: 'Please select workdays', trigger: 'change' }],
  timeRange: [{ required: true, message: 'Please select time range', trigger: 'change' }],
};

const mockData = () => {
  const now = dayjs();
  return Array.from({ length: 12 }).map((_, idx) => ({
    scheduleId: idx + 1,
    name: `Standard Shift ${idx + 1}`,
    code: `SCH-${100 + idx}`,
    cycle: idx % 2 === 0 ? 'WEEKLY' : 'MONTHLY',
    workdays: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
    timeRange: ['09:00', '18:00'],
    status: idx % 3 === 0 ? 0 : 1,
    updateTime: now.subtract(idx, 'day').format('YYYY-MM-DD'),
  }));
};

const queryScheduleList = async () => {
  loading.value = true;
  try {
    tableData.value = mockData();
    pagination.total = tableData.value.length;
  } catch (error) {
    smartSentry.captureError(error);
    message.error('Failed to load schedules');
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  Object.assign(queryForm, queryFormState);
  selectedRowKeys.value = [];
  queryScheduleList();
};

const handleTableChange = (pag) => {
  queryForm.pageNum = pag.current;
  queryForm.pageSize = pag.pageSize;
  queryScheduleList();
};

const handleAdd = () => {
  isEdit.value = false;
  Object.assign(formData, {
    scheduleId: null,
    name: '',
    code: '',
    cycle: 'WEEKLY',
    workdays: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
    timeRange: ['09:00', '18:00'],
    status: 1,
  });
  formVisible.value = true;
};

const editSchedule = (record) => {
  isEdit.value = true;
  Object.assign(formData, { ...record });
  formVisible.value = true;
};

const deleteSchedule = (record) => {
  Modal.confirm({
    title: 'Delete schedule',
    content: `Delete ${record.name}?`,
    onOk: () => {
      tableData.value = tableData.value.filter((item) => item.scheduleId !== record.scheduleId);
      message.success('Deleted');
    },
  });
};

const handleSubmit = async () => {
  try {
    await formRef.value.validateFields();
    submitLoading.value = true;
    if (isEdit.value) {
      const idx = tableData.value.findIndex((item) => item.scheduleId === formData.scheduleId);
      if (idx !== -1) {
        tableData.value[idx] = { ...formData };
      }
      message.success('Updated');
    } else {
      tableData.value.unshift({ ...formData, scheduleId: Date.now(), updateTime: dayjs().format('YYYY-MM-DD') });
      message.success('Created');
    }
    formVisible.value = false;
  } catch (error) {
    // validation errors handled by form
  } finally {
    submitLoading.value = false;
  }
};

const handleCancel = () => {
  formVisible.value = false;
};

const viewDetail = (record) => {
  currentSchedule.value = record;
  detailVisible.value = true;
};

const handleBatchEnable = () => {
  if (!selectedRowKeys.value.length) {
    message.warning('Select at least one schedule');
    return;
  }
  tableData.value = tableData.value.map((item) =>
    selectedRowKeys.value.includes(item.scheduleId) ? { ...item, status: 1 } : item
  );
  message.success('Enabled');
};

const handleBatchDisable = () => {
  if (!selectedRowKeys.value.length) {
    message.warning('Select at least one schedule');
    return;
  }
  tableData.value = tableData.value.map((item) =>
    selectedRowKeys.value.includes(item.scheduleId) ? { ...item, status: 0 } : item
  );
  message.success('Disabled');
};

onMounted(() => {
  queryScheduleList();
});
</script>

<style lang="less" scoped>
.attendance-schedule-page {
  .query-card {
    margin-bottom: 16px;
  }
}
</style>
