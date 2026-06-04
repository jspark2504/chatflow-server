# Cursor 컨텍스트 구성

이 프로젝트는 AI 컨텍스트를 **3단계**로 나눕니다.

| 레이어 | 파일 | 적용 범위 |
|--------|------|-----------|
| 전역 성향 | [user-rules.md](./user-rules.md) | **한 번** Cursor User Rules에 복사 |
| 프로젝트 규칙 | [rules/*.mdc](./rules/) | 이 repo 열 때 자동 (`alwaysApply` / `globs`) |
| 에이전트 진입점 | [../AGENTS.md](../AGENTS.md) | 에이전트가 repo 작업 시 먼저 참고 |

## 프로젝트 규칙 목록

| 파일 | 용도 |
|------|------|
| `chatflow-project-context.mdc` | 목표, 스택, 7단계 로드맵 |
| `mentor-role.mdc` | 시니어 멘토·리뷰 기준 |
| `spring-webflux-conventions.mdc` | Java/YAML 작업 시 컨벤션 |

## 최초 1회 설정

1. `.cursor/user-rules.md` 복사 → Cursor **Settings → Rules → User Rules**
2. 이 저장소를 Cursor에서 열면 `.cursor/rules/`가 자동 적용됨
3. 단계가 바뀔 때마다 `AGENTS.md`의 **현재 단계**만 수정

## 단계 변경 시

`AGENTS.md` 상단 `현재 단계: N`을 업데이트하세요. 규칙 본문의 로드맵 표는 참고용입니다.
