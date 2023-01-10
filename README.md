# Maps-Server

- continually indexes map repositories in 'github.com/triplea-maps'
- stores indexed map information in database
- uses last commit date to know if a map has been updated since the previous indexing
- provides APIs that can be used to query the maps database, eg: fetch-map-listing


## How to build

- Be sure you have set-up an API key in your ~/.gradle/gradle.properties
  <https://github.com/triplea-game/docs/tree/master/development>
- `./gradlew check`

## How to Run

- An IDEA launcher is checked in, can be accessed via the run menu
