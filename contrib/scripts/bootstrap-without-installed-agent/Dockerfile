FROM ...

# GoCD agent needs the jdk 17 and appropriate binaries for your SCM (git/svn/mercurial)
# Add then here by the appropriate package manager

# download tini to ensure that an init process exists
ADD https://github.com/krallin/tini/releases/download/v0.19.0/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

# Add a user to run the go agent
RUN adduser go go -h /go -S -D

# ensure that the container logs on stdout
ADD agent-launcher-logback-include.xml /go/config/agent-launcher-logback-include.xml
ADD agent-logback-include.xml /go/config/agent-logback-include.xml

ADD go-agent /go-agent
RUN chmod 755 /go-agent

# Run the bootstrapper as the `go` user
USER go
CMD /go/go-agent
