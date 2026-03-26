# Production Checklist

## Before First Deploy

- Fill `infra/.env.prod` from `infra/.env.prod.example`.
- Set strong `JWT_SECRET` (>=32 chars, random).
- Change all default passwords.
- Keep `SWAGGER_ENABLED=false` unless explicitly needed.

## Deploy

```powershell
docker compose --env-file infra/.env.prod -f infra/docker-compose.prod.yml up --build -d
```

## Verify

- `docker compose --env-file infra/.env.prod -f infra/docker-compose.prod.yml ps`
- `http://localhost/` loads frontend.
- `http://localhost/gateway/services` responds from gateway.
- `http://localhost:8762` is optional in prod compose (not exposed by default).

## Security Notes

- In `prod` profile, gateway and auth reject default JWT secret at startup.
- Swagger can be disabled in production via `SWAGGER_ENABLED=false`.
- Only web port `80` is exposed in production compose.
- Internal services run on Docker network only.
