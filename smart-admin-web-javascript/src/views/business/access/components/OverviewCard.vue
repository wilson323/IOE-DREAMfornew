<!--
  * 概览卡片组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-card
    class="overview-card"
    :class="{ 'clickable': clickable }"
    @click="handleClick"
  >
    <div class="overview-content">
      <div class="overview-icon">
        <component :is="iconComponent" :style="{ color: color, fontSize: '32px' }" />
      </div>
      <div class="overview-info">
        <div class="overview-title">{{ title }}</div>
        <div class="overview-value">
          <a-spin v-if="loading" size="small" />
          <span v-else>{{ formattedValue }}</span>
        </div>
        <div v-if="$slots.extra" class="overview-extra">
          <slot name="extra"></slot>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup>
  import { computed } from 'vue';
  import {
    DesktopOutlined,
    SafetyCertificateOutlined,
    LoginOutlined,
    AlertOutlined,
  } from '@ant-design/icons-vue';

  // Props
  const props = defineProps({
    title: {
      type: String,
      required: true,
    },
    value: {
      type: [Number, String],
      default: 0,
    },
    icon: {
      type: String,
      required: true,
    },
    color: {
      type: String,
      default: '#1890ff',
    },
    loading: {
      type: Boolean,
      default: false,
    },
    clickable: {
      type: Boolean,
      default: false,
    },
  });

  // Emits
  const emit = defineEmits(['click']);

  // 计算属性
  const iconComponent = computed(() => {
    const iconMap = {
      desktop: DesktopOutlined,
      'safety-certificate': SafetyCertificateOutlined,
      login: LoginOutlined,
      alert: AlertOutlined,
    };
    return iconMap[props.icon] || DesktopOutlined;
  });

  const formattedValue = computed(() => {
    if (typeof props.value === 'number') {
      return props.value.toLocaleString();
    }
    return props.value;
  });

  // 方法
  const handleClick = () => {
    if (props.clickable) {
      emit('click');
    }
  };
</script>

<style lang="less" scoped>
  .overview-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;

    &.clickable {
      cursor: pointer;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      }
    }

    :deep(.ant-card-body) {
      padding: 24px;
    }

    .overview-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .overview-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 64px;
        height: 64px;
        border-radius: 12px;
        background: rgba(24, 144, 255, 0.1);
      }

      .overview-info {
        flex: 1;

        .overview-title {
          font-size: 14px;
          color: #8c8c8c;
          margin-bottom: 8px;
        }

        .overview-value {
          font-size: 28px;
          font-weight: 700;
          color: #262626;
          margin-bottom: 8px;
          line-height: 1;
        }

        .overview-extra {
          // 额外信息样式
        }
      }
    }
  }
</style>