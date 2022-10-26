FROM registry.access.redhat.com/ubi8/openjdk-11 as base
USER root

ARG USERNAME
ARG PASSWORD
ARG BRANCH

RUN microdnf install -y yum && \
    yum install -y git

RUN mkdir -p /mypay4.pa && \
    mkdir -p /mypay4

WORKDIR /mypay4.pa

RUN mv mypay4.sources mypay4.pa

WORKDIR mypay4.pa/mypay4-be

ENV GRADLE_OPTS "-Xmx2048m"

RUN ./gradlew -PSERVER clean bootJar && \
    cp build/libs/*.jar /mypay4

FROM registry.access.redhat.com/ubi8/openjdk-11 as server
USER root
RUN groupadd --gid 1001 mypay4
RUN useradd --uid 1001 --gid 1001 --home "/mypay4" mypay4

RUN cp /etc/passwd /home/jboss/passwd
RUN chmod 555 /home/jboss
RUN chmod 444 /home/jboss/passwd

ENV JAVA_HOME="/usr/lib/jvm/java-11" \
    JAVA_VENDOR="openjdk" \
    JAVA_VERSION="11" \
    JBOSS_CONTAINER_OPENJDK_JDK_MODULE="/opt/jboss/container/openjdk/jdk" 

ADD fonts /usr/share/fonts/mypay/

RUN mkdir -p /mypay4 && \
    microdnf install -y yum && \
    yum install -y nano && \
    yum install -y curl && \
    microdnf install -y nmap 
    
 
#RUN chown -R mypay4:mypay4 /mypay4 && \
#RUN chmod -R 777 /mypay4 

COPY --from=base --chown=mypay4:mypay4 /mypay4 /mypay4

#USER mypay4

COPY --chown=mypay4:mypay4 start.sh /mypay4/start.sh

RUN chmod +x /mypay4/start.sh
#COPY --chown=mypay4:mypay4 mypay4-be-*.jar /mypay4/

USER mypay4

WORKDIR /mypay4

##CMD [ "/mypay4/start.sh" ]
CMD exec /bin/sh -c "trap : TERM INT; sleep 9999999999d & wait"

EXPOSE 8081
