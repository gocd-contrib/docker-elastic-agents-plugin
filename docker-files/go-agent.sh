#!/bin/bash

if [[ "${MODE}" == 'dev' ]]; then
  unset MODE
  exec /go-agent-dev.sh
fi

if [[ "${MODE}" == 'prod' ]]; then
  unset MODE
  exec /go-agent-prod.sh
fi
