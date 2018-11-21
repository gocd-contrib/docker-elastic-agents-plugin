## 2.2.0 - 2018-11-21

- Updated plugin libraries
- Fixed the issue with plugin settings validation

## 2.1.0 - 2018-11-14

### Added
- Allow loading credentials from config file when using a custom Docker registry
- Show warnings in server health messages

## 2.0.0 - 2018-10-26

### Added
- Terminate container once job is finished.

### Fixed
- Fixed validation issue with `enable_private_registry_authentication` and `pull_on_container_create`.

**_Note:_** *Requires GoCD version 18.10.0 and above. Plugin will not work with the older version of GoCD.*

## 1.0.2 - 2018-09-04
- Removed additional margin from status report pages

## 1.0.1 - 2018-07-04
- Fixed broken plugin settings UI.

## 1.0.0 - 2018-06-26

### Added
- Option to pull docker image on container create.
- Show message on agent status report page when container in not running for the specified job id.

## 0.9.0 - 2018-03-08

### Added
- Support for agent status report and plugin status report.
- Containers are labelled with a JobIdentifier, which is used to assign work to the right container.

### Know issues
- Better message when a container is not created for the job or docker container is killed[(#48)](https://github.com/gocd-contrib/docker-elastic-agents/issues/48).


**_Note:_** *Requires GoCD version 18.2.0 and above. Plugin will not work with the older version of GoCD.*


## 0.8.0 - 2017-09-03

#### Added

- User can now provide mapping for host entries in IP-ADDRESS HOSTNAME-1 HOSTNAME-2... format, which is the standard format for `/etc/hosts` file.

```hosts
10.0.0.1   host-x
10.0.0.2   host-y   host-z
``` 

#### Changes

- Updated spotify `docker-client` library to `v8.9.0`

## 0.7.0 - 2017-04-18

### Added

- Allow usage of a private registry for elastic agent images 

## 0.6.1 - 2016-12-11

### Changed

- Changed the `go.cd.elastic-agent.get-icon` call to use underscore instead of hyphens.

## 0.6.0 - 2016-11-19

### Added

- Added support for a few additional calls required by the GoCD server.
  * `go.cd.elastic-agent.get-profile-metadata`
  * `go.cd.elastic-agent.get-profile-view`
  * `go.cd.elastic-agent.validate-profile`
  * `go.cd.elastic-agent.get-icon`

## 0.5.0 - 2016-10-06

### Fixed

- Fix some synchronization issues that allowed more number of containers than the settings permitted

## 0.4.0 - 2016-09-29

### Changed

- The `AUTO_REGISTER_CONTENTS` contents environment variable has now been split up into 4 separate variables ([details here](https://docs.go.cd/current/advanced_usage/agent_auto_register.html)) —
  * `GO_EA_AUTO_REGISTER_KEY` - the auto-register key
  * `GO_EA_AUTO_REGISTER_ENVIRONMENT` - the auto-register environment
  * `GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID` - the elastic agent id
  * `GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID` — the elastic plugin id

### Added

- The command to execute in the docker container can now be specified using the `Command` property —

    ```xml
    <profile pluginId="cd.go.contrib.elastic-agent.docker" id="foo">
      <property>
        <key>Command</key>
        <value>
          JAVA_HOME=/opt/java
          MAKE_OPTS=-j8
        </value>
      </property>
    </profile>
    ```


## 0.3.0 - 2016-09-12

### Bug fixes

- Do not attempt to load docker certificates if they are not specified in the configuration
- When terminating instances that did not register after a timeout, gracefully handle a `ContainerNotFoundException`, in case the container was cleaned up by other means

## 0.2.0 - 2016-08-26

### Added

- Environment variables can be specified using the `Environment` property —

    ```xml
    <profile pluginId="cd.go.contrib.elastic-agent.docker" id="foo">
      <property>
        <key>Environment</key>
        <value>
          JAVA_HOME=/opt/java
          MAKE_OPTS=-j8
        </value>
      </property>
    </profile>
    ```

- If you'd like to specify environment variables globally for all containers, the plugin settings will let you do just that.
- Added support for a plugin setting to limit the maximum number of containers that should be started up.

## 0.1.0 - 2016-08-18

Initial release of plugin
