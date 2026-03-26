create table team_memberships (
    id uuid primary key,
    user_id uuid not null,
    team_id uuid not null,
    role varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_team_memberships_user foreign key (user_id) references users (id),
    constraint fk_team_memberships_team foreign key (team_id) references teams (id),
    constraint uk_team_membership_user_team unique (user_id, team_id),
    constraint chk_team_memberships_role check (role in ('MEMBER', 'PLANNER', 'LEAD'))
);

create index idx_team_memberships_user_id on team_memberships (user_id);
create index idx_team_memberships_team_id on team_memberships (team_id);
