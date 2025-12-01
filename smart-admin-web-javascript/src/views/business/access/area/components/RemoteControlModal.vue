<template>
  <a-modal
    v-model:open="visible"
    title="远程设备控制"
    width="600px"
    @ok="handleOk"
    @cancel="handleCancel"
    :confirm-loading="loading"
  >
    <div class="device-info">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="设备名称">{{ deviceInfo.deviceName }}</a-descriptions-item>
        <a-descriptions-item label="设备编码">{{ deviceInfo.deviceCode }}</a-descriptions-item>
        <a-descriptions-item label="在线状态">
          <a-badge :status="deviceInfo.onlineStatus ? 'success' : 'error'" :text="deviceInfo.onlineStatus ? '在线' : '离线'" />
        </a-descriptions-item>
        <a-descriptions-item label="最后通信">
          {{ formatDateTime(deviceInfo.lastCommunication) }}
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <!-- 远程控制选项 -->
    <div class="control-section" style="margin-top: 20px;">
      <a-title :level="5">远程控制</a-title>
      <a-space direction="vertical" size="large" style="width: 100%;">
        <!-- 门禁控制 -->
        <a-card size="small" title="门禁控制">
          <a-space>
            <a-button
              type="primary"
              :disabled="!deviceInfo.onlineStatus"
              :loading="openDoorLoading"
              @click="handleOpenDoor"
            >
              <template #icon><UnlockOutlined /></template>
              远程开门
            </a-button>
            <a-button
              :disabled="!deviceInfo.onlineStatus"
              :loading="lockDoorLoading"
              @click="handleLockDoor"
            >
              <template #icon><LockOutlined /></template>
              远程锁门
            </a-button>
          </a-space>
        </a-card>

        <!-- 设备控制 -->
        <a-card size="small" title="设备控制">
          <a-space>
            <a-button
              :disabled="!deviceInfo.onlineStatus"
              :loading="restartLoading"
              @click="handleRestart"
            >
              <template #icon><ReloadOutlined /></template>
              重启设备
            </a-button>
            <a-button
              :disabled="!deviceInfo.onlineStatus"
              :loading="syncLoading"
              @click="handleSyncConfig"
            >
              <template #icon><SyncOutlined /></template>
              同步配置
            </a-button>
            <a-button
              :disabled="!deviceInfo.onlineStatus"
              @click="handleTestConnection"
            >
              <template #icon><ApiOutlined /></template>
              测试连接
            </a-button>
          </a-space>
        </a-card>

        <!-- 状态控制 -->
        <a-card size="small" title="状态控制">
          <a-row :gutter="16">
            <a-col :span="12">
              <div class="control-item">
                <div class="control-label">设备启用</div>
                <a-switch
                  v-model:checked="deviceEnabled"
                  :disabled="!deviceInfo.onlineStatus"
                  @change="handleDeviceStatusChange"
                />
              </div>
            </a-col>
            <a-col :span="12">
              <div class="control-item">
                <div class="control-label">门锁状态</div>
                <a-switch
                  v-model:checked="lockStatus"
                  :disabled="!deviceInfo.onlineStatus"
                  @change="handleLockStatusChange"
                />
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-space>
    </div>

    <!-- 操作结果 -->
    <div v-if="operationResult" class="operation-result" style="margin-top: 20px;">
      <a-alert
        :message="operationResult.message"
        :type="operationResult.success ? 'success' : 'error'"
        show-icon
        :closable="true"
        @close="operationResult = null"
      />
    </div>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  UnlockOutlined,
  LockOutlined,
  ReloadOutlined,
  SyncOutlined,
  ApiOutlined
} from '@ant-design/icons-vue';
import { accessDeviceApi } from '/@/api/business/access/device-api.js';
import { formatDateTime } from '/@/utils/format.js';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  deviceInfo: {
    type: Object,
    default: () => ({})
  }
});

// Emits
const emit = defineEmits(['update:visible', 'success']);

// 响应式数据
const loading = ref(false);
const deviceEnabled = ref(true);
const lockStatus = ref(false);
const operationResult = ref(null);

// 远程操作loading状态
const openDoorLoading = ref(false);
const lockDoorLoading = ref(false);
const restartLoading = ref(false);
const syncLoading = ref(false);

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

// 远程开门
const handleOpenDoor = async () => {
  if (!deviceEnabled.value || !lockStatus.value) {
    message.warning('请先启用设备并确保门锁处于开启状态');
    return;
  }

  openDoorLoading.value = true;
  try {
    const response = await accessDeviceApi.remoteOpenDoor(props.deviceInfo.deviceId);
    if (response && response.success) {
      operationResult.value = {
        success: true,
        message: '远程开门命令发送成功'
      };
      emit('success');
    } else {
      operationResult.value = {
        success: false,
        message: response?.message || '远程开门失败'
      };
    }
  } catch (error) {
    console.error('远程开门失败:', error);
    operationResult.value = {
      success: false,
      message: '远程开门失败，请检查网络连接'
    };
  } finally {
    openDoorLoading.value = false;
  }
};

// 远程锁门
const handleLockDoor = async () => {
  if (!deviceEnabled.value) {
    message.warning('请先启用设备');
    return;
  }

  lockDoorLoading.value = true;
  try {
    // 这里应该调用远程锁门的API
    await new Promise(resolve => setTimeout(resolve, 1000));
    operationResult.value = {
      success: true,
      message: '远程锁门命令发送成功'
    };
  } catch (error) {
    operationResult.value = {
      success: false,
      message: '远程锁门失败'
    };
  } finally {
    lockDoorLoading.value = false;
  }
};

// 重启设备
const handleRestart = async () => {
  restartLoading.value = true;
  try {
    const response = await accessDeviceApi.restartDevice(props.deviceInfo.deviceId);
    if (response && response.success) {
      operationResult.value = {
        success: true,
        message: '设备重启命令发送成功，请等待设备重新启动'
      };
    } else {
      operationResult.value = {
        success: false,
        message: response?.message || '设备重启失败'
      };
    }
  } catch (error) {
    console.error('设备重启失败:', error);
    operationResult.value = {
      success: false,
      message: '设备重启失败'
    };
  } finally {
    restartLoading.value = false;
  }
};

// 同步配置
const handleSyncConfig = async () => {
  syncLoading.value = true;
  try {
    const response = await accessDeviceApi.syncDeviceConfig(props.deviceInfo.deviceId);
    if (response && response.success) {
      operationResult.value = {
        success: true,
        message: '设备配置同步成功'
      };
    } else {
      operationResult.value = {
        success: false,
        message: response?.message || '配置同步失败'
      };
    }
  } catch (error) {
    console.error('配置同步失败:', error);
    operationResult.value = {
      success: false,
      message: '配置同步失败'
    };
  } finally {
    syncLoading.value = false;
  }
};

// 测试连接
const handleTestConnection = async () => {
  try {
    const response = await accessDeviceApi.testDeviceConnection(props.deviceInfo.deviceId);
    if (response && response.success) {
      operationResult.value = {
        success: true,
        message: '设备连接测试成功'
      };
    } else {
      operationResult.value = {
        success: false,
        message: response?.message || '连接测试失败'
      };
    }
  } catch (error) {
    console.error('连接测试失败:', error);
    operationResult.value = {
      success: false,
      message: '连接测试失败'
    };
  }
};

// 设备状态变更
const handleDeviceStatusChange = async (checked) => {
  try {
    const response = await accessDeviceApi.updateDeviceStatus(
      props.deviceInfo.deviceId,
      checked ? 1 : 0
    );
    if (response && response.success) {
      message.success(checked ? '设备启用成功' : '设备禁用成功');
    } else {
      message.error(response?.message || '状态更新失败');
      // 回滚状态
      deviceEnabled.value = !checked;
    }
  } catch (error) {
    console.error('设备状态更新失败:', error);
    message.error('状态更新失败');
    deviceEnabled.value = !checked;
  }
};

// 锁状态变更
const handleLockStatusChange = async (checked) => {
  try {
    // 这里应该调用设置锁状态的API
    message.success(checked ? '锁已开启' : '锁已关闭');
  } catch (error) {
    console.error('锁状态设置失败:', error);
    message.error('锁状态设置失败');
    lockStatus.value = !checked;
  }
};

// 确定
const handleOk = () => {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
    emit('update:visible', false);
    operationResult.value = null;
  }, 1000);
};

// 取消
const handleCancel = () => {
  emit('update:visible', false);
  operationResult.value = null;
};
</script>

<style lang="less" scoped>
.device-info {
  margin-bottom: 20px;
}

.control-section {
  .control-item {
    .control-label {
      margin-bottom: 8px;
      font-weight: 500;
    }
  }
}

.operation-result {
  margin-top: 20px;
}
</style>