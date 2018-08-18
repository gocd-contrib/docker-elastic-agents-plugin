# GoCD Elastic agent plugin for Docker

Table of Contents
=================

  * [Requirements](#requirements)
  * [Installation](#installation)
  * [Configuration](#configuration)
    - [Configure plugin settings](#configure-plugin-settings)
    - [Create an elastic profile](#create-an-elastic-profile)
    - [Configure job to use an elastic agent profile](#configure-job-to-use-an-elastic-agent-profile)

## Requirements

* GoCD server version `v17.9.0` or above
* Docker Server

## Installation

* Copy the file `build/libs/docker-elastic-agents-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external`
and restart the server.
* The `GO_SERVER_DIR` is usually `/var/lib/go-server` on **Linux** and `C:\Program Files\Go Server` on **Windows**.

## Configuration

### Configure plugin settings

The plugin settings are used to provide global level configurations for the plugin. Configurations such as docker server configuration and private registry settings are provided in plugin settings.

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Plugins_**

    ![Plugins][1]

2. Click on **_Settings icon_** of `Docker Elastic Agent Plugin` to update plugin settings configuration.

    ![Configure plugin settings][2]

    | Field Name                      | Mandatory | Description                                                                                                                                                                                     |
    |---------------------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
    | **Go server url**               | Yes       | GoCD server url(`https://YOUR_HOST_OR_IP_ADDRESS:8154/go`). The docker elastic agent will use this URL to register itself with GoCD. <br/>The GoCD hostname or ip must resolve in your container. Don't use `localhost` or `127.0.0.1` |
    | **Environment variables**       | No        | The environment variable for docker container |
    | **Agent auto-register timeout** | Yes       | Agent auto-register timeout(in minutes). Plugin will kill the agent container if it fails to register within provided time limits |
    | **Maximum docker containers**   | Yes       | Maximum docker containers to run at any given point in time. Plugin will not create more container when running container count reached to specified limits |
    | **Docker URI**                  | Yes       | Docker swarm cluster uri. <br/>If your Go Server is running on local machine then use(for mac and linux) — `unix:///var/run/docker.sock` |
    | **Docker CA Certificate**       | No        | Docker swarm cluster CA certificate |
    | **Docker Client Key**           | No        | Docker swarm cluster client key |
    | **Docker Client Certificate**   | No        | Docker swarm cluster client certificate |
    | **Private Docker Registry**     | Yes       | Optionally specify the private docker registry settings |

### Create an elastic profile

    The Elastic Agent Profile is used to define the configuration of a docker container(GoCD docker agent). The profile is used to configure the docker image, set memory limits, provide docker command and environment variables etc...

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Agent Profiles_**

    ![Elastic Profiles][3]

2. Click on **_Add_** to create new elastic agent profile

    ![Create elastic profile][4]

    | Field Name                | Mandatory | Description                                                                                                                                                                                     |
    |---------------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
    | **Id**                    | Yes       | Unique id for current profile                                                                                                                                                                   |
    | **Plugin id**             | Yes       | Select `Docker Elastic Agent Plugin` for **_Plugin id_**                                                                                                                                   |
    | **Docker image**          | Yes       | GoCD elastic agent docker image name. Pre build GoCD agent docker images are available [here](https://www.gocd.org/download/#docker)                                                            |
    | **Docker Command**        | No        | Commands that you want to execute on container start. <br/>*_Note: This will override the existing docker entry-point defined in docker image._*                                                     |
    | **Environment Variables** | No        | Environment variables for container. This will overrides the environment variables defined in plugin settings.(enter each per line)                                                             |
    | **Host entries**          | No        | This allows users to add host entries in `/etc/hosts`(enter each per line)                                                                                                                      |


### Configure job to use an elastic agent profile

1. Click the gear icon on **_Pipeline_**

    ![Pipeline][5]

2. Click on **_Quick Edit_** button

    ![Quick edit][6]

3. Click on **_Stages_**
4. Create/Edit a job
5. Enter the `unique id` of an elastic profile in Job Settings

    ![Configure a job][7]

6. Save your changes

[1]: images/plugins.png     "Plugins"
[2]: images/plugin-settings.png    "Configure plugin settings"
[3]: images/profiles_page.png  "Elastic profiles"
[4]: images/profile.png "Create elastic profile"
[5]: images/pipeline.png  "Pipeline"
[6]: images/quick-edit.png  "Quick edit"
[7]: images/configure-job.png  "Configure a job"
