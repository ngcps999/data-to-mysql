FROM openjdk:8-alpine
WORKDIR /app
COPY target/ROOT.jar .
ENTRYPOINT ["java", "-Xmx10000M", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "ROOT.jar"]
