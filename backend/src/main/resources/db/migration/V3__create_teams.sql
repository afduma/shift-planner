create table teams (
    id uuid primary key,
    name varchar(150) not null,
    description text,
    active boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
