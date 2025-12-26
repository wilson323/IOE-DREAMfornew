<template>
  <div class="json-base-editor">
    <!-- 编辑区域 -->
    <div class="editor-section">
      <slot name="editor"></slot>
    </div>

    <!-- JSON预览 -->
    <div class="preview-section">
      <a-card title="JSON配置预览" size="small">
        <template #extra>
          <a-space>
            <a-button size="small" @click="handleCopy">
              <template #icon><CopyOutlined /></template>
              复制
            </a-button>
            <a-button size="small" @click="handleFormat">
              <template #icon><FormatPainterOutlined /></template>
              格式化
            </a-button>
          </a-space>
        </template>
        <pre class="json-preview">{{ formattedJson }}</pre>
      </a-card>
    </div>

    <!-- 错误提示 -->
    <a-alert
      v-if="errorMessage"
      type="error"
      :message="errorMessage"
      show-icon
      closable
      @close="errorMessage = ''"
      class="error-alert"
    />
  </div>
</template>

<script setup>
  import { computed, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { CopyOutlined, FormatPainterOutlined } from '@ant-design/icons-vue';

  const props = defineProps({
    modelValue: {
      type: Object,
      required: true,
      default: () => ({})
    },
    readonly: {
      type: Boolean,
      default: false
    }
  });

  const emit = defineEmits(['update:modelValue']);

  const errorMessage = ref('');

  const formattedJson = computed(() => {
    try {
      return JSON.stringify(props.modelValue, null, 2);
    } catch (error) {
      errorMessage.value = 'JSON格式错误: ' + error.message;
      return '{}';
    }
  });

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(formattedJson.value);
      message.success('已复制到剪贴板');
    } catch (error) {
      message.error('复制失败');
    }
  };

  const handleFormat = () => {
    // JSON已经格式化，这里可以添加更多格式化选项
    message.success('JSON已格式化');
  };

  const validateJson = (jsonStr) => {
    try {
      JSON.parse(jsonStr);
      return true;
    } catch (error) {
      errorMessage.value = 'JSON格式错误: ' + error.message;
      return false;
    }
  };

  defineExpose({
    validateJson,
    setErrorMessage: (msg) => {
      errorMessage.value = msg;
    }
  });
</script>

<style lang="less" scoped>
  .json-base-editor {
    .editor-section {
      margin-bottom: 16px;
    }

    .preview-section {
      margin-top: 16px;

      .json-preview {
        background: #f5f5f5;
        padding: 12px;
        border-radius: 4px;
        overflow-x: auto;
        font-size: 12px;
        line-height: 1.6;
        max-height: 400px;
        overflow-y: auto;
      }
    }

    .error-alert {
      margin-top: 16px;
    }
  }
</style>
