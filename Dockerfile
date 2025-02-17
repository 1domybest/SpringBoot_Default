# OpenJDK 17 기반의 Gradle 이미지 사용
FROM gradle:7.6.2-jdk17-alpine

# ARG: 빌드 과정에서 사용할 변수 선언
ARG JAR_FILE=build/libs/*.jar
ARG PROFILES
ARG ENV

# ARG 값을 ENV로 변환 (빌드 시 설정, 런타임에서도 사용 가능)
ENV SPRING_PROFILES_ACTIVE=$PROFILES
ENV SERVER_ENV=$ENV

# 워크 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY ${JAR_FILE} app.jar

# ENTRYPOINT 명령어로 Java 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dserver.env=${SERVER_ENV}", "-jar", "/app/app.jar"]