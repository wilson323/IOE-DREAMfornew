<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑区域' : '新增区域'"
    width="600px"
    :mask-closable="false"
    @ok="handleOk"
    @cancel="handleCancel"
    :confirm-loading="loading"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <!-- 基本信息 -->
      <a-divider>基本信息</a-divider>

      <a-form-item label="区域名称" name="areaName">
        <a-input
          v-model:value="formState.areaName"
          placeholder="请输入区域名称"
          :maxlength="50"
          show-count
        />
      </a-form-item>

      <a-form-item label="区域编码" name="areaCode">
        <a-input
          v-model:value="formState.areaCode"
          placeholder="请输入区域编码"
          :maxlength="20"
          show-count
          @blur="handleAreaCodeBlur"
        />
        <div v-if="areaCodeError" class="error-text">{{ areaCodeError }}</div>
      </a-form-item>

      <a-form-item label="区域类型" name="areaType">
        <a-select
          v-model:value="formState.areaType"
          placeholder="请选择区域类型"
        >
          <a-select-option value="BUILDING">建筑</a-select-option>
          <a-select-option value="FLOOR">楼层</a-select-option>
          <a-select-option value="ROOM">房间</a-select-option>
          <a-select-option value="AREA">区域</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="父级区域" name="parentAreaId" v-if="!isEdit || formState.parentAreaId">
        <a-tree-select
          v-model:value="formState.parentAreaId"
          :tree-data="areaTreeOptions"
          placeholder="请选择父级区域"
          allow-clear
          tree-default-expand-all
          style="width: 100%"
        />
      </a-form-item>

      <!-- 配置信息 -->
      <a-divider>配置信息</a-divider>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formState.status">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="是否默认区域" name="isDefault">
        <a-switch
          v-model:checked="formState.isDefault"
          :disabled="formState.parentAreaId === null"
        />
        <div class="form-item-help">设置为默认区域后，新用户将自动获得该区域权限</div>
      </a-form-item>

      <a-form-item label="访问权限" name="accessControl">
        <a-checkbox-group v-model:value="formState.accessControl">
          <a-checkbox value="entrance">进入权限</a-checkbox>
          <a-checkbox value="exit">离开权限</a-checkbox>
          <a-checkbox value="visitor">访客权限</a-checkbox>
          <a-checkbox value="emergency">紧急权限</a-checkbox>
        </a-checkbox-group>
      </a-form-item>

      <a-form-item label="时间段限制" name="timeRestriction">
        <a-select
          v-model:value="formState.timeRestriction"
          placeholder="请选择时间段限制"
          allow-clear
        >
          <a-select-option value="full_day">全天</a-select-option>
          <a-select-option value="work_day">工作日</a-select-option>
          <a-select-option value="custom">自定义</a-select-option>
        </a-select>
      </a-form-item>

      <!-- 详细信息 -->
      <a-divider>详细信息</a-divider>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formState.description"
          :rows="3"
          placeholder="请输入区域描述"
          :maxlength="200"
          show-count
        />
      </a-form-item>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formState.remark"
          :rows="2"
          placeholder="请输入备注信息"
          :maxlength="100"
          show-count
        />
      </a-form-item>

      <!-- 高级设置 -->
      <a-divider>高级设置</a-divider>

      <a-form-item label="最大允许人数" name="maxCapacity">
        <a-input-number
          v-model:value="formState.maxCapacity"
          placeholder="请输入最大允许人数"
          :min="1"
          :max="9999"
          style="width: 100%"
          addon-after="人"
        />
      </a-form-item>

      <a-form-item label="安全级别" name="securityLevel">
        <a-select
          v-model:value="formState.securityLevel"
          placeholder="请选择安全级别"
        >
          <a-select-option value="LOW">低</a-select-option>
          <a-select-option value="MEDIUM">中</a-select-option>
          <a-select-option value="HIGH">高</a-select-option>
          <a-select-option value="CRITICAL">极高</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { accessAreaApi } from '/@/api/business/access/area-api.js';

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  formData: {
    type: Object,
    default: null
  },
  areaTreeOptions: {
    type: Array,
    default: () => []
  }
});

// Emits
const emit = defineEmits(['update:visible', 'success']);

// 响应式数据
const formRef = ref();
const loading = ref(false);
const areaCodeError = ref('');

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

const isEdit = computed(() => !!props.formData?.areaId);

// 表单状态
const formState = reactive({
  areaId: null,
  areaName: '',
  areaCode: '',
  areaType: 'AREA',
  parentAreaId: null,
  description: '',
  remark: '',
  status: 1,
  isDefault: false,
  accessControl: ['entrance', 'exit'],
  timeRestriction: 'full_day',
  maxCapacity: null,
  securityLevel: 'MEDIUM'
});

// 表单验证规则
const rules = {
  areaName: [
    { required: true, message: '请输入区域名称', trigger: 'blur' },
    { min: 2, max: 50, message: '区域名称长度在2-50个字符之间', trigger: 'blur' }
  ],
  areaCode: [
    { required: true, message: '请输入区域编码', trigger: 'blur' },
    { min: 2, max: 20, message: '区域编码长度在2-20个字符之间', trigger: 'blur' },
    { pattern: /^[A-Z0-9_-]+$/, message: '区域编码只能包含大写字母、数字、下划线和横线', trigger: 'blur' }
  ],
  areaType: [
    { required: true, message: '请选择区域类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
};

// 监听formData变化
watch(
  () => props.formData,
  (newData) => {
    if (newData) {
      Object.assign(formState, {
        areaId: newData.areaId,
        areaName: newData.areaName || '',
        areaCode: newData.areaCode || '',
        areaType: newData.areaType || 'AREA',
        parentAreaId: newData.parentAreaId || null,
        description: newData.description || '',
        remark: newData.remark || '',
        status: newData.status ?? 1,
        isDefault: newData.isDefault ?? false,
        accessControl: newData.accessControl || ['entrance', 'exit'],
        timeRestriction: newData.timeRestriction || 'full_day',
        maxCapacity: newData.maxCapacity || null,
        securityLevel: newData.securityLevel || 'MEDIUM'
      });
    }
  },
  { immediate: true, deep: true }
);

// 区域编码唯一性验证
const handleAreaCodeBlur = async () => {
  if (!formState.areaCode) {
    areaCodeError.value = '';
    return;
  }

  try {
    const response = await accessAreaApi.validateAreaCode(
      formState.areaCode,
      isEdit.value ? formState.areaId : null
    );

    if (response && response.data) {
      if (!response.data.valid) {
        areaCodeError.value = response.data.message || '区域编码已存在';
      } else {
        areaCodeError.value = '';
      }
    }
  } catch (error) {
    console.error('验证区域编码失败:', error);
  }
};

// 提交表单
const handleOk = async () => {
  try {
    await formRef.value.validate();

    if (areaCodeError.value) {
      message.error('请先解决区域编码问题');
      return;
    }

    loading.value = true;

    let response;
    if (isEdit.value) {
      response = await accessAreaApi.updateArea(formState);
    } else {
      response = await accessAreaApi.addArea(formState);
    }

    if (response && response.success) {
      message.success(isEdit.value ? '更新成功' : '新增成功');
      emit('success');
      emit('update:visible', false);
    } else {
      message.error(response?.message || '操作失败');
    }
  } catch (error) {
    console.error('保存区域失败:', error);
    message.error('操作失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 取消
const handleCancel = () => {
  formRef.value?.resetFields();
  areaCodeError.value = '';
  emit('update:visible', false);
};
</script>

<style lang="less" scoped>
.error-text {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.form-item-help {
  color: #666;
  font-size: 12px;
  margin-top: 4px;
}

:deep(.ant-divider) {
  margin: 16px 0;
}
</style>