<!--
  * 批量状态更新弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    title="批量更新设备状态"
    :width="600"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <div class="batch-update-content">
      <!-- 设备数量提示 -->
      <a-alert
        :message="`已选择 ${deviceIds.length} 个设备`"
        type="info"
        show-icon
        class="batch-alert"
      />

      <!-- 设备列表 -->
      <div class="device-list" v-if="selectedDevices.length > 0">
        <h4>选中的设备：</h4>
        <div class="device-tags">
          <a-tag
            v-for="device in selectedDevices"
            :key="device.deviceId"
            color="blue"
            class="device-tag"
          >
            {{ device.deviceName }} ({{ device.deviceCode }})
          </a-tag>
        </div>
      </div>

      <!-- 状态更新表单 -->
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        class="update-form"
      >
        <a-form-item label="更新类型" name="updateType">
          <a-radio-group :value="formData.updateType" @update:value="val => emit('update:value', val)">
            <a-radio value="status">设备状态</a-radio>
            <a-radio value="config">同步配置</a-radio>
            <a-radio value="restart">重启设备</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 设备状态更新 -->
        <template v-if="formData.updateType === 'status'">
          <a-form-item label="设备状态" name="status">
            <a-select
              :value="formData.status" @update:value="val => emit('update:value', val)"
              placeholder="请选择设备状态"
            >
              <a-select-option value="normal">正常</a-select-option>
              <a-select-option value="maintenance">维护中</a-select-option>
              <a-select-option value="fault">故障</a-select-option>
              <a-select-option value="offline">离线</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="状态说明" name="statusRemark">
            <a-textarea
              :value="formData.statusRemark" @update:value="val => emit('update:value', val)"
              placeholder="请输入状态变更说明"
              :rows="3"
              :max-length="200"
              show-count
            />
          </a-form-item>
        </template>

        <!-- 配置同步 -->
        <template v-if="formData.updateType === 'config'">
          <a-form-item label="同步选项" name="syncOptions">
            <a-checkbox-group :value="formData.syncOptions" @update:value="val => emit('update:value', val)">
              <a-checkbox value="basic">基本配置</a-checkbox>
              <a-checkbox value="time">时间策略</a-checkbox>
              <a-checkbox value="permission">权限配置</a-checkbox>
              <a-checkbox value="firmware">固件更新</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="强制同步" name="forceSync">
            <a-switch :checked="formData.forceSync" @update:checked="val => emit('update:checked', val)" />
            <span class="form-help">强制同步会覆盖设备现有配置</span>
          </a-form-item>
        </template>

        <!-- 设备重启 -->
        <template v-if="formData.updateType === 'restart'">
          <a-form-item label="重启方式" name="restartType">
            <a-radio-group :value="formData.restartType" @update:value="val => emit('update:value', val)">
              <a-radio value="soft">软重启</a-radio>
              <a-radio value="hard">硬重启</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item label="重启延时" name="restartDelay">
            <a-input-number
              :value="formData.restartDelay" @update:value="val => emit('update:value', val)"
              :min="0"
              :max="300"
              placeholder="秒"
              style="width: 200px"
            />
            <span class="form-help">设备将在指定秒数后重启</span>
          </a-form-item>
        </template>

        <!-- 操作确认 -->
        <a-form-item label="操作确认" name="confirm">
          <a-checkbox :checked="formData.confirm" @update:checked="val => emit('update:checked', val)">
            我确认要执行此批量操作
          </a-checkbox>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { useAccessDeviceStore } from '/@/store/modules/business/access-device';

  // Props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    deviceIds: {
      type: Array,
      default: () => [],
    },
  });

  // Emits
  const emit = defineEmits(['update:visible', 'success']);

  // 状态管理
  const deviceStore = useAccessDeviceStore();

  // 响应式数据
  const formRef = ref();
  const loading = ref(false);

  // 表单数据
  const formData = reactive({
    updateType: 'status',
    status: undefined,
    statusRemark: '',
    syncOptions: ['basic'],
    forceSync: false,
    restartType: 'soft',
    restartDelay: 5,
    confirm: false,
  });

  // 表单验证规则
  const formRules = computed(() => {
    const rules = {
      updateType: [
        { required: true, message: '请选择更新类型', trigger: 'change' },
      ],
      confirm: [
        {
          validator: (rule, value) => {
            if (!value) {
              return Promise.reject('请确认操作');
            }
            return Promise.resolve();
          },
          trigger: 'change',
        },
      ],
    };

    // 根据更新类型添加相应的验证规则
    if (formData.updateType === 'status') {
      rules.status = [
        { required: true, message: '请选择设备状态', trigger: 'change' },
      ];
    }

    if (formData.updateType === 'config') {
      rules.syncOptions = [
        {
          validator: (rule, value) => {
            if (!value || value.length === 0) {
              return Promise.reject('请选择至少一个同步选项');
            }
            return Promise.resolve();
          },
          trigger: 'change',
        },
      ];
    }

    return rules;
  });

  // 计算属性
  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  const selectedDevices = computed(() => {
    return deviceStore.deviceList.filter(device =>
      props.deviceIds.includes(device.deviceId)
    );
  });

  // 监听更新类型变化
  watch(() => formData.updateType, (newType) => {
    // 重置表单数据
    if (newType === 'status') {
      formData.status = undefined;
      formData.statusRemark = '';
    } else if (newType === 'config') {
      formData.syncOptions = ['basic'];
      formData.forceSync = false;
    } else if (newType === 'restart') {
      formData.restartType = 'soft';
      formData.restartDelay = 5;
    }
    formData.confirm = false;
  });

  // 方法
  const handleSubmit = async () => {
    try {
      await formRef.value.validate();

      // 二次确认
      const confirmMessage = getConfirmMessage();
      Modal.confirm({
        title: '批量操作确认',
        content: confirmMessage,
        okText: '确认执行',
        cancelText: '取消',
        onOk: async () => {
          await executeBatchUpdate();
        },
      });
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  const getConfirmMessage = () => {
    const deviceCount = props.deviceIds.length;
    const typeMap = {
      status: `将 ${deviceCount} 个设备的状态更新为 ${getStatusText(formData.status)}`,
      config: `将向 ${deviceCount} 个设备同步配置`,
      restart: `将重启 ${deviceCount} 个设备`,
    };
    return typeMap[formData.updateType] || '执行批量操作';
  };

  const executeBatchUpdate = async () => {
    loading.value = true;
    try {
      let success = false;
      const promises = [];

      // 根据更新类型执行不同的操作
      if (formData.updateType === 'status') {
        // 批量更新设备状态
        for (const deviceId of props.deviceIds) {
          promises.push(deviceStore.updateDeviceStatus(deviceId, formData.status));
        }
        await Promise.all(promises);
        success = true;
      } else if (formData.updateType === 'config') {
        // 批量同步配置
        for (const deviceId of props.deviceIds) {
          promises.push(deviceStore.syncDeviceConfig(deviceId));
        }
        await Promise.all(promises);
        success = true;
      } else if (formData.updateType === 'restart') {
        // 批量重启设备
        for (const deviceId of props.deviceIds) {
          promises.push(deviceStore.restartDevice(deviceId));
        }
        await Promise.all(promises);
        success = true;
      }

      if (success) {
        message.success('批量操作执行成功');
        emit('success');
        handleCancel();
      }
    } catch (error) {
      console.error('批量操作失败:', error);
      message.error('批量操作执行失败');
    } finally {
      loading.value = false;
    }
  };

  const handleCancel = () => {
    visible.value = false;
    resetForm();
  };

  const resetForm = () => {
    Object.assign(formData, {
      updateType: 'status',
      status: undefined,
      statusRemark: '',
      syncOptions: ['basic'],
      forceSync: false,
      restartType: 'soft',
      restartDelay: 5,
      confirm: false,
    });

    if (formRef.value) {
      formRef.value.clearValidate();
    }
  };

  const getStatusText = (status) => {
    const textMap = {
      normal: '正常',
      maintenance: '维护中',
      fault: '故障',
      offline: '离线',
    };
    return textMap[status] || status;
  };
</script>

<style lang="less" scoped>
  .batch-update-content {
    .batch-alert {
      margin-bottom: 16px;
    }

    .device-list {
      margin-bottom: 24px;

      h4 {
        margin: 0 0 12px 0;
        font-size: 14px;
        font-weight: 600;
        color: #262626;
      }

      .device-tags {
        max-height: 120px;
        overflow-y: auto;
        border: 1px solid #f0f0f0;
        border-radius: 6px;
        padding: 12px;
        background-color: #fafafa;

        .device-tag {
          margin: 4px 8px 4px 0;
        }
      }
    }

    .update-form {
      .form-help {
        margin-left: 8px;
        color: #8c8c8c;
        font-size: 12px;
      }
    }
  }
</style>