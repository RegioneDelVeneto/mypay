FROM registry.access.redhat.com/ubi8/openjdk-8
USER root

ARG MASTER_NAME
ENV MASTER_NAME ${MASTER_NAME}

RUN mkdir -p /batch
COPY batch /batch
WORKDIR /batch

RUN groupadd --gid 1001 mypay4
RUN useradd --uid 1001 --gid 1001 mypay4
RUN chown -R 1001:1001 /batch
RUN mkdir /conf && chown -R 1001:1001 /conf
RUN microdnf -y module enable perl:5.30 && microdnf install -y perl-interpreter procps-ng
## Fix problem with openjdk redhat
RUN cp /etc/passwd /home/jboss/passwd
RUN chmod 555 /home/jboss
RUN chmod 444 /home/jboss/passwd
USER mypay4
ENV HOME /home/mypay4
CMD /bin/tail -f /dev/null
