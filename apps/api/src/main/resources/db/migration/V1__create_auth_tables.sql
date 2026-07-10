create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(255) not null,
    display_name varchar(120) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table auth_sessions (
    id uuid primary key,
    user_id uuid not null references users(id),
    token_hash varchar(255) not null unique,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null,
    last_used_at timestamptz not null
);

create index idx_auth_sessions_user_id on auth_sessions(user_id);
create index idx_auth_sessions_expires_at on auth_sessions(expires_at);
