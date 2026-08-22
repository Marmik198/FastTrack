# FastTrack

Karma Yuva Group's system for tracking children's tap/fasting point accrual, running
gifting campaigns, and managing redemption approvals.

- **Backend:** `fasttrack-backend` — Spring Boot 3.3, Java 21, Maven, Postgres (Neon), JWT auth, AES-256 field encryption for PII
- **Frontend:** `fasttrack-frontend` — React (Vite), Material UI, English/Gujarati/Hindi i18n

---

## Project structure

```
fasttrack/
├── .gitignore
├── render.yaml                          # Render Blueprint - one-click backend deploy
├── README.md
│
├── fasttrack-backend/
│   ├── .gitignore
│   ├── Dockerfile                       # multi-stage build, used by Render
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/karmayuva/pointsystem/
│       │   ├── PointSystemApplication.java
│       │   ├── config/
│       │   │   ├── JpaAuditingConfig.java     # enables creation/last-modified auditing
│       │   │   ├── OpenApiConfig.java         # Swagger UI + JWT bearer scheme
│       │   │   └── SecurityConfig.java        # route auth rules, CORS
│       │   ├── controller/
│       │   │   ├── AdminAccrualController.java
│       │   │   ├── AdminAuthController.java
│       │   │   ├── AdminCampaignController.java
│       │   │   ├── AdminCampaignResponseController.java
│       │   │   ├── AdminChildController.java
│       │   │   ├── AdminPointController.java
│       │   │   ├── AdminRedemptionController.java
│       │   │   ├── PublicCampaignController.java
│       │   │   ├── PublicCampaignResponseController.java
│       │   │   └── PublicLookupController.java
│       │   ├── dto/                           # request/response records (17 files)
│       │   ├── entity/
│       │   │   ├── Accrual.java
│       │   │   ├── Auditable.java             # @MappedSuperclass - creation/modified dates
│       │   │   ├── Campaign.java
│       │   │   ├── CampaignResponse.java
│       │   │   ├── Child.java
│       │   │   ├── Point.java
│       │   │   └── Redemption.java
│       │   ├── exception/
│       │   │   ├── BusinessException.java
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   └── ResourceNotFoundException.java
│       │   ├── repository/                    # Spring Data JPA repos (6 files)
│       │   ├── security/
│       │   │   ├── EncryptedStringConverter.java  # AES-256-GCM field encryption
│       │   │   ├── JwtAuthFilter.java
│       │   │   └── JwtUtil.java
│       │   └── service/
│       │       ├── AccrualImportService.java   # CSV/TXT/Excel bulk import
│       │       ├── AccrualService.java
│       │       ├── AuthService.java
│       │       ├── CampaignResponseService.java
│       │       ├── CampaignService.java
│       │       ├── ChildService.java
│       │       ├── NameMatchingService.java    # fuzzy name matching
│       │       ├── PointService.java           # FIFO point ledger/burning
│       │       └── RedemptionService.java
│       └── resources/
│           ├── application.properties          # env-var driven, for production
│           └── application-dev.properties       # H2 in-memory, zero-setup local dev
│
└── fasttrack-frontend/
    ├── .env.example
    ├── .gitignore
    ├── index.html
    ├── package.json
    ├── vercel.json                       # SPA routing for Vercel
    ├── vite.config.js
    └── src/
        ├── main.jsx
        ├── App.jsx                       # all routes (public + admin)
        ├── api/
        │   └── apiClient.js              # axios instance, JWT interceptor
        ├── components/
        │   ├── ChildAutocomplete.jsx     # admin searchable child picker
        │   ├── ContactFooter.jsx         # static "contact us" block
        │   └── ProtectedRoute.jsx
        ├── i18n/
        │   ├── I18nContext.jsx           # provider, useTranslation hook, language switcher
        │   └── translations.js           # en / gu / hi dictionaries
        ├── theme/
        │   └── theme.js                  # MUI custom theme
        └── pages/
            ├── public/
            │   └── NameLookupFormPage.jsx # name lookup → form → thank-you flow
            └── admin/
                ├── AdminLoginPage.jsx
                ├── AdminLayout.jsx        # sidebar nav shell
                ├── ChildrenPage.jsx
                ├── AccrualsPage.jsx
                ├── PointsSummaryPage.jsx
                ├── CampaignsPage.jsx
                ├── CampaignResponsesPage.jsx
                └── RedemptionsPage.jsx
```

---

## Running locally

### Backend

Requires **Java 21** and **Maven**.

```bash
cd fasttrack-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

This uses an in-memory H2 database - zero setup, resets every restart. Runs on `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:karmayuva`, user `sa`, no password)
- Dev admin login: `admin` / `admin123`

### Frontend

Requires **Node.js** (18+).

```bash
cd fasttrack-frontend
cp .env.example .env
npm install
npm run dev
```

Runs on `http://localhost:5173`, pointed at the backend above.

---

## Deploying for free

**Stack:** Neon (Postgres) + Render (backend, Docker) + Vercel (frontend)

### 1. Push to GitHub

Push the whole `fasttrack/` folder (both subfolders) to a new repo.

### 2. Database — Neon

1. Sign up at [neon.com](https://neon.com) → New Project
2. Copy the connection string, e.g. `postgresql://user:password@ep-xxxx.aws.neon.tech/neondb?sslmode=require`
3. Split it into a JDBC URL (`jdbc:postgresql://ep-xxxx.aws.neon.tech/neondb?sslmode=require`), username, and password

### 3. Backend — Render

Easiest path: use the included `render.yaml` Blueprint — in Render, **New +** → **Blueprint**, point it at your repo, and it will pick up `fasttrack-backend` automatically with the Dockerfile.

Otherwise, manually: **New +** → **Web Service**, Root Directory `fasttrack-backend`, Runtime **Docker**, plan **Free**.

Either way, set these environment variables in the Render dashboard:

| Key | Value |
|---|---|
| `SPRING_DATASOURCE_URL` | your Neon JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | your Neon username |
| `SPRING_DATASOURCE_PASSWORD` | your Neon password |
| `APP_JWT_SECRET` | random 32+ char string — `openssl rand -base64 32` |
| `APP_ADMIN_USERNAME` | your real admin username |
| `APP_ADMIN_PASSWORD_HASH` | BCrypt hash of your real admin password (see below) |
| `APP_ENCRYPTION_KEY` | `openssl rand -base64 32` — **back this up separately, it can't be recovered if lost** |
| `APP_CORS_ALLOWED_ORIGINS` | your Vercel URL, added after step 4 |
| `APP_FRONTEND_BASE_URL` | your Vercel URL, added after step 4 |

Generate the BCrypt hash without ever writing the plain password anywhere:
```python
import bcrypt
print(bcrypt.hashpw(b"your-real-password", bcrypt.gensalt()).decode())
```

⚠️ Render's free tier spins down after 15 minutes idle and takes ~30-50s to wake back up on the next request.

### 4. Frontend — Vercel

**Add New** → **Project**, Root Directory `fasttrack-frontend`, framework auto-detected as Vite.

| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | your Render backend URL from step 3 |

### 5. Wire it together

Go back to Render, fill in `APP_CORS_ALLOWED_ORIGINS` and `APP_FRONTEND_BASE_URL` with your actual Vercel URL, save (triggers redeploy). Then visit your Vercel URL, log into `/admin/login`, create a campaign, open it, and test the public form end-to-end.

---

## Notes

- **PII encryption:** `Child.mobile/alternateMobile/email/address` and `CampaignResponse.email` are encrypted at rest with AES-256-GCM. Losing `APP_ENCRYPTION_KEY` permanently destroys access to that data — store it in a password manager, not in the repo.
- **Fuzzy name matching:** tolerates typos, "bhai"/"ben" suffixes, and a parent's name inserted as a middle name. See `NameMatchingService.java`.
- **Accrual import:** accepts `.csv`, `.txt`, or `.xlsx` with `month`, `frequency`, and either `childId` or `childName` columns.
- **Approval workflow:** points are only burned when an admin approves a redeem-type `CampaignResponse` (not at submission time) — see `CampaignResponseService.approve()`.
