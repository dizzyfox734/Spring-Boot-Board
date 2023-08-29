#!/bin/bash
set -e

# if ! [[ -e ..//web/app.jar ]]; then
# 	cp ../../*.jar ../web/app.jar
# fi

docker-compose up -d --build

