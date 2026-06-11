FROM eclipse-temurin:11-jre-alpine
RUN apk add --no-cache ca-certificates tzdata && update-ca-certificates
VOLUME /tmp
ARG JAR_FILE=target/jelly-study-1.0.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8086 20880
ENTRYPOINT ["java","-Dhttps.protocols=TLSv1.2,TLSv1.3","-Djdk.tls.client.protocols=TLSv1.2,TLSv1.3","-jar","/app.jar"]