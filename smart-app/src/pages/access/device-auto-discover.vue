<template>
  <view class="device-auto-discover-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">设备自动发现</view>
      <view class="nav-right" @click="showHistory">
        <uni-icons type="list" size="20" color="#1890ff"></uni-icons>
      </view>
    </view>

    <!-- 发现配置卡片 -->
    <view class="config-card">
      <view class="card-title">
        <uni-icons type="settings" size="18" color="#1890ff"></uni-icons>
        <text class="title-text">发现配置</text>
      </view>

      <!-- 子网地址 -->
      <view class="form-item">
        <view class="form-label required">子网地址</view>
        <input
          class="form-input"
          v-model="discoveryForm.subnet"
          placeholder="例如: 192.168.1.0/24"
          :disabled="isScanning"
        />
        <view class="form-hint">支持CIDR格式，例如 192.168.1.0/24</view>
      </view>

      <!-- 超时时间 -->
      <view class="form-item">
        <view class="form-label">超时时间（秒）</view>
        <slider
          class="form-slider"
          :value="discoveryForm.timeout"
          :min="30"
          :max="600"
          :step="30"
          :disabled="isScanning"
          @change="onTimeoutChange"
          activeColor="#1890ff"
          show-value
        />
      </view>

      <!-- 发现协议 -->
      <view class="form-item">
        <view class="form-label">发现协议</view>
        <view class="protocol-list">
          <view
            class="protocol-item"
            :class="{ active: discoveryForm.protocols.includes('SSDP'), disabled: isScanning }"
            @click="toggleProtocol('SSDP')"
          >
            <view class="protocol-icon">
              <uni-icons type="wifi" size="20" color="#1890ff"></uni-icons>
            </view>
            <text class="protocol-name">SSDP</text>
            <text class="protocol-desc">通用设备</text>
            <uni-icons
              v-if="discoveryForm.protocols.includes('SSDP')"
              type="checkmarkempty"
              size="18"
              color="#1890ff"
            ></uni-icons>
          </view>

          <view
            class="protocol-item"
            :class="{ active: discoveryForm.protocols.includes('ONVIF'), disabled: isScanning }"
            @click="toggleProtocol('ONVIF')"
          >
            <view class="protocol-icon">
              <uni-icons type="videocam" size="20" color="#52c41a"></uni-icons>
            </view>
            <text class="protocol-name">ONVIF</text>
            <text class="protocol-desc">网络摄像头</text>
            <uni-icons
              v-if="discoveryForm.protocols.includes('ONVIF')"
              type="checkmarkempty"
              size="18"
              color="#1890ff"
            ></uni-icons>
          </view>

          <view
            class="protocol-item"
            :class="{ active: discoveryForm.protocols.includes('SNMP'), disabled: isScanning }"
            @click="toggleProtocol('SNMP')"
          >
            <view class="protocol-icon">
              <uni-icons type="loop" size="20" color="#faad14"></uni-icons>
            </view>
            <text class="protocol-name">SNMP</text>
            <text class="protocol-desc">网络设备</text>
            <uni-icons
              v-if="discoveryForm.protocols.includes('SNMP')"
              type="checkmarkempty"
              size="18"
              color="#1890ff"
            ></uni-icons>
          </view>

          <view
            class="protocol-item"
            :class="{ active: discoveryForm.protocols.includes('PRIVATE'), disabled: isScanning }"
            @click="toggleProtocol('PRIVATE')"
          >
            <view class="protocol-icon">
              <uni-icons type="locked" size="20" color="#722ed1"></uni-icons>
            </view>
            <text class="protocol-name">私有协议</text>
            <text class="protocol-desc">门禁/考勤</text>
            <uni-icons
              v-if="discoveryForm.protocols.includes('PRIVATE')"
              type="checkmarkempty"
              size="18"
              color="#1890ff"
            ></uni-icons>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button
          class="action-btn primary"
          :loading="isScanning"
          :disabled="!canStartDiscovery"
          @click="handleStartDiscovery"
        >
          <uni-icons type="radiobox" size="16" color="#fff"></uni-icons>
          <text>{{ isScanning ? '扫描中...' : '开始发现' }}</text>
        </button>
        <button
          v-if="isScanning"
          class="action-btn danger"
          @click="handleCancelDiscovery"
        >
          <uni-icons type="close" size="16" color="#fff"></uni-icons>
          <text>停止扫描</text>
        </button>
        <button
          v-if="discoveredDevices.length > 0"
          class="action-btn"
          @click="handleReset"
        >
          <uni-icons type="refresh" size="16" color="#666"></uni-icons>
          <text>重置</text>
        </button>
      </view>
    </view>

    <!-- 扫描进度卡片 -->
    <view v-if="scanProgress.scanId" class="progress-card">
      <view class="card-title">
        <uni-icons type="refresh" :spin="isScanning" size="18" color="#1890ff"></uni-icons>
        <text class="title-text">扫描进度</text>
        <view class="status-badge" :class="scanProgress.status.toLowerCase()">
          {{ getStatusText(scanProgress.status) }}
        </view>
      </view>

      <view class="progress-info">
        <view class="progress-stats">
          <view class="stat-item">
            <text class="stat-value">{{ scanProgress.discoveredDevices || 0 }}</text>
            <text class="stat-label">已发现</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ scanProgress.totalDevices || 0 }}</text>
            <text class="stat-label">总设备</text>
          </view>
        </view>

        <!-- 进度条 -->
        <view class="progress-bar-wrapper">
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: scanProgress.progress + '%' }"
            ></view>
          </view>
          <text class="progress-percent">{{ scanProgress.progress }}%</text>
        </view>

        <view class="scan-id">扫描ID: {{ scanProgress.scanId }}</view>
      </view>
    </view>

    <!-- 发现的设备列表 -->
    <view v-if="discoveredDevices.length > 0" class="devices-section">
      <view class="section-header">
        <view class="header-left">
          <uni-icons type="list" size="18" color="#1890ff"></uni-icons>
          <text class="header-title">发现的设备 ({{ discoveredDevices.length }})</text>
        </view>
        <view class="header-actions">
          <view class="action-link" @click="toggleSelectMode" v-if="!selectMode">
            <text>批量操作</text>
          </view>
          <view class="action-link" @click="handleExport" v-else>
            <text>导出</text>
          </view>
        </view>
      </view>

      <!-- 批量操作栏 -->
      <view v-if="selectMode" class="batch-actions-bar">
        <view class="batch-info">
          <text>已选择 {{ selectedDevices.length }} 台</text>
        </view>
        <view class="batch-buttons">
          <view class="batch-btn" @click="selectAllNew">
            <text>全选新设备</text>
          </view>
          <view class="batch-btn primary" @click="handleBatchAdd">
            <text>批量添加</text>
          </view>
        </view>
      </view>

      <!-- 设备列表 -->
      <scroll-view class="device-scroll" scroll-y>
        <view
          class="device-card"
          v-for="(device, index) in discoveredDevices"
          :key="device.ipAddress || index"
        >
          <!-- 选择复选框 -->
          <view class="device-checkbox" v-if="selectMode" @click.stop="toggleSelectDevice(device)">
            <uni-icons
              :type="isDeviceSelected(device) ? 'checkbox-filled' : 'circle'"
              :color="isDeviceSelected(device) ? '#1890ff' : '#d9d9d9'"
              size="20"
            ></uni-icons>
          </view>

          <view class="device-content" @click="handleDeviceClick(device)">
            <!-- 设备图标和信息 -->
            <view class="device-main">
              <view class="device-icon" :class="'type-' + device.deviceType">
                <uni-icons type="settings" size="24" color="#fff"></uni-icons>
              </view>
              <view class="device-info">
                <view class="device-name">
                  {{ device.deviceName || (device.deviceBrand + ' ' + device.deviceModel) }}
                </view>
                <view class="device-meta">
                  <text class="device-ip">{{ device.ipAddress }}:{{ device.port }}</text>
                  <view class="device-status" :class="'status-' + device.deviceStatus">
                    {{ getDeviceStatusText(device.deviceStatus) }}
                  </view>
                </view>
              </view>
            </view>

            <!-- 设备标签 -->
            <view class="device-tags">
              <view class="tag protocol">{{ device.protocol }}</view>
              <view class="tag type" :class="'type-' + device.deviceType">
                {{ getDeviceTypeName(device.deviceType) }}
              </view>
              <view class="tag" :class="device.existsInSystem ? 'exists' : 'new'">
                {{ device.existsInSystem ? '已存在' : '新设备' }}
              </view>
            </view>

            <!-- 快捷操作 -->
            <view class="device-actions">
              <view
                class="action-btn-icon"
                :class="{ disabled: device.existsInSystem }"
                @click.stop="handleAddSingle(device)"
              >
                <uni-icons type="plus" size="18" :color="device.existsInSystem ? '#d9d9d9' : '#52c41a'"></uni-icons>
              </view>
              <view class="action-btn-icon" @click.stop="handleViewDetail(device)">
                <uni-icons type="eye" size="18" color="#1890ff"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!scanProgress.scanId" class="empty-state">
      <image class="empty-icon" src="/static/images/empty-device.png" mode="aspectFit"></image>
      <text class="empty-text">配置发现参数后点击"开始发现"</text>
    </view>

    <!-- 设备详情弹窗 -->
    <uni-popup ref="detailPopup" type="bottom" :safe-area="false">
      <view class="detail-popup">
        <view class="detail-header">
          <text class="detail-title">设备详细信息</text>
          <view class="detail-close" @click="closeDetail">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <scroll-view class="detail-content" scroll-y>
          <view class="detail-section">
            <view class="detail-item">
              <text class="detail-label">设备名称</text>
              <text class="detail-value">{{ currentDetail.deviceName || '-' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">IP地址</text>
              <text class="detail-value primary">{{ currentDetail.ipAddress }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">端口</text>
              <text class="detail-value">{{ currentDetail.port }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">MAC地址</text>
              <text class="detail-value">{{ currentDetail.macAddress || '-' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">设备类型</text>
              <text class="detail-value">{{ getDeviceTypeName(currentDetail.deviceType) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">设备型号</text>
              <text class="detail-value">{{ currentDetail.deviceModel || '-' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">设备厂商</text>
              <text class="detail-value">{{ currentDetail.deviceBrand || '-' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">固件版本</text>
              <text class="detail-value">{{ currentDetail.firmwareVersion || '-' }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">发现协议</text>
              <view class="tag protocol">{{ currentDetail.protocol }}</view>
            </view>
            <view class="detail-item">
              <text class="detail-label">设备位置</text>
              <text class="detail-value">{{ currentDetail.location || '-' }}</text>
            </view>
          </view>

          <view class="detail-section" v-if="currentDetail.deviceInfo">
            <view class="section-title">设备信息</view>
            <text class="info-json">{{ formatDeviceInfo(currentDetail.deviceInfo) }}</text>
          </view>
        </scroll-view>

        <view class="detail-footer" v-if="!currentDetail.existsInSystem">
          <button class="footer-btn primary" @click="handleAddFromDetail">添加设备</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
export default {
  data() {
    return {
      // 发现表单
      discoveryForm: {
        subnet: '192.168.1.0/24',
        timeout: 180,
        protocols: ['SSDP', 'ONVIF', 'SNMP', 'PRIVATE'],
      },

      // 扫描进度
      scanProgress: {
        scanId: null,
        status: null,
        progress: 0,
        totalDevices: 0,
        discoveredDevices: [],
      },

      // 发现的设备
      discoveredDevices: [],
      selectedDevices: [],

      // UI状态
      isScanning: false,
      selectMode: false,
      progressTimer: null,

      // 当前查看的设备
      currentDetail: {},
    };
  },

  computed: {
    canStartDiscovery() {
      return this.discoveryForm.subnet &&
             this.discoveryForm.protocols.length > 0 &&
             !this.isScanning;
    },
  },

  onUnload() {
    this.stopProgressPolling();
  },

  methods: {
    // 返回
    goBack() {
      uni.navigateBack();
    },

    // 查看历史
    showHistory() {
      uni.showToast({ title: '功能开发中', icon: 'none' });
    },

    // 超时时间变化
    onTimeoutChange(e) {
      this.discoveryForm.timeout = e.detail.value;
    },

    // 切换协议选择
    toggleProtocol(protocol) {
      if (this.isScanning) return;

      const index = this.discoveryForm.protocols.indexOf(protocol);
      if (index > -1) {
        this.discoveryForm.protocols.splice(index, 1);
      } else {
        this.discoveryForm.protocols.push(protocol);
      }
    },

    // 开始设备发现
    async handleStartDiscovery() {
      if (!this.discoveryForm.subnet) {
        uni.showToast({ title: '请输入子网地址', icon: 'none' });
        return;
      }
      if (this.discoveryForm.protocols.length === 0) {
        uni.showToast({ title: '请至少选择一种发现协议', icon: 'none' });
        return;
      }

      try {
        this.isScanning = true;
        this.discoveredDevices = [];
        this.selectedDevices = [];
        this.scanProgress.scanId = null;
        this.scanProgress.progress = 0;

        const result = await this.$http.post('/api/v1/access/device-discovery/start', {
          subnet: this.discoveryForm.subnet,
          protocols: this.discoveryForm.protocols,
          timeout: this.discoveryForm.timeout,
        });

        if (result.code === 200 && result.data) {
          this.scanProgress.scanId = result.data.scanId;
          this.scanProgress.status = result.data.status;
          uni.showToast({ title: '设备发现已启动', icon: 'success' });

          // 开始轮询进度
          this.startProgressPolling();
        } else {
          uni.showToast({ title: result.message || '启动失败', icon: 'none' });
          this.isScanning = false;
        }
      } catch (error) {
        console.error('启动设备发现失败', error);
        uni.showToast({ title: '启动失败', icon: 'none' });
        this.isScanning = false;
      }
    },

    // 轮询扫描进度
    startProgressPolling() {
      if (this.progressTimer) {
        clearInterval(this.progressTimer);
      }

      this.progressTimer = setInterval(async () => {
        try {
          const result = await this.$http.get(`/api/v1/access/device-discovery/progress/${this.scanProgress.scanId}`);
          if (result.code === 200 && result.data) {
            Object.assign(this.scanProgress, result.data);

            // 更新发现的设备列表
            if (result.data.discoveredDevices && result.data.discoveredDevices.length > 0) {
              this.discoveredDevices = result.data.discoveredDevices;
            }

            // 检查是否完成
            if (result.data.status === 'COMPLETED' || result.data.status === 'FAILED') {
              this.stopProgressPolling();
              this.isScanning = false;

              if (result.data.status === 'COMPLETED') {
                uni.showToast({
                  title: `发现完成，共${this.discoveredDevices.length}台设备`,
                  icon: 'success'
                });
              } else {
                uni.showToast({ title: '设备发现失败', icon: 'none' });
              }
            }
          }
        } catch (error) {
          console.error('获取扫描进度失败', error);
        }
      }, 2000);
    },

    // 停止轮询
    stopProgressPolling() {
      if (this.progressTimer) {
        clearInterval(this.progressTimer);
        this.progressTimer = null;
      }
    },

    // 取消发现
    async handleCancelDiscovery() {
      uni.showModal({
        title: '确认停止',
        content: '确定要停止当前的设备发现任务吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await this.$http.post(`/api/v1/access/device-discovery/cancel/${this.scanProgress.scanId}`);
              if (result.code === 200) {
                uni.showToast({ title: '已停止扫描', icon: 'success' });
                this.stopProgressPolling();
                this.isScanning = false;
                this.scanProgress.status = 'CANCELLED';
              } else {
                uni.showToast({ title: result.message || '停止失败', icon: 'none' });
              }
            } catch (error) {
              console.error('停止扫描失败', error);
              uni.showToast({ title: '停止失败', icon: 'none' });
            }
          }
        },
      });
    },

    // 重置
    handleReset() {
      this.stopProgressPolling();
      this.discoveredDevices = [];
      this.selectedDevices = [];
      this.scanProgress.scanId = null;
      this.scanProgress.status = null;
      this.scanProgress.progress = 0;
      this.isScanning = false;
      this.selectMode = false;
    },

    // 切换选择模式
    toggleSelectMode() {
      this.selectMode = !this.selectMode;
      if (!this.selectMode) {
        this.selectedDevices = [];
      }
    },

    // 切换设备选择
    toggleSelectDevice(device) {
      const index = this.selectedDevices.findIndex(d => d.ipAddress === device.ipAddress);
      if (index > -1) {
        this.selectedDevices.splice(index, 1);
      } else {
        if (!device.existsInSystem) {
          this.selectedDevices.push(device);
        }
      }
    },

    // 是否已选择
    isDeviceSelected(device) {
      return this.selectedDevices.some(d => d.ipAddress === device.ipAddress);
    },

    // 全选新设备
    selectAllNew() {
      const newDevices = this.discoveredDevices.filter(d => !d.existsInSystem);
      this.selectedDevices = [...newDevices];
    },

    // 批量添加
    async handleBatchAdd() {
      if (this.selectedDevices.length === 0) {
        uni.showToast({ title: '请至少选择一台设备', icon: 'none' });
        return;
      }

      try {
        const result = await this.$http.post('/api/v1/access/device-discovery/batch-add', this.selectedDevices);
        if (result.code === 200) {
          uni.showToast({ title: '批量添加成功', icon: 'success' });
          this.selectMode = false;
          this.selectedDevices = [];
          // 重新扫描以更新状态
          this.handleStartDiscovery();
        } else {
          uni.showToast({ title: result.message || '批量添加失败', icon: 'none' });
        }
      } catch (error) {
        console.error('批量添加失败', error);
        uni.showToast({ title: '批量添加失败', icon: 'none' });
      }
    },

    // 导出结果
    async handleExport() {
      try {
        const result = await this.$http.get(`/api/v1/access/device-discovery/export/${this.scanProgress.scanId}`);
        if (result.code === 200 && result.data) {
          uni.showToast({ title: '导出成功', icon: 'success' });
        } else {
          uni.showToast({ title: result.message || '导出失败', icon: 'none' });
        }
      } catch (error) {
        console.error('导出失败', error);
        uni.showToast({ title: '导出失败', icon: 'none' });
      }
    },

    // 设备点击
    handleDeviceClick(device) {
      if (this.selectMode) {
        this.toggleSelectDevice(device);
      } else {
        this.handleViewDetail(device);
      }
    },

    // 单个添加
    handleAddSingle(device) {
      if (device.existsInSystem) return;

      this.selectedDevices = [device];
      this.handleBatchAdd();
    },

    // 查看详情
    handleViewDetail(device) {
      this.currentDetail = { ...device };
      this.$refs.detailPopup.open();
    },

    // 关闭详情
    closeDetail() {
      this.$refs.detailPopup.close();
    },

    // 从详情添加
    handleAddFromDetail() {
      this.handleAddSingle(this.currentDetail);
      this.closeDetail();
    },

    // 辅助方法
    getStatusText(status) {
      const map = {
        'PENDING': '等待中',
        'SCANNING': '扫描中',
        'COMPLETED': '已完成',
        'FAILED': '失败',
        'CANCELLED': '已取消',
      };
      return map[status] || status;
    },

    getDeviceTypeName(type) {
      const map = {
        1: '门禁',
        2: '考勤',
        3: '消费',
        4: '视频',
        5: '访客',
        6: '生物识别',
      };
      return map[type] || '未知';
    },

    getDeviceStatusText(status) {
      const map = {
        1: '在线',
        2: '离线',
        3: '故障',
        4: '维护中',
      };
      return map[status] || '未知';
    },

    formatDeviceInfo(info) {
      if (!info) return '-';
      try {
        return JSON.stringify(JSON.parse(info), null, 2);
      } catch {
        return info;
      }
    },
  },
};
</script>

<style scoped>
.device-auto-discover-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 导航栏 */
.custom-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.nav-left,
.nav-right {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 500;
  color: #333;
}

/* 配置卡片 */
.config-card,
.progress-card {
  margin: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.card-title {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.title-text {
  margin-left: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

/* 表单 */
.form-item {
  margin-bottom: 20px;
}

.form-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.form-label.required::before {
  content: '*';
  color: #ff4d4f;
  margin-right: 4px;
}

.form-input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.form-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 滑块 */
.form-slider {
  width: 100%;
  margin: 20px 0;
}

/* 协议列表 */
.protocol-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.protocol-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  transition: all 0.3s;
}

.protocol-item.active {
  border-color: #1890ff;
  background: #e6f7ff;
}

.protocol-item.disabled {
  opacity: 0.5;
}

.protocol-icon {
  margin-bottom: 8px;
}

.protocol-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.protocol-desc {
  font-size: 12px;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.action-btn {
  flex: 1;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 4px;
  font-size: 14px;
}

.action-btn.primary {
  background: #1890ff;
  color: #fff;
}

.action-btn.danger {
  background: #ff4d4f;
  color: #fff;
}

/* 进度卡片 */
.status-badge {
  margin-left: auto;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.scanning {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.completed {
  background: #f6ffed;
  color: #52c41a;
}

.progress-info {
  margin-top: 12px;
}

.progress-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #f0f0f0;
  margin: 0 24px;
}

.progress-bar-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%);
  transition: width 0.3s;
}

.progress-percent {
  margin-left: 12px;
  font-size: 16px;
  font-weight: bold;
  color: #1890ff;
}

.scan-id {
  text-align: center;
  font-size: 12px;
  color: #999;
}

/* 设备列表 */
.devices-section {
  margin: 16px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  margin-left: 8px;
  font-size: 16px;
  font-weight: 500;
}

.action-link {
  color: #1890ff;
  font-size: 14px;
}

.batch-actions-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #f0f0f0;
}

.batch-info {
  font-size: 14px;
  color: #666;
}

.batch-buttons {
  display: flex;
  gap: 8px;
}

.batch-btn {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.batch-btn.primary {
  background: #1890ff;
  color: #fff;
  border-color: #1890ff;
}

.device-scroll {
  max-height: 500px;
}

.device-card {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.device-checkbox {
  padding-right: 12px;
  display: flex;
  align-items: center;
}

.device-content {
  flex: 1;
}

.device-main {
  display: flex;
  margin-bottom: 8px;
}

.device-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.device-icon.type-1 { background: #1890ff; }
.device-icon.type-2 { background: #52c41a; }
.device-icon.type-3 { background: #faad14; }
.device-icon.type-4 { background: #722ed1; }

.device-info {
  flex: 1;
}

.device-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.device-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-ip {
  font-size: 12px;
  color: #666;
}

.device-status {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
}

.device-status.status-1 {
  background: #f6ffed;
  color: #52c41a;
}

.device-status.status-2 {
  background: #fafafa;
  color: #999;
}

.device-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
}

.tag.protocol {
  background: #e6f7ff;
  color: #1890ff;
}

.tag.type-1 { background: #e6f7ff; color: #1890ff; }
.tag.type-2 { background: #f6ffed; color: #52c41a; }
.tag.type-3 { background: #fffbe6; color: #faad14; }
.tag.type-4 { background: #f9f0ff; color: #722ed1; }

.tag.exists {
  background: #f6ffed;
  color: #52c41a;
}

.tag.new {
  background: #e6f7ff;
  color: #1890ff;
}

.device-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.action-btn-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: #f5f7fa;
}

.action-btn-icon.disabled {
  opacity: 0.5;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

/* 详情弹窗 */
.detail-popup {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title {
  font-size: 16px;
  font-weight: 500;
}

.detail-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-content {
  flex: 1;
  padding: 16px;
}

.detail-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-label {
  font-size: 14px;
  color: #666;
}

.detail-value {
  font-size: 14px;
  color: #333;
}

.detail-value.primary {
  color: #1890ff;
}

.info-json {
  display: block;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.footer-btn {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
}

.footer-btn.primary {
  background: #1890ff;
  color: #fff;
}
</style>
