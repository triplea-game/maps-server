SHELL=/bin/bash

help:     ## Show this help.
	grep -h -E '(\s##\s|^##\s)' $(MAKEFILE_LIST) | egrep -v '^--' | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[32m  %-35s\033[0m %s\n", $$1, $$2}'

.PHONY: test
test:
	./gradlew spotlessApply check --info

.PHONY: clean
clean:
	docker compose down --volumes
	./gradlew clean

.PHONY: up
up:
	./gradlew composeUp
