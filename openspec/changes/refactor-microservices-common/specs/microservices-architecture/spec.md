# Microservices Architecture Specification

## ADDED Requirements

### Requirement: Modular Common Libraries

The system SHALL organize common libraries into specialized modules with single responsibilities.

#### Scenario: Security module isolation

- **WHEN** a microservice requires authentication and authorization functionality
- **THEN** it SHALL depend only on `microservices-common-security` module
- **AND** it SHALL NOT be forced to include monitoring or business utilities

#### Scenario: Monitor module isolation

- **WHEN** a microservice requires monitoring and alerting functionality
- **THEN** it SHALL depend only on `microservices-common-monitor` module
- **AND** it SHALL NOT be forced to include security or business utilities

#### Scenario: Business module isolation

- **WHEN** a microservice requires common business utilities
- **THEN** it SHALL depend only on `microservices-common-business` module
- **AND** it SHALL NOT be forced to include security or monitoring utilities

### Requirement: Business Code Ownership

Business-specific code SHALL reside in its owning microservice, not in common libraries.

#### Scenario: Workflow code ownership

- **WHEN** workflow functionality is required
- **THEN** the code SHALL reside in `ioedream-oa-service`
- **AND** it SHALL NOT be placed in common libraries

#### Scenario: Consume code ownership

- **WHEN** consumption functionality is required
- **THEN** the code SHALL reside in `ioedream-consume-service`
- **AND** it SHALL NOT be placed in common libraries

#### Scenario: Visitor code ownership

- **WHEN** visitor management functionality is required
- **THEN** the code SHALL reside in `ioedream-visitor-service`
- **AND** it SHALL NOT be placed in common libraries

## MODIFIED Requirements

### Requirement: Dependency Management

Each microservice SHALL declare explicit dependencies on only the common modules it requires.

#### Scenario: Minimal dependency declaration

- **WHEN** configuring a microservice's dependencies
- **THEN** it SHALL include only the common modules it actually uses
- **AND** it SHALL NOT include transitive dependencies it does not need

#### Scenario: No circular dependencies

- **WHEN** common modules are organized
- **THEN** there SHALL be no circular dependencies between modules
- **AND** dependency graph SHALL be acyclic
