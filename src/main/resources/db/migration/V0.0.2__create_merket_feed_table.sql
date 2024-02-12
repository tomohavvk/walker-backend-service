create table if not exists devices_locations (
    device_id varchar(64) not null,
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