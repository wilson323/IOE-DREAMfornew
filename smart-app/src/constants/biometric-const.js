/**
 * 生物识别常量定义
 *
 * @author AI
 */
export const BIOMETRIC_TYPE_OPTIONS = [
  {
    label: '人脸识别',
    value: 'FACE',
    desc: '支持活体检测的人脸识别',
  },
  {
    label: '指纹识别',
    value: 'FINGERPRINT',
    desc: '用于手机指纹传感器的快速验证',
  },
  {
    label: '掌纹识别',
    value: 'PALMPRINT',
    desc: '掌纹/掌静脉联合认证',
  },
  {
    label: '虹膜识别',
    value: 'IRIS',
    desc: '高安全级别虹膜认证',
  },
];

export const SECURITY_LEVEL_OPTIONS = [
  { label: 'LOW', value: 'LOW' },
  { label: 'MEDIUM', value: 'MEDIUM' },
  { label: 'HIGH', value: 'HIGH' },
  { label: 'CRITICAL', value: 'CRITICAL' },
];

