package za.ac.mycput.infrastructure.security;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.access.Permission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps an HTTP method and path to the permission that must be allowed.
 * Role decisions stay in {@code RolePermissionPolicy}.
 */
@Component
public class ApiAuthorisationCatalog {

    private static final Pattern ORGANISATION_PATH = Pattern.compile(
            "^/api/v1/organisations/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})(?:/(.*))?$");

    public Rule ruleFor(String method, String rawPath) {
        String path = normalise(rawPath);
        if ("OPTIONS".equals(method)) {
            return Rule.authenticated();
        }
        if ("/api/v1/me".equals(path)
                || "/api/v1/organisations".equals(path)
                || "/api/v1/invitations/accept".equals(path)) {
            return Rule.authenticated();
        }
        Matcher organisation = ORGANISATION_PATH.matcher(path);
        if (organisation.matches()) {
            String tail = organisation.group(2);
            if (tail == null && "DELETE".equals(method)) {
                return Rule.organisation(Permission.DELETE_ORGANISATION);
            }
            if ("members".equals(tail) && reading(method)) {
                return Rule.organisation(Permission.VIEW_MEMBERS);
            }
            if ("invitations".equals(tail) && "POST".equals(method)) {
                return Rule.organisation(Permission.MANAGE_USERS);
            }
            return Rule.deny();
        }
        if (resource(path, "/api/property")) {
            return Rule.organisation(reading(method) ? Permission.VIEW_PROPERTIES : Permission.MANAGE_PROPERTIES);
        }
        if (resource(path, "/api/tenant")) {
            return Rule.organisation(reading(method) ? Permission.VIEW_OPERATIONS : Permission.MANAGE_TENANTS);
        }
        if (resource(path, "/api/lease")) {
            return Rule.organisation(reading(method) ? Permission.VIEW_OPERATIONS : Permission.MANAGE_LEASES);
        }
        if (resource(path, "/api/payment")) {
            return Rule.organisation(reading(method) ? Permission.VIEW_PAYMENTS : Permission.MANAGE_PAYMENTS);
        }
        if (resource(path, "/api/billing")) {
            return Rule.organisation(reading(method) ? Permission.VIEW_BILLING : Permission.MANAGE_BILLING);
        }
        if (resource(path, "/api/maintenance")
                || resource(path, "/api/manager")
                || resource(path, "/api/agent")
                || resource(path, "/api/address")
                || resource(path, "/api/vendor")
                || resource(path, "/api/notification")) {
            return Rule.organisation(reading(method) ? Permission.VIEW_OPERATIONS : Permission.MANAGE_OPERATIONS);
        }
        return Rule.deny();
    }

    private static boolean reading(String method) {
        return "GET".equals(method) || "HEAD".equals(method);
    }

    private static boolean resource(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private static String normalise(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return "";
        }
        String path = rawPath;
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public enum Kind {
        AUTHENTICATED,
        ORGANISATION,
        DENY
    }

    public record Rule(Kind kind, Permission permission) {
        public static Rule authenticated() {
            return new Rule(Kind.AUTHENTICATED, null);
        }

        public static Rule organisation(Permission permission) {
            return new Rule(Kind.ORGANISATION, permission);
        }

        public static Rule deny() {
            return new Rule(Kind.DENY, null);
        }
    }
}
