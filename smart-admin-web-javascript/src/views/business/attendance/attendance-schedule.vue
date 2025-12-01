<!--
 * 考勤排班管理页面
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-17
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <div class="attendance-schedule-container">
    <!-- 查询表单 -->
    <a-form class="smart-query-form" v-privilege="'attendance:schedule:query'">
      <a-row class="smart-query-form-row">
        <a-form-item label="员工" class="smart-query-form-item">
          <a-select
            v-model:value="queryForm.employeeId"
            style="width: 200px"
            :showSearch="true"
            :allowClear="true"
            placeholder="选择员工"
            :filterOption="filterEmployeeOption"
          >
            <a-select-option v-for="item in employeeList" :key="item.employeeId" :value="item.employeeId">
              {{ item.employeeName }} - {{ item.departmentName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="部门" class="smart-query-form-item">
          <a-select
            v-model:value="queryForm.departmentId"
            style="width: 150px"
            :showSearch="true"
            :allowClear="true"
            placeholder="选择部门"
          >
            <a-select-option v-for="item in departmentList" :key="item.departmentId" :value="item.departmentId">
              {{ item.departmentName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="排班类型" class="smart-query-form-item">
          <a-select v-model:value="queryForm.scheduleType" style="width: 120px" :allowClear="true" placeholder="排班类型">
            <a-select-option value="FIXED">固定排班</a-select-option>
            <a-select-option value="FLEXIBLE">弹性排班</a-select-option>
            <a-select-option value="SHIFT">轮班制</a-select-option>
            <a-select-option value="REST">休息</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="排班日期" class="smart-query-form-item">
          <a-range-picker
            v-model:value="scheduleDate"
            :presets="defaultTimeRanges"
            @change="scheduleDateChange"
            style="width: 240px"
          />
        </a-form-item>

        <a-form-item class="smart-query-form-item smart-margin-left10">
          <a-button-group>
            <a-button type="primary" @click="onSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
            <a-button @click="onReload">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
          </a-button-group>
        </a-form-item>
      </a-row>
    </a-form>

    <!-- 操作按钮区域 -->
    <a-card size="small" :bordered="false">
      <a-row class="smart-table-btn-block">
        <div class="smart-table-operate-block">
          <a-button type="primary" @click="showBatchScheduleModal" v-privilege="'attendance:schedule:add'">
            <template #icon>
              <PlusOutlined />
            </template>
            批量排班
          </a-button>
          <a-button @click="showSingleScheduleModal" v-privilege="'attendance:schedule:add'">
            <template #icon>
              <CalendarOutlined />
            </template>
            单次排班
          </a-button>
          <a-button @click="copySchedule" v-privilege="'attendance:schedule:add'">
            <template #icon>
              <CopyOutlined />
            </template>
            复制排班
          </a-button>
          <a-button danger @click="batchDelete" v-privilege="'attendance:schedule:delete'">
            <template #icon>
              <DeleteOutlined />
            </template>
            批量删除
          </a-button>
          <a-button @click="showConflictModal" v-privilege="'attendance:schedule:query'">
            <template #icon>
              <ExclamationCircleOutlined />
            </template>
            冲突检测
          </a-button>
        </div>
        <div class="smart-table-setting-block">
          <TableOperator v-model="tableColumns" :tableId="TABLE_ID_CONST.BUSINESS.ATTENDANCE.SCHEDULE" :refresh="queryScheduleList" />
        </div>
      </a-row>

      <!-- 日历视图切换 -->
      <div class="schedule-view-switch">
        <a-radio-group v-model:value="viewType" @change="onViewTypeChange">
          <a-radio-button value="calendar">日历视图</a-radio-button>
          <a-radio-button value="table">表格视图</a-radio-button>
        </a-radio-group>
      </div>

      <!-- 日历视图 -->
      <div v-if="viewType === 'calendar'" class="schedule-calendar-view">
        <a-calendar
          v-model:value="calendarValue"
          :fullscreen="false"
          @select="onCalendarSelect"
          @panelChange="onPanelChange"
        >
          <template #dateCellRender="{ current }">
            <div class="schedule-date-cell">
              <div class="schedule-date-content">
                <div
                  v-for="schedule in getSchedulesByDate(current)"
                  :key="schedule.scheduleId"
                  class="schedule-item"
                  :class="getScheduleItemClass(schedule)"
                  @click="editSchedule(schedule)"
                >
                  <div class="schedule-employee">{{ schedule.employeeName }}</div>
                  <div class="schedule-type">{{ getScheduleTypeText(schedule.scheduleType) }}</div>
                  <div class="schedule-time">{{ schedule.workStartTime }}-{{ schedule.workEndTime }}</div>
                </div>
              </div>
            </div>
          </template>
        </a-calendar>
      </div>

      <!-- 表格视图 -->
      <div v-else class="schedule-table-view">
        <a-table
          rowKey="scheduleId"
          :columns="tableColumns"
          :dataSource="tableData"
          :rowSelection="rowSelection"
          :scroll="{ x: 1200 }"
          :pagination="false"
          :loading="tableLoading"
          size="small"
          bordered
        >
          <template #bodyCell="{ column, record, text }">
            <template v-if="column.dataIndex === 'employeeInfo'">
              <div>
                <div class="employee-name">{{ record.employeeName }}</div>
                <div class="employee-dept">{{ record.departmentName }}</div>
              </div>
            </template>
            <template v-if="column.dataIndex === 'scheduleDate'">
              <a-tag color="blue">{{ format_date(text) }}</a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'scheduleType'">
              <a-tag :color="getScheduleTypeColor(record.scheduleType)">
                {{ getScheduleTypeText(record.scheduleType) }}
              </a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'workTime'">
              <div>
                <div>上班: {{ record.workStartTime }}</div>
                <div>下班: {{ record.workEndTime }}</div>
                <div v-if="record.breakStartTime">休息: {{ record.breakStartTime }}-{{ record.breakEndTime }}</div>
              </div>
            </template>
            <template v-else-if="column.dataIndex === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'orange'">
                {{ record.status === 'ACTIVE' ? '生效中' : '已失效' }}
              </a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'action'">
              <div class="smart-table-operate">
                <a-button type="link" @click="editSchedule(record)" v-privilege="'attendance:schedule:update'">编辑</a-button>
                <a-button type="link" @click="deleteSchedule(record.scheduleId)" v-privilege="'attendance:schedule:delete'" danger>删除</a-button>
              </div>
            </template>
          </template>
        </a-table>

        <!-- 分页 -->
        <div class="smart-query-table-page">
          <a-pagination
            showSizeChanger
            showQuickJumper
            show-less-items
            :pageSizeOptions="PAGE_SIZE_OPTIONS"
            :defaultPageSize="queryForm.pageSize"
            v-model:current="queryForm.pageNum"
            v-model:pageSize="queryForm.pageSize"
            :total="total"
            @change="queryScheduleList"
            :show-total="(total) => `共${total}条`"
          />
        </div>
      </div>
    </a-card>

    <!-- 批量排班模态框 -->
    <a-modal
      v-model:open="showBatchModal"
      title="批量排班"
      :width="800"
      @ok="handleBatchSchedule"
      @cancel="resetBatchForm"
    >
      <a-form :model="batchForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="排班员工" required>
              <a-select
                v-model:value="batchForm.employeeIds"
                mode="multiple"
                placeholder="选择员工"
                :showSearch="true"
                :filterOption="filterEmployeeOption"
              >
                <a-select-option v-for="item in employeeList" :key="item.employeeId" :value="item.employeeId">
                  {{ item.employeeName }} - {{ item.departmentName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="排班类型" required>
              <a-select v-model:value="batchForm.scheduleType" placeholder="选择排班类型">
                <a-select-option value="FIXED">固定排班</a-select-option>
                <a-select-option value="FLEXIBLE">弹性排班</a-select-option>
                <a-select-option value="SHIFT">轮班制</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始日期" required>
              <a-date-picker v-model:value="batchForm.startDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期" required>
              <a-date-picker v-model:value="batchForm.endDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="工作日开始时间" required>
              <a-time-picker v-model:value="batchForm.workStartTime" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="工作日结束时间" required>
              <a-time-picker v-model:value="batchForm.workEndTime" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="休息开始时间">
              <a-time-picker v-model:value="batchForm.breakStartTime" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="休息结束时间">
              <a-time-picker v-model:value="batchForm.breakEndTime" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="适用工作日">
          <a-checkbox-group v-model:value="batchForm.weekdays">
            <a-checkbox value="1">周一</a-checkbox>
            <a-checkbox value="2">周二</a-checkbox>
            <a-checkbox value="3">周三</a-checkbox>
            <a-checkbox value="4">周四</a-checkbox>
            <a-checkbox value="5">周五</a-checkbox>
            <a-checkbox value="6">周六</a-checkbox>
            <a-checkbox value="7">周日</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <a-form-item label="备注">
          <a-textarea v-model:value="batchForm.remark" :rows="3" placeholder="请输入备注信息" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 单次排班模态框 -->
    <a-modal
      v-model:open="showSingleModal"
      title="单次排班"
      :width="600"
      @ok="handleSingleSchedule"
      @cancel="resetSingleForm"
    >
      <a-form :model="singleForm" layout="vertical">
        <a-form-item label="排班员工" required>
          <a-select
            v-model:value="singleForm.employeeId"
            placeholder="选择员工"
            :showSearch="true"
            :filterOption="filterEmployeeOption"
          >
            <a-select-option v-for="item in employeeList" :key="item.employeeId" :value="item.employeeId">
              {{ item.employeeName }} - {{ item.departmentName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="排班日期" required>
          <a-date-picker v-model:value="singleForm.scheduleDate" style="width: 100%" />
        </a-form-item>

        <a-form-item label="排班类型" required>
          <a-select v-model:value="singleForm.scheduleType" placeholder="选择排班类型">
            <a-select-option value="FIXED">固定排班</a-select-option>
            <a-select-option value="FLEXIBLE">弹性排班</a-select-option>
            <a-select-option value="SHIFT">轮班制</a-select-option>
            <a-select-option value="REST">休息</a-select-option>
          </a-select>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="工作日开始时间">
              <a-time-picker v-model:value="singleForm.workStartTime" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="工作日结束时间">
              <a-time-picker v-model:value="singleForm.workEndTime" format="HH:mm" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="备注">
          <a-textarea v-model:value="singleForm.remark" :rows="3" placeholder="请输入备注信息" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 复制排班模态框 -->
    <a-modal
      v-model:open="showCopyModal"
      title="复制排班"
      :width="600"
      @ok="handleCopySchedule"
      @cancel="resetCopyForm"
    >
      <a-form :model="copyForm" layout="vertical">
        <a-form-item label="源日期范围" required>
          <a-range-picker v-model:value="copyForm.sourceDateRange" style="width: 100%" />
        </a-form-item>

        <a-form-item label="目标日期范围" required>
          <a-range-picker v-model:value="copyForm.targetDateRange" style="width: 100%" />
        </a-form-item>

        <a-form-item label="复制员工">
          <a-select
            v-model:value="copyForm.employeeIds"
            mode="multiple"
            placeholder="选择员工（不选则复制所有员工）"
            :showSearch="true"
            :filterOption="filterEmployeeOption"
          >
            <a-select-option v-for="item in employeeList" :key="item.employeeId" :value="item.employeeId">
              {{ item.employeeName }} - {{ item.departmentName }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 排班详情抽屉 -->
    <ScheduleDetailDrawer
      ref="scheduleDetailDrawer"
      @edit="handleDetailEdit"
      @copy="handleDetailCopy"
      @delete="handleDetailDelete"
    />

    <!-- 冲突检测模态框 -->
    <ScheduleConflictModal ref="conflictModal" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  CalendarOutlined,
  CopyOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { attendanceApi } from '/@/api/business/attendance/attendance-api';
import { defaultTimeRanges } from '/@/lib/default-time-ranges';
import { smartSentry } from '/@/lib/smart-sentry';
import TableOperator from '/@/components/support/table-operator/index.vue';
import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
import { format_date } from '/@/utils/format';
import ScheduleDetailDrawer from './components/schedule-detail-drawer.vue';
import ScheduleConflictModal from './components/schedule-conflict-modal.vue';

// 响应式数据
const queryFormState = {
  employeeId: undefined,
  departmentId: undefined,
  scheduleType: undefined,
  scheduleDateBegin: null,
  scheduleDateEnd: null,
  pageNum: 1,
  pageSize: PAGE_SIZE,
};
const queryForm = reactive({ ...queryFormState });

const tableData = ref([]);
const total = ref(0);
const tableLoading = ref(false);
const selectedRowKeys = ref([]);
const viewType = ref('calendar');
const calendarValue = ref(dayjs());
const scheduleDate = ref([]);

// 员工和部门列表
const employeeList = ref([]);
const departmentList = ref([]);

// 模态框状态
const showBatchModal = ref(false);
const showSingleModal = ref(false);
const showCopyModal = ref(false);

// 批量排班表单
const batchFormState = {
  employeeIds: [],
  scheduleType: 'FIXED',
  startDate: null,
  endDate: null,
  workStartTime: null,
  workEndTime: null,
  breakStartTime: null,
  breakEndTime: null,
  weekdays: ['1', '2', '3', '4', '5'],
  remark: ''
};
const batchForm = reactive({ ...batchFormState });

// 单次排班表单
const singleFormState = {
  employeeId: undefined,
  scheduleDate: null,
  scheduleType: 'FIXED',
  workStartTime: null,
  workEndTime: null,
  remark: ''
};
const singleForm = reactive({ ...singleFormState });

// 复制排班表单
const copyFormState = {
  sourceDateRange: [],
  targetDateRange: [],
  employeeIds: []
};
const copyForm = reactive({ ...copyFormState });

// 表格列定义
const tableColumns = ref([
  {
    title: '员工信息',
    dataIndex: 'employeeInfo',
    width: 150,
    ellipsis: true,
  },
  {
    title: '排班日期',
    dataIndex: 'scheduleDate',
    width: 120,
  },
  {
    title: '排班类型',
    dataIndex: 'scheduleType',
    width: 100,
  },
  {
    title: '工作时间',
    dataIndex: 'workTime',
    width: 150,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    width: 150,
    ellipsis: true,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    dataIndex: 'action',
    fixed: 'right',
    width: 120,
  },
]);

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
}));

// ------------------ 数据初始化 ------------------

onMounted(() => {
  queryEmployeeList();
  queryDepartmentList();
  queryScheduleList();
});

// ------------------ 数据查询 ------------------

// 查询员工列表
async function queryEmployeeList() {
  try {
    const result = await attendanceApi.getEmployeeSchedule({ pageNum: 1, pageSize: 1000 });
    employeeList.value = result.data.list || [];
  } catch (err) {
    smartSentry.captureError(err);
  }
}

// 查询部门列表
async function queryDepartmentList() {
  try {
    // 这里应该调用部门查询API，暂时使用模拟数据
    departmentList.value = [
      { departmentId: 1, departmentName: '技术部' },
      { departmentId: 2, departmentName: '人事部' },
      { departmentId: 3, departmentName: '财务部' },
      { departmentId: 4, departmentName: '市场部' }
    ];
  } catch (err) {
    smartSentry.captureError(err);
  }
}

// 查询排班列表
async function queryScheduleList() {
  try {
    tableLoading.value = true;
    const result = await attendanceApi.getDepartmentSchedule(queryForm);
    tableData.value = result.data.list || [];
    total.value = result.data.total || 0;
  } catch (err) {
    smartSentry.captureError(err);
  } finally {
    tableLoading.value = false;
  }
}

// 查询日历数据
async function queryCalendarSchedule() {
  try {
    const params = {
      ...queryForm,
      pageNum: 1,
      pageSize: 1000,
      scheduleDateBegin: calendarValue.value.startOf('month').format('YYYY-MM-DD'),
      scheduleDateEnd: calendarValue.value.endOf('month').format('YYYY-MM-DD')
    };
    const result = await attendanceApi.getDepartmentSchedule(params);
    tableData.value = result.data.list || [];
  } catch (err) {
    smartSentry.captureError(err);
  }
}

// ------------------ 事件处理 ------------------

// 搜索
function onSearch() {
  queryForm.pageNum = 1;
  if (viewType.value === 'calendar') {
    queryCalendarSchedule();
  } else {
    queryScheduleList();
  }
}

// 重置
function onReload() {
  Object.assign(queryForm, queryFormState);
  scheduleDate.value = [];
  selectedRowKeys.value = [];
  queryScheduleList();
}

// 日期范围变化
function scheduleDateChange(dates, dateStrings) {
  queryForm.scheduleDateBegin = dateStrings[0];
  queryForm.scheduleDateEnd = dateStrings[1];
}

// 视图类型切换
function onViewTypeChange(e) {
  viewType.value = e.target.value;
  if (viewType.value === 'calendar') {
    queryCalendarSchedule();
  } else {
    queryScheduleList();
  }
}

// 日历选择
function onCalendarSelect(date) {
  calendarValue.value = date;
}

// 日历面板变化
function onPanelChange(date) {
  calendarValue.value = date;
  queryCalendarSchedule();
}

// 获取指定日期的排班
function getSchedulesByDate(date) {
  const dateStr = date.format('YYYY-MM-DD');
  return tableData.value.filter(schedule => schedule.scheduleDate === dateStr);
}

// 获取排班类型颜色
function getScheduleTypeColor(type) {
  const colorMap = {
    'FIXED': 'blue',
    'FLEXIBLE': 'green',
    'SHIFT': 'orange',
    'REST': 'gray'
  };
  return colorMap[type] || 'default';
}

// 获取排班类型文本
function getScheduleTypeText(type) {
  const textMap = {
    'FIXED': '固定排班',
    'FLEXIBLE': '弹性排班',
    'SHIFT': '轮班制',
    'REST': '休息'
  };
  return textMap[type] || type;
}

// 获取排班项样式类
function getScheduleItemClass(schedule) {
  return `schedule-${schedule.scheduleType.toLowerCase()}`;
}

// 员工过滤
function filterEmployeeOption(input, option) {
  return option.children.toLowerCase().includes(input.toLowerCase());
}

// ------------------ 批量排班 ------------------

function showBatchScheduleModal() {
  showBatchModal.value = true;
}

async function handleBatchSchedule() {
  try {
    // 表单验证
    if (!batchForm.employeeIds.length) {
      message.error('请选择排班员工');
      return;
    }
    if (!batchForm.startDate || !batchForm.endDate) {
      message.error('请选择排班日期范围');
      return;
    }

    const params = {
      employeeIds: batchForm.employeeIds,
      scheduleType: batchForm.scheduleType,
      startDate: batchForm.startDate.format('YYYY-MM-DD'),
      endDate: batchForm.endDate.format('YYYY-MM-DD'),
      workStartTime: batchForm.workStartTime?.format('HH:mm'),
      workEndTime: batchForm.workEndTime?.format('HH:mm'),
      breakStartTime: batchForm.breakStartTime?.format('HH:mm'),
      breakEndTime: batchForm.breakEndTime?.format('HH:mm'),
      weekdays: batchForm.weekdays,
      remark: batchForm.remark
    };

    await attendanceApi.saveOrUpdateSchedule(params);
    message.success('批量排班成功');
    showBatchModal.value = false;
    resetBatchForm();
    queryScheduleList();
  } catch (err) {
    smartSentry.captureError(err);
    message.error('批量排班失败');
  }
}

function resetBatchForm() {
  Object.assign(batchForm, batchFormState);
}

// ------------------ 单次排班 ------------------

function showSingleScheduleModal() {
  showSingleModal.value = true;
}

async function handleSingleSchedule() {
  try {
    if (!singleForm.employeeId) {
      message.error('请选择员工');
      return;
    }
    if (!singleForm.scheduleDate) {
      message.error('请选择排班日期');
      return;
    }

    const params = {
      employeeId: singleForm.employeeId,
      scheduleDate: singleForm.scheduleDate.format('YYYY-MM-DD'),
      scheduleType: singleForm.scheduleType,
      workStartTime: singleForm.workStartTime?.format('HH:mm'),
      workEndTime: singleForm.workEndTime?.format('HH:mm'),
      remark: singleForm.remark
    };

    await attendanceApi.saveOrUpdateSchedule(params);
    message.success('单次排班成功');
    showSingleModal.value = false;
    resetSingleForm();
    queryScheduleList();
  } catch (err) {
    smartSentry.captureError(err);
    message.error('单次排班失败');
  }
}

function resetSingleForm() {
  Object.assign(singleForm, singleFormState);
}

// ------------------ 复制排班 ------------------

function copySchedule() {
  showCopyModal.value = true;
}

async function handleCopySchedule() {
  try {
    if (!copyForm.sourceDateRange.length || !copyForm.targetDateRange.length) {
      message.error('请选择源日期范围和目标日期范围');
      return;
    }

    const params = {
      sourceStartDate: copyForm.sourceDateRange[0].format('YYYY-MM-DD'),
      sourceEndDate: copyForm.sourceDateRange[1].format('YYYY-MM-DD'),
      targetStartDate: copyForm.targetDateRange[0].format('YYYY-MM-DD'),
      targetEndDate: copyForm.targetDateRange[1].format('YYYY-MM-DD'),
      employeeIds: copyForm.employeeIds
    };

    await attendanceApi.copySchedule(params);
    message.success('复制排班成功');
    showCopyModal.value = false;
    resetCopyForm();
    queryScheduleList();
  } catch (err) {
    smartSentry.captureError(err);
    message.error('复制排班失败');
  }
}

function resetCopyForm() {
  Object.assign(copyForm, copyFormState);
}

// ------------------ 编辑排班 ------------------

function editSchedule(schedule) {
  // 填充表单数据
  Object.assign(singleForm, {
    scheduleId: schedule.scheduleId,
    employeeId: schedule.employeeId,
    scheduleDate: dayjs(schedule.scheduleDate),
    scheduleType: schedule.scheduleType,
    workStartTime: schedule.workStartTime ? dayjs(schedule.workStartTime, 'HH:mm') : null,
    workEndTime: schedule.workEndTime ? dayjs(schedule.workEndTime, 'HH:mm') : null,
    remark: schedule.remark || ''
  });
  showSingleModal.value = true;
}

// ------------------ 删除排班 ------------------

function deleteSchedule(scheduleId) {
  Modal.confirm({
    title: '提示',
    content: '确认删除此排班记录吗?',
    onOk() {
      doDeleteSchedule(scheduleId);
    },
  });
}

async function doDeleteSchedule(scheduleId) {
  try {
    await attendanceApi.deleteSchedule(scheduleId);
    message.success('删除成功');
    queryScheduleList();
  } catch (err) {
    smartSentry.captureError(err);
    message.error('删除失败');
  }
}

// 批量删除
function batchDelete() {
  if (!selectedRowKeys.value.length) {
    message.warning('请选择要删除的排班记录');
    return;
  }

  Modal.confirm({
    title: '提示',
    content: `确认删除选中的 ${selectedRowKeys.value.length} 条排班记录吗?`,
    onOk() {
      doBatchDelete();
    },
  });
}

async function doBatchDelete() {
  try {
    await attendanceApi.batchDeleteSchedule(selectedRowKeys.value);
    message.success('批量删除成功');
    selectedRowKeys.value = [];
    queryScheduleList();
  } catch (err) {
    smartSentry.captureError(err);
    message.error('批量删除失败');
  }
}

// ------------------ 排班详情相关 ------------------

// 显示详情
function showScheduleDetail(scheduleId) {
  scheduleDetailDrawer.value.showModal(scheduleId);
}

// 详情编辑处理
function handleDetailEdit(schedule) {
  // 填充表单数据到单次排班模态框
  Object.assign(singleForm, {
    scheduleId: schedule.scheduleId,
    employeeId: schedule.employeeId,
    scheduleDate: dayjs(schedule.scheduleDate),
    scheduleType: schedule.scheduleType,
    workStartTime: schedule.workStartTime ? dayjs(schedule.workStartTime, 'HH:mm') : null,
    workEndTime: schedule.workEndTime ? dayjs(schedule.workEndTime, 'HH:mm') : null,
    remark: schedule.remark || ''
  });
  showSingleModal.value = true;
}

// 详情复制处理
function handleDetailCopy(schedule) {
  // 重置表单，复制排班信息
  Object.assign(singleForm, {
    employeeId: schedule.employeeId,
    scheduleDate: dayjs(),
    scheduleType: schedule.scheduleType,
    workStartTime: schedule.workStartTime ? dayjs(schedule.workStartTime, 'HH:mm') : null,
    workEndTime: schedule.workEndTime ? dayjs(schedule.workEndTime, 'HH:mm') : null,
    remark: `复制自 ${format_date(schedule.scheduleDate)} 的排班`
  });
  showSingleModal.value = true;
}

// 详情删除处理
function handleDetailDelete(scheduleId) {
  deleteSchedule(scheduleId);
}

// ------------------ 冲突检测相关 ------------------

// 显示冲突检测模态框
function showConflictModal() {
  conflictModal.value.showModal();
}

// 显示排班详情
function editSchedule(schedule) {
  if (schedule.scheduleId) {
    scheduleDetailDrawer.value.showModal(schedule.scheduleId);
  } else {
    // 如果没有scheduleId，使用编辑功能
    handleDetailEdit(schedule);
  }
}
</script>

<style lang="less" scoped>
.attendance-schedule-container {
  padding: 16px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.schedule-view-switch {
  margin-bottom: 16px;
  text-align: center;
}

.schedule-calendar-view {
  background: white;
  border-radius: 8px;
  padding: 16px;

  :deep(.ant-picker-calendar-header) {
    padding: 8px 0;
  }

  :deep(.ant-picker-calendar-date-content) {
    min-height: 100px;
  }
}

.schedule-date-cell {
  height: 100%;
  padding: 4px;
}

.schedule-date-content {
  height: 100%;
  overflow-y: auto;
}

.schedule-item {
  background: #f0f2f5;
  border-radius: 4px;
  padding: 4px;
  margin-bottom: 4px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #e6f7ff;
    transform: translateY(-1px);
  }

  &.schedule-fixed {
    background: #e6f7ff;
    border-left: 3px solid #1890ff;
  }

  &.schedule-flexible {
    background: #f6ffed;
    border-left: 3px solid #52c41a;
  }

  &.schedule-shift {
    background: #fff7e6;
    border-left: 3px solid #fa8c16;
  }

  &.schedule-rest {
    background: #f9f0ff;
    border-left: 3px solid #722ed1;
  }
}

.schedule-employee {
  font-weight: 500;
  font-size: 12px;
  color: #262626;
}

.schedule-type {
  font-size: 10px;
  color: #8c8c8c;
}

.schedule-time {
  font-size: 10px;
  color: #595959;
}

.schedule-table-view {
  background: white;
  border-radius: 8px;
  padding: 16px;
}

.employee-name {
  font-weight: 500;
  color: #262626;
}

.employee-dept {
  font-size: 12px;
  color: #8c8c8c;
}

.smart-table-operate {
  display: flex;
  gap: 8px;
}
</style>