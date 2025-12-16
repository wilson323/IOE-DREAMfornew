## ADDED Requirements

### Requirement: North-south traffic SHALL be routed through Gateway
All external (north-south) requests SHALL be routed through the API Gateway and use `GatewayServiceClient` for any gateway-mediated fan-out.

#### Scenario: External client access
- **WHEN** an external client accesses any backend capability
- **THEN** the request goes through the Gateway
- **AND** unified auth/limit/audit/gray controls apply

### Requirement: East-west synchronous calls SHALL use a hybrid policy
East-west synchronous calls SHALL follow a hybrid policy:
1. Low-frequency or cross-domain calls SHALL go through Gateway via `GatewayServiceClient`.
2. High-frequency, low-latency, strong-consistency calls within the same domain MAY be direct, but MUST use the unified direct-call Client and be declared in a direct-call whitelist.

#### Scenario: Whitelisted direct call on hot path
- **WHEN** a whitelisted hot-path call occurs within one domain
- **THEN** the call is performed directly using the unified Client
- **AND** timeouts/retries/circuit-breaker/ratelimit/fallback/metrics/tracing are enforced
- **AND** service-to-service authentication is validated

### Requirement: Direct-call whitelist MUST be governed and audited
Any direct call MUST be explicitly listed in the whitelist and reviewed; non-whitelisted direct calls MUST be treated as architecture violations.

#### Scenario: Non-whitelisted direct call
- **WHEN** code introduces a direct call not in the whitelist
- **THEN** CI/architecture scanning rejects the change
