FROM openjdk:21-jdk-slim

EXPOSE 8080
ADD configuration.yml /
ADD build/libs/maps-server.jar /
CMD java -jar maps-server.jar server /configuration.yml
