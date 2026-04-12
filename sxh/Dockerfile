FROM openjdk:21-jdk-slim
LABEL author="baoyuxiang"

WORKDIR /app

COPY sxh-web-0.0.1-SNAPSHOT.jar app.jar

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8082

ENTRYPOINT ["java", "-server", "-Xms512m", "-Xmx512m", "-Djava.security.egd=file:/prod/./urandom", "-jar", "app.jar", "--server.port=8082"]
