# Etapa 1: compila el JAR. Esta imagen se descarta al terminar el build.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# El pom va antes que src/ para que un cambio de codigo no invalide
# la capa de descarga de dependencias.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Los tests corren en CI, no en cada build de imagen.
RUN mvn clean package -DskipTests -B


# Etapa 2: runtime. Solo JRE y el JAR: sin Maven, compilador ni codigo fuente.
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

# Comodin para no atar el Dockerfile a la version del pom. El jar sin
# dependencias que deja el repackage es *.jar.original y no matchea el patron.
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

# Forma exec: la JVM queda como PID 1 y recibe el SIGTERM de docker stop,
# lo que dispara el apagado ordenado de Spring.
ENTRYPOINT ["java", "-jar", "app.jar"]
