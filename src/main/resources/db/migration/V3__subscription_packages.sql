-- Package catalogue, demo subscription status, and organisation caps.
-- Does not delete properties, members, or canceled subscriptions.
-- Does not record a payment.

alter table subscription_plans
    add column max_properties integer not null default 1,
    add column max_users integer not null default 1;

alter table subscription_plans
    add constraint subscription_plans_limits_chk
        check (max_properties > 0 and max_users > 0);

alter table organisation_subscriptions
    drop constraint organisation_subscriptions_status_chk;

alter table organisation_subscriptions
    add constraint organisation_subscriptions_status_chk
        check (status in ('TRIALING', 'ACTIVE', 'PAST_DUE', 'CANCELED', 'DEMO'));

drop index organisation_subscriptions_one_open_uq;

create unique index organisation_subscriptions_one_open_uq
    on organisation_subscriptions (organisation_id)
    where status in ('TRIALING', 'ACTIVE', 'PAST_DUE', 'DEMO');

alter table organisation_subscriptions
    add constraint organisation_subscriptions_id_org_uq unique (id, organisation_id);

alter table billing_invoices
    drop constraint billing_invoices_status_chk;

alter table billing_invoices
    add constraint billing_invoices_status_chk
        check (status in ('DRAFT', 'OPEN', 'PAID', 'VOID', 'DEMO'));

alter table billing_invoices
    add column plan_code varchar(40);

alter table billing_invoices
    add column provider varchar(40) not null default 'none';

alter table billing_invoices
    alter column provider drop default;

alter table billing_invoices
    add constraint billing_invoices_subscription_org_fk
        foreign key (subscription_id, organisation_id)
        references organisation_subscriptions (id, organisation_id);

insert into subscription_plans (id, code, name, amount, currency, interval_unit, max_properties, max_users)
values
    ('a1000000-0000-4000-8000-000000000001', 'STARTER', 'Starter', 197.00, 'ZAR', 'MONTH', 5, 2),
    ('a1000000-0000-4000-8000-000000000002', 'GROWTH', 'Growth', 494.00, 'ZAR', 'MONTH', 25, 8),
    ('a1000000-0000-4000-8000-000000000003', 'PROFESSIONAL', 'Professional', 989.00, 'ZAR', 'MONTH', 80, 20),
    ('a1000000-0000-4000-8000-000000000004', 'AGENCY', 'Agency', 1979.00, 'ZAR', 'MONTH', 300, 60)
on conflict (code) do update set
    name = excluded.name,
    amount = excluded.amount,
    currency = excluded.currency,
    interval_unit = excluded.interval_unit,
    max_properties = excluded.max_properties,
    max_users = excluded.max_users,
    updated_at = now();

alter table subscription_plans alter column max_properties drop default;
alter table subscription_plans alter column max_users drop default;

-- Organisations that already have any subscription, including a canceled one, are left unchanged.
insert into organisation_subscriptions (id, organisation_id, plan_id, status, provider, created_at, updated_at)
select gen_random_uuid(), organisation.id, starter.id, 'DEMO', 'demo', now(), now()
from organisations organisation
join subscription_plans starter on starter.code = 'STARTER'
where not exists (
    select 1
    from organisation_subscriptions existing
    where existing.organisation_id = organisation.id
);
