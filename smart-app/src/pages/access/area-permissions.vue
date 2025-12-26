<template>
  <view class="area-permissions-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#ffffff"></uni-icons>
      </view>
      <view class="nav-title">权限矩阵</view>
      <view class="nav-right" @click="showMoreMenu">
        <uni-icons type="more" size="20" color="#ffffff"></uni-icons>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-container">
      <!-- 区域信息卡片 -->
      <view class="area-info-card">
        <view class="area-header">
          <view class="area-icon">
            <uni-icons type="location" size="20" color="#667eea"></uni-icons>
          </view>
          <view class="area-info">
            <view class="area-name">{{ areaInfo.areaName }}</view>
            <view class="area-code">{{ areaInfo.areaCode }}</view>
          </view>
        </view>
        <view class="permission-stats">
          <view class="stat-item">
            <view class="stat-value">{{ areaInfo.totalPersonnel }}</view>
            <view class="stat-label">人员数</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-value">{{ areaInfo.totalDoors }}</view>
            <view class="stat-label">门禁数</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-value">{{ areaInfo.totalPermissions }}</view>
            <view class="stat-label">权限数</view>
          </view>
        </view>
      </view>

      <!-- 筛选工具栏 -->
      <view class="filter-toolbar">
        <!-- 部门筛选 -->
        <view class="filter-item" @click="showDepartmentPicker">
          <text class="filter-text">{{ selectedDepartment || '全部部门' }}</text>
          <uni-icons type="down" size="12" color="#999"></uni-icons>
        </view>

        <!-- 角色筛选 -->
        <view class="filter-item" @click="showRolePicker">
          <text class="filter-text">{{ selectedRole || '全部角色' }}</text>
          <uni-icons type="down" size="12" color="#999"></uni-icons>
        </view>

        <!-- 搜索 -->
        <view class="search-box">
          <uni-icons type="search" size="16" color="#999"></uni-icons>
          <input
            class="search-input"
            type="text"
            placeholder="搜索人员"
            v-model="searchKeyword"
            @input="onSearch"
          />
        </view>

        <!-- 视图切换 -->
        <view class="view-switcher">
          <view
            class="switch-item"
            :class="{ active: viewMode === 'matrix' }"
            @click="switchViewMode('matrix')"
          >
            <uni-icons type="list" size="16" :color="viewMode === 'matrix' ? '#667eea' : '#999'"></uni-icons>
          </view>
          <view
            class="switch-item"
            :class="{ active: viewMode === 'card' }"
            @click="switchViewMode('card')"
          >
            <uni-icons type="grid" size="16" :color="viewMode === 'card' ? '#667eea' : '#999'"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="quick-actions">
        <view class="action-btn" @click="refreshData">
          <uni-icons type="refreshempty" size="16" color="#667eea"></uni-icons>
          <text class="action-text">刷新</text>
        </view>
        <view class="action-btn" @click="showPermissionTemplates">
          <uni-icons type="settings" size="16" color="#667eea"></uni-icons>
          <text class="action-text">模板</text>
        </view>
        <view class="action-btn" @click="showBatchPermission" v-if="viewMode === 'matrix'">
          <uni-icons type="compose" size="16" color="#667eea"></uni-icons>
          <text class="action-text">批量设置</text>
        </view>
        <view class="action-btn" @click="exportPermissionMatrix">
          <uni-icons type="upload" size="16" color="#667eea"></uni-icons>
          <text class="action-text">导出</text>
        </view>
      </view>

      <!-- 权限矩阵视图 -->
      <scroll-view
        class="matrix-container"
        scroll-y
        scroll-x
        v-if="viewMode === 'matrix'"
      >
        <!-- 表头（门禁列表） -->
        <view class="matrix-header">
          <view class="header-corner">
            <text class="corner-text">人员\\门禁</text>
          </view>
          <scroll-view class="header-scroll" scroll-x scroll-y>
            <view class="header-doors">
              <view
                v-for="door in doorList"
                :key="door.deviceId"
                class="header-door-item"
                @click="showDoorDetail(door)"
              >
                <view class="door-name">{{ door.deviceName }}</view>
                <view class="door-code">{{ door.deviceCode }}</view>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- 数据行（人员列表） -->
        <view class="matrix-body">
          <view
            v-for="person in personnelList"
            :key="person.personId"
            class="matrix-row"
          >
            <!-- 人员信息列 -->
            <view class="row-person-info" @click="showPersonnelDetail(person)">
              <image
                class="person-avatar"
                :src="person.avatar || '/static/images/default-avatar.png'"
              ></image>
              <view class="person-details">
                <view class="person-name">{{ person.personName }}</view>
                <view class="person-code">{{ person.personCode }}</view>
              </view>
            </view>

            <!-- 权限单元格 -->
            <scroll-view class="row-cells-scroll" scroll-x>
              <view class="row-cells">
                <view
                  v-for="door in doorList"
                  :key="`${person.personId}-${door.deviceId}`"
                  class="permission-cell"
                  :class="getCellClass(person, door)"
                  @click="showPermissionDetail(person, door)"
                >
                  <view class="cell-icon">
                    <uni-icons
                      :type="getPermissionIcon(person, door)"
                      size="14"
                      :color="getPermissionColor(person, door)"
                    ></uni-icons>
                  </view>
                  <view class="cell-badge" v-if="getPermissionBadge(person, door)">
                    {{ getPermissionBadge(person, door) }}
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="personnelList.length === 0 && !loading" class="empty-state">
          <uni-icons type="locked" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无权限数据</text>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="personnelList.length > 0">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="noMore" class="no-more-text">没有更多了</text>
          <text v-else class="load-text" @click="loadMore">加载更多</text>
        </view>
      </scroll-view>

      <!-- 卡片视图 -->
      <scroll-view
        class="card-container"
        scroll-y
        v-if="viewMode === 'card'"
      >
        <view
          v-for="person in personnelList"
          :key="person.personId"
          class="permission-card"
          @click="showPersonnelDetail(person)"
        >
          <!-- 卡片头部 -->
          <view class="card-header">
            <view class="person-info">
              <image
                class="person-avatar"
                :src="person.avatar || '/static/images/default-avatar.png'"
              ></image>
              <view class="person-details">
                <view class="person-name">{{ person.personName }}</view>
                <view class="person-meta">{{ person.personCode }} | {{ person.departmentName }}</view>
              </view>
            </view>
            <view class="permission-summary">
              <text class="summary-count">{{ person.permissionCount || 0 }}</text>
              <text class="summary-label">个权限</text>
            </view>
          </view>

          <!-- 权限列表 -->
          <view class="card-permissions">
            <view
              v-for="perm in person.permissions"
              :key="perm.deviceId"
              class="permission-item"
              :class="getPermissionItemClass(perm)"
              @click.stop="showPermissionDetail(person, perm)"
            >
              <view class="perm-info">
                <text class="perm-door-name">{{ perm.deviceName }}</text>
                <text class="perm-mode">{{ getPermissionModeLabel(perm.permissionMode) }}</text>
              </view>
              <uni-icons
                type="right"
                size="14"
                color="#d9d9d9"
              ></uni-icons>
            </view>

            <view v-if="!person.permissions || person.permissions.length === 0" class="no-permissions">
              <text class="no-perm-text">暂无门禁权限</text>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="personnelList.length === 0 && !loading" class="empty-state">
          <uni-icons type="locked" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无权限数据</text>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="personnelList.length > 0">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="noMore" class="no-more-text">没有更多了</text>
          <text v-else class="load-text" @click="loadMore">加载更多</text>
        </view>
      </scroll-view>
    </view>

    <!-- 部门选择器 -->
    <picker
      mode="selector"
      :range="departmentList"
      range-key="label"
      :value="departmentIndex"
      @change="onDepartmentChange"
    >
      <view></view>
    </picker>

    <!-- 角色选择器 -->
    <picker
      mode="selector"
      :range="roleList"
      range-key="label"
      :value="roleIndex"
      @change="onRoleChange"
    >
      <view></view>
    </picker>

    <!-- 权限模板弹窗 -->
    <uni-popup ref="templatePopup" type="bottom" :safe-area="false">
      <view class="popup-container">
        <view class="popup-header">
          <view class="popup-title">权限模板</view>
          <view class="popup-close" @click="closeTemplatePopup">
            <uni-icons type="close" size="20" color="#333"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <view class="template-list">
            <view
              v-for="template in permissionTemplates"
              :key="template.id"
              class="template-item"
              @click="applyTemplate(template)"
            >
              <view class="template-icon">
                <uni-icons :type="template.icon" size="24" :color="template.color"></uni-icons>
              </view>
              <view class="template-info">
                <view class="template-name">{{ template.name }}</view>
                <view class="template-desc">{{ template.description }}</view>
              </view>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 批量设置权限弹窗 -->
    <uni-popup ref="batchPermissionPopup" type="bottom" :safe-area="false">
      <view class="popup-container">
        <view class="popup-header">
          <view class="popup-title">批量设置权限</view>
          <view class="popup-close" @click="closeBatchPermission">
            <uni-icons type="close" size="20" color="#333"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <view class="batch-config">
            <!-- 选择人员 -->
            <view class="config-section">
              <view class="section-title">选择人员</view>
              <view class="select-mode">
                <view
                  class="mode-option"
                  :class="{ active: batchMode === 'all' }"
                  @click="selectBatchMode('all')"
                >
                  全部人员
                </view>
                <view
                  class="mode-option"
                  :class="{ active: batchMode === 'department' }"
                  @click="selectBatchMode('department')"
                >
                  按部门
                </view>
                <view
                  class="mode-option"
                  :class="{ active: batchMode === 'selected' }"
                  @click="selectBatchMode('selected')"
                >
                  已选择({{ selectedCount }})
                </view>
              </view>
            </view>

            <!-- 选择门禁 -->
            <view class="config-section">
              <view class="section-title">选择门禁</view>
              <scroll-view class="door-selector" scroll-y>
                <view
                  v-for="door in doorList"
                  :key="door.deviceId"
                  class="door-select-item"
                  @click="toggleDoorSelection(door)"
                >
                  <view class="door-select">
                    <uni-icons
                      :type="isDoorSelected(door) ? 'checkbox-filled' : 'circle'"
                      :color="isDoorSelected(door) ? '#667eea' : '#d9d9d9'"
                      size="20"
                    ></uni-icons>
                  </view>
                  <view class="door-info">
                    <text class="door-name">{{ door.deviceName }}</text>
                    <text class="door-code">{{ door.deviceCode }}</text>
                  </view>
                </view>
              </scroll-view>
            </view>

            <!-- 权限设置 -->
            <view class="config-section">
              <view class="section-title">权限设置</view>
              <view class="permission-modes">
                <view
                  v-for="mode in permissionModes"
                  :key="mode.value"
                  class="mode-option"
                  :class="{ active: batchPermissionMode === mode.value }"
                  @click="selectPermissionMode(mode.value)"
                >
                  {{ mode.label }}
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="popup-footer">
          <view class="footer-btn cancel" @click="closeBatchPermission">取消</view>
          <view class="footer-btn primary" @click="confirmBatchPermission">确定</view>
        </view>
      </view>
    </uni-popup>

    <!-- 权限详情弹窗 -->
    <uni-popup ref="permissionDetailPopup" type="center" :safe-area="false">
      <view class="detail-popup">
        <view class="detail-header">
          <view class="detail-title">权限详情</view>
          <view class="detail-close" @click="closePermissionDetail">
            <uni-icons type="close" size="20" color="#333"></uni-icons>
          </view>
        </view>

        <view class="detail-content" v-if="currentPermission">
          <!-- 人员信息 -->
          <view class="detail-section">
            <view class="section-label">人员</view>
            <view class="detail-person-info">
              <image
                class="detail-avatar"
                :src="currentPermission.person?.avatar || '/static/images/default-avatar.png'"
              ></image>
              <view class="detail-person-details">
                <view class="detail-person-name">{{ currentPermission.person?.personName }}</view>
                <view class="detail-person-code">{{ currentPermission.person?.personCode }}</view>
              </view>
            </view>
          </view>

          <!-- 门禁信息 -->
          <view class="detail-section">
            <view class="section-label">门禁</view>
            <view class="detail-door-info">
              <view class="detail-door-name">{{ currentPermission.door?.deviceName }}</view>
              <view class="detail-door-code">{{ currentPermission.door?.deviceCode }}</view>
              <view class="detail-door-location">{{ currentPermission.door?.location }}</view>
            </view>
          </view>

          <!-- 权限配置 -->
          <view class="detail-section">
            <view class="section-label">权限配置</view>
            <view class="permission-config">
              <view class="config-row">
                <text class="config-label">权限模式</text>
                <text class="config-value">{{ getPermissionModeLabel(currentPermission.permissionMode) }}</text>
              </view>
              <view class="config-row" v-if="currentPermission.startTime">
                <text class="config-label">开始时间</text>
                <text class="config-value">{{ currentPermission.startTime }}</text>
              </view>
              <view class="config-row" v-if="currentPermission.endTime">
                <text class="config-label">结束时间</text>
                <text class="config-value">{{ currentPermission.endTime }}</text>
              </view>
              <view class="config-row" v-if="currentPermission.allowedWeekdays">
                <text class="config-label">允许星期</text>
                <text class="config-value">{{ formatWeekdays(currentPermission.allowedWeekdays) }}</text>
              </view>
              <view class="config-row">
                <text class="config-label">状态</text>
                <view class="status-badge" :class="{ active: currentPermission.status === 1 }">
                  {{ currentPermission.status === 1 ? '生效中' : '已禁用' }}
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="detail-footer">
          <view class="footer-btn edit" @click="editCurrentPermission">编辑</view>
          <view class="footer-btn delete" @click="deleteCurrentPermission">删除</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// 状态栏高度
const statusBarHeight = ref(0)
const areaId = ref('')

// 区域信息
const areaInfo = reactive({
  areaName: 'A栋办公楼',
  areaCode: 'AREA-A-001',
  totalPersonnel: 86,
  totalDoors: 12,
  totalPermissions: 524
})

// 视图模式
const viewMode = ref('matrix') // matrix-矩阵视图, card-卡片视图

// 筛选条件
const selectedDepartment = ref('')
const selectedRole = ref('')
const searchKeyword = ref('')
const departmentIndex = ref(0)
const roleIndex = ref(0)

// 部门列表
const departmentList = ref([
  { label: '全部部门', value: '' },
  { label: '研发部', value: '1' },
  { label: '市场部', value: '2' },
  { label: '行政部', value: '3' },
  { label: '财务部', value: '4' }
])

// 角色列表
const roleList = ref([
  { label: '全部角色', value: '' },
  { label: '管理员', value: 'admin' },
  { label: '普通员工', value: 'employee' },
  { label: '访客', value: 'visitor' }
])

// 人员列表
const personnelList = ref([])
const doorList = ref([])
const loading = ref(false)
const pageNo = ref(1)
const pageSize = 20
const noMore = ref(false)

// 批量设置
const batchMode = ref('all')
const selectedDoors = ref([])
const batchPermissionMode = ref('full')
const permissionModes = [
  { value: 'full', label: '全部权限' },
  { value: 'time_limit', label: '时间限制' },
  { value: 'week_limit', label: '工作日限制' },
  { value: 'forbidden', label: '禁止通行' }
]

// 权限模板
const permissionTemplates = ref([
  {
    id: 1,
    name: '全员全通',
    icon: 'checkmarkempty',
    color: '#52c41a',
    description: '所有人员可通行所有门禁'
  },
  {
    id: 2,
    name: '工作时间',
    icon: 'calendar',
    color: '#faad14',
    description: '仅工作时间段可通行'
  },
  {
    id: 3,
    name: '部门隔离',
    icon: 'home',
    color: '#667eea',
    description: '仅本部门门禁权限'
  },
  {
    id: 4,
    name: '禁止所有',
    icon: 'closeempty',
    color: '#ff4d4f',
    description: '移除所有门禁权限'
  }
])

// 当前权限详情
const currentPermission = ref(null)

// 页面加载
onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  if (options.areaId) {
    areaId.value = options.areaId
  }

  loadAreaInfo()
  loadDoorList()
  loadPersonnelList()
})

/**
 * 加载区域信息
 */
const loadAreaInfo = async () => {
  try {
    // TODO: 调用实际的API
    console.log('加载区域信息:', areaId.value)
  } catch (error) {
    console.error('加载区域信息失败:', error)
  }
}

/**
 * 加载门禁列表
 */
const loadDoorList = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAreaDoors(areaId.value)

    // 模拟数据
    doorList.value = Array.from({ length: 8 }, (_, i) => ({
      deviceId: i + 1,
      deviceName: `门禁${String.fromCharCode(65 + i)}`,
      deviceCode: `DOOR-${String(i + 1).padStart(3, '0')}`,
      location: `${i + 1}楼`,
      status: 1
    }))
  } catch (error) {
    console.error('加载门禁列表失败:', error)
  }
}

/**
 * 加载人员列表
 */
const loadPersonnelList = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    pageNo.value = 1
    noMore.value = false
  }

  loading.value = true

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAreaPermissions({
    //   areaId: areaId.value,
    //   departmentId: selectedDepartment.value,
    //   roleId: selectedRole.value,
    //   keyword: searchKeyword.value,
    //   pageNo: pageNo.value,
    //   pageSize: pageSize
    // })

    // 模拟数据
    const mockData = Array.from({ length: 10 }, (_, i) => {
      const personId = i + 1
      const permissions = doorList.value.map(door => ({
        deviceId: door.deviceId,
        deviceName: door.deviceName,
        permissionMode: ['full', 'time_limit', 'week_limit', 'forbidden'][Math.floor(Math.random() * 4)],
        status: Math.random() > 0.2 ? 1 : 0
      }))

      return {
        personId,
        personName: `张${i + 1}`,
        personCode: `EMP00${i + 1}`,
        departmentName: ['研发部', '市场部', '行政部'][i % 3],
        avatar: `/static/images/avatar-${(i % 3) + 1}.png`,
        permissions,
        permissionCount: permissions.filter(p => p.permissionMode !== 'forbidden').length
      }
    })

    if (isRefresh) {
      personnelList.value = mockData
    } else {
      personnelList.value.push(...mockData)
    }

    if (pageNo.value >= 3) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载人员列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 切换视图模式
 */
const switchViewMode = (mode) => {
  viewMode.value = mode
}

/**
 * 搜索
 */
const onSearch = (e) => {
  searchKeyword.value = e.detail.value
  loadPersonnelList(true)
}

/**
 * 刷新数据
 */
const refreshData = () => {
  loadPersonnelList(true)
}

/**
 * 获取单元格样式类
 */
const getCellClass = (person, door) => {
  const permission = person.permissions?.find(p => p.deviceId === door.deviceId)
  if (!permission || permission.permissionMode === 'forbidden') {
    return 'cell-forbidden'
  }
  return `cell-${permission.permissionMode}`
}

/**
 * 获取权限图标
 */
const getPermissionIcon = (person, door) => {
  const permission = person.permissions?.find(p => p.deviceId === door.deviceId)
  if (!permission) return 'closeempty'
  const iconMap = {
    full: 'checkmarkempty',
    time_limit: 'calendar',
    week_limit: 'calendar',
    forbidden: 'closeempty'
  }
  return iconMap[permission.permissionMode] || 'help'
}

/**
 * 获取权限颜色
 */
const getPermissionColor = (person, door) => {
  const permission = person.permissions?.find(p => p.deviceId === door.deviceId)
  if (!permission) return '#d9d9d9'
  const colorMap = {
    full: '#52c41a',
    time_limit: '#faad14',
    week_limit: '#667eea',
    forbidden: '#ff4d4f'
  }
  return colorMap[permission.permissionMode] || '#d9d9d9'
}

/**
 * 获取权限徽章
 */
const getPermissionBadge = (person, door) => {
  const permission = person.permissions?.find(p => p.deviceId === door.deviceId)
  if (!permission || permission.permissionMode === 'forbidden') return ''
  if (permission.permissionMode === 'time_limit') return '时段'
  if (permission.permissionMode === 'week_limit') return '工作日'
  return ''
}

/**
 * 获取权限模式标签
 */
const getPermissionModeLabel = (mode) => {
  const labelMap = {
    full: '全部权限',
    time_limit: '时间限制',
    week_limit: '工作日限制',
    forbidden: '禁止通行'
  }
  return labelMap[mode] || mode
}

/**
 * 获取权限项样式类
 */
const getPermissionItemClass = (perm) => {
  return `perm-item-${perm.permissionMode}`
}

/**
 * 显示部门选择器
 */
const showDepartmentPicker = () => {
  // 通过 picker 组件触发
}

/**
 * 部门变更
 */
const onDepartmentChange = (e) => {
  departmentIndex.value = e.detail.value
  selectedDepartment.value = departmentList.value[e.detail.value].value
  loadPersonnelList(true)
}

/**
 * 显示角色选择器
 */
const showRolePicker = () => {
  // 通过 picker 组件触发
}

/**
 * 角色变更
 */
const onRoleChange = (e) => {
  roleIndex.value = e.detail.value
  selectedRole.value = roleList.value[e.detail.value].value
  loadPersonnelList(true)
}

/**
 * 显示权限模板
 */
const showPermissionTemplates = () => {
  $refs.templatePopup.open()
}

/**
 * 关闭权限模板
 */
const closeTemplatePopup = () => {
  $refs.templatePopup.close()
}

/**
 * 应用权限模板
 */
const applyTemplate = (template) => {
  closeTemplatePopup()
  uni.showModal({
    title: '应用模板',
    content: `确定要应用"${template.name}"权限模板吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用实际的API
          uni.showToast({
            title: '应用成功',
            icon: 'success'
          })
          loadPersonnelList(true)
        } catch (error) {
          uni.showToast({
            title: '应用失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 显示批量设置
 */
const showBatchPermission = () => {
  selectedDoors.value = []
  batchPermissionMode.value = 'full'
  batchMode.value = 'all'
  $refs.batchPermissionPopup.open()
}

/**
 * 关闭批量设置
 */
const closeBatchPermission = () => {
  $refs.batchPermissionPopup.close()
}

/**
 * 选择批量模式
 */
const selectBatchMode = (mode) => {
  batchMode.value = mode
}

/**
 * 切换门禁选择
 */
const toggleDoorSelection = (door) => {
  const index = selectedDoors.value.findIndex(d => d.deviceId === door.deviceId)
  if (index > -1) {
    selectedDoors.value.splice(index, 1)
  } else {
    selectedDoors.value.push(door)
  }
}

/**
 * 是否已选择门禁
 */
const isDoorSelected = (door) => {
  return selectedDoors.value.some(d => d.deviceId === door.deviceId)
}

/**
 * 选择权限模式
 */
const selectPermissionMode = (mode) => {
  batchPermissionMode.value = mode
}

/**
 * 确认批量设置
 */
const confirmBatchPermission = async () => {
  if (selectedDoors.value.length === 0) {
    uni.showToast({
      title: '请选择门禁',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '设置中...',
    mask: true
  })

  try {
    // TODO: 调用实际的API
    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({
        title: '设置成功',
        icon: 'success'
      })
      closeBatchPermission()
      loadPersonnelList(true)
    }, 500)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({
      title: '设置失败',
      icon: 'none'
    })
  }
}

/**
 * 显示权限详情
 */
const showPermissionDetail = (person, doorOrPerm) => {
  const door = doorOrPerm.deviceId ? doorOrPerm : person.permissions?.find(p => p.deviceId === doorOrPerm.deviceId)

  currentPermission.value = {
    person,
    door: doorOrPerm.deviceId ? doorOrPerm : null,
    permissionMode: door?.permissionMode || 'full',
    startTime: door?.startTime || '08:00',
    endTime: door?.endTime || '18:00',
    allowedWeekdays: door?.allowedWeekdays || '1,2,3,4,5',
    status: door?.status || 1
  }

  $refs.permissionDetailPopup.open()
}

/**
 * 关闭权限详情
 */
const closePermissionDetail = () => {
  $refs.permissionDetailPopup.close()
}

/**
 * 格式化星期
 */
const formatWeekdays = (weekdays) => {
  const weekMap = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const days = weekdays.split(',').map(d => weekMap[parseInt(d) - 1])
  return days.join('、')
}

/**
 * 编辑当前权限
 */
const editCurrentPermission = () => {
  closePermissionDetail()
  // TODO: 跳转到编辑页面
  uni.showToast({
    title: '编辑功能开发中',
    icon: 'none'
  })
}

/**
 * 删除当前权限
 */
const deleteCurrentPermission = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除此权限吗？',
    success: (res) => {
      if (res.confirm) {
        closePermissionDetail()
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
        loadPersonnelList(true)
      }
    }
  })
}

/**
 * 显示人员详情
 */
const showPersonnelDetail = (person) => {
  uni.navigateTo({
    url: `/pages/personnel/detail?personId=${person.personId}`
  })
}

/**
 * 显示门禁详情
 */
const showDoorDetail = (door) => {
  uni.navigateTo({
    url: `/pages/access/door-detail?deviceId=${door.deviceId}`
  })
}

/**
 * 导出权限矩阵
 */
const exportPermissionMatrix = () => {
  uni.showToast({
    title: '导出功能开发中',
    icon: 'none'
  })
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    pageNo.value++
    loadPersonnelList()
  }
}

/**
 * 显示更多菜单
 */
const showMoreMenu = () => {
  uni.showActionSheet({
    itemList: ['刷新数据', '切换视图', '导出报表', '权限设置'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          refreshData()
          break
        case 1:
          switchViewMode(viewMode.value === 'matrix' ? 'card' : 'matrix')
          break
        case 2:
          exportPermissionMatrix()
          break
        case 3:
          showBatchPermission()
          break
      }
    }
  })
}

/**
 * 已选择数量
 */
const selectedCount = computed(() => {
  return personnelList.value.length
})

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.area-permissions-page {
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

  .nav-left,
  .nav-right {
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
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);

  .area-header {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;

    .area-icon {
      width: 64rpx;
      height: 64rpx;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
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
  }

  .permission-stats {
    display: flex;
    align-items: center;
    justify-content: space-around;
    background-color: rgba(255, 255, 255, 0.15);
    border-radius: 16rpx;
    padding: 30rpx 0;

    .stat-item {
      text-align: center;
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #ffffff;
        margin-bottom: 8rpx;
      }

      .stat-label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.8);
      }
    }

    .stat-divider {
      width: 1px;
      height: 40rpx;
      background-color: rgba(255, 255, 255, 0.3);
    }
  }
}

// 筛选工具栏
.filter-toolbar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
  flex-wrap: wrap;

  .filter-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 20rpx;
    background-color: #ffffff;
    border-radius: 12rpx;
    font-size: 13px;

    .filter-text {
      color: #333;
    }
  }

  .search-box {
    flex: 1;
    min-width: 200rpx;
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 12rpx 20rpx;
    background-color: #ffffff;
    border-radius: 12rpx;

    .search-input {
      flex: 1;
      font-size: 13px;
    }
  }

  .view-switcher {
    display: flex;
    gap: 8rpx;
    background-color: #ffffff;
    border-radius: 12rpx;
    padding: 8rpx;

    .switch-item {
      width: 56rpx;
      height: 56rpx;
      border-radius: 8rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.active {
        background-color: #f0f0f0;
      }
    }
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 16rpx;
  margin-bottom: 30rpx;
  flex-wrap: wrap;

  .action-btn {
    flex: 1;
    min-width: 120rpx;
    height: 60rpx;
    background-color: #ffffff;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

    .action-text {
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 矩阵视图
.matrix-container {
  background-color: #ffffff;
  border-radius: 16rpx;
  height: calc(100vh - 520rpx);

  .matrix-header {
    display: flex;
    border-bottom: 1px solid #f0f0f0;
    position: sticky;
    top: 0;
    background-color: #ffffff;
    z-index: 10;

    .header-corner {
      min-width: 200rpx;
      padding: 20rpx;
      background-color: #f5f5f5;
      border-right: 1px solid #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;

      .corner-text {
        font-size: 12px;
        color: #666;
        font-weight: 500;
      }
    }

    .header-scroll {
      flex: 1;
      white-space: nowrap;

      .header-doors {
        display: inline-flex;

        .header-door-item {
          min-width: 120rpx;
          padding: 16rpx 12rpx;
          text-align: center;
          border-right: 1px solid #f0f0f0;

          .door-name {
            font-size: 13px;
            font-weight: 500;
            color: #333;
            margin-bottom: 6rpx;
          }

          .door-code {
            font-size: 11px;
            color: #999;
          }
        }
      }
    }
  }

  .matrix-body {
    .matrix-row {
      display: flex;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .row-person-info {
        min-width: 200rpx;
        padding: 20rpx;
        background-color: #ffffff;
        border-right: 1px solid #f0f0f0;
        display: flex;
        align-items: center;
        gap: 16rpx;
        position: sticky;
        left: 0;
        z-index: 5;

        .person-avatar {
          width: 48rpx;
          height: 48rpx;
          border-radius: 50%;
          background-color: #f5f5f5;
        }

        .person-details {
          flex: 1;
          overflow: hidden;

          .person-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 6rpx;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .person-code {
            font-size: 11px;
            color: #999;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
      }

      .row-cells-scroll {
        flex: 1;
        white-space: nowrap;

        .row-cells {
          display: inline-flex;

          .permission-cell {
            min-width: 120rpx;
            height: 100rpx;
            border-right: 1px solid #f0f0f0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            position: relative;

            .cell-icon {
              margin-bottom: 6rpx;
            }

            .cell-badge {
              padding: 2rpx 8rpx;
              border-radius: 8rpx;
              font-size: 10px;
              background-color: rgba(0, 0, 0, 0.05);
            }

            &.cell-full {
              background-color: rgba(82, 196, 26, 0.1);
            }

            &.cell-time_limit,
            &.cell-week_limit {
              background-color: rgba(250, 173, 20, 0.1);
            }

            &.cell-forbidden {
              background-color: rgba(255, 77, 79, 0.05);
            }
          }
        }
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 150rpx;

    .empty-text {
      font-size: 14px;
      color: #999;
      margin-top: 30rpx;
    }
  }

  .load-more {
    padding: 30rpx 0;
    text-align: center;

    .loading-text,
    .load-text,
    .no-more-text {
      font-size: 13px;
      color: #999;
    }
  }
}

// 卡片视图
.card-container {
  height: calc(100vh - 520rpx);

  .permission-card {
    background-color: #ffffff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .person-info {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .person-avatar {
          width: 64rpx;
          height: 64rpx;
          border-radius: 50%;
          background-color: #f5f5f5;
        }

        .person-details {
          .person-name {
            font-size: 16px;
            font-weight: 500;
            color: #333;
            margin-bottom: 8rpx;
          }

          .person-meta {
            font-size: 13px;
            color: #999;
          }
        }
      }

      .permission-summary {
        text-align: center;

        .summary-count {
          font-size: 20px;
          font-weight: 600;
          color: #667eea;
        }

        .summary-label {
          font-size: 11px;
          color: #999;
        }
      }
    }

    .card-permissions {
      .permission-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16rpx;
        border-radius: 12rpx;
        margin-bottom: 12rpx;
        background-color: #f5f5f5;

        &:last-child {
          margin-bottom: 0;
        }

        .perm-info {
          flex: 1;

          .perm-door-name {
            font-size: 14px;
            color: #333;
            margin-bottom: 6rpx;
            display: block;
          }

          .perm-mode {
            font-size: 12px;
            color: #999;
          }
        }

        &.perm-item-full {
          background-color: rgba(82, 196, 26, 0.1);
        }

        &.perm-item-time_limit,
        &.perm-item-week_limit {
          background-color: rgba(250, 173, 20, 0.1);
        }

        &.perm-item-forbidden {
          background-color: rgba(255, 77, 79, 0.05);
        }
      }

      .no-permissions {
        padding: 30rpx 0;
        text-align: center;

        .no-perm-text {
          font-size: 13px;
          color: #999;
        }
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 150rpx;

    .empty-text {
      font-size: 14px;
      color: #999;
      margin-top: 30rpx;
    }
  }

  .load-more {
    padding: 30rpx 0;
    text-align: center;

    .loading-text,
    .load-text,
    .no-more-text {
      font-size: 13px;
      color: #999;
    }
  }
}

// 弹窗样式
.popup-container {
  background-color: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    flex: 1;
    overflow-y: auto;
    padding: 30rpx;
  }

  .popup-footer {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 1px solid #f0f0f0;

    .footer-btn {
      flex: 1;
      height: 88rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      font-weight: 500;

      &.cancel {
        background-color: #f5f5f5;
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #ffffff;
      }
    }
  }
}

// 权限模板列表
.template-list {
  .template-item {
    display: flex;
    align-items: center;
    padding: 24rpx;
    background-color: #f5f5f5;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .template-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 16rpx;
      background-color: rgba(102, 126, 234, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
    }

    .template-info {
      flex: 1;

      .template-name {
        font-size: 16px;
        font-weight: 500;
        color: #333;
        margin-bottom: 8rpx;
      }

      .template-desc {
        font-size: 13px;
        color: #999;
      }
    }
  }
}

// 批量配置
.batch-config {
  .config-section {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 15px;
      font-weight: 500;
      color: #333;
      margin-bottom: 16rpx;
    }

    .select-mode,
    .permission-modes {
      display: flex;
      gap: 16rpx;

      .mode-option {
        flex: 1;
        height: 64rpx;
        border: 1px solid #d9d9d9;
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: #666;
        transition: all 0.3s;

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-color: transparent;
          color: #ffffff;
          font-weight: 500;
        }
      }
    }

    .door-selector {
      max-height: 300rpx;
      background-color: #f5f5f5;
      border-radius: 12rpx;

      .door-select-item {
        display: flex;
        align-items: center;
        padding: 16rpx 20rpx;
        border-bottom: 1px solid #eaeaea;

        &:last-child {
          border-bottom: none;
        }

        .door-select {
          margin-right: 16rpx;
        }

        .door-info {
          flex: 1;

          .door-name {
            font-size: 14px;
            color: #333;
            margin-bottom: 6rpx;
            display: block;
          }

          .door-code {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
  }
}

// 权限详情弹窗
.detail-popup {
  background-color: #ffffff;
  border-radius: 24rpx;
  width: 600rpx;
  max-height: 80vh;
  display: flex;
  flex-direction: column;

  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1px solid #f0f0f0;

    .detail-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .detail-close {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .detail-content {
    flex: 1;
    overflow-y: auto;
    padding: 30rpx;

    .detail-section {
      margin-bottom: 30rpx;

      .section-label {
        font-size: 13px;
        color: #999;
        margin-bottom: 16rpx;
      }

      .detail-person-info {
        display: flex;
        align-items: center;
        gap: 16rpx;
        padding: 20rpx;
        background-color: #f5f5f5;
        border-radius: 12rpx;

        .detail-avatar {
          width: 64rpx;
          height: 64rpx;
          border-radius: 50%;
          background-color: #eaeaea;
        }

        .detail-person-details {
          .detail-person-name {
            font-size: 15px;
            font-weight: 500;
            color: #333;
            margin-bottom: 8rpx;
          }

          .detail-person-code {
            font-size: 13px;
            color: #999;
          }
        }
      }

      .detail-door-info {
        padding: 20rpx;
        background-color: #f5f5f5;
        border-radius: 12rpx;

        .detail-door-name {
          font-size: 15px;
          font-weight: 500;
          color: #333;
          margin-bottom: 8rpx;
        }

        .detail-door-code,
        .detail-door-location {
          font-size: 13px;
          color: #999;
          margin-bottom: 6rpx;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }

      .permission-config {
        .config-row {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 16rpx 0;
          border-bottom: 1px solid #f0f0f0;

          &:last-child {
            border-bottom: none;
          }

          .config-label {
            font-size: 14px;
            color: #666;
          }

          .config-value {
            font-size: 14px;
            color: #333;
            font-weight: 500;
          }

          .status-badge {
            padding: 6rpx 16rpx;
            border-radius: 8rpx;
            font-size: 12px;
            background-color: #f5f5f5;
            color: #999;

            &.active {
              background-color: rgba(82, 196, 26, 0.1);
              color: #52c41a;
            }
          }
        }
      }
    }
  }

  .detail-footer {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 1px solid #f0f0f0;

    .footer-btn {
      flex: 1;
      height: 88rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      font-weight: 500;

      &.edit {
        background-color: #f5f5f5;
        color: #666;
      }

      &.delete {
        background-color: rgba(255, 77, 79, 0.1);
        color: #ff4d4f;
      }
    }
  }
}
</style>
