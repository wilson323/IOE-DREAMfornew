<template>
  <view class="smart-list">
    <!-- 列表头部 -->
    <view v-if="showHeader || $slots.header" class="smart-list__header">
      <slot name="header">
        <view class="smart-list__title">
          <text class="smart-list__title-text">{{ title }}</text>
          <text v-if="subtitle" class="smart-list__subtitle">{{ subtitle }}</text>
        </view>
        <view class="smart-list__actions">
          <slot name="actions">
            <view v-if="showRefresh" class="smart-list__refresh" @click="handleRefresh">
              <text class="smart-list__refresh-icon">↻</text>
            </view>
            <view v-if="showFilter" class="smart-list__filter" @click="handleFilter">
              <text class="smart-list__filter-icon">⚙</text>
            </view>
          </slot>
        </view>
      </slot>
    </view>

    <!-- 搜索栏 -->
    <view v-if="searchable" class="smart-list__search">
      <input
        v-model="searchKeyword"
        :placeholder="searchPlaceholder"
        class="smart-list__search-input"
        @input="handleSearch"
        @confirm="handleSearchConfirm"
      />
      <view v-if="searchKeyword" class="smart-list__search-clear" @click="clearSearch">
        <text>✕</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view v-if="filterTags.length > 0" class="smart-list__filter-tags">
      <view
        v-for="(tag, index) in filterTags"
        :key="index"
        class="smart-list__filter-tag"
        @click="removeFilterTag(index)"
      >
        <text class="smart-list__filter-tag-text">{{ tag.text }}</text>
        <text class="smart-list__filter-tag-close">✕</text>
      </view>
    </view>

    <!-- 列表内容 -->
    <view class="smart-list__content">
      <!-- 加载状态 -->
      <view v-if="loading && data.length === 0" class="smart-list__loading">
        <view class="smart-list__loading-spinner"></view>
        <text class="smart-list__loading-text">{{ loadingText }}</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!loading && data.length === 0" class="smart-list__empty">
        <image
          :src="emptyImage"
          class="smart-list__empty-image"
          mode="aspectFit"
        />
        <text class="smart-list__empty-text">{{ emptyText }}</text>
        <view v-if="emptyAction" class="smart-list__empty-action" @click="handleEmptyAction">
          <text class="smart-list__empty-action-text">{{ emptyAction.text }}</text>
        </view>
      </view>

      <!-- 列表项 -->
      <view v-else class="smart-list__items">
        <view
          v-for="(item, index) in displayData"
          :key="getItemKey(item, index)"
          :class="[
            'smart-list__item',
            { 'smart-list__item--active': activeIndex === index },
            { 'smart-list__item--clickable': item.clickable !== false }
          ]"
          @click="handleItemClick(item, index)"
        >
          <slot name="item" :item="item" :index="index">
            <!-- 默认列表项模板 -->
            <view class="smart-list__item-content">
              <view class="smart-list__item-left">
                <image
                  v-if="item.image || item.avatar"
                  :src="item.image || item.avatar"
                  class="smart-list__item-image"
                  mode="aspectFill"
                />
                <view v-else-if="item.icon" class="smart-list__item-icon">
                  <text>{{ item.icon }}</text>
                </view>
              </view>

              <view class="smart-list__item-middle">
                <view class="smart-list__item-title">
                  <text class="smart-list__item-title-text">{{ item.title || item.name }}</text>
                  <view v-if="item.badge" class="smart-list__item-badge" :class="`smart-list__item-badge--${item.badge.type || 'default'}`">
                    <text>{{ item.badge.text }}</text>
                  </view>
                </view>
                <text v-if="item.subtitle || item.description" class="smart-list__item-subtitle">
                  {{ item.subtitle || item.description }}
                </text>
                <view v-if="item.tags" class="smart-list__item-tags">
                  <view
                    v-for="(tag, tagIndex) in item.tags"
                    :key="tagIndex"
                    class="smart-list__item-tag"
                    :class="`smart-list__item-tag--${tag.type || 'default'}`"
                  >
                    <text>{{ tag.text }}</text>
                  </view>
                </view>
              </view>

              <view class="smart-list__item-right">
                <text v-if="item.value" class="smart-list__item-value">{{ item.value }}</text>
                <text v-if="item.time" class="smart-list__item-time">{{ formatTime(item.time) }}</text>
                <text v-if="item.arrow !== false" class="smart-list__item-arrow">></text>
              </view>
            </view>
          </slot>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="showLoadMore" class="smart-list__load-more" @click="handleLoadMore">
        <view v-if="loadMoreLoading" class="smart-list__load-more-loading">
          <view class="smart-list__load-more-spinner"></view>
          <text class="smart-list__load-more-text">{{ loadMoreText }}</text>
        </view>
        <text v-else class="smart-list__load-more-text">{{ loadMoreText }}</text>
      </view>
    </view>

    <!-- 底部统计 -->
    <view v-if="showFooter" class="smart-list__footer">
      <slot name="footer">
        <text class="smart-list__footer-text">
          共 {{ total }} 条记录
          <text v-if="filteredCount !== total">，已筛选 {{ filteredCount }} 条</text>
        </text>
      </slot>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { debounce } from '@/utils/performance.js'

// Props定义
const props = defineProps({
  // 数据
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '加载中...'
  },

  // 标题
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },

  // 显示控制
  showHeader: {
    type: Boolean,
    default: false
  },
  showFooter: {
    type: Boolean,
    default: false
  },
  showRefresh: {
    type: Boolean,
    default: false
  },
  showFilter: {
    type: Boolean,
    default: false
  },
  searchable: {
    type: Boolean,
    default: false
  },

  // 搜索配置
  searchPlaceholder: {
    type: String,
    default: '请输入搜索关键词'
  },
  searchFields: {
    type: Array,
    default: () => ['title', 'name', 'description', 'subtitle']
  },

  // 空状态配置
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  emptyImage: {
    type: String,
    default: '/static/images/empty.png'
  },
  emptyAction: {
    type: Object,
    default: null
  },

  // 加载更多配置
  showLoadMore: {
    type: Boolean,
    default: false
  },
  loadMoreText: {
    type: String,
    default: '加载更多'
  },
  loadMoreLoading: {
    type: Boolean,
    default: false
  },

  // 分页
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  },

  // 其他配置
  itemKey: {
    type: [String, Function],
    default: 'id'
  }
})

// Emits定义
const emit = defineEmits([
  'refresh',
  'filter',
  'search',
  'item-click',
  'load-more',
  'empty-action',
  'filter-tag-remove'
])

// 响应式数据
const searchKeyword = ref('')
const filterTags = ref([])
const activeIndex = ref(-1)

// 计算属性
const filteredData = computed(() => {
  let result = [...props.data]

  // 搜索过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    result = result.filter(item => {
      return props.searchFields.some(field => {
        const value = item[field]
        return value && value.toString().toLowerCase().includes(keyword)
      })
    })
  }

  return result
})

const displayData = computed(() => {
  return filteredData.value
})

const filteredCount = computed(() => {
  return filteredData.value.length
})

// 防抖搜索
const debouncedSearch = debounce((keyword) => {
  emit('search', keyword)
}, 300)

// 方法
const handleRefresh = () => {
  emit('refresh')
}

const handleFilter = () => {
  emit('filter')
}

const handleSearch = (event) => {
  debouncedSearch(event.detail.value)
}

const handleSearchConfirm = (event) => {
  emit('search', event.detail.value)
}

const clearSearch = () => {
  searchKeyword.value = ''
  emit('search', '')
}

const handleItemClick = (item, index) => {
  activeIndex.value = index
  emit('item-click', {
    item,
    index
  })

  // 重置激活状态
  setTimeout(() => {
    activeIndex.value = -1
  }, 200)
}

const handleLoadMore = () => {
  if (!props.loadMoreLoading) {
    emit('load-more')
  }
}

const handleEmptyAction = () => {
  if (props.emptyAction && props.emptyAction.handler) {
    props.emptyAction.handler()
  }
  emit('empty-action')
}

const removeFilterTag = (index) => {
  const removedTag = filterTags.value[index]
  filterTags.value.splice(index, 1)
  emit('filter-tag-remove', removedTag)
}

const getItemKey = (item, index) => {
  if (typeof props.itemKey === 'function') {
    return props.itemKey(item, index)
  }
  return item[props.itemKey] || index
}

const formatTime = (time) => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return date.toLocaleDateString()
  }
}

// 监听数据变化
watch(() => props.data, () => {
  // 数据更新时重置搜索
  if (searchKeyword.value) {
    searchKeyword.value = ''
  }
}, { deep: true })

// 暴露方法给父组件
defineExpose({
  clearSearch,
  addFilterTag: (tag) => {
    filterTags.value.push(tag)
  },
  removeFilterTag,
  clearFilterTags: () => {
    filterTags.value = []
  }
})
</script>

<style lang="scss" scoped>
.smart-list {
  background: #ffffff;
  border-radius: 12rpx;
  overflow: hidden;
}

.smart-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 2rpx solid #f0f0f0;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.smart-list__title {
  flex: 1;
}

.smart-list__title-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #262626;
}

.smart-list__subtitle {
  display: block;
  font-size: 24rpx;
  color: #8c8c8c;
  margin-top: 4rpx;
}

.smart-list__actions {
  display: flex;
  gap: 16rpx;
}

.smart-list__refresh,
.smart-list__filter {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8rpx;
  transition: all 0.3s ease;

  &:active {
    background: #e0e0e0;
  }
}

.smart-list__refresh-icon,
.smart-list__filter-icon {
  font-size: 32rpx;
  color: #666;
}

.smart-list__search {
  position: relative;
  padding: 20rpx 24rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.smart-list__search-input {
  width: 100%;
  height: 72rpx;
  padding: 0 80rpx 0 24rpx;
  background: #f5f7fa;
  border-radius: 36rpx;
  font-size: 28rpx;
  color: #333;
  border: none;

  &::placeholder {
    color: #999;
  }
}

.smart-list__search-clear {
  position: absolute;
  right: 40rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ddd;
  border-radius: 50%;
  font-size: 24rpx;
  color: #666;
}

.smart-list__filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: #fafafa;
}

.smart-list__filter-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #e6f7ff;
  border-radius: 16rpx;
  border: 2rpx solid #91d5ff;
}

.smart-list__filter-tag-text {
  font-size: 24rpx;
  color: #1890ff;
}

.smart-list__filter-tag-close {
  font-size: 20rpx;
  color: #1890ff;
}

.smart-list__content {
  min-height: 200rpx;
}

.smart-list__loading,
.smart-list__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.smart-list__loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #f0f0f0;
  border-top: 4rpx solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.smart-list__loading-text,
.smart-list__empty-text {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #8c8c8c;
}

.smart-list__empty-image {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 24rpx;
}

.smart-list__empty-action {
  margin-top: 24rpx;
  padding: 16rpx 32rpx;
  background: #1890ff;
  border-radius: 24rpx;
}

.smart-list__empty-action-text {
  font-size: 28rpx;
  color: #ffffff;
}

.smart-list__items {
  background: #ffffff;
}

.smart-list__item {
  padding: 24rpx;
  border-bottom: 2rpx solid #f0f0f0;
  transition: all 0.3s ease;

  &:last-child {
    border-bottom: none;
  }

  &--clickable {
    &:active {
      background: #f5f5f5;
    }
  }

  &--active {
    background: #e6f7ff;
  }
}

.smart-list__item-content {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.smart-list__item-left {
  flex-shrink: 0;
}

.smart-list__item-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.smart-list__item-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  font-size: 32rpx;
  color: #ffffff;
}

.smart-list__item-middle {
  flex: 1;
  min-width: 0;
}

.smart-list__item-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.smart-list__item-title-text {
  font-size: 30rpx;
  font-weight: 500;
  color: #262626;
  line-height: 1.4;
}

.smart-list__item-badge {
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;

  &--default {
    background: #f0f0f0;
    color: #666;
  }

  &--primary {
    background: #e6f7ff;
    color: #1890ff;
  }

  &--success {
    background: #f6ffed;
    color: #52c41a;
  }

  &--warning {
    background: #fffbe6;
    color: #faad14;
  }

  &--error {
    background: #fff2f0;
    color: #ff4d4f;
  }
}

.smart-list__item-subtitle {
  font-size: 26rpx;
  color: #8c8c8c;
  line-height: 1.4;
  margin-bottom: 12rpx;
}

.smart-list__item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.smart-list__item-tag {
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;

  &--default {
    background: #f0f0f0;
    color: #666;
  }

  &--primary {
    background: #e6f7ff;
    color: #1890ff;
  }

  &--success {
    background: #f6ffed;
    color: #52c41a;
  }
}

.smart-list__item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
  flex-shrink: 0;
}

.smart-list__item-value {
  font-size: 30rpx;
  font-weight: 600;
  color: #1890ff;
}

.smart-list__item-time {
  font-size: 24rpx;
  color: #8c8c8c;
}

.smart-list__item-arrow {
  font-size: 24rpx;
  color: #c0c0c0;
}

.smart-list__load-more {
  padding: 32rpx;
  text-align: center;
  border-top: 2rpx solid #f0f0f0;
}

.smart-list__load-more-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.smart-list__load-more-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid #f0f0f0;
  border-top: 3rpx solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.smart-list__load-more-text {
  font-size: 28rpx;
  color: #8c8c8c;
}

.smart-list__footer {
  padding: 20rpx 24rpx;
  background: #fafafa;
  border-top: 2rpx solid #f0f0f0;
  text-align: center;
}

.smart-list__footer-text {
  font-size: 24rpx;
  color: #8c8c8c;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>