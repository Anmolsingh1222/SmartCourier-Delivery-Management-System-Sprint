# Showcase Runbook

## Goal
Use this flow to demo SmartCourier quickly in front of reviewers.

## 1. Start full stack

```powershell
docker compose -f infra/docker-compose.full.yml up --build
```

## 2. Open live systems

- Frontend: http://localhost:5173
- Eureka: http://localhost:8761
- Gateway Swagger: http://localhost:8080/swagger-ui/index.html

## 3. Demo script (5-7 minutes)

1. Open landing page `/` and explain architecture cards.
2. Login as admin: `admin@smartcourier.local / Admin@12345`.
3. Create a delivery from UI (Create Delivery page).
4. Show customer dashboard list update.
5. Track shipment on timeline page.
6. Hit admin dashboard and mention protected admin routes.
7. Open Eureka dashboard and show registered services.
8. Open Swagger and run one secured endpoint with Bearer token.

## 4. Optional API demo with Postman

Import:
- `docs/postman/SmartCourier_API.postman_collection.json`
- `docs/postman/SmartCourier_Local.postman_environment.json`

Run:
1. `Auth -> Login`
2. `Delivery -> Create Delivery`
3. `Tracking -> Track by Number`
4. `Admin -> Dashboard`

## 5. Quick smoke command

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
```
