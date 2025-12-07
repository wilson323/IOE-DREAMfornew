<!--
  * 审批表单组件
  * 提供任务审批、驳回、转办、委派等功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="approval-form">
    <a-form :model="form" layout="vertical">
      <a-form-item label="审批意见">
        <a-textarea
          v-model:value="form.comment"
          :rows="4"
          placeholder="请输入审批意见（可选）"
          :max-length="500"
          show-count
        />
      </a-form-item>

      <!-- 流程变量设置 -->
      <a-form-item v-if="showVariables" label="流程变量">
        <a-form :model="form.variables" layout="vertical">
          <a-form-item
            v-for="(value, key) in form.variables"
            :key="key"
            :label="key"
          >
            <a-input v-model:value="form.variables[key]" :placeholder="`请输入${key}`" />
          </a-form-item>
        </a-form>
      </a-form-item>

      <!-- 表单数据设置 -->
      <a-form-item v-if="showFormData" label="表单数据">
        <a-form :model="form.formData" layout="vertical">
          <a-form-item
            v-for="(value, key) in form.formData"
            :key="key"
            :label="key"
          >
            <a-input v-model:value="form.formData[key]" :placeholder="`请输入${key}`" />
          </a-form-item>
        </a-form>
      </a-form-item>

      <!-- 操作按钮 -->
      <a-form-item>
        <a-space>
          <a-button type="primary" @click="handleApprove" :loading="loading">
            同意
          </a-button>
          <a-button danger @click="handleReject" :loading="loading">
            驳回
          </a-button>
          <a-button @click="handleTransfer" :loading="loading">
            转办
          </a-button>
          <a-button @click="handleDelegate" :loading="loading">
            委派
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <!-- 转办/委派对话框 -->
    <a-modal
      v-model:open="transferModalVisible"
      :title="transferForm.type === 'transfer' ? '转办任务' : '委派任务'"
      @ok="handleTransferConfirm"
      @cancel="handleTransferCancel"
    >
      <a-form :model="transferForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="目标用户" required>
          <a-select
            v-model:value="transferForm.targetUserId"
            placeholder="请选择目标用户"
            :show-search="true"
            :filter-option="filterUserOption"
          >
            <a-select-option v-for="user in userList" :key="user.userId" :value="user.userId">
              {{ user.userName }} ({{ user.employeeName }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="transferForm.comment" :rows="3" placeholder="请输入备注（可选）" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 驳回对话框 -->
    <a-modal
      v-model:open="rejectModalVisible"
      title="驳回任务"
      @ok="handleRejectConfirm"
      @cancel="handleRejectCancel"
    >
      <a-form :model="rejectForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="驳回意见" required>
          <a-textarea
            v-model:value="rejectForm.comment"
            :rows="4"
            placeholder="请输入驳回意见"
            :max-length="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { employeeApi } from '/@/api/system/employee-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    /**
     * 任务ID
     * @type {Number}
     */
    taskId: {
      type: Number,
      required: true,
    },
    /**
     * 任务详情
     * @type {Object}
     */
    taskDetail: {
      type: Object,
      default: () => ({}),
    },
    /**
     * 是否显示流程变量
     * @type {Boolean}
     */
    showVariables: {
      type: Boolean,
      default: false,
    },
    /**
     * 是否显示表单数据
     * @type {Boolean}
     */
    showFormData: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['submit-success']);

  const workflowStore = useWorkflowStore();
  const loading = ref(false);

  // 表单数据
  const form = reactive({
    comment: '',
    variables: {},
    formData: {},
  });

  // 转办/委派相关
  const transferModalVisible = ref(false);
  const transferForm = reactive({
    type: 'transfer', // transfer 转办, delegate 委派
    targetUserId: null,
    comment: '',
  });
  const userList = ref([]);

  // 驳回相关
  const rejectModalVisible = ref(false);
  const rejectForm = reactive({
    comment: '',
  });

  /**
   * 同意审批
   */
  async function handleApprove() {
    try {
      loading.value = true;
      const params = {
        outcome: '1', // 同意
        comment: form.comment,
        variables: form.variables,
        formData: form.formData,
      };
      const success = await workflowStore.completeTask(props.taskId, params);
      if (success) {
        message.success('审批成功');
        emit('submit-success');
      }
    } catch (err) {
      console.error('审批失败:', err);
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 驳回
   */
  function handleReject() {
    rejectForm.comment = '';
    rejectModalVisible.value = true;
  }

  /**
   * 确认驳回
   */
  async function handleRejectConfirm() {
    if (!rejectForm.comment) {
      message.warning('请输入驳回意见');
      return;
    }

    try {
      loading.value = true;
      const params = {
        comment: rejectForm.comment,
        variables: form.variables,
      };
      const success = await workflowStore.rejectTask(props.taskId, params);
      if (success) {
        message.success('驳回成功');
        handleRejectCancel();
        emit('submit-success');
      }
    } catch (err) {
      console.error('驳回失败:', err);
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 取消驳回
   */
  function handleRejectCancel() {
    rejectModalVisible.value = false;
    rejectForm.comment = '';
  }

  /**
   * 转办
   */
  function handleTransfer() {
    transferForm.type = 'transfer';
    transferForm.targetUserId = null;
    transferForm.comment = '';
    transferModalVisible.value = true;
    loadUserList();
  }

  /**
   * 委派
   */
  function handleDelegate() {
    transferForm.type = 'delegate';
    transferForm.targetUserId = null;
    transferForm.comment = '';
    transferModalVisible.value = true;
    loadUserList();
  }

  /**
   * 确认转办/委派
   */
  async function handleTransferConfirm() {
    if (!transferForm.targetUserId) {
      message.warning('请选择目标用户');
      return;
    }

    try {
      loading.value = true;
      let success = false;
      if (transferForm.type === 'transfer') {
        success = await workflowStore.transferTask(props.taskId, transferForm.targetUserId);
      } else {
        success = await workflowStore.delegateTask(props.taskId, transferForm.targetUserId);
      }

      if (success) {
        message.success(transferForm.type === 'transfer' ? '转办成功' : '委派成功');
        handleTransferCancel();
        emit('submit-success');
      }
    } catch (err) {
      console.error('转办/委派失败:', err);
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 取消转办/委派
   */
  function handleTransferCancel() {
    transferModalVisible.value = false;
    transferForm.targetUserId = null;
    transferForm.comment = '';
  }

  /**
   * 加载用户列表
   */
  async function loadUserList() {
    try {
      const result = await employeeApi.queryAll();
      if (result.code === 1 || result.code === 200) {
        // 将员工数据转换为用户列表格式
        userList.value = (result.data || []).map((employee) => ({
          userId: employee.employeeId,
          userName: employee.loginName || employee.actualName,
          employeeName: employee.actualName,
        }));
      }
    } catch (err) {
      console.error('加载用户列表失败:', err);
      smartSentry.captureError(err);
      message.error('加载用户列表失败');
    }
  }

  /**
   * 用户选择过滤
   * @param {String} input - 输入值
   * @param {Object} option - 选项
   * @returns {Boolean}
   */
  function filterUserOption(input, option) {
    return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  }
</script>

<style lang="less" scoped>
  .approval-form {
    padding: 16px;
  }
</style>

