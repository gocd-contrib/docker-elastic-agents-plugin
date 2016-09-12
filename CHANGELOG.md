## 0.3.0 - 2016-09-12

### Bug fixes

- Do not attempt to load docker certificates if they are not specified in the configuration
- When terminating instances that did not register after a timeout, gracefully handle a `ContainerNotFoundException`, in case the container was cleaned up by other means

## 0.2.0 - 2016-08-26

### Added

- Environment variables can be specified using the `Environment` property —

    ```xml
    <agentConfig pluginId="cd.go.contrib.elastic-agent.docker">
      <property>
        <key>Environment</key>
        <value>
          JAVA_HOME=/opt/java
          MAKE_OPTS=-j8
        </value>
      </property>
    </agentConfig>
    ```

- If you'd like to specify environment variables globally for all containers, the plugin settings will let you do just that.
- Added support for a plugin setting to limit the maximum number of containers that should be started up.

## 0.1.0 - 2016-08-18

Initial release of plugin
