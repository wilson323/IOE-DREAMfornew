<!--
 * 访客物流管理页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-logistics-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h1>物流管理</h1>
        <p>管理物流车辆、司机和电子出门单</p>
      </div>
      <div class="quick-actions">
        <a-space>
          <a-button type="primary" @click="showVehicleModal">
            <template #icon><CarOutlined /></template>
            车辆管理
          </a-button>
          <a-button @click="showDriverModal">
            <template #icon><UserOutlined /></template>
            司机管理
          </a-button>
          <a-button @click="showPassModal">
            <template #icon><FileTextOutlined /></template>
            电子出门单
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- Tab切换 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="vehicle" tab="车辆管理">
          <VehicleManagement ref="vehicleManagementRef" />
        </a-tab-pane>
        <a-tab-pane key="driver" tab="司机管理">
          <DriverManagement ref="driverManagementRef" />
        </a-tab-pane>
        <a-tab-pane key="pass" tab="电子出门单">
          <ElectronicPassManagement ref="passManagementRef" />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import {
  CarOutlined,
  UserOutlined,
  FileTextOutlined,
} from '@ant-design/icons-vue';
import VehicleManagement from './components/VehicleManagement.vue';
import DriverManagement from './components/DriverManagement.vue';
import ElectronicPassManagement from './components/ElectronicPassManagement.vue';

const activeTab = ref('vehicle');
const vehicleManagementRef = ref();
const driverManagementRef = ref();
const passManagementRef = ref();

// Tab切换
const handleTabChange = (key) => {
  activeTab.value = key;
};

// 显示车辆管理弹窗
const showVehicleModal = () => {
  activeTab.value = 'vehicle';
  if (vehicleManagementRef.value) {
    vehicleManagementRef.value.showAddModal();
  }
};

// 显示司机管理弹窗
const showDriverModal = () => {
  activeTab.value = 'driver';
  if (driverManagementRef.value) {
    driverManagementRef.value.showAddModal();
  }
};

// 显示电子出门单弹窗
const showPassModal = () => {
  activeTab.value = 'pass';
  if (passManagementRef.value) {
    passManagementRef.value.showAddModal();
  }
};
</script>

<style scoped lang="less">
.visitor-logistics-page {
  padding: 24px;
  background: #f0f2f5;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #262626;
      }

      p {
        margin: 4px 0 0 0;
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }
}
</style>

