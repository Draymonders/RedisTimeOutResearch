FROM adoptopenjdk/openjdk8:alpine-slim

VOLUME /tmp

ENV TimeZone=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TimeZone /etc/localtime && echo $TimeZone > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/usr/share/redis-time-out-research/sturnus.jar"]

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/redis-time-out-research/sturnus.jar