<!--
  布局选择组件

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="layout-selector">
    <a-dropdown placement="bottomLeft">
      <a-button>
        <template #icon><AppstoreOutlined /></template>
        {{ currentLayoutText }}
        <template #iconRight><DownOutlined /></template>
      </a-button>

      <template #overlay>
        <a-menu @click="handleLayoutSelect">
          <a-menu-item key="1">
            <div class="layout-option">
              <div class="layout-grid layout-1"></div>
              <span>单画面</span>
            </div>
          </a-menu-item>

          <a-menu-item key="4">
            <div class="layout-option">
              <div class="layout-grid layout-4"></div>
              <span>四画面</span>
            </div>
          </a-menu-item>

          <a-menu-item key="9">
            <div class="layout-option">
              <div class="layout-grid layout-9"></div>
              <span>九画面</span>
            </div>
          </a-menu-item>

          <a-menu-item key="16">
            <div class="layout-option">
              <div class="layout-grid layout-16"></div>
              <span>十六画面</span>
            </div>
          </a-menu-item>

          <a-menu-item key="25">
            <div class="layout-option">
              <div class="layout-grid layout-25"></div>
              <span>二十五画面</span>
            </div>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { AppstoreOutlined, DownOutlined } from '@ant-design/icons-vue';

const props = defineProps({
  currentLayout: {
    type: Number,
    default: 4,
  },
});

const emit = defineEmits(['change']);

// 布局文本
const currentLayoutText = computed(() => {
  const textMap = {
    1: '单画面',
    4: '四画面',
    9: '九画面',
    16: '十六画面',
    25: '二十五画面',
  };
  return textMap[props.currentLayout] || '四画面';
});

// 处理布局选择
const handleLayoutSelect = ({ key }) => {
  const layout = parseInt(key);
  emit('change', layout);
};
</script>

<style scoped lang="less">
.layout-selector {
  :deep(.layout-option) {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 160px;
  }

  .layout-grid {
    display: grid;
    gap: 4px;
    width: 80px;
    height: 60px;

    &.layout-1 {
      grid-template-columns: 1fr;
      grid-template-rows: 1fr;
      background: #1890ff;
    }

    &.layout-4 {
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: repeat(2, 1fr);
      background:
        linear-gradient(90deg, #1890ff 0 50%, transparent 50% 100%),
        linear-gradient(0deg, #1890ff 0 50%, transparent 50% 100%);
      background-size: 50% 50%;
      background-position: top left;
    }

    &.layout-9 {
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(3, 1fr);
      background:
        repeating-linear-gradient(
          0deg,
          #1890ff,
          #1890ff 1px,
          transparent 1px,
          transparent calc(33.333% - 1px)
        ),
        repeating-linear-gradient(
          90deg,
          #1890ff,
          #1890ff 1px,
          transparent 1px,
          transparent calc(33.333% - 1px)
        );
    }

    &.layout-16 {
      grid-template-columns: repeat(4, 1fr);
      grid-template-rows: repeat(4, 1fr);
      background:
        repeating-linear-gradient(
          0deg,
          #1890ff,
          #1890ff 1px,
          transparent 1px,
          transparent calc(25% - 1px)
        ),
        repeating-linear-gradient(
          90deg,
          #1890ff,
          #1890ff 1px,
          transparent 1px,
          transparent calc(25% - 1px)
        );
    }

    &.layout-25 {
      grid-template-columns: repeat(5, 1fr);
      grid-template-rows: repeat(5, 1fr);
      background:
        repeating-linear-gradient(
          0deg,
          #1890ff,
          #1890ff 1px,
          transparent 1px,
          transparent calc(20% - 1px)
        ),
        repeating-linear-gradient(
          90deg,
          #1890ff,
          #1890ff 1px,
          transparent 1px,
          transparent calc(20% - 1px)
        );
    }
  }
}
</style>
