<!--
  批量操作弹窗组件
  基于SmartAdmin框架的Vue3企业级批量操作界面

  @Author: IOE-DREAM Team
  @Date: 2025-12-01
  @Copyright IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <a-modal
    v-model:open="visible"
    title="批量操作"
    :width="600"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #message>
          已选择 <strong>{{ selectedRows.length }}</strong> 条规则
        </template>
      </a-alert>

      <a-form-item label="操作类型" name="operation">
        <a-radio-group v-model:value="formData.operation" @change="handleOperationChange">
          <a-radio value="enable">批量启用</a-radio>
          <a-radio value="disable">批量禁用</a-radio>
          <a-radio value="delete">批量删除</a-radio>
          <a-radio value="copy">批量复制</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 复制操作时的额外选项 -->
      <a-form-item
        v-if="formData.operation === 'copy'"
        label="复制规则"
        name="copyOptions"
      >
        <a-space direction="vertical" style="width: 100%">
          <a-input
            v-model:value="formData.namePrefix"
            placeholder="规则名称前缀（可选）"
            :max-length="50"
          />
          <a-checkbox v-model:checked="formData.adjustExecutionOrder">
            自动调整执行顺序
          </a-checkbox>
          <a-checkbox v-model:checked="formData.disableOriginal">
            禁用原始规则
          </a-checkbox>
        </a-space>
      </a-form-item>

      <!-- 删除操作确认 -->
      <a-form-item
        v-if="formData.operation === 'delete'"
        label="删除确认"
        name="deleteConfirm"
      >
        <a-checkbox v-model:checked="formData.confirmDelete">
          我确认要删除选中的 {{ selectedRows.length }} 条规则
        </a-checkbox>
      </a-form-item>

      <!-- 操作说明 -->
      <a-form-item label="操作说明">
        <div class="operation-description">
          <p v-if="formData.operation === 'enable'">
            将选中的所有联动规则设置为启用状态，规则将正常执行。
          </p>
          <p v-if="formData.operation === 'disable'">
            将选中的所有联动规则设置为禁用状态，规则将不会执行。
          </p>
          <p v-if="formData.operation === 'delete'">
            永久删除选中的联动规则，此操作不可撤销。
            <br>
            <strong>警告：删除后无法恢复，请谨慎操作！</strong>
          </p>
          <p v-if="formData.operation === 'copy'">
            复制选中的联动规则，创建新的规则副本。
            可以设置规则名称前缀来区分复制的规则。
          </p>
        </div>
      </a-form-item>

      <!-- 预览影响的规则 -->
      <a-form-item label="影响规则">
        <div class="affected-rules">
          <a-tag
            v-for="rule in selectedRows.slice(0, 10)"
            :key="rule"
            color="blue"
            style="margin-bottom: 4px"
          >
            规则 #{{ rule }}
          </a-tag>
          <span v-if="selectedRows.length > 10" class="more-rules">
            还有 {{ selectedRows.length - 10 }} 条规则...
          </span>
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import { useGlobalLinkageStore } from '/@/store/modules/business/global-linkage';

// Props & Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  selectedRows: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['update:visible', 'success']);

// Store
const globalLinkageStore = useGlobalLinkageStore();

// 响应式数据
const formRef = ref();
const loading = ref(false);

// 表单数据
const formData = reactive({
  operation: 'enable',
  namePrefix: '',
  adjustExecutionOrder: true,
  disableOriginal: false,
  confirmDelete: false,
});

// 表单验证规则
const rules = computed(() => {
  const baseRules = {
    operation: [
      { required: true, message: '请选择操作类型', trigger: 'change' },
    ],
  };

  if (formData.operation === 'delete') {
    baseRules.deleteConfirm = [
      {
        validator: (rule, value) => {
          if (!value) {
            return Promise.reject('请确认删除操作');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  }

  if (formData.operation === 'copy' && !formData.namePrefix) {
    baseRules.namePrefix = [
      { max: 50, message: '名称前缀不能超过50个字符', trigger: 'blur' },
    ];
  }

  return baseRules;
});

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
});

// 方法

/**
 * 操作类型改变
 */
const handleOperationChange = () => {
  // 重置确认状态
  formData.confirmDelete = false;
};

/**
 * 提交批量操作
 */
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    let success = false;

    switch (formData.operation) {
      case 'enable':
        success = await globalLinkageStore.batchOperateRules('enable', props.selectedRows);
        break;
      case 'disable':
        success = await globalLinkageStore.batchOperateRules('disable', props.selectedRows);
        break;
      case 'delete':
        if (!formData.confirmDelete) {
          message.error('请确认删除操作');
          return;
        }
        success = await globalLinkageStore.batchDeleteRules(props.selectedRows);
        break;
      case 'copy':
        // 复制操作需要逐个处理
        success = true;
        for (const ruleId of props.selectedRows) {
          const rule = globalLinkageStore.getRuleById(ruleId);
          const newName = formData.namePrefix
            ? `${formData.namePrefix}_${rule.ruleName}`
            : `${rule.ruleName}_副本`;

          const copySuccess = await globalLinkageStore.copyRule(ruleId, newName);
          if (!copySuccess) {
            success = false;
            break;
          }
        }
        break;
      default:
        message.error('未知的操作类型');
        return;
    }

    if (success) {
      const operationText = {
        enable: '启用',
        disable: '禁用',
        delete: '删除',
        copy: '复制'
      }[formData.operation];

      message.success(`批量${operationText}成功`);
      emit('success');
      handleCancel();
    }
  } catch (error) {
    console.error('批量操作失败:', error);
  } finally {
    loading.value = false;
  }
};

/**
 * 取消
 */
const handleCancel = () => {
  visible.value = false;
  resetForm();
};

/**
 * 重置表单
 */
const resetForm = () => {
  formRef.value?.resetFields();
  Object.assign(formData, {
    operation: 'enable',
    namePrefix: '',
    adjustExecutionOrder: true,
    disableOriginal: false,
    confirmDelete: false,
  });
};

// 监听器
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      // 重置表单数据
      resetForm();
    }
  }
);
</script>

<style scoped lang="less">
.operation-description {
  padding: 12px;
  background: #f6f8fa;
  border-radius: 6px;
  border-left: 4px solid #1890ff;

  p {
    margin: 0;
    color: #666;
    line-height: 1.6;

    strong {
      color: #f5222d;
    }
  }
}

.affected-rules {
  max-height: 120px;
  overflow-y: auto;
  padding: 8px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;

  .more-rules {
    display: block;
    color: #999;
    font-size: 12px;
    margin-top: 8px;
    text-align: center;
  }
}
</style>