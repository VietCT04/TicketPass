FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package \
    && cp target/api-*.jar /workspace/application.jar

FROM eclipse-temurin:21-jre-alpine AS runtime

ARG VCS_REF=unknown
ARG APP_VERSION=unknown

LABEL org.opencontainers.image.title="TicketPass API" \
      org.opencontainers.image.source="https://github.com/VietCT04/TicketPass" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.version="${APP_VERSION}"

RUN addgroup -S -g 10001 ticketpass \
    && adduser -S -D -H -u 10001 -G ticketpass ticketpass \
    && mkdir -p /app /tmp/ticketpass \
    && chown 10001:10001 /tmp/ticketpass

WORKDIR /app

COPY --from=build /workspace/application.jar /app/application.jar
RUN chmod 0555 /app && chmod 0444 /app/application.jar

ENV SPRING_PROFILES_ACTIVE=container

EXPOSE 8080
STOPSIGNAL SIGTERM

USER 10001:10001

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=6 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health >/dev/null || exit 1

ENTRYPOINT ["java", "-Djava.io.tmpdir=/tmp/ticketpass", "-jar", "/app/application.jar"]
