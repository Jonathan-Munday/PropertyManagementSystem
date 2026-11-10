-- Initial Portico schema for an empty database.
-- Supabase Auth owns auth.users. This migration does not create or alter that table.
-- The profile foreign key is added only when auth.users is already present.
-- No sample portfolio rows and no users are inserted.
-- Supabase's default search_path can place an unqualified table outside public.
set local search_path = public;

create or replace function portico_set_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

revoke all on function portico_set_updated_at() from public;

create table organisations (
    id uuid primary key,
    name varchar(200) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table user_profiles (
    id uuid primary key,
    display_name varchar(200),
    email varchar(255),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index user_profiles_email_lower_uq
    on user_profiles (lower(email))
    where email is not null;

create table organisation_memberships (
    id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete cascade,
    user_id uuid not null references user_profiles (id) on delete cascade,
    role varchar(40) not null,
    status varchar(40) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint organisation_memberships_role_chk
        check (role in ('OWNER', 'ADMIN', 'PROPERTY_MANAGER', 'ACCOUNTANT', 'VIEWER')),
    constraint organisation_memberships_status_chk check (status in ('ACTIVE', 'SUSPENDED')),
    constraint organisation_memberships_org_user_uq unique (organisation_id, user_id)
);

create index organisation_memberships_user_idx on organisation_memberships (user_id);

create table organisation_invitations (
    id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete cascade,
    email varchar(255) not null,
    role varchar(40) not null,
    status varchar(40) not null default 'PENDING',
    token varchar(80) not null,
    expires_at timestamptz not null,
    invited_by uuid references user_profiles (id) on delete set null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint organisation_invitations_role_chk
        check (role in ('OWNER', 'ADMIN', 'PROPERTY_MANAGER', 'ACCOUNTANT', 'VIEWER')),
    constraint organisation_invitations_status_chk
        check (status in ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED')),
    constraint organisation_invitations_token_uq unique (token)
);

create unique index organisation_invitations_pending_email_uq
    on organisation_invitations (organisation_id, lower(email))
    where status = 'PENDING';

create table addresses (
    address_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    street_address varchar(500) not null,
    city varchar(120),
    province varchar(120),
    postal_code varchar(20),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint addresses_org_id_uq unique (organisation_id, address_id)
);

create index addresses_org_idx on addresses (organisation_id);

create table properties (
    property_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    address varchar(500) not null,
    description varchar(4000),
    rental_amount numeric(12, 2) not null,
    available boolean not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint properties_rent_chk check (rental_amount >= 0),
    constraint properties_org_id_uq unique (organisation_id, property_id)
);

create index properties_org_idx on properties (organisation_id);
create index properties_org_available_idx on properties (organisation_id, available);

create table tenants (
    tenant_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    first_name varchar(120) not null,
    last_name varchar(120),
    email varchar(255) not null,
    phone_number varchar(40),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint tenants_org_id_uq unique (organisation_id, tenant_id)
);

create index tenants_org_idx on tenants (organisation_id);
create index tenants_org_email_idx on tenants (organisation_id, email);

create table leases (
    lease_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    property_id uuid not null,
    tenant_id uuid,
    tenant_name varchar(200) not null,
    monthly_rent numeric(12, 2) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint leases_rent_chk check (monthly_rent >= 0),
    constraint leases_org_id_uq unique (organisation_id, lease_id),
    constraint leases_property_org_fk foreign key (organisation_id, property_id)
        references properties (organisation_id, property_id) on delete restrict,
    constraint leases_tenant_org_fk foreign key (organisation_id, tenant_id)
        references tenants (organisation_id, tenant_id) on delete restrict
);

create index leases_org_idx on leases (organisation_id);
create index leases_property_idx on leases (property_id);
create index leases_tenant_idx on leases (tenant_id);

create table rent_payments (
    payment_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    lease_id uuid not null,
    amount numeric(12, 2) not null,
    payment_date date not null,
    payment_method varchar(40) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint rent_payments_amount_chk check (amount > 0),
    constraint rent_payments_org_id_uq unique (organisation_id, payment_id),
    constraint rent_payments_lease_org_fk foreign key (organisation_id, lease_id)
        references leases (organisation_id, lease_id) on delete restrict
);

create index rent_payments_org_idx on rent_payments (organisation_id);
create index rent_payments_lease_idx on rent_payments (lease_id);

create table maintenance_requests (
    request_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    description varchar(4000) not null,
    status varchar(40) not null,
    reported_date date not null,
    cost numeric(12, 2) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint maintenance_requests_status_chk check (status in ('OPEN', 'IN_PROGRESS', 'RESOLVED')),
    constraint maintenance_requests_cost_chk check (cost >= 0),
    constraint maintenance_requests_org_id_uq unique (organisation_id, request_id)
);

create index maintenance_requests_org_idx on maintenance_requests (organisation_id);

create table managers (
    manager_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    name varchar(200) not null,
    contact_number varchar(40),
    employee_id varchar(64),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint managers_org_id_uq unique (organisation_id, manager_id)
);

create index managers_org_idx on managers (organisation_id);

create table agents (
    agent_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    name varchar(200) not null,
    email varchar(255) not null,
    role varchar(80) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint agents_org_id_uq unique (organisation_id, agent_id)
);

create index agents_org_idx on agents (organisation_id);

create table vendors (
    vendor_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    company_name varchar(200) not null,
    contact_number varchar(40) not null,
    specialty varchar(120) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint vendors_org_id_uq unique (organisation_id, vendor_id)
);

create index vendors_org_idx on vendors (organisation_id);

create table notifications (
    notification_id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    tenant_id uuid not null,
    message varchar(2000) not null,
    notification_type varchar(40),
    date_sent varchar(40),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint notifications_org_id_uq unique (organisation_id, notification_id),
    constraint notifications_tenant_org_fk foreign key (organisation_id, tenant_id)
        references tenants (organisation_id, tenant_id) on delete restrict
);

create index notifications_org_idx on notifications (organisation_id);
create index notifications_tenant_idx on notifications (tenant_id);

create table subscription_plans (
    id uuid primary key,
    code varchar(40) not null,
    name varchar(120) not null,
    amount numeric(12, 2) not null,
    currency varchar(3) not null default 'ZAR',
    interval_unit varchar(20) not null default 'MONTH',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint subscription_plans_code_uq unique (code),
    constraint subscription_plans_amount_chk check (amount >= 0),
    constraint subscription_plans_interval_chk check (interval_unit in ('MONTH', 'YEAR'))
);

create table organisation_subscriptions (
    id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    plan_id uuid not null references subscription_plans (id) on delete restrict,
    status varchar(40) not null,
    provider varchar(40) not null default 'none',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint organisation_subscriptions_status_chk
        check (status in ('TRIALING', 'ACTIVE', 'PAST_DUE', 'CANCELED'))
);

create unique index organisation_subscriptions_one_open_uq
    on organisation_subscriptions (organisation_id)
    where status in ('TRIALING', 'ACTIVE', 'PAST_DUE');

create index organisation_subscriptions_org_idx on organisation_subscriptions (organisation_id);

create table billing_invoices (
    id uuid primary key,
    organisation_id uuid not null references organisations (id) on delete restrict,
    subscription_id uuid references organisation_subscriptions (id) on delete restrict,
    amount numeric(12, 2) not null,
    currency varchar(3) not null default 'ZAR',
    status varchar(40) not null,
    issued_on date not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint billing_invoices_amount_chk check (amount >= 0),
    constraint billing_invoices_status_chk check (status in ('DRAFT', 'OPEN', 'PAID', 'VOID'))
);

create index billing_invoices_org_idx on billing_invoices (organisation_id);

create table billing_webhook_events (
    id uuid primary key,
    organisation_id uuid references organisations (id) on delete set null,
    provider varchar(40) not null,
    external_event_id varchar(120) not null,
    status varchar(40) not null,
    received_at timestamptz not null default now(),
    payload jsonb not null default '{}'::jsonb,
    constraint billing_webhook_events_status_chk
        check (status in ('RECEIVED', 'PROCESSED', 'IGNORED', 'FAILED')),
    constraint billing_webhook_events_external_uq unique (provider, external_event_id)
);

create index billing_webhook_events_org_idx on billing_webhook_events (organisation_id);

create table audit_logs (
    id uuid primary key,
    organisation_id uuid references organisations (id) on delete restrict,
    actor_user_id uuid references user_profiles (id) on delete set null,
    action varchar(40) not null,
    entity_type varchar(80) not null,
    entity_id uuid,
    recorded_at timestamptz not null default now(),
    constraint audit_logs_action_chk check (action in ('CREATE', 'UPDATE', 'DELETE'))
);

create index audit_logs_org_recorded_idx on audit_logs (organisation_id, recorded_at desc);

do $$
declare
    target text;
begin
    foreach target in array array[
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
        'billing_invoices'
    ]
    loop
        execute format(
            'create trigger %I before update on %I for each row execute function portico_set_updated_at()',
            target || '_set_updated_at',
            target
        );
    end loop;
end $$;

do $$
begin
    if exists (
        select 1
        from information_schema.tables
        where table_schema = 'auth' and table_name = 'users'
    ) then
        alter table user_profiles
            add constraint user_profiles_id_fkey
            foreign key (id) references auth.users (id) on delete cascade;
    end if;
end $$;

-- Row-level security locks out the Supabase Data API. The Java connection owns
-- these tables and is not filtered by these policies. Organisation membership
-- and role checks stay in the Java API. No permissive policy is created, because
-- a policy for the authenticated role would let a member bypass those role checks.
do $$
declare
    target text;
    api_role text;
begin
    foreach target in array array[
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
        'audit_logs'
    ]
    loop
        execute format('alter table %I enable row level security', target);
        execute format('revoke all on table %I from public', target);
        foreach api_role in array array['anon', 'authenticated', 'service_role']
        loop
            if exists (select 1 from pg_roles where rolname = api_role) then
                execute format('revoke all on table %I from %I', target, api_role);
            end if;
        end loop;
    end loop;
end $$;
