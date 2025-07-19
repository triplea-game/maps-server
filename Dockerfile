FROM openjdk:21-jdk-slim

EXPOSE 8080
ADD configuration.yml /
ADD build/libs/support-server.jar /
CMD java -jar support-server.jar server /configuration.yml
