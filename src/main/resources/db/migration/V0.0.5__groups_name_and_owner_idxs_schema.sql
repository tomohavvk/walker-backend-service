create index if not exists groups_name_idx on groups(name);
create index if not exists groups_is_private_idx on groups(is_private);
create index if not exists groups_owner_device_id_idx on groups(owner_device_id);
