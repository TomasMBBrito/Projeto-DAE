.DEFAULT_GOAL := deploy

ifneq (,$(wildcard ./.env))
    include .env
    export
    APPLICATION_NAME ?= centroXYZ
endif

up:
	docker compose up -d

down:
	docker compose down --rmi local --volumes

down-all:
	docker compose down --rmi all --volumes

build:
	mvn clean package

bash:
	docker compose exec webserver bash

logs:
	docker compose logs -f webserver

sql:
	docker compose exec db psql --username ${DB_USER} --password ${DB_NAME}

ps:
	docker compose ps

deploy: build
	docker compose cp target/${APPLICATION_NAME}.war webserver:/opt/jboss/wildfly/standalone/deployments

monitor: deploy logs

mails:
	docker compose exec --workdir /var/mail smtp bash

ollama-pull:
	docker compose exec ollama ollama pull llama3.2

ollama-bash:
	docker compose exec ollama bash

ollama-test:
	docker compose exec ollama ollama run llama3.2

ollama-logs:
	docker compose logs -f ollama

ollama-models:
	docker compose exec ollama ollama list
