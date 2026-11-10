-- Date-only lease terms. Existing rows may keep both dates null.
-- A lease may not store only one of the two dates.

alter table leases
    add column start_date date,
    add column end_date date;

alter table leases
    add constraint leases_term_chk
        check (
            (start_date is null and end_date is null)
            or (
                start_date is not null
                and end_date is not null
                and end_date >= start_date
            )
        );
