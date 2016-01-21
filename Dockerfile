FROM ubuntu:trusty
MAINTAINER GoCD Team <go-cd@googlegroups.com>

RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get -y install openjdk-7-jre-headless curl git
RUN mkdir -p /usr/local
RUN curl --silent --location --fail http://www.us.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz | tar -zxf - -C /usr/local
RUN ln -sf /usr/local/apache-maven-3.3.9/bin/mvn /usr/local/bin/mvn
