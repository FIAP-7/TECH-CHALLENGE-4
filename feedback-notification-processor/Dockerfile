FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/

USER quarkus
WORKDIR /code

RUN sed -i 's/\r$//' mvnw

RUN chmod +x mvnw && \
    mkdir -p src/main/resources && \
    touch src/main/resources/application.properties

RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

COPY src /code/src

RUN ./mvnw package -Pnative -Dquarkus.native.native-image-xmx=4g

FROM public.ecr.aws/lambda/provided:al2023

COPY --from=build /code/target/*-runner /var/runtime/bootstrap

CMD ["bootstrap"]