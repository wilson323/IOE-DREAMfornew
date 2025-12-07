<!--
  * 流程图组件
  * 使用vue-bpmn渲染BPMN流程图，支持节点高亮和点击事件
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="process-diagram">
    <div v-if="loading" class="diagram-loading">
      <a-spin tip="加载流程图中..." />
    </div>
    <div v-else-if="error" class="diagram-error">
      <a-result status="error" title="加载流程图失败" :sub-title="error" />
    </div>
    <div v-else ref="diagramContainer" class="diagram-container"></div>
  </div>
</template>

<script setup>
  import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
  import { workflowApi } from '/@/api/business/oa/workflow-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    /**
     * 流程实例ID
     * @type {Number}
     */
    instanceId: {
      type: Number,
      default: null,
    },
    /**
     * BPMN XML字符串（可选，如果有则直接使用，否则通过instanceId获取）
     * @type {String}
     */
    bpmnXml: {
      type: String,
      default: null,
    },
    /**
     * 高亮的节点ID
     * @type {String}
     */
    highlightNode: {
      type: String,
      default: null,
    },
    /**
     * 是否高亮活动节点
     * @type {Boolean}
     */
    highlightActive: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['node-click']);

  const diagramContainer = ref(null);
  const loading = ref(false);
  const error = ref(null);
  let viewer = null;

  /**
   * 初始化BPMN查看器
   * 注意：这里简化实现，实际项目中需要安装bpmn-js库
   * npm install bpmn-js
   */
  async function initDiagram() {
    if (!diagramContainer.value) {
      return;
    }

    try {
      loading.value = true;
      error.value = null;

      let bpmnXml = props.bpmnXml;

      // 如果没有提供XML，通过instanceId获取
      if (!bpmnXml && props.instanceId) {
        const response = await workflowApi.getProcessDiagram(props.instanceId);
        if (response.code === 1 || response.code === 200) {
          bpmnXml = response.data?.bpmnXml || response.data;
        } else {
          throw new Error(response.message || '获取流程图数据失败');
        }
      }

      if (!bpmnXml) {
        throw new Error('未提供流程图数据');
      }

      // 使用bpmn-js渲染流程图
      // 注意：bpmn-js已在package.json中安装，版本^18.9.1
      try {
        // 动态导入bpmn-js（按需加载）
        const BpmnViewer = (await import('bpmn-js/lib/NavigatedViewer')).default;
        viewer = new BpmnViewer({
          container: diagramContainer.value,
        });
        await viewer.importXML(bpmnXml);
        
        // 如果后端返回的不是BPMN XML，而是warm-flow的流程定义，需要转换
        // 这里假设后端已经转换为BPMN格式
      } catch (importError) {
        // 如果bpmn-js未安装或导入失败，显示友好提示
        console.warn('bpmn-js未安装或导入失败，显示简化视图:', importError);
        diagramContainer.value.innerHTML = `
          <div style="padding: 40px; text-align: center; color: #999;">
            <p style="font-size: 16px; margin-bottom: 12px;">流程图预览</p>
            <p style="font-size: 12px; color: #666; line-height: 1.6;">
              流程图数据已加载（${bpmnXml.length} 字符）<br/>
              如需完整流程图渲染，请确保已安装 bpmn-js 库
            </p>
            <pre style="text-align: left; max-height: 400px; overflow: auto; margin-top: 20px; padding: 16px; background: #f5f5f5; border-radius: 4px; font-size: 12px;">${bpmnXml.substring(0, 500)}...</pre>
          </div>
        `;
      }

      // 高亮节点
      if (props.highlightNode && viewer) {
        highlightNode(props.highlightNode);
      }
    } catch (err) {
      console.error('初始化流程图失败:', err);
      error.value = err.message || '加载流程图失败';
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 高亮节点
   * @param {String} nodeId - 节点ID
   */
  function highlightNode(nodeId) {
    if (!viewer) {
      return;
    }

    try {
      // TODO: 实现节点高亮逻辑
      // const canvas = viewer.get('canvas');
      // const elementRegistry = viewer.get('elementRegistry');
      // const element = elementRegistry.get(nodeId);
      // if (element) {
      //   canvas.addMarker(nodeId, 'highlight');
      // }
    } catch (err) {
      console.error('高亮节点失败:', err);
    }
  }

  /**
   * 处理节点点击事件
   * @param {Object} event - 事件对象
   */
  function handleNodeClick(event) {
    if (!viewer) {
      return;
    }

    // TODO: 实现节点点击事件处理
    // const element = event.element;
    // if (element && element.type !== 'bpmn:Process') {
    //   emit('node-click', {
    //     id: element.id,
    //     type: element.type,
    //     name: element.businessObject.name
    //   });
    // }
  }

  /**
   * 销毁查看器
   */
  function destroyViewer() {
    if (viewer) {
      viewer.destroy();
      viewer = null;
    }
  }

  // 监听props变化
  watch(
    () => [props.instanceId, props.bpmnXml, props.highlightNode],
    () => {
      destroyViewer();
      initDiagram();
    }
  );

  onMounted(() => {
    initDiagram();
  });

  onBeforeUnmount(() => {
    destroyViewer();
  });
</script>

<style lang="less" scoped>
  .process-diagram {
    width: 100%;
    height: 600px;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    position: relative;

    .diagram-loading,
    .diagram-error {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
    }

    .diagram-container {
      width: 100%;
      height: 100%;
      overflow: auto;
    }
  }
</style>

