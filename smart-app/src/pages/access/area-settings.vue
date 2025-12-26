<template>
  <view class="area-settings-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#ffffff"></uni-icons>
      </view>
      <view class="nav-title">区域设置</view>
      <view class="nav-right" @click="saveSettings">
        <text class="save-text">保存</text>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content-container" scroll-y>
      <!-- 区域信息卡片 -->
      <view class="area-info-card">
        <view class="area-icon">
          <uni-icons type="location" size="32" color="#667eea"></uni-icons>
        </view>
        <view class="area-info">
          <view class="area-name">{{ areaInfo.areaName }}</view>
          <view class="area-code">{{ areaInfo.areaCode }}</view>
        </view>
        <view class="area-status" :class="{ enabled: areaInfo.enabled }">
          {{ areaInfo.enabled ? '已启用' : '已禁用' }}
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="settings-section">
        <view class="section-title">基本信息</view>

        <view class="setting-item" @click="editAreaName">
          <view class="item-label">区域名称</view>
          <view class="item-value">
            <text class="value-text">{{ areaInfo.areaName }}</text>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>

        <view class="setting-item" @click="editAreaCode">
          <view class="item-label">区域编码</view>
          <view class="item-value">
            <text class="value-text">{{ areaInfo.areaCode }}</text>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>

        <view class="setting-item" @click="selectAreaType">
          <view class="item-label">区域类型</view>
          <view class="item-value">
            <text class="value-text">{{ getAreaTypeLabel(areaInfo.areaType) }}</text>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>

        <view class="setting-item" @click="editLocation">
          <view class="item-label">位置描述</view>
          <view class="item-value">
            <text class="value-text">{{ areaInfo.location || '未设置' }}</text>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>

        <view class="setting-item">
          <view class="item-label">排序序号</view>
          <view class="item-value">
            <input
              class="sort-input"
              type="number"
              v-model="areaInfo.sortOrder"
              placeholder="请输入排序序号"
            />
          </view>
        </view>
      </view>

      <!-- 访问控制 -->
      <view class="settings-section">
        <view class="section-title">访问控制</view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">启用区域</view>
            <view class="item-desc">禁用的区域将无法使用</view>
          </view>
          <switch
            :checked="areaInfo.enabled"
            color="#667eea"
            @change="onEnabledChange"
          />
        </view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">需要审批</view>
            <view class="item-desc">进入该区域需要审批通过</view>
          </view>
          <switch
            :checked="areaInfo.requireApproval"
            color="#667eea"
            @change="onRequireApprovalChange"
          />
        </view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">限流控制</view>
            <view class="item-desc">限制区域内最大人数</view>
          </view>
          <switch
            :checked="areaInfo.enableLimit"
            color="#667eea"
            @change="onEnableLimitChange"
          />
        </view>

        <view class="setting-item" v-if="areaInfo.enableLimit" @click="editMaxCapacity">
          <view class="item-label">最大容量</view>
          <view class="item-value">
            <text class="value-text">{{ areaInfo.maxCapacity || 0 }}人</text>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 时间限制 -->
      <view class="settings-section">
        <view class="section-title">时间限制</view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">启用时间限制</view>
            <view class="item-desc">限制区域访问时间段</view>
          </view>
          <switch
            :checked="areaInfo.enableTimeLimit"
            color="#667eea"
            @change="onEnableTimeLimitChange"
          />
        </view>

        <view class="time-limits" v-if="areaInfo.enableTimeLimit">
          <view
            v-for="(limit, index) in areaInfo.timeLimits"
            :key="index"
            class="time-limit-item"
          >
            <view class="limit-info">
              <text class="limit-label">{{ limit.weekdays }}</text>
              <text class="limit-time">{{ limit.startTime }}-{{ limit.endTime }}</text>
            </view>
            <view class="limit-actions">
              <uni-icons
                type="compose"
                size="16"
                color="#667eea"
                @click="editTimeLimit(index)"
              ></uni-icons>
              <uni-icons
                type="clear"
                size="16"
                color="#ff4d4f"
                @click="removeTimeLimit(index)"
              ></uni-icons>
            </view>
          </view>

          <view class="add-limit-btn" @click="addTimeLimit">
            <uni-icons type="plus" size="16" color="#667eea"></uni-icons>
            <text class="add-limit-text">添加时间段</text>
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="settings-section">
        <view class="section-title">通知设置</view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">通行通知</view>
            <view class="item-desc">有人通行时发送通知</view>
          </view>
          <switch
            :checked="areaInfo.notifyOnPass"
            color="#667eea"
            @change="onNotifyOnPassChange"
          />
        </view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">异常告警</view>
            <view class="item-desc">检测到异常时发送告警</view>
          </view>
          <switch
            :checked="areaInfo.notifyOnAlert"
            color="#667eea"
            @change="onNotifyOnAlertChange"
          />
        </view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">容量告警</view>
            <view class="item-desc">达到容量上限时发送告警</view>
          </view>
          <switch
            :checked="areaInfo.notifyOnFull"
            color="#667eea"
            @change="onNotifyOnFullChange"
          />
        </view>

        <view class="setting-item" @click="selectNotifyUsers">
          <view class="item-label">通知人员</view>
          <view class="item-value">
            <text class="value-text">{{ areaInfo.notifyUsers?.length || 0 }}人</text>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 安全设置 -->
      <view class="settings-section">
        <view class="section-title">安全设置</view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">反潜回</view>
            <view class="item-desc">防止人员重复进入</view>
          </view>
          <switch
            :checked="areaInfo.antiPassback"
            color="#667eea"
            @change="onAntiPassbackChange"
          />
        </view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">多重验证</view>
            <view class="item-desc">需要多因素认证</view>
          </view>
          <switch
            :checked="areaInfo.multiFactor"
            color="#667eea"
            @change="onMultiFactorChange"
          />
        </view>

        <view class="setting-item switch-item">
          <view class="item-left">
            <view class="item-label">拍照记录</view>
            <view class="item-desc">通行时自动拍照</view>
          </view>
          <switch
            :checked="areaInfo.capturePhoto"
            color="#667eea"
            @change="onCapturePhotoChange"
          />
        </view>

        <view class="setting-item" @click="selectSecurityLevel">
          <view class="item-label">安全级别</view>
          <view class="item-value">
            <text class="value-text">{{ getSecurityLevelLabel(areaInfo.securityLevel) }}</text>
            <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="settings-section">
        <view class="section-title">其他设置</view>

        <view class="setting-item">
          <view class="item-label">备注信息</view>
          <textarea
            class="remark-textarea"
            placeholder="请输入备注信息"
            v-model="areaInfo.remark"
            maxlength="500"
          ></textarea>
          <view class="char-count">{{ areaInfo.remark?.length || 0 }}/500</view>
        </view>
      </view>

      <!-- 危险操作 -->
      <view class="settings-section danger-section">
        <view class="section-title danger-title">危险操作</view>

        <view class="setting-item danger-item" @click="disableArea">
          <view class="item-label danger-label">{{ areaInfo.enabled ? '禁用区域' : '启用区域' }}</view>
          <uni-icons type="right" size="14" color="#ff4d4f"></uni-icons>
        </view>

        <view class="setting-item danger-item" @click="deleteArea">
          <view class="item-label danger-label">删除区域</view>
          <uni-icons type="right" size="14" color="#ff4d4f"></uni-icons>
        </view>
      </view>

      <!-- 底部留白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 区域类型选择器 -->
    <picker
      mode="selector"
      :range="areaTypes"
      range-key="label"
      :value="areaTypeIndex"
      @change="onAreaTypeChange"
    >
      <view></view>
    </picker>

    <!-- 安全级别选择器 -->
    <picker
      mode="selector"
      :range="securityLevels"
      range-key="label"
      :value="securityLevelIndex"
      @change="onSecurityLevelChange"
    >
      <view></view>
    </picker>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// 状态栏高度
const statusBarHeight = ref(0)
const areaId = ref('')

// 区域信息
const areaInfo = reactive({
  areaName: 'A栋办公楼',
  areaCode: 'AREA-A-001',
  areaType: 'building',
  location: '主楼，位于园区中心',
  sortOrder: 1,
  enabled: true,
  requireApproval: false,
  enableLimit: true,
  maxCapacity: 100,
  enableTimeLimit: true,
  timeLimits: [
    { weekdays: '工作日', startTime: '08:00', endTime: '18:00' },
    { weekdays: '周末', startTime: '09:00', endTime: '17:00' }
  ],
  notifyOnPass: false,
  notifyOnAlert: true,
  notifyOnFull: true,
  notifyUsers: [],
  antiPassback: true,
  multiFactor: false,
  capturePhoto: true,
  securityLevel: 'medium',
  remark: ''
})

// 区域类型选项
const areaTypes = [
  { value: 'building', label: '楼宇' },
  { value: 'floor', label: '楼层' },
  { value: 'room', label: '房间' }
]
const areaTypeIndex = ref(0)

// 安全级别选项
const securityLevels = [
  { value: 'low', label: '低' },
  { value: 'medium', label: '中' },
  { value: 'high', label: '高' },
  { value: 'critical', label: '最高' }
]
const securityLevelIndex = ref(1)

// 页面加载
onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  if (options.areaId) {
    areaId.value = options.areaId
    loadAreaSettings()
  }

  // 设置初始索引
  areaTypeIndex.value = areaTypes.findIndex(t => t.value === areaInfo.areaType)
  securityLevelIndex.value = securityLevels.findIndex(l => l.value === areaInfo.securityLevel)
})

/**
 * 加载区域设置
 */
const loadAreaSettings = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAreaSettings(areaId.value)

    console.log('加载区域设置:', areaId.value)
  } catch (error) {
    console.error('加载区域设置失败:', error)
  }
}

/**
 * 获取区域类型标签
 */
const getAreaTypeLabel = (type) => {
  const typeObj = areaTypes.find(t => t.value === type)
  return typeObj ? typeObj.label : type
}

/**
 * 获取安全级别标签
 */
const getSecurityLevelLabel = (level) => {
  const levelObj = securityLevels.find(l => l.value === level)
  return levelObj ? levelObj.label : level
}

/**
 * 编辑区域名称
 */
const editAreaName = () => {
  uni.showModal({
    title: '编辑区域名称',
    editable: true,
    placeholderText: areaInfo.areaName,
    success: (res) => {
      if (res.confirm && res.content) {
        areaInfo.areaName = res.content
      }
    }
  })
}

/**
 * 编辑区域编码
 */
const editAreaCode = () => {
  uni.showModal({
    title: '编辑区域编码',
    editable: true,
    placeholderText: areaInfo.areaCode,
    success: (res) => {
      if (res.confirm && res.content) {
        areaInfo.areaCode = res.content
      }
    }
  })
}

/**
 * 选择区域类型
 */
const selectAreaType = () => {
  // 通过 picker 组件触发
}

/**
 * 区域类型变更
 */
const onAreaTypeChange = (e) => {
  areaTypeIndex.value = e.detail.value
  areaInfo.areaType = areaTypes[e.detail.value].value
}

/**
 * 编辑位置描述
 */
const editLocation = () => {
  uni.showModal({
    title: '编辑位置描述',
    editable: true,
    placeholderText: areaInfo.location || '请输入位置描述',
    success: (res) => {
      if (res.confirm) {
        areaInfo.location = res.content || ''
      }
    }
  })
}

/**
 * 启用状态变更
 */
const onEnabledChange = (e) => {
  areaInfo.enabled = e.detail.value
}

/**
 * 需要审批变更
 */
const onRequireApprovalChange = (e) => {
  areaInfo.requireApproval = e.detail.value
}

/**
 * 启用限流变更
 */
const onEnableLimitChange = (e) => {
  areaInfo.enableLimit = e.detail.value
}

/**
 * 编辑最大容量
 */
const editMaxCapacity = () => {
  uni.showModal({
    title: '编辑最大容量',
    editable: true,
    placeholderText: String(areaInfo.maxCapacity || 0),
    keyboardType: 'number',
    success: (res) => {
      if (res.confirm && res.content) {
        areaInfo.maxCapacity = parseInt(res.content) || 0
      }
    }
  })
}

/**
 * 启用时间限制变更
 */
const onEnableTimeLimitChange = (e) => {
  areaInfo.enableTimeLimit = e.detail.value
}

/**
 * 添加时间段
 */
const addTimeLimit = () => {
  uni.navigateTo({
    url: `/pages/access/time-limit-edit?mode=add&areaId=${areaId.value}`
  })
}

/**
 * 编辑时间段
 */
const editTimeLimit = (index) => {
  uni.navigateTo({
    url: `/pages/access/time-limit-edit?mode=edit&areaId=${areaId.value}&index=${index}`
  })
}

/**
 * 移除时间段
 */
const removeTimeLimit = (index) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该时间段吗？',
    success: (res) => {
      if (res.confirm) {
        areaInfo.timeLimits.splice(index, 1)
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 通行通知变更
 */
const onNotifyOnPassChange = (e) => {
  areaInfo.notifyOnPass = e.detail.value
}

/**
 * 异常告警变更
 */
const onNotifyOnAlertChange = (e) => {
  areaInfo.notifyOnAlert = e.detail.value
}

/**
 * 容量告警变更
 */
const onNotifyOnFullChange = (e) => {
  areaInfo.notifyOnFull = e.detail.value
}

/**
 * 选择通知人员
 */
const selectNotifyUsers = () => {
  uni.navigateTo({
    url: `/pages/access/notify-users-select?areaId=${areaId.value}&selected=${JSON.stringify(areaInfo.notifyUsers || [])}`
  })
}

/**
 * 反潜回变更
 */
const onAntiPassbackChange = (e) => {
  areaInfo.antiPassback = e.detail.value
}

/**
 * 多因素认证变更
 */
const onMultiFactorChange = (e) => {
  areaInfo.multiFactor = e.detail.value
}

/**
 * 拍照记录变更
 */
const onCapturePhotoChange = (e) => {
  areaInfo.capturePhoto = e.detail.value
}

/**
 * 选择安全级别
 */
const selectSecurityLevel = () => {
  // 通过 picker 组件触发
}

/**
 * 安全级别变更
 */
const onSecurityLevelChange = (e) => {
  securityLevelIndex.value = e.detail.value
  areaInfo.securityLevel = securityLevels[e.detail.value].value
}

/**
 * 保存设置
 */
const saveSettings = async () => {
  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    // TODO: 调用实际的API
    // await areaApi.updateAreaSettings(areaId.value, areaInfo)

    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
    }, 500)
  } catch (error) {
    uni.hideLoading()
    console.error('保存设置失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

/**
 * 禁用/启用区域
 */
const disableArea = () => {
  const action = areaInfo.enabled ? '禁用' : '启用'
  uni.showModal({
    title: `确认${action}`,
    content: `确定要${action}该区域吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用实际的API
          // await areaApi.toggleAreaStatus(areaId.value, !areaInfo.enabled)

          areaInfo.enabled = !areaInfo.enabled
          uni.showToast({
            title: `${action}成功`,
            icon: 'success'
          })
        } catch (error) {
          uni.showToast({
            title: `${action}失败`,
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 删除区域
 */
const deleteArea = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该区域吗？删除后无法恢复！',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用实际的API
          // await areaApi.deleteArea(areaId.value)

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })

          setTimeout(() => {
            uni.navigateBack()
          }, 500)
        } catch (error) {
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.area-settings-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

// 自定义导航栏
.custom-navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  height: 44px;

  .nav-left {
    width: 40px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #ffffff;
    flex: 1;
    text-align: center;
  }

  .nav-right {
    width: 60px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;

    .save-text {
      font-size: 15px;
      color: #ffffff;
      font-weight: 500;
    }
  }
}

// 内容区域
.content-container {
  padding: 30rpx;
}

// 区域信息卡片
.area-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);

  .area-icon {
    width: 80rpx;
    height: 80rpx;
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .area-info {
    flex: 1;

    .area-name {
      font-size: 18px;
      font-weight: 600;
      color: #ffffff;
      margin-bottom: 8rpx;
    }

    .area-code {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.8);
    }
  }

  .area-status {
    padding: 8rpx 16rpx;
    border-radius: 12rpx;
    font-size: 12px;
    background-color: rgba(255, 255, 255, 0.2);
    color: #ffffff;

    &.enabled {
      background-color: rgba(82, 196, 26, 0.9);
    }
  }
}

// 设置区块
.settings-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  overflow: hidden;

  .section-title {
    padding: 24rpx 30rpx 16rpx;
    font-size: 15px;
    font-weight: 500;
    color: #333;
    border-bottom: 1px solid #f0f0f0;
  }

  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 30rpx;
    border-bottom: 1px solid #f0f0f0;
    position: relative;

    &:last-child {
      border-bottom: none;
    }

    &.switch-item {
      .item-left {
        flex: 1;

        .item-label {
          font-size: 15px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .item-desc {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .item-label {
      font-size: 15px;
      color: #333;
      flex: 1;
    }

    .item-value {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .value-text {
        font-size: 14px;
        color: #666;
        max-width: 400rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .sort-input {
        width: 120rpx;
        height: 56rpx;
        padding: 0 16rpx;
        background-color: #f5f5f5;
        border-radius: 8rpx;
        font-size: 14px;
        color: #333;
        text-align: right;

        &::placeholder {
          color: #999;
        }
      }
    }

    .remark-textarea {
      flex: 1;
      min-height: 120rpx;
      padding: 16rpx;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      font-size: 14px;
      color: #333;
      margin-top: 16rpx;

      &::placeholder {
        color: #999;
      }
    }

    .char-count {
      position: absolute;
      bottom: 24rpx;
      right: 30rpx;
      font-size: 12px;
      color: #999;
    }
  }

  // 时间限制列表
  .time-limits {
    padding: 0 30rpx 20rpx;

    .time-limit-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx;
      background-color: #f5f5f5;
      border-radius: 12rpx;
      margin-bottom: 16rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .limit-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .limit-label {
          font-size: 14px;
          color: #333;
          font-weight: 500;
        }

        .limit-time {
          font-size: 13px;
          color: #667eea;
        }
      }

      .limit-actions {
        display: flex;
        gap: 16rpx;
      }
    }

    .add-limit-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      padding: 20rpx;
      background-color: rgba(102, 126, 234, 0.1);
      border-radius: 12rpx;
      margin-top: 16rpx;

      .add-limit-text {
        font-size: 14px;
        color: #667eea;
      }
    }
  }

  // 危险操作样式
  &.danger-section {
    .section-title {
      &.danger-title {
        color: #ff4d4f;
      }
    }

    .setting-item {
      &.danger-item {
        .item-label {
          &.danger-label {
            color: #ff4d4f;
          }
        }
      }
    }
  }
}

// 底部留白
.bottom-spacer {
  height: 60rpx;
}
</style>
