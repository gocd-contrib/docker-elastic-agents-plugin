# GoCD Elastic agent plugin for Docker [![Build Status](https://snap-ci.com/gocd-contrib/docker-elastic-agents/branch/master/build_image)](https://snap-ci.com/gocd-contrib/docker-elastic-agents/branch/master)

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## Configuring

### Mac OsX

Assuming you're using docker for mac (https://docs.docker.com/docker-for-mac/) —

* Setup the server url to https://host:8154/go. Where host is the ipaddress of your default interface (`ipconfig getifaddr en0` or `ipconfig getifaddr en1`)
* Setup the Docker URI to `unix:///var/run/docker.sock`

## License

```plain
Copyright 2015 ThoughtWorks, Inc.

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
