FROM ubuntu:trusty
MAINTAINER GoCD Team <go-cd@googlegroups.com>

RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install openjdk-7-jre-headless git

RUN adduser go go -h /go -S -D
ADD https://github.com/ketan/gocd-golang-bootstrapper/releases/download/0.9/go-bootstrapper-0.9.linux.amd64 /go/go-agent
RUN chmod 755 /go/go-agent

ADD https://github.com/krallin/tini/releases/download/v0.10.0/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

USER go
CMD /go/go-agent
