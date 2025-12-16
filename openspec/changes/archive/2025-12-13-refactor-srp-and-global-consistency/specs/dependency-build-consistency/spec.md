## ADDED Requirements

### Requirement: Dependency versions SHALL be single-sourced
Dependency versions SHALL be declared only in the parent POM/BOM and SHALL NOT be overridden in child modules unless explicitly justified and documented.

#### Scenario: Version drift prevention
- **WHEN** a child module declares a dependency already managed by the parent BOM
- **THEN** the child module does not specify a different version
- **AND** Maven Enforcer fails the build if drift is introduced

### Requirement: Gateway SHALL remain reactive and stateless
`ioedream-gateway-service` SHALL not include blocking persistence dependencies (JDBC/MyBatis/MySQL/Druid) unless a documented, approved exception exists.

#### Scenario: Gateway dependency hygiene
- **WHEN** gateway dependencies are reviewed
- **THEN** only WebFlux, security, routing, resilience, and observability dependencies remain

### Requirement: Build system SHALL have a single source of truth
The project SHALL use Maven as the sole build definition; Gradle scripts SHALL be removed or frozen to avoid conflicting dependency graphs.

#### Scenario: Build truth unification
- **WHEN** build tooling is executed in CI
- **THEN** Maven is the authoritative build path
- **AND** Gradle artifacts do not alter released dependencies

### Requirement: microservices-common SHALL be modularized by stability and domain
`microservices-common` SHALL be decomposed into a small set of artifacts (target 5–8) based on stability/domain ownership, including:
- `common-core` (pure Java, stable shared primitives)
- `common-spring`/`common-web` (framework cross-cutting)
- `common-starter-*` (capability starters: cache/mq/seata/security/mybatis/…)
- `*-domain-api` (cross-service contracts only, no shared implementations)

#### Scenario: Gradual package migration
- **WHEN** a package family is migrated out of `microservices-common`
- **THEN** it moves to the appropriate artifact by domain/stability
- **AND** services update dependencies and tests pass
- **AND** no domain implementation is re-introduced into `common-core`
