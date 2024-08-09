FROM amazoncorretto:17-alpine
WORKDIR /app

#TimeZone 설정
RUN apk update && apk upgrade --no-cache && \
    apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} log-monitoring.jar

ENV JAVA_OPTS="-Xms512m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar log-monitoring.jar"]