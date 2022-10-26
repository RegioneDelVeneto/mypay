FROM fluent/fluentd:v1.7-1
USER root
RUN ["gem", "install", "elasticsearch", "--no-document", "--version", "7.13.3"]
RUN ["gem", "install", "fluent-plugin-elasticsearch", "--no-document", "--version", "5.2.3"]
RUN ["gem", "install", "fluent-plugin-grok-parser", "--no-document", "--version", "2.6.2"]
COPY fluent.conf /fluentd/etc/
RUN addgroup --gid 9001 mypay4
RUN adduser fluent mypay4
USER fluent

