# GoCD Elastic agent plugin for Docker [![Build Status](https://snap-ci.com/gocd-contrib/docker-elastic-agents/branch/master/build_image)](https://snap-ci.com/gocd-contrib/docker-elastic-agents/branch/master)

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## Is this production ready?

It depends.

The plugin, as it is currently implemented is meant to be a very simple plugin to demonstrate how to get started with GoCD [elastic agent](https://plugin-api.go.cd/current/elastic-agents) feature. This plugin terminates docker containers very aggressively (within a minute or two of the agent being idle). Depending on your usage, this may not be desirable. If this behavior is undesirable to you, you may need to fork this plugin and [tweak it a bit](https://github.com/gocd-contrib/docker-elastic-agents/blob/master/src/main/java/cd/go/contrib/elasticagents/docker/executors/ServerPingRequestExecutor.java) so the docker containers are not terminated as aggressively.

## Customizing your docker image to run as a GoCD Elastic Agent

There are two ways to customize your docker image to work with this plugin
 
### Use the GoCD agent

* Ensure that you have installed the go-agent for your distribution, using apt/yum or a zip file if you're using a distribution that does not support apt or yum.
* Once the agent is installed, create a simple shell script executed via (`CMD`) that will accept the following variables and execute the agent bootstrapper process. These environment variables are passed by this plugin when performing a `docker run`. Your image is expected to use these variables to create a correct `autoregister.properties`:
  * `GO_EA_SERVER_URL` - the URL of the GoCD server (from the plugin settings page)
  * `GO_EA_AUTO_REGISTER_KEY` - the auto-register key
  * `GO_EA_AUTO_REGISTER_ENVIRONMENT` - the auto-register environment
  * `GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID` - the elastic agent id
  * `GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID` — the elastic plugin id

### Use the GoCD golang bootstrapper

This method is a [bit insecure (PR welcome)](https://github.com/ketan/gocd-golang-bootstrapper), but uses lesser memory and boots up and starts off a build quickly:
 
```dockerfile
FROM yourimage

# install whatever packages you need, in addition to the JRE, and git
# apt-get install openjdk-8-jre-headless git
# yum install java-1.8.0-openjdk-headless git

# Add a user to run the go agent
RUN adduser go go -h /go -S -D

# download the agent bootstrapper
ADD https://github.com/ketan/gocd-golang-bootstrapper/releases/download/0.9/go-bootstrapper-0.9.linux.amd64 /go/go-agent
RUN chmod 755 /go/go-agent

# download tini
ADD https://github.com/krallin/tini/releases/download/v0.10.0/tini /tini
RUN chmod +x /tini
ENTRYPOINT ["/tini", "--"]

# Run the bootstrapper as the `go` user
USER go
CMD /go/go-agent
``` 

## Usage instructions

* Download and install Docker for your favorite OS from https://docs.docker.com/engine/installation/

If you already have it running it on a mac, make sure to restart it (see https://github.com/docker/for-mac/issues/17#mobyaccess). Time drift is known to cause the plugin to not work, because the timestamps returned by the docker API has drifted from the host.

A good way to know if there's a time drift is to run `docker ps` —

    ```
    CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
    e0754c9f4cdb        alpine:latest       "/bin/sh"           32 minutes ago      Up 17 seconds                           test
    809f310ba1e4        ubuntu:trusty       "/bin/bash"         33 minutes ago      Up About a minute                       reverent_raman
    ```

Notice how the `CREATED` and `STATUS` are several minutes apart for a recently created container.

* Download the latest GoCD installer from https://go.cd/download

    ```shell
    $ unzip go-server-VERSION.zip
    $ mkdir -p go-server-VERSION/plugins/external
    ```
* Download the docker plugin (https://github.com/gocd-contrib/docker-elastic-agents/releases)
* Copy the docker plugin to the go server directory

    ```
    $ cp docker-elastic-agents-0.1-SNAPSHOT.jar /path/to/go-server-VERSION/plugins/external
    ```

* Start the server and configure the plugin (turn on debug logging to get more logs, they're not that noisy)

  On linux/mac

    ```shell
    $ GO_SERVER_SYSTEM_PROPERTIES='-Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug' ./server.sh
    ```

  On windows

    ```
    C:> set GO_SERVER_SYSTEM_PROPERTIES='-Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug'
    C:> server.cmd
    ```

To configure the plugin, navigate to the plugin settings page on your GoCD server http://localhost:8153/go/admin/plugins and setup the following settings for the docker plugin.

```
Go Server Host — https://YOUR_IP_ADDRESS:8154/go — do not use "localhost"
Docker URI (for mac and linux) — unix:///var/run/docker.sock
Auto register timeout - between 1-3 minutes
```

Now setup the config.xml —

* add `agentAutoRegisterKey="some-secret-key"` to the `<server/>` tag.
* setup a job —

```xml
<server agentAutoRegisterKey="...">
  <elastic>
    <profiles>
      <profile id="docker.unit-tests" pluginId="cd.go.contrib.elastic-agent.docker">
        <!-- The following properties are currently supported -->
        <property>
          <!-- Allows you to select the docker image that the build should run with -->
          <key>Image</key>
          <value>gocdcontrib/ubuntu-docker-elastic-agent</value>
        </property>
        <property>
          <!-- Allows you to set the environment variables when starting the docker container -->
          <key>Environment</key>
          <value>
            JAVA_HOME=/opt/java
            MAKE_OPTS=-j8
          </value>
        </property>
        <property>
          <!-- Allows you to set the command that should be run on the container, separate executable and each args by a newline -->
          <key>Command</key>
          <value>
            ls
            -al
            /usr/bin
          </value>
        </property>
      </profile>
    </profiles>
  </elastic>
</server>
...
<pipelines group="defaultGroup">
  <pipeline name="Foo">
    <materials>
      <git url="YOUR GIT URL" />
    </materials>
    <stage name="defaultStage">
      <jobs>
        <job name="defaultJob" elasticProfileId="docker.unit-tests">
          <tasks>
            <exec command="ls" />
          </tasks>
        </job>
      </jobs>
    </stage>
  </pipeline>
</pipelines>
```

## Troubleshooting

Enabling debug level logging can help you troubleshoot an issue with the elastic agent plugin. To enable debug level logs, edit the `/etc/default/go-server` (for Linux) to add:

```bash
export GO_SERVER_SYSTEM_PROPERTIES="$GO_SERVER_SYSTEM_PROPERTIES -Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug"
```

If you're running the server via `./server.sh` script —

```
$ GO_SERVER_SYSTEM_PROPERTIES="-Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug" ./server.sh
```

## Credits

Thanks to @konpa for the [docker image](https://raw.githubusercontent.com/konpa/devicon/b80c6d9acb7b58b80904769015f9e0dd36fe46d2/icons/docker/docker-plain.svg) provided by the plugin.

## License

```plain
Copyright 2016, ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
