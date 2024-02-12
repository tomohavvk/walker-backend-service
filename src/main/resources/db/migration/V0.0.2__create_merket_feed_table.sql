-- devices
create table if not exists devices (
 id varchar(64) not null PRIMARY KEY,
 name varchar(64) not null,
 created_at timestamp not null
);
--

-- groups
create table if not exists groups (
 id varchar(64) PRIMARY KEY,
 owner_device_id varchar(64) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
 name varchar(128) not null,
 created_at timestamp not null
);

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
