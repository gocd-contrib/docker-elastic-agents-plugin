# GoCD Elastic agent plugin for Docker

Table of Contents
=================

  * [Building the code base](#building-the-code-base)
  * [Is this production ready?](#is-this-production-ready)
  * [Using your own docker image with elastic agents](#using-your-own-docker-image-with-elastic-agents)
     * [Using the GoCD agent, installed via .deb/.rpm](#using-the-gocd-agent-installed-via-debrpm)
     * [Use a custom bootstrapper](#use-a-custom-bootstrapper)
  * [Usage instructions](#usage-instructions)
  * [Troubleshooting](#troubleshooting)
  * [Credits](#credits)
  * [License](#license)

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## Is this production ready?

It depends.

**tl;dr;**

If you need something simple to get started with, use this plugin. If you're looking to run a lot of builds, you probably want to look at the [docker swarm elastic agent plugin](https://github.com/gocd-contrib/docker-swarm-elastic-agents).

**The long answer:**

The plugin, as it is currently implemented is meant to be a very simple plugin to demonstrate how to get started with GoCD [elastic agent](https://plugin-api.go.cd/current/elastic-agents) feature. It does not support some of the other interesting things you can do with docker (resource limits, host file mapping etc.)

## Using your own docker image with elastic agents

The plugin executes the equivalent of the following docker command to start the agent —

```
docker run -e GO_EA_SERVER_URL=...
           -e GO_EA_AUTO_REGISTER_KEY=...
           -e GO_EA_AUTO_REGISTER_ENVIRONMENT=...
           -e GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=...
           -e GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=...
           ...
           IMAGE_ID
```

Your docker image is expected to contain a bootstrap program (to be executed via docker's `CMD`) that will create an [`autoregister.properties`](https://docs.gocd.io/current/advanced_usage/agent_auto_register.html) file using these variables. The `GO_EA_SERVER_URL` will point to the server url that the agent must communicate with.

Here is an example shell script to do this —

```bash
# write out autoregister.properties
(
cat <<EOF
agent.auto.register.key=${GO_EA_AUTO_REGISTER_KEY}
agent.auto.register.environments=${GO_EA_AUTO_REGISTER_ENVIRONMENT}
agent.auto.register.elasticAgent.agentId=${GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID}
agent.auto.register.elasticAgent.pluginId=${GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID}
EOF
) > /var/lib/go-agent/config/autoregister.properties
```

### Using the GoCD agent, installed via `.deb/.rpm`

See the bootstrap script and docker file here under [`contrib/scripts/bootstrap-via-installer`](contrib/scripts/bootstrap-via-installer).

### Use a custom bootstrapper

This method uses lesser memory and boots up the agent process and starts off a build quickly:

See the bootstrap script and docker file here under [`contrib/scripts/bootstrap-without-installed-agent`](contrib/scripts/bootstrap-without-installed-agent).

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
          <value>alpine</value>
        </property>
        <property>
            <key>Command</key>
            <value>ls</value>
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

Thanks to @konpa for the [docker icon](https://raw.githubusercontent.com/konpa/devicon/b80c6d9acb7b58b80904769015f9e0dd36fe46d2/icons/docker/docker-plain.svg) used by the plugin.

## License

```plain
Copyright 2018, ThoughtWorks, Inc.

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
