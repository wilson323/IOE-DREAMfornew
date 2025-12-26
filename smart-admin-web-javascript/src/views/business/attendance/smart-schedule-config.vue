<template>
  <div class="smart-schedule-config">
    <!-- 页面头部 -->
    <a-card title="智能排班配置" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-button type="primary" @click="showCreateModal">
            <template #icon><PlusOutlined /></template>
            创建排班计划
          </a-button>
        </a-col>
        <a-col :span="12" style="text-align: right">
          <a-space>
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- 排班计划列表 -->
    <a-card title="排班计划列表" :bordered="false" style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="planList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="planId"
      >
        <!-- 计划名称 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'planName'">
            <a @click="viewPlanDetail(record.planId)">{{ record.planName }}</a>
          </template>

          <!-- 优化目标 -->
          <template v-else-if="column.key === 'optimizationGoal'">
            <a-tag :color="getGoalColor(record.optimizationGoal)">
              {{ getGoalLabel(record.optimizationGoal) }}
            </a-tag>
          </template>

          <!-- 执行状态 -->
          <template v-else-if="column.key === 'executionStatus'">
            <a-tag :color="getStatusColor(record.executionStatus)">
              {{ getStatusLabel(record.executionStatus) }}
            </a-tag>
          </template>

          <!-- 适应度 -->
          <template v-else-if="column.key === 'fitnessScore'">
            <a-progress
              :percent="record.fitnessScore ? (record.fitnessScore * 100).toFixed(2) : 0"
              :status="getFitnessStatus(record.fitnessScore)"
              size="small"
            />
          </template>

          <!-- 时间范围 -->
          <template v-else-if="column.key === 'dateRange'">
            {{ record.startDate }} 至 {{ record.endDate }}
          </template>

          <!-- 操作按钮 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.executionStatus === 0"
                type="link"
                size="small"
                @click="executeOptimization(record.planId)"
              >
                执行优化
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="viewResults(record.planId)"
              >
                查看结果
              </a-button>
              <a-button
                v-if="record.executionStatus === 2"
                type="link"
                size="small"
                @click="confirmPlan(record.planId)"
              >
                确认
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="exportResult(record.planId)">
                      <ExportOutlined /> 导出结果
                    </a-menu-item>
                    <a-menu-item @click="editPlan(record.planId)">
                      <EditOutlined /> 编辑计划
                    </a-menu-item>
                    <a-menu-item @click="deletePlan(record.planId)">
                      <DeleteOutlined /> 删除计划
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑排班计划弹窗 -->
    <a-modal
      v-model:visible="createModalVisible"
      :title="isEditMode ? '编辑排班计划' : '创建排班计划'"
      :width="1000"
      @ok="handleCreateOk"
      @cancel="createModalVisible = false"
    >
      <a-form
        ref="planFormRef"
        :model="planForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <!-- 基本信息 -->
        <a-divider orientation="left">基本信息</a-divider>

        <a-form-item
          label="计划名称"
          name="planName"
          :rules="[{ required: true, message: '请输入计划名称' }]"
        >
          <a-input v-model:value="planForm.planName" placeholder="请输入计划名称" />
        </a-form-item>

        <a-form-item
          label="计划描述"
          name="planDescription"
        >
          <a-textarea
            v-model:value="planForm.planDescription"
            placeholder="请输入计划描述"
            :rows="3"
          />
        </a-form-item>

        <a-form-item
          label="排班周期"
          name="dateRange"
          :rules="[{ required: true, message: '请选择排班周期' }]"
        >
          <a-range-picker
            v-model:value="planForm.dateRange"
            style="width: 100%"
          />
        </a-form-item>

        <!-- 员工和班次选择 -->
        <a-divider orientation="left">员工和班次</a-divider>

        <a-form-item
          label="选择员工"
          name="employeeIds"
          :rules="[{ required: true, message: '请选择员工' }]"
        >
          <a-select
            v-model:value="planForm.employeeIds"
            mode="multiple"
            placeholder="请选择员工"
            :options="employeeOptions"
            :filter-option="filterOption"
            show-search
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item
          label="选择班次"
          name="shiftIds"
          :rules="[{ required: true, message: '请选择班次' }]"
        >
          <a-select
            v-model:value="planForm.shiftIds"
            mode="multiple"
            placeholder="请选择班次"
            :options="shiftOptions"
            style="width: 100%"
          />
        </a-form-item>

        <!-- 优化目标 -->
        <a-divider orientation="left">优化目标</a-divider>

        <a-form-item
          label="优化目标"
          name="optimizationGoal"
          :rules="[{ required: true, message: '请选择优化目标' }]"
        >
          <a-select v-model:value="planForm.optimizationGoal" placeholder="请选择优化目标">
            <a-select-option :value="1">公平性优先</a-select-option>
            <a-select-option :value="2">成本优先</a-select-option>
            <a-select-option :value="3">效率优先</a-select-option>
            <a-select-option :value="4">满意度优先</a-select-option>
            <a-select-option :value="5">综合优化</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 约束条件 -->
        <a-divider orientation="left">约束条件</a-divider>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="最大连续工作天数" name="maxConsecutiveWorkDays">
              <a-input-number
                v-model:value="planForm.maxConsecutiveWorkDays"
                :min="1"
                :max="15"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最小休息天数" name="minRestDays">
              <a-input-number
                v-model:value="planForm.minRestDays"
                :min="1"
                :max="7"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="每日最少在岗人数" name="minDailyStaff">
              <a-input-number
                v-model:value="planForm.minDailyStaff"
                :min="1"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="每月最多工作天数" name="maxMonthlyWorkDays">
              <a-input-number
                v-model:value="planForm.maxMonthlyWorkDays"
                :min="1"
                :max="31"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 算法参数 -->
        <a-divider orientation="left">算法参数</a-divider>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="种群大小" name="populationSize">
              <a-input-number
                v-model:value="planForm.populationSize"
                :min="10"
                :max="500"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="最大迭代次数" name="maxIterations">
              <a-input-number
                v-model:value="planForm.maxIterations"
                :min="100"
                :max="5000"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="交叉率" name="crossoverRate">
              <a-slider
                v-model:value="planForm.crossoverRate"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="变异率" name="mutationRate">
              <a-slider
                v-model:value="planForm.mutationRate"
                :min="0"
                :max="1"
                :step="0.01"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="选择率" name="selectionRate">
              <a-slider
                v-model:value="planForm.selectionRate"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 优化权重 -->
        <a-divider orientation="left">优化权重</a-divider>

        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="公平性权重" name="fairnessWeight">
              <a-slider
                v-model:value="planForm.fairnessWeight"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="成本权重" name="costWeight">
              <a-slider
                v-model:value="planForm.costWeight"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="效率权重" name="efficiencyWeight">
              <a-slider
                v-model:value="planForm.efficiencyWeight"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="满意度权重" name="satisfactionWeight">
              <a-slider
                v-model:value="planForm.satisfactionWeight"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 排班结果查看弹窗 -->
    <a-modal
      v-model:visible="resultModalVisible"
      title="排班结果"
      :width="1400"
      :footer="null"
    >
      <smart-schedule-result-view :plan-id="selectedPlanId" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  PlusOutlined,
  ReloadOutlined,
  ExportOutlined,
  EditOutlined,
  DeleteOutlined,
  DownOutlined
} from '@ant-design/icons-vue';
import SmartScheduleResultView from './smart-schedule-result-view.vue';
import { smartScheduleApi } from '@/api/business/attendance/smart-schedule-api';

// 表格列定义
const columns = ref([
  {
    title: '计划名称',
    dataIndex: 'planName',
    key: 'planName',
    width: 200
  },
  {
    title: '优化目标',
    dataIndex: 'optimizationGoal',
    key: 'optimizationGoal',
    width: 120
  },
  {
    title: '执行状态',
    dataIndex: 'executionStatus',
    key: 'executionStatus',
    width: 100
  },
  {
    title: '适应度',
    dataIndex: 'fitnessScore',
    key: 'fitnessScore',
    width: 150
  },
  {
    title: '时间范围',
    dataIndex: 'dateRange',
    key: 'dateRange',
    width: 200
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
    width: 200,
    fixed: 'right'
  }
]);

// 数据
const planList = ref([]);
const loading = ref(false);
const createModalVisible = ref(false);
const resultModalVisible = ref(false);
const isEditMode = ref(false);
const selectedPlanId = ref(null);
const planFormRef = ref();

// 员工选项
const employeeOptions = ref([]);

// 班次选项
const shiftOptions = ref([]);

// 表单数据
const planForm = reactive({
  planName: '',
  planDescription: '',
  dateRange: [],
  employeeIds: [],
  shiftIds: [],
  optimizationGoal: 5,
  maxConsecutiveWorkDays: 6,
  minRestDays: 1,
  minDailyStaff: 5,
  maxMonthlyWorkDays: 22,
  populationSize: 100,
  maxIterations: 500,
  crossoverRate: 0.8,
  mutationRate: 0.1,
  selectionRate: 0.5,
  elitismRate: 0.1,
  fairnessWeight: 0.4,
  costWeight: 0.3,
  efficiencyWeight: 0.2,
  satisfactionWeight: 0.1
});

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

// 加载排班计划列表
const loadPlanList = async () => {
  loading.value = true;
  try {
    const res = await smartScheduleApi.queryPlanPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    });
    planList.value = res.data.list;
    pagination.total = res.data.total;
  } catch (error) {
    message.error('加载排班计划列表失败');
  } finally {
    loading.value = false;
  }
};

// 刷新列表
const refreshList = () => {
  loadPlanList();
};

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadPlanList();
};

// 显示创建弹窗
const showCreateModal = () => {
  isEditMode.value = false;
  createModalVisible.value = true;
};

// 查看计划详情
const viewPlanDetail = (planId) => {
  selectedPlanId.value = planId;
  // TODO: 实现详情查看
};

// 查看结果
const viewResults = (planId) => {
  selectedPlanId.value = planId;
  resultModalVisible.value = true;
};

// 执行优化
const executeOptimization = (planId) => {
  Modal.confirm({
    title: '确认执行优化',
    content: '执行优化可能需要较长时间，是否继续？',
    onOk: async () => {
      try {
        const res = await smartScheduleApi.executeOptimization(planId);
        message.success('优化执行完成！');
        loadPlanList();
      } catch (error) {
        message.error('优化执行失败：' + error.message);
      }
    }
  });
};

// 确认计划
const confirmPlan = (planId) => {
  Modal.confirm({
    title: '确认排班计划',
    content: '确认后将无法修改，是否继续？',
    onOk: async () => {
      try {
        await smartScheduleApi.confirmPlan(planId);
        message.success('计划确认成功！');
        loadPlanList();
      } catch (error) {
        message.error('计划确认失败：' + error.message);
      }
    }
  });
};

// 编辑计划
const editPlan = (planId) => {
  isEditMode.value = true;
  // TODO: 加载计划数据并显示弹窗
};

// 删除计划
const deletePlan = (planId) => {
  Modal.confirm({
    title: '确认删除',
    content: '确认删除该排班计划吗？',
    onOk: async () => {
      try {
        await smartScheduleApi.deletePlan(planId);
        message.success('删除成功！');
        loadPlanList();
      } catch (error) {
        message.error('删除失败：' + error.message);
      }
    }
  });
};

// 导出结果
const exportResult = async (planId) => {
  try {
    const res = await smartScheduleApi.exportResult(planId);
    // 处理文件下载
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `schedule_result_${planId}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    message.success('导出成功！');
  } catch (error) {
    message.error('导出失败：' + error.message);
  }
};

// 创建计划确认
const handleCreateOk = async () => {
  try {
    planFormRef.value.validate().async(async (errors) => {
      if (!errors) {
        const params = {
          ...planForm,
          startDate: planForm.dateRange[0].format('YYYY-MM-DD'),
          endDate: planForm.dateRange[1].format('YYYY-MM-DD')
        };
        await smartScheduleApi.createPlan(params);
        message.success('创建成功！');
        createModalVisible.value = false;
        loadPlanList();
      }
    });
  } catch (error) {
    message.error('创建失败：' + error.message);
  }
};

// 过滤选项
const filterOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

// 获取优化目标颜色
const getGoalColor = (goal) => {
  const colors = {
    1: 'blue',
    2: 'green',
    3: 'orange',
    4: 'purple',
    5: 'red'
  };
  return colors[goal] || 'default';
};

// 获取优化目标标签
const getGoalLabel = (goal) => {
  const labels = {
    1: '公平性优先',
    2: '成本优先',
    3: '效率优先',
    4: '满意度优先',
    5: '综合优化'
  };
  return labels[goal] || '未知';
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = {
    0: 'default',
    1: 'processing',
    2: 'success',
    3: 'error'
  };
  return colors[status] || 'default';
};

// 获取状态标签
const getStatusLabel = (status) => {
  const labels = {
    0: '待执行',
    1: '执行中',
    2: '已完成',
    3: '执行失败'
  };
  return labels[status] || '未知';
};

// 获取适应度状态
const getFitnessStatus = (score) => {
  if (score >= 0.9) return 'success';
  if (score >= 0.7) return 'normal';
  if (score >= 0.5) return 'active';
  return 'exception';
};

// 初始化
onMounted(() => {
  loadPlanList();
  // TODO: 加载员工和班次选项
});
</script>

<style lang="scss" scoped>
.smart-schedule-config {
  padding: 16px;
}
</style>
