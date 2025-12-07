/**
 * 表单验证工具单元测试
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 * 
 * 测试覆盖：
 * - 手机号验证
 * - 身份证验证
 * - 邮箱验证
 * - 车牌号验证
 * - 必填验证
 * - 长度验证
 * - 数值范围验证
 * - 日期验证
 * - 金额验证
 * - FormValidator类
 */

import { describe, it, expect } from '@jest/globals'
import {
  validatePhone,
  validateIdCard,
  validateEmail,
  validateVehicleNumber,
  validateRequired,
  validateLength,
  validateRange,
  validateDate,
  validateDateRange,
  validateAmount,
  FormValidator,
  commonRules
} from '../form-validation'

describe('form-validation - 手机号验证', () => {
  it('应该验证通过正确的手机号', () => {
    const validPhones = [
      '13800138000',
      '15912345678',
      '18888888888',
      '19999999999'
    ]
    
    validPhones.forEach(phone => {
      const result = validatePhone(phone)
      expect(result.valid).toBe(true)
    })
  })
  
  it('应该拒绝错误的手机号', () => {
    const invalidPhones = [
      '',              // 空值
      '12345678901',   // 错误的前缀
      '1380013800',    // 位数不足
      '138001380000',  // 位数过多
      'abcdefghijk',   // 非数字
      '02112345678'    // 座机号
    ]
    
    invalidPhones.forEach(phone => {
      const result = validatePhone(phone)
      expect(result.valid).toBe(false)
      expect(result.message).toBeDefined()
    })
  })
})

describe('form-validation - 身份证验证', () => {
  it('应该验证通过正确的身份证号', () => {
    const validIdCards = [
      '110101199001011234',      // 18位
      '110101900101123',         // 15位
      '11010119900101123X',      // 末位X
      '11010119900101123x'       // 末位小写x
    ]
    
    validIdCards.forEach(idCard => {
      const result = validateIdCard(idCard)
      expect(result.valid).toBe(true)
    })
  })
  
  it('应该拒绝错误的身份证号', () => {
    const invalidIdCards = [
      '',
      '12345',                   // 位数不足
      '1234567890123456789',     // 位数过多
      'abcdefghijklmnopq'        // 非数字
    ]
    
    invalidIdCards.forEach(idCard => {
      const result = validateIdCard(idCard)
      expect(result.valid).toBe(false)
    })
  })
})

describe('form-validation - 邮箱验证', () => {
  it('应该验证通过正确的邮箱', () => {
    const validEmails = [
      'test@example.com',
      'user.name@example.com',
      'user+tag@example.co.uk',
      'user_name@example-domain.com'
    ]
    
    validEmails.forEach(email => {
      const result = validateEmail(email)
      expect(result.valid).toBe(true)
    })
  })
  
  it('应该拒绝错误的邮箱', () => {
    const invalidEmails = [
      '',
      'notanemail',
      '@example.com',
      'user@',
      'user@.com',
      'user@domain'
    ]
    
    invalidEmails.forEach(email => {
      const result = validateEmail(email)
      expect(result.valid).toBe(false)
    })
  })
})

describe('form-validation - 车牌号验证', () => {
  it('应该验证通过正确的车牌号', () => {
    const validPlates = [
      '京A12345',
      '沪B88888',
      '粤C00000',
      '川D99999'
    ]
    
    validPlates.forEach(plate => {
      const result = validateVehicleNumber(plate)
      expect(result.valid).toBe(true)
    })
  })
  
  it('应该拒绝错误的车牌号', () => {
    const invalidPlates = [
      '',
      'A12345',      // 缺少省份
      '京12345',     // 缺少字母
      '京AA2345',    // 格式错误
      '京A1234'      // 位数不足
    ]
    
    invalidPlates.forEach(plate => {
      const result = validateVehicleNumber(plate)
      expect(result.valid).toBe(false)
    })
  })
})

describe('form-validation - 必填验证', () => {
  it('应该拒绝空值', () => {
    const emptyValues = [null, undefined, '', '   ']
    
    emptyValues.forEach(value => {
      const result = validateRequired(value, '测试字段')
      expect(result.valid).toBe(false)
      expect(result.message).toContain('测试字段')
    })
  })
  
  it('应该通过非空值', () => {
    const validValues = ['test', '0', 0, false]
    
    validValues.forEach(value => {
      const result = validateRequired(value)
      expect(result.valid).toBe(true)
    })
  })
})

describe('form-validation - 长度验证', () => {
  it('应该验证最小长度', () => {
    const result1 = validateLength('ab', 3, null, '测试')
    expect(result1.valid).toBe(false)
    expect(result1.message).toContain('不能少于3')
    
    const result2 = validateLength('abc', 3, null, '测试')
    expect(result2.valid).toBe(true)
  })
  
  it('应该验证最大长度', () => {
    const result1 = validateLength('abcdef', null, 5, '测试')
    expect(result1.valid).toBe(false)
    expect(result1.message).toContain('不能超过5')
    
    const result2 = validateLength('abcde', null, 5, '测试')
    expect(result2.valid).toBe(true)
  })
  
  it('应该验证长度范围', () => {
    const result1 = validateLength('ab', 3, 5)
    expect(result1.valid).toBe(false)
    
    const result2 = validateLength('abc', 3, 5)
    expect(result2.valid).toBe(true)
    
    const result3 = validateLength('abcdef', 3, 5)
    expect(result3.valid).toBe(false)
  })
})

describe('form-validation - 数值范围验证', () => {
  it('应该验证最小值', () => {
    const result1 = validateRange(5, 10, null, '数值')
    expect(result1.valid).toBe(false)
    
    const result2 = validateRange(10, 10, null)
    expect(result2.valid).toBe(true)
  })
  
  it('应该验证最大值', () => {
    const result1 = validateRange(15, null, 10, '数值')
    expect(result1.valid).toBe(false)
    
    const result2 = validateRange(10, null, 10)
    expect(result2.valid).toBe(true)
  })
  
  it('应该拒绝非数字', () => {
    const result = validateRange('abc', 0, 100)
    expect(result.valid).toBe(false)
    expect(result.message).toContain('必须是数字')
  })
})

describe('form-validation - 日期验证', () => {
  it('应该验证通过正确的日期', () => {
    const validDates = [
      '2024-01-01',
      '2024-12-31',
      new Date().toISOString()
    ]
    
    validDates.forEach(date => {
      const result = validateDate(date)
      expect(result.valid).toBe(true)
    })
  })
  
  it('应该拒绝无效日期', () => {
    const invalidDates = [
      '',
      'not-a-date',
      '2024-13-01',    // 无效月份
      '2024-01-32'     // 无效日期
    ]
    
    invalidDates.forEach(date => {
      const result = validateDate(date)
      expect(result.valid).toBe(false)
    })
  })
})

describe('form-validation - 日期范围验证', () => {
  it('应该验证正确的日期范围', () => {
    const result = validateDateRange('2024-01-01', '2024-12-31')
    expect(result.valid).toBe(true)
  })
  
  it('应该拒绝倒序的日期范围', () => {
    const result = validateDateRange('2024-12-31', '2024-01-01')
    expect(result.valid).toBe(false)
    expect(result.message).toContain('不能晚于')
  })
  
  it('应该拒绝空日期', () => {
    const result = validateDateRange('', '2024-12-31')
    expect(result.valid).toBe(false)
  })
})

describe('form-validation - 金额验证', () => {
  it('应该验证通过正确的金额', () => {
    const validAmounts = [
      '0.01',
      '100',
      '999.99',
      '1234.5'
    ]
    
    validAmounts.forEach(amount => {
      const result = validateAmount(amount)
      expect(result.valid).toBe(true)
    })
  })
  
  it('应该拒绝超出范围的金额', () => {
    const result1 = validateAmount('-1')
    expect(result1.valid).toBe(false)
    
    const result2 = validateAmount('1000000')
    expect(result2.valid).toBe(false)
  })
  
  it('应该拒绝小数位数过多的金额', () => {
    const result = validateAmount('100.123')
    expect(result.valid).toBe(false)
    expect(result.message).toContain('最多保留2位小数')
  })
  
  it('应该拒绝非数字金额', () => {
    const result = validateAmount('abc')
    expect(result.valid).toBe(false)
  })
})

describe('FormValidator - 表单验证器类', () => {
  const rules = {
    username: [
      { required: true, label: '用户名' },
      { minLength: 2, maxLength: 20 }
    ],
    phone: [
      { required: true, label: '手机号' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
    ],
    age: [
      { required: false },
      { min: 0, max: 150 }
    ]
  }
  
  it('应该验证通过合法的表单数据', () => {
    const validator = new FormValidator(rules)
    const formData = {
      username: '张三',
      phone: '13800138000',
      age: 25
    }
    
    const result = validator.validate(formData)
    expect(result.valid).toBe(true)
    expect(Object.keys(result.errors).length).toBe(0)
  })
  
  it('应该捕获必填字段错误', () => {
    const validator = new FormValidator(rules)
    const formData = {
      username: '',
      phone: '13800138000'
    }
    
    const result = validator.validate(formData)
    expect(result.valid).toBe(false)
    expect(result.errors.username).toBeDefined()
  })
  
  it('应该捕获格式错误', () => {
    const validator = new FormValidator(rules)
    const formData = {
      username: '张三',
      phone: '12345678901'  // 错误的手机号
    }
    
    const result = validator.validate(formData)
    expect(result.valid).toBe(false)
    expect(result.errors.phone).toContain('格式不正确')
  })
  
  it('应该捕获长度错误', () => {
    const validator = new FormValidator(rules)
    const formData = {
      username: '这是一个超过二十个字符的非常长的用户名',
      phone: '13800138000'
    }
    
    const result = validator.validate(formData)
    expect(result.valid).toBe(false)
    expect(result.errors.username).toBeDefined()
  })
  
  it('应该允许选填字段为空', () => {
    const validator = new FormValidator(rules)
    const formData = {
      username: '张三',
      phone: '13800138000'
      // age 未提供
    }
    
    const result = validator.validate(formData)
    expect(result.valid).toBe(true)
  })
  
  it('应该支持自定义验证函数', () => {
    const customRules = {
      password: [
        { required: true },
        { 
          validator: (value) => {
            if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
              return { valid: false, message: '密码必须包含大小写字母和数字' }
            }
            return { valid: true }
          }
        }
      ]
    }
    
    const validator = new FormValidator(customRules)
    
    const result1 = validator.validate({ password: '123456' })
    expect(result1.valid).toBe(false)
    
    const result2 = validator.validate({ password: 'Abc123' })
    expect(result2.valid).toBe(true)
  })
  
  it('应该支持获取单个字段错误', () => {
    const validator = new FormValidator(rules)
    const formData = {
      username: '',
      phone: '13800138000'
    }
    
    validator.validate(formData)
    const error = validator.getFieldError('username')
    expect(error).toBeDefined()
    expect(error.length).toBeGreaterThan(0)
  })
  
  it('应该支持清除错误', () => {
    const validator = new FormValidator(rules)
    validator.validate({ username: '' })
    
    expect(Object.keys(validator.errors).length).toBeGreaterThan(0)
    
    validator.clearErrors()
    expect(Object.keys(validator.errors).length).toBe(0)
  })
})

describe('commonRules - 常用验证规则', () => {
  it('应该包含用户名规则', () => {
    expect(commonRules.username).toBeDefined()
    expect(commonRules.username.length).toBeGreaterThan(0)
  })
  
  it('应该包含密码规则', () => {
    expect(commonRules.password).toBeDefined()
    expect(commonRules.password.some(r => r.pattern)).toBe(true)
  })
  
  it('应该包含手机号规则', () => {
    expect(commonRules.phone).toBeDefined()
  })
  
  it('应该包含身份证规则', () => {
    expect(commonRules.idCard).toBeDefined()
  })
  
  it('应该包含邮箱规则', () => {
    expect(commonRules.email).toBeDefined()
  })
  
  it('应该包含车牌号规则', () => {
    expect(commonRules.vehicleNumber).toBeDefined()
  })
  
  it('应该包含金额规则', () => {
    expect(commonRules.amount).toBeDefined()
  })
})

describe('form-validation - 边界情况测试', () => {
  it('应该处理null和undefined', () => {
    expect(validatePhone(null).valid).toBe(false)
    expect(validatePhone(undefined).valid).toBe(false)
    expect(validateEmail(null).valid).toBe(false)
  })
  
  it('应该处理空白字符串', () => {
    expect(validateRequired('   ').valid).toBe(false)
    expect(validateLength('   ', 1, 5).valid).toBe(true) // 空值跳过长度验证
  })
  
  it('应该处理特殊字符', () => {
    const result = validateEmail('test@domain.中国')
    // 根据实际需求决定是否支持国际化域名
  })
})

describe('form-validation - 性能测试', () => {
  it('应该在合理时间内完成大量验证', () => {
    const validator = new FormValidator({
      field1: [{ required: true }, { minLength: 2, maxLength: 100 }],
      field2: [{ pattern: /^1[3-9]\d{9}$/ }]
    })
    
    const startTime = Date.now()
    
    for (let i = 0; i < 1000; i++) {
      validator.validate({
        field1: 'test value',
        field2: '13800138000'
      })
    }
    
    const endTime = Date.now()
    const duration = endTime - startTime
    
    // 1000次验证应在100ms内完成
    expect(duration).toBeLessThan(100)
  })
})

