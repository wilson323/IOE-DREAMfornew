<template>
  <view class="auth-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">多因子认证配置</text>
        </view>
        <view class="navbar-right">
          <view class="save-btn" @tap="saveConfig">
            <text>保存</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 功能总开关 -->
      <view class="switch-section">
        <view class="switch-card">
          <view class="switch-info">
            <view class="switch-icon">
              <uni-icons type="safe" size="28" color="#fff"></uni-icons>
            </view>
            <view class="switch-details">
              <text class="switch-title">启用多因子认证</text>
              <text class="switch-desc">要求用户通过多种方式验证身份</text>
            </view>
          </view>
          <switch
            :checked="config.enabled"
            color="#667eea"
            @change="onEnabledChange"
          ></switch>
        </view>
      </view>

      <!-- 配置内容 -->
      <view v-if="config.enabled" class="config-content">
        <!-- 认证方式选择 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">认证方式</text>
            <text class="section-desc">选择支持的认证方式（至少选择2种）</text>
          </view>
          <view class="methods-grid">
            <view
              class="method-item"
              :class="{ active: isMethodSelected('face') }"
              @tap="toggleMethod('face')"
            >
              <view class="method-icon face">
                <uni-icons type="person" size="24" color="#fff"></uni-icons>
              </view>
              <text class="method-name">人脸识别</text>
              <view class="method-check" v-if="isMethodSelected('face')">
                <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
              </view>
            </view>
            <view
              class="method-item"
              :class="{ active: isMethodSelected('fingerprint') }"
              @tap="toggleMethod('fingerprint')"
            >
              <view class="method-icon fingerprint">
                <uni-icons type="hand" size="24" color="#fff"></uni-icons>
              </view>
              <text class="method-name">指纹识别</text>
              <view class="method-check" v-if="isMethodSelected('fingerprint')">
                <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
              </view>
            </view>
            <view
              class="method-item"
              :class="{ active: isMethodSelected('password') }"
              @tap="toggleMethod('password')"
            >
              <view class="method-icon password">
                <uni-icons type="locked" size="24" color="#fff"></uni-icons>
              </view>
              <text class="method-name">密码认证</text>
              <view class="method-check" v-if="isMethodSelected('password')">
                <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
              </view>
            </view>
            <view
              class="method-item"
              :class="{ active: isMethodSelected('card') }"
              @tap="toggleMethod('card')"
            >
              <view class="method-icon card">
                <uni-icons type="bankcard" size="24" color="#fff"></uni-icons>
              </view>
              <text class="method-name">IC卡/NFC</text>
              <view class="method-check" v-if="isMethodSelected('card')">
                <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
              </view>
            </view>
            <view
              class="method-item"
              :class="{ active: isMethodSelected('qr') }"
              @tap="toggleMethod('qr')"
            >
              <view class="method-icon qr">
                <uni-icons type="qrcode" size="24" color="#fff"></uni-icons>
              </view>
              <text class="method-name">二维码</text>
              <view class="method-check" v-if="isMethodSelected('qr')">
                <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
              </view>
            </view>
            <view
              class="method-item"
              :class="{ active: isMethodSelected('pin') }"
              @tap="toggleMethod('pin')"
            >
              <view class="method-icon pin">
                <uni-icons type="phone" size="24" color="#fff"></uni-icons>
              </view>
              <text class="method-name">短信验证码</text>
              <view class="method-check" v-if="isMethodSelected('pin')">
                <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <!-- 认证策略配置 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">认证策略</text>
            <text class="section-desc">设置认证因子数量要求</text>
          </view>
          <view class="strategy-options">
            <view
              class="strategy-item"
              :class="{ active: config.strategy === 'single' }"
              @tap="selectStrategy('single')"
            >
              <view class="strategy-icon">
                <text class="strategy-number">1</text>
              </view>
              <view class="strategy-info">
                <text class="strategy-name">单因子认证</text>
                <text class="strategy-desc">只需通过一种认证方式</text>
              </view>
              <view class="strategy-check" v-if="config.strategy === 'single'">
                <uni-icons type="checkmarkempty" size="18" color="#667eea"></uni-icons>
              </view>
            </view>
            <view
              class="strategy-item"
              :class="{ active: config.strategy === 'double' }"
              @tap="selectStrategy('double')"
            >
              <view class="strategy-icon double">
                <text class="strategy-number">2</text>
              </view>
              <view class="strategy-info">
                <text class="strategy-name">双因子认证</text>
                <text class="strategy-desc">需要通过两种认证方式</text>
              </view>
              <view class="strategy-check" v-if="config.strategy === 'double'">
                <uni-icons type="checkmarkempty" size="18" color="#667eea"></uni-icons>
              </view>
            </view>
            <view
              class="strategy-item"
              :class="{ active: config.strategy === 'triple' }"
              @tap="selectStrategy('triple')"
            >
              <view class="strategy-icon triple">
                <text class="strategy-number">3</text>
              </view>
              <view class="strategy-info">
                <text class="strategy-name">三因子认证</text>
                <text class="strategy-desc">需要通过三种认证方式</text>
              </view>
              <view class="strategy-check" v-if="config.strategy === 'triple'">
                <uni-icons type="checkmarkempty" size="18" color="#667eea"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <!-- 认证优先级 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">认证优先级</text>
            <text class="section-desc">设置认证方式的优先顺序</text>
          </view>
          <view class="priority-list">
            <view
              class="priority-item"
              v-for="(item, index) in priorityList"
              :key="index"
            >
              <view class="priority-number">
                <text>{{ index + 1 }}</text>
              </view>
              <view class="priority-info">
                <text class="priority-name">{{ item.name }}</text>
                <text class="priority-desc">{{ item.desc }}</text>
              </view>
              <view class="priority-actions">
                <view
                  class="action-btn up"
                  :class="{ disabled: index === 0 }"
                  @tap="moveUp(index)"
                >
                  <uni-icons type="up" size="16" color="#999"></uni-icons>
                </view>
                <view
                  class="action-btn down"
                  :class="{ disabled: index === priorityList.length - 1 }"
                  @tap="moveDown(index)"
                >
                  <uni-icons type="down" size="16" color="#999"></uni-icons>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 区域认证规则 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">区域认证规则</text>
            <text class="section-desc">为不同区域设置认证要求</text>
          </view>
          <view class="area-rules-list">
            <view
              class="area-rule-item"
              v-for="(item, index) in areaRules"
              :key="index"
              @tap="editAreaRule(item)"
            >
              <view class="area-rule-header">
                <view class="area-icon">
                  <uni-icons type="location" size="16" color="#fff"></uni-icons>
                </view>
                <text class="area-name">{{ item.areaName }}</text>
                <view class="security-level" :class="'level-' + item.level">
                  <text>{{ getSecurityLevelText(item.level) }}</text>
                </view>
              </view>
              <view class="area-rule-detail">
                <view class="rule-methods">
                  <view
                    class="method-tag"
                    v-for="(method, idx) in item.methods"
                    :key="idx"
                  >
                    <text>{{ method }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <view class="add-area-rule-btn" @tap="addAreaRule">
            <uni-icons type="plus" size="18" color="#667eea"></uni-icons>
            <text class="add-text">添加区域规则</text>
          </view>
        </view>

        <!-- 人员认证配置 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">人员认证配置</text>
            <text class="section-desc">特殊人员的认证要求</text>
          </view>
          <view class="person-config-list">
            <view
              class="person-config-item"
              v-for="(item, index) in personConfigs"
              :key="index"
              @tap="editPersonConfig(item)"
            >
              <image
                class="person-avatar"
                :src="item.avatar || '/static/default-avatar.png'"
                mode="aspectFill"
              ></image>
              <view class="person-info">
                <text class="person-name">{{ item.name }}</text>
                <text class="person-dept">{{ item.department }}</text>
              </view>
              <view class="person-strategy">
                <text>{{ getStrategyText(item.strategy) }}</text>
              </view>
            </view>
          </view>
          <view class="add-person-config-btn" @tap="addPersonConfig">
            <uni-icons type="plus" size="18" color="#667eea"></uni-icons>
            <text class="add-text">添加人员配置</text>
          </view>
        </view>

        <!-- 认证统计 -->
        <view class="config-section">
          <view class="section-header">
            <text class="section-title">认证统计</text>
            <text class="section-more" @tap="viewAllStatistics">查看详情</text>
          </view>
          <view class="statistics-overview">
            <view class="statistics-item">
              <text class="statistics-value">{{ statistics.todayAuth }}</text>
              <text class="statistics-label">今日认证</text>
            </view>
            <view class="statistics-item">
              <text class="statistics-value success">{{ statistics.successRate }}%</text>
              <text class="statistics-label">成功率</text>
            </view>
            <view class="statistics-item">
              <text class="statistics-value warning">{{ statistics.avgTime }}秒</text>
              <text class="statistics-label">平均耗时</text>
            </view>
          </view>
          <view class="method-usage">
            <view class="usage-header">
              <text class="usage-title">认证方式使用率</text>
            </view>
            <view
              class="usage-item"
              v-for="(item, index) in methodUsage"
              :key="index"
            >
              <text class="usage-label">{{ item.name }}</text>
              <view class="usage-bar">
                <view
                  class="usage-fill"
                  :style="{ width: item.percent + '%', background: item.color }"
                ></view>
              </view>
              <text class="usage-percent" :style="{ color: item.color }">{{ item.percent }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 未启用提示 -->
      <view v-else class="disabled-tip">
        <view class="tip-icon">
          <uni-icons type="safe" size="48" color="#ccc"></uni-icons>
        </view>
        <text class="tip-title">多因子认证未启用</text>
        <text class="tip-desc">启用后可配置多因子认证规则</text>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 配置数据
const config = reactive({
  enabled: true,
  strategy: 'double', // single, double, triple
  methods: ['face', 'card'] // face, fingerprint, password, card, qr, pin
})

// 优先级列表
const priorityList = ref([
  { key: 'face', name: '人脸识别', desc: '最快捷的认证方式' },
  { key: 'fingerprint', name: '指纹识别', desc: '高精度生物识别' },
  { key: 'card', name: 'IC卡/NFC', desc: '刷卡快速通行' },
  { key: 'password', name: '密码认证', desc: '数字密码输入' },
  { key: 'qr', name: '二维码', desc: '动态二维码扫描' },
  { key: 'pin', name: '短信验证码', desc: '手机短信验证' }
])

// 区域认证规则
const areaRules = ref([
  {
    areaId: 1,
    areaName: 'A栋办公区',
    level: 2,
    methods: ['人脸识别', 'IC卡/NFC']
  },
  {
    areaId: 2,
    areaName: 'B栋生产区',
    level: 3,
    methods: ['人脸识别', '指纹识别', 'IC卡/NFC']
  },
  {
    areaId: 3,
    areaName: 'C栋仓储区',
    level: 1,
    methods: ['IC卡/NFC']
  }
])

// 人员认证配置
const personConfigs = ref([
  {
    userId: 1,
    name: '张三',
    department: '行政部',
    avatar: '/static/avatar1.png',
    strategy: 'single'
  },
  {
    userId: 2,
    name: '李四',
    department: '人事部',
    avatar: '/static/avatar2.png',
    strategy: 'double'
  },
  {
    userId: 3,
    name: '王五',
    department: '财务部',
    avatar: '/static/avatar3.png',
    strategy: 'triple'
  }
])

// 统计数据
const statistics = reactive({
  todayAuth: 156,
  successRate: 98.5,
  avgTime: 2.3
})

// 认证方式使用率
const methodUsage = ref([
  { name: '人脸识别', percent: 65, color: '#667eea' },
  { name: 'IC卡/NFC', percent: 25, color: '#52c41a' },
  { name: '指纹识别', percent: 8, color: '#faad14' },
  { name: '密码认证', percent: 2, color: '#ff4d4f' }
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

// 启用状态变更
const onEnabledChange = (e) => {
  config.enabled = e.detail.value
}

// 判断认证方式是否选中
const isMethodSelected = (method) => {
  return config.methods.includes(method)
}

// 切换认证方式
const toggleMethod = (method) => {
  const index = config.methods.indexOf(method)
  if (index > -1) {
    // 至少保留2种认证方式
    if (config.methods.length > 2) {
      config.methods.splice(index, 1)
    } else {
      uni.showToast({
        title: '至少选择2种认证方式',
        icon: 'none'
      })
    }
  } else {
    config.methods.push(method)
  }
}

// 选择认证策略
const selectStrategy = (strategy) => {
  config.strategy = strategy
}

// 上移优先级
const moveUp = (index) => {
  if (index === 0) return
  const temp = priorityList.value[index]
  priorityList.value[index] = priorityList.value[index - 1]
  priorityList.value[index - 1] = temp
}

// 下移优先级
const moveDown = (index) => {
  if (index === priorityList.value.length - 1) return
  const temp = priorityList.value[index]
  priorityList.value[index] = priorityList.value[index + 1]
  priorityList.value[index + 1] = temp
}

// 获取安全等级文本
const getSecurityLevelText = (level) => {
  const levelMap = {
    1: '低',
    2: '中',
    3: '高'
  }
  return levelMap[level] || '中'
}

// 获取策略文本
const getStrategyText = (strategy) => {
  const strategyMap = {
    single: '单因子',
    double: '双因子',
    triple: '三因子'
  }
  return strategyMap[strategy] || '双因子'
}

// 编辑区域规则
const editAreaRule = (rule) => {
  uni.navigateTo({
    url: `/pages/access/area-auth-rule-edit?areaId=${rule.areaId}`
  })
}

// 添加区域规则
const addAreaRule = () => {
  uni.navigateTo({
    url: '/pages/access/area-auth-rule-add'
  })
}

// 编辑人员配置
const editPersonConfig = (person) => {
  uni.navigateTo({
    url: `/pages/access/person-auth-config-edit?userId=${person.userId}`
  })
}

// 添加人员配置
const addPersonConfig = () => {
  uni.navigateTo({
    url: '/pages/access/person-auth-config-add'
  })
}

// 查看全部统计
const viewAllStatistics = () => {
  uni.navigateTo({
    url: '/pages/access/auth-statistics'
  })
}

// 保存配置
const saveConfig = () => {
  // 验证至少选择2种认证方式
  if (config.methods.length < 2) {
    uni.showToast({
      title: '至少选择2种认证方式',
      icon: 'none'
    })
    return
  }

  uni.showLoading({ title: '保存中...' })

  // TODO: 调用实际API保存配置
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })
  }, 1000)
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.auth-container {
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
      .save-btn {
        padding: 8rpx 24rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20rpx;
        font-size: 13px;
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

// 总开关
.switch-section {
  padding: 30rpx;

  .switch-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .switch-info {
      display: flex;
      align-items: center;
      flex: 1;

      .switch-icon {
        width: 64rpx;
        height: 64rpx;
        border-radius: 16rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .switch-details {
        display: flex;
        flex-direction: column;

        .switch-title {
          font-size: 16px;
          font-weight: 500;
          color: #333;
          margin-bottom: 6rpx;
        }

        .switch-desc {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

// 配置内容
.config-content {
  padding-bottom: 30rpx;
}

.config-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      display: block;
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin-bottom: 6rpx;
    }

    .section-desc {
      font-size: 12px;
      color: #999;
    }

    .section-more {
      float: right;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 认证方式网格
.methods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;

  .method-item {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30rpx 20rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      border-color: #667eea;
    }

    .method-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 20rpx;

      &.face {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.fingerprint {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.password {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.card {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }

      &.qr {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }

      &.pin {
        background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
      }
    }

    .method-name {
      font-size: 13px;
      color: #333;
    }

    .method-check {
      position: absolute;
      top: 10rpx;
      right: 10rpx;
      width: 32rpx;
      height: 32rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

// 认证策略
.strategy-options {
  .strategy-item {
    display: flex;
    align-items: center;
    padding: 30rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s;

    &:last-child {
      margin-bottom: 0;
    }

    &.active {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      border-color: #667eea;
    }

    .strategy-icon {
      width: 56rpx;
      height: 56rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;

      .strategy-number {
        font-size: 20px;
        font-weight: 600;
        color: #fff;
      }
    }

    .strategy-info {
      flex: 1;

      .strategy-name {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6rpx;
      }

      .strategy-desc {
        font-size: 12px;
        color: #999;
      }
    }

    .strategy-check {
      padding: 10rpx;
    }
  }
}

// 优先级列表
.priority-list {
  .priority-item {
    display: flex;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .priority-number {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;

      text {
        font-size: 14px;
        font-weight: 500;
        color: #fff;
      }
    }

    .priority-info {
      flex: 1;

      .priority-name {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .priority-desc {
        font-size: 12px;
        color: #999;
      }
    }

    .priority-actions {
      display: flex;
      flex-direction: column;

      .action-btn {
        width: 40rpx;
        height: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f5f5;
        border-radius: 8rpx;

        &:not(:last-child) {
          margin-bottom: 10rpx;
        }

        &.disabled {
          opacity: 0.3;
        }
      }
    }
  }
}

// 区域认证规则
.area-rules-list {
  .area-rule-item {
    padding: 30rpx;
    background: #f9f9f9;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .area-rule-header {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;

      .area-icon {
        width: 40rpx;
        height: 40rpx;
        border-radius: 10rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .area-name {
        flex: 1;
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .security-level {
        padding: 4rpx 16rpx;
        border-radius: 12rpx;
        font-size: 10px;
        color: #fff;

        &.level-1 {
          background: #52c41a;
        }

        &.level-2 {
          background: #faad14;
        }

        &.level-3 {
          background: #ff4d4f;
        }
      }
    }

    .area-rule-detail {
      .rule-methods {
        display: flex;
        flex-wrap: wrap;
        gap: 10rpx;

        .method-tag {
          padding: 6rpx 16rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 16rpx;
          font-size: 11px;
          color: #fff;
        }
      }
    }
  }

  .add-area-rule-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;
    border: 1rpx dashed #667eea;
    border-radius: 12rpx;
    margin-top: 20rpx;

    .add-text {
      margin-left: 10rpx;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 人员配置列表
.person-config-list {
  .person-config-item {
    display: flex;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .person-avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: #f5f5f5;
      margin-right: 20rpx;
    }

    .person-info {
      flex: 1;

      .person-name {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .person-dept {
        font-size: 12px;
        color: #999;
      }
    }

    .person-strategy {
      padding: 6rpx 16rpx;
      background: #f0f0f0;
      border-radius: 12rpx;
      font-size: 11px;
      color: #666;
    }
  }

  .add-person-config-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;
    border: 1rpx dashed #667eea;
    border-radius: 12rpx;
    margin-top: 20rpx;

    .add-text {
      margin-left: 10rpx;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 统计概览
.statistics-overview {
  display: flex;
  margin-bottom: 30rpx;

  .statistics-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30rpx 20rpx;
    background: #f9f9f9;
    border-radius: 16rpx;

    &:not(:last-child) {
      margin-right: 20rpx;
    }

    .statistics-value {
      font-size: 28px;
      font-weight: 600;
      color: #333;
      margin-bottom: 10rpx;

      &.success {
        color: #52c41a;
      }

      &.warning {
        color: #faad14;
      }
    }

    .statistics-label {
      font-size: 12px;
      color: #999;
    }
  }
}

// 认证方式使用率
.method-usage {
  .usage-header {
    margin-bottom: 20rpx;

    .usage-title {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }
  }

  .usage-item {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .usage-label {
      width: 120rpx;
      font-size: 12px;
      color: #666;
    }

    .usage-bar {
      flex: 1;
      height: 12rpx;
      background: #f5f5f5;
      border-radius: 6rpx;
      margin: 0 20rpx;
      overflow: hidden;

      .usage-fill {
        height: 100%;
        border-radius: 6rpx;
        transition: width 0.3s;
      }
    }

    .usage-percent {
      width: 60rpx;
      text-align: right;
      font-size: 12px;
      font-weight: 500;
    }
  }
}

// 未启用提示
.disabled-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;

  .tip-icon {
    margin-bottom: 30rpx;
  }

  .tip-title {
    font-size: 16px;
    color: #999;
    margin-bottom: 10rpx;
  }

  .tip-desc {
    font-size: 13px;
    color: #ccc;
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
