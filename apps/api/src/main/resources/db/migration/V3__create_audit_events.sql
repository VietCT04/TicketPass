create table audit_events (
    id uuid primary key,
    actor_user_id uuid not null references users(id),
    action varchar(80) not null,
    entity_type varchar(80) not null,
    entity_id uuid not null,
    created_at timestamptz not null
);

create index idx_audit_events_entity
    on audit_events(entity_type, entity_id);
