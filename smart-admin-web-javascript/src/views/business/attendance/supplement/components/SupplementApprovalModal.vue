<!--
  @fileoverview 补签审批对话框
  @author IOE-DREAM Team
  @description 补签审批表单
-->
<template>
  <a-modal
    :visible="visible"
    title="补签审批"
    :width="700"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <!-- 补签信息展示 -->
    <a-descriptions title="补签信息" bordered :column="2" size="small" style="margin-bottom: 16px">
      <a-descriptions-item label="员工姓名">
        {{ supplementData?.employeeName }}
      </a-descriptions-item>
      <a-descriptions-item label="部门">
        {{ supplementData?.departmentName }}
      </a-descriptions-item>
      <a-descriptions-item label="补签日期" :span="2">
        {{ supplementData?.supplementDate }}
      </a-descriptions-item>
      <a-descriptions-item label="打卡时间" :span="2">
        {{ supplementData?.punchTime }}
      </a-descriptions-item>
      <a-descriptions-item label="打卡类型">
        <a-tag :color="supplementData?.punchType === 'CHECK_IN' ? 'blue' : 'green'">
          {{ supplementData?.punchType === 'CHECK_IN' ? '上班打卡' : '下班打卡' }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="申请时间">
        {{ supplementData?.createTime }}
      </a-descriptions-item>
      <a-descriptions-item label="补签原因" :span="2">
        {{ supplementData?.reason }}
      </a-descriptions-item>
      <a-descriptions-item v-if="supplementData?.remark" label="备注" :span="2">
        {{ supplementData?.remark }}
      </a-descriptions-item>
    </a-descriptions>

    <!-- 审批操作 -->
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 18 }"
    >
      <!-- 审批结果 -->
      <a-form-item label="审批结果" name="approvalResult">
        <a-radio-group v-model:value="formData.approvalResult">
          <a-radio value="approve">
            <CheckCircleOutlined style="color: #52c41a; margin-right: 4px" />
            通过
          </a-radio>
          <a-radio value="reject" style="margin-left: 24px">
            <CloseCircleOutlined style="color: #f5222d; margin-right: 4px" />
            驳回
          </a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 审批意见 -->
      <a-form-item label="审批意见" name="approvalComment">
        <a-textarea
          v-model:value="formData.approvalComment"
          placeholder="请输入审批意见"
          :rows="4"
          :maxlength="500"
          showCount
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons-vue';
import { supplementApi, SupplementApprovalForm, SupplementRejectionForm, SupplementRecordVO } from '@/api/business/attendance/supplement';

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
  approvalResult: 'approve' as 'approve' | 'reject',
  approvalComment: '',
});

/**
 * 加载状态
 */
const loading = ref(false);

/**
 * 表单验证规则
 */
const rules: Record<string, Rule[]> = {
  approvalComment: [
    { required: true, message: '请输入审批意见', trigger: 'blur' },
    { min: 5, message: '审批意见至少5个字符', trigger: 'blur' },
    { max: 500, message: '审批意见最多500个字符', trigger: 'blur' },
  ],
};

/**
 * 重置表单
 */
const resetForm = () => {
  formRef.value?.resetFields();
  Object.assign(formData, {
    approvalResult: 'approve',
    approvalComment: '',
  });
};

/**
 * 提交审批
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    if (!props.supplementData) {
      message.error('补签信息不存在');
      return;
    }

    loading.value = true;

    if (formData.approvalResult === 'approve') {
      const approvalForm: SupplementApprovalForm = {
        approvalComment: formData.approvalComment,
      };
      await supplementApi.approveSupplement(props.supplementData.supplementNo, approvalForm);
      message.success('审批通过');
    } else {
      const rejectionForm: SupplementRejectionForm = {
        rejectionReason: formData.approvalComment,
      };
      await supplementApi.rejectSupplement(props.supplementData.supplementNo, rejectionForm);
      message.success('已驳回');
    }

    emit('success');
  } catch (error: any) {
    console.error('审批失败', error);
    if (error.errorFields) {
      return;
    }
    message.error('审批失败');
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
  if (!newVal) {
    resetForm();
  }
});
</script>

<style scoped lang="less">
:deep(.ant-modal-body) {
  padding: 24px;
}

:deep(.ant-descriptions) {
  .ant-descriptions-item-label {
    font-weight: 500;
  }
}
</style>
