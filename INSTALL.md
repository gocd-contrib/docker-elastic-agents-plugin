# GoCD Elastic agent plugin for Docker

Table of Contents
=================

  * [Requirements](#requirements)
  * [Installation](#installation)
  * [Configuration](#configuration)
    - [Configure a Cluster Profile](#configure-a-cluster-profile)
    - [Create an Elastic Profile](#create-an-elastic-profile)
    - [Configure a Job to use an Elastic Agent Profile](#configure-a-job-to-use-an-elastic-agent-profile)
    - [Connecting to a remote docker daemon secured with TLS](#connecting-to-a-remote-docker-daemon-secured-with-tls)

## Requirements

* GoCD server version `v19.3.0` or above
* Docker Server

## Installation

* Copy the file `build/libs/docker-elastic-agents-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external`
and restart the server.
* The `GO_SERVER_DIR` is usually `/var/lib/go-server` on **Linux** and `C:\Program Files\Go Server` on **Windows**.

## Configuration

### Configure a Cluster Profile

The cluster profile settings are used to provide cluster level configurations for the plugin. Configurations such as docker server configuration and private registry settings are provided in cluster profile settings.

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Profile_**

    ![Elastic Profiles][1]

2. Click on **_Add Cluster Profile_**. Select `Docker Elastic Agent Plugin` from the plugin ID dropdown. 

    ![Cluster Profile basic settings][2]
    ![Cluster Profile docker client settings][8]

    | Field Name                      | Mandatory | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
    |---------------------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
    | **Go server url**               | Yes       | GoCD server url (`http://YOUR_HOST_OR_IP_ADDRESS:8153/go`). The docker elastic agent will use this URL to register itself with GoCD. <br/>The GoCD hostname or ip must resolve in your container. Don't use `localhost` or `127.0.0.1`                                                                                                                                                                                                                                                                                                                                                                                                                          |
    | **Environment variables**       | No        | The environment variable for docker container                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
    | **Agent auto-register timeout** | Yes       | Agent auto-register timeout(in minutes). Plugin will kill the agent container if it fails to register within provided time limits                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
    | **Maximum docker containers**   | Yes       | Maximum docker containers to run at any given point in time. Plugin will not create more container when running container count reached to specified limits                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
    | **Docker URI**                  | Yes       | Docker daemon uri. <br/>If your Go Server is running on local machine then use(for mac and linux) — `unix:///var/run/docker.sock`. Otherwise, refer to [Connecting to a remote docker daemon secured with TLS](#connecting-to-a-remote-docker-daemon-secured-with-tls)                                                                                                                                                                                                                                                                                                                                                                                          |
    | **Docker CA Certificate**       | No        | Docker CA certificate, refer to [Connecting to a remote docker daemon secured with TLS](#connecting-to-a-remote-docker-daemon-secured-with-tls)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
    | **Docker Client Key**           | No        | Docker client key, to refer to [Connecting to a remote docker daemon secured with TLS](#connecting-to-a-remote-docker-daemon-secured-with-tls)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
    | **Docker Client Certificate**   | No        | Docker client certificate, refer to [Connecting to a remote docker daemon secured with TLS](#connecting-to-a-remote-docker-daemon-secured-with-tls)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
    | **Private Docker Registry**     | Yes       | Optionally specify the private docker registry settings, either by inputting custom credentials on the GoCD admin UI, or by creating a docker configuration file in `$HOME/.docker/config.json` that is accessible by GoCD server. Please note that if you choose to use custom credentials and initially set the `private_registry_username` and `private_registry_password` fields, then switch to using a docker configuration file later, they will still appear in the cluster configuration UI. If you don't want those values to show up, please explicitly clear them out in the configuration form before switching to use a docker configuration file |

### Create an elastic profile

    The Elastic Agent Profile is used to define the configuration of a docker container(GoCD docker agent). The profile is used to configure the docker image, set memory limits, provide docker command and environment variables etc...

1. Login to `GoCD server` as admin and navigate to **_Admin_** _>_ **_Elastic Profiles_**

    ![Elastic Profiles][3]

2. Click on **_New Elastic Agent Profile_** to create new elastic agent profile for a cluster.

    ![Create elastic profile][4]

    | Field Name                | Mandatory | Description                                                                                                                                                                                     |
    |---------------------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
    | **Id**                    | Yes       | Unique id for current profile                                                                                                                                                                   |
    | **Plugin id**             | Yes       | Select `Docker Elastic Agent Plugin` for **_Plugin id_**                                                                                                                                   |
    | **Docker image**          | Yes       | GoCD elastic agent docker image name. Pre build GoCD agent docker images are available [here](https://www.gocd.org/download/#docker)                                                            |
    | **Docker Command**        | No        | Commands that you want to execute on container start. <br/>*_Note: This will override the existing docker entry-point defined in docker image._*                                                     |
    | **Environment Variables** | No        | Environment variables for container. This will overrides the environment variables defined in cluster profile.(enter each per line)                                                             |
    | **Resource Constraints (Reserved Memory, Max Memory, CPUs)** | No | Resource allocation and limits for agent containers |
    | **Volume Mounts** | No | Any volumes you want to mount from the host machine onto your agent containers, in the format of `source-path:dest-path[:ro]` on each line (eg. you might want to mount a `.ssh` folder containing SSH keys on your host machine inside the agent container, so the agent can pull from your preferred VCS with SSH) |
    | **Host entries**          | No        | This allows users to add host entries in `/etc/hosts`(enter each per line)                                                                                                                      |


### Configure a job to use an elastic agent profile

1. Click the gear icon on **_Pipeline_**

    ![Pipeline][5]

2. Click on **_Quick Edit_** button

    ![Quick edit][6]

3. Click on **_Stages_**
4. Create/Edit a job
5. Enter the `unique id` of an elastic profile in Job Settings

    ![Configure a job][7]

6. Save your changes

[1]: images/elastic_profiles_spa.png     "Elastic Profiles"
[2]: images/cluster-profiles/basic-settings.png    "Cluster Profile basic settings"
[3]: images/profiles_page.png  "Elastic profiles"
[4]: images/profile.png "Create elastic profile"
[5]: images/pipeline.png  "Pipeline"
[6]: images/quick-edit.png  "Quick edit"
[7]: images/configure-job.png  "Configure a job"
[8]: images/cluster-profiles/docker-client-settings.png "Cluster Profile docker client settings" 

### Connecting to a remote docker daemon secured with TLS

If you would like to run elastic agents on a separate machine from GoCD server, you can configure the agent machine to allow remote access to its docker daemon socket, following the [official Docker documentation](https://docs.docker.com/config/daemon/remote-access/).

You must make sure that the IP and exposed port of the docker daemon is accessible from GoCD server (ie. any firewall/security group rules are configured correctly to allow incoming traffic from GoCD server to the docker daemon socket). You can test network connectivity from your GoCD server with a tool like netcat, eg.: `nc -vz <docker-daemon-machine-IP-or-hostname> 2375`.

It is recommended that a docker daemon socket exposed remotely be secured with TLS. You may follow the steps in the [official Docker documentation](https://docs.docker.com/engine/security/protect-access/#use-tls-https-to-protect-the-docker-daemon-socket) for generating the required CA, server, and client cert and keys. The CA cert, server cert and key, and `--tlsverify` flag must be passed to the docker daemon on start-up, while the CA and client cert and key must be added to your cluster profile configuration under **Docker CA Certificate**, **Docker Client cert** and **Docker Client key**. These will be used by GoCD server to carry out a TLS handshake with the remote docker daemon. If any of these values are incorrectly configured, you will see TLS/SSL handshake errors in your GoCD server logs.
