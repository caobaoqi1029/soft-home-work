FROM xldevops/jdk17-lts
LABEL authors="cbq"

COPY target/monitor-client.jar /work/app.jar
WORKDIR /work
ENV CBQ_TOKEN=""
ENV CBQ_ADDRESS="http://localhost:8080"
CMD ["java", "-jar", "app.jar","--CBQ_TOKEN=$CBQ_TOKEN","--CBQ_ADDRESS=$CBQ_ADDRESS"]