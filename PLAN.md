# SmartCourier Implementation Plan

## 1) Locked Decisions (as requested)

1. Frontend: **React** (not Angular).
2. Database: **MySQL 8** (not PostgreSQL).
3. Architecture: Spring Boot microservices + Spring Cloud Gateway + React web app.
4. Services: `auth-service`, `delivery-service`, `tracking-service`, `admin-service`, `api-gateway`.
5. Security: JWT + role-based authorization (`CUSTOMER`, `ADMIN`).
6. Total API target: **48 APIs** overall (defined in section 5).

## 2) High-Level Architecture

Flow:

1. React client calls `/gateway/**`.
2. Spring Cloud Gateway routes to microservices.
3. Each microservice owns its own MySQL schema.
4. JWT validated at gateway and service layers.
5. Service authorization enforced with `@PreAuthorize`.

Core service responsibilities:

1. `auth-service`
   - user registration/login
   - JWT token issuance and refresh
   - user profile and role mappings
2. `delivery-service`
   - create/update/cancel delivery
   - delivery lifecycle transitions
   - pricing/ETA and pickup scheduling
3. `tracking-service`
   - tracking events timeline
   - document upload metadata
   - proof-of-delivery retrieval
4. `admin-service`
   - dashboard metrics
   - delivery monitoring and exception resolution
   - report generation and user/hub management
5. `api-gateway`
   - single ingress and route forwarding
   - request filters, auth propagation, rate limiting (basic)

## 3) Frontend Plan (React)

Tech stack:

1. React 19 + TypeScript + Vite
2. React Router for route protection
3. Axios client with JWT interceptor
4. State management: Redux Toolkit (or Zustand if we decide lighter)

React app modules/pages:

1. Auth
   - `/auth/login`
   - `/auth/signup`
2. Customer
   - `/customer/dashboard`
   - `/customer/deliveries`
   - `/customer/track/:id`
3. Delivery Wizard
   - sender -> receiver -> package -> review
4. Admin
   - `/admin/dashboard`
   - `/admin/deliveries`
   - `/admin/reports`
   - `/admin/users`
   - `/admin/hubs`

## 4) Database Plan (MySQL 8)

MySQL approach:

1. Separate schemas per service for isolation.
2. InnoDB engine for transactional consistency.
3. Flyway migrations per service.

Schemas:

1. `auth_db`
2. `delivery_db`
3. `tracking_db`
4. `admin_db`

Initial tables:

1. Auth
   - `users`
   - `roles`
   - `user_roles`
   - `refresh_tokens`
2. Delivery
   - `deliveries`
   - `packages`
   - `addresses`
   - `pickup_slots`
   - `pricing_quotes`
3. Tracking
   - `tracking_events`
   - `documents`
   - `delivery_proofs`
4. Admin
   - `hubs`
   - `service_locations`
   - `delivery_exceptions`
   - `report_cache`

Data conventions:

1. Primary keys: UUID (char(36)) or bigint auto-increment (choose one globally before coding).
2. Timestamps: UTC in `datetime(3)`.
3. Optimistic lock column: `version` where needed.
4. Key indexes:
   - `deliveries(tracking_number)` unique
   - `deliveries(customer_id, created_at)`
   - `tracking_events(delivery_id, event_time)`
   - `delivery_exceptions(status, created_at)`

## 5) 48 API Catalog (Fixed Target)

### Auth Service (8)

1. `POST /gateway/auth/signup`
2. `POST /gateway/auth/login`
3. `POST /gateway/auth/refresh`
4. `POST /gateway/auth/logout`
5. `GET /gateway/auth/me`
6. `PUT /gateway/auth/me`
7. `PUT /gateway/auth/password`
8. `GET /gateway/auth/roles`

### Delivery Service (16)

1. `POST /gateway/deliveries`
2. `GET /gateway/deliveries/my`
3. `GET /gateway/deliveries/{id}`
4. `PUT /gateway/deliveries/{id}`
5. `DELETE /gateway/deliveries/{id}`
6. `POST /gateway/deliveries/{id}/book`
7. `POST /gateway/deliveries/{id}/pickup/schedule`
8. `PUT /gateway/deliveries/{id}/pickup/reschedule`
9. `POST /gateway/deliveries/{id}/cancel`
10. `GET /gateway/deliveries/{id}/price`
11. `POST /gateway/deliveries/estimate`
12. `POST /gateway/deliveries/{id}/status/picked-up`
13. `POST /gateway/deliveries/{id}/status/in-transit`
14. `POST /gateway/deliveries/{id}/status/out-for-delivery`
15. `POST /gateway/deliveries/{id}/status/delivered`
16. `GET /gateway/deliveries/search`

### Tracking Service (12)

1. `GET /gateway/tracking/{trackingNumber}`
2. `GET /gateway/tracking/{trackingNumber}/events`
3. `POST /gateway/tracking/events`
4. `POST /gateway/tracking/documents/upload`
5. `GET /gateway/tracking/documents/{id}`
6. `DELETE /gateway/tracking/documents/{id}`
7. `GET /gateway/tracking/{id}/proof`
8. `POST /gateway/tracking/{id}/proof`
9. `GET /gateway/tracking/{id}/proof/download`
10. `GET /gateway/tracking/{id}/timeline`
11. `GET /gateway/tracking/{id}/latest`
12. `GET /gateway/tracking/health`

### Admin Service (12)

1. `GET /gateway/admin/dashboard`
2. `GET /gateway/admin/deliveries`
3. `GET /gateway/admin/deliveries/{id}`
4. `PUT /gateway/admin/deliveries/{id}/resolve`
5. `POST /gateway/admin/deliveries/{id}/exception`
6. `GET /gateway/admin/reports`
7. `GET /gateway/admin/reports/daily`
8. `GET /gateway/admin/reports/sla`
9. `GET /gateway/admin/users`
10. `PUT /gateway/admin/users/{id}/role`
11. `GET /gateway/admin/hubs`
12. `POST /gateway/admin/hubs`

Total = 8 + 16 + 12 + 12 = **48 APIs**.

## 6) Delivery Lifecycle

1. `Draft -> Booked`
2. `Booked -> Picked Up`
3. `Picked Up -> In Transit`
4. `In Transit -> Out for Delivery`
5. `Out for Delivery -> Delivered`
6. Exception transitions from operational states: `Delayed | Failed | Returned`

Rules:

1. Customer controls flow until booking confirmation.
2. Admin/system controls operational transitions after booking.
3. Every status change creates tracking event entry.

## 7) Implementation Roadmap

Phase 0: Bootstrap

1. Convert root into multi-module project (services + frontend React app).
2. Add base Spring dependencies, shared config strategy, and gateway routes.
3. Provision MySQL schemas and Flyway baselines.

Phase 1: Security + Auth + Gateway

1. Signup/login/refresh/logout + role handling.
2. JWT filters and route protection at gateway.
3. React auth pages and protected routes.

Phase 2: Delivery + Tracking MVP

1. Delivery wizard APIs and React wizard UI.
2. Customer dashboard and details pages.
3. Tracking events + document upload + POD retrieval.

Phase 3: Admin + Reports

1. Monitoring, exceptions, user/hub management.
2. Reports endpoints and dashboard integration.
3. Validate all 48 APIs are wired and tested.

Phase 4: Hardening

1. Unit/integration tests and API contract tests.
2. Performance tuning and index refinement.
3. Logging, metrics, and error standardization.

## 8) Testing & Quality

Backend:

1. JUnit 5 + Mockito unit tests.
2. Spring MVC controller tests.
3. Testcontainers MySQL integration tests.

Frontend:

1. React Testing Library + Vitest.
2. Route/auth guard and API client tests.
3. Cypress smoke tests for core flows.

Quality gates:

1. All 48 APIs documented in Swagger and Postman.
2. Service-layer coverage target >= 80%.
3. End-to-end happy paths: signup -> create delivery -> track -> admin resolve.

## 9) Approval Items

1. Confirm API list in section 5 as final scope for v1 (48 total).
2. Confirm ID strategy: UUID or bigint.
3. Confirm async messaging in v1 or REST-only first.
4. Confirm local-file vs object storage for documents in v1.
