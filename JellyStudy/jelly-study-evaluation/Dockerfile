FROM jellystudy-jelly-study-instance1:latest
VOLUME /tmp
ARG JAR_FILE=target/jelly-study-evaluation-1.0.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8089 20881
ENTRYPOINT ["java","-jar","/app.jar"]