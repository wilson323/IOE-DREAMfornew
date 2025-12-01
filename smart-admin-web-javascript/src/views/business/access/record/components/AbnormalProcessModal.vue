<!--
  * 异常记录处理弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="处理异常记录"
    :width="600"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <div v-if="record" class="abnormal-process">
      <!-- 异常信息摘要 -->
      <a-alert
        :message="`异常记录 - ${record.userName} - ${formatDateTime(record.accessTime)}`"
        :description="`异常类型: ${getAbnormalTypeText(record.abnormalType)} - ${record.abnormalReason || '未知原因'}`"
        type="warning"
        show-icon
        class="abnormal-alert"
      />

      <!-- 记录基本信息 -->
      <a-card title="记录信息" class="info-card" size="small">
        <a-descriptions :column="2" size="small" bordered>
          <a-descriptions-item label="用户姓名">
            {{ record.userName }}
          </a-descriptions-item>
          <a-descriptions-item label="卡号">
            {{ record.userCardNumber }}
          </a-descriptions-item>
          <a-descriptions-item label="设备位置">
            {{ record.location }}
          </a-descriptions-item>
          <a-descriptions-item label="通行结果">
            <a-tag
              :color="getResultColor(record.accessResult)"
            >
              {{ getResultText(record.accessResult) }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 处理表单 -->
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        class="process-form"
      >
        <a-form-item label="处理动作" name="action">
          <a-radio-group v-model:value="formData.action">
            <a-radio value="resolve">解决异常</a-radio>
            <a-radio value="ignore">忽略异常</a-radio>
            <a-radio value="investigate">进一步调查</a-radio>
            <a-radio value="escalate">上报处理</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="处理结果" name="result">
          <a-select
            v-model:value="formData.result"
            placeholder="请选择处理结果"
          >
            <a-select-option value="verified">验证通过</a-select-option>
            <a-select-option value="corrected">已修正</a-select-option>
            <a-select-option value="ignored">已忽略</a-select-option>
            <a-select-option value="pending">待处理</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="处理说明" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            placeholder="请输入处理说明"
            :rows="4"
            :max-length="500"
            show-count
          />
        </a-form-item>

        <!-- 解决异常时的额外选项 -->
        <template v-if="formData.action === 'resolve'">
          <a-form-item label="补偿措施" name="compensation">
            <a-checkbox-group v-model:value="formData.compensation">
              <a-checkbox value="manual_access">手动开通通行</a-checkbox>
              <a-checkbox value="update_card">更新卡片信息</a-checkbox>
              <a-checkbox value="adjust_device">调整设备设置</a-checkbox>
              <a-checkbox value="notify_user">通知用户</a-checkbox>
            </a-checkbox-group>
          </a-form-item>
        </template>

        <!-- 进一步调查时的选项 -->
        <template v-if="formData.action === 'investigate'">
          <a-form-item label="调查方向" name="investigation">
            <a-checkbox-group v-model:value="formData.investigation">
              <a-checkbox value="user_info">核实用户身份</a-checkbox>
              <a-checkbox value="card_status">检查卡片状态</a-checkbox>
              <a-checkbox value="device_log">查看设备日志</a-checkbox>
              <a-checkbox value="access_history">查询历史记录</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="后续跟进" name="followup">
            <a-select
              v-model:value="formData.followup"
              placeholder="请选择后续跟进方式"
            >
              <a-select-option value="contact_user">联系用户</a-select-option>
              <a-select-option value="check_device">检查设备</a-checkbox>
              <a-select-option value="monitor">持续监控</a-checkbox>
              <a-select-option value="none">无需跟进</a-checkbox>
            </a-select>
          </a-form-item>
        </template>

        <!-- 上报处理时的选项 -->
        <template v-if="formData.action === 'escalate'">
          <a-form-item label="上报对象" name="escalateTo">
            <a-select
              v-model:value="formData.escalateTo"
              placeholder="请选择上报对象"
            >
              <a-select-option value="security">安保部门</a-checkbox>
              <a-select-option value="it">IT部门</a-checkbox>
              <a-select-option value="admin">系统管理员</a-checkbox>
              <a-select-option value="supervisor">主管</a-checkbox>
            </a-select>
          </a-form-item>

          <a-form-item label="紧急程度" name="urgency">
            <a-radio-group v-model:value="formData.urgency">
              <a-radio value="low">低</a-radio>
              <a-radio value="medium">中</a-radio>
              <a-radio value="high">高</a-radio>
              <a-radio value="urgent">紧急</a-radio>
            </a-radio-group>
          </a-form-item>
        </template>

        <!-- 通用选项 -->
        <a-form-item label="处理时间" name="processTime">
          <a-date-picker
            v-model:value="formData.processTime"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择处理时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="通知设置" name="notifications">
          <a-checkbox-group v-model:value="formData.notifications">
            <a-checkbox value="user">通知用户</a-checkbox>
            <a-checkbox value="manager">通知管理员</a-checkbox>
            <a-checkbox value="security">通知安保</a-checkbox>
          </a-checkbox-group>
        </a-form-item>

        <!-- 处理确认 -->
        <a-form-item label="确认信息" name="confirmation">
          <a-checkbox v-model:checked="formData.confirmation">
            我确认以上处理信息准确无误
          </a-checkbox>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { accessRecordApi } from '/@/api/business/access/record-api';
  import { formatDateTime } from '/@/lib/format';

  // Props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    record: {
      type: Object,
      default: null,
    },
  });

  // Emits
  const emit = defineEmits(['update:visible', 'success']);

  // 响应式数据
  const formRef = ref();
  const loading = ref(false);

  // 表单数据
  const formData = reactive({
    action: 'resolve',
    result: undefined,
    remark: '',
    compensation: [],
    investigation: [],
    followup: undefined,
    escalateTo: undefined,
    urgency: 'medium',
    processTime: null,
    notifications: [],
    confirmation: false,
  });

  // 表单验证规则
  const formRules = computed(() => {
    const rules = {
      action: [
        { required: true, message: '请选择处理动作', trigger: 'change' },
      ],
      remark: [
        { required: true, message: '请输入处理说明', trigger: 'blur' },
        { min: 10, message: '处理说明至少10个字符', trigger: 'blur' },
      ],
      confirmation: [
        {
          validator: (rule, value) => {
            if (!value) {
              return Promise.reject('请确认处理信息');
            }
            return Promise.resolve();
          },
          trigger: 'change',
        },
      ],
    };

    // 根据处理动作添加相应的验证规则
    if (formData.action === 'resolve') {
      rules.result = [
        { required: true, message: '请选择处理结果', trigger: 'change' },
      ];
    }

    if (formData.action === 'escalate') {
      rules.escalateTo = [
        { required: true, message: '请选择上报对象', trigger: 'change' },
      ];
    }

    return rules;
  });

  // 计算属性
  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  // 监听处理动作变化
  watch(() => formData.action, (newAction) => {
    // 重置表单数据
    if (newAction === 'resolve') {
      formData.result = 'verified';
      formData.compensation = [];
      formData.investigation = [];
      formData.followup = undefined;
      formData.escalateTo = undefined;
      formData.urgency = 'medium';
    } else if (newAction === 'ignore') {
      formData.result = 'ignored';
      formData.compensation = [];
      formData.investigation = [];
      formData.followup = 'none';
      formData.escalateTo = undefined;
    } else if (newAction === 'investigate') {
      formData.result = 'pending';
      formData.compensation = [];
      formData.investigation = [];
      formData.followup = undefined;
      formData.escalateTo = undefined;
    } else if (newAction === 'escalate') {
      formData.result = 'pending';
      formData.compensation = [];
      formData.investigation = [];
      formData.followup = undefined;
      formData.escalateTo = undefined;
      formData.urgency = 'medium';
    }
    formData.confirmation = false;
  });

  // 方法
  const handleSubmit = async () => {
    try {
      await formRef.value.validate();

      if (!formData.confirmation) {
        message.error('请确认处理信息');
        return;
      }

      loading.value = true;

      // 构建处理数据
      const processData = {
        recordId: props.record.recordId,
        action: formData.action,
        result: formData.result,
        remark: formData.remark,
        processTime: formData.processTime ? formData.processTime.format('YYYY-MM-DD HH:mm:ss') : null,
        notifications: formData.notifications,
        ...buildActionSpecificData(),
      };

      // 调用处理API
      const response = await accessRecordApi.handleAbnormalRecord(processData);

      if (response.code === 1) {
        message.success('异常记录处理成功');
        emit('success');
        handleCancel();
      } else {
        message.error(response.message || '处理失败');
      }
    } catch (error) {
      console.error('处理异常记录失败:', error);
      message.error('处理失败，请稍后重试');
    } finally {
      loading.value = false;
    }
  };

  const buildActionSpecificData = () => {
    const data = {};

    if (formData.action === 'resolve') {
      data.compensation = formData.compensation;
    } else if (formData.action === 'investigate') {
      data.investigation = formData.investigation;
      data.followup = formData.followup;
    } else if (formData.action === 'escalate') {
      data.escalateTo = formData.escalateTo;
      data.urgency = formData.urgency;
    }

    return data;
  };

  const handleCancel = () => {
    visible.value = false;
    resetForm();
  };

  const resetForm = () => {
    Object.assign(formData, {
      action: 'resolve',
      result: undefined,
      remark: '',
      compensation: [],
      investigation: [],
      followup: undefined,
      escalateTo: undefined,
      urgency: 'medium',
      processTime: null,
      notifications: [],
      confirmation: false,
    });

    if (formRef.value) {
      formRef.value.clearValidate();
    }
  };

  // 辅助方法
  const getAbnormalTypeText = (type) => {
    const typeMap = {
      verification_failed: '验证失败',
      invalid_card: '无效卡片',
      expired_card: '卡片过期',
      blacklisted: '黑名单用户',
      access_denied: '拒绝访问',
      device_error: '设备异常',
      network_error: '网络异常',
    };
    return typeMap[type] || type;
  };

  const getResultColor = (result) => {
    const colorMap = {
      success: 'green',
      failed: 'red',
      denied: 'orange',
      timeout: 'default',
    };
    return colorMap[result] || 'default';
  };

  const getResultText = (result) => {
    const textMap = {
      success: '成功',
      failed: '失败',
      denied: '拒绝',
      timeout: '超时',
    };
    return textMap[result] || result;
  };
</script>

<style lang="less" scoped>
  .abnormal-process {
    .abnormal-alert {
      margin-bottom: 16px;
    }

    .info-card {
      margin-bottom: 24px;
    }

    .process-form {
      .form-help {
        margin-left: 8px;
        color: #8c8c8c;
        font-size: 12px;
      }
    }
  }
</style>