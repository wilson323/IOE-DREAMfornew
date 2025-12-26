<template>
  <div class="cross-day-shift-config">
    <!-- 页面头部 -->
    <a-card title="跨天班次配置" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-space>
            <a-button type="primary" @click="showCreateModal">
              <template #icon><PlusOutlined /></template>
              新建跨天班次
            </a-button>
            <a-button @click="batchImport">
              <template #icon><UploadOutlined /></template>
              批量导入
            </a-button>
          </a-space>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-space>
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索班次名称"
              style="width: 200px"
              @search="handleSearch"
            />
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="跨天班次总数"
            :value="statistics.totalCount"
            prefix=""
            :value-style="{ color: '#3f8600' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="启用班次"
            :value="statistics.enabledCount"
            prefix=""
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="夜班班次"
            :value="statistics.nightShiftCount"
            prefix=""
            :value-style="{ color: '#722ed1' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="平均工作时长"
            :value="statistics.avgWorkHours"
            suffix="小时"
            :precision="1"
            :value-style="{ color: '#fa8c16' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 班次列表 -->
    <a-card title="班次列表" :bordered="false" style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="shiftList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="shiftId"
      >
        <!-- 班次类型 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'shiftType'">
            <a-tag :color="getShiftTypeColor(record.shiftType)">
              {{ getShiftTypeLabel(record.shiftType) }}
            </a-tag>
          </template>

          <!-- 跨天标识 -->
          <template v-else-if="column.key === 'isCrossDay'">
            <a-tag v-if="record.isCrossDay" color="purple">
              <CheckOutlined />
              跨天班次
            </a-tag>
            <a-tag v-else color="default">
              当天班次
            </a-tag>
          </template>

          <!-- 工作时间 -->
          <template v-else-if="column.key === 'workTime'">
            <a-space direction="vertical" :size="0">
              <span>
                <ClockCircleOutlined style="margin-right: 4px" />
                {{ record.startTime }} - {{ record.endTime }}
              </span>
              <span v-if="record.isCrossDay" style="color: #722ed1; font-size: 12px">
                +{{ record.endDateOffset }}天
              </span>
            </a-space>
          </template>

          <!-- 工作时长 -->
          <template v-else-if="column.key === 'workHours'">
            <a-progress
              :percent="record.workHours * 10"
              :format="() => `${record.workHours}小时`"
              :stroke-color="getWorkHoursColor(record.workHours)"
            />
          </template>

          <!-- 状态 -->
          <template v-else-if="column.key === 'status'">
            <a-switch
              v-model:checked="record.enabled"
              checked-children="启用"
              un-checked-children="禁用"
              @change="handleStatusChange(record)"
            />
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="editShift(record)"
              >
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="viewDetail(record)"
              >
                详情
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="deleteShift(record.shiftId)"
              >
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 班次表单模态框 -->
    <a-modal
      v-model:visible="formModalVisible"
      :title="formModalTitle"
      width="900px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="shiftFormRef"
        :model="shiftForm"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-divider orientation="left">基本信息</a-divider>

        <a-form-item label="班次名称" name="shiftName">
          <a-input
            v-model:value="shiftForm.shiftName"
            placeholder="请输入班次名称"
            :max-length="100"
            show-count
          />
        </a-form-item>

        <a-form-item label="班次编码" name="shiftCode">
          <a-input
            v-model:value="shiftForm.shiftCode"
            placeholder="请输入班次编码（英文，如：NIGHT_SHIFT_A）"
            :max-length="50"
          />
        </a-form-item>

        <a-form-item label="班次类型" name="shiftType">
          <a-select
            v-model:value="shiftForm.shiftType"
            placeholder="请选择班次类型"
            @change="handleShiftTypeChange"
          >
            <a-select-option :value="1">固定班次</a-select-option>
            <a-select-option :value="2">弹性班次</a-select-option>
            <a-select-option :value="3">轮班班次</a-select-option>
          </a-select>
        </a-form-item>

        <a-divider orientation="left">时间配置</a-divider>

        <a-form-item label="开始时间" name="startTime">
          <a-time-picker
            v-model:value="shiftForm.startTime"
            placeholder="请选择开始时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="结束时间" name="endTime">
          <a-time-picker
            v-model:value="shiftForm.endTime"
            placeholder="请选择结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="是否跨天">
          <a-tooltip title="勾选此选项表示班次跨越午夜，需要设置结束日期偏移">
            <a-switch
              v-model:checked="shiftForm.isCrossDay"
              checked-children="是"
              un-checked-children="否"
              @change="handleCrossDayChange"
            />
          </a-tooltip>
        </a-form-item>

        <a-form-item v-if="shiftForm.isCrossDay" label="结束日期偏移" name="endDateOffset">
          <a-input-number
            v-model:value="shiftForm.endDateOffset"
            :min="1"
            :max="2"
            style="width: 200px"
          />
          <span style="margin-left: 8px">天（结束时间在后一天或后两天）</span>
          <div style="margin-top: 8px; color: #722ed1; font-size: 12px">
            <InfoCircleOutlined style="margin-right: 4px" />
            示例：开始时间22:00，结束时间06:00，偏移1天，表示当晚22:00到次日06:00
          </div>
        </a-form-item>

        <a-form-item label="工作时长" name="workHours">
          <a-input-number
            v-model:value="shiftForm.workHours"
            :min="0"
            :max="24"
            :precision="1"
            :step="0.5"
            style="width: 200px"
          />
          <span style="margin-left: 8px">小时</span>
          <a-button
            type="link"
            @click="calculateWorkHours"
            style="margin-left: 8px"
          >
            自动计算
          </a-button>
        </a-form-item>

        <a-divider orientation="left">休息配置</a-divider>

        <a-form-item label="休息时长">
          <a-input-number
            v-model:value="shiftForm.breakDuration"
            :min="0"
            :max="120"
            style="width: 200px"
          />
          <span style="margin-left: 8px">分钟（不计入工作时长）</span>
        </a-form-item>

        <a-form-item label="午休开始">
          <a-time-picker
            v-model:value="shiftForm.breakStartTime"
            placeholder="请选择午休开始时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="午休结束">
          <a-time-picker
            v-model:value="shiftForm.breakEndTime"
            placeholder="请选择午休结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </a-form-item>

        <a-divider orientation="left">其他配置</a-divider>

        <a-form-item label="弹性时间">
          <a-input-group compact>
            <a-input-number
              v-model:value="shiftForm.flexTimeBefore"
              :min="0"
              :max="60"
              placeholder="前"
              style="width: 45%"
            />
            <a-input
              style="width: 10%; pointer-events: none"
              placeholder="~"
              disabled
            />
            <a-input-number
              v-model:value="shiftForm.flexTimeAfter"
              :min="0"
              :max="60"
              placeholder="后"
              style="width: 45%"
            />
          </a-input-group>
          <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px">
            允许员工提前或推迟打卡的分钟数
          </div>
        </a-form-item>

        <a-form-item label="打卡次数">
          <a-radio-group v-model:value="shiftForm.punchCount">
            <a-radio :value="1">一日一打卡</a-radio>
            <a-radio :value="2">一日两打卡</a-radio>
            <a-radio :value="4">一日四打卡</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="排序号">
          <a-input-number
            v-model:value="shiftForm.sortOrder"
            :min="0"
            :max="999"
            style="width: 200px"
          />
          <span style="margin-left: 8px">数字越小越靠前</span>
        </a-form-item>

        <a-form-item label="备注">
          <a-textarea
            v-model:value="shiftForm.remark"
            placeholder="请输入备注信息"
            :rows="3"
            :max="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 班次详情模态框 -->
    <a-modal
      v-model:visible="detailModalVisible"
      title="班次详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions
        v-if="currentShift"
        title="基本信息"
        :column="2"
        bordered
      >
        <a-descriptions-item label="班次名称">
          {{ currentShift.shiftName }}
        </a-descriptions-item>
        <a-descriptions-item label="班次编码">
          {{ currentShift.shiftCode }}
        </a-descriptions-item>
        <a-descriptions-item label="班次类型">
          <a-tag :color="getShiftTypeColor(currentShift.shiftType)">
            {{ getShiftTypeLabel(currentShift.shiftType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="跨天标识">
          <a-tag v-if="currentShift.isCrossDay" color="purple">
            <CheckOutlined />
            跨天班次
          </a-tag>
          <a-tag v-else color="default">
            当天班次
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="开始时间">
          {{ currentShift.startTime }}
        </a-descriptions-item>
        <a-descriptions-item label="结束时间">
          {{ currentShift.endTime }}
          <span v-if="currentShift.isCrossDay" style="margin-left: 8px; color: #722ed1">
            (+{{ currentShift.endDateOffset }}天)
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="工作时长">
          {{ currentShift.workHours }}小时
        </a-descriptions-item>
        <a-descriptions-item label="休息时长">
          {{ currentShift.breakDuration || 0 }}分钟
        </a-descriptions-item>
        <a-descriptions-item label="弹性时间" :span="2">
          前{{ currentShift.flexTimeBefore || 0 }}分钟 ~ 后{{ currentShift.flexTimeAfter || 0 }}分钟
        </a-descriptions-item>
        <a-descriptions-item label="打卡次数" :span="2">
          一日{{ currentShift.punchCount }}打卡
        </a-descriptions-item>
        <a-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(currentShift.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">
          {{ currentShift.remark || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import {
  PlusOutlined,
  UploadOutlined,
  ReloadOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';

// 数据状态
const loading = ref(false);
const searchText = ref('');
const shiftList = ref([]);
const currentShift = ref(null);
const statistics = ref({
  totalCount: 0,
  enabledCount: 0,
  nightShiftCount: 0,
  avgWorkHours: 0
});

// 模态框状态
const formModalVisible = ref(false);
const detailModalVisible = ref(false);

// 表单引用
const shiftFormRef = ref();

// 编辑模式
const isEditMode = ref(false);
const editingShiftId = ref(null);

// 班次表单
const shiftForm = reactive({
  shiftName: '',
  shiftCode: '',
  shiftType: undefined,
  startTime: null,
  endTime: null,
  isCrossDay: false,
  endDateOffset: 1,
  workHours: 8,
  breakDuration: 60,
  breakStartTime: null,
  breakEndTime: null,
  flexTimeBefore: 30,
  flexTimeAfter: 30,
  punchCount: 2,
  sortOrder: 100,
  remark: ''
});

// 表单验证规则
const rules = {
  shiftName: [
    { required: true, message: '请输入班次名称', trigger: 'blur' },
    { min: 2, max: 100, message: '班次名称长度在2-100个字符', trigger: 'blur' }
  ],
  shiftCode: [
    { required: true, message: '请输入班次编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '班次编码只能包含大写字母、数字和下划线', trigger: 'blur' }
  ],
  shiftType: [
    { required: true, message: '请选择班次类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  endDateOffset: [
    { required: true, message: '请输入结束日期偏移', trigger: 'change' }
  ],
  workHours: [
    { required: true, message: '请输入工作时长', trigger: 'change' }
  ]
};

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 表格列定义
const columns = [
  {
    title: '班次名称',
    dataIndex: 'shiftName',
    key: 'shiftName',
    width: 150
  },
  {
    title: '班次编码',
    dataIndex: 'shiftCode',
    key: 'shiftCode',
    width: 150
  },
  {
    title: '班次类型',
    dataIndex: 'shiftType',
    key: 'shiftType',
    width: 120
  },
  {
    title: '跨天标识',
    dataIndex: 'isCrossDay',
    key: 'isCrossDay',
    width: 120
  },
  {
    title: '工作时间',
    key: 'workTime',
    width: 180
  },
  {
    title: '工作时长',
    key: 'workHours',
    width: 150
  },
  {
    title: '状态',
    key: 'status',
    width: 100
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    width: 80
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
];

// 模态框标题
const formModalTitle = computed(() => {
  return isEditMode.value ? '编辑跨天班次' : '新建跨天班次';
});

// 生命周期
onMounted(() => {
  fetchShiftList();
  fetchStatistics();
});

// 获取班次列表
const fetchShiftList = async () => {
  loading.value = true;
  try {
    // TODO: 调用实际API
    // const response = await attendanceApi.getCrossDayShiftList({
    //   pageNum: pagination.current,
    //   pageSize: pagination.pageSize,
    //   searchText: searchText.value
    // });

    // 模拟数据
    const mockData = {
      records: [
        {
          shiftId: '1',
          shiftName: '夜班A',
          shiftCode: 'NIGHT_SHIFT_A',
          shiftType: 1,
          startTime: '22:00',
          endTime: '06:00',
          isCrossDay: true,
          endDateOffset: 1,
          workHours: 8,
          breakDuration: 60,
          flexTimeBefore: 30,
          flexTimeAfter: 30,
          punchCount: 2,
          enabled: true,
          sortOrder: 1,
          createTime: '2025-12-26 10:00:00'
        },
        {
          shiftId: '2',
          shiftName: '夜班B',
          shiftCode: 'NIGHT_SHIFT_B',
          shiftType: 1,
          startTime: '23:00',
          endTime: '07:00',
          isCrossDay: true,
          endDateOffset: 1,
          workHours: 8,
          breakDuration: 60,
          flexTimeBefore: 15,
          flexTimeAfter: 15,
          punchCount: 2,
          enabled: true,
          sortOrder: 2,
          createTime: '2025-12-26 11:00:00'
        }
      ],
      total: 2
    };

    shiftList.value = mockData.records;
    pagination.total = mockData.total;
  } catch (error) {
    message.error('获取班次列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStatistics = async () => {
  try {
    // TODO: 调用实际API
    // const response = await attendanceApi.getCrossDayShiftStatistics();

    // 模拟数据
    statistics.value = {
      totalCount: 15,
      enabledCount: 12,
      nightShiftCount: 8,
      avgWorkHours: 8.5
    };
  } catch (error) {
    message.error('获取统计信息失败：' + error.message);
  }
};

// 显示创建模态框
const showCreateModal = () => {
  isEditMode.value = false;
  editingShiftId.value = null;
  resetForm();
  formModalVisible.value = true;
};

// 编辑班次
const editShift = (record) => {
  isEditMode.value = true;
  editingShiftId.value = record.shiftId;
  Object.assign(shiftForm, record);
  formModalVisible.value = true;
};

// 处理表单提交
const handleSubmit = async () => {
  try {
    await shiftFormRef.value.validate();

    // TODO: 调用实际API
    // if (isEditMode.value) {
    //   await attendanceApi.updateCrossDayShift(editingShiftId.value, shiftForm);
    // } else {
    //   await attendanceApi.createCrossDayShift(shiftForm);
    // }

    message.success(isEditMode.value ? '班次更新成功' : '班次创建成功');
    formModalVisible.value = false;
    resetForm();
    fetchShiftList();
    fetchStatistics();
  } catch (error) {
    if (error.errorFields) {
      message.error('请检查表单填写是否正确');
    } else {
      message.error('提交失败：' + error.message);
    }
  }
};

// 重置表单
const resetForm = () => {
  shiftFormRef.value?.resetFields();
  Object.assign(shiftForm, {
    shiftName: '',
    shiftCode: '',
    shiftType: undefined,
    startTime: null,
    endTime: null,
    isCrossDay: false,
    endDateOffset: 1,
    workHours: 8,
    breakDuration: 60,
    breakStartTime: null,
    breakEndTime: null,
    flexTimeBefore: 30,
    flexTimeAfter: 30,
    punchCount: 2,
    sortOrder: 100,
    remark: ''
  });
};

// 取消创建
const handleCancel = () => {
  formModalVisible.value = false;
  resetForm();
};

// 查看详情
const viewDetail = (record) => {
  currentShift.value = record;
  detailModalVisible.value = true;
};

// 删除班次
const deleteShift = async (shiftId) => {
  try {
    // TODO: 调用实际API
    // await attendanceApi.deleteCrossDayShift(shiftId);

    message.success('班次删除成功');
    fetchShiftList();
    fetchStatistics();
  } catch (error) {
    message.error('删除失败：' + error.message);
  }
};

// 状态变更
const handleStatusChange = async (record) => {
  try {
    // TODO: 调用实际API
    // await attendanceApi.updateCrossDayShiftStatus(record.shiftId, record.enabled);

    message.success('班次状态更新成功');
    fetchStatistics();
  } catch (error) {
    record.enabled = !record.enabled; // 回滚状态
    message.error('状态更新失败：' + error.message);
  }
};

// 跨天状态变更
const handleCrossDayChange = (checked) => {
  if (!checked) {
    shiftForm.endDateOffset = 0;
  } else {
    shiftForm.endDateOffset = 1;
  }
};

// 自动计算工作时长
const calculateWorkHours = () => {
  if (!shiftForm.startTime || !shiftForm.endTime) {
    message.warning('请先设置开始时间和结束时间');
    return;
  }

  const startParts = shiftForm.startTime.split(':');
  const endParts = shiftForm.endTime.split(':');

  let startMinutes = parseInt(startParts[0]) * 60 + parseInt(startParts[1]);
  let endMinutes = parseInt(endParts[0]) * 60 + parseInt(endParts[1]);

  // 如果跨天，加上24小时的分钟数
  if (shiftForm.isCrossDay) {
    endMinutes += shiftForm.endDateOffset * 24 * 60;
  }

  // 计算工作时长（分钟）
  let workMinutes = endMinutes - startMinutes;

  // 减去休息时间
  if (shiftForm.breakDuration) {
    workMinutes -= shiftForm.breakDuration;
  }

  // 转换为小时
  const workHours = (workMinutes / 60).toFixed(1);
  shiftForm.workHours = parseFloat(workHours);

  message.success(`工作时长已自动计算：${workHours}小时`);
};

// 班次类型变更
const handleShiftTypeChange = (value) => {
  console.log('班次类型变化:', value);
};

// 批量导入
const batchImport = () => {
  message.info('批量导入功能开发中...');
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  fetchShiftList();
};

// 刷新列表
const refreshList = () => {
  fetchShiftList();
  fetchStatistics();
};

// 表格变化
const handleTableChange = (pag, filters, sorter) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchShiftList();
};

// 工具方法
const getShiftTypeColor = (type) => {
  const colorMap = {
    1: 'blue',    // 固定班次
    2: 'green',   // 弹性班次
    3: 'orange'   // 轮班班次
  };
  return colorMap[type] || 'default';
};

const getShiftTypeLabel = (type) => {
  const labelMap = {
    1: '固定班次',
    2: '弹性班次',
    3: '轮班班次'
  };
  return labelMap[type] || '未知';
};

const getWorkHoursColor = (hours) => {
  if (hours < 4) return '#ff4d4f';
  if (hours < 8) return '#faad14';
  return '#52c41a';
};

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-';
  return dateTimeStr;
};
</script>

<style scoped lang="less">
.cross-day-shift-config {
  padding: 16px;

  :deep(.ant-card) {
    .ant-card-head {
      border-bottom: 1px solid #f0f0f0;
    }
  }

  :deep(.ant-statistic) {
    .ant-statistic-title {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }

    .ant-statistic-content {
      font-size: 24px;
      font-weight: 600;
    }
  }

  :deep(.ant-progress) {
    .ant-progress-text {
      font-size: 12px;
    }
  }
}
</style>
