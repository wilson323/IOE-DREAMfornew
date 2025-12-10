<template>
  <view class="smart-table">
    <!-- 表格头部 -->
    <view v-if="showHeader || title" class="smart-table__header">
      <slot name="header">
        <view class="smart-table__title">
          <text class="smart-table__title-text">{{ title }}</text>
          <text v-if="subtitle" class="smart-table__subtitle">{{ subtitle }}</text>
        </view>
        <view class="smart-table__actions">
          <slot name="actions">
            <view v-if="showRefresh" class="smart-table__refresh" @click="handleRefresh">
              <text class="smart-table__refresh-icon">↻</text>
            </view>
            <view v-if="showExport" class="smart-table__export" @click="handleExport">
              <text class="smart-table__export-icon">📥</text>
            </view>
          </slot>
        </view>
      </slot>
    </view>

    <!-- 统计信息 -->
    <view v-if="showStats && stats" class="smart-table__stats">
      <view
        v-for="(stat, index) in statsData"
        :key="index"
        class="smart-table__stat-item"
        :class="`smart-table__stat-item--${stat.type || 'default'}`"
      >
        <text class="smart-table__stat-value">{{ stat.value }}</text>
        <text class="smart-table__stat-label">{{ stat.label }}</text>
      </view>
    </view>

    <!-- 表格内容 -->
    <view class="smart-table__container">
      <!-- 表格头 -->
      <view class="smart-table__thead">
        <view class="smart-table__tr">
          <view
            v-for="(column, index) in columns"
            :key="column.key || index"
            :class="[
              'smart-table__th',
              `smart-table__th--${column.align || 'left'}`,
              { 'smart-table__th--sortable': column.sortable }
            ]"
            :style="getCellStyle(column)"
            @click="handleSort(column)"
          >
            <text class="smart-table__th-text">{{ column.title }}</text>
            <view v-if="column.sortable" class="smart-table__sort-icon">
              <text
                :class="[
                  'smart-table__sort-arrow',
                  { 'smart-table__sort-arrow--active': sortField === column.key }
                ]"
              >
                {{ getSortIcon(column.key) }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 表格主体 -->
      <scroll-view
        class="smart-table__tbody"
        :scroll-y="true"
        :style="{ height: tableHeight }"
        @scrolltolower="handleScrollToLower"
      >
        <!-- 加载状态 -->
        <view v-if="loading && data.length === 0" class="smart-table__loading">
          <view class="smart-table__loading-spinner"></view>
          <text class="smart-table__loading-text">{{ loadingText }}</text>
        </view>

        <!-- 空状态 -->
        <view v-else-if="!loading && data.length === 0" class="smart-table__empty">
          <image
            :src="emptyImage"
            class="smart-table__empty-image"
            mode="aspectFit"
          />
          <text class="smart-table__empty-text">{{ emptyText }}</text>
        </view>

        <!-- 数据行 -->
        <view v-else class="smart-table__tbody-content">
          <view
            v-for="(row, rowIndex) in displayData"
            :key="getRowKey(row, rowIndex)"
            :class="[
              'smart-table__tr',
              { 'smart-table__tr--striped': striped && rowIndex % 2 === 1 },
              { 'smart-table__tr--selected': selectedRows.includes(getRowKey(row, rowIndex)) }
            ]"
            @click="handleRowClick(row, rowIndex)"
          >
            <view
              v-for="(column, colIndex) in columns"
              :key="column.key || colIndex"
              :class="[
                'smart-table__td',
                `smart-table__td--${column.align || 'left'}`
              ]"
              :style="getCellStyle(column)"
            >
              <!-- 自定义渲染 -->
              <slot
                :name="`cell-${column.key}`"
                :row="row"
                :column="column"
                :value="getCellValue(row, column)"
                :rowIndex="rowIndex"
                :colIndex="colIndex"
              >
                <!-- 默认渲染 -->
                <view class="smart-table__cell-content">
                  <text class="smart-table__cell-text">{{ getCellValue(row, column) }}</text>
                </view>
              </slot>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="showLoadMore" class="smart-table__load-more" @click="handleLoadMore">
          <view v-if="loadMoreLoading" class="smart-table__load-more-loading">
            <view class="smart-table__load-more-spinner"></view>
            <text class="smart-table__load-more-text">{{ loadMoreText }}</text>
          </view>
          <text v-else class="smart-table__load-more-text">{{ loadMoreText }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 表格底部 -->
    <view v-if="showFooter || $slots.footer" class="smart-table__footer">
      <slot name="footer">
        <view class="smart-table__pagination">
          <text class="smart-table__pagination-info">
            共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
          </text>
          <view class="smart-table__pagination-controls">
            <view
              :class="[
                'smart-table__page-btn',
                { 'smart-table__page-btn--disabled': currentPage <= 1 }
              ]"
              @click="handlePageChange(currentPage - 1)"
            >
              <text>上一页</text>
            </view>
            <view
              :class="[
                'smart-table__page-btn',
                { 'smart-table__page-btn--disabled': currentPage >= totalPages }
              ]"
              @click="handlePageChange(currentPage + 1)"
            >
              <text>下一页</text>
            </view>
          </view>
        </view>
      </slot>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

// Props定义
const props = defineProps({
  // 数据
  data: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    required: true
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
  showExport: {
    type: Boolean,
    default: false
  },
  showStats: {
    type: Boolean,
    default: false
  },

  // 分页
  pagination: {
    type: Object,
    default: () => ({
      current: 1,
      pageSize: 10,
      total: 0
    })
  },

  // 排序
  sortField: {
    type: String,
    default: ''
  },
  sortOrder: {
    type: String,
    default: '',
    validator: (value) => ['', 'ascend', 'descend'].includes(value)
  },

  // 选择
  rowSelection: {
    type: Object,
    default: null
  },

  // 样式
  striped: {
    type: Boolean,
    default: true
  },
  bordered: {
    type: Boolean,
    default: true
  },
  size: {
    type: String,
    default: 'middle',
    validator: (value) => ['small', 'middle', 'large'].includes(value)
  },

  // 其他配置
  tableHeight: {
    type: String,
    default: '500rpx'
  },
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  emptyImage: {
    type: String,
    default: '/static/images/empty.png'
  },
  rowKey: {
    type: [String, Function],
    default: 'id'
  },

  // 加载更多
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

  // 统计数据
  stats: {
    type: Array,
    default: () => []
  }
})

// Emits定义
const emit = defineEmits([
  'refresh',
  'export',
  'sort-change',
  'row-click',
  'selection-change',
  'page-change',
  'load-more'
])

// 响应式数据
const selectedRows = ref([])

// 计算属性
const currentPage = computed(() => props.pagination?.current || 1)
const pageSize = computed(() => props.pagination?.pageSize || 10)
const total = computed(() => props.pagination?.total || props.data.length)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const statsData = computed(() => {
  if (typeof props.stats === 'function') {
    return props.stats(props.data)
  }
  return props.stats
})

const displayData = computed(() => {
  // 如果有分页，返回当前页数据
  if (props.pagination) {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return props.data.slice(start, end)
  }
  return props.data
})

// 方法
const handleRefresh = () => {
  emit('refresh')
}

const handleExport = () => {
  emit('export')
}

const handleSort = (column) => {
  if (!column.sortable) return

  let newOrder = 'ascend'
  if (props.sortField === column.key) {
    if (props.sortOrder === 'ascend') {
      newOrder = 'descend'
    } else if (props.sortOrder === 'descend') {
      newOrder = ''
    }
  }

  emit('sort-change', {
    field: column.key,
    order: newOrder
  })
}

const handleRowClick = (row, index) => {
  emit('row-click', {
    row,
    index
  })

  // 处理行选择
  if (props.rowSelection) {
    const rowKey = getRowKey(row, index)
    const selectedIndex = selectedRows.value.indexOf(rowKey)

    if (selectedIndex === -1) {
      if (!props.rowSelection.multiple) {
        selectedRows.value = [rowKey]
      } else {
        selectedRows.value.push(rowKey)
      }
    } else {
      selectedRows.value.splice(selectedIndex, 1)
    }

    emit('selection-change', selectedRows.value)
  }
}

const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value) return
  emit('page-change', page)
}

const handleScrollToLower = () => {
  if (props.showLoadMore && !props.loadMoreLoading) {
    emit('load-more')
  }
}

const getRowKey = (row, index) => {
  if (typeof props.rowKey === 'function') {
    return props.rowKey(row, index)
  }
  return row[props.rowKey] || index
}

const getCellValue = (row, column) => {
  if (column.render) {
    return column.render(row, column)
  }

  const value = row[column.key || column.dataIndex]
  if (value === null || value === undefined) {
    return column.emptyText || '-'
  }

  if (column.formatter) {
    return column.formatter(value, row, column)
  }

  return value
}

const getCellStyle = (column) => {
  const style = {}
  if (column.width) {
    style.width = typeof column.width === 'string' ? column.width : `${column.width}rpx`
  }
  if (column.minWidth) {
    style.minWidth = typeof column.minWidth === 'string' ? column.minWidth : `${column.minWidth}rpx`
  }
  if (column.ellipsis) {
    style.textOverflow = 'ellipsis'
    style.whiteSpace = 'nowrap'
    style.overflow = 'hidden'
  }
  return style
}

const getSortIcon = (field) => {
  if (props.sortField !== field || !props.sortOrder) {
    return '⇅'
  }
  return props.sortOrder === 'ascend' ? '↑' : '↓'
}

// 暴露方法给父组件
defineExpose({
  clearSelection: () => {
    selectedRows.value = []
  },
  selectAll: () => {
    selectedRows.value = displayData.value.map((row, index) => getRowKey(row, index))
  },
  getSelectedRows: () => selectedRows.value
})
</script>

<style lang="scss" scoped>
.smart-table {
  background: #ffffff;
  border-radius: 12rpx;
  overflow: hidden;
}

.smart-table__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 2rpx solid #f0f0f0;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.smart-table__title {
  flex: 1;
}

.smart-table__title-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #262626;
}

.smart-table__subtitle {
  display: block;
  font-size: 24rpx;
  color: #8c8c8c;
  margin-top: 4rpx;
}

.smart-table__actions {
  display: flex;
  gap: 16rpx;
}

.smart-table__refresh,
.smart-table__export {
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

.smart-table__refresh-icon,
.smart-table__export-icon {
  font-size: 32rpx;
  color: #666;
}

.smart-table__stats {
  display: flex;
  padding: 20rpx 24rpx;
  background: #fafafa;
  border-bottom: 2rpx solid #f0f0f0;
}

.smart-table__stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  &--primary {
    .smart-table__stat-value {
      color: #1890ff;
    }
  }

  &--success {
    .smart-table__stat-value {
      color: #52c41a;
    }
  }

  &--warning {
    .smart-table__stat-value {
      color: #faad14;
    }
  }

  &--error {
    .smart-table__stat-value {
      color: #ff4d4f;
    }
  }
}

.smart-table__stat-value {
  font-size: 36rpx;
  font-weight: 600;
  color: #262626;
}

.smart-table__stat-label {
  font-size: 24rpx;
  color: #8c8c8c;
}

.smart-table__container {
  position: relative;
}

.smart-table__thead {
  background: #fafafa;
  border-bottom: 2rpx solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.smart-table__tr {
  display: flex;
  align-items: stretch;
  min-height: 88rpx;
}

.smart-table__th {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #262626;
  border-right: 2rpx solid #e0e0e0;
  background: #fafafa;
  position: relative;

  &:last-child {
    border-right: none;
  }

  &--sortable {
    cursor: pointer;
    transition: background-color 0.3s ease;

    &:active {
      background: #e8e8e8;
    }
  }

  &--left {
    justify-content: flex-start;
  }

  &--center {
    justify-content: center;
  }

  &--right {
    justify-content: flex-end;
  }
}

.smart-table__th-text {
  flex: 1;
}

.smart-table__sort-icon {
  flex-shrink: 0;
  margin-left: 8rpx;
}

.smart-table__sort-arrow {
  font-size: 24rpx;
  color: #c0c0c0;
  transition: color 0.3s ease;

  &--active {
    color: #1890ff;
  }
}

.smart-table__tbody {
  position: relative;
}

.smart-table__loading,
.smart-table__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.smart-table__loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #f0f0f0;
  border-top: 4rpx solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.smart-table__loading-text,
.smart-table__empty-text {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #8c8c8c;
}

.smart-table__empty-image {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 24rpx;
}

.smart-table__tbody-content {
  background: #ffffff;
}

.smart-table__td {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 20rpx 16rpx;
  font-size: 28rpx;
  color: #262626;
  border-right: 2rpx solid #f0f0f0;
  border-bottom: 2rpx solid #f0f0f0;
  min-height: 88rpx;

  &:last-child {
    border-right: none;
  }

  &--left {
    justify-content: flex-start;
    .smart-table__cell-content {
      justify-content: flex-start;
    }
  }

  &--center {
    justify-content: center;
    .smart-table__cell-content {
      justify-content: center;
    }
  }

  &--right {
    justify-content: flex-end;
    .smart-table__cell-content {
      justify-content: flex-end;
    }
  }
}

.smart-table__tr {
  &--striped {
    .smart-table__td {
      background: #fafafa;
    }
  }

  &--selected {
    .smart-table__td {
      background: #e6f7ff !important;
    }
  }

  &:active {
    .smart-table__td {
      background: #f5f5f5;
    }
  }
}

.smart-table__cell-content {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.smart-table__cell-text {
  word-break: break-all;
  line-height: 1.4;
}

.smart-table__load-more {
  padding: 32rpx;
  text-align: center;
  border-top: 2rpx solid #f0f0f0;
}

.smart-table__load-more-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.smart-table__load-more-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid #f0f0f0;
  border-top: 3rpx solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.smart-table__load-more-text {
  font-size: 28rpx;
  color: #8c8c8c;
}

.smart-table__footer {
  padding: 24rpx;
  background: #fafafa;
  border-top: 2rpx solid #e0e0e0;
}

.smart-table__pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.smart-table__pagination-info {
  font-size: 26rpx;
  color: #8c8c8c;
}

.smart-table__pagination-controls {
  display: flex;
  gap: 16rpx;
}

.smart-table__page-btn {
  padding: 16rpx 24rpx;
  background: #ffffff;
  border: 2rpx solid #d9d9d9;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #262626;
  transition: all 0.3s ease;

  &:active {
    background: #f5f5f5;
  }

  &--disabled {
    opacity: 0.5;
    cursor: not-allowed;

    &:active {
      background: #ffffff;
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>