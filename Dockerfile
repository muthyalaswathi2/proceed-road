FROM openjdk:18
WORKDIR /workspace
COPY target/*.jar road-to-cloud-native.jar
ENTRYPOINT ["java", "-jar", "road-to-cloud-native.jar"]
