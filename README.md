# Support Server

## Tech Overview

- Java 21
- DropWizard (http server)
- Junit5 (unit tests)
- assertj (unit tests)
- JDBI (no ORM, JDBI instead)
- Postgres (database layer)
- Docker & Docker Compose 
- gradle (build tool)
- Makefile (developer build commands)


## Development

If working on shared code between `triplea` and `support-server`, see: `make localBuild`.
Warning: First make sure that the local triplea project can build cleanly.

- Run `make help` for list of full commands
- `make verify` will run formatting and all tests (unit+integ)

### Integration Tests

These are tests that run against a live database & server running on locally.
The server & database are set up with docker-compose.
First we build a shadow jar, then we use a gradle plugin to launch docker-compose
which stands up database, runs migration (with flyway), and launches the server.


## Deployment

### Output Artifacts

(1) 'Application' Docker image for running the support-server application

(2) 'Flyway' Docker image containing database migrations


### CI/CD

On merge to master:
- builds a new docker image & publishes the new image to Github Packages

See:
- `.github/workflows/master.yml`


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


### Maps System Design

#### Key Classes

- `MapIndexer`: fetches all the data of a given map, creates a `MapIndexingResult`
- `MapIndexingResult`: represents all desired data of a parsed map, eg: map name, download size, description
- `MapIndexDao`: upserts `MapIndexingResult` into database

