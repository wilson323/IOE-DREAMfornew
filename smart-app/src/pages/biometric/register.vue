<template>
  <view class="biometric-page">
    <view class="card">
      <view class="card-title">生物特征注册</view>
      <uni-forms ref="formRef" :modelValue="form" label-position="top" :rules="rules">
        <uni-forms-item label="员工ID" name="employeeId" required>
          <uni-easyinput v-model="form.employeeId" type="number" placeholder="请输入员工ID" />
        </uni-forms-item>
        <uni-forms-item label="员工姓名" name="employeeName" required>
          <uni-easyinput v-model="form.employeeName" placeholder="请输入员工姓名" />
        </uni-forms-item>
        <uni-forms-item label="设备ID" name="deviceId" required>
          <uni-easyinput v-model="form.deviceId" type="number" placeholder="请输入设备ID" />
        </uni-forms-item>
        <uni-forms-item label="识别类型" name="biometricType" required>
          <picker :range="typeRange" range-key="label" @change="onTypeChange">
            <view class="picker-value">
              {{ currentType.label }}
            </view>
          </picker>
        </uni-forms-item>
        <uni-forms-item label="样本采集" name="sample" required>
          <uni-file-picker
            :limit="1"
            :disable-preview="false"
            :value="sampleFile"
            @select="onSelectSample"
            @delete="onDeleteSample"
          ></uni-file-picker>
          <view class="tip">支持jpg/png，建议使用实际采集终端生成的样本文件</view>
        </uni-forms-item>
        <uni-forms-item label="定位信息" name="location">
          <view class="location-row">
            <view class="picker-value location-text">{{ locationLabel }}</view>
            <button size="mini" type="primary" @click="fetchLocation">更新定位</button>
          </view>
        </uni-forms-item>
      </uni-forms>
      <view class="actions">
        <button type="primary" class="action-btn" @click="submitRegister">提交注册</button>
      </view>
    </view>

    <view class="card" v-if="registerResult">
      <view class="card-title">注册结果</view>
      <view class="result-row">
        <text class="label">模板ID</text>
        <text class="value">{{ registerResult.templateId }}</text>
      </view>
      <view class="result-row">
        <text class="label">密钥ID</text>
        <text class="value">{{ registerResult.encryptionKeyId }}</text>
      </view>
      <view class="result-row">
        <text class="label">安全指纹</text>
        <text class="value">{{ registerResult.securityFingerprint }}</text>
      </view>
      <view class="result-row">
        <text class="label">备注</text>
        <text class="value">{{ registerResult.message }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { biometricApi } from '@/api/biometric';
import { fileApi } from '@/api/support/file-api';
import { SmartLoading, SmartToast } from '@/lib/smart-support';
import { smartSentry } from '@/lib/smart-sentry';
import { FILE_FOLDER_TYPE_ENUM } from '@/constants/support/file-const';
import { BIOMETRIC_TYPE_OPTIONS } from '@/constants/biometric-const';

const formRef = ref();
const sampleFile = ref([]);
const registerResult = ref(null);
const currentType = reactive({
  label: BIOMETRIC_TYPE_OPTIONS[0].label,
  value: BIOMETRIC_TYPE_OPTIONS[0].value,
});

const form = reactive({
  employeeId: '',
  employeeName: '',
  deviceId: '',
  biometricType: currentType.value,
  location: '',
});

const rules = {
  employeeId: {
    rules: [{ required: true, errorMessage: '请输入员工ID' }],
  },
  employeeName: {
    rules: [{ required: true, errorMessage: '请输入员工姓名' }],
  },
  deviceId: {
    rules: [{ required: true, errorMessage: '请输入设备ID' }],
  },
};

const capturedSample = ref(null);

const typeRange = computed(() => BIOMETRIC_TYPE_OPTIONS);
const locationLabel = computed(() => {
  if (!form.location) {
    return '未采集定位';
  }
  return form.location;
});

function onTypeChange(event) {
  const option = BIOMETRIC_TYPE_OPTIONS[event.detail.value];
  if (option) {
    currentType.value = option.value;
    currentType.label = option.label;
    form.biometricType = option.value;
  }
}

async function onSelectSample(event) {
  const files = event.tempFilePaths || [];
  if (!files.length) {
    return;
  }
  await uploadSample(files[0]);
}

function onDeleteSample() {
  sampleFile.value = [];
  capturedSample.value = null;
}

/**
 * 上传样本文件
 * @param {string} path 选择的临时路径
 */
async function uploadSample(path) {
  try {
    SmartLoading.show('上传中');
    const res = await fileApi.upload(path, FILE_FOLDER_TYPE_ENUM.COMMON.value);
    sampleFile.value = [{ url: res.data.url || res.data.fileUrl || path }];
    capturedSample.value = {
      url: res.data.url || res.data.fileUrl || path,
      fileName: res.data.originalFileName,
      size: res.data.size,
    };
  } catch (error) {
    smartSentry.captureError(error);
    SmartToast.toast('文件上传失败');
  } finally {
    SmartLoading.hide();
  }
}

async function fetchLocation() {
  try {
    const location = await uni.getLocation({ type: 'wgs84' });
    form.location = JSON.stringify({
      latitude: location.latitude,
      longitude: location.longitude,
      accuracy: location.accuracy,
      timestamp: Date.now(),
    });
    SmartToast.toast('定位更新成功');
  } catch (error) {
    smartSentry.captureError(error);
    SmartToast.toast('定位失败，请检查权限');
  }
}

/**
 * 提交注册请求
 */
async function submitRegister() {
  if (!capturedSample.value) {
    SmartToast.toast('请先采集样本');
    return;
  }
  try {
    await formRef.value.validate();
    SmartLoading.show('注册中');
    const payload = {
      employeeId: Number(form.employeeId),
      employeeName: form.employeeName,
      deviceId: Number(form.deviceId),
      biometricType: form.biometricType,
      biometricPayload: capturedSample.value.url,
      captureMetadata: JSON.stringify(capturedSample.value),
      captureChannel: 'MOBILE_APP',
      location: form.location,
    };
    const res = await biometricApi.registerTemplate(payload);
    registerResult.value = res.data;
    SmartToast.toast('注册成功');
  } catch (error) {
    smartSentry.captureError(error);
    SmartToast.toast(error?.msg || '注册失败');
  } finally {
    SmartLoading.hide();
  }
}

onLoad(() => {
  form.biometricType = currentType.value;
});
</script>

<style lang="scss" scoped>
.biometric-page {
  padding: 24rpx;
  background-color: #f5f7fb;
  min-height: 100vh;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(26, 154, 255, 0.08);
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
  color: #1a1a1a;
}

.picker-value {
  padding: 16rpx;
  border: 1px solid #ebebeb;
  border-radius: 12rpx;
  color: #333;
}

.tip {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.actions {
  margin-top: 32rpx;
}

.action-btn {
  width: 100%;
  border-radius: 12rpx;
}

.result-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  color: #333;
}

.result-row .label {
  color: #666;
}

.location-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.location-text {
  flex: 1;
  margin-right: 20rpx;
}
</style>

