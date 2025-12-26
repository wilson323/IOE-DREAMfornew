<template>
  <view class="logs-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">系统日志</text>
        </view>
        <view class="navbar-right">
          <view class="filter-btn" @tap="showFilterPopup">
            <uni-icons type="settings" size="16" color="#fff"></uni-icons>
            <text class="filter-text">筛选</text>
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
              :class="{ active: selectedLevel === 'all' }"
              @tap="filterByLevel('all')"
            >
              <text>全部</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedLevel === 'error' }"
              @tap="filterByLevel('error')"
            >
              <view class="chip-dot error"></view>
              <text>错误</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedLevel === 'warn' }"
              @tap="filterByLevel('warn')"
            >
              <view class="chip-dot warn"></view>
              <text>警告</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedLevel === 'info' }"
              @tap="filterByLevel('info')"
            >
              <view class="chip-dot info"></view>
              <text>信息</text>
            </view>
            <view
              class="filter-chip"
              :class="{ active: selectedLevel === 'debug' }"
              @tap="filterByLevel('debug')"
            >
              <view class="chip-dot debug"></view>
              <text>调试</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 日志列表 -->
      <view class="logs-list">
        <view
          class="log-item"
          v-for="(log, index) in filteredLogs"
          :key="index"
          @tap="viewLogDetail(log)"
        >
          <view class="log-level" :class="log.level">
            <text>{{ getLevelText(log.level) }}</text>
          </view>
          <view class="log-content">
            <text class="log-message">{{ log.message }}</text>
            <view class="log-meta">
              <text class="log-module">{{ log.module }}</text>
              <text class="log-separator">·</text>
              <text class="log-time">{{ log.time }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore">
        <text>加载更多...</text>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 高级筛选弹窗 -->
    <uni-popup ref="filterPopup" type="right">
      <view class="filter-popup">
        <view class="popup-header">
          <text class="popup-title">筛选条件</text>
          <view class="popup-close" @tap="closeFilterPopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <!-- 日志级别 -->
          <view class="filter-group">
            <text class="group-title">日志级别</text>
            <view class="group-options">
              <view
                class="option-item"
                :class="{ active: filterConfig.level.includes('error') }"
                @tap="toggleLevel('error')"
              >
                <view class="option-dot error"></view>
                <text>错误</text>
              </view>
              <view
                class="option-item"
                :class="{ active: filterConfig.level.includes('warn') }"
                @tap="toggleLevel('warn')"
              >
                <view class="option-dot warn"></view>
                <text>警告</text>
              </view>
              <view
                class="option-item"
                :class="{ active: filterConfig.level.includes('info') }"
                @tap="toggleLevel('info')"
              >
                <view class="option-dot info"></view>
                <text>信息</text>
              </view>
              <view
                class="option-item"
                :class="{ active: filterConfig.level.includes('debug') }"
                @tap="toggleLevel('debug')"
              >
                <view class="option-dot debug"></view>
                <text>调试</text>
              </view>
            </view>
          </view>

          <!-- 模块选择 -->
          <view class="filter-group">
            <text class="group-title">模块</text>
            <view class="group-options">
              <view
                class="option-tag"
                :class="{ active: filterConfig.modules.includes('access') }"
                @tap="toggleModule('access')"
              >
                <text>门禁管理</text>
              </view>
              <view
                class="option-tag"
                :class="{ active: filterConfig.modules.includes('attendance') }"
                @tap="toggleModule('attendance')"
              >
                <text>考勤管理</text>
              </view>
              <view
                class="option-tag"
                :class="{ active: filterConfig.modules.includes('consume') }"
                @tap="toggleModule('consume')"
              >
                <text>消费管理</text>
              </view>
              <view
                class="option-tag"
                :class="{ active: filterConfig.modules.includes('visitor') }"
                @tap="toggleModule('visitor')"
              >
                <text>访客管理</text>
              </view>
              <view
                class="option-tag"
                :class="{ active: filterConfig.modules.includes('video') }"
                @tap="toggleModule('video')"
              >
                <text>视频监控</text>
              </view>
              <view
                class="option-tag"
                :class="{ active: filterConfig.modules.includes('system') }"
                @tap="toggleModule('system')"
              >
                <text>系统</text>
              </view>
            </view>
          </view>

          <!-- 时间范围 -->
          <view class="filter-group">
            <text class="group-title">时间范围</text>
            <view class="group-options">
              <view
                class="option-item"
                :class="{ active: filterConfig.timeRange === '1h' }"
                @tap="selectTimeRange('1h')"
              >
                <text>最近1小时</text>
              </view>
              <view
                class="option-item"
                :class="{ active: filterConfig.timeRange === '24h' }"
                @tap="selectTimeRange('24h')"
              >
                <text>最近24小时</text>
              </view>
              <view
                class="option-item"
                :class="{ active: filterConfig.timeRange === '7d' }"
                @tap="selectTimeRange('7d')"
              >
                <text>最近7天</text>
              </view>
              <view
                class="option-item"
                :class="{ active: filterConfig.timeRange === '30d' }"
                @tap="selectTimeRange('30d')"
              >
                <text>最近30天</text>
              </view>
            </view>
          </view>

          <!-- 关键词搜索 -->
          <view class="filter-group">
            <text class="group-title">关键词搜索</text>
            <view class="search-box">
              <input
                class="search-input"
                v-model="filterConfig.keyword"
                placeholder="输入关键词搜索"
                @confirm="applyFilter"
              />
            </view>
          </view>
        </scroll-view>

        <view class="popup-actions">
          <view class="action-btn reset" @tap="resetFilter">重置</view>
          <view class="action-btn apply" @tap="applyFilter">应用</view>
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
const hasMore = ref(true)

// 选中的级别
const selectedLevel = ref('all')

// 筛选配置
const filterConfig = reactive({
  level: ['error', 'warn', 'info', 'debug'],
  modules: ['access', 'attendance', 'consume', 'visitor', 'video', 'system'],
  timeRange: '24h',
  keyword: ''
})

// 日志列表
const logs = ref([
  {
    id: 1,
    level: 'error',
    module: '门禁管理',
    message: '设备DEV001连接失败，无法获取状态',
    time: '2分钟前',
    details: '设备通讯超时，请检查设备网络连接'
  },
  {
    id: 2,
    level: 'warn',
    module: '考勤管理',
    message: '用户张三今天尚未打卡',
    time: '15分钟前',
    details: '该用户应在8:00-9:00之间打卡'
  },
  {
    id: 3,
    level: 'info',
    module: '消费管理',
    message: '设备POS002完成离线数据同步',
    time: '30分钟前',
    details: '同步了15条消费记录'
  },
  {
    id: 4,
    level: 'info',
    module: '访客管理',
    message: '访客李四的预约已过期',
    time: '1小时前',
    details: '访客预约ID:V20241223001'
  },
  {
    id: 5,
    level: 'error',
    module: '视频监控',
    message: '摄像头CAM003录像存储空间不足',
    time: '2小时前',
    details: '可用空间小于5%，请及时清理'
  },
  {
    id: 6,
    level: 'debug',
    module: '系统',
    message: '定时任务执行成功：清理临时文件',
    time: '3小时前',
    details: '清理了128个临时文件，释放空间156MB'
  },
  {
    id: 7,
    level: 'info',
    module: '门禁管理',
    message: '设备DEV005固件升级完成',
    time: '5小时前',
    details: '从v2.2.0升级到v2.3.5'
  },
  {
    id: 8,
    level: 'warn',
    module: '系统',
    message: '数据库连接池使用率超过80%',
    time: '6小时前',
    details: '当前连接数:18/20，建议增加连接池大小'
  },
  {
    id: 9,
    level: 'info',
    module: '考勤管理',
    message: '自动排班任务执行完成',
    time: '8小时前',
    details: '为下周生成了256条排班记录'
  },
  {
    id: 10,
    level: 'debug',
    module: '消费管理',
    message: '账户余额同步完成',
    time: '12小时前',
    details: '同步了128个账户的余额信息'
  }
])

// 计算属性 - 筛选后的日志
const filteredLogs = computed(() => {
  let result = logs.value

  // 按级别筛选
  if (selectedLevel.value !== 'all') {
    result = result.filter(log => log.level === selectedLevel.value)
  }

  return result
})

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 获取级别文本
const getLevelText = (level) => {
  const levelMap = {
    error: '错误',
    warn: '警告',
    info: '信息',
    debug: '调试'
  }
  return levelMap[level] || '未知'
}

// 按级别筛选
const filterByLevel = (level) => {
  selectedLevel.value = level
}

// 查看日志详情
const viewLogDetail = (log) => {
  uni.navigateTo({
    url: `/pages/access/log-detail?logId=${log.id}`
  })
}

// 显示筛选弹窗
const showFilterPopup = () => {
  uni.$emit('showFilterPopup')
}

// 关闭筛选弹窗
const closeFilterPopup = () => {
  uni.$emit('hideFilterPopup')
}

// 切换级别
const toggleLevel = (level) => {
  const index = filterConfig.level.indexOf(level)
  if (index > -1) {
    filterConfig.level.splice(index, 1)
  } else {
    filterConfig.level.push(level)
  }
}

// 切换模块
const toggleModule = (module) => {
  const index = filterConfig.modules.indexOf(module)
  if (index > -1) {
    filterConfig.modules.splice(index, 1)
  } else {
    filterConfig.modules.push(module)
  }
}

// 选择时间范围
const selectTimeRange = (range) => {
  filterConfig.timeRange = range
}

// 应用筛选
const applyFilter = () => {
  closeFilterPopup()
  // TODO: 根据筛选条件重新加载日志
  uni.showToast({
    title: '筛选已应用',
    icon: 'success'
  })
}

// 重置筛选
const resetFilter = () => {
  filterConfig.level = ['error', 'warn', 'info', 'debug']
  filterConfig.modules = ['access', 'attendance', 'consume', 'visitor', 'video', 'system']
  filterConfig.timeRange = '24h'
  filterConfig.keyword = ''
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
  if (!hasMore.value) return
  // TODO: 分页加载
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.logs-container {
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
      .filter-btn {
        display: flex;
        align-items: center;
        padding: 8rpx 20rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20rpx;

        .filter-text {
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
        padding: 12rpx 20rpx;
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
        }

        .chip-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 6rpx;
          margin-right: 10rpx;

          &.error {
            background: #ff4d4f;
          }

          &.warn {
            background: #faad14;
          }

          &.info {
            background: #1890ff;
          }

          &.debug {
            background: #52c41a;
          }
        }
      }
    }
  }
}

// 日志列表
.logs-list {
  padding: 30rpx;

  .log-item {
    display: flex;
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    &:last-child {
      margin-bottom: 0;
    }

    .log-level {
      width: 64rpx;
      height: 64rpx;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
      flex-shrink: 0;
      font-size: 12px;
      color: #fff;

      &.error {
        background: #ff4d4f;
      }

      &.warn {
        background: #faad14;
      }

      &.info {
        background: #1890ff;
      }

      &.debug {
        background: #52c41a;
      }
    }

    .log-content {
      flex: 1;

      .log-message {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 10rpx;
        line-height: 1.5;
      }

      .log-meta {
        display: flex;
        align-items: center;
        font-size: 11px;
        color: #999;

        .log-separator {
          margin: 0 10rpx;
        }
      }
    }
  }
}

// 加载更多
.load-more {
  padding: 30rpx;
  text-align: center;
  font-size: 12px;
  color: #999;
}

// 筛选弹窗
.filter-popup {
  width: 600rpx;
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;

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
    flex: 1;
    padding: 30rpx;

    .filter-group {
      margin-bottom: 40rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .group-title {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 20rpx;
      }

      .group-options {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;

        .option-item {
          display: flex;
          align-items: center;
          padding: 12rpx 20rpx;
          background: #f5f5f5;
          border-radius: 12rpx;
          border: 1rpx solid transparent;
          font-size: 12px;
          color: #666;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
            border-color: #667eea;
            color: #667eea;
          }

          .option-dot {
            width: 12rpx;
            height: 12rpx;
            border-radius: 6rpx;
            margin-right: 10rpx;

            &.error {
              background: #ff4d4f;
            }

            &.warn {
              background: #faad14;
            }

            &.info {
              background: #1890ff;
            }

            &.debug {
              background: #52c41a;
            }
          }
        }

        .option-tag {
          padding: 12rpx 20rpx;
          background: #f5f5f5;
          border-radius: 12rpx;
          border: 1rpx solid transparent;
          font-size: 12px;
          color: #666;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: transparent;
            color: #fff;
          }
        }
      }

      .search-box {
        .search-input {
          width: 100%;
          height: 72rpx;
          padding: 0 20rpx;
          background: #f5f5f5;
          border-radius: 12rpx;
          font-size: 13px;
          color: #333;
        }
      }
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

      &.reset {
        color: #666;
        border-right: 1rpx solid #f0f0f0;
      }

      &.apply {
        color: #667eea;
      }
    }
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
