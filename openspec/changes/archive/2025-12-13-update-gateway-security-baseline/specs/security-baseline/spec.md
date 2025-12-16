# Spec Delta: security-baseline

## ADDED Requirements

### Requirement: Gateway SHALL enforce authentication for protected routes

The system SHALL require a valid JWT access token for any gateway request whose path does not match the configured whitelist.

#### Scenario: Missing token

- **WHEN** a request is sent to a non-whitelisted path without an `Authorization` header
- **THEN** the gateway returns HTTP 401 with a JSON body indicating unauthorized

#### Scenario: Invalid token

- **WHEN** a request is sent to a non-whitelisted path with an invalid or expired token
- **THEN** the gateway returns HTTP 401 with a JSON body indicating unauthorized

#### Scenario: Refresh token used as access token

- **WHEN** a request is sent to a non-whitelisted path using a refresh token
- **THEN** the gateway returns HTTP 401

### Requirement: Gateway SHALL provide coarse-grained RBAC as a first authorization layer

The system SHALL support path-based coarse RBAC rules at the gateway.

#### Scenario: RBAC disabled

- **WHEN** `security.rbac.enabled=false`
- **THEN** the gateway SHALL NOT deny requests based on RBAC rules

#### Scenario: RBAC rule matched but user lacks required permission

- **WHEN** `security.rbac.enabled=true` and the request path matches a configured RBAC rule
- **AND** the user token does not include any required role/permission
- **THEN** the gateway returns HTTP 403 with a JSON body indicating forbidden

### Requirement: Gateway SHALL forward identity headers downstream

The system SHALL forward identity headers derived from JWT claims to downstream services.

#### Scenario: Authenticated request

- **WHEN** a request is authenticated
- **THEN** the gateway forwards `X-User-Id` and `X-User-Name`

### Requirement: CORS SHALL be configuration-driven and safe by default

The system SHALL configure CORS via configuration properties and SHALL avoid insecure combinations.

#### Scenario: allowCredentials with wildcard origins

- **WHEN** CORS is configured with `allowCredentials=true` and wildcard origins
- **THEN** the gateway SHALL downgrade by disabling credentials and log a warning

#### Scenario: Comma-separated CORS values

- **WHEN** CORS configuration values are provided as comma-separated strings
- **THEN** the system SHALL parse them as lists and apply them correctly
