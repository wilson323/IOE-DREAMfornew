<template>
  <div class="access-advanced-container">
    <!-- 页面头部 -->
    <a-page-header
      title="门禁高级功能管理"
      sub-title="全局联动、全局互锁、疏散点管理、人数控制、人员限制"
      @back="handleBack"
    />

    <!-- 功能标签页 -->
    <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
      <!-- 全局联动 -->
      <a-tab-pane key="linkage" tab="全局联动">
        <access-linkage />
      </a-tab-pane>

      <!-- 全局互锁 -->
      <a-tab-pane key="interlock" tab="全局互锁">
        <access-interlock />
      </a-tab-pane>

      <!-- 疏散点管理 -->
      <a-tab-pane key="evacuation" tab="疏散点管理">
        <access-evacuation />
      </a-tab-pane>

      <!-- 人数控制 -->
      <a-tab-pane key="capacity" tab="人数控制">
        <access-capacity />
      </a-tab-pane>

      <!-- 人员限制 -->
      <a-tab-pane key="restriction" tab="人员限制">
        <access-restriction />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import AccessLinkage from './components/AccessLinkage.vue';
import AccessInterlock from './components/AccessInterlock.vue';
import AccessEvacuation from './components/AccessEvacuation.vue';
import AccessCapacity from './components/AccessCapacity.vue';
import AccessRestriction from './components/AccessRestriction.vue';

const router = useRouter();
const activeTab = ref('linkage');

const handleBack = () => {
  router.back();
};

const handleTabChange = (key) => {
  console.log('[高级功能] 切换标签:', key);
};
</script>

<style lang="scss" scoped>
.access-advanced-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}
</style>
