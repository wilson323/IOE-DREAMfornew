/**
 * 访客表单验证工具
 * 提供预约表单、访客信息、物品寄存等表单的验证功能
 */

// ==================== 通用验证规则 ====================

/**
 * 验证手机号
 * @param {string} phone 手机号
 * @returns {boolean} 是否有效
 */
export function validatePhoneNumber(phone) {
  if (!phone) return false;
  const phoneReg = /^1[3-9]\d{9}$/;
  return phoneReg.test(phone);
}

/**
 * 验证身份证号
 * @param {string} idCard 身份证号
 * @returns {boolean} 是否有效
 */
export function validateIdCard(idCard) {
  if (!idCard) return false;

  // 15位或18位身份证号码
  const idCardReg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  if (!idCardReg.test(idCard)) {
    return false;
  }

  // 18位身份证校验
  if (idCard.length === 18) {
    const factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    const parity = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
    let sum = 0;
    let ai = 0;
    let wi = 0;

    for (let i = 0; i < 17; i++) {
      ai = parseInt(idCard[i]);
      wi = factor[i];
      sum += ai * wi;
    }

    const last = parity[sum % 11];
    if (last !== idCard[17].toUpperCase()) {
      return false;
    }
  }

  return true;
}

/**
 * 验证姓名
 * @param {string} name 姓名
 * @returns {boolean} 是否有效
 */
export function validateName(name) {
  if (!name) return false;
  // 中文姓名2-10个字符
  const nameReg = /^[\u4e00-\u9fa5]{2,10}$/;
  return nameReg.test(name);
}

/**
 * 验证车牌号
 * @param {string} plate 车牌号
 * @returns {boolean} 是否有效
 */
export function validateVehiclePlate(plate) {
  if (!plate) return false;
  // 普通车牌：省份简称+字母+5位数字或字母
  const plateReg = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z][A-Z][A-HJ-NP-Z0-9]{5}$/;
  return plateReg.test(plate);
}

/**
 * 验证日期格式
 * @param {string} dateStr 日期字符串
 * @returns {boolean} 是否有效
 */
export function validateDate(dateStr) {
  if (!dateStr) return false;
  const date = new Date(dateStr);
  return !isNaN(date.getTime());
}

/**
 * 验证未来日期
 * @param {string} dateStr 日期字符串
 * @returns {boolean} 是否是未来日期
 */
export function validateFutureDate(dateStr) {
  if (!validateDate(dateStr)) return false;
  const date = new Date(dateStr);
  const now = new Date();
  return date > now;
}

/**
 * 验证日期范围
 * @param {string} dateStr 日期字符串
 * @param {number} minDays 最小天数（相对今天）
 * @param {number} maxDays 最大天数（相对今天）
 * @returns {boolean} 是否在范围内
 */
export function validateDateRange(dateStr, minDays = 0, maxDays = 30) {
  if (!validateDate(dateStr)) return false;

  const date = new Date(dateStr);
  const now = new Date();
  const diffDays = Math.ceil((date - now) / (1000 * 60 * 60 * 24));

  return diffDays >= minDays && diffDays <= maxDays;
}

/**
 * 验证时间格式 HH:mm
 * @param {string} timeStr 时间字符串
 * @returns {boolean} 是否有效
 */
export function validateTime(timeStr) {
  if (!timeStr) return false;
  const timeReg = /^([01]\d|2[0-3]):([0-5]\d)$/;
  return timeReg.test(timeStr);
}

/**
 * 验证图片文件
 * @param {string} filePath 文件路径
 * @param {number} maxSize 最大文件大小（MB，默认5MB）
 * @returns {Promise<Object>} 验证结果
 */
export async function validateImageFile(filePath, maxSize = 5) {
  return new Promise((resolve) => {
    uni.getFileInfo({
      filePath: filePath,
      success: (res) => {
        const sizeMB = res.size / (1024 * 1024);

        // 检查文件大小
        if (sizeMB > maxSize) {
          resolve({
            valid: false,
            error: `图片大小不能超过${maxSize}MB`
          });
          return;
        }

        // 检查文件类型
        const fileType = res.type || '';
        const allowedTypes = ['jpg', 'jpeg', 'png', 'bmp', 'webp'];
        const isImage = allowedTypes.some(type => fileType.toLowerCase().includes(type));

        if (!isImage) {
          resolve({
            valid: false,
            error: '只支持jpg、png、bmp、webp格式的图片'
          });
          return;
        }

        resolve({
          valid: true
        });
      },
      fail: (error) => {
        resolve({
          valid: false,
          error: '无法读取文件信息'
        });
      }
    });
  });
}

// ==================== 预约表单验证 ====================

/**
 * 验证访客预约表单
 * @param {Object} formData 表单数据
 * @returns {Object} 验证结果
 */
export function validateAppointmentForm(formData) {
  const errors = [];

  // 访客姓名
  if (!formData.visitorName) {
    errors.push('请输入访客姓名');
  } else if (!validateName(formData.visitorName)) {
    errors.push('访客姓名格式不正确');
  }

  // 访客电话
  if (!formData.visitorPhone) {
    errors.push('请输入访客电话');
  } else if (!validatePhoneNumber(formData.visitorPhone)) {
    errors.push('访客电话格式不正确');
  }

  // 身份证号（可选）
  if (formData.visitorIdCard && !validateIdCard(formData.visitorIdCard)) {
    errors.push('身份证号格式不正确');
  }

  // 被访人ID
  if (!formData.visiteeId) {
    errors.push('请选择被访人');
  }

  // 预约类型
  if (!formData.appointmentType) {
    errors.push('请选择预约类型');
  }

  // 预约时间
  if (!formData.appointmentTime) {
    errors.push('请选择预约时间');
  } else if (!validateDate(formData.appointmentTime)) {
    errors.push('预约时间格式不正确');
  } else if (!validateFutureDate(formData.appointmentTime)) {
    errors.push('预约时间必须是未来时间');
  } else if (!validateDateRange(formData.appointmentTime, 0, 30)) {
    errors.push('预约时间只能在30天内');
  }

  // 预计到达时间（可选）
  if (formData.expectedArrivalTime && !validateTime(formData.expectedArrivalTime)) {
    errors.push('预计到达时间格式不正确');
  }

  // 拜访理由
  if (!formData.visitReason) {
    errors.push('请输入拜访理由');
  } else if (formData.visitReason.length < 5) {
    errors.push('拜访理由至少5个字符');
  } else if (formData.visitReason.length > 200) {
    errors.push('拜访理由不能超过200个字符');
  }

  // 访问区域（可选）
  if (formData.accessAreas && Array.isArray(formData.accessAreas)) {
    if (formData.accessAreas.length === 0) {
      errors.push('请选择至少一个访问区域');
    }
  }

  // 备注（可选）
  if (formData.remark && formData.remark.length > 500) {
    errors.push('备注不能超过500个字符');
  }

  return {
    valid: errors.length === 0,
    errors
  };
}

/**
 * 验证预约表单字段
 * @param {string} field 字段名称
 * @param {*} value 字段值
 * @returns {Object} 验证结果
 */
export function validateAppointmentField(field, value) {
  switch (field) {
    case 'visitorName':
      if (!value) {
        return { valid: false, error: '请输入访客姓名' };
      }
      if (!validateName(value)) {
        return { valid: false, error: '访客姓名格式不正确' };
      }
      return { valid: true };

    case 'visitorPhone':
      if (!value) {
        return { valid: false, error: '请输入访客电话' };
      }
      if (!validatePhoneNumber(value)) {
        return { valid: false, error: '访客电话格式不正确' };
      }
      return { valid: true };

    case 'visitorIdCard':
      if (value && !validateIdCard(value)) {
        return { valid: false, error: '身份证号格式不正确' };
      }
      return { valid: true };

    case 'visiteeId':
      if (!value) {
        return { valid: false, error: '请选择被访人' };
      }
      return { valid: true };

    case 'appointmentType':
      if (!value) {
        return { valid: false, error: '请选择预约类型' };
      }
      return { valid: true };

    case 'appointmentTime':
      if (!value) {
        return { valid: false, error: '请选择预约时间' };
      }
      if (!validateDate(value)) {
        return { valid: false, error: '预约时间格式不正确' };
      }
      if (!validateFutureDate(value)) {
        return { valid: false, error: '预约时间必须是未来时间' };
      }
      if (!validateDateRange(value, 0, 30)) {
        return { valid: false, error: '预约时间只能在30天内' };
      }
      return { valid: true };

    case 'visitReason':
      if (!value) {
        return { valid: false, error: '请输入拜访理由' };
      }
      if (value.length < 5) {
        return { valid: false, error: '拜访理由至少5个字符' };
      }
      if (value.length > 200) {
        return { valid: false, error: '拜访理由不能超过200个字符' };
      }
      return { valid: true };

    default:
      return { valid: true };
  }
}

// ==================== 物品寄存表单验证 ====================

/**
 * 验证物品寄存表单
 * @param {Object} formData 表单数据
 * @returns {Object} 验证结果
 */
export function validateItemDepositForm(formData) {
  const errors = [];

  // 物品名称
  if (!formData.itemName) {
    errors.push('请输入物品名称');
  } else if (formData.itemName.length < 2) {
    errors.push('物品名称至少2个字符');
  } else if (formData.itemName.length > 50) {
    errors.push('物品名称不能超过50个字符');
  }

  // 物品类型
  if (!formData.itemType) {
    errors.push('请选择物品类型');
  }

  // 物品数量
  if (!formData.itemCount) {
    errors.push('请输入物品数量');
  } else if (formData.itemCount <= 0) {
    errors.push('物品数量必须大于0');
  } else if (formData.itemCount > 100) {
    errors.push('物品数量不能超过100');
  }

  // 寄存人姓名
  if (!formData.depositorName) {
    errors.push('请输入寄存人姓名');
  } else if (!validateName(formData.depositorName)) {
    errors.push('寄存人姓名格式不正确');
  }

  // 寄存人电话
  if (!formData.depositorPhone) {
    errors.push('请输入寄存人电话');
  } else if (!validatePhoneNumber(formData.depositorPhone)) {
    errors.push('寄存人电话格式不正确');
  }

  // 物品描述（可选）
  if (formData.itemDescription && formData.itemDescription.length > 200) {
    errors.push('物品描述不能超过200个字符');
  }

  // 物品照片（可选，但如果提供则验证格式）
  if (formData.itemPhoto) {
    // 这里只是路径验证，实际文件验证在上传时进行
    if (typeof formData.itemPhoto !== 'string' || formData.itemPhoto.length === 0) {
      errors.push('物品照片路径不正确');
    }
  }

  return {
    valid: errors.length === 0,
    errors
  };
}

/**
 * 验证物品领取表单
 * @param {Object} formData 表单数据
 * @returns {Object} 验证结果
 */
export function validateItemPickupForm(formData) {
  const errors = [];

  // 物流单号
  if (!formData.logisticsNo) {
    errors.push('请输入物流单号');
  }

  // 领取人姓名
  if (!formData.pickupPersonName) {
    errors.push('请输入领取人姓名');
  } else if (!validateName(formData.pickupPersonName)) {
    errors.push('领取人姓名格式不正确');
  }

  // 领取人电话
  if (!formData.pickupPersonPhone) {
    errors.push('请输入领取人电话');
  } else if (!validatePhoneNumber(formData.pickupPersonPhone)) {
    errors.push('领取人电话格式不正确');
  }

  // 领取人身份证（可选）
  if (formData.pickupPersonIdCard && !validateIdCard(formData.pickupPersonIdCard)) {
    errors.push('领取人身份证号格式不正确');
  }

  return {
    valid: errors.length === 0,
    errors
  };
}

// ==================== 访客信息表单验证 ====================

/**
 * 验证访客信息表单
 * @param {Object} formData 表单数据
 * @returns {Object} 验证结果
 */
export function validateVisitorInfoForm(formData) {
  const errors = [];

  // 访客姓名
  if (!formData.visitorName) {
    errors.push('请输入访客姓名');
  } else if (!validateName(formData.visitorName)) {
    errors.push('访客姓名格式不正确');
  }

  // 访客电话
  if (!formData.visitorPhone) {
    errors.push('请输入访客电话');
  } else if (!validatePhoneNumber(formData.visitorPhone)) {
    errors.push('访客电话格式不正确');
  }

  // 身份证号（可选）
  if (formData.visitorIdCard && !validateIdCard(formData.visitorIdCard)) {
    errors.push('身份证号格式不正确');
  }

  // 公司名称（当访客类型是企业时必填）
  if (formData.visitorType === 'COMPANY' && !formData.companyName) {
    errors.push('企业访客必须填写公司名称');
  }

  // 身份证正面照片（可选）
  if (formData.idCardFrontPhoto) {
    if (typeof formData.idCardFrontPhoto !== 'string' || formData.idCardFrontPhoto.length === 0) {
      errors.push('身份证正面照片路径不正确');
    }
  }

  // 身份证背面照片（可选）
  if (formData.idCardBackPhoto) {
    if (typeof formData.idCardBackPhoto !== 'string' || formData.idCardBackPhoto.length === 0) {
      errors.push('身份证背面照片路径不正确');
    }
  }

  // 人脸照片（可选）
  if (formData.facePhoto) {
    if (typeof formData.facePhoto !== 'string' || formData.facePhoto.length === 0) {
      errors.push('人脸照片路径不正确');
    }
  }

  return {
    valid: errors.length === 0,
    errors
  };
}

// ==================== 批量验证工具 ====================

/**
 * 批量验证表单字段
 * @param {Object} formData 表单数据
 * @param {Array} fields 字段列表
 * @param {Function} validator 验证函数
 * @returns {Object} 验证结果
 */
export function validateFields(formData, fields, validator) {
  const errors = {};
  let valid = true;

  fields.forEach(field => {
    const result = validator(field, formData[field]);
    if (!result.valid) {
      errors[field] = result.error;
      valid = false;
    }
  });

  return {
    valid,
    errors
  };
}

/**
 * 获取第一个错误信息
 * @param {Object} errors 错误对象
 * @returns {string|null} 第一个错误信息
 */
export function getFirstError(errors) {
  if (typeof errors === 'string') {
    return errors;
  }

  if (Array.isArray(errors)) {
    return errors.length > 0 ? errors[0] : null;
  }

  if (typeof errors === 'object') {
    const keys = Object.keys(errors);
    return keys.length > 0 ? errors[keys[0]] : null;
  }

  return null;
}

/**
 * 显示验证错误提示
 * @param {Object} result 验证结果
 * @param {Function} showToast 显示Toast的函数（可选）
 */
export function showValidationError(result, showToast = uni.showToast) {
  if (!result.valid) {
    const firstError = getFirstError(result.errors);
    if (firstError) {
      showToast({
        title: firstError,
        icon: 'none',
        duration: 2000
      });
    }
  }
  return result.valid;
}

// ==================== 导出 ====================

export default {
  // 通用验证
  validatePhoneNumber,
  validateIdCard,
  validateName,
  validateVehiclePlate,
  validateDate,
  validateFutureDate,
  validateDateRange,
  validateTime,
  validateImageFile,

  // 预约表单验证
  validateAppointmentForm,
  validateAppointmentField,

  // 物品表单验证
  validateItemDepositForm,
  validateItemPickupForm,

  // 访客信息验证
  validateVisitorInfoForm,

  // 批量验证工具
  validateFields,
  getFirstError,
  showValidationError
};
