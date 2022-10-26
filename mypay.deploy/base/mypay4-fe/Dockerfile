FROM registry.access.redhat.com/ubi7/nodejs-12 as base
USER root

ARG USERNAME
ARG PASSWORD
ARG BRANCH

RUN yum install -y git

RUN mkdir -p /mypay4.pa && \
    mkdir -p /mypay4
   
WORKDIR /mypay4.pa


RUN mv yourpay4.sources mypay4.pa

WORKDIR mypay4.pa/mypay4-fe
	
RUN npm install -g @angular/cli@~10.0.1

RUN npm install -g typescript@~3.9.6

RUN npm install

RUN npm run build -- mypay4-fe-cittadino --prod --base-href /mypay4/snapshot/cittadino/ --output-path /mypay4/snapshot/mypay4-fe-cittadino --configuration mywire-snapshot
RUN npm run build -- mypay4-fe-operatore --prod --base-href /mypay4/snapshot/operatore/ --output-path /mypay4/snapshot/mypay4-fe-operatore --configuration mywire-snapshot

FROM registry.access.redhat.com/ubi8/openjdk-11 as server

USER root

RUN groupadd -g 9000 mypay4 \
 && chmod -R 777 /home/jboss \
 && adduser \
    --home "/mypay4" \
    --gid 9000 \
    --no-create-home \
    --uid 9000 \
    "mypay4"

RUN microdnf install -y yum && \
    yum install -y nano && \
    yum install -y nmap && \
    yum install -y curl && \
    mkdir -p mypay4 && \
    mkdir -p mypay4/snapshot

#ADD mypay4-fe-cittadino /mypay4/snapshot/mypay4-fe-cittadino
#ADD mypay4-fe-operatore /mypay4/snapshot/mypay4-fe-operatore

COPY --from=base --chown=mypay4:mypay4 /mypay4 /mypay4


#ENV nginxversion="1.18.0-1" \
#    os="rhel" \
#    osversion="8" \
#    elversion="8"
#RUN yum install -y wget openssl sed &&\
#    yum -y autoremove &&\
#    yum clean all &&\
#    wget http://nginx.org/packages/$os/$osversion/x86_64/RPMS/nginx-$nginxversion.el$elversion.ngx.x86_64.rpm &&\

ADD nginx-1.18.0-1.el8.ngx.x86_64.rpm /mypay4/nginx-1.18.0-1.el8.ngx.x86_64.rpm
RUN rpm -iv /mypay4/nginx-1.18.0-1.el8.ngx.x86_64.rpm && \
    rm /mypay4/nginx-1.18.0-1.el8.ngx.x86_64.rpm

COPY ./cert/dhparam.pem          /mypay4/ssl/certs/dhparam.pem
COPY ./cert/nginx-selfsigned.crt /mypay4/ssl/certs/nginx-selfsigned.crt
COPY ./cert/nginx-selfsigned.key /mypay4/ssl/private/nginx-selfsigned.key

RUN mkdir -p /var/log/nginx \
&&  mkdir -p /var/lib/nginx \ 
&&  ln -sf /dev/stdout /var/log/nginx/access.log \
&&  ln -sf /dev/stderr /var/log/nginx/error.log \
&&  chown 9000:9000 /var/log/nginx \
&&  chown 9000:9000 /var/lib/nginx \
&&  rm /etc/nginx/nginx.conf \
&&  rm -r /etc/nginx/conf.d \
&&  chown -R 9000:9000 /mypay4 \
&&  chmod -R 777 /mypay4 \
&&  chown -R 9000:9000 /var/cache/nginx \
&&  chmod -R g+w /var/cache/nginx \
&&  chown -R 9000:9000 /etc/nginx \
&&  chmod -R g+w /etc/nginx

WORKDIR /mypay4
#USER mypay4

EXPOSE 8443

CMD ["nginx", "-g", "daemon off;"]

