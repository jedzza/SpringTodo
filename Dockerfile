FROM openjdk:20-jdk
MAINTAINER lazy@thelazy.company
EXPOSE 8080
ARG JAR_FILE=target/spring-boot-lazy-todo-0.1.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
#COPY target/docker-message-server-1.0.0.jar message-server-1.0.0.jar
ENTRYPOINT ["java","-jar","/app.jar"]