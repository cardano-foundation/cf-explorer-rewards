compose-up:
	docker compose --env-file .env.${network} -p reward-${network} up -d --build

compose-down:
	docker compose --env-file .env.${network} -p reward-${network} down


