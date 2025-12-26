# 协议PDF文档实现指南

## 📋 概述

本文档指导如何根据PDF协议文档完善协议解析的字段映射和校验和验证算法。

**参考文档**：
- 考勤PUSH通讯协议 （熵基科技） V4.0-20210113
- 安防PUSH通讯协议 （熵基科技）V4.8-20240107
- 消费PUSH通讯协议 （中控智慧） V1.0-20181225

---

## 🔍 第一步：分析PDF协议文档

### 需要提取的关键信息

#### 1. 协议头格式
- [ ] 协议头字节序列（例如：`0xAA 0x55`）
- [ ] 协议头长度（字节数）
- [ ] 协议头位置（通常在消息开头）

#### 2. 消息结构
- [ ] 消息总长度（固定长度或可变长度）
- [ ] 消息体结构（字段顺序、字段长度、字段类型）
- [ ] 字节序（大端序/小端序）

#### 3. 字段定义
对于每种消息类型，需要提取：
- [ ] 字段名称
- [ ] 字段位置（偏移量）
- [ ] 字段长度（字节数）
- [ ] 字段类型（字节、短整型、整型、字符串等）
- [ ] 字段含义（业务含义）

#### 4. 校验和算法
- [ ] 校验和算法类型（累加和、CRC、异或等）
- [ ] 校验和位置（消息末尾或特定位置）
- [ ] 校验和长度（字节数）
- [ ] 校验和计算范围（哪些字节参与计算）

#### 5. 消息类型
- [ ] 消息类型定义（命令码、功能码等）
- [ ] 消息类型位置
- [ ] 消息类型长度

---

## 📝 第二步：完善字段解析

### 考勤协议（AttendanceProtocolHandler）

#### 需要完善的字段（根据PDF文档）

**考勤记录消息（ATTENDANCE_RECORD）**：
```java
// 根据PDF文档填写以下字段的解析逻辑
// 位置：parseMessage方法中的消息体解析部分

// 示例字段（需要根据实际PDF文档调整）：
// 1. 用户ID（位置：offset，长度：4字节，类型：int）
int userId = buffer.getInt();

// 2. 打卡时间（位置：offset，长度：4字节，类型：Unix时间戳）
int punchTime = buffer.getInt();

// 3. 打卡类型（位置：offset，长度：1字节，类型：byte）
// 0-上班，1-下班，2-外出，3-返回等
byte punchType = buffer.get();

// 4. 打卡方式（位置：offset，长度：1字节，类型：byte）
// 0-卡片，1-人脸，2-指纹等
byte punchMethod = buffer.get();

// 5. 其他字段（根据PDF文档添加）
// - 温度（如果有）
// - 位置信息（经纬度）
// - 照片URL
// - 备注信息
```

#### 实现步骤

1. **打开PDF文档**，找到"考勤记录消息格式"章节
2. **记录字段定义表**，包括：
   - 字段名称
   - 字节偏移
   - 字节长度
   - 数据类型
   - 取值范围
3. **更新代码**，在`AttendanceProtocolHandler.parseMessage()`方法中：
   ```java
   // 根据PDF文档的字段定义，按顺序解析
   if (messageType == 0x01) { // 考勤记录消息类型码（根据PDF文档确定）
       // 解析用户ID（根据PDF文档的位置和长度）
       int userId = buffer.getInt(); // 或 buffer.getShort()，根据PDF文档
       data.put("userId", userId);
       
       // 解析打卡时间（根据PDF文档的格式）
       int timestamp = buffer.getInt(); // 或使用其他格式
       data.put("punchTime", timestamp);
       
       // ... 继续解析其他字段
   }
   ```

---

### 门禁协议（AccessProtocolHandler）

#### 需要完善的字段（根据PDF文档）

**门禁事件消息（ACCESS_RECORD）**：
```java
// 根据PDF文档填写以下字段的解析逻辑

// 示例字段（需要根据实际PDF文档调整）：
// 1. 用户ID
int userId = buffer.getInt();

// 2. 通行时间
int passTime = buffer.getInt();

// 3. 通行类型（0-进入，1-离开）
byte passType = buffer.get();

// 4. 门号
byte doorNo = buffer.get();

// 5. 通行方式（0-卡片，1-人脸，2-指纹等）
byte passMethod = buffer.get();

// 6. 通行结果（0-失败，1-成功）
byte accessResult = buffer.get();

// 7. 其他字段（根据PDF文档添加）
// - 温度
// - 照片URL
// - 报警类型（如果有）
```

**报警事件消息（ALARM_EVENT）**：
```java
// 根据PDF文档填写报警事件的字段解析
// 1. 报警类型
// 2. 报警时间
// 3. 报警级别
// 4. 报警描述
```

#### 实现步骤

1. **打开PDF文档**，找到"门禁事件消息格式"和"报警事件消息格式"章节
2. **记录字段定义**，包括所有字段的详细信息
3. **更新代码**，在`AccessProtocolHandler.parseMessage()`方法中完善字段解析

---

### 消费协议（ConsumeProtocolHandler）

#### 需要完善的字段（根据PDF文档）

**消费记录消息（CONSUME_RECORD）**：
```java
// 根据PDF文档填写以下字段的解析逻辑

// 示例字段（需要根据实际PDF文档调整）：
// 1. 用户ID
int userId = buffer.getInt();

// 2. 消费金额（单位：分）
int amount = buffer.getInt(); // 或使用其他格式

// 3. 消费时间
int consumeTime = buffer.getInt();

// 4. 消费类型（0-刷卡，1-刷脸，2-NFC等）
byte consumeType = buffer.get();

// 5. 消费结果（0-失败，1-成功）
byte consumeResult = buffer.get();

// 6. 其他字段（根据PDF文档添加）
// - 余额（消费后余额）
// - 交易流水号
// - 设备编号
```

**余额查询消息（BALANCE_QUERY）**：
```java
// 根据PDF文档填写余额查询的字段解析
// 1. 用户ID
// 2. 查询时间
// 3. 其他查询参数
```

#### 实现步骤

1. **打开PDF文档**，找到"消费记录消息格式"和"余额查询消息格式"章节
2. **记录字段定义**，特别注意金额的格式（分/元）
3. **更新代码**，在`ConsumeProtocolHandler.parseMessage()`方法中完善字段解析

---

## 🔐 第三步：实现校验和验证算法

### 常见校验和算法

#### 1. 累加和校验（Sum Check）
```java
/**
 * 累加和校验
 * 计算指定范围内所有字节的累加和
 * 
 * @param data 数据字节数组
 * @param start 起始位置
 * @param end 结束位置（不包含）
 * @return 校验和值（通常取低16位或低8位）
 */
private int calculateSumCheck(byte[] data, int start, int end) {
    int sum = 0;
    for (int i = start; i < end; i++) {
        sum += data[i] & 0xFF;
    }
    return sum & 0xFFFF; // 取低16位，或使用 & 0xFF 取低8位
}
```

#### 2. CRC校验（Cyclic Redundancy Check）
```java
/**
 * CRC16校验
 * 使用CRC16-CCITT算法（或其他算法，根据PDF文档确定）
 * 
 * @param data 数据字节数组
 * @param start 起始位置
 * @param end 结束位置（不包含）
 * @return CRC16校验值
 */
private int calculateCRC16(byte[] data, int start, int end) {
    int crc = 0xFFFF; // 初始值（根据PDF文档确定）
    int polynomial = 0x1021; // CRC16-CCITT多项式（根据PDF文档确定）
    
    for (int i = start; i < end; i++) {
        crc ^= (data[i] & 0xFF) << 8;
        for (int j = 0; j < 8; j++) {
            if ((crc & 0x8000) != 0) {
                crc = (crc << 1) ^ polynomial;
            } else {
                crc <<= 1;
            }
        }
    }
    
    return crc & 0xFFFF;
}
```

#### 3. 异或校验（XOR Check）
```java
/**
 * 异或校验
 * 计算指定范围内所有字节的异或值
 * 
 * @param data 数据字节数组
 * @param start 起始位置
 * @param end 结束位置（不包含）
 * @return 异或校验值
 */
private int calculateXORCheck(byte[] data, int start, int end) {
    int xor = 0;
    for (int i = start; i < end; i++) {
        xor ^= data[i] & 0xFF;
    }
    return xor & 0xFF;
}
```

### 实现步骤

1. **打开PDF文档**，找到"校验和算法"或"数据校验"章节
2. **确定校验算法类型**：
   - 累加和校验
   - CRC校验（CRC16/CRC32）
   - 异或校验
   - 其他算法
3. **确定校验和位置**：
   - 消息末尾
   - 消息头中
   - 特定位置
4. **确定校验和长度**：
   - 1字节（8位）
   - 2字节（16位）
   - 4字节（32位）
5. **确定计算范围**：
   - 从协议头后到校验和前
   - 从某个字段开始到校验和前
   - 整个消息（不包括校验和本身）
6. **更新代码**，在`validateChecksum()`方法中实现正确的算法

---

## 📋 实现检查清单

### 考勤协议（AttendanceProtocolHandler）

- [ ] 协议头格式已根据PDF文档确认
- [ ] 消息类型定义已根据PDF文档确认
- [ ] 考勤记录消息的所有字段已解析
- [ ] 字段位置、长度、类型已根据PDF文档确认
- [ ] 校验和算法已根据PDF文档实现
- [ ] 校验和位置和长度已根据PDF文档确认
- [ ] 字节序（大端/小端）已根据PDF文档确认

### 门禁协议（AccessProtocolHandler）

- [ ] 协议头格式已根据PDF文档确认
- [ ] 消息类型定义已根据PDF文档确认
- [ ] 门禁事件消息的所有字段已解析
- [ ] 报警事件消息的所有字段已解析
- [ ] 设备状态消息的所有字段已解析
- [ ] 字段位置、长度、类型已根据PDF文档确认
- [ ] 校验和算法已根据PDF文档实现
- [ ] 校验和位置和长度已根据PDF文档确认
- [ ] 字节序（大端/小端）已根据PDF文档确认

### 消费协议（ConsumeProtocolHandler）

- [ ] 协议头格式已根据PDF文档确认
- [ ] 消息类型定义已根据PDF文档确认
- [ ] 消费记录消息的所有字段已解析
- [ ] 余额查询消息的所有字段已解析
- [ ] 设备状态消息的所有字段已解析
- [ ] 字段位置、长度、类型已根据PDF文档确认
- [ ] 金额格式（分/元）已根据PDF文档确认
- [ ] 校验和算法已根据PDF文档实现
- [ ] 校验和位置和长度已根据PDF文档确认
- [ ] 字节序（大端/小端）已根据PDF文档确认

---

## 🛠️ 代码模板

### 字段解析模板

```java
@Override
public ProtocolMessage parseMessage(byte[] rawData) throws ProtocolParseException {
    // ... 协议头验证 ...
    
    ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN); // 或 BIG_ENDIAN，根据PDF文档
    buffer.position(PROTOCOL_HEADER.length); // 跳过协议头
    
    // 解析消息类型（根据PDF文档的位置和长度）
    int messageType = buffer.get() & 0xFF; // 或 buffer.getShort()，根据PDF文档
    
    // 解析设备编号（根据PDF文档的长度）
    byte[] deviceCodeBytes = new byte[DEVICE_CODE_LENGTH]; // 根据PDF文档确定长度
    buffer.get(deviceCodeBytes);
    String deviceCode = new String(deviceCodeBytes).trim();
    
    // 根据消息类型解析不同的字段
    Map<String, Object> data = new HashMap<>();
    
    if (messageType == MESSAGE_TYPE_CODE) { // 根据PDF文档确定消息类型码
        // 字段1：根据PDF文档的位置、长度、类型解析
        int field1 = buffer.getInt(); // 或 buffer.getShort()、buffer.get()等
        data.put("field1", field1);
        
        // 字段2：继续解析
        // ...
    }
    
    message.setData(data);
    return message;
}
```

### 校验和验证模板

```java
private boolean validateChecksum(byte[] data) {
    if (data == null || data.length < MIN_MESSAGE_LENGTH) {
        return false;
    }
    
    // 根据PDF文档确定校验和位置
    int checksumPosition = data.length - CHECKSUM_LENGTH; // 或根据PDF文档确定位置
    
    // 根据PDF文档确定计算范围
    int calculateStart = PROTOCOL_HEADER.length; // 或根据PDF文档确定
    int calculateEnd = checksumPosition; // 或根据PDF文档确定
    
    // 根据PDF文档实现校验算法
    int calculatedChecksum = calculateChecksum(data, calculateStart, calculateEnd);
    
    // 根据PDF文档读取消息中的校验和
    int receivedChecksum = readChecksum(data, checksumPosition, CHECKSUM_LENGTH);
    
    // 比较校验和
    boolean isValid = (calculatedChecksum == receivedChecksum);
    
    if (!isValid) {
        log.warn("[协议] 校验和验证失败，计算值={}, 接收值={}", calculatedChecksum, receivedChecksum);
    }
    
    return isValid;
}

/**
 * 根据PDF文档实现校验算法
 */
private int calculateChecksum(byte[] data, int start, int end) {
    // 根据PDF文档选择算法：
    // 1. 累加和校验
    // 2. CRC校验
    // 3. 异或校验
    // 4. 其他算法
    
    // 示例：累加和校验
    int sum = 0;
    for (int i = start; i < end; i++) {
        sum += data[i] & 0xFF;
    }
    return sum & 0xFFFF; // 或 & 0xFF，根据PDF文档
}

/**
 * 根据PDF文档读取校验和
 */
private int readChecksum(byte[] data, int position, int length) {
    // 根据PDF文档的字节序（大端/小端）读取
    if (length == 1) {
        return data[position] & 0xFF;
    } else if (length == 2) {
        // 小端序
        return (data[position] & 0xFF) | ((data[position + 1] & 0xFF) << 8);
        // 或大端序
        // return ((data[position] & 0xFF) << 8) | (data[position + 1] & 0xFF);
    }
    // ... 其他长度
    return 0;
}
```

---

## 📚 参考资源

### 协议文档位置
- `docs/各个设备通讯协议/考勤PUSH通讯协议 （熵基科技） V4.0-20210113(水印版).pdf`
- `docs/各个设备通讯协议/安防PUSH通讯协议 （熵基科技）V4.8-20240107(水印版).pdf`
- `docs/各个设备通讯协议/消费PUSH通讯协议 （中控智慧） V1.0-20181225.pdf`

### 需要修改的代码文件
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/devicecomm/protocol/handler/impl/AttendanceProtocolHandler.java`
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/devicecomm/protocol/handler/impl/AccessProtocolHandler.java`
- `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/devicecomm/protocol/handler/impl/ConsumeProtocolHandler.java`

---

## ⚠️ 注意事项

1. **字节序**：注意大端序（Big-Endian）和小端序（Little-Endian）的区别
2. **字段对齐**：注意字段是否按字节对齐
3. **字符串编码**：注意字符串字段的编码格式（ASCII、UTF-8等）
4. **时间格式**：注意时间字段的格式（Unix时间戳、BCD码等）
5. **数值格式**：注意数值字段的格式（有符号/无符号、整数/浮点数）
6. **测试验证**：实现后需要使用实际设备数据测试验证

---

**文档维护**: IOE-DREAM Team  
**最后更新**: 2025-01-30

