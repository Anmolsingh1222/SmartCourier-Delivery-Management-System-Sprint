# Architecture Overview (v3)

## Flow

1. React UI -> API Gateway (`/gateway/*`)
2. API Gateway -> Service Discovery (Eureka)
3. Eureka resolves service instances
4. Gateway forwards to Auth/Delivery/Tracking/Admin services
5. Services persist to dedicated MySQL schemas

## Discovery

- `eureka-server` is the registry
- All services are Eureka clients
- Gateway routes use `lb://service-name`

## Security

- JWT issued by Auth service
- Gateway validates JWT and applies admin route restrictions
- Services enforce ownership/role checks
- Refresh token lifecycle: persist, rotate, revoke

## Databases (per-service isolation)

- `auth_db`
- `delivery_db`
- `tracking_db`
- `admin_db`

## API Testing & Docs

- Swagger UI enabled in each service + gateway
- Postman collection + environment included in `docs/postman`

## Infra

- Dockerfiles for each service + frontend
- `infra/docker-compose.full.yml` runs complete stack
- CI pipeline in `.github/workflows/ci.yml`
