SHELL=/bin/bash

help tasks targets: ## Show this help text
	grep -h -E '^[a-z]+.*:' $(MAKEFILE_LIST) | \
		awk -F ":|#+" '{printf "\033[31m%s \033[0m \n   %s \033[0m\n    \033[3;37mDepends On: \033[0m [ %s ]\n", $$1, $$3, $$2}'

printVersions: ## Prints versions of system dependencies (EG: java, docker)
	echo -e "\033[31m### Java Version on OS ###\033[0m"
	java -version

	echo -e "\n\033[31m### Docker Compose Version ###\033[0m"
	docker compose version

	echo -e "\n\033[31m### Versions used by Gradle ###\033[0m"
	./gradlew --version

format: ## Runs formatting
	./gradlew spotlessApply

testForPr: ## Runs all checks used to verify a Pull-Request
	./gradlew check

.PHONY: test verify
test verify: format testForPr ## Applies formatting and runs all tests locally

.PHONY: clean
clean: ## Removes build artifacts and stops docker containers and removes docker volumes
	./gradlew clean

.PHONY: up
up: ## Build & run server, launches a docker database
	docker compose build flyway
	./gradlew composeUp

.PHONY: connectDb
connectDb: up ## Connects to locally running docker database
	docker exec -u postgres -it support-server-database-1 psql support_db

.PHONEY: serverLogs
serverLogs: ## Util command to print the server logs
	docker logs support-server-server-1

buildContainers: ## Creates 'docker container' build artifacts
	./gradlew shadowJar
	docker build database -f database/flyway.Dockerfile --tag ghcr.io/triplea-game/support-server/flyway:latest
	docker build . --tag ghcr.io/triplea-game/support-server/server:latest

pushContainers: buildContainers ## Pushes 'docker container' build artifacts to github docker container registry
	docker push ghcr.io/triplea-game/support-server/flyway:latest
	docker push ghcr.io/triplea-game/support-server/server:latest

localBuild: ## Builds 'triplea' game client dependency, useful if working on shared libraries between 'support-server' and 'triplea'
	./gradlew --info --include-build ../triplea compileJava
