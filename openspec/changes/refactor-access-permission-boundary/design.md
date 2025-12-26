## Context
Access-service currently uses organization-area relations as a proxy for device access permission. This violates domain boundaries and conflicts with access module documentation.

## Goals / Non-Goals
- Goals: isolate access-device permission model, separate events, remove ambiguity between software and device permissions
- Non-Goals: redesign RBAC or menu permissions, change unrelated microservices

## Decisions
- Decision: access-device permission model lives in access-service and owns device validation
- Decision: software permission events remain in common-service, access-device events are separate

## Risks / Trade-offs
- Risk: migration requires updating multiple services and tests
- Mitigation: incremental migration with compatibility adapters and explicit mapping

## Migration Plan
1) Introduce access-device permission entity/dao/manager/service in access-service
2) Add access permission change event and update device sync listener
3) Replace device validation lookups to use access-device permission model
4) Deprecate or rename UserAreaPermission* in common-service if still needed for org relations
5) Update documentation and tests

## Open Questions
- What backward-compatibility period is required for existing endpoints?
