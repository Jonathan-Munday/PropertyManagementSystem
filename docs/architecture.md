# Portico architecture

Portico is the property-management API in this repository. The coursework domain model is still the source of the business concepts. Spring Boot 4.1.2 on Java 21 supplies the runtime around it.

## Dependency rule

Dependencies point inward:

```
interfaces.rest
    -> application
        -> domain
infrastructure
    -> application
    -> domain
```

The domain packages do not import Spring, Jakarta Persistence, HTTP, security, PostgreSQL, or a billing SDK. Policy objects are plain Java. `DomainPolicyConfig` is the only place that turns them into Spring beans.

| Layer | Package | Responsibility |
| --- | --- | --- |
| Domain model | `za.ac.mycput.domain.model` | Existing entities and their builders |
| Domain factory | `za.ac.mycput.domain.factory` | Existing factories, plus id-preserving overloads |
| Domain repository | `za.ac.mycput.domain.repository` | Persistence ports. No Spring Data types |
| Domain service | `za.ac.mycput.domain.service` | Rules that span a concept or two entities |
| Value object | `za.ac.mycput.domain.valueobject` | `Money` and `EmailAddress` |
| Application | `za.ac.mycput.application` | Commands, use cases, and coordinating services |
| Persistence | `za.ac.mycput.infrastructure.persistence` | JPA entities, mappers, and repository adapters |
| Memory | `za.ac.mycput.infrastructure.persistence.memory` | Coursework in-memory repositories, not Spring beans |
| Security | `za.ac.mycput.infrastructure.security` | Stateless JWT resource server |
| Billing | `za.ac.mycput.infrastructure.billing` | Subscription boundary. No provider is called |
| REST | `za.ac.mycput.interfaces.rest` | Thin controllers, API DTOs, and the exception translator |

`PorticoApplication` is the Spring Boot entry point. `Main` remains the original coursework entry point and is not the API.

## Bounded contexts

One modular monolith holds four contexts. They share the process and the database, and they do not share a ledger:

1. **Identity and organisations.** Profiles, memberships, invitations, and roles. `AccountApplicationService` creates a profile from the verified token subject and an `OWNER` membership when that user creates an organisation. It does not insert a Supabase Auth user.
2. **Property management.** Properties and addresses.
3. **Tenancy and operations.** Tenants, leases, rent payments, maintenance, managers, agents, vendors, and notifications. A `Payment` is rent collected against a lease.
4. **SaaS billing.** Subscription plans, organisation subscriptions, software invoices, and billing webhook events. `SubscriptionBillingPort` is the outbound port. The current adapter reports `NOT_CONFIGURED` and does not call Stripe.

## Request path

```
HTTP DTO -> REST mapper -> application command
    -> use case or application service
        -> domain factory and domain policy
            -> repository port
                -> JPA adapter -> mapper -> PostgreSQL
```

Controllers do not call repositories. They do not decide whether a property can be leased. `CreateLeaseUseCase` loads the property, asks `PropertyOccupancyPolicy`, saves the lease, and marks that property unavailable. `RecordRentPaymentUseCase` checks `RentCollectionPolicy`, requires the lease, and stores a rent payment.

Repository ports still return `null` when a create or update cannot be applied. Application services translate that into `EntityNotFoundException` or `DuplicateEntityException`. The REST handler turns those into 404 and 409. A database foreign-key failure becomes 409 as well.

## Persistence

Flyway owns the schema (`src/main/resources/db/migration`). Hibernate validates it and does not generate tables. `V1__initial_schema.sql` is the multi-tenant schema: UUID primary keys, organisation ownership, and row-level security. JPA entities live in `infrastructure.persistence.entity` and are mapped to and from domain objects. Money is `numeric(12,2)` in PostgreSQL and `BigDecimal` on the JPA entities. Domain entities still expose `double` so the existing factories and tests keep their contracts. `Money` is used by the policies. Timestamps are stored as `timestamptz` and read as UTC `Instant` values.

The in-memory repositories remain for the existing unit tests. They are not registered as Spring beans. `getRepository()` is retained only so those tests can keep using it. Production updates go through the JPA adapters, which load one id inside the caller's organisation and save that row. They return unmodifiable lists.

## Security

The API is an OAuth2 resource server. Sessions are not created. `/actuator/health` and `/actuator/info` are public. `/api/**` requires a Supabase access token. `SupabaseJwtDecoderFactory` builds a Nimbus decoder from the JWKS URI and checks the issuer, audience, and expiry. Supported signing algorithms are RS256 and ES256. The legacy HMAC project secret is not accepted. The `sub` claim is the only user id. `RolePermissionPolicy` decides what that user's membership role may do, and `ApiAuthorisationCatalog` attaches one permission to each route. Details and the signing-key rotation order are in [authentication.md](authentication.md).

Every portfolio query is filtered by the organisation resolved from that membership. Row-level security denies the Supabase Data API. The Java database login owns the tables and bypasses that security, so the repository filter remains required. The `local` profile is the only mode that accepts a call without a bearer token, and it still scopes that call to the configured organisation. A bearer token presented to the local profile is still verified.

## Tests

Existing JUnit 5 factory and repository tests are unchanged in behaviour. TestNG remains on the test classpath because the coursework build declared it; there are still no TestNG test classes. New tests cover money, email, the portfolio policies, and the lease and rent use cases. `PropertyApiTest` starts PostgreSQL with Testcontainers and exercises the property, lease, and subscription routes. That class is skipped when Docker is not available, and it runs as part of `mvn test` when Docker is. JUnit 5 is the standard for new tests.

## Explicitly out of this increment

Stripe is not called. Lease start and end dates, and a persisted rent-payment status, are not columns. The React client may send them; Jackson ignores the unknown fields. A lease may store `tenantId` when the client sends one. The original factory methods stay valid.
