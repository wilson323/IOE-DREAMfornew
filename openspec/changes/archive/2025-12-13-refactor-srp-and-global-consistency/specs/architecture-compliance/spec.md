## MODIFIED Requirements

### Requirement: Service Communication Architecture
All inter-service synchronous communication SHALL follow the hybrid policy defined in `communication-architecture`:
1. Cross-domain or low-frequency east-west calls SHALL be mediated through Gateway using `GatewayServiceClient`.
2. Same-domain hot-path calls MAY be direct only when whitelisted and using the unified direct-call Client with enforced resilience, observability, and service-to-service authentication.

#### Scenario: Hybrid communication compliance
- **WHEN** a service performs a synchronous inter-service call
- **THEN** it uses Gateway mediation unless it matches a whitelisted hot-path direct call
- **AND** governance guardrails apply in both cases
