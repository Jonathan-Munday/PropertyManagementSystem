# Portico

Property management dashboard for the Java backend. Sign-in, registration, email confirmation, and password reset go through Supabase Auth. The API at `http://localhost:8080/api` receives the Supabase access token. See [docs/authentication.md](../docs/authentication.md).

```bash
npm install
npm run dev
```

`frontend/.env` already exists. Do not overwrite it. `.env.example` shows the variable names. `VITE_DEMO_MODE` stays `false` so a failed API call does not replace the desk with sample properties.

```bash
VITE_SUPABASE_URL=https://<project-ref>.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=<publishable key>
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_DEMO_MODE=false
```

New records use UUID identifiers. The API requires a bearer token whose `sub` is a member of an organisation, unless the API is started with the `local` profile. See `docs/database.md`.
