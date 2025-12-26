<template>
  <view class="area-add-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">新增区域</view>
      <view class="nav-right">
        <text class="save-btn" @click="handleSave">保存</text>
      </view>
    </view>

    <!-- 表单内容 -->
    <view class="form-container">
      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <!-- 区域名称 -->
        <view class="form-item">
          <view class="form-label required">区域名称</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入区域名称"
            v-model="formData.areaName"
            maxlength="50"
          />
          <view class="form-count">{{ formData.areaName.length }}/50</view>
        </view>

        <!-- 区域类型 -->
        <view class="form-item">
          <view class="form-label required">区域类型</view>
          <picker
            mode="selector"
            :range="areaTypes"
            range-key="label"
            :value="areaTypeIndex"
            @change="onAreaTypeChange"
          >
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.areaType }]">
                {{ getAreaTypeLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 父区域 -->
        <view class="form-item">
          <view class="form-label">父区域</view>
          <picker
            mode="selector"
            :range="parentAreaList"
            range-key="areaName"
            :value="parentAreaIndex"
            @change="onParentAreaChange"
          >
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.parentId }]">
                {{ getParentAreaLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>
      </view>

      <!-- 位置信息 -->
      <view class="form-section">
        <view class="section-title">位置信息</view>

        <!-- 楼栋 -->
        <view class="form-item" v-if="formData.areaType === 'floor' || formData.areaType === 'room'">
          <view class="form-label">所属楼栋</view>
          <picker
            mode="selector"
            :range="buildingList"
            range-key="areaName"
            :value="buildingIndex"
            @change="onBuildingChange"
          >
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.buildingId }]">
                {{ getBuildingLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 楼层 -->
        <view class="form-item" v-if="formData.areaType === 'room'">
          <view class="form-label">所属楼层</view>
          <picker
            mode="selector"
            :range="floorList"
            range-key="areaName"
            :value="floorIndex"
            @change="onFloorChange"
          >
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.floorId }]">
                {{ getFloorLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 位置描述 -->
        <view class="form-item">
          <view class="form-label">位置描述</view>
          <textarea
            class="form-textarea"
            placeholder="请输入位置描述（可选）"
            v-model="formData.location"
            maxlength="200"
          />
          <view class="form-count">{{ formData.location.length }}/200</view>
        </view>
      </view>

      <!-- 其他信息 -->
      <view class="form-section">
        <view class="section-title">其他信息</view>

        <!-- 排序序号 -->
        <view class="form-item">
          <view class="form-label">排序序号</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入排序序号（数字越小越靠前）"
            v-model="formData.sortOrder"
          />
        </view>

        <!-- 备注 -->
        <view class="form-item">
          <view class="form-label">备注</view>
          <textarea
            class="form-textarea"
            placeholder="请输入备注信息（可选）"
            v-model="formData.remark"
            maxlength="500"
          />
          <view class="form-count">{{ formData.remark.length }}/500</view>
        </view>

        <!-- 启用区域 -->
        <view class="form-item switch-item">
          <view class="switch-left">
            <text class="form-label">启用区域</text>
            <text class="form-desc">禁用的区域将无法使用</text>
          </view>
          <switch
            :checked="formData.enabled"
            color="#1890ff"
            @change="onEnabledChange"
          />
        </view>
      </view>
    </view>

    <!-- 底部提示 -->
    <view class="form-tips">
      <uni-icons type="info" size="14" color="#999"></uni-icons>
      <text class="tips-text">新增区域后，可以为该区域分配设备和管理权限</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 表单数据
const formData = ref({
  areaName: '',
  areaType: null, // building-楼宇 floor-楼层 room-房间
  parentId: null,
  buildingId: null,
  floorId: null,
  location: '',
  sortOrder: 0,
  remark: '',
  enabled: true
})

// 区域类型选项
const areaTypes = [
  { value: 'building', label: '楼宇' },
  { value: 'floor', label: '楼层' },
  { value: 'room', label: '房间' }
]
const areaTypeIndex = ref(-1)

// 父区域列表
const parentAreaList = ref([])
const parentAreaIndex = ref(-1)

// 楼栋列表
const buildingList = ref([])
const buildingIndex = ref(-1)

// 楼层列表
const floorList = ref([])
const floorIndex = ref(-1)

// 页面生命周期
onLoad((options) => {
  // 如果有 parentId 参数，表示在某个区域下新增子区域
  if (options.parentId) {
    formData.value.parentId = parseInt(options.parentId)
  }

  loadParentAreas()
})

/**
 * 加载父区域列表
 */
const loadParentAreas = async () => {
  try {
    const result = await accessApi.getAreaList({
      pageNum: 1,
      pageSize: 1000,
      enabled: 1
    })

    if (result.success && result.data) {
      parentAreaList.value = result.data.list || []
    }
  } catch (error) {
    console.error('加载父区域列表失败:', error)
  }
}

/**
 * 获取区域类型标签
 */
const getAreaTypeLabel = () => {
  if (!formData.value.areaType) {
    return '请选择区域类型'
  }
  const type = areaTypes.find(t => t.value === formData.value.areaType)
  return type ? type.label : '请选择区域类型'
}

/**
 * 区域类型变更
 */
const onAreaTypeChange = (e) => {
  const index = e.detail.value
  areaTypeIndex.value = index
  formData.value.areaType = areaTypes[index].value

  // 根据类型加载对应的父区域列表
  if (formData.value.areaType === 'floor') {
    loadBuildings()
  } else if (formData.value.areaType === 'room') {
    loadBuildings()
  }
}

/**
 * 获取父区域标签
 */
const getParentAreaLabel = () => {
  if (!formData.value.parentId) {
    return '无（作为根区域）'
  }
  const area = parentAreaList.value.find(a => a.areaId === formData.value.parentId)
  return area ? area.areaName : '请选择父区域'
}

/**
 * 父区域变更
 */
const onParentAreaChange = (e) => {
  const index = e.detail.value
  parentAreaIndex.value = index

  if (index >= 0) {
    formData.value.parentId = parentAreaList.value[index].areaId
  } else {
    formData.value.parentId = null
  }
}

/**
 * 加载楼栋列表
 */
const loadBuildings = async () => {
  try {
    const result = await accessApi.getAreaList({
      pageNum: 1,
      pageSize: 1000,
      areaType: 'building',
      enabled: 1
    })

    if (result.success && result.data) {
      buildingList.value = result.data.list || []
    }
  } catch (error) {
    console.error('加载楼栋列表失败:', error)
  }
}

/**
 * 获取楼栋标签
 */
const getBuildingLabel = () => {
  if (!formData.value.buildingId) {
    return '请选择楼栋'
  }
  const building = buildingList.value.find(b => b.areaId === formData.value.buildingId)
  return building ? building.areaName : '请选择楼栋'
}

/**
 * 楼栋变更
 */
const onBuildingChange = async (e) => {
  const index = e.detail.value
  buildingIndex.value = index
  formData.value.buildingId = buildingList.value[index].areaId

  // 如果是房间，加载楼层列表
  if (formData.value.areaType === 'room') {
    await loadFloors(formData.value.buildingId)
  }
}

/**
 * 加载楼层列表
 */
const loadFloors = async (buildingId) => {
  try {
    const result = await accessApi.getAreaList({
      pageNum: 1,
      pageSize: 1000,
      areaType: 'floor',
      parentId: buildingId,
      enabled: 1
    })

    if (result.success && result.data) {
      floorList.value = result.data.list || []
    }
  } catch (error) {
    console.error('加载楼层列表失败:', error)
  }
}

/**
 * 获取楼层标签
 */
const getFloorLabel = () => {
  if (!formData.value.floorId) {
    return '请选择楼层'
  }
  const floor = floorList.value.find(f => f.areaId === formData.value.floorId)
  return floor ? floor.areaName : '请选择楼层'
}

/**
 * 楼层变更
 */
const onFloorChange = (e) => {
  const index = e.detail.value
  floorIndex.value = index
  formData.value.floorId = floorList.value[index].areaId
}

/**
 * 启用状态变更
 */
const onEnabledChange = (e) => {
  formData.value.enabled = e.detail.value
}

/**
 * 验证表单
 */
const validateForm = () => {
  // 区域名称必填
  if (!formData.value.areaName || formData.value.areaName.trim().length === 0) {
    uni.showToast({
      title: '请输入区域名称',
      icon: 'none'
    })
    return false
  }

  if (formData.value.areaName.trim().length < 2) {
    uni.showToast({
      title: '区域名称至少2个字符',
      icon: 'none'
    })
    return false
  }

  // 区域类型必选
  if (!formData.value.areaType) {
    uni.showToast({
      title: '请选择区域类型',
      icon: 'none'
    })
    return false
  }

  // 楼层类型必须选择楼栋
  if (formData.value.areaType === 'floor' && !formData.value.buildingId) {
    uni.showToast({
      title: '请选择所属楼栋',
      icon: 'none'
    })
    return false
  }

  // 房间类型必须选择楼栋和楼层
  if (formData.value.areaType === 'room') {
    if (!formData.value.buildingId) {
      uni.showToast({
        title: '请选择所属楼栋',
        icon: 'none'
      })
      return false
    }
    if (!formData.value.floorId) {
      uni.showToast({
        title: '请选择所属楼层',
        icon: 'none'
      })
      return false
    }
  }

  return true
}

/**
 * 保存区域
 */
const handleSave = async () => {
  // 验证表单
  if (!validateForm()) {
    return
  }

  // 构造请求数据
  const data = {
    areaName: formData.value.areaName.trim(),
    areaType: formData.value.areaType,
    location: formData.value.location.trim(),
    sortOrder: formData.value.sortOrder || 0,
    remark: formData.value.remark.trim(),
    enabled: formData.value.enabled
  }

  // 设置父区域
  if (formData.value.parentId) {
    data.parentId = formData.value.parentId
  }

  // 楼层类型，设置楼栋为父区域
  if (formData.value.areaType === 'floor' && formData.value.buildingId) {
    data.parentId = formData.value.buildingId
  }

  // 房间类型，设置楼层为父区域
  if (formData.value.areaType === 'room' && formData.value.floorId) {
    data.parentId = formData.value.floorId
  }

  // 显示加载提示
  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    // 调用新增区域API
    const result = await accessApi.addArea(data)

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '新增成功',
        icon: 'success'
      })

      // 返回区域列表
      setTimeout(() => {
        uni.navigateBack()
      }, 500)
    } else {
      uni.showToast({
        title: result.message || '新增失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('新增区域失败:', error)
    uni.showToast({
      title: '新增失败，请稍后重试',
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
.area-add-page {
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

  .save-btn {
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
  margin-bottom: 15px;
  overflow: hidden;

  .section-title {
    padding: 15px;
    font-size: 16px;
    font-weight: 500;
    color: #333;
    border-bottom: 1px solid #f0f0f0;
  }

  .form-item {
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
    position: relative;

    &:last-child {
      border-bottom: none;
    }

    &.switch-item {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .switch-left {
        flex: 1;

        .form-desc {
          font-size: 12px;
          color: #999;
          margin-top: 6px;
          line-height: 1.4;
        }
      }
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
      bottom: 15px;
      right: 15px;
      font-size: 12px;
      color: #999;
    }
  }
}

// 底部提示
.form-tips {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  margin: 0 15px;

  .tips-text {
    flex: 1;
    margin-left: 8px;
    font-size: 13px;
    color: #999;
    line-height: 1.5;
  }
}
</style>
