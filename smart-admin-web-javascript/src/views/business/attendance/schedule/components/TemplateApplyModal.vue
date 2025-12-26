<!--
  @fileoverview 模板应用对话框组件
  @author IOE-DREAM Team
  @description 将排班模板应用到指定日期范围和人员
-->
<template>
  <a-modal
    :visible="visible"
    title="应用排班模板"
    :width="800"
    :confirm-loading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <!-- 基本信息 -->
      <a-divider orientation="left">应用范围</a-divider>

      <a-row>
        <a-col :span="12">
          <a-form-item label="开始日期" name="startDate">
            <a-date-picker
              v-model:value="startDateValue"
              format="YYYY-MM-DD"
              placeholder="请选择开始日期"
              style="width: 100%;"
              @change="handleStartDateChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="结束日期" name="endDate">
            <a-date-picker
              v-model:value="endDateValue"
              format="YYYY-MM-DD"
              placeholder="请选择结束日期"
              style="width: 100%;"
              @change="handleEndDateChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 应用对象 -->
      <a-divider orientation="left">应用对象</a-divider>

      <a-form-item label="应用范围" name="scheduleScope">
        <a-radio-group v-model:value="scheduleScope">
          <a-radio value="DEPARTMENT">按部门</a-radio>
          <a-radio value="EMPLOYEE">按员工</a-radio>
          <a-radio value="ALL">全体人员</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item v-if="scheduleScope === 'DEPARTMENT'" label="选择部门" name="departmentIds">
        <a-select
          v-model:value="formData.departmentIds"
          mode="multiple"
          placeholder="请选择部门"
          :options="departmentOptions"
          :max-tag-count="3"
        />
      </a-form-item>

      <a-form-item v-if="scheduleScope === 'EMPLOYEE'" label="选择员工" name="employeeIds">
        <a-select
          v-model:value="formData.employeeIds"
          mode="multiple"
          placeholder="请选择员工"
          :options="employeeOptions"
          :max-tag-count="3"
          show-search
        />
      </a-form-item>

      <!-- 优化选项 -->
      <a-divider orientation="left">优化选项</a-divider>

      <a-form-item label="启用优化" name="enableOptimization">
        <a-switch
          v-model:checked="formData.enableOptimization"
          checked-children="是"
          un-checked-children="否"
        />
        <span style="margin-left: 8px; color: #8c8c8c;">
          启用后将根据优化目标调整排班
        </span>
      </a-form-item>

      <a-form-item
        v-if="formData.enableOptimization"
        label="优化目标"
        name="optimizationTarget"
      >
        <a-select
          v-model:value="formData.optimizationTarget"
          placeholder="请选择优化目标"
        >
          <a-select-option value="BALANCE_WORKLOAD">均衡工作负载</a-select-option>
          <a-select-option value="MINIMIZE_OVERTIME">最小化加班</a-select-option>
          <a-select-option value="MAXIMIZE_COVERAGE">最大化覆盖</a-select-option>
          <a-select-option value="REDUCE_COST">降低成本</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="自动解决冲突" name="autoResolveConflicts">
        <a-switch
          v-model:checked="formData.autoResolveConflicts"
          checked-children="是"
          un-checked-children="否"
        />
        <span style="margin-left: 8px; color: #8c8c8c;">
          启用后将自动解决检测到的冲突
        </span>
      </a-form-item>

      <!-- 预览信息 -->
      <a-divider orientation="left">应用预览</a-divider>

      <a-form-item label="预计统计">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic title="应用天数" :value="estimatedDays" suffix="天" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="应用人员" :value="estimatedEmployees" suffix="人" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="预计班次" :value="estimatedShifts" suffix="个" />
          </a-col>
        </a-row>
      </a-form-item>

      <a-alert
        v-if="hasConflicts"
        type="warning"
        message="检测到潜在冲突"
        description="应用模板可能会产生排班冲突，建议启用自动解决冲突功能"
        show-icon
      />
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import { scheduleApi, type ApplyTemplateRequest } from '@/api/business/attendance/schedule';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  templateId?: number;
}

/**
 * 组件事件
 */
interface Emits {
  (e: 'update:visible', visible: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// 表单引用
const formRef = ref();

// 应用范围
const scheduleScope = ref<'DEPARTMENT' | 'EMPLOYEE' | 'ALL'>('ALL');

// 部门选项
const departmentOptions = ref([
  { label: '技术部', value: 1 },
  { label: '市场部', value: 2 },
  { label: '行政部', value: 3 },
  { label: '财务部', value: 4 },
  { label: '人事部', value: 5 }
]);

// 员工选项
const employeeOptions = ref([
  { label: '张三 - 技术部', value: 1 },
  { label: '李四 - 市场部', value: 2 },
  { label: '王五 - 行政部', value: 3 },
  { label: '赵六 - 技术部', value: 4 },
  { label: '钱七 - 财务部', value: 5 }
]);

// 表单数据
const formData = ref<ApplyTemplateRequest>({
  startDate: '',
  endDate: '',
  departmentIds: [],
  employeeIds: [],
  enableOptimization: false,
  optimizationTarget: 'BALANCE_WORKLOAD',
  autoResolveConflicts: true
});

// 日期选择器值
const startDateValue = ref<Dayjs>();
const endDateValue = ref<Dayjs>();

// 确认加载
const confirmLoading = ref(false);

// 表单验证规则
const rules = {
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
};

// 预计统计
const estimatedDays = computed(() => {
  if (!formData.value.startDate || !formData.value.endDate) return 0;
  const start = dayjs(formData.value.startDate);
  const end = dayjs(formData.value.endDate);
  return end.diff(start, 'day') + 1;
});

const estimatedEmployees = computed(() => {
  if (scheduleScope.value === 'ALL') {
    return employeeOptions.value.length;
  } else if (scheduleScope.value === 'DEPARTMENT') {
    return Math.floor(Math.random() * 50) + 10; // TODO: 根据部门实际人数计算
  } else {
    return formData.value.employeeIds?.length || 0;
  }
});

const estimatedShifts = computed(() => {
  return estimatedDays.value * estimatedEmployees.value;
});

// 检测冲突
const hasConflicts = computed(() => {
  // TODO: 实际应用时调用API检测冲突
  return false;
});

// 时间处理函数
const handleStartDateChange = (date: Dayjs) => {
  formData.value.startDate = date ? date.format('YYYY-MM-DD') : '';
};

const handleEndDateChange = (date: Dayjs) => {
  formData.value.endDate = date ? date.format('YYYY-MM-DD') : '';
};

/**
 * 重置表单
 */
const resetForm = () => {
  formData.value = {
    startDate: dayjs().format('YYYY-MM-DD'),
    endDate: dayjs().add(7, 'day').format('YYYY-MM-DD'),
    departmentIds: [],
    employeeIds: [],
    enableOptimization: false,
    optimizationTarget: 'BALANCE_WORKLOAD',
    autoResolveConflicts: true
  };
  startDateValue.value = dayjs();
  endDateValue.value = dayjs().add(7, 'day');
  scheduleScope.value = 'ALL';
  formRef.value?.clearValidate();
};

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    confirmLoading.value = true;

    const request = {
      ...formData.value,
      departmentIds: scheduleScope.value === 'DEPARTMENT' ? formData.value.departmentIds : undefined,
      employeeIds: scheduleScope.value === 'EMPLOYEE' ? formData.value.employeeIds : undefined
    };

    const result = await scheduleApi.applyTemplate(props.templateId!, request);
    if (result.data) {
      if (result.data.conflicts && result.data.conflicts.length > 0) {
        message.warning(`模板应用成功，但检测到 ${result.data.conflicts.length} 个冲突`);
      } else {
        message.success(`模板应用成功，共生成 ${result.data.createdRecords} 条排班记录`);
      }
      emit('success');
      handleCancel();
    }
  } catch (error: any) {
    if (error.errorFields) {
      return;
    }
    console.error('应用模板失败:', error);
    message.error('应用模板失败');
  } finally {
    confirmLoading.value = false;
  }
};

/**
 * 取消
 */
const handleCancel = () => {
  emit('update:visible', false);
};

/**
 * 监听visible变化
 */
watch(() => props.visible, (visible) => {
  if (visible) {
    resetForm();
  }
});
</script>

<style scoped lang="less">
:deep(.ant-divider) {
  margin: 16px 0;
  font-size: 14px;
  font-weight: 600;
}
</style>
