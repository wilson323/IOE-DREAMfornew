<!----区域新增/编辑弹窗------>
<template>
  <a-modal
    :visible="visible"
    :title="modalTitle"
    :width="800"
    :confirm-loading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :maskClosable="false"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="区域编码" name="areaCode">
        <a-input v-model:value="formData.areaCode" placeholder="请输入区域编码" :disabled="isViewMode" />
      </a-form-item>

      <a-form-item label="区域名称" name="areaName">
        <a-input v-model:value="formData.areaName" placeholder="请输入区域名称" :disabled="isViewMode" />
      </a-form-item>

      <a-form-item label="区域类型" name="areaType">
        <a-select v-model:value="formData.areaType" placeholder="请选择区域类型" :disabled="isViewMode">
          <a-select-option value="CAMPUS">园区</a-select-option>
          <a-select-option value="BUILDING">楼栋</a-select-option>
          <a-select-option value="FLOOR">楼层</a-select-option>
          <a-select-option value="ROOM">房间</a-select-option>
          <a-select-option value="OUTDOOR">室外</a-select-option>
          <a-select-option value="PARKING">停车场</a-select-option>
          <a-select-option value="ENTRANCE">出入口</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="父区域" name="parentId">
        <a-tree-select
          v-model:value="formData.parentId"
          :tree-data="parentAreaTree"
          placeholder="请选择父区域"
          tree-default-expand-all
          :field-names="{ label: 'areaName', value: 'areaId', children: 'children' }"
          :disabled="isViewMode"
        />
      </a-form-item>

      <a-form-item label="排序" name="sortOrder">
        <a-input-number
          v-model:value="formData.sortOrder"
          placeholder="请输入排序"
          :min="0"
          style="width: 100%"
          :disabled="isViewMode"
        />
      </a-form-item>

      <a-form-item label="负责人" name="managerId">
        <a-input v-model:value="formData.managerId" placeholder="请选择负责人" :disabled="isViewMode" />
      </a-form-item>

      <a-form-item label="联系电话" name="contactPhone">
        <a-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" :disabled="isViewMode" />
      </a-form-item>

      <a-form-item label="详细地址" name="address">
        <a-textarea
          v-model:value="formData.address"
          placeholder="请输入详细地址"
          :rows="2"
          :disabled="isViewMode"
        />
      </a-form-item>

      <a-form-item label="经纬度">
        <a-input-group compact>
          <a-input
            v-model:value="formData.longitude"
            placeholder="经度"
            style="width: 50%"
            :disabled="isViewMode"
          />
          <a-input
            v-model:value="formData.latitude"
            placeholder="纬度"
            style="width: 50%"
            :disabled="isViewMode"
          />
        </a-input-group>
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status" :disabled="isViewMode">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="区域描述" name="areaDesc">
        <a-textarea
          v-model:value="formData.areaDesc"
          placeholder="请输入区域描述"
          :rows="3"
          :disabled="isViewMode"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { areaApi } from '/@/api/system/area-api';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  areaData: {
    type: Object,
    default: null,
  },
  areaTree: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['update:visible', 'success']);

// 响应式数据
const formRef = ref();
const confirmLoading = ref(false);

const formData = reactive({
  areaId: undefined,
  areaCode: '',
  areaName: '',
  areaType: undefined,
  parentId: 0,
  sortOrder: 0,
  managerId: undefined,
  contactPhone: '',
  address: '',
  longitude: undefined,
  latitude: undefined,
  status: 1,
  areaDesc: '',
});

// 表单验证规则
const formRules = {
  areaCode: [
    { required: true, message: '请输入区域编码', trigger: 'blur' },
    { max: 100, message: '区域编码长度不能超过100个字符', trigger: 'blur' },
  ],
  areaName: [
    { required: true, message: '请输入区域名称', trigger: 'blur' },
    { max: 200, message: '区域名称长度不能超过200个字符', trigger: 'blur' },
  ],
  areaType: [{ required: true, message: '请选择区域类型', trigger: 'change' }],
  parentId: [{ required: true, message: '请选择父区域', trigger: 'change' }],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
};

// 计算属性
const modalTitle = computed(() => {
  if (isViewMode.value) {
    return '查看区域';
  }
  return formData.areaId ? '编辑区域' : '新增区域';
});

const isViewMode = computed(() => {
  return props.areaData && props.areaData.viewMode;
});

const parentAreaTree = computed(() => {
  // 添加根节点
  return [
    {
      areaId: 0,
      areaName: '根区域',
      children: props.areaTree,
    },
  ];
});

// 监听区域数据变化
watch(
  () => props.areaData,
  (newVal) => {
    if (newVal) {
      // 编辑模式
      Object.assign(formData, {
        areaId: newVal.areaId,
        areaCode: newVal.areaCode,
        areaName: newVal.areaName,
        areaType: newVal.areaType,
        parentId: newVal.parentId || 0,
        sortOrder: newVal.sortOrder || 0,
        managerId: newVal.managerId,
        contactPhone: newVal.contactPhone,
        address: newVal.address,
        longitude: newVal.longitude,
        latitude: newVal.latitude,
        status: newVal.status,
        areaDesc: newVal.areaDesc,
      });
    } else {
      // 新增模式
      resetForm();
    }
  },
  { immediate: true, deep: true }
);

// 方法
const resetForm = () => {
  Object.assign(formData, {
    areaId: undefined,
    areaCode: '',
    areaName: '',
    areaType: undefined,
    parentId: 0,
    sortOrder: 0,
    managerId: undefined,
    contactPhone: '',
    address: '',
    longitude: undefined,
    latitude: undefined,
    status: 1,
    areaDesc: '',
  });
  formRef.value?.clearValidate();
};

const handleSubmit = async () => {
  if (isViewMode.value) {
    handleCancel();
    return;
  }

  try {
    await formRef.value.validate();
    confirmLoading.value = true;

    if (formData.areaId) {
      // 编辑
      await areaApi.update(formData);
      message.success('更新成功');
    } else {
      // 新增
      await areaApi.add(formData);
      message.success('新增成功');
    }

    emit('success');
    handleCancel();
  } catch (error) {
    if (error.errorFields) {
      return;
    }
    message.error(error.msg || '操作失败');
  } finally {
    confirmLoading.value = false;
  }
};

const handleCancel = () => {
  emit('update:visible', false);
  resetForm();
};
</script>

<style lang="less" scoped>
// 样式可根据需要添加
</style>
