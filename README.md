# College Achievement Portal — Advanced Java demo

## Included
- Java 24 + Spring Boot 3.5
- Spring JDBC + MySQL
- HTML/CSS/JavaScript UI served by Spring Boot
- Add achievement, search/filter, approve/reject, demo scoring, leaderboard capped at Top 100, overview stats
- Sample demo students and achievements

## Run it
1. Start MySQL Server.
2. Open MySQL Workbench and run `CREATE DATABASE college_achievement_portal;`
3. Open `src/main/resources/application.properties` and replace `YOUR_MYSQL_PASSWORD` with your local MySQL password. Change username if needed.
4. Open this project folder in VS Code.
5. In the terminal, from the folder containing `pom.xml`, run `mvn spring-boot:run`.
6. The first run requires internet to download Maven dependencies.
7. Open http://localhost:8080

## API endpoints
- GET `/api/stats`
- GET `/api/students`
- GET `/api/achievements?status=ALL&search=`
- POST `/api/achievements`
- PATCH `/api/achievements/{id}/review`
- GET `/api/leaderboard?limit=100`

## Important limitations
This is a working demo starter, not production-ready. It has no real login/authentication or role-based authorization. The UI is a demo/admin view. Proof is an optional URL, not a file upload. Sample records are fictional. Scoring rules in `PortalController.calculatePoints()` are illustrative and must be approved by faculty before real rankings are used.
