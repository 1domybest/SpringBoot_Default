# OpenJDK 17이 포함된 Gradle 이미지 사용
FROM gradle:7.6.2-jdk17-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 소스 코드 복사
COPY --chown=gradle:gradle . .

# Gradle 빌드 실행 (JAR 생성)
RUN gradle build --no-daemon

# ---- 실행 이미지 ----
FROM openjdk:17-alpine

# 환경 변수
ARG PROFILES
ARG ENV

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 실행 이미지로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 복사된 파일 확인
RUN ls -al /app

# 컨테이너 실행 시 Spring Boot 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "/app/app.jar"]
