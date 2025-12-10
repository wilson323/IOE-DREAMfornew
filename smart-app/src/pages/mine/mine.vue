<template>
  <view class="personal-center">
    <!-- 现代化头部背景 -->
    <view class="header-background">
      <view class="header-pattern"></view>
    </view>

    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-avatar-section">
        <view class="avatar-wrapper" @click="changeAvatar">
          <image class="user-avatar" :src="userInfo.avatarUrl || '/static/images/default-avatar.png'" mode="aspectFill"></image>
          <view class="avatar-edit">
            <uni-icons type="camera" size="16" color="#fff"></uni-icons>
          </view>
        </view>
        <view class="user-info">
          <text class="user-name">{{ userInfo.userName || '未知用户' }}</text>
          <text class="user-role">{{ userInfo.position || '员工' }}</text>
          <view class="user-department">
            <uni-icons type="home" size="14" color="#999"></uni-icons>
            <text class="dept-text">{{ userInfo.departmentName || '未知部门' }}</text>
          </view>
        </view>
      </view>

      <view class="user-stats">
        <view class="stat-item" @click="goToWorkStats">
          <text class="stat-number">{{ userInfo.workDays || 156 }}</text>
          <text class="stat-label">工作天数</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="goToLeaveRecords">
          <text class="stat-number">{{ userInfo.leaveDays || 5 }}</text>
          <text class="stat-label">请假天数</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @click="goToAttendance">
          <text class="stat-number">{{ userInfo.attendanceRate || '98%' }}</text>
          <text class="stat-label">出勤率</text>
        </view>
      </view>
    </view>

    <!-- 快捷服务 -->
    <view class="quick-services">
      <view class="section-header">
        <text class="section-title">快捷服务</text>
        <text class="section-subtitle">快速访问常用功能</text>
      </view>

      <view class="services-grid">
        <view class="service-item" @click="goToProfile">
          <view class="service-icon-wrapper gradient-blue">
            <uni-icons type="person" size="20" color="#fff"></uni-icons>
          </view>
          <text class="service-text">个人信息</text>
        </view>

        <view class="service-item" @click="goToSettings">
          <view class="service-icon-wrapper gradient-purple">
            <uni-icons type="gear" size="20" color="#fff"></uni-icons>
          </view>
          <text class="service-text">系统设置</text>
        </view>

        <view class="service-item" @click="goToNotification">
          <view class="service-icon-wrapper gradient-orange">
            <uni-icons type="notification" size="20" color="#fff"></uni-icons>
            <view class="notification-dot" v-if="unreadCount > 0"></view>
          </view>
          <text class="service-text">消息通知</text>
        </view>

        <view class="service-item" @click="goToHelp">
          <view class="service-icon-wrapper gradient-green">
            <uni-icons type="help" size="20" color="#fff"></uni-icons>
          </view>
          <text class="service-text">帮助中心</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="function-menu">
      <view class="section-header">
        <text class="section-title">更多功能</text>
      </view>

      <view class="menu-list">
        <view class="menu-item" @click="goToSecurity">
          <view class="menu-left">
            <view class="menu-icon security">
              <uni-icons type="locked" size="18" color="#667eea"></uni-icons>
            </view>
            <view class="menu-content">
              <text class="menu-title">账号安全</text>
              <text class="menu-desc">密码、手机、邮箱</text>
            </view>
          </view>
          <uni-icons type="right" size="16" color="#ccc"></uni-icons>
        </view>

        <view class="menu-item" @click="goToPrivacy">
          <view class="menu-left">
            <view class="menu-icon privacy">
              <uni-icons type="eye-slash" size="18" color="#52c41a"></uni-icons>
            </view>
            <view class="menu-content">
              <text class="menu-title">隐私设置</text>
              <text class="menu-desc">信息可见性</text>
            </view>
          </view>
          <uni-icons type="right" size="16" color="#ccc"></uni-icons>
        </view>

        <view class="menu-item" @click="goToDataExport">
          <view class="menu-left">
            <view class="menu-icon export">
              <uni-icons type="download" size="18" color="#faad14"></uni-icons>
            </view>
            <view class="menu-content">
              <text class="menu-title">数据导出</text>
              <text class="menu-desc">考勤、消费记录</text>
            </view>
          </view>
          <uni-icons type="right" size="16" color="#ccc"></uni-icons>
        </view>

        <view class="menu-item" @click="goToFeedback">
          <view class="menu-left">
            <view class="menu-icon feedback">
              <uni-icons type="email" size="18" color="#f759ab"></uni-icons>
            </view>
            <view class="menu-content">
              <text class="menu-title">意见反馈</text>
              <text class="menu-desc">问题建议</text>
            </view>
          </view>
          <uni-icons type="right" size="16" color="#ccc"></uni-icons>
        </view>

        <view class="menu-item" @click="goToAbout">
          <view class="menu-left">
            <view class="menu-icon about">
              <uni-icons type="info" size="18" color="#40a9ff"></uni-icons>
            </view>
            <view class="menu-content">
              <text class="menu-title">关于我们</text>
              <text class="menu-desc">版本 v2.0.1</text>
            </view>
          </view>
          <uni-icons type="right" size="16" color="#ccc"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主题切换 -->
    <view class="theme-switcher">
      <view class="section-header">
        <text class="section-title">界面主题</text>
      </view>
      <view class="theme-options">
        <view
          class="theme-option"
          :class="{ active: currentTheme === 'light' }"
          @click="switchTheme('light')"
        >
          <view class="theme-preview light-preview"></view>
          <text class="theme-name">浅色主题</text>
        </view>
        <view
          class="theme-option"
          :class="{ active: currentTheme === 'dark' }"
          @click="switchTheme('dark')"
        >
          <view class="theme-preview dark-preview"></view>
          <text class="theme-name">深色主题</text>
        </view>
        <view
          class="theme-option"
          :class="{ active: currentTheme === 'auto' }"
          @click="switchTheme('auto')"
        >
          <view class="theme-preview auto-preview"></view>
          <text class="theme-name">跟随系统</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <view class="logout-btn" @click="showLogoutConfirm">
        <uni-icons type="poweroff" size="20" color="#ff4d4f"></uni-icons>
        <text class="logout-text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const userInfo = reactive({
  userName: '张三',
  position: '高级工程师',
  departmentName: '技术研发部',
  avatarUrl: '',
  workDays: 156,
  leaveDays: 5,
  attendanceRate: '98%',
  employeeId: 'EMP1001',
  phoneNumber: '138****5678',
  email: 'zhangsan@company.com'
})

const currentTheme = ref('light')
const unreadCount = ref(3)

const functionStats = reactive({
  messages: 12,
  notifications: 5,
  tasks: 8
})

// 页面生命周期
onMounted(() => {
  initPage()
})

// uni-app 页面显示生命周期
onShow(() => {
  loadUserInfo()
})

onPullDownRefresh(() => {
  loadUserInfo()
})

// 方法实现
const initPage = () => {
  loadUserInfo()
  loadTheme()
}

const loadUserInfo = async () => {
  try {
    // 这里调用真实API获取用户信息
    // const result = await userApi.getUserInfo()
    // if (result.success) {
    //   Object.assign(userInfo, result.data)
    // }

    // 模拟加载
    uni.stopPullDownRefresh()
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadTheme = () => {
  try {
    const theme = uni.getStorageSync('theme') || 'light'
    currentTheme.value = theme
  } catch (error) {
    console.error('加载主题失败:', error)
  }
}

const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 这里上传头像到服务器
      uploadAvatar(tempFilePath)
    }
  })
}

const uploadAvatar = async (filePath) => {
  uni.showLoading({
    title: '上传中...'
  })

  try {
    // 这里调用上传API
    // const result = await uploadApi.uploadAvatar(filePath)
    // if (result.success) {
    //   userInfo.avatarUrl = result.data.url
    // }

    uni.showToast({
      title: '头像更新成功',
      icon: 'success'
    })
  } catch (error) {
    uni.showToast({
      title: '头像更新失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

const switchTheme = (theme) => {
  currentTheme.value = theme
  uni.setStorageSync('theme', theme)

  // 应用主题样式
  applyTheme(theme)

  uni.showToast({
    title: '主题切换成功',
    icon: 'success'
  })
}

const applyTheme = (theme) => {
  // 这里可以实现主题切换逻辑
  console.log('切换主题:', theme)
}

const showLogoutConfirm = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        logout()
      }
    }
  })
}

const logout = async () => {
  uni.showLoading({
    title: '退出中...'
  })

  try {
    // 调用退出登录API
    // await userApi.logout()

    // 清除本地存储
    uni.clearStorageSync()

    // 跳转到登录页
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/login/login'
      })
    }, 500)

    uni.showToast({
      title: '退出成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('退出登录失败:', error)
    uni.showToast({
      title: '退出失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 页面导航方法
const goToProfile = () => {
  uni.navigateTo({
    url: '/pages/mine/profile'
  })
}

const goToSettings = () => {
  uni.navigateTo({
    url: '/pages/mine/settings'
  })
}

const goToNotification = () => {
  uni.navigateTo({
    url: '/pages/message/list'
  })
}

const goToHelp = () => {
  uni.navigateTo({
    url: '/pages/mine/help'
  })
}

const goToSecurity = () => {
  uni.navigateTo({
    url: '/pages/mine/security'
  })
}

const goToPrivacy = () => {
  uni.navigateTo({
    url: '/pages/mine/privacy'
  })
}

const goToDataExport = () => {
  uni.navigateTo({
    url: '/pages/mine/data-export'
  })
}

const goToFeedback = () => {
  uni.navigateTo({
    url: '/pages/mine/feedback'
  })
}

const goToAbout = () => {
  uni.navigateTo({
    url: '/pages/mine/about'
  })
}

const goToWorkStats = () => {
  uni.navigateTo({
    url: '/pages/attendance/statistics'
  })
}

const goToLeaveRecords = () => {
  uni.navigateTo({
    url: '/pages/attendance/leave'
  })
}

const goToAttendance = () => {
  uni.navigateTo({
    url: '/pages/attendance/index'
  })
}
</script>

<style lang="scss" scoped>
.personal-center {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
  position: relative;
}

// 头部背景
.header-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1;
  overflow: hidden;

  .header-pattern {
    position: absolute;
    top: -50%;
    right: -20%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
    background-size: 20px 20px;
    animation: float 20s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

// 用户卡片
.user-card {
  position: relative;
  z-index: 2;
  margin: 30rpx;
  padding: 40rpx;
  background: white;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);

  .user-avatar-section {
    display: flex;
    align-items: center;
    margin-bottom: 40rpx;

    .avatar-wrapper {
      position: relative;
      margin-right: 24rpx;

      .user-avatar {
        width: 120rpx;
        height: 120rpx;
        border-radius: 60rpx;
        border: 4rpx solid rgba(255, 255, 255, 0.3);
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
      }

      .avatar-edit {
        position: absolute;
        bottom: -4rpx;
        right: -4rpx;
        width: 40rpx;
        height: 40rpx;
        border-radius: 20rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.4);
      }
    }

    .user-info {
      flex: 1;

      .user-name {
        font-size: 36rpx;
        font-weight: bold;
        color: #1a1a1a;
        margin-bottom: 8rpx;
        display: block;
      }

      .user-role {
        font-size: 26rpx;
        color: #667eea;
        margin-bottom: 12rpx;
        display: block;
        font-weight: 500;
      }

      .user-department {
        display: flex;
        align-items: center;

        .dept-text {
          font-size: 24rpx;
          color: #999;
          margin-left: 8rpx;
        }
      }
    }
  }

  .user-stats {
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding-top: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;

      .stat-number {
        font-size: 32rpx;
        font-weight: bold;
        color: #1a1a1a;
        margin-bottom: 8rpx;
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }

    .stat-divider {
      width: 1rpx;
      height: 60rpx;
      background: linear-gradient(180deg, transparent 0%, #e0e0e0 50%, transparent 100%);
    }
  }
}

// 通用头部样式
.section-header {
  margin-bottom: 30rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #1a1a1a;
    margin-bottom: 8rpx;
    display: block;
  }

  .section-subtitle {
    font-size: 24rpx;
    color: #999;
    display: block;
  }
}

// 快捷服务
.quick-services {
  padding: 30rpx;

  .services-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;

    .service-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 30rpx 20rpx;
      background: white;
      border-radius: 20rpx;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
      transition: all 0.3s ease;
      position: relative;

      &:active {
        transform: scale(0.95);
        box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
      }

      .service-icon-wrapper {
        width: 70rpx;
        height: 70rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16rpx;
        position: relative;

        .notification-dot {
          position: absolute;
          top: -4rpx;
          right: -4rpx;
          width: 16rpx;
          height: 16rpx;
          background: #ff4d4f;
          border-radius: 8rpx;
        }
      }

      .gradient-blue {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
      }

      .gradient-purple {
        background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
        box-shadow: 0 4rpx 12rpx rgba(167, 139, 250, 0.3);
      }

      .gradient-orange {
        background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
        box-shadow: 0 4rpx 12rpx rgba(251, 146, 60, 0.3);
      }

      .gradient-green {
        background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
        box-shadow: 0 4rpx 12rpx rgba(74, 222, 128, 0.3);
      }

      .service-text {
        font-size: 24rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }
}

// 功能菜单
.function-menu {
  padding: 0 30rpx 30rpx;

  .menu-list {
    background: white;
    border-radius: 24rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .menu-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx;
      border-bottom: 1rpx solid #f8f8f8;
      transition: all 0.3s ease;

      &:last-child {
        border-bottom: none;
      }

      &:active {
        background: #f8f8f8;
      }

      .menu-left {
        display: flex;
        align-items: center;
        flex: 1;

        .menu-icon {
          width: 60rpx;
          height: 60rpx;
          border-radius: 16rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20rpx;

          &.security {
            background: rgba(102, 126, 234, 0.1);
          }

          &.privacy {
            background: rgba(82, 196, 26, 0.1);
          }

          &.export {
            background: rgba(250, 173, 20, 0.1);
          }

          &.feedback {
            background: rgba(247, 89, 171, 0.1);
          }

          &.about {
            background: rgba(64, 169, 255, 0.1);
          }
        }

        .menu-content {
          .menu-title {
            font-size: 30rpx;
            font-weight: 500;
            color: #1a1a1a;
            margin-bottom: 6rpx;
            display: block;
          }

          .menu-desc {
            font-size: 24rpx;
            color: #999;
            display: block;
          }
        }
      }
    }
  }
}

// 主题切换
.theme-switcher {
  padding: 0 30rpx 30rpx;

  .theme-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;

    .theme-option {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 30rpx 20rpx;
      background: white;
      border-radius: 20rpx;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
      transition: all 0.3s ease;
      border: 2rpx solid transparent;

      &.active {
        border-color: #667eea;
        transform: translateY(-4rpx);
        box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.2);
      }

      &:active {
        transform: scale(0.95);
      }

      .theme-preview {
        width: 60rpx;
        height: 60rpx;
        border-radius: 12rpx;
        margin-bottom: 16rpx;
        position: relative;
        overflow: hidden;

        .light-preview {
          background: linear-gradient(135deg, #ffffff 0%, #f5f5f5 100%);
          border: 1rpx solid #e0e0e0;
        }

        .dark-preview {
          background: linear-gradient(135deg, #333333 0%, #1a1a1a 100%);
        }

        .auto-preview {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #4ade80 100%);
        }
      }

      .theme-name {
        font-size: 24rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }
}

// 退出登录
.logout-section {
  padding: 0 30rpx 60rpx;

  .logout-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 30rpx;
    background: white;
    border-radius: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    border: 2rpx solid #fff1f0;

    &:active {
      transform: scale(0.98);
      background: #fff1f0;
    }

    .logout-text {
      font-size: 32rpx;
      color: #ff4d4f;
      font-weight: 500;
      margin-left: 12rpx;
    }
  }
}
</style>
