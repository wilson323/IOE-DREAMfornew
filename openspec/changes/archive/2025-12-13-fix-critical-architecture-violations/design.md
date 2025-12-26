# Design: Critical Architecture Violations Remediation

## Context

IOE-DREAM project has accumulated critical architecture violations and security vulnerabilities through rapid development and lack of consistent coding standards enforcement. The project currently operates with 26 @Repository violations, 39 @Autowired violations, 116 configuration security risks, and widespread API design non-compliance.

These violations prevent the system from meeting enterprise-grade production standards and pose immediate security and operational risks. The remediation effort must balance urgency to fix P0 issues with system stability and team productivity.

## Goals / Non-Goals

**Goals**:
- Eliminate all P0 security vulnerabilities and architecture violations within 2 weeks
- Achieve 100% compliance with Jakarta EE and RepoWiki coding standards
- Implement enterprise-grade configuration security and management
- Establish RESTful API design standards across all microservices
- Enable production deployment with enterprise security compliance

**Non-Goals**:
- Complete system rearchitecture (focus on violations only)
- Adding new features or capabilities
- Changing core business logic or data models
- Performance optimization beyond fixing violations
- Database schema changes (except for security-related configurations)

## Decisions

### 1. Annotation Standards Enforcement
**Decision**: Strictly enforce @Mapper for DAO interfaces and @Resource for dependency injection
**Rationale**:
- MyBatis-Plus requires @Mapper annotation for proper interface proxy creation
- @Resource provides consistent dependency injection behavior across Jakarta EE
- Prevents mixing of Spring Framework and Jakarta EE injection patterns

### 2. Configuration Security Strategy
**Decision**: Implement Nacos-based encrypted configuration with environment variable fallbacks
**Rationale**:
- Provides centralized configuration management for all microservices
- Supports encrypted sensitive data storage and automatic decryption
- Enables environment-specific configurations (dev, test, prod)
- Maintains compatibility with existing deployment patterns

### 3. Service Communication Architecture
**Decision**: Mandate Gateway-only inter-service communication, eliminate direct FeignClient usage
**Rationale**:
- Simplifies service discovery and load balancing
- Provides centralized request/response logging and monitoring
- Enables uniform security policies and rate limiting
- Prevents service coupling and dependency complexity

### 4. RESTful API Standardization
**Decision**: Enforce HTTP method semantics (GET for queries, POST for creates, PUT for updates, DELETE for removes)
**Rationale**:
- Follows industry best practices and client expectations
- Improves API caching and proxy compatibility
- Enables proper HTTP status code usage
- Supports automated API documentation generation

## Risks / Trade-offs

### High Severity Risks
1. **Configuration Migration Risk** → **Mitigation**: Full backup + rollback procedures + environment validation
2. **Service Dependency Breakage** → **Mitigation**: Phased rollout + comprehensive testing + feature flags
3. **Development Team Disruption** → **Mitigation**: Detailed documentation + training sessions + gradual adoption

### Medium Severity Risks
1. **API Client Compatibility** → **Mitigation**: Versioned APIs + backward compatibility period + client migration guides
2. **Performance Regression** → **Mitigation**: Performance testing + baseline measurements + optimization checkpoints

### Trade-offs
- **Short-term productivity vs long-term maintainability**: Accept reduced velocity during remediation for long-term code quality
- **Configuration complexity vs security**: Accept more complex configuration setup for enhanced security
- **Strict standards vs flexibility**: Enforce consistent patterns while allowing documented exceptions

## Migration Plan

### Phase 1: Critical Security Fixes (Week 1)
1. **Preparation**: Complete backup of all source code and configurations
2. **Repository Violations**: Replace @Repository with @Mapper across all DAO interfaces
3. **Dependency Injection**: Replace @Autowired with @Resource across all Spring components
4. **Validation**: Compile and test each microservice independently

### Phase 2: Configuration Security (Week 1-2)
1. **Environment Setup**: Configure Nacos server with encryption capabilities
2. **Configuration Migration**: Replace plaintext passwords with encrypted variables
3. **Service Testing**: Validate service startup and connectivity
4. **Documentation**: Create deployment configuration guides

### Phase 3: API and Communication Standards (Week 2-4)
1. **API Refactoring**: Update endpoints to follow RESTful patterns
2. **Service Communication**: Replace FeignClient with Gateway calls
3. **Client Updates**: Update API documentation and client examples
4. **End-to-end Testing**: Complete integration testing pipeline

### Rollback Procedures
- **Code Rollback**: Git-based rollback to pre-change commits
- **Configuration Rollback**: Restore configuration files from backups
- **Service Rollback**: Individual service redeployment with previous versions
- **Database Rollback**: Configuration rollback (no schema changes expected)

## Open Questions

1. **Environment Variable Management**: How to manage environment variable synchronization across development teams?
2. **API Versioning Strategy**: Should we implement API versioning during the RESTful refactoring?
3. **Testing Coverage**: What level of automated testing is required for validating the fixes?
4. **Team Training**: How quickly can the development team adopt the new standards?
5. **Production Timeline**: What is the earliest safe deployment date after remediation?

## Success Criteria

- [ ] 0 @Repository violations across all microservices
- [ ] 0 @Autowired violations across all Spring components
- [ ] 0 plaintext passwords in configuration files
- [ ] 100% RESTful API compliance for HTTP method usage
- [ ] 0 direct FeignClient usage (all through Gateway)
- [ ] All microservices compile and start successfully
- [ ] Integration tests pass with 100% success rate
- [ ] Production deployment checklist completed