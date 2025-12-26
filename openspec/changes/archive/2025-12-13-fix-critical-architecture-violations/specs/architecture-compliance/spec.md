## MODIFIED Requirements

### Requirement: Annotation Standards Compliance
All DAO interfaces SHALL use @Mapper annotation and all dependency injection SHALL use @Resource annotation in accordance with Jakarta EE and RepoWiki standards.

#### Scenario: DAO Interface Annotation Compliance
- **WHEN** creating or modifying DAO interface files
- **THEN** all interfaces MUST use @Mapper annotation instead of @Repository
- **AND** proper MyBatis-Plus imports SHALL be included

#### Scenario: Dependency Injection Standards
- **WHEN** implementing Spring components (Controller, Service, Manager)
- **THEN** all field injection MUST use @Resource annotation instead of @Autowired
- **AND** jakarta.annotation.Resource SHALL be imported instead of javax.annotation.Resource

### Requirement: Configuration Security Management
All sensitive configuration values SHALL be encrypted and managed through Nacos configuration center with environment variable fallbacks for security compliance.

#### Scenario: Password Security
- **WHEN** configuring database or service connections
- **THEN** passwords MUST be stored as encrypted values in Nacos
- **AND** environment variables SHALL be used for development environments

#### Scenario: Configuration File Security
- **WHEN** deploying applications to production
- **THEN** no plaintext passwords SHALL exist in configuration files
- **AND** all sensitive data MUST be encrypted or externalized

### Requirement: RESTful API Design Standards
All API endpoints SHALL follow RESTful design principles with proper HTTP method semantics for query, create, update, and delete operations.

#### Scenario: Query Operations
- **WHEN** implementing data retrieval endpoints
- **THEN** @GetMapping or @RequestMapping(method=GET) MUST be used
- **AND** POST methods SHALL NOT be used for query operations

#### Scenario: Create Operations
- **WHEN** implementing resource creation endpoints
- **THEN** @PostMapping MUST be used with appropriate request bodies
- **AND** proper HTTP status codes (201 Created) SHALL be returned

### Requirement: Service Communication Architecture
All inter-service communication SHALL be mediated through the Gateway service using GatewayServiceClient, eliminating direct FeignClient usage.

#### Scenario: Service-to-Service Calls
- **WHEN** one microservice needs to call another
- **THEN** GatewayServiceClient MUST be used instead of direct FeignClient
- **AND** proper error handling and fallback mechanisms SHALL be implemented

#### Scenario: Service Discovery
- **WHEN** configuring service endpoints
- **THEN** all service discovery SHALL route through Gateway
- **AND** direct service-to-service communication SHALL be disabled

### Requirement: Four-Layer Architecture Compliance
All code SHALL strictly follow Controller → Service → Manager → DAO layered architecture with proper separation of concerns.

#### Scenario: Layer Boundaries
- **WHEN** implementing business logic
- **THEN** Controller layer SHALL only handle HTTP requests/responses
- **AND** Service layer SHALL contain business logic and transaction management
- **AND** Manager layer SHALL handle complex workflow orchestration
- **AND** DAO layer SHALL only handle data access operations

#### Scenario: Cross-Layer Access Prevention
- **WHEN** accessing data or business logic
- **THEN** Controllers SHALL NOT directly access DAO or Manager layers
- **AND** Services SHALL access DAO only through Manager layers when complex operations needed

## ADDED Requirements

### Requirement: Architecture Violation Detection
Automated scanning tools SHALL detect and report architecture violations during code review and CI/CD processes.

#### Scenario: Violation Scanning
- **WHEN** code is committed to repository
- **THEN** automated scans SHALL detect @Repository and @Autowired violations
- **AND** build process SHALL fail on critical violations

#### Scenario: Security Configuration Scanning
- **WHEN** configuration files are modified
- **THEN** automated scans SHALL detect plaintext passwords
- **AND** security violations SHALL be reported immediately

### Requirement: Configuration Encryption Standards
All sensitive configuration values SHALL use industry-standard encryption with proper key management.

#### Scenario: Encryption Implementation
- **WHEN** storing sensitive configuration in Nacos
- **THEN** AES-256 encryption SHALL be used for all password fields
- **AND** proper key rotation SHALL be implemented

#### Scenario: Key Management
- **WHEN** managing encryption keys
- **THEN** keys SHALL be stored securely separate from encrypted data
- **AND** access to keys SHALL be restricted to authorized personnel only

### Requirement: API Documentation Compliance
All RESTful API endpoints SHALL be properly documented with OpenAPI/Swagger specifications following RESTful standards.

#### Scenario: API Documentation
- **WHEN** creating or modifying API endpoints
- **THEN** proper @ApiOperation and @ApiParam annotations SHALL be included
- **AND** request/response examples SHALL be provided

#### Scenario: HTTP Method Documentation
- **WHEN** documenting API behavior
- **THEN** HTTP method semantics SHALL be clearly documented
- **AND** proper status codes SHALL be specified for each response type

### Requirement: Service Monitoring and Observability
All microservices SHALL implement distributed tracing and monitoring for service communication through Gateway.

#### Scenario: Tracing Implementation
- **WHEN** services communicate through Gateway
- **THEN** distributed tracing headers SHALL be propagated
- **AND** correlation IDs SHALL be maintained across service boundaries

#### Scenario: Monitoring Integration
- **WHEN** monitoring service health
- **THEN** Gateway communication metrics SHALL be collected
- **AND** performance data SHALL be available for analysis