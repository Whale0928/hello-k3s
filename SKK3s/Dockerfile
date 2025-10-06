# ============================================
# Stage 1: Build Stage
# ============================================
# 목적: Gradle을 사용해 Spring Boot 애플리케이션을 빌드
# 왜 멀티 스테이지를 사용하는가?
# - 빌드 도구(Gradle)와 실행 환경을 분리하여 최종 이미지 크기 최소화
# - 빌드 의존성(Gradle, 소스코드)이 최종 이미지에 포함되지 않음
FROM gradle:8.11-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 스크립트 복사
# 이 파일들을 먼저 복사하면 의존성 다운로드를 캐싱할 수 있음
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle .

# 의존성 다운로드 (레이어 캐싱 최적화)
# 소스 코드 변경 시에도 의존성은 캐시에서 재사용 가능
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
# --no-daemon: CI 환경에서는 Gradle 데몬이 불필요하므로 비활성화
# -x test: 빌드 시간 단축을 위해 테스트 스킵 (선택사항)
RUN ./gradlew bootJar --no-daemon -x test

# ============================================
# Stage 2: Runtime Stage
# ============================================
# 목적: 빌드된 JAR 파일만 포함한 경량 실행 이미지 생성
FROM eclipse-temurin:21-jre

# 애플리케이션 실행 사용자 생성 (보안 강화)
# root로 실행하지 않기 위함
RUN addgroup --system spring && adduser --system --group spring
USER spring:spring

# 작업 디렉토리 설정
WORKDIR /app

# Stage 1에서 빌드된 JAR 파일을 복사
# build/libs/*.jar 파일을 app.jar로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션이 사용할 포트 노출 (문서화 목적)
# 실제 포트 매핑은 Kubernetes Service에서 정의
EXPOSE 8080

# 헬스체크는 Kubernetes의 liveness/readiness probe에서 처리
# Docker HEALTHCHECK는 K8s 환경에서는 불필요

# JVM 옵션 설정
# -XX:+UseContainerSupport: 컨테이너 환경의 메모리 제한을 인식
# -XX:MaxRAMPercentage=75.0: 컨테이너 메모리의 75%까지 힙 사용
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 애플리케이션 실행
# exec 형식을 사용하여 PID 1로 실행 (graceful shutdown 지원)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]