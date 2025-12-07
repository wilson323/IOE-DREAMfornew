<template>
  <view class="virtual-list" :style="{ height }">
    <scroll-view
      class="scroll-container"
      scroll-y
      :scroll-top="scrollTop"
      @scroll="handleScroll"
      :refresher-enabled="refresherEnabled"
      :refresher-triggered="refresherTriggered"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 占位元素（撑开滚动高度） -->
      <view class="spacer-top" :style="{ height: offsetY + 'px' }"></view>
      
      <!-- 渲染可见区域的项目 -->
      <view
        v-for="item in visibleItems"
        :key="getItemKey(item)"
        class="list-item"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item" :index="item._index"></slot>
      </view>
      
      <!-- 占位元素（撑开滚动高度） -->
      <view class="spacer-bottom" :style="{ height: bottomSpacerHeight + 'px' }"></view>
      
      <!-- 加载更多提示 -->
      <view v-if="loading" class="loading-tip">
        <text>加载中...</text>
      </view>
      <view v-else-if="!hasMore && items.length > 0" class="no-more-tip">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'

export default {
  name: 'VirtualList',
  
  props: {
    // 数据列表
    items: {
      type: Array,
      default: () => []
    },
    // 每项高度（px）
    itemHeight: {
      type: Number,
      default: 100
    },
    // 容器高度
    height: {
      type: String,
      default: '100vh'
    },
    // 缓冲区大小（渲染可见区域±buffer项）
    buffer: {
      type: Number,
      default: 5
    },
    // 唯一key字段名
    keyField: {
      type: String,
      default: 'id'
    },
    // 是否有更多数据
    hasMore: {
      type: Boolean,
      default: true
    },
    // 是否正在加载
    loading: {
      type: Boolean,
      default: false
    },
    // 是否启用下拉刷新
    refresherEnabled: {
      type: Boolean,
      default: true
    },
    // 下拉刷新状态
    refresherTriggered: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['load-more', 'refresh'],
  
  setup(props, { emit }) {
    const scrollTop = ref(0)
    const containerHeight = ref(0)
    
    // 计算偏移量
    const offsetY = computed(() => {
      const startIndex = Math.floor(scrollTop.value / props.itemHeight)
      return Math.max(0, (startIndex - props.buffer) * props.itemHeight)
    })
    
    // 计算底部占位高度
    const bottomSpacerHeight = computed(() => {
      const totalHeight = props.items.length * props.itemHeight
      const visibleHeight = offsetY.value + (visibleItems.value.length * props.itemHeight)
      return Math.max(0, totalHeight - visibleHeight)
    })
    
    // 计算可见项目
    const visibleItems = computed(() => {
      if (!props.items.length) return []
      
      const startIndex = Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.buffer)
      const endIndex = Math.min(
        props.items.length,
        Math.ceil((scrollTop.value + containerHeight.value) / props.itemHeight) + props.buffer
      )
      
      return props.items.slice(startIndex, endIndex).map((item, idx) => ({
        ...item,
        _index: startIndex + idx
      }))
    })
    
    // 获取项目key
    const getItemKey = (item) => {
      return item[props.keyField] || item._index
    }
    
    // 处理滚动
    const handleScroll = (e) => {
      scrollTop.value = e.detail.scrollTop
    }
    
    // 下拉刷新
    const onRefresh = () => {
      emit('refresh')
    }
    
    // 加载更多
    const onLoadMore = () => {
      if (props.hasMore && !props.loading) {
        emit('load-more')
      }
    }
    
    // 初始化容器高度
    onMounted(() => {
      uni.createSelectorQuery().select('.virtual-list').boundingClientRect(data => {
        if (data) {
          containerHeight.value = data.height
        }
      }).exec()
    })
    
    return {
      scrollTop,
      offsetY,
      bottomSpacerHeight,
      visibleItems,
      getItemKey,
      handleScroll,
      onRefresh,
      onLoadMore
    }
  }
}
</script>

<style lang="scss" scoped>
.virtual-list {
  position: relative;
  overflow: hidden;
}

.scroll-container {
  height: 100%;
}

.list-item {
  overflow: hidden;
}

.loading-tip,
.no-more-tip {
  text-align: center;
  padding: 32rpx;
  font-size: 28rpx;
  color: rgba(0, 0, 0, 0.45);
}
</style>

