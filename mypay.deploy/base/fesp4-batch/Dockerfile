FROM registry.access.redhat.com/ubi8/openjdk-8
USER root

ARG MASTER_NAME
ENV MASTER_NAME ${MASTER_NAME}

RUN dnf install -y yum && \
    yum install -y perl && \
    yum install -y nano

RUN mkdir -p /batch
COPY batch /batch
WORKDIR /batch

CMD exec /bin/sh -c "trap : TERM INT; sleep 9999999999d & wait"
#CMD [${MASTER_NAME}]
