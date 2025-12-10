<template>
  <view class="access-permission-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">权限管理</view>
      <view class="nav-right"></view>
    </view>

    <!-- 权限列表 -->
    <view class="permission-list">
      <view
        class="permission-item"
        v-for="(permission, index) in permissionList"
        :key="index"
      >
        <view class="permission-info">
          <text class="area-name">{{ permission.areaName }}</text>
          <text class="permission-type">{{ permission.permissionType }}</text>
        </view>
        <view class="permission-status">
          <text :class="['status-tag', permission.active ? 'active' : 'inactive']">
            {{ permission.active ? '有效' : '失效' }}
          </text>
        </view>
      </view>

      <view class="no-data" v-if="permissionList.length === 0 && !loading">
        <text>暂无权限</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { permissionApi } from '@/api/business/access/access-api.js'

// 响应式数据
const userStore = useUserStore()
const permissionList = ref([])
const loading = ref(false)

// 页面生命周期
onMounted(() => {
  loadPermissions()
})

onShow(() => {
  // 页面显示时可以刷新数据
})

onPullDownRefresh(() => {
  loadPermissions()
  uni.stopPullDownRefresh()
})

// 方法实现
const loadPermissions = async () => {
  loading.value = true
  try {
    // 从用户store获取用户ID
    const userId = userStore.employeeId
    if (!userId) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      loading.value = false
      return
    }
    // 调用权限列表API
    const result = await permissionApi.getUserPermissions(userId)
    if (result.success && result.data) {
      // 转换数据格式
      permissionList.value = (result.data.allowedAreaIds || []).map((areaId, index) => ({
        areaId: areaId,
        areaName: `区域${areaId}`,
        permissionType: result.data.permissionLevel || '标准权限',
        active: true
      }))
    }
  } catch (error) {
    console.error('加载权限列表失败:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.access-permission-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
  }

  .icon-back {
    font-size: 20px;
    color: #333;
  }
}

.permission-list {
  padding: 15px;

  .permission-item {
    background-color: #fff;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .permission-info {
      flex: 1;

      .area-name {
        display: block;
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }

      .permission-type {
        display: block;
        font-size: 12px;
        color: #999;
      }
    }

    .permission-status {
      .status-tag {
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;

        &.active {
          background-color: #f6ffed;
          color: #52c41a;
        }

        &.inactive {
          background-color: #fff2f0;
          color: #ff4d4f;
        }
      }
    }
  }

  .no-data {
    text-align: center;
    padding: 40px 0;
    color: #999;
    font-size: 14px;
  }
}
</style>

