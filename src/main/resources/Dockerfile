# @dockerfile.Template@
FROM adoptopenjdk/openjdk14
MAINTAINER @project.maintainer@

ENV MALLOC_ARENA_MAX 2

RUN set -ex && \
    apt-get update && \
    apt-get install libjemalloc1 && \
    echo "/usr/lib/x86_64-linux-gnu/libjemalloc.so.1" > /etc/ld.so.preload && \
    rm -rf /var/lib/apt/lists/*

COPY fault-detector-1.3.56-SNAPSHOT.jar /opt/fault-detector/fault-detector.jar

CMD [ "java", "-XX:+PrintFlagsFinal", \
              "-XX:OnOutOfMemoryError=kill %p", \
              "-XX:+HeapDumpOnOutOfMemoryError", \
              "-jar", "/opt/fault-detector/fault-detector.jar" ]

EXPOSE 8022

LABEL com.rbkmoney.fault-detector.parent=service-java \
    com.rbkmoney.fault-detector.parent_tag=bc95d0d6dc13c693acd2b274531a7d604b877bf3 \
    com.rbkmoney.fault-detector.commit_id=030116dd64e1e5ac48c0c6be211aeca6cf7f0bc6 \
    com.rbkmoney.fault-detector.branch=epic/java13

WORKDIR /opt/fault-detector