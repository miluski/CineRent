FROM eclipse-temurin:21.0.5_11-jdk-jammy AS build

WORKDIR /app

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends wget ca-certificates && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz && \
    tar -xzf apache-maven-3.9.9-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.9 /opt/maven && \
    rm apache-maven-3.9.9-bin.tar.gz && \
    apt-get purge -y wget && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

COPY --from=build --chown=nonroot:nonroot /app/target/backend-1.0.0.jar /app/backend-1.0.0.jar

EXPOSE 443

ENTRYPOINT ["java", \
            "-Xms2g", \
            "-Xmx4g", \
            "-XX:+UseG1GC", \
            "-XX:MaxGCPauseMillis=200", \
            "-XX:+UseStringDeduplication", \
            "-XX:+OptimizeStringConcat", \
            "-Djava.net.preferIPv4Stack=true", \
            "-Dfile.encoding=UTF-8", \
            "-jar", "/app/backend-1.0.0.jar"]
