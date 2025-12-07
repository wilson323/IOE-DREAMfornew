<template>
  <view :class="cardClass" @click="handleClick">
    <!-- 卡片头部 -->
    <view v-if="hasHeader" class="card-header">
      <view class="card-title-wrap">
        <slot name="title">
          <text class="card-title">{{ title }}</text>
        </slot>
      </view>
      <view v-if="hasExtra" class="card-extra">
        <slot name="extra"></slot>
      </view>
    </view>

    <!-- 卡片主体 -->
    <view class="card-body">
      <slot></slot>
    </view>

    <!-- 卡片底部 -->
    <view v-if="hasFooter" class="card-footer">
      <slot name="footer"></slot>
    </view>
  </view>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'SmartCard',
  
  props: {
    // 卡片标题
    title: {
      type: String,
      default: ''
    },
    // 是否可点击
    hoverable: {
      type: Boolean,
      default: false
    },
    // 是否有边框
    bordered: {
      type: Boolean,
      default: true
    },
    // 是否有阴影
    shadow: {
      type: Boolean,
      default: true
    }
  },
  
  emits: ['click'],
  
  setup(props, { emit, slots }) {
    const hasHeader = computed(() => {
      return !!props.title || !!slots.title || !!slots.extra
    })

    const hasExtra = computed(() => {
      return !!slots.extra
    })

    const hasFooter = computed(() => {
      return !!slots.footer
    })

    const cardClass = computed(() => {
      return [
        'smart-card',
        {
          'smart-card-hoverable': props.hoverable,
          'smart-card-bordered': props.bordered,
          'smart-card-shadow': props.shadow
        }
      ]
    })

    const handleClick = (e) => {
      if (props.hoverable) {
        emit('click', e)
        uni.vibrateShort({ type: 'light' })
      }
    }

    return {
      hasHeader,
      hasExtra,
      hasFooter,
      cardClass,
      handleClick
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system/variables.scss';

.smart-card {
  background: $bg-white;
  border-radius: $border-radius-base;
  overflow: hidden;
  
  &-bordered {
    border: 1px solid $border-color;
  }
  
  &-shadow {
    box-shadow: $shadow-base;
  }
  
  &-hoverable {
    transition: all $animation-duration-base;
    
    &:active {
      transform: scale(0.98);
      box-shadow: $shadow-md;
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-md $spacing-sm;
  border-bottom: 1px solid $divider-color;
  
  .card-title-wrap {
    flex: 1;
    
    .card-title {
      font-size: $font-size-md;
      font-weight: $font-weight-bold;
      color: $text-primary;
    }
  }
  
  .card-extra {
    margin-left: $spacing-md;
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.card-body {
  padding: $spacing-md;
}

.card-footer {
  padding: $spacing-sm $spacing-md $spacing-md;
  border-top: 1px solid $divider-color;
}
</style>

