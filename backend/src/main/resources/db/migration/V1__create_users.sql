create table users (
    id uuid primary key,
    email varchar(255) not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    active boolean not null,
    system_role varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_users_email unique (email),
    constraint chk_users_system_role check (system_role in ('ADMIN', 'USER'))
);
