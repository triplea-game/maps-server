# Maps Server

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
