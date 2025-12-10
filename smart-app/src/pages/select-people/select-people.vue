<template>
  <view class="select-people-page">
    <!-- 头部选择区域 -->
    <view class="header-section">
      <view class="select-all-card" @click="toggleSelectAll">
        <view class="select-indicator" :class="{ selected: isAllSelected }">
          <text v-if="isAllSelected" class="check-icon">✓</text>
        </view>
        <text class="select-text">全选</text>
        <text class="total-count">共{{ peopleList.length }}人</text>
      </view>
    </view>

    <!-- 人员列表 -->
    <scroll-view class="people-scroll" scroll-y :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="people-list">
        <view
          v-for="(person, index) in peopleList"
          :key="person.id"
          class="person-card"
          :class="{ selected: selectedPeople.includes(person.id) }"
          @click="togglePersonSelection(person.id)"
        >
          <!-- 选择指示器 -->
          <view class="selection-area">
            <view class="select-indicator" :class="{ selected: selectedPeople.includes(person.id) }">
              <text v-if="selectedPeople.includes(person.id)" class="check-icon">✓</text>
            </view>
          </view>

          <!-- 人员信息 -->
          <view class="person-info">
            <view class="person-header">
              <text class="person-name">{{ person.name }}</text>
              <view class="person-status" :class="person.status">
                {{ getStatusText(person.status) }}
              </view>
            </view>
            <text class="person-role">{{ person.role }}</text>
            <text class="person-department">{{ person.department }}</text>
          </view>

          <!-- 头像 -->
          <view class="avatar-section">
            <image :src="person.avatar" class="person-avatar" mode="aspectFill" />
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="peopleList.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">👥</text>
        <text class="empty-text">暂无可选人员</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-action-bar">
      <view class="action-content">
        <view class="selection-info">
          <text class="selected-count">已选择 {{ selectedPeople.length }} 人</text>
          <view class="selected-preview">
            <text
              v-for="(personId, index) in selectedPeople.slice(0, 3)"
              :key="personId"
              class="preview-item"
            >
              {{ getPersonById(personId)?.name }}{{ index === 2 && selectedPeople.length > 3 ? '...' : '' }}
            </text>
          </view>
        </view>

        <button
          class="confirm-btn"
          :class="{ disabled: selectedPeople.length === 0 }"
          :disabled="selectedPeople.length === 0"
          @click="confirmSelection"
        >
          确认选择 ({{ selectedPeople.length }}/{{ maxSelection }})
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 响应式状态
const loading = ref(false)
const refreshing = ref(false)
const selectedPeople = ref([])
const maxSelection = ref(12)

// 模拟人员数据
const peopleList = ref([
  {
    id: 1,
    name: '张三',
    role: '架构师',
    department: '技术部',
    avatar: '/src/static/images/avatar/default.png',
    status: 'online'
  },
  {
    id: 2,
    name: '李四',
    role: '前端工程师',
    department: '技术部',
    avatar: '/src/static/images/avatar/default.png',
    status: 'online'
  },
  {
    id: 3,
    name: '王五',
    role: '后端工程师',
    department: '技术部',
    avatar: '/src/static/images/avatar/default.png',
    status: 'offline'
  },
  {
    id: 4,
    name: '赵六',
    role: '产品经理',
    department: '产品部',
    avatar: '/src/static/images/avatar/default.png',
    status: 'busy'
  },
  {
    id: 5,
    name: '卓大',
    role: '架构师',
    department: '技术部',
    avatar: '/src/static/images/avatar/default.png',
    status: 'online'
  }
])

// 计算属性
const isAllSelected = computed(() => {
  return peopleList.value.length > 0 && selectedPeople.value.length === peopleList.value.length
})

// 生命周期钩子
onMounted(() => {
  loadPeopleList()
})

onShow(() => {
  // 页面显示时可以刷新数据
})

onPullDownRefresh(() => {
  onRefresh()
})

// 方法
const loadPeopleList = async () => {
  try {
    loading.value = true
    // 这里可以调用API获取真实数据
    // const res = await api.getPeopleList()
    // peopleList.value = res.data || []

    // 模拟加载延迟
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    console.error('加载人员列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedPeople.value = []
  } else {
    selectedPeople.value = peopleList.value.map(person => person.id)
  }
  uni.vibrateShort()
}

const togglePersonSelection = (personId) => {
  const index = selectedPeople.value.indexOf(personId)
  if (index > -1) {
    selectedPeople.value.splice(index, 1)
  } else {
    if (selectedPeople.value.length >= maxSelection.value) {
      uni.showToast({
        title: `最多选择${maxSelection.value}人`,
        icon: 'none'
      })
      return
    }
    selectedPeople.value.push(personId)
  }
  uni.vibrateShort()
}

const confirmSelection = () => {
  if (selectedPeople.value.length === 0) {
    uni.showToast({
      title: '请至少选择一人',
      icon: 'none'
    })
    return
  }

  const selectedData = selectedPeople.value.map(id => getPersonById(id)).filter(Boolean)

  // 返回选中的数据
  uni.$emit('people-selected', selectedData)

  uni.showToast({
    title: `已选择${selectedPeople.value.length}人`,
    icon: 'success'
  })

  // 返回上一页
  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}

const getPersonById = (id) => {
  return peopleList.value.find(person => person.id === id)
}

const getStatusText = (status) => {
  const statusMap = {
    online: '在线',
    offline: '离线',
    busy: '忙碌'
  }
  return statusMap[status] || '未知'
}

const onRefresh = async () => {
  refreshing.value = true
  await loadPeopleList()
  refreshing.value = false
  uni.stopPullDownRefresh()
}
</script>

<style lang="scss" scoped>
page {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.select-people-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 168rpx;
}

.header-section {
  padding: 32rpx 24rpx 16rpx;
}

.select-all-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  border-radius: 20rpx;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    background: rgba(255, 255, 255, 0.9);
  }

  .select-indicator {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    border: 3rpx solid #e0e0e0;
    margin-right: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;

    &.selected {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-color: #667eea;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.4);

      .check-icon {
        color: #fff;
        font-size: 28rpx;
        font-weight: bold;
      }
    }
  }

  .select-text {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-right: 16rpx;
  }

  .total-count {
    font-size: 24rpx;
    color: #666;
    background: rgba(102, 126, 234, 0.1);
    padding: 8rpx 16rpx;
    border-radius: 12rpx;
  }
}

.people-scroll {
  height: calc(100vh - 320rpx);
  padding: 0 24rpx;
}

.people-list {
  .person-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10rpx);
    border-radius: 20rpx;
    margin-bottom: 16rpx;
    padding: 24rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
    border: 1rpx solid rgba(255, 255, 255, 0.2);
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &:active {
      transform: scale(0.98);
      background: rgba(255, 255, 255, 0.9);
    }

    &.selected {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      border-color: #667eea;
      box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.2);

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 6rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
    }

    .selection-area {
      margin-right: 24rpx;

      .select-indicator {
        width: 44rpx;
        height: 44rpx;
        border-radius: 50%;
        border: 3rpx solid #e0e0e0;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;

        &.selected {
          background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
          border-color: #52c41a;
          box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.4);

          .check-icon {
            color: #fff;
            font-size: 24rpx;
            font-weight: bold;
          }
        }
      }
    }

    .person-info {
      flex: 1;

      .person-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8rpx;

        .person-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
        }

        .person-status {
          padding: 6rpx 16rpx;
          border-radius: 12rpx;
          font-size: 22rpx;
          font-weight: 500;

          &.online {
            background: rgba(82, 196, 26, 0.1);
            color: #52c41a;
          }

          &.offline {
            background: rgba(140, 140, 140, 0.1);
            color: #8c8c8c;
          }

          &.busy {
            background: rgba(250, 140, 22, 0.1);
            color: #fa8c16;
          }
        }
      }

      .person-role {
        font-size: 28rpx;
        color: #666;
        margin-bottom: 4rpx;
        display: block;
      }

      .person-department {
        font-size: 24rpx;
        color: #999;
        display: block;
      }
    }

    .avatar-section {
      margin-left: 16rpx;

      .person-avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        border: 3rpx solid rgba(255, 255, 255, 0.8);
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 24rpx;
    opacity: 0.6;
  }

  .empty-text {
    font-size: 28rpx;
    color: #fff;
    opacity: 0.8;
  }
}

.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20rpx);
  padding: 24rpx 32rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.1);

  .action-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24rpx;

    .selection-info {
      flex: 1;

      .selected-count {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
        display: block;
      }

      .selected-preview {
        display: flex;
        gap: 12rpx;
        flex-wrap: wrap;

        .preview-item {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
          padding: 6rpx 16rpx;
          border-radius: 12rpx;
          font-size: 22rpx;
          font-weight: 500;
        }
      }
    }

    .confirm-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border: none;
      border-radius: 50rpx;
      padding: 0 48rpx;
      height: 88rpx;
      font-size: 30rpx;
      font-weight: 600;
      box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
      transition: all 0.3s ease;
      min-width: 200rpx;

      &:active:not(.disabled) {
        transform: scale(0.95);
        box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.4);
      }

      &.disabled {
        background: #e0e0e0;
        color: #999;
        box-shadow: none;
        cursor: not-allowed;
      }
    }
  }
}
</style>
