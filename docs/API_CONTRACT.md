# API Contract (48 Endpoints)

## Auth (8)
- POST /gateway/auth/signup
- POST /gateway/auth/login
- POST /gateway/auth/refresh
- POST /gateway/auth/logout
- GET /gateway/auth/me
- PUT /gateway/auth/me
- PUT /gateway/auth/password
- GET /gateway/auth/roles

`POST /gateway/auth/logout` expects payload:
`{ "refreshToken": "<token>" }`

## Delivery (16)
- POST /gateway/deliveries
- GET /gateway/deliveries/my
- GET /gateway/deliveries/{id}
- PUT /gateway/deliveries/{id}
- DELETE /gateway/deliveries/{id}
- POST /gateway/deliveries/{id}/book
- POST /gateway/deliveries/{id}/pickup/schedule
- PUT /gateway/deliveries/{id}/pickup/reschedule
- POST /gateway/deliveries/{id}/cancel
- GET /gateway/deliveries/{id}/price
- POST /gateway/deliveries/estimate
- POST /gateway/deliveries/{id}/status/picked-up
- POST /gateway/deliveries/{id}/status/in-transit
- POST /gateway/deliveries/{id}/status/out-for-delivery
- POST /gateway/deliveries/{id}/status/delivered
- GET /gateway/deliveries/search

## Tracking (12)
- GET /gateway/tracking/{trackingNumber}
- GET /gateway/tracking/{trackingNumber}/events
- POST /gateway/tracking/events
- POST /gateway/tracking/documents/upload
- GET /gateway/tracking/documents/{id}
- DELETE /gateway/tracking/documents/{id}
- GET /gateway/tracking/{id}/proof
- POST /gateway/tracking/{id}/proof
- GET /gateway/tracking/{id}/proof/download
- GET /gateway/tracking/{id}/timeline
- GET /gateway/tracking/{id}/latest
- GET /gateway/tracking/health

## Admin (12)
- GET /gateway/admin/dashboard
- GET /gateway/admin/deliveries
- GET /gateway/admin/deliveries/{id}
- PUT /gateway/admin/deliveries/{id}/resolve
- POST /gateway/admin/deliveries/{id}/exception
- GET /gateway/admin/reports
- GET /gateway/admin/reports/daily
- GET /gateway/admin/reports/sla
- GET /gateway/admin/users
- PUT /gateway/admin/users/{id}/role
- GET /gateway/admin/hubs
- POST /gateway/admin/hubs
