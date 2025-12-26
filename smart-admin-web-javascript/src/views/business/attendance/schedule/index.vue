<!--
  @fileoverview 排班管理主页面
  @author IOE-DREAM Team
  @description 支持日历视图、列表视图和模板管理切换，集成智能排班功能
-->
<template>
  <div class="attendance-schedule-page">
    <!-- 视图切换标签页 -->
    <a-card :bordered="false" class="view-switcher-card">
      <a-radio-group v-model:value="viewMode" button-style="solid" size="large">
        <a-radio-button value="CALENDAR">
          <CalendarOutlined />
          日历视图
        </a-radio-button>
        <a-radio-button value="LIST">
          <UnorderedListOutlined />
          列表视图
        </a-radio-button>
        <a-radio-button value="TEMPLATE">
          <FileTextOutlined />
          模板管理
        </a-radio-button>
      </a-radio-group>
    </a-card>

    <!-- 日历视图 -->
    <template v-if="viewMode === 'CALENDAR'">
      <CalendarView
        :plan-id="selectedPlanId"
        @date-change="handleDateChange"
        @record-click="handleRecordClick"
      />
    </template>

    <!-- 列表视图 -->
    <template v-else-if="viewMode === 'LIST'">
      <a-card size="small" :bordered="false" class="query-card">
        <a-form layout="inline" class="smart-query-form">
          <a-row :gutter="[16, 16]" class="smart-query-form-row">
            <a-form-item label="计划名称" class="smart-query-form-item">
              <a-input
                v-model:value="queryForm.keyword"
                placeholder="请输入计划名称"
                style="width: 220px"
                allow-clear
              />
            </a-form-item>

            <a-form-item label="状态" class="smart-query-form-item">
              <a-select
                v-model:value="queryForm.status"
                placeholder="请选择状态"
                style="width: 140px"
                :allow-clear="true"
              >
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item class="smart-query-form-item smart-margin-left10">
              <a-space>
                <a-button type="primary" @click="queryScheduleList">
                  <template #icon><SearchOutlined /></template>
                  查询
                </a-button>
                <a-button @click="resetQuery">
                  <template #icon><ReloadOutlined /></template>
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-row>
        </a-form>
      </a-card>

      <a-card :bordered="false" style="margin-top: 16px">
        <template #title>
          <a-space>
            <span>排班计划列表</span>
            <a-tag v-if="selectedRowKeys.length > 0" color="blue">
              已选 {{ selectedRowKeys.length }} 项
            </a-tag>
          </a-space>
        </template>

        <template #extra>
          <a-space>
            <a-button type="primary" @click="handleAdd">
              <template #icon><PlusOutlined /></template>
              新增计划
            </a-button>
            <a-button @click="handleBatchEnable" :disabled="selectedRowKeys.length === 0">
              <template #icon><CheckCircleOutlined /></template>
              批量启用
            </a-button>
            <a-button @click="handleBatchDisable" :disabled="selectedRowKeys.length === 0" danger>
              <template #icon><StopOutlined /></template>
              批量禁用
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
                :text="record.status === 1 ? '启用' : '禁用'"
              />
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="viewDetail(record)">查看</a-button>
                <a-button type="link" size="small" @click="editSchedule(record)">编辑</a-button>
                <a-button type="link" size="small" @click="deleteSchedule(record)" danger>删除</a-button>
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

    <!-- 模板管理视图 -->
    <template v-else-if="viewMode === 'TEMPLATE'">
      <TemplateManagement />
    </template>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  CheckCircleOutlined,
  StopOutlined,
  CalendarOutlined,
  UnorderedListOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { smartSentry } from '/@/lib/smart-sentry';
import CalendarView from './components/CalendarView.vue';
import TemplateManagement from './components/TemplateManagement.vue';
import type { ScheduleRecord } from '@/api/business/attendance/schedule';

// 视图模式
const viewMode = ref<'CALENDAR' | 'LIST' | 'TEMPLATE'>('CALENDAR');

// 选中的计划ID
const selectedPlanId = ref<number>(1);

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
  showTotal: (total) => `共 ${total} 条记录`,
});

const columns = [
  { title: '计划名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '计划编码', dataIndex: 'code', key: 'code', width: 120 },
  { title: '周期类型', dataIndex: 'cycle', key: 'cycle', width: 120 },
  { title: '工作日', dataIndex: 'workdays', key: 'workdays', width: 200 },
  { title: '时间段', dataIndex: 'timeRange', key: 'timeRange', width: 160 },
  { title: '状态', key: 'status', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 160 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
];

const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
};

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
  workdays: ['周一', '周二', '周三', '周四', '周五'],
  timeRange: ['09:00', '18:00'],
  status: 1,
});

const formRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入计划编码', trigger: 'blur' }],
  workdays: [{ required: true, message: '请选择工作日', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择时间段', trigger: 'change' }],
};

// 模拟数据
const mockData = () => {
  const now = dayjs();
  return Array.from({ length: 12 }).map((_, idx) => ({
    scheduleId: idx + 1,
    name: `标准排班 ${idx + 1}`,
    code: `SCH-${100 + idx}`,
    cycle: idx % 2 === 0 ? '每周' : '每月',
    workdays: ['周一', '周二', '周三', '周四', '周五'],
    timeRange: ['09:00', '18:00'],
    status: idx % 3 === 0 ? 0 : 1,
    updateTime: now.subtract(idx, 'day').format('YYYY-MM-DD'),
  }));
};

// 日期变化处理
const handleDateChange = (date: string) => {
  console.log('日期变化:', date);
};

// 记录点击处理
const handleRecordClick = (record: ScheduleRecord) => {
  console.log('点击记录:', record);
};

const queryScheduleList = async () => {
  loading.value = true;
  try {
    tableData.value = mockData();
    pagination.total = tableData.value.length;
  } catch (error) {
    smartSentry.captureError(error);
    message.error('加载排班计划失败');
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
    workdays: ['周一', '周二', '周三', '周四', '周五'],
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
    title: '删除排班计划',
    content: `确定要删除 ${record.name} 吗？`,
    onOk: () => {
      tableData.value = tableData.value.filter((item) => item.scheduleId !== record.scheduleId);
      message.success('删除成功');
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
      message.success('更新成功');
    } else {
      tableData.value.unshift({ ...formData, scheduleId: Date.now(), updateTime: dayjs().format('YYYY-MM-DD') });
      message.success('创建成功');
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
    message.warning('请至少选择一个计划');
    return;
  }
  tableData.value = tableData.value.map((item) =>
    selectedRowKeys.value.includes(item.scheduleId) ? { ...item, status: 1 } : item
  );
  message.success('批量启用成功');
};

const handleBatchDisable = () => {
  if (!selectedRowKeys.value.length) {
    message.warning('请至少选择一个计划');
    return;
  }
  tableData.value = tableData.value.map((item) =>
    selectedRowKeys.value.includes(item.scheduleId) ? { ...item, status: 0 } : item
  );
  message.success('批量禁用成功');
};

onMounted(() => {
  queryScheduleList();
});
</script>

<style lang="less" scoped>
.attendance-schedule-page {
  .view-switcher-card {
    margin-bottom: 16px;
    text-align: center;
    padding: 12px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;

    :deep(.ant-radio-group) {
      background: rgba(255, 255, 255, 0.95);
      padding: 4px;
      border-radius: 6px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }

    :deep(.ant-radio-button-wrapper) {
      border: none;
      background: transparent;
      transition: all 0.3s;

      &.ant-radio-button-wrapper-checked {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border-color: transparent;
      }
    }
  }

  .query-card {
    margin-bottom: 16px;
  }
}
</style>
