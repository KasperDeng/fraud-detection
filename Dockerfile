FROM khipu/openjdk17-alpine

ARG CONFIG_FILE_PATH=/etc/fraud-detection/config
ARG CONFIG_CM_FILE_PATH=/etc/fraud-detection/config

COPY fraud-detection-app/target/fraud-detection-app-1.0.0-SNAPSHOT.jar /opt/fraud-detection/fraud-detection-app.jar
COPY fraud-detection-start.sh /opt/fraud-detection/fraud-detection-start.sh

RUN set -x && \
    mkdir -p ${CONFIG_FILE_PATH} && \
    mkdir -p ${CONFIG_CM_FILE_PATH} && \
    echo '299748:x:299748:' >> /etc/group && \
    echo '299748:x:299748:299748:fraud-detection:/opt/fraud-detection:/bin/bash' >> /etc/passwd && \
    echo '299748:!:::::::' >> /etc/shadow && \
    chown -R 299748:0 /etc/fraud-detection /opt/fraud-detection && \
    chmod -R g=u /etc/fraud-detection /opt/fraud-detection && \
    chmod 755 /opt/fraud-detection/fraud-detection-start.sh

WORKDIR /opt/fraud-detection/
USER 299748

ENTRYPOINT [ "/opt/fraud-detection/fraud-detection-start.sh" ]
