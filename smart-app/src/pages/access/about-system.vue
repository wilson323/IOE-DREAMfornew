<template>
  <view class="about-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">关于系统</text>
        </view>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- Logo和版本 -->
      <view class="header-section">
        <view class="logo-wrapper">
          <image class="app-logo" src="/static/logo.png" mode="aspectFit"></image>
        </view>
        <text class="app-name">IOE-DREAM 智慧园区</text>
        <text class="app-version">版本 1.0.0</text>
        <view class="update-btn" @tap="checkUpdate">
          <text>检查更新</text>
        </view>
      </view>

      <!-- 系统信息 -->
      <view class="info-section">
        <view class="section-title">系统信息</view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">应用名称</text>
            <text class="info-value">IOE-DREAM 智慧园区管理系统</text>
          </view>
          <view class="info-item">
            <text class="info-label">当前版本</text>
            <text class="info-value">v1.0.0 (Build 20241224)</text>
          </view>
          <view class="info-item">
            <text class="info-label">系统架构</text>
            <text class="info-value">Spring Boot 3.5 + Vue 3</text>
          </view>
          <view class="info-item">
            <text class="info-label">开发团队</text>
            <text class="info-value">IOE-DREAM 研发中心</text>
          </view>
          <view class="info-item">
            <text class="info-label">许可证</text>
            <text class="info-value">企业版</text>
          </view>
        </view>
      </view>

      <!-- 功能模块 -->
      <view class="features-section">
        <view class="section-title">功能模块</view>
        <view class="features-grid">
          <view class="feature-item">
            <view class="feature-icon access">
              <uni-icons type="locked" size="24" color="#fff"></uni-icons>
            </view>
            <text class="feature-name">门禁管理</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon attendance">
              <uni-icons type="calendar" size="24" color="#fff"></uni-icons>
            </view>
            <text class="feature-name">考勤管理</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon consume">
              <uni-icons type="wallet" size="24" color="#fff"></uni-icons>
            </view>
            <text class="feature-name">消费管理</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon visitor">
              <uni-icons type="person" size="24" color="#fff"></uni-icons>
            </view>
            <text class="feature-name">访客管理</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon video">
              <uni-icons type="videocam" size="24" color="#fff"></uni-icons>
            </view>
            <text class="feature-name">视频监控</text>
          </view>
          <view class="feature-item">
            <view class="feature-icon report">
              <uni-icons type="pie-chart" size="24" color="#fff"></uni-icons>
            </view>
            <text class="feature-name">数据报表</text>
          </view>
        </view>
      </view>

      <!-- 技术支持 -->
      <view class="support-section">
        <view class="section-title">技术支持</view>
        <view class="support-list">
          <view class="support-item" @tap="gotoLink('website')">
            <view class="support-icon website">
              <uni-icons type="world" size="20" color="#fff"></uni-icons>
            </view>
            <view class="support-info">
              <text class="support-label">官方网站</text>
              <text class="support-value">www.ioedream.com</text>
            </view>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="support-item" @tap="gotoLink('doc')">
            <view class="support-icon doc">
              <uni-icons type="book" size="20" color="#fff"></uni-icons>
            </view>
            <view class="support-info">
              <text class="support-label">开发文档</text>
              <text class="support-value">docs.ioedream.com</text>
            </view>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="support-item" @tap="gotoLink('github')">
            <view class="support-icon github">
              <uni-icons type="github" size="20" color="#fff"></uni-icons>
            </view>
            <view class="support-info">
              <text class="support-label">GitHub</text>
              <text class="support-value">github.com/ioedream</text>
            </view>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="support-item" @tap="makeCall">
            <view class="support-icon phone">
              <uni-icons type="phone" size="20" color="#fff"></uni-icons>
            </view>
            <view class="support-info">
              <text class="support-label">技术热线</text>
              <text class="support-value">400-123-4567</text>
            </view>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 法律信息 -->
      <view class="legal-section">
        <view class="section-title">法律信息</view>
        <view class="legal-list">
          <view class="legal-item" @tap="viewLegal('privacy')">
            <text class="legal-title">隐私政策</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="legal-item" @tap="viewLegal('terms')">
            <text class="legal-title">服务条款</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="legal-item" @tap="viewLegal('license')">
            <text class="legal-title">开源许可</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 致谢 -->
      <view class="thanks-section">
        <view class="section-title">致谢</view>
        <view class="thanks-content">
          <text class="thanks-text">感谢以下开源项目：</text>
          <view class="thanks-list">
            <text class="thanks-item">• Spring Boot</text>
            <text class="thanks-item">• Vue.js</text>
            <text class="thanks-item">• uni-app</text>
            <text class="thanks-item">• MyBatis-Plus</text>
            <text class="thanks-item">• Ant Design Vue</text>
          </view>
        </view>
      </view>

      <!-- 底部版权 -->
      <view class="footer-section">
        <text class="copyright">© 2024 IOE-DREAM. All rights reserved.</text>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 检查更新
const checkUpdate = () => {
  uni.showLoading({ title: '检查中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '已是最新版本',
      icon: 'success'
    })
  }, 1500)
}

// 跳转链接
const gotoLink = (type) => {
  uni.showToast({
    title: '即将打开浏览器',
    icon: 'none'
  })
  // TODO: 实现实际的跳转逻辑
}

// 拨打电话
const makeCall = () => {
  uni.makePhoneCall({
    phoneNumber: '400-123-4567'
  })
}

// 查看法律信息
const viewLegal = (type) => {
  uni.navigateTo({
    url: `/pages/about/${type}`
  })
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.about-container {
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
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 44px);
}

// 头部区域
.header-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 30rpx;
  background: #fff;

  .logo-wrapper {
    width: 128rpx;
    height: 128rpx;
    border-radius: 24rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 30rpx;

    .app-logo {
      width: 80rpx;
      height: 80rpx;
    }
  }

  .app-name {
    font-size: 20px;
    font-weight: 600;
    color: #333;
    margin-bottom: 10rpx;
  }

  .app-version {
    font-size: 13px;
    color: #999;
    margin-bottom: 30rpx;
  }

  .update-btn {
    padding: 12rpx 32rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    font-size: 13px;
    color: #fff;
  }
}

// 区块标题
.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 20rpx;
}

// 系统信息
.info-section {
  margin: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .info-list {
    .info-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .info-label {
        font-size: 14px;
        color: #999;
      }

      .info-value {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

// 功能模块
.features-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;

    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 30rpx 20rpx;

      .feature-icon {
        width: 64rpx;
        height: 64rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 20rpx;

        &.access {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.attendance {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.consume {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        &.visitor {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }

        &.video {
          background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        }

        &.report {
          background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
        }
      }

      .feature-name {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

// 技术支持
.support-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .support-list {
    .support-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .support-icon {
        width: 48rpx;
        height: 48rpx;
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;

        &.website {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.doc {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }

        &.github {
          background: linear-gradient(135deg, #333 0%, #666 100%);
        }

        &.phone {
          background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        }
      }

      .support-info {
        flex: 1;

        .support-label {
          display: block;
          font-size: 14px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .support-value {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

// 法律信息
.legal-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .legal-list {
    .legal-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .legal-title {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

// 致谢
.thanks-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .thanks-content {
    .thanks-text {
      display: block;
      font-size: 14px;
      color: #666;
      margin-bottom: 20rpx;
    }

    .thanks-list {
      display: flex;
      flex-direction: column;

      .thanks-item {
        font-size: 13px;
        color: #999;
        margin-bottom: 10rpx;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

// 底部版权
.footer-section {
  padding: 30rpx;
  text-align: center;

  .copyright {
    font-size: 12px;
    color: #999;
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
