<!--
  * Record Detail Modal Component
-->
<template>
  <div class="record-detail-modal">
    <a-modal
      :visible="visible"
      @update:visible="(val) => $emit('update:visible', val)"
      title="Access Record Details"
      width="700px"
      :footer="null"
    >
      <a-empty description="Record detail feature under development" />
    </a-modal>
  </div>
</template>

<script setup>
  import { reactive, ref } from 'vue';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['update:visible']);
</script>

<style lang="less" scoped>
  .record-detail-modal {
    // 基础样式
  }
</style>