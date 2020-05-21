FROM azul/zulu-openjdk-alpine:11
MAINTAINER Australian e-Health Research Centre, CSIRO <ontoserver-support@csiro.au>

RUN apk add --no-cache ttf-dejavu
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

