#!/bin/bash

REPOSITORY=/home/ec2-user/docker/Spring-Boot-Board
PROJECT_NAME=web/spring-boot-board
JAR_DIR=build/libs
BACKUP_DIR=backup
SCRIPT_DIR=docker/scripts

cd $REPOSITORY/$PROJECT_NAME

# echo "> Git Pull"
# git pull

echo "> Destroy docker container running now"
cd $REPOSITORY/$SCRIPT_DIR
pwd
sh ./destroy.sh

if ! [[ -d $REPOSITORY/$BACKUP_DIR ]]; then
	mkdir $REPOSITORY/$BACKUP_DIR
fi

echo "> Backup previous version"
sudo mv $REPOSITORY/$PROJECT_NAME/$JAR_DIR/* $REPOSITORY/$BACKUP_DIR

echo "> Start Building Project"
cd $REPOSITORY/$PROJECT_NAME
./gradlew build --exclude-task test

echo "> Grant Permission"
chmod 777 ./build/libs/*

echo "> Deploy new application"
cd $REPOSITORY/$SCRIPT_DIR
sh ./create.sh
