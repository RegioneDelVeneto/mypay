from fluent/fluentd:v1.13-1
USER root
RUN gem install elasticsearch -v 7.13.3
RUN gem install fluent-plugin-elasticsearch && \
    gem install fluent-plugin-grok-parser
COPY fluent.conf /fluentd/etc/
ENV FLUENTD_CONF="fluent.conf"

#RUN groupadd -g 9000 mypay4 && \
#    useradd -u 9000 -g mypay4 mypay4
#USER mypay4

