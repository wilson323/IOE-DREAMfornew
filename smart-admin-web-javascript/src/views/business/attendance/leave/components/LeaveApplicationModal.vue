<!--
  @fileoverview 请假申请对话框
  @author IOE-DREAM Team
  @description 请假申请表单
-->
<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑请假' : '新增请假'"
    :width="600"
    :confirm-loading="loading"
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
      <!-- 员工信息（仅显示） -->
      <a-form-item label="员工姓名" name="employeeId">
        <a-input v-model:value="formData.employeeName" disabled placeholder="系统自动获取" />
      </a-form-item>

      <!-- 请假类型 -->
      <a-form-item label="请假类型" name="leaveType">
        <a-select v-model:value="formData.leaveType" placeholder="请选择请假类型" showSearch>
          <a-select-option value="ANNUAL">年假</a-select-option>
          <a-select-option value="SICK">病假</a-select-option>
          <a-select-option value="PERSONAL">事假</a-select-option>
          <a-select-option value="MARRIAGE">婚假</a-select-option>
          <a-select-option value="MATERNITY">产假</a-select-option>
          <a-select-option value="PATERNITY">陪产假</a-select-option>
          <a-select-option value="OTHER">其他</a-select-option>
        </a-select>
      </a-form-item>

      <!-- 请假日期范围 -->
      <a-form-item label="请假日期" name="dateRange">
        <a-range-picker
          v-model:value="formData.dateRange"
          :format="['YYYY-MM-DD', 'YYYY-MM-DD']"
          :placeholder="['开始日期', '结束日期']"
          style="width: 100%"
          @change="handleDateChange"
        />
      </a-form-item>

      <!-- 请假天数 -->
      <a-form-item label="请假天数" name="leaveDays">
        <a-input-number
          v-model:value="formData.leaveDays"
          :min="0.5"
          :max="365"
          :step="0.5"
          :precision="1"
          placeholder="请输入请假天数"
          style="width: 100%"
        >
          <template #addonAfter>天</template>
        </a-input-number>
        <div style="color: #999; font-size: 12px; margin-top: 4px">
          支持半天请假，最小单位0.5天
        </div>
      </a-form-item>

      <!-- 请假原因 -->
      <a-form-item label="请假原因" name="reason">
        <a-textarea
          v-model:value="formData.reason"
          placeholder="请输入请假原因"
          :rows="4"
          :maxlength="500"
          showCount
        />
      </a-form-item>

      <!-- 备注 -->
      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="请输入备注（可选）"
          :rows="2"
          :maxlength="200"
          showCount
        />
      </a-form-item>

      <!-- 请假说明 -->
      <a-alert
        v-if="formData.leaveType"
        message="请假说明"
        :description="getLeaveTypeDescription(formData.leaveType)"
        type="info"
        show-icon
        style="margin-top: 16px"
      />
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import type { Dayjs } from 'dayjs';
import { leaveApi, LeaveAddForm, LeaveType, LeaveRecordVO } from '@/api/business/attendance/leave';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  leaveData?: LeaveRecordVO | null;
}

const props = defineProps<Props>();

/**
 * 组件事件
 */
interface Emits {
  (e: 'cancel'): void;
  (e: 'success'): void;
}

const emit = defineEmits<Emits>();

/**
 * 表单引用
 */
const formRef = ref<FormInstance>();

/**
 * 表单数据
 */
const formData = reactive({
  employeeId: 0,
  employeeName: '',
  leaveType: undefined as LeaveType | undefined,
  dateRange: null as [Dayjs, Dayjs] | null,
  startDate: '',
  endDate: '',
  leaveDays: 1,
  reason: '',
  remark: '',
});

/**
 * 加载状态
 */
const loading = ref(false);

/**
 * 是否编辑模式
 */
const isEdit = computed(() => !!props.leaveData);

/**
 * 表单验证规则
 */
const rules: Record<string, Rule[]> = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择请假日期', trigger: 'change' }],
  leaveDays: [
    { required: true, message: '请输入请假天数', trigger: 'blur' },
    { type: 'number', min: 0.5, message: '请假天数最小0.5天', trigger: 'blur' },
  ],
  reason: [
    { required: true, message: '请输入请假原因', trigger: 'blur' },
    { min: 5, message: '请假原因至少5个字符', trigger: 'blur' },
    { max: 500, message: '请假原因最多500个字符', trigger: 'blur' },
  ],
};

/**
 * 日期范围变化
 */
const handleDateChange = (dates: [Dayjs, Dayjs] | null) => {
  if (dates && dates.length === 2) {
    formData.startDate = dates[0].format('YYYY-MM-DD');
    formData.endDate = dates[1].format('YYYY-MM-DD');

    // 自动计算请假天数
    const start = dates[0];
    const end = dates[1];
    const days = end.diff(start, 'day') + 1;
    formData.leaveDays = days;
  }
};

/**
 * 获取请假类型说明
 */
const getLeaveTypeDescription = (type: LeaveType) => {
  const descriptions: Record<LeaveType, string> = {
    ANNUAL: '年假：带薪休假，需提前申请，通常需要部门主管审批。',
    SICK: '病假：因病需要休息，建议提供医疗证明，具体天数按公司规定执行。',
    PERSONAL: '事假：因个人事务需要请假，通常为无薪假，需要提前申请。',
    MARRIAGE: '婚假：员工结婚可申请，需提供结婚证明，按公司规定享受假期。',
    MATERNITY: '产假：女性员工生育可申请，需提供相关医疗证明，按国家规定执行。',
    PATERNITY: '陪产假：男性员工配偶生育可申请，按公司规定执行。',
    OTHER: '其他：其他特殊类型的请假，请在备注中详细说明。',
  };
  return descriptions[type] || '';
};

/**
 * 重置表单
 */
const resetForm = () => {
  formRef.value?.resetFields();
  Object.assign(formData, {
    employeeId: 0,
    employeeName: '',
    leaveType: undefined,
    dateRange: null,
    startDate: '',
    endDate: '',
    leaveDays: 1,
    reason: '',
    remark: '',
  });
};

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();

    loading.value = true;

    const submitData: LeaveAddForm = {
      employeeId: formData.employeeId,
      leaveType: formData.leaveType!,
      startDate: formData.startDate,
      endDate: formData.endDate,
      leaveDays: formData.leaveDays,
      reason: formData.reason,
      remark: formData.remark,
    };

    if (isEdit.value && props.leaveData) {
      // 编辑模式
      await leaveApi.updateLeave(props.leaveData.id, submitData);
      message.success('修改成功');
    } else {
      // 新增模式
      await leaveApi.createLeave(submitData);
      message.success('申请成功');
    }

    emit('success');
  } catch (error: any) {
    console.error('提交失败', error);
    if (error.errorFields) {
      // 表单验证失败
      return;
    }
    message.error(isEdit.value ? '修改失败' : '申请失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 取消
 */
const handleCancel = () => {
  resetForm();
  emit('cancel');
};

/**
 * 监听visible变化
 */
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 打开对话框时，如果是编辑模式，填充数据
    if (props.leaveData) {
      Object.assign(formData, {
        employeeId: props.leaveData.employeeId,
        employeeName: props.leaveData.employeeName,
        leaveType: props.leaveData.leaveType,
        dateRange: [props.leaveData.startDate, props.leaveData.endDate] as any,
        startDate: props.leaveData.startDate,
        endDate: props.leaveData.endDate,
        leaveDays: props.leaveData.leaveDays,
        reason: props.leaveData.reason,
        remark: props.leaveData.remark || '',
      });
    } else {
      // 新增模式，设置默认员工信息（从用户信息中获取）
      // TODO: 从登录用户信息中获取
      formData.employeeId = 1;
      formData.employeeName = '当前员工';
    }
  } else {
    resetForm();
  }
});
</script>

<style scoped lang="less">
:deep(.ant-modal-body) {
  padding: 24px;
}

:deep(.ant-alert) {
  margin-top: 16px;
}
</style>
