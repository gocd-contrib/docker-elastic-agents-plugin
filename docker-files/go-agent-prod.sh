#!/bin/bash

cd /var/lib/go-agent

mkdir -p config
echo "${AUTO_REGISTER_CONTENTS}" > config/autoregister.properties
unset AUTO_REGISTER_CONTENTS

PRODUCTION_MODE=Y AGENT_WORK_DIR=/var/lib/go-agent bash -x /usr/share/go-agent/agent.sh
