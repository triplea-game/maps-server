# Maps Server


## Map Indexing Overview

Maps are stored, one each, as a repository in the github organization:
[triplea-maps](https://github.com/triplea-maps/)

The server keeps a database of all maps. For each map, we store the following data:


(1) repository URL. EG: https://github.com/triplea-maps/test-map

The server gets the list of all repositories from github's web API, so we can
automatically get this list from github.

(2) map name. Read from the 'map.yml' file in the repository, 'map_name' attribute.

EG: https://github.com/triplea-maps/test-map/blob/master/map.yml

(3) preview image URL. We simply assume this file is named 'preview.png'

(4) description. We assume this data will be contained in a file 'description.html'

EG: https://github.com/triplea-maps/test-map/blob/master/description.html

(5) version. The server can store a version number for each map starting at 'one'. The
server can also obtain from github the last time a repository was updated. Whenever
this 'last updated' timestamp updates, we can update the version number of the map. So
this is fully automated.

(6) download size. Whenever a map repository changes, the server can actually physically
downoad the map file and determine the size. The download size is stored in database,
and can be returned as part of the 'list-maps' payload to clients.




## Development

./gradlew --info --include-build ../triplea compileJava

- gotchya, make sure 'triplea' main project (the included build) compiles successfully!

`./gradlew clean` will remove stop all docker containers and remove database data.


## Deployment

On merge to master:
- builds a new docker image & publishes the new image to Github Packages
- deploys to production:
    - sets up production environment (idempotent install)
    - runs database migration
    - triggers production environment blue/green deployment

See:
- `.github/workflows/master.yml`
- `deploy/run.sh`


### Connect to DB:

Typically something like this (double check the container name, that can change):

```bash
docker exec -it --user postgres maps-server-database-1 psql maps_db
```


### Maps System Design

#### Key Classes

- `MapIndexer`: fetches all the data of a given map, creates a `MapIndexingResult`
- `MapIndexingResult`: represents all desired data of a parsed map, eg: map name, download size, description
- `MapIndexDao`: upserts `MapIndexingResult` into database

