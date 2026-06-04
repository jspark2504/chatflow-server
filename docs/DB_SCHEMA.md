# DB 스키마 (기획문서 · DB 테이블정의서)

출처: `채팅 시스템 기획.xlsx` → 시트 **DB 테이블정의서**  
Flyway: `src/main/resources/db/migration/V2__planning_schema.sql`

## users

| 컬럼 | 타입 | 비고 |
|------|------|------|
| id | bigint PK | 사용자 ID |
| email | varchar(255) UNIQUE | 이메일 |
| password | varchar(255) | BCrypt |
| nickname | varchar(100) | 닉네임 |
| profile_image | varchar(500) | nullable |
| online_status | boolean | 기본 false |
| created_at / updated_at | timestamp(3) | |

## chat_room / chat_room_member

채팅방·멤버 (2단계 API에서 사용)

## chat_message

room_id, sender_id, message_type, message_content, is_read, created_at

## notification

user_id, notification_type, content, is_read, created_at

## user_connection

WebSocket 세션 이력 (3단계 이후)

## ERD 참고

기획문서 내 dbdiagram.io 링크 동일.
