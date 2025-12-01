<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑区域' : '新增区域'"
    :width="600"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="区域名称" name="areaName">
        <a-input v-model:value="formData.areaName" placeholder="请输入区域名称" />
      </a-form-item>

      <a-form-item label="区域编码" name="areaCode">
        <a-input v-model:value="formData.areaCode" placeholder="请输入区域编码" />
      </a-form-item>

      <a-form-item label="上级区域" name="parentId">
        <a-tree-select
          v-model:value="formData.parentId"
          :tree-data="areaTreeData"
          placeholder="请选择上级区域"
          allow-clear
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
        />
      </a-form-item>

      <a-form-item label="区域类型" name="areaType">
        <a-select v-model:value="formData.areaType" placeholder="请选择区域类型">
          <a-select-option value="1">省级</a-select-option>
          <a-select-option value="2">市级</a-select-option>
          <a-select-option value="3">区县级</a-select-option>
          <a-select-option value="4">乡镇级</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="排序" name="sortOrder">
        <a-input-number
          v-model:value="formData.sortOrder"
          :min="0"
          :max="9999"
          placeholder="请输入排序号"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          :rows="3"
          placeholder="请输入备注信息"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  areaData: {
    type: Object,
    default: () => ({})
  },
  areaTreeData: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:visible', 'submit'])

// Refs
const formRef = ref()

// Form data
const formData = reactive({
  areaId: null,
  areaName: '',
  areaCode: '',
  parentId: null,
  areaType: null,
  sortOrder: 0,
  status: 1,
  remark: ''
})

// Form rules
const rules = {
  areaName: [
    { required: true, message: '请输入区域名称', trigger: 'blur' }
  ],
  areaCode: [
    { required: true, message: '请输入区域编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '区域编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  areaType: [
    { required: true, message: '请选择区域类型', trigger: 'change' }
  ]
}

// Watch for areaData changes
watch(() => props.areaData, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    Object.assign(formData, newVal)
  } else {
    resetForm()
  }
}, { immediate: true, deep: true })

// Reset form
const resetForm = () => {
  Object.assign(formData, {
    areaId: null,
    areaName: '',
    areaCode: '',
    parentId: null,
    areaType: null,
    sortOrder: 0,
    status: 1,
    remark: ''
  })
  formRef.value?.resetFields()
}

// Handle submit
const handleOk = async () => {
  try {
    await formRef.value.validate()
    emit('submit', { ...formData })
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// Handle cancel
const handleCancel = () => {
  emit('update:visible', false)
}
</script>