<!--
  @fileoverview 请假审批对话框
  @author IOE-DREAM Team
  @description 请假审批表单
-->
<template>
  <a-modal
    :visible="visible"
    title="请假审批"
    :width="700"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <!-- 请假信息展示 -->
    <a-descriptions title="请假信息" bordered :column="2" size="small" style="margin-bottom: 16px">
      <a-descriptions-item label="员工姓名">
        {{ leaveData?.employeeName }}
      </a-descriptions-item>
      <a-descriptions-item label="部门">
        {{ leaveData?.departmentName }}
      </a-descriptions-item>
      <a-descriptions-item label="请假类型">
        <a-tag :color="getLeaveTypeColor(leaveData?.leaveType)">
          {{ getLeaveTypeName(leaveData?.leaveType) }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="请假天数">
        {{ leaveData?.leaveDays }} 天
      </a-descriptions-item>
      <a-descriptions-item label="开始日期">
        {{ leaveData?.startDate }}
      </a-descriptions-item>
      <a-descriptions-item label="结束日期">
        {{ leaveData?.endDate }}
      </a-descriptions-item>
      <a-descriptions-item label="申请时间" :span="2">
        {{ leaveData?.createTime }}
      </a-descriptions-item>
      <a-descriptions-item label="请假原因" :span="2">
        {{ leaveData?.reason }}
      </a-descriptions-item>
      <a-descriptions-item v-if="leaveData?.remark" label="备注" :span="2">
        {{ leaveData?.remark }}
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

      <!-- 提示信息 -->
      <a-alert
        v-if="formData.approvalResult === 'reject'"
        message="驳回说明"
        description="驳回后，员工将收到通知，可以根据驳回原因重新提交请假申请。"
        type="warning"
        show-icon
        style="margin-top: 8px"
      />
      <a-alert
        v-if="formData.approvalResult === 'approve'"
        message="通过说明"
        description="通过后，请假申请将被记录为已批准状态，员工可以正常休假。"
        type="success"
        show-icon
        style="margin-top: 8px"
      />
    </a-form>

    <!-- 历史审批记录 -->
    <a-divider style="margin: 16px 0">审批历史</a-divider>
    <a-timeline v-if="approvalHistory.length > 0">
      <a-timeline-item v-for="record in approvalHistory" :key="record.approvalId">
        <template #dot>
          <CheckCircleOutlined v-if="record.approvalResult === 'APPROVED'" style="color: #52c41a; font-size: 16px" />
          <CloseCircleOutlined v-else-if="record.approvalResult === 'REJECTED'" style="color: #f5222d; font-size: 16px" />
          <ClockCircleOutlined v-else style="color: #faad14; font-size: 16px" />
        </template>
        <div>
          <div style="font-weight: 500">{{ record.approverName }}</div>
          <div style="color: #666; font-size: 12px; margin-top: 4px">
            {{ record.approvalStep }} · {{ record.approvalTime }}
          </div>
          <div v-if="record.approvalComment" style="color: #999; font-size: 12px; margin-top: 4px">
            {{ record.approvalComment }}
          </div>
          <div v-if="record.isProxyApproval" style="color: #1890ff; font-size: 12px; margin-top: 4px">
            代理审批：{{ record.proxyApproverName }}
          </div>
        </div>
      </a-timeline-item>
    </a-timeline>
    <a-empty v-else description="暂无审批历史" :image-style="{ height: '60px' }" />
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue';
import { leaveApi, LeaveApprovalForm, LeaveRejectionForm, LeaveType, LeaveRecordVO, ApprovalRecord } from '@/api/business/attendance/leave';

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
  approvalResult: 'approve' as 'approve' | 'reject',
  approvalComment: '',
});

/**
 * 加载状态
 */
const loading = ref(false);

/**
 * 审批历史
 */
const approvalHistory = ref<ApprovalRecord[]>([]);

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
 * 获取请假类型名称
 */
const getLeaveTypeName = (type?: LeaveType) => {
  if (!type) return '';
  const map: Record<LeaveType, string> = {
    ANNUAL: '年假',
    SICK: '病假',
    PERSONAL: '事假',
    MARRIAGE: '婚假',
    MATERNITY: '产假',
    PATERNITY: '陪产假',
    OTHER: '其他',
  };
  return map[type] || type;
};

/**
 * 获取请假类型颜色
 */
const getLeaveTypeColor = (type?: LeaveType) => {
  if (!type) return 'default';
  const map: Record<LeaveType, string> = {
    ANNUAL: 'blue',
    SICK: 'orange',
    PERSONAL: 'red',
    MARRIAGE: 'pink',
    MATERNITY: 'purple',
    PATERNITY: 'cyan',
    OTHER: 'default',
  };
  return map[type] || 'default';
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
  approvalHistory.value = [];
};

/**
 * 提交审批
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    if (!props.leaveData) {
      message.error('请假信息不存在');
      return;
    }

    loading.value = true;

    if (formData.approvalResult === 'approve') {
      // 通过
      const approvalForm: LeaveApprovalForm = {
        approvalComment: formData.approvalComment,
      };
      await leaveApi.approveLeave(props.leaveData.id, approvalForm);
      message.success('审批通过');
    } else {
      // 驳回
      const rejectionForm: LeaveRejectionForm = {
        rejectionReason: formData.approvalComment,
      };
      await leaveApi.rejectLeave(props.leaveData.id, rejectionForm);
      message.success('已驳回');
    }

    emit('success');
  } catch (error: any) {
    console.error('审批失败', error);
    if (error.errorFields) {
      // 表单验证失败
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
  if (newVal && props.leaveData) {
    // TODO: 加载审批历史
    // 审批历史应该从详情接口获取
    approvalHistory.value = [];
  } else {
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

:deep(.ant-timeline) {
  margin-top: 16px;
}
</style>
