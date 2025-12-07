<!--
  * 流程进度组件
  * 使用Ant Design Steps组件展示工作流流程进度
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="process-steps">
    <a-steps :current="currentStep" :status="stepStatus" :items="stepItems" />
  </div>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({
    /**
     * 流程历史记录列表
     * @type {Array}
     */
    historyList: {
      type: Array,
      default: () => [],
    },
  });

  /**
   * 当前步骤索引
   */
  const currentStep = computed(() => {
    if (!props.historyList || props.historyList.length === 0) {
      return 0;
    }
    // 找到第一个未完成的步骤
    const index = props.historyList.findIndex((item) => !item.endTime);
    return index >= 0 ? index : props.historyList.length - 1;
  });

  /**
   * 步骤状态
   */
  const stepStatus = computed(() => {
    if (props.historyList.length === 0) {
      return 'wait';
    }
    const currentItem = props.historyList[currentStep.value];
    if (currentItem && currentItem.endTime) {
      return 'finish';
    }
    return 'process';
  });

  /**
   * 步骤项配置
   */
  const stepItems = computed(() => {
    if (!props.historyList || props.historyList.length === 0) {
      return [];
    }

    return props.historyList.map((item, index) => {
      let status = 'wait';
      if (item.endTime) {
        status = 'finish';
      } else if (index === currentStep.value) {
        status = 'process';
      }

      let title = item.nodeName || `节点${index + 1}`;
      let description = '';

      if (item.assigneeName) {
        description += `处理人：${item.assigneeName}`;
      }
      if (item.endTime) {
        description += description ? ` | 完成时间：${item.endTime}` : `完成时间：${item.endTime}`;
      }
      if (item.comment) {
        description += description ? ` | 意见：${item.comment}` : `意见：${item.comment}`;
      }

      return {
        title,
        description,
        status,
      };
    });
  });
</script>

<style lang="less" scoped>
  .process-steps {
    padding: 16px;
  }
</style>

