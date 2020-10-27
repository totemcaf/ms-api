FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1 as builder
MAINTAINER Carlos Fau <carlos.fau@gmail.com>

WORKDIR /app

COPY . /app

RUN sbt assembly

FROM openjdk:8u222

EXPOSE 8080

WORKDIR /app

COPY --from=builder /app/target/scala-2.12/minesweeper-assembly-0.1.0-SNAPSHOT.jar /app/minesweeper.jar

# Define default command.
ENTRYPOINT ["java", "-jar", "minesweeper.jar"]
