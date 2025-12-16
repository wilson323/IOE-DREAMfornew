## ADDED Requirements

### Requirement: Hotspot classes SHALL be decomposed by SRP
The system SHALL refactor hotspot classes that exceed agreed size/complexity limits into cohesive components with clear, single responsibilities (orchestration, domain logic, adapters/clients).

#### Scenario: Payment domain refactor
- **WHEN** `PaymentService` is refactored
- **THEN** payment orchestration remains in a thin service
- **AND** WeChat/Alipay/Bank/Refund/Notify/Signature logic are isolated into dedicated adapters/strategies
- **AND** external API contracts and transactional behaviour remain backward compatible

### Requirement: Each new component MUST have explicit boundaries and tests
Each component introduced by SRP refactor MUST expose a stable interface, use constructor-based injection, and be covered by unit tests for success and failure paths.

#### Scenario: Device sync refactor testability
- **WHEN** `DeviceSyncService` is decomposed into managers/clients
- **THEN** protocol and socket concerns are test-isolated behind interfaces
- **AND** service/manager tests validate business flows without real device IO

