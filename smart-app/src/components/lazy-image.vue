<template>
  <view class="lazy-image-container" :style="{ width: width, height: height }">
    <!-- 加载中占位 -->
    <view
      v-if="!loaded && !error"
      class="image-placeholder"
      :style="{
        width: width,
        height: height,
        borderRadius: borderRadius
      }"
    >
      <slot name="placeholder">
        <view class="placeholder-content">
          <uni-icons type="image" size="40" color="#ddd"></uni-icons>
          <text class="placeholder-text">加载中...</text>
        </view>
      </slot>
    </view>

    <!-- 实际图片 -->
    <image
      v-show="loaded && !error"
      :src="actualSrc"
      :mode="mode"
      :lazy-load="true"
      :class="['lazy-image', { 'fade-in': loaded }]"
      :style="{
        width: width,
        height: height,
        borderRadius: borderRadius
      }"
      @load="handleLoad"
      @error="handleError"
    />

    <!-- 错误状态 -->
    <view
      v-if="error"
      class="image-error"
      :style="{
        width: width,
        height: height,
        borderRadius: borderRadius
      }"
    >
      <slot name="error">
        <view class="error-content">
          <uni-icons type="info" size="40" color="#ddd"></uni-icons>
          <text class="error-text">加载失败</text>
        </view>
      </slot>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

/**
 * 图片懒加载组件
 * 支持占位图、错误提示、淡入动画
 */

const props = defineProps({
  // 图片地址
  src: {
    type: String,
    required: true
  },
  // 宽度
  width: {
    type: String,
    default: '100%'
  },
  // 高度
  height: {
    type: String,
    default: '200rpx'
  },
  // 图片裁剪、缩放模式
  mode: {
    type: String,
    default: 'aspectFill'
  },
  // 圆角
  borderRadius: {
    type: String,
    default: '0'
  },
  // 默认图片
  defaultImage: {
    type: String,
    default: ''
  },
  // 错误图片
  errorImage: {
    type: String,
    default: ''
  },
  // 是否使用IntersectionObserver
  useObserver: {
    type: Boolean,
    default: true
  },
  // 观察器选项
  observerOptions: {
    type: Object,
    default: () => ({
      rootMargin: '50px'
    })
  }
})

const emit = defineEmits(['load', 'error'])

// 是否已加载
const loaded = ref(false)

// 是否加载失败
const error = ref(false)

// 是否在视口中
const inView = ref(!props.useObserver)

// 实际显示的图片地址
const actualSrc = computed(() => {
  if (error.value && props.errorImage) {
    return props.errorImage
  }
  if (!inView.value) {
    return props.defaultImage || ''
  }
  return props.src
})

// IntersectionObserver实例
let observer = null

// 处理图片加载成功
const handleLoad = (e) => {
  loaded.value = true
  error.value = false
  emit('load', e)
  console.log('[懒加载图片] 加载成功:', props.src)
}

// 处理图片加载失败
const handleError = (e) => {
  error.value = true
  loaded.value = false
  emit('error', e)
  console.warn('[懒加载图片] 加载失败:', props.src)
}

// 初始化IntersectionObserver
const initObserver = () => {
  if (!props.useObserver) {
    return
  }

  // 创建IntersectionObserver
  observer = uni.createIntersectionObserver((relative) => {
    if (relative.intersectRect.width > 0 || relative.intersectRect.height > 0) {
      // 进入视口
      inView.value = true
      console.log('[懒加载图片] 进入视口:', props.src)

      // 停止观察
      if (observer) {
        observer.disconnect()
        observer = null
      }
    }
  }, props.observerOptions)

  // 开始观察
  observer.observe('.lazy-image-container', this)

  console.log('[懒加载图片] 开始观察:', props.src)
}

// 组件挂载
onMounted(() => {
  if (props.useObserver) {
    initObserver()
  } else {
    inView.value = true
  }
})

// 组件卸载
onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})

// 监听src变化
watch(() => props.src, (newSrc, oldSrc) => {
  if (newSrc !== oldSrc) {
    loaded.value = false
    error.value = false
    inView.value = !props.useObserver

    if (props.useObserver) {
      // 重新观察
      if (observer) {
        observer.disconnect()
      }
      initObserver()
    }
  }
})

// 手动触发加载
const loadImage = () => {
  inView.value = true
  if (observer) {
    observer.disconnect()
    observer = null
  }
}

// 重置状态
const reset = () => {
  loaded.value = false
  error.value = false
  inView.value = !props.useObserver
}

// 暴露方法给父组件
defineExpose({
  loadImage,
  reset
})
</script>

<style lang="scss" scoped>
.lazy-image-container {
  position: relative;
  overflow: hidden;
}

.image-placeholder,
.image-error {
  position: absolute;
  top: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.placeholder-content,
.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 24rpx;
  color: #999;

  .placeholder-text,
  .error-text {
    margin-top: 16rpx;
  }
}

.lazy-image {
  display: block;
  width: 100%;
  height: 100%;
  will-change: opacity;

  &.fade-in {
    animation: fadeIn 0.3s ease-in-out;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
