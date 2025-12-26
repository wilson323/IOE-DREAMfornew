# Consume Service Specification Delta

## ADDED Requirements

### Requirement: Wechat Payment Service

The system SHALL provide a dedicated `WechatPayService` for handling WeChat payment operations.

#### Scenario: Create WeChat JSAPI Order

- **WHEN** a valid payment request with WeChat JSAPI type is received
- **THEN** the system SHALL create a WeChat JSAPI order and return prepay_id

#### Scenario: Handle WeChat Payment Callback

- **WHEN** a WeChat payment callback notification is received
- **THEN** the system SHALL verify the signature, update payment record, and return success response

### Requirement: Alipay Payment Service

The system SHALL provide a dedicated `AlipayPayService` for handling Alipay payment operations.

#### Scenario: Create Alipay Web Order

- **WHEN** a valid payment request with Alipay Web type is received
- **THEN** the system SHALL create an Alipay order and return the payment form HTML

#### Scenario: Handle Alipay Payment Callback

- **WHEN** an Alipay payment callback notification is received
- **THEN** the system SHALL verify the signature, update payment record, and return "success"

### Requirement: Payment Callback Service

The system SHALL provide a dedicated `PaymentCallbackService` for unified callback processing.

#### Scenario: Process Payment Success Callback

- **WHEN** a payment success callback is received from any payment channel
- **THEN** the system SHALL update the payment record status, trigger business logic, and send notifications

#### Scenario: Idempotent Callback Processing

- **WHEN** a duplicate callback is received for the same transaction
- **THEN** the system SHALL return success without processing again

## MODIFIED Requirements

### Requirement: Payment Service Orchestration

The `PaymentService` SHALL act as an orchestrator that delegates to specialized payment services.

#### Scenario: Delegate to Wechat Pay Service

- **WHEN** a WeChat payment request is received
- **THEN** the system SHALL delegate to `WechatPayService`

#### Scenario: Delegate to Alipay Service

- **WHEN** an Alipay payment request is received
- **THEN** the system SHALL delegate to `AlipayPayService`
