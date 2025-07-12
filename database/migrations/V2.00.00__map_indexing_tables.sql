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

-- eg: era, difficulty
create table map_tag_category
(
    id serial primary key,
    name varchar(64) not null unique,
    display_order int not null unique check (display_order >=0 and display_order <= 1000)
);

create table map_tag
(
    id serial primary key,
    value varchar(64) not null unique,
    map_tag_category_id int not null references map_tag_category(id)
);

create table map_index_tag
(
    id serial primary key,
    map_tag_id int not null references map_tag(id),
    map_index_id int not null references map_index(id)
);
alter table map_index_tag
    add constraint map_index_tag_uk unique (map_tag_id, map_index_id);
comment on table map_index_tag is 'Join table relating maps to tags';

        /*
            assertThat(mapTags).contains(MapTag.builder().name("difficulty").value("easy").build());
    assertThat(mapTags).contains(MapTag.builder().name("era").value("ancient").build());
  }
         */