<template>
  <view class="biometric-page">
    <view class="card">
      <view class="card-title">多模态认证</view>
      <uni-forms ref="formRef" :modelValue="form" label-position="top" :rules="rules">
        <uni-forms-item label="人员ID" name="personId" required>
          <uni-easyinput v-model="form.personId" type="number" placeholder="请输入人员ID" />
        </uni-forms-item>
        <uni-forms-item label="设备ID" name="deviceId" required>
          <uni-easyinput v-model="form.deviceId" type="number" placeholder="请输入设备ID" />
        </uni-forms-item>
        <uni-forms-item label="识别类型" name="biometricType" required>
          <picker :range="typeRange" range-key="label" @change="onTypeChange">
            <view class="picker-value">{{ currentType.label }}</view>
          </picker>
        </uni-forms-item>
        <uni-forms-item label="认证凭证" name="credential" required>
          <uni-easyinput
            v-model="form.credential"
            placeholder="请输入模板ID/特征编号"
          />
        </uni-forms-item>
        <uni-forms-item label="需要活体检测" name="requireLiveness">
          <switch :checked="form.requireLiveness" @change="onLivenessChange" />
        </uni-forms-item>
        <uni-forms-item label="离线令牌" name="offlineToken">
          <view class="location-row">
            <uni-easyinput v-model="form.offlineToken" placeholder="可选：填写离线令牌" />
            <button size="mini" type="primary" @click="applyOfflineToken">获取</button>
          </view>
        </uni-forms-item>
        <uni-forms-item label="定位信息" name="location">
          <view class="location-row">
            <view class="picker-value location-text">{{ locationLabel }}</view>
            <button size="mini" type="primary" @click="fetchLocation">更新定位</button>
          </view>
        </uni-forms-item>
      </uni-forms>
      <view class="actions">
        <button type="primary" class="action-btn" @click="submitVerify">发起认证</button>
      </view>
    </view>

    <view v-if="verifyResult" class="card">
      <view class="card-title">认证结果</view>
      <view class="result-row">
        <text class="label">认证状态</text>
        <text class="value" :class="{ success: verifyResult.success, fail: !verifyResult.success }">
          {{ verifyResult.success ? '通过' : '拒绝' }}
        </text>
      </view>
      <view class="result-row">
        <text class="label">置信度</text>
        <text class="value">{{ verifyResult.confidence.toFixed(4) }}</text>
      </view>
      <view class="result-row">
        <text class="label">会话ID</text>
        <text class="value">{{ verifyResult.sessionId }}</text>
      </view>
      <view class="result-row">
        <text class="label">信息</text>
        <text class="value">{{ verifyResult.message }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { biometricApi } from '@/api/biometric';
import { SmartLoading, SmartToast } from '@/lib/smart-support';
import { smartSentry } from '@/lib/smart-sentry';
import { BIOMETRIC_TYPE_OPTIONS } from '@/constants/biometric-const';

const formRef = ref();
const verifyResult = ref(null);
const currentType = reactive({
  label: BIOMETRIC_TYPE_OPTIONS[0].label,
  value: BIOMETRIC_TYPE_OPTIONS[0].value,
});

const form = reactive({
  personId: '',
  deviceId: '',
  biometricType: currentType.value,
  credential: '',
  requireLiveness: true,
  offlineToken: '',
  location: '',
});

const rules = {
  personId: {
    rules: [{ required: true, errorMessage: '请输入人员ID' }],
  },
  deviceId: {
    rules: [{ required: true, errorMessage: '请输入设备ID' }],
  },
  credential: {
    rules: [{ required: true, errorMessage: '请输入认证凭证' }],
  },
};

const typeRange = computed(() => BIOMETRIC_TYPE_OPTIONS);
const locationLabel = computed(() => (form.location ? form.location : '未采集定位'));

function onTypeChange(event) {
  const option = BIOMETRIC_TYPE_OPTIONS[event.detail.value];
  if (option) {
    currentType.value = option.value;
    currentType.label = option.label;
    form.biometricType = option.value;
  }
}

function onLivenessChange(event) {
  form.requireLiveness = event.detail.value;
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
    SmartToast.toast('定位失败');
  }
}

async function applyOfflineToken() {
  if (!form.personId || !form.deviceId) {
    SmartToast.toast('请先填写人员ID和设备ID');
    return;
  }
  try {
    SmartLoading.show('申请中');
    const res = await biometricApi.requestOfflineToken({
      personId: Number(form.personId),
      deviceId: Number(form.deviceId),
      validMinutes: 60,
    });
    form.offlineToken = res.data.accessToken;
    SmartToast.toast('离线令牌获取成功');
  } catch (error) {
    smartSentry.captureError(error);
    SmartToast.toast(error?.msg || '获取离线令牌失败');
  } finally {
    SmartLoading.hide();
  }
}

/**
 * 执行本地生物识别，可用则启用，不支持时降级
 */
async function performLocalBiometricCheck() {
  let supported = false;
  /* #ifdef APP-PLUS */
  if (plus?.fingerprint?.isSupport()) {
    supported = true;
    await new Promise((resolve, reject) => {
      plus.fingerprint.authenticate(
        () => resolve(),
        (err) => reject(err),
        { message: '请验证本机指纹后继续' },
      );
    });
  }
  /* #endif */
  if (!supported && typeof uni.startSoterAuthentication === 'function') {
    supported = true;
    await new Promise((resolve, reject) => {
      uni.startSoterAuthentication({
        requestAuthModes: ['fingerPrint', 'facial'],
        challenge: String(Date.now()),
        success: () => resolve(),
        fail: (error) => reject(error),
      });
    });
  }
  if (!supported) {
    SmartToast.toast('当前环境不支持本地生物识别，直接发起云端认证');
  }
}

async function submitVerify() {
  try {
    await formRef.value.validate();
    await performLocalBiometricCheck();
    SmartLoading.show('认证中');
    const payload = {
      personId: Number(form.personId),
      deviceId: Number(form.deviceId),
      biometricType: form.biometricType,
      credential: form.credential,
      requireLiveness: form.requireLiveness,
      offlineToken: form.offlineToken,
      location: form.location,
    };
    const res = await biometricApi.verify(payload);
    verifyResult.value = res.data;
    SmartToast.toast(res.data.success ? '认证通过' : '认证未通过');
  } catch (error) {
    if (error?.errCode === 'fingerprint cancel') {
      SmartToast.toast('已取消本地认证');
      return;
    }
    smartSentry.captureError(error);
    SmartToast.toast(error?.msg || '认证失败');
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

.result-row .value.success {
  color: #1bae85;
}

.result-row .value.fail {
  color: #ff4d4f;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.location-text {
  flex: 1;
}
</style>

