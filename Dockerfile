# 1. Java 17 환경
FROM amazoncorretto:17-al2023-headless

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너 내부로 복사
COPY build/libs/*-SNAPSHOT.jar app.jar

# 4. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]