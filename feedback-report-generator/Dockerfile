## Estágio 1: Build Nativo com Java 21 e Mandrel
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

# Compila o nativo
RUN ./mvnw package -Pnative -Dquarkus.native.native-image-xmx=4g

## Estágio 2: Criação da Imagem Lambda (Runtime)
FROM public.ecr.aws/lambda/provided:al2023

# 1. Instala dependências de sistema para fontes e gráficos
RUN dnf install -y freetype fontconfig libX11 libXext libXrender && dnf clean all

# 2. Prepara diretórios
RUN mkdir -p /var/task/lib

# 3. COPIA AS LIBS NATIVAS (.so) 
# Elas costumam ficar na pasta de sources nativos
COPY --from=build /code/target/feedback-report-generator-*-native-image-source-jar/*.so /var/task/lib/

# 4. COPIA O BINÁRIO (Runner)
# Tentamos copiar de target/ ou da subpasta. O Quarkus gera o arquivo terminado em "-runner"
# Copiamos para a pasta temporária para renomear depois com segurança
COPY --from=build /code/target/*-runner /var/task/

# 5. Configura o Bootstrap da Lambda
# O binário deve se chamar obrigatoriamente 'bootstrap' para runtimes 'provided'
RUN cp /var/task/*-runner /var/runtime/bootstrap && chmod +x /var/runtime/bootstrap

# 6. Variáveis de Ambiente
ENV LD_LIBRARY_PATH=/var/task/lib
ENV JAVA_AWT_HEADLESS=true

# 7. Pré-gera o cache de fontes (Ajuda a evitar o "Init Timeout")
RUN fc-cache -f -v

CMD ["bootstrap"]