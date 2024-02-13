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
 owner_device_id varchar(64) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
 name varchar(128) not null,
 device_count numeric not null,
 created_at timestamp not null
);

create unique index if not exists groups_owner_device_id_name_idx on groups(owner_device_id, name);

create table if not exists devices_groups (
 device_id varchar(64) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
 group_id varchar(64) NOT NULL REFERENCES groups(id) ON DELETE CASCADE
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
