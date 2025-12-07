/**
 * 字符串工具函数单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect } from 'vitest'
import {
  convertLowerHyphen,
  convertUpperCamel,
  convertLowerCamel
} from '../str-util.js'

describe('convertLowerHyphen - 转为小写中划线', () => {
  it('应该转换驼峰命名', () => {
    expect(convertLowerHyphen('HelloWorld')).toBe('hello-world')
    expect(convertLowerHyphen('MyComponent')).toBe('my-component')
  })

  it('应该处理单个单词', () => {
    expect(convertLowerHyphen('Hello')).toBe('ello') // 首字母被移除
  })

  it('应该处理空字符串', () => {
    expect(convertLowerHyphen('')).toBe('')
    expect(convertLowerHyphen(null)).toBe('')
    expect(convertLowerHyphen(undefined)).toBe('')
  })

  it('应该处理全小写', () => {
    expect(convertLowerHyphen('hello')).toBe('ello')
  })
})

describe('convertUpperCamel - 转为大驼峰', () => {
  it('应该转换下划线命名', () => {
    expect(convertUpperCamel('hello_world')).toBe('HelloWorld')
    expect(convertUpperCamel('my_component')).toBe('MyComponent')
  })

  it('应该处理单个单词', () => {
    expect(convertUpperCamel('hello')).toBe('Hello')
  })

  it('应该处理空字符串', () => {
    expect(convertUpperCamel('')).toBe('')
    expect(convertUpperCamel(null)).toBe('')
    expect(convertUpperCamel(undefined)).toBe('')
  })

  it('应该处理已经是驼峰的字符串', () => {
    expect(convertUpperCamel('HelloWorld')).toBe('HelloWorld')
  })
})

describe('convertLowerCamel - 转为小驼峰', () => {
  it('应该转换下划线命名', () => {
    expect(convertLowerCamel('hello_world')).toBe('helloWorld')
    expect(convertLowerCamel('my_component')).toBe('myComponent')
  })

  it('应该处理单个单词', () => {
    expect(convertLowerCamel('hello')).toBe('hello')
  })

  it('应该处理空字符串', () => {
    expect(convertLowerCamel('')).toBe('')
    expect(convertLowerCamel(null)).toBe('')
    expect(convertLowerCamel(undefined)).toBe('')
  })

  it('应该处理已经是驼峰的字符串', () => {
    expect(convertLowerCamel('helloWorld')).toBe('helloWorld')
  })
})
