# GoCD Elastic agent plugin for Docker

Table of Contents
=================

  * [Installation](#installation)
  * [Building the code base](#building-the-code-base)
  * [Is this production ready?](#is-this-production-ready)
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
