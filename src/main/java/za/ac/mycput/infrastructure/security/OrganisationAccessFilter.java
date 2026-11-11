package za.ac.mycput.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import za.ac.mycput.application.service.AccountApplicationService;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.OrganisationAccessException;
import za.ac.mycput.domain.exception.PermissionDeniedException;
import za.ac.mycput.domain.service.RolePermissionPolicy;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.security.ApiAuthorisationCatalog.Rule;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganisationAccessFilter extends OncePerRequestFilter {

    private static final Pattern ORGANISATION_PATH = Pattern.compile(
            "^/api/v1/organisations/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})(?:/.*)?$");

    private final OrganisationAccessService access;
    private final AccountApplicationService accounts;
    private final ApiAuthorisationCatalog catalog;
    private final RolePermissionPolicy policy;
    private final boolean localAccess;
    private final String localUserId;
    private final String localOrganisationId;

    public OrganisationAccessFilter(OrganisationAccessService access,
                                    AccountApplicationService accounts,
                                    ApiAuthorisationCatalog catalog,
                                    RolePermissionPolicy policy,
                                    boolean localAccess,
                                    String localUserId,
                                    String localOrganisationId) {
        this.access = access;
        this.accounts = accounts;
        this.catalog = catalog;
        this.policy = policy;
        this.localAccess = localAccess;
        this.localUserId = localUserId;
        this.localOrganisationId = localOrganisationId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api") || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean synthetic = false;
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            if (!localAccess) {
                filterChain.doFilter(request, response);
                return;
            }
            authentication = localAuthentication();
            synthetic = true;
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        try {
            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                write(response, 401, "INVALID_TOKEN", "The access token has no user id");
                return;
            }
            if (!synthetic && explicitlyUnverified(jwt)) {
                write(response, 403, "EMAIL_NOT_VERIFIED", "Confirm your email before using Portico");
                return;
            }
            UUID userId;
            try {
                userId = Identifiers.parse(jwt.getSubject());
            } catch (DomainRuleException exception) {
                write(response, 401, "INVALID_TOKEN", "The access token has no user id");
                return;
            }
            accounts.ensureProfile(CurrentUser.from(jwt));
            Rule rule = catalog.ruleFor(request.getMethod(), request.getRequestURI());
            if (rule.kind() == ApiAuthorisationCatalog.Kind.DENY) {
                write(response, 403, "FORBIDDEN", "This action is not available");
                return;
            }
            if (rule.kind() == ApiAuthorisationCatalog.Kind.AUTHENTICATED) {
                filterChain.doFilter(request, response);
                return;
            }
            String requested = requestedOrganisation(request, jwt);
            OrganisationScope scope = access.resolve(userId, requested);
            if (!policy.permits(scope.role(), rule.permission())) {
                write(response, 403, "FORBIDDEN", AccessDenialMessages.forPermission(rule.permission(), scope.role()));
                return;
            }
            OrganisationContext.set(scope);
            filterChain.doFilter(request, response);
        } catch (PermissionDeniedException exception) {
            write(response, 403, "FORBIDDEN", exception.getMessage());
        } catch (OrganisationAccessException exception) {
            write(response, 403, "ORGANISATION_ACCESS", exception.getMessage());
        } catch (DomainRuleException exception) {
            write(response, 400, "DOMAIN_RULE", exception.getMessage());
        } finally {
            OrganisationContext.clear();
        }
    }

    private String requestedOrganisation(HttpServletRequest request, Jwt jwt) {
        String header = request.getHeader("X-Organisation-Id");
        Matcher matcher = ORGANISATION_PATH.matcher(path(request));
        if (matcher.matches()) {
            String pathOrganisation = matcher.group(1);
            if (header != null && !header.isBlank() && !pathOrganisation.equalsIgnoreCase(header.trim())) {
                throw new OrganisationAccessException("You are not a member of that organisation");
            }
            return pathOrganisation;
        }
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        Object claim = jwt.getClaim("organisation_id");
        if (claim != null && !claim.toString().isBlank()) {
            return claim.toString();
        }
        return null;
    }

    private String path(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private boolean explicitlyUnverified(Jwt jwt) {
        Object value = jwt.getClaim("email_verified");
        if (value instanceof Boolean flag) {
            return !flag;
        }
        if (value instanceof String text) {
            return "false".equalsIgnoreCase(text);
        }
        return false;
    }

    private Authentication localAuthentication() {
        Jwt jwt = Jwt.withTokenValue("local-dev")
                .header("alg", "none")
                .subject(localUserId)
                .claim("organisation_id", localOrganisationId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    private void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        String safe = message == null ? "" : message.replace("\\", "\\\\").replace("\"", "'");
        response.getWriter().write("{\"error\":\"" + code + "\",\"message\":\"" + safe + "\"}");
    }
}
