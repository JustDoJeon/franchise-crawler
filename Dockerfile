# 1단계: Gradle로 빌드 (Gradle 이미지 사용 → wrapper 불필요)
FROM gradle:8.9-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon

# 2단계: 실행 이미지 (Chrome 포함)
FROM eclipse-temurin:17-jre-jammy

# Chrome 설치
RUN apt-get update && apt-get install -y \
    wget gnupg ca-certificates curl unzip \
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub \
       | gpg --dearmor > /usr/share/keyrings/google-chrome.gpg \
    && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] \
       http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update && apt-get install -y google-chrome-stable \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
