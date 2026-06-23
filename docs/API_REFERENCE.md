# API Reference

Base URL: `http://localhost:8082`

## Authentication

### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "12345678@Abc"
}

Response 200:
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### Refresh Token
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Get Current User
```
GET /api/auth/me
Authorization: Bearer <accessToken>
```

## Tenders

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tenders` | List (paginated, filterable) |
| POST | `/api/tenders` | Create |
| GET | `/api/tenders/{id}` | Get by ID |
| PUT | `/api/tenders/{id}` | Update |
| DELETE | `/api/tenders/{id}` | Soft delete |
| PATCH | `/api/tenders/{id}/status` | Update status |
| POST | `/api/tenders/{id}/clone` | Clone tender |
| GET | `/api/tenders/{id}/items` | List items |

## Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List (paginated, searchable) |
| POST | `/api/products` | Create |
| GET | `/api/products/{id}` | Get by ID |
| PUT | `/api/products/{id}` | Update |
| DELETE | `/api/products/{id}` | Soft delete |

## HSMT Processing

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/hsmt/upload` | Upload HSMT file (multipart) |
| GET | `/api/hsmt/{tenderId}/requirements` | List requirements |
| GET | `/api/hsmt/{tenderId}/documents` | List documents |
| PUT | `/api/hsmt/requirements/{id}` | Update requirement |
| POST | `/api/hsmt/requirements/{id}/approve` | Approve requirement |

## Matching

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/match` | Match product to tender |
| GET | `/api/match/{tenderId}/best?limit=5` | Find best matches |

## Export

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/export/word/{tenderId}` | Export Word |
| GET | `/api/export/pdf/{tenderId}` | Export PDF |
| GET | `/api/export/zip/{tenderId}` | Export ZIP |
| GET | `/api/export/excel/{tenderId}` | Export Excel |
| GET | `/api/export/history` | Export history |

## Users (ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | List users |
| POST | `/api/users` | Create user |
| GET | `/api/users/{id}` | Get user |
| PUT | `/api/users/{id}` | Update user |
| PATCH | `/api/users/{id}/lock` | Lock/unlock |
| PATCH | `/api/users/{id}/reset-password` | Reset password |

## Notifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications?userId=` | List notifications |
| GET | `/api/notifications/unread-count?userId=` | Unread count |
| PATCH | `/api/notifications/{id}/read` | Mark as read |
| PATCH | `/api/notifications/read-all?userId=` | Mark all read |

## Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats` | Dashboard statistics |

## Chatbot

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chatbot/ask` | Ask a question |
| GET | `/api/chatbot/faqs` | List FAQs |
| POST | `/api/chatbot/faqs` | Create FAQ (ADMIN) |
| PUT | `/api/chatbot/faqs/{id}` | Update FAQ (ADMIN) |
| DELETE | `/api/chatbot/faqs/{id}` | Delete FAQ (ADMIN) |

## Backup (ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/backup` | Create backup |
| POST | `/api/backup/restore?file=` | Restore backup |

## Health

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Custom health check |
| GET | `/` | Service info |
| GET | `/actuator/health` | Spring Actuator health |

## Status Codes

- `200` — Success
- `201` — Created
- `204` — No Content (delete success)
- `400` — Bad Request / Validation Error
- `401` — Unauthorized
- `403` — Forbidden
- `404` — Not Found
- `413` — File Too Large
- `429` — Too Many Requests (rate limit)
- `500` — Internal Server Error
