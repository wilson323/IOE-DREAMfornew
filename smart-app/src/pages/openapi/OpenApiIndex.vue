<template>
  <view class="openapi-index">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="header-content">
        <view class="title">
          <uni-icons type="api" size="24" color="#1890ff" />
          <text class="title-text">开放平台API</text>
        </view>
        <view class="subtitle">智慧园区API服务</view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-grid">
        <view class="stats-item">
          <view class="stats-icon" style="background: #e6f7ff;">
            <uni-icons type="api" size="20" color="#1890ff" />
          </view>
          <view class="stats-content">
            <view class="stats-number">{{ apiStats.totalApis }}</view>
            <view class="stats-label">API总数</view>
          </view>
        </view>
        <view class="stats-item">
          <view class="stats-icon" style="background: #f6ffed;">
            <uni-icons type="checkmarkempty" size="20" color="#52c41a" />
          </view>
          <view class="stats-content">
            <view class="stats-number">{{ apiStats.onlineServices }}</view>
            <view class="stats-label">在线服务</view>
          </view>
        </view>
        <view class="stats-item">
          <view class="stats-icon" style="background: #fff2e8;">
            <uni-icons type="fire" size="20" color="#faad14" />
          </view>
          <view class="stats-content">
            <view class="stats-number">{{ apiStats.todayCalls }}</view>
            <view class="stats-label">今日调用</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 服务分类 -->
    <view class="category-section">
      <view class="section-title">服务分类</view>
      <view class="category-grid">
        <view
          v-for="category in categories"
          :key="category.key"
          class="category-item"
          :class="{ active: selectedCategory === category.key }"
          @click="selectCategory(category.key)"
        >
          <view class="category-icon" :style="{ background: category.color }">
            <uni-icons :type="category.icon" size="24" color="#fff" />
          </view>
          <view class="category-info">
            <view class="category-name">{{ category.name }}</view>
            <view class="category-count">{{ category.count }}个接口</view>
          </view>
        </view>
      </view>
    </view>

    <!-- API列表 -->
    <view class="api-list-section">
      <view class="section-title">API接口列表</view>
      <view class="api-list">
        <view
          v-for="api in filteredApiList"
          :key="api.id"
          class="api-item"
          @click="viewApiDetail(api)"
        >
          <view class="api-header">
            <view class="api-method" :style="{ background: getMethodColor(api.method) }">
              {{ api.method }}
            </view>
            <view class="api-path">{{ api.path }}</view>
            <view class="api-status">
              <uni-icons
                :type="api.status === 'online' ? 'checkmarkempty' : 'closeempty'"
                :color="api.status === 'online' ? '#52c41a' : '#ff4d4f'"
                size="16"
              />
            </view>
          </view>
          <view class="api-summary">{{ api.summary }}</view>
          <view class="api-footer">
            <view class="api-service">{{ api.serviceName }}</view>
            <view class="api-actions">
              <view class="action-btn" @click.stop="testApi(api)">
                <uni-icons type="play-filled" size="16" color="#1890ff" />
                <text>测试</text>
              </view>
              <view class="action-btn" @click.stop="viewDoc(api)">
                <uni-icons type="help" size="16" color="#52c41a" />
                <text>文档</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷工具 -->
    <view class="tools-section">
      <view class="section-title">快捷工具</view>
      <view class="tools-grid">
        <view class="tool-item" @click="getToken">
          <view class="tool-icon" style="background: #e6f7ff;">
            <uni-icons type="key" size="20" color="#1890ff" />
          </view>
          <view class="tool-name">获取Token</view>
        </view>
        <view class="tool-item" @click="viewSwagger">
          <view class="tool-icon" style="background: #f6ffed;">
            <uni-icons type="file-text" size="20" color="#52c41a" />
          </view>
          <view class="tool-name">Swagger文档</view>
        </view>
        <view class="tool-item" @click="viewMonitor">
          <view class="tool-icon" style="background: #fff2e8;">
            <uni-icons type="monitor" size="20" color="#faad14" />
          </view>
          <view class="tool-name">监控面板</view>
        </view>
        <view class="tool-item" @click="viewLogs">
          <view class="tool-icon" style="background: #f9f0ff;">
            <uni-icons type="list" size="20" color="#722ed1" />
          </view>
          <view class="tool-name">调用日志</view>
        </view>
      </view>
    </view>

    <!-- Token获取弹窗 -->
    <uni-popup ref="tokenPopup" type="center" :mask-click="false">
      <view class="token-modal">
        <view class="modal-header">
          <text class="modal-title">获取API访问令牌</text>
          <uni-icons type="close" size="20" @click="closeTokenModal" />
        </view>
        <view class="modal-content">
          <view class="form-item">
            <text class="form-label">应用ID</text>
            <input
              v-model="tokenForm.appId"
              class="form-input"
              placeholder="请输入应用ID"
            />
          </view>
          <view class="form-item">
            <text class="form-label">应用密钥</text>
            <input
              v-model="tokenForm.appSecret"
              class="form-input"
              placeholder="请输入应用密钥"
              password
            />
          </view>
          <view class="form-item">
            <text class="form-label">有效期</text>
            <picker
              v-model="tokenForm.expiry"
              :range="expiryOptions"
              @change="onExpiryChange"
            >
              <view class="picker-view">
                {{ tokenForm.expiryText }}
                <uni-icons type="down" size="16" />
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn-cancel" @click="closeTokenModal">取消</button>
          <button class="btn-primary" @click="generateToken">生成Token</button>
        </view>
      </view>
    </uni-popup>

    <!-- Token结果弹窗 -->
    <uni-popup ref="tokenResultPopup" type="center" :mask-click="false">
      <view class="token-result-modal">
        <view class="modal-header">
          <text class="modal-title">Token生成成功</text>
          <uni-icons type="close" size="20" @click="closeTokenResultModal" />
        </view>
        <view class="modal-content">
          <view class="token-display">
            <text class="token-label">访问令牌</text>
            <view class="token-value" @click="copyToken">
              {{ generatedToken }}
            </view>
            <view class="token-actions">
              <button class="btn-copy" @click="copyToken">复制Token</button>
            </view>
          </view>
          <view class="token-info">
            <view class="info-item">
              <text class="info-label">有效期：</text>
              <text class="info-value">{{ tokenForm.expiryText }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">生成时间：</text>
              <text class="info-value">{{ tokenGeneratedTime }}</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn-primary" @click="closeTokenResultModal">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedCategory: 'all',
      apiStats: {
        totalApis: 98,
        totalServices: 7,
        onlineServices: 7,
        todayCalls: 12543
      },
      categories: [
        {
          key: 'all',
          name: '全部',
          icon: 'apps',
          color: '#1890ff',
          count: 98
        },
        {
          key: 'user',
          name: '用户管理',
          icon: 'person',
          color: '#52c41a',
          count: 12
        },
        {
          key: 'access',
          name: '门禁管理',
          icon: 'safety',
          color: '#faad14',
          count: 12
        },
        {
          key: 'attendance',
          name: '考勤管理',
          icon: 'calendar',
          color: '#722ed1',
          count: 12
        },
        {
          key: 'consume',
          name: '消费管理',
          icon: 'wallet',
          color: '#f5222d',
          count: 15
        },
        {
          key: 'visitor',
          name: '访客管理',
          icon: 'team',
          color: '#13c2c2',
          count: 15
        },
        {
          key: 'video',
          name: '视频监控',
          icon: 'videocam',
          color: '#eb2f96',
          count: 16
        },
        {
          key: 'data',
          name: '数据分析',
          icon: 'bars',
          color: '#fa8c16',
          count: 16
        }
      ],
      apiList: [
        {
          id: 1,
          method: 'POST',
          path: '/open/api/v1/user/auth/login',
          summary: '用户登录认证',
          serviceName: '公共业务服务',
          status: 'online',
          category: 'user'
        },
        {
          id: 2,
          method: 'GET',
          path: '/open/api/v1/user/info',
          summary: '获取用户信息',
          serviceName: '公共业务服务',
          status: 'online',
          category: 'user'
        },
        {
          id: 3,
          method: 'POST',
          path: '/open/api/v1/access/verify',
          summary: '门禁权限验证',
          serviceName: '门禁管理服务',
          status: 'online',
          category: 'access'
        },
        {
          id: 4,
          method: 'POST',
          path: '/open/api/v1/attendance/clock',
          summary: '考勤打卡记录',
          serviceName: '考勤管理服务',
          status: 'online',
          category: 'attendance'
        },
        {
          id: 5,
          method: 'POST',
          path: '/open/api/v1/consume/transaction/consume',
          summary: '消费交易处理',
          serviceName: '消费管理服务',
          status: 'online',
          category: 'consume'
        },
        {
          id: 6,
          method: 'POST',
          path: '/open/api/v1/visitor/appointment',
          summary: '访客预约申请',
          serviceName: '访客管理服务',
          status: 'online',
          category: 'visitor'
        },
        {
          id: 7,
          method: 'GET',
          path: '/open/api/v1/video/stream/live/{deviceId}',
          summary: '获取实时视频流',
          serviceName: '视频监控服务',
          status: 'online',
          category: 'video'
        },
        {
          id: 8,
          method: 'POST',
          path: '/open/api/v1/data-analysis/trends',
          summary: '趋势分析报告',
          serviceName: '数据分析服务',
          status: 'online',
          category: 'data'
        }
      ],
      tokenForm: {
        appId: '',
        appSecret: '',
        expiry: '24h',
        expiryText: '24小时'
      },
      expiryOptions: [
        '1小时',
        '24小时',
        '7天',
        '30天'
      ],
      generatedToken: '',
      tokenGeneratedTime: ''
    }
  },
  computed: {
    filteredApiList() {
      if (this.selectedCategory === 'all') {
        return this.apiList
      }
      return this.apiList.filter(api => api.category === this.selectedCategory)
    }
  },
  methods: {
    selectCategory(key) {
      this.selectedCategory = key
    },
    getMethodColor(method) {
      const colors = {
        GET: '#52c41a',
        POST: '#1890ff',
        PUT: '#faad14',
        DELETE: '#ff4d4f',
        PATCH: '#722ed1'
      }
      return colors[method] || '#8c8c8c'
    },
    viewApiDetail(api) {
      uni.navigateTo({
        url: `/pages/openapi/ApiDetail?id=${api.id}`
      })
    },
    testApi(api) {
      uni.navigateTo({
        url: `/pages/openapi/ApiTest?id=${api.id}`
      })
    },
    viewDoc(api) {
      uni.showLoading({
        title: '加载中'
      })

      // 根据服务获取文档地址
      const docUrls = {
        '公共业务服务': 'http://localhost:8088/doc.html',
        '门禁管理服务': 'http://localhost:8090/doc.html',
        '考勤管理服务': 'http://localhost:8091/doc.html',
        '消费管理服务': 'http://localhost:8094/doc.html',
        '访客管理服务': 'http://localhost:8095/doc.html',
        '视频监控服务': 'http://localhost:8092/doc.html',
        '数据分析服务': 'http://localhost:8088/openapi/doc.html'
      }

      const docUrl = docUrls[api.serviceName]

      if (docUrl) {
        // 在移动端打开文档需要特殊处理
        // 这里可以复制到剪贴板或显示提示
        uni.setClipboardData({
          data: docUrl,
          success: () => {
            uni.hideLoading()
            uni.showToast({
              title: '文档地址已复制',
              icon: 'success'
            })
            uni.showModal({
              title: 'API文档',
              content: `请在浏览器中访问：\n${docUrl}`,
              showCancel: false,
              confirmText: '知道了'
            })
          },
          fail: () => {
            uni.hideLoading()
            uni.showToast({
              title: '复制失败',
              icon: 'error'
            })
          }
        })
      } else {
        uni.hideLoading()
        uni.showToast({
          title: '文档地址获取失败',
          icon: 'error'
        })
      }
    },
    getToken() {
      this.$refs.tokenPopup.open()
    },
    closeTokenModal() {
      this.$refs.tokenPopup.close()
    },
    onExpiryChange(e) {
      const index = e.detail.value
      this.tokenForm.expiryText = this.expiryOptions[index]
    },
    generateToken() {
      if (!this.tokenForm.appId || !this.tokenForm.appSecret) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        })
        return
      }

      uni.showLoading({
        title: '生成中...'
      })

      // 模拟Token生成
      setTimeout(() => {
        this.generatedToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c'
        this.tokenGeneratedTime = new Date().toLocaleString()

        uni.hideLoading()
        this.closeTokenModal()
        this.$refs.tokenResultPopup.open()
      }, 1500)
    },
    closeTokenResultModal() {
      this.$refs.tokenResultPopup.close()
    },
    copyToken() {
      uni.setClipboardData({
        data: this.generatedToken,
        success: () => {
          uni.showToast({
            title: 'Token已复制',
            icon: 'success'
          })
        }
      })
    },
    viewSwagger() {
      uni.showModal({
        title: 'Swagger文档',
        content: '请访问 http://localhost:8080/doc.html 查看完整的API文档',
        showCancel: false,
        confirmText: '知道了'
      })
    },
    viewMonitor() {
      uni.navigateTo({
        url: '/pages/monitor/MonitorIndex'
      })
    },
    viewLogs() {
      uni.navigateTo({
        url: '/pages/logs/ApiLogs'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.openapi-index {
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  color: white;

  .header-content {
    text-align: center;

    .title {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16rpx;
      margin-bottom: 16rpx;

      .title-text {
        font-size: 36rpx;
        font-weight: 600;
      }
    }

    .subtitle {
      font-size: 28rpx;
      opacity: 0.9;
    }
  }
}

.stats-section {
  margin: 30rpx;
  padding: 0 30rpx;

  .stats-grid {
    display: flex;
    gap: 20rpx;
    background: white;
    border-radius: 16rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

    .stats-item {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 20rpx;

      .stats-icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .stats-content {
        flex: 1;

        .stats-number {
          font-size: 36rpx;
          font-weight: 600;
          color: #333;
          line-height: 1;
        }

        .stats-label {
          font-size: 24rpx;
          color: #999;
          margin-top: 8rpx;
        }
      }
    }
  }
}

.category-section,
.api-list-section,
.tools-section {
  margin: 30rpx;
  padding: 0 30rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .category-item {
    background: white;
    border-radius: 16rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    gap: 20rpx;
    transition: all 0.3s;

    &.active {
      border: 2rpx solid #1890ff;
    }

    .category-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .category-info {
      flex: 1;

      .category-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }

      .category-count {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.api-list {
  .api-item {
    background: white;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

    .api-header {
      display: flex;
      align-items: center;
      gap: 20rpx;
      margin-bottom: 16rpx;

      .api-method {
        padding: 8rpx 16rpx;
        border-radius: 8rpx;
        color: white;
        font-size: 24rpx;
        font-weight: 600;
        min-width: 80rpx;
        text-align: center;
      }

      .api-path {
        flex: 1;
        font-family: 'Monaco', 'Menlo', monospace;
        font-size: 24rpx;
        color: #666;
      }

      .api-status {
        width: 40rpx;
        height: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .api-summary {
      font-size: 28rpx;
      color: #666;
      margin-bottom: 20rpx;
    }

    .api-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .api-service {
        font-size: 24rpx;
        color: #999;
      }

      .api-actions {
        display: flex;
        gap: 20rpx;

        .action-btn {
          display: flex;
          align-items: center;
          gap: 8rpx;
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .tool-item {
    background: white;
    border-radius: 16rpx;
    padding: 40rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
    text-align: center;

    .tool-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20rpx;
    }

    .tool-name {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

.token-modal,
.token-result-modal {
  background: white;
  border-radius: 20rpx;
  width: 600rpx;
  max-width: 90vw;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .modal-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .modal-content {
    padding: 30rpx;

    .form-item {
      margin-bottom: 30rpx;

      .form-label {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 16rpx;
      }

      .form-input {
        width: 100%;
        padding: 20rpx;
        border: 1rpx solid #ddd;
        border-radius: 8rpx;
        font-size: 28rpx;
      }

      .picker-view {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx;
        border: 1rpx solid #ddd;
        border-radius: 8rpx;
        font-size: 28rpx;
      }
    }

    .token-display {
      margin-bottom: 30rpx;

      .token-label {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 16rpx;
      }

      .token-value {
        background: #f6f8fa;
        border: 1rpx solid #e1e4e8;
        border-radius: 8rpx;
        padding: 20rpx;
        font-family: 'Monaco', 'Menlo', monospace;
        font-size: 24rpx;
        line-height: 1.5;
        word-break: break-all;
        margin-bottom: 16rpx;
      }

      .token-actions {
        text-align: center;

        .btn-copy {
          background: #1890ff;
          color: white;
          border: none;
          padding: 16rpx 32rpx;
          border-radius: 8rpx;
          font-size: 28rpx;
        }
      }
    }

    .token-info {
      .info-item {
        display: flex;
        justify-content: space-between;
        padding: 16rpx 0;
        border-bottom: 1rpx solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .info-label {
          color: #666;
          font-size: 28rpx;
        }

        .info-value {
          color: #333;
          font-size: 28rpx;
        }
      }
    }
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .btn-cancel,
    .btn-primary {
      padding: 20rpx 40rpx;
      border-radius: 8rpx;
      font-size: 28rpx;
      text-align: center;
    }

    .btn-cancel {
      background: #f5f5f5;
      color: #666;
      border: 1rpx solid #ddd;
    }

    .btn-primary {
      background: #1890ff;
      color: white;
      border: 1rpx solid #1890ff;
    }
  }
}

// 响应式设计
@media (max-width: 750px) {
  .category-grid {
    grid-template-columns: 1fr;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    flex-direction: column;
    gap: 20rpx;
  }

  .api-item {
    .api-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 10rpx;
    }

    .api-footer {
      flex-direction: column;
      gap: 20rpx;
      align-items: flex-start;
    }
  }
}
</style>