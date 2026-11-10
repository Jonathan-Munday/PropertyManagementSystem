-- Read-only check before the first Portico migration.
-- Safe to run in the Supabase SQL Editor. It does not create, change, or delete anything.
-- Interpretation:
--   Zero rows: none of the Portico tables or flyway_schema_history exist in public. The first migration may be considered.
--   Any row: stop. Do not start the API. Inspect that object before applying Flyway.
-- This query does not look inside auth, storage, or other Supabase-managed schemas.

select table_schema, table_name, table_type
from information_schema.tables
where table_schema = 'public'
  and table_name in (
      'organisations',
      'user_profiles',
      'organisation_memberships',
      'organisation_invitations',
      'addresses',
      'properties',
      'tenants',
      'leases',
      'rent_payments',
      'maintenance_requests',
      'managers',
      'agents',
      'vendors',
      'notifications',
      'subscription_plans',
      'organisation_subscriptions',
      'billing_invoices',
      'billing_webhook_events',
      'audit_logs',
      'flyway_schema_history'
  )
order by table_name;
