CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- devices
create table if not exists devices (
 id varchar(64) not null PRIMARY KEY,
 name varchar(64) not null,
 created_at timestamp not null
);
--

-- devices groups
create table if not exists groups (
 id varchar(64) PRIMARY KEY,
 public_id varchar(21) not null,
 owner_device_id varchar(64) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
 name varchar(128) not null,
 description varchar(256) not null,
 device_count numeric not null,
 is_public boolean not null,
 created_at timestamp not null,
 updated_at timestamp not null
);

create unique index if not exists groups_public_id_uniq_idx on groups(public_id);
create  index if not exists groups_public_id_idx on groups USING gin(public_id gin_trgm_ops);
create index if not exists groups_name_trgm_idx on groups USING gin(name gin_trgm_ops);
create index if not exists groups_is_public_idx on groups(is_public);
create index if not exists groups_owner_device_id_idx on groups(owner_device_id);
create index if not exists groups_updated_at_idx on groups(updated_at desc);

create table if not exists devices_groups (
 device_id varchar(64) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
 group_id varchar(64) NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
 created_at timestamp not null
);

create unique index if not exists devices_groups_device_id_group_id_idx on devices_groups(device_id, group_id);
--

-- devices_locations
create table if not exists devices_locations (
    device_id varchar(64) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    latitude numeric not null,
    longitude numeric not null,
    accuracy numeric not null,
    altitude numeric not null,
    speed numeric not null,
    bearing numeric not null,
    altitude_accuracy numeric not null,
    time timestamp not null
);
create unique index if not exists devices_locations_device_id_time_idx on devices_locations(device_id, time);

--
