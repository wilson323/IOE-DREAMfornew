# Change: Refactor access permission boundary between software authz and device access

## Why
Current access-service mixes software/organization permissions with device access permissions, which causes boundary violations and incorrect device validation semantics.

## What Changes
- Define access-device permission model owned by access-service (entity/dao/manager/service)
- Separate software permission events from device access permission events
- Remove or rename misleading UserAreaPermission* usage that maps to organization relations
- Align access validation and permission sync to the access-device permission model

## Impact
- Affected specs: common-boundaries
- Affected code: ioedream-access-service, ioedream-common-service, microservices-common-entity, microservices-common-business
