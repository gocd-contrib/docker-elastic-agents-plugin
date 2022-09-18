# GoCD Elastic agent plugin for Docker

Table of Contents
=================

  * [Installation](#installation)
  * [Building the code base](#building-the-code-base)
  * [Using your own docker image with elastic agents](#using-your-own-docker-image-with-elastic-agents)
     * [Using the GoCD agent, installed via .deb/.rpm](#using-the-gocd-agent-installed-via-debrpm)
     * [Use a custom bootstrapper](#use-a-custom-bootstrapper)
  * [Troubleshooting](#troubleshooting)
  * [Credits](#credits)
  * [License](#license)

## Installation

Documentation for installation is available [here](INSTALL.md).

## Building the code base

To build the jar, run `./gradlew clean test assemble`

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

## Troubleshooting

### Enable Debug Logs

#### If you are on GoCD version 19.6 and above:

Edit the file `wrapper-properties.conf` on your GoCD server and add the following options. The location of the `wrapper-properties.conf` can be found in the [installation documentation](https://docs.gocd.org/current/installation/installing_go_server.html) of the GoCD server.

```properties
# We recommend that you begin with the index `100` and increment the index for each system property
wrapper.java.additional.100=-Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug
```

If you're running with GoCD server 19.6 and above on docker using one of the supported GoCD server images, set the environment variable `GOCD_SERVER_JVM_OPTIONS`:

```shell
docker run -e "GOCD_SERVER_JVM_OPTIONS=-Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug" ...
```

#### If you are on GoCD version 19.5 and lower:

* On Linux:

    Enabling debug level logging can help you troubleshoot an issue with this plugin. To enable debug level logs, edit the file `/etc/default/go-server` (for Linux) to add:

    ```shell
    export GO_SERVER_SYSTEM_PROPERTIES="$GO_SERVER_SYSTEM_PROPERTIES -Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug"
    ```

    If you're running the server via `./server.sh` script:

    ```shell
    $ GO_SERVER_SYSTEM_PROPERTIES="-Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug" ./server.sh
    ```

* On windows:

    Edit the file `config/wrapper-properties.conf` inside the GoCD Server installation directory (typically `C:\Program Files\Go Server`):

    ```
    # config/wrapper-properties.conf
    # since the last "wrapper.java.additional" index is 15, we use the next available index.
    wrapper.java.additional.16=-Dplugin.cd.go.contrib.elastic-agent.docker.log.level=debug
    ```

## Credits

Thanks to @konpa for the [docker icon](https://raw.githubusercontent.com/konpa/devicon/b80c6d9acb7b58b80904769015f9e0dd36fe46d2/icons/docker/docker-plain.svg) used by the plugin.

## License

```plain
Copyright 2022 Thoughtworks, Inc.

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
