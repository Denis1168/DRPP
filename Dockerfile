FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /app
COPY . .

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN ./mvnw clean package -DskipTests -Dvaadin.productionMode=true

FROM eclipse-temurin:25-jre-noble

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libglpk40 graphviz fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

ENV GLPK_LIBRARY=libglpk.so.40

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--vaadin.launch-browser=false"]