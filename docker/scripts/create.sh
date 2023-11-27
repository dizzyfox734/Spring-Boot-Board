#!/bin/bash
set -e

#if ! [[ -d ../logs/mysql ]]; then
#    mkdir -p ../logs/mariadb
#fi

if ! [[ -d ../logs/redis ]]; then
    mkdir -p ../logs/redis
fi

#if ! [[ -d ../database ]]; then
#    mkdir ../database
#fi

if ! [[ -d ../redis-data ]]; then
    mkdir ../redis-data
fi

# if ! [[ -e ..//web/app.jar ]]; then
# 	cp ../../*.jar ../web/app.jar
# fi

docker-compose up -d --build

