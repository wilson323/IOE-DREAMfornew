<template>
  <view class="area-permission-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">权限管理</view>
      <view class="nav-right" @click="openAddUserModal">
        <uni-icons type="plus" size="20" color="#1890ff"></uni-icons>
      </view>
    </view>

    <!-- 区域信息 -->
    <view class="area-info-card">
      <view class="area-header">
        <uni-icons type="home" size="20" color="#667eea"></uni-icons>
        <text class="area-name">{{ areaInfo.areaName }}</text>
      </view>
      <text class="area-path">{{ areaInfo.areaPath || areaInfo.areaName }}</text>
    </view>

    <!-- 操作栏 -->
    <view class="action-bar">
      <view class="search-box" @click="openSearchModal">
        <uni-icons type="search" size="16" color="#999"></uni-icons>
        <text class="search-placeholder">搜索用户</text>
      </view>
      <view class="filter-btn" @click="openFilterModal">
        <uni-icons type="filter" size="18" color="#666"></uni-icons>
        <text class="filter-text">筛选</text>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view class="batch-bar" v-if="selectedUsers.length > 0">
      <text class="batch-count">已选择{{ selectedUsers.length }}人</text>
      <view class="batch-actions">
        <button class="batch-btn danger" @click="batchRemovePermissions">移除权限</button>
        <button class="batch-btn" @click="clearSelection">取消</button>
      </view>
    </view>

    <!-- 权限用户列表 -->
    <view class="user-list">
      <view
        class="user-item"
        v-for="user in filteredPermissionList"
        :key="user.permissionId"
        @click="toggleSelectUser(user)"
      >
        <view class="user-left">
          <uni-icons
            :type="selectedUsers.includes(user.permissionId) ? 'checkbox-filled' : 'circle'"
            :size="selectedUsers.includes(user.permissionId) ? '20' : '18'"
            :color="selectedUsers.includes(user.permissionId) ? '#1890ff' : '#d9d9d9'"
          ></uni-icons>
          <image class="user-avatar" :src="user.avatar || '/static/images/default-avatar.png'" mode="aspectFill"></image>
          <view class="user-info">
            <text class="user-name">{{ user.userName }}</text>
            <view class="user-meta">
              <text class="user-dept">{{ user.departmentName || '未分配' }}</text>
              <text class="user-divider">|</text>
              <text class="user-position">{{ user.positionName || '未设置' }}</text>
            </view>
          </view>
        </view>
        <view class="user-right" @click.stop="showPermissionMenu(user)">
          <view class="permission-type" :class="getPermissionTypeClass(user.permissionType)">
            {{ getPermissionTypeLabel(user.permissionType) }}
          </view>
          <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredPermissionList.length === 0 && !loading">
        <image class="empty-icon" src="/static/images/empty.png" mode="aspectFit"></image>
        <text class="empty-text">暂无授权用户</text>
        <button class="empty-btn" @click="openAddUserModal">添加授权</button>
      </view>
    </view>

    <!-- 搜索弹窗 -->
    <uni-popup ref="searchPopup" type="top">
      <view class="search-modal">
        <view class="search-input-box">
          <uni-icons type="search" size="18" color="#999"></uni-icons>
          <input
            class="search-input"
            type="text"
            placeholder="搜索用户名称或工号"
            v-model="searchKeyword"
            @input="onSearchInput"
            @confirm="doSearch"
          />
          <text class="search-clear" v-if="searchKeyword" @click="clearSearch">清除</text>
        </view>
        <view class="search-actions">
          <text class="search-cancel" @click="closeSearchModal">取消</text>
        </view>
      </view>
    </uni-popup>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom">
      <view class="filter-container">
        <view class="filter-header">
          <text class="filter-title">筛选条件</text>
          <view class="filter-close" @click="closeFilterModal">
            <uni-icons type="close" size="18" color="#999"></uni-icons>
          </view>
        </view>
        <view class="filter-content">
          <!-- 权限类型 -->
          <view class="filter-section">
            <view class="filter-label">权限类型</view>
            <view class="filter-tags">
              <view
                class="filter-tag"
                :class="{ active: filterType === '' }"
                @click="selectFilterType('')"
              >
                全部
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterType === 'permanent' }"
                @click="selectFilterType('permanent')"
              >
                永久
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterType === 'temporary' }"
                @click="selectFilterType('temporary')"
              >
                临时
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterType === 'time' }"
                @click="selectFilterType('time')"
              >
                时间段
              </view>
            </view>
          </view>

          <!-- 状态筛选 -->
          <view class="filter-section">
            <view class="filter-label">有效期</view>
            <view class="filter-tags">
              <view
                class="filter-tag"
                :class="{ active: filterExpiry === '' }"
                @click="selectFilterExpiry('')"
              >
                全部
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterExpiry === 'valid' }"
                @click="selectFilterExpiry('valid')"
              >
                有效期内
              </view>
              <view
                class="filter-tag"
                :class="{ active: filterExpiry === 'expired' }"
                @click="selectFilterExpiry('expired')"
              >
                已过期
              </view>
            </view>
          </view>
        </view>
        <view class="filter-footer">
          <button class="filter-btn reset" @click="resetFilter">重置</button>
          <button class="filter-btn confirm" @click="applyFilter">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- 添加用户弹窗 -->
    <uni-popup ref="addUserPopup" type="bottom">
      <view class="add-user-container">
        <view class="popup-header">
          <text class="popup-title">添加授权用户</text>
          <view class="popup-close" @click="closeAddUserModal">
            <uni-icons type="close" size="18" color="#999"></uni-icons>
          </view>
        </view>
        <scroll-view class="popup-content" scroll-y>
          <!-- 用户搜索 -->
          <view class="user-search">
            <view class="search-input-wrapper">
              <uni-icons type="search" size="16" color="#999"></uni-icons>
              <input
                class="search-input"
                type="text"
                placeholder="搜索用户名称或工号"
                v-model="addUserKeyword"
                @input="onAddUserSearch"
              />
            </view>
          </view>

          <!-- 用户列表 -->
          <view class="available-users">
            <view
              class="available-user-item"
              v-for="user in availableUsers"
              :key="user.userId"
              @click="toggleSelectUserToAdd(user)"
            >
              <view class="user-left">
                <uni-icons
                  :type="usersToAdd.includes(user.userId) ? 'checkbox-filled' : 'circle'"
                  :size="18"
                  :color="usersToAdd.includes(user.userId) ? '#1890ff' : '#d9d9d9'"
                ></uni-icons>
                <image class="user-avatar" :src="user.avatar || '/static/images/default-avatar.png'" mode="aspectFill"></image>
                <view class="user-info">
                  <text class="user-name">{{ user.userName }}</text>
                  <text class="user-workno">{{ user.workNo || user.loginName }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="popup-footer">
          <button class="popup-btn cancel" @click="closeAddUserModal">取消</button>
          <button class="popup-btn confirm" @click="confirmAddUsers" :disabled="usersToAdd.length === 0">
            确定({{ usersToAdd.length }})
          </button>
        </view>
      </view>
    </uni-popup>

    <!-- 权限设置弹窗 -->
    <uni-popup ref="permissionSettingPopup" type="bottom">
      <view class="permission-setting-container">
        <view class="popup-header">
          <text class="popup-title">设置权限</text>
          <view class="popup-close" @click="closePermissionSetting">
            <uni-icons type="close" size="18" color="#999"></uni-icons>
          </view>
        </view>
        <scroll-view class="popup-content" scroll-y>
          <view class="setting-form">
            <!-- 权限类型 -->
            <view class="form-item">
              <view class="form-label required">权限类型</view>
              <picker
                mode="selector"
                :range="permissionTypes"
                range-key="label"
                :value="permissionTypeIndex"
                @change="onPermissionTypeChange"
              >
                <view class="form-picker">
                  <text :class="['picker-text', { placeholder: !currentPermission.type }]">
                    {{ getPermissionTypeLabel(currentPermission.type) }}
                  </text>
                  <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
                </view>
              </picker>
            </view>

            <!-- 有效期设置 -->
            <view class="form-item" v-if="currentPermission.type === 'temporary'">
              <view class="form-label required">有效期至</view>
              <picker
                mode="date"
                :value="expiryDate"
                @change="onExpiryDateChange"
              >
                <view class="form-picker">
                  <text :class="['picker-text', { placeholder: !currentPermission.expiryDate }]">
                    {{ currentPermission.expiryDate || '请选择有效期' }}
                  </text>
                  <uni-icons type="calendar" size="16" color="#d9d9d9"></uni-icons>
                </view>
              </picker>
            </view>

            <!-- 时间段设置 -->
            <view class="form-item" v-if="currentPermission.type === 'time'">
              <view class="form-label">允许通行时间</view>
              <view class="time-slots">
                <view
                  class="time-slot"
                  v-for="(slot, index) in timeSlots"
                  :key="index"
                  :class="{ active: currentTimeSlot === index }"
                  @click="selectTimeSlot(index)"
                >
                  <text class="slot-label">{{ slot.label }}</text>
                  <text class="slot-time">{{ slot.time }}</text>
                </view>
              </view>
            </view>

            <!-- 周设置 -->
            <view class="form-item" v-if="currentPermission.type === 'time'">
              <view class="form-label">允许通行日期</view>
              <view class="weekdays">
                <view
                  class="weekday-item"
                  v-for="(day, index) in weekdays"
                  :key="index"
                  :class="{ active: selectedWeekdays.includes(index) }"
                  @click="toggleWeekday(index)"
                >
                  <text class="weekday-text">{{ day }}</text>
                </view>
              </view>
            </view>

            <!-- 备注 -->
            <view class="form-item">
              <view class="form-label">备注</view>
              <textarea
                class="form-textarea"
                placeholder="请输入备注信息（可选）"
                v-model="currentPermission.remark"
                maxlength="200"
              />
              <view class="form-count">{{ (currentPermission.remark || '').length }}/200</view>
            </view>
          </view>
        </scroll-view>
        <view class="popup-footer">
          <button class="popup-btn cancel" @click="closePermissionSetting">取消</button>
          <button class="popup-btn confirm" @click="savePermissionSetting">保存</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 区域信息
const areaInfo = ref({})
const areaId = ref(null)

// 权限用户列表
const permissionList = ref([])
const loading = ref(false)

// 搜索和筛选
const searchPopup = ref(null)
const filterPopup = ref(null)
const searchKeyword = ref('')
const filterType = ref('')
const filterExpiry = ref('')

// 批量选择
const selectedUsers = ref([])

// 添加用户相关
const addUserPopup = ref(null)
const addUserKeyword = ref('')
const availableUsers = ref([])
const usersToAdd = ref([])

// 权限设置
const permissionSettingPopup = ref(null)
const currentUser = ref(null)
const currentPermission = ref({
  type: 'permanent',
  expiryDate: '',
  timeSlots: [],
  weekdays: [],
  remark: ''
})

// 权限类型
const permissionTypes = [
  { value: 'permanent', label: '永久权限' },
  { value: 'temporary', label: '临时权限' },
  { value: 'time', label: '时间段权限' }
]
const permissionTypeIndex = ref(0)

// 时间段
const timeSlots = [
  { label: '全天', time: '00:00-24:00', value: 'all' },
  { label: '白天', time: '08:00-18:00', value: 'day' },
  { label: '夜间', time: '18:00-08:00', value: 'night' },
  { label: '上班时段', time: '09:00-18:00', value: 'work' }
]
const currentTimeSlot = ref(0)

// 星期
const weekdays = ['一', '二', '三', '四', '五', '六', '日']
const selectedWeekdays = ref([0, 1, 2, 3, 4, 5, 6])

// 过滤后的列表
const filteredPermissionList = computed(() => {
  let list = permissionList.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(user => {
      return user.userName && user.userName.toLowerCase().includes(keyword) ||
             user.workNo && user.workNo.toLowerCase().includes(keyword)
    })
  }

  // 类型过滤
  if (filterType.value) {
    list = list.filter(user => user.permissionType === filterType.value)
  }

  // 有效期过滤
  if (filterExpiry.value === 'valid') {
    const now = new Date()
    list = list.filter(user => !user.expiryDate || new Date(user.expiryDate) > now)
  } else if (filterExpiry.value === 'expired') {
    const now = new Date()
    list = list.filter(user => user.expiryDate && new Date(user.expiryDate) <= now)
  }

  return list
})

// 页面生命周期
onLoad((options) => {
  if (options.id) {
    areaId.value = parseInt(options.id)
    loadAreaDetail()
    loadPermissions()
  }
})

/**
 * 加载区域详情
 */
const loadAreaDetail = async () => {
  try {
    const result = await accessApi.getAreaDetail(areaId.value)
    if (result.success && result.data) {
      areaInfo.value = result.data.area || {}
    }
  } catch (error) {
    console.error('加载区域详情失败:', error)
  }
}

/**
 * 加载权限用户列表
 */
const loadPermissions = async () => {
  loading.value = true

  try {
    const result = await accessApi.getAreaPermissions(areaId.value, {
      pageNum: 1,
      pageSize: 1000
    })

    if (result.success && result.data) {
      permissionList.value = result.data.list || []
    }
  } catch (error) {
    console.error('加载权限列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 获取权限类型标签
 */
const getPermissionTypeLabel = (type) => {
  const types = {
    'permanent': '永久',
    'temporary': '临时',
    'time': '时间段'
  }
  return types[type] || '未知'
}

/**
 * 获取权限类型样式类
 */
const getPermissionTypeClass = (type) => {
  const classes = {
    'permanent': 'type-permanent',
    'temporary': 'type-temporary',
    'time': 'type-time'
  }
  return classes[type] || 'type-unknown'
}

/**
 * 选择用户
 */
const toggleSelectUser = (user) => {
  const index = selectedUsers.value.indexOf(user.permissionId)
  if (index >= 0) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(user.permissionId)
  }
}

/**
 * 清除选择
 */
const clearSelection = () => {
  selectedUsers.value = []
}

/**
 * 批量移除权限
 */
const batchRemovePermissions = () => {
  uni.showModal({
    title: '确认移除',
    content: `确定要移除这${selectedUsers.value.length}个用户的权限吗？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '处理中...',
          mask: true
        })

        try {
          const result = await accessApi.setAreaPermissions(areaId.value, {
            userIds: selectedUsers.value,
            action: 'remove'
          })

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '移除成功',
              icon: 'success'
            })

            // 刷新列表
            loadPermissions()
            selectedUsers.value = []
          } else {
            uni.showToast({
              title: result.message || '移除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('批量移除权限失败:', error)
          uni.showToast({
            title: '移除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 打开搜索弹窗
 */
const openSearchModal = () => {
  searchPopup.value.open()
}

/**
 * 关闭搜索弹窗
 */
const closeSearchModal = () => {
  searchPopup.value.close()
}

/**
 * 搜索输入
 */
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
}

/**
 * 执行搜索
 */
const doSearch = () => {
  closeSearchModal()
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
}

/**
 * 打开筛选弹窗
 */
const openFilterModal = () => {
  filterPopup.value.open()
}

/**
 * 关闭筛选弹窗
 */
const closeFilterModal = () => {
  filterPopup.value.close()
}

/**
 * 选择筛选类型
 */
const selectFilterType = (type) => {
  filterType.value = type
}

/**
 * 选择筛选有效期
 */
const selectFilterExpiry = (expiry) => {
  filterExpiry.value = expiry
}

/**
 * 应用筛选
 */
const applyFilter = () => {
  closeFilterModal()
}

/**
 * 重置筛选
 */
const resetFilter = () => {
  filterType.value = ''
  filterExpiry.value = ''
}

/**
 * 显示权限菜单
 */
const showPermissionMenu = (user) => {
  uni.showActionSheet({
    itemList: ['编辑权限', '移除权限'],
    success: (res) => {
      if (res.tapIndex === 0) {
        editPermission(user)
      } else if (res.tapIndex === 1) {
        removePermission(user)
      }
    }
  })
}

/**
 * 编辑权限
 */
const editPermission = (user) => {
  currentUser.value = user
  currentPermission.value = {
    type: user.permissionType || 'permanent',
    expiryDate: user.expiryDate || '',
    timeSlots: user.timeSlots || [],
    weekdays: user.weekdays || [],
    remark: user.remark || ''
  }

  // 设置默认值
  const typeIndex = permissionTypes.findIndex(t => t.value === currentPermission.value.type)
  if (typeIndex >= 0) {
    permissionTypeIndex.value = typeIndex
  }

  permissionSettingPopup.value.open()
}

/**
 * 移除权限
 */
const removePermission = (user) => {
  uni.showModal({
    title: '确认移除',
    content: `确定要移除用户"${user.userName}"的权限吗？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '处理中...',
          mask: true
        })

        try {
          const result = await accessApi.setAreaPermissions(areaId.value, {
            userIds: [user.userId],
            action: 'remove'
          })

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '移除成功',
              icon: 'success'
            })

            // 刷新列表
            loadPermissions()
          } else {
            uni.showToast({
              title: result.message || '移除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('移除权限失败:', error)
          uni.showToast({
            title: '移除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 打开添加用户弹窗
 */
const openAddUserModal = () => {
  addUserPopup.value.open()
  searchAvailableUsers()
}

/**
 * 关闭添加用户弹窗
 */
const closeAddUserModal = () => {
  addUserPopup.value.close()
  usersToAdd.value = []
  addUserKeyword.value = ''
}

/**
 * 搜索可用用户
 */
const searchAvailableUsers = async (keyword = '') => {
  try {
    // 这里需要调用用户搜索API
    // 暂时使用模拟数据
    const mockUsers = [
      { userId: 1, userName: '张三', workNo: 'E001', loginName: 'zhangsan', departmentName: '技术部', positionName: '工程师', avatar: '' },
      { userId: 2, userName: '李四', workNo: 'E002', loginName: 'lisi', departmentName: '市场部', positionName: '经理', avatar: '' },
      { userId: 3, userName: '王五', workNo: 'E003', loginName: 'wangwu', departmentName: '行政部', positionName: '专员', avatar: '' },
      { userId: 4, userName: '赵六', workNo: 'E004', loginName: 'zhaoliu', departmentName: '技术部', positionName: '开发工程师', avatar: '' }
    ]

    if (keyword) {
      availableUsers.value = mockUsers.filter(user =>
        user.userName.includes(keyword) ||
        user.workNo.includes(keyword) ||
        user.loginName.includes(keyword)
      )
    } else {
      availableUsers.value = mockUsers
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  }
}

/**
 * 添加用户搜索
 */
const onAddUserSearch = (e) => {
  addUserKeyword.value = e.detail.value
  searchAvailableUsers(addUserKeyword.value)
}

/**
 * 切换选择用户
 */
const toggleSelectUserToAdd = (user) => {
  const index = usersToAdd.value.indexOf(user.userId)
  if (index >= 0) {
    usersToAdd.value.splice(index, 1)
  } else {
    usersToAdd.value.push(user.userId)
  }
}

/**
 * 确认添加用户
 */
const confirmAddUsers = async () => {
  if (usersToAdd.value.length === 0) {
    return
  }

  uni.showLoading({
    title: '添加中...',
    mask: true
  })

  try {
    const result = await accessApi.setAreaPermissions(areaId.value, {
      userIds: usersToAdd.value,
      permissionType: 'permanent',
      action: 'add'
    })

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '添加成功',
        icon: 'success'
      })

      closeAddUserModal()
      // 刷新列表
      loadPermissions()
    } else {
      uni.showToast({
        title: result.message || '添加失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('添加用户失败:', error)
    uni.showToast({
      title: '添加失败',
      icon: 'none'
    })
  }
}

/**
 * 权限类型变更
 */
const onPermissionTypeChange = (e) => {
  const index = e.detail.value
  permissionTypeIndex.value = index
  currentPermission.value.type = permissionTypes[index].value
}

/**
 * 有效期变更
 */
const onExpiryDateChange = (e) => {
  currentPermission.value.expiryDate = e.detail.value
}

/**
 * 选择时间段
 */
const selectTimeSlot = (index) => {
  currentTimeSlot.value = index
}

/**
 * 切换星期
 */
const toggleWeekday = (index) => {
  const idx = selectedWeekdays.value.indexOf(index)
  if (idx >= 0) {
    selectedWeekdays.value.splice(idx, 1)
  } else {
    selectedWeekdays.value.push(index)
  }
}

/**
 * 关闭权限设置
 */
const closePermissionSetting = () => {
  permissionSettingPopup.value.close()
}

/**
 * 保存权限设置
 */
const savePermissionSetting = async () => {
  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    const data = {
      userIds: [currentUser.value.userId],
      permissionType: currentPermission.value.type,
      expiryDate: currentPermission.value.expiryDate,
      timeSlots: currentPermission.value.timeSlots,
      weekdays: selectedWeekdays.value,
      remark: currentPermission.value.remark
    }

    const result = await accessApi.setAreaPermissions(areaId.value, data)

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })

      closePermissionSetting()
      // 刷新列表
      loadPermissions()
    } else {
      uni.showToast({
        title: result.message || '保存失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('保存权限设置失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
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
.area-permission-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 30px;
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
}

// 区域信息卡片
.area-info-card {
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .area-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;

    .area-name {
      flex: 1;
      margin-left: 8px;
      font-size: 18px;
      font-weight: bold;
      color: #fff;
    }
  }

  .area-path {
    display: block;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.8);
  }
}

// 操作栏
.action-bar {
  display: flex;
  align-items: center;
  padding: 0 15px 15px;
  gap: 12px;

  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    height: 36px;
    padding: 0 12px;
    background-color: #fff;
    border-radius: 18px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .search-placeholder {
      flex: 1;
      margin-left: 8px;
      font-size: 14px;
      color: #999;
    }
  }

  .filter-btn {
    display: flex;
    align-items: center;
    padding: 0 16px;
    height: 36px;
    background-color: #fff;
    border-radius: 18px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .filter-text {
      margin-left: 6px;
      font-size: 14px;
      color: #666;
    }
  }
}

// 批量操作栏
.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #fff3cd;
  border-bottom: 1px solid #ffe58f;

  .batch-count {
    font-size: 14px;
    color: #faad14;
    font-weight: 500;
  }

  .batch-actions {
    display: flex;
    gap: 8px;

    .batch-btn {
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 13px;
      border: none;

      &.danger {
        background-color: #ff4d4f;
        color: #fff;
      }

      &:not(.danger) {
        background-color: #fff;
        color: #666;
      }
    }
  }
}

// 用户列表
.user-list {
  padding: 0 15px;

  .user-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .user-left {
      flex: 1;
      display: flex;
      align-items: center;
      overflow: hidden;

      .user-avatar {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        margin-right: 12px;
      }

      .user-info {
        flex: 1;
        overflow: hidden;

        .user-name {
          display: block;
          font-size: 15px;
          color: #333;
          font-weight: 500;
          margin-bottom: 4px;
        }

        .user-meta {
          display: flex;
          align-items: center;
          font-size: 12px;

          .user-dept {
            color: #666;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .user-divider {
            margin: 0 6px;
            color: #d9d9d9;
          }

          .user-position {
            color: #999;
          }
        }
      }
    }

    .user-right {
      display: flex;
      align-items: center;

      .permission-type {
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 12px;

        &.type-permanent {
          background-color: #f6ffed;
          color: #52c41a;
        }

        &.type-temporary {
          background-color: #fff7e6;
          color: #fa8c16;
        }

        &.type-time {
          background-color: #e6f7ff;
          color: #1890ff;
        }
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;

  .empty-icon {
    width: 120px;
    height: 120px;
    margin-bottom: 20px;
  }

  .empty-text {
    font-size: 15px;
    color: #999;
    margin-bottom: 30px;
  }

  .empty-btn {
    width: 120px;
    height: 44px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 22px;
    color: #fff;
    font-size: 15px;
    border: none;
  }
}

// 搜索弹窗
.search-modal {
  background-color: #fff;
  padding: 15px;
  border-bottom: 1px solid #eee;

  .search-input-box {
    display: flex;
    align-items: center;
    height: 40px;
    padding: 0 12px;
    background-color: #f5f5f5;
    border-radius: 20px;

    .search-input {
      flex: 1;
      height: 100%;
      margin-left: 8px;
      font-size: 15px;
      border: none;
      background: transparent;
    }

    .search-clear {
      font-size: 14px;
      color: #1890ff;
      margin-left: 8px;
    }
  }

  .search-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;

    .search-cancel {
      font-size: 15px;
      color: #666;
    }
  }
}

// 筛选弹窗
.filter-container {
  background-color: #fff;
  border-radius: 20px 20px 0 0;
  padding: 20px;

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .filter-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .filter-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 16px;
    }
  }

  .filter-content {
    .filter-section {
      margin-bottom: 25px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-label {
        font-size: 15px;
        color: #333;
        margin-bottom: 12px;
        font-weight: 500;
      }

      .filter-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .filter-tag {
          padding: 8px 16px;
          background-color: #f5f5f5;
          border-radius: 20px;
          font-size: 14px;
          color: #666;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
          }
        }
      }
    }
  }

  .filter-footer {
    display: flex;
    gap: 12px;
    margin-top: 20px;

    .filter-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      border: none;

      &.reset {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

// 添加用户弹窗
.add-user-container {
  height: 70vh;
  background-color: #fff;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 16px;
    }
  }

  .popup-content {
    flex: 1;
    padding: 15px;

    .user-search {
      margin-bottom: 15px;

      .search-input-wrapper {
        display: flex;
        align-items: center;
        height: 40px;
        padding: 0 12px;
        background-color: #f5f5f5;
        border-radius: 20px;

        .search-input {
          flex: 1;
          height: 100%;
          margin-left: 8px;
          font-size: 15px;
          border: none;
          background: transparent;
        }
      }
    }

    .available-users {
      .available-user-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .user-left {
          flex: 1;
          display: flex;
          align-items: center;

          .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            margin-right: 12px;
          }

          .user-info {
            flex: 1;

            .user-name {
              display: block;
              font-size: 15px;
              color: #333;
              margin-bottom: 4px;
            }

            .user-workno {
              display: block;
              font-size: 13px;
              color: #999;
            }
          }
        }
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #f0f0f0;

    .popup-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      border: none;

      &.cancel {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;

        &:disabled {
          opacity: 0.6;
        }
      }
    }
  }
}

// 权限设置弹窗
.permission-setting-container {
  height: 70vh;
  background-color: #fff;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 16px;
    }
  }

  .popup-content {
    flex: 1;
    padding: 20px;

    .setting-form {
      .form-item {
        margin-bottom: 20px;

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

        .form-picker {
          display: flex;
          justify-content: space-between;
          align-items: center;
          height: 48px;
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

        .time-slots {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 10px;

          .time-slot {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 12px;
            background-color: #f5f5f5;
            border-radius: 8px;
            border: 2px solid transparent;

            &.active {
              border-color: #1890ff;
              background-color: #e6f7ff;
            }

            .slot-label {
              font-size: 14px;
              color: #333;
              margin-bottom: 4px;
            }

            .slot-time {
              font-size: 12px;
              color: #666;
            }
          }
        }

        .weekdays {
          display: flex;
          justify-content: space-between;

          .weekday-item {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f5f5f5;
            border: 2px solid transparent;
            font-size: 14px;
            color: #666;

            &.active {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: #fff;
            }

            .weekday-text {
              font-size: 14px;
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
        }

        .form-count {
          position: absolute;
          bottom: 10px;
          right: 12px;
          font-size: 12px;
          color: #999;
        }
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #f0f0f0;

    .popup-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      border: none;

      &.cancel {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}
</style>
