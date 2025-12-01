<!--
 * 考勤打卡页面
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-17
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <div class="attendance-punch-container">
    <!-- 考勤状态卡片 -->
    <a-row :gutter="16" class="mb-4">
      <a-col :span="24">
        <a-card :bordered="false" class="attendance-status-card">
          <div class="status-header">
            <div class="current-time">{{ currentTime }}</div>
            <div class="current-date">{{ currentDate }}</div>
            <div class="current-weekday">{{ currentWeekday }}</div>
          </div>
          <div class="status-content">
            <div class="today-records" v-if="todayRecords.length > 0">
              <div class="record-item" v-for="(record, index) in todayRecords" :key="index">
                <div class="record-type" :class="record.punchType === '上班' ? 'punch-in' : 'punch-out'">
                  <ClockCircleOutlined />
                  {{ record.punchType }}
                </div>
                <div class="record-time">{{ record.punchTime }}</div>
                <div class="record-location" v-if="record.location">
                  <EnvironmentOutlined />
                  {{ record.location }}
                </div>
              </div>
            </div>
            <div class="no-records" v-else>
              <ClockCircleOutlined class="no-records-icon" />
              <div class="no-records-text">今日暂无打卡记录</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 打卡操作区域 -->
    <a-row :gutter="16">
      <a-col :span="16">
        <a-card :bordered="false" title="位置打卡" class="punch-card">
          <template #extra>
            <a-button type="link" @click="switchPunchType" v-if="canSwitchPunchType">
              切换为{{ nextPunchType }}打卡
            </a-button>
          </template>

          <div class="punch-content">
            <!-- 地图显示 -->
            <div class="map-container" v-if="showMap">
              <div class="map-placeholder">
                <EnvironmentOutlined class="map-icon" />
                <div class="map-text">当前位置：{{ currentLocation || '获取中...' }}</div>
                <div class="map-distance" v-if="currentDistance > 0">
                  距离公司：{{ currentDistance }}米
                  <a-tag :color="currentDistance <= maxDistance ? 'green' : 'red'">
                    {{ currentDistance <= maxDistance ? '在范围内' : '超出范围' }}
                  </a-tag>
                </div>
              </div>
            </div>

            <!-- 打卡表单 -->
            <a-form :model="punchForm" layout="vertical" class="punch-form">
              <a-form-item label="打卡类型" class="form-item-large">
                <a-tag :color="punchForm.punchType === '上班' ? 'blue' : 'green'" size="large" class="punch-type-tag">
                  {{ punchForm.punchType }}打卡
                </a-tag>
              </a-form-item>

              <a-form-item label="打卡时间" class="form-item-large">
                <a-input :value="currentTime" disabled size="large" class="time-input" />
              </a-form-item>

              <a-form-item label="设备信息" class="form-item-large" v-if="deviceInfo">
                <a-input :value="deviceInfo" disabled size="large" />
              </a-form-item>

              <a-form-item label="位置信息" class="form-item-large" v-if="currentLocation">
                <a-textarea v-model:value="punchForm.location"
                  placeholder="请输入详细位置信息"
                  :rows="2"
                  size="large" />
              </a-form-item>

              <a-form-item label="备注" class="form-item-large">
                <a-textarea v-model:value="punchForm.remark"
                  placeholder="请输入备注信息（可选）"
                  :rows="2"
                  size="large" />
              </a-form-item>

              <a-form-item class="form-item-large">
                <a-button type="primary"
                  size="large"
                  block
                  :loading="punching"
                  :disabled="!canPunch"
                  @click="handlePunch">
                  <template #icon>
                    <ClockCircleOutlined />
                  </template>
                  {{ canPunch ? '立即打卡' : punchDisabledReason }}
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-card>
      </a-col>

      <!-- 侧边栏 -->
      <a-col :span="8">
        <!-- 今日考勤统计 -->
        <a-card :bordered="false" title="今日考勤" class="statistics-card">
          <div class="statistics-item">
            <div class="statistic-label">上班打卡</div>
            <div class="statistic-value" :class="{'has-record': punchInTime}">
              {{ punchInTime || '--:--' }}
            </div>
          </div>
          <div class="statistics-item">
            <div class="statistic-label">下班打卡</div>
            <div class="statistic-value" :class="{'has-record': punchOutTime}">
              {{ punchOutTime || '--:--' }}
            </div>
          </div>
          <div class="statistics-item">
            <div class="statistic-label">工作时长</div>
            <div class="statistic-value">
              {{ workHours || '--:--' }}
            </div>
          </div>
          <div class="statistics-item">
            <div class="statistic-label">考勤状态</div>
            <div class="statistic-value">
              <a-tag :color="attendanceStatusColor" size="large">
                {{ attendanceStatus }}
              </a-tag>
            </div>
          </div>
        </a-card>

        <!-- 最近打卡记录 -->
        <a-card :bordered="false" title="最近记录" class="recent-records-card">
          <a-list :data-source="recentRecords" size="small">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: item.avatarColor }">
                      {{ item.avatarText }}
                    </a-avatar>
                  </template>
                  <template #title>
                    {{ item.punchType }} - {{ item.punchTime }}
                  </template>
                  <template #description>
                    {{ item.location }}
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 打卡成功模态框 -->
    <a-modal v-model:open="showSuccessModal"
      :title="'打卡成功'"
      :footer="null"
      :maskClosable="false"
      class="success-modal">
      <div class="success-content">
        <a-result
          status="success"
          title="打卡成功！"
          :sub-title="punchSuccessMessage"
        >
          <template #icon>
            <CheckCircleOutlined />
          </template>
        </a-result>
      </div>
    </a-modal>

    <!-- 拍照模态框 -->
    <a-modal v-model:open="showPhotoModal"
      title="拍照验证"
      :maskClosable="false"
      :footer="null"
      width="400px"
      class="photo-modal">
      <div class="photo-content">
        <div class="photo-preview">
          <video ref="videoRef"
            :width="320"
            :height="240"
            autoplay
            muted
            @loadedmetadata="handleVideoLoaded"
            @error="handleVideoError" />
        </div>
        <div class="photo-actions">
          <a-button @click="capturePhoto" :loading="capturing" block>
            <template #icon>
              <CameraOutlined />
            </template>
            拍照
          </a-button>
          <a-button @click="skipPhoto" class="mt-2" block>
            跳过拍照
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  ClockCircleOutlined,
  EnvironmentOutlined,
  CheckCircleOutlined,
  CameraOutlined
} from '@ant-design/icons-vue';
import { attendanceApi } from '/@/api/business/attendance/attendance-api';
import dayjs from 'dayjs';

// 响应式数据
const currentTime = ref('');
const currentDate = ref('');
const currentWeekday = ref('');
const currentLocation = ref('');
const currentDistance = ref(0);
const maxDistance = ref(500); // 最大允许距离
const deviceInfo = ref('');

const todayRecords = ref([]);
const recentRecords = ref([]);
const attendanceStatus = ref('');
const attendanceStatusColor = ref('');

const punchInTime = ref('');
const punchOutTime = ref('');
const workHours = ref('');

const showMap = ref(true);
const showSuccessModal = ref(false);
const showPhotoModal = ref(false);
const capturing = ref(false);
const punching = ref(false);

const videoRef = ref(null);
const capturedPhoto = ref('');

const canPunch = ref(false);
const canSwitchPunchType = ref(false);
const punchDisabledReason = ref('');

const nextPunchType = computed(() => {
  if (!punchInTime.value) return '上班';
  if (!punchOutTime.value) return '下班';
  return '';
});

const punchForm = reactive({
  punchType: '上班',
  location: '',
  remark: '',
  latitude: null,
  longitude: null,
  deviceId: '',
  photoBase64: ''
});

// 获取当前时间
const updateCurrentTime = () => {
  const now = dayjs();
  currentTime.value = now.format('HH:mm:ss');
  currentDate.value = now.format('YYYY-MM-DD');
  currentWeekday.value = now.format('dddd');
};

// 获取位置信息
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    message.warning('您的浏览器不支持地理定位');
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords;
      currentLocation.value = `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
      currentDistance.value = calculateDistance(latitude, longitude, 39.9042, 116.4074); // 示例公司坐标

      punchForm.latitude = latitude;
      punchForm.longitude = longitude;
    },
    (error) => {
      console.error('获取位置失败:', error);
      message.warning('获取位置失败，请检查位置权限');
    }
  );
};

// 计算距离（示例实现）
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371000; // 地球半径（米）
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

// 获取设备信息
const getDeviceInfo = () => {
  const userAgent = navigator.userAgent;
  const platform = navigator.platform;
  deviceInfo.value = `${platform} - ${userAgent.substring(0, 50)}...`;
  punchForm.deviceId = `${platform}-${Date.now()}`;
};

// 获取今日打卡记录
const getTodayPunchRecords = async () => {
  try {
    const result = await attendanceApi.getTodayPunchRecord();
    if (result.success && result.data) {
      todayRecords.value = result.data.records || [];
      recentRecords.value = result.data.recentRecords || [];

      // 更新打卡状态
      const punchInRecord = todayRecords.value.find(r => r.punchType === '上班');
      const punchOutRecord = todayRecords.value.find(r => r.punchType === '下班');

      punchInTime.value = punchInRecord?.punchTime || '';
      punchOutTime.value = punchOutRecord?.punchTime || '';
      attendanceStatus.value = result.data.status || '正常';
      attendanceStatusColor.value = result.data.statusColor || 'default';

      // 更新工作时长
      if (punchInTime.value && punchOutTime.value) {
        workHours.value = result.data.workHours || '';
      }

      // 更新打卡类型
      punchForm.punchType = !punchInTime.value ? '上班' : '下班';
    }
  } catch (error) {
    console.error('获取今日打卡记录失败:', error);
    message.error('获取今日打卡记录失败');
  }
};

// 检查是否可以打卡
const checkCanPunch = () => {
  // 检查是否已经打过对应类型的卡
  const hasPunchIn = todayRecords.value.some(r => r.punchType === '上班');
  const hasPunchOut = todayRecords.value.some(r => r.punchType === '下班');

  canPunch.value = true;
  canSwitchPunchType.value = hasPunchIn && !hasPunchOut;

  if (punchForm.punchType === '上班' && hasPunchIn) {
    canPunch.value = false;
    punchDisabledReason.value = '今日已打过上班卡';
  } else if (punchForm.punchType === '下班' && !hasPunchIn) {
    canPunch.value = false;
    punchDisabledReason.value = '请先打上班卡';
  } else if (punchForm.punchType === '下班' && hasPunchOut) {
    canPunch.value = false;
    punchDisabledReason.value = '今日已打过下班卡';
  }
};

// 切换打卡类型
const switchPunchType = () => {
  punchForm.punchType = nextPunchType.value;
  checkCanPunch();
};

// 拍照
const capturePhoto = () => {
  if (!videoRef.value) return;

  capturing.value = true;

  const canvas = document.createElement('canvas');
  const video = videoRef.value;
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;

  const ctx = canvas.getContext('2d');
  ctx.drawImage(video, 0, 0);

  capturedPhoto.value = canvas.toDataURL('image/jpeg');
  punchForm.photoBase64 = capturedPhoto.value;
  showPhotoModal.value = false;
  capturing.value = false;
};

// 跳过拍照
const skipPhoto = () => {
  showPhotoModal.value = false;
  punchForm.photoBase64 = '';
  handlePunch();
};

// 视频加载完成
const handleVideoLoaded = () => {
  console.log('视频加载完成');
};

// 视频加载错误
const handleVideoError = (error) => {
  console.error('视频加载失败:', error);
  message.warning('摄像头加载失败，将跳过拍照验证');
};

// 处理打卡
const handlePunch = async () => {
  punching.value = true;

  try {
    const params = {
      ...punchForm,
      punchTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
    };

    const api = punchForm.punchType === '上班' ? attendanceApi.punchIn : attendanceApi.punchOut;
    const result = await api(params);

    if (result.success) {
      punchSuccessMessage.value = `${punchForm.punchType}打卡成功！`;
      showSuccessModal.value = true;

      // 刷新打卡记录
      await getTodayPunchRecords();

      // 重置表单
      punchForm.remark = '';

      // 3秒后关闭成功模态框
      setTimeout(() => {
        showSuccessModal.value = false;
      }, 3000);
    } else {
      message.error(result.message || '打卡失败');
    }
  } catch (error) {
    console.error('打卡失败:', error);
    message.error('打卡失败，请重试');
  } finally {
    punching.value = false;
  }
};

// 定时器
let timeTimer = null;

// 组件挂载
onMounted(() => {
  updateCurrentTime();
  getCurrentLocation();
  getDeviceInfo();
  getTodayPunchRecords();
  checkCanPunch();

  // 启动时间定时器
  timeTimer = setInterval(updateCurrentTime, 1000);
});

// 组件卸载
onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer);
  }
});
</script>

<style lang="less" scoped>
.attendance-punch-container {
  padding: 16px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.attendance-status-card {
  .status-header {
    text-align: center;
    padding: 20px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 8px;
    margin-bottom: 20px;

    .current-time {
      font-size: 32px;
      font-weight: bold;
      margin-bottom: 8px;
    }

    .current-date {
      font-size: 18px;
      margin-bottom: 4px;
      opacity: 0.9;
    }

    .current-weekday {
      font-size: 16px;
      opacity: 0.8;
    }
  }

  .status-content {
    padding: 0 20px 20px;

    .record-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-type {
        display: flex;
        align-items: center;
        font-weight: 500;

        &.punch-in {
          color: #1890ff;
        }

        &.punch-out {
          color: #52c41a;
        }
      }

      .record-time {
        font-size: 16px;
        font-weight: 500;
      }

      .record-location {
        font-size: 12px;
        color: #666;
      }
    }

    .no-records {
      text-align: center;
      padding: 40px 0;
      color: #999;

      .no-records-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }

      .no-records-text {
        font-size: 16px;
      }
    }
  }
}

.punch-card {
  .punch-content {
    .map-container {
      margin-bottom: 20px;

      .map-placeholder {
        text-align: center;
        padding: 30px;
        background: #f8f9fa;
        border: 1px dashed #d9d9d9;
        border-radius: 8px;

        .map-icon {
          font-size: 32px;
          color: #1890ff;
          margin-bottom: 8px;
        }

        .map-text {
          font-size: 16px;
          color: #666;
          margin-bottom: 12px;
        }

        .map-distance {
          font-size: 14px;
          color: #666;
        }
      }
    }

    .punch-form {
      .form-item-large {
        margin-bottom: 24px;

        .punch-type-tag {
          font-size: 16px;
          padding: 8px 16px;
        }

        .time-input {
          text-align: center;
          font-size: 18px;
          font-weight: bold;
        }
      }
    }
  }
}

.statistics-card {
  margin-bottom: 16px;

  .statistics-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .statistic-label {
      color: #666;
      font-size: 14px;
    }

    .statistic-value {
      font-size: 16px;
      font-weight: 500;

      &.has-record {
        color: #52c41a;
      }
    }
  }
}

.recent-records-card {
  :deep(.ant-list-item-meta-title) {
    font-weight: 500;
  }
}

.success-modal {
  :deep(.ant-result-icon) {
    font-size: 48px;
  }
}

.photo-modal {
  .photo-content {
    .photo-preview {
      text-align: center;
      margin-bottom: 16px;

      video {
        border-radius: 8px;
      }
    }

    .photo-actions {
      .mt-2 {
        margin-top: 8px;
      }
    }
  }
}
</style>