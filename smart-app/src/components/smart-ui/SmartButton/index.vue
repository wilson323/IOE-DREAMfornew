<template>
  <button
    :class="buttonClass"
    :disabled="disabled || loading"
    :hover-class="hoverClass"
    @click="handleClick"
  >
    <view v-if="loading" class="button-loading">
      <text class="loading-icon">⏳</text>
    </view>
    <text class="button-text">
      <slot></slot>
    </text>
  </button>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'SmartButton',
  
  props: {
    // 按钮类型
    type: {
      type: String,
      default: 'default',
      validator: (value) => ['primary', 'success', 'warning', 'danger', 'default'].includes(value)
    },
    // 按钮尺寸
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['small', 'default', 'large'].includes(value)
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },
    // 是否加载中
    loading: {
      type: Boolean,
      default: false
    },
    // 是否块级按钮
    block: {
      type: Boolean,
      default: false
    },
    // 是否圆角按钮
    round: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['click'],
  
  setup(props, { emit }) {
    const buttonClass = computed(() => {
      return [
        'smart-button',
        `smart-button-${props.type}`,
        `smart-button-${props.size}`,
        {
          'smart-button-disabled': props.disabled,
          'smart-button-loading': props.loading,
          'smart-button-block': props.block,
          'smart-button-round': props.round
        }
      ]
    })

    const hoverClass = computed(() => {
      if (props.disabled || props.loading) {
        return 'none'
      }
      return 'smart-button-hover'
    })

    const handleClick = (e) => {
      if (!props.disabled && !props.loading) {
        emit('click', e)
        uni.vibrateShort({ type: 'light' })
      }
    }

    return {
      buttonClass,
      hoverClass,
      handleClick
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system/variables.scss';

.smart-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: $btn-height-default;
  padding: 0 $spacing-lg;
  background: $bg-white;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-primary;
  transition: all $animation-duration-base;
  
  // 类型样式
  &-primary {
    background: $primary-color;
    border-color: $primary-color;
    color: #fff;
  }
  
  &-success {
    background: $success-color;
    border-color: $success-color;
    color: #fff;
  }
  
  &-warning {
    background: $warning-color;
    border-color: $warning-color;
    color: #fff;
  }
  
  &-danger {
    background: $error-color;
    border-color: $error-color;
    color: #fff;
  }
  
  // 尺寸样式
  &-small {
    height: $btn-height-small;
    padding: 0 $spacing-md;
    font-size: $font-size-sm;
  }
  
  &-large {
    height: $btn-height-large;
    padding: 0 $spacing-xl;
    font-size: $font-size-md;
  }
  
  // 状态样式
  &-disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  &-loading {
    opacity: 0.8;
  }
  
  &-block {
    display: flex;
    width: 100%;
  }
  
  &-round {
    border-radius: $btn-height-default / 2;
    
    &.smart-button-small {
      border-radius: $btn-height-small / 2;
    }
    
    &.smart-button-large {
      border-radius: $btn-height-large / 2;
    }
  }
  
  // 悬停样式
  &-hover:not(.smart-button-disabled):not(.smart-button-loading) {
    opacity: 0.8;
    transform: scale(0.98);
  }
  
  .button-loading {
    margin-right: $spacing-sm;
    
    .loading-icon {
      animation: rotate 1s linear infinite;
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>

