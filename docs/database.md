# Database

Flyway is the only source of the schema. Hibernate validates the mapping and does not create tables.

| Migration | What it does |
| --- | --- |
| `V1__initial_schema.sql` | Creates the organisation-owned schema on an empty database. |
| `V2__lease_term_dates.sql` | Adds the lease start and end date columns. A lease may store both dates or neither, and the end date cannot be earlier than the start date. |
| `V3__subscription_packages.sql` | Adds property and member caps to subscription packages, allows a `DEMO` subscription and invoice status, and inserts the four proposed packages. |

Flyway applies pending migrations automatically when the backend starts. These files are the versions in the repository. That does not mean V1, V2, or V3 has already been applied to a live Supabase database. V1 does not insert properties, tenants, users, or memberships, and it does not drop existing tables. V3 inserts the four subscription packages only.

## Connection

`application.yml` contains no database password and no fallback URL. Startup fails until these process environment variables are set:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SUPABASE_JWT_ISSUER
SUPABASE_JWKS_URI
SUPABASE_JWT_AUDIENCE
```

Spring Boot reads those names itself. Do not create a backend `.env` file and expect the API to load it. The API listens on `PORT`, or 8080 when that variable is unset. Flyway applies any pending migration, including V1, V2, and V3, when the backend starts, then Hibernate checks that the tables match the JPA entities. `spring.flyway.clean-disabled` is true, so a normal startup cannot wipe the schema.

Copy the URL, host, port, database name, and user from the Supabase dashboard. Do not invent a pooler hostname. The password stays in the environment of the PowerShell window or the IDE run configuration. The exact Windows steps are in [Run locally](#run-locally).

### Which Supabase connection to use

Open the project, then **Connect** (or **Project Settings > Database > Connection string**). Choose the URI mode and JDBC if it is offered. Three methods appear:

| Method | Port | Use it for Portico? |
| --- | --- | --- |
| Direct connection. Host normally starts with `db.` | 5432 | Yes, when the machine has IPv6, or the project has the IPv4 add-on. Best later for a hosted Java server that can open a stable connection. |
| Session pooler. Host is the pooler host shown in the dashboard. | 5432 | Yes. This is the right choice for local Windows development on a normal IPv4 network. Hibernate and Flyway can keep a JDBC session. |
| Transaction pooler | 6543 | No. The API refuses this port for a Supabase host. It breaks Flyway's lock and Hibernate's prepared statements. |

Build the JDBC URL from the dashboard values. It must use TLS:

```text
jdbc:postgresql://<host-from-the-dashboard>:5432/postgres?sslmode=require
```

The database name in a new Supabase project is `postgres`. The session-pooler user usually looks like `postgres.<project-ref>`. The direct-connection user is `postgres`. Use whichever user belongs to the connection method you copied. Put that user in `SPRING_DATASOURCE_USERNAME`. Do not put the user or the password inside the URL. The startup guard rejects an embedded password, a localhost URL, a Supabase URL without `sslmode=require` (or `verify-ca` / `verify-full`), and port 6543.

`PORTICO_DATASOURCE_ALLOW_LOCAL=true` is the only switch that permits `localhost`. Leave it unset for Supabase.

These are generic configuration examples. Replace `<project-ref>` with the reference shown in the Supabase dashboard. They are not the settings of a particular deployment.

```text
SUPABASE_JWT_ISSUER=https://<project-ref>.supabase.co/auth/v1
SUPABASE_JWKS_URI=https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json
SUPABASE_JWT_AUDIENCE=authenticated
```

The running API has no default issuer. A missing value stops startup. Signing keys are explained in [authentication.md](authentication.md). Do not put the database password or the service-role key in the frontend. Do not create or alter `auth.users`. When that table exists, V1 adds:

```sql
user_profiles.id references auth.users(id) on delete cascade
```

On a plain PostgreSQL server the foreign key is skipped because Supabase owns the authentication schema.

The Java process connects with the database username and password. That login owns the tables it creates. On Supabase the `postgres` role also bypasses row-level security, so those policies do not identify the signed-in person. The Java API does that, using the verified token subject.

The `local` profile inserts one organisation, one user profile, and one owner membership, and it accepts API calls that have no bearer token. The API refuses to start that profile when the JDBC host contains Supabase, because the inserted user is not an `auth.users` row. Do not use it for this project.

## Run locally

From the repository root, in PowerShell. This does not print the password.

```powershell
$env:SUPABASE_JWT_ISSUER = "https://<project-ref>.supabase.co/auth/v1"
$env:SUPABASE_JWKS_URI = "https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json"
$env:SUPABASE_JWT_AUDIENCE = "authenticated"
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://<host-from-the-dashboard>:5432/postgres?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME = "<user-from-the-same-panel>"
$secure = Read-Host "Supabase database password" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
$env:SPRING_DATASOURCE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)

.\scripts\start-backend.ps1
```

In a second window:

```powershell
.\scripts\start-frontend.ps1
```

The React app reads `frontend/.env`. That file already exists. Do not overwrite it. In Cursor or IntelliJ, put the same six variables in the Spring Boot run configuration's environment field instead of `application.yml`.

`scripts/start-backend.ps1` checks that the variables are present, refuses the `local` profile, and runs `mvnw.cmd` or `mvn`. It does not write a secrets file.

Before the first start, run the read-only query in [scripts/preflight-public-schema.sql](../scripts/preflight-public-schema.sql) in the Supabase SQL Editor. Stop if it returns any row.

## Hosting later

Do not deploy this until the first migration has been approved and checked.

- The React app is built with `npm run build` and hosted on Vercel. Set `VITE_SUPABASE_URL`, `VITE_SUPABASE_PUBLISHABLE_KEY`, `VITE_API_BASE_URL`, and `VITE_DEMO_MODE=false` in the Vercel project. `VITE_API_BASE_URL` becomes the public API origin plus `/api/v1`, for example `https://api.example.com/api/v1`. Add that site origin to Supabase **Authentication > URL Configuration** and to `PORTICO_CORS_ORIGINS` on the API.
- The Java API is a long-running process, not a Vercel function. Give the host the same six environment variables. Prefer the direct connection on port 5432 when that host has IPv6 or the IPv4 add-on. Otherwise use the session pooler on port 5432. Do not use port 6543.
- Keep `PORTICO_DATASOURCE_ALLOW_LOCAL` unset and do not activate the `local` profile.

## Tables

Business rows carry `organisation_id`, `created_at`, and `updated_at`. Timestamps are `timestamptz` and the JDBC session uses UTC. Money is `numeric(12,2)`. Calendar dates such as `payment_date` stay as `date`.

| Table | Owns |
| --- | --- |
| `user_profiles` | Profile for an existing `auth.users.id` |
| `organisations` | Tenant |
| `organisation_memberships` | User role `OWNER`, `ADMIN`, `PROPERTY_MANAGER`, `ACCOUNTANT`, or `VIEWER`, status `ACTIVE` or `SUSPENDED` |
| `organisation_invitations` | Pending invitation, unique token |
| `addresses` | Structured address |
| `properties` | Lettable unit. `address` remains the single line stored on the property |
| `tenants` | Person who may sign a lease |
| `leases` | Agreement for one property in the same organisation. Optional `tenant_id` references `tenants` |
| `rent_payments` | Rent collected against a lease in the same organisation |
| `maintenance_requests` | Request status `OPEN`, `IN_PROGRESS`, or `RESOLVED` |
| `managers` | Staff manager |
| `agents` | Staff agent. `role` is the job label, not the membership role |
| `vendors` | Contractor |
| `notifications` | Message for a tenant in the same organisation |
| `subscription_plans` | Platform plan catalog |
| `organisation_subscriptions` | One open subscription per organisation |
| `billing_invoices` | Software invoice, separate from rent |
| `billing_webhook_events` | Provider event id, unique per provider |
| `audit_logs` | Create, update, and delete of portfolio rows |

Delete behaviour:

- A property cannot be deleted while a lease references it.
- A lease cannot be deleted while a rent payment references it.
- A tenant cannot be deleted while a lease or notification references that tenant.
- Deleting an organisation removes memberships and invitations. Portfolio rows restrict the delete.
- Deleting a Supabase auth user cascades to `user_profiles` when the foreign key exists. Memberships then cascade. Portfolio rows stay with the organisation.

## Row level security

Two different controls are in force. They are not substitutes for each other.

| Control | Where it runs | What a caller gets |
| --- | --- | --- |
| JWT signature, issuer, audience, and expiry | Java, from the Supabase JWKS | A bad token is rejected before a query runs. The user id is the token `sub`. |
| Organisation membership and role | Java, read from `organisation_memberships` | A user only reaches organisations where that user has an `ACTIVE` membership. The role on that row is the only role. |
| Repository filters | Java, every portfolio query | `organisation_id` comes from the membership, not from the request body. Changing an id returns no row. |
| Row level security enabled, with no policies | PostgreSQL | The Supabase Data API, using `anon` or `authenticated`, sees no rows. |
| `REVOKE` from `PUBLIC`, `anon`, `authenticated`, and `service_role` | PostgreSQL, only when that role exists | Those roles also lack table privileges. `service_role` bypasses RLS on Supabase, so the revoke matters. |
| `set_config('app.current_organisation_id', ...)` | Java session variable | Nothing. No policy reads it. It is not a security control. |

`auth.uid()` is empty on the Java connection. The Java login is the database user from the dashboard, and a table owner bypasses row-level security in PostgreSQL. RLS therefore does not protect queries made by this API. Do not remove the Java checks because RLS is enabled.

That design is acceptable for the first deployment: the browser talks to the Java API, and the Data API is closed. Before commercial production, add a non-owner database role that does not have `BYPASSRLS`, grant it only the DML it needs, and add policies that use a transaction-local setting set by the API. Do that in a later migration. Do not weaken the current Java checks to make the Data API writable.

`GET /api/billing/subscription` reads `organisation_subscriptions`. With no subscription row the API applies the Starter demo cap. V3 backfills that row for organisations that have none. It does not call a payment provider and it does not read `rent_payments`. An invoice that names a subscription must use that subscription's organisation.

Deleting an organisation is restricted by portfolio rows and by `audit_logs`. Memberships and invitations cascade. Audit history is kept, so an organisation that has already recorded an audit row cannot be deleted until that reference is handled in a later, deliberate migration.
