<template>
  <view
    class="smart-card"
    :class="[`smart-card--${type}`, { 'smart-card--shadow': shadow, 'smart-card--border': border }]"
    :style="cardStyle"
    @click="handleClick"
  >
    <!-- 卡片头部 -->
    <view v-if="showHeader || title || $slots.header" class="smart-card__header">
      <slot name="header">
        <view class="smart-card__title">
          <text class="smart-card__title-text">{{ title }}</text>
          <text v-if="subtitle" class="smart-card__subtitle">{{ subtitle }}</text>
        </view>
        <view v-if="showExtra" class="smart-card__extra">
          <slot name="extra">{{ extra }}</slot>
        </view>
      </slot>
    </view>

    <!-- 卡片内容 -->
    <view class="smart-card__body" :style="bodyStyle">
      <slot>
        <view v-if="loading" class="smart-card__loading">
          <view class="smart-card__loading-spinner"></view>
          <text class="smart-card__loading-text">{{ loadingText }}</text>
        </view>
        <view v-else-if="empty" class="smart-card__empty">
          <image
            :src="emptyImage"
            class="smart-card__empty-image"
            mode="aspectFit"
          />
          <text class="smart-card__empty-text">{{ emptyText }}</text>
        </view>
      </slot>
    </view>

    <!-- 卡片底部 -->
    <view v-if="showFooter || $slots.footer" class="smart-card__footer">
      <slot name="footer">
        <view class="smart-card__actions">
          <button
            v-for="(action, index) in actions"
            :key="index"
            :class="['smart-card__action', `smart-card__action--${action.type || 'default'}`]"
            :disabled="action.disabled"
            @click.stop="handleActionClick(action, $event)"
          >
            {{ action.text }}
          </button>
        </view>
      </slot>
    </view>

    <!-- 加载遮罩 -->
    <view v-if="loading" class="smart-card__loading-mask">
      <view class="smart-card__loading-content">
        <view class="smart-card__loading-spinner"></view>
        <text class="smart-card__loading-text">{{ loadingText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'

// Props定义
const props = defineProps({
  // 基础属性
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'error'].includes(value)
  },

  // 样式属性
  shadow: {
    type: Boolean,
    default: true
  },
  border: {
    type: Boolean,
    default: false
  },
  radius: {
    type: String,
    default: '12rpx'
  },
  padding: {
    type: String,
    default: '24rpx'
  },

  // 状态属性
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '加载中...'
  },
  empty: {
    type: Boolean,
    default: false
  },
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  emptyImage: {
    type: String,
    default: '/static/images/empty.png'
  },

  // 显示控制
  showHeader: {
    type: Boolean,
    default: true
  },
  showFooter: {
    type: Boolean,
    default: false
  },
  showExtra: {
    type: Boolean,
    default: false
  },

  // 操作按钮
  actions: {
    type: Array,
    default: () => []
  },

  // 点击行为
  clickable: {
    type: Boolean,
    default: false
  },

  // 额外数据
  extra: {
    type: String,
    default: ''
  },

  // 自定义样式
  bodyStyle: {
    type: [String, Object],
    default: ''
  }
})

// Emits定义
const emit = defineEmits(['click', 'action'])

// 计算属性
const cardStyle = computed(() => {
  return {
    borderRadius: props.radius,
    padding: props.padding
  }
})

// 事件处理
const handleClick = (event) => {
  if (props.clickable) {
    emit('click', event)
  }
}

const handleActionClick = (action, event) => {
  if (!action.disabled && action.handler) {
    action.handler(action)
  }
  emit('action', {
    action,
    event
  })
}
</script>

<style lang="scss" scoped>
.smart-card {
  position: relative;
  background: #ffffff;
  overflow: hidden;
  transition: all 0.3s ease;

  &--shadow {
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  }

  &--border {
    border: 2rpx solid #f0f0f0;
  }

  &--primary {
    border-left: 8rpx solid #1890ff;
  }

  &--success {
    border-left: 8rpx solid #52c41a;
  }

  &--warning {
    border-left: 8rpx solid #faad14;
  }

  &--error {
    border-left: 8rpx solid #ff4d4f;
  }

  &:active {
    transform: scale(0.98);
  }
}

.smart-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.smart-card__title {
  flex: 1;
}

.smart-card__title-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #262626;
  line-height: 1.4;
}

.smart-card__subtitle {
  display: block;
  font-size: 24rpx;
  color: #8c8c8c;
  margin-top: 8rpx;
}

.smart-card__extra {
  font-size: 28rpx;
  color: #1890ff;
}

.smart-card__body {
  position: relative;
}

.smart-card__loading,
.smart-card__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
}

.smart-card__loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #f0f0f0;
  border-top: 4rpx solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.smart-card__loading-text {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #8c8c8c;
}

.smart-card__empty-image {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}

.smart-card__empty-text {
  font-size: 28rpx;
  color: #8c8c8c;
}

.smart-card__footer {
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #f0f0f0;
}

.smart-card__actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.smart-card__action {
  min-width: 120rpx;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;

  &--default {
    background: #f5f5f5;
    color: #666;
  }

  &--primary {
    background: #1890ff;
    color: #fff;
  }

  &--success {
    background: #52c41a;
    color: #fff;
  }

  &--warning {
    background: #faad14;
    color: #fff;
  }

  &--error {
    background: #ff4d4f;
    color: #fff;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.smart-card__loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.smart-card__loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>