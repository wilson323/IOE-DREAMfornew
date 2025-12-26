<template>
  <view class="help-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">帮助中心</text>
        </view>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 搜索框 -->
      <view class="search-section">
        <view class="search-box">
          <uni-icons type="search" size="18" color="#999"></uni-icons>
          <input
            class="search-input"
            v-model="searchKeyword"
            placeholder="搜索问题或关键词"
            @confirm="onSearch"
          />
        </view>
      </view>

      <!-- 快捷入口 -->
      <view class="quick-links">
        <view class="link-item" @tap="gotoLink('user-guide')">
          <view class="link-icon guide">
            <uni-icons type="book" size="24" color="#fff"></uni-icons>
          </view>
          <text class="link-title">使用指南</text>
        </view>
        <view class="link-item" @tap="gotoLink('video-tutorial')">
          <view class="link-icon video">
            <uni-icons type="videocam" size="24" color="#fff"></uni-icons>
          </view>
          <text class="link-title">视频教程</text>
        </view>
        <view class="link-item" @tap="gotoLink('faq')">
          <view class="link-icon faq">
            <uni-icons type="help" size="24" color="#fff"></uni-icons>
          </view>
          <text class="link-title">常见问题</text>
        </view>
        <view class="link-item" @tap="gotoLink('contact')">
          <view class="link-icon contact">
            <uni-icons type="chatbubble" size="24" color="#fff"></uni-icons>
          </view>
          <text class="link-title">在线客服</text>
        </view>
      </view>

      <!-- 热门问题 -->
      <view class="hot-questions">
        <view class="section-header">
          <text class="section-title">热门问题</text>
        </view>
        <view class="questions-list">
          <view
            class="question-item"
            v-for="(item, index) in hotQuestions"
            :key="index"
            @tap="viewQuestion(item)"
          >
            <view class="question-icon">
              <uni-icons type="flag" size="16" color="#ff4d4f"></uni-icons>
            </view>
            <text class="question-text">{{ item.question }}</text>
            <uni-icons type="right" size="14" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 分类浏览 -->
      <view class="category-section">
        <view class="section-header">
          <text class="section-title">分类浏览</text>
        </view>
        <view class="category-grid">
          <view
            class="category-item"
            v-for="(category, index) in categories"
            :key="index"
            @tap="viewCategory(category)"
          >
            <view class="category-icon" :class="category.type">
              <uni-icons :type="category.icon" size="28" color="#fff"></uni-icons>
            </view>
            <text class="category-name">{{ category.name }}</text>
            <text class="category-count">{{ category.count }}个问题</text>
          </view>
        </view>
      </view>

      <!-- 使用教程 -->
      <view class="tutorial-section">
        <view class="section-header">
          <text class="section-title">使用教程</text>
          <text class="section-more" @tap="viewAllTutorials">查看全部</text>
        </view>
        <view class="tutorial-list">
          <view
            class="tutorial-item"
            v-for="(item, index) in tutorials"
            :key="index"
            @tap="viewTutorial(item)"
          >
            <image class="tutorial-thumb" :src="item.thumb" mode="aspectFill"></image>
            <view class="tutorial-info">
              <text class="tutorial-title">{{ item.title }}</text>
              <view class="tutorial-meta">
                <text class="tutorial-duration">{{ item.duration }}</text>
                <text class="tutorial-views">{{ item.views }}次观看</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 联系方式 -->
      <view class="contact-section">
        <view class="section-header">
          <text class="section-title">联系我们</text>
        </view>
        <view class="contact-list">
          <view class="contact-item" @tap="makeCall('400-123-4567')">
            <view class="contact-icon phone">
              <uni-icons type="phone" size="20" color="#fff"></uni-icons>
            </view>
            <view class="contact-info">
              <text class="contact-label">客服热线</text>
              <text class="contact-value">400-123-4567</text>
            </view>
          </view>
          <view class="contact-item" @tap="sendEmail('support@ioedream.com')">
            <view class="contact-icon email">
              <uni-icons type="email" size="20" color="#fff"></uni-icons>
            </view>
            <view class="contact-info">
              <text class="contact-label">技术支持</text>
              <text class="contact-value">support@ioedream.com</text>
            </view>
          </view>
          <view class="contact-item" @tap="openWeChat">
            <view class="contact-icon wechat">
              <uni-icons type="weixin" size="20" color="#fff"></uni-icons>
            </view>
            <view class="contact-info">
              <text class="contact-label">微信公众号</text>
              <text class="contact-value">IOE-DREAM智慧园区</text>
            </view>
          </view>
          <view class="contact-item" @tap="gotoFeedback">
            <view class="contact-icon feedback">
              <uni-icons type="compose" size="20" color="#fff"></uni-icons>
            </view>
            <view class="contact-info">
              <text class="contact-label">意见反馈</text>
              <text class="contact-value">提交您的建议</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 版本信息 -->
      <view class="version-section">
        <text class="version-text">当前版本 v1.0.0</text>
        <text class="update-text" @tap="checkUpdate">检查更新</text>
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

// 搜索关键词
const searchKeyword = ref('')

// 热门问题
const hotQuestions = ref([
  {
    id: 1,
    question: '如何添加新的门禁设备？'
  },
  {
    id: 2,
    question: '忘记门禁密码怎么办？'
  },
  {
    id: 3,
    question: '如何查看考勤记录？'
  },
  {
    id: 4,
    question: '门禁设备离线如何处理？'
  },
  {
    id: 5,
    question: '如何设置访客预约？'
  }
])

// 分类
const categories = ref([
  {
    id: 1,
    type: 'access',
    icon: 'locked',
    name: '门禁管理',
    count: 28
  },
  {
    id: 2,
    type: 'attendance',
    icon: 'calendar',
    name: '考勤管理',
    count: 24
  },
  {
    id: 3,
    type: 'consume',
    icon: 'wallet',
    name: '消费管理',
    count: 18
  },
  {
    id: 4,
    type: 'visitor',
    icon: 'person',
    name: '访客管理',
    count: 15
  },
  {
    id: 5,
    type: 'video',
    icon: 'videocam',
    name: '视频监控',
    count: 20
  },
  {
    id: 6,
    type: 'system',
    icon: 'settings',
    name: '系统设置',
    count: 32
  }
])

// 教程
const tutorials = ref([
  {
    id: 1,
    title: '门禁设备快速入门',
    thumb: 'https://via.placeholder.com/300x200',
    duration: '5:30',
    views: 1520
  },
  {
    id: 2,
    title: '考勤规则配置详解',
    thumb: 'https://via.placeholder.com/300x200',
    duration: '8:15',
    views: 980
  },
  {
    id: 3,
    title: '访客预约完整流程',
    thumb: 'https://via.placeholder.com/300x200',
    duration: '6:45',
    views: 756
  }
])

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 搜索
const onSearch = () => {
  if (!searchKeyword.value.trim()) {
    uni.showToast({
      title: '请输入搜索关键词',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages/help/search-result?keyword=${searchKeyword.value}`
  })
}

// 跳转链接
const gotoLink = (type) => {
  const pageMap = {
    'user-guide': '/pages/help/user-guide',
    'video-tutorial': '/pages/help/video-tutorial-list',
    'faq': '/pages/help/faq',
    'contact': '/pages/help/customer-service'
  }
  uni.navigateTo({
    url: pageMap[type]
  })
}

// 查看问题
const viewQuestion = (item) => {
  uni.navigateTo({
    url: `/pages/help/question-detail?questionId=${item.id}`
  })
}

// 查看分类
const viewCategory = (category) => {
  uni.navigateTo({
    url: `/pages/help/category-detail?categoryId=${category.id}&type=${category.type}`
  })
}

// 查看教程
const viewTutorial = (item) => {
  uni.navigateTo({
    url: `/pages/help/tutorial-detail?tutorialId=${item.id}`
  })
}

// 查看全部教程
const viewAllTutorials = () => {
  uni.navigateTo({
    url: '/pages/help/tutorial-list'
  })
}

// 拨打电话
const makeCall = (phone) => {
  uni.makePhoneCall({
    phoneNumber: phone
  })
}

// 发送邮件
const sendEmail = (email) => {
  uni.showToast({
    title: '即将打开邮件客户端',
    icon: 'none'
  })
}

// 打开微信
const openWeChat = () => {
  uni.showToast({
    title: '请搜索"IOE-DREAM智慧园区"关注我们',
    icon: 'none'
  })
}

// 意见反馈
const gotoFeedback = () => {
  uni.navigateTo({
    url: '/pages/help/feedback'
  })
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

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.help-container {
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

// 搜索框
.search-section {
  padding: 30rpx;

  .search-box {
    display: flex;
    align-items: center;
    height: 72rpx;
    background: #fff;
    border-radius: 36rpx;
    padding: 0 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .search-input {
      flex: 1;
      margin-left: 20rpx;
      font-size: 14px;
      color: #333;
    }
  }
}

// 快捷入口
.quick-links {
  display: flex;
  padding: 0 30rpx 30rpx;

  .link-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30rpx 20rpx;
    background: #fff;
    border-radius: 24rpx;
    margin-right: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    &:last-child {
      margin-right: 0;
    }

    .link-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 20rpx;

      &.guide {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.video {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.faq {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.contact {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }

    .link-title {
      font-size: 12px;
      color: #333;
    }
  }
}

// 热门问题
.hot-questions {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .questions-list {
    .question-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .question-icon {
        margin-right: 20rpx;
      }

      .question-text {
        flex: 1;
        font-size: 14px;
        color: #333;
      }
    }
  }
}

// 分类浏览
.category-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .category-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;

    .category-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 30rpx 20rpx;
      background: #f9f9f9;
      border-radius: 16rpx;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      &:active {
        border-color: #667eea;
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      }

      .category-icon {
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

        &.system {
          background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
        }
      }

      .category-name {
        font-size: 13px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .category-count {
        font-size: 11px;
        color: #999;
      }
    }
  }
}

// 使用教程
.tutorial-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .section-more {
      font-size: 13px;
      color: #667eea;
    }
  }

  .tutorial-list {
    .tutorial-item {
      display: flex;
      margin-bottom: 20rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .tutorial-thumb {
        width: 200rpx;
        height: 140rpx;
        border-radius: 12rpx;
        background: #f5f5f5;
        margin-right: 20rpx;
      }

      .tutorial-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .tutorial-title {
          font-size: 14px;
          color: #333;
          line-height: 1.4;
        }

        .tutorial-meta {
          display: flex;
          align-items: center;
          font-size: 11px;
          color: #999;

          .tutorial-duration {
            margin-right: 20rpx;
          }
        }
      }
    }
  }
}

// 联系方式
.contact-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .contact-list {
    .contact-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .contact-icon {
        width: 48rpx;
        height: 48rpx;
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;

        &.phone {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.email {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }

        &.wechat {
          background: linear-gradient(135deg, #09bb07 0%, #43e97b 100%);
        }

        &.feedback {
          background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        }
      }

      .contact-info {
        flex: 1;

        .contact-label {
          display: block;
          font-size: 12px;
          color: #999;
          margin-bottom: 6rpx;
        }

        .contact-value {
          font-size: 14px;
          color: #333;
        }
      }
    }
  }
}

// 版本信息
.version-section {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  text-align: center;

  .version-text {
    display: block;
    font-size: 13px;
    color: #999;
    margin-bottom: 10rpx;
  }

  .update-text {
    font-size: 13px;
    color: #667eea;
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
