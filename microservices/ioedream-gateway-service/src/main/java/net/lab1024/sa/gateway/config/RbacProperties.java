package net.lab1024.sa.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "security.rbac")
public class RbacProperties {

    private boolean enabled = false;

    private List<Rule> rules = new ArrayList<>();

    @Data
    public static class Rule {

        private List<String> pathPatterns = new ArrayList<>();

        private List<String> requiredAnyRoles = new ArrayList<>();

        private List<String> requiredAnyPermissions = new ArrayList<>();

        private List<String> requiredAnyPermissionPrefixes = new ArrayList<>();
    }
}
