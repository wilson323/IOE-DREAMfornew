<!--
  @fileoverview 补签申请对话框
  @author IOE-DREAM Team
  @description 补签申请表单
-->
<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑补签' : '新增补签'"
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

      <!-- 补签日期 -->
      <a-form-item label="补签日期" name="supplementDate">
        <a-date-picker
          v-model:value="formData.supplementDate"
          placeholder="请选择补签日期"
          style="width: 100%"
          @change="handleDateChange"
        />
      </a-form-item>

      <!-- 打卡时间 -->
      <a-form-item label="打卡时间" name="punchTime">
        <a-time-picker
          v-model:value="formData.punchTime"
          :format="'HH:mm:ss'"
          placeholder="请选择打卡时间"
          style="width: 100%"
          @change="handleTimeChange"
        />
        <div style="color: #999; font-size: 12px; margin-top: 4px">
          请选择实际打卡时间
        </div>
      </a-form-item>

      <!-- 打卡类型 -->
      <a-form-item label="打卡类型" name="punchType">
        <a-radio-group v-model:value="formData.punchType">
          <a-radio value="CHECK_IN">
            <LoginOutlined style="color: #1890ff; margin-right: 4px" />
            上班打卡
          </a-radio>
          <a-radio value="CHECK_OUT" style="margin-left: 24px">
            <LogoutOutlined style="color: #52c41a; margin-right: 4px" />
            下班打卡
          </a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 补签原因 -->
      <a-form-item label="补签原因" name="reason">
        <a-textarea
          v-model:value="formData.reason"
          placeholder="请输入补签原因"
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
import {
  LoginOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue';
import { supplementApi, SupplementAddForm, SupplementRecordVO } from '@/api/business/attendance/supplement';

/**
 * 组件属性
 */
interface Props {
  visible: boolean;
  supplementData?: SupplementRecordVO | null;
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
  supplementDate: null as Dayjs | null,
  supplementDateStr: '',
  punchTime: null as Dayjs | null,
  punchTimeStr: '',
  punchType: 'CHECK_IN' as 'CHECK_IN' | 'CHECK_OUT',
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
const isEdit = computed(() => !!props.supplementData);

/**
 * 表单验证规则
 */
const rules: Record<string, Rule[]> = {
  supplementDate: [{ required: true, message: '请选择补签日期', trigger: 'change' }],
  punchTime: [{ required: true, message: '请选择打卡时间', trigger: 'change' }],
  punchType: [{ required: true, message: '请选择打卡类型', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入补签原因', trigger: 'blur' },
    { min: 5, message: '补签原因至少5个字符', trigger: 'blur' },
    { max: 500, message: '补签原因最多500个字符', trigger: 'blur' },
  ],
};

/**
 * 日期变化
 */
const handleDateChange = (date: Dayjs | null) => {
  formData.supplementDate = date;
  formData.supplementDateStr = date ? date.format('YYYY-MM-DD') : '';
};

/**
 * 时间变化
 */
const handleTimeChange = (time: Dayjs | null) => {
  formData.punchTime = time;
  formData.punchTimeStr = time ? time.format('HH:mm:ss') : '';
};

/**
 * 重置表单
 */
const resetForm = () => {
  formRef.value?.resetFields();
  Object.assign(formData, {
    employeeId: 0,
    employeeName: '',
    supplementDate: null,
    supplementDateStr: '',
    punchTime: null,
    punchTimeStr: '',
    punchType: 'CHECK_IN',
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

    const submitData: SupplementAddForm = {
      employeeId: formData.employeeId,
      supplementDate: formData.supplementDateStr,
      punchTime: formData.punchTimeStr,
      punchType: formData.punchType,
      reason: formData.reason,
      remark: formData.remark,
    };

    await supplementApi.submitSupplement(submitData);
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
    if (props.supplementData) {
      // 编辑模式填充数据
      Object.assign(formData, {
        employeeId: props.supplementData.employeeId,
        employeeName: props.supplementData.employeeName,
        supplementDate: props.supplementData.supplementDate as any,
        supplementDateStr: props.supplementData.supplementDate,
        punchTime: props.supplementData.punchTime as any,
        punchTimeStr: props.supplementData.punchTime,
        punchType: props.supplementData.punchType,
        reason: props.supplementData.reason,
        remark: props.supplementData.remark || '',
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
