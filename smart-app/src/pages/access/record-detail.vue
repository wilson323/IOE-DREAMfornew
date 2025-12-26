<template>
  <view class="record-detail-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">记录详情</text>
        </view>
        <view class="navbar-right">
          <view class="share-btn" @tap="shareRecord">
            <uni-icons type="redo" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <scroll-view class="page-content" scroll-y>
      <!-- 状态头部 -->
      <view class="status-header" :class="recordDetail.status">
        <view class="status-icon">
          <uni-icons :type="getStatusIcon(recordDetail.status)" size="60" color="#fff"></uni-icons>
        </view>
        <view class="status-info">
          <text class="status-title">{{ getStatusTitle(recordDetail.status) }}</text>
          <text class="status-time">{{ formatFullTime(recordDetail.passTime) }}</text>
        </view>
      </view>

      <!-- 人员信息卡片 -->
      <view class="info-card">
        <view class="card-title">
          <uni-icons type="person" size="18" color="#667eea"></uni-icons>
          <text class="title-text">人员信息</text>
        </view>

        <view class="person-detail">
          <image :src="recordDetail.avatar" class="person-avatar" mode="aspectFill"></image>
          <view class="person-info">
            <view class="info-row">
              <text class="info-label">姓名:</text>
              <text class="info-value">{{ recordDetail.personName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">工号:</text>
              <text class="info-value">{{ recordDetail.personCode }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">部门:</text>
              <text class="info-value">{{ recordDetail.departmentName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">职位:</text>
              <text class="info-value">{{ recordDetail.position }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 通行信息卡片 -->
      <view class="info-card">
        <view class="card-title">
          <uni-icons type="home" size="18" color="#667eea"></uni-icons>
          <text class="title-text">通行信息</text>
        </view>

        <view class="detail-list">
          <view class="detail-item">
            <text class="detail-label">通行设备:</text>
            <text class="detail-value">{{ recordDetail.doorName }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">所在区域:</text>
            <text class="detail-value">{{ recordDetail.areaName }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">通行方向:</text>
            <view class="direction-badge" :class="recordDetail.direction">
              <uni-icons :type="recordDetail.direction === 'in' ? 'up' : 'down'" size="14" color="#fff"></uni-icons>
              <text class="direction-text">{{ recordDetail.direction === 'in' ? '进入' : '离开' }}</text>
            </view>
          </view>
          <view class="detail-item">
            <text class="detail-label">验证方式:</text>
            <text class="detail-value">{{ recordDetail.verifyMethod }}</text>
          </view>
          <view class="detail-item" v-if="recordDetail.verifyResult">
            <text class="detail-label">验证结果:</text>
            <text class="detail-value">{{ recordDetail.verifyResult }}</text>
          </view>
        </view>
      </view>

      <!-- 拒绝/告警原因 -->
      <view class="info-card warning" v-if="recordDetail.status === 'denied' || recordDetail.status === 'alarm'">
        <view class="card-title warning">
          <uni-icons type="notification" size="18" color="#ff6b6b"></uni-icons>
          <text class="title-text">{{ recordDetail.status === 'denied' ? '拒绝原因' : '告警信息' }}</text>
        </view>

        <view class="warning-content">
          <view class="warning-item" v-if="recordDetail.rejectReason">
            <text class="warning-label">拒绝原因:</text>
            <text class="warning-value">{{ recordDetail.rejectReason }}</text>
          </view>
          <view class="warning-item" v-if="recordDetail.alarmType">
            <text class="warning-label">告警类型:</text>
            <text class="warning-value">{{ recordDetail.alarmType }}</text>
          </view>
          <view class="warning-item" v-if="recordDetail.alarmLevel">
            <text class="warning-label">告警等级:</text>
            <view class="alarm-level-badge" :class="recordDetail.alarmLevel">
              <text>{{ getAlarmLevelText(recordDetail.alarmLevel) }}</text>
            </view>
          </view>
          <view class="warning-item" v-if="recordDetail.alarmDesc">
            <text class="warning-label">详细说明:</text>
            <text class="warning-desc">{{ recordDetail.alarmDesc }}</text>
          </view>
        </view>
      </view>

      <!-- 抓拍图片 -->
      <view class="info-card" v-if="recordDetail.snapshotImage || recordDetail.hasVideo">
        <view class="card-title">
          <uni-icons type="camera" size="18" color="#667eea"></uni-icons>
          <text class="title-text">多媒体资料</text>
        </view>

        <view class="media-content">
          <view class="snapshot-section" v-if="recordDetail.snapshotImage">
            <view class="section-label">抓拍图片</view>
            <image
              :src="recordDetail.snapshotImage"
              class="snapshot-image"
              mode="widthFix"
              @tap="previewSnapshot"
            ></image>
          </view>

          <view class="video-section" v-if="recordDetail.hasVideo">
            <view class="section-label">视频录像</view>
            <view class="video-player" @tap="playVideo">
              <image :src="recordDetail.snapshotImage" class="video-thumb" mode="aspectFill"></image>
              <view class="play-overlay">
                <uni-icons type="play-filled" size="40" color="#fff"></uni-icons>
              </view>
              <view class="video-duration">{{ recordDetail.videoDuration || '00:30' }}</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 设备信息 -->
      <view class="info-card">
        <view class="card-title">
          <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
          <text class="title-text">设备信息</text>
        </view>

        <view class="detail-list">
          <view class="detail-item">
            <text class="detail-label">设备名称:</text>
            <text class="detail-value">{{ recordDetail.doorName }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">设备编号:</text>
            <text class="detail-value">{{ recordDetail.deviceCode }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">设备位置:</text>
            <text class="detail-value">{{ recordDetail.deviceLocation }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">IP地址:</text>
            <text class="detail-value">{{ recordDetail.deviceIp }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">设备状态:</text>
            <view class="device-status-badge online">
              <text>在线</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 处理记录 -->
      <view class="info-card" v-if="recordDetail.handledBy || recordDetail.handleRemark">
        <view class="card-title">
          <uni-icons type="compose" size="18" color="#667eea"></uni-icons>
          <text class="title-text">处理记录</text>
        </view>

        <view class="handle-content">
          <view class="handle-item" v-if="recordDetail.handledBy">
            <text class="handle-label">处理人:</text>
            <text class="handle-value">{{ recordDetail.handledBy }}</text>
          </view>
          <view class="handle-item" v-if="recordDetail.handleTime">
            <text class="handle-label">处理时间:</text>
            <text class="handle-value">{{ formatFullTime(recordDetail.handleTime) }}</text>
          </view>
          <view class="handle-item" v-if="recordDetail.handleRemark">
            <text class="handle-label">处理说明:</text>
            <text class="handle-remark">{{ recordDetail.handleRemark }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <view class="action-btn secondary" @tap="exportRecord">
          <uni-icons type="download" size="18" color="#667eea"></uni-icons>
          <text class="btn-text">导出记录</text>
        </view>
        <view class="action-btn secondary" @tap="printRecord">
          <uni-icons type="printer" size="18" color="#667eea"></uni-icons>
          <text class="btn-text">打印</text>
        </view>
        <view class="action-btn primary" @tap="handleAlarm" v-if="recordDetail.status === 'alarm' && !recordDetail.handledBy">
          <uni-icons type="checkmarkempty" size="18" color="#fff"></uni-icons>
          <text class="btn-text">处理告警</text>
        </view>
      </view>
    </scroll-view>

    <!-- 告警处理弹窗 -->
    <uni-popup ref="handlePopup" type="center">
      <view class="handle-popup">
        <view class="popup-header">
          <text class="popup-title">处理告警</text>
          <view class="close-btn" @tap="closeHandlePopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <view class="form-section">
            <view class="form-label">
              <text class="label-text">处理方式</text>
              <text class="required">*</text>
            </view>
            <picker mode="selector" :range="handleMethods" @change="onHandleMethodChange">
              <view class="picker-input">
                <text class="picker-text" :class="{ placeholder: !selectedHandleMethod }">
                  {{ selectedHandleMethod || '请选择处理方式' }}
                </text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <view class="form-section">
            <view class="form-label">
              <text class="label-text">处理说明</text>
              <text class="required">*</text>
            </view>
            <textarea
              class="textarea-input"
              v-model="handleRemark"
              placeholder="请详细说明处理情况"
              maxlength="500"
            ></textarea>
            <view class="char-count">{{ handleRemark.length }}/500</view>
          </view>
        </view>

        <view class="popup-actions">
          <view class="popup-btn secondary" @tap="closeHandlePopup">
            <text>取消</text>
          </view>
          <view class="popup-btn primary" @tap="confirmHandle">
            <text>确认处理</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 记录详情
const recordDetail = reactive({
  recordId: '',
  personName: '张三',
  personCode: 'E001234',
  departmentName: '技术部',
  position: '软件工程师',
  avatar: 'https://picsum.photos/200/200?random=1',
  doorName: '主入口门禁',
  areaName: 'A栋1楼大厅',
  passTime: Date.now() - 3600000,
  direction: 'in',
  status: 'success',
  verifyMethod: '人脸识别',
  verifyResult: '识别成功',
  rejectReason: '',
  alarmType: '',
  alarmLevel: '',
  alarmDesc: '',
  snapshotImage: 'https://picsum.photos/600/400?random=1',
  hasVideo: true,
  videoDuration: '00:30',
  deviceCode: 'D0001',
  deviceLocation: 'A栋1楼主入口',
  deviceIp: '192.168.1.101',
  handledBy: '',
  handleTime: null,
  handleRemark: ''
})

// 告警处理
const selectedHandleMethod = ref('')
const handleRemark = ref('')
const handleMethods = ['现场查看', '电话核实', '视频确认', '已解决误报', '上报主管']

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  // 获取记录详情
  loadRecordDetail()
})

// 加载记录详情
const loadRecordDetail = () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.recordId) {
    recordDetail.recordId = options.recordId
    // 根据recordId加载详情
  }
}

// 获取状态图标
const getStatusIcon = (status) => {
  const icons = {
    success: 'checkmarkempty',
    denied: 'close',
    alarm: 'notification'
  }
  return icons[status] || 'info'
}

// 获取状态标题
const getStatusTitle = (status) => {
  const titles = {
    success: '通行成功',
    denied: '通行拒绝',
    alarm: '告警记录'
  }
  return titles[status] || '未知'
}

// 获取告警等级文本
const getAlarmLevelText = (level) => {
  const texts = {
    high: '高危',
    medium: '中等',
    low: '低级'
  }
  return texts[level] || '未知'
}

// 格式化完整时间
const formatFullTime = (timestamp) => {
  if (!timestamp) return '--'

  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

// 预览抓拍
const previewSnapshot = () => {
  uni.previewImage({
    urls: [recordDetail.snapshotImage],
    current: recordDetail.snapshotImage
  })
}

// 播放视频
const playVideo = () => {
  uni.navigateTo({
    url: `/pages/access/record-video?recordId=${recordDetail.recordId}`
  })
}

// 导出记录
const exportRecord = () => {
  uni.showLoading({
    title: '导出中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  }, 1500)
}

// 打印记录
const printRecord = () => {
  uni.showLoading({
    title: '准备打印...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '发送到打印队列',
      icon: 'success'
    })
  }, 1000)
}

// 分享记录
const shareRecord = () => {
  uni.share({
    provider: 'weixin',
    scene: 'WXSceneSession',
    type: 0,
    title: '通行记录详情',
    imageUrl: recordDetail.snapshotImage
  })
}

// 处理告警
const handleAlarm = () => {
  uni.$emit('openHandlePopup')
}

const closeHandlePopup = () => {
  uni.$emit('closeHandlePopup')
}

const onHandleMethodChange = (e) => {
  selectedHandleMethod.value = handleMethods[e.detail.value]
}

const confirmHandle = () => {
  if (!selectedHandleMethod.value) {
    uni.showToast({
      title: '请选择处理方式',
      icon: 'none'
    })
    return
  }

  if (!handleRemark.value) {
    uni.showToast({
      title: '请填写处理说明',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '提交中...'
  })

  setTimeout(() => {
    uni.hideLoading()

    recordDetail.handledBy = '当前用户'
    recordDetail.handleTime = Date.now()
    recordDetail.handleRemark = handleRemark.value

    closeHandlePopup()

    uni.showToast({
      title: '处理成功',
      icon: 'success'
    })
  }, 1000)
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.record-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e8ecf1 100%);
}

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
      gap: 10rpx;

      .back-text {
        font-size: 28rpx;
        color: #fff;
      }
    }

    .navbar-center {
      .navbar-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .navbar-right {
      .share-btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.page-content {
  padding-top: calc(44px + var(--status-bar-height));
  padding-bottom: 40rpx;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 30rpx;
  padding: 60rpx 30rpx;
  margin-bottom: 30rpx;

  &.success {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  }

  &.denied {
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
  }

  &.alarm {
    background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
  }

  .status-icon {
    width: 120rpx;
    height: 120rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 24rpx;
  }

  .status-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12rpx;

    .status-title {
      font-size: 36rpx;
      font-weight: 700;
      color: #fff;
    }

    .status-time {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

.info-card {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  &.warning {
    border-left: 4rpx solid #ff6b6b;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 30rpx;
    padding-bottom: 20rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &.warning {
      .title-text {
        color: #ff6b6b;
      }
    }

    .title-text {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .person-detail {
    display: flex;
    gap: 30rpx;

    .person-avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
    }

    .person-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 20rpx;

      .info-row {
        display: flex;
        align-items: center;

        .info-label {
          min-width: 120rpx;
          font-size: 26rpx;
          color: #999;
        }

        .info-value {
          font-size: 28rpx;
          color: #333;
        }
      }
    }
  }

  .detail-list {
    .detail-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .detail-label {
        font-size: 26rpx;
        color: #999;
      }

      .detail-value {
        font-size: 28rpx;
        color: #333;
      }

      .direction-badge {
        display: flex;
        align-items: center;
        gap: 6rpx;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.in {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
          color: #fff;
        }

        &.out {
          background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
          color: #fff;
        }

        .direction-text {
          font-size: 24rpx;
        }
      }

      .device-status-badge {
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.online {
          background: #f6ffed;
          color: #52c41a;
        }

        &.offline {
          background: #fff2f0;
          color: #ff6b6b;
        }
      }
    }
  }

  .warning-content {
    .warning-item {
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .warning-label {
        display: block;
        font-size: 26rpx;
        color: #999;
        margin-bottom: 12rpx;
      }

      .warning-value {
        font-size: 28rpx;
        color: #ff6b6b;
        font-weight: 600;
      }

      .alarm-level-badge {
        display: inline-block;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.high {
          background: #ff6b6b;
          color: #fff;
        }

        &.medium {
          background: #ffa940;
          color: #fff;
        }

        &.low {
          background: #ffec3d;
          color: #fff;
        }
      }

      .warning-desc {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
      }
    }
  }

  .media-content {
    .snapshot-section,
    .video-section {
      margin-bottom: 30rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .section-label {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 16rpx;
      }

      .snapshot-image {
        width: 100%;
        border-radius: 16rpx;
      }

      .video-player {
        position: relative;
        width: 100%;
        height: 400rpx;
        border-radius: 16rpx;
        overflow: hidden;

        .video-thumb {
          width: 100%;
          height: 100%;
        }

        .play-overlay {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 100rpx;
          height: 100rpx;
          background: rgba(0, 0, 0, 0.5);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .video-duration {
          position: absolute;
          bottom: 20rpx;
          right: 20rpx;
          padding: 8rpx 16rpx;
          background: rgba(0, 0, 0, 0.6);
          border-radius: 8rpx;
          font-size: 24rpx;
          color: #fff;
        }
      }
    }
  }

  .handle-content {
    .handle-item {
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .handle-label {
        display: block;
        font-size: 26rpx;
        color: #999;
        margin-bottom: 12rpx;
      }

      .handle-value {
        font-size: 28rpx;
        color: #333;
      }

      .handle-remark {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 30rpx;

  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    height: 80rpx;
    border-radius: 16rpx;
    font-size: 28rpx;

    &.secondary {
      background: #fff;
      color: #667eea;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
    }

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }

    .btn-text {
      font-size: 28rpx;
    }
  }
}

// 处理弹窗
.handle-popup {
  width: 690rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    padding: 30rpx;

    .form-section {
      margin-bottom: 30rpx;

      .form-label {
        margin-bottom: 20rpx;

        .label-text {
          font-size: 28rpx;
          color: #333;
          font-weight: 600;
        }

        .required {
          color: #ff6b6b;
          margin-left: 4rpx;
        }
      }

      .picker-input {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx 30rpx;
        background: #f5f5f5;
        border-radius: 16rpx;

        .picker-text {
          font-size: 28rpx;

          &.placeholder {
            color: #999;
          }
        }
      }

      .textarea-input {
        width: 100%;
        min-height: 200rpx;
        padding: 24rpx 30rpx;
        background: #f5f5f5;
        border-radius: 16rpx;
        font-size: 28rpx;
        line-height: 1.6;
      }

      .char-count {
        text-align: right;
        margin-top: 12rpx;
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .popup-actions {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .popup-btn {
      flex: 1;
      height: 80rpx;
      line-height: 80rpx;
      text-align: center;
      border-radius: 16rpx;
      font-size: 28rpx;

      &.secondary {
        background: #f5f5f5;
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}
</style>
