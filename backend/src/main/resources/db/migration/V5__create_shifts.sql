create table shifts (
    id uuid primary key,
    user_id uuid not null,
    team_id uuid not null,
    shift_date date not null,
    start_time time not null,
    end_time time not null,
    type varchar(20) not null,
    notes text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_shifts_user foreign key (user_id) references users (id),
    constraint fk_shifts_team foreign key (team_id) references teams (id),
    constraint chk_shifts_type check (type in ('REGULAR', 'TRAINING', 'ON_CALL')),
    constraint chk_shifts_time_range check (end_time > start_time)
);

create index idx_shifts_user_date on shifts (user_id, shift_date);
create index idx_shifts_team_date on shifts (team_id, shift_date);
