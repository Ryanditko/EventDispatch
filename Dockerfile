FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="EventDispatch Team"
LABEL description="Event-driven notification orchestration service built with Clojure, Kafka, and Datomic"

WORKDIR /app

COPY target/event-dispatch-*-standalone.jar app.jar
COPY config/ ./config/

ENV JVM_OPTS="-Xmx512m -Xms256m"

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
