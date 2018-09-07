FROM openjdk:8

COPY target/scala-2.12/statistic-scaler.jar /statistic-scaler.jar
COPY kube-config /root/.kube/config

CMD "/usr/bin/java" "-jar" "/statistic-scaler.jar"
