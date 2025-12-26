<template>
  <view class="virtual-list-container" :style="{ height: containerHeight }">
    <!-- 可滚动容器 -->
    <scroll-view
      class="virtual-scroll"
      :scroll-y="true"
      :scroll-top="scrollTop"
      @scroll="handleScroll"
      @scrolltoupper="handleScrollToUpper"
      @scrolltolower="handleScrollToLower"
    >

      <!-- 上方占位空间 -->
      <view :style="{ height: topSpacerHeight + 'px' }"></view>

      <!-- 可见区域列表项 -->
      <view
        v-for="item in visibleItems"
        :key="item._index"
        class="virtual-list-item"
        :style="{
          height: itemHeight + 'px',
          minHeight: itemHeight + 'px'
        }"
      >
        <slot :item="item.data" :index="item._index"></slot>
      </view>

      <!-- 下方占位空间 -->
      <view :style="{ height: bottomSpacerHeight + 'px' }"></view>

      <!-- 加载更多 -->
      <view
        v-if="hasMore"
        class="load-more"
        :style="{ height: loadMoreHeight + 'px' }"
      >
        <slot name="loading">
          <view class="loading-spinner">
            <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
            <text>加载中...</text>
          </view>
        </slot>
      </view>

      <!-- 空状态 -->
      <view v-if="isEmpty" class="empty-state">
        <slot name="empty">
          <uni-icons type="inbox" size="60" color="#ddd"></uni-icons>
          <text class="empty-text">暂无数据</text>
        </slot>
      </view>

    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

/**
 * 虚拟列表组件
 * 用于优化长列表性能，只渲染可见区域的列表项
 */

const props = defineProps({
  // 列表数据
  items: {
    type: Array,
    default: () => []
  },
  // 列表项高度
  itemHeight: {
    type: Number,
    default: 100
  },
  // 容器高度
  containerHeight: {
    type: String,
    default: '100vh'
  },
  // 缓冲区倍数（屏幕外的缓冲数量）
  bufferScale: {
    type: Number,
    default: 2
  },
  // 是否还有更多数据
  hasMore: {
    type: Boolean,
    default: false
  },
  // 加载更多高度
  loadMoreHeight: {
    type: Number,
    default: 50
  }
})

const emit = defineEmits(['loadMore', 'scrollToUpper'])

// 容器高度(px)
const containerHeightPx = ref(0)

// 可见区域起始索引
const startIndex = ref(0)

// 可见区域结束索引
const endIndex = ref(0)

// 滚动位置
const scrollTop = ref(0)

// 容器ref
const containerRef = ref(null)

// 计算可见列表项
const visibleItems = computed(() => {
  const result = []
  for (let i = startIndex.value; i <= endIndex.value; i++) {
    if (i < props.items.length) {
      result.push({
        _index: i,
        data: props.items[i]
      })
    }
  }
  return result
})

// 上方占位高度
const topSpacerHeight = computed(() => {
  return startIndex.value * props.itemHeight
})

// 下方占位高度
const bottomSpacerHeight = computed(() => {
  const remaining = props.items.length - endIndex.value - 1
  return Math.max(0, remaining * props.itemHeight)
})

// 是否为空
const isEmpty = computed(() => {
  return props.items.length === 0
})

// 获取可见区域索引范围
const getVisibleRange = (scrollTopValue) => {
  const containerHeight = containerHeightPx.value
  const bufferHeight = containerHeight * props.bufferScale

  const start = Math.floor(
    Math.max(0, scrollTopValue - bufferHeight) / props.itemHeight
  )

  const visibleCount = Math.ceil(
    (containerHeight + bufferHeight * 2) / props.itemHeight
  )

  const end = Math.min(
    props.items.length - 1,
    start + visibleCount
  )

  return { start, end }
}

// 更新可见范围
const updateVisibleRange = (scrollTopValue) => {
  const { start, end } = getVisibleRange(scrollTopValue)

  startIndex.value = start
  endIndex.value = end
}

// 处理滚动事件
const handleScroll = (e) => {
  const scrollTopValue = e.detail.scrollTop
  scrollTop.value = scrollTopValue

  updateVisibleRange(scrollTopValue)
}

// 滚动到顶部
const handleScrollToUpper = () => {
  emit('scrollToUpper')
}

// 滚动到底部
const handleScrollToLower = () => {
  if (props.hasMore) {
    emit('loadMore')
  }
}

// 滚动到指定索引
const scrollToIndex = (index) => {
  if (index < 0 || index >= props.items.length) {
    return
  }

  const targetScrollTop = index * props.itemHeight
  scrollTop.value = targetScrollTop
}

// 滚动到顶部
const scrollToTop = () => {
  scrollTop.value = 0
}

// 滚动到底部
const scrollToBottom = () => {
  const bottomScrollTop = Math.max(
    0,
    (props.items.length - 1) * props.itemHeight
  )
  scrollTop.value = bottomScrollTop
}

// 初始化容器高度
const initContainerHeight = () => {
  const query = uni.createSelectorQuery()

  query.select('.virtual-list-container').boundingClientRect((data) => {
    if (data) {
      containerHeightPx.value = data.height
      console.log('[虚拟列表] 容器高度:', data.height)
    }
  }).exec()
}

// 监听列表数据变化
watch(() => props.items, () => {
  updateVisibleRange(scrollTop.value)
}, { deep: true })

// 组件挂载
onMounted(() => {
  console.log('[虚拟列表] 组件挂载')
  initContainerHeight()
  updateVisibleRange(0)
})

// 组件卸载
onUnmounted(() => {
  console.log('[虚拟列表] 组件卸载')
})

// 暴露方法给父组件
defineExpose({
  scrollToIndex,
  scrollToTop,
  scrollToBottom
})
</script>

<style lang="scss" scoped>
.virtual-list-container {
  position: relative;
  width: 100%;
  overflow: hidden;
}

.virtual-scroll {
  width: 100%;
  height: 100%;
}

.virtual-list-item {
  width: 100%;
  box-sizing: border-box;
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;

  .loading-spinner {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #999;

    ::v-deep .uni-icons {
      margin-right: 8rpx;
      animation: rotate 1s linear infinite;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .empty-text {
    margin-top: 24rpx;
    font-size: 24rpx;
    color: #999;
  }
}
</style>
