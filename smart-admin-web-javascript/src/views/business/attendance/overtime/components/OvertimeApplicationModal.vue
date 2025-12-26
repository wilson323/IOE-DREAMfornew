<!--
  @fileoverview 加班申请对话框
  @author IOE-DREAM Team
  @description 加班申请表单
-->
<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑加班' : '新增加班'"
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
      <!-- 员工信息 -->
      <a-form-item label="员工姓名" name="employeeId">
        <a-input v-model:value="formData.employeeName" disabled placeholder="系统自动获取" />
      </a-form-item>

      <!-- 加班日期 -->
      <a-form-item label="加班日期" name="overtimeDate">
        <a-date-picker
          v-model:value="formData.overtimeDate"
          placeholder="请选择加班日期"
          style="width: 100%"
          @change="handleDateChange"
        />
      </a-form-item>

      <!-- 加班时间 -->
      <a-form-item label="加班时间" name="timeRange">
        <a-time-range-picker
          v-model:value="formData.timeRange"
          :format="'HH:mm'"
          :placeholder="['开始时间', '结束时间']"
          style="width: 100%"
          @change="handleTimeChange"
        />
      </a-form-item>

      <!-- 加班时长 -->
      <a-form-item label="加班时长" name="overtimeHours">
        <a-input-number
          v-model:value="formData.overtimeHours"
          :min="0.5"
          :max="24"
          :step="0.5"
          :precision="1"
          placeholder="请输入加班时长"
          style="width: 100%"
        >
          <template #addonAfter>小时</template>
        </a-input-number>
        <div style="color: #999; font-size: 12px; margin-top: 4px">
          支持半小时加班，最小单位0.5小时
        </div>
      </a-form-item>

      <!-- 加班原因 -->
      <a-form-item label="加班原因" name="reason">
        <a-textarea
          v-model:value="formData.reason"
          placeholder="请输入加班原因"
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
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import type { Dayjs } from 'dayjs';
import { overtimeApi, OvertimeAddForm, OvertimeRecordVO } from '@/api/business/attendance/overtime';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  overtimeData?: OvertimeRecordVO | null;
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
  overtimeDate: null as Dayjs | null,
  timeRange: null as [Dayjs, Dayjs] | null,
  startDate: '',
  startTime: '',
  endTime: '',
  overtimeHours: 1,
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
const isEdit = computed(() => !!props.overtimeData);

/**
 * 表单验证规则
 */
const rules: Record<string, Rule[]> = {
  overtimeDate: [{ required: true, message: '请选择加班日期', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择加班时间', trigger: 'change' }],
  overtimeHours: [
    { required: true, message: '请输入加班时长', trigger: 'blur' },
    { type: 'number', min: 0.5, message: '加班时长最小0.5小时', trigger: 'blur' },
  ],
  reason: [
    { required: true, message: '请输入加班原因', trigger: 'blur' },
    { min: 5, message: '加班原因至少5个字符', trigger: 'blur' },
    { max: 500, message: '加班原因最多500个字符', trigger: 'blur' },
  ],
};

/**
 * 日期变化
 */
const handleDateChange = (date: Dayjs | null) => {
  formData.overtimeDate = date;
  formData.startDate = date ? date.format('YYYY-MM-DD') : '';
};

/**
 * 时间范围变化
 */
const handleTimeChange = (times: [Dayjs, Dayjs] | null) => {
  if (times && times.length === 2) {
    formData.startTime = times[0].format('HH:mm:ss');
    formData.endTime = times[1].format('HH:mm:ss');

    // 自动计算加班时长
    const start = times[0];
    const end = times[1];
    const hours = end.diff(start, 'hour', true);
    formData.overtimeHours = Math.round(hours * 10) / 10; // 保留一位小数
  }
};

/**
 * 重置表单
 */
const resetForm = () => {
  formRef.value?.resetFields();
  Object.assign(formData, {
    employeeId: 0,
    employeeName: '',
    overtimeDate: null,
    timeRange: null,
    startDate: '',
    startTime: '',
    endTime: '',
    overtimeHours: 1,
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

    const submitData: OvertimeAddForm = {
      employeeId: formData.employeeId,
      overtimeDate: formData.startDate,
      startTime: formData.startTime,
      endTime: formData.endTime,
      overtimeHours: formData.overtimeHours,
      reason: formData.reason,
      remark: formData.remark,
    };

    await overtimeApi.submitOvertime(submitData);
    message.success('申请成功');

    emit('success');
  } catch (error: any) {
    console.error('提交失败', error);
    if (error.errorFields) {
      return;
    }
    message.error('申请失败');
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
    if (props.overtimeData) {
      // 编辑模式填充数据
      Object.assign(formData, {
        employeeId: props.overtimeData.employeeId,
        employeeName: props.overtimeData.employeeName,
        overtimeDate: props.overtimeData.overtimeDate as any,
        timeRange: [props.overtimeData.startTime, props.overtimeData.endTime] as any,
        startDate: props.overtimeData.overtimeDate,
        startTime: props.overtimeData.startTime,
        endTime: props.overtimeData.endTime,
        overtimeHours: props.overtimeData.overtimeHours,
        reason: props.overtimeData.reason,
        remark: props.overtimeData.remark || '',
      });
    } else {
      // 新增模式设置默认员工信息
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
</style>
