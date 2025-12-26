<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px', height: (statusBarHeight + 44) + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="back-text">取消</text>
        </view>
        <view class="navbar-title">
          <text class="title-text">发起申请</text>
        </view>
        <view class="navbar-right">
          <view class="draft-btn" @tap="saveDraft">
            <text>草稿</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <scroll-view class="page-content" scroll-y enable-back-to-top>
      <!-- 申请类型选择 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="doc" size="18" color="#667eea"></uni-icons>
          <text class="section-title">申请类型</text>
        </view>
        <view class="type-grid">
          <view
            class="type-item"
            :class="{ active: selectedType === 'access' }"
            @tap="selectType('access')"
          >
            <view class="type-icon access">
              <uni-icons type="locked" size="28" color="#fff"></uni-icons>
            </view>
            <text class="type-name">门禁权限</text>
            <text class="type-desc">申请区域通行权限</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedType === 'visitor' }"
            @tap="selectType('visitor')"
          >
            <view class="type-icon visitor">
              <uni-icons type="person" size="28" color="#fff"></uni-icons>
            </view>
            <text class="type-name">访客预约</text>
            <text class="type-desc">预约访客进入</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedType === 'overtime' }"
            @tap="selectType('overtime')"
          >
            <view class="type-icon overtime">
              <uni-icons type="clock" size="28" color="#fff"></uni-icons>
            </view>
            <text class="type-name">加班申请</text>
            <text class="type-desc">申请加班权限</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedType === 'leave' }"
            @tap="selectType('leave')"
          >
            <view class="type-icon leave">
              <uni-icons type="calendar" size="28" color="#fff"></uni-icons>
            </view>
            <text class="type-name">请假申请</text>
            <text class="type-desc">申请临时离岗</text>
          </view>
        </view>
      </view>

      <!-- 门禁权限申请表单 -->
      <view class="form-section" v-if="selectedType === 'access'">
        <!-- 区域选择 -->
        <view class="section-card">
          <view class="section-header">
            <uni-icons type="location" size="18" color="#667eea"></uni-icons>
            <text class="section-title">访问区域</text>
            <text class="required">*</text>
          </view>
          <view class="area-selector">
            <view class="selector-trigger" @tap="showAreaPicker">
              <text v-if="!selectedAreas.length" class="placeholder">请选择访问区域</text>
              <view v-else class="selected-tags">
                <view class="tag-item" v-for="(area, index) in selectedAreas" :key="index">
                  <text>{{ area.areaName }}</text>
                  <view class="tag-close" @tap.stop="removeArea(index)">
                    <uni-icons type="close" size="12" color="#fff"></uni-icons>
                  </view>
                </view>
              </view>
              <uni-icons type="right" size="14" color="#999"></uni-icons>
            </view>
          </view>
        </view>

        <!-- 生效时间 -->
        <view class="section-card">
          <view class="section-header">
            <uni-icons type="calendar" size="18" color="#667eea"></uni-icons>
            <text class="section-title">生效时间</text>
            <text class="required">*</text>
          </view>
          <view class="time-range">
            <view class="time-field">
              <text class="field-label">开始时间</text>
              <picker mode="date" :value="startTime" @change="onStartTimeChange">
                <view class="picker-input">
                  <text>{{ startTime || '请选择开始日期' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="time-separator">至</view>
            <view class="time-field">
              <text class="field-label">结束时间</text>
              <picker mode="date" :value="endTime" @change="onEndTimeChange">
                <view class="picker-input">
                  <text>{{ endTime || '请选择结束日期' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </view>
        </view>

        <!-- 时间段限制 -->
        <view class="section-card">
          <view class="section-header">
            <uni-icons type="clock" size="18" color="#667eea"></uni-icons>
            <text class="section-title">通行时段</text>
          </view>
          <view class="time-setting">
            <view class="setting-switch">
              <text class="switch-label">限制通行时段</text>
              <switch :checked="limitTimeRange" @change="onLimitTimeChange" color="#667eea" />
            </view>
            <view class="time-range-inputs" v-if="limitTimeRange">
              <picker mode="time" :value="startHour" @change="onStartHourChange">
                <view class="time-input">
                  <text>{{ startHour || '开始时间' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
              <text class="time-separator">至</text>
              <picker mode="time" :value="endHour" @change="onEndHourChange">
                <view class="time-input">
                  <text>{{ endHour || '结束时间' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </view>
        </view>
      </view>

      <!-- 访客预约表单 -->
      <view class="form-section" v-if="selectedType === 'visitor'">
        <!-- 访客信息 -->
        <view class="section-card">
          <view class="section-header">
            <uni-icons type="person" size="18" color="#667eea"></uni-icons>
            <text class="section-title">访客信息</text>
            <text class="required">*</text>
          </view>
          <view class="form-list">
            <view class="form-item">
              <text class="form-label">访客姓名</text>
              <input class="form-input" v-model="visitorForm.name" placeholder="请输入访客姓名" />
            </view>
            <view class="form-item">
              <text class="form-label">手机号码</text>
              <input class="form-input" v-model="visitorForm.phone" type="number" placeholder="请输入手机号码" />
            </view>
            <view class="form-item">
              <text class="form-label">访客单位</text>
              <input class="form-input" v-model="visitorForm.company" placeholder="请输入访客单位" />
            </view>
            <view class="form-item">
              <text class="form-label">同行人数</text>
              <input class="form-input" v-model="visitorForm.count" type="number" placeholder="请输入同行人数（含本人）" />
            </view>
          </view>
        </view>

        <!-- 访问时间 -->
        <view class="section-card">
          <view class="section-header">
            <uni-icons type="calendar" size="18" color="#667eea"></uni-icons>
            <text class="section-title">访问时间</text>
            <text class="required">*</text>
          </view>
          <view class="visit-time">
            <view class="time-field full">
              <text class="field-label">访问日期</text>
              <picker mode="date" :value="visitDate" @change="onVisitDateChange">
                <view class="picker-input">
                  <text>{{ visitDate || '请选择访问日期' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="time-field">
              <text class="field-label">开始时间</text>
              <picker mode="time" :value="visitStartTime" @change="onVisitStartTimeChange">
                <view class="picker-input">
                  <text>{{ visitStartTime || '开始' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="time-field">
              <text class="field-label">结束时间</text>
              <picker mode="time" :value="visitEndTime" @change="onVisitEndTimeChange">
                <view class="picker-input">
                  <text>{{ visitEndTime || '结束' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </view>
        </view>
      </view>

      <!-- 加班申请表单 -->
      <view class="form-section" v-if="selectedType === 'overtime'">
        <view class="section-card">
          <view class="section-header">
            <uni-icons type="calendar" size="18" color="#667eea"></uni-icons>
            <text class="section-title">加班信息</text>
            <text class="required">*</text>
          </view>
          <view class="form-list">
            <view class="form-item">
              <text class="form-label">加班日期</text>
              <picker mode="date" :value="overtimeDate" @change="onOvertimeDateChange">
                <view class="picker-input-full">
                  <text>{{ overtimeDate || '请选择加班日期' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">开始时间</text>
              <picker mode="time" :value="overtimeStartTime" @change="onOvertimeStartTimeChange">
                <view class="picker-input-full">
                  <text>{{ overtimeStartTime || '请选择开始时间' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">结束时间</text>
              <picker mode="time" :value="overtimeEndTime" @change="onOvertimeEndTimeChange">
                <view class="picker-input-full">
                  <text>{{ overtimeEndTime || '请选择结束时间' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">预计工时</text>
              <text class="form-value">{{ calculatedHours }}小时</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 请假申请表单 -->
      <view class="form-section" v-if="selectedType === 'leave'">
        <view class="section-card">
          <view class="section-header">
            <uni-icons type="calendar" size="18" color="#667eea"></uni-icons>
            <text class="section-title">请假信息</text>
            <text class="required">*</text>
          </view>
          <view class="form-list">
            <view class="form-item">
              <text class="form-label">请假类型</text>
              <picker mode="selector" :range="leaveTypes" range-key="name" @change="onLeaveTypeChange">
                <view class="picker-input-full">
                  <text>{{ selectedLeaveType ? selectedLeaveType.name : '请选择请假类型' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">开始日期</text>
              <picker mode="date" :value="leaveStartDate" @change="onLeaveStartDateChange">
                <view class="picker-input-full">
                  <text>{{ leaveStartDate || '请选择开始日期' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">结束日期</text>
              <picker mode="date" :value="leaveEndDate" @change="onLeaveEndDateChange">
                <view class="picker-input-full">
                  <text>{{ leaveEndDate || '请选择结束日期' }}</text>
                  <uni-icons type="right" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">请假天数</text>
              <text class="form-value">{{ calculatedDays }}天</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 申请事由（所有类型通用） -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="compose" size="18" color="#667eea"></uni-icons>
          <text class="section-title">申请事由</text>
          <text class="required">*</text>
        </view>
        <view class="reason-input">
          <textarea
            v-model="applyReason"
            placeholder="请详细说明申请事由"
            maxlength="500"
          ></textarea>
          <view class="input-count">{{ applyReason.length }}/500</view>
        </view>
      </view>

      <!-- 备注说明 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="info" size="18" color="#667eea"></uni-icons>
          <text class="section-title">备注说明</text>
        </view>
        <view class="remark-input">
          <textarea
            v-model="applyRemark"
            placeholder="如有其他说明，请在此填写（选填）"
            maxlength="500"
          ></textarea>
          <view class="input-count">{{ applyRemark.length }}/500</view>
        </view>
      </view>

      <!-- 附件上传 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="paperclip" size="18" color="#667eea"></uni-icons>
          <text class="section-title">附件上传</text>
          <text class="attachment-count">({{ attachments.length }})</text>
        </view>
        <view class="attachment-list">
          <view
            class="attachment-item"
            v-for="(file, index) in attachments"
            :key="index"
          >
            <view class="file-info">
              <uni-icons :type="getFileIcon(file.type)" size="32" color="#667eea"></uni-icons>
              <text class="file-name">{{ file.name }}</text>
            </view>
            <view class="file-remove" @tap="removeAttachment(index)">
              <uni-icons type="close" size="16" color="#ef4444"></uni-icons>
            </view>
          </view>
          <view class="upload-btn" @tap="chooseAttachment">
            <uni-icons type="plus" size="24" color="#999"></uni-icons>
            <text>添加附件</text>
          </view>
        </view>
      </view>

      <!-- 紧急程度 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="notification" size="18" color="#667eea"></uni-icons>
          <text class="section-title">紧急程度</text>
        </view>
        <view class="urgency-options">
          <view
            class="urgency-item"
            :class="{ active: selectedUrgency === 'normal' }"
            @tap="selectUrgency('normal')"
          >
            <view class="urgency-dot normal"></view>
            <text>普通</text>
          </view>
          <view
            class="urgency-item"
            :class="{ active: selectedUrgency === 'urgent' }"
            @tap="selectUrgency('urgent')"
          >
            <view class="urgency-dot urgent"></view>
            <text>紧急</text>
          </view>
          <view
            class="urgency-item"
            :class="{ active: selectedUrgency === 'very-urgent' }"
            @tap="selectUrgency('very-urgent')"
          >
            <view class="urgency-dot very-urgent"></view>
            <text>非常紧急</text>
          </view>
        </view>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <view class="submit-btn" @tap="submitApplication">
        <uni-icons type="paperplane" size="18" color="#fff"></uni-icons>
        <text>提交申请</text>
      </view>
    </view>

    <!-- 区域选择弹窗 -->
    <uni-popup ref="areaPopup" type="bottom">
      <view class="area-popup">
        <view class="popup-header">
          <text class="popup-title">选择访问区域</text>
          <view class="close-btn" @tap="closeAreaPopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <scroll-view class="popup-content" scroll-y>
          <view class="area-list">
            <view
              class="area-item"
              :class="{ selected: isAreaSelected(area.areaId) }"
              v-for="area in availableAreas"
              :key="area.areaId"
              @tap="toggleArea(area)"
            >
              <view class="area-info">
                <text class="area-name">{{ area.areaName }}</text>
                <text class="area-path">{{ area.areaPath }}</text>
              </view>
              <view class="area-check" v-if="isAreaSelected(area.areaId)">
                <uni-icons type="checkmarkempty" size="18" color="#667eea"></uni-icons>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="popup-footer">
          <view class="footer-btn" @tap="confirmAreaSelection">
            <text>确定({{ selectedAreas.length }})</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 选中的申请类型
const selectedType = ref('access')

// 门禁权限表单数据
const selectedAreas = ref([])
const startTime = ref('')
const endTime = ref('')
const limitTimeRange = ref(false)
const startHour = ref('08:00')
const endHour = ref('18:00')

// 访客表单数据
const visitorForm = reactive({
  name: '',
  phone: '',
  company: '',
  count: '1',
})
const visitDate = ref('')
const visitStartTime = ref('09:00')
const visitEndTime = ref('18:00')

// 加班表单数据
const overtimeDate = ref('')
const overtimeStartTime = ref('18:00')
const overtimeEndTime = ref('22:00')

// 请假表单数据
const leaveTypes = [
  { value: 1, name: '事假' },
  { value: 2, name: '病假' },
  { value: 3, name: '年假' },
  { value: 4, name: '调休' },
  { value: 5, name: '其他' },
]
const selectedLeaveType = ref(null)
const leaveStartDate = ref('')
const leaveEndDate = ref('')

// 通用表单数据
const applyReason = ref('')
const applyRemark = ref('')
const attachments = ref([])
const selectedUrgency = ref('normal')

// 可用区域列表
const availableAreas = ref([
  { areaId: 1, areaName: 'A栋1楼大厅', areaPath: 'A栋 > 1楼 > 大厅' },
  { areaId: 2, areaName: 'A栋2楼办公区', areaPath: 'A栋 > 2楼 > 办公区' },
  { areaId: 3, areaName: 'B栋1楼大厅', areaPath: 'B栋 > 1楼 > 大厅' },
  { areaId: 4, areaName: 'B栋2楼会议室', areaPath: 'B栋 > 2楼 > 会议室' },
  { areaId: 5, areaName: 'C栋1楼大厅', areaPath: 'C栋 > 1楼 > 大厅' },
])

// 弹窗引用
const areaPopup = ref(null)

// 计算加班工时
const calculatedHours = computed(() => {
  if (!overtimeStartTime.value || !overtimeEndTime.value) return 0
  const start = new Date(`2000-01-01 ${overtimeStartTime.value}`)
  const end = new Date(`2000-01-01 ${overtimeEndTime.value}`)
  if (end < start) return 0
  const diff = (end - start) / (1000 * 60 * 60)
  return diff.toFixed(1)
})

// 计算请假天数
const calculatedDays = computed(() => {
  if (!leaveStartDate.value || !leaveEndDate.value) return 0
  const start = new Date(leaveStartDate.value)
  const end = new Date(leaveEndDate.value)
  if (end < start) return 0
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
  return diff
})

// 返回上一页
const goBack = () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消申请吗？已填写的内容将被清空',
    success: (res) => {
      if (res.confirm) {
        uni.navigateBack()
      }
    },
  })
}

// 保存草稿
const saveDraft = () => {
  uni.showToast({
    title: '草稿已保存',
    icon: 'success',
  })
}

// 选择申请类型
const selectType = (type) => {
  selectedType.value = type
}

// 显示区域选择器
const showAreaPicker = () => {
  areaPopup.value?.open()
}

// 关闭区域弹窗
const closeAreaPopup = () => {
  areaPopup.value?.close()
}

// 判断区域是否已选择
const isAreaSelected = (areaId) => {
  return selectedAreas.value.some(area => area.areaId === areaId)
}

// 切换区域选择
const toggleArea = (area) => {
  const index = selectedAreas.value.findIndex(a => a.areaId === area.areaId)
  if (index > -1) {
    selectedAreas.value.splice(index, 1)
  } else {
    selectedAreas.value.push(area)
  }
}

// 移除区域
const removeArea = (index) => {
  selectedAreas.value.splice(index, 1)
}

// 确认区域选择
const confirmAreaSelection = () => {
  closeAreaPopup()
}

// 开始时间变更
const onStartTimeChange = (e) => {
  startTime.value = e.detail.value
}

// 结束时间变更
const onEndTimeChange = (e) => {
  endTime.value = e.detail.value
}

// 时段限制开关变更
const onLimitTimeChange = (e) => {
  limitTimeRange.value = e.detail.value
}

// 开始小时变更
const onStartHourChange = (e) => {
  startHour.value = e.detail.value
}

// 结束小时变更
const onEndHourChange = (e) => {
  endHour.value = e.detail.value
}

// 访问日期变更
const onVisitDateChange = (e) => {
  visitDate.value = e.detail.value
}

// 访问开始时间变更
const onVisitStartTimeChange = (e) => {
  visitStartTime.value = e.detail.value
}

// 访问结束时间变更
const onVisitEndTimeChange = (e) => {
  visitEndTime.value = e.detail.value
}

// 加班日期变更
const onOvertimeDateChange = (e) => {
  overtimeDate.value = e.detail.value
}

// 加班开始时间变更
const onOvertimeStartTimeChange = (e) => {
  overtimeStartTime.value = e.detail.value
}

// 加班结束时间变更
const onOvertimeEndTimeChange = (e) => {
  overtimeEndTime.value = e.detail.value
}

// 请假类型变更
const onLeaveTypeChange = (e) => {
  const index = e.detail.value
  selectedLeaveType.value = leaveTypes[index]
}

// 请假开始日期变更
const onLeaveStartDateChange = (e) => {
  leaveStartDate.value = e.detail.value
}

// 请假结束日期变更
const onLeaveEndDateChange = (e) => {
  leaveEndDate.value = e.detail.value
}

// 选择紧急程度
const selectUrgency = (urgency) => {
  selectedUrgency.value = urgency
}

// 获取文件图标
const getFileIcon = (type) => {
  const iconMap = {
    pdf: 'file-pdf',
    excel: 'file-excel',
    word: 'file-word',
    image: 'image',
  }
  return iconMap[type] || 'paperclip'
}

// 选择附件
const chooseAttachment = () => {
  uni.chooseImage({
    count: 9 - attachments.value.length,
    success: (res) => {
      res.tempFilePaths.forEach((filePath, index) => {
        attachments.value.push({
          name: `附件${attachments.value.length + 1}.jpg`,
          type: 'image',
          path: filePath,
        })
      })
    },
  })
}

// 移除附件
const removeAttachment = (index) => {
  attachments.value.splice(index, 1)
}

// 提交申请
const submitApplication = () => {
  // 验证表单
  if (!validateForm()) {
    return
  }

  uni.showLoading({ title: '提交中...' })

  // TODO: 调用API提交申请
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '提交成功',
      icon: 'success',
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1000)
}

// 验证表单
const validateForm = () => {
  // 验证申请事由
  if (!applyReason.value.trim()) {
    uni.showToast({
      title: '请填写申请事由',
      icon: 'none',
    })
    return false
  }

  // 根据类型验证特定字段
  if (selectedType.value === 'access') {
    if (selectedAreas.value.length === 0) {
      uni.showToast({
        title: '请选择访问区域',
        icon: 'none',
      })
      return false
    }
    if (!startTime.value || !endTime.value) {
      uni.showToast({
        title: '请选择生效时间',
        icon: 'none',
      })
      return false
    }
  }

  if (selectedType.value === 'visitor') {
    if (!visitorForm.name) {
      uni.showToast({
        title: '请输入访客姓名',
        icon: 'none',
      })
      return false
    }
    if (!visitorForm.phone) {
      uni.showToast({
        title: '请输入手机号码',
        icon: 'none',
      })
      return false
    }
    if (!visitDate.value) {
      uni.showToast({
        title: '请选择访问日期',
        icon: 'none',
      })
      return false
    }
  }

  if (selectedType.value === 'overtime') {
    if (!overtimeDate.value) {
      uni.showToast({
        title: '请选择加班日期',
        icon: 'none',
      })
      return false
    }
  }

  if (selectedType.value === 'leave') {
    if (!selectedLeaveType.value) {
      uni.showToast({
        title: '请选择请假类型',
        icon: 'none',
      })
      return false
    }
    if (!leaveStartDate.value || !leaveEndDate.value) {
      uni.showToast({
        title: '请选择请假日期',
        icon: 'none',
      })
      return false
    }
  }

  return true
}

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e4e8ec 100%);
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
      gap: 8rpx;

      .back-text {
        font-size: 28rpx;
        color: #fff;
      }
    }

    .navbar-title {
      flex: 1;
      text-align: center;

      .title-text {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .navbar-right {
      width: 100rpx;
      display: flex;
      justify-content: flex-end;

      .draft-btn {
        font-size: 28rpx;
        color: #fff;
      }
    }
  }
}

.page-content {
  padding-top: calc(var(--status-bar-height) + 44px);
  padding-bottom: 140rpx;
  min-height: 100vh;
}

.section-card {
  background: #fff;
  margin: 20rpx 30rpx;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 30rpx;

    .section-title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .required {
      font-size: 28rpx;
      color: #ef4444;
      font-weight: 600;
    }

    .attachment-count {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .type-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    padding: 30rpx 20rpx;
    border-radius: 16rpx;
    border: 2rpx solid #e5e7eb;
    transition: all 0.3s;

    &.active {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
    }

    .type-icon {
      width: 88rpx;
      height: 88rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.access {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.visitor {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      }

      &.overtime {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.leave {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
      }
    }

    .type-name {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }

    .type-desc {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.form-section {
  // 表单特定样式
}

.area-selector {
  .selector-trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx;
    background: #f5f7fa;
    border-radius: 12rpx;

    .placeholder {
      font-size: 28rpx;
      color: #999;
    }

    .selected-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;
      flex: 1;
      margin-right: 16rpx;

      .tag-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 16rpx;
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #667eea;
        border: 1rpx solid rgba(102, 126, 234, 0.3);

        .tag-close {
          display: flex;
          align-items: center;
        }
      }
    }
  }
}

.time-range {
  display: flex;
  align-items: center;
  gap: 20rpx;

  .time-field {
    flex: 1;

    .field-label {
      display: block;
      font-size: 26rpx;
      color: #999;
      margin-bottom: 16rpx;
    }

    .picker-input {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
    }
  }

  .time-separator {
    font-size: 28rpx;
    color: #999;
    padding-top: 40rpx;
  }
}

.time-setting {
  .setting-switch {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .switch-label {
      font-size: 28rpx;
      color: #333;
    }
  }

  .time-range-inputs {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .time-input {
      flex: 1;
      padding: 20rpx 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .time-separator {
      font-size: 28rpx;
      color: #999;
    }
  }
}

.form-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  .form-item {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .form-label {
      font-size: 28rpx;
      color: #333;
      min-width: 140rpx;
    }

    .form-input {
      flex: 1;
      padding: 20rpx 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
      text-align: right;
    }

    .form-value {
      font-size: 28rpx;
      color: #667eea;
      font-weight: 600;
    }

    .picker-input-full {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
    }
  }
}

.visit-time {
  display: flex;
  flex-direction: column;
  gap: 20rpx;

  .time-field {
    &.full {
      .field-label {
        margin-bottom: 16rpx;
      }
    }

    .field-label {
      display: block;
      font-size: 26rpx;
      color: #999;
      margin-bottom: 8rpx;
    }

    .picker-input {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
    }
  }
}

.reason-input,
.remark-input {
  position: relative;

  textarea {
    width: 100%;
    min-height: 200rpx;
    padding: 20rpx;
    background: #f5f7fa;
    border-radius: 12rpx;
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
  }

  .input-count {
    position: absolute;
    bottom: 16rpx;
    right: 20rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .attachment-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 16rpx 20rpx;
    background: #f5f7fa;
    border-radius: 12rpx;

    .file-info {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .file-name {
        font-size: 26rpx;
        color: #333;
        max-width: 300rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .file-remove {
      display: flex;
      align-items: center;
    }
  }

  .upload-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    width: 160rpx;
    height: 160rpx;
    border: 2rpx dashed #d1d5db;
    border-radius: 12rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.urgency-options {
  display: flex;
  gap: 20rpx;

  .urgency-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 24rpx;
    border-radius: 12rpx;
    border: 2rpx solid #e5e7eb;
    font-size: 26rpx;
    color: #666;
    transition: all 0.3s;

    &.active {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
      color: #667eea;
    }

    .urgency-dot {
      width: 24rpx;
      height: 24rpx;
      border-radius: 50%;

      &.normal {
        background: #667eea;
      }

      &.urgent {
        background: #f59e0b;
      }

      &.very-urgent {
        background: #ef4444;
      }
    }
  }
}

.bottom-spacer {
  height: 40rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0f0f0;

  .submit-btn {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16rpx;
    font-size: 30rpx;
    font-weight: 600;
    color: #fff;
  }
}

// 区域选择弹窗
.area-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  height: 70vh;

  .popup-header {
    position: relative;
    text-align: center;
    margin-bottom: 30rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    height: calc(70vh - 160rpx);

    .area-list {
      display: flex;
      flex-direction: column;
      gap: 16rpx;

      .area-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx;
        background: #f5f7fa;
        border-radius: 12rpx;
        border: 2rpx solid transparent;
        transition: all 0.3s;

        &.selected {
          border-color: #667eea;
          background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
        }

        .area-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          gap: 8rpx;

          .area-name {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
          }

          .area-path {
            font-size: 24rpx;
            color: #999;
          }
        }

        .area-check {
          display: flex;
          align-items: center;
        }
      }
    }
  }

  .popup-footer {
    margin-top: 30rpx;

    .footer-btn {
      width: 100%;
      height: 88rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 16rpx;
      font-size: 30rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}
</style>
