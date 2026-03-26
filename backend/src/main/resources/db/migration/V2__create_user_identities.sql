create table user_identities (
    id uuid primary key,
    user_id uuid not null,
    provider varchar(20) not null,
    subject varchar(255) not null,
    password_hash varchar(100),
    created_at timestamptz not null,
    constraint fk_user_identities_user foreign key (user_id) references users (id),
    constraint uk_user_identity_provider_subject unique (provider, subject),
    constraint chk_user_identities_provider check (provider in ('LOCAL', 'OIDC'))
);

create index idx_user_identities_user_id on user_identities (user_id);
