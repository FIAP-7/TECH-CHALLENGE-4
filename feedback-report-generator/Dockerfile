FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build

COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/

USER quarkus
WORKDIR /code

RUN sed -i 's/\r$//' mvnw
RUN chmod +x mvnw
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

COPY src /code/src

RUN ./mvnw package -Pnative -Dquarkus.native.native-image-xmx=4g

FROM public.ecr.aws/lambda/provided:al2023

RUN dnf install -y freetype fontconfig libX11 libXext libXrender && dnf clean all

RUN mkdir -p /var/task/lib

COPY --from=build /code/target/feedback-report-generator-*-native-image-source-jar/*.so /var/task/lib/

COPY --from=build /code/target/*-runner /var/task/

RUN cp /var/task/*-runner /var/runtime/bootstrap && chmod +x /var/runtime/bootstrap

ENV LD_LIBRARY_PATH=/var/task/lib
ENV JAVA_AWT_HEADLESS=true

RUN fc-cache -f -v

CMD ["bootstrap"]