create table map_index
(
    id               serial primary key,
    map_name         varchar(256) not null,
    last_commit_date timestamptz  not null check (last_commit_date < now()),
    repo_url         varchar(256) not null unique check (repo_url like 'http%'),
    preview_image_url varchar(256) not null check (repo_url like 'http%'),
    description         varchar(3000) not null,
    download_size_bytes integer       not null,
    download_url        varchar(256)  not null unique check (download_url like 'http%'),
    default_branch   varchar(128) not null,
    date_created     timestamptz  not null default now(),
    date_updated     timestamptz  not null default now()
);

create table map_tag
(
    id serial primary key,
    name varchar(64) not null unique,
    display_order int not null unique check (display_order >=0 and display_order <= 1000)
);
comment on table map_tag is 'Defines the set of map tags.';

create table map_tag_allowed_value
(
    id serial primary key,
    map_tag_id int not null references map_tag(id),
    value varchar(64) not null
);
alter table map_tag_allowed_value
    add constraint map_tag_values_unique unique (map_tag_id, value);
comment on table map_tag_allowed_value is 'Defines the set of allowed values per tag.';

create table map_tag_value
(
    id serial primary key,
    map_tag_id int not null references map_tag(id),
    map_index_id int not null references map_index(id),
    map_tag_allowed_value_id int not null references map_tag_allowed_value(id)
);
alter table map_tag_value
    add constraint map_tag_value_unique unique (map_tag_id, map_index_id);
comment on table map_tag_value is 'Join table relating maps to tags';
