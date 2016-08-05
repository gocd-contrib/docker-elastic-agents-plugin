#!/bin/bash

mkdir -p /go-agent
cd /go-agent

mkdir -p config
echo "${AUTO_REGISTER_CONTENTS}" > config/autoregister.properties
unset AUTO_REGISTER_CONTENTS

curl -k "${GO_SERVER_URL}/admin/agent" > agent.jar


exec java -jar agent.jar -serverUrl "${GO_SERVER_URL}" > agent.stdout.log 2>&1
