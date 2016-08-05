#!/bin/bash

cd /var/lib/go-agent

mkdir -p config
echo "${AUTO_REGISTER_CONTENTS}" > config/autoregister.properties
unset AUTO_REGISTER_CONTENTS

PRODUCTION_MODE=Y /usr/share/go-agent/agent.sh
