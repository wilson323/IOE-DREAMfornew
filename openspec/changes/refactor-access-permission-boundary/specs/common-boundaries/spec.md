## ADDED Requirements
### Requirement: Access Device Permission Boundary
The system SHALL define access-device permission model within access-service and MUST NOT use organization relations or software permissions for device access validation.

#### Scenario: Device validation uses access permission model
- **WHEN** access-service performs device access validation
- **THEN** it uses access-device permission data (AccessUserPermission*) and not AreaUser* or software permission models

### Requirement: Permission Event Separation
The system SHALL separate software permission change events from access-device permission change events, and device sync MUST only consume access-device events.

#### Scenario: Device sync listens to access permission events
- **WHEN** an access-device permission is added or removed
- **THEN** AccessPermissionSyncService consumes access-device events and software permission events do not trigger device sync
