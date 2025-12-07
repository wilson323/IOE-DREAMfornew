# API文档模板

**版本**: v1.0.0  
**日期**: 2025-01-30  
**说明**: API文档编写模板和示例

---

## 📋 API文档结构

### 1. 接口基本信息

```markdown
## 接口名称

**接口路径**: `/api/v1/{module}/{resource}/{action}`

**请求方法**: `POST` / `GET` / `PUT` / `DELETE`

**接口描述**: 简要描述接口功能

**权限要求**: `ROLE_XXX` 或 `@PreAuthorize` 表达式
```

### 2. 请求参数

```markdown
### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| param1 | String | 是 | 参数说明 | "value1" |
| param2 | Integer | 否 | 参数说明 | 100 |

### 请求示例

```json
{
  "param1": "value1",
  "param2": 100
}
```
```

### 3. 响应结果

```markdown
### 响应结果

**成功响应** (200 OK):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 响应数据
  }
}
```

**错误响应** (400 Bad Request):

```json
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

### 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| 400 | 参数错误 | 检查请求参数 |
| 401 | 未授权 | 检查Token |
| 403 | 无权限 | 检查用户权限 |
| 500 | 服务器错误 | 联系管理员 |
```

---

## 📝 完整示例

### 创建银行支付订单

**接口路径**: `/api/v1/consume/payment/bank/createOrder`

**请求方法**: `POST`

**接口描述**: 创建银行支付订单，返回支付所需参数

**权限要求**: `CONSUME_MANAGER` 或 `CONSUME_USER`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| accountId | Long | 是 | 账户ID | 1001 |
| amount | BigDecimal | 是 | 支付金额（元） | 100.00 |
| orderId | String | 是 | 订单ID | "ORDER001" |
| description | String | 是 | 商品描述 | "测试订单" |
| bankCardNo | String | 否 | 银行卡号 | "6222021234567890" |

#### 请求示例

```bash
curl -X POST "http://localhost:8094/api/v1/consume/payment/bank/createOrder" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "accountId": 1001,
    "amount": 100.00,
    "orderId": "ORDER001",
    "description": "测试订单",
    "bankCardNo": "6222021234567890"
  }'
```

#### 响应结果

**成功响应** (200 OK):

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "tradeNo": "BANK20250130001",
    "message": "银行支付成功"
  }
}
```

**错误响应** (400 Bad Request):

```json
{
  "code": 400,
  "message": "创建银行支付订单失败: 账户不存在",
  "data": null
}
```

#### 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| 400 | 参数错误 | 检查请求参数是否完整 |
| 401 | 未授权 | 检查Token是否有效 |
| 403 | 无权限 | 检查用户是否有支付权限 |
| 500 | 服务器错误 | 联系管理员 |

---

## 🔧 Swagger/OpenAPI注解示例

```java
@PostMapping("/bank/createOrder")
@Operation(
    summary = "创建银行支付订单",
    description = "创建银行支付订单，返回支付所需参数",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "银行支付订单表单",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BankPaymentForm.class),
            examples = @ExampleObject(
                name = "示例",
                value = "{\"accountId\": 1001, \"amount\": 100.00, \"orderId\": \"ORDER001\"}"
            )
        )
    ),
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数错误"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器错误"
        )
    }
)
@PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
public ResponseDTO<Map<String, Object>> createBankPaymentOrder(
        @Parameter(description = "账户ID", required = true) @RequestParam @NotNull Long accountId,
        @Parameter(description = "支付金额", required = true) @RequestParam @NotNull BigDecimal amount,
        @Parameter(description = "订单ID", required = true) @RequestParam @NotBlank String orderId,
        @Parameter(description = "商品描述", required = true) @RequestParam @NotBlank String description,
        @Parameter(description = "银行卡号") @RequestParam(required = false) String bankCardNo) {
    // 实现逻辑
}
```

---

**使用此模板编写所有API接口文档**

