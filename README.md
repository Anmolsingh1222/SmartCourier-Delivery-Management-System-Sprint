# SmartCourier Platform (Final v3)

React frontend + Spring Boot microservices + Spring Cloud Gateway + Eureka Discovery + MySQL + Flyway + Swagger + Postman + Docker.

## Microservices

- `eureka-server` (`8761`)
- `api-gateway` (`8080`)
- `auth-service` (`8081`)
- `delivery-service` (`8082`)
- `tracking-service` (`8083`)
- `admin-service` (`8084`)
- `frontend/web` (`5173` dev, `80` in container)

## One-command full run (dev/demo)

```powershell
docker compose -f infra/docker-compose.full.yml up --build
```

## Access URLs

- Frontend: `http://localhost:5174`
- Eureka Dashboard: `http://localhost:8762`
- Gateway Base: `http://localhost:8088/gateway`

## Swagger/OpenAPI (dev/demo)

- Gateway: `http://localhost:8088/swagger-ui.html`
- Auth: `http://localhost:8081/swagger-ui/index.html`
- Delivery: `http://localhost:8082/swagger-ui/index.html`
- Tracking: `http://localhost:8083/swagger-ui/index.html`
- Admin: `http://localhost:8084/swagger-ui/index.html`

## Postman

Import both files:

- Collection: `docs/postman/SmartCourier_API.postman_collection.json`
- Environment: `docs/postman/SmartCourier_Local.postman_environment.json`

Then run in order:

1. `Auth -> Login` (auto-sets `accessToken` + `refreshToken`)
2. Delivery flow requests
3. Tracking flow requests
4. Admin flow requests

## Default Admin

Bootstrap admin created by auth-service:

- Email: `admin@smartcourier.local`
- Password: `Admin@12345`

Override via env vars:

- `BOOTSTRAP_ADMIN_ENABLED`
- `BOOTSTRAP_ADMIN_NAME`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`

## Current Technical Status

- Eureka service discovery enabled for gateway + all business services.
- Gateway uses discovery routing (`lb://auth-service`, etc.)
- JWT security + role checks (`ADMIN` restrictions enforced in gateway and service layer)
- Delivery lifecycle emits tracking events automatically.
- Auth refresh tokens persisted/revoked/rotated.
- Unit tests added across auth/delivery/tracking/admin core logic.
- CI workflow added (`.github/workflows/ci.yml`) for backend tests + frontend build.

## Local non-docker mode (optional)

1. Start MySQL: `docker compose -f infra/docker-compose.yml up -d`
2. Start Eureka: `mvn -pl services/eureka-server spring-boot:run`
3. Start all services + gateway + frontend.

## Production Mode

1. Create production env file from template:

```powershell
Copy-Item infra/.env.prod.example infra/.env.prod
```

2. Edit `infra/.env.prod` and set strong values for:
   - `MYSQL_ROOT_PASSWORD`
   - `MYSQL_APP_PASSWORD`
   - `JWT_SECRET` (minimum 32 chars)
   - `BOOTSTRAP_ADMIN_PASSWORD`

3. Start production stack:

```powershell
docker compose --env-file infra/.env.prod -f infra/docker-compose.prod.yml up --build -d
```

4. Production entrypoint:
   - App + API (single entry): `http://localhost/`
   - API base: `http://localhost/gateway`
   - Swagger (if `SWAGGER_ENABLED=true`): `http://localhost/swagger-ui.html`

### MySQL Workbench Connection

- Host: `127.0.0.1`
- Port: `3307` (dev/demo stack)
- Username: `root`
- Password: `root` (change for production)

For production compose, MySQL is intentionally not exposed publicly by default.

## Jenkins CI (localhost:8085)

### Prerequisites in Jenkins UI

Install plugins:
- `Pipeline`
- `Git`

If you are using the provided `jenkins/jenkins:lts` Docker container,
install required build tools once inside container:

```powershell
docker exec -u root smartcourier-jenkins sh -lc "apt-get update && apt-get install -y maven nodejs npm && apt-get clean"
```

### Create Pipeline Job

1. New Item -> Pipeline
2. In Pipeline section choose:
   - `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: your SmartCourier repo
   - Script Path: `Jenkinsfile`
3. Save and click `Build Now`

### Pipeline Behavior

- Default run does:
  - backend tests (`mvn test`)
  - frontend build (`npm run build`)
- Optional Docker stages are disabled by default.
  - Enable build parameter `RUN_DOCKER_STAGES=true` only if Jenkins agent has Docker CLI + daemon access.


## Showcase Runbook
- See: docs/SHOWCASE_RUNBOOK.md


