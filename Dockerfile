FROM alpine:latest
ENV LANG=zh_CN.UTF-8
RUN apk add --no-cache tzdata openjdk21-jdk && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
COPY app.jar /app.jar
ENTRYPOINT ["/bin/sh","-c","java -Dfile.encoding=utf8 -jar app.jar"]