<template>
  <view class="access-area-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">区域管理</view>
      <view class="nav-right"></view>
    </view>

    <!-- 区域列表 -->
    <view class="area-list">
      <view
        class="area-item"
        v-for="(area, index) in areaList"
        :key="index"
        @click="viewAreaDetail(area)"
      >
        <view class="area-info">
          <text class="area-name">{{ area.areaName }}</text>
          <text class="area-type">{{ area.areaType }}</text>
        </view>
        <view class="area-stats">
          <text class="stat-item">设备: {{ area.deviceCount || 0 }}</text>
          <text class="stat-item">权限: {{ area.permissionCount || 0 }}</text>
        </view>
      </view>

      <view class="no-data" v-if="areaList.length === 0 && !loading">
        <text>暂无区域</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { permissionApi } from '@/api/business/access/access-api.js'

export default {
  name: 'AccessArea',
  setup() {
    const userStore = useUserStore()
    const areaList = ref([])
    const loading = ref(false)

    // 加载区域列表
    const loadAreas = async () => {
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
        // 通过权限API获取用户有权限的区域列表
        // 注意：后端需要添加区域列表的移动端接口，当前通过权限接口获取
        try {
          const result = await permissionApi.getUserPermissions(userId)
          if (result.success && result.data) {
            // 转换数据格式（后端需要提供区域详情接口）
            areaList.value = (result.data.allowedAreaIds || []).map((areaId) => ({
              areaId: areaId,
              areaName: `区域${areaId}`,
              areaType: '标准区域',
              deviceCount: 0,
              permissionCount: 1
            }))
          }
        } catch (error) {
          console.error('加载区域列表失败:', error)
          uni.showToast({
            title: '加载区域列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载区域列表失败:', error)
      } finally {
        loading.value = false
      }
    }

    // 查看区域详情
    const viewAreaDetail = (area) => {
      uni.showToast({ title: '详情功能开发中', icon: 'none' })
    }

    // 返回
    const goBack = () => {
      uni.navigateBack()
    }

    // 初始化
    onMounted(() => {
      loadAreas()
    })

    return {
      areaList,
      loading,
      viewAreaDetail,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.access-area-page {
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

.area-list {
  padding: 15px;

  .area-item {
    background-color: #fff;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .area-info {
      margin-bottom: 10px;

      .area-name {
        display: block;
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }

      .area-type {
        display: block;
        font-size: 12px;
        color: #999;
      }
    }

    .area-stats {
      display: flex;
      gap: 15px;

      .stat-item {
        font-size: 12px;
        color: #666;
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

