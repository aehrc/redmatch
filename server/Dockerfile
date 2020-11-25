FROM azul/zulu-openjdk-alpine:11
LABEL copyright="Copyright © 2018-2020, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source Software Licence Agreement." \
  author="Alejandro Metke <Alejandro.Metke@csiro.au>"

RUN apk add --no-cache ttf-dejavu

RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    mkdir /usr/share/redmatch && \
    chown spring /usr/share/redmatch

VOLUME /usr/share/redmatch

USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]