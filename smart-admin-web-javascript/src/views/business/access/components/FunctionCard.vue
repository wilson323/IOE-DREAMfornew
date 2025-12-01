<!--
  * 功能模块卡片组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-card
    class="function-card"
    :class="{ 'has-badge': badge }"
    hoverable
    @click="handleClick"
  >
    <template #extra>
      <a-badge v-if="badge" :count="badge.count" :type="badge.type">
        <MoreOutlined style="color: #8c8c8c;" />
      </a-badge>
    </template>

    <div class="function-content">
      <div class="function-icon">
        <component :is="iconComponent" />
      </div>
      <div class="function-info">
        <h3 class="function-title">{{ title }}</h3>
        <p class="function-description">{{ description }}</p>
      </div>
    </div>

    <div v-if="$slots.actions" class="function-actions">
      <slot name="actions"></slot>
    </div>
  </a-card>
</template>

<script setup>
  import { computed } from 'vue';
  import {
    DesktopOutlined,
    SafetyCertificateOutlined,
    HistoryOutlined,
    SettingOutlined,
    BarChartOutlined,
    MoreOutlined,
  } from '@ant-design/icons-vue';

  // Props
  const props = defineProps({
    title: {
      type: String,
      required: true,
    },
    description: {
      type: String,
      required: true,
    },
    icon: {
      type: String,
      required: true,
    },
    badge: {
      type: Object,
      default: null,
    },
  });

  // Emits
  const emit = defineEmits(['click']);

  // 计算属性
  const iconComponent = computed(() => {
    const iconMap = {
      desktop: DesktopOutlined,
      'safety-certificate': SafetyCertificateOutlined,
      history: HistoryOutlined,
      setting: SettingOutlined,
      'bar-chart': BarChartOutlined,
    };
    return iconMap[props.icon] || DesktopOutlined;
  });

  // 方法
  const handleClick = () => {
    emit('click');
  };
</script>

<style lang="less" scoped>
  .function-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    height: 180px;
    display: flex;
    flex-direction: column;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }

    &.has-badge {
      :deep(.ant-card-head) {
        border-bottom: none;
      }
    }

    :deep(.ant-card-body) {
      padding: 24px;
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .function-content {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      flex: 1;

      .function-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        border-radius: 12px;
        background: linear-gradient(135deg, #1890ff, #722ed1);
        color: #fff;
        font-size: 24px;
      }

      .function-info {
        flex: 1;

        .function-title {
          margin: 0 0 8px 0;
          font-size: 18px;
          font-weight: 600;
          color: #262626;
        }

        .function-description {
          margin: 0;
          color: #8c8c8c;
          font-size: 14px;
          line-height: 1.5;
        }
      }
    }

    .function-actions {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
      display: flex;
      gap: 8px;

      :deep(.ant-btn-link) {
        padding: 0;
        height: auto;
        font-size: 12px;
      }
    }
  }
</style>