<!--
  智能视频-设备表单组件（新增/编辑）

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <a-modal
    :open="visible"
    :title="isEdit ? '编辑设备' : '新增设备'"
    width="900px"
    :confirm-loading="loading"
    :mask-closable="false"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备名称" name="deviceName">
            <a-input
              v-model:value="formData.deviceName"
              placeholder="请输入设备名称"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="设备编码" name="deviceCode">
            <a-input
              v-model:value="formData.deviceCode"
              placeholder="请输入设备编码（国标编码）"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备类型" name="deviceType">
            <a-select
              v-model:value="formData.deviceType"
              placeholder="请选择设备类型"
            >
              <a-select-option value="ipc">网络摄像机</a-select-option>
              <a-select-option value="nvr">NVR</a-select-option>
              <a-select-option value="dvr">DVR</a-select-option>
              <a-select-option value="decoder">解码器</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="厂商" name="manufacturer">
            <a-select
              v-model:value="formData.manufacturer"
              placeholder="请选择厂商"
            >
              <a-select-option value="hikvision">海康威视</a-select-option>
              <a-select-option value="dahua">大华</a-select-option>
              <a-select-option value="uniview">宇视</a-select-option>
              <a-select-option value="tiandy">天地伟业</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="IP地址" name="ipAddress">
            <a-input
              v-model:value="formData.ipAddress"
              placeholder="请输入IP地址"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="端口" name="port">
            <a-input-number
              v-model:value="formData.port"
              placeholder="请输入端口"
              :min="1"
              :max="65535"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="用户名" name="username">
            <a-input
              v-model:value="formData.username"
              placeholder="请输入用户名"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="密码" name="password">
            <a-input-password
              v-model:value="formData.password"
              placeholder="请输入密码"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="所属分组" name="groupId">
            <a-tree-select
              v-model:value="formData.groupId"
              :tree-data="groupTreeData"
              placeholder="请选择所属分组"
              allowClear
              tree-default-expand-all
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="安装位置" name="location">
            <a-input
              v-model:value="formData.location"
              placeholder="请输入安装位置"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="设备描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入设备描述"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="连接测试" v-if="!isEdit">
        <a-button @click="testConnection" :loading="testing">
          <template #icon><ApiOutlined /></template>
          测试连接
        </a-button>
        <span
          v-if="testResult"
          :style="{
            marginLeft: '12px',
            color: testResult.success ? '#52c41a' : '#ff4d4f'
          }"
        >
          {{ testResult.message }}
        </span>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { message } from 'ant-design-vue';
import { ApiOutlined } from '@ant-design/icons-vue';
import deviceMockData from '/@/views/business/smart-video/mock/device-mock-data';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  deviceData: {
    type: Object,
    default: () => ({}),
  },
  groupTreeData: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['update:visible', 'success', 'cancel']);

const formRef = ref();
const loading = ref(false);
const testing = ref(false);
const testResult = ref(null);

const formData = reactive({
  deviceName: '',
  deviceCode: '',
  deviceType: '',
  manufacturer: '',
  ipAddress: '',
  port: 8000,
  username: '',
  password: '',
  groupId: undefined,
  location: '',
  description: '',
});

const formRules = {
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
  ],
  deviceCode: [
    { required: true, message: '请输入设备编码', trigger: 'blur' },
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' },
  ],
  manufacturer: [
    { required: true, message: '请选择厂商', trigger: 'change' },
  ],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    {
      pattern: /^(\d{1,3}\.){3}\d{1,3}$/,
      message: '请输入正确的IP地址',
      trigger: 'blur',
    },
  ],
  port: [
    { required: true, message: '请输入端口', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
};

// 监听编辑数据变化
watch(
  () => props.deviceData,
  (newVal) => {
    if (newVal && Object.keys(newVal).length > 0) {
      Object.assign(formData, newVal);
    }
  },
  { immediate: true, deep: true }
);

// 监听visible变化,重置测试结果
watch(
  () => props.visible,
  (newVal) => {
    if (!newVal) {
      testResult.value = null;
    }
  }
);

// 测试连接
const testConnection = async () => {
  try {
    await formRef.value.validateFields(['ipAddress', 'port', 'username', 'password']);

    testing.value = true;
    testResult.value = null;

    // 使用mock数据测试
    const result = deviceMockData.mockTestConnection({
      ipAddress: formData.ipAddress,
      port: formData.port,
      username: formData.username,
      password: formData.password,
    });

    if (result.code === 1) {
      testResult.value = {
        success: true,
        message: '连接成功！',
      };
      message.success('设备连接测试成功');
    } else {
      testResult.value = {
        success: false,
        message: result.msg || '连接失败',
      };
      message.error('设备连接测试失败');
    }
  } catch (error) {
    console.error('连接测试失败:', error);
    testResult.value = {
      success: false,
      message: '连接测试失败',
    };
  } finally {
    testing.value = false;
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    loading.value = true;

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500));

    message.success(props.isEdit ? '编辑成功' : '新增成功');
    emit('success');
    handleCancel();
  } catch (error) {
    console.error('表单提交失败:', error);
  } finally {
    loading.value = false;
  }
};

// 取消
const handleCancel = () => {
  formRef.value?.resetFields();
  testResult.value = null;
  emit('update:visible', false);
  emit('cancel');
};
</script>

<style scoped>
/* 组件样式 */
</style>
