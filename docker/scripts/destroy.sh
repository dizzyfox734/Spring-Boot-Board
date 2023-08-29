#!/bin/bash
set -e

docker-compose down --volumes
docker rmi docker-test-web docker-test-db # docker-test-nginx
