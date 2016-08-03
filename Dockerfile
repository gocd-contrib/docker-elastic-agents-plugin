FROM ubuntu:trusty
MAINTAINER GoCD Team <go-cd@googlegroups.com>

RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get -y install openjdk-7-jre-headless curl git
