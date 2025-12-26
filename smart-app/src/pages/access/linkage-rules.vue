<template>
  <view class="linkage-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">联动规则配置</text>
        </view>
        <view class="navbar-right">
          <view class="add-btn" @tap="addRule">
            <uni-icons type="plus" size="16" color="#fff"></uni-icons>
            <text class="add-text">添加</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 快速筛选 -->
      <view class="quick-filters">
        <scroll-view scroll-x class="filter-scroll" show-scrollbar>
          <view class="filter-chips">
            <view
              class="filter-chip"
              :class="{ active: selectedType === 'all' }"
              @tap="filterByType('all')"
            >
              <text>全部</text>
              <text class="chip-count">({{ totalCount }})</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedType === 'video' }"
              @tap="filterByType('video')"
            >
              <text>视频联动</text>
              <text class="chip-count">({{ videoCount }})</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedType === 'light' }"
              @tap="filterByType('light')"
            >
              <text>灯光联动</text>
              <text class="chip-count">({{ lightCount }})</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedType === 'ac' }"
              @tap="filterByType('ac')"
            >
              <text>空调联动</text>
              <text class="chip-count">({{ acCount }})</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedType === 'alarm' }"
              @tap="filterByType('alarm')"
            >
              <text>报警联动</text>
              <text class="chip-count">({{ alarmCount }})</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 联动规则列表 -->
      <view class="rules-list">
        <view
          class="rule-card"
          v-for="(rule, index) in filteredRules"
          :key="index"
          @tap="viewRuleDetail(rule)"
        >
          <view class="rule-header">
            <view class="rule-type" :class="rule.type">
              <uni-icons :type="getTypeIcon(rule.type)" size="16" color="#fff"></uni-icons>
              <text class="type-text">{{ getTypeName(rule.type) }}</text>
            </view>
            <view class="rule-status" :class="{ active: rule.enabled }">
              <text>{{ rule.enabled ? '已启用' : '已禁用' }}</text>
            </view>
          </view>
          <view class="rule-content">
            <text class="rule-name">{{ rule.name }}</text>
            <view class="rule-trigger">
              <uni-icons type="help" size="14" color="#999"></uni-icons>
              <text class="trigger-text">触发：{{ rule.trigger }}</text>
            </view>
            <view class="rule-action">
              <uni-icons type="flag" size="14" color="#999"></uni-icons>
              <text class="action-text">执行：{{ rule.action }}</text>
            </view>
          </view>
          <view class="rule-footer">
            <view class="rule-time">
              <uni-icons type="clock" size="12" color="#999"></uni-icons>
              <text class="time-text">更新于 {{ rule.updateTime }}</text>
            </view>
            <view class="rule-actions" @tap.stop>
              <view class="action-btn" @tap="toggleRuleStatus(rule)">
                <uni-icons
                  :type="rule.enabled ? 'eye' : 'eye-slash'"
                  size="16"
                  :color="rule.enabled ? '#52c41a' : '#999'"
                ></uni-icons>
              </view>
              <view class="action-btn" @tap="editRule(rule)">
                <uni-icons type="compose" size="16" color="#667eea"></uni-icons>
              </view>
              <view class="action-btn" @tap="testRule(rule)">
                <uni-icons type="play-circle" size="16" color="#faad14"></uni-icons>
              </view>
              <view class="action-btn" @tap="deleteRule(rule, index)">
                <uni-icons type="trash" size="16" color="#ff4d4f"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredRules.length === 0">
        <view class="empty-icon">
          <uni-icons type="link" size="64" color="#ccc"></uni-icons>
        </view>
        <text class="empty-title">暂无联动规则</text>
        <text class="empty-desc">点击添加按钮创建第一条联动规则</text>
        <view class="empty-btn" @tap="addRule">
          <text>添加规则</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 删除确认弹窗 -->
    <uni-popup ref="deletePopup" type="center">
      <view class="delete-popup">
        <view class="popup-header">
          <text class="popup-title">确认删除</text>
          <view class="popup-close" @tap="closeDeletePopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>
        <view class="popup-content">
          <text class="content-text">确定要删除该联动规则吗？此操作不可撤销。</text>
        </view>
        <view class="popup-actions">
          <view class="action-btn cancel" @tap="closeDeletePopup">取消</view>
          <view class="action-btn confirm" @tap="confirmDelete">删除</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)
const refreshing = ref(false)

// 筛选类型
const selectedType = ref('all')

// 联动规则列表
const rules = ref([
  {
    id: 1,
    type: 'video',
    name: '门禁通行录像',
    trigger: '门禁刷卡成功',
    action: '开启视频录像30秒',
    enabled: true,
    updateTime: '2小时前'
  },
  {
    id: 2,
    type: 'light',
    name: '办公室自动开灯',
    trigger: '门禁刷卡进入',
    action: '开启办公室灯光',
    enabled: true,
    updateTime: '1天前'
  },
  {
    id: 3,
    type: 'ac',
    name: '会议室空调联动',
    trigger: '会议室门禁进入',
    action: '开启空调至24度',
    enabled: true,
    updateTime: '3天前'
  },
  {
    id: 4,
    type: 'alarm',
    name: '非法闯入报警',
    trigger: '门禁强制开启',
    action: '触发声光报警+推送通知',
    enabled: true,
    updateTime: '5天前'
  },
  {
    id: 5,
    type: 'video',
    name: '异常抓拍',
    trigger: '门禁多次验证失败',
    action: '抓拍现场照片+视频录制',
    enabled: false,
    updateTime: '1周前'
  },
  {
    id: 6,
    type: 'light',
    name: '走廊灯光感应',
    trigger: '检测到人员活动',
    action: '开启走廊灯光5分钟',
    enabled: true,
    updateTime: '1周前'
  },
  {
    id: 7,
    type: 'alarm',
    name: '门长时间未关',
    trigger: '门开启超过5分钟',
    action: '蜂鸣器提醒+推送通知',
    enabled: true,
    updateTime: '2周前'
  },
  {
    id: 8,
    type: 'ac',
    name: '下班自动关闭空调',
    trigger: '所有门禁签退',
    action: '关闭该区域空调',
    enabled: false,
    updateTime: '2周前'
  }
])

// 待删除的规则
const ruleToDelete = ref(null)

// 计算属性 - 筛选后的规则
const filteredRules = computed(() => {
  if (selectedType.value === 'all') {
    return rules.value
  }
  return rules.value.filter(rule => rule.type === selectedType.value)
})

// 计数
const totalCount = computed(() => rules.value.length)
const videoCount = computed(() => rules.value.filter(r => r.type === 'video').length)
const lightCount = computed(() => rules.value.filter(r => r.type === 'light').length)
const acCount = computed(() => rules.value.filter(r => r.type === 'ac').length)
const alarmCount = computed(() => rules.value.filter(r => r.type === 'alarm').length)

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 获取类型图标
const getTypeIcon = (type) => {
  const iconMap = {
    video: 'videocam',
    light: 'lightbulb',
    ac: 'settings',
    alarm: 'sound'
  }
  return iconMap[type] || 'help'
}

// 获取类型名称
const getTypeName = (type) => {
  const nameMap = {
    video: '视频联动',
    light: '灯光联动',
    ac: '空调联动',
    alarm: '报警联动'
  }
  return nameMap[type] || '其他联动'
}

// 按类型筛选
const filterByType = (type) => {
  selectedType.value = type
}

// 查看规则详情
const viewRuleDetail = (rule) => {
  uni.navigateTo({
    url: `/pages/access/linkage-rule-detail?ruleId=${rule.id}`
  })
}

// 切换规则状态
const toggleRuleStatus = (rule) => {
  rule.enabled = !rule.enabled
  uni.showToast({
    title: rule.enabled ? '已启用' : '已禁用',
    icon: 'success'
  })
  // TODO: 调用API更新状态
}

// 编辑规则
const editRule = (rule) => {
  uni.navigateTo({
    url: `/pages/access/linkage-rule-edit?ruleId=${rule.id}`
  })
}

// 测试规则
const testRule = (rule) => {
  uni.showModal({
    title: '测试联动规则',
    content: `确定要测试"${rule.name}"吗？`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '测试中...' })
        // TODO: 调用实际测试API
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '测试成功',
            icon: 'success'
          })
        }, 1500)
      }
    }
  })
}

// 删除规则
const deleteRule = (rule, index) => {
  ruleToDelete.value = { rule, index }
  // 显示删除确认弹窗
  uni.$emit('showDeletePopup')
}

// 关闭删除弹窗
const closeDeletePopup = () => {
  ruleToDelete.value = null
  uni.$emit('hideDeletePopup')
}

// 确认删除
const confirmDelete = () => {
  if (ruleToDelete.value) {
    const { index } = ruleToDelete.value
    rules.value.splice(index, 1)
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    })
    closeDeletePopup()
    // TODO: 调用实际删除API
  }
}

// 添加规则
const addRule = () => {
  uni.navigateTo({
    url: '/pages/access/linkage-rule-add'
  })
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  // TODO: 重新加载数据
  setTimeout(() => {
    refreshing.value = false
  }, 1000)
}

// 加载更多
const loadMore = () => {
  // TODO: 分页加载
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.linkage-container {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1000;

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 30rpx;

    .navbar-left {
      display: flex;
      align-items: center;

      .navbar-title {
        margin-left: 20rpx;
        font-size: 18px;
        font-weight: 500;
        color: #fff;
      }
    }

    .navbar-right {
      .add-btn {
        display: flex;
        align-items: center;
        padding: 8rpx 20rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20rpx;

        .add-text {
          margin-left: 8rpx;
          font-size: 13px;
          color: #fff;
        }
      }
    }
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 44px);
}

// 快速筛选
.quick-filters {
  padding: 30rpx 0;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .filter-scroll {
    white-space: nowrap;

    .filter-chips {
      display: inline-flex;
      padding: 0 30rpx;

      .filter-chip {
        display: inline-flex;
        align-items: center;
        padding: 12rpx 24rpx;
        background: #f5f5f5;
        border-radius: 20rpx;
        margin-right: 20rpx;
        white-space: nowrap;
        transition: all 0.3s;

        &:last-child {
          margin-right: 0;
        }

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;

          .chip-count {
            color: rgba(255, 255, 255, 0.8);
          }
        }

        .chip-count {
          margin-left: 8rpx;
          font-size: 11px;
          color: #999;
        }
      }
    }
  }
}

// 规则列表
.rules-list {
  padding: 30rpx;

  .rule-card {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    &:last-child {
      margin-bottom: 0;
    }

    .rule-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .rule-type {
        display: flex;
        align-items: center;
        padding: 6rpx 16rpx;
        border-radius: 16rpx;
        font-size: 11px;
        color: #fff;

        &.video {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.light {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.ac {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        &.alarm {
          background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
        }

        .type-text {
          margin-left: 8rpx;
        }
      }

      .rule-status {
        padding: 4rpx 16rpx;
        background: #f0f0f0;
        border-radius: 12rpx;
        font-size: 10px;
        color: #999;

        &.active {
          background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
          color: #fff;
        }
      }
    }

    .rule-content {
      margin-bottom: 20rpx;

      .rule-name {
        display: block;
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 16rpx;
      }

      .rule-trigger,
      .rule-action {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;

        &:last-child {
          margin-bottom: 0;
        }

        text {
          margin-left: 10rpx;
          font-size: 12px;
          color: #666;
        }

        .trigger-text,
        .action-text {
          flex: 1;
        }
      }
    }

    .rule-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;

      .rule-time {
        display: flex;
        align-items: center;

        .time-text {
          margin-left: 8rpx;
          font-size: 11px;
          color: #999;
        }
      }

      .rule-actions {
        display: flex;
        gap: 20rpx;

        .action-btn {
          padding: 8rpx;
        }
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;

  .empty-icon {
    margin-bottom: 30rpx;
  }

  .empty-title {
    font-size: 16px;
    color: #999;
    margin-bottom: 10rpx;
  }

  .empty-desc {
    font-size: 13px;
    color: #ccc;
    margin-bottom: 40rpx;
  }

  .empty-btn {
    padding: 16rpx 48rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
    font-size: 14px;
    color: #fff;
  }
}

// 删除确认弹窗
.delete-popup {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      padding: 10rpx;
    }
  }

  .popup-content {
    padding: 40rpx 30rpx;

    .content-text {
      font-size: 14px;
      color: #666;
      line-height: 1.6;
    }
  }

  .popup-actions {
    display: flex;
    border-top: 1rpx solid #f0f0f0;

    .action-btn {
      flex: 1;
      height: 88rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 15px;
      font-weight: 500;

      &.cancel {
        color: #666;
        border-right: 1rpx solid #f0f0f0;
      }

      &.confirm {
        color: #ff4d4f;
      }
    }
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
