# Authentication and organisation access

Portico is a multi-tenant API. Supabase Auth stores passwords and issues access tokens. The React app uses the official Supabase JavaScript client. The Java API verifies those tokens with Spring Security's OAuth2 resource server and Nimbus JOSE + JWT. This service does not hash passwords, does not issue tokens, and does not modify `auth.users`.

A caller cannot read another organisation's rows by changing a URL, a JSON field, a query parameter, or `X-Organisation-Id`. The user id is the verified `sub` claim. The organisation is an active membership of that user. A client-supplied user id or organisation id is never treated as proof.

## Create the Supabase project

1. Create a project at [supabase.com](https://supabase.com). Wait until the database is ready.
2. In **Authentication > Sign In / Providers**, enable Email.
3. Turn on **Confirm email**. The desk stays closed until `email_confirmed_at` is set, and the API rejects a token whose `email_verified` claim is false.
4. Set the password policy to at least 8 characters. The React forms use the same minimum. Supabase stores the password.
5. In **Authentication > URL Configuration**, set Site URL to the frontend origin (`http://localhost:5173` for local development).
6. Add redirect URLs:
   - `http://localhost:5173/**`
   - the production origin, with `/**`
   - Confirmation links return to `/login`. Recovery links return to `/reset-password`.
7. In **Authentication > Email Templates**, keep the confirm-signup and reset-password templates. The reset template must send the user to the Site URL path `/reset-password`.
8. Copy the project URL and the publishable key into `frontend/.env` as `VITE_SUPABASE_URL` and `VITE_SUPABASE_PUBLISHABLE_KEY` (see `frontend/.env.example`). Do not put the service-role key in the frontend or in the Java process.

Point the Java datasource at the Supabase Postgres connection chosen in [database.md](database.md). Flyway applies the single migration `V1__initial_schema.sql`. Do not create or alter `auth.users`. When that table already exists, `V1` adds `user_profiles.id references auth.users(id) on delete cascade`.

## Signing keys

New Supabase projects sign access tokens with an asymmetric key published at a JWKS URL. This API accepts only those keys.

| Setting | Environment variable | Value |
| --- | --- | --- |
| Issuer | `SUPABASE_JWT_ISSUER` | `https://<project-ref>.supabase.co/auth/v1` |
| JWKS URI | `SUPABASE_JWKS_URI` | `https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json` |
| Audience | `SUPABASE_JWT_AUDIENCE` | `authenticated` |

The issuer string must match the token exactly, including the `/auth/v1` suffix and with no trailing slash. The audience is the Supabase access-token audience `authenticated`.

Supported algorithms are **RS256** and **ES256**. Supabase JWT Signing Keys currently default to ES256 (P-256). RSA keys are also accepted. The decoder reads `kid` from the token header and selects the matching public key from the JWKS document.

The legacy shared secret (HS256, the project JWT secret) is not accepted. A shared secret cannot be rotated by publishing a new public key, and this API will not be configured with it.

### Rotation

Nimbus caches the JWKS document. When a token arrives with a `kid` that is not in the cache, the decoder fetches the JWKS URL again. Publishing a new signing key in the Supabase dashboard is enough; the API process does not need a new secret or a restart.

Revocation is not instant. A key that was removed from the JWKS can still verify a token until the cache refreshes, because a cached key whose `kid` matches is used without another download. Rotate in this order:

1. In **Authentication > JWT Signing Keys** (also shown under Project Settings), create the new ECC P-256 or RSA key and publish it. Leave the previous key in the JWKS.
2. Wait until access tokens signed by the previous key have expired. Supabase's default access-token lifetime is one hour.
3. Revoke the previous key in the dashboard.

Do not disable JWT signing keys and fall back to the shared secret. Tokens from that mode will be rejected.

`application.yml` has no default issuer or JWKS URI. The process does not start until `SUPABASE_JWT_ISSUER`, `SUPABASE_JWKS_URI`, and `SUPABASE_JWT_AUDIENCE` are set. `PORTICO_CORS_ORIGINS` is a comma-separated list of frontend origins. The default is `http://localhost:5173`.

## What the frontend does

`@supabase/supabase-js` is the only authentication client.

| Action | Supabase API |
| --- | --- |
| Register | `signUp` with email and password. The display name is metadata only. |
| Sign in | `signInWithPassword` |
| Email verification | Confirm-email link. The app calls `resend` and `getUser` again. |
| Password recovery | `resetPasswordForEmail` to `/reset-password` |
| Password reset | `updateUser({ password })` after the recovery session is detected |
| Logout | `signOut` |
| Persistence | `persistSession: true` (local storage) |
| Refresh | `autoRefreshToken: true`, plus one refresh retry when the API returns 401 |
| Current user | `getUser()`, which asks Supabase Auth rather than trusting the cached session alone |

The access token is sent as `Authorization: Bearer`. `X-Organisation-Id` is sent only for an organisation returned by `GET /api/v1/me`. The server checks that membership again. After registration the user creates an organisation, enters its name, completes their profile, and becomes the owner. An invitation link is `/invite?token=...`. Accepting joins that organisation with the role stored on the invitation.

## API identity

| Method | Path | Who |
| --- | --- | --- |
| GET | `/api/v1/me` | Signed-in user, including someone with no organisation |
| PUT | `/api/v1/me` | Updates the display name. Email comes from the token. |
| GET | `/api/v1/organisations` | Active memberships for the token subject |
| POST | `/api/v1/organisations` | Creates an organisation and an `OWNER` membership for the token subject |
| GET | `/api/v1/organisations/{id}/members` | Active members of an organisation the caller belongs to |
| POST | `/api/v1/organisations/{id}/invitations` | Users with manage-users permission |
| POST | `/api/v1/invitations/accept` | The signed-in user, when the token email matches the invitation |
| DELETE | `/api/v1/organisations/{id}` | Owner, and only when portfolio rows do not still reference the organisation |

Portfolio routes stay on `/api/property`, `/api/tenant`, and the other existing paths. Every `/api/**` route requires a verified bearer token, except when the `local` profile is on. A missing, expired, unsigned, HMAC, or wrongly issued token is `401`. A token with `email_verified: false` is `403 EMAIL_NOT_VERIFIED`.

The `local` profile is a development bypass for a machine that has no Supabase users. It applies only when the bearer token is absent. A token that is present is still verified. Do not enable `portico.tenancy.local-access` in a shared or production deployment.

## Roles

`RolePermissionPolicy` is the only map from a role to a permission. Controllers do not compare role names. `ApiAuthorisationCatalog` maps the HTTP method and path onto one permission, and the filter asks the policy.

Membership roles are `OWNER`, `ADMIN`, `PROPERTY_MANAGER`, `ACCOUNTANT`, and `VIEWER`.

| Permission | Owner | Admin | Property manager | Accountant | Viewer |
| --- | --- | --- | --- | --- | --- |
| View properties | Yes | Yes | Yes | Yes | Yes |
| Manage properties | Yes | Yes | Yes | No | No |
| Manage tenants | Yes | Yes | Yes | No | No |
| Manage leases | Yes | Yes | Yes | No | No |
| View rent payments | Yes | Yes | Yes | Yes | Yes |
| Record rent payments | Yes | Yes | Yes | Yes | No |
| View maintenance, staff, addresses, vendors, notifications | Yes | Yes | Yes | Yes | Yes |
| Change those records | Yes | Yes | Yes | No | No |
| View subscription billing | Yes | Yes | No | No | No |
| Change subscription billing | Yes | No | No | No | No |
| View members | Yes | Yes | Yes | Yes | Yes |
| Invite and manage users | Yes | Yes | No | No | No |
| Delete organisation | Yes | No | No | No | No |

Admin billing is the limited column: an admin may read `GET /api/billing/subscription` and may not call a billing change. Recording rent is not subscription billing. There is no endpoint that changes a member's role. An invitation stores the role the inviter was allowed to grant, and accept uses that stored role. An owner may grant any role, including another owner. An admin may grant admin, property manager, accountant, or viewer, and may not grant owner. A user who is already a member is not upgraded by accepting another invitation.

A user may belong to more than one organisation. With one active membership the API selects it. With more than one, the caller sends `X-Organisation-Id` and the membership must exist. The header, a path id, a query parameter, and a body field cannot select an organisation the user has not joined. Repository queries filter by the resolved organisation id. Row-level security does not filter the Java login, because that login owns the tables.
