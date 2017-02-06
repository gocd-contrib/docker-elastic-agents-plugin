FROM ...


# GoCD agent needs the jdk and git/svn/mercurial...
# Uncomment one of the lines below to ensure that openjdk is installed.
# apt-get install openjdk-8-jre-headless git
# yum install java-1.8.0-openjdk-headless git

# add steps to download and install the gocd agent
# See https://docs.gocd.io/current/installation/install/agent/linux.html

# download tini to ensure that an init process exists
ADD https://github.com/krallin/tini/releases/download/v0.14.0/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

# ensure that the container logs on stdout
ADD log4j.properties /var/lib/go-agent/log4j.properties
ADD log4j.properties /var/lib/go-agent/go-agent-log4j.properties

ADD go-agent /go-agent
RUN chmod 755 /go-agent

# Run the bootstrapper as the `go` user
USER go
CMD /go-agent
