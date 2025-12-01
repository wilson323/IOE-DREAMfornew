<!--
  * 快速操作组件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-13
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="quick-action" @click="handleClick">
    <div class="action-icon" :style="{ backgroundColor: color + '20' }">
      <component :is="iconComponent" :style="{ color }" />
    </div>
    <div class="action-content">
      <h4 class="action-title">{{ title }}</h4>
      <p class="action-description">{{ description }}</p>
    </div>
  </div>
</template>

<script setup>
  import { computed } from 'vue';
  import {
    UnlockOutlined,
    ClockCircleOutlined,
    LockOutlined,
    CheckCircleOutlined,
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
    color: {
      type: String,
      default: '#1890ff',
    },
  });

  // Emits
  const emit = defineEmits(['click']);

  // 计算属性
  const iconComponent = computed(() => {
    const iconMap = {
      unlock: UnlockOutlined,
      'clock-circle': ClockCircleOutlined,
      lock: LockOutlined,
      'check-circle': CheckCircleOutlined,
    };
    return iconMap[props.icon] || CheckCircleOutlined;
  });

  // 方法
  const handleClick = () => {
    emit('click');
  };
</script>

<style lang="less" scoped>
  .quick-action {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #fff;
    border: 1px solid #f0f0f0;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #1890ff;
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
      transform: translateY(-2px);
    }

    .action-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border-radius: 12px;
      font-size: 24px;
    }

    .action-content {
      flex: 1;

      .action-title {
        margin: 0 0 4px 0;
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }

      .action-description {
        margin: 0;
        color: #8c8c8c;
        font-size: 14px;
        line-height: 1.4;
      }
    }
  }
</style>