<template>
  <view class="area-personnel-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#ffffff"></uni-icons>
      </view>
      <view class="nav-title">区域人员管理</view>
      <view class="nav-right">
        <uni-icons type="more" size="20" color="#ffffff"></uni-icons>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-container">
      <!-- 区域信息卡片 -->
      <view class="area-info-card">
        <view class="area-header">
          <view class="area-icon">
            <uni-icons type="location" size="24" color="#667eea"></uni-icons>
          </view>
          <view class="area-info">
            <view class="area-name">{{ areaInfo.areaName }}</view>
            <view class="area-code">{{ areaInfo.areaCode }}</view>
          </view>
          <view class="area-status" :class="{ online: areaInfo.status === 1 }">
            {{ areaInfo.status === 1 ? '正常' : '禁用' }}
          </view>
        </view>

        <view class="area-stats">
          <view class="stat-item">
            <view class="stat-value">{{ areaInfo.totalPersonnel }}</view>
            <view class="stat-label">总人数</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-value">{{ areaInfo.onlinePersonnel }}</view>
            <view class="stat-label">在线</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-value">{{ areaInfo.todayPasses }}</view>
            <view class="stat-label">今日通行</view>
          </view>
        </view>
      </view>

      <!-- 快捷操作栏 -->
      <view class="quick-actions">
        <view class="action-btn" @click="refreshList">
          <uni-icons type="refreshempty" size="18" color="#667eea"></uni-icons>
          <text class="action-text">刷新</text>
        </view>
        <view class="action-btn" @click="toggleBatchMode" v-if="!batchMode">
          <uni-icons type="list" size="18" color="#667eea"></uni-icons>
          <text class="action-text">管理</text>
        </view>
        <view class="action-btn" @click="toggleSelectAll" v-if="batchMode">
          <uni-icons :type="allSelected ? 'checkbox' : 'circle'" size="18" color="#667eea"></uni-icons>
          <text class="action-text">{{ allSelected ? '取消全选' : '全选' }}</text>
        </view>
        <view class="action-btn" @click="showAddPersonnel" v-if="!batchMode">
          <uni-icons type="plus" size="18" color="#667eea"></uni-icons>
          <text class="action-text">添加人员</text>
        </view>
        <view class="action-btn danger" @click="batchRemove" v-if="batchMode && selectedPersonnel.length > 0">
          <uni-icons type="trash" size="18" color="#ff4d4f"></uni-icons>
          <text class="action-text">移除({{ selectedPersonnel.length }})</text>
        </view>
        <view class="action-btn" @click="showAutoAssign" v-if="!batchMode">
          <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
          <text class="action-text">自动分配</text>
        </view>
      </view>

      <!-- 筛选标签 -->
      <view class="filter-tags">
        <view
          v-for="(tag, index) in filterTags"
          :key="index"
          class="filter-tag"
          :class="{ active: activeFilter === index }"
          @click="switchFilter(index)"
        >
          {{ tag.label }}
        </view>
      </view>

      <!-- 人员列表 -->
      <scroll-view
        class="personnel-list"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view v-if="personnelList.length === 0 && !loading" class="empty-state">
          <uni-icons type="person" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无人员数据</text>
        </view>

        <view
          v-for="person in personnelList"
          :key="person.personId"
          class="personnel-card"
          @click="viewPersonnelDetail(person)"
        >
          <!-- 批量选择模式 -->
          <view class="select-mode" v-if="batchMode" @click.stop="toggleSelect(person)">
            <uni-icons
              :type="isSelected(person) ? 'checkbox-filled' : 'circle'"
              :color="isSelected(person) ? '#667eea' : '#d9d9d9'"
              size="22"
            ></uni-icons>
          </view>

          <!-- 人员头像 -->
          <view class="personnel-avatar">
            <image :src="person.avatar || '/static/images/default-avatar.png'" class="avatar-image"></image>
            <view class="avatar-status" :class="{ online: person.online }"></view>
          </view>

          <!-- 人员信息 -->
          <view class="personnel-info">
            <view class="personnel-header">
              <view class="personnel-name">{{ person.personName }}</view>
              <view class="permission-badge" :class="getPermissionClass(person.permissionType)">
                {{ getPermissionLabel(person.permissionType) }}
              </view>
            </view>
            <view class="personnel-meta">
              <text class="meta-text">{{ person.personCode }}</text>
              <text class="meta-divider">|</text>
              <text class="meta-text">{{ person.departmentName }}</text>
            </view>
            <view class="personnel-time" v-if="person.validPeriod">
              <uni-icons type="calendar" size="12" color="#999"></uni-icons>
              <text class="time-text">{{ person.validPeriod }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="personnel-actions" @click.stop>
            <view class="action-icon" @click="editPermission(person)">
              <uni-icons type="compose" size="18" color="#667eea"></uni-icons>
            </view>
            <view class="action-icon" @click="removePersonnel(person)">
              <uni-icons type="clear" size="18" color="#ff4d4f"></uni-icons>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="personnelList.length > 0">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="noMore" class="no-more-text">没有更多了</text>
          <text v-else class="load-text">上拉加载更多</text>
        </view>
      </scroll-view>
    </view>

    <!-- 添加人员弹窗 -->
    <uni-popup ref="addPersonnelPopup" type="bottom" :safe-area="false">
      <view class="popup-container">
        <view class="popup-header">
          <view class="popup-title">添加人员</view>
          <view class="popup-close" @click="closeAddPersonnel">
            <uni-icons type="close" size="20" color="#333"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <!-- 人员搜索 -->
          <view class="search-box">
            <uni-icons type="search" size="16" color="#999"></uni-icons>
            <input
              class="search-input"
              type="text"
              placeholder="搜索姓名或工号"
              v-model="searchKeyword"
              @input="onPersonnelSearch"
            />
          </view>

          <!-- 可选人员列表 -->
          <scroll-view class="available-personnel-list" scroll-y>
            <view
              v-for="person in availablePersonnel"
              :key="person.personId"
              class="available-personnel-item"
              @click="togglePersonnelSelection(person)"
            >
              <view class="personnel-select">
                <uni-icons
                  :type="isPersonnelSelected(person) ? 'checkbox-filled' : 'circle'"
                  :color="isPersonnelSelected(person) ? '#667eea' : '#d9d9d9'"
                  size="20"
                ></uni-icons>
              </view>
              <image class="personnel-thumb" :src="person.avatar || '/static/images/default-avatar.png'"></image>
              <view class="personnel-details">
                <view class="personnel-name-line">
                  <text class="personnel-name">{{ person.personName }}</text>
                  <text class="personnel-code">{{ person.personCode }}</text>
                </view>
                <text class="personnel-dept">{{ person.departmentName }}</text>
              </view>
            </view>

            <view v-if="availablePersonnel.length === 0" class="empty-search">
              <text class="empty-text">未找到匹配的人员</text>
            </view>
          </scroll-view>

          <!-- 权限配置 -->
          <view class="permission-config">
            <view class="config-title">权限设置</view>

            <!-- 权限类型 -->
            <view class="form-item">
              <view class="form-label">权限类型</view>
              <view class="permission-types">
                <view
                  v-for="type in permissionTypes"
                  :key="type.value"
                  class="type-option"
                  :class="{ active: permissionType === type.value }"
                  @click="selectPermissionType(type.value)"
                >
                  {{ type.label }}
                </view>
              </view>
            </view>

            <!-- 有效期设置 -->
            <view class="form-item" v-if="permissionType === 'temporary'">
              <view class="form-label">有效期</view>
              <picker mode="date" :value="startDate" @change="onStartDateChange">
                <view class="date-picker">
                  <text :class="{ placeholder: !startDate }">
                    {{ startDate || '选择开始日期' }}
                  </text>
                  <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
                </view>
              </picker>
              <picker mode="date" :value="endDate" @change="onEndDateChange">
                <view class="date-picker">
                  <text :class="{ placeholder: !endDate }">
                    {{ endDate || '选择结束日期' }}
                  </text>
                  <uni-icons type="right" size="14" color="#d9d9d9"></uni-icons>
                </view>
              </picker>
            </view>
          </view>
        </view>

        <view class="popup-footer">
          <view class="footer-btn cancel" @click="closeAddPersonnel">取消</view>
          <view class="footer-btn primary" @click="confirmAddPersonnel">
            确定{{ tempSelectedPersonnel.length > 0 ? `(${tempSelectedPersonnel.length})` : '' }}
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 自动分配弹窗 -->
    <uni-popup ref="autoAssignPopup" type="bottom" :safe-area="false">
      <view class="popup-container">
        <view class="popup-header">
          <view class="popup-title">自动分配权限</view>
          <view class="popup-close" @click="closeAutoAssign">
            <uni-icons type="close" size="20" color="#333"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <view class="assign-options">
            <view class="assign-option" @click="autoAssignByDepartment">
              <view class="option-icon">
                <uni-icons type="home" size="32" color="#667eea"></uni-icons>
              </view>
              <view class="option-info">
                <view class="option-title">按部门分配</view>
                <view class="option-desc">自动分配指定部门的所有人员</view>
              </view>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>

            <view class="assign-option" @click="autoAssignByRole">
              <view class="option-icon">
                <uni-icons type="person" size="32" color="#667eea"></uni-icons>
              </view>
              <view class="option-info">
                <view class="option-title">按角色分配</view>
                <view class="option-desc">自动分配指定角色的所有人员</view>
              </view>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>

            <view class="assign-option" @click="autoAssignBySubArea">
              <view class="option-icon">
                <uni-icons type="location" size="32" color="#667eea"></uni-icons>
              </view>
              <view class="option-info">
                <view class="option-title">按子区域分配</view>
                <view class="option-desc">自动分配子区域的所有人员</view>
              </view>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </view>
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
  status: 1,
  totalPersonnel: 86,
  onlinePersonnel: 72,
  todayPasses: 234
})

// 人员列表
const personnelList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNo = ref(1)
const pageSize = 20
const noMore = ref(false)

// 批量操作
const batchMode = ref(false)
const selectedPersonnel = ref([])
const allSelected = ref(false)

// 筛选
const filterTags = [
  { label: '全部', value: null },
  { label: '全部权限', value: 'full' },
  { label: '受限访问', value: 'limited' },
  { label: '临时访问', value: 'temporary' }
]
const activeFilter = ref(0)

// 添加人员相关
const searchKeyword = ref('')
const availablePersonnel = ref([])
const tempSelectedPersonnel = ref([])
const permissionType = ref('full')
const permissionTypes = [
  { value: 'full', label: '全部权限' },
  { value: 'limited', label: '受限访问' },
  { value: 'temporary', label: '临时访问' }
]
const startDate = ref('')
const endDate = ref('')

// 页面加载
onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  if (options.areaId) {
    areaId.value = options.areaId
  }

  loadAreaInfo()
  loadPersonnelList()
  loadAvailablePersonnel()
})

/**
 * 加载区域信息
 */
const loadAreaInfo = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAreaDetail(areaId.value)

    console.log('加载区域信息:', areaId.value)
  } catch (error) {
    console.error('加载区域信息失败:', error)
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
    // const res = await areaApi.getAreaPersonnel({
    //   areaId: areaId.value,
    //   pageNo: pageNo.value,
    //   pageSize: pageSize,
    //   permissionType: filterTags[activeFilter.value].value
    // })

    // 模拟数据
    const mockData = Array.from({ length: 10 }, (_, i) => ({
      personId: i + 1,
      personName: `张${i + 1}`,
      personCode: `EMP00${i + 1}`,
      departmentName: '研发部',
      avatar: '/static/images/avatar-' + (i % 3 + 1) + '.png',
      permissionType: ['full', 'limited', 'temporary'][i % 3],
      online: Math.random() > 0.3,
      validPeriod: i % 3 === 2 ? '2024-01-01 至 2024-12-31' : ''
    }))

    if (isRefresh) {
      personnelList.value = mockData
    } else {
      personnelList.value.push(...mockData)
    }

    // 模拟没有更多数据
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
    refreshing.value = false
  }
}

/**
 * 加载可选人员列表
 */
const loadAvailablePersonnel = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await personnelApi.getAvailablePersonnel({ areaId: areaId.value })

    // 模拟数据
    availablePersonnel.value = Array.from({ length: 15 }, (_, i) => ({
      personId: i + 100,
      personName: `李${i + 1}`,
      personCode: `EMP00${i + 100}`,
      departmentName: ['研发部', '市场部', '行政部'][i % 3],
      avatar: '/static/images/avatar-' + (i % 3 + 1) + '.png'
    }))
  } catch (error) {
    console.error('加载可选人员失败:', error)
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadPersonnelList(true)
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
 * 刷新列表
 */
const refreshList = () => {
  loadPersonnelList(true)
}

/**
 * 切换批量模式
 */
const toggleBatchMode = () => {
  batchMode.value = !batchMode.value
  if (!batchMode.value) {
    selectedPersonnel.value = []
    allSelected.value = false
  }
}

/**
 * 切换全选
 */
const toggleSelectAll = () => {
  allSelected.value = !allSelected.value
  if (allSelected.value) {
    selectedPersonnel.value = personnelList.value.map(p => p.personId)
  } else {
    selectedPersonnel.value = []
  }
}

/**
 * 切换选择
 */
const toggleSelect = (person) => {
  const index = selectedPersonnel.value.indexOf(person.personId)
  if (index > -1) {
    selectedPersonnel.value.splice(index, 1)
  } else {
    selectedPersonnel.value.push(person.personId)
  }
  allSelected.value = selectedPersonnel.value.length === personnelList.value.length
}

/**
 * 是否已选择
 */
const isSelected = (person) => {
  return selectedPersonnel.value.includes(person.personId)
}

/**
 * 切换筛选标签
 */
const switchFilter = (index) => {
  activeFilter.value = index
  loadPersonnelList(true)
}

/**
 * 获取权限标签
 */
const getPermissionLabel = (type) => {
  const map = {
    full: '全部权限',
    limited: '受限访问',
    temporary: '临时访问'
  }
  return map[type] || type
}

/**
 * 获取权限样式类
 */
const getPermissionClass = (type) => {
  const map = {
    full: 'permission-full',
    limited: 'permission-limited',
    temporary: 'permission-temporary'
  }
  return map[type] || ''
}

/**
 * 显示添加人员弹窗
 */
const showAddPersonnel = () => {
  tempSelectedPersonnel.value = []
  searchKeyword.value = ''
  permissionType.value = 'full'
  startDate.value = ''
  endDate.value = ''
  $refs.addPersonnelPopup.open()
}

/**
 * 关闭添加人员弹窗
 */
const closeAddPersonnel = () => {
  $refs.addPersonnelPopup.close()
}

/**
 * 人员搜索
 */
const onPersonnelSearch = (e) => {
  const keyword = e.detail.value
  // TODO: 调用搜索API
  console.log('搜索人员:', keyword)
}

/**
 * 切换人员选择
 */
const togglePersonnelSelection = (person) => {
  const index = tempSelectedPersonnel.value.findIndex(p => p.personId === person.personId)
  if (index > -1) {
    tempSelectedPersonnel.value.splice(index, 1)
  } else {
    tempSelectedPersonnel.value.push(person)
  }
}

/**
 * 是否已选择人员
 */
const isPersonnelSelected = (person) => {
  return tempSelectedPersonnel.value.some(p => p.personId === person.personId)
}

/**
 * 选择权限类型
 */
const selectPermissionType = (type) => {
  permissionType.value = type
}

/**
 * 开始日期变更
 */
const onStartDateChange = (e) => {
  startDate.value = e.detail.value
}

/**
 * 结束日期变更
 */
const onEndDateChange = (e) => {
  endDate.value = e.detail.value
}

/**
 * 确认添加人员
 */
const confirmAddPersonnel = async () => {
  if (tempSelectedPersonnel.value.length === 0) {
    uni.showToast({
      title: '请选择人员',
      icon: 'none'
    })
    return
  }

  // 临时权限需要设置有效期
  if (permissionType.value === 'temporary' && (!startDate.value || !endDate.value)) {
    uni.showToast({
      title: '请设置有效期',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '添加中...',
    mask: true
  })

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.addPersonnelToArea(areaId.value, {
    //   personIds: tempSelectedPersonnel.value.map(p => p.personId),
    //   permissionType: permissionType.value,
    //   startDate: startDate.value,
    //   endDate: endDate.value
    // })

    setTimeout(() => {
      uni.hideLoading()
      uni.showToast({
        title: '添加成功',
        icon: 'success'
      })
      closeAddPersonnel()
      loadPersonnelList(true)
      loadAreaInfo()
    }, 500)
  } catch (error) {
    uni.hideLoading()
    console.error('添加人员失败:', error)
    uni.showToast({
      title: '添加失败',
      icon: 'none'
    })
  }
}

/**
 * 显示自动分配弹窗
 */
const showAutoAssign = () => {
  $refs.autoAssignPopup.open()
}

/**
 * 关闭自动分配弹窗
 */
const closeAutoAssign = () => {
  $refs.autoAssignPopup.close()
}

/**
 * 按部门自动分配
 */
const autoAssignByDepartment = () => {
  closeAutoAssign()
  // TODO: 实现按部门分配
  uni.showToast({
    title: '按部门分配功能开发中',
    icon: 'none'
  })
}

/**
 * 按角色自动分配
 */
const autoAssignByRole = () => {
  closeAutoAssign()
  // TODO: 实现按角色分配
  uni.showToast({
    title: '按角色分配功能开发中',
    icon: 'none'
  })
}

/**
 * 按子区域自动分配
 */
const autoAssignBySubArea = () => {
  closeAutoAssign()
  // TODO: 实现按子区域分配
  uni.showToast({
    title: '按子区域分配功能开发中',
    icon: 'none'
  })
}

/**
 * 查看人员详情
 */
const viewPersonnelDetail = (person) => {
  uni.navigateTo({
    url: `/pages/personnel/detail?personId=${person.personId}`
  })
}

/**
 * 编辑权限
 */
const editPermission = (person) => {
  uni.navigateTo({
    url: `/pages/access/area-permission-edit?areaId=${areaId.value}&personId=${person.personId}`
  })
}

/**
 * 移除人员
 */
const removePersonnel = (person) => {
  uni.showModal({
    title: '确认移除',
    content: `确定要移除 ${person.personName} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用实际的API
          // await areaApi.removePersonnelFromArea(areaId.value, person.personId)

          uni.showToast({
            title: '移除成功',
            icon: 'success'
          })
          loadPersonnelList(true)
          loadAreaInfo()
        } catch (error) {
          console.error('移除人员失败:', error)
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
 * 批量移除
 */
const batchRemove = () => {
  uni.showModal({
    title: '确认批量移除',
    content: `确定要移除选中的 ${selectedPersonnel.value.length} 名人员吗？`,
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '移除中...',
          mask: true
        })

        try {
          // TODO: 调用实际的API
          // await areaApi.batchRemovePersonnel(areaId.value, selectedPersonnel.value)

          setTimeout(() => {
            uni.hideLoading()
            uni.showToast({
              title: '移除成功',
              icon: 'success'
            })
            batchMode.value = false
            selectedPersonnel.value = []
            allSelected.value = false
            loadPersonnelList(true)
            loadAreaInfo()
          }, 500)
        } catch (error) {
          uni.hideLoading()
          console.error('批量移除失败:', error)
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
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.area-personnel-page {
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
      width: 80rpx;
      height: 80rpx;
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

    .area-status {
      padding: 8rpx 16rpx;
      border-radius: 12rpx;
      font-size: 12px;
      background-color: rgba(255, 255, 255, 0.2);
      color: #ffffff;

      &.online {
        background-color: rgba(82, 196, 26, 0.9);
      }
    }
  }

  .area-stats {
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

// 快捷操作栏
.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 30rpx;

  .action-btn {
    flex: 1;
    min-width: 140rpx;
    height: 64rpx;
    background-color: #ffffff;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

    .action-text {
      font-size: 14px;
      color: #667eea;
    }

    &.danger {
      .action-text {
        color: #ff4d4f;
      }
    }
  }
}

// 筛选标签
.filter-tags {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
  overflow-x: auto;

  .filter-tag {
    padding: 12rpx 24rpx;
    background-color: #ffffff;
    border-radius: 20rpx;
    font-size: 13px;
    color: #666;
    white-space: nowrap;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #ffffff;
      font-weight: 500;
    }
  }
}

// 人员列表
.personnel-list {
  height: calc(100vh - 500rpx);

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

  .personnel-card {
    background-color: #ffffff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    display: flex;
    align-items: center;
    position: relative;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

    .select-mode {
      margin-right: 20rpx;
    }

    .personnel-avatar {
      position: relative;
      margin-right: 20rpx;

      .avatar-image {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        background-color: #f5f5f5;
      }

      .avatar-status {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        border: 2px solid #ffffff;
        background-color: #d9d9d9;

        &.online {
          background-color: #52c41a;
        }
      }
    }

    .personnel-info {
      flex: 1;
      overflow: hidden;

      .personnel-header {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;

        .personnel-name {
          font-size: 16px;
          font-weight: 500;
          color: #333;
          margin-right: 16rpx;
        }

        .permission-badge {
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
          font-size: 11px;
          font-weight: 500;

          &.permission-full {
            background-color: rgba(82, 196, 26, 0.1);
            color: #52c41a;
          }

          &.permission-limited {
            background-color: rgba(250, 173, 20, 0.1);
            color: #faad14;
          }

          &.permission-temporary {
            background-color: rgba(102, 126, 234, 0.1);
            color: #667eea;
          }
        }
      }

      .personnel-meta {
        display: flex;
        align-items: center;
        margin-bottom: 8rpx;

        .meta-text {
          font-size: 13px;
          color: #666;
        }

        .meta-divider {
          margin: 0 12rpx;
          color: #d9d9d9;
        }
      }

      .personnel-time {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .time-text {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .personnel-actions {
      display: flex;
      gap: 20rpx;

      .action-icon {
        width: 56rpx;
        height: 56rpx;
        border-radius: 50%;
        background-color: #f5f5f5;
        display: flex;
        align-items: center;
        justify-content: center;
      }
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

// 弹窗容器
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

// 搜索框
.search-box {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  margin-bottom: 30rpx;

  .search-input {
    flex: 1;
    font-size: 14px;
    color: #333;

    &::placeholder {
      color: #999;
    }
  }
}

// 可选人员列表
.available-personnel-list {
  max-height: 400rpx;
  margin-bottom: 30rpx;

  .available-personnel-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .personnel-select {
      margin-right: 20rpx;
    }

    .personnel-thumb {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background-color: #f5f5f5;
      margin-right: 20rpx;
    }

    .personnel-details {
      flex: 1;

      .personnel-name-line {
        display: flex;
        align-items: center;
        margin-bottom: 8rpx;

        .personnel-name {
          font-size: 15px;
          font-weight: 500;
          color: #333;
          margin-right: 16rpx;
        }

        .personnel-code {
          font-size: 12px;
          color: #999;
        }
      }

      .personnel-dept {
        font-size: 13px;
        color: #666;
      }
    }
  }

  .empty-search {
    padding: 60rpx 0;
    text-align: center;

    .empty-text {
      font-size: 14px;
      color: #999;
    }
  }
}

// 权限配置
.permission-config {
  .config-title {
    font-size: 15px;
    font-weight: 500;
    color: #333;
    margin-bottom: 20rpx;
  }

  .form-item {
    margin-bottom: 30rpx;

    .form-label {
      font-size: 14px;
      color: #333;
      margin-bottom: 16rpx;
    }

    .permission-types {
      display: flex;
      gap: 16rpx;

      .type-option {
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

    .date-picker {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 64rpx;
      padding: 0 24rpx;
      background-color: #f5f5f5;
      border-radius: 12rpx;
      margin-bottom: 16rpx;
      font-size: 14px;

      .placeholder {
        color: #999;
      }
    }
  }
}

// 自动分配选项
.assign-options {
  .assign-option {
    display: flex;
    align-items: center;
    padding: 30rpx;
    background-color: #f5f5f5;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .option-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 16rpx;
      background-color: rgba(102, 126, 234, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
    }

    .option-info {
      flex: 1;

      .option-title {
        font-size: 16px;
        font-weight: 500;
        color: #333;
        margin-bottom: 8rpx;
      }

      .option-desc {
        font-size: 13px;
        color: #999;
      }
    }
  }
}
</style>
