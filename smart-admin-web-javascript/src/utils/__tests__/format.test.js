/**
 * 格式化工具函数单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  formatDateTime,
  formatRelativeTime,
  formatDuration,
  formatFileSize,
  formatBytes,
  formatNumber,
  formatPercentage,
  formatCurrency,
  formatGrowthRate,
  formatPhoneNumber,
  formatBankCard,
  formatText,
  formatBoolean,
  formatArray
} from '../format.js'

describe('formatDateTime - 日期时间格式化', () => {
  it('应该正确格式化日期时间', () => {
    const timestamp = new Date('2024-01-15 14:30:45').getTime()
    const result = formatDateTime(timestamp)
    expect(result).toBe('2024-01-15 14:30:45')
  })

  it('应该使用自定义格式', () => {
    const timestamp = new Date('2024-01-15 14:30:45').getTime()
    const result = formatDateTime(timestamp, 'YYYY/MM/DD')
    expect(result).toBe('2024/01/15')
  })

  it('应该处理空值返回"-"', () => {
    expect(formatDateTime(null)).toBe('-')
    expect(formatDateTime(undefined)).toBe('-')
    expect(formatDateTime('')).toBe('-')
  })

  it('应该处理无效日期返回"-"', () => {
    expect(formatDateTime('invalid')).toBe('-')
    expect(formatDateTime(NaN)).toBe('-')
  })
})

describe('formatRelativeTime - 相对时间格式化', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  it('应该显示"刚刚"', () => {
    const now = Date.now()
    vi.setSystemTime(now)
    const result = formatRelativeTime(now - 500)
    expect(result).toBe('刚刚')
  })

  it('应该显示秒数', () => {
    const now = Date.now()
    vi.setSystemTime(now)
    const result = formatRelativeTime(now - 5000)
    expect(result).toBe('5秒前')
  })

  it('应该显示分钟数', () => {
    const now = Date.now()
    vi.setSystemTime(now)
    const result = formatRelativeTime(now - 120000)
    expect(result).toBe('2分钟前')
  })

  it('应该显示小时数', () => {
    const now = Date.now()
    vi.setSystemTime(now)
    const result = formatRelativeTime(now - 7200000)
    expect(result).toBe('2小时前')
  })

  it('应该处理空值', () => {
    expect(formatRelativeTime(null)).toBe('-')
    expect(formatRelativeTime(undefined)).toBe('-')
  })
})

describe('formatDuration - 持续时间格式化', () => {
  it('应该格式化秒数', () => {
    expect(formatDuration(5000)).toBe('5秒')
  })

  it('应该格式化分钟和秒', () => {
    expect(formatDuration(125000)).toBe('2分钟5秒')
  })

  it('应该格式化小时、分钟和秒', () => {
    expect(formatDuration(3665000)).toBe('1小时1分钟5秒')
  })

  it('应该格式化天、小时和分钟', () => {
    expect(formatDuration(90061000)).toBe('1天1小时1分钟')
  })

  it('应该处理0或负数', () => {
    expect(formatDuration(0)).toBe('0秒')
    expect(formatDuration(-1000)).toBe('0秒')
  })
})

describe('formatFileSize - 文件大小格式化', () => {
  it('应该格式化字节', () => {
    expect(formatFileSize(0)).toBe('0 B')
    expect(formatFileSize(500)).toBe('500 B')
  })

  it('应该格式化KB', () => {
    expect(formatFileSize(1024)).toBe('1 KB')
    expect(formatFileSize(2048)).toBe('2 KB')
  })

  it('应该格式化MB', () => {
    expect(formatFileSize(1048576)).toBe('1 MB')
  })

  it('应该格式化GB', () => {
    expect(formatFileSize(1073741824)).toBe('1 GB')
  })

  it('应该支持自定义小数位数', () => {
    expect(formatFileSize(1536, 1)).toBe('1.5 KB')
  })
})

describe('formatBytes - 字节格式化别名', () => {
  it('应该与formatFileSize行为一致', () => {
    expect(formatBytes(1024)).toBe(formatFileSize(1024))
  })
})

describe('formatNumber - 数字格式化', () => {
  it('应该格式化基本数字', () => {
    expect(formatNumber(1234.56)).toBe('1,234.56')
  })

  it('应该支持自定义分隔符', () => {
    expect(formatNumber(1234.56, { separator: '' })).toBe('1234.56')
  })

  it('应该支持前缀和后缀', () => {
    expect(formatNumber(100, { prefix: '$', suffix: '元' })).toBe('$100.00元')
  })

  it('应该支持显示正号', () => {
    expect(formatNumber(100, { showPlus: true })).toBe('+100.00')
  })

  it('应该处理null和undefined', () => {
    expect(formatNumber(null)).toBe('-')
    expect(formatNumber(undefined)).toBe('-')
  })
})

describe('formatPercentage - 百分比格式化', () => {
  it('应该计算百分比', () => {
    expect(formatPercentage(50, 100)).toBe('50.0%')
  })

  it('应该支持自定义小数位数', () => {
    expect(formatPercentage(33, 100, 2)).toBe('33.00%')
  })

  it('应该处理0总数', () => {
    expect(formatPercentage(50, 0)).toBe('0%')
  })
})

describe('formatCurrency - 货币格式化', () => {
  it('应该格式化人民币', () => {
    expect(formatCurrency(1234.56)).toBe('¥1,234.56')
  })

  it('应该支持其他货币符号', () => {
    expect(formatCurrency(1234.56, '$')).toBe('$1,234.56')
  })

  it('应该支持自定义选项', () => {
    expect(formatCurrency(1234.56, '¥', { decimals: 0 })).toBe('¥1,235')
  })
})

describe('formatGrowthRate - 增长率格式化', () => {
  it('应该计算正增长', () => {
    const result = formatGrowthRate(120, 100)
    expect(result.direction).toBe('up')
    expect(result.text).toBe('+20.0%')
    expect(result.value).toBe(20)
  })

  it('应该计算负增长', () => {
    const result = formatGrowthRate(80, 100)
    expect(result.direction).toBe('down')
    expect(result.text).toBe('-20.0%')
    expect(result.value).toBe(-20)
  })

  it('应该处理0之前值', () => {
    const result = formatGrowthRate(100, 0)
    expect(result.text).toBe('∞%')
  })
})

describe('formatPhoneNumber - 手机号格式化', () => {
  it('应该格式化11位手机号', () => {
    expect(formatPhoneNumber('13800138000')).toBe('138-0013-8000')
  })

  it('应该处理非11位号码', () => {
    expect(formatPhoneNumber('123456')).toBe('123456')
  })

  it('应该处理空值', () => {
    expect(formatPhoneNumber(null)).toBe('-')
    expect(formatPhoneNumber('')).toBe('-')
  })
})

describe('formatBankCard - 银行卡号格式化', () => {
  it('应该格式化银行卡号', () => {
    expect(formatBankCard('1234567890123456')).toBe('1234 **** **** 3456')
  })

  it('应该处理短卡号', () => {
    expect(formatBankCard('1234')).toBe('1234')
  })

  it('应该处理空值', () => {
    expect(formatBankCard(null)).toBe('-')
  })
})

describe('formatText - 文本截断', () => {
  it('应该截断长文本', () => {
    const longText = 'a'.repeat(150)
    const result = formatText(longText, 100)
    expect(result.length).toBe(103) // 100 + '...'
    expect(result).toContain('...')
  })

  it('应该保留短文本', () => {
    expect(formatText('short')).toBe('short')
  })

  it('应该处理空值', () => {
    expect(formatText(null)).toBe('-')
  })
})

describe('formatBoolean - 布尔值格式化', () => {
  it('应该格式化true值', () => {
    expect(formatBoolean(true)).toBe('是')
  })

  it('应该格式化false值', () => {
    expect(formatBoolean(false)).toBe('否')
  })

  it('应该支持自定义选项', () => {
    expect(formatBoolean(true, ['启用', '禁用'])).toBe('启用')
    expect(formatBoolean(false, ['启用', '禁用'])).toBe('禁用')
  })

  it('应该处理null和undefined', () => {
    expect(formatBoolean(null)).toBe('-')
    expect(formatBoolean(undefined)).toBe('-')
  })
})

describe('formatArray - 数组格式化', () => {
  it('应该格式化短数组', () => {
    expect(formatArray(['a', 'b', 'c'])).toBe('a, b, c')
  })

  it('应该截断长数组', () => {
    const arr = ['a', 'b', 'c', 'd', 'e']
    const result = formatArray(arr, ', ', 3)
    expect(result).toBe('a, b, c ... (+2项)')
  })

  it('应该处理空数组', () => {
    expect(formatArray([])).toBe('-')
  })

  it('应该处理非数组', () => {
    expect(formatArray(null)).toBe('-')
  })
})
