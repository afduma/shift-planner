create table shifts (
    id uuid primary key,
    user_id uuid not null,
    team_id uuid not null,
    start_at timestamptz not null,
    end_at timestamptz not null,
    type varchar(20) not null,
    notes text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_shifts_user foreign key (user_id) references users (id),
    constraint fk_shifts_team foreign key (team_id) references teams (id),
    constraint chk_shifts_type check (type in ('REGULAR', 'TRAINING', 'ON_CALL')),
    constraint chk_shifts_time_range check (end_at > start_at)
);

create index idx_shifts_user_start_at on shifts (user_id, start_at);
create index idx_shifts_team_start_at on shifts (team_id, start_at);
