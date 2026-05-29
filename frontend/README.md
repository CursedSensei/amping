# Frontend

This directory contains two frontend clients:

- `hc_professional/` — React + TypeScript + Vite dashboard
- `patient/` — Android app (Kotlin, Gradle)

## Healthcare Professional Dashboard (`hc_professional`)

From `/tmp/workspace/CursedSensei/amping/frontend/hc_professional`:

1. Install dependencies:
   - `npm ci`
2. Run development server:
   - `npm run dev`
3. Build:
   - `npm run build`
4. Lint:
   - `npm run lint`

API base behavior:
- Uses `VITE_API_BASE_URL` when provided.
- Falls back to `http://localhost:8000/api/v1/web` for local development.

## Patient App (`patient`)

From `/tmp/workspace/CursedSensei/amping/frontend/patient`:

1. Build / test with Gradle wrapper:
   - `./gradlew test`
2. Open in Android Studio for emulator/device runs.

Current local mobile API base is configured in:
- `app/src/main/java/com/pinghtdog/amping/data/repository/GabbyRepository.kt`
