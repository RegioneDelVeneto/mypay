from fluent/fluentd:v1.7-1
USER root
RUN gem install fluent-plugin-elasticsearch && \
    gem install fluent-plugin-grok-parser
COPY fluent.conf /fluentd/etc/
ENV FLUENTD_CONF="fluent.conf"
