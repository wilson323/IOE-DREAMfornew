<template>
  <div class="work-shift-manage">
    <!-- 班次列表 -->
    <a-card title="班次配置管理" :bordered="false">
      <!-- 工具栏 -->
      <template #extra>
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <template #icon><PlusOutlined /></template>
            新增班次
          </a-button>
        </a-space>
      </template>

      <!-- 搜索表单 -->
      <a-form layout="inline" style="margin-bottom: 16px">
        <a-form-item label="班次名称">
          <a-input
            v-model:value="searchForm.shiftName"
            placeholder="请输入班次名称"
            style="width: 200px"
            allowClear
          />
        </a-form-item>
        <a-form-item label="班次类型">
          <a-select
            v-model:value="searchForm.shiftType"
            placeholder="全部类型"
            style="width: 150px"
            allowClear
          >
            <a-select-option :value="1">固定班次</a-select-option>
            <a-select-option :value="2">弹性班次</a-select-option>
            <a-select-option :value="3">轮班班次</a-select-option>
            <a-select-option :value="4">临时班次</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="是否跨天">
          <a-select
            v-model:value="searchForm.isOvernight"
            placeholder="全部"
            style="width: 120px"
            allowClear
          >
            <a-select-option :value="true">跨天班次</a-select-option>
            <a-select-option :value="false">正常班次</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadWorkShiftList">
              查询
            </a-button>
            <a-button @click="resetSearch">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 班次表格 -->
      <a-table
        :columns="shiftColumns"
        :data-source="workShiftList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="shiftId"
        :scroll="{ x: 1800 }"
      >
        <!-- 自定义单元格渲染 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'shiftType'">
            <a-tag :color="getShiftTypeColor(record.shiftType)">
              {{ getShiftTypeName(record.shiftType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'isOvernight'">
            <a-tag v-if="record.isOvernight" color="orange">
              <ClockCircleOutlined style="margin-right: 4px" />
              跨天
            </a-tag>
            <a-tag v-else color="green">
              <CheckCircleOutlined style="margin-right: 4px" />
              正常
            </a-tag>
          </template>

          <template v-else-if="column.key === 'crossDayRule'">
            <a-tag v-if="record.crossDayRule" :color="getCrossDayRuleColor(record.crossDayRule)">
              {{ getCrossDayRuleName(record.crossDayRule) }}
            </a-tag>
            <span v-else style="color: #999">-</span>
          </template>

          <template v-else-if="column.key === 'workHours'">
            <span>{{ record.workHours }}小时</span>
          </template>

          <template v-else-if="column.key === 'timeRange'">
            <span>
              {{ record.startTime }} - {{ record.endTime }}
            </span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewWorkShift(record)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="editWorkShift(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="确定删除该班次吗？"
                @confirm="deleteWorkShift(record.shiftId)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑班次Modal -->
    <a-modal
      v-model:visible="shiftModalVisible"
      :title="isEditMode ? '编辑班次' : '新增班次'"
      width="800px"
      @ok="handleSaveShift"
      @cancel="handleCancelEdit"
      :confirm-loading="saving"
    >
      <a-form
        ref="shiftFormRef"
        :model="shiftForm"
        :rules="shiftFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="班次名称" name="shiftName">
              <a-input v-model:value="shiftForm.shiftName" placeholder="请输入班次名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="班次类型" name="shiftType">
              <a-select v-model:value="shiftForm.shiftType" placeholder="请选择班次类型">
                <a-select-option :value="1">固定班次</a-select-option>
                <a-select-option :value="2">弹性班次</a-select-option>
                <a-select-option :value="3">轮班班次</a-select-option>
                <a-select-option :value="4">临时班次</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="上班时间" name="startTime">
              <a-time-picker
                v-model:value="shiftForm.startTime"
                format="HH:mm"
                placeholder="请选择上班时间"
                style="width: 100%"
                @change="handleTimeChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="下班时间" name="endTime">
              <a-time-picker
                v-model:value="shiftForm.endTime"
                format="HH:mm"
                placeholder="请选择下班时间"
                style="width: 100%"
                @change="handleTimeChange"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 跨天检测提示 -->
        <a-alert
          v-if="isCrossDay"
          message="检测到跨天班次"
          type="warning"
          show-icon
          style="margin-bottom: 16px"
        >
          <template #description>
            该班次的下班时间早于上班时间，系统已自动标记为跨天班次。
            请选择跨天归属规则。
          </template>
        </a-alert>

        <a-form-item label="工作时长(小时)" name="workHours">
          <a-input-number
            v-model:value="shiftForm.workHours"
            :min="0"
            :max="24"
            :step="0.5"
            placeholder="请输入工作时长"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="休息时长(分钟)" name="breakMinutes">
          <a-input-number
            v-model:value="shiftForm.breakMinutes"
            :min="0"
            :max="240"
            :step="15"
            placeholder="请输入休息时长"
            style="width: 100%"
          />
        </a-form-item>

        <!-- 跨天规则选择 -->
        <a-form-item v-if="isCrossDay" label="跨天规则" name="crossDayRule">
          <a-select
            v-model:value="shiftForm.crossDayRule"
            placeholder="请选择跨天归属规则"
          >
            <a-select-option value="START_DATE">
              <div style="font-weight: 500">以开始日期为准（推荐）</div>
              <div style="font-size: 12px; color: #999">
                所有打卡记录归属到班次开始日期，适合夜班考勤统计
              </div>
            </a-select-option>
            <a-select-option value="END_DATE">
              <div style="font-weight: 500">以结束日期为准</div>
              <div style="font-size: 12px; color: #999">
                所有打卡记录归属到班次结束日期
              </div>
            </a-select-option>
            <a-select-option value="SPLIT">
              <div style="font-weight: 500">分别归属（不推荐）</div>
              <div style="font-size: 12px; color: #999">
                上班打卡归属开始日期，下班打卡归属结束日期
              </div>
            </a-select-option>
          </a-select>
          <div style="margin-top: 8px">
            <a-button type="link" size="small" @click="showCrossDayHelp">
              <QuestionCircleOutlined />
              什么是跨天规则？
            </a-button>
          </div>
        </a-form-item>

        <a-form-item label="排序号" name="sortOrder">
          <a-input-number
            v-model:value="shiftForm.sortOrder"
            :min="1"
            :max="999"
            placeholder="请输入排序号"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 跨天规则说明Modal -->
    <a-modal
      v-model:visible="helpVisible"
      title="跨天规则说明"
      :footer="null"
      width="700px"
    >
      <a-steps direction="vertical" :current="-1">
        <a-step title="以开始日期为准（START_DATE）- 推荐">
          <template #description>
            <div>
              <p><strong>适用场景</strong>: 夜班考勤，如22:00-06:00</p>
              <p><strong>规则说明</strong>: 所有打卡记录（包括第二天的下班打卡）都归属到班次开始日期</p>
              <p><strong>示例</strong>:</p>
              <ul>
                <li>班次：1月1日 22:00 - 1月2日 06:00</li>
                <li>上班打卡：1月1日 21:55 → 归属到1月1日</li>
                <li>下班打卡：1月2日 06:05 → 归属到1月1日</li>
              </ul>
              <a-tag color="green">适合夜班考勤统计</a-tag>
            </div>
          </template>
        </a-step>

        <a-step title="以结束日期为准（END_DATE）">
          <template #description>
            <div>
              <p><strong>适用场景</strong>: 需要将夜班归属到第二天</p>
              <p><strong>规则说明</strong>: 所有打卡记录都归属到班次结束日期</p>
              <p><strong>示例</strong>:</p>
              <ul>
                <li>班次：1月1日 22:00 - 1月2日 06:00</li>
                <li>上班打卡：1月1日 21:55 → 归属到1月2日</li>
                <li>下班打卡：1月2日 06:05 → 归属到1月2日</li>
              </ul>
            </div>
          </template>
        </a-step>

        <a-step title="分别归属（SPLIT）- 不推荐">
          <template #description>
            <div>
              <p><strong>适用场景</strong>: 特殊需求，分别统计上下班</p>
              <p><strong>规则说明</strong>: 上班打卡归属到开始日期，下班打卡归属到结束日期</p>
              <p><strong>示例</strong>:</p>
              <ul>
                <li>班次：1月1日 22:00 - 1月2日 06:00</li>
                <li>上班打卡：1月1日 21:55 → 归属到1月1日</li>
                <li>下班打卡：1月2日 06:05 → 归属到1月2日</li>
              </ul>
              <a-tag color="orange">可能导致考勤统计异常</a-tag>
            </div>
          </template>
        </a-step>
      </a-steps>

      <div style="text-align: center; margin-top: 24px">
        <a-button type="primary" @click="helpVisible = false">我知道了</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, ClockCircleOutlined, CheckCircleOutlined, QuestionCircleOutlined } from '@ant-design/icons-vue';
import dayjs, { Dayjs } from 'dayjs';
import { workShiftApi } from '@/api/business/attendance/workshift-api';

// ========== 数据定义 ==========
const loading = ref(false);
const saving = ref(false);
const workShiftList = ref([]);
const shiftModalVisible = ref(false);
const helpVisible = ref(false);
const isEditMode = ref(false);
const shiftFormRef = ref();

// 搜索表单
const searchForm = reactive({
  shiftName: '',
  shiftType: undefined,
  isOvernight: undefined
});

// 班次表单
const shiftForm = reactive({
  shiftId: null,
  shiftName: '',
  shiftType: 1,
  startTime: null,
  endTime: null,
  workHours: 8.0,
  breakMinutes: 0,
  isOvernight: false,
  crossDayRule: 'START_DATE',
  sortOrder: 1
});

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

// 表格列定义
const shiftColumns = [
  {
    title: '班次ID',
    key: 'shiftId',
    width: 100,
    fixed: 'left'
  },
  {
    title: '班次名称',
    key: 'shiftName',
    width: 150
  },
  {
    title: '班次类型',
    key: 'shiftType',
    width: 120,
    align: 'center'
  },
  {
    title: '工作时间',
    key: 'timeRange',
    width: 180,
    align: 'center'
  },
  {
    title: '工作时长',
    key: 'workHours',
    width: 120,
    align: 'center'
  },
  {
    title: '休息时长',
    key: 'breakMinutes',
    width: 120,
    align: 'center'
  },
  {
    title: '跨天标识',
    key: 'isOvernight',
    width: 120,
    align: 'center'
  },
  {
    title: '跨天规则',
    key: 'crossDayRule',
    width: 150,
    align: 'center'
  },
  {
    title: '排序号',
    key: 'sortOrder',
    width: 100,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    align: 'center'
  }
];

// 表单验证规则
const shiftFormRules = {
  shiftName: [
    { required: true, message: '请输入班次名称', trigger: 'blur' },
    { max: 100, message: '班次名称不能超过100个字符', trigger: 'blur' }
  ],
  shiftType: [
    { required: true, message: '请选择班次类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择上班时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择下班时间', trigger: 'change' },
    {
      validator: async (rule, value) => {
        if (!value || !shiftForm.startTime) {
          return Promise.resolve();
        }

        // 调用后端API验证跨天
        const startTimeStr = shiftForm.startTime.format('HH:mm');
        const endTimeStr = value.format('HH:mm');

        try {
          const response = await workShiftApi.checkCrossDay(startTimeStr, endTimeStr);

          if (response.data && !shiftForm.crossDayRule) {
            return Promise.reject('检测到跨天班次，请选择跨天规则');
          }

          return Promise.resolve();
        } catch (error) {
          return Promise.reject('跨天检测失败');
        }
      },
      trigger: 'change'
    }
  ],
  crossDayRule: [
    {
      validator: (rule, value) => {
        if (isCrossDay.value && !value) {
          return Promise.reject('跨天班次必须选择跨天规则');
        }
        return Promise.resolve();
      },
      trigger: 'change'
    }
  ],
  workHours: [
    { required: true, message: '请输入工作时长', trigger: 'blur' }
  ]
};

// ========== 计算属性 ==========
const isCrossDay = computed(() => {
  if (shiftForm.startTime && shiftForm.endTime) {
    return shiftForm.endTime.isBefore(shiftForm.startTime);
  }
  return false;
});

// ========== 方法 ==========
const loadWorkShiftList = async () => {
  loading.value = true;
  try {
    const response = await workShiftApi.getAllWorkShift();
    if (response.code === 200) {
      workShiftList.value = response.data || [];
      pagination.total = workShiftList.value.length;

      // 前端过滤（实际应由后端实现）
      let filteredList = workShiftList.value;

      if (searchForm.shiftName) {
        filteredList = filteredList.filter(item =>
          item.shiftName.includes(searchForm.shiftName)
        );
      }

      if (searchForm.shiftType !== undefined && searchForm.shiftType !== null) {
        filteredList = filteredList.filter(item =>
          item.shiftType === searchForm.shiftType
        );
      }

      if (searchForm.isOvernight !== undefined && searchForm.isOvernight !== null) {
        filteredList = filteredList.filter(item =>
          item.isOvernight === searchForm.isOvernight
        );
      }

      workShiftList.value = filteredList;
    }
  } catch (error) {
    message.error('加载班次列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  searchForm.shiftName = '';
  searchForm.shiftType = undefined;
  searchForm.isOvernight = undefined;
  loadWorkShiftList();
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadWorkShiftList();
};

const showAddModal = () => {
  isEditMode.value = false;
  resetShiftForm();
  shiftModalVisible.value = true;
};

const resetShiftForm = () => {
  Object.assign(shiftForm, {
    shiftId: null,
    shiftName: '',
    shiftType: 1,
    startTime: null,
    endTime: null,
    workHours: 8.0,
    breakMinutes: 0,
    isOvernight: false,
    crossDayRule: 'START_DATE',
    sortOrder: 1
  });
  shiftFormRef.value?.clearValidate();
};

const handleTimeChange = async () => {
  if (shiftForm.startTime && shiftForm.endTime) {
    try {
      const startTimeStr = shiftForm.startTime.format('HH:mm');
      const endTimeStr = shiftForm.endTime.format('HH:mm');

      const response = await workShiftApi.checkCrossDay(startTimeStr, endTimeStr);

      if (response.data) {
        shiftForm.isOvernight = true;
        if (!shiftForm.crossDayRule) {
          shiftForm.crossDayRule = 'START_DATE';
        }
        message.warning('检测到跨天班次，已自动设置跨天规则');
      } else {
        shiftForm.isOvernight = false;
        shiftForm.crossDayRule = null;
      }
    } catch (error) {
      console.error('跨天检测失败:', error);
    }
  }
};

const handleSaveShift = async () => {
  try {
    await shiftFormRef.value.validate();
    saving.value = true;

    const data = {
      ...shiftForm,
      startTime: shiftForm.startTime ? shiftForm.startTime.format('HH:mm') : null,
      endTime: shiftForm.endTime ? shiftForm.endTime.format('HH:mm') : null
    };

    if (isEditMode.value) {
      await workShiftApi.updateWorkShift(shiftForm.shiftId, data);
      message.success('更新班次成功');
    } else {
      await workShiftApi.createWorkShift(data);
      message.success('创建班次成功');
    }

    shiftModalVisible.value = false;
    loadWorkShiftList();
  } catch (error) {
    if (error.errorFields) {
      console.log('表单验证失败:', error);
    } else {
      message.error('保存班次失败：' + error.message);
    }
  } finally {
    saving.value = false;
  }
};

const handleCancelEdit = () => {
  shiftModalVisible.value = false;
  resetShiftForm();
};

const viewWorkShift = (record) => {
  // TODO: 实现查看详情功能
  message.info('查看班次详情功能待实现');
};

const editWorkShift = (record) => {
  isEditMode.value = true;
  Object.assign(shiftForm, {
    ...record,
    startTime: record.startTime ? dayjs(record.startTime, 'HH:mm') : null,
    endTime: record.endTime ? dayjs(record.endTime, 'HH:mm') : null
  });
  shiftModalVisible.value = true;
};

const deleteWorkShift = async (shiftId) => {
  try {
    await workShiftApi.deleteWorkShift(shiftId);
    message.success('删除班次成功');
    loadWorkShiftList();
  } catch (error) {
    message.error('删除班次失败：' + error.message);
  }
};

const showCrossDayHelp = () => {
  helpVisible.value = true;
};

// 辅助方法
const getShiftTypeColor = (type) => {
  const colors = {
    1: 'blue',
    2: 'orange',
    3: 'purple',
    4: 'green'
  };
  return colors[type] || 'default';
};

const getShiftTypeName = (type) => {
  const names = {
    1: '固定班次',
    2: '弹性班次',
    3: '轮班班次',
    4: '临时班次'
  };
  return names[type] || '未知';
};

const getCrossDayRuleColor = (rule) => {
  const colors = {
    'START_DATE': 'green',
    'END_DATE': 'blue',
    'SPLIT': 'orange'
  };
  return colors[rule] || 'default';
};

const getCrossDayRuleName = (rule) => {
  const names = {
    'START_DATE': '以开始日期为准',
    'END_DATE': '以结束日期为准',
    'SPLIT': '分别归属'
  };
  return names[rule] || rule;
};

// 监听跨天标识
watch(() => shiftForm.isOvernight, (newVal) => {
  if (newVal && !shiftForm.crossDayRule) {
    shiftForm.crossDayRule = 'START_DATE';
  }
});

// ========== 生命周期 ==========
onMounted(() => {
  loadWorkShiftList();
});
</script>

<style scoped>
.work-shift-manage {
  padding: 16px;
}
</style>
