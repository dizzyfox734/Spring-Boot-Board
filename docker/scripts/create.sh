#!/bin/bash
set -e

if ! [[ -d ../logs/redis ]]; then
    mkdir -p ../logs/redis
fi

if ! [[ -d ../redis-data ]]; then
    mkdir ../redis-data
fi

# if ! [[ -e ..//web/app.jar ]]; then
# 	cp ../../*.jar ../web/app.jar
# fi

docker-compose up -d --build

