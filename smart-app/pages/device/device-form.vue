<template>
  <view class="device-form">
    <!-- 表单头部 -->
    <view class="form-header">
      <text class="form-title">{{ isEdit ? '编辑设备' : '新增设备' }}</text>
    </view>

    <!-- 表单内容 -->
    <scroll-view scroll-y class="form-scroll">
      <view class="form-section">
        <!-- 基本信息 -->
        <view class="section-title">基本信息</view>

        <view class="form-item">
          <text class="item-label">设备编码 <text class="required">*</text></text>
          <uni-easyinput
            v-model="formData.deviceCode"
            placeholder="请输入设备编码"
            :disabled="isEdit"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">设备名称 <text class="required">*</text></text>
          <uni-easyinput
            v-model="formData.deviceName"
            placeholder="请输入设备名称"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">设备类型 <text class="required">*</text></text>
          <uni-data-select
            v-model="formData.deviceType"
            :localdata="deviceTypeOptions"
            placeholder="请选择设备类型"
            :disabled="isEdit"
          />
        </view>

        <view class="form-item">
          <text class="item-label">设备品牌</text>
          <uni-easyinput
            v-model="formData.deviceBrand"
            placeholder="请输入设备品牌"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">设备型号</text>
          <uni-easyinput
            v-model="formData.deviceModel"
            placeholder="请输入设备型号"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">设备序列号</text>
          <uni-easyinput
            v-model="formData.deviceSerial"
            placeholder="请输入设备序列号"
            :styles="inputStyles"
          />
        </view>
      </view>

      <view class="form-section">
        <!-- 区域信息 -->
        <view class="section-title">区域信息</view>

        <view class="form-item">
          <text class="item-label">所属区域ID</text>
          <uni-easyinput
            v-model="formData.areaId"
            type="number"
            placeholder="请输入所属区域ID"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">所属区域名称</text>
          <uni-easyinput
            v-model="formData.areaName"
            placeholder="请输入所属区域名称"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">位置描述</text>
          <uni-easyinput
            v-model="formData.locationDesc"
            type="textarea"
            placeholder="请输入位置描述"
            :styles="textareaStyles"
            maxlength="500"
          />
        </view>
      </view>

      <view class="form-section">
        <!-- 网络信息 -->
        <view class="section-title">网络信息</view>

        <view class="form-item">
          <text class="item-label">IP地址</text>
          <uni-easyinput
            v-model="formData.ipAddress"
            placeholder="请输入IP地址"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">端口号</text>
          <uni-easyinput
            v-model="formData.port"
            type="number"
            placeholder="请输入端口号"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">MAC地址</text>
          <uni-easyinput
            v-model="formData.macAddress"
            placeholder="请输入MAC地址"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">通信协议</text>
          <uni-data-select
            v-model="formData.protocolType"
            :localdata="protocolTypeOptions"
            placeholder="请选择通信协议"
          />
        </view>
      </view>

      <view class="form-section">
        <!-- 联系信息 -->
        <view class="section-title">联系信息</view>

        <view class="form-item">
          <text class="item-label">联系人</text>
          <uni-easyinput
            v-model="formData.contactPerson"
            placeholder="请输入联系人"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">联系电话</text>
          <uni-easyinput
            v-model="formData.contactPhone"
            placeholder="请输入联系电话"
            :styles="inputStyles"
          />
        </view>

        <view class="form-item">
          <text class="item-label">供应商信息</text>
          <uni-easyinput
            v-model="formData.vendorInfo"
            type="textarea"
            placeholder="请输入供应商信息"
            :styles="textareaStyles"
            maxlength="500"
          />
        </view>
      </view>

      <view class="form-section">
        <!-- 时间信息 -->
        <view class="section-title">时间信息</view>

        <view class="form-item">
          <text class="item-label">安装时间</text>
          <uni-datetime-picker
            v-model="formData.installTime"
            type="datetime"
            placeholder="请选择安装时间"
          />
        </view>

        <view class="form-item">
          <text class="item-label">保修到期时间</text>
          <uni-datetime-picker
            v-model="formData.warrantyEndTime"
            type="datetime"
            placeholder="请选择保修到期时间"
          />
        </view>
      </view>

      <view class="form-section">
        <!-- 配置信息 -->
        <view class="section-title">配置信息</view>

        <view class="form-item">
          <text class="item-label">设备配置</text>
          <uni-easyinput
            v-model="configJsonText"
            type="textarea"
            placeholder="请输入设备配置（JSON格式）"
            :styles="textareaStyles"
            @blur="handleConfigJsonBlur"
          />
          <view v-if="configJsonError" class="error-text">
            配置格式不正确：{{ configJsonError }}
          </view>
        </view>

        <view class="form-item">
          <text class="item-label">扩展信息</text>
          <uni-easyinput
            v-model="extendInfoText"
            type="textarea"
            placeholder="请输入扩展信息（JSON格式）"
            :styles="textareaStyles"
            @blur="handleExtendInfoBlur"
          />
          <view v-if="extendInfoError" class="error-text">
            扩展信息格式不正确：{{ extendInfoError }}
          </view>
        </view>
      </view>

      <!-- 底部占位 -->
      <view style="height: 120rpx;"></view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="form-footer">
      <button class="submit-btn" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '更新' : '保存' }}
      </button>
    </view>
  </view>
</template>

<script>
import { deviceApi } from '@/api/device/deviceApi'

export default {
  data() {
    return {
      submitting: false,
      isEdit: false,
      deviceId: null,

      // 表单数据
      formData: {
        deviceId: null,
        deviceCode: '',
        deviceName: '',
        deviceType: '',
        deviceBrand: '',
        deviceModel: '',
        deviceSerial: '',
        areaId: null,
        areaName: '',
        locationDesc: '',
        ipAddress: '',
        port: null,
        macAddress: '',
        protocolType: '',
        isEnabled: 1,
        vendorInfo: '',
        contactPerson: '',
        contactPhone: '',
        installTime: null,
        warrantyEndTime: null,
        configJson: null,
        extendInfo: null
      },

      // JSON文本
      configJsonText: '',
      extendInfoText: '',
      configJsonError: '',
      extendInfoError: '',

      // 选项数据
      deviceTypeOptions: [
        { value: 'CAMERA', text: '摄像头' },
        { value: 'ACCESS_CONTROLLER', text: '门禁控制器' },
        { value: 'CONSUMPTION_TERMINAL', text: '消费终端' },
        { value: 'ATTENDANCE_MACHINE', text: '考勤机' },
        { value: 'ALARM_DEVICE', text: '报警器' },
        { value: 'INTERCOM', text: '对讲机' },
        { value: 'LED_SCREEN', text: 'LED屏' },
        { value: 'DOOR_MAGNET', text: '门磁' }
      ],

      protocolTypeOptions: [
        { value: 'TCP', text: 'TCP' },
        { value: 'UDP', text: 'UDP' },
        { value: 'HTTP', text: 'HTTP' },
        { value: 'WEBSOCKET', text: 'WebSocket' },
        { value: 'MODBUS', text: 'MODBUS' }
      ],

      // 输入框样式
      inputStyles: {
        backgroundColor: '#fff',
        borderRadius: '12rpx',
        padding: '24rpx'
      },

      textareaStyles: {
        backgroundColor: '#fff',
        borderRadius: '12rpx',
        padding: '24rpx',
        height: '120rpx'
      }
    }
  },

  computed: {
    configJsonValue: {
      get() {
        return this.configJsonText
      },
      set(value) {
        this.configJsonText = value
        this.handleConfigJsonBlur()
      }
    },

    extendInfoValue: {
      get() {
        return this.extendInfoText
      },
      set(value) {
        this.extendInfoText = value
        this.handleExtendInfoBlur()
      }
    }
  },

  onLoad(options) {
    if (options.id) {
      this.isEdit = true
      this.deviceId = options.id
      this.loadDeviceDetail()
    }
  },

  methods: {
    // 加载设备详情
    async loadDeviceDetail() {
      try {
        const response = await deviceApi.getDetail(this.deviceId)
        Object.assign(this.formData, response.data)

        // 转换JSON字段为文本
        if (response.data.configJson) {
          try {
            this.configJsonText = JSON.stringify(response.data.configJson, null, 2)
          } catch (error) {
            this.configJsonText = ''
          }
        }

        if (response.data.extendInfo) {
          try {
            this.extendInfoText = JSON.stringify(response.data.extendInfo, null, 2)
          } catch (error) {
            this.extendInfoText = ''
          }
        }
      } catch (error) {
        console.error('加载设备详情失败:', error)
        uni.showToast({
          title: '加载设备详情失败',
          icon: 'error'
        })
      }
    },

    // 处理配置JSON失去焦点
    handleConfigJsonBlur() {
      try {
        if (this.configJsonText) {
          this.formData.configJson = JSON.parse(this.configJsonText)
          this.configJsonError = ''
        } else {
          this.formData.configJson = null
        }
      } catch (error) {
        this.configJsonError = 'JSON格式错误'
      }
    },

    // 处理扩展信息失去焦点
    handleExtendInfoBlur() {
      try {
        if (this.extendInfoText) {
          this.formData.extendInfo = JSON.parse(this.extendInfoText)
          this.extendInfoError = ''
        } else {
          this.formData.extendInfo = null
        }
      } catch (error) {
        this.extendInfoError = 'JSON格式错误'
      }
    },

    // 验证表单
    validateForm() {
      if (!this.formData.deviceCode.trim()) {
        uni.showToast({
          title: '请输入设备编码',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.deviceName.trim()) {
        uni.showToast({
          title: '请输入设备名称',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.deviceType) {
        uni.showToast({
          title: '请选择设备类型',
          icon: 'none'
        })
        return false
      }

      if (this.configJsonError) {
        uni.showToast({
          title: '设备配置格式错误',
          icon: 'none'
        })
        return false
      }

      if (this.extendInfoError) {
        uni.showToast({
          title: '扩展信息格式错误',
          icon: 'none'
        })
        return false
      }

      return true
    },

    // 提交表单
    async handleSubmit() {
      if (!this.validateForm()) {
        return
      }

      try {
        this.submitting = true

        const submitData = { ...this.formData }

        if (this.isEdit) {
          await deviceApi.update(submitData)
          uni.showToast({
            title: '更新成功',
            icon: 'success'
          })
        } else {
          await deviceApi.add(submitData)
          uni.showToast({
            title: '新增成功',
            icon: 'success'
          })
        }

        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      } catch (error) {
        console.error('提交失败:', error)
        uni.showToast({
          title: '提交失败',
          icon: 'error'
        })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.device-form {
  min-height: 100vh;
  background-color: #f5f5f5;

  .form-header {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
    padding: 60rpx 40rpx 40rpx;
    color: #fff;

    .form-title {
      font-size: 36rpx;
      font-weight: bold;
    }
  }

  .form-scroll {
    height: calc(100vh - 200rpx);
  }

  .form-section {
    margin: 20rpx;
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;

    .section-title {
      padding: 30rpx;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      background: #fafafa;
      border-bottom: 1px solid #f0f0f0;
    }

    .form-item {
      padding: 30rpx;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .item-label {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 16rpx;

        .required {
          color: #ff4d4f;
        }
      }

      .error-text {
        margin-top: 12rpx;
        font-size: 24rpx;
        color: #ff4d4f;
      }
    }
  }

  .form-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 20rpx;
    border-top: 1px solid #f0f0f0;
    box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.1);

    .submit-btn {
      width: 100%;
      padding: 28rpx;
      background: #1890ff;
      color: #fff;
      border: none;
      border-radius: 12rpx;
      font-size: 32rpx;
      font-weight: bold;
    }
  }
}
</style>