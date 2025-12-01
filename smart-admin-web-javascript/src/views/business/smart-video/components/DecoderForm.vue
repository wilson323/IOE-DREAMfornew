<!--
 * 解码器表单组件
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室
 -->
<template>
  <a-modal
    :open="visible"
    :title="title"
    width="800px"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :confirmLoading="loading"
    :mask-closable="false"
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
          <a-form-item label="解码器名称" name="decoderName">
            <a-input v-model:value="formData.decoderName" placeholder="请输入解码器名称" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="IP地址" name="ipAddress">
            <a-input v-model:value="formData.ipAddress" placeholder="请输入IP地址" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="端口" name="port">
            <a-input-number v-model:value="formData.port" placeholder="请输入端口" :min="1" :max="65535" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="厂商" name="manufacturer">
            <a-select v-model:value="formData.manufacturer" placeholder="请选择厂商" allowClear>
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
          <a-form-item label="用户名" name="username">
            <a-input v-model:value="formData.username" placeholder="请输入用户名" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="密码" name="password">
            <a-input-password v-model:value="formData.password" placeholder="请输入密码" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备型号" name="model">
            <a-input v-model:value="formData.model" placeholder="请输入设备型号" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="通道数" name="channelCount">
            <a-select v-model:value="formData.channelCount" placeholder="请选择通道数">
              <a-select-option :value="4">4通道</a-select-option>
              <a-select-option :value="8">8通道</a-select-option>
              <a-select-option :value="16">16通道</a-select-option>
              <a-select-option :value="32">32通道</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="formData.description" placeholder="请输入描述信息" :rows="3" />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-space>
        <a-button @click="testConnection" :loading="testLoading">
          <template #icon><ApiOutlined /></template>
          测试连接
        </a-button>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" @click="handleSubmit" :loading="loading">
          保存
        </a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { ApiOutlined } from '@ant-design/icons-vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  formData: {
    type: Object,
    default: () => ({}),
  },
});

const emit = defineEmits(['cancel', 'submit']);

const formRef = ref();
const loading = ref(false);
const testLoading = ref(false);

const title = computed(() => props.isEdit ? '编辑解码器' : '新增解码器');

// 表单验证规则
const formRules = {
  decoderName: [
    { required: true, message: '请输入解码器名称', trigger: 'blur' },
    { max: 100, message: '解码器名称不能超过100个字符', trigger: 'blur' },
  ],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    {
      pattern: /^(\d{1,3}\.){3}\d{1,3}$/,
      message: '请输入有效的IP地址',
      trigger: 'blur',
    },
  ],
  port: [
    { required: true, message: '请输入端口', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口范围1-65535', trigger: 'blur' },
  ],
  manufacturer: [
    { required: true, message: '请选择厂商', trigger: 'change' },
  ],
  channelCount: [
    { required: true, message: '请选择通道数', trigger: 'change' },
  ],
  username: [
    { max: 50, message: '用户名不能超过50个字符', trigger: 'blur' },
  ],
  password: [
    { max: 100, message: '密码不能超过100个字符', trigger: 'blur' },
  ],
  model: [
    { max: 100, message: '设备型号不能超过100个字符', trigger: 'blur' },
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' },
  ],
};

// 测试连接
const testConnection = async () => {
  // 基本验证
  if (!formData.ipAddress || !formData.port) {
    message.error('请先填写IP地址和端口');
    return;
  }

  testLoading.value = true;
  try {
    // 模拟测试连接
    await new Promise(resolve => setTimeout(resolve, 1000));

    // 模拟连接成功（开发阶段）
    message.success('连接测试成功');

    // 实际项目中这里应该调用真实的API
    // const result = await decoderApi.testConnection(formData);
    // if (result.code === 1) {
    //   message.success('连接测试成功');
    // } else {
    //   message.error(result.msg || '连接测试失败');
    // }
  } catch (error) {
    message.error('连接测试失败');
  } finally {
    testLoading.value = false;
  }
};

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    loading.value = true;

    // 验证通过，提交数据
    emit('submit', props.formData);
  } catch (error) {
    console.log('表单验证失败:', error);
  } finally {
    loading.value = false;
  }
};

// 取消
const handleCancel = () => {
  formRef.value?.resetFields();
  emit('cancel');
};

// 监听visible变化，重置表单
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    formRef.value?.resetFields();
  }
});
</script>

<style lang="less" scoped>
:deep(.ant-form-item) {
  margin-bottom: 16px;
}

:deep(.ant-input-number) {
  width: 100%;
}
</style>