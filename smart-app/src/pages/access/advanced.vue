<template>
  <view class="advanced-page">
    <!-- 标签页 -->
    <u-tabs :list="tabs" @click="handleTabChange" v-model="activeTab">
      <u-tab-pane name="linkage">
        <access-linkage-mobile />
      </u-tab-pane>
      <u-tab-pane name="interlock">
        <access-interlock-mobile />
      </u-tab-pane>
      <u-tab-pane name="evacuation">
        <access-evacuation-mobile />
      </u-tab-pane>
      <u-tab-pane name="capacity">
        <access-capacity-mobile />
      </u-tab-pane>
      <u-tab-pane name="restriction">
        <access-restriction-mobile />
      </u-tab-pane>
    </u-tabs>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import AccessLinkageMobile from './linkage-mobile.vue';
import AccessInterlockMobile from './interlock-mobile.vue';
import AccessEvacuationMobile from './evacuation-mobile.vue';
import AccessCapacityMobile from './capacity-mobile.vue';
import AccessRestrictionMobile from './restriction-mobile.vue';

const activeTab = ref('linkage');
const tabs = [
  { name: 'linkage', label: '全局联动' },
  { name: 'interlock', label: '全局互锁' },
  { name: 'evacuation', label: '疏散点' },
  { name: 'capacity', label: '人数控制' },
  { name: 'restriction', label: '人员限制' }
];

const handleTabChange = (index) => {
  console.log('[高级功能] 切换标签:', index);
};
</script>

<style lang="scss" scoped>
.advanced-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
