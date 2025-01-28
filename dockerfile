FROM maven:3.9.9-amazoncorretto-23-debian AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B


COPY src ./src
RUN mvn clean package -DskipTests


FROM amazoncorretto:23-alpine-jdk
WORKDIR /app
COPY --from=builder /app/target/listings-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
