# 프로젝트 안내

클린 아키텍처 기반 Spring Boot 프로젝트에서
AI 에이전트 개발과 자동 검증을 실습한다.

## 기술 스택

- Java 21
- Spring Boot
- Gradle Groovy DSL
- Spring Web MVC
- Spring Data JPA
- H2 Database

## 패키지 구조

기본 패키지: org.sopt.springagentlab

- domain: 도메인 객체와 업무 규칙
- application/post/port/in: 유스케이스 인터페이스
- application/post/service: 유스케이스 구현
- application/post/port/out: 저장소 인터페이스
- infrastructure: JPA 엔티티와 저장소 구현
- presentation/controller: HTTP 요청 처리
- presentation/dto: 요청 및 응답 DTO

## 의존성 규칙

- domain은 다른 프로젝트 계층과 Spring·JPA에 의존하지 않는다.
- application은 domain을 사용할 수 있지만,
  presentation과 infrastructure에 의존하지 않는다.
- application의 저장소 인터페이스는 domain 타입을 사용한다.
- infrastructure는 application의 저장소 인터페이스를 구현한다.
- JPA 엔티티와 domain 객체는 분리한다.
- presentation은 application의 유스케이스를 호출한다.
- presentation에서 Repository를 직접 호출하지 않는다.
- JPA 엔티티를 API 응답으로 직접 반환하지 않는다.
- - presentation과 infrastructure는 서로 직접 의존하지 않는다.
- application의 입력·출력 타입에 HTTP·JPA 전용 타입을 사용하지 않는다.
  예: ResponseEntity, HttpServletRequest, JPA 엔티티

## 작업 방식

- 요청한 작업에 필요한 범위만 변경한다.
- 새로운 의존성을 추가할 때는 이유를 설명한다.
- 동작이 변경되면 관련 테스트를 작성하거나 수정한다.
- 테스트를 통과시키기 위해 검증 조건을 임의로 완화하지 않는다.
- 구조나 개발 명령이 바뀌면 관련 문서도 갱신한다.

## 검증 명령

Windows PowerShell 기준:

- 테스트: .\gradlew.bat test
- 전체 빌드: .\gradlew.bat build

## 작업 완료 보고

- 변경 내용과 이유를 설명한다.
- 실행한 검사와 결과를 적는다.
- 실행하지 못한 검사는 이유를 적는다.