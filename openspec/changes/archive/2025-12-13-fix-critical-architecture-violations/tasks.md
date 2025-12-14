## Tasks: Critical Architecture Violations and Security Fixes

### Phase 1: P0 Critical Fixes (Week 1-2)

#### 1. Repository Annotation Violations (26 instances)
- [ ] 1.1 Backup all DAO interface files before modification
- [ ] 1.2 Replace @Repository with @Mapper in ioedream-common-service (7 files)
- [ ] 1.3 Replace @Repository with @Mapper in ioedream-attendance-service (2 files)
- [ ] 1.4 Replace @Repository with @Mapper in microservices-common (17 files)
- [ ] 1.5 Verify MyBatis-Plus @Mapper import in all modified files
- [ ] 1.6 Compile test each microservice after changes

#### 2. Dependency Injection Violations (39 instances)
- [ ] 2.1 Search and identify all @Autowired usage across microservices
- [ ] 2.2 Replace @Autowired with @Resource in Service implementations
- [ ] 2.3 Replace @Autowired with @Resource in Controller classes
- [ ] 2.4 Replace @Autowired with @Resource in Manager classes
- [ ] 2.5 Update import statements for javax.annotation.Resource → jakarta.annotation.Resource
- [ ] 2.6 Verify Spring dependency injection works correctly after changes

#### 3. Configuration Security Remediation (116 risks)
- [ ] 3.1 Identify all configuration files with plaintext passwords
- [ ] 3.2 Create environment variable templates for sensitive configs
- [ ] 3.3 Replace plaintext passwords with ${VARIABLE_NAME} placeholders
- [ ] 3.4 Configure Nacos encryption for sensitive configuration
- [ ] 3.5 Update database connection strings to use encrypted format
- [ ] 3.6 Update Redis connection configurations for security
- [ ] 3.7 Document environment variable requirements for deployment

#### 4. Immediate Validation and Testing
- [ ] 4.1 Run `mvn clean compile` on all microservices
- [ ] 4.2 Run integration tests to verify functionality
- [ ] 4.3 Validate Spring Boot startup for all services
- [ ] 4.4 Check database connectivity and Redis connections
- [ ] 4.5 Document any remaining issues for Phase 2

### Phase 2: P1 Important Fixes (Week 2-4)

#### 5. RESTful API Design Compliance
- [ ] 5.1 Identify all @PostMapping endpoints performing query operations
- [ ] 5.2 Refactor query endpoints from POST to GET methods
- [ ] 5.3 Update list endpoints from POST to GET methods
- [ ] 5.4 Update find/search endpoints to use GET methods
- [ ] 5.5 Ensure proper HTTP status codes for all endpoints
- [ ] 5.6 Update API documentation (Swagger/OpenAPI) for changes

#### 6. Service Communication Standardization
- [ ] 6.1 Identify all FeignClient usage across microservices
- [ ] 6.2 Create GatewayServiceClient wrapper for service-to-service communication
- [ ] 6.3 Replace direct service calls with Gateway-mediated calls
- [ ] 6.4 Update service discovery and load balancing configurations
- [ ] 6.5 Test service-to-service communication reliability

#### 7. Architecture Compliance Verification
- [ ] 7.1 Verify Controller → Service → Manager → DAO layer separation
- [ ] 7.2 Check for cross-layer access violations
- [ ] 7.3 Validate transaction management @Transactional usage
- [ ] 7.4 Ensure proper exception handling and logging
- [ ] 7.5 Run static code analysis for compliance verification

### Phase 3: P2 Optimization (Month 2)

#### 8. Enterprise Features Implementation
- [ ] 8.1 Implement distributed tracing (Spring Cloud Sleuth + Zipkin)
- [ ] 8.2 Add circuit breakers and bulkheads for resilience
- [ ] 8.3 Implement comprehensive monitoring and alerting
- [ ] 8.4 Add performance metrics and health checks
- [ ] 8.5 Configure log aggregation and analysis

#### 9. Final Validation and Documentation
- [ ] 9.1 Complete end-to-end testing of all fixes
- [ ] 9.2 Update development standards and coding guidelines
- [ ] 9.3 Create deployment documentation with security requirements
- [ ] 9.4 Provide training materials for updated standards
- [ ] 9.5 Archive changes and update project specifications

### Dependencies and Parallel Work

**Dependencies**:
- Phase 1 must be completed before Phase 2 can begin
- Configuration security fixes require environment setup coordination
- API changes may require frontend client updates

**Parallelizable Work**:
- Repository and Autowired fixes can be done simultaneously across different microservices
- Configuration security fixes can be done in parallel with code fixes
- RESTful API refactoring can be done per service independently

**Risk Mitigation**:
- All changes require backup before modification
- Each microservice should be tested independently after changes
- Rollback procedures should be documented before starting fixes