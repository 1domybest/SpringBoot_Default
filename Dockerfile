# OpenJDK 23 기반의 Gradle 이미지 사용
FROM gradle:7.6.2-jdk17-alpine

# ARG: 빌드 과정에서 사용할 변수 선언
ARG JAR_FILE=build/libs/*.jar
ARG PROFILES
ARG ENV

# 워크 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY ${JAR_FILE} app.jar


