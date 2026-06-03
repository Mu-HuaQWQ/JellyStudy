FROM eclipse-temurin:11-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/jelly-study-1.0.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8086 20880
ENTRYPOINT ["java","-jar","/app.jar"]