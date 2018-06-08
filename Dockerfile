FROM openjdk:8-alpine
WORKDIR /app
COPY target/ROOT.jar .
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "ROOT.jar"]
