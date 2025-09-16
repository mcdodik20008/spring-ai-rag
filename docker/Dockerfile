# docker/Dockerfile
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# параметризуем имя файла; по умолчанию берём единственный jar из build/libs
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# безопасный пользователь
RUN useradd -m -U -s /bin/false appuser && chown -R appuser:appuser /app
USER appuser

ENV TZ=Europe/Moscow \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+AlwaysActAsServerClassMachine -Dfile.encoding=UTF-8"

EXPOSE 8080

# при наличии actuator можно включить healthcheck (иначе удалите)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
#   CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]
