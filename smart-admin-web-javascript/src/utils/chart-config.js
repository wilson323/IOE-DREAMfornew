/*
 * ECharts图表配置工具
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import * as echarts from 'echarts';

// 图表主题颜色配置
export const CHART_COLORS = {
  primary: '#1890ff',
  success: '#52c41a',
  warning: '#faad14',
  error: '#f5222d',
  info: '#722ed1',
  cyan: '#13c2c2',
  magenta: '#eb2f96',
  volcano: '#fa541c',
  orange: '#fa8c16',
  gold: '#faad14',
  lime: '#a0d911',
  green: '#52c41a',
  teal: '#13c2c2',
  blue: '#1890ff',
  geekblue: '#2f54eb',
  purple: '#722ed1',
  gray: '#8c8c8c'
};

// 颜色数组
export const COLOR_ARRAYS = {
  default: [
    '#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1',
    '#13c2c2', '#eb2f96', '#fa541c', '#fa8c16', '#a0d911'
  ],
  business: [
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
    '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#00bcd4'
  ],
  traffic: [
    '#4f81bd', '#c0504d', '#9bbb59', '#8064a2', '#4bacc6',
    '#f79646', '#8064a2', '#4bacc6', '#1f497d', '#c0504d'
  ],
  security: [
    '#d9534f', '#5bc0de', '#5cb85c', '#f0ad4e', '#337ab7',
    '#d9534f', '#5bc0de', '#5cb85c', '#f0ad4e', '#337ab7'
  ]
};

// 通用图表配置生成器
export class ChartConfigGenerator {
  /**
   * 创建基础配置
   */
  static createBaseConfig(options = {}) {
    return {
      backgroundColor: 'transparent',
      title: {
        textStyle: {
          color: '#262626',
          fontSize: 16,
          fontWeight: 600
        },
        left: 'center',
        top: 10
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#e8e8e8',
        borderWidth: 1,
        textStyle: {
          color: '#262626',
          fontSize: 12
        },
        extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); border-radius: 6px;'
      },
      legend: {
        textStyle: {
          color: '#595959',
          fontSize: 12
        },
        top: 40
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: 80,
        containLabel: true,
        borderColor: '#f0f0f0'
      },
      ...options
    };
  }

  /**
   * 创建折线图配置
   */
  static createLineConfig(data, options = {}) {
    const { series = [], xAxis = [], ...otherOptions } = data;

    return this.createBaseConfig({
      xAxis: {
        type: 'category',
        data: xAxis,
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisTick: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c',
          fontSize: 12
        },
        ...options.xAxis
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisTick: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c',
          fontSize: 12
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0',
            type: 'dashed'
          }
        },
        ...options.yAxis
      },
      series: series.map((item, index) => ({
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: COLOR_ARRAYS.default[index % COLOR_ARRAYS.default.length]
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: options.showArea ? {
          opacity: 0.1
        } : undefined,
        ...item
      })),
      ...otherOptions
    });
  }

  /**
   * 创建柱状图配置
   */
  static createBarConfig(data, options = {}) {
    const { series = [], xAxis = [], ...otherOptions } = data;

    return this.createBaseConfig({
      xAxis: {
        type: 'category',
        data: xAxis,
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisTick: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c',
          fontSize: 12
        },
        ...options.xAxis
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisTick: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        axisLabel: {
          color: '#8c8c8c',
          fontSize: 12
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0',
            type: 'dashed'
          }
        },
        ...options.yAxis
      },
      series: series.map((item, index) => ({
        type: 'bar',
        barWidth: options.barWidth || 'auto',
        itemStyle: {
          borderRadius: options.barRadius || [4, 4, 0, 0],
          color: item.color || COLOR_ARRAYS.default[index % COLOR_ARRAYS.default.length]
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        ...item
      })),
      ...otherOptions
    });
  }

  /**
   * 创建饼图配置
   */
  static createPieConfig(data, options = {}) {
    const { series = [], ...otherOptions } = data;

    return this.createBaseConfig({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        textStyle: {
          color: '#595959',
          fontSize: 12
        }
      },
      series: series.map(item => ({
        type: 'pie',
        radius: options.radius || ['40%', '70%'],
        center: options.center || ['50%', '60%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
          show: true
        },
        ...item
      })),
      ...otherOptions
    });
  }

  /**
   * 创建热力图配置
   */
  static createHeatmapConfig(data, options = {}) {
    const { series = [], xAxis = [], yAxis = [], ...otherOptions } = data;

    return this.createBaseConfig({
      tooltip: {
        position: 'top',
        formatter: function(params) {
          return `${params.name}<br/>数值: ${params.value[2]}`;
        }
      },
      grid: {
        height: '50%',
        top: '10%'
      },
      xAxis: {
        type: 'category',
        data: xAxis,
        splitArea: {
          show: true
        },
        axisLabel: {
          color: '#8c8c8c',
          fontSize: 12
        }
      },
      yAxis: {
        type: 'category',
        data: yAxis,
        splitArea: {
          show: true
        },
        axisLabel: {
          color: '#8c8c8c',
          fontSize: 12
        }
      },
      visualMap: {
        min: options.visualMin || 0,
        max: options.visualMax || 100,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '15%',
        inRange: {
          color: ['#f7fbff', '#08519c']
        }
      },
      series: series.map(item => ({
        type: 'heatmap',
        label: {
          show: options.showLabel || false
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        ...item
      })),
      ...otherOptions
    });
  }

  /**
   * 创建仪表盘配置
   */
  static createGaugeConfig(data, options = {}) {
    const { series = [], ...otherOptions } = data;

    return this.createBaseConfig({
      series: series.map(item => ({
        type: 'gauge',
        center: options.center || ['50%', '60%'],
        radius: options.radius || '90%',
        min: options.min || 0,
        max: options.max || 100,
        splitNumber: options.splitNumber || 10,
        startAngle: options.startAngle || 225,
        endAngle: options.endAngle || -45,
        axisLine: {
          lineStyle: {
            width: options.lineWidth || 8,
            color: [
              [0.3, '#ff6e76'],
              [0.7, '#58d9f9'],
              [1, '#7cffb2']
            ]
          }
        },
        pointer: {
          icon: options.pointerIcon || 'path://M12.8,0.7l12,40.7H8.0L8.0,0.7H12.8z',
          length: options.pointerLength || '75%',
          width: options.pointerWidth || 16,
          offsetCenter: [0, '5%']
        },
        axisTick: {
          distance: -30,
          length: 8,
          lineStyle: {
            color: '#fff',
            width: 2
          }
        },
        splitLine: {
          distance: -30,
          length: 30,
          lineStyle: {
            color: '#fff',
            width: 4
          }
        },
        axisLabel: {
          color: 'inherit',
          distance: 40,
          fontSize: 12
        },
        detail: {
          fontSize: options.detailFontSize || 30,
          offsetCenter: [0, '70%'],
          valueAnimation: true,
          formatter: function(value) {
            return `${Math.round(value)}${options.unit || '%'}`;
          },
          color: 'inherit'
        },
        data: item.data || [{ value: 70, name: item.name || 'Score' }],
        ...item
      })),
      ...otherOptions
    });
  }

  /**
   * 创建雷达图配置
   */
  static createRadarConfig(data, options = {}) {
    const { series = [], radar = {}, ...otherOptions } = data;

    return this.createBaseConfig({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        data: series.map(item => item.name),
        bottom: 0
      },
      radar: {
        indicator: options.indicator || [],
        shape: options.shape || 'polygon',
        splitNumber: options.splitNumber || 5,
        axisName: {
          color: '#8c8c8c',
          fontSize: 12
        },
        splitLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        splitArea: {
          show: true,
          areaStyle: {
            color: ['rgba(24, 144, 255, 0.1)', 'rgba(24, 144, 255, 0.05)']
          }
        },
        axisLine: {
          lineStyle: {
            color: '#d9d9d9'
          }
        },
        ...radar
      },
      series: series.map((item, index) => ({
        type: 'radar',
        name: item.name,
        data: item.data,
        lineStyle: {
          color: COLOR_ARRAYS.default[index % COLOR_ARRAYS.default.length]
        },
        areaStyle: {
          opacity: 0.2,
          color: COLOR_ARRAYS.default[index % COLOR_ARRAYS.default.length]
        },
        symbol: 'circle',
        symbolSize: 6,
        ...item
      })),
      ...otherOptions
    });
  }
}

// 图表工具函数
export const ChartUtils = {
  /**
   * 格式化数字
   */
  formatNumber(num, precision = 2) {
    if (num >= 100000000) {
      return (num / 100000000).toFixed(precision) + '亿';
    } else if (num >= 10000) {
      return (num / 10000).toFixed(precision) + '万';
    } else {
      return num.toString();
    }
  },

  /**
   * 格式化百分比
   */
  formatPercent(num, precision = 2) {
    return (num * 100).toFixed(precision) + '%';
  },

  /**
   * 格式化日期
   */
  formatDate(date, format = 'YYYY-MM-DD') {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hour = String(d.getHours()).padStart(2, '0');
    const minute = String(d.getMinutes()).padStart(2, '0');

    return format
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hour)
      .replace('mm', minute);
  },

  /**
   * 获取趋势图标
   */
  getTrendIcon(trend) {
    if (trend > 0) return '↗';
    if (trend < 0) return '↘';
    return '→';
  },

  /**
   * 获取趋势颜色
   */
  getTrendColor(trend) {
    if (trend > 0) return CHART_COLORS.success;
    if (trend < 0) return CHART_COLORS.error;
    return CHART_COLORS.gray;
  },

  /**
   * 深色主题配置
   */
  getDarkTheme() {
    return {
      backgroundColor: '#1f1f1f',
      title: {
        textStyle: {
          color: '#ffffff'
        }
      },
      legend: {
        textStyle: {
          color: '#cccccc'
        }
      },
      xAxis: {
        axisLine: {
          lineStyle: {
            color: '#666666'
          }
        },
        axisLabel: {
          color: '#cccccc'
        }
      },
      yAxis: {
        axisLine: {
          lineStyle: {
            color: '#666666'
          }
        },
        axisLabel: {
          color: '#cccccc'
        },
        splitLine: {
          lineStyle: {
            color: '#333333'
          }
        }
      }
    };
  },

  /**
   * 自适应图表大小
   */
  resizeChart(chartInstance, container) {
    if (chartInstance && container) {
      chartInstance.resize({
        width: container.clientWidth,
        height: container.clientHeight
      });
    }
  }
};

// 导出默认配置
export default {
  CHART_COLORS,
  COLOR_ARRAYS,
  ChartConfigGenerator,
  ChartUtils
};