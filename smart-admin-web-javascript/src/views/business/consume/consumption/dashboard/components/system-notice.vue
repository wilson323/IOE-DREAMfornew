<!--
  * 系统通知组件
  * 
  * @Author:    SmartAdmin
  * @Date:      2025-11-04
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div v-if="noticeData && noticeData.content" class="system-notice" :class="`notice-${noticeData.type || 'info'}`">
    <div class="notice-icon">
      <component :is="$antIcons[noticeIcon]" />
    </div>
    <div class="notice-content">
      <div class="notice-title">{{ noticeData.title || '系统通知' }}</div>
      <div class="notice-text">{{ noticeData.content }}</div>
    </div>
    <a-button v-if="noticeData.closable !== false" type="text" size="small" class="notice-close" @click="handleClose">
      <template #icon>
        <CloseOutlined />
      </template>
    </a-button>
  </div>
</template>

<script setup>
  import { computed } from 'vue';
  import { CloseOutlined } from '@ant-design/icons-vue';

  // ----------------------- props -----------------------
  const props = defineProps({
    noticeData: {
      type: Object,
      default: () => ({
        title: '系统通知',
        content: '',
        type: 'info', // info | success | warning | error
        closable: false,
      }),
    },
  });

  // ----------------------- emits -----------------------
  const emit = defineEmits(['close']);

  // ----------------------- computed -----------------------
  const noticeIcon = computed(() => {
    const iconMap = {
      info: 'InfoCircleOutlined',
      success: 'CheckCircleOutlined',
      warning: 'ExclamationCircleOutlined',
      error: 'CloseCircleOutlined',
    };
    return iconMap[props.noticeData.type] || iconMap.info;
  });

  // ----------------------- methods -----------------------
  function handleClose() {
    emit('close');
  }
</script>

<style lang="less" scoped>
  .system-notice {
    background: #e6f4ff;
    border: 1px solid #91caff;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 24px;
    display: flex;
    align-items: flex-start;
    gap: 12px;
    position: relative;
    transition: all 0.3s ease;

    .notice-icon {
      font-size: 20px;
      color: #1890ff;
      flex-shrink: 0;
      margin-top: 2px;
    }

    .notice-content {
      flex: 1;
      min-width: 0;

      .notice-title {
        font-weight: 600;
        color: #1890ff;
        margin-bottom: 4px;
        font-size: 14px;
      }

      .notice-text {
        font-size: 14px;
        color: #595959;
        line-height: 1.6;
      }
    }

    .notice-close {
      flex-shrink: 0;
      color: #8c8c8c;

      &:hover {
        color: #262626;
      }
    }

    // 不同类型的样式
    &.notice-success {
      background: #f6ffed;
      border-color: #b7eb8f;

      .notice-icon,
      .notice-title {
        color: #52c41a;
      }
    }

    &.notice-warning {
      background: #fffbe6;
      border-color: #ffe58f;

      .notice-icon,
      .notice-title {
        color: #faad14;
      }
    }

    &.notice-error {
      background: #fff2f0;
      border-color: #ffccc7;

      .notice-icon,
      .notice-title {
        color: #ff4d4f;
      }
    }
  }

  @media (max-width: 768px) {
    .system-notice {
      padding: 12px;

      .notice-icon {
        font-size: 18px;
      }

      .notice-content {
        .notice-title {
          font-size: 13px;
        }

        .notice-text {
          font-size: 13px;
        }
      }
    }
  }
</style>

