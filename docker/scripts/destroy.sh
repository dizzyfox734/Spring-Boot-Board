#!/bin/bash
set -e

docker-compose down --volumes
docker rmi spring-boot-board_web spring-boot-board_db spring-boot-board_redis #docker-test-nginx
