<!--
  * 设备表单弹窗组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :open="visible" @update:open="val => emit('update:visible', val)"
    :title="title"
    :width="800"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备编码" name="deviceCode">
            <a-input
              :value="formData.deviceCode" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备编码"
              :disabled="mode === 'edit'"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="设备名称" name="deviceName">
            <a-input
              :value="formData.deviceName" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备名称"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="设备类型" name="deviceType">
            <a-select
              :value="formData.deviceType" @update:value="val => emit('update:value', val)"
              placeholder="请选择设备类型"
            >
              <a-select-option value="door_controller">门禁控制器</a-select-option>
              <a-select-option value="turnstile">闸机</a-select-option>
              <a-select-option value="gate">门禁门</a-select-option>
              <a-select-option value="barrier">道闸</a-select-option>
              <a-select-option value="elevator">电梯</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="所属区域" name="areaId">
            <a-tree-select
              :value="formData.areaId" @update:value="val => emit('update:value', val)"
              :tree-data="areaTreeData"
              placeholder="请选择所属区域"
              allow-clear
              show-search
              tree-default-expand-all
              :tree-node-filter-prop="'title'"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="安装位置" name="location">
        <a-input
          :value="formData.location" @update:value="val => emit('update:value', val)"
          placeholder="请输入安装位置"
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="IP地址" name="ip">
            <a-input
              :value="formData.ip" @update:value="val => emit('update:value', val)"
              placeholder="请输入IP地址"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="端口号" name="port">
            <a-input-number
              :value="formData.port" @update:value="val => emit('update:value', val)"
              :min="1"
              :max="65535"
              placeholder="请输入端口号"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="制造商" name="manufacturer">
            <a-input
              :value="formData.manufacturer" @update:value="val => emit('update:value', val)"
              placeholder="请输入制造商"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="设备型号" name="model">
            <a-input
              :value="formData.model" @update:value="val => emit('update:value', val)"
              placeholder="请输入设备型号"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="固件版本" name="firmwareVersion">
        <a-input
          :value="formData.firmwareVersion" @update:value="val => emit('update:value', val)"
          placeholder="请输入固件版本"
        />
      </a-form-item>

      <a-form-item label="设备描述" name="description">
        <a-textarea
          :value="formData.description" @update:value="val => emit('update:value', val)"
          placeholder="请输入设备描述"
          :rows="3"
          :max-length="200"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, computed, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { useAccessDeviceStore } from '/@/store/modules/business/access-device';
  import { DeviceAddForm, DeviceUpdateForm } from '/@/types/access/device';

  // Props
  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    device: {
      type: Object,
      default: null,
    },
    mode: {
      type: String,
      default: 'add', // add | edit
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
    deviceCode: '',
    deviceName: '',
    deviceType: undefined,
    areaId: undefined,
    location: '',
    ip: '',
    port: 8080,
    manufacturer: '',
    model: '',
    firmwareVersion: '',
    description: '',
  });

  // 区域树数据
  const areaTreeData = ref([]);

  // 表单验证规则
  const formRules = {
    deviceCode: [
      { required: true, message: '请输入设备编码', trigger: 'blur' },
      { max: 50, message: '设备编码长度不能超过50个字符', trigger: 'blur' },
      { pattern: /^[A-Za-z0-9_-]+$/, message: '设备编码只能包含字母、数字、下划线和连字符', trigger: 'blur' },
    ],
    deviceName: [
      { required: true, message: '请输入设备名称', trigger: 'blur' },
      { max: 100, message: '设备名称长度不能超过100个字符', trigger: 'blur' },
    ],
    deviceType: [
      { required: true, message: '请选择设备类型', trigger: 'change' },
    ],
    location: [
      { required: true, message: '请输入安装位置', trigger: 'blur' },
      { max: 200, message: '安装位置长度不能超过200个字符', trigger: 'blur' },
    ],
    ip: [
      { required: true, message: '请输入IP地址', trigger: 'blur' },
      { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: '请输入正确的IP地址格式', trigger: 'blur' },
    ],
    port: [
      { required: true, message: '请输入端口号', trigger: 'blur' },
      { type: 'number', min: 1, max: 65535, message: '端口号必须在1-65535之间', trigger: 'blur' },
    ],
  };

  // 计算属性
  const title = computed(() => {
    return props.mode === 'add' ? '添加设备' : '编辑设备';
  });

  const visible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value),
  });

  // 监听设备数据变化
  watch(() => props.device, (newDevice) => {
    if (newDevice && props.mode === 'edit') {
      Object.assign(formData, {
        deviceId: newDevice.deviceId,
        deviceCode: newDevice.deviceCode,
        deviceName: newDevice.deviceName,
        deviceType: newDevice.deviceType,
        areaId: newDevice.areaId,
        location: newDevice.location,
        ip: newDevice.ip,
        port: newDevice.port,
        manufacturer: newDevice.manufacturer || '',
        model: newDevice.model || '',
        firmwareVersion: newDevice.firmwareVersion || '',
        description: newDevice.description || '',
      });
    } else {
      resetForm();
    }
  }, { immediate: true });

  // 监听弹窗显示状态
  watch(() => props.visible, (visible) => {
    if (visible) {
      loadAreaTree();
    }
  });

  // 方法
  const loadAreaTree = async () => {
    try {
      const treeData = await deviceStore.fetchGroupTree();
      if (treeData) {
        areaTreeData.value = transformAreaTree(treeData);
      }
    } catch (error) {
      console.error('加载区域树失败:', error);
    }
  };

  const transformAreaTree = (areas) => {
    return areas.map(area => ({
      value: area.groupId,
      title: area.groupName,
      key: area.groupId,
      children: area.children ? transformAreaTree(area.children) : undefined,
    }));
  };

  const resetForm = () => {
    Object.assign(formData, {
      deviceCode: '',
      deviceName: '',
      deviceType: undefined,
      areaId: undefined,
      location: '',
      ip: '',
      port: 8080,
      manufacturer: '',
      model: '',
      firmwareVersion: '',
      description: '',
    });

    if (formRef.value) {
      formRef.value.clearValidate();
    }
  };

  const handleSubmit = async () => {
    try {
      await formRef.value.validate();
      loading.value = true;

      let success;
      if (props.mode === 'add') {
        const addData = {
          deviceCode: formData.deviceCode,
          deviceName: formData.deviceName,
          deviceType: formData.deviceType,
          areaId: formData.areaId,
          location: formData.location,
          ip: formData.ip,
          port: formData.port,
          manufacturer: formData.manufacturer,
          model: formData.model,
          firmwareVersion: formData.firmwareVersion,
          description: formData.description,
        };
        success = await deviceStore.addDevice(addData);
      } else {
        const updateData = {
          deviceId: formData.deviceId,
          deviceName: formData.deviceName,
          deviceType: formData.deviceType,
          areaId: formData.areaId,
          location: formData.location,
          ip: formData.ip,
          port: formData.port,
          manufacturer: formData.manufacturer,
          model: formData.model,
          firmwareVersion: formData.firmwareVersion,
          description: formData.description,
        };
        success = await deviceStore.updateDevice(updateData);
      }

      if (success) {
        emit('success');
      }
    } catch (error) {
      console.error('表单验证失败:', error);
    } finally {
      loading.value = false;
    }
  };

  const handleCancel = () => {
    visible.value = false;
    resetForm();
  };
</script>