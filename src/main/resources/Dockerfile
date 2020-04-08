# @dockerfile.Template@
FROM openjdk:14-slim
MAINTAINER @project.maintainer@

ENV MALLOC_ARENA_MAX 2

RUN apt-get update && \
    apt-get install libjemalloc2 && \
    echo "/usr/lib/x86_64-linux-gnu/libjemalloc.so.2" > /etc/ld.so.preload && \
    apt-get clean &&
    rm -rf /var/lib/apt/lists/*

COPY @artifactId@-@version@.jar /opt/@artifactId@/@artifactId@.jar

CMD [ "java", "-XX:+PrintFlagsFinal", \
              "-XX:-TieredCompilation", \
              "-XX:OnOutOfMemoryError=kill %p", \
              "-XX:+HeapDumpOnOutOfMemoryError", \
              "-jar", "/opt/@artifactId@/@artifactId@.jar" ]

EXPOSE @exposed.ports@

LABEL com.rbkmoney.@artifactId@.parent=service-java \
    com.rbkmoney.@artifactId@.parent_tag=openjdk-14-slim \
    com.rbkmoney.@artifactId@.commit_id=@git.commit.id@ \
    com.rbkmoney.@artifactId@.branch=@git.branch@

WORKDIR /opt/@artifactId@
