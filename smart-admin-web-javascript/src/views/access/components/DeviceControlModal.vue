<!--
  * Device Control Modal Component
-->
<template>
  <div class="device-control-modal">
    <a-modal
      :visible="visible"
      @update:visible="(val) => $emit('update:visible', val)"
      title="Device Control"
      width="600px"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-empty description="Device control feature under development" />
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

  const handleSubmit = () => {
    emit('update:visible', false);
  };

  const handleCancel = () => {
    emit('update:visible', false);
  };
</script>

<style lang="less" scoped>
  .device-control-modal {
    // 基础样式
  }
</style>