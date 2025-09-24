FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/comparatio-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env
COPY src/main/resources/templates/ /opt/compa/templates/
EXPOSE 8080
CMD ["/bin/sh","-lc","export $(grep -v '^#' .env | xargs) && java ${JAVA_OPTS:-} -jar app.jar"]


