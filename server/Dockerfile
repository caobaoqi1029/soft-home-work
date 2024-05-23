FROM xldevops/jdk17-lts
LABEL authors="cbq"

COPY target/monitor-server.jar /work/app.jar
WORKDIR /work

CMD ["bash", "-c", "echo 'host will sleep 30s to wait path work' && sleep 30 && echo 'start java' && java -jar app.jar"]