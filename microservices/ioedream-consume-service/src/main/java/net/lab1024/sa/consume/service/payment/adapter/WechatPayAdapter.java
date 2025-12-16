package net.lab1024.sa.consume.service.payment.adapter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.app.AppService;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * 微信支付渠道适配器
 * <p>
 * 仅承载微信 SDK/协议交互与签名校验逻辑，供 PaymentService 编排层调用。
 * </p>
 */
@Slf4j
@Component
@SuppressWarnings("null")
public class WechatPayAdapter {

    @Value("${wechat.pay.app-id:}")
    private String wechatAppId;

    @Value("${wechat.pay.mch-id:}")
    private String wechatMchId;

    @Value("${wechat.pay.api-key:}")
    private String wechatApiKey;

    @Value("${wechat.pay.cert-path:}")
    private String wechatCertPath;

    @Value("${wechat.pay.notify-url:}")
    private String wechatNotifyUrl;

    @Value("${wechat.pay.merchant-serial-number:}")
    private String wechatMerchantSerialNumber;

    @Value("${wechat.pay.api-v3-key:}")
    private String wechatApiV3Key;

    @Value("${wechat.pay.enabled:false}")
    private boolean wechatPayEnabled;

    private Config wechatPayConfig;
    private JsapiService jsapiService;
    private NativePayService nativePayService;
    private AppService appPayService;
    private H5Service h5PayService;
    private RefundService refundService;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    public boolean isEnabled() {
        return wechatPayEnabled;
    }

    public void initIfNeeded() {
        initWechatPayConfig();
    }

    public boolean isReady() {
        return wechatPayConfig != null;
    }

    /**
     * 创建微信支付订单
     */
    public Map<String, Object> createWechatPayOrder(
            String orderId,
            BigDecimal amount,
            String description,
            String openId,
            String payType) {

        log.info("[微信支付] 创建订单, orderId={}, amount={}, payType={}", orderId, amount, payType);

        try {
            if (!wechatPayEnabled) {
                log.warn("[微信支付] 支付服务未启用，返回模拟数据");
                return getMockWechatPayResult(orderId, payType);
            }

            // 1. 参数验证
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(description)) {
                throw new BusinessException("商品描述不能为空");
            }

            // 2. 初始化配置
            initWechatPayConfig();
            if (wechatPayConfig == null) {
                log.warn("[微信支付] 配置未初始化，返回模拟数据");
                return getMockWechatPayResult(orderId, payType);
            }

            // 3. 根据支付类型调用不同的服务
            if ("JSAPI".equals(payType)) {
                if (!StringUtils.hasText(openId)) {
                    throw new BusinessException("JSAPI支付需要openId");
                }
                return createJsapiPayOrder(orderId, amount, description, openId);
            } else if ("Native".equals(payType)) {
                return createNativePayOrder(orderId, amount, description);
            } else if ("APP".equals(payType)) {
                return createAppPayOrder(orderId, amount, description);
            } else if ("H5".equals(payType)) {
                return createH5PayOrder(orderId, amount, description);
            } else {
                throw new BusinessException("不支持的支付类型: " + payType);
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付] 创建订单参数错误: error={}", e.getMessage(), e);
            throw new ParamException("WECHAT_PAY_ORDER_PARAM_ERROR", "微信支付创建订单参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[微信支付] 创建订单业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[微信支付] 创建订单系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("WECHAT_PAY_ORDER_SYSTEM_ERROR", "微信支付创建订单失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[微信支付] 创建订单未知异常", e);
            throw new SystemException("WECHAT_PAY_ORDER_SYSTEM_ERROR", "微信支付创建订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 微信支付退款
     */
    public Map<String, Object> wechatRefund(
            String orderId,
            String refundId,
            Integer totalAmount,
            Integer refundAmount) {

        log.info("[微信退款] orderId={}, refundId={}, amount={}", orderId, refundId, refundAmount);

        try {
            // 1. 参数验证
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (!StringUtils.hasText(refundId)) {
                throw new BusinessException("退款单ID不能为空");
            }
            if (totalAmount == null || totalAmount <= 0) {
                throw new BusinessException("订单总金额必须大于0");
            }
            if (refundAmount == null || refundAmount <= 0) {
                throw new BusinessException("退款金额必须大于0");
            }
            if (refundAmount > totalAmount) {
                throw new BusinessException("退款金额不能大于订单总金额");
            }

            // 2. 初始化配置
            initWechatPayConfig();
            if (refundService == null) {
                log.warn("[微信退款] 服务未初始化，返回模拟数据");
                return Map.of("success", true, "refundId", refundId, "refundFee", refundAmount, "mock", true);
            }

            // 3. 构建退款请求
            CreateRequest refundRequest = new CreateRequest();
            refundRequest.setOutTradeNo(orderId);
            refundRequest.setOutRefundNo(refundId);
            refundRequest.setReason("用户申请退款");

            // 设置退款金额信息（使用AmountReq，需要Long类型）
            AmountReq refundAmountObj = new AmountReq();
            refundAmountObj.setTotal((long) totalAmount);
            refundAmountObj.setRefund((long) refundAmount);
            refundAmountObj.setCurrency("CNY");
            refundRequest.setAmount(refundAmountObj);

            // 4. 调用退款API（返回Refund类型）
            com.wechat.pay.java.service.refund.model.Refund refundResponse = refundService.create(refundRequest);

            // 5. 构建返回结果
            Map<String, Object> result = new HashMap<>();
            boolean isSuccess = refundResponse != null
                    && refundResponse.getStatus() != null
                    && "SUCCESS".equals(refundResponse.getStatus().toString());
            result.put("success", isSuccess);
            result.put("refundId", refundResponse != null ? refundResponse.getRefundId() : refundId);
            result.put("refundFee", refundAmount);
            result.put("mock", false);

            log.info("[微信退款] 退款成功: orderId={}, refundId={}, amount={}", orderId, refundId, refundAmount);
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信退款] 退款参数错误: error={}", e.getMessage(), e);
            throw new ParamException("WECHAT_REFUND_PARAM_ERROR", "微信退款参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[微信退款] 退款业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[微信退款] 退款系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("WECHAT_REFUND_SYSTEM_ERROR", "微信退款失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[微信退款] 退款未知异常", e);
            throw new SystemException("WECHAT_REFUND_SYSTEM_ERROR", "微信退款失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询单个微信支付订单
     */
    public Transaction queryWechatOrderByOutTradeNo(String outTradeNo) {
        try {
            if (wechatPayConfig == null) {
                log.warn("[支付对账] 微信支付配置未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }
            return queryWechatOrderByHttp(outTradeNo);
        } catch (Exception e) {
            log.error("[支付对账] 查询微信订单异常，outTradeNo={}", outTradeNo, e);
            return null;
        }
    }

    /**
     * 验证微信支付V3回调签名
     */
    public boolean verifyWechatPaySignature(String serialNumber, String timestamp,
            String nonce, String body, String signature) {
        try {
            String signString = timestamp + "\n" + nonce + "\n" + body + "\n";
            byte[] signBytes = signString.getBytes(StandardCharsets.UTF_8);
            byte[] signatureBytes = Base64.getDecoder().decode(signature);

            PublicKey publicKey = getWechatPayPlatformCertificate(serialNumber);
            if (publicKey == null) {
                boolean isProduction = !"dev".equals(System.getProperty("spring.profiles.active", "dev"));
                if (isProduction) {
                    log.error("[微信支付] 生产环境无法获取平台证书，签名验证失败，serialNumber={}", serialNumber);
                    return false;
                } else {
                    log.warn("[微信支付] 开发环境无法获取平台证书，跳过签名验证，serialNumber={}", serialNumber);
                    return true;
                }
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(signBytes);
            boolean isValid = sig.verify(signatureBytes);

            if (isValid) {
                log.info("[微信支付] 签名验证成功，serialNumber={}", serialNumber);
            } else {
                log.error("[微信支付] 签名验证失败，serialNumber={}", serialNumber);
            }

            return isValid;

        } catch (NoSuchAlgorithmException e) {
            log.error("[微信支付] 签名算法不支持", e);
            return false;
        } catch (InvalidKeyException e) {
            log.error("[微信支付] 证书公钥无效", e);
            return false;
        } catch (SignatureException e) {
            log.error("[微信支付] 签名验证异常", e);
            return false;
        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付] 签名验证参数错误: error={}", e.getMessage(), e);
            return false;
        } catch (BusinessException e) {
            log.error("[微信支付] 签名验证业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return false;
        } catch (SystemException e) {
            log.error("[微信支付] 签名验证系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[微信支付] 签名验证未知异常", e);
            return false;
        }
    }

    private void initWechatPayConfig() {
        if (wechatPayConfig != null) {
            return;
        }

        if (!wechatPayEnabled || !StringUtils.hasText(wechatMchId) ||
            !StringUtils.hasText(wechatCertPath) || !StringUtils.hasText(wechatMerchantSerialNumber) ||
            !StringUtils.hasText(wechatApiV3Key)) {
            log.warn("[微信支付] 配置不完整，无法初始化");
            return;
        }

        try {
            wechatPayConfig = new RSAAutoCertificateConfig.Builder()
                    .merchantId(wechatMchId)
                    .privateKeyFromPath(wechatCertPath)
                    .merchantSerialNumber(wechatMerchantSerialNumber)
                    .apiV3Key(wechatApiV3Key)
                    .build();

            jsapiService = new JsapiService.Builder().config(wechatPayConfig).build();
            nativePayService = new NativePayService.Builder().config(wechatPayConfig).build();
            appPayService = new AppService.Builder().config(wechatPayConfig).build();
            h5PayService = new H5Service.Builder().config(wechatPayConfig).build();
            refundService = new RefundService.Builder().config(wechatPayConfig).build();

            log.info("[微信支付] 配置初始化成功");
        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付] 配置初始化参数错误: error={}", e.getMessage(), e);
            throw new ParamException("WECHAT_PAY_CONFIG_PARAM_ERROR", "微信支付配置参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[微信支付] 配置初始化业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[微信支付] 配置初始化系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("WECHAT_PAY_CONFIG_SYSTEM_ERROR", "微信支付配置初始化失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[微信支付] 配置初始化未知异常", e);
            throw new SystemException("WECHAT_PAY_CONFIG_SYSTEM_ERROR", "微信支付配置初始化失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getMockWechatPayResult(String orderId, String payType) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("payType", payType);
        result.put("mock", true);

        if ("JSAPI".equals(payType)) {
            result.put("appId", wechatAppId);
            result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
            result.put("package", "prepay_id=wx20251205000000000000000000");
            result.put("signType", "RSA");
            result.put("paySign", "mock_sign_" + UUID.randomUUID().toString());
        } else if ("APP".equals(payType)) {
            result.put("appid", wechatAppId);
            result.put("partnerid", wechatMchId);
            result.put("prepayid", "wx20251205000000000000000000");
            result.put("package", "Sign=WXPay");
            result.put("noncestr", UUID.randomUUID().toString().replace("-", ""));
            result.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("sign", "mock_sign_" + UUID.randomUUID().toString());
        } else if ("Native".equals(payType)) {
            result.put("codeUrl", "weixin://wxpay/bizpayurl?pr=mock_code_url");
        }

        return result;
    }

    private Map<String, Object> createJsapiPayOrder(String orderId, BigDecimal amount,
            String description, String openId) {
        try {
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wechatAppId);
            request.setMchid(wechatMchId);
            request.setDescription(description);
            request.setOutTradeNo(orderId);
            request.setNotifyUrl(wechatNotifyUrl);

            Amount amountObj = new Amount();
            amountObj.setTotal(amount.multiply(new BigDecimal("100")).intValue());
            request.setAmount(amountObj);

            com.wechat.pay.java.service.payments.jsapi.model.Payer payer =
                    new com.wechat.pay.java.service.payments.jsapi.model.Payer();
            payer.setOpenid(openId);
            request.setPayer(payer);

            PrepayResponse response = jsapiService.prepay(request);

            Map<String, Object> result = new HashMap<>();
            result.put("appId", wechatAppId);
            result.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            result.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
            result.put("package", "prepay_id=" + response.getPrepayId());
            result.put("signType", "RSA");
            result.put("paySign", "需要SDK生成");
            result.put("prepayId", response.getPrepayId());
            result.put("mock", false);

            log.info("[微信支付JSAPI] 订单创建成功: orderId={}, prepayId={}", orderId, response.getPrepayId());
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付JSAPI] 创建订单参数错误: error={}", e.getMessage(), e);
            throw new ParamException("WECHAT_PAY_JSAPI_ORDER_PARAM_ERROR", "微信支付JSAPI创建订单参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[微信支付JSAPI] 创建订单业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[微信支付JSAPI] 创建订单系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("WECHAT_PAY_JSAPI_ORDER_SYSTEM_ERROR", "微信支付JSAPI创建订单失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[微信支付JSAPI] 创建订单未知异常", e);
            throw new SystemException("WECHAT_PAY_JSAPI_ORDER_SYSTEM_ERROR", "微信支付JSAPI创建订单失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createNativePayOrder(String orderId, BigDecimal amount, String description) {
        try {
            com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
            request.setAppid(wechatAppId);
            request.setMchid(wechatMchId);
            request.setDescription(description);
            request.setOutTradeNo(orderId);
            request.setNotifyUrl(wechatNotifyUrl);

            com.wechat.pay.java.service.payments.nativepay.model.Amount amountObj =
                    new com.wechat.pay.java.service.payments.nativepay.model.Amount();
            amountObj.setTotal(amount.multiply(new BigDecimal("100")).intValue());
            request.setAmount(amountObj);

            com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse response =
                    nativePayService.prepay(request);

            Map<String, Object> result = new HashMap<>();
            result.put("codeUrl", response.getCodeUrl());
            result.put("orderId", orderId);
            result.put("mock", false);

            log.info("[微信支付Native] 订单创建成功: orderId={}, codeUrl={}", orderId, response.getCodeUrl());
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付Native] 创建订单参数错误: error={}", e.getMessage(), e);
            throw new ParamException("WECHAT_PAY_NATIVE_ORDER_PARAM_ERROR", "微信支付Native创建订单参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[微信支付Native] 创建订单业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[微信支付Native] 创建订单系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("WECHAT_PAY_NATIVE_ORDER_SYSTEM_ERROR", "微信支付Native创建订单失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[微信支付Native] 创建订单未知异常", e);
            throw new SystemException("WECHAT_PAY_NATIVE_ORDER_SYSTEM_ERROR", "微信支付Native创建订单失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createAppPayOrder(String orderId, BigDecimal amount, String description) {
        try {
            com.wechat.pay.java.service.payments.app.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.app.model.PrepayRequest();
            request.setAppid(wechatAppId);
            request.setMchid(wechatMchId);
            request.setDescription(description);
            request.setOutTradeNo(orderId);
            request.setNotifyUrl(wechatNotifyUrl);

            com.wechat.pay.java.service.payments.app.model.Amount amountObj =
                    new com.wechat.pay.java.service.payments.app.model.Amount();
            amountObj.setTotal(amount.multiply(new BigDecimal("100")).intValue());
            request.setAmount(amountObj);

            com.wechat.pay.java.service.payments.app.model.PrepayResponse response =
                    appPayService.prepay(request);

            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String packageValue = "Sign=WXPay";

            Map<String, Object> result = new HashMap<>();
            result.put("prepayId", response.getPrepayId());
            result.put("appId", wechatAppId);
            result.put("partnerId", wechatMchId);
            result.put("packageValue", packageValue);
            result.put("nonceStr", nonceStr);
            result.put("timeStamp", timeStamp);

            String sign = generateAppPaySignature(wechatAppId, wechatMchId, response.getPrepayId(),
                    packageValue, nonceStr, timeStamp);
            result.put("sign", sign);
            result.put("orderId", orderId);
            result.put("mock", false);

            log.info("[微信支付APP] 订单创建成功: orderId={}, prepayId={}", orderId, response.getPrepayId());
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付APP] 创建订单参数错误: error={}", e.getMessage(), e);
            throw new ParamException("WECHAT_PAY_APP_ORDER_PARAM_ERROR", "微信支付APP创建订单参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[微信支付APP] 创建订单业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[微信支付APP] 创建订单系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("WECHAT_PAY_APP_ORDER_SYSTEM_ERROR", "微信支付APP创建订单失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[微信支付APP] 创建订单未知异常", e);
            throw new SystemException("WECHAT_PAY_APP_ORDER_SYSTEM_ERROR", "微信支付APP创建订单失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createH5PayOrder(String orderId, BigDecimal amount, String description) {
        try {
            com.wechat.pay.java.service.payments.h5.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
            request.setAppid(wechatAppId);
            request.setMchid(wechatMchId);
            request.setDescription(description);
            request.setOutTradeNo(orderId);
            request.setNotifyUrl(wechatNotifyUrl);

            com.wechat.pay.java.service.payments.h5.model.Amount amountObj =
                    new com.wechat.pay.java.service.payments.h5.model.Amount();
            amountObj.setTotal(amount.multiply(new BigDecimal("100")).intValue());
            request.setAmount(amountObj);

            com.wechat.pay.java.service.payments.h5.model.SceneInfo sceneInfo =
                    new com.wechat.pay.java.service.payments.h5.model.SceneInfo();
            sceneInfo.setPayerClientIp("127.0.0.1");
            com.wechat.pay.java.service.payments.h5.model.H5Info h5Info =
                    new com.wechat.pay.java.service.payments.h5.model.H5Info();
            h5Info.setType("Wap");
            sceneInfo.setH5Info(h5Info);
            request.setSceneInfo(sceneInfo);

            com.wechat.pay.java.service.payments.h5.model.PrepayResponse response =
                    h5PayService.prepay(request);

            Map<String, Object> result = new HashMap<>();
            result.put("h5Url", response.getH5Url());
            result.put("orderId", orderId);
            result.put("mock", false);

            log.info("[微信支付H5] 订单创建成功: orderId={}, h5Url={}", orderId, response.getH5Url());
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付H5] 创建订单参数错误: error={}", e.getMessage(), e);
            throw new ParamException("WECHAT_PAY_H5_ORDER_PARAM_ERROR", "微信支付H5创建订单参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[微信支付H5] 创建订单业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (SystemException e) {
            log.error("[微信支付H5] 创建订单系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("WECHAT_PAY_H5_ORDER_SYSTEM_ERROR", "微信支付H5创建订单失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[微信支付H5] 创建订单未知异常", e);
            throw new SystemException("WECHAT_PAY_H5_ORDER_SYSTEM_ERROR", "微信支付H5创建订单失败: " + e.getMessage(), e);
        }
    }

    private Transaction queryWechatOrderByHttp(String outTradeNo) {
        try {
            if (wechatPayConfig == null) {
                log.warn("[支付对账] 微信支付配置未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;
            }

            try {
                if (jsapiService != null) {
                    try {
                        String apiPath = "/v3/pay/transactions/out-trade-no/" + outTradeNo + "?mchid=" + wechatMchId;
                        String apiUrl = "https://api.mch.weixin.qq.com" + apiPath;

                        log.info("[支付对账] 手动调用微信支付订单查询接口：{}", apiUrl);

                        try {
                            String authorization = buildWechatPayAuthorization("GET", apiPath, "");

                            HttpHeaders headers = new HttpHeaders();
                            headers.set("Authorization", authorization);
                            headers.set("Accept", "application/json");
                            headers.set("User-Agent", "IOE-DREAM-PaymentService/1.0");

                            HttpEntity<String> entity = new HttpEntity<>(headers);
                            ResponseEntity<String> response = restTemplate.exchange(
                                    apiUrl,
                                    HttpMethod.GET,
                                    entity,
                                    String.class
                            );

                            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(),
                                        new TypeReference<Map<String, Object>>() {});

                                Transaction transaction = new Transaction();
                                if (responseMap.containsKey("out_trade_no")) {
                                    transaction.setOutTradeNo((String) responseMap.get("out_trade_no"));
                                }
                                if (responseMap.containsKey("transaction_id")) {
                                    transaction.setTransactionId((String) responseMap.get("transaction_id"));
                                }
                                if (responseMap.containsKey("trade_state")) {
                                    String tradeStateStr = (String) responseMap.get("trade_state");
                                    try {
                                        Transaction.TradeStateEnum tradeStateEnum =
                                                Transaction.TradeStateEnum.valueOf(tradeStateStr);
                                        transaction.setTradeState(tradeStateEnum);
                                    } catch (IllegalArgumentException e) {
                                        log.warn("[支付对账] 无法识别的交易状态：{}", tradeStateStr);
                                    }
                                }

                                log.info("[支付对账] 微信支付订单查询成功，outTradeNo={}, tradeState={}",
                                        outTradeNo, transaction.getTradeState() != null ? transaction.getTradeState().name() : "null");
                                return transaction;
                            } else {
                                log.warn("[支付对账] 微信支付订单查询失败，状态码={}, 响应={}",
                                        response.getStatusCode(), response.getBody());
                                return null;
                            }

                        } catch (Exception e) {
                            log.error("[支付对账] 手动HTTP请求查询微信订单失败，outTradeNo={}", outTradeNo, e);
                            return null;
                        }

                    } catch (Exception e) {
                        log.error("[支付对账] 使用微信支付SDK查询订单失败，outTradeNo={}", outTradeNo, e);
                        return null;
                    }
                }

                if (nativePayService != null) {
                    log.info("[支付对账] 尝试使用NativePayService查询订单，outTradeNo={}", outTradeNo);
                    return null;
                }

                log.warn("[支付对账] 微信支付服务未初始化，无法查询订单，outTradeNo={}", outTradeNo);
                return null;

            } catch (Exception e) {
                log.error("[支付对账] 使用微信支付SDK查询订单异常，outTradeNo={}", outTradeNo, e);
                return null;
            }

        } catch (Exception e) {
            log.error("[支付对账] HTTP客户端查询微信订单失败，outTradeNo={}", outTradeNo, e);
            return null;
        }
    }

    private String buildWechatPayAuthorization(String method, String urlPath, String body) {
        try {
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            String signString = method + "\n" + urlPath + "\n" + timestamp + "\n" + nonceStr + "\n" + (body != null ? body : "") + "\n";
            byte[] signBytes = signString.getBytes(StandardCharsets.UTF_8);

            java.security.PrivateKey privateKey = getWechatPayPrivateKey();
            if (privateKey == null) {
                log.error("[微信支付] 无法获取私钥，无法构建Authorization请求头");
                throw new BusinessException("无法获取微信支付私钥");
            }

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signBytes);
            byte[] signatureBytes = signature.sign();

            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

            String authorization = String.format(
                    "WECHATPAY-SHA256-RSA2048 mchid=\"%s\",nonce_str=\"%s\",timestamp=\"%s\",signature=\"%s\",serial_no=\"%s\"",
                    wechatMchId, nonceStr, timestamp, signatureBase64, wechatMerchantSerialNumber
            );

            log.debug("[微信支付] Authorization请求头构建成功");
            return authorization;

        } catch (NoSuchAlgorithmException e) {
            log.error("[微信支付] 签名算法不支持", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("[微信支付] 私钥无效", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        } catch (SignatureException e) {
            log.error("[微信支付] 签名异常", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[微信支付] 构建Authorization请求头异常", e);
            throw new BusinessException("构建Authorization请求头失败：" + e.getMessage());
        }
    }

    private java.security.PrivateKey getWechatPayPrivateKey() {
        try {
            if (wechatPayConfig instanceof RSAAutoCertificateConfig) {
                RSAAutoCertificateConfig rsaConfig = (RSAAutoCertificateConfig) wechatPayConfig;

                try {
                    java.lang.reflect.Method getPrivateKeyMethod = RSAAutoCertificateConfig.class
                            .getDeclaredMethod("getPrivateKey");
                    getPrivateKeyMethod.setAccessible(true);
                    Object privateKey = getPrivateKeyMethod.invoke(rsaConfig);

                    if (privateKey instanceof java.security.PrivateKey) {
                        log.debug("[微信支付] 成功获取私钥");
                        return (java.security.PrivateKey) privateKey;
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("[微信支付] 无法通过反射获取私钥，尝试从证书路径读取");
                } catch (Exception e) {
                    log.debug("[微信支付] 获取私钥失败，错误：{}", e.getMessage());
                }

                if (StringUtils.hasText(wechatCertPath)) {
                    try {
                        java.nio.file.Path certPath = java.nio.file.Paths.get(wechatCertPath);
                        String privateKeyContent = new String(java.nio.file.Files.readAllBytes(certPath), StandardCharsets.UTF_8);

                        privateKeyContent = privateKeyContent.replace("-----BEGIN PRIVATE KEY-----", "")
                                .replace("-----END PRIVATE KEY-----", "")
                                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                                .replace("-----END RSA PRIVATE KEY-----", "")
                                .replaceAll("\\s", "");

                        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

                        java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
                        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
                        java.security.PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

                        log.debug("[微信支付] 从证书路径成功读取私钥");
                        return privateKey;
                    } catch (Exception e) {
                        log.warn("[微信支付] 从证书路径读取私钥失败，错误：{}", e.getMessage());
                    }
                }
            }

            log.error("[微信支付] 无法获取私钥");
            return null;

        } catch (Exception e) {
            log.error("[微信支付] 获取私钥异常", e);
            return null;
        }
    }

    private String generateAppPaySignature(String appId, String partnerId, String prepayId,
            String packageValue, String nonceStr, String timeStamp) {
        try {
            initWechatPayConfig();
            if (wechatPayConfig == null) {
                log.warn("[微信支付APP] 配置未初始化，返回空签名（开发环境）");
                return "";
            }

            Map<String, String> params = new HashMap<>();
            params.put("appId", appId);
            params.put("partnerId", partnerId);
            params.put("prepayId", prepayId);
            params.put("package", packageValue);
            params.put("nonceStr", nonceStr);
            params.put("timeStamp", timeStamp);

            String signString = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(java.util.stream.Collectors.joining("&"));

            java.security.PrivateKey privateKey = getWechatPayPrivateKey();
            if (privateKey == null) {
                log.error("[微信支付APP] 无法获取商户私钥，签名生成失败");
                return "";
            }

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signString.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();

            String sign = Base64.getEncoder().encodeToString(signBytes);

            log.debug("[微信支付APP] 签名生成成功，signString={}", signString);
            return sign;

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            log.error("[微信支付APP] 签名生成失败", e);
            return "";
        } catch (Exception e) {
            log.error("[微信支付APP] 签名生成异常", e);
            return "";
        }
    }

    private PublicKey getWechatPayPlatformCertificate(String serialNumber) {
        try {
            if (wechatPayConfig instanceof RSAAutoCertificateConfig) {
                RSAAutoCertificateConfig rsaConfig = (RSAAutoCertificateConfig) wechatPayConfig;

                try {
                    java.lang.reflect.Method getCertificateMethod = RSAAutoCertificateConfig.class
                            .getDeclaredMethod("getCertificate", String.class);
                    getCertificateMethod.setAccessible(true);
                    Object certificate = getCertificateMethod.invoke(rsaConfig, serialNumber);

                    if (certificate instanceof java.security.cert.X509Certificate) {
                        java.security.cert.X509Certificate x509Certificate =
                                (java.security.cert.X509Certificate) certificate;
                        PublicKey publicKey = x509Certificate.getPublicKey();
                        log.debug("[微信支付] 成功获取平台证书（方法1），serialNumber={}", serialNumber);
                        return publicKey;
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("[微信支付] 方法1不可用，尝试方法2，serialNumber={}", serialNumber);
                } catch (Exception e) {
                    log.debug("[微信支付] 方法1执行失败，尝试方法2，serialNumber={}，错误：{}",
                            serialNumber, e.getMessage());
                }

                try {
                    java.lang.reflect.Method getProviderMethod = RSAAutoCertificateConfig.class
                            .getDeclaredMethod("getCertificateProvider");
                    getProviderMethod.setAccessible(true);
                    Object certificateProvider = getProviderMethod.invoke(rsaConfig);

                    if (certificateProvider != null) {
                        java.lang.reflect.Method getCertMethod = certificateProvider.getClass()
                                .getMethod("getCertificate", String.class);
                        Object certificate = getCertMethod.invoke(certificateProvider, serialNumber);

                        if (certificate instanceof java.security.cert.X509Certificate) {
                            java.security.cert.X509Certificate x509Certificate =
                                    (java.security.cert.X509Certificate) certificate;
                            PublicKey publicKey = x509Certificate.getPublicKey();
                            log.debug("[微信支付] 成功获取平台证书（方法2），serialNumber={}", serialNumber);
                            return publicKey;
                        }
                    }
                } catch (IllegalArgumentException | ParamException e) {
                    log.warn("[微信支付] 方法2执行失败（参数错误），serialNumber={}，错误：{}",
                            serialNumber, e.getMessage());
                } catch (BusinessException e) {
                    log.warn("[微信支付] 方法2执行失败（业务异常），serialNumber={}，错误：{}",
                            serialNumber, e.getMessage());
                } catch (SystemException e) {
                    log.warn("[微信支付] 方法2执行失败（系统异常），serialNumber={}，错误：{}",
                            serialNumber, e.getMessage());
                } catch (Exception e) {
                    log.warn("[微信支付] 方法2执行失败，serialNumber={}，错误：{}",
                            serialNumber, e.getMessage());
                }
            }

            boolean isProduction = !"dev".equals(System.getProperty("spring.profiles.active", "dev"));
            if (isProduction) {
                log.error("[微信支付] 生产环境无法获取平台证书，serialNumber={}，签名验证失败", serialNumber);
                return null;
            } else {
                log.warn("[微信支付] 开发环境无法获取平台证书，serialNumber={}，跳过签名验证", serialNumber);
                return null;
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[微信支付] 获取平台证书参数错误，serialNumber={}, error={}", serialNumber, e.getMessage(), e);
            return null;
        } catch (BusinessException e) {
            log.error("[微信支付] 获取平台证书业务异常，serialNumber={}, code={}, message={}", serialNumber, e.getCode(), e.getMessage(), e);
            return null;
        } catch (SystemException e) {
            log.error("[微信支付] 获取平台证书系统异常，serialNumber={}, code={}, message={}", serialNumber, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[微信支付] 获取平台证书未知异常，serialNumber={}", serialNumber, e);
            return null;
        }
    }
}



