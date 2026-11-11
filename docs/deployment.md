# Deployment

The frontend and the API are deployed separately. This document does not start either one, and it does not apply database migrations.

## Vercel

Set the Vercel project root to `frontend/`. The install command is `npm ci`. The build command is `npm run build`. The output directory is `dist`, which `frontend/vercel.json` also declares.

`frontend/vercel.json` rewrites unknown paths to `index.html` so a refresh on a React Router path still loads the app. Existing files, including `/assets`, are served first.

These variables are public and are inlined when Vite builds. Do not put a service-role key, database password, JWT secret, or payment secret in them.

| Variable | Required value |
| --- | --- |
| `VITE_SUPABASE_URL` | Supabase project URL, for example `https://<project-ref>.supabase.co` |
| `VITE_SUPABASE_PUBLISHABLE_KEY` | Supabase publishable (anon) client key. Not the service-role key |
| `VITE_API_BASE_URL` | Public API origin plus `/api`, for example `https://<backend-host>/api`. A value ending in `/api/v1` is also accepted |
| `VITE_DEMO_MODE` | `false` |

A production build does not use the Vite dev-server proxy and does not fall back to the sample portfolio. Leave `VITE_BASE` unset so the site is served from `/`. In the Supabase dashboard, set the site URL and redirect allow list to the Vercel origin, for example `https://<vercel-host>`.

## Render

The repository root `Dockerfile` builds the Spring Boot jar with the Maven wrapper on Java 21 and runs it on a Java 21 JRE. The process reads `PORT` and otherwise listens on 8080. Use `/actuator/health` as the health check.

Set these on the Render service. They stay on the server. Do not invent the hostnames: copy them from the Supabase dashboard and from the Vercel project after the frontend URL exists.

| Variable | Required value |
| --- | --- |
| `SPRING_DATASOURCE_URL` | Supabase JDBC URL using the direct connection or the session pooler. Include `sslmode=require`. Do not use the transaction pooler on port 6543. Do not put the user or password in the URL |
| `SPRING_DATASOURCE_USERNAME` | Database user that belongs to that connection method |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SUPABASE_JWT_ISSUER` | `https://<project-ref>.supabase.co/auth/v1` |
| `SUPABASE_JWKS_URI` | `https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json` |
| `SUPABASE_JWT_AUDIENCE` | `authenticated` |
| `PORTICO_CORS_ORIGINS` | The exact Vercel origin, for example `https://<vercel-host>`. Several origins are comma-separated. Do not use `*` |
| `PORT` | Supplied by Render. The process listens on it. Local fallback is 8080 only when `PORT` is absent |
| `PORTICO_DB_POOL_SIZE` | Optional. Defaults to 5 |

`PORTICO_CORS_ORIGINS` has no production default. If it is missing, the API refuses to start instead of allowing `http://localhost:5173`. Do not set `PORTICO_DATASOURCE_ALLOW_LOCAL` or `portico.tenancy.local-access` on Render.

The health check path is `/actuator/health`.

Flyway runs when the API process starts. It applies pending migrations and does not clean the database. V1 is already applied. The pending migrations are:

- `V2__lease_term_dates.sql` adds optional `start_date` and `end_date` on `leases`. Existing leases may keep both dates null. A lease cannot store only one of the two dates.
- `V3__subscription_packages.sql` adds package limits, allows subscription status `DEMO` and invoice status `DEMO`, stores an invoice provider, and adds a foreign key so an invoice and its subscription share one organisation. It sets the catalogue limits to Starter 5 properties / 2 members, Growth 25 / 8, Professional 80 / 20, and Agency 300 / 60, including rows that would otherwise keep the column default of 1. Prices stay Starter R197, Growth R494, Professional R989, and Agency R1 979 per month. Organisations with no subscription row receive one Starter subscription with status `DEMO` and provider `demo`. Organisations that already have a subscription, including a canceled one, are not given a second row and are not reactivated. Properties and members are not deleted.

Starting the backend is the migration step. Do not paste these scripts into the Supabase SQL editor as a second copy.

## PayFast or Paystack later

Checkout in this build is explicitly a demo. `BillingProviderStatus.liveChargesEnabled()` stays false even when merchant credentials are present, because no webhook signature is verified. A simulated success stores subscription status `DEMO`, provider `demo`, and an invoice status of `DEMO`. It never stores invoice status `PAID` or subscription status `ACTIVE`. A simulated cancellation or failure writes neither. Do not set `PAYFAST_MERCHANT_ID`, `PAYFAST_MERCHANT_KEY`, or `PAYSTACK_SECRET_KEY` for this deployment.

To take a real recurring payment later, keep the provider behind the billing service and add all of the following before any row is marked paid:

PayFast:

- Merchant id, merchant key, and passphrase in the server environment only.
- A subscription or recurring billing setup created in PayFast for each package code.
- An ITN endpoint that rebuilds the posted payload, validates it with PayFast, and checks the signature with the passphrase.
- The browser return URL must not mark a subscription paid.

Paystack:

- Secret key in the server environment only.
- A Paystack plan for Starter, Growth, Professional, and Agency, with the same monthly rand amounts.
- A webhook that checks `x-paystack-signature` as HMAC SHA512 over the raw body using the secret key.
- Handle the subscription and successful charge events only after that check. Store the provider event id in `billing_webhook_events`.
- The browser return URL must not mark a subscription paid.

No card number is collected or stored by Portico.
