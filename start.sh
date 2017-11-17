#!/bin/sh
#set -x

###########################
## Shell script to run the compiled chat server from 
## https://www.github.com/amhiggin/ChatroomServer github repository.
## Requires running compile.sh first, and then running script with argument $1 portNumber
## Make sure that both 1) and 2) are not uncommented at the same time
###########################

echo "Starting up chatroom server on port $1"

## (1) This is for regular launching
java -cp cs4400chatserver/src main.java.ChatServer $1

## (2) This is for launching with debug port open (23456) - will not allow connections until debugger connects
#java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=23456 -cp cs4400chatserver/src main.java.ChatServer $1




