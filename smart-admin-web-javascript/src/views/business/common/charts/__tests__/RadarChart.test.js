/**
 * RadarChart组件单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RadarChart from '../RadarChart.vue'
import * as echarts from 'echarts'

// Mock echarts
vi.mock('echarts', () => ({
  default: {
    init: vi.fn(() => ({
      setOption: vi.fn(),
      resize: vi.fn(),
      dispose: vi.fn()
    })),
    graphic: {
      RadialGradient: vi.fn()
    }
  }
}))

describe('RadarChart组件测试', () => {
  let wrapper
  let mockChartInstance

  beforeEach(() => {
    mockChartInstance = {
      setOption: vi.fn(),
      resize: vi.fn(),
      dispose: vi.fn()
    }
    echarts.init.mockReturnValue(mockChartInstance)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  it('应该正确渲染组件', () => {
    wrapper = mount(RadarChart, {
      props: {
        title: '测试雷达图',
        height: 400,
        indicator: [
          { name: '指标1', max: 100 },
          { name: '指标2', max: 100 }
        ],
        series: [
          {
            name: '系列1',
            value: [80, 90]
          }
        ]
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.ant-card').exists()).toBe(true)
  })

  it('应该使用默认props值', () => {
    wrapper = mount(RadarChart, {
      props: {
        indicator: [],
        series: []
      }
    })

    expect(wrapper.props('title')).toBe('雷达图分析')
    expect(wrapper.props('height')).toBe(450)
  })

  it('应该在挂载时初始化图表', () => {
    wrapper = mount(RadarChart, {
      props: {
        indicator: [{ name: '指标1', max: 100 }],
        series: [{ name: '系列1', value: [80] }]
      }
    })

    expect(echarts.init).toHaveBeenCalled()
    expect(mockChartInstance.setOption).toHaveBeenCalled()
  })

  it('应该响应props变化更新图表', async () => {
    wrapper = mount(RadarChart, {
      props: {
        indicator: [{ name: '指标1', max: 100 }],
        series: [{ name: '系列1', value: [80] }]
      }
    })

    const initialCallCount = mockChartInstance.setOption.mock.calls.length

    await wrapper.setProps({
      indicator: [{ name: '指标1', max: 100 }, { name: '指标2', max: 100 }],
      series: [{ name: '系列1', value: [80, 90] }]
    })

    // 等待watch触发
    await wrapper.vm.$nextTick()

    expect(mockChartInstance.setOption.mock.calls.length).toBeGreaterThan(initialCallCount)
  })

  it('应该在窗口resize时调整图表大小', async () => {
    wrapper = mount(RadarChart, {
      props: {
        indicator: [],
        series: []
      }
    })

    // 模拟窗口resize事件
    window.dispatchEvent(new Event('resize'))

    // 等待事件处理
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(mockChartInstance.resize).toHaveBeenCalled()
  })

  it('应该在卸载时销毁图表实例', () => {
    wrapper = mount(RadarChart, {
      props: {
        indicator: [],
        series: []
      }
    })

    wrapper.unmount()

    expect(mockChartInstance.dispose).toHaveBeenCalled()
  })

  it('应该正确处理空的indicator数组', () => {
    wrapper = mount(RadarChart, {
      props: {
        indicator: [],
        series: []
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(mockChartInstance.setOption).toHaveBeenCalled()
  })

  it('应该正确处理空的series数组', () => {
    wrapper = mount(RadarChart, {
      props: {
        indicator: [{ name: '指标1', max: 100 }],
        series: []
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(mockChartInstance.setOption).toHaveBeenCalled()
  })

  it('应该正确设置图表高度', () => {
    wrapper = mount(RadarChart, {
      props: {
        height: 600,
        indicator: [],
        series: []
      }
    })

    const chartDiv = wrapper.find('div[ref="chartRef"]')
    expect(chartDiv.exists()).toBe(true)
    expect(chartDiv.attributes('style')).toContain('height: 600px')
  })
})
