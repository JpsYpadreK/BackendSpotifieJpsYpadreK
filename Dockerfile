# Dockerfile para Spring Boot con Gradle-Kotlin-DSL
# Basado en la documentación oficial de Spring Boot 3.5.3

# Perform the extraction in a separate builder container
FROM bellsoft/liberica-openjre-debian:24-cds AS builder
WORKDIR /builder

# This points to the built jar file in the build folder
# For Gradle projects, jars are in build/libs/*.jar (not target/ like Maven)
ARG JAR_FILE=build/libs/*.jar

# Copy the jar file to the working directory and rename it to application.jar
COPY ${JAR_FILE} application.jar

# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# This is the runtime container
FROM bellsoft/liberica-openjre-debian:24-cds
WORKDIR /application

# Copy the extracted jar contents from the builder container into the working directory in the runtime container
# Every copy step creates a new docker layer
# This allows docker to only pull the changes it really needs
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Expose port 8080 (default Spring Boot port)
EXPOSE 8080

# Start the application jar - this is not the uber jar used by the builder
# This jar only contains application code and references to the extracted jar files
# This layout is efficient to start up and CDS/AOT cache friendly
ENTRYPOINT ["java", "-jar", "application.jar"]