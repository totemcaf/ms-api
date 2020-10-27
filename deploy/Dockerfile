FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1 as builder
MAINTAINER Carlos Fau <carlos.fau@gmail.com>

WORKDIR /app

EXPOSE 8080

COPY minesweeper.jar /app/minesweeper.jar

# Define default command.
ENTRYPOINT ["java", "-jar", "minesweeper.jar"]
