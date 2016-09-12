FROM ubuntu:trusty
MAINTAINER GoCD Team <go-cd@googlegroups.com>

RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get -y install openjdk-7-jre-headless git

ADD https://github.com/ketan/gocd-golang-bootstrapper/releases/download/0.3/go-bootstrapper-0.3.linux.amd64 /go-agent
RUN chmod 755 /go-agent
RUN mkdir -p /go
CMD ["/go-agent"]
