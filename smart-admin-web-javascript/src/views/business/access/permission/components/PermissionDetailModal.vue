<!--
  * Permission detail modal
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
-->
<template>
  <a-modal
    :open="visible"
    title="Permission Detail"
    :width="900"
    @cancel="handleCancel"
    :footer="null"
    destroy-on-close
  >
    <div v-if="!detail">
      <a-empty description="No data" />
    </div>

    <div v-else class="permission-detail">
      <a-card class="detail-card" :bordered="false" title="User">
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Name</span>
              <span class="info-value">{{ detail.userName }}</span>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Code</span>
              <span class="info-value">{{ detail.userCode }}</span>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Phone</span>
              <span class="info-value">{{ detail.phoneNumber || '-' }}</span>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Department</span>
              <a-tag color="blue">{{ detail.departmentName }}</a-tag>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card class="detail-card" :bordered="false" title="Permission">
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Area</span>
              <span class="info-value">{{ detail.areaName }}</span>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Area Code</span>
              <span class="info-value">{{ detail.areaCode }}</span>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Type</span>
              <a-tag :color="getPermissionTypeColor(detail.permissionType)">
                {{ getPermissionTypeText(detail.permissionType) }}
              </a-tag>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Status</span>
              <a-badge
                :status="getStatusBadge(detail.status)"
                :text="getStatusText(detail.status)"
              />
            </div>
          </a-col>
        </a-row>
      </a-card>

      <a-card class="detail-card" :bordered="false" title="Validity">
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="info-item">
              <span class="info-label">Valid Type</span>
              <span class="info-value">{{ getValidityTypeText(detail.validityType) }}</span>
            </div>
          </a-col>
          <template v-if="detail.validityType === 'RANGE'">
            <a-col :span="12">
              <div class="info-item">
                <span class="info-label">Start</span>
                <span class="info-value">{{ detail.startTime }}</span>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="info-item">
                <span class="info-label">End</span>
                <span class="info-value">{{ detail.endTime }}</span>
              </div>
            </a-col>
          </template>
          <template v-if="detail.validityType === 'COUNT'">
            <a-col :span="12">
              <div class="info-item">
                <span class="info-label">Total Count</span>
                <span class="info-value">{{ detail.accessCount }} times</span>
              </div>
            </a-col>
            <a-col :span="12">
              <div class="info-item">
                <span class="info-label">Used</span>
                <span class="info-value">{{ detail.usedCount }} times</span>
              </div>
            </a-col>
          </template>
        </a-row>
      </a-card>

      <a-card class="detail-card" :bordered="false" title="Rules">
        <a-row :gutter="24">
          <a-col :span="6">
            <div class="info-item">
              <span class="info-label">Anti-passback</span>
              <a-tag :color="detail.allowAntiPassback ? 'green' : 'default'">
                {{ detail.allowAntiPassback ? 'Enabled' : 'Disabled' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :span="6">
            <div class="info-item">
              <span class="info-label">Biometric</span>
              <a-tag :color="detail.requireBiometric ? 'green' : 'default'">
                {{ detail.requireBiometric ? 'Required' : 'Optional' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :span="6">
            <div class="info-item">
              <span class="info-label">Multi Entry</span>
              <a-tag :color="detail.allowMultiEntry ? 'green' : 'default'">
                {{ detail.allowMultiEntry ? 'Allowed' : 'Single only' }}
              </a-tag>
            </div>
          </a-col>
          <a-col :span="6">
            <div class="info-item">
              <span class="info-label">Emergency</span>
              <a-tag :color="detail.enableEmergencyMode ? 'orange' : 'default'">
                {{ detail.enableEmergencyMode ? 'On' : 'Off' }}
              </a-tag>
            </div>
          </a-col>
        </a-row>
      </a-card>
    </div>
  </a-modal>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  detail: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['update:visible']);

const handleCancel = () => {
  emit('update:visible', false);
};

const getPermissionTypeText = (type) => {
  const map = { FULL: 'Full Access', TIME_LIMIT: 'Time Limit', CUSTOM: 'Custom' };
  return map[type] || type;
};

const getPermissionTypeColor = (type) => {
  const map = { FULL: 'green', TIME_LIMIT: 'orange', CUSTOM: 'blue' };
  return map[type] || 'default';
};

const getStatusBadge = (status) => {
  if (status === 1 || status === 'ENABLED') return 'success';
  if (status === 2 || status === 'PENDING') return 'processing';
  return 'default';
};

const getStatusText = (status) => {
  if (status === 1 || status === 'ENABLED') return 'Enabled';
  if (status === 2 || status === 'PENDING') return 'Pending';
  return 'Disabled';
};

const getValidityTypeText = (type) => {
  const map = { RANGE: 'Date Range', COUNT: 'Usage Count', PERMANENT: 'Permanent' };
  return map[type] || type;
};
</script>

<style lang="less" scoped>
.detail-card {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;

  .info-label {
    width: 120px;
    color: rgba(0, 0, 0, 0.45);
    display: inline-block;
  }

  .info-value {
    color: rgba(0, 0, 0, 0.85);
    font-weight: 500;
  }
}
</style>
