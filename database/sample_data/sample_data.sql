insert into map_index
(id, map_name, last_commit_date, repo_url,
 description, download_size_bytes, download_url,
 preview_image_url)
values (1, 'test-map', now() - interval '1 days', 'http://repo-url',
        'map description', 1024, 'http://download-url',
        'http://preview-image');

insert into map_tag (id, name, display_order)
values (1000, 'Rating', 1),
       (2000, 'Category', 2);

insert into map_tag_allowed_value (id, map_tag_id, value)
values (400, 2000, 'Complete'),
       (401, 2000, 'Awesome');

