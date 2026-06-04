# 기술 스택

> `chatflow-server` 저장소·기획 엑셀(개요 시트) 공통 기준.  
> WebFlux 실시간 채팅에 맞게 **Maven**, **Spring Data R2DBC** 사용 (Gradle·JPA 아님).

출처: `채팅 시스템 기획.xlsx` (개요 시트)

---

## 백엔드 (chatflow-server)

| 구분 | 기술 | 버전 / 비고 |
|------|------|-------------|
| Language | Java | **17** (LTS) |
| Framework | Spring Boot | **3.2.5** |
| API | Spring WebFlux | reactive stack |
| Security | Spring Security + JWT | jjwt 0.12, 커스텀 WebFilter |
| Database | MySQL | **8.0** (Docker Compose) |
| Data access | Spring Data R2DBC | Flyway(JDBC) + R2DBC |
| Migration | Flyway | `db/migration` |
| Build | Maven | `pom.xml` |
| Container (local) | Docker Compose | MySQL |
| IDE | STS / Eclipse | m2e |

---

## 로드맵에 따른 추가

| 구분 | 기술 | 단계 | 상태 |
|------|------|------|------|
| Realtime | WebSocket (JSON) | 3 | **구현** (`/ws/chat`) |
| Pub/Sub | Redis | 3 | **구현** (`chat:room:{id}`) |
| Messaging | Kafka | 5 | 예정 |
| CI/CD | GitHub Actions | 7 | 예정 |
| Cloud | AWS | 8 | 예정 |

---

## 엑셀 「개요」 시트 (기술·버전)

| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| Security | Spring Security | 6.x |
| API | Spring WebFlux | — |
| Realtime | WebSocket | STOMP (3단계) |
| Messaging | Kafka | 4.x (7단계) |
| Cache | Redis | 8.x (3~5단계) |
| Database | MySQL | 8.0~8.4 |
| Data access | Spring Data R2DBC | Flyway |
| Build | Maven | 3.9+ |
| Container | Docker | Latest |
| Orchestration | Docker Compose | Latest |
| CI/CD | GitHub Actions | 5단계 (예정) |
| Cloud | AWS | 6단계 (예정) |
| Frontend | Next.js | 15 (별도 프로젝트) |

---

## 기획(구버전) → 문서 정정

| 항목 | 기존(엑셀) | **문서·코드** |
|------|------------|---------------|
| Build | Gradle | **Maven** |
| ORM | Spring Data JPA | **Spring Data R2DBC** |
| Java | 21 | **17** |
| Boot | 3.5.x | **3.2.5** |

---

## 면접 한 줄

WebFlux 채팅은 블로킹 ORM(JPA) 대신 **R2DBC**, 빌드는 **Maven**으로 맞춰 일관된 reactive 스택을 유지한다.
