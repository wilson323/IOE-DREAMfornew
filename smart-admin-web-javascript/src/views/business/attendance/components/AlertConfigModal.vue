<!--
 * 预警配置弹窗组件
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-25
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <a-modal
    v-model:open="modalVisible"
    title="预警规则配置"
    :width="900"
    :confirmLoading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :okText="保存配置"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
    >
      <!-- 基础配置 -->
      <a-form-item label="规则名称" name="ruleName" required>
        <a-input
          v-model:value="formData.ruleName"
          placeholder="请输入规则名称"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="规则类型" name="ruleType" required>
        <a-select
          v-model:value="formData.ruleType"
          placeholder="请选择规则类型"
          @change="handleRuleTypeChange"
        >
          <a-select-option value="attendance_rate">出勤率预警</a-select-option>
          <a-select-option value="late_count">迟到次数预警</a-select-option>
          <a-select-option value="early_leave_count">早退次数预警</a-select-option>
          <a-select-option value="absence_count">缺勤天数预警</a-select-option>
          <a-select-option value="work_hours">工作时长预警</a-select-option>
          <a-select-option value="abnormal_pattern">异常模式预警</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="触发条件" name="conditionType" required>
        <a-radio-group
          v-model:value="formData.conditionType"
          @change="handleConditionTypeChange"
        >
          <a-radio value="greater_than">大于</a-radio>
          <a-radio value="less_than">小于</a-radio>
          <a-radio value="equals">等于</a-radio>
          <a-radio value="percentage_change">百分比变化</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="阈值" name="threshold" required>
        <a-input-number
          v-model:value="formData.threshold"
          :min="0"
          :max="formData.ruleType === 'attendance_rate' ? 100 : 999"
          :step="formData.ruleType === 'attendance_rate' ? 0.1 : 1"
          :precision="formData.ruleType === 'attendance_rate' ? 1 : 0"
          :formatter="(value) => `${value}%`"
          :parser="(value) => value.replace('%', '')"
          placeholder="请输入阈值"
          style="width: 200px"
          allow-clear
        />
        <span class="ml-2 text-gray-500">
          {{ getThresholdUnit() }}
        </span>
      </a-form-item>

      <!-- 监控范围 -->
      <a-form-item label="监控范围" name="monitoringScope">
        <a-space direction="vertical" style="width: 100%">
          <div>
            <span>监控对象：</span>
            <a-select
              v-model:value="formData.monitoringScope.targets"
              mode="multiple"
              placeholder="请选择监控对象"
              style="width: 100%; margin-top: 4px"
              :disabled="isCompanyLevelRule"
            >
              <a-select-option value="employee">员工</a-select-option>
              <a-select-option value="department">部门</a-select-option>
              <a-select-option value="team">团队</a-select-option>
            </a-select>
          </div>

          <div v-if="formData.monitoringScope.targets.includes('employee')">
            <span>员工选择：</span>
            <a-select
              v-model:value="formData.monitoringScope.employeeIds"
              mode="multiple"
              placeholder="请选择员工"
              style="width: 100%; margin-top: 4px"
              :showSearch="true"
              :filter-option="filterEmployeeOption"
            >
              <a-select-option
                v-for="employee in employeeOptions"
                :key="employee.employeeId"
                :value="employee.employeeId"
              >
                {{ employee.employeeName }} ({{ employee.departmentName }})
              </a-select-option>
            </a-select>
          </div>

          <div v-if="formData.monitoringScope.targets.includes('department')">
            <span>部门选择：</span>
            <a-tree-select
              v-model:value="formData.monitoringScope.departmentIds"
              :tree-data="departmentTreeData"
              placeholder="请选择部门"
              allow-clear
              multiple
              tree-checkable
              style="width: 100%; margin-top: 4px"
            />
          </div>
        </a-space>
      </a-form-item>

      <!-- 时间配置 -->
      <a-form-item label="时间范围" name="timeRange">
        <a-space direction="vertical" style="width: 100%">
          <div>
            <span>统计周期：</span>
            <a-select
              v-model:value="formData.timeRange.cycle"
              style="width: 150px; margin-top: 4px"
            >
              <a-select-option value="daily">按日</a-select-option>
              <a-select-option value="weekly">按周</a-select-option>
              <a-select-option value="monthly">按月</a-select-option>
              <a-select-option value="quarterly">按季度</a-select-option>
            </a-select>
          </div>

          <div>
            <span>时间范围：</span>
            <a-range-picker
              v-model:value="formData.timeRange.dateRange"
              :presets="dateRangePresets"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 300px; margin-top: 4px"
            />
          </div>

          <div>
            <span>排除周末：</span>
            <a-switch
              v-model:checked="formData.timeRange.excludeWeekend"
              checked-children="排除"
              un-checked-children="包含"
            />
          </div>
        </space>
      </a-form-item>

      <!-- 通知配置 -->
      <a-form-item label="通知方式" name="notificationMethods">
        <a-checkbox-group
          v-model:value="formData.notificationMethods"
        >
          <a-checkbox value="system">系统通知</a-checkbox>
          <a-checkbox value="email">邮件通知</a-checkbox>
          <a-checkbox value="sms">短信通知</a-checkbox>
          <a-checkbox value="dingtalk">钉钉通知</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-form-item label="邮件配置" name="emailConfig" v-if="formData.notificationMethods.includes('email')">
        <a-card size="small" :bordered="false">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="收件人">
                <a-textarea
                  v-model:value="formData.notificationConfig.email.recipients"
                  placeholder="请输入邮箱地址，多个用逗号分隔"
                  :rows="3"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="抄送人">
                <a-textarea
                  v-model:value="formData.notificationConfig.email.cc"
                  placeholder="请输入抄送邮箱地址，多个用逗号分隔"
                  :rows="3"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>
      </a-form-item>

      <!-- 执行动作 -->
      <a-form-item label="执行动作" name="actions">
        <a-space direction="vertical" style="width: 100%">
          <a-checkbox
            v-model:checked="formData.actions.createReport"
          >
            生成预警报告
          </a-checkbox>
          <a-checkbox
            v-model:checked="formData.actions.sendNotification"
          >
            发送通知消息
          </a-checkbox>
          <a-checkbox
            v-model:checked="actions.createTicket"
          >
            创建工单
          </a-checkbox>
          <a-checkbox
            v-model:checked="formData.actions.updateRiskScore"
          >
            更新风险评分
          </a-checkbox>
        </a-space>
      </a-form-item>

      <!-- 高级配置 -->
      <a-form-item label="高级配置" name="advancedConfig">
        <a-collapse ghost>
          <a-collapse-panel key="1" header="连续触发设置">
            <a-space direction="vertical" style="width: 100%">
              <div>
                <span>连续触发次数：</span>
                <a-input-number
                  v-model:value="formData.advancedConfig.consecutiveCount"
                  :min="1"
                  :max="10"
                  placeholder="连续多少次触发"
                  style="width: 150px; margin-top: 4px"
                />
              </div>
              <div>
                <span>时间窗口：</span>
                <a-input-number
                  v-model:value="formData.advancedConfig.timeWindow"
                  :min="1"
                  :max="24"
                  placeholder="时间窗口（小时）"
                  suffix="小时"
                  style="width: 150px; margin-top: 4px"
                />
              </div>
            </a-space>
          </a-collapse-panel>

          <a-collapse-panel key="2" header="恢复策略">
            <a-space direction="vertical" style="width: 100%">
              <div>
                <span>自动恢复：</span>
                <a-switch
                  v-model:checked="formData.advancedConfig.autoRecover"
                  checked-children="启用"
                  un-checked-children="禁用"
                />
              </div>
              <div v-if="formData.advancedConfig.autoRecover">
                <span>恢复时间：</span>
                <a-input-number
                  v-model:value="formData.advancedConfig.recoveryTime"
                  :min="1"
                  :max="168"
                  placeholder="恢复时间（小时）"
                  suffix="小时"
                  style="width: 150px; margin-top: 4px"
                />
              </div>
            </div>
          </a-collapse-panel>

          <a-collapse-panel key="3" header="权重设置">
            <div>
              <span>优先级：</span>
              <a-select
                v-model:value="formData.advancedConfig.priority"
                style="width: 120px; margin-top: 4px"
              >
                <a-select-option value="high">高</a-select-option>
                <a-select-option value="medium">中</a-select-option>
                <a-select-option value="low">低</a-select-option>
              </a-select>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </a-form-item>

      <!-- 规则状态 -->
      <a-form-item label="规则状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio value="active">启用</a-radio>
          <a-radio value="inactive">禁用</a-radio>
          <a-radio value="testing">测试</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="备注说明" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入备注说明"
          :rows="3"
          allow-clear
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { attendanceStatisticsApi } from '/@/api/business/attendance/attendance-statistics-api';

// Props定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  ruleData: {
    type: Object,
    default: null
  }
});

// Emits定义
const emit = defineEmits(['update:visible', 'success']);

// 响应式数据
const modalVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

const confirmLoading = ref(false);
const formRef = ref();
const employeeOptions = ref([]);
const departmentTreeData = ref([]);

// 表单数据
const formData = reactive({
  ruleName: '',
  ruleType: undefined,
  conditionType: 'less_than',
  threshold: null,
  monitoringScope: {
    targets: ['employee'],
    employeeIds: [],
    departmentIds: []
  },
  timeRange: {
    cycle: 'monthly',
    dateRange: [],
    excludeWeekend: false
  },
  notificationMethods: ['system'],
  notificationConfig: {
    email: {
      recipients: '',
      cc: ''
    }
  },
  actions: {
    createReport: true,
    sendNotification: true,
    createTicket: false,
    updateRiskScore: false
  },
  advancedConfig: {
    consecutiveCount: 3,
    timeWindow: 24,
    autoRecover: false,
    recoveryTime: 24,
    priority: 'medium'
  },
  status: 'active',
  description: ''
});

// 表单验证规则
const formRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  conditionType: [
    { required: true, message: '请选择触发条件', trigger: 'change' }
  ],
  threshold: [
    { required: true, message: '请输入阈值', trigger: 'blur' }
  ]
};

// 计算属性
const isCompanyLevelRule = computed(() => {
  return ['department_comparison', 'company_overview'].includes(formData.ruleType);
});

// 日期范围预设
const dateRangePresets = [
  { label: '本周', value: () => [dayjs().startOf('week'), dayjs().endOf('week')] },
  { label: '本月', value: () => [dayjs().startOf('month'), dayjs().endOf('month')] },
  { label: '上月', value: () => [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')] },
  { label: '本季度', value: () => [dayjs().startOf('quarter'), dayjs().endOf('quarter')] },
  { label: '本年', value: () => [dayjs().startOf('year'), dayjs().endOf('year')] }
];

// 监听弹窗显示状态
watch(
  modalVisible,
  (newVisible) => {
    if (newVisible) {
      if (props.ruleData) {
        Object.assign(formData, props.ruleData);
      }
      loadBasicData();
    }
  }
);

// 监听规则类型变化
watch(
  () => formData.ruleType,
  () => {
    // 根据规则类型调整默认阈值
    adjustThresholdByRuleType();
  }
);

// ============ 方法 ============

// 加载基础数据
const loadBasicData = async () => {
  try {
    // 加载员工选项
    await loadEmployeeOptions();
    // 加载部门树数据
    await loadDepartmentTreeData();
  } catch (err) {
    console.error('加载基础数据失败:', err);
  }
};

// 加载员工选项
const loadEmployeeOptions = async () => {
  try {
    // 模拟数据
    employeeOptions.value = [
      { employeeId: 1, employeeName: '张三', departmentName: '技术部' },
      { employeeId: 2, employeeName: '李四', departmentName: '人事部' },
      { employeeId: 3, employeeName: '王五', departmentName: '财务部' }
    ];
  } catch (err) {
    console.error('加载员工选项失败:', err);
  }
};

// 加载部门树数据
const loadDepartmentTreeData = async () => {
  try {
    // 模拟数据
    departmentTreeData.value = [
      {
        key: 1,
        title: '技术部',
        value: 1,
        children: [
          { key: 11, title: '前端组', value: 11 },
          { key: 12, title: '后端组', value: 12 }
        ]
      },
      {
        key: 2,
        title: '人事部',
        value: 2
      },
      {
        key: 3,
        title: '财务部',
        value: 3
      }
    ];
  } catch (err) {
    console.error('加载部门树数据失败:', err);
  }
};

// 根据规则类型调整阈值
const adjustThresholdByRuleType = () => {
  switch (formData.ruleType) {
    case 'attendance_rate':
      formData.threshold = 90;
      formData.conditionType = 'less_than';
      break;
    case 'late_count':
      formData.threshold = 3;
      formData.conditionType = 'greater_than';
      break;
    case 'early_leave_count':
      formData.threshold = 2;
      formData.conditionType = 'greater_than';
      break;
    case 'absence_count':
      formData.threshold = 1;
      formData.conditionType = 'greater_than';
      break;
    case 'work_hours':
      formData.threshold = 7;
      formData.conditionType = 'less_than';
      break;
    case 'abnormal_pattern':
      formData.threshold = 5;
      formData.conditionType = 'greater_than';
      break;
  }
};

// 获取阈值单位
const getThresholdUnit = () => {
  switch (formData.ruleType) {
    case 'attendance_rate':
      return '%';
    case 'late_count':
    case 'early_leave_count':
    case 'absence_count':
      return '次';
    case 'work_hours':
      return '小时';
    default:
      return '';
  }
};

// 规则类型变化处理
const handleRuleTypeChange = (value) => {
  adjustThresholdByRuleType();
};

// 条件类型变化处理
const handleConditionTypeChange = () => {
  // 条件类型变化时可以调整表单字段
};

// 确认操作
const handleOk = async () => {
  try {
    await formRef.value.validate();
    confirmLoading.value = true;

    const submitData = { ...formData };

    if (props.ruleData) {
      // 编辑现有规则
      const result = await attendanceStatisticsApi.updateStatisticsAlertRule(props.ruleData.ruleId, submitData);
      if (result.success) {
        message.success('预警规则更新成功');
        emit('success', result.data);
        modalVisible.value = false;
      }
    } else {
      // 创建新规则
      const result = await attendanceStatisticsApi.saveStatisticsAlertRules(submitData);
      if (result.success) {
        message.success('预警规则创建成功');
        emit('success', result.data);
        modalVisible.value = false;
      }
    }
  } catch (error) {
    console.error('保存预警规则失败:', error);
    message.error('保存预警规则失败');
  } finally {
    confirmLoading.value = false;
  }
};

// 取消操作
const handleCancel = () => {
  modalVisible.value = false;
};

// 员工过滤
const filterEmployeeOption = (input, option) => {
  return option.children.toLowerCase().includes(input.toLowerCase());
};
</script>

<style lang="less" scoped>
.ml-2 {
  margin-left: 8px;
}

.text-gray-500 {
  color: #8c8c8c;
}
</style>