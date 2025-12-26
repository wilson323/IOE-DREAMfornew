<template>
  <view class="permission-apply-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">权限申请</view>
      <view class="nav-right">
        <text class="submit-btn" @click="showPreview">预览</text>
      </view>
    </view>

    <!-- 表单内容 -->
    <view class="form-container">
      <!-- 申请类型 -->
      <view class="form-section">
        <view class="section-title">申请类型</view>

        <view class="type-grid">
          <view
            v-for="(type, index) in applyTypes"
            :key="index"
            :class="['type-card', { active: formData.applyType === type.value }]"
            @click="selectApplyType(type.value)"
          >
            <view class="type-icon">
              <uni-icons :type="type.icon" size="28" :color="formData.applyType === type.value ? '#1890ff' : '#666'"></uni-icons>
            </view>
            <view class="type-label">{{ type.label }}</view>
            <view class="type-desc">{{ type.desc }}</view>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <!-- 访问区域 -->
        <view class="form-item">
          <view class="form-label required">访问区域</view>
          <picker mode="selector" :range="areaList" range-key="areaName" :value="areaIndex" @change="onAreaChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.areaId }]">
                {{ getAreaLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 申请理由 -->
        <view class="form-item">
          <view class="form-label required">申请理由</view>
          <textarea
            class="form-textarea"
            placeholder="请详细说明申请理由（必填）"
            v-model="formData.applyReason"
            maxlength="500"
          />
          <view class="form-count">{{ formData.applyReason.length }}/500</view>
        </view>
      </view>

      <!-- 时间设置 -->
      <view class="form-section">
        <view class="section-title">时间设置</view>

        <!-- 权限类型 -->
        <view v-if="formData.applyType === 1" class="form-item">
          <view class="form-label required">权限类型</view>
          <picker mode="selector" :range="permissionTypes" range-key="label" :value="permissionTypeIndex" @change="onPermissionTypeChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.permissionType }]">
                {{ getPermissionTypeLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 有效期 -->
        <view class="form-item">
          <view class="form-label required">有效期</view>
          <picker mode="selector" :range="expiryTypes" range-key="label" :value="expiryIndex" @change="onExpiryChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.expiryType }]">
                {{ getExpiryLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 开始时间 -->
        <view v-if="formData.expiryType === 'custom'" class="form-item">
          <view class="form-label required">开始时间</view>
          <picker mode="time" :value="formData.startTime" @change="onStartTimeChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.startTime }]">
                {{ formData.startTime || '请选择开始时间' }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 结束时间 -->
        <view v-if="formData.expiryType === 'custom'" class="form-item">
          <view class="form-label required">结束时间</view>
          <picker mode="time" :value="formData.endTime" @change="onEndTimeChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.endTime }]">
                {{ formData.endTime || '请选择结束时间' }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 有效天数 -->
        <view v-if="formData.expiryType === 'days'" class="form-item">
          <view class="form-label required">有效天数</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入有效天数"
            v-model="formData.validDays"
            maxlength="3"
          />
        </view>

        <!-- 时间段设置（临时权限） -->
        <view v-if="formData.permissionType === 2" class="form-item">
          <view class="form-label">允许时间段</view>
          <view class="time-slots">
            <view
              v-for="(slot, index) in timeSlots"
              :key="index"
              :class="['slot-chip', { active: formData.timeSlots.includes(slot.value) }]"
              @click="toggleTimeSlot(slot.value)"
            >
              <text class="slot-text">{{ slot.label }}</text>
            </view>
          </view>
        </view>

        <!-- 星期设置（临时权限） -->
        <view v-if="formData.permissionType === 2" class="form-item">
          <view class="form-label">允许星期</view>
          <view class="weekdays">
            <view
              v-for="(day, index) in weekdays"
              :key="index"
              :class="['day-chip', { active: formData.weekdays.includes(day.value) }]"
              @click="toggleWeekday(day.value)"
            >
              <text class="day-text">{{ day.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 访客信息（访客预约） -->
      <view v-if="formData.applyType === 2" class="form-section">
        <view class="section-title">访客信息</view>

        <!-- 访客姓名 -->
        <view class="form-item">
          <view class="form-label required">访客姓名</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入访客姓名"
            v-model="formData.visitorName"
            maxlength="50"
          />
        </view>

        <!-- 访客手机 -->
        <view class="form-item">
          <view class="form-label required">访客手机</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入访客手机号"
            v-model="formData.visitorPhone"
            maxlength="11"
          />
        </view>

        <!-- 访客单位 -->
        <view class="form-item">
          <view class="form-label">访客单位</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入访客单位（可选）"
            v-model="formData.visitorCompany"
            maxlength="100"
          />
        </view>

        <!-- 访客数量 -->
        <view class="form-item">
          <view class="form-label required">同行人数</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入同行人数（包含本人）"
            v-model="formData.visitorCount"
            maxlength="2"
          />
        </view>

        <!-- 预约时间 -->
        <view class="form-item">
          <view class="form-label required">预约时间</view>
          <picker mode="datetime" :value="formData.appointmentTime" @change="onAppointmentTimeChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.appointmentTime }]">
                {{ formData.appointmentTime ? formatDateTime(formData.appointmentTime) : '请选择预约时间' }}
              </text>
              <uni-icons type="calendar" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>
      </view>

      <!-- 紧急联系人（紧急权限） -->
      <view v-if="formData.applyType === 3" class="form-section">
        <view class="section-title">紧急信息</view>

        <!-- 紧急程度 -->
        <view class="form-item">
          <view class="form-label required">紧急程度</view>
          <picker mode="selector" :range="urgencyLevels" range-key="label" :value="urgencyIndex" @change="onUrgencyChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.urgencyLevel }]">
                {{ getUrgencyLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 紧急联系人 -->
        <view class="form-item">
          <view class="form-label required">紧急联系人</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入紧急联系人姓名"
            v-model="formData.emergencyContact"
            maxlength="50"
          />
        </view>

        <!-- 联系电话 -->
        <view class="form-item">
          <view class="form-label required">联系电话</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入联系电话"
            v-model="formData.emergencyPhone"
            maxlength="11"
          />
        </view>
      </view>

      <!-- 审批流程预览 -->
      <view class="form-section">
        <view class="section-title">审批流程</view>

        <view class="approval-flow">
          <view v-for="(step, index) in approvalSteps" :key="index" class="flow-step">
            <view class="step-line" v-if="index > 0"></view>
            <view class="step-icon">
              <uni-icons :type="step.icon" size="20" color="#1890ff"></uni-icons>
            </view>
            <view class="step-content">
              <view class="step-title">{{ step.title }}</view>
              <view class="step-desc">{{ step.desc }}</view>
              <view v-if="step.approver" class="step-approver">审批人：{{ step.approver }}</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-section">
        <view class="section-title">备注信息</view>

        <view class="form-item">
          <view class="form-label">备注</view>
          <textarea
            class="form-textarea"
            placeholder="请输入备注信息（可选）"
            v-model="formData.remark"
            maxlength="200"
          />
          <view class="form-count">{{ formData.remark.length }}/200</view>
        </view>
      </view>
    </view>

    <!-- 底部提交栏 -->
    <view class="submit-bar">
      <button class="submit-btn-cancel" @click="saveDraft">保存草稿</button>
      <button class="submit-btn-primary" @click="submitApply">提交申请</button>
    </view>

    <!-- 预览弹窗 -->
    <uni-popup ref="previewPopup" type="center">
      <view class="preview-popup">
        <view class="popup-header">
          <view class="popup-title">申请预览</view>
          <view class="popup-close" @click="closePreview">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <view class="preview-section">
            <view class="preview-label">申请类型</view>
            <view class="preview-value">{{ getApplyTypeText() }}</view>
          </view>

          <view class="preview-section">
            <view class="preview-label">访问区域</view>
            <view class="preview-value">{{ getSelectedAreaName() }}</view>
          </view>

          <view class="preview-section">
            <view class="preview-label">申请理由</view>
            <view class="preview-value">{{ formData.applyReason || '无' }}</view>
          </view>

          <view v-if="formData.permissionType" class="preview-section">
            <view class="preview-label">权限类型</view>
            <view class="preview-value">{{ getPermissionTypeText() }}</view>
          </view>

          <view class="preview-section">
            <view class="preview-label">有效期</view>
            <view class="preview-value">{{ getExpiryText() }}</view>
          </view>

          <view v-if="formData.applyType === 2">
            <view class="preview-section">
              <view class="preview-label">访客姓名</view>
              <view class="preview-value">{{ formData.visitorName || '无' }}</view>
            </view>

            <view class="preview-section">
              <view class="preview-label">访客手机</view>
              <view class="preview-value">{{ formData.visitorPhone || '无' }}</view>
            </view>

            <view class="preview-section">
              <view class="preview-label">同行人数</view>
              <view class="preview-value">{{ formData.visitorCount || 1 }}人</view>
            </view>
          </view>

          <view v-if="formData.applyType === 3">
            <view class="preview-section">
              <view class="preview-label">紧急程度</view>
              <view class="preview-value">{{ getUrgencyText() }}</view>
            </view>

            <view class="preview-section">
              <view class="preview-label">紧急联系人</view>
              <view class="preview-value">{{ formData.emergencyContact || '无' }}</view>
            </view>
          </view>

          <view v-if="formData.remark" class="preview-section">
            <view class="preview-label">备注</view>
            <view class="preview-value">{{ formData.remark }}</view>
          </view>
        </scroll-view>

        <view class="popup-footer">
          <button class="popup-btn cancel-btn" @click="closePreview">返回修改</button>
          <button class="popup-btn confirm-btn" @click="confirmSubmit">确认提交</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 申请类型
const applyTypes = [
  { value: 1, label: '临时权限', desc: '临时访问权限', icon: 'locked' },
  { value: 2, label: '访客预约', desc: '访客预约访问', icon: 'person' },
  { value: 3, label: '紧急权限', desc: '紧急情况申请', icon: 'fire' }
]

// 权限类型
const permissionTypes = [
  { value: 1, label: '永久权限' },
  { value: 2, label: '临时权限' },
  { value: 3, label: '时间段权限' }
]

// 有效期类型
const expiryTypes = [
  { value: 'hours', label: '按小时' },
  { value: 'days', label: '按天数' },
  { value: 'custom', label: '自定义' }
]

// 时间段选项
const timeSlots = [
  { label: '全天', value: 'all' },
  { label: '上午', value: 'morning' },
  { label: '下午', value: 'afternoon' },
  { label: '晚上', value: 'evening' }
]

// 星期选项
const weekdays = [
  { label: '一', value: 1 },
  { label: '二', value: 2 },
  { label: '三', value: 3 },
  { label: '四', value: 4 },
  { label: '五', value: 5 },
  { label: '六', value: 6 },
  { label: '日', value: 7 }
]

// 紧急程度
const urgencyLevels = [
  { value: 1, label: '一般' },
  { value: 2, label: '紧急' },
  { value: 3, label: '非常紧急' }
]

// 表单数据
const formData = ref({
  applyType: 1,              // 申请类型
  areaId: null,              // 区域ID
  applyReason: '',           // 申请理由
  permissionType: null,      // 权限类型
  expiryType: 'hours',       // 有效期类型
  startTime: '',             // 开始时间
  endTime: '',               // 结束时间
  validDays: 1,              // 有效天数
  timeSlots: [],             // 时间段
  weekdays: [],              // 星期
  visitorName: '',           // 访客姓名
  visitorPhone: '',          // 访客手机
  visitorCompany: '',        // 访客单位
  visitorCount: 1,           // 访客数量
  appointmentTime: '',       // 预约时间
  urgencyLevel: null,        // 紧急程度
  emergencyContact: '',      // 紧急联系人
  emergencyPhone: '',        // 紧急电话
  remark: ''                 // 备注
})

// 区域列表
const areaList = ref([])
const areaIndex = ref(-1)

// 权限类型索引
const permissionTypeIndex = ref(-1)

// 有效期索引
const expiryIndex = ref(0)

// 紧急程度索引
const urgencyIndex = ref(-1)

// 审批流程
const approvalSteps = ref([
  { title: '提交申请', desc: '提交权限申请', icon: 'compose', approver: null },
  { title: '部门审批', desc: '等待部门主管审批', icon: 'person', approver: '张主管' },
  { title: '安全审批', desc: '等待安全管理员审批', icon: 'locked', approver: '李安全' },
  { title: '审批完成', desc: '审批通过后生效', icon: 'checkmarkempty', approver: null }
])

// 页面生命周期
onLoad((options) => {
  // 如果有areaId参数，预选区域
  if (options.areaId) {
    formData.value.areaId = parseInt(options.areaId)
  }

  loadAreaList()
})

/**
 * 加载区域列表
 */
const loadAreaList = async () => {
  try {
    const result = await accessApi.getAreaList({
      pageNum: 1,
      pageSize: 1000,
      enabled: 1
    })

    if (result.success && result.data) {
      areaList.value = result.data.list || []

      // 如果有预选区域，设置索引
      if (formData.value.areaId) {
        const index = areaList.value.findIndex(a => a.areaId === formData.value.areaId)
        if (index >= 0) {
          areaIndex.value = index
        }
      }
    }
  } catch (error) {
    console.error('加载区域列表失败:', error)
  }
}

/**
 * 选择申请类型
 */
const selectApplyType = (value) => {
  formData.value.applyType = value
}

/**
 * 区域变更
 */
const onAreaChange = (e) => {
  const index = e.detail.value
  areaIndex.value = index
  formData.value.areaId = areaList.value[index].areaId
}

/**
 * 权限类型变更
 */
const onPermissionTypeChange = (e) => {
  const index = e.detail.value
  permissionTypeIndex.value = index
  formData.value.permissionType = permissionTypes[index].value
}

/**
 * 有效期变更
 */
const onExpiryChange = (e) => {
  const index = e.detail.value
  expiryIndex.value = index
  formData.value.expiryType = expiryTypes[index].value
}

/**
 * 开始时间变更
 */
const onStartTimeChange = (e) => {
  formData.value.startTime = e.detail.value
}

/**
 * 结束时间变更
 */
const onEndTimeChange = (e) => {
  formData.value.endTime = e.detail.value
}

/**
 * 预约时间变更
 */
const onAppointmentTimeChange = (e) => {
  formData.value.appointmentTime = e.detail.value
}

/**
 * 紧急程度变更
 */
const onUrgencyChange = (e) => {
  const index = e.detail.value
  urgencyIndex.value = index
  formData.value.urgencyLevel = urgencyLevels[index].value
}

/**
 * 切换时间段
 */
const toggleTimeSlot = (value) => {
  const index = formData.value.timeSlots.indexOf(value)
  if (index >= 0) {
    formData.value.timeSlots.splice(index, 1)
  } else {
    formData.value.timeSlots.push(value)
  }
}

/**
 * 切换星期
 */
const toggleWeekday = (value) => {
  const index = formData.value.weekdays.indexOf(value)
  if (index >= 0) {
    formData.value.weekdays.splice(index, 1)
  } else {
    formData.value.weekdays.push(value)
  }
}

/**
 * 获取区域标签
 */
const getAreaLabel = () => {
  if (!formData.value.areaId) {
    return '请选择访问区域'
  }
  const area = areaList.value.find(a => a.areaId === formData.value.areaId)
  return area ? area.areaName : '请选择访问区域'
}

/**
 * 获取权限类型标签
 */
const getPermissionTypeLabel = () => {
  if (!formData.value.permissionType) {
    return '请选择权限类型'
  }
  const type = permissionTypes.find(t => t.value === formData.value.permissionType)
  return type ? type.label : '请选择权限类型'
}

/**
 * 获取有效期标签
 */
const getExpiryLabel = () => {
  const type = expiryTypes.find(t => t.value === formData.value.expiryType)
  return type ? type.label : '请选择有效期'
}

/**
 * 获取紧急程度标签
 */
const getUrgencyLabel = () => {
  if (!formData.value.urgencyLevel) {
    return '请选择紧急程度'
  }
  const level = urgencyLevels.find(l => l.value === formData.value.urgencyLevel)
  return level ? level.label : '请选择紧急程度'
}

/**
 * 获取申请类型文本
 */
const getApplyTypeText = () => {
  const type = applyTypes.find(t => t.value === formData.value.applyType)
  return type ? type.label : '未知'
}

/**
 * 获取选中的区域名称
 */
const getSelectedAreaName = () => {
  if (!formData.value.areaId) return '未选择'
  const area = areaList.value.find(a => a.areaId === formData.value.areaId)
  return area ? area.areaName : '未选择'
}

/**
 * 获取权限类型文本
 */
const getPermissionTypeText = () => {
  if (!formData.value.permissionType) return '未选择'
  const type = permissionTypes.find(t => t.value === formData.value.permissionType)
  return type ? type.label : '未选择'
}

/**
 * 获取有效期文本
 */
const getExpiryText = () => {
  if (formData.value.expiryType === 'hours') {
    return '按小时计算'
  } else if (formData.value.expiryType === 'days') {
    return `${formData.value.validDays}天`
  } else if (formData.value.expiryType === 'custom') {
    return `${formData.value.startTime} - ${formData.value.endTime}`
  }
  return '未设置'
}

/**
 * 获取紧急程度文本
 */
const getUrgencyText = () => {
  if (!formData.value.urgencyLevel) return '未选择'
  const level = urgencyLevels.find(l => l.value === formData.value.urgencyLevel)
  return level ? level.label : '未选择'
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ')
}

/**
 * 验证表单
 */
const validateForm = () => {
  // 申请类型
  if (!formData.value.applyType) {
    uni.showToast({ title: '请选择申请类型', icon: 'none' })
    return false
  }

  // 访问区域
  if (!formData.value.areaId) {
    uni.showToast({ title: '请选择访问区域', icon: 'none' })
    return false
  }

  // 申请理由
  if (!formData.value.applyReason || formData.value.applyReason.trim().length === 0) {
    uni.showToast({ title: '请输入申请理由', icon: 'none' })
    return false
  }

  // 权限类型（临时权限）
  if (formData.value.applyType === 1 && !formData.value.permissionType) {
    uni.showToast({ title: '请选择权限类型', icon: 'none' })
    return false
  }

  // 有效期
  if (!formData.value.expiryType) {
    uni.showToast({ title: '请选择有效期', icon: 'none' })
    return false
  }

  // 自定义时间
  if (formData.value.expiryType === 'custom') {
    if (!formData.value.startTime || !formData.value.endTime) {
      uni.showToast({ title: '请选择完整的时间范围', icon: 'none' })
      return false
    }
  }

  // 按天数
  if (formData.value.expiryType === 'days' && !formData.value.validDays) {
    uni.showToast({ title: '请输入有效天数', icon: 'none' })
    return false
  }

  // 访客信息
  if (formData.value.applyType === 2) {
    if (!formData.value.visitorName || formData.value.visitorName.trim().length === 0) {
      uni.showToast({ title: '请输入访客姓名', icon: 'none' })
      return false
    }

    if (!formData.value.visitorPhone || formData.value.visitorPhone.length !== 11) {
      uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return false
    }

    if (!formData.value.appointmentTime) {
      uni.showToast({ title: '请选择预约时间', icon: 'none' })
      return false
    }
  }

  // 紧急信息
  if (formData.value.applyType === 3) {
    if (!formData.value.urgencyLevel) {
      uni.showToast({ title: '请选择紧急程度', icon: 'none' })
      return false
    }

    if (!formData.value.emergencyContact || formData.value.emergencyContact.trim().length === 0) {
      uni.showToast({ title: '请输入紧急联系人', icon: 'none' })
      return false
    }

    if (!formData.value.emergencyPhone || formData.value.emergencyPhone.length !== 11) {
      uni.showToast({ title: '请输入正确的联系电话', icon: 'none' })
      return false
    }
  }

  return true
}

/**
 * 显示预览
 */
const showPreview = () => {
  if (!validateForm()) {
    return
  }

  $refs.previewPopup.open()
}

/**
 * 关闭预览
 */
const closePreview = () => {
  $refs.previewPopup.close()
}

/**
 * 保存草稿
 */
const saveDraft = async () => {
  uni.showLoading({ title: '保存中...', mask: true })

  try {
    // TODO: 调用保存草稿API
    await new Promise(resolve => setTimeout(resolve, 500))

    uni.hideLoading()
    uni.showToast({ title: '草稿已保存', icon: 'success' })
  } catch (error) {
    uni.hideLoading()
    console.error('保存草稿失败:', error)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

/**
 * 提交申请
 */
const submitApply = () => {
  if (!validateForm()) {
    return
  }

  showPreview()
}

/**
 * 确认提交
 */
const confirmSubmit = async () => {
  closePreview()

  uni.showLoading({ title: '提交中...', mask: true })

  try {
    const result = await accessApi.createPermissionApply(formData.value)

    uni.hideLoading()

    if (result.success) {
      uni.showToast({ title: '提交成功', icon: 'success' })

      setTimeout(() => {
        uni.navigateBack()
      }, 500)
    } else {
      uni.showToast({ title: result.message || '提交失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('提交申请失败:', error)
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.permission-apply-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
}

// 导航栏
.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;

  .nav-left,
  .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
    flex: 1;
    text-align: center;
  }

  .submit-btn {
    font-size: 15px;
    color: #1890ff;
    font-weight: 500;
  }
}

// 表单容器
.form-container {
  padding: 15px;
}

// 表单区块
.form-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;
  }
}

// 申请类型网格
.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;

  .type-card {
    padding: 15px 10px;
    background-color: #f5f5f5;
    border-radius: 12px;
    text-align: center;
    border: 2px solid transparent;

    &.active {
      background-color: #e6f7ff;
      border-color: #1890ff;
    }

    .type-icon {
      margin-bottom: 8px;
    }

    .type-label {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 4px;
    }

    .type-desc {
      font-size: 12px;
      color: #999;
    }
  }
}

// 表单项
.form-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  position: relative;

  &:last-child {
    border-bottom: none;
  }

  .form-label {
    font-size: 15px;
    color: #333;
    margin-bottom: 12px;
    font-weight: 500;

    &.required::before {
      content: '*';
      color: #ff4d4f;
      margin-right: 4px;
    }
  }

  .form-input {
    width: 100%;
    height: 40px;
    padding: 0 12px;
    font-size: 15px;
    color: #333;
    background-color: #f5f5f5;
    border-radius: 8px;
    box-sizing: border-box;

    &::placeholder {
      color: #999;
    }
  }

  .form-picker {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 40px;
    padding: 0 12px;
    background-color: #f5f5f5;
    border-radius: 8px;

    .picker-text {
      font-size: 15px;
      color: #333;

      &.placeholder {
        color: #999;
      }
    }
  }

  .form-textarea {
    width: 100%;
    min-height: 80px;
    padding: 10px 12px;
    font-size: 15px;
    color: #333;
    background-color: #f5f5f5;
    border-radius: 8px;
    box-sizing: border-box;

    &::placeholder {
      color: #999;
    }
  }

  .form-count {
    position: absolute;
    bottom: 12px;
    right: 15px;
    font-size: 12px;
    color: #999;
  }

  // 时间段选择
  .time-slots {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .slot-chip {
      padding: 6px 16px;
      background-color: #f5f5f5;
      border-radius: 16px;
      font-size: 14px;
      color: #666;

      &.active {
        background-color: #e6f7ff;
        color: #1890ff;
      }

      .slot-text {
        font-size: 14px;
      }
    }
  }

  // 星期选择
  .weekdays {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .day-chip {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 50%;
      font-size: 14px;
      color: #666;

      &.active {
        background-color: #1890ff;
        color: #fff;
      }

      .day-text {
        font-size: 14px;
      }
    }
  }
}

// 审批流程
.approval-flow {
  padding: 15px 0;

  .flow-step {
    display: flex;
    position: relative;

    &:not(:last-child) {
      padding-bottom: 20px;
    }

    .step-line {
      position: absolute;
      left: 10px;
      top: 30px;
      bottom: -10px;
      width: 2px;
      background-color: #e6f7ff;
      z-index: 0;
    }

    .step-icon {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background-color: #e6f7ff;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      flex-shrink: 0;
      z-index: 1;
    }

    .step-content {
      flex: 1;
      padding-top: 2px;

      .step-title {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }

      .step-desc {
        font-size: 13px;
        color: #666;
        margin-bottom: 4px;
      }

      .step-approver {
        font-size: 12px;
        color: #1890ff;
      }
    }
  }
}

// 底部提交栏
.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 10px;
  padding: 10px 15px;
  background-color: #fff;
  border-top: 1px solid #eee;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));

  .submit-btn-cancel,
  .submit-btn-primary {
    flex: 1;
    height: 44px;
    line-height: 44px;
    border-radius: 22px;
    font-size: 16px;
    text-align: center;
    border: none;
  }

  .submit-btn-cancel {
    background-color: #f5f5f5;
    color: #666;
  }

  .submit-btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
}

// 预览弹窗
.preview-popup {
  width: 85vw;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 16px;
  overflow: hidden;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #eee;

    .popup-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    padding: 15px;
    max-height: 50vh;
    overflow-y: auto;

    .preview-section {
      margin-bottom: 15px;

      &:last-child {
        margin-bottom: 0;
      }

      .preview-label {
        font-size: 13px;
        color: #999;
        margin-bottom: 6px;
      }

      .preview-value {
        font-size: 15px;
        color: #333;
        line-height: 1.5;
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 10px;
    padding: 15px;
    border-top: 1px solid #eee;

    .popup-btn {
      flex: 1;
      height: 40px;
      line-height: 40px;
      border-radius: 8px;
      font-size: 15px;
      text-align: center;
      border: none;

      &.cancel-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm-btn {
        background-color: #1890ff;
        color: #fff;
      }
    }
  }
}
</style>
