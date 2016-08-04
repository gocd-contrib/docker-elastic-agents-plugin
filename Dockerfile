FROM ubuntu:trusty
MAINTAINER GoCD Team <go-cd@googlegroups.com>

COPY docker-files/gocd.list /etc/apt/sources.list.d/gocd.list
COPY docker-files/GOCD-GPG-KEY.asc /tmp/GOCD-GPG-KEY.asc
RUN sudo apt-key add /tmp/GOCD-GPG-KEY.asc

RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get -y install openjdk-7-jre-headless curl git go-agent

# because all configuration comes at runtime
RUN rm /etc/default/go-agent
