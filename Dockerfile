FROM openjdk:8-alpine
WORKDIR /app
COPY target/ROOT.jar .
ENTRYPOINT ["java", "-jar", "ROOT.jar"]